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
        LLAMA2,      // Llama 2 Chat
        LLAMA3,      // Llama 3 Instruct
        MISTRAL,     // Mistral Instruct
        ZEPHYR,      // Zephyr (Mistral-based)
        GEMMA,       // Gemma / Gemma 2 / Gemma 3
        PHI,         // Phi-2 / Phi-3
        QWEN,        // Qwen / Qwen2
        DEEPSEEK,    // DeepSeek / DeepSeek Coder
        YI,          // Yi Chat
        COMMAND_R,   // Cohere Command-R
        CHATML,      // ChatML (Orca, OpenChat, Nous Hermes, Dolphin)
        VICUNA,      // Vicuna
        ALPACA,      // Alpaca
        FALCON,      // Falcon Instruct
        MP,          // MPT Chat
        NEUTRAL      // Универсальный формат
    }

    private fun detectModelFormat(modelPath: String): ModelFormat {
        val lowerPath = modelPath.lowercase()
        return when {
            // Llama 3 / Llama 3.1 / Llama 3.2
            lowerPath.contains("llama-3") || lowerPath.contains("llama3") -> ModelFormat.LLAMA3
            // Llama 2
            lowerPath.contains("llama-2") || lowerPath.contains("llama2") -> ModelFormat.LLAMA2
            // Mistral / Mixtral
            lowerPath.contains("mistral") || lowerPath.contains("mixtral") -> ModelFormat.MISTRAL
            // Zephyr (основан на Mistral)
            lowerPath.contains("zephyr") -> ModelFormat.ZEPHYR
            // Gemma (все версии)
            lowerPath.contains("gemma") -> ModelFormat.GEMMA
            // Phi-2 / Phi-3
            lowerPath.contains("phi-2") || lowerPath.contains("phi-3") || lowerPath.contains("phi2") || lowerPath.contains("phi3") -> ModelFormat.PHI
            // Qwen / Qwen2
            lowerPath.contains("qwen") -> ModelFormat.QWEN
            // DeepSeek
            lowerPath.contains("deepseek") -> ModelFormat.DEEPSEEK
            // Yi
            lowerPath.contains("yi-") || lowerPath.contains("yi ") -> ModelFormat.YI
            // Command-R / Command-R+
            lowerPath.contains("command-r") || lowerPath.contains("c4ai") -> ModelFormat.COMMAND_R
            // ChatML (Orca, OpenChat, Nous Hermes, Dolphin)
            lowerPath.contains("orca") || lowerPath.contains("openchat") || lowerPath.contains("nous") || lowerPath.contains("hermes") || lowerPath.contains("dolphin") -> ModelFormat.CHATML
            // Vicuna
            lowerPath.contains("vicuna") -> ModelFormat.VICUNA
            // Alpaca
            lowerPath.contains("alpaca") -> ModelFormat.ALPACA
            // Falcon
            lowerPath.contains("falcon") -> ModelFormat.FALCON
            // MPT
            lowerPath.contains("mpt") -> ModelFormat.MP
            // Универсальный формат
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
            // Автоопределение формата модели
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

            mmprojPath?.let {
                val mmUri = Uri.parse(it)
                Log.d("LlamaHelper", ">>> Opening mmproj FD for URI: $mmUri")
                val mmPfd = contentResolver.openFileDescriptor(mmUri, "r")
                if (mmPfd != null) {
                    val mmFd = mmPfd.detachFd()
                    config["mmproj"] = it
                    config["mmproj_fd"] = mmFd
                    Log.d("LlamaHelper", ">>> Mmproj FD: $mmFd")
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

        // Формируем полный промпт в зависимости от формата модели
        val fullPrompt = if (!systemPrompt.isNullOrEmpty()) {
            when (currentModelFormat) {
                ModelFormat.LLAMA2 -> {
                    "[INST] <<SYS>>\n$systemPrompt\n<</SYS>>\n\n$prompt [/INST]"
                }
                ModelFormat.LLAMA3 -> {
                    "<|begin_of_text|><|start_header_id|>system<|end_header_id|>\n\n$systemPrompt<|eot_id|><|start_header_id|>user<|end_header_id|>\n\n$prompt<|eot_id|><|start_header_id|>assistant<|end_header_id|>\n\n"
                }
                ModelFormat.MISTRAL -> {
                    "<s>[INST] $systemPrompt [/INST]</s>\n[INST] $prompt [/INST]"
                }
                ModelFormat.ZEPHYR -> {
                    "<|system|>\n$systemPrompt</s>\n<|user|>\n$prompt</s>\n<|assistant|>\n"
                }
                ModelFormat.GEMMA -> {
                    "<bos><start_of_turn>user\n$systemPrompt\n\n$prompt<end_of_turn>\n<start_of_turn>model\n"
                }
                ModelFormat.PHI -> {
                    "<|system|>\n$systemPrompt<|end|>\n<|user|>\n$prompt<|end|>\n<|assistant|>\n"
                }
                ModelFormat.QWEN -> {
                    "<|im_start|>system\n$systemPrompt<|im_end|>\n<|im_start|>user\n$prompt<|im_end|>\n<|im_start|>assistant\n"
                }
                ModelFormat.DEEPSEEK -> {
                    "<|begin_of_sentence|>System: $systemPrompt\n\nUser: $prompt\n\nAssistant:"
                }
                ModelFormat.YI -> {
                    "<|im_start|>system\n$systemPrompt<|im_end|>\n<|im_start|>user\n$prompt<|im_end|>\n<|im_start|>assistant\n"
                }
                ModelFormat.COMMAND_R -> {
                    "<BOS_TOKEN><|START_OF_TURN_TOKEN|><|SYSTEM_TOKEN|>$systemPrompt<|END_OF_TURN_TOKEN|><|START_OF_TURN_TOKEN|><|USER_TOKEN|>$prompt<|END_OF_TURN_TOKEN|><|START_OF_TURN_TOKEN|><|CHATBOT_TOKEN|>"
                }
                ModelFormat.CHATML -> {
                    "<|im_start|>system\n$systemPrompt<|im_end|>\n<|im_start|>user\n$prompt<|im_end|>\n<|im_start|>assistant\n"
                }
                ModelFormat.VICUNA -> {
                    "SYSTEM: $systemPrompt\nUSER: $prompt\nASSISTANT:"
                }
                ModelFormat.ALPACA -> {
                    "### System:\n$systemPrompt\n\n### User:\n$prompt\n\n### Assistant:\n"
                }
                ModelFormat.FALCON -> {
                    "System: $systemPrompt\nUser: $prompt\nFalcon:"
                }
                ModelFormat.MP -> {
                    "<|im_start|>system\n$systemPrompt<|im_end|>\n<|im_start|>user\n$prompt<|im_end|>\n<|im_start|>assistant\n"
                }
                ModelFormat.NEUTRAL -> {
                    "System: $systemPrompt\n\nUser: $prompt\n\nAssistant:"
                }
            }
        } else {
            when (currentModelFormat) {
                ModelFormat.LLAMA2 -> {
                    "[INST] $prompt [/INST]"
                }
                ModelFormat.LLAMA3 -> {
                    "<|begin_of_text|><|start_header_id|>user<|end_header_id|>\n\n$prompt<|eot_id|><|start_header_id|>assistant<|end_header_id|>\n\n"
                }
                ModelFormat.MISTRAL -> {
                    "<s>[INST] $prompt [/INST]"
                }
                ModelFormat.ZEPHYR -> {
                    "<|user|>\n$prompt</s>\n<|assistant|>\n"
                }
                ModelFormat.GEMMA -> {
                    "<bos><start_of_turn>user\n$prompt<end_of_turn>\n<start_of_turn>model\n"
                }
                ModelFormat.PHI -> {
                    "<|user|>\n$prompt<|end|>\n<|assistant|>\n"
                }
                ModelFormat.QWEN -> {
                    "<|im_start|>user\n$prompt<|im_end|>\n<|im_start|>assistant\n"
                }
                ModelFormat.DEEPSEEK -> {
                    "<|begin_of_sentence|>User: $prompt\n\nAssistant:"
                }
                ModelFormat.YI -> {
                    "<|im_start|>user\n$prompt<|im_end|>\n<|im_start|>assistant\n"
                }
                ModelFormat.COMMAND_R -> {
                    "<BOS_TOKEN><|START_OF_TURN_TOKEN|><|USER_TOKEN|>$prompt<|END_OF_TURN_TOKEN|><|START_OF_TURN_TOKEN|><|CHATBOT_TOKEN|>"
                }
                ModelFormat.CHATML -> {
                    "<|im_start|>user\n$prompt<|im_end|>\n<|im_start|>assistant\n"
                }
                ModelFormat.VICUNA -> {
                    "USER: $prompt\nASSISTANT:"
                }
                ModelFormat.ALPACA -> {
                    "### User:\n$prompt\n\n### Assistant:\n"
                }
                ModelFormat.FALCON -> {
                    "User: $prompt\nFalcon:"
                }
                ModelFormat.MP -> {
                    "<|im_start|>user\n$prompt<|im_end|>\n<|im_start|>assistant\n"
                }
                ModelFormat.NEUTRAL -> {
                    "User: $prompt\n\nAssistant:"
                }
            }
        }
        
        Log.d("LlamaHelper", "=== predict: modelFormat = $currentModelFormat")
        Log.d("LlamaHelper", "=== predict: fullPrompt length = ${fullPrompt.length}")
        Log.d("LlamaHelper", "=== predict: fullPrompt первые 300 символов = ${fullPrompt.take(300)}")

        // Стоп-слова в зависимости от формата модели
        val stopWords = when (currentModelFormat) {
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
        
        val params = mutableMapOf<String, Any>(
            "prompt" to fullPrompt,
            "emit_partial_completion" to true,
            "temperature" to 0.7,
            "n_predict" to maxTokens,
            "top_k" to 40,
            "top_p" to 0.95,
            "stop" to stopWords
        )
        
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
            
            // Очищаем ответ от маркеров форматирования
            val cleanedText = allText
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
            
            sharedFlow.tryEmit(LLMEvent.Done(cleanedText, tokenCount, duration))
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
