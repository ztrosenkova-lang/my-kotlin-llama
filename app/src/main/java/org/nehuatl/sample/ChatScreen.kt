package org.nehuatl.sample

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.speech.RecognizerIntent
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import androidx.compose.animation.core.*
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sin
import kotlin.math.cos
import androidx.compose.animation.core.keyframes
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.Brush

enum class AIMode {
    LOCAL,
    NEUTRAL,
    CLOUD
}

@Composable
fun ChatScreen(
    modifier: Modifier = Modifier,
    viewModel: MainViewModel,
    currentModelPath: String?,
    mmprojPath: String?,
    onPickModel: () -> Unit,
    onPickMmproj: () -> Unit,
    onPickImage: () -> Unit,
    onImageUsed: () -> Unit,
    imagePath: String? = null
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val generatedText by viewModel.generatedText.collectAsStateWithLifecycle()
    val cloudGeneratedText by viewModel.cloudGeneratedText.collectAsStateWithLifecycle()
    val systemPromptText by viewModel.systemPrompt.collectAsStateWithLifecycle()
    val chatMessages by viewModel.chatHistory.collectAsStateWithLifecycle()
    val temperature by viewModel.temperature.collectAsStateWithLifecycle()
    val maxTokens by viewModel.maxTokens.collectAsStateWithLifecycle()
    val contextSize by viewModel.contextSize.collectAsStateWithLifecycle()
    val cloudState by viewModel.cloudState.collectAsStateWithLifecycle()
    val isModelLoaded by viewModel.isModelLoaded.collectAsStateWithLifecycle()
    val isTtsReady by viewModel.isTtsReady.collectAsStateWithLifecycle()
    val currentMode by viewModel.currentMode.collectAsStateWithLifecycle()
    val isAppLocked by viewModel.isAppLocked.collectAsStateWithLifecycle()
    val isDeviceBound by viewModel.isDeviceBound.collectAsStateWithLifecycle(initialValue = false)
    val loadedModelName by viewModel.loadedModelName.collectAsStateWithLifecycle(initialValue = "")
    val remainingTimeText by viewModel.remainingTimeText.collectAsStateWithLifecycle(initialValue = "")
    val isPermanentlyUnlocked by viewModel.isPermanentlyUnlocked.collectAsStateWithLifecycle(initialValue = false)
    val isPermanentlyBlocked by viewModel.isPermanentlyBlocked.collectAsStateWithLifecycle(initialValue = false)
    val isSpeaking by viewModel.isSpeaking.collectAsStateWithLifecycle(initialValue = false)
    val speakStartTrigger by viewModel.speakStartTrigger.collectAsStateWithLifecycle(initialValue = false)
    val pendingTextToPrint by viewModel.pendingTextToPrint.collectAsStateWithLifecycle(initialValue = "")

    var promptInput by remember { mutableStateOf("") }
    var showModelDialog by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }
    var showPromptSettings by remember { mutableStateOf(false) }
    var showCloudDialog by remember { mutableStateOf(false) }
    var tempPromptText by remember(systemPromptText) { mutableStateOf(systemPromptText) }
    var tempTemperature by remember(temperature) { mutableStateOf(temperature) }
    var showHelpDialog by remember { mutableStateOf(false) }
    var showMemoryEditor by remember { mutableStateOf(false) }
    var memoryEditText by remember { mutableStateOf("") }
    var cloudApiUrl by remember { mutableStateOf("https://gigachat.devices.sberbank.ru/api/v1/chat/completions") }
    var cloudAuthKey by remember { mutableStateOf("") }
    var cloudIsGigaChat by remember { mutableStateOf(true) }
    var isGeneratingToken by remember { mutableStateOf(false) }
    var secretPhraseInput by remember { mutableStateOf("") }
    var welcomeStarted by remember { mutableStateOf(false) }
    var welcomeTextPrinted by remember { mutableStateOf(false) }
    var pendingTextPrinted by remember { mutableStateOf(false) }

    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()

    val speechRecognizerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val data: Intent? = result.data
            val results = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            if (!results.isNullOrEmpty()) {
                val recognizedText = results[0]
                if (recognizedText.isNotBlank()) {
                    viewModel.sendUserMessage(recognizedText)
                }
            }
        } else {
            viewModel.appendSystemMessage("⚠️ Распознавание речи отменено или не удалось")
        }
    }

    LaunchedEffect(Unit) {
        val hasRecordPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
        if (!hasRecordPermission) {
            viewModel.appendSystemMessage("⚠️ Для работы распознавания речи требуется разрешение на запись аудио.")
        }
    }

    val fullWelcomeString = "Привет! Я твой персональный ИИ Друг. 🤖✨ " +
        "Я лучший хранитель паролей, переводчик с разных языков и просто умный собеседник. " +
        "Я создан, чтобы быть твоим надежным и автономным союзником. " +
        "Я умею слышать и говорить. 🎤 Нажимай на микрофон внизу, чтобы общаться голосом. " +
        "Я обладаю уникальной памятью. 🧠 Подробнее об этом ты можешь узнать в справке. " +
        "А ещё я могу напоминать тебе о важных событиях,заменяя тебе органайзер. ⏰ " +
        "Давай общаться! Включи локальный движок Llama или облачный ИИ в шапке приложения, и погнали! 🚀"

    LaunchedEffect(isTtsReady) {
        if (isTtsReady && !welcomeStarted) {
            welcomeStarted = true
            viewModel.speakText(fullWelcomeString)
        }
    }

    LaunchedEffect(speakStartTrigger) {
    if (speakStartTrigger && welcomeStarted && !welcomeTextPrinted) {
        welcomeTextPrinted = true
        var runningText = ""
        for (i in fullWelcomeString.indices) {
            runningText += fullWelcomeString[i]
            viewModel.updateLastSystemMessage(runningText)
            delay(50)
        }
    }
}

LaunchedEffect(speakStartTrigger, pendingTextToPrint) {
    if (speakStartTrigger && pendingTextToPrint.isNotEmpty() && !pendingTextPrinted) {
        pendingTextPrinted = true
        var runningText = ""
        for (i in pendingTextToPrint.indices) {
            runningText += pendingTextToPrint[i]
            viewModel.updateAssistantMessage(runningText)
            delay(50)
        }
        viewModel.clearPendingText()
    }
}

