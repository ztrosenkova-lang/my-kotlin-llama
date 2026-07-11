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
                // Передаем параметры по правилам локального модуля с обязательной лямбдой loaded
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

    fun generate(prompt: String, imagePath: String? = null) {
        if (!_state.value.canGenerate()) return
        scope.launch {
            // Читаем актуальное значение системного промпта
            val currentSystemPrompt = _systemPrompt.value
            
            // Визуальный маркер для мультимодальных моделей
            val visualPrompt = if (!imagePath.isNullOrEmpty()) {
                "<|vision_start|><|image_pad|><|vision_end|>$prompt"
            } else {
                prompt
            }
            
            // Динамический выбор промпта и стоп-токенов
            val (formattedPrompt, stopTokensList) = when {
                currentModelName.contains("qwen") -> {
                    "<|im_start|>system\n$currentSystemPrompt<|im_end|>\n<|im_start|>user\n$visualPrompt<|im_end|>\n<|im_start|>assistant\n" to listOf("<|im_end|>", "<|im_start|>")
                }
                currentModelName.contains("llama") -> {
                    "<|start_header_id|>system<|end_header_id|>\n\n$currentSystemPrompt<|eot_id|><|start_header_id|>user<|end_header_id|>\n\n$visualPrompt<|eot_id|><|start_header_id|>assistant<|end_header_id|>\n\n" to listOf("<|eot_id|>", "<|start_header_id|>")
                }
                else -> {
                    "<|system|>\n$currentSystemPrompt\n<|user|>\n$visualPrompt\n<|assistant|>\n" to listOf("<|user|>", "<|eot_id|>")
                }
            }

            _state.value = GenerationState.Generating(
                prompt = prompt,
                startTime = System.currentTimeMillis(),
                tokensGenerated = 0
            )
            _generatedText.value = ""

            // Принудительно очищаем буфер перед новым вопросом
            llamaHelper.abort()

            // Оригинальный мультимодальный вызов predict через строковый путь к изображению
            llamaHelper.predict(prompt = formattedPrompt, imagePath = imagePath)

            llmFlow.collect { event ->
                when (event) {
                    is LlamaHelper.LLMEvent.Started -> {
                        Log.i("MainViewModel", "Generation started")
                    }
                    is LlamaHelper.LLMEvent.Ongoing -> {
                        val word = event.word

                        // 1. Мгновенная проверка на стоп-токены ролей (убраны все пробелы)
                        if (word.contains("<|") || word.contains("|>") || 
                            word.contains("User:") || word.contains("Assistant:") ||
                            word.contains("Question:") || word.contains("Answer:")) {
                            Log.i("MainViewModel", "Стоп-токен обнаружен. Остановка.")
                            _state.value = GenerationState.Completed(prompt, event.tokenCount, 0)
                            return@collect
                        }

                        // 2. Накапливаем и склеиваем поток букв, убирая микро-двоение
                        val currentText = _generatedText.value
                        
                        // Фильтруем системные маркеры, которые модель пытается выдать в текст
                        if (!word.startsWith("<|") && !word.endsWith("|>")) {
                            _generatedText.value = currentText + word
                        }

                        // Обновляем счетчик токенов в состоянии
                        val currentState = _state.value
                        if (currentState is GenerationState.Generating) {
                            _state.value = currentState.copy(tokensGenerated = event.tokenCount)
                        }
                    }
                    is LlamaHelper.LLMEvent.Done -> {
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
