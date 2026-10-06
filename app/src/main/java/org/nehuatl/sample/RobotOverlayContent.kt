package org.nehuatl.sample

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay
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

    val isThinking = state is GenerationState.Generating ||
                     cloudState is CloudAIState.Generating
    val isIdle = !isSpeaking && !isThinking

    // Реакция на одиночный тап — наклон головы
    var headTiltTarget by remember { mutableStateOf(0f) }
    var headNodTarget by remember { mutableStateOf(0f) }
    var isListening by remember { mutableStateOf(false) }

    // Автосброс наклона через 0.5 сек
    LaunchedEffect(headTiltTarget) {
        if (headTiltTarget != 0f) {
            delay(500)
            headTiltTarget = 0f
        }
    }

    // Автосброс кивка через 0.5 сек
    LaunchedEffect(headNodTarget) {
        if (headNodTarget != 0f) {
            delay(500)
            headNodTarget = 0f
        }
    }

    Box(
        modifier = Modifier
            .fillMaxHeight()
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { offset ->
                        // Одиночный тап — наклон головы в сторону тапа
                        val boxWidth = size.width.toFloat()
                        val tiltDirection = if (offset.x < boxWidth / 2f) -1f else 1f
                        headTiltTarget = tiltDirection * 8f
                        headNodTarget = 0.3f
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
                ThinkingRobotAnimation(
            height = 72.dp,
            isActive = true,
            isSpeaking = isSpeaking,
            isThinking = isThinking,
            isIdle = isIdle && !overlayListening,
            shouldWave = waveSignal || robotGreetingSignal,
            isAiReady = isModelLoaded || (cloudState is CloudAIState.Ready),
            isSmartMode = isSmartMode,
            headTilt = headTiltTarget,
            headNod = headNodTarget,
            uDivisor = 350f,
            yOffsetUnits = 24f,
            modifier = Modifier.fillMaxHeight()
        )
    }
}
