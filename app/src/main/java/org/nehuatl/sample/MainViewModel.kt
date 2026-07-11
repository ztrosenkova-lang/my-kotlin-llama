package org.nehuatl.sample

import android.content.ContentResolver
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
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

// Структура данных для сообщений чата
data class ChatMessage(val role: String, val text: String) // role: "user" или "assistant"

class MainViewModel(val contentResolver: ContentResolver): ViewModel() {

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
    
    // Переменная для хранения имени файла текущей модели
    private var currentModelName: String = ""

    // Динамический системный промпт (доступен для изменения из UI)
    private val _systemPrompt = MutableStateFlow("Ты — полезный, умный и лаконичный ИИ-ассистент. Отвечай строго на русском языке.")
    val systemPrompt = _systemPrompt.asStateFlow()

    // История чата
    private val _chatHistory = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatHistory = _chatHistory.asStateFlow()

    // Файл долговременной памяти
    private val memoryFile: File by lazy {
        val context = androidx.core.app.CoreComponentFactory().createContextForApplication(androidx.core.app.CoreComponentFactory())
        File(context.filesDir, "memory.txt")
    }

    private val llamaHelper by lazy {
        LlamaHelper(
            contentResolver = contentResolver,
            scope = scope,
            sharedFlow = _llmFlow,
        )
    }

