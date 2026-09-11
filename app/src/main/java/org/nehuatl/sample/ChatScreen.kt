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
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
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
import kotlin.math.atan2
import kotlin.math.min
import kotlin.math.sqrt
import androidx.compose.animation.core.keyframes
import java.util.Random
import androidx.compose.runtime.rememberUpdatedState
private data class AppColors(
    val background: Color,
    val surfaceGray: Color,
    val borderGray: Color,
    val accent: Color,
    val text: Color,
    val chatFont: FontFamily,
    val green: Color,
    val paleYellow: Color
)

private val LightColors = AppColors(
    background = Color(0xFFFFFFFF),
    surfaceGray = Color(0xFFF1F3F5),
    borderGray = Color(0xFFCED4DA),
    accent = Color(0xFF74C0FC),
    text = Color(0xFF212529),
    chatFont = FontFamily.Monospace,
    green = Color(0xFF2E7D32),
    paleYellow = Color(0xFFFFF9DB)
)

private val DarkColors = AppColors(
    background = Color(0xFF121212),
    surfaceGray = Color(0xFF1E1E1E),
    borderGray = Color(0xFF3A3A3A),
    accent = Color(0xFF90CAF9),
    text = Color(0xFFE0E0E0),
    chatFont = FontFamily.Monospace,
    green = Color(0xFF81C784),
    paleYellow = Color(0xFF2A2A1E)
)

enum class AIMode {
    LOCAL,
    NEUTRAL,
    CLOUD
}
private object SpaceConstants {
    const val ORBIT_CENTER_X_RATIO = 0.50f
    const val ORBIT_CENTER_Y_RATIO = 0.50f
    const val ROBOT_ORBIT_RX = 0.14f
    const val ROBOT_ORBIT_RY = 0.10f
    const val MOON_ORBIT_RX = 0.27f
    const val MOON_ORBIT_RY = 0.22f
    const val EARTH_ORBIT_RX = 0.37f
    const val EARTH_ORBIT_RY = 0.33f
    const val SATURN_ORBIT_RX = 0.46f
    const val SATURN_ORBIT_RY = 0.42f
    const val ROBOT_SIZE_RATIO = 0.050f
    const val PLANET_SIZE_RATIO = 0.055f
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
    val isDarkTheme by viewModel.isDarkTheme.collectAsStateWithLifecycle(initialValue = false)
    val showBrainEditorState by viewModel.showBrainEditor.collectAsStateWithLifecycle(initialValue = false)

    val colors = if (isDarkTheme) DarkColors else LightColors

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

    // Состояния для робота после приземления
    var robotIsLanded by remember { mutableStateOf(false) }
    var robotScale by remember { mutableStateOf(1f) }
    var robotIsFlyingHome by remember { mutableStateOf(false) }
    var robotIsFlyingHere by remember { mutableStateOf(false) }
    var robotOnOrbit by remember { mutableStateOf(true) }
    var robotOrbitAngle by remember { mutableStateOf(0f) }
    var robotOffsetX by remember { mutableStateOf(0f) }
    var robotOffsetY by remember { mutableStateOf(0f) }

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
                    val command = recognizedText.trim().lowercase()
                    when {
                                                command == "лети домой" -> {
                            robotIsFlyingHome = true
                            robotIsFlyingHere = false
                            robotIsLanded = false
                            robotOnOrbit = false
                            viewModel.appendSystemMessage("🤖 Робот улетает на орбиту")
                        }
                        command == "лети сюда" -> {
                            robotIsFlyingHere = true
                            robotIsFlyingHome = false
                            robotOnOrbit = false
                            viewModel.appendSystemMessage("🤖 Робот прилетает с орбиты")
                        }
                        else -> {
                            viewModel.sendUserMessage(recognizedText)
                        }
                    }
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

    val isFirstLaunch by viewModel.isFirstLaunch.collectAsStateWithLifecycle(initialValue = false)

    LaunchedEffect(isTtsReady) {
        if (isTtsReady && !welcomeStarted) {
            welcomeStarted = true
            val greeting = if (isFirstLaunch) {
                fullWelcomeString
            } else {
                "Привет друг. Чем сегодня займемся?"
            }
            viewModel.speakText(greeting)
        }
    }

    LaunchedEffect(Unit) {
    delay(1500)
    if (!robotIsLanded && !robotIsFlyingHome) {
        robotIsFlyingHere = true
        robotOnOrbit = false
    }
}
            LaunchedEffect(robotOnOrbit) {
        if (robotOnOrbit) {
            while (true) {
                robotOrbitAngle += 0.01f
                delay(16)
            }
        }
    }

