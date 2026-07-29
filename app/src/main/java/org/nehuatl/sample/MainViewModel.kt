package org.nehuatl.sample

import android.app.Application
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.nehuatl.llamacpp.LlamaHelper
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import java.lang.ref.WeakReference
import java.util.zip.ZipInputStream

data class ChatMessage(val role: String, val text: String)

class MainViewModel(application: Application, val contentResolver: ContentResolver) : AndroidViewModel(application) {
    companion object {
        @Volatile var instance: MainViewModel? = null
        private const val TAG = "MainViewModel"
        private const val REMEMBER_COMMAND = "запомни"
        private const val REMEMBER_FULL_COMMAND = "сделай выводы и запомни"
        private const val REMEMBER_ANALYZE_COMMAND = "проанализируй и запомни"
        private const val FIND_COMMAND = "найди"
        private const val SEARCH_COMMAND = "поищи"
        private const val RECALL_COMMAND = "вспомни"
        private const val ALARM_COMMAND = "будильник"
        private const val REMIND_COMMAND = "напомни"
        private const val AUTO_SEND_DELAY = 5000L
        // FIXED: Correct URL
        private const val VOSK_MODEL_URL = "http://alphacephei.com"
        private const val VOSK_MODEL_DIR = "vosk-model-large"
        private const val VOSK_MODEL_READY_FLAG = "is_vosk_large_ready"
    }

