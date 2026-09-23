package org.nehuatl.sample

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun RobotOverlayContent(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val isSpeaking by viewModel.isSpeaking.collectAsStateWithLifecycle()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val cloudState by viewModel.cloudState.collectAsStateWithLifecycle()
    val isModelLoaded by viewModel.isModelLoaded.collectAsStateWithLifecycle()

    val isThinking = state is GenerationState.Generating ||
                     cloudState is CloudAIState.Generating
    val isIdle = !isSpeaking && !isThinking

    Box(
        modifier = Modifier.fillMaxHeight(),
        contentAlignment = Alignment.Center
    ) {
                ThinkingRobotAnimation(
            height = 90.dp,
            isActive = true,
            isSpeaking = isSpeaking,
            isThinking = isThinking,
            isIdle = isIdle,
            shouldWave = false,
            isAiReady = isModelLoaded || (cloudState is CloudAIState.Ready),
            modifier = Modifier.fillMaxSize()
        )
    }
}