LaunchedEffect(isSpeaking) {
    if (!isSpeaking) {
        pendingTextPrinted = false
    }
}

    LaunchedEffect(showCloudDialog) {
        if (showCloudDialog) {
            val config = viewModel.getCloudConfig()
            if (config != null) {
                cloudApiUrl = config.apiUrl
                cloudAuthKey = config.authKey
                cloudIsGigaChat = config.isGigaChat
            } else {
                cloudApiUrl = "https://gigachat.devices.sberbank.ru/api/v1/chat/completions"
                cloudIsGigaChat = true
            }
        }
    }

    val lastMessageText = chatMessages.lastOrNull()?.text ?: ""
    LaunchedEffect(chatMessages.size, generatedText.length, cloudGeneratedText.length, lastMessageText) {
        if (chatMessages.isNotEmpty() || generatedText.isNotEmpty() || cloudGeneratedText.isNotEmpty()) {
            scrollState.animateScrollTo(
                value = scrollState.maxValue,
                animationSpec = tween(
                    durationMillis = 300,
                    easing = LinearOutSlowInEasing
                )
            )
        }
    }

    LaunchedEffect(showSettings) {
        if (showSettings) {
            tempTemperature = temperature
        }
    }

    if (isAppLocked) {
        LockScreen(
            secretPhrase = secretPhraseInput,
            onSecretPhraseChange = { secretPhraseInput = it },
            onVerify = {
                viewModel.verifySecretPhrase(secretPhraseInput)
                secretPhraseInput = ""
            },
            viewModel = viewModel,
            isPermanentlyBlocked = isPermanentlyBlocked
        )
        return
    }

    if (showModelDialog) {
        ModelPickerDialog(
            currentModelPath = currentModelPath,
            mmprojPath = mmprojPath,
            onPickModel = onPickModel,
            onPickMmproj = onPickMmproj,
            onLoad = {
                showModelDialog = false
                if (currentModelPath != null) {
                    viewModel.loadModel(currentModelPath, mmprojPath)
                    viewModel.setCurrentMode(AIMode.LOCAL)
                }
            },
            onDismiss = { showModelDialog = false }
        )
    }

    val isCloudReady = viewModel.getCloudConfig()?.authKey?.isNotEmpty() == true || cloudState is CloudAIState.Ready

    if (showCloudDialog) {
        CloudAIDialog(
            apiUrl = cloudApiUrl,
            authKey = cloudAuthKey,
            isGigaChat = cloudIsGigaChat,
            isCloudReady = isCloudReady,
            onApiUrlChange = { cloudApiUrl = it },
            onAuthKeyChange = { cloudAuthKey = it },
            onIsGigaChatChange = { cloudIsGigaChat = it },
            onSave = {
                val config = CloudAIConfig(
                    apiUrl = cloudApiUrl,
                    modelId = if (cloudIsGigaChat) "GigaChat" else "Custom",
                    authKey = cloudAuthKey,
                    isGigaChat = cloudIsGigaChat
                )
                viewModel.saveCloudConfig(config)
                viewModel.setCurrentMode(AIMode.CLOUD)
                showCloudDialog = false
                viewModel.appendSystemMessage("☁️ Облачный ИИ активирован")
            },
            onClear = {
                cloudApiUrl = if (cloudIsGigaChat) "https://gigachat.devices.sberbank.ru/api/v1/chat/completions" else "https://openrouter.ai/api/v1/chat/completions"
                cloudAuthKey = ""
                viewModel.clearCloudConfig()
                viewModel.appendSystemMessage("🧹 Настройки облачного ИИ сброшены")
                if (currentMode == AIMode.CLOUD) {
                    viewModel.setCurrentMode(AIMode.NEUTRAL)
                }
            },
            onDismiss = { showCloudDialog = false },
            onGenerateToken = {
                isGeneratingToken = true
                viewModel.generateCloudToken { success ->
                    isGeneratingToken = false
                    if (success) {
                        viewModel.appendSystemMessage("✅ Токен получен. Нажмите 'Сохранить' для активации облачного ИИ.")
                        val config = CloudAIConfig(
                            apiUrl = cloudApiUrl,
                            modelId = if (cloudIsGigaChat) "GigaChat" else "Custom",
                            authKey = cloudAuthKey,
                            isGigaChat = cloudIsGigaChat
                        )
                        viewModel.saveCloudConfig(config)
                        viewModel.setCloudReady(config.modelId)
                    } else {
                        viewModel.appendSystemMessage("❌ Ошибка подключения к облачному ИИ")
                    }
                }
            },
            isGeneratingToken = isGeneratingToken
        )
    }

    if (showHelpDialog) {
        HelpDialog(
            onDismiss = { showHelpDialog = false },
            viewModel = viewModel
        )
    }

    if (showMemoryEditor) {
        MemoryEditorDialog(
            initialText = viewModel.readFromLongTermMemory(),
            onSave = { viewModel.overwriteLongTermMemory(it) },
            onDismiss = { showMemoryEditor = false }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppBackground)
            .imePadding()
    ) {
      TopBarWithSwitch(
    currentMode = currentMode,
    onModeChange = { newMode ->
        viewModel.setCurrentMode(newMode)
        if (newMode == AIMode.NEUTRAL) {
            viewModel.releaseModel()
            viewModel.clearCloudConfig()
            viewModel.appendSystemMessage("📢 ИИ выгружен из памяти")
        }
    },
    isModelLoaded = isModelLoaded,
    cloudConfig = viewModel.getCloudConfig(),
    onCloudForceDialog = { showCloudDialog = true },
    onLocalForceDialog = { showModelDialog = true },
    statusText = when (currentMode) {
        AIMode.LOCAL -> {
            when (state) {
                is GenerationState.Generating -> "🤖 Локальный ИИ думает..."
                else -> "🤖 Локальный ИИ: Готов к работе"
            }
        }
        AIMode.CLOUD -> {
            when (cloudState) {
                is CloudAIState.Generating -> "☁️ Облако думает..."
                else -> "☁️ Облако: Готово к работе"
            }
        }
           else -> "🤖 ИИ выгружен"
    },
    isGenerating = state.isActive() || cloudState.isActive(),
    isSpeaking = isSpeaking
)

        ControlPanel(
            onMemoryClick = {
                memoryEditText = viewModel.readFromLongTermMemory()
                showMemoryEditor = true
                viewModel.speakText("Редактор базы знаний")
            },
            onSettingsClick = {
                showSettings = !showSettings
                viewModel.speakText("Настройки движка ИИ")
            },
            onPromptSettingsClick = {
                showPromptSettings = !showPromptSettings
                viewModel.speakText("Настройка роли ИИ")
            },
            onHelpClick = {
                viewModel.speakText("Открываю руководство пользователя.")
                showHelpDialog = true
            },
            isTtsReady = isTtsReady,
            viewModel = viewModel,
            context = context,
            coroutineScope = coroutineScope
        )

        if (showSettings) {
            SettingsPanel(
                temperature = tempTemperature,
                onTemperatureChange = { tempTemperature = it },
                maxTokens = maxTokens,
                onMaxTokensChange = { viewModel.updateMaxTokens(it) },
                contextSize = contextSize,
                onContextSizeChange = { viewModel.updateContextSize(it) },
                onModelChangeClick = { showModelDialog = true },
                onSave = {
                    viewModel.updateTemperature(tempTemperature)
                    showSettings = false
                },
                onClose = {
                    tempTemperature = temperature
                    showSettings = false
                }
            )
        }

        if (showPromptSettings) {
            PromptSettingsPanel(
                promptText = tempPromptText,
                onPromptChange = { tempPromptText = it },
                onSave = {
                    viewModel.updateSystemPrompt(tempPromptText)
                    showPromptSettings = false
                }
            )
        }

        StatusBar(
            state = state,
            cloudState = cloudState,
            currentMode = currentMode,
            currentModel = if (isModelLoaded) currentModelPath else null,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            AndroidView(
                factory = { context ->
                    MatrixChatBackground(context)
                },
                modifier = Modifier.matchParentSize()
            )

            Card(
                modifier = Modifier.fillMaxSize(),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, BorderGray),
                colors = CardDefaults.cardColors(
                    containerColor = Color.Transparent
                )
            ) {
                SelectionContainer {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                            .verticalScroll(scrollState)
                    ) {
                        chatMessages.forEach { message ->
    val prefix = when (message.role) {
        "user" -> "Вы: "
        "assistant" -> "ИИ-Друг: "
        "system" -> "📢 "
        else -> ""
    }
    val textColor = when (message.role) {
        "user" -> GreenColor
        "assistant" -> DarkText
        else -> DarkText
    }
    Text(
        text = prefix + message.text,
        color = textColor,
        fontFamily = ChatFontFamily,
        fontSize = 10.sp,
        modifier = Modifier.padding(vertical = 2.dp)
    )
}

                        if (generatedText.isNotEmpty() && state is GenerationState.Generating) {
    Text(
        text = "ИИ: $generatedText",
        color = DarkText,
        fontFamily = ChatFontFamily,
        fontSize = 10.sp,
        modifier = Modifier.padding(vertical = 2.dp)
    )
}

if (cloudGeneratedText.isNotEmpty() && cloudState is CloudAIState.Generating) {
    Text(
        text = "☁️ ИИ: $cloudGeneratedText",
        color = DarkText.copy(alpha = 0.8f),
        fontFamily = ChatFontFamily,
        fontSize = 10.sp,
        modifier = Modifier.padding(vertical = 2.dp)
    )
}

                        if (cloudGeneratedText.isNotEmpty() && cloudState is CloudAIState.Generating) {
    Text(
        text = "☁️ ИИ: $cloudGeneratedText",
        color = DarkText.copy(alpha = 0.8f),
        fontFamily = ChatFontFamily,
        fontSize = 10.sp,
        modifier = Modifier.padding(vertical = 2.dp)
    )
}
                    }
                }
            }
        }

        if (imagePath != null) {
            ImagePreview(imagePath = imagePath)
        }

        PromptInput(
            prompt = promptInput,
            onPromptChange = { promptInput = it },
            onGenerate = {
                keyboardController?.hide()
                viewModel.sendUserMessage(promptInput)
                promptInput = ""
                onImageUsed()
            },
            onAbort = {
                keyboardController?.hide()
                viewModel.abortLocal()
                viewModel.abortCloud()
            },
            onClearChat = { viewModel.clearChat() },
            onPickImage = onPickImage,
            enabled = true,
            isGenerating = state.isActive() || cloudState.isActive(),
            isSpeaking = isSpeaking,
            focusRequester = focusRequester,
            isTtsReady = isTtsReady,
            viewModel = viewModel,
            context = context,
            speechRecognizerLauncher = speechRecognizerLauncher,
            isBound = isDeviceBound,
            modelName = loadedModelName,
            remainingTimeText = remainingTimeText,
            isPermanentlyUnlocked = isPermanentlyUnlocked,
            currentMode = currentMode,
            modifier = Modifier.padding(8.dp)
        )
    }
}

