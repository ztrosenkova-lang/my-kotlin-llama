package org.nehuatl.sample

import android.app.Application
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.Settings
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
import org.nehuatl.llamacpp.LlamaHelper
import java.io.File
import java.security.KeyStore
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.*
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyPairGenerator

// Модель данных для сообщений чата
data class ChatMessage(val role: String, val text: String)

class MainViewModel(application: Application, val contentResolver: ContentResolver) : AndroidViewModel(application) {
    companion object {
        @Volatile var instance: MainViewModel? = null
        private const val TAG = "MainViewModel"
        
        // Команды для работы с памятью
        private const val REMEMBER_COMMAND = "запомни"
        private const val FIND_COMMAND = "найди"
        private const val SEARCH_COMMAND = "поищи"
        private const val RECALL_COMMAND = "вспомни"
        private const val ALARM_COMMAND = "будильник"
        private const val REMIND_COMMAND = "напомни"
        private const val CHAT_LOOKUP_COMMAND = "посмотри в чате"
        
        // Порог для автоматической фоновой компрессии диалога
        private const val AUTO_BRAIN_COMPRESSION_THRESHOLD = 14
        
        // Секретная фраза и лимит времени для защиты
        private const val SECRET_PHRASE_HASH = "632f146be48ba42ca3406ef5a8ebca73df15aa2d5d8cb960dfbe22262d0577fb"
        private const val ONE_DAY_MS = 86400000L
    }