    LaunchedEffect(speakStartTrigger) {
        if (speakStartTrigger && welcomeStarted && !welcomeTextPrinted) {
            welcomeTextPrinted = true
            val greeting = if (isFirstLaunch) {
                fullWelcomeString
            } else {
                "Привет друг. Чем сегодня займемся ?"
            }
            var runningText = ""
            for (i in greeting.indices) {
                runningText += greeting[i]
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
            isPermanentlyBlocked = isPermanentlyBlocked,
            colors = colors
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
            onDismiss = { showModelDialog = false },
            colors = colors,
            isDarkTheme = isDarkTheme
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
            isGeneratingToken = isGeneratingToken,
            colors = colors
        )
    }

    if (showHelpDialog) {
        MaterialTheme(
            colorScheme = if (isDarkTheme) darkColorScheme() else lightColorScheme()
        ) {
            HelpDialog(
                onDismiss = { showHelpDialog = false },
                viewModel = viewModel,
                colors = colors
            )
        }
    }

    if (showMemoryEditor) {
        MaterialTheme(
            colorScheme = if (isDarkTheme) darkColorScheme() else lightColorScheme()
        ) {
            MemoryEditorDialog(
                initialText = viewModel.readFromLongTermMemory(),
                onSave = { viewModel.overwriteLongTermMemory(it) },
                onDismiss = { showMemoryEditor = false },
                colors = colors
            )
        }
    }

    if (showBrainEditorState) {
        BrainEditorDialog(
            initialText = viewModel.readBrain(),
            onSave = { viewModel.overwriteBrain(it) },
            onDismiss = { viewModel.hideBrainEditor() },
            colors = colors,
            isDarkTheme = isDarkTheme
        )
    }

    val density = LocalDensity.current

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
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
                isSpeaking = isSpeaking,
                isDarkTheme = isDarkTheme,
                onToggleTheme = { viewModel.toggleTheme() },
                colors = colors,
                isTtsReady = isTtsReady
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
                coroutineScope = coroutineScope,
                colors = colors,
                isDarkTheme = isDarkTheme
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
                    },
                    colors = colors
                )
            }

            if (showPromptSettings) {
                PromptSettingsPanel(
                    promptText = tempPromptText,
                    onPromptChange = { tempPromptText = it },
                    onSave = {
                        viewModel.updateSystemPrompt(tempPromptText)
                        showPromptSettings = false
                    },
                    colors = colors
                )
            }

            StatusBar(
                state = state,
                cloudState = cloudState,
                currentMode = currentMode,
                currentModel = if (isModelLoaded) currentModelPath else null,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                colors = colors
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
                    modifier = Modifier
                        .matchParentSize()
                        .clip(RoundedCornerShape(16.dp))
                )

                Card(
                    modifier = Modifier.fillMaxSize(),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, colors.borderGray),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDarkTheme) Color(0xFF1E1E1E).copy(alpha = 0.2f) else Color.Transparent
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
                                    "user" -> colors.green
                                    "assistant" -> colors.text
                                    else -> colors.text
                                }
                                Text(
                                    text = prefix + message.text,
                                    color = textColor,
                                    fontFamily = colors.chatFont,
                                    fontSize = 10.sp,
                                    modifier = Modifier.padding(vertical = 2.dp)
                                )
                            }

                            if (generatedText.isNotEmpty() && state is GenerationState.Generating) {
                                Text(
                                    text = "ИИ: $generatedText",
                                    color = colors.text,
                                    fontFamily = colors.chatFont,
                                    fontSize = 10.sp,
                                    modifier = Modifier.padding(vertical = 2.dp)
                                )
                            }

                            if (cloudGeneratedText.isNotEmpty() && cloudState is CloudAIState.Generating) {
                                Text(
                                    text = "☁️ ИИ: $cloudGeneratedText",
                                    color = colors.text.copy(alpha = 0.8f),
                                    fontFamily = colors.chatFont,
                                    fontSize = 10.sp,
                                    modifier = Modifier.padding(vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }

            if (imagePath != null) {
                ImagePreview(imagePath = imagePath, colors = colors)
            }

            PromptInput(
                prompt = promptInput,
                onPromptChange = { promptInput = it },
                onGenerate = {
                    keyboardController?.hide()
                    val command = promptInput.trim().lowercase()
                    when {
                            command == "лети домой" -> {
                            robotIsFlyingHome = true
                            robotIsFlyingHere = false
                            robotIsLanded = false
                            robotOnOrbit = false
                            viewModel.appendSystemMessage("🤖 Робот улетает на орбиту")
                            promptInput = ""
                        }
                        command == "лети сюда" -> {
                            robotIsFlyingHere = true
                            robotIsFlyingHome = false
                            robotOnOrbit = false
                            viewModel.appendSystemMessage("🤖 Робот прилетает с орбиты")
                            promptInput = ""
                        }
                        else -> {
                            viewModel.sendUserMessage(promptInput)
                            promptInput = ""
                            onImageUsed()
                        }
                    }
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
                modifier = Modifier.padding(8.dp),
                colors = colors,
                isDarkTheme = isDarkTheme
            )
        }
                val flightProgress by animateFloatAsState(
            targetValue = if (robotIsFlyingHere || robotIsFlyingHome) 1f else 0f,
            animationSpec = tween(durationMillis = 3000, easing = FastOutSlowInEasing),
            label = "flight_progress"
        )

        LaunchedEffect(flightProgress) {
            if (flightProgress >= 0.99f) {
                if (robotIsFlyingHere) {
                    robotIsFlyingHere = false
                    robotIsLanded = true
                    robotOnOrbit = false
                } else if (robotIsFlyingHome) {
                    robotIsFlyingHome = false
                    robotIsLanded = false
                    robotOnOrbit = true
                }
            }
        }

        // ===== ЕДИНЫЙ РОБОТ =====
        // Робот на орбите
        if (robotOnOrbit && !robotIsFlyingHere && !robotIsFlyingHome) {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(84.dp)
                    .padding(4.dp)
            ) {
                val w = constraints.maxWidth.toFloat()
                val h = constraints.maxHeight.toFloat()
                val cx = w * 0.5f
                val cy = h * 0.5f
                val rRx = w * 0.14f
                val rRy = h * 0.10f
                val orbitAngle = robotOrbitAngle
                val robotX = cx + cos(orbitAngle) * rRx
                val robotY = cy + sin(orbitAngle) * rRy
                val robotSize = h * 0.16f

                val density = LocalDensity.current
                val robotSizeDp = with(density) { robotSize.toDp() }
                val offsetXDp = with(density) { (robotX - robotSize / 2f).toDp() }
                val offsetYDp = with(density) { (robotY - robotSize / 2f).toDp() }

                Box(
                    modifier = Modifier
                        .offset(x = offsetXDp, y = offsetYDp)
                        .size(robotSizeDp)
                        .graphicsLayer(
                            rotationZ = (orbitAngle * 180f / PI.toFloat()) + 90f
                        )
                ) {
                    ThinkingRobotAnimation(
                        height = robotSizeDp,
                        isActive = false,
                        isSpeaking = false,
                        isThinking = false,
                        isIdle = true,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }

        // Робот в полёте
        if (robotIsFlyingHere || robotIsFlyingHome) {
            BoxWithConstraints(
                modifier = Modifier.fillMaxSize()
            ) {
                val w = constraints.maxWidth.toFloat()
                val h = constraints.maxHeight.toFloat()
                val topBarHeight = with(LocalDensity.current) { 84.dp.toPx() }
                val topBarWidth = w
                val orbitCenterX = topBarWidth * 0.5f
                val orbitCenterY = topBarHeight * 0.5f
                val orbitRx = topBarWidth * 0.14f
                val orbitRy = topBarHeight * 0.10f
                val orbitSize = topBarHeight * 0.16f

                val density = LocalDensity.current
                val robotSizeOnScreen = with(density) { (70.dp * robotScale).toPx() }

                val startX: Float
                val startY: Float
                val startSize: Float
                val endX: Float
                val endY: Float
                val endSize: Float

                val currentRobotCenterX = robotOffsetX + with(density) { (70.dp * robotScale).toPx() } / 2f
                val currentRobotCenterY = robotOffsetY + with(density) { (70.dp * robotScale).toPx() } / 2f

                if (robotIsFlyingHere) {
                    // С орбиты на экран
                    startX = orbitCenterX + cos(robotOrbitAngle) * orbitRx
                    startY = orbitCenterY + sin(robotOrbitAngle) * orbitRy
                    startSize = orbitSize
                    endX = currentRobotCenterX
                    endY = currentRobotCenterY
                    endSize = robotSizeOnScreen
                } else {
                    // С экрана на орбиту
                    startX = currentRobotCenterX
                    startY = currentRobotCenterY
                    startSize = robotSizeOnScreen
                    endX = orbitCenterX + cos(robotOrbitAngle) * orbitRx
                    endY = orbitCenterY + sin(robotOrbitAngle) * orbitRy
                    endSize = orbitSize
                }

                val t = flightProgress
                val oneMinusT = 1f - t
                val ctrlX = (startX + endX) / 2f
                val ctrlY = min(startY, endY) - 100f

                val currentX = oneMinusT * oneMinusT * startX +
                        2f * oneMinusT * t * ctrlX +
                        t * t * endX
                val currentY = oneMinusT * oneMinusT * startY +
                        2f * oneMinusT * t * ctrlY +
                        t * t * endY
                val currentSize = startSize + (endSize - startSize) * t

                val offsetXDp = with(density) { (currentX - currentSize / 2f).toDp() }
                val offsetYDp = with(density) { (currentY - currentSize / 2f).toDp() }
                val currentSizeDp = with(density) { currentSize.toDp() }

                Box(
                    modifier = Modifier
                        .offset(x = offsetXDp, y = offsetYDp)
                        .size(currentSizeDp)
                ) {
                    ThinkingRobotAnimation(
                        height = currentSizeDp,
                        isActive = false,
                        isSpeaking = false,
                        isThinking = false,
                        isIdle = true,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }

        // Робот на экране
        if (robotIsLanded && !robotOnOrbit && !robotIsFlyingHome) {
            BoxWithConstraints(
                modifier = Modifier.fillMaxSize()
            ) {
                val screenWidthPx = constraints.maxWidth.toFloat()
                val screenHeightPx = constraints.maxHeight.toFloat()
                val robotSizePx = with(LocalDensity.current) { 70.dp.toPx() }

                Box(
                    modifier = Modifier
                        .offset(x = robotOffsetX.dp, y = robotOffsetY.dp)
                        .size(70.dp)
                        .graphicsLayer(
                            scaleX = robotScale,
                            scaleY = robotScale
                        )
                        .pointerInput(Unit) {
    detectTransformGestures { _, pan, zoom, _ ->
        robotOffsetX = (robotOffsetX + pan.x).coerceIn(0f, screenWidthPx - robotSizePx * robotScale)
        robotOffsetY = (robotOffsetY + pan.y).coerceIn(0f, screenHeightPx - robotSizePx * robotScale)
        robotScale = (robotScale * zoom).coerceIn(0.5f, 3f)

                            }
                        }
                ) {
                    ThinkingRobotAnimation(
                        height = 70.dp,
                        isActive = false,
                        isSpeaking = isSpeaking,
                        isThinking = state is GenerationState.Generating || cloudState is CloudAIState.Generating,
                        isIdle = !isSpeaking && state !is GenerationState.Generating && cloudState !is CloudAIState.Generating,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
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
    val transition = rememberInfiniteTransition(label = "robot")

    val bob by transition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = SineClientEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bob"
    )

    val blink by transition.animateFloat(
        initialValue = 1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 4500
                1f at 0
                1f at 4000
                0.05f at 4180
                1f at 4350
                1f at 4500
            }
        ),
        label = "blink"
    )

    val mouthPhase by transition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(380, easing = LinearEasing)
        ),
        label = "mouthPhase"
    )

    val lookX by transition.animateFloat(
        initialValue = 0f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 7000
                0f at 0
                0f at 900
                1f at 1600
                1f at 2600
                -1f at 3300
                -1f at 4400
                0.5f at 5100
                0.5f at 5800
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
                0f at 1500
                -0.6f at 2200
                -0.6f at 3000
                0.4f at 3800
                0.4f at 4600
                0f at 5400
                0f at 7000
            }
        ),
        label = "lookY"
    )

    val flamePhase by transition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(300, easing = LinearEasing)
        ),
        label = "flamePhase"
    )

    val pulse by transition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse"
    )

    Canvas(
        modifier = modifier
            .height(height)
            .width(height * 0.85f)
    ) {
        val u = size.height / 200f
        val cx = size.width / 2f
        val bobOffset = if (isActive) sin(bob) * 1.5f * u else 0f
        val lookOffsetX = lookX * 1f * u
        val lookOffsetY = lookY * 0.8f * u
        val currentBlink = if (isActive) blink else 1f

        // Цвета
        val whiteBody = Color(0xFFF8F9FA)
        val lightGray = Color(0xFFE9ECEF)
        val mediumGray = Color(0xFFADB5BD)
        val darkGray = Color(0xFF495057)
        val darkerGray = Color(0xFF343A40)
        val screenBlack = Color(0xFF1A1A2E)
        val neonBlue = Color(0xFF00D9FF)
        val neonBlueGlow = Color(0xFF80EFFF)
        
        val neonBluePulse = if (isThinking) {
            neonBlue.copy(alpha = 0.7f + sin(pulse) * 0.3f)
        } else {
            neonBlue
        }

        fun pt(x: Float, y: Float) = Offset(cx + x * u, y * u + bobOffset)

        // ================= ПЛАМЯ РАКЕТНОГО ДВИГАТЕЛЯ (с градиентом) =================
        val flameFlicker = sin(flamePhase * 2f) * 1.5f
        val flameHeight = 35f + flameFlicker
        
        // Внешнее пламя (градиент оранжево-красный)
        val outerFlamePath = Path().apply {
            moveTo(pt(-11f, 172f).x, pt(-11f, 172f).y)
            quadraticBezierTo(
                pt(-8f, 195f + flameFlicker).x, pt(-8f, 195f + flameFlicker).y,
                pt(0f, 202f + flameFlicker).x, pt(0f, 202f + flameFlicker).y
            )
            quadraticBezierTo(
                pt(8f, 195f + flameFlicker).x, pt(8f, 195f + flameFlicker).y,
                pt(11f, 172f).x, pt(11f, 172f).y
            )
        }
        
        drawPath(
            outerFlamePath,
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFFFF6600),
                    Color(0xFFFF4500),
                    Color(0xFFFF3300).copy(alpha = 0.8f)
                ),
                startY = pt(-11f, 172f).y,
                endY = pt(0f, 202f + flameFlicker).y
            )
        )

        // Внутреннее пламя (жёлтое)
        val innerFlamePath = Path().apply {
            moveTo(pt(-5f, 172f).x, pt(-5f, 172f).y)
            quadraticBezierTo(
                pt(-3f, 185f + flameFlicker * 0.7f).x, pt(-3f, 185f + flameFlicker * 0.7f).y,
                pt(0f, 192f + flameFlicker * 0.7f).x, pt(0f, 192f + flameFlicker * 0.7f).y
            )
            quadraticBezierTo(
                pt(3f, 185f + flameFlicker * 0.7f).x, pt(3f, 185f + flameFlicker * 0.7f).y,
                pt(5f, 172f).x, pt(5f, 172f).y
            )
        }
        
        drawPath(
            innerFlamePath,
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFFFFD700),
                    Color(0xFFFFAA00),
                    Color(0xFFFF8C00)
                ),
                startY = pt(-5f, 172f).y,
                endY = pt(0f, 192f + flameFlicker * 0.7f).y
            )
        )

        // ================= СОПЛО =================
        // Тень под соплом
        drawRoundRect(
            color = Color.Black.copy(alpha = 0.15f),
            topLeft = pt(-17f, 163f),
            size = Size(34f * u, 7f * u),
            cornerRadius = CornerRadius(3f * u)
        )
        
        // Верхний фланец сопла
        drawRoundRect(
            color = darkerGray,
            topLeft = pt(-16f, 162f),
            size = Size(32f * u, 5f * u),
            cornerRadius = CornerRadius(2f * u)
        )
        drawRoundRect(
            color = darkGray,
            topLeft = pt(-16f, 162f),
            size = Size(32f * u, 5f * u),
            cornerRadius = CornerRadius(2f * u),
            style = Stroke(width = 1.2f * u)
        )
        
        // Основной раструб
        val nozzlePath = Path().apply {
            moveTo(pt(-16f, 167f).x, pt(-16f, 167f).y)
            lineTo(pt(-11f, 172f).x, pt(-11f, 172f).y)
            lineTo(pt(11f, 172f).x, pt(11f, 172f).y)
            lineTo(pt(16f, 167f).x, pt(16f, 167f).y)
        }
        drawPath(nozzlePath, color = darkerGray)
        drawPath(nozzlePath, color = darkGray, style = Stroke(width = 1.2f * u))

        // ================= ТУЛОВИЩЕ =================
        // Тень
        val upperBodyShadow = Path().apply {
            moveTo(pt(-42f, 89f).x, pt(-42f, 89f).y)
            quadraticBezierTo(pt(-48f, 79f).x, pt(-48f, 79f).y, pt(-42f, 75f).x, pt(-42f, 75f).y)
            lineTo(pt(42f, 75f).x, pt(42f, 75f).y)
            quadraticBezierTo(pt(48f, 79f).x, pt(48f, 79f).y, pt(42f, 89f).x, pt(42f, 89f).y)
            lineTo(pt(36f, 121f).x, pt(36f, 121f).y)
            quadraticBezierTo(pt(30f, 146f).x, pt(30f, 146f).y, pt(17f, 163f).x, pt(17f, 163f).y)
            lineTo(pt(-17f, 163f).x, pt(-17f, 163f).y)
            quadraticBezierTo(pt(-30f, 146f).x, pt(-30f, 146f).y, pt(-36f, 121f).x, pt(-36f, 121f).y)
            close()
        }
        drawPath(upperBodyShadow, color = Color.Black.copy(alpha = 0.1f))

        // Верхняя часть груди (градиент)
        val upperBodyPath = Path().apply {
            moveTo(pt(-42f, 88f).x, pt(-42f, 88f).y)
            quadraticBezierTo(pt(-48f, 78f).x, pt(-48f, 78f).y, pt(-42f, 74f).x, pt(-42f, 74f).y)
            lineTo(pt(42f, 74f).x, pt(42f, 74f).y)
            quadraticBezierTo(pt(48f, 78f).x, pt(48f, 78f).y, pt(42f, 88f).x, pt(42f, 88f).y)
            lineTo(pt(36f, 120f).x, pt(36f, 120f).y)
            quadraticBezierTo(pt(30f, 145f).x, pt(30f, 145f).y, pt(16f, 162f).x, pt(16f, 162f).y)
            lineTo(pt(-16f, 162f).x, pt(-16f, 162f).y)
            quadraticBezierTo(pt(-30f, 145f).x, pt(-30f, 145f).y, pt(-36f, 120f).x, pt(-36f, 120f).y)
            close()
        }
        
        drawPath(
            upperBodyPath,
            brush = Brush.verticalGradient(
                colors = listOf(whiteBody, lightGray),
                startY = pt(-42f, 74f).y,
                endY = pt(-36f, 120f).y
            )
        )
        drawPath(upperBodyPath, color = darkGray, style = Stroke(width = 1.2f * u))

        // Панель на груди (тёмная с градиентом)
        drawRoundRect(
            color = screenBlack,
            topLeft = pt(-24f, 92f),
            size = Size(48f * u, 42f * u),
            cornerRadius = CornerRadius(10f * u)
        )
        drawRoundRect(
            color = darkerGray,
            topLeft = pt(-20f, 96f),
            size = Size(40f * u, 34f * u),
            cornerRadius = CornerRadius(8f * u)
        )

        // Индикатор-сердце (светящийся)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(neonBluePulse, neonBlue.copy(alpha = 0.5f), Color.Transparent),
                radius = 12f * u
            ),
            center = pt(0f, 113f),
            radius = 12f * u
        )
        drawCircle(
            color = neonBlue,
            radius = 10f * u,
            center = pt(0f, 113f)
        )
        drawCircle(
            color = neonBlueGlow,
            radius = 5f * u,
            center = pt(0f, 113f)
        )

        // ================= ШЕЯ =================
        drawRoundRect(
            color = mediumGray,
            topLeft = pt(-14f, 62f),
            size = Size(28f * u, 14f * u),
            cornerRadius = CornerRadius(4f * u)
        )
        drawRoundRect(
            color = darkGray,
            topLeft = pt(-14f, 62f),
            size = Size(28f * u, 14f * u),
            cornerRadius = CornerRadius(4f * u),
            style = Stroke(width = 1.2f * u)
        )

        // ================= ЛЕВЫЙ НАУШНИК =================
        drawRoundRect(
            color = lightGray,
            topLeft = pt(-58f, 22f),
            size = Size(14f * u, 32f * u),
            cornerRadius = CornerRadius(7f * u)
        )
        drawRoundRect(
            color = darkGray,
            topLeft = pt(-58f, 22f),
            size = Size(14f * u, 32f * u),
            cornerRadius = CornerRadius(7f * u),
            style = Stroke(width = 1.2f * u)
        )
        drawCircle(
            color = mediumGray,
            center = pt(-51f, 38f),
            radius = 7f * u
        )
        drawCircle(
            color = darkGray,
            center = pt(-51f, 38f),
            radius = 7f * u,
            style = Stroke(width = 1.2f * u)
        )
        drawCircle(
            color = neonBlue.copy(alpha = 0.6f + sin(pulse) * 0.4f),
            center = pt(-51f, 38f),
            radius = 3f * u
        )

        // ================= ПРАВЫЙ НАУШНИК =================
        drawRoundRect(
            color = lightGray,
            topLeft = pt(44f, 22f),
            size = Size(14f * u, 32f * u),
            cornerRadius = CornerRadius(7f * u)
        )
        drawRoundRect(
            color = darkGray,
            topLeft = pt(44f, 22f),
            size = Size(14f * u, 32f * u),
            cornerRadius = CornerRadius(7f * u),
            style = Stroke(width = 1.2f * u)
        )
        drawCircle(
            color = mediumGray,
            center = pt(51f, 38f),
            radius = 7f * u
        )
        drawCircle(
            color = darkGray,
            center = pt(51f, 38f),
            radius = 7f * u,
            style = Stroke(width = 1.2f * u)
        )
        // Светящаяся точка на правом наушнике
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(neonBlue, neonBlue.copy(alpha = 0.3f), Color.Transparent),
                radius = 8f * u
            ),
            center = pt(51f, 38f),
            radius = 8f * u
        )
        drawCircle(
            color = neonBlue,
            center = pt(51f, 38f),
            radius = 5f * u
        )

        // ================= ГОЛОВА =================
        // Тень головы
        val headShadowPath = Path().apply {
            moveTo(pt(-42f, 53f).x, pt(-42f, 53f).y)
            quadraticBezierTo(pt(-46f, 31f).x, pt(-46f, 31f).y, pt(-40f, 17f).x, pt(-40f, 17f).y)
            quadraticBezierTo(pt(-30f, 5f).x, pt(-30f, 5f).y, pt(0f, 5f).x, pt(0f, 5f).y)
            quadraticBezierTo(pt(30f, 5f).x, pt(30f, 5f).y, pt(40f, 17f).x, pt(40f, 17f).y)
            quadraticBezierTo(pt(46f, 31f).x, pt(46f, 31f).y, pt(42f, 53f).x, pt(42f, 53f).y)
            close()
        }
        drawPath(headShadowPath, color = Color.Black.copy(alpha = 0.08f))

        // Основная голова (градиент)
        val headPath = Path().apply {
            moveTo(pt(-42f, 52f).x, pt(-42f, 52f).y)
            quadraticBezierTo(pt(-46f, 30f).x, pt(-46f, 30f).y, pt(-40f, 16f).x, pt(-40f, 16f).y)
            quadraticBezierTo(pt(-30f, 4f).x, pt(-30f, 4f).y, pt(0f, 4f).x, pt(0f, 4f).y)
            quadraticBezierTo(pt(30f, 4f).x, pt(30f, 4f).y, pt(40f, 16f).x, pt(40f, 16f).y)
            quadraticBezierTo(pt(46f, 30f).x, pt(46f, 30f).y, pt(42f, 52f).x, pt(42f, 52f).y)
            close()
        }
        
        drawPath(
            headPath,
            brush = Brush.verticalGradient(
                colors = listOf(whiteBody, lightGray),
                startY = pt(-40f, 16f).y,
                endY = pt(-42f, 52f).y
            )
        )
        drawPath(headPath, color = darkGray, style = Stroke(width = 1.5f * u))

        // ================= ВИЗОР (чёрный экран) =================
        drawRoundRect(
            color = screenBlack,
            topLeft = pt(-36f, 18f),
            size = Size(72f * u, 38f * u),
            cornerRadius = CornerRadius(18f * u)
        )
        drawRoundRect(
            color = darkerGray,
            topLeft = pt(-32f, 22f),
            size = Size(64f * u, 30f * u),
            cornerRadius = CornerRadius(15f * u)
        )

        // ================= ГЛАЗА (светящиеся) =================
        val eyeY = 38f
        val eyeRX = 5f * u
        val eyeRY = if (currentBlink < 0.5f) 1.2f * u else 8f * u

        // Левый глаз
        val leftEyeX = -18f + lookOffsetX / u
        val leftEyeCenter = pt(leftEyeX, eyeY + lookOffsetY / u)
        
        // Свечение глаза
        drawOval(
            brush = Brush.radialGradient(
                colors = listOf(neonBluePulse, neonBlue.copy(alpha = 0.4f), Color.Transparent),
                radius = 12f * u
            ),
            topLeft = pt(leftEyeX - 6f, eyeY + lookOffsetY / u - 10f),
            size = Size(12f * u, 20f * u)
        )
        
        // Основная часть глаза
        drawOval(
            color = neonBluePulse,
            topLeft = pt(leftEyeX - eyeRX / u, eyeY + lookOffsetY / u - eyeRY / u),
            size = Size(eyeRX * 2f, eyeRY * 2f)
        )
        
        if (currentBlink > 0.5f) {
            // Зрачок
            drawCircle(
                color = Color.White,
                radius = 2f * u,
                center = leftEyeCenter
            )
            // Блик
            drawCircle(
                color = Color.White.copy(alpha = 0.8f),
                radius = 0.7f * u,
                center = pt(leftEyeX - 1f, eyeY + lookOffsetY / u - 1f)
            )
        }

        // Правый глаз
        val rightEyeX = 18f + lookOffsetX / u
        val rightEyeCenter = pt(rightEyeX, eyeY + lookOffsetY / u)
        
        drawOval(
            brush = Brush.radialGradient(
                colors = listOf(neonBluePulse, neonBlue.copy(alpha = 0.4f), Color.Transparent),
                radius = 12f * u
            ),
            topLeft = pt(rightEyeX - 6f, eyeY + lookOffsetY / u - 10f),
            size = Size(12f * u, 20f * u)
        )
        
        drawOval(
            color = neonBluePulse,
            topLeft = pt(rightEyeX - eyeRX / u, eyeY + lookOffsetY / u - eyeRY / u),
            size = Size(eyeRX * 2f, eyeRY * 2f)
        )
        
        if (currentBlink > 0.5f) {
            drawCircle(
                color = Color.White,
                radius = 2f * u,
                center = rightEyeCenter
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.8f),
                radius = 0.7f * u,
                center = pt(rightEyeX - 1f, eyeY + lookOffsetY / u - 1f)
            )
        }

        // ================= РОТ =================
        if (isSpeaking) {
            val mouthH = (3f + 2f * sin(mouthPhase)) * u
            drawOval(
                color = neonBlue,
                topLeft = pt(-10f, 50f - mouthH / u / 2f),
                size = Size(20f * u, mouthH * 2f)
            )
        } else {
            // Улыбка
            drawArc(
                color = neonBlue,
                startAngle = 30f,
                sweepAngle = 120f,
                useCenter = false,
                topLeft = pt(-14f, 48f),
                size = Size(28f * u, 14f * u),
                style = Stroke(width = 2f * u, cap = StrokeCap.Round)
            )
        }

        // ================= АНТЕННЫ =================
        drawLine(
            color = darkGray,
            start = pt(-28f, 8f),
            end = pt(-28f, -6f),
            strokeWidth = 2.5f * u,
            cap = StrokeCap.Round
        )
        drawLine(
            color = darkGray,
            start = pt(28f, 8f),
            end = pt(28f, -6f),
            strokeWidth = 2.5f * u,
            cap = StrokeCap.Round
        )

        // ================= РУКИ =================
        // ЛЕВАЯ РУКА
        drawCircle(
            color = lightGray,
            center = pt(-46f, 92f),
            radius = 14f * u
        )
        drawCircle(
            color = darkGray,
            center = pt(-46f, 92f),
            radius = 14f * u,
            style = Stroke(width = 1.2f * u)
        )
        
        val leftArmPath = Path().apply {
            moveTo(pt(-58f, 100f).x, pt(-58f, 100f).y)
            quadraticBezierTo(
                pt(-68f, 118f).x, pt(-68f, 118f).y,
                pt(-56f, 130f).x, pt(-56f, 130f).y
            )
        }
        drawPath(leftArmPath, color = lightGray)
        drawPath(leftArmPath, color = darkGray, style = Stroke(width = 1.2f * u))
        
        drawCircle(
            color = darkerGray,
            center = pt(-56f, 128f),
            radius = 7f * u
        )

        // ЛЕВАЯ КИСТЬ
        drawOval(
            color = darkerGray,
            topLeft = pt(-42f, 118f),
            size = Size(24f * u, 18f * u)
        )
        for (i in 0..3) {
            val fx = -38f + i * 7f
            drawRoundRect(
                color = darkGray,
                topLeft = pt(fx, 110f),
                size = Size(6f * u, 14f * u),
                cornerRadius = CornerRadius(3f * u)
            )
        }

        // ПРАВАЯ РУКА
        drawCircle(
            color = lightGray,
            center = pt(46f, 92f),
            radius = 14f * u
        )
        drawCircle(
            color = darkGray,
            center = pt(46f, 92f),
            radius = 14f * u,
            style = Stroke(width = 1.2f * u)
        )
        
        val rightArmPath = Path().apply {
            moveTo(pt(58f, 100f).x, pt(58f, 100f).y)
            quadraticBezierTo(
                pt(68f, 118f).x, pt(68f, 118f).y,
                pt(56f, 130f).x, pt(56f, 130f).y
            )
        }
        drawPath(rightArmPath, color = lightGray)
        drawPath(rightArmPath, color = darkGray, style = Stroke(width = 1.2f * u))
        
        drawCircle(
            color = darkerGray,
            center = pt(56f, 128f),
            radius = 7f * u
        )

        // ПРАВАЯ КИСТЬ
        drawOval(
            color = darkerGray,
            topLeft = pt(18f, 120f),
            size = Size(26f * u, 18f * u)
        )
       for (i in 0..3) {
            val fx = 22f + i * 7f
            drawRoundRect(
                color = darkGray,
                topLeft = pt(fx, 112f),
                size = Size(6f * u, 14f * u),
                cornerRadius = CornerRadius(3f * u)
            )
        }
    }
}

