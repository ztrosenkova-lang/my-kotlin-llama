package org.nehuatl.sample

data class CloudAIConfig(
    val apiUrl: String,
    val modelId: String = "GigaChat",
    val authKey: String,
    val isGigaChat: Boolean = true
) {
    fun isValid(): Boolean {
        return apiUrl.isNotBlank() && authKey.isNotBlank()
    }
}