    // Скоупы корутин
    private val viewModelJob = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + viewModelJob)

    // ==================== Локальный ИИ ====================
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

    // ==================== Облачный ИИ ====================
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

    // ==================== Настройки ИИ ====================
    private var currentModelName: String = ""
    
    // Публичное поле для отображения имени загруженной модели в UI
    private val _loadedModelName = MutableStateFlow("")
    val loadedModelName: StateFlow<String> = _loadedModelName.asStateFlow()

    private val _systemPrompt = MutableStateFlow("Ты — полезный, умный и лаконичный ИИ-ассистент. Отвечай строго на русском языке.")
    val systemPrompt = _systemPrompt.asStateFlow()

    private val _chatHistory = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatHistory = _chatHistory.asStateFlow()

    val temperature = MutableStateFlow(0.3f)
    val contextSize = MutableStateFlow(2048)
    val maxTokens = MutableStateFlow(512)

    // ==================== Файлы памяти ====================
    // Основная база знаний (долговременная память)
    private val memoryFile: File by lazy {
        File(getApplication<Application>().filesDir, "memory.txt")
    }
    
    // Сжатые выводы из диалогов (мозг)
    private val brainFile: File by lazy {
        File(getApplication<Application>().filesDir, "brain.txt")
    }

    // ==================== TTS (голосовой движок) ====================
    private var textToSpeech: TextToSpeech? = null
    private var isTtsEnabled = false
    private val _isTtsReady = MutableStateFlow(false)
    val isTtsReady: StateFlow<Boolean> = _isTtsReady.asStateFlow()

    // ==================== Будильник ====================
    private val alarmManager by lazy {
        getApplication<Application>().getSystemService(Context.ALARM_SERVICE) as AlarmManager
    }

    // ==================== Режим работы ИИ ====================
    private val _currentMode = MutableStateFlow(AIMode.NEUTRAL)
    val currentMode = _currentMode.asStateFlow()

    fun setCurrentMode(mode: AIMode) {
        _currentMode.value = mode
    }

    // ==================== Мониторинг памяти устройства ====================
    private val _memoryInfoText = MutableStateFlow("Всего доступно: 0.0 ГБ / Занято: 0.0 ГБ")
    val memoryInfoText: StateFlow<String> = _memoryInfoText.asStateFlow()

    private var ttsInitJob: Job? = null

    // ==================== Защита приложения ====================
    private val _isAppLocked = MutableStateFlow(true)
    val isAppLocked: StateFlow<Boolean> = _isAppLocked.asStateFlow()
    private var isUnlockedPermanently = false
    private var isSelfDestructed = false
    private val hardwareKeyAlias = "app_hardware_key"
    private val prefs: android.content.SharedPreferences by lazy {
        getApplication<Application>().getSharedPreferences("app_security", Context.MODE_PRIVATE)
    }

    // Статус привязки устройства
    private val _isDeviceBound = MutableStateFlow(false)
    val isDeviceBound: StateFlow<Boolean> = _isDeviceBound.asStateFlow()

    // ==================== LlamaHelper ====================
    private val llamaHelper by lazy {
        LlamaHelper(
            contentResolver = contentResolver,
            scope = scope,
            sharedFlow = _llmFlow,
        )
    }

    // ==================== ИНИЦИАЛИЗАЦИЯ ====================
    init {
        instance = this

        // Проверка привязки к устройству
        if (!verifyDeviceBinding()) {
            Log.e(TAG, "Device binding verification FAILED! Initiating self-destruct.")
            selfDestruct()
        } else {
            if (!verifyHardwareBinding()) {
                Log.e(TAG, "Hardware binding verification FAILED! Initiating self-destruct.")
                selfDestruct()
            } else {
                if (!verifyTimeLimit()) {
                    Log.e(TAG, "Time limit verification FAILED! Initiating self-destruct.")
                    selfDestruct()
                } else {
                    _isAppLocked.value = false
                    _isDeviceBound.value = true
                    Log.i(TAG, "All security checks passed. App is unlocked and device is bound.")
                }
            }
        }

        // Инициализация файлов памяти
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (!memoryFile.exists()) memoryFile.createNewFile()
                if (!brainFile.exists()) brainFile.createNewFile()
                Log.d(TAG, "Local storage files initialized successfully.")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to initialize local text memory files: ${e.message}")
            }
        }

        // Инициализация TTS
        initTts()

        // Подписка на события облачного ИИ
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
                            speakText(fullText)
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
                                    delay(30)
                                }
                            }
                            saveBrain(fullText)
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

        // Подписка на события локального ИИ
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
                            speakText(fullText)
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
                                    delay(30)
                                }
                            }
                            saveBrain(fullText)
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

        // Мониторинг оперативной памяти устройства
        scope.launch(Dispatchers.Default) {
            val context = getApplication<Application>().applicationContext
            val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? android.app.ActivityManager
            val memoryInfo = android.app.ActivityManager.MemoryInfo()
            while (true) {
                if (activityManager != null) {
                    activityManager.getMemoryInfo(memoryInfo)
                    val totalGb = memoryInfo.totalMem.toDouble() / (1024.0 * 1024.0 * 1024.0)
                    val availGb = memoryInfo.availMem.toDouble() / (1024.0 * 1024.0 * 1024.0)
                    val usedGb = totalGb - availGb
                    val formattedString = String.format(
                        java.util.Locale.US,
                        "Всего доступно %.1f ГБ / Занято %.1f ГБ",
                        totalGb,
                        usedGb
                    )
                    _memoryInfoText.value = formattedString
                }
                delay(1000)
            }
        }
    }

    // ==================== ЗАЩИТА ПРИЛОЖЕНИЯ ====================

    /**
     * Проверка привязки к уникальному идентификатору устройства (Android ID).
     * При первом запуске сохраняет хэш Android ID.
     * При последующих запусках сверяет сохранённый хэш с текущим.
     */
    private fun verifyDeviceBinding(): Boolean {
        return try {
            val context = getApplication<Application>()
            val androidId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
            
            if (androidId.isNullOrEmpty()) {
                Log.e(TAG, "Failed to get Android ID. Device binding impossible.")
                _isDeviceBound.value = false
                return false
            }

            val md = MessageDigest.getInstance("SHA-256")
            val deviceHash = md.digest(androidId.toByteArray(Charsets.UTF_8))
            val deviceHashString = deviceHash.joinToString("") { "%02x".format(it) }

            val storedHash = prefs.getString("device_hash", null)

            if (storedHash == null) {
                prefs.edit().putString("device_hash", deviceHashString).apply()
                _isDeviceBound.value = true
                Log.i(TAG, "Device binding initialized for this device.")
                true
            } else {
                val isMatch = storedHash == deviceHashString
                if (!isMatch) {
                    Log.e(TAG, "Device mismatch! Stored: $storedHash, Current: $deviceHashString")
                    _isDeviceBound.value = false
                } else {
                    _isDeviceBound.value = true
                }
                isMatch
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error verifying device binding: ${e.message}", e)
            _isDeviceBound.value = false
            false
        }
    }

    /**
     * Проверка привязки к аппаратному ключу в Android Keystore.
     * Ключ генерируется при первом запуске и не может быть скопирован на другое устройство.
     */
    private fun verifyHardwareBinding(): Boolean {
        return try {
            val keyStore = KeyStore.getInstance("AndroidKeyStore")
            keyStore.load(null)
            val isKeyExists = keyStore.containsAlias(hardwareKeyAlias)
            val storedTime = prefs.getLong("app_start_time", 0L)

            if (!isKeyExists && storedTime == 0L) {
                generateHardwareKey()
                prefs.edit().putLong("app_start_time", System.currentTimeMillis()).apply()
                Log.i(TAG, "Hardware key generated for first launch.")
                true
            } else if (isKeyExists && storedTime > 0L) {
                true
            } else if (isKeyExists && storedTime == 0L) {
                Log.e(TAG, "Hardware key exists but start time is missing! Possible cloning.")
                false
            } else {
                Log.e(TAG, "Start time exists but hardware key is missing! Possible cloning.")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Hardware binding verification error: ${e.message}", e)
            false
        }
    }

    /**
     * Генерация аппаратного ключа в Android Keystore.
     * Ключ привязан к устройству и не может быть экспортирован.
     */
    private fun generateHardwareKey() {
        try {
            val keyPairGenerator = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_RSA, "AndroidKeyStore")
            val spec = KeyGenParameterSpec.Builder(
                hardwareKeyAlias,
                KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY
            )
                .setDigests(KeyProperties.DIGEST_SHA256)
                .setSignaturePaddings(KeyProperties.SIGNATURE_PADDING_RSA_PKCS1)
                .setUserAuthenticationRequired(false)
                .setKeySize(2048)
                .build()
            keyPairGenerator.initialize(spec)
            keyPairGenerator.generateKeyPair()
            Log.i(TAG, "Hardware key generated successfully.")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to generate hardware key: ${e.message}", e)
        }
    }

    /**
     * Проверка временного лимита (24 часа).
     * Если лимит истёк — приложение блокируется.
     * Постоянная разблокировка через секретную фразу отключает проверку.
     */
    private fun verifyTimeLimit(): Boolean {
        if (isUnlockedPermanently) {
            return true
        }

        val storedTime = prefs.getLong("app_start_time", 0L)
        if (storedTime == 0L) {
            Log.e(TAG, "Start time not found. Verification failed.")
            return false
        }

        val currentTime = System.currentTimeMillis()
        if (currentTime < storedTime) {
            Log.e(TAG, "System time was set back! Possible tampering.")
            return false
        }

        val elapsed = currentTime - storedTime
        if (elapsed > ONE_DAY_MS) {
            Log.e(TAG, "One-day limit exceeded. Elapsed: ${elapsed / 86400000} days.")
            return false
        }

        Log.i(TAG, "Time limit verification passed. Hours remaining: ${(ONE_DAY_MS - elapsed) / 3600000}")
        return true
    }

    /**
     * Проверка секретной фразы для постоянной разблокировки.
     * При неверном вводе запускает самоуничтожение.
     */
    fun verifySecretPhrase(input: String): Boolean {
        val inputHash = hashStringSha256(input)
        val isMatch = MessageDigest.isEqual(inputHash.toByteArray(), SECRET_PHRASE_HASH.toByteArray())
        if (isMatch) {
            isUnlockedPermanently = true
            _isAppLocked.value = false
            _isDeviceBound.value = true
            prefs.edit().putLong("app_start_time", System.currentTimeMillis()).apply()
            prefs.edit().putBoolean("unlocked_permanently", true).apply()
            Log.i(TAG, "Device permanently unlocked with secret phrase.")
        } else {
            Log.w(TAG, "Incorrect secret phrase entered. Initiating self-destruct.")
            selfDestruct()
        }
        return isMatch
    }

    /**
     * Хэширование строки алгоритмом SHA-256.
     */
    private fun hashStringSha256(input: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(input.toByteArray(Charsets.UTF_8))
        return digest.joinToString("") { "%02x".format(it) }
    }

    /**
     * Самоуничтожение приложения: очистка памяти, удаление файлов, блокировка движка.
     */
    private fun selfDestruct() {
        if (isSelfDestructed) {
            return
        }
        isSelfDestructed = true
        _isDeviceBound.value = false
        Log.e(TAG, "!!! SELF-DESTRUCT SEQUENCE INITIATED !!!")

        try {
            _chatHistory.value = emptyList()
            _generatedText.value = ""
            _cloudGeneratedText.value = ""
            Log.i(TAG, "Memory buffers cleared.")

            deleteFileRecursively(memoryFile)
            deleteFileRecursively(brainFile)
            Log.i(TAG, "Memory files deleted.")

            val context = getApplication<Application>()
            val ttsDir = File(context.filesDir, "tts-model")
            val voskDir = File(context.filesDir, "vosk-model")
            deleteFileRecursively(ttsDir)
            deleteFileRecursively(voskDir)
            Log.i(TAG, "TTS and Vosk model directories deleted.")

            prefs.edit().putBoolean("engine_permanently_dead", true).apply()
            releaseModel()
            Log.i(TAG, "Llama engine blocked.")

            try {
                val process = Runtime.getRuntime().exec("pm clear ${context.packageName}")
                process.waitFor()
                Log.i(TAG, "pm clear executed successfully.")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to execute pm clear: ${e.message}", e)
            }

            _isAppLocked.value = true
            android.os.Process.killProcess(android.os.Process.myPid())
        } catch (e: Exception) {
            Log.e(TAG, "Error during self-destruct sequence: ${e.message}", e)
        }
    }

    /**
     * Рекурсивное удаление файлов и директорий.
     */
    private fun deleteFileRecursively(file: File) {
        if (!file.exists()) return
        if (file.isDirectory) {
            file.listFiles()?.forEach { deleteFileRecursively(it) }
        }
        file.delete()
        Log.i(TAG, "Deleted: ${file.absolutePath}")
    }

    // ==================== TTS (ГОЛОСОВОЙ ДВИЖОК) ====================

    /**
     * Инициализация Text-to-Speech движка с русским языком.
     */
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
                            val welcomeText = "Привет! Я твоя локальная языковая модель. Голосовой движок полностью готов к работе, чем я могу помочь?"
                            appendSystemMessage(welcomeText)
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
                textToSpeech?.setLanguage(Locale("ru", "RU"))
            } catch (e: Exception) {
                _isTtsReady.value = false
                appendSystemMessage("🔴 Ошибка инициализации TTS: ${e.message}")
                Log.e(TAG, "TTS init error: ${e.message}", e)
            }
        }
    }

    /**
     * Включение TTS. Если движок не инициализирован — загружает его заново.
     */
    fun enableTts() {
        if (_isTtsReady.value && isTtsEnabled) {
            appendSystemMessage("🔊 Озвучка уже включена")
            return
        }

        if (_isTtsReady.value && !isTtsEnabled) {
            isTtsEnabled = true
            appendSystemMessage("🔊 Озвучка включена")
            return
        }

        ttsInitJob?.cancel()
        ttsInitJob = viewModelScope.launch {
            try {
                val context = getApplication<Application>()
                textToSpeech?.shutdown()
                textToSpeech = null
                _isTtsReady.value = false

                textToSpeech = TextToSpeech(context) { status ->
                    if (status == TextToSpeech.SUCCESS) {
                        val result = textToSpeech?.setLanguage(Locale("ru", "RU"))
                        if (result == TextToSpeech.LANG_AVAILABLE || result == TextToSpeech.LANG_COUNTRY_AVAILABLE) {
                            _isTtsReady.value = true
                            isTtsEnabled = true
                            appendSystemMessage("🟢 Голосовой движок Android TTS успешно загружен.")
                            Log.d(TAG, "TTS enabled successfully")
                        } else {
                            _isTtsReady.value = false
                            appendSystemMessage("⚠️ Русский язык не поддерживается TTS. Проверьте настройки.")
                            Log.w(TAG, "TTS enable: Russian language not available")
                        }
                    } else {
                        _isTtsReady.value = false
                        appendSystemMessage("🔴 Ошибка загрузки TTS: $status")
                        Log.e(TAG, "TTS enable failed with status: $status")
                    }
                }
                textToSpeech?.setLanguage(Locale("ru", "RU"))
            } catch (e: Exception) {
                _isTtsReady.value = false
                appendSystemMessage("🔴 Ошибка загрузки TTS: ${e.message}")
                Log.e(TAG, "TTS enable error: ${e.message}", e)
            }
        }
    }

    /**
     * Отключение TTS с полной выгрузкой из памяти.
     */
    fun disableTts() {
        isTtsEnabled = false
        if (textToSpeech?.isSpeaking == true) {
            textToSpeech?.stop()
        }
        textToSpeech?.shutdown()
        textToSpeech = null
        _isTtsReady.value = false
        appendSystemMessage("🔇 Озвучка отключена, TTS выгружен из памяти")
        Log.d(TAG, "TTS disabled and unloaded")
    }

    /**
     * Фильтрация текста для озвучки: удаление Markdown-символов.
     */
        private fun filterTextForSpeech(text: String): String {
        // Оставляем: буквы (включая русские), цифры, пробелы, базовые знаки препинания (. , ! ?)
        val cleanText = text.replace(Regex("[^\\p{L}\\p{N}\\s.,!?]"), "")
        // Удаляем множественные подряд идущие пробелы (оставляем один)
        return cleanText.replace(Regex("\\s+"), " ").trim()
    }

    /**
     * Озвучка текста через TTS с предварительной фильтрацией.
     */
    fun speakText(text: String) {
        if (!_isTtsReady.value || !isTtsEnabled || text.isBlank() || textToSpeech == null) {
            return
        }
        val filteredText = filterTextForSpeech(text)
        if (filteredText.isBlank()) {
            return
        }
        textToSpeech?.speak(filteredText, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    /**
     * Принудительная установка состояния облачного ИИ в "Готов".
     */
    fun setCloudReady(modelId: String) {
        _cloudState.value = CloudAIState.Ready(modelId)
        Log.d(TAG, "Cloud state set to Ready for model: $modelId")
    }

    // ==================== РАБОТА С ПАМЯТЬЮ ====================

    /**
     * Извлечение корня русского слова путём отсечения окончаний и суффиксов.
     * Используется для поиска по ключевым словам в базе знаний.
     */
    private fun extractRussianRoot(word: String): String {
        val lowerWord = word.lowercase()
        val suffixes = listOf(
            "ами", "ые", "ой", "ых", "ого", "его", "ому", "ему", "им", "ым",
            "ая", "яя", "ое", "ее", "ие", "ые", "ий", "ый", "ой", "ей",
            "ам", "ям", "ом", "ем", "ах", "ях", "ов", "ев", "ин", "ын",
            "а", "я", "о", "е", "и", "ы", "у", "ю"
        )
        var stem = lowerWord
        for (suffix in suffixes) {
            if (stem.endsWith(suffix) && stem.length > suffix.length + 1) {
                stem = stem.substring(0, stem.length - suffix.length)
                break
            }
        }
        return if (stem.length < 2) lowerWord else stem
    }

    /**
     * Фоновая компрессия диалога: при достижении порога сообщений
     * отправляет диалог в LLM для выделения ключевых фактов.
     * Результат сохраняется в brain.txt (мозг).
     */
    private fun triggerBackgroundDialogueCompression(history: List<ChatMessage>) {
        if (history.size < AUTO_BRAIN_COMPRESSION_THRESHOLD) return

        scope.launch(Dispatchers.IO) {
            try {
                val dialogueText = history.joinToString("\n") { message ->
                    val prefix = when (message.role) {
                        "user" -> "Пользователь"
                        "assistant" -> "Ассистент"
                        else -> "Система"
                    }
                    "$prefix: ${message.text}"
                }

                val prompt = "Проанализируй этот диалог. Выдели из него новые важные факты о личности, имени, привычках или планах Пользователя. Сформулируй краткие выводы тезисно, строго по одной строке на факт. Пиши только новые выводы, без лишних слов. Если новых данных нет, верни пустоту.\n\n$dialogueText"

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

    /**
     * Обновление последнего системного сообщения (для эффекта печатной машинки).
     */
    fun updateLastSystemMessage(newText: String) {
        val currentList = _chatHistory.value.toMutableList()
        if (currentList.isNotEmpty() && currentList.last().role == "system") {
            currentList[currentList.lastIndex] = ChatMessage("system", newText)
            _chatHistory.value = currentList
        } else {
            _chatHistory.value = currentList + ChatMessage("system", newText)
        }
    }

    /**
     * Определение категории текста по ключевым словам.
     * Используется для маркировки записей в базе знаний и умного поиска.
     */
    private fun determineCategory(text: String): String {
        val lowerText = text.lowercase()
        val categories = mapOf(
            "[ПАРОЛЬ]" to listOf("пароль", "логин", "доступ", "код", "пин", "секрет", "ключ"),
            "[КОНТАКТ]" to listOf("телефон", "номер", "контакт", "позвонить", "мобильный", "вотсап", "телеграм"),
            "[ПРАЙС]" to listOf("руб", "цена", "стоимость", "прайс", "оплата", "расчёт", "скидка", "тариф"),
            "[ИНСТРУКЦИЯ]" to listOf("как", "инструкция", "алгоритм", "пошагово", "руководство", "порядок", "действия"),
            "[АДРЕС]" to listOf("адрес", "улица", "город", "метро", "район", "дом"),
            "[ДАТА]" to listOf("дата", "время", "встреча", "напоминание", "дедлайн")
        )

        val scores = categories.mapValues { (_, keywords) ->
            keywords.count { keyword -> lowerText.contains(keyword) }
        }

        val bestCategory = scores.maxByOrNull { it.value }
        return if (bestCategory != null && bestCategory.value > 0) {
            bestCategory.key
        } else {
            "[ОБЩЕЕ]"
        }
    }

    /**
     * Сохранение текста в долговременную память (memory.txt) с меткой категории и временной меткой.
     * Вызывается командой "запомни" в любом режиме.
     */
    fun saveToLongTermMemory(text: String) {
        try {
            if (!memoryFile.exists()) {
                memoryFile.createNewFile()
            }
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            val timestamp = dateFormat.format(Date())
            val category = determineCategory(text)
            val taggedText = "$category $text"
            memoryFile.appendText("[$timestamp] $taggedText\n")
            Log.d(TAG, "Записано в долговременную память: $taggedText")
            appendSystemMessage("🧠 Запомнено: $text")
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка записи памяти: ${e.message}")
        }
    }

    /**
     * Поиск в базе знаний (memory.txt) по ключевым словам.
     * Если указана категория — поиск сначала в ней, затем по всей базе.
     * Используется командами "найди", "поищи", "вспомни", "посмотри в чате".
     */
    private fun searchMemory(query: String, category: String? = null): String {
        val fullMemory = readFromLongTermMemory()
        if (fullMemory.isEmpty()) return ""

        val lines = fullMemory.split("\n").filter { it.isNotEmpty() }

        if (category != null) {
            val filteredLines = lines.filter { it.startsWith(category) }
            if (filteredLines.isNotEmpty()) {
                val keywords = extractKeywords(query)
                return if (keywords.isNotEmpty()) {
                    filteredLines.filter { line ->
                        val lowerLine = line.lowercase()
                        keywords.any { keyword -> lowerLine.contains(keyword) }
                    }.joinToString("\n")
                } else {
                    filteredLines.joinToString("\n")
                }
            }
        }

        val keywords = extractKeywords(query)
        return if (keywords.isNotEmpty()) {
            lines.filter { line ->
                val lowerLine = line.lowercase()
                keywords.any { keyword -> lowerLine.contains(keyword) }
            }.joinToString("\n")
        } else {
            ""
        }
    }

    /**
     * Извлечение ключевых слов из поискового запроса.
     * Удаляет командные слова, разбивает на слова, фильтрует короткие,
     * извлекает корни для морфологического поиска.
     */
    private fun extractKeywords(query: String): List<String> {
        return query.lowercase()
            .replace(RECALL_COMMAND, "")
            .replace(FIND_COMMAND, "")
            .replace(SEARCH_COMMAND, "")
            .replace(CHAT_LOOKUP_COMMAND, "")
            .trim()
            .split(" ")
            .map { it.trim() }
            .filter { it.length > 2 }
            .map { extractRussianRoot(it) }
            .distinct()
    }

    /**
     * Построение системного промпта для LLM.
     * При поисковых командах добавляет данные из базы знаний, мозга и истории чата.
     * При обычных командах возвращает только базовый системный промпт.
     */
    private fun buildSystemPrompt(isSearchCommand: Boolean, prompt: String): String {
        val basePrompt = _systemPrompt.value
        if (!isSearchCommand) {
            return basePrompt
        }

        val brainData = readBrain()
        val chatHistory = _chatHistory.value
        val queryCategory = determineCategory(prompt)

        var filteredMemory = searchMemory(prompt, queryCategory)
        if (filteredMemory.isEmpty()) {
            filteredMemory = searchMemory(prompt, null)
        }

        val memorySection = if (filteredMemory.isNotEmpty()) {
            "ЛОКАЛЬНАЯ БАЗА ЗНАНИЙ (НАЙДЕННЫЕ ФАКТЫ):\n$filteredMemory\n\n"
        } else {
            "ЛОКАЛЬНАЯ БАЗА ЗНАНИЙ: подходящих фактов не найдено.\n\n"
        }

        return buildString {
            append(basePrompt)
            append("\n\n")
            append(memorySection)
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

    // ==================== ОТПРАВКА СООБЩЕНИЙ ====================

    /**
     * Главный метод отправки сообщения пользователя.
     * Обрабатывает команды: "запомни", "будильник", "напомни", поисковые команды.
     * В NEUTRAL режиме поисковые команды выполняются локально без LLM.
     * В LOCAL/CLOUD режимах поисковые команды передаются в LLM с полным контекстом памяти.
     */
    fun sendUserMessage(text: String) {
        if (text.isBlank()) return

        _chatHistory.value = _chatHistory.value + ChatMessage("user", text)
        triggerBackgroundDialogueCompression(_chatHistory.value)

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
                if (_currentMode.value == AIMode.NEUTRAL) {
                    val memoryData = searchMemory(text, determineCategory(text))
                    if (memoryData.isNotEmpty()) {
                        appendSystemMessage("🔍 Найдено в памяти:\n$memoryData")
                    } else {
                        appendSystemMessage("🔍 Ничего не найдено по запросу '$text'")
                    }
                    return
                }
            }
        }

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

    // ==================== ОБЛАЧНЫЙ ИИ ====================

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

    /**
     * Генерация ответа через облачный ИИ.
     * При поисковых командах передаёт полный контекст памяти в системном промпте.
     */
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

    // ==================== ЛОКАЛЬНЫЙ ИИ ====================

    /**
     * Загрузка локальной модели.
     * Блокируется, если движок помечен как "мёртвый" после самоуничтожения.
     */
    fun loadModel(path: String, mmprojPath: String? = null) {
        if (path.isEmpty()) return

        if (prefs.getBoolean("engine_permanently_dead", false)) {
            Log.e(TAG, "Engine is permanently dead. Load blocked.")
            _state.value = GenerationState.Error("Engine is permanently dead.")
            return
        }

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
                        _loadedModelName.value = currentModelName
                    }
                )
            } catch (e: Exception) {
                _state.value = GenerationState.Error(e.message ?: "Unknown error")
                _isModelLoaded.value = false
            }
        }
    }

    /**
     * Генерация ответа через локальный ИИ.
     * Блокируется, если движок помечен как "мёртвый".
     * При поисковых командах передаёт полный контекст памяти в системном промпте.
     */
    fun generateLocal(prompt: String, imagePath: String? = null) {
        val lowerPrompt = prompt.trim().lowercase()

        if (prefs.getBoolean("engine_permanently_dead", false)) {
            Log.e(TAG, "Engine is permanently dead. Generate blocked.")
            _state.value = GenerationState.Error("Engine is permanently dead.")
            return
        }

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

    // ==================== БУДИЛЬНИК ====================

    /**
     * Обработка команды будильника/напоминания.
     * Извлекает время и сообщение из текста команды.
     */
    private fun handleAlarmCommand(prompt: String) {
        val timePattern = Regex("(?:в|в\\s+|напомни\\s+в\\s+)(\\d{1,2}[:.]\\d{2})")
        val match = timePattern.find(prompt)

        if (match != null) {
            val timeStr = match.groupValues[1].replace(".", ":")
            val message = prompt.replace(Regex("(?:в\\s+|напомни\\s+в\\s+)\\d{1,2}[:.]\\d{2}\\s*"), "").trim()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val context = getApplication<Application>().applicationContext
                val alarmManagerSystem = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
                if (alarmManagerSystem?.canScheduleExactAlarms() == false) {
                    appendSystemMessage("⚠️ Для установки точных напоминаний ИИ-Другу требуется специальное разрешение. Пожалуйста, включите тумблер в открывшихся настройках.")
                    try {
                        val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        appendSystemMessage("❌ Не удалось автоматически открыть системные настройки разрешений: ${e.message}")
                    }
                    return
                }
            }

            setAlarm(timeStr, message)
            appendSystemMessage("⏰ Будильник установлен на $timeStr: '$message'")
        } else {
            appendSystemMessage("⚠️ Не удалось распознать время. Используйте формат: 'в 18.00 идем в гараж'")
        }
    }

    /**
     * Установка системного будильника на указанное время.
     */
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

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
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

    // ==================== ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ====================

    /**
     * Добавление системного сообщения в историю чата.
     */
    internal fun appendSystemMessage(text: String) {
        _chatHistory.value = _chatHistory.value + ChatMessage("system", text)
    }

    /**
     * Прерывание генерации локального ИИ.
     */
    fun abortLocal() {
        if (_state.value.isActive()) {
            Log.i(TAG, "Aborting generation")
            llamaHelper.abort()
        }
    }

    /**
     * Чтение всей базы знаний из memory.txt.
     */
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

    /**
     * Полная перезапись базы знаний.
     */
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

    /**
     * Сохранение сжатых выводов в brain.txt (мозг).
     * Вызывается автоматически после каждой генерации LLM.
     */
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

    /**
     * Чтение сжатых выводов из brain.txt (мозг).
     */
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

    /**
     * Очистка истории чата и сгенерированного текста.
     */
    fun clearChat() {
        _chatHistory.value = emptyList()
        _generatedText.value = ""
        _cloudGeneratedText.value = ""
    }

    // ==================== НАСТРОЙКИ ====================

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

    /**
     * Выгрузка локальной модели из памяти.
     */
    fun releaseModel() {
        _isModelLoaded.value = false
        _loadedModelName.value = ""
        llamaHelper.release()
    }

    /**
     * Очистка ресурсов при уничтожении ViewModel.
     */
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

/**
 * Получение имени файла из URI контента.
 */
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
