package org.nehuatl.sample

sealed class CloudAIEvent {
    data class Started(val prompt: String) : CloudAIEvent()
    data class Ongoing(val text: String, val tokenCount: Int) : CloudAIEvent()
    data class Done(val fullText: String, val tokenCount: Int, val duration: Long) : CloudAIEvent()
    data class Error(val message: String) : CloudAIEvent()
    object TokenReceived : CloudAIEvent()
}