@Composable
private fun VoiceWaveAnimation(
    color: Color,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition()

    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 4f * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 800,
                easing = LinearEasing
            )
        )
    )

    val speechTime by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1800,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        )
    )

    val rawMouthOpen = (
        sin(speechTime * 2.3f) * 0.5f +
        sin(speechTime * 5.7f) * 0.3f +
        sin(speechTime * 11.3f) * 0.2f
    )
    val mouthOpen = 0.15f + 0.85f * abs(rawMouthOpen)

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val midY = height / 2
        val maxAmplitude = height * 0.48f
        val currentAmplitude = maxAmplitude * mouthOpen

        val paddingPx = 11.3f
        val gridLeft = paddingPx
        val gridRight = width - paddingPx
        val gridTop = paddingPx
        val gridBottom = height - paddingPx
        val gridWidth = gridRight - gridLeft
        val gridHeight = gridBottom - gridTop

        val cornerRadius = 16f
        val borderColor = Color(0xFF9E9E9E)
        val borderWidth = 2f

        drawRoundRect(
            color = Color.Transparent,
            topLeft = Offset(gridLeft, gridTop),
            size = androidx.compose.ui.geometry.Size(gridWidth, gridHeight),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius, cornerRadius),
            style = Stroke(width = borderWidth)
        )

        val cellSize = 12f
        val gridColor = Color(0xFFBDBDBD)
        val gridLineWidth = 1f

        var gridX = gridLeft + cellSize
        while (gridX < gridRight) {
            drawLine(
                color = gridColor,
                start = Offset(gridX, gridTop),
                end = Offset(gridX, gridBottom),
                strokeWidth = gridLineWidth
            )
            gridX += cellSize
        }

        var gridY = gridTop + cellSize
        while (gridY < gridBottom) {
            drawLine(
                color = gridColor,
                start = Offset(gridLeft, gridY),
                end = Offset(gridRight, gridY),
                strokeWidth = gridLineWidth
            )
            gridY += cellSize
        }

        val step = 2f
        val startX = gridLeft
        val endX = gridRight

        val upperPath = Path()
        var firstUpperPoint = true

        for (x in 0..width.toInt() step step.toInt()) {
            val xf = x.toFloat()
            if (xf < startX || xf > endX) continue

            val normalizedX = (xf - startX) / (endX - startX)
            val edgeFactor = sin(normalizedX * PI.toFloat())
            val wave = sin(normalizedX * 12f + phase) * 0.5f +
                    sin(normalizedX * 23f + phase * 1.7f) * 0.3f +
                    sin(normalizedX * 37f + phase * 2.3f) * 0.2f
            val y = midY - (currentAmplitude * 0.35f + currentAmplitude * 0.65f * abs(wave)) * edgeFactor

            if (firstUpperPoint) {
                upperPath.moveTo(xf, y)
                firstUpperPoint = false
            } else {
                upperPath.lineTo(xf, y)
            }
        }

        drawPath(
            path = upperPath,
            color = color,
            style = Stroke(
                width = 4f,
                cap = StrokeCap.Round
            )
        )

        val lowerPath = Path()
        var firstLowerPoint = true

        for (x in 0..width.toInt() step step.toInt()) {
            val xf = x.toFloat()
            if (xf < startX || xf > endX) continue

            val normalizedX = (xf - startX) / (endX - startX)
            val edgeFactor = sin(normalizedX * PI.toFloat())
            val wave = sin(normalizedX * 12f + phase + PI.toFloat()) * 0.5f +
                    sin(normalizedX * 23f + phase * 1.7f + PI.toFloat()) * 0.3f +
                    sin(normalizedX * 37f + phase * 2.3f + PI.toFloat()) * 0.2f
            val y = midY + (currentAmplitude * 0.35f + currentAmplitude * 0.65f * abs(wave)) * edgeFactor

            if (firstLowerPoint) {
                lowerPath.moveTo(xf, y)
                firstLowerPoint = false
            } else {
                lowerPath.lineTo(xf, y)
            }
        }

        drawPath(
            path = lowerPath,
            color = color.copy(alpha = 0.85f),
            style = Stroke(
                width = 3.5f,
                cap = StrokeCap.Round
            )
        )

        val connectorLength = 8f

        drawLine(
            color = color,
            start = Offset(startX, midY),
            end = Offset(startX + connectorLength, midY),
            strokeWidth = 4f,
            cap = StrokeCap.Round
        )
        drawLine(
            color = color,
            start = Offset(endX, midY),
            end = Offset(endX - connectorLength, midY),
            strokeWidth = 4f,
            cap = StrokeCap.Round
        )
    }
}