private val SineClientEasing = Easing { fraction ->
    sin(fraction * PI.toFloat() / 2f).toFloat()
}


@Composable
private fun LockScreen(
    secretPhrase: String,
    onSecretPhraseChange: (String) -> Unit,
    onVerify: () -> Unit,
    viewModel: MainViewModel,
    isPermanentlyBlocked: Boolean,
    colors: AppColors
) {
    val context = LocalContext.current

    BackHandler(enabled = true) {
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.surfaceGray),
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
                    color = colors.text,
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
                    color = colors.text,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                OutlinedTextField(
                    value = secretPhrase,
                    onValueChange = onSecretPhraseChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    placeholder = { Text("Введите фразу...", color = colors.text.copy(alpha = 0.5f)) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = colors.text,
                        unfocusedTextColor = colors.text,
                        focusedContainerColor = colors.background,
                        unfocusedContainerColor = colors.background,
                        focusedBorderColor = colors.accent,
                        unfocusedBorderColor = colors.borderGray,
                        cursorColor = colors.accent
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
                    colors = ButtonDefaults.buttonColors(containerColor = colors.accent),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .height(56.dp)
                ) {
                    Text(
                        text = "Подтвердить",
                        color = colors.text,
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
    isSpeaking: Boolean = false,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    colors: AppColors,
    isTtsReady: Boolean
    ) {
    val isLocalReady = isModelLoaded
    val isCloudReady = cloudConfig?.authKey?.isNotEmpty() == true
    val localIndicatorColor = if (isLocalReady) colors.green else colors.paleYellow
    val cloudIndicatorColor = if (isCloudReady) colors.green else colors.paleYellow

    val transition = rememberInfiniteTransition(label = "top_bar_transition")

    val planetPulse by transition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "planet_pulse"
    )
    var topBarPositionInRoot by remember { mutableStateOf(Offset.Zero) }
    val robotOrbitAngle by transition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "robot_orbit"
    )

   Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(84.dp)
            .padding(4.dp)
            .onGloballyPositioned { coordinates ->
                topBarPositionInRoot = coordinates.positionInRoot()
            }
    ) {
       
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = if (!isDarkTheme) {
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFFFFFDF5),
                                Color(0xFFFFF8DC),
                                Color(0xFFF0E0B8)
                            )
                        )
                    } else {
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF3A3A2E),
                                Color(0xFF2A2A1E),
                                Color(0xFF1A1A10)
                            )
                        )
                    },
                    shape = RoundedCornerShape(8.dp)
                )
                .border(1.dp, colors.borderGray, RoundedCornerShape(8.dp))
        )

                SpaceBackground(
            isDarkTheme = isDarkTheme,
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(8.dp)),
            planetPulse = planetPulse,
            robotOrbitAngle = robotOrbitAngle,
            robotOnOrbitAlpha = 1f
        )

       Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        brush = if (!isDarkTheme) {
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFFFFFFFF),
                                    Color(0xFFF5F7FA),
                                    Color(0xFFE8ECF1)
                                )
                            )
                        } else {
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFF3A3A2E),
                                    Color(0xFF2A2A1E),
                                    Color(0xFF1A1A10)
                                )
                            )
                        },
                        shape = RoundedCornerShape(8.dp)
                    )
                    .border(1.dp, colors.borderGray, RoundedCornerShape(8.dp))
                    .clickable { onToggleTheme() }
                    .padding(4.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Image(
                        painter = painterResource(id = R.mipmap.ic_launcher),
                        contentDescription = "Логотип",
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(6.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Text(
                        text = "ИИ-Друг",
                        color = colors.accent,
                        fontSize = 8.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center,
                        maxLines = 1
                    )
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                contentAlignment = Alignment.Center
            ) {
                // Робот здесь больше не отображается — он в ChatScreen
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
                        text = "локальный ИИ",
                        colors = colors
                    )
                    StatusIndicator(
                        color = cloudIndicatorColor,
                        text = "Облачный ИИ",
                        colors = colors
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
                        modifier = Modifier.width(42.dp).height(22.dp),
                        colors = colors
                    )
                    ModeButton(
                        label = "Neutral",
                        isSelected = currentMode == AIMode.NEUTRAL,
                        onClick = { onModeChange(AIMode.NEUTRAL) },
                        modifier = Modifier.width(42.dp).height(22.dp),
                        colors = colors
                    )
                    ModeButton(
                        label = "Cloud",
                        isSelected = currentMode == AIMode.CLOUD,
                        onClick = { onCloudForceDialog() },
                        modifier = Modifier.width(42.dp).height(22.dp),
                        colors = colors
                    )
                }
            }
        }
    }
}

