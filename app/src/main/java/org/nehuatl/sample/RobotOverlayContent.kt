package org.nehuatl.sample

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
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
    val waveSignal by viewModel.overlayWaveSignal.collectAsStateWithLifecycle(initialValue = false)
    val isSmartMode by viewModel.isSmartMode.collectAsStateWithLifecycle(initialValue = false)
    val robotGreetingSignal by viewModel.robotGreetingSignal.collectAsStateWithLifecycle(initialValue = false)
    val overlayListening by viewModel.overlayListening.collectAsStateWithLifecycle(initialValue = false)
    val overlayHeadTilt by viewModel.overlayHeadTilt.collectAsStateWithLifecycle(initialValue = 0f)
    val overlayHeadNod by viewModel.overlayHeadNod.collectAsStateWithLifecycle(initialValue = 0f)
    val overlayTapWaveDirection by viewModel.overlayTapWaveDirection.collectAsStateWithLifecycle(initialValue = 0f)
    val robotAutoAnimation by viewModel.robotAutoAnimation.collectAsStateWithLifecycle(initialValue = 0)
    val isCompressing by viewModel.isCompressing.collectAsStateWithLifecycle(initialValue = false)
    val visionPhase by viewModel.visionPhase.collectAsStateWithLifecycle(initialValue = VisionPhase.IDLE)
    val isPreparingSpeech by viewModel.isPreparingSpeech.collectAsStateWithLifecycle(initialValue = false)
    val robotJumpSignal by viewModel.robotJumpSignal.collectAsStateWithLifecycle(initialValue = false)
    val overlayRobotScale by viewModel.overlayRobotScale.collectAsStateWithLifecycle(initialValue = 1.0f)

    val isThinking = state is GenerationState.Generating ||
                     cloudState is CloudAIState.Generating
    val isIdle = !isSpeaking && !isThinking

                   Box(
        modifier = Modifier
            .width(290.dp * overlayRobotScale * 0.85f)
            .height(290.dp * overlayRobotScale),
        contentAlignment = Alignment.Center
    ) {
            ThinkingRobotAnimation(
            height = (290 * overlayRobotScale).dp,
            isActive = true,
            isSpeaking = isSpeaking,
            isThinking = isThinking,
            isIdle = isIdle && !overlayListening,
            isListening = overlayListening,
            shouldWave = waveSignal || robotGreetingSignal,
            isAiReady = isModelLoaded || (cloudState is CloudAIState.Ready),
            isSmartMode = isSmartMode,
            headTilt = overlayHeadTilt,
            headNod = overlayHeadNod,
            uDivisor = 285f,
            yOffsetUnits = 10f,
            tapWaveDirection = overlayTapWaveDirection,
            autoAnimation = robotAutoAnimation,
                       isCompressing = isCompressing,
            isLoadingModel = state is GenerationState.LoadingModel,
            visionPhase = visionPhase,
            isPreparingSpeech = isPreparingSpeech,
            isJumping = robotJumpSignal,
            bobAmplitude = 0f,
            modifier = Modifier
        )
    }
}
