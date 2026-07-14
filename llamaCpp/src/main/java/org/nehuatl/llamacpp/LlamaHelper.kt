package org.nehuatl.llamacpp

import android.content.ContentResolver
import android.net.Uri
import android.util.Log
import io.github.ljcamargo.kotlinllamacpp.LlamaAndroid
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

class LlamaHelper(
    val contentResolver: ContentResolver,
    val scope: CoroutineScope = CoroutineScope(Dispatchers.IO),
    val sharedFlow: MutableSharedFlow<LLMEvent>
) {
    private val llama by lazy { LlamaAndroid(contentResolver) }
    private var currentContext: Int? = null

    fun load(path: String, contextLength: Int, mmprojPath: String? = null, loaded: (Long) -> Unit) {
        scope.launch(Dispatchers.IO) {
            _state.value = GenerationState.Loading
            try {
                val pfd = contentResolver.openFileDescriptor(Uri.parse(path), "r") ?: throw Exception("PFD null")
                val modelFd = pfd.detachFd()

                val config = mutableMapOf<String, Any>(
                    "model_fd" to modelFd,
                    "n_ctx" to contextLength,
                    "n_threads" to 4,
                    "use_mmap" to true,
                    "use_mlock" to false
                )

                if (!mmprojPath.isNullOrEmpty()) {
                    val mmPfd = contentResolver.openFileDescriptor(Uri.parse(mmprojPath), "r")
                    mmPfd?.let { config["mmproj_fd"] = it.detachFd() }
                }

                val result = llama.startEngine(config)
                val id = result?.get("context_id") ?: result?.get("contextId") ?: throw Exception("context_id not found")
                
                currentContext = (id as Number).toInt()
                _state.value = GenerationState.ModelLoaded(path)
                loaded(currentContext!!.toLong())
            } catch (e: Exception) {
                _state.value = GenerationState.Error(e.message ?: "Failed to start engine")
            }
        }
    }

    fun predict(prompt: String, imagePath: String? = null) {
        val id = currentContext ?: return
        scope.launch(Dispatchers.IO) {
            try {
                val params = mutableMapOf<String, Any>(
                    "prompt" to prompt,
                    "top_k" to 40,
                    "top_p" to 0.9f,
                    "repetition_penalty" to 1.15f
                )

                if (!imagePath.isNullOrEmpty()) {
                    val imgPfd = contentResolver.openFileDescriptor(Uri.parse(imagePath), "r")
                    imgPfd?.let { params["image_fd"] = it.detachFd() }
                }

                // ПРАВИЛЬНЫЙ ВЫЗОВ: передаем лямбду tokenCallback третьим параметром
                llama.launchCompletion(id, params) { token ->
                    scope.launch {
                        sharedFlow.emit(LLMEvent.Ongoing(token))
                    }
                }
            } catch (e: Exception) {
                Log.e("LlamaHelper", "Prediction failed", e)
            }
        }
    }

    fun reset() {
        try {
            currentContext?.let { id ->
                llama.releaseContext(id)
                currentContext = null
            }
        } catch (e: Exception) {
            Log.e("LlamaHelper", "Reset failed", e)
        }
    }

    fun stopPrediction() {
        try {
            currentContext?.let { id ->
                llama.stopCompletion(id)
            }
        } catch (e: Exception) {
            Log.e("LlamaHelper", "Stop failed", e)
        }
    }

    private val _state = kotlinx.coroutines.flow.MutableStateFlow<GenerationState>(GenerationState.Idle)
    val state: kotlinx.coroutines.flow.StateFlow<GenerationState> = _state
}

sealed interface GenerationState {
    object Idle : GenerationState
    object Loading : GenerationState
    data class ModelLoaded(val path: String) : GenerationState
    data class Error(val message: String) : GenerationState
}

sealed interface LLMEvent {
    data class Ongoing(val token: String) : LLMEvent
}