@Composable
private fun SpaceBackground(
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier,
    planetPulse: Float = 1f,
    robotOrbitAngle: Float = 0f,
    robotOnOrbitAlpha: Float = 1f
) {
    val transition = rememberInfiniteTransition(label = "space_bg")
    val t by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 24000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "space_t"
    )

    Box(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val cx = w * SpaceConstants.ORBIT_CENTER_X_RATIO
            val cy = h * SpaceConstants.ORBIT_CENTER_Y_RATIO
            val T = t * 2f * PI.toFloat()
            val vis = if (isDarkTheme) 1f else 0.55f

            fun rnd(i: Int, s: Int): Float {
                val x = sin(i * 12.9898f + s * 78.233f) * 43758.5453f
                return (x % 1f + 1f) % 1f
            }

            // ===================== ФОНОВЫЕ ТУМАННОСТИ =====================
            drawCircle(
                Brush.radialGradient(
                    listOf(Color(0xFF9B59B6).copy(alpha = 0.08f * vis), Color.Transparent),
                    center = Offset(w * 0.18f, h * 0.75f), radius = h * 0.7f
                ),
                radius = h * 0.7f, center = Offset(w * 0.18f, h * 0.75f)
            )
            drawCircle(
                Brush.radialGradient(
                    listOf(Color(0xFF2ECC71).copy(alpha = 0.05f * vis), Color.Transparent),
                    center = Offset(w * 0.75f, h * 0.25f), radius = h * 0.5f
                ),
                radius = h * 0.5f, center = Offset(w * 0.75f, h * 0.25f)
            )
            drawCircle(
                Brush.radialGradient(
                    listOf(Color(0xFFFFD1DC).copy(alpha = 0.05f * vis), Color.Transparent),
                    center = Offset(cx, cy), radius = h * 0.6f
                ),
                radius = h * 0.6f, center = Offset(cx, cy)
            )

            // ===================== ЗВЁЗДЫ =====================
            val starCore = if (isDarkTheme) Color(0xFFEAF6FF) else Color(0xFF8FB6E8)
            val starGlow = Color(0xFF7FB4FF)
            for (i in 0 until 70) {
                val sx = rnd(i, 1) * w
                val sy = rnd(i, 2) * h
                val seed = rnd(i, 3)
                val speed = 2f + (i % 3).toFloat()
                val tw = 0.5f + 0.5f * sin(T * speed + seed * 6.283f)
                val r = (0.5f + 1.1f * seed) * (h / 90f)
                val a = (0.10f + 0.80f * tw * tw) * vis
                drawCircle(
                    Brush.radialGradient(
                        listOf(starGlow.copy(alpha = a * 0.5f), Color.Transparent),
                        center = Offset(sx, sy), radius = r * 4f
                    ),
                    radius = r * 4f, center = Offset(sx, sy)
                )
                drawCircle(starCore.copy(alpha = a), r, Offset(sx, sy))
                if (tw > 0.8f) {
                    val la = (tw - 0.8f) * 5f * 0.6f * vis
                    drawLine(starCore.copy(alpha = la),
                        Offset(sx - r * 3.5f, sy), Offset(sx + r * 3.5f, sy), strokeWidth = 1f)
                    drawLine(starCore.copy(alpha = la),
                        Offset(sx, sy - r * 3.5f), Offset(sx, sy + r * 3.5f), strokeWidth = 1f)
                }
            }

            // ===================== УДАЛЕННЫЕ СВЕРХНОВЫЕ И КВАЗАРЫ =====================
            val qx1 = w * 0.85f; val qy1 = h * 0.10f
            val qPulse1 = 0.5f + 0.5f * sin(T * 5f)
            drawCircle(
                Brush.radialGradient(
                    listOf(Color(0xFFFFFFFF).copy(alpha = 0.9f * vis), Color(0xFF00FFFF).copy(alpha = 0.3f * vis), Color.Transparent),
                    center = Offset(qx1, qy1), radius = h * 0.05f
                ),
                radius = h * 0.05f, center = Offset(qx1, qy1)
            )
            rotate(45f, pivot = Offset(qx1, qy1)) {
                drawLine(Color.White.copy(alpha = 0.3f * vis * qPulse1), Offset(qx1 - h * 0.09f, qy1), Offset(qx1 + h * 0.09f, qy1), strokeWidth = h * 0.002f)
                drawLine(Color.White.copy(alpha = 0.2f * vis * qPulse1), Offset(qx1, qy1 - h * 0.09f), Offset(qx1, qy1 + h * 0.09f), strokeWidth = h * 0.002f)
            }

            val qx2 = w * 0.08f; val qy2 = h * 0.35f
            val qPulse2 = 0.5f + 0.5f * sin(T * 4f + 2f)
            drawCircle(
                Brush.radialGradient(
                    listOf(Color(0xFFFFFFFF).copy(alpha = 0.8f * vis), Color(0xFFFF00FF).copy(alpha = 0.2f * vis), Color.Transparent),
                    center = Offset(qx2, qy2), radius = h * 0.04f
                ),
                radius = h * 0.04f, center = Offset(qx2, qy2)
            )
            rotate(-30f, pivot = Offset(qx2, qy2)) {
                drawLine(Color(0xFFFF00FF).copy(alpha = 0.3f * vis * qPulse2), Offset(qx2 - h * 0.06f, qy2), Offset(qx2 + h * 0.06f, qy2), strokeWidth = h * 0.0015f)
                drawLine(Color(0xFFFF00FF).copy(alpha = 0.2f * vis * qPulse2), Offset(qx2, qy2 - h * 0.06f), Offset(qx2, qy2 + h * 0.06f), strokeWidth = h * 0.0015f)
            }

            val snX = w * 0.20f; val snY = h * 0.45f
            val snLife = (T * 0.7f) % (2f * PI.toFloat())
            val snPhase = snLife / (2f * PI.toFloat())
            if (snPhase < 0.4f) {
                val snBrightness = sin(PI.toFloat() * (snPhase / 0.4f))
                drawCircle(
                    Brush.radialGradient(
                        listOf(Color.White.copy(alpha = snBrightness * vis), Color(0xFFFFAA00).copy(alpha = snBrightness * 0.5f * vis), Color.Transparent),
                        center = Offset(snX, snY), radius = h * 0.12f
                    ),
                    radius = h * 0.12f, center = Offset(snX, snY)
                )
                for (i in 0 until 12) {
                    val extAngle = (2f * PI.toFloat() / 12f) * i
                    val extDist = h * (0.08f + 0.1f * snPhase)
                    drawLine(Color(0xFFFFAA00).copy(alpha = (1f - snPhase) * 0.8f * vis),
                        Offset(snX, snY),
                        Offset(snX + cos(extAngle) * extDist, snY + sin(extAngle) * extDist),
                        strokeWidth = h * 0.002f)
                }
            }

            // ===== ОРБИТЫ (без наклона, совпадают с траекториями) =====
            val orbitColor = Color(0xFF7FB4FF)
            val mRx = w * 0.27f; val mRy = h * 0.22f
            val eRx = w * 0.37f; val eRy = h * 0.33f
            val sRx = w * 0.46f; val sRy = h * 0.42f
            val rRx = w * SpaceConstants.ROBOT_ORBIT_RX
            val rRy = h * SpaceConstants.ROBOT_ORBIT_RY

            val orbits = listOf(
                Pair(rRx, rRy),
                Pair(mRx, mRy),
                Pair(eRx, eRy),
                Pair(sRx, sRy)
            )
            for (orbit in orbits) {
                val rx = orbit.first
                val ry = orbit.second
                drawOval(
                    orbitColor.copy(alpha = 0.14f * vis),
                    topLeft = Offset(cx - rx, cy - ry),
                    size = Size(rx * 2f, ry * 2f),
                    style = Stroke(width = h / 70f)
                )
            }

            // ===== ЭЛЕКТРИЧЕСКОЕ СОЛНЦЕ =====
            val baseSunRadius = h * SpaceConstants.PLANET_SIZE_RATIO
            val sunPulse = 1f + 0.08f * sin(T * 4f)
            val sunRadius = baseSunRadius * sunPulse * planetPulse

            drawCircle(
                Brush.radialGradient(
                    colors = listOf(
                        Color(0x00FFFFFF),
                        Color(0x30CCEEFF),
                        Color(0x6088CCFF),
                        Color(0x004499FF)
                    ),
                    center = Offset(cx, cy),
                    radius = sunRadius * 3.2f
                ),
                radius = sunRadius * 3.2f,
                center = Offset(cx, cy)
            )
            drawCircle(
                Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF00FFFF).copy(alpha = 0.8f * vis),
                        Color(0xFF00BFFF).copy(alpha = 0.4f * vis),
                        Color(0xFF0044FF).copy(alpha = 0f)
                    ),
                    center = Offset(cx, cy),
                    radius = sunRadius * 1.8f
                ),
                radius = sunRadius * 1.8f,
                center = Offset(cx, cy)
            )
            drawCircle(
                Brush.radialGradient(
                    colors = listOf(
                        Color.White,
                        Color(0xFF00FFFF),
                        Color(0xFF00BFFF),
                        Color(0xFF0044FF)
                    ),
                    center = Offset(cx, cy),
                    radius = sunRadius
                ),
                radius = sunRadius,
                center = Offset(cx, cy)
            )

            val particleCount = 80
            for (i in 0 until particleCount) {
                val u = rnd(i, 10) * 2f * PI.toFloat()
                val v = rnd(i, 20) * PI.toFloat()
                val px = cx + sunRadius * sin(v) * cos(u)
                val py = cy + sunRadius * sin(v) * sin(u)
                val pr = 0.5f + rnd(i, 30) * 1.2f
                val pa = 0.3f + 0.5f * (0.5f + 0.5f * sin(T * 6f + i))
                drawCircle(
                    Color(0xFF00FFFF).copy(alpha = pa * vis),
                    pr,
                    Offset(px, py)
                )
            }

            for (i in 0..2) {
                val phase = i * 2f * PI.toFloat() / 3f
                val orbitR = sunRadius * (1.5f + 0.2f * sin(T * 3f + i))
                val boltAngle = T * 4f + phase
                val bx = cx + cos(boltAngle) * orbitR
                val by = cy + sin(boltAngle) * orbitR

                for (tail in 0..5) {
                    val tailT = tail / 5f
                    val tailAngle = boltAngle - tailT * 0.4f
                    val tailX = cx + cos(tailAngle) * orbitR
                    val tailY = cy + sin(tailAngle) * orbitR
                    val tailAlpha = (1f - tailT) * 0.5f * vis
                    drawCircle(
                        Color(0xFF00FFFF).copy(alpha = tailAlpha),
                        2.5f - tailT * 1.5f,
                        Offset(tailX, tailY)
                    )
                }

                drawCircle(
                    Brush.radialGradient(
                        listOf(
                            Color.White,
                            Color(0xFF00FFFF).copy(alpha = 0.6f * vis),
                            Color.Transparent
                        ),
                        center = Offset(bx, by),
                        radius = 8f
                    ),
                    radius = 8f,
                    center = Offset(bx, by)
                )
                drawCircle(
                    Color.White.copy(alpha = 0.9f * vis),
                    2.5f,
                    Offset(bx, by)
                )
            }

            drawCircle(
                Color.White.copy(alpha = 0.7f * vis),
                sunRadius * 0.25f,
                Offset(cx - sunRadius * 0.3f, cy - sunRadius * 0.3f)
            )

            // ===== ЛУНА =====
            val moonAngle = T * 2f + 2.1f
            val moonX = cx + cos(moonAngle) * mRx
            val moonY = cy + sin(moonAngle) * mRy
            val moonRadius = h * 0.055f
            val moonTw = 0.5f + 0.5f * sin(T * 3f + 1f)
            val moonCol = Color(0xFFFFEE88)
            drawCircle(
                Brush.radialGradient(
                    listOf(moonCol.copy(alpha = (0.15f + 0.25f * moonTw) * vis), Color.Transparent),
                    center = Offset(moonX, moonY),
                    radius = moonRadius * 3f
                ),
                radius = moonRadius * 3f,
                center = Offset(moonX, moonY)
            )
            drawCircle(moonCol.copy(alpha = 0.55f + 0.45f * vis), moonRadius, Offset(moonX, moonY))
            drawCircle(Color(0xFFCCAA44).copy(alpha = 0.7f * vis), moonRadius * 0.35f,
                Offset(moonX - moonRadius * 0.25f, moonY + moonRadius * 0.10f))

            // ===== ЗЕМЛЯ =====
            val earthAngle = T * 1f + 0.6f
            val earthX = cx + cos(earthAngle) * eRx
            val earthY = cy + sin(earthAngle) * eRy
            val earthRadius = h * 0.095f
            val earthTw = 0.5f + 0.5f * sin(T * 2f + 2f)
            val earthCol = Color(0xFF3F8FD6)
            drawCircle(
                Brush.radialGradient(
                    listOf(earthCol.copy(alpha = (0.20f + 0.30f * earthTw) * vis), Color.Transparent),
                    center = Offset(earthX, earthY),
                    radius = earthRadius * 3f
                ),
                radius = earthRadius * 3f,
                center = Offset(earthX, earthY)
            )
            drawCircle(
                Brush.radialGradient(
                    listOf(Color(0xFF9CD2FF), earthCol, Color(0xFF1F5FA0)),
                    center = Offset(earthX, earthY),
                    radius = earthRadius * 1.4f
                ),
                radius = earthRadius,
                center = Offset(earthX, earthY)
            )
            drawCircle(Color(0xFF57B368).copy(alpha = 0.85f), earthRadius * 0.42f,
                Offset(earthX - earthRadius * 0.25f, earthY - earthRadius * 0.15f))
            drawCircle(Color(0xFF57B368).copy(alpha = 0.75f), earthRadius * 0.30f,
                Offset(earthX + earthRadius * 0.30f, earthY + earthRadius * 0.25f))
            drawCircle(Color.White.copy(alpha = 0.5f * vis), earthRadius * 0.22f,
                Offset(earthX - earthRadius * 0.35f, earthY - earthRadius * 0.40f))

            // ===== МАЛЕНЬКАЯ ЛУНА ВОКРУГ ЗЕМЛИ =====
            val miniMoonOrbitRadius = earthRadius * 2.2f
            val miniMoonAngle = T * 4f
            val miniMoonX = earthX + cos(miniMoonAngle) * miniMoonOrbitRadius
            val miniMoonY = earthY + sin(miniMoonAngle) * miniMoonOrbitRadius * 0.7f
            val miniMoonRadius = earthRadius * 0.3f
            drawCircle(
                Brush.radialGradient(
                    listOf(Color(0xFFEEEEEE), Color(0xFFAAAAAA)),
                    center = Offset(miniMoonX - miniMoonRadius * 0.3f, miniMoonY - miniMoonRadius * 0.3f),
                    radius = miniMoonRadius
                ),
                radius = miniMoonRadius,
                center = Offset(miniMoonX, miniMoonY)
            )

            // ===== САТУРН =====
            val saturnAngle = T * 1f + 3.6f
            val saturnX = cx + cos(saturnAngle) * sRx
            val saturnY = cy + sin(saturnAngle) * sRy
            val saturnRadius = h * 0.08f
            val saturnTw = 0.5f + 0.5f * sin(T * 2f + 4f)
            val saturnCol = Color(0xFFE0B97E)
            drawCircle(
                Brush.radialGradient(
                    listOf(saturnCol.copy(alpha = (0.18f + 0.28f * saturnTw) * vis), Color.Transparent),
                    center = Offset(saturnX, saturnY),
                    radius = saturnRadius * 3f
                ),
                radius = saturnRadius * 3f,
                center = Offset(saturnX, saturnY)
            )
            drawCircle(
                Brush.radialGradient(
                    listOf(Color(0xFFF4DCA8), saturnCol, Color(0xFF9C7A45)),
                    center = Offset(saturnX, saturnY),
                    radius = saturnRadius * 1.4f
                ),
                radius = saturnRadius,
                center = Offset(saturnX, saturnY)
            )
            rotate(-18f, pivot = Offset(saturnX, saturnY)) {
                drawOval(
                    Color(0xFFD9C08F).copy(alpha = 0.8f * vis),
                    topLeft = Offset(saturnX - saturnRadius * 1.9f, saturnY - saturnRadius * 0.55f),
                    size = Size(saturnRadius * 3.8f, saturnRadius * 1.1f),
                    style = Stroke(width = saturnRadius * 0.28f)
                )
            }

            // ===================== СТРАНСТВУЮЩАЯ КОМЕТА =====================
            val comP = (T * 0.15f) % 1f
            val cometX = lerp(-w * 0.2f, w * 1.3f, comP)
            val cometY = lerp(h * 1.0f, -h * 0.2f, comP)
            val cometAngle = atan2(-cometY, cometX)
            val cometRadius = h * 0.015f
            val cometTailLen = h * 0.18f * (1f - comP)

            rotate(degrees = cometAngle * 180f / PI.toFloat(), pivot = Offset(cometX, cometY)) {
                drawCircle(
                    Brush.radialGradient(
                        listOf(Color.White.copy(alpha = 0.9f * vis), Color(0xFF00FFFF).copy(alpha = 0.5f * vis), Color.Transparent),
                        center = Offset(cometX, cometY),
                        radius = cometRadius * 5f
                    ),
                    radius = cometRadius * 5f,
                    center = Offset(cometX, cometY)
                )

                val tailParticles = 30
                for (i in 0 until tailParticles) {
                    val tailT = i / tailParticles.toFloat()
                    val tailX = cometX - cos(cometAngle) * (cometTailLen * tailT)
                    val tailY = cometY - sin(cometAngle) * (cometTailLen * tailT)
                    val alpha = (1f - tailT) * 0.8f * vis
                    val radius = (cometRadius * 0.8f) * (1f - tailT * 0.6f)

                    drawCircle(
                        Color(0xFF55FFFF).copy(alpha = alpha),
                        radius,
                        Offset(tailX, tailY)
                    )
                }

                drawCircle(
                    Brush.radialGradient(
                        listOf(Color.White, Color(0xFF00FFFF)),
                        center = Offset(cometX, cometY),
                        radius = cometRadius
                    ),
                    radius = cometRadius,
                    center = Offset(cometX, cometY)
                )
            }
        }
    }
}

