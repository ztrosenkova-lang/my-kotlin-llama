package org.nehuatl.sample

sealed class GenerationState {
    object Idle : GenerationState()
    object LoadingModel : GenerationState()
    data class ModelLoaded(val path: String) : GenerationState()
    data class AnalyzingImage(val imagePath: String) : GenerationState()
    data class Generating(
        val prompt: String,
        val tokensGenerated: Int
    ) : GenerationState()
    data class Completed(val tokenCount: Int, val durationMs: Long) : GenerationState()
    data class Error(val message: String) : GenerationState()
    
    fun isActive(): Boolean = this is Generating || this is AnalyzingImage
    fun canGenerate(): Boolean = this is Idle || this is ModelLoaded || this is Completed || this is Error
}
