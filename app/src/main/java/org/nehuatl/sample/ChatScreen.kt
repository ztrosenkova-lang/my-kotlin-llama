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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
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
        // Сигнал для махания рукой — устанавливается, когда нужно помахать
    var waveSignal by remember { mutableStateOf(false) }
        // Сигнал для полёта в центр экрана и увеличения
    var growBigSignal by remember { mutableStateOf(false) }
    // Сигнал для полёта в левый верхний угол и уменьшения
    var shrinkSmallSignal by remember { mutableStateOf(false) }

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
                                               command == "привет" || command == "махни рукой" -> {
                            waveSignal = true
                        }
                        command == "стань большим" -> {
                            growBigSignal = true
                        }
                        command == "стань маленьким" -> {
                            shrinkSmallSignal = true
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

        // При приветствии робота (когда он говорит "Привет друг...") — махать
    LaunchedEffect(speakStartTrigger) {
        if (speakStartTrigger) {
            waveSignal = true
            delay(2500)
            waveSignal = false
        } else {
            waveSignal = false
        }
    }

           // При появлении слова "привет" или "махни рукой" в чате — махать
    LaunchedEffect(chatMessages.size) {
        val last = chatMessages.lastOrNull() ?: return@LaunchedEffect
        val text = last.text.lowercase()
        if (text.contains("привет") || text.contains("махни рукой")) {
            waveSignal = true
            delay(2500)
            waveSignal = false
        }
    }
        // Автосброс waveSignal на случай застревания (страховка)
    LaunchedEffect(waveSignal) {
        if (waveSignal) {
            delay(3000)
            waveSignal = false
        }
    }
        // Команда "стань большим" — плавный полёт в центр и увеличение до 3f
    LaunchedEffect(growBigSignal) {
        if (!growBigSignal) return@LaunchedEffect
        if (robotIsLanded && robotScale >= 3f) {
            // Уже большой в центре — ничего не делать
            growBigSignal = false
            return@LaunchedEffect
        }
        // Если робот на орбите или в полёте — сначала дождёмся посадки
        if (!robotIsLanded) {
            robotIsFlyingHere = true
            robotOnOrbit = false
            robotIsFlyingHome = false
            delay(3200)
        }
        // Плавный полёт к центру и увеличение
        robotIsLanded = true
        robotOnOrbit = false
        robotIsFlyingHere = false
        robotIsFlyingHome = false
        growBigSignal = false
    }

    // Команда "стань маленьким" — плавный полёт в левый верхний угол и уменьшение до 0.5f
    LaunchedEffect(shrinkSmallSignal) {
        if (!shrinkSmallSignal) return@LaunchedEffect
        if (robotIsLanded && robotScale <= 0.5f && robotOffsetX == 0f && robotOffsetY == 0f) {
            // Уже маленький в углу — ничего не делать
            shrinkSmallSignal = false
            return@LaunchedEffect
        }
        // Если робот на орбите — сначала летит на экран, потом в угол
        if (!robotIsLanded) {
            robotIsFlyingHere = true
            robotOnOrbit = false
            robotIsFlyingHome = false
            delay(3200)
        }
        robotIsLanded = true
        robotOnOrbit = false
        robotIsFlyingHere = false
        robotIsFlyingHome = false
        shrinkSmallSignal = false
    }
    
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
                                                command == "привет" || command == "махни рукой" -> {
                            waveSignal = true
                            promptInput = ""
                        }
                        command == "стань большим" -> {
                            growBigSignal = true
                            promptInput = ""
                        }
                        command == "стань маленьким" -> {
                            shrinkSmallSignal = true
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
                        shouldWave = waveSignal,
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
                        shouldWave = waveSignal,
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

                // Сигналы команд "стань большим" / "стань маленьким" имеют приоритет
                val isCommandActive = growBigSignal || shrinkSmallSignal

                // Целевые позиция и масштаб
                val targetOffsetX = when {
                    growBigSignal -> (screenWidthPx - robotSizePx * 3f) / 2f
                    shrinkSmallSignal -> 0f
                    else -> robotOffsetX
                }
                val targetOffsetY = when {
                    growBigSignal -> (screenHeightPx - robotSizePx * 3f) / 2f
                    shrinkSmallSignal -> 0f
                    else -> robotOffsetY
                }
                val targetScale = when {
                    growBigSignal -> 3f
                    shrinkSmallSignal -> 0.5f
                    else -> robotScale
                }

                // Плавная анимация
                val animatedOffsetX by animateFloatAsState(
                    targetValue = targetOffsetX,
                    animationSpec = tween(
                        durationMillis = if (isCommandActive) 3000 else 200,
                        easing = if (isCommandActive) FastOutSlowInEasing else LinearOutSlowInEasing
                    ),
                    label = "animated_offset_x"
                )
                val animatedOffsetY by animateFloatAsState(
                    targetValue = targetOffsetY,
                    animationSpec = tween(
                        durationMillis = if (isCommandActive) 3000 else 200,
                        easing = if (isCommandActive) FastOutSlowInEasing else LinearOutSlowInEasing
                    ),
                    label = "animated_offset_y"
                )
                val animatedScale by animateFloatAsState(
                    targetValue = targetScale,
                    animationSpec = tween(
                        durationMillis = if (isCommandActive) 3000 else 200,
                        easing = if (isCommandActive) FastOutSlowInEasing else LinearOutSlowInEasing
                    ),
                    label = "animated_scale"
                )

                Box(
                    modifier = Modifier
                        .offset(x = animatedOffsetX.dp, y = animatedOffsetY.dp)
                        .size(70.dp)
                        .graphicsLayer(
                            scaleX = animatedScale,
                            scaleY = animatedScale
                        )
                        .pointerInput(Unit) {
                            detectTransformGestures { _, pan, zoom, _ ->
                                // Позиция — мгновенно при перетаскивании
                                robotOffsetX = (robotOffsetX + pan.x).coerceIn(0f, screenWidthPx - robotSizePx * robotScale)
                                robotOffsetY = (robotOffsetY + pan.y).coerceIn(0f, screenHeightPx - robotSizePx * robotScale)
                                // Масштаб — тоже мгновенно меняется в robotScale, но отображается плавно через animatedScale
                                robotScale = (robotScale * zoom).coerceIn(0.5f, 3f)
                            }
                        }
                ) {
                    ThinkingRobotAnimation(
                        height = 70.dp,
                        isActive = true,
                        isSpeaking = isSpeaking,
                        isThinking = state is GenerationState.Generating || cloudState is CloudAIState.Generating,
                        isIdle = !isSpeaking && state !is GenerationState.Generating && cloudState !is CloudAIState.Generating,
                        shouldWave = waveSignal,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // Синхронизация robotOffsetX/Y и robotScale с анимированными значениями
                LaunchedEffect(animatedOffsetX, animatedOffsetY, animatedScale, isCommandActive) {
                    if (!isCommandActive) {
                        robotOffsetX = animatedOffsetX
                        robotOffsetY = animatedOffsetY
                        robotScale = animatedScale
                    }
                }

                // Завершение команд
                LaunchedEffect(growBigSignal) {
                    if (growBigSignal) {
                        delay(3000)
                        robotOffsetX = (screenWidthPx - robotSizePx * 3f) / 2f
                        robotOffsetY = (screenHeightPx - robotSizePx * 3f) / 2f
                        robotScale = 3f
                        growBigSignal = false
                    }
                }
                                LaunchedEffect(shrinkSmallSignal) {
                    if (shrinkSmallSignal) {
                        delay(3000)
                        robotOffsetX = 0f
                        robotOffsetY = 0f
                        robotScale = 0.5f
                        shrinkSmallSignal = false
                    }
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
    isIdle: Boolean = false,
    shouldWave: Boolean = false,
    commandScale: Float? = null
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
                durationMillis = 6000
                1f at 0
                1f at 4500
                0.05f at 4700
                0.05f at 5600
                1f at 5800
                1f at 6000
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
           // Цикл спокойного состояния глаз: Обычное -> Любопытство -> Сканирование -> Обычное
    val idleEyePhase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "idleEyePhase"
    )

       val armPhase by transition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "armPhase"
    )

            // Внутренний сигнал от ChatScreen — включается при приветствии
    var externalWave by remember { mutableStateOf(false) }
    LaunchedEffect(shouldWave) {
        if (shouldWave) {
            externalWave = true
            delay(2500)
            externalWave = false
        }
    }

    // Автоматическое махание через случайные промежутки 1–4 минуты
    // (когда нет внешнего сигнала)
    var randomWave by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        while (true) {
            // Случайный интервал от 60 до 240 секунд
            val nextDelay = (60..240).random() * 1000L
            delay(nextDelay)
            randomWave = true
            delay(2000)
            randomWave = false
        }
    }

    // Объединённое состояние махания
    val isWaving = externalWave || randomWave

    // Плавный вход/выход из режима махания (0 = не машет, 1 = машет)
    val waveAmount by animateFloatAsState(
        targetValue = if (isWaving) 1f else 0f,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "wave_amount"
    )

             val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current

    // Плавное изменение масштаба при командах "стань большим" / "стань маленьким"
    val animatedScale by animateFloatAsState(
        targetValue = commandScale ?: 1f,
        animationSpec = tween(durationMillis = 3000, easing = FastOutSlowInEasing),
        label = "command_scale"
    )

        Canvas(
        modifier = modifier
            .height(height)
            .width(height * 0.85f)
            .graphicsLayer(
                scaleX = if (commandScale != null) animatedScale else 1f,
                scaleY = if (commandScale != null) animatedScale else 1f
            )
    ) {
        val u = size.height / 200f
        val cx = size.width / 2f
        val bobOffset = if (isActive) sin(bob) * 10f * u else 0f
        val lookOffsetX = lookX * 1f * u
        val lookOffsetY = lookY * 0.8f * u
        val currentBlink = if (isActive) blink else 1f

        // ================= ПАЛИТРА =================
        val whiteBody = Color(0xFFF4F6F8)
        val whiteHighlight = Color(0xFFFFFFFF)
        val lightGray = Color(0xFFD9DEE3)
        val mediumGray = Color(0xFFA8AFB6)
        val darkGray = Color(0xFF4A525A)
        val darkerGray = Color(0xFF2C3238)
        val visorDark = Color(0xFF1A1A2E)
        val visorGlass = Color(0xFF22303C)
        val neonBlue = Color(0xFF00D9FF)
        val neonBlueGlow = Color(0xFF80EFFF)

        val neonBluePulse = if (isThinking) {
            neonBlue.copy(alpha = 0.7f + sin(pulse) * 0.3f)
        } else {
            neonBlue
        }

        fun pt(x: Float, y: Float) = Offset(cx + x * u, y * u + bobOffset)

                       // ================= СОПЛО (подогнано под новый корпус) =================
        // Верх: ширина 44 (-22..22), низ: ширина 44.8 (-22.4..22.4) — почти цилиндр
        val nozzlePath = Path().apply {
            moveTo(pt(-22f, 160f).x, pt(-22f, 160f).y)
            lineTo(pt(22f, 160f).x, pt(22f, 160f).y)
            lineTo(pt(24f, 180f).x, pt(24f, 180f).y)
            lineTo(pt(-24f, 180f).x, pt(-24f, 180f).y)
            close()
        }

        // Заливка сопла
        drawPath(
            nozzlePath,
            brush = Brush.verticalGradient(
                colors = listOf(mediumGray, darkGray, darkerGray),
                startY = pt(0f, 160f).y,
                endY = pt(0f, 180f).y
            )
        )
        drawPath(nozzlePath, color = darkerGray, style = Stroke(width = 1.4f * u))

        // Горизонтальные линии-решётки на сопле
        drawLine(darkerGray, pt(-22.5f, 165f), pt(22.5f, 165f), strokeWidth = 0.8f * u)
        drawLine(darkerGray, pt(-23f, 170f), pt(23f, 170f), strokeWidth = 0.8f * u)
        drawLine(darkerGray, pt(-23.5f, 175f), pt(23.5f, 175f), strokeWidth = 0.8f * u)

        // Блик слева на сопле
        drawRoundRect(
            color = whiteHighlight.copy(alpha = 0.5f),
            topLeft = pt(-20f, 163f),
            size = Size(3f * u, 14f * u),
            cornerRadius = CornerRadius(1.5f * u)
        )

        // Верхний ободок сопла
        drawRoundRect(
            color = mediumGray,
            topLeft = pt(-23f, 159f),
            size = Size(46f * u, 3f * u),
            cornerRadius = CornerRadius(1.5f * u)
        )
        drawRoundRect(
            color = darkerGray,
            topLeft = pt(-23f, 159f),
            size = Size(46f * u, 3f * u),
            cornerRadius = CornerRadius(1.5f * u),
            style = Stroke(width = 1f * u)
        )

        // Нижний ободок сопла (выход пламени)
        drawRoundRect(
            color = darkerGray,
            topLeft = pt(-25f, 179f),
            size = Size(50f * u, 3f * u),
            cornerRadius = CornerRadius(1.5f * u)
        )
        drawRoundRect(
            color = Color(0xFF0A0A14),
            topLeft = pt(-25f, 179f),
            size = Size(50f * u, 3f * u),
            cornerRadius = CornerRadius(1.5f * u),
            style = Stroke(width = 1f * u)
        )

        // ================= ПЛАМЯ РАКЕТНОГО ДВИГАТЕЛЯ (подогнано) =================
        // Три слоя пламени + искры + свечение + дымка

        val flameFlicker = sin(flamePhase * 2f) * 1.8f
        val flameFlickerX = sin(flamePhase * 3.3f) * 1.2f
        val flamePulse = 0.7f + 0.3f * sin(flamePhase * 5f)

        // ===== СЛОЙ 0. СВЕЧЕНИЕ ВОКРУГ ПЛАМЕНИ =====
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFF0066FF).copy(alpha = 0.35f * flamePulse),
                    Color(0xFF00AAFF).copy(alpha = 0.15f * flamePulse),
                    Color.Transparent
                ),
                center = pt(0f, 200f).copy(x = pt(0f, 200f).x + flameFlickerX * u),
                radius = 26f * u
            ),
            radius = 26f * u,
            center = pt(0f, 200f).copy(x = pt(0f, 200f).x + flameFlickerX * u)
        )

        // ===== СЛОЙ 1. ВНЕШНЕЕ ПЛАМЯ (красно-оранжевое) =====
        val outerFlamePath = Path().apply {
            moveTo(pt(-18f, 182f).x, pt(-18f, 182f).y)
            quadraticBezierTo(
                pt(-14f + flameFlickerX, 200f + flameFlicker).x,
                pt(-14f + flameFlickerX, 200f + flameFlicker).y,
                pt(-5f + flameFlickerX * 0.5f, 212f + flameFlicker).x,
                pt(-5f + flameFlickerX * 0.5f, 212f + flameFlicker).y
            )
            quadraticBezierTo(
                pt(0f, 222f + flameFlicker * 1.2f).x, pt(0f, 222f + flameFlicker * 1.2f).y,
                pt(5f + flameFlickerX * 0.5f, 212f + flameFlicker).x,
                pt(5f + flameFlickerX * 0.5f, 212f + flameFlicker).y
            )
            quadraticBezierTo(
                pt(14f + flameFlickerX, 200f + flameFlicker).x,
                pt(14f + flameFlickerX, 200f + flameFlicker).y,
                pt(18f, 182f).x, pt(18f, 182f).y
            )
        }
        drawPath(
            outerFlamePath,
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFFFF3300),
                    Color(0xFFFF4500),
                    Color(0xFFFF6600),
                    Color(0xFFFF8800).copy(alpha = 0.8f)
                ),
                startY = pt(0f, 182f).y,
                endY = pt(0f, 220f + flameFlicker).y
            )
        )

        // ===== СЛОЙ 2. СРЕДНЕЕ ПЛАМЯ (оранжево-жёлтое) =====
        val middleFlamePath = Path().apply {
            moveTo(pt(-12f, 182f).x, pt(-12f, 182f).y)
            quadraticBezierTo(
                pt(-10f + flameFlickerX * 0.7f, 197f + flameFlicker * 0.9f).x,
                pt(-10f + flameFlickerX * 0.7f, 197f + flameFlicker * 0.9f).y,
                pt(-3f + flameFlickerX * 0.3f, 206f + flameFlicker * 0.9f).x,
                pt(-3f + flameFlickerX * 0.3f, 206f + flameFlicker * 0.9f).y
            )
            quadraticBezierTo(
                pt(0f, 213f + flameFlicker * 1.0f).x, pt(0f, 213f + flameFlicker * 1.0f).y,
                pt(3f + flameFlickerX * 0.3f, 206f + flameFlicker * 0.9f).x,
                pt(3f + flameFlickerX * 0.3f, 206f + flameFlicker * 0.9f).y
            )
            quadraticBezierTo(
                pt(10f + flameFlickerX * 0.7f, 197f + flameFlicker * 0.9f).x,
                pt(10f + flameFlickerX * 0.7f, 197f + flameFlicker * 0.9f).y,
                pt(12f, 182f).x, pt(12f, 182f).y
            )
        }
        drawPath(
            middleFlamePath,
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFFFF8800),
                    Color(0xFFFFAA00),
                    Color(0xFFFFCC00),
                    Color(0xFFFFE066).copy(alpha = 0.9f)
                ),
                startY = pt(0f, 182f).y,
                endY = pt(0f, 212f + flameFlicker).y
            )
        )

        // ===== СЛОЙ 3. ВНУТРЕННЕЕ БЕЛО-ЖЁЛТОЕ ЯДРО =====
        val innerFlamePath = Path().apply {
            moveTo(pt(-6f, 182f).x, pt(-6f, 182f).y)
            quadraticBezierTo(
                pt(-5f + flameFlickerX * 0.4f, 192f + flameFlicker * 0.7f).x,
                pt(-5f + flameFlickerX * 0.4f, 192f + flameFlicker * 0.7f).y,
                pt(-2f + flameFlickerX * 0.2f, 198f + flameFlicker * 0.7f).x,
                pt(-2f + flameFlickerX * 0.2f, 198f + flameFlicker * 0.7f).y
            )
            quadraticBezierTo(
                pt(0f, 203f + flameFlicker * 0.8f).x, pt(0f, 203f + flameFlicker * 0.8f).y,
                pt(2f + flameFlickerX * 0.2f, 198f + flameFlicker * 0.7f).x,
                pt(2f + flameFlickerX * 0.2f, 198f + flameFlicker * 0.7f).y
            )
            quadraticBezierTo(
                pt(5f + flameFlickerX * 0.4f, 192f + flameFlicker * 0.7f).x,
                pt(5f + flameFlickerX * 0.4f, 192f + flameFlicker * 0.7f).y,
                pt(6f, 182f).x, pt(6f, 182f).y
            )
        }
        drawPath(
            innerFlamePath,
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color.White,
                    Color(0xFFFFF5B0),
                    Color(0xFFFFE066),
                    Color(0xFFFFCC00).copy(alpha = 0.8f)
                ),
                startY = pt(0f, 182f).y,
                endY = pt(0f, 200f + flameFlicker * 0.7f).y
            )
        )

        // ===== СЛОЙ 4. ЯРКОЕ СВЕЧЕНИЕ В ЦЕНТРЕ =====
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.9f * flamePulse),
                    Color(0xFFFFF5B0).copy(alpha = 0.5f * flamePulse),
                    Color.Transparent
                ),
                center = pt(0f, 190f),
                radius = 7f * u
            ),
            radius = 7f * u,
            center = pt(0f, 190f)
        )

        // ===== СЛОЙ 5. ИСКРЫ, ЛЕТЯЩИЕ ВНИЗ =====
        val sparkCount = 8
        for (i in 0 until sparkCount) {
            val sparkPhase = (flamePhase * 2f + i * 0.7f) % (2f * PI.toFloat())
            val sparkProgress = sparkPhase / (2f * PI.toFloat())

            val sparkY = 182f + sparkProgress * 45f
            val sparkX = -10f + i * 2.8f + sin(sparkPhase * 3f) * 2f

            val sparkAlpha = (1f - sparkProgress) * 0.9f
            val sparkR = (1.1f - sparkProgress * 0.75f) * u

            if (sparkAlpha > 0.05f && sparkR > 0f) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFFFDD00).copy(alpha = sparkAlpha * 0.7f),
                            Color(0xFFFF8800).copy(alpha = sparkAlpha * 0.4f),
                            Color.Transparent
                        ),
                        center = pt(sparkX, sparkY),
                        radius = sparkR * 3f
                    ),
                    radius = sparkR * 3f,
                    center = pt(sparkX, sparkY)
                )
                drawCircle(
                    color = Color(0xFFFFEE88).copy(alpha = sparkAlpha),
                    radius = sparkR,
                    center = pt(sparkX, sparkY)
                )
                drawCircle(
                    color = Color.White.copy(alpha = sparkAlpha * 0.9f),
                    radius = sparkR * 0.5f,
                    center = pt(sparkX, sparkY)
                )
            }
        }

        // ===== СЛОЙ 6. ТЁМНАЯ ДЫМКА ВОКРУГ КОНЧИКА =====
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFF333333).copy(alpha = 0.2f),
                    Color(0xFF555555).copy(alpha = 0.1f),
                    Color.Transparent
                ),
                center = pt(0f, 215f + flameFlicker),
                radius = 12f * u
            ),
            radius = 12f * u,
            center = pt(0f, 215f + flameFlicker)
        )

                                                    // ================= АНИМАЦИЯ РУК =================
        val armSway = sin(armPhase)
        val leftArmOffsetY = when {
            isSpeaking -> armSway * 1.26f
            isThinking -> armSway * 1.26f
            isIdle -> armSway * 1.26f
            else -> 0f
        }
        val rightArmOffsetY = when {
            isSpeaking -> sin(armPhase + PI.toFloat()) * 1.26f
            isThinking -> sin(armPhase + PI.toFloat()) * 1.26f
            isIdle -> sin(armPhase + PI.toFloat()) * 1.26f
            else -> 0f
        }
                // Углы приветствия — плавно появляются при waveAmount → 1
        // Верхнее звено: поворот вокруг плеча на -60°
        val shoulderWaveAngle = -60f * waveAmount
        // Нижнее звено: поворот вокруг локтя на -50°
        val forearmWaveAngle = -50f * waveAmount
        // Махание кисти: колебание ±18°, частота 1 Гц
        val handWiggle = sin(armPhase * 2f) * 18f * waveAmount

        // ================= РУКИ (опущены вниз по бокам) =================
        // Предплечье в 2 раза толще бицепса. Руки укорочены на 20%.
        // Все части выровнены по центральной оси. Левая ось: X = -46f. Правая ось: X = 46f.

        // ЛЕВАЯ РУКА
        // Плечо (круглый сустав)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(whiteHighlight, lightGray),
                radius = 13f * u,
                center = pt(-46f, 96f + leftArmOffsetY)
            ),
            radius = 13f * u,
            center = pt(-46f, 96f + leftArmOffsetY)
        )
        drawCircle(
            color = darkGray,
            radius = 13f * u,
            center = pt(-46f, 96f + leftArmOffsetY),
            style = Stroke(width = 1.3f * u)
        )
        drawCircle(
            color = mediumGray,
            radius = 4.5f * u,
            center = pt(-46f, 96f + leftArmOffsetY)
        )

        // Бицепс — ширина 16, скругление 4, высота 21
        drawRoundRect(
            color = lightGray,
            topLeft = pt(-54f, 104f + leftArmOffsetY),
            size = Size(16f * u, 21f * u),
            cornerRadius = CornerRadius(4f * u)
        )
        drawRoundRect(
            color = darkGray,
            topLeft = pt(-54f, 104f + leftArmOffsetY),
            size = Size(16f * u, 21f * u),
            cornerRadius = CornerRadius(4f * u),
            style = Stroke(width = 1.3f * u)
        )
        // Блик на бицепсе
        drawRoundRect(
            color = whiteHighlight.copy(alpha = 0.8f),
            topLeft = pt(-52f, 108f + leftArmOffsetY),
            size = Size(3f * u, 14f * u),
            cornerRadius = CornerRadius(1.5f * u)
        )

        // ЛОКОТЬ
        drawCircle(
            color = mediumGray,
            radius = 7f * u,
            center = pt(-46f, 126f + leftArmOffsetY)
        )
        drawCircle(
            color = darkGray,
            radius = 7f * u,
            center = pt(-46f, 126f + leftArmOffsetY),
            style = Stroke(width = 1.3f * u)
        )

        // Предплечье — ширина 25.92 (уменьшено на 10%), высота 24.2 (увеличено на 10%), скругление 7
        drawRoundRect(
            color = lightGray,
            topLeft = pt(-58.96f, 128f + leftArmOffsetY),
            size = Size(25.92f * u, 24.2f * u),
            cornerRadius = CornerRadius(7f * u)
        )
        drawRoundRect(
            color = darkGray,
            topLeft = pt(-58.96f, 128f + leftArmOffsetY),
            size = Size(25.92f * u, 24.2f * u),
            cornerRadius = CornerRadius(7f * u),
            style = Stroke(width = 1.3f * u)
        )
        // Блик на предплечье
        drawRoundRect(
            color = whiteHighlight.copy(alpha = 0.8f),
            topLeft = pt(-56f, 132f + leftArmOffsetY),
            size = Size(3f * u, 15f * u),
            cornerRadius = CornerRadius(1.5f * u)
        )

        // ЛЕВАЯ КИСТЬ
        drawOval(
            color = lightGray,
            topLeft = pt(-54f, 150f + leftArmOffsetY),
            size = Size(16f * u, 20f * u)
        )
        drawOval(
            color = darkGray,
            topLeft = pt(-54f, 150f + leftArmOffsetY),
            size = Size(16f * u, 20f * u),
            style = Stroke(width = 1.3f * u)
        )

        // Пальцы левой кисти
        for (i in 0..3) {
            val fx = -52f + i * 4f
            drawRoundRect(
                color = mediumGray,
                topLeft = pt(fx, 162f + leftArmOffsetY),
                size = Size(3.5f * u, 12f * u),
                cornerRadius = CornerRadius(1.75f * u)
            )
            drawRoundRect(
                color = darkGray,
                topLeft = pt(fx, 162f + leftArmOffsetY),
                size = Size(3.5f * u, 12f * u),
                cornerRadius = CornerRadius(1.75f * u),
                style = Stroke(width = 0.9f * u)
            )
            drawCircle(
                color = darkerGray,
                radius = 0.8f * u,
                center = pt(fx + 1.75f, 167f + leftArmOffsetY)
            )
            drawCircle(
                color = darkerGray,
                radius = 0.8f * u,
                center = pt(fx + 1.75f, 172f + leftArmOffsetY)
            )
        }

        // Большой палец левой кисти
        drawRoundRect(
            color = mediumGray,
            topLeft = pt(-51f, 156f + leftArmOffsetY),
            size = Size(5f * u, 10f * u),
            cornerRadius = CornerRadius(2.5f * u)
        )
        drawRoundRect(
            color = darkGray,
            topLeft = pt(-51f, 156f + leftArmOffsetY),
            size = Size(5f * u, 10f * u),
            cornerRadius = CornerRadius(2.5f * u),
            style = Stroke(width = 0.9f * u)
        )

                        // ================= ПРАВАЯ РУКА (с приветственным маханием) =================
        // Внешний rotate — верхнее звено (плечо + бицепс) вокруг плеча.
        rotate(shoulderWaveAngle, pivot = pt(46f, 96f)) {

            // Плечо
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(whiteHighlight, lightGray),
                    radius = 13f * u,
                    center = pt(46f, 96f + rightArmOffsetY)
                ),
                radius = 13f * u,
                center = pt(46f, 96f + rightArmOffsetY)
            )
            drawCircle(
                color = darkGray,
                radius = 13f * u,
                center = pt(46f, 96f + rightArmOffsetY),
                style = Stroke(width = 1.3f * u)
            )
            drawCircle(
                color = mediumGray,
                radius = 4.5f * u,
                center = pt(46f, 96f + rightArmOffsetY)
            )

            // Бицепс
            drawRoundRect(
                color = lightGray,
                topLeft = pt(38f, 104f + rightArmOffsetY),
                size = Size(16f * u, 21f * u),
                cornerRadius = CornerRadius(4f * u)
            )
            drawRoundRect(
                color = darkGray,
                topLeft = pt(38f, 104f + rightArmOffsetY),
                size = Size(16f * u, 21f * u),
                cornerRadius = CornerRadius(4f * u),
                style = Stroke(width = 1.3f * u)
            )
            drawRoundRect(
                color = whiteHighlight.copy(alpha = 0.8f),
                topLeft = pt(49f, 108f + rightArmOffsetY),
                size = Size(3f * u, 14f * u),
                cornerRadius = CornerRadius(1.5f * u)
            )

            // Локоть
            drawCircle(
                color = mediumGray,
                radius = 7f * u,
                center = pt(46f, 126f + rightArmOffsetY)
            )
            drawCircle(
                color = darkGray,
                radius = 7f * u,
                center = pt(46f, 126f + rightArmOffsetY),
                style = Stroke(width = 1.3f * u)
            )

            // Внутренний rotate — нижнее звено (предплечье + кисть + пальцы) вокруг локтя
            rotate(forearmWaveAngle + handWiggle, pivot = pt(46f, 126f + rightArmOffsetY)) {

                // Предплечье
                drawRoundRect(
                    color = lightGray,
                    topLeft = pt(33.04f, 128f + rightArmOffsetY),
                    size = Size(25.92f * u, 24.2f * u),
                    cornerRadius = CornerRadius(7f * u)
                )
                drawRoundRect(
                    color = darkGray,
                    topLeft = pt(33.04f, 128f + rightArmOffsetY),
                    size = Size(25.92f * u, 24.2f * u),
                    cornerRadius = CornerRadius(7f * u),
                    style = Stroke(width = 1.3f * u)
                )
                drawRoundRect(
                    color = whiteHighlight.copy(alpha = 0.8f),
                    topLeft = pt(53f, 132f + rightArmOffsetY),
                    size = Size(3f * u, 15f * u),
                    cornerRadius = CornerRadius(1.5f * u)
                )

                // Кисть
                drawOval(
                    color = lightGray,
                    topLeft = pt(38f, 150f + rightArmOffsetY),
                    size = Size(16f * u, 20f * u)
                )
                drawOval(
                    color = darkGray,
                    topLeft = pt(38f, 150f + rightArmOffsetY),
                    size = Size(16f * u, 20f * u),
                    style = Stroke(width = 1.3f * u)
                )

                // Пальцы
                for (i in 0..3) {
                    val fx = 40f + i * 4f
                    drawRoundRect(
                        color = mediumGray,
                        topLeft = pt(fx, 162f + rightArmOffsetY),
                        size = Size(3.5f * u, 12f * u),
                        cornerRadius = CornerRadius(1.75f * u)
                    )
                    drawRoundRect(
                        color = darkGray,
                        topLeft = pt(fx, 162f + rightArmOffsetY),
                        size = Size(3.5f * u, 12f * u),
                        cornerRadius = CornerRadius(1.75f * u),
                        style = Stroke(width = 0.9f * u)
                    )
                    drawCircle(
                        color = darkerGray,
                        radius = 0.8f * u,
                        center = pt(fx + 1.75f, 167f + rightArmOffsetY)
                    )
                    drawCircle(
                        color = darkerGray,
                        radius = 0.8f * u,
                        center = pt(fx + 1.75f, 172f + rightArmOffsetY)
                    )
                }

                // Большой палец
                drawRoundRect(
                    color = mediumGray,
                    topLeft = pt(46f, 156f + rightArmOffsetY),
                    size = Size(5f * u, 10f * u),
                    cornerRadius = CornerRadius(2.5f * u)
                )
                drawRoundRect(
                    color = darkGray,
                    topLeft = pt(46f, 156f + rightArmOffsetY),
                    size = Size(5f * u, 10f * u),
                    cornerRadius = CornerRadius(2.5f * u),
                    style = Stroke(width = 0.9f * u)
                )
            }
        }
        
        // ================= ТУЛОВИЩЕ (3D цилиндр) =================
        // Верх: 84 (-42..42), низ сужен на 20% от предыдущего: 67.2 - 20% = 53.76 (-26.88..26.88)
        // Верхний овал виден как крышка 3D-цилиндра. Верхние углы скруглены сильнее.

        // 1. ЗАДНЯЯ ЧАСТЬ ВЕРХНЕГО ОВАЛА (тёмная половина, за головой)
        drawOval(
            color = mediumGray,
            topLeft = pt(-37f, 70f),
            size = Size(74f * u, 14f * u)
        )

        // 2. ОСНОВНОЕ ТЕЛО ЦИЛИНДРА (боковые стенки, сужаются к низу)
        // Верхние углы скруглены радиусом 10 (было 6)
        val cylinderPath = Path().apply {
            // Начало — верхний левый угол (после большего скругления)
            moveTo(pt(-32f, 77f).x, pt(-32f, 77f).y)
            // Скругление верхнего левого угла — больше
            quadraticBezierTo(
                pt(-42f, 77f).x, pt(-42f, 77f).y,
                pt(-42f, 87f).x, pt(-42f, 87f).y
            )
            // Левая боковина вниз (новый низ — уже)
            lineTo(pt(-26.88f, 160f).x, pt(-26.88f, 160f).y)
            // Низ
            lineTo(pt(26.88f, 160f).x, pt(26.88f, 160f).y)
            // Правая боковина вверх
            lineTo(pt(42f, 87f).x, pt(42f, 87f).y)
            // Скругление верхнего правого угла
            quadraticBezierTo(
                pt(42f, 77f).x, pt(42f, 77f).y,
                pt(32f, 77f).x, pt(32f, 77f).y
            )
            close()
        }

                // Заливка корпуса (градиент слева-направо + сверху-вниз)
        drawPath(
            cylinderPath,
            brush = Brush.horizontalGradient(
                colors = listOf(
                    lightGray,
                    whiteHighlight,
                    whiteBody,
                    lightGray,
                    mediumGray
                )
            )
        )
        // Второй слой градиента — вертикальный (затемнение снизу)
        drawPath(
            cylinderPath,
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color.Transparent,
                    Color.Transparent,
                    Color.Black.copy(alpha = 0.1f)
                )
            )
        )
        drawPath(cylinderPath, color = darkGray, style = Stroke(width = 1.4f * u))

        

                        // 5. ТЁМНАЯ ЗОНА СПРАВА (затенение для 3D) — повёрнута параллельно границе
        rotate(11.7f, pivot = pt(34f, 94f)) {
            drawRoundRect(
                color = darkGray.copy(alpha = 0.15f),
                topLeft = pt(34f, 94f),
                size = Size(7f * u, 52f * u),
                cornerRadius = CornerRadius(3.5f * u)
            )
        }
                // ================= РАДИАЛЬНЫЙ ОТСВЕТ ОТ ЭКРАНА НА ГРУДИ =================
        // Мягкое голубое свечение вокруг экрана — как будто экран подсвечивает корпус.
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    neonBlue.copy(alpha = 0.18f),
                    neonBlue.copy(alpha = 0.08f),
                    Color.Transparent
                ),
                center = pt(0f, 108f),
                radius = 38f * u
            ),
            radius = 38f * u,
            center = pt(0f, 108f)
        )

        // 6. ПЕРЕДНЯЯ ЧАСТЬ ВЕРХНЕГО ОВАЛА (светлая половина, крышка цилиндра)
        drawOval(
            color = whiteBody,
            topLeft = pt(-37f, 70f),
            size = Size(74f * u, 14f * u)
        )
        drawOval(
            color = darkGray,
            topLeft = pt(-37f, 70f),
            size = Size(74f * u, 14f * u),
            style = Stroke(width = 1.3f * u)
        )

        // ================= КЛЁПКИ НА КРЫШКЕ ЦИЛИНДРА =================
        // 7 клёпок с равным промежутком по всему периметру крышки.
        // Отступ от края — 3 единицы.
        val capRivetColor = mediumGray
        val capRivetRadius = 1f * u

        drawCircle(
            color = capRivetColor,
            radius = capRivetRadius,
            center = pt(0f, 73f)
        )
        drawCircle(
            color = capRivetColor,
            radius = capRivetRadius,
            center = pt(-26.58f, 74.51f)
        )
        drawCircle(
            color = capRivetColor,
            radius = capRivetRadius,
            center = pt(-33.15f, 77.89f)
        )
        drawCircle(
            color = capRivetColor,
            radius = capRivetRadius,
            center = pt(-14.75f, 80.60f)
        )
        drawCircle(
            color = capRivetColor,
            radius = capRivetRadius,
            center = pt(14.75f, 80.60f)
        )
        drawCircle(
            color = capRivetColor,
            radius = capRivetRadius,
            center = pt(33.15f, 77.89f)
        )
        drawCircle(
            color = capRivetColor,
            radius = capRivetRadius,
            center = pt(26.58f, 74.51f)
        )

        // 7. ВНУТРЕННИЙ ЭЛЛИПС НА КРЫШКЕ (для реализма 3D)
        drawOval(
            color = lightGray,
            topLeft = pt(-27f, 73f),
            size = Size(54f * u, 8f * u)
        )
        drawOval(
            color = mediumGray,
            topLeft = pt(-27f, 73f),
            size = Size(54f * u, 8f * u),
            style = Stroke(width = 0.8f * u)
        )

        // 8. БЛИК НА КРЫШКЕ (овал-отражение)
        drawOval(
            color = whiteHighlight.copy(alpha = 0.75f),
            topLeft = pt(-20f, 73.5f),
            size = Size(28f * u, 4f * u)
        )
                // ================= КЛЁПКИ ПО ПЕРИМЕТРУ ПЕРЕДНЕЙ ПАНЕЛИ КОРПУСА =================
        // Отступ 3 единицы внутрь от края цилиндра. Интервал как на крышке (~20).
        val bodyRivetColor = mediumGray
        val bodyRivetRadius = 1f * u

        // Верхняя граница (5 клёпок)
        drawCircle(color = bodyRivetColor, radius = bodyRivetRadius, center = pt(-39f, 90f))
        drawCircle(color = bodyRivetColor, radius = bodyRivetRadius, center = pt(-19.5f, 90f))
        drawCircle(color = bodyRivetColor, radius = bodyRivetRadius, center = pt(0f, 90f))
        drawCircle(color = bodyRivetColor, radius = bodyRivetRadius, center = pt(19.5f, 90f))
        drawCircle(color = bodyRivetColor, radius = bodyRivetRadius, center = pt(39f, 90f))

        // Правая боковина (3 клёпки)
        drawCircle(color = bodyRivetColor, radius = bodyRivetRadius, center = pt(35.2f, 106.8f))
        drawCircle(color = bodyRivetColor, radius = bodyRivetRadius, center = pt(31.4f, 123.5f))
        drawCircle(color = bodyRivetColor, radius = bodyRivetRadius, center = pt(27.7f, 140.2f))

        // Нижняя граница (2 клёпки)
        drawCircle(color = bodyRivetColor, radius = bodyRivetRadius, center = pt(11.94f, 157f))
        drawCircle(color = bodyRivetColor, radius = bodyRivetRadius, center = pt(-11.94f, 157f))

        // Левая боковина (3 клёпки)
        drawCircle(color = bodyRivetColor, radius = bodyRivetRadius, center = pt(-27.7f, 140.2f))
        drawCircle(color = bodyRivetColor, radius = bodyRivetRadius, center = pt(-31.4f, 123.5f))
        drawCircle(color = bodyRivetColor, radius = bodyRivetRadius, center = pt(-35.2f, 106.8f))

                
        // ================= РЕМЕНЬ-ОБРУЧ НА КОРПУСЕ (делит корпус 75% / 25%) =================
        // Верх корпуса: y = 77f, низ корпуса: y = 160f. Высота = 83.
        // 75% от 83 = 62.25. Линия ремня: y = 77 + 62.25 = 139.25f.
        val beltCenterY = 139.25f
        val beltHeight = 5f * u
        val beltTop = beltCenterY - beltHeight / (2f * u)
        val beltWidth = 60f * u
        val beltLeft = -30f

        // Заливка ремня
        drawRoundRect(
            color = darkerGray,
            topLeft = pt(beltLeft, beltTop),
            size = Size(beltWidth, beltHeight),
            cornerRadius = CornerRadius(2f * u)
        )
        // Обводка ремня
        drawRoundRect(
            color = darkGray,
            topLeft = pt(beltLeft, beltTop),
            size = Size(beltWidth, beltHeight),
            cornerRadius = CornerRadius(2f * u),
            style = Stroke(width = 1f * u)
        )

        // ================= БЕГУЩИЙ ИНДИКАТОР ЗАРЯДКИ ВНУТРИ РЕМНЯ =================
        // Индикатор: от -28f до 28f (отступ 2 единицы от краёв ремня), ширина 56f.
        // Высота индикатора: 1.5f * u, центрирована по вертикали ремня.
        val chargeBarLeft = beltLeft + 2f                       // -28f
        val chargeBarRight = beltLeft + 30f + 28f - 2f          // 28f
        val chargeBarWidth = chargeBarRight - chargeBarLeft     // 56f
        val chargeBarHeight = 1.5f
        val chargeBarY = beltTop + (beltHeight / (2f * u)) - chargeBarHeight / 2f

        // Прогресс бегущей полоски (используем pulse)
        val chargeProgress = ((pulse % (2f * PI.toFloat())) / (2f * PI.toFloat())).toFloat()

        // Фон индикатора (тёмный)
        drawRoundRect(
            Color(0xFF0A1520),
            topLeft = pt(chargeBarLeft, chargeBarY),
            size = Size(chargeBarWidth * u, chargeBarHeight * u),
            cornerRadius = CornerRadius(0.7f * u)
        )

        // Бегущая полоска (слева направо)
        val chargeBarTravel = chargeBarWidth * chargeProgress
        val chargeBarStart = chargeBarLeft + chargeBarTravel

        if (chargeBarTravel > 0.1f) {
            drawRoundRect(
                Brush.horizontalGradient(
                    listOf(neonBlue, neonBlueGlow, neonBlue),
                    startX = pt(chargeBarStart, chargeBarY).x,
                    endX = pt(chargeBarStart + 6f, chargeBarY).x
                ),
                topLeft = pt(chargeBarStart, chargeBarY),
                size = Size(6f * u, chargeBarHeight * u),
                cornerRadius = CornerRadius(0.7f * u)
            )

            // Свечение вокруг полоски
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        neonBlue.copy(alpha = 0.35f),
                        Color.Transparent
                    ),
                    center = pt(chargeBarStart + 3f, chargeBarY + chargeBarHeight / 2f),
                    radius = 3f * u
                ),
                radius = 3f * u,
                center = pt(chargeBarStart + 3f, chargeBarY + chargeBarHeight / 2f)
            )

            // Яркая точка в конце полоски
            drawCircle(
                color = neonBlueGlow.copy(alpha = 0.9f),
                radius = 0.5f * u,
                center = pt(chargeBarStart + 6f, chargeBarY + chargeBarHeight / 2f)
            )
        }

        // 10. ОБОДОК МЕЖДУ ЦИЛИНДРОМ И СОПЛОМ (внизу корпуса, сужен)
        drawRoundRect(
            color = mediumGray,
            topLeft = pt(-28f, 158f),
            size = Size(56f * u, 4f * u),
            cornerRadius = CornerRadius(2f * u)
        )
        drawRoundRect(
            color = darkGray,
            topLeft = pt(-28f, 158f),
            size = Size(56f * u, 4f * u),
            cornerRadius = CornerRadius(2f * u),
            style = Stroke(width = 1f * u)
        )

                // ================= ТЁМНЫЙ ЭКРАН НА ГРУДИ ПОД СОЛНЦЕ =================
        // Опущен вниз на 20% от высоты корпуса (было y=90, стало y=108)
        val screenBlack = Color(0xFF0F1216)
        val screenCenterY = 108f

        // Внешняя рамка экрана
        drawRoundRect(
            color = darkerGray,
            topLeft = pt(-22f, screenCenterY - 12f),
            size = Size(44f * u, 24f * u),
            cornerRadius = CornerRadius(6f * u)
        )

        // Внутренняя тёмная панель
        drawRoundRect(
            color = screenBlack,
            topLeft = pt(-20f, screenCenterY - 10f),
            size = Size(40f * u, 20f * u),
            cornerRadius = CornerRadius(5f * u)
        )

        // Блик на верхней части экрана
        drawRoundRect(
            color = Color.White.copy(alpha = 0.1f),
            topLeft = pt(-18f, screenCenterY - 9f),
            size = Size(36f * u, 4f * u),
            cornerRadius = CornerRadius(2f * u)
        )

        // ================= ПУЛЬСИРУЮЩЕЕ СОЛНЦЕ НА ГРУДИ =================
        // Уменьшено в 2 раза. Опущено вниз на 20% (y=100 → y=108).

        val heartCenter = pt(0f, screenCenterY)
        val corePulse = 0.5f + 0.5f * sin(pulse * 1.5f)
        val sunRadius = 5f * u * (1f + 0.15f * corePulse)

        // 1. Внешнее свечение вокруг солнца (большое гало)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0x0000FFFF),
                    Color(0x3000BFFF).copy(alpha = 0.4f + 0.2f * corePulse),
                    Color(0x600088FF).copy(alpha = 0.3f),
                    Color.Transparent
                ),
                center = heartCenter,
                radius = sunRadius * 3.2f
            ),
            radius = sunRadius * 3.2f,
            center = heartCenter
        )

        // 2. Среднее свечение (циан)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFF00FFFF).copy(alpha = (0.7f + 0.3f * corePulse) * 0.8f),
                    Color(0xFF00BFFF).copy(alpha = 0.5f),
                    Color(0xFF0044FF).copy(alpha = 0f)
                ),
                center = heartCenter,
                radius = sunRadius * 1.8f
            ),
            radius = sunRadius * 1.8f,
            center = heartCenter
        )

        // 3. Основное ядро солнца (яркое, белое с голубым)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color.White,
                    Color(0xFF00FFFF),
                    Color(0xFF00BFFF),
                    Color(0xFF0044FF)
                ),
                center = heartCenter,
                radius = sunRadius
            ),
            radius = sunRadius,
            center = heartCenter
        )

        // 4. Точечные частицы вокруг солнца (как на орбите)
        val particleCount = 12
        for (i in 0 until particleCount) {
            val angle = (i.toFloat() / particleCount) * 2f * PI.toFloat() + pulse
            val distance = sunRadius * (1.3f + 0.4f * sin(pulse * 2f + i * 0.8f))
            val px = heartCenter.x + cos(angle) * distance
            val py = heartCenter.y + sin(angle) * distance
            val pr = (0.3f + 0.3f * sin(pulse * 3f + i)) * u

            drawCircle(
                color = Color(0xFF00FFFF).copy(alpha = 0.4f + 0.4f * sin(pulse * 4f + i)),
                radius = pr,
                center = Offset(px, py)
            )
        }

        // 5. Внутренний белый блик (яркий центр)
        drawCircle(
            color = Color.White.copy(alpha = 0.9f),
            radius = sunRadius * 0.35f,
            center = Offset(heartCenter.x - sunRadius * 0.2f, heartCenter.y - sunRadius * 0.2f)
        )

        // 6. Тонкое кольцо-орбита вокруг солнца (как на картинке)
        drawCircle(
            color = Color(0xFF00FFFF).copy(alpha = 0.3f + 0.2f * corePulse),
            radius = sunRadius * 1.6f,
            center = heartCenter,
            style = Stroke(width = 0.6f * u)
        )
                       // ================= НАДПИСЬ "ИИ-Друг" МЕЖДУ ЭКРАНОМ И РЕМНЁМ =================
        val labelText = "ИИ-Друг"
                val labelFontSize = with(density) { (10f * u).toSp() }
        val labelStyle = TextStyle(
            color = Color.Black,
            fontSize = labelFontSize,
            fontWeight = FontWeight.Bold
        )
        val labelLayout = textMeasurer.measure(
            text = labelText,
            style = labelStyle
        )
                val labelCenterY = 130.375f
        val labelTopY = labelCenterY * u - labelLayout.size.height / 2f
        drawText(
            textLayoutResult = labelLayout,
            topLeft = Offset(
                x = size.width / 2f - labelLayout.size.width / 2f,
                y = labelTopY + bobOffset
            )
        )

                // ================= ШЕЯ (гибкое сочленение из колец) =================
        // 4 кольца — сужаются к голове

        // Нижнее кольцо (самое широкое, основание)
        drawRoundRect(
            color = lightGray,
            topLeft = pt(-14f, 62f),
            size = Size(28f * u, 4f * u),
            cornerRadius = CornerRadius(2f * u)
        )
        drawRoundRect(
            color = darkGray,
            topLeft = pt(-14f, 62f),
            size = Size(28f * u, 4f * u),
            cornerRadius = CornerRadius(2f * u),
            style = Stroke(width = 1f * u)
        )

        // Второе кольцо
        drawRoundRect(
            color = mediumGray,
            topLeft = pt(-13f, 66f),
            size = Size(26f * u, 3.5f * u),
            cornerRadius = CornerRadius(1.8f * u)
        )
        drawRoundRect(
            color = darkGray,
            topLeft = pt(-13f, 66f),
            size = Size(26f * u, 3.5f * u),
            cornerRadius = CornerRadius(1.8f * u),
            style = Stroke(width = 1f * u)
        )

        // Третье кольцо
        drawRoundRect(
            color = lightGray,
            topLeft = pt(-12f, 69.5f),
            size = Size(24f * u, 3.5f * u),
            cornerRadius = CornerRadius(1.8f * u)
        )
        drawRoundRect(
            color = darkGray,
            topLeft = pt(-12f, 69.5f),
            size = Size(24f * u, 3.5f * u),
            cornerRadius = CornerRadius(1.8f * u),
            style = Stroke(width = 1f * u)
        )

        // Четвёртое кольцо (самое узкое, у головы)
        drawRoundRect(
            color = mediumGray,
            topLeft = pt(-11f, 73f),
            size = Size(22f * u, 3f * u),
            cornerRadius = CornerRadius(1.5f * u)
        )
        drawRoundRect(
            color = darkGray,
            topLeft = pt(-11f, 73f),
            size = Size(22f * u, 3f * u),
            cornerRadius = CornerRadius(1.5f * u),
            style = Stroke(width = 1f * u)
        )

        // Тёмный жёлоб между кольцами (для эффекта сочленения)
        drawLine(
            darkerGray,
            pt(-12f, 65.5f),
            pt(12f, 65.5f),
            strokeWidth = 0.5f * u
        )
        drawLine(
            darkerGray,
            pt(-12f, 69f),
            pt(12f, 69f),
            strokeWidth = 0.5f * u
        )
        drawLine(
            darkerGray,
            pt(-11f, 72.5f),
            pt(11f, 72.5f),
            strokeWidth = 0.5f * u
        )

        // Блик слева на кольцах
        drawRoundRect(
            color = whiteHighlight.copy(alpha = 0.6f),
            topLeft = pt(-13f, 62.5f),
            size = Size(2f * u, 3f * u),
            cornerRadius = CornerRadius(1f * u)
        )
        drawRoundRect(
            color = whiteHighlight.copy(alpha = 0.6f),
            topLeft = pt(-12f, 66.5f),
            size = Size(2f * u, 2.5f * u),
            cornerRadius = CornerRadius(1f * u)
        )
        drawRoundRect(
            color = whiteHighlight.copy(alpha = 0.6f),
            topLeft = pt(-11f, 70f),
            size = Size(2f * u, 2.5f * u),
            cornerRadius = CornerRadius(1f * u)
        )
        drawRoundRect(
            color = whiteHighlight.copy(alpha = 0.6f),
            topLeft = pt(-10f, 73.5f),
            size = Size(2f * u, 2f * u),
            cornerRadius = CornerRadius(1f * u)
        )

             // ================= ЛЕВЫЙ НАУШНИК (повёрнут к голове на 12°) =================
        rotate(12f, pivot = pt(-42f, 45f)) {
            // Внешний полукруг
            val leftEarOuterPath = Path().apply {
                moveTo(pt(-42f, 18f).x, pt(-42f, 18f).y)
                cubicTo(
                    pt(-56.4f, 20f).x, pt(-56.4f, 20f).y,
                    pt(-56.4f, 43f).x, pt(-56.4f, 43f).y,
                    pt(-42f, 45f).x, pt(-42f, 45f).y
                )
                lineTo(pt(-42f, 18f).x, pt(-42f, 18f).y)
                close()
            }
            drawPath(
                leftEarOuterPath,
                brush = Brush.horizontalGradient(
                    colors = listOf(lightGray, whiteBody),
                    startX = pt(-56.4f, 32f).x,
                    endX = pt(-42f, 32f).x
                )
            )
            drawPath(leftEarOuterPath, color = darkGray, style = Stroke(width = 1.3f * u))

            // Внутренний полукруг
            val leftEarInnerPath = Path().apply {
                moveTo(pt(-47f, 23f).x, pt(-47f, 23f).y)
                cubicTo(
                    pt(-55f, 25f).x, pt(-55f, 25f).y,
                    pt(-55f, 38f).x, pt(-55f, 38f).y,
                    pt(-47f, 40f).x, pt(-47f, 40f).y
                )
                lineTo(pt(-47f, 23f).x, pt(-47f, 23f).y)
                close()
            }
            drawPath(leftEarInnerPath, color = mediumGray)
            drawPath(leftEarInnerPath, color = darkGray, style = Stroke(width = 1.1f * u))

            // Тёмный центр
            drawCircle(
                color = darkerGray,
                radius = 2.8f * u,
                center = pt(-50f, 31f)
            )

            // Антенна
            drawLine(
                color = darkGray,
                start = pt(-46f, 18f),
                end = pt(-46f, -4f),
                strokeWidth = 1.8f * u,
                cap = StrokeCap.Round
            )
            drawRoundRect(
                color = darkerGray,
                topLeft = pt(-48f, 17f),
                size = Size(4f * u, 3f * u),
                cornerRadius = CornerRadius(1.5f * u)
            )
            drawCircle(
                color = mediumGray,
                radius = 1.8f * u,
                center = pt(-46f, -4f)
            )
            drawCircle(
                color = darkGray,
                radius = 1.8f * u,
                center = pt(-46f, -4f),
                style = Stroke(width = 0.8f * u)
            )
        }

                // ================= ПРАВЫЙ НАУШНИК (повёрнут к голове на 12°) =================
        rotate(-12f, pivot = pt(42f, 45f)) {
            val rightEarOuterPath = Path().apply {
                moveTo(pt(42f, 18f).x, pt(42f, 18f).y)
                cubicTo(
                    pt(56.4f, 20f).x, pt(56.4f, 20f).y,
                    pt(56.4f, 43f).x, pt(56.4f, 43f).y,
                    pt(42f, 45f).x, pt(42f, 45f).y
                )
                lineTo(pt(42f, 18f).x, pt(42f, 18f).y)
                close()
            }
            drawPath(
                rightEarOuterPath,
                brush = Brush.horizontalGradient(
                    colors = listOf(whiteBody, lightGray),
                    startX = pt(42f, 32f).x,
                    endX = pt(56.4f, 32f).x
                )
            )
            drawPath(rightEarOuterPath, color = darkGray, style = Stroke(width = 1.3f * u))

            val rightEarInnerPath = Path().apply {
                moveTo(pt(47f, 23f).x, pt(47f, 23f).y)
                cubicTo(
                    pt(55f, 25f).x, pt(55f, 25f).y,
                    pt(55f, 38f).x, pt(55f, 38f).y,
                    pt(47f, 40f).x, pt(47f, 40f).y
                )
                lineTo(pt(47f, 23f).x, pt(47f, 23f).y)
                close()
            }
            drawPath(rightEarInnerPath, color = mediumGray)
            drawPath(rightEarInnerPath, color = darkGray, style = Stroke(width = 1.1f * u))

            drawCircle(
                color = darkerGray,
                radius = 2.8f * u,
                center = pt(50f, 31f)
            )

            drawLine(
                color = darkGray,
                start = pt(46f, 18f),
                end = pt(46f, -4f),
                strokeWidth = 1.8f * u,
                cap = StrokeCap.Round
            )
            drawRoundRect(
                color = darkerGray,
                topLeft = pt(44f, 17f),
                size = Size(4f * u, 3f * u),
                cornerRadius = CornerRadius(1.5f * u)
            )
            drawCircle(
                color = mediumGray,
                radius = 1.8f * u,
                center = pt(46f, -4f)
            )
            drawCircle(
                color = darkGray,
                radius = 1.8f * u,
                center = pt(46f, -4f),
                style = Stroke(width = 0.8f * u)
            )
        }
                                       
        // ================= ГОЛОВА (обрезанный шар, выпуклый низ) =================
        // Верх — полукруг, низ — дуга, выпуклая вниз. Низ опущен до шеи.
        val headPath = Path().apply {
            // Левая точка нижнего края
            moveTo(pt(-42f, 48f).x, pt(-42f, 48f).y)
            // Дуга вверх (половина круга)
            cubicTo(
                pt(-42f, 10f).x, pt(-42f, 10f).y,
                pt(-25f, -6f).x, pt(-25f, -6f).y,
                pt(0f, -6f).x, pt(0f, -6f).y
            )
            cubicTo(
                pt(25f, -6f).x, pt(25f, -6f).y,
                pt(42f, 10f).x, pt(42f, 10f).y,
                pt(42f, 48f).x, pt(42f, 48f).y
            )
            // Низ — выпуклая дуга вниз, опущена до шеи
            cubicTo(
                pt(42f, 58f).x, pt(42f, 58f).y,
                pt(20f, 62f).x, pt(20f, 62f).y,
                pt(0f, 62f).x, pt(0f, 62f).y
            )
            cubicTo(
                pt(-20f, 62f).x, pt(-20f, 62f).y,
                pt(-42f, 58f).x, pt(-42f, 58f).y,
                pt(-42f, 48f).x, pt(-42f, 48f).y
            )
            close()
        }

        // Заливка головы с градиентом
        drawPath(
            headPath,
            brush = Brush.verticalGradient(
                colors = listOf(whiteHighlight, whiteBody, lightGray),
                startY = pt(0f, -6f).y,
                endY = pt(0f, 62f).y
            )
        )
        drawPath(headPath, color = darkGray, style = Stroke(width = 1.5f * u))

        // ================= ЛИНИЯ ВЫШЕ НИЗА ГОЛОВЫ (на 4 единицы) =================
        val upperBandLinePath = Path().apply {
            moveTo(pt(-42f, 44f).x, pt(-42f, 44f).y)
            cubicTo(
                pt(-42f, 54f).x, pt(-42f, 54f).y,
                pt(-20f, 58f).x, pt(-20f, 58f).y,
                pt(0f, 58f).x, pt(0f, 58f).y
            )
            cubicTo(
                pt(20f, 58f).x, pt(20f, 58f).y,
                pt(42f, 54f).x, pt(42f, 54f).y,
                pt(42f, 44f).x, pt(42f, 44f).y
            )
        }

        drawPath(upperBandLinePath, color = darkGray, style = Stroke(width = 1.2f * u))

        // ================= КЛЁПКИ ПОСЕРЕДИНЕ МЕЖДУ ЛИНИЯМИ =================
        // 7 клёпок с равным промежутком вдоль средней линии.
        // Средняя линия = низ головы − 2 единицы по Y (половина ширины 4).
        val rivetColor = mediumGray
        val rivetRadius = 1f * u

        // Левая половина: 3 клёпки
        drawCircle(
            color = rivetColor,
            radius = rivetRadius,
            center = pt(-38.25f, 52.42f)
        )
        drawCircle(
            color = rivetColor,
            radius = rivetRadius,
            center = pt(-28.5f, 56.75f)
        )
        drawCircle(
            color = rivetColor,
            radius = rivetRadius,
            center = pt(-15.01f, 59.22f)
        )

        // Центр: 1 клёпка
        drawCircle(
            color = rivetColor,
            radius = rivetRadius,
            center = pt(0f, 60f)
        )

        // Правая половина: 3 клёпки (симметрично)
        drawCircle(
            color = rivetColor,
            radius = rivetRadius,
            center = pt(15.01f, 59.22f)
        )
        drawCircle(
            color = rivetColor,
            radius = rivetRadius,
            center = pt(28.5f, 56.75f)
        )
        drawCircle(
            color = rivetColor,
            radius = rivetRadius,
            center = pt(38.25f, 52.42f)
        )
                // ================= ОБЛАСТЬ МЕЖДУ ЛИНИЯМИ НА ГОЛОВЕ =================
        // Верх — дуга шлема, низ — верхняя кромка визора,
        // бока — leftHeadLinePath и rightHeadLinePath.
        val headPanelPath = Path().apply {
            // Левый верхний угол
            moveTo(pt(-16f, -4f).x, pt(-16f, -4f).y)
            // Верхняя граница — дуга шлема к правому верхнему углу
            cubicTo(
                pt(-8f, -6f).x, pt(-8f, -6f).y,
                pt(8f, -6f).x, pt(8f, -6f).y,
                pt(16f, -4f).x, pt(16f, -4f).y
            )
            // Правая линия — сверху вниз (обратный порядок rightHeadLinePath)
            cubicTo(
                pt(16f, 2f).x, pt(16f, 2f).y,
                pt(15.5f, 8f).x, pt(15.5f, 8f).y,
                pt(15f, 14f).x, pt(15f, 14f).y
            )
            // Нижняя граница — прямая к левому нижнему углу
            lineTo(pt(-15f, 14f).x, pt(-15f, 14f).y)
            // Левая линия — снизу вверх (обратный порядок leftHeadLinePath)
            cubicTo(
                pt(-15.5f, 8f).x, pt(-15.5f, 8f).y,
                pt(-16f, 2f).x, pt(-16f, 2f).y,
                pt(-16f, -4f).x, pt(-16f, -4f).y
            )
            close()
        }

        // Заливка светло-серым градиентом
        drawPath(
            headPanelPath,
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFFEDEFF2),
                    Color(0xFFD9DEE3),
                    Color(0xFFC4CAD1)
                ),
                startY = pt(0f, -4f).y,
                endY = pt(0f, 14f).y
            )
        )

        // Блик — вертикальная полоса слева
        val headPanelHighlight = Path().apply {
            moveTo(pt(-9f, -2f).x, pt(-9f, -2f).y)
            lineTo(pt(-4f, -2f).x, pt(-4f, -2f).y)
            lineTo(pt(-4f, 12f).x, pt(-4f, 12f).y)
            lineTo(pt(-9f, 12f).x, pt(-9f, 12f).y)
            close()
        }
        drawPath(
            headPanelHighlight,
            color = Color.White.copy(alpha = 0.5f)
        )
        
                // ================= ДВЕ ЛИНИИ НА ГОЛОВЕ (от верхнего края шлема до визора) =================
        // Левая линия — идёт по дуге шлема сверху вниз до верхней кромки визора (y = 14)
        val leftHeadLinePath = Path().apply {
            moveTo(pt(-16f, -4f).x, pt(-16f, -4f).y)
            cubicTo(
                pt(-16f, 2f).x, pt(-16f, 2f).y,
                pt(-15.5f, 8f).x, pt(-15.5f, 8f).y,
                pt(-15f, 14f).x, pt(-15f, 14f).y
            )
        }
        drawPath(
            leftHeadLinePath,
            color = darkGray,
            style = Stroke(width = 1f * u, cap = StrokeCap.Round)
        )

        // Правая линия — симметрично
        val rightHeadLinePath = Path().apply {
            moveTo(pt(16f, -4f).x, pt(16f, -4f).y)
            cubicTo(
                pt(16f, 2f).x, pt(16f, 2f).y,
                pt(15.5f, 8f).x, pt(15.5f, 8f).y,
                pt(15f, 14f).x, pt(15f, 14f).y
            )
        }
        drawPath(
            rightHeadLinePath,
            color = darkGray,
            style = Stroke(width = 1f * u, cap = StrokeCap.Round)
        )

        // ================= ВИЗОР (СТЕКЛО) — лыжная маска =================
        // Уменьшен на 10%, верхние углы скруглены на 20% сильнее
        val visorPath = Path().apply {
            // Левый верхний угол — сильнее скруглён
            moveTo(pt(-29.2f, 14f).x, pt(-29.2f, 14f).y)
            quadraticBezierTo(
                pt(-34f, 14f).x, pt(-34f, 14f).y,
                pt(-34f, 22.64f).x, pt(-34f, 22.64f).y
            )
            // Левая боковина вниз
            lineTo(pt(-34f, 29.04f).x, pt(-34f, 29.04f).y)
            // Ещё большее скругление левого нижнего угла
            quadraticBezierTo(
                pt(-34f, 42f).x, pt(-34f, 42f).y,
                pt(-21.04f, 42f).x, pt(-21.04f, 42f).y
            )
            // Плоский низ к выемке
            lineTo(pt(-10f, 42f).x, pt(-10f, 42f).y)
            // Выемка под нос — плавный полукруг, меньше и мягче
            cubicTo(
                pt(-6f, 39f).x, pt(-6f, 39f).y,
                pt(-3f, 36f).x, pt(-3f, 36f).y,
                pt(0f, 36f).x, pt(0f, 36f).y
            )
            cubicTo(
                pt(3f, 36f).x, pt(3f, 36f).y,
                pt(6f, 39f).x, pt(6f, 39f).y,
                pt(10f, 42f).x, pt(10f, 42f).y
            )
            // Плоский низ к правому нижнему углу
            lineTo(pt(21.04f, 42f).x, pt(21.04f, 42f).y)
            // Ещё большее скругление правого нижнего угла
            quadraticBezierTo(
                pt(34f, 42f).x, pt(34f, 42f).y,
                pt(34f, 29.04f).x, pt(34f, 29.04f).y
            )
            // Правая боковина вверх
            lineTo(pt(34f, 22.64f).x, pt(34f, 22.64f).y)
            // Сильнее скругление правого верхнего угла
            quadraticBezierTo(
                pt(34f, 14f).x, pt(34f, 14f).y,
                pt(29.2f, 14f).x, pt(29.2f, 14f).y
            )
            // Прямой верх
            lineTo(pt(-29.2f, 14f).x, pt(-29.2f, 14f).y)
            close()
        }

        // Заливка визора
        drawPath(
            visorPath,
            brush = Brush.verticalGradient(
                colors = listOf(visorGlass, visorDark),
                startY = pt(0f, 14f).y,
                endY = pt(0f, 42f).y
            )
        )
        drawPath(visorPath, color = darkerGray, style = Stroke(width = 1.5f * u))

        // Блик на стекле — верхняя полоса
        val glassHighlight = Path().apply {
            moveTo(pt(-27f, 18f).x, pt(-27f, 18f).y)
            lineTo(pt(27f, 18f).x, pt(27f, 18f).y)
            lineTo(pt(25f, 22f).x, pt(25f, 22f).y)
            lineTo(pt(-25f, 22f).x, pt(-25f, 22f).y)
            close()
        }
        drawPath(glassHighlight, color = Color.White.copy(alpha = 0.3f))

                      // ================= ГЛАЗА (Трапеция, длинный край к носу) =================
        val isBlinking = currentBlink < 0.5f

        // Базовые размеры
        val baseEyeW = 14f * u
        val baseEyeH = 10f * u

        // Вычисляем текущие размеры глаза в зависимости от состояния
        val (eyeW, eyeH) = when {
            isBlinking -> baseEyeW to 1.5f * u
            isThinking -> baseEyeW to 7f * u
            isSpeaking -> 15f * u to 12f * u
            isIdle -> {
                val p = idleEyePhase
                when {
                    p < 0.4f -> baseEyeW to baseEyeH
                    p < 0.6f -> {
                        val t = (p - 0.4f) / 0.2f
                        (baseEyeW + 2f * u * t) to (baseEyeH + 3f * u * t)
                    }
                    p < 0.8f -> {
                        val t = (p - 0.6f) / 0.2f
                        (baseEyeW + 3f * u * t) to (baseEyeH - 6f * u * t)
                    }
                    else -> {
                        val t = (p - 0.8f) / 0.2f
                        ((baseEyeW + 3f * u) - 3f * u * t) to (4f * u + 6f * u * t)
                    }
                }
            }
            else -> baseEyeW to baseEyeH
        }

        // Размеры зрачка
        val (pupilW, pupilH) = when {
            isBlinking -> 2f * u to 1f * u
            isThinking -> 3f * u to 5f * u
            isSpeaking -> 6f * u to 8f * u
            isIdle -> {
                val p = idleEyePhase
                when {
                    p < 0.4f -> 5f * u to 7f * u
                    p < 0.6f -> 6f * u to 9f * u
                    p < 0.8f -> 8f * u to 2.5f * u
                    else -> {
                        val t = (p - 0.8f) / 0.2f
                        ((8f * u) - 3f * u * t) to (2.5f * u + 4.5f * u * t)
                    }
                }
            }
            else -> 5f * u to 7f * u
        }

                fun createTrapezoidEyePath(centerX: Float, centerY: Float, width: Float, height: Float, isLeft: Boolean): Path {
            // Увеличиваем размеры на 20%
            val scaledW = width * 1.2f
            val scaledH = height * 1.2f

            val halfW = scaledW / 2f
            val halfH = scaledH / 2f
            
            // Коэффициент сужения внешнего края (45% от внутреннего)
            val outerRatio = 0.45f
            
            // Внутренний край (к носу) — ДЛИННЫЙ
            val innerTopY = centerY - halfH
            val innerBottomY = centerY + halfH
            
            // Внешний край (наружу) — КОРОТКИЙ
            val outerTopY = centerY - halfH * outerRatio
            val outerBottomY = centerY + halfH * outerRatio
            
            // X-координаты краёв
            val innerX = if (isLeft) centerX + halfW * 0.85f else centerX - halfW * 0.85f
            val outerX = if (isLeft) centerX - halfW else centerX + halfW
            
            // Радиус скругления углов — увеличен для плавности
            val cornerRadius = 2.5f * u
            
            return Path().apply {
                // Начинаем с внутреннего верхнего угла (к носу, сверху)
                moveTo(innerX - cornerRadius, innerTopY)
                
                // Верхняя сторона — к внешнему верхнему углу
                lineTo(outerX + cornerRadius, outerTopY)
                
                // Скругление внешнего верхнего угла
                quadraticBezierTo(
                    outerX, outerTopY,
                    outerX, outerTopY + cornerRadius
                )
                
                // Внешняя вертикаль (короткая)
                lineTo(outerX, outerBottomY - cornerRadius)
                
                // Скругление внешнего нижнего угла
                quadraticBezierTo(
                    outerX, outerBottomY,
                    outerX - cornerRadius, outerBottomY
                )
                
                // Нижняя сторона — к внутреннему нижнему углу
                lineTo(innerX + cornerRadius, innerBottomY)
                
                // Скругление внутреннего нижнего угла
                quadraticBezierTo(
                    innerX, innerBottomY,
                    innerX, innerBottomY - cornerRadius
                )
                
                // Внутренняя вертикаль (длинная)
                lineTo(innerX, innerTopY + cornerRadius)
                
                // Скругление внутреннего верхнего угла
                quadraticBezierTo(
                    innerX, innerTopY,
                    innerX - cornerRadius, innerTopY
                )
                
                close()
            }
        }

        // ========== ЛЕВЫЙ ГЛАЗ ==========
        val leftEyeCenterX = -12f + lookOffsetX / u
        val leftEyeCenterY = 28f + lookOffsetY / u
        val leftCenter = pt(leftEyeCenterX, leftEyeCenterY)

        // Создаём форму левого глаза (длинный край справа, к носу)
        val leftEyePath = createTrapezoidEyePath(leftCenter.x, leftCenter.y, eyeW, eyeH, isLeft = true)

        // 1. Неоновое свечение вокруг глаза
        drawPath(
            path = leftEyePath,
            brush = Brush.radialGradient(
                colors = listOf(
                    neonBluePulse.copy(alpha = 0.4f),
                    neonBluePulse.copy(alpha = 0.1f),
                    Color.Transparent
                ),
                center = leftCenter,
                radius = eyeW * 0.8f
            )
        )

        // 2. Тёмная подложка глаза
        drawPath(leftEyePath, color = Color(0xFF0A0A14))

        // 3. Основной цвет глаза (голубой градиент)
        drawPath(
            path = leftEyePath,
            brush = Brush.radialGradient(
                colors = listOf(neonBluePulse, neonBluePulse.copy(alpha = 0.7f)),
                center = leftCenter,
                radius = eyeW * 0.5f
            )
        )

        // 4. Зрачок
        val pupilOffsetX = (lookOffsetX / u) * 0.3f * u
        val pupilOffsetY = (lookOffsetY / u) * 0.3f * u
        val leftPupilCenter = Offset(leftCenter.x + pupilOffsetX, leftCenter.y + pupilOffsetY)

        drawOval(
            color = Color(0xFF050510),
            topLeft = Offset(leftPupilCenter.x - pupilW / 2f, leftPupilCenter.y - pupilH / 2f),
            size = Size(pupilW, pupilH)
        )

        // 5. Блики
        drawOval(
            color = Color.White.copy(alpha = 0.9f),
            topLeft = Offset(leftPupilCenter.x - pupilW * 0.3f, leftPupilCenter.y - pupilH * 0.35f),
            size = Size(pupilW * 0.4f, pupilH * 0.5f)
        )
        drawCircle(
            color = Color.White.copy(alpha = 0.7f),
            radius = 1.2f * u,
            center = Offset(leftPupilCenter.x + pupilW * 0.25f, leftPupilCenter.y - pupilH * 0.25f)
        )

        // ========== ПРАВЫЙ ГЛАЗ ==========
        val rightEyeCenterX = 12f + lookOffsetX / u
        val rightEyeCenterY = 28f + lookOffsetY / u
        val rightCenter = pt(rightEyeCenterX, rightEyeCenterY)

        // Создаём форму правого глаза (длинный край слева, к носу)
        val rightEyePath = createTrapezoidEyePath(rightCenter.x, rightCenter.y, eyeW, eyeH, isLeft = false)

        // 1. Свечение
        drawPath(
            path = rightEyePath,
            brush = Brush.radialGradient(
                colors = listOf(
                    neonBluePulse.copy(alpha = 0.4f),
                    neonBluePulse.copy(alpha = 0.1f),
                    Color.Transparent
                ),
                center = rightCenter,
                radius = eyeW * 0.8f
            )
        )

        // 2. Тёмная подложка
        drawPath(rightEyePath, color = Color(0xFF0A0A14))

        // 3. Основной цвет
        drawPath(
            path = rightEyePath,
            brush = Brush.radialGradient(
                colors = listOf(neonBluePulse, neonBluePulse.copy(alpha = 0.7f)),
                center = rightCenter,
                radius = eyeW * 0.5f
            )
        )

        // 4. Зрачок
        val rightPupilCenter = Offset(rightCenter.x + pupilOffsetX, rightCenter.y + pupilOffsetY)
        drawOval(
            color = Color(0xFF050510),
            topLeft = Offset(rightPupilCenter.x - pupilW / 2f, rightPupilCenter.y - pupilH / 2f),
            size = Size(pupilW, pupilH)
        )

        // 5. Блики
        drawOval(
            color = Color.White.copy(alpha = 0.9f),
            topLeft = Offset(rightPupilCenter.x - pupilW * 0.3f, rightPupilCenter.y - pupilH * 0.35f),
            size = Size(pupilW * 0.4f, pupilH * 0.5f)
        )
        drawCircle(
            color = Color.White.copy(alpha = 0.7f),
            radius = 1.2f * u,
            center = Offset(rightPupilCenter.x + pupilW * 0.25f, rightPupilCenter.y - pupilH * 0.25f)
        )
        
                // ================= РОТ =================
        if (isSpeaking) {
            // Открытый рот при говорении
            val mouthH = (3f + 3f * sin(mouthPhase)) * u
            drawOval(
                color = darkerGray,
                topLeft = pt(-10f, 50f - mouthH / u / 2f),
                size = Size(20f * u, mouthH * 2f)
            )
            drawOval(
                color = Color(0xFF0A0A14),
                topLeft = pt(-8f, 50f - mouthH / u / 2f + 0.5f),
                size = Size(16f * u, mouthH * 2f - 1f * u)
            )
        } else {
            // Простая полукруглая линия — улыбка
            val smilePath = Path().apply {
                moveTo(pt(-10f, 48f).x, pt(-10f, 48f).y)
                cubicTo(
                    pt(-5f, 53f).x, pt(-5f, 53f).y,
                    pt(5f, 53f).x, pt(5f, 53f).y,
                    pt(10f, 48f).x, pt(10f, 48f).y
                )
            }
            drawPath(
                smilePath,
                color = darkerGray,
                style = Stroke(width = 2f * u, cap = StrokeCap.Round)
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