// Вспомогательная функция для интерполяции
private fun lerp(start: Float, stop: Float, fraction: Float): Float {
    return start + (stop - start) * fraction
}
@Composable
private fun StatusIndicator(
    color: Color,
    text: String,
    colors: AppColors
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(color, shape = CircleShape)
                .border(0.5.dp, colors.borderGray, CircleShape)
        )
        Text(
            text = text,
            fontSize = 6.sp,
            color = colors.text
        )
    }
}

@Composable
private fun ModeButton(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    colors: AppColors
) {
    Box(
        modifier = modifier
            .clickable { onClick() }
            .background(
                color = if (isSelected) colors.accent else colors.surfaceGray,
                shape = RoundedCornerShape(4.dp)
            )
            .border(
                width = if (isSelected) 1.dp else 0.5.dp,
                color = if (isSelected) colors.accent else colors.borderGray,
                shape = RoundedCornerShape(4.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (isSelected) colors.background else colors.text,
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
    coroutineScope: kotlinx.coroutines.CoroutineScope,
    colors: AppColors,
    isDarkTheme: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, colors.borderGray),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = if (!isDarkTheme) {
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFFFFFFFF),
                                Color(0xFFF5F7FA),
                                Color(0xFFE8ECF1)
                            )
                        )
                    } else {
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF3A3A2E),
                                Color(0xFF2A2A1E),
                                Color(0xFF1A1A10)
                            )
                        )
                    },
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButtonWithLabel(
                icon = Icons.Default.Memory,
                label = "мозг",
                onClick = onMemoryClick,
                colors = colors
            )
            IconButtonWithLabel(
                icon = Icons.Default.Settings,
                label = "движок",
                onClick = onSettingsClick,
                colors = colors
            )
            IconButtonWithLabel(
                icon = Icons.Default.Psychology,
                label = "характер",
                onClick = onPromptSettingsClick,
                colors = colors
            )
            IconButtonWithLabel(
                icon = Icons.Default.Info,
                label = "справка",
                onClick = onHelpClick,
                colors = colors
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
                },
                colors = colors
            )
        }
    }
}
@Composable
private fun IconButtonWithLabel(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
    colors: AppColors
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        IconButton(onClick = onClick) {
            Icon(imageVector = icon, contentDescription = label, tint = colors.accent)
        }
        Text(text = label, color = colors.text, fontSize = 8.sp)
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
    onClose: () -> Unit,
    colors: AppColors
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = colors.surfaceGray),
        modifier = Modifier.fillMaxWidth().padding(8.dp),
        border = BorderStroke(1.dp, colors.borderGray)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("🌡️ Настройки движка ИИ", color = colors.text, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Креативность (Температура): ${String.format("%.1f", temperature)}", color = colors.text)
            Slider(
                value = temperature,
                onValueChange = onTemperatureChange,
                valueRange = 0.1f..1.0f,
                steps = 9,
                modifier = Modifier.fillMaxWidth(),
                colors = SliderDefaults.colors(thumbColor = colors.accent, activeTrackColor = colors.accent, inactiveTrackColor = colors.borderGray)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = "Максимум токенов: $maxTokens", color = colors.text)
            Slider(
                value = maxTokens.toFloat(),
                onValueChange = { onMaxTokensChange(it.toInt()) },
                valueRange = 1f..4096f,
                steps = 50,
                modifier = Modifier.fillMaxWidth(),
                colors = SliderDefaults.colors(thumbColor = colors.accent, activeTrackColor = colors.accent, inactiveTrackColor = colors.borderGray)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = "Размер контекстного окна: $contextSize", color = colors.text)
            Slider(
                value = contextSize.toFloat(),
                onValueChange = { onContextSizeChange(it.toInt()) },
                valueRange = 512f..8192f,
                steps = 15,
                modifier = Modifier.fillMaxWidth(),
                colors = SliderDefaults.colors(thumbColor = colors.accent, activeTrackColor = colors.accent, inactiveTrackColor = colors.borderGray)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onModelChangeClick,
                colors = ButtonDefaults.buttonColors(containerColor = colors.borderGray),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Сменить или перезагрузить модель", color = colors.text)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        onSave()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.accent),
                    modifier = Modifier.weight(1f)
                ) { Text("Сохранить", color = colors.background) }
                Button(
                    onClick = onClose,
                    colors = ButtonDefaults.buttonColors(containerColor = colors.borderGray),
                    modifier = Modifier.weight(1f)
                ) { Text("Закрыть", color = colors.text) }
            }
        }
    }
}

