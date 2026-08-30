package org.nehuatl.sample

import android.app.Application
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.Settings
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

data class ChatMessage(val role: String, val text: String)

class MainViewModel(application: Application, val contentResolver: ContentResolver) : AndroidViewModel(application) {
    companion object {
        @Volatile var instance: MainViewModel? = null
        private const val TAG = "MainViewModel"

        private const val REMEMBER_COMMAND = "запомни"
        private const val FIND_COMMAND = "найди"
        private const val SEARCH_COMMAND = "поищи"
        private const val RECALL_COMMAND = "вспомни"
        private const val ALARM_COMMAND = "будильник"
        private const val REMIND_COMMAND = "напомни"
        private const val CHAT_LOOKUP_COMMAND = "посмотри в чате"

        private const val AUTO_BRAIN_COMPRESSION_THRESHOLD = 10

        private const val SECRET_PHRASE_HASH = "af5f2b759f2adf6f46fdd7b1441ed77086f833dcb5f74d9a5ea6930aa8634505"
        private const val ONE_DAY_MS = 86400000L

        private val CATEGORIES = listOf("[ПАРОЛЬ]", "[КОНТАКТ]", "[ПРАЙС]", "[ИНСТРУКЦИЯ]", "[АДРЕС]", "[ДАТА]", "[ОБЩЕЕ]")
    }

