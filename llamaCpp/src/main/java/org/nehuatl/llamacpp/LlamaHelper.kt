package org.nehuatl.llamacpp

import android.content.ContentResolver
import android.net.Uri
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
                
                // Открываем файловый дескриптор модели
                val pfd = contentResolver.openFileDescriptor(Uri.parse(path), "r")
                    ?: throw Exception("Failed to open model PFD")
                val modelFd = pfd.detachFd()
                Log.d("LlamaHelper", ">>> Model FD: $modelFd")

                // Собираем конфигурацию со строгим приведением типов
                val config = mutableMapOf<String, Any>(
                    "model_fd" to modelFd,
                    "n_ctx" to contextLength,
                    "n_threads" to 4,
                    "use_mmap" to true,
                    "use_mlock" to false
                )

                // Если выбран mmproj для зрения
                if (!mmprojPath.isNullOrEmpty()) {
                    val mmPfd = contentResolver.openFileDescriptor(Uri.parse(mmprojPath), "r")
                    mmPfd?.let { 
                        val mmFd = it.detachFd()
                        config["mmproj_fd"] = mmFd
                        Log.d("LlamaHelper", ">>> Mmproj FD: $mmFd")
                    }
                }

                // Запускаем движок через официальный метод startEngine
                val result = llama.startEngine(config)
                
                // Извлекаем ID контекста из результата с безопасной проверкой
                val id = result?.get("context_id") ?: result?.get("contextId") 
                    ?: throw Exception("context_id not found in result")
                
                currentContext = (id as Number).toInt()
                Log.d("LlamaHelper", ">>> Context loaded successfully with ID: $currentContext")
                
                sharedFlow.tryEmit(LLMEvent.Loaded(path))
                loaded(currentContext!!.toLong())
                
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
                val imgPfd = contentResolver.openFileDescriptor(Uri.parse(it), "r")
                imgPfd?.let { pfd ->
                    val imgFd = pfd.detachFd()
                    params["image_fd"] = imgFd
                    Log.d("LlamaHelper", ">>> Image FD added to params: $imgFd")
                }
            } catch (e: Exception) {
                Log.e("LlamaHelper", "Failed to open image FD", e)
            }
        }

        completionJob = scope.launch {
            sharedFlow.tryEmit(LLMEvent.Started(prompt))
            
            // Вызываем launchCompletion с тремя параметрами: id, params и tokenCallback
            val result = llama.launchCompletion(
                id = context,
                params = params
            ) { token ->
                // Обрабатываем каждый токен в реальном времени
                allText += token
                tokenCount++
                sharedFlow.tryEmit(LLMEvent.Ongoing(token, tokenCount))
            }
            
            // Безопасно обрабатываем финальные метрики, если они есть
            if (result != null) {
                // Если в результате есть дополнительные данные, можно их обработать
                Log.d("LlamaHelper", "Generation completed with result: $result")
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
