package org.nehuatl.sample

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.UUID
import java.util.concurrent.TimeUnit

class CloudAIProvider(
    private val context: Context,
    private val scope: CoroutineScope,
    private val sharedFlow: MutableSharedFlow<CloudAIEvent>,
    private val preferences: SharedPreferences
) {
    companion object {
        private const val TAG = "CloudAIProvider"
        private const val PREFS_KEY_API_URL = "cloud_api_url"
        private const val PREFS_KEY_MODEL_ID = "cloud_model_id"
        private const val PREFS_KEY_AUTH_KEY = "cloud_auth_key"
        private const val PREFS_KEY_ACCESS_TOKEN = "cloud_access_token"
        private const val PREFS_KEY_IS_GIGACHAT = "cloud_is_gigachat"
        private const val AUTH_URL = "https://ngw.devices.sberbank.ru:9443/api/v2/oauth"
        private val JSON_MEDIA_TYPE = "application/json".toMediaType()
    }

    private val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(90, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()
    }

    private var accessToken: String? = null

    fun isConfigured(): Boolean {
        val authKey = preferences.getString(PREFS_KEY_AUTH_KEY, null)
        val apiUrl = preferences.getString(PREFS_KEY_API_URL, null)
        return !authKey.isNullOrEmpty() && !apiUrl.isNullOrEmpty()
    }

    fun getConfig(): CloudAIConfig? {
        val apiUrl = preferences.getString(PREFS_KEY_API_URL, null)
        val modelId = preferences.getString(PREFS_KEY_MODEL_ID, "GigaChat")
        val authKey = preferences.getString(PREFS_KEY_AUTH_KEY, null)
        val isGigaChat = preferences.getBoolean(PREFS_KEY_IS_GIGACHAT, true)
        
        return if (apiUrl != null && authKey != null) {
            CloudAIConfig(
                apiUrl = apiUrl,
                modelId = modelId ?: "GigaChat",
                authKey = authKey,
                isGigaChat = isGigaChat
            )
        } else null
    }

    fun saveConfig(config: CloudAIConfig) {
        preferences.edit().apply {
            putString(PREFS_KEY_API_URL, config.apiUrl)
            putString(PREFS_KEY_MODEL_ID, config.modelId)
            putString(PREFS_KEY_AUTH_KEY, config.authKey)
            putBoolean(PREFS_KEY_IS_GIGACHAT, config.isGigaChat)
            apply()
        }
        if (config.isGigaChat) {
            accessToken = null
            preferences.edit().remove(PREFS_KEY_ACCESS_TOKEN).apply()
        } else {
            accessToken = config.authKey
        }
    }

    fun clearConfig() {
        preferences.edit().clear().apply()
        accessToken = null
    }

    suspend fun generateToken(): Boolean {
        val config = getConfig() ?: return false
        if (!config.isGigaChat) {
            accessToken = config.authKey
            sharedFlow.tryEmit(CloudAIEvent.TokenReceived)
            return true
        }

        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Запрашиваем токен GigaChat")
                val requestBody = "scope=GIGACHAT_API_PERS".toRequestBody("application/x-www-form-urlencoded".toMediaType())

                val request = Request.Builder()
                    .url(AUTH_URL)
                    .header("Authorization", "Basic ${config.authKey}")
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .header("RqUID", UUID.randomUUID().toString())
                    .post(requestBody)
                    .build()

                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        val errorBody = response.body?.string() ?: "Unknown error"
                        Log.e(TAG, "Ошибка получения токена: ${response.code}, $errorBody")
                        sharedFlow.tryEmit(CloudAIEvent.Error("Ошибка авторизации: ${response.code}"))
                        return@withContext false
                    }

                    val responseBody = response.body?.string() ?: return@withContext false
                    val json = JSONObject(responseBody)
                    val token = json.optString("access_token", null)
                    
                    if (token != null) {
                        accessToken = token
                        preferences.edit().putString(PREFS_KEY_ACCESS_TOKEN, token).apply()
                        Log.d(TAG, "Токен получен")
                        sharedFlow.tryEmit(CloudAIEvent.TokenReceived)
                        return@withContext true
                    } else {
                        Log.e(TAG, "Токен не найден в ответе: $responseBody")
                        sharedFlow.tryEmit(CloudAIEvent.Error("Не удалось получить токен"))
                        return@withContext false
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Ошибка получения токена", e)
                sharedFlow.tryEmit(CloudAIEvent.Error("Ошибка сети: ${e.message}"))
                return@withContext false
            }
        }
    }

    fun generate(prompt: String, systemPrompt: String, chatHistory: List<ChatMessage>) {
        val config = getConfig()
        if (config == null) {
            sharedFlow.tryEmit(CloudAIEvent.Error("Облачный ИИ не настроен"))
            return
        }

        scope.launch {
            try {
                sharedFlow.tryEmit(CloudAIEvent.Started(prompt))

                val token = if (config.isGigaChat) {
                    val savedToken = preferences.getString(PREFS_KEY_ACCESS_TOKEN, null)
                    if (savedToken != null) {
                        accessToken = savedToken
                        savedToken
                    } else {
                        val success = generateToken()
                        if (!success) {
                            sharedFlow.tryEmit(CloudAIEvent.Error("Не удалось получить токен"))
                            return@launch
                        }
                        accessToken ?: run {
                            sharedFlow.tryEmit(CloudAIEvent.Error("Токен пустой"))
                            return@launch
                        }
                    }
                } else {
                    config.authKey
                }

                val messages = JSONArray().apply {
                    put(JSONObject().apply {
                        put("role", "system")
                        put("content", systemPrompt)
                    })
                    
                    val historySize = minOf(chatHistory.size, 20)
                    for (i in (chatHistory.size - historySize) until chatHistory.size) {
                        val msg = chatHistory[i]
                        put(JSONObject().apply {
                            put("role", if (msg.role == "user") "user" else "assistant")
                            put("content", msg.text)
                        })
                    }
                    
                    put(JSONObject().apply {
                        put("role", "user")
                        put("content", prompt)
                    })
                }

                val requestBody = JSONObject().apply {
                    put("model", config.modelId)
                    put("messages", messages)
                    put("temperature", 0.7)
                    put("max_tokens", 2048)
                }.toString().toRequestBody(JSON_MEDIA_TYPE)

                val request = Request.Builder()
                    .url(config.apiUrl)
                    .header("Authorization", "Bearer $token")
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .post(requestBody)
                    .build()

                var fullResponse = ""
                var tokenCount = 0
                val startTime = System.currentTimeMillis()

                val response = client.newCall(request).execute()
                try {
                    if (!response.isSuccessful) {
                        val errorBody = response.body?.string() ?: "Unknown error"
                        Log.e(TAG, "Ошибка генерации: ${response.code}, $errorBody")
                        
                        if (response.code == 401 && config.isGigaChat) {
                            Log.d(TAG, "Токен истек, пробуем обновить")
                            preferences.edit().remove(PREFS_KEY_ACCESS_TOKEN).apply()
                            accessToken = null
                            
                            val success = generateToken()
                            if (success) {
                                val newToken = accessToken ?: return@launch
                                val retryRequest = request.newBuilder()
                                    .header("Authorization", "Bearer $newToken")
                                    .build()
                                val retryResponse = client.newCall(retryRequest).execute()
                                try {
                                    if (retryResponse.isSuccessful) {
                                        val retryBody = retryResponse.body?.string() ?: ""
                                        fullResponse = parseResponse(retryBody)
                                    } else {
                                        sharedFlow.tryEmit(CloudAIEvent.Error("Ошибка после обновления токена: ${retryResponse.code}"))
                                        return@launch
                                    }
                                } finally {
                                    retryResponse.close()
                                }
                            } else {
                                sharedFlow.tryEmit(CloudAIEvent.Error("Не удалось обновить токен"))
                                return@launch
                            }
                        } else {
                            sharedFlow.tryEmit(CloudAIEvent.Error("API ошибка: ${response.code}"))
                            return@launch
                        }
                    } else {
                        val responseBody = response.body?.string() ?: ""
                        fullResponse = parseResponse(responseBody)
                    }
                } finally {
                    response.close()
                }

                if (fullResponse.isNotEmpty()) {
                    tokenCount = fullResponse.split(" ").size
                    sharedFlow.tryEmit(CloudAIEvent.Ongoing(fullResponse, tokenCount))
                    val duration = System.currentTimeMillis() - startTime
                    sharedFlow.tryEmit(CloudAIEvent.Done(fullResponse, tokenCount, duration))
                } else {
                    sharedFlow.tryEmit(CloudAIEvent.Error("Получен пустой ответ"))
                }

            } catch (e: IOException) {
                Log.e(TAG, "Сетевая ошибка", e)
                sharedFlow.tryEmit(CloudAIEvent.Error("Сетевая ошибка: ${e.message}"))
            } catch (e: Exception) {
                Log.e(TAG, "Ошибка генерации", e)
                sharedFlow.tryEmit(CloudAIEvent.Error("Ошибка: ${e.message}"))
            }
        }
    }

    private fun parseResponse(responseBody: String): String {
        return try {
            val json = JSONObject(responseBody)
            val choices = json.optJSONArray("choices")
            if (choices != null && choices.length() > 0) {
                val choice = choices.getJSONObject(0)
                val message = choice.optJSONObject("message")
                message?.optString("content", "") ?: ""
            } else {
                json.optString("response", json.optString("result", ""))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка парсинга ответа", e)
            responseBody
        }
    }

    fun abort() {
        sharedFlow.tryEmit(CloudAIEvent.Error("Отмена запроса"))
    }
}

// CloudAIConfig вынесен в отдельный файл, поэтому здесь он не объявляется!