@Composable
fun ThinkingRobotAnimation(
    modifier: Modifier = Modifier,
    height: Dp = 85.dp,
    isActive: Boolean = true,
    isSpeaking: Boolean = false,
    isThinking: Boolean = false,
    isIdle: Boolean = false
) {
    val transition = rememberInfiniteTransition(label = "thinking_robot")

    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = (2.0 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400, easing = LinearEasing)
        ),
        label = "phase"
    )

    val pulse by transition.animateFloat(
        initialValue = 0f,
        targetValue = (2.0 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = LinearEasing)
        ),
        label = "pulse"
    )

    val bob by transition.animateFloat(
        initialValue = 0f,
        targetValue = (2.0 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3200, easing = LinearEasing)
        ),
        label = "bob"
    )

    val blink by transition.animateFloat(
        initialValue = 1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 3600
                1f at 0
                1f at 3200
                0.08f at 3350
                1f at 3500
                1f at 3600
            }
        ),
        label = "blink"
    )

    val lookX by transition.animateFloat(
        initialValue = 0f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 7000
                0f at 0
                0f at 1000
                1f at 1400
                1f at 2600
                -1f at 3000
                -1f at 4200
                0f at 4600
                0f at 5200
                0.6f at 5600
                0.6f at 6200
                0f at 6600
                0f at 7000
            }
        ),
        label = "lookX"
    )

    val lookY by transition.animateFloat(
        initialValue = 0f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 7000
                0f at 0
                0f at 1800
                -0.7f at 2200
                -0.7f at 3000
                0.5f at 3400
                0.5f at 4200
                0f at 4600
                0f at 7000
            }
        ),
        label = "lookY"
    )

    val finalPhase = if (isThinking || isSpeaking) phase else 0f
    val finalPulse = if (isThinking || isSpeaking) pulse else 0f
    val finalBob = if (isIdle) bob else 0f
    val finalBlink = if (isActive || isSpeaking) blink else 1f
    val finalLookX = if (isIdle) lookX else 0f
    val finalLookY = when {
        isIdle -> lookY
        isThinking -> -0.6f
        else -> 0f
    }

    Canvas(
        modifier = modifier
            .height(height)
            .semantics { contentDescription = "ИИ обдумывает запрос" }
    ) {
        val metalLight = Color(0xFFD5DBE0)
        val metal = Color(0xFFB0B8C0)
        val metalDark = Color(0xFF8C98A5)
        val eyeDark = Color(0xFF0F3D4C)
        val cyan = Color(0xFF3FE3F5)
        val cyanSoft = Color(0xFFB9F6FF)
        val brainPink = Color(0xFFEFA5B3)
        val brainDark = Color(0xFFD67E93)
        val orange = Color(0xFFF89B3C)

        val u = size.height / 84f
        val offX = (size.width - 100f * u) / 2f
        val robotTop = 27f
        val robotBottom = 100f
        val robotVisualHeight = (robotBottom - robotTop) * u
        val baseOffY = (size.height - robotVisualHeight) / 2f - robotTop * u
        val offY = baseOffY + 1.2f * u * sin(finalBob)

        fun pt(x: Float, y: Float) = Offset(offX + x * u, offY + y * u)

        fun glow(center: Offset, radius: Float, color: Color) {
            if (radius <= 0f) return
            drawCircle(
                Brush.radialGradient(listOf(color, color.copy(alpha = 0f)), center, radius),
                radius,
                center
            )
        }

        // ================= Плечи и корпус =================
        drawOval(
            Brush.verticalGradient(listOf(metalLight, metalDark)),
            topLeft = pt(28f, 77f),
            size = Size(44f * u, 18f * u)
        )
        drawCircle(metal, 6f * u, pt(29f, 85f))
        drawCircle(metal, 6f * u, pt(71f, 85f))

        drawCircle(Color.White.copy(alpha = 0.35f), 1.6f * u, pt(27f, 83f))
        drawCircle(Color.White.copy(alpha = 0.35f), 1.6f * u, pt(69f, 83f))
        drawOval(Color.White.copy(alpha = 0.15f), topLeft = pt(33f, 78.5f), size = Size(14f * u, 3.5f * u))

        // ================= Сопла + ракетное пламя =================
        for ((idx, sx) in listOf(29f, 71f).withIndex()) {
            drawRoundRect(
                metalDark,
                topLeft = pt(sx - 2.5f, 88.5f),
                size = Size(5f * u, 3f * u),
                cornerRadius = CornerRadius(1f * u)
            )
            drawRoundRect(
                Color(0xFF334457),
                topLeft = pt(sx - 1.6f, 90.6f),
                size = Size(3.2f * u, 1.4f * u),
                cornerRadius = CornerRadius(0.7f * u)
            )
            if (finalBob != 0f) {
                val flick = 0.5f + 0.5f * sin(phase * 4f + idx * 2.1f)
                val len = (5f + 4.5f * flick) * u
                val fw = 4.4f * u
                val top = pt(sx - 2.2f, 91.2f)
                glow(
                    Offset(top.x + fw / 2f, top.y + len * 0.4f),
                    (5f + 2f * flick) * u,
                    Color(0xFF2F80ED).copy(alpha = 0.35f)
                )
                drawOval(
                    Brush.verticalGradient(
                        listOf(
                            cyanSoft,
                            cyan,
                            Color(0xFF2F80ED),
                            Color(0xFF2F80ED).copy(alpha = 0f)
                        )
                    ),
                    topLeft = top,
                    size = Size(fw, len)
                )
                drawOval(
                    Brush.verticalGradient(
                        listOf(Color.White, cyanSoft.copy(alpha = 0f))
                    ),
                    topLeft = Offset(top.x + fw * 0.28f, top.y),
                    size = Size(fw * 0.44f, len * 0.55f)
                )
            }
        }

        // ================= Панель груди =================
        drawRoundRect(
            Color(0xFF334457),
            topLeft = pt(39f, 80f),
            size = Size(22f * u, 11f * u),
            cornerRadius = CornerRadius(3f * u)
        )
        drawRoundRect(
            Color(0xFF22303F),
            topLeft = pt(41f, 82f),
            size = Size(12f * u, 7f * u),
            cornerRadius = CornerRadius(2f * u)
        )
        val coreP = 0.5f + 0.5f * sin(pulse * 1.5f)
        glow(pt(47f, 85.5f), (4f + 1.5f * coreP) * u, cyan.copy(alpha = 0.4f))
        drawCircle(cyan, (1.8f + 0.5f * coreP) * u, pt(47f, 85.5f))
        drawCircle(Color.White.copy(alpha = 0.9f), 0.7f * u, pt(46.4f, 84.9f))
        drawCircle(orange, 1.3f * u, pt(56.5f, 83.5f))
        drawCircle(Color(0xFF69D2A7), 1.3f * u, pt(56.5f, 87.5f))

        // ================= Антенна (укороченная) =================
        drawLine(metalDark, pt(24f, 63f), pt(24f, 34f), strokeWidth = 1.6f * u)
        val orbP = if (finalPulse != 0f) 0.5f + 0.5f * sin(finalPulse * 2f) else 0f
        glow(pt(24f, 35f), (6f + 2f * orbP) * u, orange.copy(alpha = if (finalPulse != 0f) 0.55f else 0f))
        drawCircle(orange, (2.6f + 0.4f * orbP) * u, pt(24f, 35f))
        drawCircle(Color(0xFFFFD9A6), 1f * u, pt(23.2f, 34f))

        // ================= Уши =================
        drawOval(metal, topLeft = pt(19f, 55f), size = Size(9f * u, 16f * u))
        drawOval(metal, topLeft = pt(72f, 55f), size = Size(9f * u, 16f * u))
        drawOval(metalDark, topLeft = pt(21.5f, 58f), size = Size(4.5f * u, 10f * u))
        drawOval(metalDark, topLeft = pt(74f, 58f), size = Size(4.5f * u, 10f * u))

        // ================= Купол =================
        val domeC = pt(50f, 46f)
        val domeR = 26f * u
        val domeTL = Offset(domeC.x - domeR, domeC.y - domeR)
        val domeSize = Size(domeR * 2f, domeR * 2f)
        drawArc(
            Color(0xFFBFE9F2).copy(alpha = 0.40f),
            180f,
            180f,
            true,
            topLeft = domeTL,
            size = domeSize
        )

        // ================= Мозг =================
        val s = 1f + 0.05f * sin(finalPulse * 1.5f)
        fun bp(x: Float, y: Float) = pt(50f + (x - 50f) * s, 39f + (y - 39f) * s)

        glow(bp(50f, 38f), 10f * u, Color(0xFFE36F8C).copy(alpha = if (finalPulse != 0f) 0.30f else 0f))
        listOf(
            Triple(43f, 39f, 4.5f), Triple(57f, 39f, 4.5f),
            Triple(50f, 33f, 4.2f), Triple(50f, 42f, 4.2f),
            Triple(37f, 42f, 3f), Triple(63f, 42f, 3f),
            Triple(46f, 35f, 2.5f), Triple(54f, 35f, 2.5f),
            Triple(40f, 37f, 2.5f), Triple(60f, 37f, 2.5f)
        ).forEach { (x, y, r) -> drawCircle(brainPink, r * s * u, bp(x, y)) }

        listOf(
            42f to 33f, 50f to 30f, 58f to 34f,
            44f to 41f, 56f to 41f, 49f to 37f,
            38f to 39f, 62f to 39f, 46f to 33f,
            54f to 33f, 41f to 36f, 59f to 36f
        ).forEach { (x, y) ->
            drawArc(
                brainDark,
                200f,
                140f,
                false,
                topLeft = bp(x - 3f, y - 3f),
                size = Size(6f * s * u, 6f * s * u),
                style = Stroke(1.1f * u)
            )
        }

        // ================= Орбиты и искры =================
        rotate(-16f, pivot = pt(50f, 38f)) {
            drawOval(
                cyan.copy(alpha = 0.55f),
                topLeft = pt(28f, 30.5f),
                size = Size(44f * u, 15f * u),
                style = Stroke(0.7f * u)
            )
        }
        rotate(12f, pivot = pt(50f, 38f)) {
            drawOval(
                cyan.copy(alpha = 0.45f),
                topLeft = pt(29.5f, 31.5f),
                size = Size(41f * u, 13f * u),
                style = Stroke(0.7f * u)
            )
        }

        fun orbitPos(rx: Float, ry: Float, rotDeg: Float, a: Float): Offset {
            val r = rotDeg * (PI / 180.0).toFloat()
            val cr = cos(r)
            val sr = sin(r)
            val x = cos(a) * rx
            val y = sin(a) * ry
            return pt(50f + x * cr - y * sr, 38f + x * sr + y * cr)
        }

        if (finalPhase != 0f) {
            for (i in 0..5) {
                val a = finalPhase * (1.2f + 0.17f * i) + i * 1.9f
                val p = if (i % 2 == 0) orbitPos(22f, 7.5f, -16f, a)
                else orbitPos(20.5f, 6.5f, 12f, a)
                val tw = 0.5f + 0.5f * sin(finalPulse * 2f + i * 1.3f)
                glow(p, (1.8f + 1.2f * tw) * u, cyan.copy(alpha = 0.25f + 0.45f * tw))
                drawCircle(cyanSoft, 0.9f * u, p)
            }
        }

        // ================= Купол: ободок и блик =================
        drawArc(
            Color(0xFFDFF7FC).copy(alpha = 0.6f),
            180f,
            180f,
            false,
            topLeft = domeTL,
            size = domeSize,
            style = Stroke(1.1f * u)
        )
        drawArc(
            Color.White.copy(alpha = 0.8f),
            195f,
            45f,
            false,
            topLeft = Offset(domeC.x - domeR + 2.5f * u, domeC.y - domeR + 2.5f * u),
            size = Size(domeR * 2f - 5f * u, domeR * 2f - 5f * u),
            style = Stroke(1.6f * u)
        )
        drawOval(metalDark, topLeft = pt(27.5f, 46.2f), size = Size(45f * u, 3.4f * u))

        // ================= Голова =================
        drawOval(
            Brush.verticalGradient(listOf(metalLight, metal, metalDark)),
            topLeft = pt(26f, 47f),
            size = Size(48f * u, 32f * u)
        )

        rotate(-16f, pivot = pt(35f, 53f)) {
            drawOval(
                Color.White.copy(alpha = 0.22f),
                topLeft = pt(29f, 51f),
                size = Size(13f * u, 3.6f * u)
            )
        }
        glow(pt(37f, 67f), 4.5f * u, Color(0xFFE36F8C).copy(alpha = 0.20f))
        glow(pt(63f, 67f), 4.5f * u, Color(0xFFE36F8C).copy(alpha = 0.20f))

        // ================= Глаза + зрачки =================
        for (sx in listOf(-1f, 1f)) {
            val ec = pt(50f + sx * 9.5f, 61f)
            val ry = 7f * u * finalBlink
            glow(ec, 8f * u, cyan.copy(alpha = 0.30f))
            drawOval(
                eyeDark,
                topLeft = Offset(ec.x - 5f * u, ec.y - ry),
                size = Size(10f * u, ry * 2f)
            )
            val iry = 5.8f * u * finalBlink
            drawOval(
                Brush.verticalGradient(listOf(cyanSoft, cyan)),
                topLeft = Offset(ec.x - 3.8f * u, ec.y - iry),
                size = Size(7.6f * u, iry * 2f)
            )
            if (finalBlink > 0.25f) {
                val pr = 2.3f * u
                val pc = Offset(
                    ec.x + finalLookX * 1.6f * u,
                    ec.y + finalLookY * 1.4f * u * finalBlink
                )
                drawOval(
                    eyeDark,
                    topLeft = Offset(pc.x - pr, pc.y - pr * finalBlink),
                    size = Size(pr * 2f, pr * 2f * finalBlink)
                )
                drawCircle(
                    Color.White,
                    0.7f * u,
                    Offset(pc.x - 0.6f * u, pc.y - 0.8f * u * finalBlink)
                )
            }
            if (finalBlink > 0.3f) {
                drawCircle(
                    Color.White.copy(alpha = 0.9f * finalBlink),
                    1.3f * u,
                    Offset(ec.x - 1.6f * u, ec.y - 2.5f * u * finalBlink)
                )
            }
        }

        // ================= Нос =================
        drawRoundRect(
            metalDark,
            topLeft = pt(48.4f, 64.2f),
            size = Size(3.2f * u, 2.2f * u),
            cornerRadius = CornerRadius(1.1f * u)
        )
        drawCircle(Color.White.copy(alpha = 0.35f), 0.6f * u, pt(49.3f, 64.9f))

        // ================= Рот =================
        if (isSpeaking) {
            for (i in 0..4) {
                val h = (3f + 4f * (0.5f + 0.5f * sin(finalPhase * 3f + i * 1.1f))) * u
                val x = 50f + (i - 2) * 2.6f
                drawRoundRect(
                    cyan,
                    topLeft = Offset(offX + (x - 0.7f) * u, offY + 73f * u - h),
                    size = Size(1.4f * u, h),
                    cornerRadius = CornerRadius(0.7f * u)
                )
            }
        } else if (isThinking) {
            for (i in 0..4) {
                val h = (2.5f + 2f * (0.5f + 0.5f * sin(finalPhase * 2f + i * 1.1f))) * u
                val x = 50f + (i - 2) * 2.6f
                drawRoundRect(
                    cyan,
                    topLeft = Offset(offX + (x - 0.7f) * u, offY + 73f * u - h),
                    size = Size(1.4f * u, h),
                    cornerRadius = CornerRadius(0.7f * u)
                )
            }
        } else {
            val smileTL = pt(44.5f, 62.5f)
            val smileSize = Size(11f * u, 11f * u)
            drawArc(
                cyan.copy(alpha = 0.35f),
                startAngle = 20f,
                sweepAngle = 140f,
                useCenter = false,
                topLeft = smileTL,
                size = smileSize,
                style = Stroke(2.6f * u, cap = StrokeCap.Round)
            )
            drawArc(
                cyan,
                startAngle = 20f,
                sweepAngle = 140f,
                useCenter = false,
                topLeft = smileTL,
                size = smileSize,
                style = Stroke(1.5f * u, cap = StrokeCap.Round)
            )
        }
    }
}
@Composable
private fun LockScreen(
    secretPhrase: String,
    onSecretPhraseChange: (String) -> Unit,
    onVerify: () -> Unit,
    viewModel: MainViewModel,
    isPermanentlyBlocked: Boolean
) {
    val context = LocalContext.current

    BackHandler(enabled = true) {
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceGray),
        contentAlignment = Alignment.Center
    ) {
        if (isPermanentlyBlocked) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "🔴 Приложение заблокировано",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.Red,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                Text(
                    text = "Удалите приложение.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = DarkText,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "🤖 Введите секретную фразу",
                    style = MaterialTheme.typography.headlineSmall,
                    color = DarkText,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                OutlinedTextField(
                    value = secretPhrase,
                    onValueChange = onSecretPhraseChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    placeholder = { Text("Введите фразу...", color = DarkText.copy(alpha = 0.5f)) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = DarkText,
                        unfocusedTextColor = DarkText,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = AccentColor,
                        unfocusedBorderColor = BorderGray,
                        cursorColor = AccentColor
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        try {
                            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                val vibratorManager = context.getSystemService(android.content.Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                                vibratorManager.defaultVibrator
                            } else {
                                context.getSystemService(android.content.Context.VIBRATOR_SERVICE) as Vibrator
                            }
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
                            } else {
                                vibrator.vibrate(50)
                            }
                        } catch (e: Exception) {
                        }
                        onVerify()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentColor),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .height(56.dp)
                ) {
                    Text(
                        text = "Подтвердить",
                        color = DarkText,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun TopBarWithSwitch(
    currentMode: AIMode,
    onModeChange: (AIMode) -> Unit,
    isModelLoaded: Boolean,
    cloudConfig: CloudAIConfig?,
    onCloudForceDialog: () -> Unit,
    onLocalForceDialog: () -> Unit,
    statusText: String = "",
    isGenerating: Boolean = false,
    isSpeaking: Boolean = false
) {
    val isLocalReady = isModelLoaded
    val isCloudReady = cloudConfig?.authKey?.isNotEmpty() == true
    val localIndicatorColor = if (isLocalReady) GreenColor else PaleYellowColor
    val cloudIndicatorColor = if (isCloudReady) GreenColor else PaleYellowColor

    Row(
    modifier = Modifier
        .fillMaxWidth()
        .height(84.dp)
        .padding(4.dp)
        .background(Color(0xFFFFF9DB), RoundedCornerShape(8.dp))
        .border(1.dp, BorderGray, RoundedCornerShape(8.dp))
        .padding(horizontal = 8.dp),
    verticalAlignment = Alignment.CenterVertically
) {
      Column(
    modifier = Modifier
        .width(56.dp)
        .fillMaxHeight(),
    verticalArrangement = Arrangement.Center,
    horizontalAlignment = Alignment.CenterHorizontally
) {
    Image(
        painter = painterResource(id = R.mipmap.ic_launcher),
        contentDescription = "Лого",
        modifier = Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(8.dp)),
        contentScale = ContentScale.Crop
    )
    Spacer(modifier = Modifier.height(1.dp))
    Text(
        text = "ИИ-Друг",
        color = AccentColor,
        fontSize = 8.sp,
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.ExtraBold,
        textAlign = TextAlign.Center,
        maxLines = 1
    )
}

        Box(
    modifier = Modifier
        .weight(1f)
        .fillMaxHeight(),
    contentAlignment = Alignment.Center
) {
    ThinkingRobotAnimation(
    height = 70.dp,
    isActive = isGenerating,
    isSpeaking = isSpeaking,
    isThinking = isGenerating,
    isIdle = isLocalReady || isCloudReady,
    modifier = Modifier.fillMaxWidth()
)
}

        Column(
            modifier = Modifier
                .width(132.dp)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatusIndicator(
                    color = localIndicatorColor,
                    text = "локальный ИИ"
                )
                StatusIndicator(
                    color = cloudIndicatorColor,
                    text = "Облачный ИИ"
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ModeButton(
                    label = "Local",
                    isSelected = currentMode == AIMode.LOCAL,
                    onClick = { onLocalForceDialog() },
                    modifier = Modifier.width(42.dp).height(22.dp)
                )
                ModeButton(
                    label = "Neutral",
                    isSelected = currentMode == AIMode.NEUTRAL,
                    onClick = { onModeChange(AIMode.NEUTRAL) },
                    modifier = Modifier.width(42.dp).height(22.dp)
                )
                ModeButton(
                    label = "Cloud",
                    isSelected = currentMode == AIMode.CLOUD,
                    onClick = { onCloudForceDialog() },
                    modifier = Modifier.width(42.dp).height(22.dp)
                )
            }
        }
    }
}
@Composable
private fun StatusIndicator(
    color: Color,
    text: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(color, shape = CircleShape)
                .border(0.5.dp, BorderGray, CircleShape)
        )
        Text(
            text = text,
            fontSize = 6.sp,
            color = DarkText
        )
    }
}

