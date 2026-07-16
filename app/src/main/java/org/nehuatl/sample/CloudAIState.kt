package org.nehuatl.sample

sealed class CloudAIState {
    object Idle : CloudAIState()
    data class Ready(val modelId: String) : CloudAIState()
    data class Generating(
        val prompt: String,
        val startTime: Long,
        val tokensGenerated: Int
    ) : CloudAIState()
    data class Completed(val tokenCount: Int, val durationMs: Long) : CloudAIState()
    data class Error(val message: String) : CloudAIState()
    
    fun isActive(): Boolean = this is Generating
    fun canGenerate(): Boolean = this is Ready || this is Idle || this is Completed || this is Error
}