@Composable
private fun PromptSettingsPanel(
    promptText: String,
    onPromptChange: (String) -> Unit,
    onSave: () -> Unit,
    colors: AppColors
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = colors.surfaceGray),
        modifier = Modifier.fillMaxWidth().padding(8.dp),
        border = BorderStroke(1.dp, colors.borderGray)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("🧠 Роль ИИ (Системный промпт)", color = colors.text, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = promptText,
                onValueChange = onPromptChange,
                label = { Text("Инструкция для ИИ", color = colors.text) },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3,
                singleLine = false,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = colors.text,
                    unfocusedTextColor = colors.text,
                    focusedContainerColor = colors.background,
                    unfocusedContainerColor = colors.background,
                    focusedBorderColor = colors.accent,
                    unfocusedBorderColor = colors.borderGray,
                    cursorColor = colors.accent
                )
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onSave,
                colors = ButtonDefaults.buttonColors(containerColor = colors.accent),
                modifier = Modifier.align(Alignment.End)
            ) { Text("Сохранить", color = colors.background) }
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
    isGeneratingToken: Boolean,
    colors: AppColors
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = colors.surfaceGray),
            border = BorderStroke(1.dp, colors.borderGray),
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
                        color = colors.accent,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Text(
                    text = "Введите данные для подключения к облачному ИИ",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.text
                )

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("🔵 GigaChat", color = colors.text, fontSize = 14.sp)
                    Switch(
                        checked = isGigaChat,
                        onCheckedChange = onIsGigaChatChange,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = colors.accent,
                            checkedTrackColor = colors.accent.copy(alpha = 0.5f),
                            uncheckedThumbColor = colors.borderGray,
                            uncheckedTrackColor = colors.borderGray.copy(alpha = 0.5f)
                        )
                    )
                    Text("🌐 Другой провайдер", color = colors.text, fontSize = 14.sp)
                }

                OutlinedTextField(
                    value = apiUrl,
                    onValueChange = onApiUrlChange,
                    label = { Text("API URL", color = colors.text, fontSize = 14.sp) },
                    placeholder = {
                        Text(
                            if (isGigaChat) "https://gigachat.devices.sberbank.ru/api/v1/chat/completions"
                            else "https://openrouter.ai/api/v1/chat/completions",
                            color = colors.text.copy(alpha = 0.5f),
                            fontSize = 12.sp
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = colors.text,
                        unfocusedTextColor = colors.text,
                        focusedBorderColor = colors.accent,
                        unfocusedBorderColor = colors.borderGray,
                        cursorColor = colors.accent
                    )
                )

                OutlinedTextField(
                    value = authKey,
                    onValueChange = onAuthKeyChange,
                    label = {
                        Text(
                            if (isGigaChat) "Authorization Key (Client Secret)"
                            else "API Key",
                            color = colors.text,
                            fontSize = 14.sp
                        )
                    },
                    placeholder = {
                        Text(
                            if (isGigaChat) "Введите ключ из Сбер Студии"
                            else "Введите ваш API ключ",
                            color = colors.text.copy(alpha = 0.5f),
                            fontSize = 12.sp
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = colors.text,
                        unfocusedTextColor = colors.text,
                        focusedBorderColor = colors.accent,
                        unfocusedBorderColor = colors.borderGray,
                        cursorColor = colors.accent
                    )
                )

                Button(
                    onClick = onGenerateToken,
                    enabled = !isGeneratingToken && authKey.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.accent,
                        contentColor = colors.background,
                        disabledContainerColor = colors.borderGray,
                        disabledContentColor = colors.text.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, colors.borderGray),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isGeneratingToken) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = colors.text, strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Получение токена...", color = colors.text, fontSize = 14.sp)
                    } else {
                        Text(
                            text = if (isCloudReady) "✅ Токен подключен" else if (isGigaChat) "🔑 Получить токен" else "🔑 Установить ключ",
                            color = colors.background,
                            fontSize = 14.sp
                        )
                    }
                }

                if (!isGigaChat) {
                    Text(
                        text = "ℹ️ Для обычных провайдеров ключ используется как токен",
                        color = colors.text.copy(alpha = 0.6f),
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
                                color = if (isCloudReady) colors.green else Color.Red,
                                shape = CircleShape
                            )
                            .border(
                                width = 1.dp,
                                color = if (isCloudReady) colors.green else Color.Red,
                                shape = CircleShape
                            )
                    )

                    Button(
                        onClick = onSave,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.accent,
                            contentColor = colors.background
                        ),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, colors.borderGray),
                        modifier = Modifier.weight(1f).height(36.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                    ) {
                        Text(
                            text = "Сохранить",
                            color = colors.background,
                            fontSize = 12.sp
                        )
                    }

                    Button(
                        onClick = onClear,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.accent,
                            contentColor = colors.background
                        ),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, colors.borderGray),
                        modifier = Modifier.weight(1f).height(36.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                    ) {
                        Text(
                            text = "Очистить",
                            color = colors.background,
                            fontSize = 12.sp
                        )
                    }

                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.accent,
                            contentColor = colors.background
                        ),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, colors.borderGray),
                        modifier = Modifier.weight(1f).height(36.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                    ) {
                        Text(
                            text = "Закрыть",
                            color = colors.background,
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
    viewModel: MainViewModel,
    colors: AppColors
) {
    AlertDialog(
        onDismissRequest = {
            viewModel.abortLocal()
            onDismiss()
        },
        title = { Text("🛡️ Руководство пользователя", style = MaterialTheme.typography.titleLarge, color = colors.text) },
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
                    color = colors.text,
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
                colors = ButtonDefaults.buttonColors(containerColor = colors.accent)
            ) {
                Text("Понятно", color = colors.background)
            }
        }
    )
}