    private val viewModelJob = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + viewModelJob)

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

    private val _loadedModelName = MutableStateFlow("")
    val loadedModelName: StateFlow<String> = _loadedModelName.asStateFlow()

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

    private var isCompressionRequest = false

    private var isTtsEnabled = false
    private val _isTtsReady = MutableStateFlow(false)
    val isTtsReady: StateFlow<Boolean> = _isTtsReady.asStateFlow()

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _speakStartTrigger = MutableStateFlow(false)
    val speakStartTrigger: StateFlow<Boolean> = _speakStartTrigger.asStateFlow()

    private val _pendingTextToPrint = MutableStateFlow("")
    val pendingTextToPrint: StateFlow<String> = _pendingTextToPrint.asStateFlow()

    private lateinit var ttsReceiver: BroadcastReceiver

    private val alarmManager by lazy {
        getApplication<Application>().getSystemService(Context.ALARM_SERVICE) as AlarmManager
    }

    private val _currentMode = MutableStateFlow(AIMode.NEUTRAL)
    val currentMode = _currentMode.asStateFlow()

    fun setCurrentMode(mode: AIMode) {
        _currentMode.value = mode
    }

    private val _memoryInfoText = MutableStateFlow("Всего доступно: 0.0 ГБ / Занято: 0.0 ГБ")
    val memoryInfoText: StateFlow<String> = _memoryInfoText.asStateFlow()

    private val _isAppLocked = MutableStateFlow(true)
    val isAppLocked: StateFlow<Boolean> = _isAppLocked.asStateFlow()
    private var isUnlockedPermanently = false
    private val hardwareKeyAlias = "app_hardware_key"
    private val prefs: android.content.SharedPreferences by lazy {
        getApplication<Application>().getSharedPreferences("app_security", Context.MODE_PRIVATE)
    }

    private val _isDeviceBound = MutableStateFlow(false)
    val isDeviceBound: StateFlow<Boolean> = _isDeviceBound.asStateFlow()

    private val _remainingTimeText = MutableStateFlow("")
    val remainingTimeText: StateFlow<String> = _remainingTimeText.asStateFlow()

    private val _isPermanentlyUnlocked = MutableStateFlow(false)
    val isPermanentlyUnlocked: StateFlow<Boolean> = _isPermanentlyUnlocked.asStateFlow()

    private val _isPermanentlyBlocked = MutableStateFlow(false)
    val isPermanentlyBlocked: StateFlow<Boolean> = _isPermanentlyBlocked.asStateFlow()

    private val llamaHelper by lazy {
        LlamaHelper(
            contentResolver = contentResolver,
            scope = scope,
            sharedFlow = _llmFlow,
        )
    }

    init {
        instance = this

        ttsReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                when (intent?.action) {
                    TtsService.ACTION_TTS_READY -> {
                        val message = intent.getStringExtra(TtsService.EXTRA_MESSAGE) ?: "✅ Офлайн голосовой движок успешно загружен"
                        _isTtsReady.value = true
                        isTtsEnabled = true
                        appendSystemMessage(message)
                        Log.d(TAG, "TTS ready broadcast received: $message")
                    }
                    TtsService.ACTION_TTS_ERROR -> {
                        val message = intent.getStringExtra(TtsService.EXTRA_MESSAGE) ?: "❌ Ошибка инициализации офлайн TTS"
                        _isTtsReady.value = false
                        isTtsEnabled = false
                        appendSystemMessage(message)
                        Log.e(TAG, "TTS error broadcast received: $message")
                    }
                    TtsService.ACTION_SPEAK_START -> {
                        _isSpeaking.value = true
                        _speakStartTrigger.value = true
                        Log.d(TAG, "SPEAK_START broadcast received")
                    }
                    TtsService.ACTION_SPEAK_END -> {
                        _isSpeaking.value = false
                        _speakStartTrigger.value = false
                        Log.d(TAG, "SPEAK_END broadcast received")
                    }
                }
            }
        }

        val ttsFilter = IntentFilter().apply {
            addAction(TtsService.ACTION_TTS_READY)
            addAction(TtsService.ACTION_TTS_ERROR)
            addAction(TtsService.ACTION_SPEAK_START)
            addAction(TtsService.ACTION_SPEAK_END)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            getApplication<Application>().registerReceiver(
                ttsReceiver,
                ttsFilter,
                Context.RECEIVER_NOT_EXPORTED
            )
        } else {
            getApplication<Application>().registerReceiver(ttsReceiver, ttsFilter)
        }

        if (prefs.getBoolean("is_permanently_blocked", false)) {
            Log.e(TAG, "App is permanently blocked.")
            _isAppLocked.value = true
            _isDeviceBound.value = false
            _isPermanentlyBlocked.value = true
            _remainingTimeText.value = "🔴 Приложение заблокировано"
        } else {
            isUnlockedPermanently = prefs.getBoolean("unlocked_permanently", false)
            if (isUnlockedPermanently) {
                _isPermanentlyUnlocked.value = true
                _isAppLocked.value = false
                _isDeviceBound.value = true
                _remainingTimeText.value = "✅ Приложение разблокировано"
                Log.i(TAG, "App is permanently unlocked.")
            } else {
                if (!verifyDeviceBinding()) {
                    Log.e(TAG, "Device binding verification FAILED!")
                    _isAppLocked.value = true
                    _isDeviceBound.value = false
                    _isPermanentlyBlocked.value = true
                    _remainingTimeText.value = "🔴 Приложение заблокировано"
                } else {
                    if (!verifyHardwareBinding()) {
                        Log.e(TAG, "Hardware binding verification FAILED!")
                        _isAppLocked.value = true
                        _isDeviceBound.value = false
                        _isPermanentlyBlocked.value = true
                        _remainingTimeText.value = "🔴 Приложение заблокировано"
                    } else {
                        if (!verifyTimeLimit()) {
                            Log.e(TAG, "Time limit verification FAILED!")
                        } else {
                            _isAppLocked.value = false
                            _isDeviceBound.value = true
                            _remainingTimeText.value = ""
                            Log.i(TAG, "All security checks passed. App is unlocked and device is bound.")
                        }
                    }
                }
            }
        }

        scope.launch(Dispatchers.Default) {
            while (true) {
                updateRemainingTime()
                delay(60000)
            }
        }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (!memoryFile.exists()) memoryFile.createNewFile()
                if (!brainFile.exists()) brainFile.createNewFile()
                Log.d(TAG, "Local storage files initialized successfully.")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to initialize local text memory files: ${e.message}")
            }
        }

        isTtsEnabled = true
        _isTtsReady.value = false
        appendSystemMessage("🔄 Инициализация офлайн голосового движка...")
        Log.d(TAG, "TTS auto-enabled, waiting for initialization")

        val ttsStartIntent = Intent(getApplication<Application>(), TtsService::class.java)
        getApplication<Application>().startService(ttsStartIntent)

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
                            if (isCompressionRequest) {
                                saveBrain(fullText)
                                isCompressionRequest = false
                                appendSystemMessage("✅ Беседа записана в память")
                            } else {
                                _cloudGeneratedText.value = fullText
                                _pendingTextToPrint.value = fullText
                                speakText(fullText)
                            }
                        }
                        _cloudGeneratedText.value = fullText
                    }
                    is CloudAIEvent.Error -> {
                        _cloudState.value = CloudAIState.Error(event.message)
                        isCompressionRequest = false
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
                            if (isCompressionRequest) {
                                saveBrain(fullText)
                                isCompressionRequest = false
                                appendSystemMessage("✅ Беседа записана в память")
                            } else {
                                _generatedText.value = fullText
                                _pendingTextToPrint.value = fullText
                                speakText(fullText)
                            }
                        }
                        _generatedText.value = fullText
                    }
                    is LlamaHelper.LLMEvent.Error -> {
                        _state.value = GenerationState.Error(event.message)
                        isCompressionRequest = false
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

    private fun updateRemainingTime() {
        if (_isPermanentlyBlocked.value) {
            _remainingTimeText.value = "🔴 Приложение заблокировано"
            return
        }

        if (isUnlockedPermanently) {
            _remainingTimeText.value = "✅ Приложение разблокировано"
            _isPermanentlyUnlocked.value = true
            return
        }

        _isPermanentlyUnlocked.value = false

        val storedTime = prefs.getLong("app_start_time", 0L)
        if (storedTime == 0L) {
            _remainingTimeText.value = "⚠️ Время не установлено"
            return
        }

        val currentTime = System.currentTimeMillis()
        val elapsed = currentTime - storedTime
        val remaining = ONE_DAY_MS - elapsed

        if (remaining <= 0) {
            _remainingTimeText.value = "🔴 Приложение заблокировано. Введите секретную фразу."
            _isAppLocked.value = true
            return
        }

        val hours = remaining / 3600000
        val minutes = (remaining % 3600000) / 60000
        _remainingTimeText.value = "⏳ До блокировки: ${hours}ч ${minutes}мин"
    }

    private fun verifyDeviceBinding(): Boolean {
        return try {
            val context = getApplication<Application>()
            val androidId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)

            if (androidId.isNullOrEmpty()) {
                Log.e(TAG, "Failed to get Android ID. Device binding impossible.")
                _isDeviceBound.value = false
                _isAppLocked.value = true
                _isPermanentlyBlocked.value = true
                prefs.edit().putBoolean("is_permanently_blocked", true).apply()
                _remainingTimeText.value = "🔴 Приложение заблокировано"
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
                    _isAppLocked.value = true
                    _isPermanentlyBlocked.value = true
                    prefs.edit().putBoolean("is_permanently_blocked", true).apply()
                    _remainingTimeText.value = "🔴 Приложение заблокировано"
                } else {
                    _isDeviceBound.value = true
                }
                isMatch
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error verifying device binding: ${e.message}", e)
            _isDeviceBound.value = false
            _isAppLocked.value = true
            _isPermanentlyBlocked.value = true
            prefs.edit().putBoolean("is_permanently_blocked", true).apply()
            _remainingTimeText.value = "🔴 Приложение заблокировано"
            false
        }
    }

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
                _isAppLocked.value = true
                _isDeviceBound.value = false
                _isPermanentlyBlocked.value = true
                prefs.edit().putBoolean("is_permanently_blocked", true).apply()
                _remainingTimeText.value = "🔴 Приложение заблокировано"
                false
            } else {
                Log.e(TAG, "Start time exists but hardware key is missing! Possible cloning.")
                _isAppLocked.value = true
                _isDeviceBound.value = false
                _isPermanentlyBlocked.value = true
                prefs.edit().putBoolean("is_permanently_blocked", true).apply()
                _remainingTimeText.value = "🔴 Приложение заблокировано"
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Hardware binding verification error: ${e.message}", e)
            _isAppLocked.value = true
            _isDeviceBound.value = false
            _isPermanentlyBlocked.value = true
            prefs.edit().putBoolean("is_permanently_blocked", true).apply()
            _remainingTimeText.value = "🔴 Приложение заблокировано"
            false
        }
    }

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

    private fun verifyTimeLimit(): Boolean {
        if (isUnlockedPermanently) {
            _isPermanentlyUnlocked.value = true
            return true
        }

        val storedTime = prefs.getLong("app_start_time", 0L)
        if (storedTime == 0L) {
            Log.e(TAG, "Start time not found. Verification failed.")
            _isAppLocked.value = true
            _remainingTimeText.value = "🔴 Приложение заблокировано"
            return false
        }

        val currentTime = System.currentTimeMillis()
        if (currentTime < storedTime) {
            Log.e(TAG, "System time was set back! Possible tampering.")
            _isAppLocked.value = true
            _isPermanentlyBlocked.value = true
            prefs.edit().putBoolean("is_permanently_blocked", true).apply()
            _remainingTimeText.value = "🔴 Приложение заблокировано"
            return false
        }

        val elapsed = currentTime - storedTime
        if (elapsed > ONE_DAY_MS) {
            Log.e(TAG, "One-day limit exceeded. App locked. Secret phrase required.")
            _isAppLocked.value = true
            _remainingTimeText.value = "🔴 Приложение заблокировано. Введите секретную фразу."
            return false
        }

        Log.i(TAG, "Time limit verification passed. Hours remaining: ${(ONE_DAY_MS - elapsed) / 3600000}")
        return true
    }

    fun verifySecretPhrase(input: String): Boolean {
        val inputHash = hashStringSha256(input)
        val isMatch = MessageDigest.isEqual(inputHash.toByteArray(), SECRET_PHRASE_HASH.toByteArray())
        if (isMatch) {
            isUnlockedPermanently = true
            _isPermanentlyUnlocked.value = true
            _isPermanentlyBlocked.value = false
            _isAppLocked.value = false
            _isDeviceBound.value = true
            _remainingTimeText.value = "✅ Приложение разблокировано"
            prefs.edit().putLong("app_start_time", System.currentTimeMillis()).apply()
            prefs.edit().putBoolean("unlocked_permanently", true).apply()
            prefs.edit().putBoolean("is_permanently_blocked", false).apply()
            Log.i(TAG, "Device permanently unlocked with secret phrase.")
        } else {
            Log.w(TAG, "Incorrect secret phrase entered. Permanent block.")
            _isPermanentlyBlocked.value = true
            _isAppLocked.value = true
            _isDeviceBound.value = false
            _remainingTimeText.value = "🔴 Приложение заблокировано"
            prefs.edit().putBoolean("is_permanently_blocked", true).apply()
        }
        return isMatch
    }

    private fun hashStringSha256(input: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(input.toByteArray(Charsets.UTF_8))
        return digest.joinToString("") { "%02x".format(it) }
    }

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

        isTtsEnabled = true
        _isTtsReady.value = false
        appendSystemMessage("🔄 Инициализация офлайн голосового движка...")
        Log.d(TAG, "TTS service starting")

        val ttsStartIntent = Intent(getApplication<Application>(), TtsService::class.java)
        getApplication<Application>().startService(ttsStartIntent)
    }

    fun disableTts() {
        isTtsEnabled = false
        _isSpeaking.value = false
        _isTtsReady.value = false
        appendSystemMessage("🔇 Озвучка отключена, TTS выгружен из памяти")
        Log.d(TAG, "TTS disabled")

        val ttsStopIntent = Intent(getApplication<Application>(), TtsService::class.java)
        getApplication<Application>().stopService(ttsStopIntent)
    }

    private fun filterTextForSpeech(text: String): String {
    var cleanText = text
        .replace("+", " плюс ")
        .replace("=", " равно ")
        .replace("*", " умножить на ")
        .replace("/", " разделить на ")
        .replace("-", " минус ")

    // Заменяем запятую в числах на слово "запятая"
    cleanText = Regex("(\\d),(\\d)").replace(cleanText) { match ->
        "${match.groupValues[1]} запятая ${match.groupValues[2]}"
    }

    cleanText = cleanText.replace(Regex("[^\\p{L}\\p{N}\\s.!?]"), "")
    return cleanText.replace(Regex("\\s+"), " ").trim()
}

    fun speakText(text: String) {
        if (!isTtsEnabled || text.isBlank()) {
            return
        }
        val filteredText = filterTextForSpeech(text)
        if (filteredText.isBlank()) {
            return
        }

        val context = getApplication<Application>()
        val intent = Intent(context, TtsService::class.java).apply {
            action = TtsService.ACTION_SPEAK
            putExtra(TtsService.EXTRA_TEXT, filteredText)
        }
        context.startService(intent)
    }

    fun clearPendingText() {
        _pendingTextToPrint.value = ""
    }

    fun setCloudReady(modelId: String) {
        _cloudState.value = CloudAIState.Ready(modelId)
        Log.d(TAG, "Cloud state set to Ready for model: $modelId")
    }

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

                val prompt = "Сожми кратко этот диалог. Выдели самое важное из того, о чём говорили. Сохрани факты, договорённости, планы, имена, пароли, контакты, цены, адреса — всё что может понадобиться в будущем. Пиши тезисно, строго по одной строке на факт. Без лишних слов.\n\n$dialogueText"

                isCompressionRequest = true

                appendSystemMessage("🧠 ИИ запоминает нашу беседу...")

                if (_isModelLoaded.value && llamaHelper.getContextId() != null) {
                    llamaHelper.predict(
                        prompt = prompt,
                        imagePath = null,
                        systemPrompt = "Ты — полезный, умный и лаконичный ИИ-ассистент. Отвечай строго на русском языке.",
                        maxTokens = 512
                    )
                } else if (cloudAIProvider.isConfigured()) {
                    cloudAIProvider.generate(
                        prompt = prompt,
                        systemPrompt = "Ты — полезный, умный и лаконичный ИИ-ассистент. Отвечай строго на русском языке.",
                        chatHistory = history,
                        maxTokens = maxTokens.value
                    )
                } else {
                    isCompressionRequest = false
                    appendSystemMessage("⚠️ Нет активного ИИ для запоминания")
                }
            } catch (e: Exception) {
                isCompressionRequest = false
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

    fun updateAssistantMessage(newText: String) {
        val currentList = _chatHistory.value.toMutableList()
        if (currentList.isNotEmpty() && currentList.last().role == "assistant") {
            currentList[currentList.lastIndex] = ChatMessage("assistant", newText)
            _chatHistory.value = currentList
        } else {
            _chatHistory.value = currentList + ChatMessage("assistant", newText)
        }
    }

        private fun determineCategory(text: String): String {
        val lowerText = text.lowercase()

        // Если строка содержит цену — это прайс, независимо от слов
        val hasPrice = Regex("\\d+[.,]?\\d*\\s*(р|руб|₽)").containsMatchIn(lowerText) ||
                lowerText.contains("р/м") ||
                lowerText.contains("р/кг") ||
                lowerText.contains("р/шт") ||
                lowerText.contains("р/лист") ||
                lowerText.contains("р/пм") ||
                lowerText.contains("р/т")

        if (hasPrice) return "[ПРАЙС]"

        val categories = mapOf(
            "[ПАРОЛЬ]" to listOf("пароль", "логин", "доступ", "код", "пин", "секрет", "ключ"),
            "[КОНТАКТ]" to listOf("телефон", "номер", "контакт", "позвонить", "мобильный", "вотсап", "телеграм"),
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

    private fun appendToCategory(text: String) {
        try {
            if (!memoryFile.exists()) {
                memoryFile.createNewFile()
            }

            val category = determineCategory(text)
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            val timestamp = dateFormat.format(Date())

            val existing = memoryFile.readText()
            val lines = existing.split("\n").toMutableList()

            val categoryIndex = lines.indexOfFirst { it.trim() == category }

            if (categoryIndex == -1) {
                lines.add("")
                lines.add(category)
                lines.add("[$timestamp] $text")
            } else {
                var insertIndex = categoryIndex + 1
                while (insertIndex < lines.size && lines[insertIndex].isNotBlank() && !CATEGORIES.any { lines[insertIndex].trim().startsWith(it) }) {
                    insertIndex++
                }
                lines.add(insertIndex, "[$timestamp] $text")
            }

            memoryFile.writeText(lines.joinToString("\n"))
            Log.d(TAG, "Записано в категорию $category: $text")
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка записи в категорию: ${e.message}")
        }
    }

    fun saveToLongTermMemory(text: String) {
        appendToCategory(text)
        appendSystemMessage("🧠 Запомнено: $text")
    }

    private fun getCategorySection(category: String): String {
        val fullMemory = readFromLongTermMemory()
        if (fullMemory.isEmpty()) return ""

        val lines = fullMemory.split("\n")
        val categoryIndex = lines.indexOfFirst { it.trim() == category }
        if (categoryIndex == -1) return ""

        val sectionLines = mutableListOf<String>()
        var i = categoryIndex + 1
        while (i < lines.size && !CATEGORIES.any { lines[i].trim().startsWith(it) }) {
            if (lines[i].isNotBlank()) {
                sectionLines.add(lines[i])
            }
            i++
        }

        return sectionLines.joinToString("\n")
    }

    private fun extractNumbers(query: String): List<String> {
        return Regex("\\d+[.,]?\\d*").findAll(query).map { it.value }.toList()
    }

    private fun extractUnits(query: String): List<String> {
        val units = listOf("р/м²", "р/пм", "р/кг", "р/шт", "р/лист", "р/тонна", "р/т", "р/м", "руб")
        return units.filter { query.contains(it, ignoreCase = true) }
    }

    private fun getSynonyms(word: String): List<String> {
        return when (word) {
            "плитк" -> listOf("плитк", "кафел", "керамогранит", "мозаик")
            "покраск" -> listOf("покраск", "окраск", "малярн")
            "труб" -> listOf("труб", "вгп", "э/с", "профильн")
            "лист" -> listOf("лист", "листов")
            "металл" -> listOf("металл", "стальн")
            "укладк" -> listOf("укладк", "монтаж", "установк")
            else -> listOf(word)
        }
    }

    private fun searchMemory(query: String, category: String? = null): String {
        val fullMemory = readFromLongTermMemory()
        if (fullMemory.isEmpty()) return ""

        val lines = fullMemory.split("\n")

        if (category != null) {
            val categoryIndex = lines.indexOfFirst { it.trim() == category }
            if (categoryIndex != -1) {
                val sectionLines = mutableListOf<String>()
                var i = categoryIndex + 1
                while (i < lines.size && !CATEGORIES.any { lines[i].trim().startsWith(it) }) {
                    if (lines[i].isNotBlank()) {
                        sectionLines.add(lines[i])
                    }
                    i++
                }

                val keywords = extractKeywords(query)
                val numbers = extractNumbers(query)
                val units = extractUnits(query)

                val expandedKeywords = keywords.flatMap { getSynonyms(it) }.distinct()

                if (expandedKeywords.isEmpty() && numbers.isEmpty() && units.isEmpty()) {
                    return sectionLines.joinToString("\n")
                }

                // Поэтапное сужение

                // Этап 1: строки, содержащие ВСЕ ключевые слова
                var filtered = sectionLines.filter { line ->
                    val lowerLine = line.lowercase()
                    expandedKeywords.all { keyword -> lowerLine.contains(keyword) }
                }

                // Этап 2: из них — строки с числами
                if (numbers.isNotEmpty()) {
                    val withNumbers = filtered.filter { line ->
                        numbers.all { number -> line.contains(number) }
                    }
                    if (withNumbers.isNotEmpty()) {
                        filtered = withNumbers
                    }
                }

                // Этап 3: из них — строки с единицами измерения
                if (units.isNotEmpty()) {
                    val withUnits = filtered.filter { line ->
                        units.any { unit -> line.lowercase().contains(unit) }
                    }
                    if (withUnits.isNotEmpty()) {
                        filtered = withUnits
                    }
                }

                if (filtered.isNotEmpty()) {
                    // Ищем заголовок подкатегории
                    val firstMatchIndex = sectionLines.indexOfFirst { line -> filtered.contains(line) }
                    var headerLine = ""
                    if (firstMatchIndex > 0) {
                        for (idx in firstMatchIndex - 1 downTo 0) {
                            val candidate = sectionLines[idx]
                            val hasPrice = candidate.contains("р/") || candidate.contains("руб")
                            if (!hasPrice) {
                                headerLine = candidate
                                break
                            }
                        }
                    }

                    val resultLines = mutableListOf<String>()
                    if (headerLine.isNotEmpty()) {
                        resultLines.add(headerLine)
                    }
                    resultLines.addAll(filtered)
                    return resultLines.joinToString("\n")
                }

                // Этап 4: ослабляем — строки с хотя бы 2 ключевыми словами
                filtered = sectionLines.filter { line ->
                    val lowerLine = line.lowercase()
                    expandedKeywords.count { keyword -> lowerLine.contains(keyword) } >= 2
                }

                if (filtered.isNotEmpty()) {
                    return filtered.take(20).joinToString("\n")
                }

                // Этап 5: ещё слабее — строки с хотя бы 1 ключевым словом
                filtered = sectionLines.filter { line ->
                    val lowerLine = line.lowercase()
                    expandedKeywords.any { keyword -> lowerLine.contains(keyword) }
                }

                if (filtered.isNotEmpty()) {
                    return filtered.take(10).joinToString("\n")
                }
            }
        }

        return ""
    }

    private fun searchBrain(query: String): String {
        val brainData = readBrain()
        if (brainData.isEmpty()) return ""

        val lines = brainData.split("\n").filter { it.isNotEmpty() }
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

    private fun searchChat(query: String): String {
        val history = _chatHistory.value
        if (history.isEmpty()) return ""

        val keywords = extractKeywords(query)

        return if (keywords.isNotEmpty()) {
            history.filter { message ->
                val lowerText = message.text.lowercase()
                keywords.any { keyword -> lowerText.contains(keyword) }
            }.joinToString("\n") { message ->
                val prefix = when (message.role) {
                    "user" -> "Пользователь"
                    "assistant" -> "Ассистент"
                    else -> "Система"
                }
                "$prefix: ${message.text}"
            }
        } else {
            ""
        }
    }

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

    private fun buildSystemPrompt(commandType: String, prompt: String): String {
        val basePrompt = _systemPrompt.value
        val lowerPrompt = prompt.lowercase()

        return buildString {
            append(basePrompt)
            append("\n\n")

            when {
                lowerPrompt.contains(CHAT_LOOKUP_COMMAND) -> {
                    val fullChatHistory = _chatHistory.value.joinToString("\n") { message ->
                        val prefix = when (message.role) {
                            "user" -> "Пользователь"
                            "assistant" -> "Ассистент"
                            else -> "Система"
                        }
                        "$prefix: ${message.text}"
                    }
                    if (fullChatHistory.isNotEmpty()) {
                        append("ПОЛНАЯ ИСТОРИЯ ЧАТА:\n$fullChatHistory\n\n")
                        append("Пользователь просит найти информацию в истории чата. Изучи ВСЮ историю чата выше и найди то, что относится к запросу пользователя. Используй найденные сообщения для ответа.")
                    } else {
                        append("ПОЛНАЯ ИСТОРИЯ ЧАТА: пока пусто.\n\n")
                        append("Пользователь просит найти информацию в истории чата, но история пуста. Честно скажи об этом.")
                    }
                }
                lowerPrompt.contains(RECALL_COMMAND) -> {
                    val brainData = readBrain()
                    if (brainData.isNotEmpty()) {
                        append("СЖАТАЯ ИСТОРИЯ ПРОШЛЫХ РАЗГОВОРОВ (МОЗГ):\n$brainData\n\n")
                        append("Пользователь просит вспомнить информацию из прошлых разговоров. Изучи ВЕСЬ сжатый архив выше и найди то, что относится к запросу пользователя. Используй найденные факты для ответа.")
                    } else {
                        append("СЖАТАЯ ИСТОРИЯ ПРОШЛЫХ РАЗГОВОРОВ (МОЗГ): пока пусто.\n\n")
                        append("Пользователь просит вспомнить информацию, но сжатой истории пока нет. Честно скажи об этом.")
                    }
                }
                else -> {
                    val queryCategory = determineCategory(prompt)
                    val filteredMemory = searchMemory(prompt, queryCategory)

                    if (filteredMemory.isNotEmpty()) {
                        append("ЛОКАЛЬНАЯ БАЗА ЗНАНИЙ (категория $queryCategory):\n$filteredMemory\n\n")
                        append("Пользователь просит найти информацию в базе знаний. Изучи найденные факты выше и используй их для ответа.")
                    } else {
                        append("ЛОКАЛЬНАЯ БАЗА ЗНАНИЙ: подходящих фактов не найдено.\n\n")
                        append("Честно скажи, что в базе знаний нет информации по запросу, и попроси пользователя перефразировать запрос.")
                    }
                }
            }
        }
    }

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
                    val queryCategory = determineCategory(text)
                    val memoryData = searchMemory(text, queryCategory)
                    if (memoryData.isNotEmpty()) {
                        appendSystemMessage("🔍 Найдено в категории $queryCategory:\n$memoryData")
                    } else {
                        appendSystemMessage("🔍 Ничего не найдено. Перефразируйте запрос.")
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

        val fullSystemPrompt = if (isSearchCommand) {
            buildSystemPrompt("search", prompt)
        } else {
            _systemPrompt.value
        }

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

        val effectivePrompt = if (imagePath != null && prompt.isBlank()) {
            "Опиши подробно, что изображено на этой картинке. Опиши все объекты, людей, текст, цвета и обстановку."
        } else {
            prompt
        }

        val isSearchCommand = if (imagePath != null) {
            false
        } else {
            effectivePrompt.contains(FIND_COMMAND, ignoreCase = true) ||
                    effectivePrompt.contains(SEARCH_COMMAND, ignoreCase = true) ||
                    effectivePrompt.contains(RECALL_COMMAND, ignoreCase = true) ||
                    effectivePrompt.contains(CHAT_LOOKUP_COMMAND, ignoreCase = true)
        }

        val fullSystemPrompt = if (isSearchCommand) {
            buildSystemPrompt("search", effectivePrompt)
        } else {
            _systemPrompt.value
        }

        _generatedText.value = ""
        _state.value = GenerationState.Generating(prompt = effectivePrompt, tokensGenerated = 0)

        scope.launch {
            try {
                llamaHelper.predict(effectivePrompt, imagePath, fullSystemPrompt, maxTokens.value)
            } catch (e: Exception) {
                _state.value = GenerationState.Error(e.message ?: "Unknown error")
                _isModelLoaded.value = false
            }
        }
    }

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

    internal fun appendSystemMessage(text: String) {
        _chatHistory.value = _chatHistory.value + ChatMessage("system", text)
    }

    fun abortLocal() {
        if (_state.value.isActive()) {
            Log.i(TAG, "Aborting generation")
            llamaHelper.abort()
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

            val lines = newFullText.split("\n").filter { it.isNotBlank() }
            val categorizedLines = mutableListOf<String>()

            for (line in lines) {
                val category = determineCategory(line)
                categorizedLines.add(category)
                categorizedLines.add(line)
                categorizedLines.add("")
            }

            memoryFile.writeText(categorizedLines.joinToString("\n"))
            Log.d(TAG, "База знаний успешно обновлена и разделена по категориям")
            appendSystemMessage("🧠 База знаний обновлена и разделена по категориям")
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
        _loadedModelName.value = ""
        llamaHelper.release()
    }

    override fun onCleared() {
        super.onCleared()
        instance = null
        _isTtsReady.value = false
        _isSpeaking.value = false
        _isModelLoaded.value = false
        llamaHelper.abort()
        llamaHelper.release()
        viewModelJob.cancel()

        try {
            val ttsStopIntent = Intent(getApplication<Application>(), TtsService::class.java)
            getApplication<Application>().stopService(ttsStopIntent)
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping TTS service: ${e.message}")
        }

        try {
            getApplication<Application>().unregisterReceiver(ttsReceiver)
        } catch (e: Exception) {
            Log.e(TAG, "Error unregistering TTS receiver: ${e.message}")
        }
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
