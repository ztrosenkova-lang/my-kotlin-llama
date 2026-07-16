package org.nehuatl.sample

sealed class GenerationState {
    object Idle : GenerationState()
    object LoadingModel : GenerationState()
    object ModelLoaded : GenerationState()
    object AnalyzingImage : GenerationState() // Новое состояние для анализа изображения
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