@Composable
private fun ModeButton(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clickable { onClick() }
            .background(
                color = if (isSelected) AccentColor else SurfaceGray,
                shape = RoundedCornerShape(4.dp)
            )
            .border(
                width = if (isSelected) 1.dp else 0.5.dp,
                color = if (isSelected) AccentColor else BorderGray,
                shape = RoundedCornerShape(4.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (isSelected) Color.White else DarkText,
            fontSize = 7.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
private fun ControlPanel(
    onMemoryClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onPromptSettingsClick: () -> Unit,
    onHelpClick: () -> Unit,
    isTtsReady: Boolean,
    viewModel: MainViewModel,
    context: android.content.Context,
    coroutineScope: kotlinx.coroutines.CoroutineScope
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, BorderGray),
        colors = CardDefaults.cardColors(containerColor = SurfaceGray)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButtonWithLabel(
                icon = Icons.Default.Memory,
                label = "мозг",
                onClick = onMemoryClick
            )
            IconButtonWithLabel(
                icon = Icons.Default.Settings,
                label = "движок",
                onClick = onSettingsClick
            )
            IconButtonWithLabel(
                icon = Icons.Default.Psychology,
                label = "характер",
                onClick = onPromptSettingsClick
            )
            IconButtonWithLabel(
                icon = Icons.Default.Info,
                label = "справка",
                onClick = onHelpClick
            )
            IconButtonWithLabel(
                icon = if (isTtsReady) Icons.Default.VolumeUp else Icons.Default.VolumeMute,
                label = if (isTtsReady) "озвучка вкл" else "озвучка выкл",
                onClick = {
                    if (isTtsReady) {
                        viewModel.disableTts()
                    } else {
                        viewModel.enableTts()
                    }
                }
            )
        }
    }
}

