package org.nehuatl.sample

import android.app.Application
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
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
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import java.lang.ref.WeakReference
import java.util.zip.ZipInputStream
import kotlinx.coroutines.runBlocking
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

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
        private const val CHAT_LOOKUP_COMMAND = "посмотри в чате"
        private const val AUTO_SEND_DELAY = 5000L
        private const val AUTO_BRAIN_COMPRESSION_THRESHOLD = 14
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

    // === НОВЫЙ TTS и Speech Recognizer ===
    private var textToSpeech: TextToSpeech? = null
    private var isTtsEnabled = false
    private val _isTtsReady = MutableStateFlow(false)
    val isTtsReady: StateFlow<Boolean> = _isTtsReady.asStateFlow()

    // === НОВЫЙ Speech Recognizer (будет инициализироваться через Activity) ===
    private var speechRecognizerLauncher: ((String) -> Unit)? = null

    private val alarmManager by lazy {
        getApplication<Application>().getSystemService(Context.ALARM_SERVICE) as AlarmManager
    }

    // === Текущий режим ИИ ===
    private val _currentMode = MutableStateFlow(AIMode.NEUTRAL)
    val currentMode = _currentMode.asStateFlow()

    fun setCurrentMode(mode: AIMode) {
        _currentMode.value = mode
    }

    // === RAM Memory Info State Flow ===
    private val _memoryInfoText = MutableStateFlow("Всего доступно: 0.0 ГБ / Занято: 0.0 ГБ")
    val memoryInfoText: StateFlow<String> = _memoryInfoText.asStateFlow()

    // === Флаг загрузки TTS ===
    private var ttsInitJob: Job? = null

    private val llamaHelper by lazy {
        LlamaHelper(
            contentResolver = contentResolver,
            scope = scope,
            sharedFlow = _llmFlow,
        )
    }

    init {
        instance = this

        // Read local configuration maps and files safely
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (!memoryFile.exists()) memoryFile.createNewFile()
                if (!brainFile.exists()) brainFile.createNewFile()
                Log.d("LlamaViewModel", "Local storage files initialized successfully.")
            } catch (e: Exception) {
                Log.e("LlamaViewModel", "Failed to initialize local text memory files: ${e.message}")
            }
        }

        // НОВАЯ ИНИЦИАЛИЗАЦИЯ TTS
        initTts()

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

        // НЕТ централизованного голосового синтезатора — теперь озвучка управляется через speakText()
        // и вызывается явно из Done-событий
    }

    // === НОВЫЙ МЕТОД: Инициализация TTS ===
    private fun initTts() {
        ttsInitJob?.cancel()
        ttsInitJob = viewModelScope.launch {
            try {
                val context = getApplication<Application>()
                textToSpeech = TextToSpeech(context) { status ->
                    if (status == TextToSpeech.SUCCESS) {
                        val result = textToSpeech?.setLanguage(Locale("ru", "RU"))
                        if (result == TextToSpeech.LANG_AVAILABLE || result == TextToSpeech.LANG_COUNTRY_AVAILABLE) {
                            _isTtsReady.value = true
                            isTtsEnabled = true
                            appendSystemMessage("🟢 Голосовой движок Android TTS успешно инициализирован.")
                            // Приветственное сообщение
                            val welcomeText = "Привет! Я твоя локальная языковая модель. Голосовой движок полностью готов к работе, чем я могу помочь?"
                            appendSystemMessage(welcomeText)
                            speakText(welcomeText)
                            Log.d(TAG, "TTS initialized successfully with Russian language")
                        } else {
                            _isTtsReady.value = false
                            appendSystemMessage("⚠️ Русский язык не поддерживается TTS. Проверьте настройки.")
                            Log.w(TAG, "TTS init: Russian language not available")
                        }
                    } else {
                        _isTtsReady.value = false
                        appendSystemMessage("🔴 Ошибка инициализации TTS: $status")
                        Log.e(TAG, "TTS init failed with status: $status")
                    }
                }
                // Устанавливаем речь на русском языке (повторно, если первый вызов не сработал)
                textToSpeech?.setLanguage(Locale("ru", "RU"))
            } catch (e: Exception) {
                _isTtsReady.value = false
                appendSystemMessage("🔴 Ошибка инициализации TTS: ${e.message}")
                Log.e(TAG, "TTS init error: ${e.message}", e)
            }
        }
    }

    // === НОВЫЙ МЕТОД: Включение/отключение TTS ===
    fun enableTts() {
        if (!_isTtsReady.value) {
            appendSystemMessage("⚠️ TTS ещё не инициализирован. Попробуйте позже.")
            return
        }
        isTtsEnabled = true
        appendSystemMessage("🔊 Озвучка включена")
    }

    fun disableTts() {
        isTtsEnabled = false
        if (textToSpeech?.isSpeaking == true) {
            textToSpeech?.stop()
        }
        appendSystemMessage("🔇 Озвучка отключена")
    }

    // === НОВЫЙ МЕТОД: Озвучка текста ===
    fun speakText(text: String) {
        if (!_isTtsReady.value || !isTtsEnabled || text.isBlank()) {
            return
        }
        textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    // === НОВЫЙ МЕТОД: Запуск распознавания речи (через ActivityResultLauncher) ===
    fun startListening(onResult: (String) -> Unit) {
        if (speechRecognizerLauncher == null) {
            appendSystemMessage("⚠️ Распознавание речи не настроено. Передайте Launcher из Activity.")
            return
        }
        speechRecognizerLauncher?.invoke(onResult)
    }

    // === НОВЫЙ МЕТОД: Установка Launcher для распознавания речи ===
    fun setSpeechRecognizerLauncher(launcher: (String) -> Unit) {
        speechRecognizerLauncher = launcher
    }

    // === ОСТАВЛЯЕМ БЕЗ ИЗМЕНЕНИЙ (кроме удаления Vosk) ===

    /**
     * Extracts the stem from a Russian word by removing common suffixes.
     * Case-insensitive and handles common grammatical variations.
     */
    private fun extractRussianRoot(word: String): String {
        val lowerWord = word.lowercase()
        // List of common Russian suffixes to remove (longest first)
        val suffixes = listOf(
            "ами", "ые", "ой", "ых", "ого", "его", "ому", "ему", "им", "ым",
            "ая", "яя", "ое", "ее", "ие", "ые", "ий", "ый", "ой", "ей",
            "ам", "ям", "ом", "ем", "ах", "ях", "ов", "ев", "ин", "ын",
            "а", "я", "о", "е", "и", "ы", "у", "ю"
        )

        // Try to remove suffixes from the end
        var stem = lowerWord
        for (suffix in suffixes) {
            if (stem.endsWith(suffix) && stem.length > suffix.length + 1) {
                stem = stem.substring(0, stem.length - suffix.length)
                break
            }
        }

        // If the word is very short or no suffix was removed, return the original
        return if (stem.length < 2) lowerWord else stem
    }

    private fun triggerBackgroundDialogueCompression(history: List<ChatMessage>) {
        if (history.size < AUTO_BRAIN_COMPRESSION_THRESHOLD) return

        scope.launch(Dispatchers.IO) {
            try {
                // Build dialogue text for analysis
                val dialogueText = history.joinToString("\n") { message ->
                    val prefix = when (message.role) {
                        "user" -> "Пользователь"
                        "assistant" -> "Ассистент"
                        else -> "Система"
                    }
                    "$prefix: ${message.text}"
                }

                val prompt = "Проанализируй этот диалог. Выдели из него новые важные факты о личности, имени, привычках или планах Пользователя. Сформулируй краткие выводы тезисно, строго по одной строке на факт. Пиши только новые выводы, без лишних слов. Если новых данных нет, верни пустоту.\n\n$dialogueText"

                // Use local model if available, otherwise cloud
                if (_isModelLoaded.value && llamaHelper.getContextId() != null) {
                    llamaHelper.predict(
                        prompt = prompt,
                        imagePath = null,
                        systemPrompt = "Ты — умный аналитик. Выделяй только новые факты из диалога.",
                        maxTokens = 512
                    )
                } else if (cloudAIProvider.isConfigured()) {
                    cloudAIProvider.generate(
                        prompt = prompt,
                        systemPrompt = "Ты — умный аналитик. Выделяй только новые факты из диалога.",
                        chatHistory = history,
                        maxTokens = maxTokens.value
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Background compression failed: ${e.message}")
            }
        }
    }

    fun updateLastSystemMessage(newText: String) {
        val currentList = _chatHistory.value.toMutableList()
        if (currentList.isNotEmpty() && currentList.last().role == "system") {
            currentList[currentList.lastIndex] = ChatMessage("system", newText)
            _chatHistory.value = currentList
        } else {
            _chatHistory.value = currentList + ChatMessage("system", newText)
        }
    }

    // === Отправка сообщений ===
    fun sendUserMessage(text: String) {
        if (text.isBlank()) return

        _chatHistory.value = _chatHistory.value + ChatMessage("user", text)

        // Trigger background dialogue compression if threshold is met
        triggerBackgroundDialogueCompression(_chatHistory.value)

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
            lowerText.contains(RECALL_COMMAND) || lowerText.contains(FIND_COMMAND) || lowerText.contains(SEARCH_COMMAND) || lowerText.contains(CHAT_LOOKUP_COMMAND) -> {
                // Для NEUTRAL режима выполняем локальный поиск и выводим результат
                if (_currentMode.value == AIMode.NEUTRAL) {
                    val searchResult = buildSystemPrompt(true, text)
                    val memoryData = readFromLongTermMemory()
                    if (memoryData.isNotEmpty()) {
                        val cleanSearchQuery = text.lowercase()
                            .replace(RECALL_COMMAND, "")
                            .replace(FIND_COMMAND, "")
                            .replace(SEARCH_COMMAND, "")
                            .replace(CHAT_LOOKUP_COMMAND, "")
                            .trim()
                        // Используем интеллектуальный стемминг для ключевых слов
                        val keywords = cleanSearchQuery.split(" ")
                            .map { it.trim() }
                            .filter { it.length > 2 }
                            .map { extractRussianRoot(it) }
                            .distinct()
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
                prompt.contains(RECALL_COMMAND, ignoreCase = true) ||
                prompt.contains(CHAT_LOOKUP_COMMAND, ignoreCase = true)

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
            return
        }

        val isSearchCommand = prompt.contains(FIND_COMMAND, ignoreCase = true) ||
                prompt.contains(SEARCH_COMMAND, ignoreCase = true) ||
                prompt.contains(RECALL_COMMAND, ignoreCase = true) ||
                prompt.contains(CHAT_LOOKUP_COMMAND, ignoreCase = true)

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

        // Извлечение ключевых слов из запроса для поиска в памяти с использованием стемминга
        val cleanSearchQuery = prompt.lowercase()
            .replace("вспомни", "")
            .replace("найди", "")
            .replace("поищи", "")
            .replace("посмотри в чате", "")
            .trim()

        // Используем интеллектуальный стемминг для извлечения корней слов
        val keywords = cleanSearchQuery.split(" ")
            .map { it.trim() }
            .filter { it.length > 2 }
            .flatMap { word ->
                // Извлекаем корень слова с помощью стеммера
                val root = extractRussianRoot(word)
                mutableListOf(root).also {
                    // Добавляем оригинальное слово для более точного поиска
                    it.add(word)
                }
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

    fun releaseModel() {
        _isModelLoaded.value = false
        llamaHelper.release()
    }

    override fun onCleared() {
        super.onCleared()
        instance = null
        textToSpeech?.shutdown()
        textToSpeech = null
        _isTtsReady.value = false
        _isModelLoaded.value = false
        llamaHelper.abort()
        llamaHelper.release()
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
