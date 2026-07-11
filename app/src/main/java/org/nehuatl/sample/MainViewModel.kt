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

    private val llamaHelper by lazy {
        LlamaHelper(
            contentResolver = contentResolver,
            scope = scope,
            sharedFlow = _llmFlow,
        )
    }

    fun loadModel(path: String) {
        if (path.isEmpty()) return
        _state.value = GenerationState.LoadingModel
        scope.launch {
            try {
                // Передаем параметры именно так, как требует компилятор библиотеки версии 0.4.0
                llamaHelper.load(
                    path = path,
                    contextLength = 2048,
                    loaded = { id ->
                        _state.value = GenerationState.ModelLoaded(path)
                        Log.d("MainViewModel", "Gemma 4 успешно инициализирована с ID: $id")
                    }
                )
            } catch (e: Exception) {
                Log.e("MainViewModel", "Ошибка загрузки: ${e.message}")
                _state.value = GenerationState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun generate(prompt: String, imagePath: String? = null) {
        if (!_state.value.canGenerate()) {
            Log.w("MainViewModel", "Cannot generate in current state: ${_state.value}")
            return
        }

        scope.launch {
            Log.d("MainViewModel", "Generating with image: $imagePath")
            
            // Системный промпт для настройки поведения ИИ
            val systemPrompt = "Ты — полезный, умный и лаконичный ИИ-ассистент. Отвечай всегда строго на русском языке."
            
            // Обертывание промпта для соответствия Chat Template
            val formattedPrompt = "<|system|>\n$systemPrompt\n<|user|>\n$prompt\n<|assistant|>\n"
            
            // Set initial generating state immediately
            _state.value = GenerationState.Generating(
                prompt = prompt,
                startTime = System.currentTimeMillis(),
                tokensGenerated = 0
            )
            _generatedText.value = ""

            llamaHelper.predict(formattedPrompt, imagePath)

            llmFlow.collect { event ->
                when (event) {
                    is LlamaHelper.LLMEvent.Started -> {
                        // Убираем очистку текста отсюда, чтобы не ломать поток букв
                        Log.i("MainViewModel", "Generation started")
                    }
                    is LlamaHelper.LLMEvent.Ongoing -> {
                        // Проверяем, что пришедший кусочек текста не равен служебным тегам модели
                        val word = event.word
                        if (!word.contains("<|") && !word.contains("|>")) {
                            _generatedText.value += word
                        }
                        
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
                        // В версии 0.4.0 метод stopPrediction вызывать не нужно, библиотека делает это сама
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