    private val viewModelJob = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + viewModelJob)

    // === Локальный ИИ ===
    private val _llmFlow = MutableSharedFlow<LlamaHelper.LLMEvent>(
        replay = 0,
        extraBufferCapacity = 64,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val llmFlow: SharedFlow<LlamaHelper.LLMEvent> = _llmFlow.asSharedFlow()

    private val _state = MutableStateFlow<GenerationState>(GenerationState.Idle)
    val state = _state.asStateFlow()

    private val _generatedText = MutableStateFlow("")
    val generatedText = _generatedText.asStateFlow()

    private val _isModelLoaded = MutableStateFlow(false)
    val isModelLoaded: MutableStateFlow<Boolean> = _isModelLoaded

    // === Облачный ИИ ===
    private val _cloudState = MutableStateFlow<CloudAIState>(CloudAIState.Idle)
    val cloudState = _cloudState.asStateFlow()

    private val _cloudGeneratedText = MutableStateFlow("")
    val cloudGeneratedText = _cloudGeneratedText.asStateFlow()

    private val _cloudFlow = MutableSharedFlow<CloudAIEvent>(
        replay = 0,
        extraBufferCapacity = 64,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val cloudFlow: SharedFlow<CloudAIEvent> = _cloudFlow.asSharedFlow()

    private val cloudPreferences: android.content.SharedPreferences by lazy {
        getApplication<Application>().getSharedPreferences("cloud_ai", Context.MODE_PRIVATE)
    }

    private val cloudAIProvider by lazy {
        CloudAIProvider(
            context = getApplication(),
            scope = scope,
            sharedFlow = _cloudFlow,
            preferences = cloudPreferences
        )
    }

    private var currentModelName: String = ""

    private val _systemPrompt = MutableStateFlow("Ты — полезный, умный и лаконичный ИИ-ассистент. Отвечай строго на русском языке.")
    val systemPrompt = _systemPrompt.asStateFlow()

    private val _chatHistory = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatHistory = _chatHistory.asStateFlow()

    val temperature = MutableStateFlow(0.3f)
    val contextSize = MutableStateFlow(2048)
    val maxTokens = MutableStateFlow(512)

    private val memoryFile: File by lazy {
        File(getApplication<Application>().filesDir, "memory.txt")
    }

    private val brainFile: File by lazy {
        File(getApplication<Application>().filesDir, "brain.txt")
    }

    private var tts: TextToSpeech? = null

    private val alarmManager by lazy {
        getApplication<Application>().getSystemService(Context.ALARM_SERVICE) as AlarmManager
    }

    // === Текущий режим ИИ ===
    private val _currentMode = MutableStateFlow(AIMode.NEUTRAL)
    val currentMode = _currentMode.asStateFlow()

    fun setCurrentMode(mode: AIMode) {
        _currentMode.value = mode
        if (mode == AIMode.NEUTRAL) {
            stopRecording()
        }
    }

    // === Vosk ===
    private val _isRecording = MutableStateFlow(false)
    val isRecording = _isRecording.asStateFlow()

    private val _recognizedText = MutableStateFlow("")
    val recognizedText = _recognizedText.asStateFlow()

    private var autoSendJob: Job? = null
    private var voskRecognizer: VoskRecognizer? = null

    // Реактивный поток состояния Vosk
    private val _isVoskLoaded = MutableStateFlow(false)
    val isVoskLoaded = _isVoskLoaded.asStateFlow()

    // === RAM Memory Info State Flow ===
    private val _memoryInfoText = MutableStateFlow("Всего доступно: 0.0 ГБ / Занято: 0.0 ГБ")
    val memoryInfoText: StateFlow<String> = _memoryInfoText.asStateFlow()

    // === Vosk File Picker Event Flow ===
    private val _voskFilePickerEvent = MutableSharedFlow<Unit>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val voskFilePickerEvent = _voskFilePickerEvent.asSharedFlow()

    private val onVoiceResult: (String) -> Unit = { recognizedText ->
        if (recognizedText.isNotEmpty()) {
            _recognizedText.value = recognizedText
            appendSystemMessage("🎤 Распознано: $recognizedText")
            autoSendJob?.cancel()
            autoSendJob = scope.launch {
                delay(AUTO_SEND_DELAY)
                val textToSend = _recognizedText.value
                if (textToSend.isNotEmpty()) {
                    appendSystemMessage("⏱ Автоотправка через ${AUTO_SEND_DELAY/1000} сек")
                    sendUserMessage(textToSend)
                    _recognizedText.value = ""
                }
            }
        }
    }

    private val onVoiceLog: (String) -> Unit = { logText ->
        Log.d("VoskLog", logText)
    }

    init {
        instance = this

        tts = TextToSpeech(getApplication()) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale("ru")
                Log.d(TAG, "TTS инициализирован успешно")
            } else {
                Log.e(TAG, "Ошибка инициализации TTS")
            }
        }

        scope.launch {
            _cloudFlow.collect { event ->
                when (event) {
                    is CloudAIEvent.Started -> {
                        _cloudGeneratedText.value = ""
                        _cloudState.value = CloudAIState.Generating(
                            prompt = event.prompt,
                            startTime = System.currentTimeMillis(),
                            tokensGenerated = 0
                        )
                    }
                    is CloudAIEvent.Ongoing -> {
                        _cloudGeneratedText.value = event.text
                        val currentState = _cloudState.value
                        if (currentState is CloudAIState.Generating) {
                            _cloudState.value = currentState.copy(tokensGenerated = event.tokenCount)
                        }
                    }
                    is CloudAIEvent.Done -> {
                        _cloudState.value = CloudAIState.Completed(event.tokenCount, event.duration)
                        val fullText = event.fullText
                        if (fullText.isNotEmpty()) {
                            stopRecording()
                            // Speak the complete text once before typewriter
                            speakText(fullText)
                            // Add empty assistant message and type it out
                            _chatHistory.value = _chatHistory.value + ChatMessage("assistant", "")
                            scope.launch(Dispatchers.Main) {
                                var typedText = ""
                                for (char in fullText) {
                                    typedText += char
                                    val currentList = _chatHistory.value.toMutableList()
                                    if (currentList.isNotEmpty() && currentList.last().role == "assistant") {
                                        currentList[currentList.lastIndex] = ChatMessage("assistant", typedText)
                                        _chatHistory.value = currentList
                                    }
                                    delay(30) // Typing speed
                                }
                            }
                        }
                        _cloudGeneratedText.value = fullText
                    }
                    is CloudAIEvent.Error -> {
                        _cloudState.value = CloudAIState.Error(event.message)
                        Log.e(TAG, "Ошибка облачного ИИ: ${event.message}")
                    }
                    is CloudAIEvent.TokenReceived -> {
                        val config = cloudAIProvider.getConfig()
                        if (config != null) {
                            _cloudState.value = CloudAIState.Ready(config.modelId)
                        }
                    }
                }
            }
        }

        scope.launch {
            _llmFlow.collect { event ->
                when (event) {
                    is LlamaHelper.LLMEvent.Started -> {
                        _state.value = GenerationState.Generating(prompt = event.prompt, tokensGenerated = 0)
                    }
                    is LlamaHelper.LLMEvent.Ongoing -> {
                        _generatedText.value += event.word
                        val currentState = _state.value
                        if (currentState is GenerationState.Generating) {
                            _state.value = currentState.copy(tokensGenerated = event.tokenCount)
                        }
                    }
                    is LlamaHelper.LLMEvent.Done -> {
                        _state.value = GenerationState.Completed(event.tokenCount, event.duration)
                        val fullText = event.fullText
                        if (fullText.isNotEmpty()) {
                            stopRecording()
                            // Speak the complete text once before typewriter
                            speakText(fullText)
                            // Add empty assistant message and type it out
                            _chatHistory.value = _chatHistory.value + ChatMessage("assistant", "")
                            scope.launch(Dispatchers.Main) {
                                var typedText = ""
                                for (char in fullText) {
                                    typedText += char
                                    val currentList = _chatHistory.value.toMutableList()
                                    if (currentList.isNotEmpty() && currentList.last().role == "assistant") {
                                        currentList[currentList.lastIndex] = ChatMessage("assistant", typedText)
                                        _chatHistory.value = currentList
                                    }
                                    delay(30) // Typing speed
                                }
                            }
                        }
                        _generatedText.value = fullText
                    }
                    is LlamaHelper.LLMEvent.Error -> {
                        _state.value = GenerationState.Error(event.message)
                        Log.e(TAG, "Ошибка локального ИИ: ${event.message}")
                        _isModelLoaded.value = false
                    }
                    is LlamaHelper.LLMEvent.Loaded -> {
                        _state.value = GenerationState.ModelLoaded(event.path)
                        _isModelLoaded.value = true
                    }
                }
            }
        }

        // Запуск фоновой корутины для мониторинга RAM
        scope.launch(Dispatchers.Default) {
            val context = getApplication<Application>().applicationContext
            val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? android.app.ActivityManager
            val memoryInfo = android.app.ActivityManager.MemoryInfo()
            while (true) {
                if (activityManager != null) {
                    activityManager.getMemoryInfo(memoryInfo)
                    // Конвертируем байты в гигабайты (1 ГБ = 1024 * 1024 * 1024 байт)
                    val totalGb = memoryInfo.totalMem.toDouble() / (1024.0 * 1024.0 * 1024.0)
                    val availGb = memoryInfo.availMem.toDouble() / (1024.0 * 1024.0 * 1024.0)
                    val usedGb = totalGb - availGb
                    // Форматируем строку до одного знака после запятой
                    val formattedString = String.format(
                        java.util.Locale.US,
                        "Всего доступно %.1f ГБ / Занято %.1f ГБ",
                        totalGb,
                        usedGb
                    )
                    // Перенаправляем обновление стейта в поток данных
                    _memoryInfoText.value = formattedString
                }
                // Задержка ровно в 1 секунду (1000мс) перед следующим замером
                delay(1000)
            }
        }

        // Централизованный голосовой синтезатор для новых сообщений
        var lastSpokenMessageIndex = -1

        scope.launch {
            chatHistory.collect { historyList ->
                if (historyList.isNotEmpty()) {
                    val lastIndex = historyList.lastIndex
                    if (lastIndex > lastSpokenMessageIndex) {
                        val newlyArrivedItem = historyList.last()
                        lastSpokenMessageIndex = lastIndex

                        // Пропускаем assistant (уже обработано в Done) и системные индикаторы прогресса
                        val isTypewriterActive = newlyArrivedItem.role == "system" &&
                                (newlyArrivedItem.text.startsWith("⏳ Распаковка") ||
                                        newlyArrivedItem.text.startsWith("⚙️ Инициализация") ||
                                        newlyArrivedItem.text.startsWith("📥 Загрузка") ||
                                        newlyArrivedItem.text.startsWith("📦") ||
                                        newlyArrivedItem.text.startsWith("🧹"))

                        val isAssistant = newlyArrivedItem.role == "assistant"

                        if (newlyArrivedItem.text.isNotEmpty() && !isTypewriterActive && !isAssistant) {
                            speakText(newlyArrivedItem.text)
                        }
                    }
                } else {
                    lastSpokenMessageIndex = -1 // Полный сброс индекса при очистке чата
                }
            }
        }

        // Vosk теперь инициализируется лениво, при первом нажатии на микрофон
    }

    private val llamaHelper by lazy {
        LlamaHelper(
            contentResolver = contentResolver,
            scope = scope,
            sharedFlow = _llmFlow,
        )
    }

    private fun updateLastSystemMessage(newText: String) {
        val currentList = _chatHistory.value.toMutableList()
        if (currentList.isNotEmpty() && currentList.last().role == "system") {
            currentList[currentList.lastIndex] = ChatMessage("system", newText)
            _chatHistory.value = currentList
        } else {
            _chatHistory.value = currentList + ChatMessage("system", newText)
        }
    }

    fun isVoskInitialized(): Boolean = _isVoskLoaded.value

    fun initVoskLazily(context: Context) {
        if (_isVoskLoaded.value) {
            return
        }

        val prefs = context.getSharedPreferences("cloud_ai", Context.MODE_PRIVATE)
        val isReady = prefs.getBoolean(VOSK_MODEL_READY_FLAG, false)
        val targetDir = File(context.filesDir, VOSK_MODEL_DIR)

        if (!isReady) {
            appendSystemMessage("⏳ Высокоточная голосовая модель не найдена. Начинаю безопасную загрузку (~1.2 ГБ)... Пожалуйста, не закрывайте приложение.")
            scope.launch {
                withContext(Dispatchers.IO) {
                    var tempFile: File? = null
                    try {
                        val url = URL(VOSK_MODEL_URL)
                        val connection = url.openConnection() as HttpURLConnection
                        connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                        connection.connectTimeout = 15000
                        connection.readTimeout = 15000
                        connection.connect()

                        val fileLength = connection.contentLength
                        val inputStream = connection.inputStream
                        tempFile = File(context.cacheDir, "vosk-model-temp.zip")
                        tempFile?.parentFile?.let {
                            if (!it.exists()) it.mkdirs()
                        }
                        try {
                            tempFile?.createNewFile()
                        } catch (ioe: java.io.IOException) {
                            Log.e(TAG, "Не удалось создать временный файл: " + ioe.message)
                        }

                        withContext(Dispatchers.Main) {
                            appendSystemMessage("📥 Начинаю загрузку модели...")
                        }

                        // Скачивание ZIP-архива с прогрессом
                        FileOutputStream(tempFile).use { output ->
                            val buffer = ByteArray(8192)
                            var bytesRead: Int
                            var totalBytesRead = 0L
                            var lastLoggedPercent = -1

                            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                                output.write(buffer, 0, bytesRead)
                                totalBytesRead += bytesRead

                                if (fileLength > 0) {
                                    val progressPercent = (totalBytesRead * 100 / fileLength).toInt()
                                    if (progressPercent % 5 == 0 && progressPercent != lastLoggedPercent) {
                                        lastLoggedPercent = progressPercent
                                        withContext(Dispatchers.Main) {
                                            appendSystemMessage("📥 Загрузка голосового движка: $progressPercent%")
                                        }
                                    }
                                } else {
                                    val currentMB = totalBytesRead / (1024 * 1024)
                                    if (currentMB % 25 == 0L && currentMB > 0) {
                                        withContext(Dispatchers.Main) {
                                            appendSystemMessage("📥 Загружено: $currentMB МБ")
                                        }
                                    }
                                }
                            }
                        }

                        // Single clear message before unzip starts
                        withContext(Dispatchers.Main) {
                            appendSystemMessage("⏳ Начинаю распаковку голосового движка Vosk... Это займет около 30-40 секунд, пожалуйста, подождите.")
                        }

                        // Распаковка архива - with 250-file progress tracking and in-place updates
                        targetDir.mkdirs()
                        ZipInputStream(tempFile.inputStream()).use { zis ->
                            var entry = zis.nextEntry
                            var entryCounter = 0
                            while (entry != null) {
                                entryCounter++
                                if (entryCounter % 250 == 0) {
                                    withContext(Dispatchers.Main) {
                                        updateLastSystemMessage("⏳ Распаковка архива (разворачивание 3.7 ГБ на диске): извлечено $entryCounter файлов...")
                                    }
                                }
                                val entryFile = File(targetDir, entry.name)
                                if (entry.isDirectory) {
                                    entryFile.mkdirs()
                                } else {
                                    entryFile.parentFile?.mkdirs()
                                    FileOutputStream(entryFile).use { fos ->
                                        val buffer = ByteArray(8192)
                                        var len: Int
                                        while (zis.read(buffer).also { len = it } != -1) {
                                            fos.write(buffer, 0, len)
                                        }
                                    }
                                }
                                zis.closeEntry()
                                entry = zis.nextEntry
                            }
                        }
                        withContext(Dispatchers.Main) {
                            updateLastSystemMessage("📦 Всего распаковано файлов: $entryCounter")
                        }

                    } catch (e: Exception) {
                        Log.e(TAG, "Ошибка загрузки/распаковки модели Vosk: ${e.message}")
                        // Network failure - trigger offline mode with file picker
                        withContext(Dispatchers.Main) {
                            appendSystemMessage("⚠ Сеть заблокирована или отсутствует подключение к интернету. Перехожу в автономный режим.")
                            appendSystemMessage("📂 Пожалуйста, выберите скачанный архив 'vosk-model-ru-0.42.zip' из памяти вашего телефона или флешки.")
                            _voskFilePickerEvent.tryEmit(Unit)
                        }
                    } finally {
                        // 1. File Purge
                        tempFile?.let {
                            if (it.exists()) {
                                it.delete()
                                Log.d(TAG, "Временный ZIP-файл успешно удален")
                            }
                        }
                        // 2. State Commit
                        prefs.edit().putBoolean(VOSK_MODEL_READY_FLAG, true).apply()
                        // 3. Completion Notice on Main thread
                        withContext(Dispatchers.Main) {
                            appendSystemMessage("✅ Голосовой движок успешно настроен, архив очищен!")
                        }
                        // 4. RAM Memory Allocation (synchronous flow) with OOM protection
                        initializeVoskModel(getApplication<Application>().applicationContext)
                    }
                }
            }
        } else {
            // Модель уже готова - инициализируем синхронно
            scope.launch {
                initializeVoskModel(getApplication<Application>().applicationContext)
            }
        }
    }

    // === Offline Unzipping Routine ===
    fun processLocalVoskZip(uri: Uri, context: Context) {
        if (_isVoskLoaded.value) {
            return
        }

        val prefs = context.getSharedPreferences("cloud_ai", Context.MODE_PRIVATE)
        val targetDir = File(context.filesDir, VOSK_MODEL_DIR)

        scope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Main) {
                appendSystemMessage("⏳ Начинаю локальную распаковку архива с диска... Пожалуйста, подождите.")
            }

            try {
                // Open secure input stream from content resolver
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    // Create target directory if it doesn't exist
                    targetDir.mkdirs()

                    // Extract archive with 250-file progress tracking and in-place updates
                    ZipInputStream(inputStream).use { zis ->
                        var entry = zis.nextEntry
                        var entryCounter = 0
                        while (entry != null) {
                            entryCounter++
                            if (entryCounter % 250 == 0) {
                                withContext(Dispatchers.Main) {
                                    updateLastSystemMessage("⏳ Распаковка архива (разворачивание 3.7 ГБ на диске): извлечено $entryCounter файлов...")
                                }
                            }
                            val entryFile = File(targetDir, entry.name)
                            if (entry.isDirectory) {
                                entryFile.mkdirs()
                            } else {
                                entryFile.parentFile?.mkdirs()
                                FileOutputStream(entryFile).use { fos ->
                                    val buffer = ByteArray(8192)
                                    var len: Int
                                    while (zis.read(buffer).also { len = it } != -1) {
                                        fos.write(buffer, 0, len)
                                    }
                                }
                            }
                            zis.closeEntry()
                            entry = zis.nextEntry
                        }
                    }
                    withContext(Dispatchers.Main) {
                        updateLastSystemMessage("📦 Всего распаковано файлов: $entryCounter")
                    }
                } ?: run {
                    withContext(Dispatchers.Main) {
                        appendSystemMessage("⚠️ Не удалось открыть выбранный файл. Убедитесь, что это корректный ZIP-архив.")
                    }
                    return@launch
                }
            } catch (e: Exception) {
                Log.e(TAG, "Ошибка локальной распаковки Vosk: ${e.message}")
                withContext(Dispatchers.Main) {
                    appendSystemMessage("⚠️ Ошибка при распаковке архива: ${e.message}")
                }
            } finally {
                // Commit state flag
                prefs.edit().putBoolean(VOSK_MODEL_READY_FLAG, true).apply()
                withContext(Dispatchers.Main) {
                    appendSystemMessage("✅ Голосовой движок успешно настроен автономно!")
                }
                // Initialize Vosk model with OOM protection
                initializeVoskModel(context.applicationContext)
            }
        }
    }

    private suspend fun initializeVoskModel(context: Context) {
        var ramProgressJob: Job? = null

        try {
            val targetDir = File(context.filesDir, VOSK_MODEL_DIR)

            // Intelligent path detection: find the directory containing the "am" folder
            val modelPath = findModelPath(targetDir)

            // Validate that the model directory actually exists
            if (modelPath == null || !File(modelPath).exists()) {
                withContext(Dispatchers.Main) {
                    appendSystemMessage("⚠️ Модель не найдена. Попробуйте перезапустить приложение.")
                }
                return
            }

            // OOM Protection: Clear memory before initializing VoskRecognizer
            withContext(Dispatchers.Main) {
                updateLastSystemMessage("🧹 Очистка памяти перед загрузкой Vosk...")
            }
            Log.d(TAG, "OOM Protection: Running GC before VoskRecognizer init")
            System.gc()
            Runtime.getRuntime().gc()
            delay(200)

            // Start RAM loading animation
            ramProgressJob = scope.launch(Dispatchers.Main) {
                var dots = 1
                while (true) {
                    updateLastSystemMessage("⚙️ Инициализация ядра Vosk... Загрузка весов модели в ОЗУ" + ".".repeat(dots))
                    delay(600)
                    dots = (dots % 3) + 1
                }
            }

            // Wrap VoskRecognizer initialization in try-catch for OutOfMemoryError
            try {
                voskRecognizer = VoskRecognizer(
                    contextRef = WeakReference(context.applicationContext),
                    onResult = onVoiceResult,
                    onLog = onVoiceLog,
                    scope = scope,
                    externalModelPath = modelPath
                )

                // Cancel RAM loading animation on success
                ramProgressJob?.cancel()
                ramProgressJob = null

                withContext(Dispatchers.Main) {
                    _isVoskLoaded.value = true
                    appendSystemMessage("✅ Голосовой движок Vosk полностью загружен и готов в ОЗУ!")
                }
            } catch (t: Throwable) {
                // Cancel RAM loading animation on error
                ramProgressJob?.cancel()
                ramProgressJob = null

                if (t is OutOfMemoryError || t.message?.contains("OutOfMemoryError") == true) {
                    Log.e(TAG, "OutOfMemoryError при инициализации Vosk", t)
                    withContext(Dispatchers.Main) {
                        appendSystemMessage("⚠️ Недостаточно оперативной памяти для загрузки голосовой модели. Попробуйте освободить память или перезапустить приложение.")
                        appendSystemMessage("💡 Для работы Vosk требуется ~500 МБ свободной RAM.")
                    }
                } else {
                    Log.e(TAG, "Ошибка инициализации Vosk: ${t.message}", t)
                    withContext(Dispatchers.Main) {
                        appendSystemMessage("⚠️ Ошибка инициализации Vosk: ${t.message}")
                    }
                }
                // Re-throw to allow caller to handle, but we already logged and notified user
                throw t
            }

        } catch (e: Exception) {
            // Cancel RAM loading animation if still running
            ramProgressJob?.cancel()
            ramProgressJob = null

            Log.e(TAG, "Ошибка инициализации Vosk: ${e.message}")
            withContext(Dispatchers.Main) {
                appendSystemMessage("⚠️ Ошибка инициализации Vosk: ${e.message}")
            }
        } finally {
            // Ensure RAM loading animation is cancelled in all cases
            ramProgressJob?.cancel()
            ramProgressJob = null
        }
    }

    private fun findModelPath(targetDir: File): String? {
        // First priority: Check for nested structure: vosk-model-ru-0.42/am
        val nestedDir = File(targetDir, "vosk-model-ru-0.42")
        if (nestedDir.exists() && nestedDir.isDirectory && File(nestedDir, "am").exists()) {
            return nestedDir.absolutePath
        }

        // Check if targetDir itself contains the "am" folder (flattened extraction)
        if (File(targetDir, "am").exists()) {
            return targetDir.absolutePath
        }

        // Check for any subdirectory that contains "am"
        val subDirs = targetDir.listFiles { file -> file.isDirectory }
        if (subDirs != null) {
            for (subDir in subDirs) {
                if (File(subDir, "am").exists()) {
                    return subDir.absolutePath
                }
            }
        }

        // Fallback: return targetDir if it exists
        return if (targetDir.exists()) targetDir.absolutePath else null
    }

    // === НОВЫЙ МЕТОД: Полная выгрузка Vosk из памяти ===
    fun releaseVosk() {
        try {
            if (_isRecording.value) {
                stopRecording()
            }
            voskRecognizer?.release()
            voskRecognizer = null
            _isVoskLoaded.value = false
            appendSystemMessage("🔄 Голосовой движок Vosk полностью выгружен из памяти")
            Log.d(TAG, "Vosk модель успешно освобождена из ОЗУ")
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка при выгрузке Vosk: ${e.message}")
            appendSystemMessage("⚠ Ошибка при выгрузке голосового движка")
        }
    }

    // === Отправка сообщений ===
    fun sendUserMessage(text: String) {
        if (text.isBlank()) return

        _chatHistory.value = _chatHistory.value + ChatMessage("user", text)

        // Обработка локальных команд в любом режиме
        val lowerText = text.lowercase()
        when {
            lowerText.contains(REMEMBER_COMMAND) -> {
                val cleanText = text.substringAfter(REMEMBER_COMMAND).trim()
                if (cleanText.isNotEmpty()) {
                    saveToLongTermMemory(cleanText)
                } else {
                    appendSystemMessage("⚠️ Что именно мне нужно запомнить?")
                }
                return
            }
            lowerText.contains(ALARM_COMMAND) || lowerText.contains(REMIND_COMMAND) -> {
                handleAlarmCommand(text)
                return
            }
            lowerText.contains(RECALL_COMMAND) || lowerText.contains(FIND_COMMAND) || lowerText.contains(SEARCH_COMMAND) -> {
                // Для NEUTRAL режима выполняем локальный поиск и выводим результат
                if (_currentMode.value == AIMode.NEUTRAL) {
                    val searchResult = buildSystemPrompt(true, text)
                    val memoryData = readFromLongTermMemory()
                    if (memoryData.isNotEmpty()) {
                        val cleanSearchQuery = text.lowercase()
                            .replace(RECALL_COMMAND, "")
                            .replace(FIND_COMMAND, "")
                            .replace(SEARCH_COMMAND, "")
                            .trim()

                        val keywords = cleanSearchQuery.split(" ")
                            .map { it.trim() }
                            .filter { it.length > 2 }
                            .map { if (it.length > 4) it.substring(0, 4) else it }

                        val filteredLines = memoryData.split("\n")
                            .filter { line ->
                                val lowerLine = line.lowercase()
                                keywords.any { keyword -> lowerLine.contains(keyword) }
                            }
                            .joinToString("\n")

                        if (filteredLines.isNotEmpty()) {
                            appendSystemMessage("🔍 Найдено в памяти:\n$filteredLines")
                        } else {
                            appendSystemMessage("🔍 Ничего не найдено по запросу '$cleanSearchQuery'")
                        }
                    } else {
                        appendSystemMessage("🔍 База знаний пуста. Сохраните что-нибудь через 'запомни'")
                    }
                    return
                }
                // Для LOCAL/CLOUD режимов пропускаем блок и передаем управление дальше для обработки LLM
            }
        }

        // Обычная обработка режимов
        when (_currentMode.value) {
            AIMode.LOCAL -> {
                if (_isModelLoaded.value) {
                    generateLocal(text, null)
                } else {
                    appendSystemMessage("⚠️ Локальная модель не загружена. Загрузите модель через 'движок'.")
                }
            }
            AIMode.CLOUD -> {
                if (isCloudConfigured()) {
                    generateCloud(text)
                } else {
                    appendSystemMessage("⚠️ Облачный ИИ не настроен. Настройте через 'облачный ии'.")
                }
            }
            AIMode.NEUTRAL -> {
                appendSystemMessage("⚠️ Выберите режим работы: локальный или облачный ИИ")
            }
        }
    }

    // === Методы для Vosk ===
    fun startRecording() {
        if (_isRecording.value) return
        _recognizedText.value = ""
        appendSystemMessage("🎤 Запуск записи...")
        voskRecognizer?.startRecording()
        _isRecording.value = true
    }

    fun stopRecording() {
        if (!_isRecording.value) return
        appendSystemMessage("⏹ Остановка записи")
        voskRecognizer?.stopRecording()
        _isRecording.value = false
        autoSendJob?.cancel()
        val textToSend = _recognizedText.value
        if (textToSend.isNotEmpty()) {
            appendSystemMessage("📤 Отправка текста")
            sendUserMessage(textToSend)
            _recognizedText.value = ""
        }
    }

    // === Методы для облачного ИИ ===
    fun isCloudConfigured(): Boolean = cloudAIProvider.isConfigured()

    fun getCloudConfig(): CloudAIConfig? = cloudAIProvider.getConfig()

    fun saveCloudConfig(config: CloudAIConfig) {
        cloudAIProvider.saveConfig(config)
        if (config.isValid()) {
            _cloudState.value = CloudAIState.Ready(config.modelId)
        } else {
            _cloudState.value = CloudAIState.Idle
        }
    }

    fun clearCloudConfig() {
        cloudAIProvider.clearConfig()
        _cloudState.value = CloudAIState.Idle
        _cloudGeneratedText.value = ""
        appendSystemMessage("🔄 Настройки облачного ИИ успешно сброшены")
    }

    fun generateCloudToken(callback: (Boolean) -> Unit) {
        scope.launch {
            val success = cloudAIProvider.generateToken()
            callback(success)
        }
    }

    fun generateCloud(prompt: String) {
        val lowerPrompt = prompt.trim().lowercase()

        if (lowerPrompt.startsWith(REMEMBER_COMMAND)) {
            val cleanText = prompt.substringAfter(REMEMBER_COMMAND).trim()
            if (cleanText.isNotEmpty()) {
                saveToLongTermMemory(cleanText)
            } else {
                appendSystemMessage("⚠️ Что именно мне нужно запомнить?")
            }
            return
        }

        if (prompt.lowercase().contains(ALARM_COMMAND) || prompt.lowercase().contains(REMIND_COMMAND)) {
            handleAlarmCommand(prompt)
            return
        }

        if (!cloudAIProvider.isConfigured()) {
            _cloudState.value = CloudAIState.Error("Облачный ИИ не настроен")
            return
        }

        val isSearchCommand = prompt.contains(FIND_COMMAND, ignoreCase = true) ||
                prompt.contains(SEARCH_COMMAND, ignoreCase = true) ||
                prompt.contains(RECALL_COMMAND, ignoreCase = true)

        val fullSystemPrompt = buildSystemPrompt(isSearchCommand, prompt)
        val cloudHistory = _chatHistory.value

        cloudAIProvider.generate(
            prompt = prompt,
            systemPrompt = fullSystemPrompt,
            chatHistory = cloudHistory,
            maxTokens = maxTokens.value
        )
    }

    fun abortCloud() {
        cloudAIProvider.abort()
        _cloudState.value = CloudAIState.Idle
    }

    // === Методы для локального ИИ ===
    fun loadModel(path: String, mmprojPath: String? = null) {
        if (path.isEmpty()) return
        _state.value = GenerationState.LoadingModel
        _isModelLoaded.value = false

        scope.launch {
            try {
                llamaHelper.load(
                    path,
                    contextSize.value,
                    if (mmprojPath.isNullOrEmpty()) null else mmprojPath,
                    { id ->
                        _state.value = GenerationState.ModelLoaded(path)
                        _isModelLoaded.value = true
                        val uri = Uri.parse(path)
                        currentModelName = getFileNameFromUri(contentResolver, uri)
                    }
                )
            } catch (e: Exception) {
                _state.value = GenerationState.Error(e.message ?: "Unknown error")
                _isModelLoaded.value = false
            }
        }
    }

    fun generateLocal(prompt: String, imagePath: String? = null) {
        val lowerPrompt = prompt.trim().lowercase()

        if (lowerPrompt.startsWith(REMEMBER_COMMAND)) {
            val cleanText = prompt.substringAfter(REMEMBER_COMMAND).trim()
            if (cleanText.isNotEmpty()) {
                saveToLongTermMemory(cleanText)
            } else {
                appendSystemMessage("⚠️ Что именно мне нужно запомнить?")
            }
            return
        }

        if (prompt.lowercase().contains(ALARM_COMMAND) || prompt.lowercase().contains(REMIND_COMMAND)) {
            handleAlarmCommand(prompt)
            return
        }

        if (llamaHelper.getContextId() == null) {
            _state.value = GenerationState.Error("Модель не загружена. Загрузите модель через 'движок'.")
            return        }

        val isSearchCommand = prompt.contains(FIND_COMMAND, ignoreCase = true) ||
                prompt.contains(SEARCH_COMMAND, ignoreCase = true) ||
                prompt.contains(RECALL_COMMAND, ignoreCase = true)

        val fullSystemPrompt = buildSystemPrompt(isSearchCommand, prompt)
        _generatedText.value = ""
        _state.value = GenerationState.Generating(prompt = prompt, tokensGenerated = 0)

        scope.launch {
            try {
                llamaHelper.predict(prompt, imagePath, fullSystemPrompt, maxTokens.value)
            } catch (e: Exception) {
                _state.value = GenerationState.Error(e.message ?: "Unknown error")
                _isModelLoaded.value = false
            }
        }
    }

    private fun buildSystemPrompt(isSearchCommand: Boolean, prompt: String): String {
        val basePrompt = _systemPrompt.value
        if (!isSearchCommand) {
            return basePrompt
        }

        val brainData = readBrain()
        val chatHistory = _chatHistory.value

        // Извлечение ключевых слов из запроса для поиска в памяти
        val cleanSearchQuery = prompt.lowercase()
            .replace("вспомни", "")
            .replace("найди", "")
            .replace("поищи", "")
            .trim()

        val keywords = cleanSearchQuery.split(" ")
            .map { it.trim() }
            .filter { it.length > 2 }
            .flatMap { word ->
                // Извлекаем корни слов для более широкого поиска
                val roots = mutableListOf<String>()
                if (word.length > 4) {
                    roots.add(word.substring(0, 4))
                }
                roots.add(word)
                roots
            }
            .distinct()

        val fullMemory = readFromLongTermMemory()

        val filteredMemory = if (fullMemory.isNotEmpty() && keywords.isNotEmpty()) {
            fullMemory.split("\n")
                .filter { line ->
                    val lowerLine = line.lowercase()
                    keywords.any { keyword -> lowerLine.contains(keyword) }
                }
                .joinToString("\n")
        } else {
            ""
        }

        return buildString {
            append(basePrompt)
            append("\n\n")

            if (filteredMemory.isNotEmpty()) {
                append("ЛОКАЛЬНАЯ БАЗА ЗНАНИЙ (НАЙДЕННЫЕ ФАКТЫ):\n$filteredMemory\n\n")
            }

            if (brainData.isNotEmpty()) {
                append("КРАТКИЕ ВЫВОДЫ ИЗ ПРОШЛЫХ РАЗГОВОРОВ (МОЗГ):\n$brainData\n\n")
            }

            if (chatHistory.isNotEmpty()) {
                append("ИСТОРИЯ ЧАТА (ВЕСЬ ДИАЛОГ):\n")
                chatHistory.forEach { message ->
                    val prefix = if (message.role == "user") "Пользователь" else "Ассистент"
                    append("$prefix: ${message.text}\n")
                }
                append("\n")
            }

            append("Пользователь просит тебя НАЙТИ ИЛИ ВСПОМНИТЬ информацию из его личной базы знаний, а также ВЫПОЛНИТЬ МАТЕМАТИЧЕСКИЙ ИЛИ ЛОГИЧЕСКИЙ РАСЧЕТ на основе найденных фактов. Внимательно изучи предоставленные строки ЛОКАЛЬНОЙ БАЗЫ ЗНАНИЙ. Если там указана цена, тариф или условие (например, цена плитки за квадратный метр), используй эти точные цифры для выполнения математического действия, запрошенного пользователем (например, умножь площадь на стоимость). Дай развернутый, понятный и дружелюбный ответ с демонстрацией хода вычислений. Если нужных данных в памяти нет — честно скажи об этом.")
        }
    }

    private fun handleAlarmCommand(prompt: String) {
        val timePattern = Regex("(?:в|в\\s+|напомни\\s+в\\s+)(\\d{1,2}[:.]\\d{2})")
        val match = timePattern.find(prompt)

        if (match != null) {
            val timeStr = match.groupValues[1].replace(".", ":")
            val message = prompt.replace(Regex("(?:в\\s+|напомни\\s+в\\s+)\\d{1,2}[:.]\\d{2}\\s*"), "").trim()

            // Безопасная проверка разрешения на точные будильники для Android 12+
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                val context = getApplication<Application>().applicationContext
                val alarmManagerSystem = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
                if (alarmManagerSystem?.canScheduleExactAlarms() == false) {
                    appendSystemMessage("⚠️ Для установки точных напоминаний ИИ-Другу требуется специальное разрешение. Пожалуйста, включите тумблер в открывшихся настройках.")
                    try {
                        val intent = Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        appendSystemMessage("❌ Не удалось автоматически открыть системные настройки разрешений: ${e.message}")
                    }
                    return // Мгновенно прерываем поток выполнения, предотвращая вызов setAlarm и краш процесса
                }
            }

            setAlarm(timeStr, message)
            appendSystemMessage("⏰ Будильник установлен на $timeStr: '$message'")
        } else {
            appendSystemMessage("⚠️ Не удалось распознать время. Используйте формат: 'в 18.00 идем в гараж'")
        }
    }

    private fun setAlarm(timeStr: String, message: String) {
        try {
            val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            val alarmTime = dateFormat.parse(timeStr)

            if (alarmTime == null) {
                Log.e(TAG, "Не удалось распарсить время: $timeStr")
                appendSystemMessage("⚠️ Не удалось распознать время: $timeStr")
                return
            }

            val calendar = Calendar.getInstance().apply {
                val timeCal = Calendar.getInstance().apply { time = alarmTime }
                set(Calendar.HOUR_OF_DAY, timeCal.get(Calendar.HOUR_OF_DAY))
                set(Calendar.MINUTE, timeCal.get(Calendar.MINUTE))
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)

                if (timeInMillis < System.currentTimeMillis()) {
                    add(Calendar.DAY_OF_YEAR, 1)
                }
            }

            val intent = Intent(getApplication(), AlarmReceiver::class.java).apply {
                putExtra("message", message)
            }

            val pendingIntent = PendingIntent.getBroadcast(
                getApplication(),
                System.currentTimeMillis().toInt(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            )

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
            }

            Log.d(TAG, "Будильник установлен на ${calendar.time} ($timeStr): $message")

        } catch (e: Exception) {
            Log.e(TAG, "Ошибка установки будильника: ${e.message}")
            appendSystemMessage("⚠️ Ошибка установки будильника: ${e.message}")
        }
    }

    private fun getFullChatHistory(): String {
        return _chatHistory.value.joinToString("\n") { "${it.role}: ${it.text}" }
    }

    internal fun appendSystemMessage(text: String) {
        _chatHistory.value = _chatHistory.value + ChatMessage("system", text)
    }

    fun abortLocal() {
        if (_state.value.isActive()) {
            Log.i(TAG, "Aborting generation")
            tts?.stop()
            llamaHelper.abort()
        }
    }

    // === Работа с памятью ===
    fun saveToLongTermMemory(text: String) {
        try {
            if (!memoryFile.exists()) {
                memoryFile.createNewFile()
            }

            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            val timestamp = dateFormat.format(Date())

            memoryFile.appendText("[$timestamp] $text\n")
            Log.d(TAG, "Записано в долговременную память: $text")
            appendSystemMessage("🧠 Запомнено: $text")
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка записи памяти: ${e.message}")
        }
    }

    fun readFromLongTermMemory(): String {
        return try {
            if (memoryFile.exists()) {
                memoryFile.readText().trim()
            } else {
                ""
            }
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка чтения памяти: ${e.message}")
            ""
        }
    }

    fun overwriteLongTermMemory(newFullText: String) {
        try {
            if (!memoryFile.exists()) {
                memoryFile.createNewFile()
            }
            memoryFile.writeText(newFullText)
            Log.d(TAG, "База знаний успешно обновлена")
            appendSystemMessage("🧠 База знаний обновлена")
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка перезаписи базы знаний: ${e.message}")
        }
    }

    private fun saveBrain(text: String) {
        try {
            if (!brainFile.exists()) {
                brainFile.createNewFile()
            }

            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            val timestamp = dateFormat.format(Date())

            brainFile.appendText("[$timestamp] $text\n")
            Log.d(TAG, "Записано в мозг: $text")
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка записи мозга: ${e.message}")
        }
    }

    private fun readBrain(): String {
        return try {
            if (brainFile.exists()) {
                brainFile.readText().trim()
            } else {
                ""
            }
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка чтения мозга: ${e.message}")
            ""
        }
    }

    fun clearChat() {
        _chatHistory.value = emptyList()
        _generatedText.value = ""
        _cloudGeneratedText.value = ""
        _recognizedText.value = ""
        autoSendJob?.cancel()
        tts?.stop()
    }

    fun updateSystemPrompt(newPrompt: String) {
        _systemPrompt.value = newPrompt
    }

    fun updateTemperature(temp: Float) {
        temperature.value = temp.coerceIn(0.0f, 1.0f)
    }

    fun updateContextSize(size: Int) {
        contextSize.value = size.coerceAtLeast(512)
    }

    fun updateMaxTokens(tokens: Int) {
        maxTokens.value = tokens.coerceIn(1, 4096)
    }

    internal fun speakText(text: String) {
        val cleanText = text.replace(Regex("[*#`_]"), "")
        tts?.speak(cleanText, TextToSpeech.QUEUE_FLUSH, null, null)
        Log.d(TAG, "Озвучка запущена: ${cleanText.take(50)}...")
    }

    fun releaseModel() {
        _isModelLoaded.value = false
        llamaHelper.release()
    }

    override fun onCleared() {
        super.onCleared()
        instance = null
        tts?.stop()
        tts?.shutdown()
        _isModelLoaded.value = false
        llamaHelper.abort()
        llamaHelper.release()
        voskRecognizer?.release()
        autoSendJob?.cancel()
        viewModelJob.cancel()
    }
}

private fun getFileNameFromUri(contentResolver: ContentResolver, uri: Uri): String {
    var name = ""
    try {
        val cursor = contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val index = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                if (index != -1) name = it.getString(index)
            }
        }
    } catch (e: Exception) {
        Log.e("MainViewModel", "Ошибка чтения имени файла: ${e.message}")
    }

    if (name.isEmpty()) {
        name = uri.lastPathSegment ?: ""
    }

    val cleanName = name.replace(Regex("^primary%3AModels%"), "").replace(Regex("^primary:Models:"), "")
    return cleanName
}