@Composable
private fun IconButtonWithLabel(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        IconButton(onClick = onClick) {
            Icon(imageVector = icon, contentDescription = label, tint = AccentColor)
        }
        Text(text = label, color = DarkText, fontSize = 8.sp)
    }
}

@Composable
private fun SettingsPanel(
    temperature: Float,
    onTemperatureChange: (Float) -> Unit,
    maxTokens: Int,
    onMaxTokensChange: (Int) -> Unit,
    contextSize: Int,
    onContextSizeChange: (Int) -> Unit,
    onModelChangeClick: () -> Unit,
    onSave: () -> Unit,
    onClose: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceGray),
        modifier = Modifier.fillMaxWidth().padding(8.dp),
        border = BorderStroke(1.dp, BorderGray)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("🌡️ Настройки движка ИИ", color = DarkText, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Креативность (Температура): ${String.format("%.1f", temperature)}", color = DarkText)
            Slider(
                value = temperature,
                onValueChange = onTemperatureChange,
                valueRange = 0.1f..1.0f,
                steps = 9,
                modifier = Modifier.fillMaxWidth(),
                colors = SliderDefaults.colors(thumbColor = AccentColor, activeTrackColor = AccentColor, inactiveTrackColor = BorderGray)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = "Максимум токенов: $maxTokens", color = DarkText)
            Slider(
                value = maxTokens.toFloat(),
                onValueChange = { onMaxTokensChange(it.toInt()) },
                valueRange = 1f..4096f,
                steps = 50,
                modifier = Modifier.fillMaxWidth(),
                colors = SliderDefaults.colors(thumbColor = AccentColor, activeTrackColor = AccentColor, inactiveTrackColor = BorderGray)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = "Размер контекстного окна: $contextSize", color = DarkText)
            Slider(
                value = contextSize.toFloat(),
                onValueChange = { onContextSizeChange(it.toInt()) },
                valueRange = 512f..8192f,
                steps = 15,
                modifier = Modifier.fillMaxWidth(),
                colors = SliderDefaults.colors(thumbColor = AccentColor, activeTrackColor = AccentColor, inactiveTrackColor = BorderGray)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onModelChangeClick,
                colors = ButtonDefaults.buttonColors(containerColor = BorderGray),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Сменить или перезагрузить модель", color = DarkText)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        onSave()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentColor),
                    modifier = Modifier.weight(1f)
                ) { Text("Сохранить", color = DarkText) }
                Button(
                    onClick = onClose,
                    colors = ButtonDefaults.buttonColors(containerColor = BorderGray),
                    modifier = Modifier.weight(1f)
                ) { Text("Закрыть", color = DarkText) }
            }
        }
    }
}

@Composable
private fun PromptSettingsPanel(promptText: String, onPromptChange: (String) -> Unit, onSave: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceGray),
        modifier = Modifier.fillMaxWidth().padding(8.dp),
        border = BorderStroke(1.dp, BorderGray)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("🧠 Роль ИИ (Системный промпт)", color = DarkText, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = promptText,
                onValueChange = onPromptChange,
                label = { Text("Инструкция для ИИ", color = DarkText) },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3,
                singleLine = false,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = DarkText,
                    unfocusedTextColor = DarkText,
                    focusedContainerColor = AppBackground,
                    unfocusedContainerColor = AppBackground,
                    focusedBorderColor = AccentColor,
                    unfocusedBorderColor = BorderGray,
                    cursorColor = AccentColor
                )
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onSave,
                colors = ButtonDefaults.buttonColors(containerColor = AccentColor),
                modifier = Modifier.align(Alignment.End)
            ) { Text("Сохранить", color = DarkText) }
        }
    }
}

