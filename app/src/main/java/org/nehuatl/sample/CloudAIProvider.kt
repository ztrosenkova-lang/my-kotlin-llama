package org.nehuatl.sample

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class CloudAIProvider(
    private val context: Context,
    private val scope: CoroutineScope,
    private val sharedFlow: MutableSharedFlow<CloudAIEvent>,
    private val preferences: SharedPreferences
) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private var currentConfig: CloudAIConfig? = null

    init {
        loadConfig()
    }

    private fun loadConfig() {
        val config = CloudAIConfig(
            apiUrl = preferences.getString("cloud_api_url", "") ?: "",
            modelId = preferences.getString("cloud_model_id", "") ?: "",
            apiToken = preferences.getString("cloud_api_token", "") ?: ""
        )
        if (config.isValid()) {
            currentConfig = config
            Log.d("CloudAIProvider", "Конфигурация загружена: ${config.modelId}")
        }
    }

    fun saveConfig(config: CloudAIConfig) {
        preferences.edit().apply {
            putString("cloud_api_url", config.apiUrl)
            putString("cloud_model_id", config.modelId)
            putString("cloud_api_token", config.apiToken)
            apply()
        }
        currentConfig = config
        Log.d("CloudAIProvider", "Конфигурация сохранена: ${config.modelId}")
    }

    fun clearConfig() {
        preferences.edit().apply {
            remove("cloud_api_url")
            remove("cloud_model_id")
            remove("cloud_api_token")
            apply()
        }
        currentConfig = null
        Log.d("CloudAIProvider", "Конфигурация очищена")
    }

    fun isConfigured(): Boolean {
        return currentConfig?.isValid() == true
    }

    fun getConfig(): CloudAIConfig? {
        return currentConfig
    }

    fun generate(prompt: String, systemPrompt: String = "", chatHistory: List<ChatMessage> = emptyList()) {
        val config = currentConfig
        if (config == null || !config.isValid()) {
            scope.launch {
                sharedFlow.emit(CloudAIEvent.Error("Облачный ИИ не настроен"))
            }
            return
        }

        scope.launch {
            try {
                sharedFlow.emit(CloudAIEvent.Started(prompt))
                val startTime = System.currentTimeMillis()

                // Формируем запрос для СберГигачата (совместим с OpenAI API)
                val messages = mutableListOf<Map<String, String>>()
                
                // Добавляем системный промпт
                if (systemPrompt.isNotEmpty()) {
                    messages.add(mapOf("role" to "system", "content" to systemPrompt))
                }
                
                // Добавляем историю чата
                chatHistory.forEach { msg ->
                    messages.add(mapOf("role" to msg.role, "content" to msg.text))
                }
                
                // Добавляем текущий запрос
                messages.add(mapOf("role" to "user", "content" to prompt))

                val jsonBody = JSONObject().apply {
                    put("model", config.modelId)
                    put("messages", messages)
                    put("stream", false)
                    put("temperature", 0.7)
                    put("max_tokens", 2048)
                }

                val request = Request.Builder()
                    .url(config.apiUrl)
                    .addHeader("Authorization", "Bearer ${config.apiToken}")
                    .addHeader("Content-Type", "application/json")
                    .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
                    .build()

                val response = withContext(Dispatchers.IO) {
                    client.newCall(request).execute()
                }

                if (response.isSuccessful) {
                    val responseBody = response.body?.string() ?: ""
                    val jsonResponse = JSONObject(responseBody)
                    
                    // Парсим ответ (формат OpenAI)
                    val content = jsonResponse
                        .getJSONArray("choices")
                        .getJSONObject(0)
                        .getJSONObject("message")
                        .getString("content")

                    val totalTokens = jsonResponse
                        .getJSONObject("usage")
                        .optInt("total_tokens", 0)

                    val duration = System.currentTimeMillis() - startTime
                    
                    // Эмулируем стриминг токенов для единообразия
                    val words = content.split(" ")
                    var tokenCount = 0
                    words.forEach { word ->
                        tokenCount++
                        sharedFlow.emit(CloudAIEvent.Ongoing(word, tokenCount))
                    }

                    sharedFlow.emit(CloudAIEvent.Done(content, totalTokens, duration))
                    Log.d("CloudAIProvider", "Генерация завершена, токенов: $totalTokens")
                } else {
                    val errorBody = response.body?.string() ?: "Unknown error"
                    Log.e("CloudAIProvider", "Ошибка API: ${response.code} - $errorBody")
                    sharedFlow.emit(CloudAIEvent.Error("Ошибка API: ${response.code}"))
                }

                response.close()
            } catch (e: Exception) {
                Log.e("CloudAIProvider", "Ошибка генерации", e)
                sharedFlow.emit(CloudAIEvent.Error("Ошибка: ${e.message}"))
            }
        }
    }
}

sealed class CloudAIEvent {
    data class Started(val prompt: String) : CloudAIEvent()
    data class Ongoing(val token: String, val tokenCount: Int) : CloudAIEvent()
    data class Done(val fullText: String, val tokenCount: Int, val duration: Long) : CloudAIEvent()
    data class Error(val message: String) : CloudAIEvent()
}
