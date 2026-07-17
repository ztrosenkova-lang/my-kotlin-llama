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
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.nehuatl.llamacpp.LlamaHelper
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

data class ChatMessage(val role: String, val text: String)

class MainViewModel(application: Application, val contentResolver: ContentResolver) : AndroidViewModel(application) {

    companion object {
        @Volatile var instance: MainViewModel? = null
        private const val TAG = "MainViewModel"
        private const val REMEMBER_COMMAND = "сделай выводы и запомни"
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

    // Файлы памяти
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

        // Сбор событий от облачного ИИ
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
                            _chatHistory.value = _chatHistory.value + ChatMessage("assistant", fullText)
                            speakText(fullText)
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

        // Сбор событий от локального ИИ
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
                            _chatHistory.value = _chatHistory.value + ChatMessage("assistant", fullText)
                            speakText(fullText)
                            // Проверяем, нужно ли запомнить (команда "сделай выводы и запомни")
                            val lastUserMessage = _chatHistory.value.lastOrNull { it.role == "user" }?.text ?: ""
                            if (lastUserMessage.contains(REMEMBER_COMMAND, ignoreCase = true)) {
                                saveToLongTermMemory(fullText)
                            }
                        }
                        _generatedText.value = fullText
                    }
                    is LlamaHelper.LLMEvent.Error -> {
                        _state.value = GenerationState.Error(event.message)
                        Log.e(TAG, "Ошибка локального ИИ: ${event.message}")
                    }
                    is LlamaHelper.LLMEvent.Loaded -> {
                        _state.value = GenerationState.ModelLoaded(event.path)
                    }
                }
            }
        }
    }

    private val llamaHelper by lazy {
        LlamaHelper(
            contentResolver = contentResolver,
            scope = scope,
            sharedFlow = _llmFlow,
        )
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
    }

    fun generateCloudToken(callback: (Boolean) -> Unit) {
        scope.launch {
            val success = cloudAIProvider.generateToken()
            callback(success)
        }
    }

    fun generateCloud(prompt: String) {
        if (!cloudAIProvider.isConfigured()) {
            _cloudState.value = CloudAIState.Error("Облачный ИИ не настроен")
            return
        }

        val newUserMessage = ChatMessage("user", prompt)
        _chatHistory.value = _chatHistory.value + newUserMessage

        val currentSystemPrompt = _systemPrompt.value
        val history = _chatHistory.value
        val memoryData = readFromLongTermMemory()
        val brainData = readBrain()
        val memoryContext = buildString {
            if (memoryData.isNotEmpty()) {
                append("Дополнительная локальная база знаний и факты от пользователя:\n$memoryData\n")
            }
            if (brainData.isNotEmpty()) {
                append("Важные выводы из прошлых разговоров (мозг):\n$brainData\n")
            }
        }

        val cloudHistory = history.dropLast(1)
        cloudAIProvider.generate(
            prompt = prompt,
            systemPrompt = currentSystemPrompt + (if (memoryContext.isNotEmpty()) "\n\n$memoryContext" else ""),
            chatHistory = cloudHistory
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
        scope.launch {
            try {
                llamaHelper.load(
                    path = path,
                    contextLength = contextSize.value,
                    mmprojPath = if (mmprojPath.isNullOrEmpty()) null else mmprojPath,
                    loaded = { id ->
                        _state.value = GenerationState.ModelLoaded(path)
                        val uri = Uri.parse(path)
                        currentModelName = getFileNameFromUri(contentResolver, uri)
                    }
                )
            } catch (e: Exception) {
                _state.value = GenerationState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun generateLocal(prompt: String, imagePath: String? = null) {
        // Проверяем, загружена ли модель
        if (llamaHelper.getContextId() == null) {
            _state.value = GenerationState.Error("Модель не загружена. Загрузите модель через 'движок'.")
            return
        }

        // Обработка специальных команд
        if (prompt.lowercase().contains(REMEMBER_COMMAND)) {
            // Команда "сделай выводы и запомни" — обрабатываем отдельно через облачный ИИ или локальный
            analyzeAndRemember(prompt)
            return
        }

        if (prompt.lowercase().contains("будильник") || prompt.lowercase().contains("напомни")) {
            // Обработка команды будильника
            handleAlarmCommand(prompt)
            return
        }

        // Обычная генерация
        val newUserMessage = ChatMessage("user", prompt)
        _chatHistory.value = _chatHistory.value + newUserMessage
        _generatedText.value = ""
        _state.value = GenerationState.Generating(prompt = prompt, tokensGenerated = 0)

        scope.launch {
            try {
                // ПЕРЕДАЕМ СИСТЕМНЫЙ ПРОМПТ В ЛОКАЛЬНУЮ МОДЕЛЬ
                llamaHelper.predict(prompt, imagePath, _systemPrompt.value)
            } catch (e: Exception) {
                _state.value = GenerationState.Error(e.message ?: "Unknown error")
            }
        }
    }

    private fun analyzeAndRemember(prompt: String) {
        // Логика для команды "сделай выводы и запомни"
        val history = getFullChatHistory()
        if (history.isEmpty()) {
            _state.value = GenerationState.Error("Нет истории для анализа")
            return
        }

        // Используем локальную модель для анализа, если она загружена
        if (llamaHelper.getContextId() != null) {
            _state.value = GenerationState.Generating(prompt = prompt, tokensGenerated = 0)
            scope.launch {
                try {
                    val analysisPrompt = "Проанализируй историю нашего разговора и сделай краткие выводы. Выдели самую важную информацию, факты и инсайты. Ответ должен быть кратким (не более 3-5 предложений):\n\n$history"
                    llamaHelper.predict(analysisPrompt, null, _systemPrompt.value)
                } catch (e: Exception) {
                    _state.value = GenerationState.Error(e.message ?: "Unknown error")
                }
            }
        } else {
            // Если локальная модель не загружена, используем облачную
            generateCloud("Сделай краткие выводы из этого диалога и запомни их. Только суть, факты, важные детали:\n$history")
        }
    }

    private fun handleAlarmCommand(prompt: String) {
        // Парсим время из команды "в 18.00 идем в гараж" или "напомни в 09.30 сдать отчет"
        val timePattern = Regex("(?:в|в\\s+|напомни\\s+в\\s+)(\\d{1,2}[:.]\\d{2})")
        val match = timePattern.find(prompt)
        if (match != null) {
            val timeStr = match.groupValues[1].replace(".", ":")
            val message = prompt.replace(Regex("(?:в\\s+|напомни\\s+в\\s+)\\d{1,2}[:.]\\d{2}\\s*"), "").trim()
            setAlarm(timeStr, message)
            appendSystemMessage("⏰ Будильник установлен на $timeStr: '$message'")
        } else {
            appendSystemMessage("⚠️ Не удалось распознать время. Используйте формат: 'в 18.00 идем в гараж'")
        }
    }

    private fun setAlarm(timeStr: String, message: String) {
        try {
            val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            val alarmTime = dateFormat.parse(timeStr) ?: return
            val calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, alarmTime.hours)
                set(Calendar.MINUTE, alarmTime.minutes)
                set(Calendar.SECOND, 0)
                // Если время уже прошло сегодня, устанавливаем на завтра
                if (timeInMillis < System.currentTimeMillis()) {
                    add(Calendar.DAY_OF_YEAR, 1)
                }
            }

            val intent = Intent(getApplication(), AlarmReceiver::class.java).apply {
                putExtra("MESSAGE", message)
                putExtra("TIME", timeStr)
            }
            val pendingIntent = PendingIntent.getBroadcast(
                getApplication(),
                System.currentTimeMillis().toInt(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
            Log.d(TAG, "Будильник установлен на $timeStr: $message")
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
    private fun saveToLongTermMemory(text: String) {
        try {
            if (!memoryFile.exists()) {
                memoryFile.createNewFile()
            }
            // Добавляем новую запись с датой
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

    // Работа с мозгом (краткие выводы)
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

    override fun onCleared() {
        super.onCleared()
        instance = null
        tts?.stop()
        tts?.shutdown()
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
    // Убираем префикс "primary%3AModels%" если он есть
    val cleanName = name.replace(Regex("^primary%3AModels%"), "").replace(Regex("^primary:Models:"), "")
    return cleanName
}

// Дополнительный класс для приема будильника
class AlarmReceiver : android.content.BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val message = intent.getStringExtra("MESSAGE") ?: "Напоминание!"
        val time = intent.getStringExtra("TIME") ?: ""
        // Показываем уведомление и говорим текст
        val notificationText = "⏰ Будильник ($time): $message"
        
        // Отправляем сообщение в чат через ViewModel
        MainViewModel.instance?.let { vm ->
            vm.appendSystemMessage(notificationText)
            // Проговариваем 5 раз
            repeat(5) {
                vm.speakText(notificationText)
                Thread.sleep(1000)
            }
        }
    }
}