@Composable
private fun MemoryEditorDialog(
    initialText: String,
    onSave: (String) -> Unit,
    onDismiss: () -> Unit,
    colors: AppColors
) {
    var text by remember { mutableStateOf(initialText) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("🧠 База Знаний ИИ", style = MaterialTheme.typography.titleLarge, color = colors.text) },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                placeholder = { Text("Вставь сюда свой прайс-лист или данные...", color = colors.text.copy(alpha = 0.5f), fontSize = 10.sp) },
                modifier = Modifier.fillMaxWidth().height(400.dp),
                maxLines = 100,
                singleLine = false,
                textStyle = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp, color = colors.text),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = colors.text,
                    unfocusedTextColor = colors.text,
                    focusedContainerColor = colors.surfaceGray,
                    unfocusedContainerColor = colors.surfaceGray,
                    focusedBorderColor = colors.accent,
                    unfocusedBorderColor = colors.borderGray,
                    cursorColor = colors.accent
                )
            )
        },
        confirmButton = {
            Button(onClick = { onSave(text); onDismiss() }, colors = ButtonDefaults.buttonColors(containerColor = colors.accent)) {
                Text("Сохранить", color = colors.background)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Закрыть", color = colors.text) }
        }
    )
}

@Composable
private fun BrainEditorDialog(
    initialText: String,
    onSave: (String) -> Unit,
    onDismiss: () -> Unit,
    colors: AppColors,
    isDarkTheme: Boolean
) {
    var text by remember { mutableStateOf(initialText) }

    MaterialTheme(
        colorScheme = if (isDarkTheme) darkColorScheme() else lightColorScheme()
    ) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("🧠 Brain.txt — Долговременная память", style = MaterialTheme.typography.titleLarge, color = colors.text) },
            text = {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    placeholder = { Text("Здесь хранится сжатая история разговоров...", color = colors.text.copy(alpha = 0.5f), fontSize = 10.sp) },
                    modifier = Modifier.fillMaxWidth().height(400.dp),
                    maxLines = 100,
                    singleLine = false,
                    textStyle = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp, color = colors.text),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = colors.text,
                        unfocusedTextColor = colors.text,
                        focusedContainerColor = colors.surfaceGray,
                        unfocusedContainerColor = colors.surfaceGray,
                        focusedBorderColor = colors.accent,
                        unfocusedBorderColor = colors.borderGray,
                        cursorColor = colors.accent
                    )
                )
            },
            confirmButton = {
                Button(onClick = { onSave(text); onDismiss() }, colors = ButtonDefaults.buttonColors(containerColor = colors.accent)) {
                    Text("Сохранить", color = colors.background)
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) { Text("Закрыть", color = colors.text) }
            }
        )
    }
}

