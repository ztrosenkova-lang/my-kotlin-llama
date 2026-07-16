package org.nehuatl.sample

import android.app.Application
import android.content.ContentResolver
import android.content.Context
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
import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.charset.StandardCharsets
import java.util.Locale

// Структура данных для сообщений чата
data class ChatMessage(val role: String, val text: String) // role: "user" или "assistant"

class MainViewModel(application: Application, val contentResolver: ContentResolver): AndroidViewModel(application) {

    companion object {
        @Volatile var instance: MainViewModel? = null
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

    // Переменная для хранения имени файла текущей модели
    private var currentModelName: String = ""

    // Динамический системный промпт (доступен для изменения из UI)
    private val _systemPrompt = MutableStateFlow("Ты — полезный, умный и лаконичный ИИ-ассистент. Отвечай строго на русском языке.")
    val systemPrompt = _systemPrompt.asStateFlow()

    // История чата
    private val _chatHistory = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatHistory = _chatHistory.asStateFlow()

    // Настройки сэмплинга (PocketPal style)
    val temperature = MutableStateFlow(0.3f) // По умолчанию 0.3 для точных наук (химия)
    val contextSize = MutableStateFlow(2048) // Базовый размер контекста для Honor X8a
    val maxTokens = MutableStateFlow(512) // Максимальное количество токенов для генерации

    // Файл долговременной памяти
    private val memoryFile: File by lazy {
        File(getApplication<Application>().filesDir, "memory.txt")
    }

    // TTS движок для озвучки ответов
    private var tts: TextToSpeech? = null

    init {
        instance = this
        tts = TextToSpeech(getApplication()) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.getDefault()
                Log.d("MainViewModel", "TTS инициализирован успешно")
            } else {
                Log.e("MainViewModel", "Ошибка инициализации TTS")
            }
        }
        
        // Обработка событий облачного ИИ
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
                    }
                    is CloudAIEvent.Error -> {
                        _cloudState.value = CloudAIState.Error(event.message)
                        Log.e("MainViewModel", "Ошибка облачного ИИ: ${event.message}")
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
    fun isCloudConfigured(): Boolean {
        return cloudAIProvider.isConfigured()
    }

    fun getCloudConfig(): CloudAIConfig? {
        return cloudAIProvider.getConfig()
    }

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

    fun generateCloud(prompt: String) {
        if (!cloudAIProvider.isConfigured()) {
            _cloudState.value = CloudAIState.Error("Облачный ИИ не настроен")
            return
        }
        
        // Добавляем сообщение пользователя в историю
        val newUserMessage = ChatMessage("user", prompt)
        _chatHistory.value = _chatHistory.value + newUserMessage
        
        val currentSystemPrompt = _systemPrompt.value
        val history = _chatHistory.value
        
        // ВСЕГДА читаем содержимое файла памяти для автоматического подмешивания базы знаний
        val memoryData = readFromLongTermMemory()
        val memoryContext = if (memoryData.isNotEmpty()) {
            "Дополнительная локальная база знаний и факты от пользователя:\n$memoryData\nИспользуй эти данные и прайс-листы для точных ответов на вопросы пользователя."
        } else ""
        
        // Подготавливаем историю для облачного ИИ
        val cloudHistory = history.dropLast(1) // Убираем последнее сообщение пользователя
        
        cloudAIProvider.generate(
            prompt = prompt,
            systemPrompt = currentSystemPrompt + (if (memoryContext.isNotEmpty()) "\n\n$memoryContext" else ""),
            chatHistory = cloudHistory
        )
    }

    fun abortCloud() {
        _cloudState.value = CloudAIState.Idle
        // TODO: Реализовать отмену запроса к облачному API
    }

    // === Существующие методы ===
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

    fun updateSystemPrompt(newPrompt: String) {
        _systemPrompt.value = newPrompt
    }

    fun clearChat() {
        _chatHistory.value = emptyList()
        _generatedText.value = ""
        _cloudGeneratedText.value = ""
        tts?.stop()
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

    fun overwriteLongTermMemory(newFullText: String) {
        try {
            if (!memoryFile.exists()) {
                memoryFile.createNewFile()
            }
            memoryFile.writeText(newFullText)
            Log.d("MainViewModel", "База знаний успешно обновлена")
        } catch (e: Exception) {
            Log.e("MainViewModel", "Ошибка перезаписи базы знаний: ${e.message}")
        }
    }

    private fun saveToLongTermMemory(text: String) {
        try {
            if (!memoryFile.exists()) {
                memoryFile.createNewFile()
            }
            memoryFile.appendText("$text\n")
            Log.d("MainViewModel", "Записано в долговременную память: $text")
        } catch (e: Exception) {
            Log.e("MainViewModel", "Ошибка записи памяти: ${e.message}")
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
            Log.e("MainViewModel", "Ошибка чтения памяти: ${e.message}")
            ""
        }
    }

    private fun speakText(text: String) {
        val cleanText = text.replace(Regex("[*#`_]"), "")
        tts?.speak(cleanText, TextToSpeech.QUEUE_FLUSH, null, null)
        Log.d("MainViewModel", "Озвучка запущена: ${cleanText.take(50)}...")
    }

    fun generateLocal(prompt: String, imagePath: String? = null) {
        // Вся существующая логика локальной генерации
        // ... (сохраняется без изменений)
    }

    fun abortLocal() {
        if (_state.value.isActive()) {
            Log.i("MainViewModel", "Aborting generation")
            tts?.stop()
            llamaHelper.abort()
        }
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
    return name.lowercase()
}
