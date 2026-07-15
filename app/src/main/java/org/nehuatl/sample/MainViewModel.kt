package org.nehuatl.sample

import android.app.Application
import android.content.ContentResolver
import android.content.Intent
import android.os.SystemClock
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.nehuatl.llamacpp.LLamaContext // ПРАВИЛЬНЫЙ ИМПОРТ ИЗ ЯДРА
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.Locale

class MainViewModel(
    application: Application,
    val contentResolver: ContentResolver
) : AndroidViewModel(application), TextToSpeech.OnInitListener {

    private val scope = CoroutineScope(Dispatchers.IO)
    private val _llmFlow = MutableSharedFlow<LLamaContext.LLMEvent>()

    private val _state = MutableStateFlow<GenerationState>(GenerationState.Idle)
    val state: StateFlow<GenerationState> = _state.asStateFlow()

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    var contextSize = mutableStateOf(2048)
    var currentModelName = mutableStateOf("Модель не выбрана")
    private var tts: TextToSpeech? = null
    private var isTtsReady = false

    private val llamaHelper by lazy {
        LLamaContext(contentResolver)
    }

    init {
        tts = TextToSpeech(application, this)
        setupFlowCollection()
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.let {
                val result = it.setLanguage(Locale("ru"))
                if (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED) {
                    isTtsReady = true
                }
            }
        }
    }

    fun scheduleInternalReminder(message: String, delayMs: Long) {
        viewModelScope.launch(Dispatchers.Default) {
            delay(delayMs)
            triggerVoiceAlarm(message)
        }
    }

    fun triggerVoiceAlarm(message: String) {
        GlobalScope.launch(Dispatchers.Main) {
            if (isTtsReady) {
                repeat(5) {
                    tts?.speak(message, TextToSpeech.QUEUE_FLUSH, null, null)
                    delay(4000)
                }
            }
        }
    }

    fun loadModel(path: String, mmprojPath: String? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            _state.value = GenerationState.Loading
            try {
                llamaHelper.load(path, contextSize.value, mmprojPath)
                _state.value = GenerationState.ModelLoaded(path)
                val file = File(path)
                currentModelName.value = file.name
            } catch (e: Exception) {
                _state.value = GenerationState.Error(e.message ?: "Ошибка загрузки")
            }
        }
    }

    fun clearChat() {
        _messages.value = emptyList()
        viewModelScope.launch(Dispatchers.IO) {
            try {
                llamaHelper.release()
            } catch (e: Exception) {
                Log.e("MainViewModel", "Ошибка очистки памяти", e)
            }
        }
    }

    fun searchInFullChatHistory(query: String): String {
        val cleanQuery = query.removePrefix("найди:").trim()
        if (cleanQuery.isEmpty()) return "Запрос пуст."
        val found = _messages.value.filter {
            it.text.contains(cleanQuery, ignoreCase = true) && !it.text.startsWith("найди:")
        }
        if (found.isEmpty()) return "Ничего не найдено по запросу: $cleanQuery"
        return found.joinToString("\n") { "${if (it.isUser) "Вы" else "ИИ"}: ${it.text}" }
    }

    fun sendPrompt(prompt: String, imagePath: String? = null) {
        if (prompt.isBlank() && imagePath == null) return

        if (prompt.trim().startsWith("найди:")) {
            val userMsg = ChatMessage(text = prompt, isUser = true)
            _messages.value = _messages.value + userMsg
            val searchResult = searchInFullChatHistory(prompt)
            val aiMsg = ChatMessage(text = searchResult, isUser = false)
            _messages.value = _messages.value + aiMsg
            triggerVoiceAlarm(searchResult)
            return
        }

        val userMessage = ChatMessage(text = prompt, isUser = true, imagePath = imagePath)
        _messages.value = _messages.value + userMessage

        val systemPrompt = "Ты — Меч Правды v2.0, автономный голосовой ассистент. Отвечай кратко, емко, строго на русском языке."
        val limitedHistory = _messages.value.takeLast(8)

        val fullPromptBuilder = StringBuilder()
        fullPromptBuilder.append("<|im_start|>system\n").append(systemPrompt).append("<|im_end|>\n")
        for (msg in limitedHistory) {
            val role = if (msg.isUser) "user" else "assistant"
            fullPromptBuilder.append("<|im_start|>").append(role).append("\n").append(msg.text).append("<|im_end|>\n")
        }
        fullPromptBuilder.append("<|im_start|>assistant\n")

        _state.value = GenerationState.Generating("")

        viewModelScope.launch(Dispatchers.IO) {
            try {
                llamaHelper.predict(fullPromptBuilder.toString(), imagePath)
            } catch (e: Exception) {
                _state.value = GenerationState.Error(e.message ?: "Ошибка генерации")
            }
        }
    }

    private fun setupFlowCollection() {
        viewModelScope.launch(Dispatchers.IO) {
            val byteBuffer = ByteArrayOutputStream()
            var lastUpdateTime = 0L

            llamaHelper.state.collect { helperState ->
                // Синхронизация состояний из оригинального класса ядра
                when (helperState) {
                    is org.nehuatl.llamacpp.GenerationState.Generating -> {
                        val token = helperState.text
                        byteBuffer.write(token.toByteArray(Charsets.UTF_8))
                        val currentText = byteBuffer.toString("UTF-8")
                        
                        val now = SystemClock.uptimeMillis()
                        if (now - lastUpdateTime > 64) {
                            _state.value = GenerationState.Generating(currentText)
                            lastUpdateTime = now
                        }
                    }
                    is org.nehuatl.llamacpp.GenerationState.Success -> {
                        val finalResult = byteBuffer.toString("UTF-8")
                        _messages.value = _messages.value + ChatMessage(text = finalResult, isUser = false)
                        _state.value = GenerationState.ModelLoaded(currentModelName.value)
                        triggerVoiceAlarm(finalResult)
                        byteBuffer.reset()
                    }
                    is org.nehuatl.llamacpp.GenerationState.Error -> {
                        _state.value = GenerationState.Error(helperState.message)
                        byteBuffer.reset()
                    }
                    else -> {}
                }
            }
        }
    }

    fun abortGeneration() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                llamaHelper.abort()
            } catch (e: Exception) {
                Log.e("MainViewModel", "Ошибка остановки", e)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        tts?.stop()
        tts?.shutdown()
    }
}

sealed interface GenerationState {
    object Idle : GenerationState
    object Loading : GenerationState
    data class ModelLoaded(val path: String) : GenerationState
    data class Generating(val text: String) : GenerationState
    data class Error(val message: String) : GenerationState
}

data class ChatMessage(
    val text: String,
    val isUser: Boolean,
    val imagePath: String? = null
)
