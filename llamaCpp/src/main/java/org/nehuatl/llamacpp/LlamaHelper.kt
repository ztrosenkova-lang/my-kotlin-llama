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
    private var currentModelFormat: ModelFormat = ModelFormat.LLAMA2

    fun getContextId(): Int? = currentContext

    // Определение формата модели по имени файла
    private enum class ModelFormat {
        LLAMA2, LLAMA3, MISTRAL, ZEPHYR, GEMMA, PHI, QWEN,
        DEEPSEEK, YI, COMMAND_R, CHATML, VICUNA, ALPACA, FALCON, MP, NEUTRAL
    }

    private fun detectModelFormat(modelPath: String): ModelFormat {
        val lowerPath = modelPath.lowercase()
        return when {
            lowerPath.contains("llama-3") || lowerPath.contains("llama3") -> ModelFormat.LLAMA3
            lowerPath.contains("llama-2") || lowerPath.contains("llama2") -> ModelFormat.LLAMA2
            lowerPath.contains("mistral") || lowerPath.contains("mixtral") -> ModelFormat.MISTRAL
            lowerPath.contains("zephyr") -> ModelFormat.ZEPHYR
            lowerPath.contains("gemma") -> ModelFormat.GEMMA
            lowerPath.contains("phi-2") || lowerPath.contains("phi-3") || lowerPath.contains("phi2") || lowerPath.contains("phi3") -> ModelFormat.PHI
            lowerPath.contains("qwen") -> ModelFormat.QWEN
            lowerPath.contains("deepseek") -> ModelFormat.DEEPSEEK
            lowerPath.contains("yi-") || lowerPath.contains("yi ") -> ModelFormat.YI
            lowerPath.contains("command-r") || lowerPath.contains("c4ai") -> ModelFormat.COMMAND_R
            lowerPath.contains("orca") || lowerPath.contains("openchat") || lowerPath.contains("nous") || lowerPath.contains("hermes") || lowerPath.contains("dolphin") -> ModelFormat.CHATML
            lowerPath.contains("vicuna") -> ModelFormat.VICUNA
            lowerPath.contains("alpaca") -> ModelFormat.ALPACA
            lowerPath.contains("falcon") -> ModelFormat.FALCON
            lowerPath.contains("mpt") -> ModelFormat.MP
            else -> ModelFormat.NEUTRAL
        }
    }

    fun load(
        path: String,
        contextLength: Int,
        mmprojPath: String? = null,
        loaded: (Long) -> Unit
    ) {
        currentContext?.let { id -> llama.releaseContext(id) }
        
        try {
            currentModelFormat = detectModelFormat(path)
            Log.d("LlamaHelper", ">>> Detected model format: $currentModelFormat for path: $path")

            val modelUri = Uri.parse(path)
            Log.d("LlamaHelper", ">>> Opening model FD for URI: $modelUri")
            
            contentResolver.openInputStream(modelUri)?.use { input ->
                val firstByte = input.read()
                val size = contentResolver.openFileDescriptor(modelUri, "r")?.use { it.statSize } ?: -1
                Log.d("LlamaHelper", ">>> Model is readable, first byte: $firstByte, size: $size")
            } ?: Log.e("LlamaHelper", ">>> Model is NOT readable via openInputStream")

            val modelPfd = contentResolver.openFileDescriptor(modelUri, "r")
                ?: throw IllegalArgumentException("Cannot open model URI: $modelUri")
            val modelFd = modelPfd.detachFd()
            Log.d("LlamaHelper", ">>> Model FD: $modelFd")

            val config = mutableMapOf<String, Any>(
                "model" to path,
                "model_fd" to modelFd,
                "use_mmap" to false,
                "use_mlock" to false,
                "n_ctx" to contextLength,
                "embedding" to false,
                "n_batch" to 512,
                "n_threads" to 0,
                "n_gpu_layers" to 0,
                "vocab_only" to false,
                "lora" to "",
                "lora_scaled" to 1.0,
                "rope_freq_base" to 0.0,
                "rope_freq_scale" to 0.0
            )

            // Загрузка проектора — передаём URI строкой, а не файловый дескриптор
            mmprojPath?.let {
                val mmUri = Uri.parse(it)
                Log.d("LlamaHelper", ">>> Opening mmproj FD for URI: $mmUri")
                val mmPfd = contentResolver.openFileDescriptor(mmUri, "r")
                if (mmPfd != null) {
                    val mmFd = mmPfd.detachFd()
                    config["mmproj"] = it  // URI проектора строкой
                    config["mmproj_fd"] = mmFd
                    Log.d("LlamaHelper", ">>> Mmproj URI: $it, FD: $mmFd")
                }
            }

            loadJob = scope.launch {
                Log.d("LlamaHelper", ">>> will start llama context with config: $config")
                val result = try {
                    llama.startEngine(config) {
                        allText += it
                        tokenCount++
                        sharedFlow.tryEmit(LLMEvent.Ongoing(it, tokenCount))
                    }
                } catch (e: Exception) {
                    Log.e("LlamaHelper", "Engine start failed", e)
                    null
                }

                if (result == null) {
                    sharedFlow.tryEmit(LLMEvent.Error("Model initialization failed"))
                    return@launch
                }

                val id = result["contextId"] ?: throw Exception("contextId not found in result map")
                currentContext = (id as Number).toInt()

                Log.d("LlamaHelper", ">>> Context loaded successfully with ID: $currentContext")
                sharedFlow.tryEmit(LLMEvent.Loaded(path))
                loaded(currentContext!!.toLong())
            }
        } catch (e: Exception) {
            Log.e("LlamaHelper", "Failed to prepare model loading", e)
            sharedFlow.tryEmit(LLMEvent.Error("Failed to open files: ${e.message}"))
        }
    }

    fun predict(prompt: String, imagePath: String? = null, systemPrompt: String? = null, maxTokens: Int = 512) {
        val context = currentContext ?: throw Exception("Model was not loaded yet")
        val startTime = System.currentTimeMillis()
        tokenCount = 0
        allText = ""

        val fullPrompt = buildPrompt(prompt, systemPrompt)
        
        Log.d("LlamaHelper", "=== predict: modelFormat = $currentModelFormat")
        Log.d("LlamaHelper", "=== predict: fullPrompt length = ${fullPrompt.length}")

        val stopWords = getStopWords()
        
        val params = mutableMapOf<String, Any>(
            "prompt" to fullPrompt,
            "emit_partial_completion" to true,
            "temperature" to 0.7,
            "n_predict" to maxTokens,
            "top_k" to 40,
            "top_p" to 0.95,
            "stop" to stopWords
        )
        
        // Передаём изображение как путь к файлу, а не как FD
        imagePath?.let {
            params["image"] = it
            Log.d("LlamaHelper", ">>> Image path added to params: $it")
        }

        completionJob = scope.launch {
            sharedFlow.tryEmit(LLMEvent.Started(prompt))
            llama.launchCompletion(
                id = context,
                params = params
            )
            val duration = System.currentTimeMillis() - startTime
            
            val cleanedText = cleanResponse(allText)
            
            sharedFlow.tryEmit(LLMEvent.Done(cleanedText, tokenCount, duration))
        }
    }

    private fun buildPrompt(prompt: String, systemPrompt: String?): String {
        return when (currentModelFormat) {
            ModelFormat.LLAMA2 -> {
                if (!systemPrompt.isNullOrEmpty()) {
                    "[INST] <<SYS>>\n$systemPrompt\n<</SYS>>\n\n$prompt [/INST]"
                } else {
                    "[INST] $prompt [/INST]"
                }
            }
            ModelFormat.LLAMA3 -> {
                if (!systemPrompt.isNullOrEmpty()) {
                    "<|begin_of_text|><|start_header_id|>system<|end_header_id|>\n\n$systemPrompt<|eot_id|><|start_header_id|>user<|end_header_id|>\n\n$prompt<|eot_id|><|start_header_id|>assistant<|end_header_id|>\n\n"
                } else {
                    "<|begin_of_text|><|start_header_id|>user<|end_header_id|>\n\n$prompt<|eot_id|><|start_header_id|>assistant<|end_header_id|>\n\n"
                }
            }
            ModelFormat.MISTRAL -> {
                if (!systemPrompt.isNullOrEmpty()) {
                    "<s>[INST] $systemPrompt [/INST]</s>\n[INST] $prompt [/INST]"
                } else {
                    "<s>[INST] $prompt [/INST]"
                }
            }
            ModelFormat.ZEPHYR -> {
                if (!systemPrompt.isNullOrEmpty()) {
                    "<|system|>\n$systemPrompt</s>\n<|user|>\n$prompt</s>\n<|assistant|>\n"
                } else {
                    "<|user|>\n$prompt</s>\n<|assistant|>\n"
                }
            }
            ModelFormat.GEMMA -> {
                if (!systemPrompt.isNullOrEmpty()) {
                    "<bos><start_of_turn>user\n$systemPrompt\n\n$prompt<end_of_turn>\n<start_of_turn>model\n"
                } else {
                    "<bos><start_of_turn>user\n$prompt<end_of_turn>\n<start_of_turn>model\n"
                }
            }
            ModelFormat.PHI -> {
                if (!systemPrompt.isNullOrEmpty()) {
                    "<|system|>\n$systemPrompt<|end|>\n<|user|>\n$prompt<|end|>\n<|assistant|>\n"
                } else {
                    "<|user|>\n$prompt<|end|>\n<|assistant|>\n"
                }
            }
            ModelFormat.QWEN -> {
                if (!systemPrompt.isNullOrEmpty()) {
                    "<|im_start|>system\n$systemPrompt<|im_end|>\n<|im_start|>user\n$prompt<|im_end|>\n<|im_start|>assistant\n"
                } else {
                    "<|im_start|>user\n$prompt<|im_end|>\n<|im_start|>assistant\n"
                }
            }
            ModelFormat.DEEPSEEK -> {
                if (!systemPrompt.isNullOrEmpty()) {
                    "<|begin_of_sentence|>System: $systemPrompt\n\nUser: $prompt\n\nAssistant:"
                } else {
                    "<|begin_of_sentence|>User: $prompt\n\nAssistant:"
                }
            }
            ModelFormat.YI -> {
                if (!systemPrompt.isNullOrEmpty()) {
                    "<|im_start|>system\n$systemPrompt<|im_end|>\n<|im_start|>user\n$prompt<|im_end|>\n<|im_start|>assistant\n"
                } else {
                    "<|im_start|>user\n$prompt<|im_end|>\n<|im_start|>assistant\n"
                }
            }
            ModelFormat.COMMAND_R -> {
                if (!systemPrompt.isNullOrEmpty()) {
                    "<BOS_TOKEN><|START_OF_TURN_TOKEN|><|SYSTEM_TOKEN|>$systemPrompt<|END_OF_TURN_TOKEN|><|START_OF_TURN_TOKEN|><|USER_TOKEN|>$prompt<|END_OF_TURN_TOKEN|><|START_OF_TURN_TOKEN|><|CHATBOT_TOKEN|>"
                } else {
                    "<BOS_TOKEN><|START_OF_TURN_TOKEN|><|USER_TOKEN|>$prompt<|END_OF_TURN_TOKEN|><|START_OF_TURN_TOKEN|><|CHATBOT_TOKEN|>"
                }
            }
            ModelFormat.CHATML -> {
                if (!systemPrompt.isNullOrEmpty()) {
                    "<|im_start|>system\n$systemPrompt<|im_end|>\n<|im_start|>user\n$prompt<|im_end|>\n<|im_start|>assistant\n"
                } else {
                    "<|im_start|>user\n$prompt<|im_end|>\n<|im_start|>assistant\n"
                }
            }
            ModelFormat.VICUNA -> {
                if (!systemPrompt.isNullOrEmpty()) {
                    "SYSTEM: $systemPrompt\nUSER: $prompt\nASSISTANT:"
                } else {
                    "USER: $prompt\nASSISTANT:"
                }
            }
            ModelFormat.ALPACA -> {
                if (!systemPrompt.isNullOrEmpty()) {
                    "### System:\n$systemPrompt\n\n### User:\n$prompt\n\n### Assistant:\n"
                } else {
                    "### User:\n$prompt\n\n### Assistant:\n"
                }
            }
            ModelFormat.FALCON -> {
                if (!systemPrompt.isNullOrEmpty()) {
                    "System: $systemPrompt\nUser: $prompt\nFalcon:"
                } else {
                    "User: $prompt\nFalcon:"
                }
            }
            ModelFormat.MP -> {
                if (!systemPrompt.isNullOrEmpty()) {
                    "<|im_start|>system\n$systemPrompt<|im_end|>\n<|im_start|>user\n$prompt<|im_end|>\n<|im_start|>assistant\n"
                } else {
                    "<|im_start|>user\n$prompt<|im_end|>\n<|im_start|>assistant\n"
                }
            }
            ModelFormat.NEUTRAL -> {
                if (!systemPrompt.isNullOrEmpty()) {
                    "System: $systemPrompt\n\nUser: $prompt\n\nAssistant:"
                } else {
                    "User: $prompt\n\nAssistant:"
                }
            }
        }
    }

    private fun getStopWords(): List<String> {
        return when (currentModelFormat) {
            ModelFormat.LLAMA2 -> listOf("</s>", "<|endoftext|>", "<|eot_id|>", "<|im_end|>")
            ModelFormat.LLAMA3 -> listOf("<|eot_id|>", "<|end_of_text|>", "<|endoftext|>")
            ModelFormat.MISTRAL -> listOf("</s>", "[INST]", "<|endoftext|>", "<|im_end|>")
            ModelFormat.ZEPHYR -> listOf("</s>", "<|endoftext|>", "<|user|>")
            ModelFormat.GEMMA -> listOf("<end_of_turn>", "<eos>", "<|endoftext|>", "<|im_end|>")
            ModelFormat.PHI -> listOf("<|end|>", "<|endoftext|>", "<|im_end|>")
            ModelFormat.QWEN -> listOf("<|im_end|>", "<|endoftext|>", "<|end|>")
            ModelFormat.DEEPSEEK -> listOf("<|end_of_sentence|>", "<|endoftext|>", "User:")
            ModelFormat.YI -> listOf("<|im_end|>", "<|endoftext|>")
            ModelFormat.COMMAND_R -> listOf("<|END_OF_TURN_TOKEN|>", "<|endoftext|>")
            ModelFormat.CHATML -> listOf("<|im_end|>", "<|endoftext|>")
            ModelFormat.VICUNA -> listOf("USER:", "ASSISTANT:", "</s>", "<|endoftext|>")
            ModelFormat.ALPACA -> listOf("### User:", "### Assistant:", "<|endoftext|>")
            ModelFormat.FALCON -> listOf("User:", "Falcon:", "<|endoftext|>")
            ModelFormat.MP -> listOf("<|im_end|>", "<|endoftext|>")
            ModelFormat.NEUTRAL -> listOf("</s>", "<|endoftext|>", "<|im_end|>", "<|eot_id|>")
        }
    }

    private fun cleanResponse(text: String): String {
        return text
            .replace(Regex("\\[/?INST\\]"), "")
            .replace(Regex("</?s>"), "")
            .replace(Regex("<<SYS>>"), "")
            .replace(Regex("<</SYS>>"), "")
            .replace(Regex("<\\|im_start\\|>.*?(?=\\n|$)"), "")
            .replace(Regex("<\\|im_end\\|>"), "")
            .replace(Regex("<\\|start_header_id\\|>.*?<\\|end_header_id\\|>"), "")
            .replace(Regex("<\\|eot_id\\|>"), "")
            .replace(Regex("<\\|begin_of_text\\|>"), "")
            .replace(Regex("<\\|end_of_text\\|>"), "")
            .replace(Regex("<start_of_turn>.*?<end_of_turn>"), "")
            .replace(Regex("<bos>"), "")
            .replace(Regex("<eos>"), "")
            .replace(Regex("<\\|system\\|>"), "")
            .replace(Regex("<\\|user\\|>"), "")
            .replace(Regex("<\\|assistant\\|>"), "")
            .replace(Regex("<\\|end\\|>"), "")
            .replace(Regex("<BOS_TOKEN>"), "")
            .replace(Regex("<\\|START_OF_TURN_TOKEN\\|>"), "")
            .replace(Regex("<\\|SYSTEM_TOKEN\\|>"), "")
            .replace(Regex("<\\|USER_TOKEN\\|>"), "")
            .replace(Regex("<\\|CHATBOT_TOKEN\\|>"), "")
            .replace(Regex("<\\|END_OF_TURN_TOKEN\\|>"), "")
            .replace(Regex("<\\|begin_of_sentence\\|>"), "")
            .replace(Regex("<\\|end_of_sentence\\|>"), "")
            .replace(Regex("### (System|User|Assistant):\\s*"), "")
            .replace(Regex("(SYSTEM|USER|ASSISTANT|System|User|Falcon):\\s*"), "")
            .trim()
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
