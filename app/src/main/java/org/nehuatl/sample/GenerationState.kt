package org.nehuatl.sample

sealed class GenerationState {
    object Idle : GenerationState()
    object LoadingModel : GenerationState()
    data class ModelLoaded(val path: String) : GenerationState()  // Изменено на data class с параметром path
    object AnalyzingImage : GenerationState()
    data class Generating(val prompt: String, val startTime: Long, val tokensGenerated: Int) : GenerationState()
    data class Completed(val prompt: String, val tokenCount: Int, val durationMs: Long) : GenerationState()
    data class Error(val message: String) : GenerationState()

    fun canGenerate(): Boolean {
        return this is Idle || this is ModelLoaded || this is Completed || this is Error
    }

    fun isActive(): Boolean {
        return this is Generating || this is AnalyzingImage
    }
}
