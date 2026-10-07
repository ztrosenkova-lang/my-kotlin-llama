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
        GEMMA,       // Gemma / Gemma 2 / Gemma 3
        PHI,         // Phi-2 / Phi-3
        QWEN,        // Qwen / Qwen2
        DEEPSEEK,    // DeepSeek / DeepSeek Coder
        YI,          // Yi Chat
        COMMAND_R,   // Cohere Command-R
        CHATML,      // ChatML формат (Orca, OpenChat, Nous Hermes)
        ZEPHYR,      // Zephyr (Mistral-based)
        VICUNA,      // Vicuna
        ALPACA,      // Alpaca
        FALCON,      // Falcon Instruct
        MP,         // MPT Chat
        NEUTRAL      // Универсальный формат без специальных токенов
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
            // Phi-2 / Phi-3 / Phi-4
            lowerPath.contains("phi-2") || lowerPath.contains("phi-3") ||
                    lowerPath.contains("phi-4") || lowerPath.contains("phi2") ||
                    lowerPath.contains("phi3") -> ModelFormat.PHI
            // Qwen / Qwen2 / Qwen2.5 / CodeQwen
            lowerPath.contains("qwen") -> ModelFormat.QWEN
            // DeepSeek
            lowerPath.contains("deepseek") -> ModelFormat.DEEPSEEK
            // Yi
            lowerPath.contains("yi-") || lowerPath.contains("yi ") -> ModelFormat.YI
            // Command-R / Command-R+ / Aya (Cohere)
            lowerPath.contains("command-r") || lowerPath.contains("c4ai") ||
                    lowerPath.contains("aya") -> ModelFormat.COMMAND_R
            // ChatML-семейство (Orca, OpenChat, Nous, Hermes, Dolphin,
            // Granite, SmolLM, StableLM, InternLM, Baichuan, TinyLlama-chat,
            // Vicuna-современные, OpenHermes и др.)
            lowerPath.contains("orca") || lowerPath.contains("openchat") ||
                    lowerPath.contains("nous") || lowerPath.contains("hermes") ||
                    lowerPath.contains("dolphin") || lowerPath.contains("granite") ||
                    lowerPath.contains("smollm") || lowerPath.contains("stablelm") ||
                    lowerPath.contains("internlm") || lowerPath.contains("baichuan") ||
                    lowerPath.contains("tinyllama") || lowerPath.contains("openhermes") ||
                    lowerPath.contains("chatml") -> ModelFormat.CHATML
            // Vicuna
            lowerPath.contains("vicuna") -> ModelFormat.VICUNA
            // Alpaca
            lowerPath.contains("alpaca") -> ModelFormat.ALPACA
            // Falcon
            lowerPath.contains("falcon") -> ModelFormat.FALCON
            // MPT
            lowerPath.contains("mpt") -> ModelFormat.MP
            // Универсальный fallback — ChatML (большинство современных моделей)
            else -> ModelFormat.CHATML
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

                    fun predict(
        prompt: String,
        imagePath: String? = null,
        systemPrompt: String? = null,
        maxTokens: Int = 512,
        chatHistory: List<Pair<String, String>> = emptyList()
    ) {
        val context = currentContext ?: throw Exception("Model was not loaded yet")
        val startTime = System.currentTimeMillis()
        tokenCount = 0
        allText = ""

        // Формируем промпт в зависимости от формата модели
        val fullPrompt = buildPrompt(prompt, systemPrompt, chatHistory)
        
        Log.d("LlamaHelper", "=== predict: modelFormat = $currentModelFormat")
        Log.d("LlamaHelper", "=== predict: fullPrompt length = ${fullPrompt.length}")
        Log.d("LlamaHelper", "=== predict: fullPrompt первые 300 символов = ${fullPrompt.take(300)}")

        // Стоп-слова в зависимости от формата модели
        val stopWords = getStopWords()
        
                val params = mutableMapOf<String, Any>(
            "prompt" to fullPrompt,
            "emit_partial_completion" to true,
            "temperature" to 0.7,
            "n_predict" to maxTokens,
            "top_k" to 40,
            "top_p" to 0.95,
            "stop" to stopWords,
            // DRY (Don't Repeat Yourself) — против зацикливания на фразах и абзацах.
            // multiplier > 0 включает DRY. 0.8 — рекомендуемое значение.
            "dry_multiplier" to 0.8,
            // base — база экспоненциального штрафа. 1.75 — стандарт llama.cpp.
            "dry_base" to 1.75,
            // allowed_length — минимальная длина повтора (в токенах), после которой DRY срабатывает.
            // 3 — значит, повтор из 3+ токенов будет штрафоваться.
            "dry_allowed_length" to 3,
            // penalty_last_n — сколько последних токенов проверять. 0 = весь контекст.
            "dry_penalty_last_n" to 0
        )
        
        imagePath?.let {
            try {
                val imgUri = Uri.parse(it)
                Log.d("LlamaHelper", ">>> Opening image FD for URI: $imgUri")
                contentResolver.openFileDescriptor(imgUri, "r")?.use { pfd ->
                    val imgFd = pfd.detachFd()
                    params["image_fds"] = listOf(imgFd)
                    Log.d("LlamaHelper", ">>> Image FD added to params: $imgFd")
                }
            } catch (e: Exception) {
                Log.e("LlamaHelper", "Failed to open image FD", e)
            }
        }

        completionJob = scope.launch {
            sharedFlow.tryEmit(LLMEvent.Started(prompt))
            llama.launchCompletion(
                id = context,
                params = params
            )
            val duration = System.currentTimeMillis() - startTime
            
            // Очищаем ответ от маркеров форматирования
            val cleanedText = cleanResponse(allText)
            
            sharedFlow.tryEmit(LLMEvent.Done(cleanedText, tokenCount, duration))
        }
    }

                         private fun buildPrompt(
        prompt: String,
        systemPrompt: String?,
        chatHistory: List<Pair<String, String>> = emptyList()
    ): String {
        // Если истории нет — одна реплика (режим калькулятора)
        if (chatHistory.isEmpty()) {
            return buildSingleTurnPrompt(prompt, systemPrompt)
        }

        // С историей — многосекционная сборка в формате конкретной модели
        return when (currentModelFormat) {
            ModelFormat.CHATML, ModelFormat.QWEN, ModelFormat.YI, ModelFormat.MP -> buildString {
                if (!systemPrompt.isNullOrEmpty()) {
                    append("<|im_start|>system\n")
                    append(systemPrompt)
                    append("<|im_end|>\n")
                }
                for ((role, text) in chatHistory) {
                    if (role == "system") continue
                    val tag = if (role == "user") "user" else "assistant"
                    append("<|im_start|>")
                    append(tag)
                    append("\n")
                    append(text)
                    append("<|im_end|>\n")
                }
                if (prompt.isNotBlank()) {
                    append("<|im_start|>user\n")
                    append(prompt)
                    append("<|im_end|>\n")
                }
                append("<|im_start|>assistant\n")
            }

            ModelFormat.LLAMA3 -> buildString {
                append("<|begin_of_text|>")
                if (!systemPrompt.isNullOrEmpty()) {
                    append("<|start_header_id|>system<|end_header_id|>\n\n")
                    append(systemPrompt)
                    append("<|eot_id|>")
                }
                for ((role, text) in chatHistory) {
                    if (role == "system") continue
                    val headerRole = if (role == "user") "user" else "assistant"
                    append("<|start_header_id|>")
                    append(headerRole)
                    append("<|end_header_id|>\n\n")
                    append(text)
                    append("<|eot_id|>")
                }
                if (prompt.isNotBlank()) {
                    append("<|start_header_id|>user<|end_header_id|>\n\n")
                    append(prompt)
                    append("<|eot_id|>")
                }
                append("<|start_header_id|>assistant<|end_header_id|>\n\n")
            }

            ModelFormat.GEMMA -> buildString {
                if (!systemPrompt.isNullOrEmpty()) {
                    append("<bos><start_of_turn>user\n")
                    append(systemPrompt)
                    append("\n\n")
                }
                var firstUser = true
                for ((role, text) in chatHistory) {
                    if (role == "system") continue
                    if (role == "user") {
                        if (firstUser && !systemPrompt.isNullOrEmpty()) {
                            append(text)
                            append("<end_of_turn>\n")
                            firstUser = false
                        } else {
                            append("<start_of_turn>user\n")
                            append(text)
                            append("<end_of_turn>\n")
                        }
                    } else {
                        append("<start_of_turn>model\n")
                        append(text)
                        append("<end_of_turn>\n")
                    }
                }
                if (prompt.isNotBlank()) {
                    append("<start_of_turn>user\n")
                    append(prompt)
                    append("<end_of_turn>\n")
                }
                append("<start_of_turn>model\n")
            }

            ModelFormat.MISTRAL -> buildString {
                append("<s>")
                if (!systemPrompt.isNullOrEmpty()) {
                    append("[INST] ")
                    append(systemPrompt)
                    append(" [/INST]</s>\n")
                }
                for ((role, text) in chatHistory) {
                    if (role == "system") continue
                    if (role == "user") {
                        append("[INST] ")
                        append(text)
                        append(" [/INST]")
                    } else {
                        append(text)
                        append("</s>\n")
                    }
                }
                if (prompt.isNotBlank()) {
                    append("[INST] ")
                    append(prompt)
                    append(" [/INST]")
                }
            }

            ModelFormat.DEEPSEEK -> buildString {
                if (!systemPrompt.isNullOrEmpty()) {
                    append(systemPrompt)
                    append("\n")
                }
                for ((role, text) in chatHistory) {
                    if (role == "system") continue
                    if (role == "user") {
                        append("<｜User｜>")
                        append(text)
                    } else {
                        append("<｜Assistant｜>")
                        append(text)
                        append("<｜end▁of▁sentence｜>")
                    }
                }
                if (prompt.isNotBlank()) {
                    append("<｜User｜>")
                    append(prompt)
                }
                append("<｜Assistant｜>")
            }

            ModelFormat.LLAMA2 -> buildString {
                append("<s>")
                if (!systemPrompt.isNullOrEmpty()) {
                    append("[INST] <<SYS>>\n")
                    append(systemPrompt)
                    append("\n<</SYS>>\n\n")
                }
                for ((role, text) in chatHistory) {
                    if (role == "system") continue
                    if (role == "user") {
                        append(text)
                        append(" [/INST] ")
                    } else {
                        append(text)
                        append(" </s><s>[INST] ")
                    }
                }
                if (prompt.isNotBlank()) {
                    append(prompt)
                    append(" [/INST]")
                }
            }

            ModelFormat.PHI -> buildString {
                if (!systemPrompt.isNullOrEmpty()) {
                    append("<|system|>\n")
                    append(systemPrompt)
                    append("<|end|>\n")
                }
                for ((role, text) in chatHistory) {
                    if (role == "system") continue
                    val tag = if (role == "user") "user" else "assistant"
                    append("<|")
                    append(tag)
                    append("|>\n")
                    append(text)
                    append("<|end|>\n")
                }
                if (prompt.isNotBlank()) {
                    append("<|user|>\n")
                    append(prompt)
                    append("<|end|>\n")
                }
                append("<|assistant|>\n")
            }

            ModelFormat.COMMAND_R -> buildString {
                append("<BOS_TOKEN>")
                if (!systemPrompt.isNullOrEmpty()) {
                    append("<|START_OF_TURN_TOKEN|><|SYSTEM_TOKEN|>")
                    append(systemPrompt)
                    append("<|END_OF_TURN_TOKEN|>")
                }
                for ((role, text) in chatHistory) {
                    if (role == "system") continue
                    val tag = if (role == "user") "<|USER_TOKEN|>" else "<|CHATBOT_TOKEN|>"
                    append("<|START_OF_TURN_TOKEN|>")
                    append(tag)
                    append(text)
                    append("<|END_OF_TURN_TOKEN|>")
                }
                if (prompt.isNotBlank()) {
                    append("<|START_OF_TURN_TOKEN|><|USER_TOKEN|>")
                    append(prompt)
                    append("<|END_OF_TURN_TOKEN|>")
                }
                append("<|START_OF_TURN_TOKEN|><|CHATBOT_TOKEN|>")
            }

            ModelFormat.VICUNA -> buildString {
                if (!systemPrompt.isNullOrEmpty()) {
                    append("SYSTEM: ")
                    append(systemPrompt)
                    append("\n")
                }
                for ((role, text) in chatHistory) {
                    if (role == "system") continue
                    val tag = if (role == "user") "USER" else "ASSISTANT"
                    append(tag)
                    append(": ")
                    append(text)
                    append("\n")
                }
                if (prompt.isNotBlank()) {
                    append("USER: ")
                    append(prompt)
                    append("\n")
                }
                append("ASSISTANT:")
            }

            ModelFormat.ALPACA -> buildString {
                if (!systemPrompt.isNullOrEmpty()) {
                    append("### System:\n")
                    append(systemPrompt)
                    append("\n\n")
                }
                for ((role, text) in chatHistory) {
                    if (role == "system") continue
                    val tag = if (role == "user") "User" else "Assistant"
                    append("### ")
                    append(tag)
                    append(":\n")
                    append(text)
                    append("\n\n")
                }
                if (prompt.isNotBlank()) {
                    append("### User:\n")
                    append(prompt)
                    append("\n\n")
                }
                append("### Assistant:\n")
            }

            ModelFormat.FALCON -> buildString {
                if (!systemPrompt.isNullOrEmpty()) {
                    append("System: ")
                    append(systemPrompt)
                    append("\n")
                }
                for ((role, text) in chatHistory) {
                    if (role == "system") continue
                    val tag = if (role == "user") "User" else "Falcon"
                    append(tag)
                    append(": ")
                    append(text)
                    append("\n")
                }
                if (prompt.isNotBlank()) {
                    append("User: ")
                    append(prompt)
                    append("\n")
                }
                append("Falcon:")
            }

            ModelFormat.ZEPHYR -> buildString {
                append("<s>")
                if (!systemPrompt.isNullOrEmpty()) {
                    append("<|system|>\n")
                    append(systemPrompt)
                    append("</s>\n")
                }
                for ((role, text) in chatHistory) {
                    if (role == "system") continue
                    if (role == "user") {
                        append("<|user|>\n")
                        append(text)
                        append("</s>\n")
                    } else {
                        append("<|assistant|>\n")
                        append(text)
                        append("</s>\n")
                    }
                }
                if (prompt.isNotBlank()) {
                    append("<|user|>\n")
                    append(prompt)
                    append("</s>\n")
                }
                append("<|assistant|>\n")
            }

            ModelFormat.NEUTRAL -> buildString {
                if (!systemPrompt.isNullOrEmpty()) {
                    append("System: ")
                    append(systemPrompt)
                    append("\n\n")
                }
                for ((role, text) in chatHistory) {
                    if (role == "system") continue
                    val tag = if (role == "user") "User" else "Assistant"
                    append(tag)
                    append(": ")
                    append(text)
                    append("\n\n")
                }
                if (prompt.isNotBlank()) {
                    append("User: ")
                    append(prompt)
                    append("\n\n")
                }
                append("Assistant:")
            }
        }
    }
           private fun buildSingleTurnPrompt(prompt: String, systemPrompt: String?): String {
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
            ModelFormat.DEEPSEEK -> listOf("<｜end▁of▁sentence｜>", "<|endoftext|>")
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
            // Убираем блоки размышлений reasoning-моделей
            .replace(Regex("<think>[\\s\\S]*?</think>", RegexOption.IGNORE_CASE), "")
            .replace(Regex("<thinking>[\\s\\S]*?</thinking>", RegexOption.IGNORE_CASE), "")
            .replace(Regex("<reasoning>[\\s\\S]*?</reasoning>", RegexOption.IGNORE_CASE), "")
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
