package org.nehuatl.llamacpp

import android.content.ContentResolver
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

class LlamaHelper(
    val contentResolver: ContentResolver,
    val scope: CoroutineScope = CoroutineScope(Dispatchers.IO),
    val sharedFlow: MutableSharedFlow<LLMEvent>
) {

    private val llama by lazy { LlamaAndroid(contentResolver) }
    private var loadJob: Job? = null
    private var completionJob: Job? = null
    private var currentContext: Int? = null
    private var tokenCount = 0
    private var allText = ""

    /**
     * Загрузка модели с поддержкой мультимодальности (mmproj)
     * @param path Путь к модели GGUF
     * @param contextLength Размер контекста (KV-кэш)
     * @param mmprojPath Путь к проектору для зрения (опционально)
     * @param loaded Callback с ID контекста
     */
    fun load(
        path: String,
        contextLength: Int,
        mmprojPath: String? = null,
        loaded: (Long) -> Unit
    ) {
        currentContext?.let { id -> llama.releaseContext(id) }
        
        loadJob = scope.launch {
            try {
                Log.d("LlamaHelper", ">>> Loading model with URI: $path")
                
                // ЗАВОДСКОЙ ВЫЗОВ ИНИЦИАЛИЗАЦИИ v0.4.0:
                // Просто передаем строковые URI-пути к модели и проектору напрямую в метод движка
                llama.load(
                    path = path,
                    contextLength = contextLength,
                    mmprojPath = mmprojPath
                ) { contextId ->
                    currentContext = contextId
                    Log.d("LlamaHelper", ">>> Context loaded successfully with ID: $currentContext")
                    sharedFlow.tryEmit(LLMEvent.Loaded(path))
                    loaded(contextId.toLong())
                }
            } catch (e: Exception) {
                Log.e("LlamaHelper", "Failed to load model", e)
                sharedFlow.tryEmit(LLMEvent.Error("Failed to load model: ${e.message}"))
            }
        }
    }

    /**
     * Основной метод генерации с поддержкой мультимодальности
     * @param prompt Текстовый запрос пользователя
     * @param imagePath URI изображения для анализа (опционально)
     * @param partialCompletion Включить стриминг токенов
     */
    fun predict(prompt: String, imagePath: String? = null, partialCompletion: Boolean = true) {
        val context = currentContext ?: throw Exception("Model was not loaded yet")
        val startTime = System.currentTimeMillis()
        tokenCount = 0
        allText = ""
        
        val params = mutableMapOf<String, Any>(
            "prompt" to prompt,
            "emit_partial_completion" to partialCompletion,
            // Настраиваем сэмплинг против заиканий (PocketPal style)
            "repetition_penalty" to 1.15f,   // Штраф за повторы букв и слогов
            "top_k" to 40,                   // Ограничиваем выбор самыми логичными токенами
            "top_p" to 0.9f                  // Отсекаем случайный мусор
        )
        
        // Передаем изображение через правильный параметр image_fd
        imagePath?.let {
            try {
                Log.d("LlamaHelper", ">>> Opening image for URI: $imagePath")
                params["image_path"] = imagePath
            } catch (e: Exception) {
                Log.e("LlamaHelper", "Failed to open image", e)
            }
        }

        completionJob = scope.launch {
            sharedFlow.tryEmit(LLMEvent.Started(prompt))
            llama.launchCompletion(
                id = context,
                params = params
            ) { word ->
                allText += word
                tokenCount++
                sharedFlow.tryEmit(LLMEvent.Ongoing(word, tokenCount))
            }
            val duration = System.currentTimeMillis() - startTime
            sharedFlow.tryEmit(LLMEvent.Done(allText, tokenCount, duration))
        }
    }

    /**
     * Официальный метод очистки KV-кэша (освобождение ОЗУ)
     * Сбрасывает текущий контекст через releaseContext
     */
    fun reset() {
        try {
            Log.d("LlamaHelper", ">>> Resetting KV cache via official library method")
            currentContext?.let { id -> 
                // Сбрасываем и очищаем кэш токенов для текущего контекста
                llama.releaseContext(id) 
                currentContext = null
            }
            Log.d("LlamaHelper", ">>> KV cache cleared successfully")
        } catch (e: Exception) {
            Log.e("LlamaHelper", "Failed to reset session", e)
        }
    }

    fun stopPrediction() {
        val id = currentContext ?: return
        scope.launch {
            llama.stopCompletion(id)
        }
        completionJob?.cancel()
    }

    fun release() {
        currentContext?.let { id ->
            llama.releaseContext(id)
        }
        currentContext = null
    }

    fun abort() {
        loadJob?.cancel()
        stopPrediction()
    }

    sealed class LLMEvent {
        data class Loaded(val path: String) : LLMEvent()
        data class Started(val prompt: String) : LLMEvent()
        data class Ongoing(val word: String, val tokenCount: Int) : LLMEvent()
        data class Done(val fullText: String, val tokenCount: Int, val duration: Long) : LLMEvent()
        data class Error(val message: String) : LLMEvent()
    }
}