    fun loadModel(path: String, mmprojPath: String? = null) {
        if (path.isEmpty()) return
        _state.value = GenerationState.LoadingModel
        scope.launch {
            try {
                llamaHelper.load(
                    path = path,
                    contextLength = 2048,
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
    }

    // Функция записи новой заметки в файл долговременной памяти
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

    // Функция чтения всех сохраненных заметок из файла долговременной памяти
    private fun readFromLongTermMemory(): String {
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

    fun generate(prompt: String, imagePath: String? = null) {
        if (!_state.value.canGenerate()) return
        scope.launch {
            val cleanPrompt = prompt.trim()
            val lowerPrompt = cleanPrompt.lowercase()

            // Перехват команды ЗАПОМНИ
            if (lowerPrompt.startsWith("запомни")) {
                val textToRemember = cleanPrompt.substringAfter("запомни").trim()
                if (textToRemember.isNotEmpty()) {
                    saveToLongTermMemory(textToRemember)
                    _chatHistory.value = _chatHistory.value + ChatMessage("user", prompt)
                    _chatHistory.value = _chatHistory.value + ChatMessage("assistant", "Я успешно записал это в свою долговременную память! Теперь я буду это знать.")
                }
                return@launch
            }

            // Добавляем сообщение пользователя в историю
            val newUserMessage = ChatMessage("user", prompt)
            _chatHistory.value = _chatHistory.value + newUserMessage

            val currentSystemPrompt = _systemPrompt.value
            val history = _chatHistory.value

            // Проверяем, есть ли команда "вспомни" для подмешивания долговременной памяти
            val longTermMemoryData = if (lowerPrompt.contains("вспомни")) {
                readFromLongTermMemory()
            } else ""

            // Формируем промпт на основе всей истории с подмешиванием памяти
            val formattedPrompt = when {
                currentModelName.contains("qwen") -> {
                    val sb = StringBuilder()
                    sb.append("<|im_start|>system\n$currentSystemPrompt<|im_end|>\n")
                    if (longTermMemoryData.isNotEmpty()) {
                        sb.append("<|im_start|>system\nДополнительные сохраненные факты из памяти пользователя:\n$longTermMemoryData\nИспользуй эти факты для ответа на текущий вопрос.<|im_end|>\n")
                    }
                    history.forEach { msg ->
                        when (msg.role) {
                            "user" -> sb.append("<|im_start|>user\n${msg.text}<|im_end|>\n")
                            "assistant" -> sb.append("<|im_start|>assistant\n${msg.text}<|im_end|>\n")
                        }
                    }
                    sb.append("<|im_start|>assistant\n")
                    sb.toString()
                }
                currentModelName.contains("moondream") -> {
                    val sb = StringBuilder()
                    sb.append("$currentSystemPrompt\n\n")
                    if (longTermMemoryData.isNotEmpty()) {
                        sb.append("Дополнительные сохраненные факты из памяти пользователя:\n$longTermMemoryData\n\nИспользуй эти факты для ответа на текущий вопрос.\n\n")
                    }
                    history.forEach { msg ->
                        when (msg.role) {
                            "user" -> sb.append("Question: ${msg.text}\n\n")
                            "assistant" -> sb.append("Answer: ${msg.text}\n\n")
                        }
                    }
                    sb.append("Answer:")
                    sb.toString()
                }
                currentModelName.contains("llama") -> {
                    val sb = StringBuilder()
                    sb.append("<|start_header_id|>system<|end_header_id|>\n\n$currentSystemPrompt<|eot_id|>")
                    if (longTermMemoryData.isNotEmpty()) {
                        sb.append("<|start_header_id|>system<|end_header_id|>\n\nДополнительные сохраненные факты из памяти пользователя:\n$longTermMemoryData\nИспользуй эти факты для ответа на текущий вопрос.<|eot_id|>")
                    }
                    history.forEach { msg ->
                        when (msg.role) {
                            "user" -> sb.append("<|start_header_id|>user<|end_header_id|>\n\n${msg.text}<|eot_id|>")
                            "assistant" -> sb.append("<|start_header_id|>assistant<|end_header_id|>\n\n${msg.text}<|eot_id|>")
                        }
                    }
                    sb.append("<|start_header_id|>assistant<|end_header_id|>\n\n")
                    sb.toString()
                }
                else -> {
                    val sb = StringBuilder()
                    sb.append("<|system|>\n$currentSystemPrompt\n")
                    if (longTermMemoryData.isNotEmpty()) {
                        sb.append("<|system|>\nДополнительные сохраненные факты из памяти пользователя:\n$longTermMemoryData\nИспользуй эти факты для ответа на текущий вопрос.\n")
                    }
                    history.forEach { msg ->
                        when (msg.role) {
                            "user" -> sb.append("<|user|>\n${msg.text}\n")
                            "assistant" -> sb.append("<|assistant|>\n${msg.text}\n")
                        }
                    }
                    sb.append("<|assistant|>\n")
                    sb.toString()
                }
            }

            // Стоп-токены для каждой модели
            val stopTokensList = when {
                currentModelName.contains("moondream") -> listOf("Question:", "Answer:", "<|end|>", "<|user|>")
                currentModelName.contains("qwen") -> listOf("<|im_end|>", "<|im_start|>")
                currentModelName.contains("llama") -> listOf("<|eot_id|>", "<|start_header_id|>")
                else -> listOf("<|user|>", "<|eot_id|>")
            }

            _state.value = GenerationState.Generating(
                prompt = prompt,
                startTime = System.currentTimeMillis(),
                tokensGenerated = 0
            )
            _generatedText.value = ""

            llamaHelper.abort()
            llamaHelper.predict(prompt = formattedPrompt, imagePath = imagePath)

            llmFlow.collect { event ->
                when (event) {
                    is LlamaHelper.LLMEvent.Started -> {
                        Log.i("MainViewModel", "Generation started")
                    }
                    is LlamaHelper.LLMEvent.Ongoing -> {
                        val word = event.word

                        // Проверка на стоп-токены ролей
                        if (word.contains("<|") || word.contains("|>") || 
                            word.contains("User:") || word.contains("Assistant:") ||
                            word.contains("Question:") || word.contains("Answer:")) {
                            Log.i("MainViewModel", "Стоп-токен обнаружен. Остановка.")
                            val aiResponse = _generatedText.value
                            if (aiResponse.isNotEmpty()) {
                                _chatHistory.value = _chatHistory.value + ChatMessage("assistant", aiResponse)
                            }
                            _state.value = GenerationState.Completed(prompt, event.tokenCount, 0)
                            return@collect
                        }

                        val currentText = _generatedText.value
                        if (!word.startsWith("<|") && !word.endsWith("|>")) {
                            _generatedText.value = currentText + word
                        }

                        val currentState = _state.value
                        if (currentState is GenerationState.Generating) {
                            _state.value = currentState.copy(tokensGenerated = event.tokenCount)
                        }
                    }
                    is LlamaHelper.LLMEvent.Done -> {
                        val aiResponse = _generatedText.value
                        if (aiResponse.isNotEmpty()) {
                            _chatHistory.value = _chatHistory.value + ChatMessage("assistant", aiResponse)
                        }
                        _state.value = GenerationState.Completed(
                            prompt = prompt,
                            tokenCount = event.tokenCount,
                            durationMs = event.duration
                        )
                        Log.i("MainViewModel", "Generation completed")
                    }
                    is LlamaHelper.LLMEvent.Error -> {
                        _state.value = GenerationState.Error("Generation interrupted: ${event.message}")
                        Log.e("MainViewModel", "Generation interrupted ${event.message}")
                    }
                    else -> {}
                }
            }
        }
    }

    fun abort() {
        if (_state.value.isActive()) {
            Log.i("MainViewModel", "Aborting generation")
            llamaHelper.abort()

            val currentState = _state.value
            if (currentState is GenerationState.Generating) {
                val duration = System.currentTimeMillis() - currentState.startTime
                _state.value = GenerationState.Completed(
                    prompt = currentState.prompt,
                    tokenCount = currentState.tokensGenerated,
                    durationMs = duration
                )
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        llamaHelper.abort()
        llamaHelper.release()
        viewModelJob.cancel()
    }
}

// Вспомогательная функция для получения реального имени файла из URI Android
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
    // Если через cursor не нашлось, берем последний сегмент ссылки
    if (name.isEmpty()) {
        name = uri.lastPathSegment ?: ""
    }
    return name.lowercase()
}