@Composable
private fun CloudAIDialog(
    apiUrl: String,
    authKey: String,
    isGigaChat: Boolean,
    isCloudReady: Boolean,
    onApiUrlChange: (String) -> Unit,
    onAuthKeyChange: (String) -> Unit,
    onIsGigaChatChange: (Boolean) -> Unit,
    onSave: () -> Unit,
    onClear: () -> Unit,
    onDismiss: () -> Unit,
    onGenerateToken: () -> Unit,
    isGeneratingToken: Boolean
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceGray),
            border = BorderStroke(1.dp, BorderGray),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                    Text(
                        text = "☁️",
                        fontSize = 24.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Настройки облачного ИИ",
                        style = MaterialTheme.typography.titleMedium,
                        color = AccentColor,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Text(
                    text = "Введите данные для подключения к облачному ИИ",
                    style = MaterialTheme.typography.bodySmall,
                    color = DarkText
                )

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("🔵 GigaChat", color = DarkText, fontSize = 14.sp)
                    Switch(
                        checked = isGigaChat,
                        onCheckedChange = onIsGigaChatChange,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = AccentColor,
                            checkedTrackColor = AccentColor.copy(alpha = 0.5f),
                            uncheckedThumbColor = BorderGray,
                            uncheckedTrackColor = BorderGray.copy(alpha = 0.5f)
                        )
                    )
                    Text("🌐 Другой провайдер", color = DarkText, fontSize = 14.sp)
                }

                OutlinedTextField(
                    value = apiUrl,
                    onValueChange = onApiUrlChange,
                    label = { Text("API URL", color = DarkText, fontSize = 14.sp) },
                    placeholder = {
                        Text(
                            if (isGigaChat) "https://gigachat.devices.sberbank.ru/api/v1/chat/completions"
                            else "https://openrouter.ai/api/v1/chat/completions",
                            color = DarkText.copy(alpha = 0.5f),
                            fontSize = 12.sp
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = DarkText,
                        unfocusedTextColor = DarkText,
                        focusedBorderColor = AccentColor,
                        unfocusedBorderColor = BorderGray,
                        cursorColor = AccentColor
                    )
                )

                OutlinedTextField(
                    value = authKey,
                    onValueChange = onAuthKeyChange,
                    label = {
                        Text(
                            if (isGigaChat) "Authorization Key (Client Secret)"
                            else "API Key",
                            color = DarkText,
                            fontSize = 14.sp
                        )
                    },
                    placeholder = {
                        Text(
                            if (isGigaChat) "Введите ключ из Сбер Студии"
                            else "Введите ваш API ключ",
                            color = DarkText.copy(alpha = 0.5f),
                            fontSize = 12.sp
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = DarkText,
                        unfocusedTextColor = DarkText,
                        focusedBorderColor = AccentColor,
                        unfocusedBorderColor = BorderGray,
                        cursorColor = AccentColor
                    )
                )

                Button(
                    onClick = onGenerateToken,
                    enabled = !isGeneratingToken && authKey.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentColor,
                        contentColor = DarkText,
                        disabledContainerColor = BorderGray,
                        disabledContentColor = DarkText.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, BorderGray),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isGeneratingToken) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = DarkText, strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Получение токена...", color = DarkText, fontSize = 14.sp)
                    } else {
                        Text(
                            text = if (isCloudReady) "✅ Токен подключен" else if (isGigaChat) "🔑 Получить токен" else "🔑 Установить ключ",
                            color = DarkText,
                            fontSize = 14.sp
                        )
                    }
                }

                if (!isGigaChat) {
                    Text(
                        text = "ℹ️ Для обычных провайдеров ключ используется как токен",
                        color = DarkText.copy(alpha = 0.6f),
                        fontSize = 12.sp
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(
                                color = if (isCloudReady) GreenColor else Color.Red,
                                shape = CircleShape
                            )
                            .border(
                                width = 1.dp,
                                color = if (isCloudReady) GreenColor else Color.Red,
                                shape = CircleShape
                            )
                    )

                    Button(
                        onClick = onSave,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AccentColor,
                            contentColor = DarkText
                        ),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, BorderGray),
                        modifier = Modifier.weight(1f).height(36.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                    ) {
                        Text(
                            text = "Сохранить",
                            color = DarkText,
                            fontSize = 12.sp
                        )
                    }

                    Button(
                        onClick = onClear,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AccentColor,
                            contentColor = DarkText
                        ),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, BorderGray),
                        modifier = Modifier.weight(1f).height(36.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                    ) {
                        Text(
                            text = "Очистить",
                            color = DarkText,
                            fontSize = 12.sp
                        )
                    }

                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AccentColor,
                            contentColor = DarkText
                        ),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, BorderGray),
                        modifier = Modifier.weight(1f).height(36.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                    ) {
                        Text(
                            text = "Закрыть",
                            color = DarkText,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HelpDialog(
    onDismiss: () -> Unit,
    viewModel: MainViewModel
) {
    AlertDialog(
        onDismissRequest = {
            viewModel.abortLocal()
            onDismiss()
        },
        title = { Text("🛡️ Руководство пользователя", style = MaterialTheme.typography.titleLarge, color = DarkText) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(500.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(8.dp)
            ) {
                Text(
                    text = HelpText.fullHelp,
                    color = DarkText,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    lineHeight = 16.sp
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    viewModel.abortLocal()
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = AccentColor)
            ) {
                Text("Понятно", color = DarkText)
            }
        }
    )
}

@Composable
private fun MemoryEditorDialog(initialText: String, onSave: (String) -> Unit, onDismiss: () -> Unit) {
    var text by remember { mutableStateOf(initialText) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("🧠 База Знаний ИИ", style = MaterialTheme.typography.titleLarge, color = DarkText) },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                placeholder = { Text("Вставь сюда свой прайс-лист или данные...", color = DarkText.copy(alpha = 0.5f), fontSize = 10.sp) },
                modifier = Modifier.fillMaxWidth().height(400.dp),
                maxLines = 100,
                singleLine = false,
                textStyle = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp, color = DarkText),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = DarkText,
                    unfocusedTextColor = DarkText,
                    focusedContainerColor = SurfaceGray,
                    unfocusedContainerColor = SurfaceGray,
                    focusedBorderColor = AccentColor,
                    unfocusedBorderColor = BorderGray,
                    cursorColor = AccentColor
                )
            )
        },
        confirmButton = {
            Button(onClick = { onSave(text); onDismiss() }, colors = ButtonDefaults.buttonColors(containerColor = AccentColor)) {
                Text("Сохранить", color = DarkText)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Закрыть", color = DarkText) }
        }
    )
}
@Composable
private fun ImagePreview(imagePath: String) {
    Card(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceGray)
    ) {
        Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("[Изображение]", style = MaterialTheme.typography.bodySmall, color = DarkText)
        }
    }
}

@Composable
private fun StatusBar(
    state: GenerationState,
    cloudState: CloudAIState,
    currentMode: AIMode,
    currentModel: String?,
    modifier: Modifier = Modifier
) {
    val (containerColor, statusText, showProgress) = when (currentMode) {
        AIMode.CLOUD -> {
            when (cloudState) {
                is CloudAIState.Idle -> Triple(
                    SurfaceGray,
                    "☁️ Облако: Готово к работе",
                    false
                )
                is CloudAIState.Ready -> Triple(
                    SurfaceGray,
                    "☁️ Облако: Готов (${cloudState.modelId})",
                    false
                )
                is CloudAIState.Generating -> Triple(
                    AccentColor.copy(alpha = 0.15f),
                    if (cloudState.tokensGenerated == 0) "☁️ Облако: Думает..." else "☁️ Облако: ${cloudState.tokensGenerated} т.",
                    true
                )
                is CloudAIState.Completed -> Triple(
                    AccentColor.copy(alpha = 0.15f),
                    "☁️ Облако: ${cloudState.tokenCount} т. ${cloudState.durationMs}мс",
                    false
                )
                is CloudAIState.Error -> Triple(
                    AccentColor.copy(alpha = 0.15f),
                    "⚠️ Облако: ${cloudState.message}",
                    false
                )
            }
        }
        else -> {
            when (state) {
                is GenerationState.Idle -> Triple(
                    SurfaceGray,
                    if (currentModel == null) "🤖 Локальный ИИ: выгружен из памяти" else "🤖 Локальный ИИ: Готов к работе",
                    false
                )
                is GenerationState.LoadingModel -> Triple(
                    BorderGray.copy(alpha = 0.3f),
                    "⏳ Загрузка модели...",
                    true
                )
                is GenerationState.ModelLoaded -> Triple(
                    SurfaceGray,
                    run {
                        val modelName = (currentModel?.substringAfterLast("/") ?: "нейросеть")
                            .replace("primary%3AModels%", "")
                        if (currentModel == null) "🤖 Локальный ИИ: выгружен из памяти" else "🤖 Модель $modelName успешно загружена"
                    },
                    false
                )
                is GenerationState.AnalyzingImage -> Triple(
                    AccentColor.copy(alpha = 0.15f),
                    "🧐 Анализ...",
                    true
                )
                is GenerationState.Generating -> Triple(
                    AccentColor.copy(alpha = 0.15f),
                    if (state.tokensGenerated == 0) "🤖 Локальный ИИ: Думает..." else "🤖 Локальный ИИ: ${state.tokensGenerated} т.",
                    true
                )
                is GenerationState.Completed -> Triple(
                    AccentColor.copy(alpha = 0.15f),
                    "✅ ${state.tokenCount} т. ${state.durationMs}мс",
                    false
                )
                is GenerationState.Error -> Triple(
                    AccentColor.copy(alpha = 0.15f),
                    "⚠️ Ошибка: ${state.message}",
                    false
                )
            }
        }
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(1.dp, BorderGray)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            if (showProgress) {
                CircularProgressIndicator(
                    modifier = Modifier.size(12.dp),
                    color = AccentColor,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = statusText,
                color = DarkText,
                fontSize = 8.sp
            )
        }
    }
}