@Composable
private fun ImagePreview(imagePath: String, colors: AppColors) {
    Card(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surfaceGray)
    ) {
        Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("[Изображение]", style = MaterialTheme.typography.bodySmall, color = colors.text)
        }
    }
}

@Composable
private fun StatusBar(
    state: GenerationState,
    cloudState: CloudAIState,
    currentMode: AIMode,
    currentModel: String?,
    modifier: Modifier = Modifier,
    colors: AppColors
) {
    val (containerColor, statusText, showProgress) = when (currentMode) {
        AIMode.CLOUD -> {
            when (cloudState) {
                is CloudAIState.Idle -> Triple(
                    colors.surfaceGray,
                    "☁️ Облако: Готово к работе",
                    false
                )
                is CloudAIState.Ready -> Triple(
                    colors.surfaceGray,
                    "☁️ Облако: Готов (${cloudState.modelId})",
                    false
                )
                is CloudAIState.Generating -> Triple(
                    colors.accent.copy(alpha = 0.15f),
                    if (cloudState.tokensGenerated == 0) "☁️ Облако: Думает..." else "☁️ Облако: ${cloudState.tokensGenerated} т.",
                    true
                )
                is CloudAIState.Completed -> Triple(
                    colors.accent.copy(alpha = 0.15f),
                    "☁️ Облако: ${cloudState.tokenCount} т. ${cloudState.durationMs}мс",
                    false
                )
                is CloudAIState.Error -> Triple(
                    colors.accent.copy(alpha = 0.15f),
                    "⚠️ Облако: ${cloudState.message}",
                    false
                )
            }
        }
        else -> {
            when (state) {
                is GenerationState.Idle -> Triple(
                    colors.surfaceGray,
                    if (currentModel == null) "🤖 Локальный ИИ: выгружен из памяти" else "🤖 Локальный ИИ: Готов к работе",
                    false
                )
                is GenerationState.LoadingModel -> Triple(
                    colors.borderGray.copy(alpha = 0.3f),
                    "⏳ Загрузка модели...",
                    true
                )
                is GenerationState.ModelLoaded -> Triple(
                    colors.surfaceGray,
                    run {
                        val modelName = (currentModel?.substringAfterLast("/") ?: "нейросеть")
                            .replace("primary%3AModels%", "")
                        if (currentModel == null) "🤖 Локальный ИИ: выгружен из памяти" else "🤖 Модель $modelName успешно загружена"
                    },
                    false
                )
                is GenerationState.AnalyzingImage -> Triple(
                    colors.accent.copy(alpha = 0.15f),
                    "🧐 Анализ...",
                    true
                )
                is GenerationState.Generating -> Triple(
                    colors.accent.copy(alpha = 0.15f),
                    if (state.tokensGenerated == 0) "🤖 Локальный ИИ: Думает..." else "🤖 Локальный ИИ: ${state.tokensGenerated} т.",
                    true
                )
                is GenerationState.Completed -> Triple(
                    colors.accent.copy(alpha = 0.15f),
                    "✅ ${state.tokenCount} т. ${state.durationMs}мс",
                    false
                )
                is GenerationState.Error -> Triple(
                    colors.accent.copy(alpha = 0.15f),
                    "⚠️ Ошибка: ${state.message}",
                    false
                )
            }
        }
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(1.dp, colors.borderGray)
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
                    color = colors.accent,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = statusText,
                color = colors.text,
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
    onDismiss: () -> Unit,
    colors: AppColors,
    isDarkTheme: Boolean
) {
    val context = LocalContext.current

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, colors.borderGray, RoundedCornerShape(16.dp))
        ) {
            AndroidView(
                factory = { matrixContext ->
                    MatrixChatBackground(matrixContext)
                },
                modifier = Modifier
                    .matchParentSize()
                    .clip(RoundedCornerShape(16.dp))
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(if (isDarkTheme) Color(0xFF1E1E1E).copy(alpha = 0.95f) else colors.surfaceGray)
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
                        color = colors.accent,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Языковая модель", color = colors.text, fontSize = 14.sp)
                    val displayModelPath = currentModelPath?.substringAfterLast("/")?.replace("primary%3AModels%", "") ?: "Не выбрана"
                    Text(
                        text = "Текущая модель: $displayModelPath",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.text.copy(alpha = 0.7f),
                        fontFamily = colors.chatFont
                    )
                    Button(
                        onClick = onPickModel,
                        modifier = Modifier.fillMaxWidth(0.7f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.accent,
                            contentColor = colors.background
                        ),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, colors.borderGray)
                    ) {
                        Text(
                            text = if (currentModelPath != null) "Изменить модель" else "Выбрать модель",
                            color = colors.background,
                            fontSize = 13.sp
                        )
                    }
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Мультимодальный проектор", color = colors.text, fontSize = 14.sp)
                    val displayMmprojPath = mmprojPath?.substringAfterLast("/")?.replace("primary%3AModels%", "") ?: "Не выбран"
                    Text(
                        text = "Текущий проектор: $displayMmprojPath",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.text.copy(alpha = 0.7f),
                        fontFamily = colors.chatFont
                    )
                    Button(
                        onClick = onPickMmproj,
                        modifier = Modifier.fillMaxWidth(0.7f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.accent,
                            contentColor = colors.background
                        ),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, colors.borderGray)
                    ) {
                        Text(
                            text = if (mmprojPath != null) "Изменить проектор" else "Выбрать проектор",
                            color = colors.background,
                            fontSize = 13.sp
                        )
                    }
                }

                Button(
                    onClick = onLoad,
                    enabled = currentModelPath != null,
                    modifier = Modifier.fillMaxWidth(0.7f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (currentModelPath != null) Color(0xFF4CAF50) else colors.borderGray,
                        contentColor = if (currentModelPath != null) Color.White else colors.text.copy(alpha = 0.5f),
                        disabledContainerColor = colors.borderGray,
                        disabledContentColor = colors.text.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, colors.borderGray)
                ) {
                    Text("Запустить нейросеть", color = if (currentModelPath != null) Color.White else colors.text.copy(alpha = 0.5f), fontSize = 13.sp)
                }

                Button(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://huggingface.co/AnkitAI/Parable-Granite-4.1-3B-Claude-Fable-5-GGUF/resolve/main/Parable-Granite-4.1-3B-Claude-Fable-5-GGUF-Q6_K.gguf"))
                        context.startActivity(intent)
                    },
                    modifier = Modifier.fillMaxWidth(0.7f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.accent,
                        contentColor = colors.background
                    ),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, colors.borderGray)
                ) {
                    Text("⬇ Скачать модель", color = colors.background, fontSize = 13.sp)
                }

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(0.7f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.accent,
                        contentColor = colors.background
                    ),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, colors.borderGray)
                ) {
                    Text("Отмена", color = colors.background, fontSize = 13.sp)
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
    modifier: Modifier = Modifier,
    colors: AppColors,
    isDarkTheme: Boolean
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

    val waveColor = if (currentMode == AIMode.CLOUD) Color(0xFF00B4D8) else colors.green

    val textColor = when {
        !isBound -> Color.Red
        remainingTimeText.contains("🔴") -> Color.Red
        remainingTimeText.contains("⏳") -> Color(0xFFFFA500)
        else -> colors.green
    }

    Card(
        modifier = modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, colors.borderGray),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = if (!isDarkTheme) {
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFFFFFDF5),
                                Color(0xFFFFF8DC),
                                Color(0xFFF0E0B8)
                            )
                        )
                                        } else {
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF3A3A2E),
                                Color(0xFF2A2A1E),
                                Color(0xFF1A1A10)
                            )
                        )
                    },
                    shape = RoundedCornerShape(24.dp)
                )
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
                        fontFamily = colors.chatFont,
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
                            .background(colors.surfaceGray, shape = CircleShape)
                            .border(1.dp, colors.borderGray, CircleShape),
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
                                tint = if (enabled && !isGenerating && !isSpeaking) colors.accent else colors.text.copy(alpha = 0.4f)
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(41.dp)
                            .background(colors.surfaceGray, shape = CircleShape)
                            .border(1.dp, colors.borderGray, CircleShape),
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
                                tint = colors.accent
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
                    placeholder = { Text("Введите запрос...", color = colors.text.copy(alpha = 0.5f)) },
                    maxLines = 3,
                    singleLine = false,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = colors.text,
                        unfocusedTextColor = colors.text,
                        focusedContainerColor = colors.background,
                        unfocusedContainerColor = colors.background,
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        cursorColor = colors.accent
                    )
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(41.dp)
                            .background(colors.surfaceGray, shape = CircleShape)
                            .border(1.dp, colors.borderGray, CircleShape),
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
                                tint = if (isTtsReady) colors.accent else colors.text.copy(alpha = 0.4f)
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(41.dp)
                            .background(colors.surfaceGray, shape = CircleShape)
                            .border(1.dp, colors.borderGray, CircleShape),
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
                                    tint = colors.accent
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
                                    tint = if (enabled) colors.accent else colors.text.copy(alpha = 0.4f)
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
                    if (totalGb > 0f && (usedGb / totalGb) > 0.85f) Color.Red else colors.green
                }
                else -> colors.green
            }

            Text(
                text = memoryInfoText,
                color = memoryColor,
                fontSize = 8.sp,
                fontFamily = colors.chatFont,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 4.dp, end = 4.dp, bottom = 2.dp),
                textAlign = TextAlign.Center
            )
        }
    }
}
