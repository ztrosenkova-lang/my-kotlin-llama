package org.nehuatl.sample

data class CloudAIConfig(
    val apiUrl: String = "",
    val modelId: String = "",
    val apiToken: String = ""
) {
    fun isValid(): Boolean {
        return apiUrl.isNotBlank() && modelId.isNotBlank() && apiToken.isNotBlank()
    }
}