@Composable
private fun ModelPickerDialog(
    currentModelPath: String?,
    mmprojPath: String?,
    onPickModel: () -> Unit,
    onPickMmproj: () -> Unit,
    onLoad: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, BorderGray, RoundedCornerShape(16.dp))
        ) {
            // Фон — матрица
            AndroidView(
                factory = { matrixContext ->
                    MatrixChatBackground(matrixContext)
                },
                modifier = Modifier.matchParentSize()
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceGray.copy(alpha = 0.3f))
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                    Text(
                        text = "🤖",
                        fontSize = 24.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Настройка ИИ",
                        style = MaterialTheme.typography.titleMedium,
                        color = AccentColor,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Языковая модель", color = DarkText, fontSize = 14.sp)
                    val displayModelPath = currentModelPath?.substringAfterLast("/")?.replace("primary%3AModels%", "") ?: "Не выбрана"
                    Text(
                        text = "Текущая модель: $displayModelPath",
                        style = MaterialTheme.typography.bodySmall,
                        color = DarkText.copy(alpha = 0.7f),
                        fontFamily = ChatFontFamily
                    )
                    Button(
                        onClick = onPickModel,
                        modifier = Modifier.fillMaxWidth(0.7f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AccentColor,
                            contentColor = DarkText
                        ),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, BorderGray)
                    ) {
                        Text(
                            text = if (currentModelPath != null) "Изменить модель" else "Выбрать модель",
                            color = DarkText,
                            fontSize = 13.sp
                        )
                    }
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Мультимодальный проектор", color = DarkText, fontSize = 14.sp)
                    val displayMmprojPath = mmprojPath?.substringAfterLast("/")?.replace("primary%3AModels%", "") ?: "Не выбран"
                    Text(
                        text = "Текущий проектор: $displayMmprojPath",
                        style = MaterialTheme.typography.bodySmall,
                        color = DarkText.copy(alpha = 0.7f),
                        fontFamily = ChatFontFamily
                    )
                    Button(
                        onClick = onPickMmproj,
                        modifier = Modifier.fillMaxWidth(0.7f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AccentColor,
                            contentColor = DarkText
                        ),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, BorderGray)
                    ) {
                        Text(
                            text = if (mmprojPath != null) "Изменить проектор" else "Выбрать проектор",
                            color = DarkText,
                            fontSize = 13.sp
                        )
                    }
                }

                Button(
                    onClick = onLoad,
                    enabled = currentModelPath != null,
                    modifier = Modifier.fillMaxWidth(0.7f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentColor,
                        contentColor = DarkText,
                        disabledContainerColor = BorderGray,
                        disabledContentColor = DarkText.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, BorderGray)
                ) {
                    Text("Запустить нейросеть", color = DarkText, fontSize = 13.sp)
                }

                Button(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://huggingface.co/AnkitAI/Parable-Granite-4.1-3B-Claude-Fable-5-GGUF/resolve/main/Parable-Granite-4.1-3B-Claude-Fable-5-GGUF-Q6_K.gguf"))
                        context.startActivity(intent)
                    },
                    modifier = Modifier.fillMaxWidth(0.7f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentColor,
                        contentColor = DarkText
                    ),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, BorderGray)
                ) {
                    Text("⬇ Скачать модель", color = DarkText, fontSize = 13.sp)
                }

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(0.7f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentColor,
                        contentColor = DarkText
                    ),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, BorderGray)
                ) {
                    Text("Отмена", color = DarkText, fontSize = 13.sp)
                }
            }
        }
    }
}
@Composable
private fun PromptInput(
    prompt: String,
    onPromptChange: (String) -> Unit,
    onGenerate: () -> Unit,
    onAbort: () -> Unit,
    onClearChat: () -> Unit,
    onPickImage: () -> Unit,
    enabled: Boolean,
    isGenerating: Boolean,
    isSpeaking: Boolean,
    focusRequester: FocusRequester,
    isTtsReady: Boolean,
    viewModel: MainViewModel,
    context: android.content.Context,
    speechRecognizerLauncher: androidx.activity.result.ActivityResultLauncher<Intent>,
    isBound: Boolean,
    modelName: String,
    remainingTimeText: String,
    isPermanentlyUnlocked: Boolean,
    currentMode: AIMode,
    modifier: Modifier = Modifier
) {
    val memoryInfoText by viewModel.memoryInfoText.collectAsStateWithLifecycle(initialValue = "Загрузка памяти...")

    var printedText by remember { mutableStateOf("") }
    var offsetX by remember { mutableStateOf(0f) }
    var currentPhraseIndex by remember { mutableStateOf(0) }

    val phrases = buildList {
        if (!isBound) {
            add("🔴 Приложение заблокировано")
        } else if (isPermanentlyUnlocked) {
            add("✅ Приложение разблокировано")
        } else {
            if (modelName.isNotEmpty()) {
                add("✅ Приложение привязано • Модель: $modelName")
            }
            if (remainingTimeText.isNotEmpty()) {
                add("✅ Приложение привязано • $remainingTimeText")
            }
            if (modelName.isEmpty() && remainingTimeText.isEmpty()) {
                add("✅ Приложение привязано к данному устройству")
            }
        }
    }

    LaunchedEffect(isBound, modelName, remainingTimeText, isPermanentlyUnlocked) {
        while (true) {
            if (phrases.isEmpty()) {
                delay(120000)
                continue
            }

            val currentPhrase = phrases[currentPhraseIndex % phrases.size]
            currentPhraseIndex = (currentPhraseIndex + 1) % phrases.size

            printedText = ""
            for (i in currentPhrase.indices) {
                printedText += currentPhrase[i]
                delay(35)
            }

            delay(2000)

            val textWidth = printedText.length * 8f
            for (step in 0..textWidth.toInt() step 4) {
                offsetX = -step.toFloat()
                delay(16)
            }

            offsetX = 0f
            printedText = ""

            delay(120000)
        }
    }

    val waveColor = if (currentMode == AIMode.CLOUD) Color(0xFF00B4D8) else GreenColor

    val textColor = when {
        !isBound -> Color.Red
        remainingTimeText.contains("🔴") -> Color.Red
        remainingTimeText.contains("⏳") -> Color(0xFFFFA500)
        else -> GreenColor
    }

    Card(
        modifier = modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, BorderGray),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF9DB))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(20.dp),
                contentAlignment = Alignment.Center
            ) {
                if (isSpeaking) {
                    VoiceWaveAnimation(
                        color = waveColor,
                        modifier = Modifier
                            .fillMaxWidth(0.6f)
                            .fillMaxHeight()
                    )
                } else {
                    Text(
                        text = when {
                            !isBound -> "🔴 Приложение заблокировано"
                            printedText.isNotEmpty() -> printedText
                            modelName.isNotEmpty() -> "✅ Модель: $modelName"
                            else -> "✅ Приложение привязано"
                        },
                        color = textColor,
                        fontSize = 8.sp,
                        fontFamily = ChatFontFamily,
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .offset(x = offsetX.dp),
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(41.dp)
                            .background(Color(0xFFF1F3F5), shape = CircleShape)
                            .border(1.dp, BorderGray, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        IconButton(
                            onClick = onPickImage,
                            enabled = enabled && !isGenerating && !isSpeaking,
                            modifier = Modifier.size(41.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Добавить изображение",
                                tint = if (enabled && !isGenerating && !isSpeaking) AccentColor else DarkText.copy(alpha = 0.4f)
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(41.dp)
                            .background(Color(0xFFF1F3F5), shape = CircleShape)
                            .border(1.dp, BorderGray, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        IconButton(
                            onClick = onClearChat,
                            enabled = true,
                            modifier = Modifier.size(41.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Очистить чат",
                                tint = AccentColor
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = prompt,
                    onValueChange = onPromptChange,
                    modifier = Modifier
                        .weight(1f)
                        .focusRequester(focusRequester),
                    enabled = enabled && !isGenerating && !isSpeaking,
                    placeholder = { Text("Введите запрос...", color = DarkText.copy(alpha = 0.5f)) },
                    maxLines = 3,
                    singleLine = false,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = DarkText,
                        unfocusedTextColor = DarkText,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        cursorColor = AccentColor
                    )
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(41.dp)
                            .background(Color(0xFFF1F3F5), shape = CircleShape)
                            .border(1.dp, BorderGray, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        IconButton(
                            onClick = {
                                if (ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.RECORD_AUDIO
                                    ) != PackageManager.PERMISSION_GRANTED
                                ) {
                                    viewModel.appendSystemMessage("⚠️ Нет разрешения на запись аудио")
                                    return@IconButton
                                }
                                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ru-RU")
                                    putExtra(RecognizerIntent.EXTRA_PROMPT, "Говорите...")
                                }
                                speechRecognizerLauncher.launch(intent)
                            },
                            enabled = true,
                            modifier = Modifier.size(41.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = "Распознать речь",
                                tint = if (isTtsReady) AccentColor else DarkText.copy(alpha = 0.4f)
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(41.dp)
                            .background(Color(0xFFF1F3F5), shape = CircleShape)
                            .border(1.dp, BorderGray, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isGenerating || isSpeaking) {
                            IconButton(
                                onClick = onAbort,
                                modifier = Modifier.size(41.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Стоп",
                                    tint = AccentColor
                                )
                            }
                        } else {
                            IconButton(
                                onClick = onGenerate,
                                enabled = enabled,
                                modifier = Modifier.size(41.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ArrowUpward,
                                    contentDescription = "Отправить",
                                    tint = if (enabled) AccentColor else DarkText.copy(alpha = 0.4f)
                                )
                            }
                        }
                    }
                }
            }

                                  Spacer(modifier = Modifier.height(2.dp))

                        val memoryColor = when {
                            memoryInfoText.contains("Занято") && memoryInfoText.contains("ГБ") -> {
                                val usedGb = Regex("Занято ([\\d.]+)").find(memoryInfoText)?.groupValues?.get(1)?.toFloatOrNull() ?: 0f
                                val totalGb = Regex("Всего доступно ([\\d.]+)").find(memoryInfoText)?.groupValues?.get(1)?.toFloatOrNull() ?: 1f
                                if (totalGb > 0f && (usedGb / totalGb) > 0.85f) Color.Red else GreenColor
                            }
                            else -> GreenColor
                        }

                        Text(
                            text = memoryInfoText,
                            color = memoryColor,
                            fontSize = 8.sp,
                            fontFamily = ChatFontFamily,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 4.dp, end = 4.dp, bottom = 2.dp),
                            textAlign = TextAlign.Center
                        )
        }
    }
}
