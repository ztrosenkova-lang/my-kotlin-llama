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
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.ui.graphics.graphicsLayer
import kotlin.math.atan2
import kotlin.math.min
import java.util.Random
import androidx.compose.ui.platform.LocalDensity

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

        val isFirstLaunch by viewModel.isFirstLaunch.collectAsStateWithLifecycle(initialValue = false)

    LaunchedEffect(isTtsReady) {
        if (isTtsReady && !welcomeStarted) {
            welcomeStarted = true
            val greeting = if (isFirstLaunch) {
                fullWelcomeString
            } else {
                "Привет друг. Чем займемся?"
            }
            viewModel.speakText(greeting)
        }
    }

        LaunchedEffect(speakStartTrigger) {
        if (speakStartTrigger && welcomeStarted && !welcomeTextPrinted) {
            welcomeTextPrinted = true
            val greeting = if (isFirstLaunch) {
                fullWelcomeString
            } else {
                "Привет друг. Чем займемся?"
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

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
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
            modifier = Modifier.padding(8.dp),
            colors = colors,
            isDarkTheme = isDarkTheme
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
        // Новая, более глубокая палитра
        val metalTop = Color(0xFFF2F6FA) // Почти белый хром (блик)
        val metalLight = Color(0xFFCED6DF)
        val metalMid = Color(0xFF9DA9B6)
        val metalDark = Color(0xFF68737F)
        val metalDeep = Color(0xFF353D45) // Глубокая тень
        val eyeDark = Color(0xFF081C26)
        val cyan = Color(0xFF3FE3F5)
        val cyanBright = Color(0xFF9DF5FF)
        val cyanDeep = Color(0xFF00A6C9)
        val brainPink = Color(0xFFEFA5B3)
        val brainDark = Color(0xFFB5536B)
        val orange = Color(0xFFF89B3C)
        val orangeBright = Color(0xFFFFE0B2)

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
                Brush.radialGradient(
                    listOf(color, color.copy(alpha = 0f)),
                    center, radius
                ),
                radius,
                center
            )
        }

        // ================= Плечи и корпус =================
        // Основной овал с глубоким вертикальным градиентом
        drawOval(
            Brush.verticalGradient(listOf(metalLight, metalMid, metalDark)),
            topLeft = pt(28f, 77f),
            size = Size(44f * u, 18f * u)
        )
        
        // Добавляем блик сверху на плечи
        drawOval(
            Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.55f), Color.Transparent)),
            topLeft = pt(30f, 77.5f),
            size = Size(40f * u, 5f * u)
        )

        // Тень под плечами
        drawOval(
            Brush.verticalGradient(listOf(Color.Transparent, metalDeep.copy(alpha = 0.8f))),
            topLeft = pt(28f, 90f),
            size = Size(44f * u, 5f * u)
        )

        // Крепления и суставы
        drawCircle(Brush.radialGradient(listOf(metalLight, metalDeep), center = pt(29f, 83f), radius = 7f*u), 6f * u, pt(29f, 85f))
        drawCircle(Brush.radialGradient(listOf(metalLight, metalDeep), center = pt(71f, 83f), radius = 7f*u), 6f * u, pt(71f, 85f))
        
        drawCircle(Color.White.copy(alpha = 0.5f), 1.2f * u, pt(27.5f, 83f))
        drawCircle(Color.White.copy(alpha = 0.5f), 1.2f * u, pt(69.5f, 83f))
        
        drawOval(Color.White.copy(alpha = 0.15f), topLeft = pt(33f, 78.5f), size = Size(14f * u, 3.5f * u))

        // ================= Сопла + ракетное пламя =================
        for ((idx, sx) in listOf(29f, 71f).withIndex()) {
            // Корпус сопла (с тенью внутри)
            drawRoundRect(
                Brush.verticalGradient(listOf(metalDark, metalDeep)),
                topLeft = pt(sx - 2.5f, 88.5f),
                size = Size(5f * u, 3f * u),
                cornerRadius = CornerRadius(1f * u)
            )
            drawRoundRect(
                Color(0xFF111A22),
                topLeft = pt(sx - 1.6f, 90.6f),
                size = Size(3.2f * u, 1.4f * u),
                cornerRadius = CornerRadius(0.7f * u)
            )
            if (finalBob != 0f) {
                val flick = 0.5f + 0.5f * sin(phase * 4f + idx * 2.1f)
                val len = (5f + 4.5f * flick) * u
                val fw = 4.4f * u
                val top = pt(sx - 2.2f, 91.2f)
                
                // Свечение пламени
                glow(
                    Offset(top.x + fw / 2f, top.y + len * 0.4f),
                    (6f + 3f * flick) * u,
                    Color(0xFF0077FF).copy(alpha = 0.5f)
                )
                
                // Пламя (глубокий градиент с горячим центром)
                drawOval(
                    Brush.verticalGradient(
                        listOf(
                            cyanBright,
                            cyan,
                            Color(0xFF0066FF),
                            Color(0xFF0033AA).copy(alpha = 0f)
                        )
                    ),
                    topLeft = top,
                    size = Size(fw, len)
                )
                // Внутреннее белое ядро пламени
                drawOval(
                    Brush.verticalGradient(
                        listOf(Color.White, cyanBright.copy(alpha = 0f))
                    ),
                    topLeft = Offset(top.x + fw * 0.28f, top.y),
                    size = Size(fw * 0.44f, len * 0.55f)
                )
            }
        }

        // ================= Панель груди =================
        // Основной корпус панели (объем)
        drawRoundRect(
            Brush.verticalGradient(listOf(metalMid, metalDark, metalDeep)),
            topLeft = pt(39f, 80f),
            size = Size(22f * u, 11f * u),
            cornerRadius = CornerRadius(3f * u)
        )
        // Внутренний экран
        drawRoundRect(
            Brush.verticalGradient(listOf(Color(0xFF1D2B3A), Color(0xFF0D1A26))),
            topLeft = pt(41f, 82f),
            size = Size(12f * u, 7f * u),
            cornerRadius = CornerRadius(2f * u)
        )
        // Добавляем блик на стекло экрана
        drawRoundRect(
            Color.White.copy(alpha = 0.15f),
            topLeft = pt(41.5f, 82.5f),
            size = Size(11f * u, 2f * u),
            cornerRadius = CornerRadius(2f * u)
        )

        // Основное ядро
        val coreP = 0.5f + 0.5f * sin(pulse * 1.5f)
        glow(pt(47f, 85.5f), (5f + 2f * coreP) * u, cyan.copy(alpha = 0.5f))
        drawCircle(Brush.radialGradient(listOf(cyanBright, cyan, cyanDeep), center = pt(47f, 84.5f), radius = 3f * u), (1.8f + 0.5f * coreP) * u, pt(47f, 85.5f))
        drawCircle(Color.White.copy(alpha = 0.9f), 0.7f * u, pt(46.4f, 84.9f))
        
        // Индикаторы состояния
        drawCircle(Brush.radialGradient(listOf(orangeBright, orange, Color(0xFF9E4F00))), 1.3f * u, pt(56.5f, 83.5f))
        drawCircle(Color.White.copy(alpha = 0.4f), 0.4f * u, pt(56.1f, 83.1f))
        
        drawCircle(Brush.radialGradient(listOf(Color(0xFF9DF5CB), Color(0xFF69D2A7), Color(0xFF1B7A4B))), 1.3f * u, pt(56.5f, 87.5f))
        drawCircle(Color.White.copy(alpha = 0.4f), 0.4f * u, pt(56.1f, 87.1f))

        // ================= Антенна =================
        // Стебель антенны
        drawLine(
            Brush.linearGradient(listOf(metalLight, metalDark), start = pt(23.2f, 63f), end = pt(24.8f, 63f)),
            pt(24f, 63f), pt(24f, 34f), strokeWidth = 1.6f * u
        )
        // Основание антенны (соединительная гайка)
        drawRoundRect(metalDark, topLeft = pt(23.2f, 60f), size = Size(1.6f * u, 4f * u), cornerRadius = CornerRadius(0.3f * u))

        val orbP = if (finalPulse != 0f) 0.5f + 0.5f * sin(finalPulse * 2f) else 0f
        // Мощное свечение вокруг шара
        glow(pt(24f, 35f), (7f + 3f * orbP) * u, orange.copy(alpha = if (finalPulse != 0f) 0.65f else 0.2f))
        // Сам шар (градиент для 3D объема)
        drawCircle(
            Brush.radialGradient(listOf(orangeBright, orange, Color(0xFFB35A00)), center = pt(23.5f, 34.5f), radius = 3.5f * u),
            (2.6f + 0.4f * orbP) * u, pt(24f, 35f)
        )
        drawCircle(Color.White.copy(alpha = 0.9f), 1f * u, pt(23.2f, 34f))

        // ================= Уши (с внутренними деталями) =================
        // Левые
        drawOval(Brush.verticalGradient(listOf(metalLight, metalDark)), topLeft = pt(19f, 55f), size = Size(9f * u, 16f * u))
        drawOval(metalDeep, topLeft = pt(21.5f, 58f), size = Size(4.5f * u, 10f * u))
        // Добавляем решетку внутри уха
        for (i in 0 until 3) {
            drawLine(metalDark, pt(22.2f, 59.5f + i * 2.5f), pt(25.3f, 59.5f + i * 2.5f), strokeWidth = 0.3f * u)
        }

        // Правые
        drawOval(Brush.verticalGradient(listOf(metalLight, metalDark)), topLeft = pt(72f, 55f), size = Size(9f * u, 16f * u))
        drawOval(metalDeep, topLeft = pt(74f, 58f), size = Size(4.5f * u, 10f * u))
        for (i in 0 until 3) {
            drawLine(metalDark, pt(74.7f, 59.5f + i * 2.5f), pt(77.8f, 59.5f + i * 2.5f), strokeWidth = 0.3f * u)
        }

        // ================= Купол =================
        val domeC = pt(50f, 46f)
        val domeR = 26f * u
        val domeTL = Offset(domeC.x - domeR, domeC.y - domeR)
        val domeSize = Size(domeR * 2f, domeR * 2f)
        
        // Объемное стекло (затемнение снизу)
        drawArc(
            Brush.verticalGradient(
                0f to Color(0xFFBFE9F2).copy(alpha = 0.25f),
                0.8f to Color(0xFFBFE9F2).copy(alpha = 0.15f),
                1f to Color(0xFF446E79).copy(alpha = 0.45f)
            ),
            180f,
            180f,
            true,
            topLeft = domeTL,
            size = domeSize
        )

        // ================= Мозг =================
        val s = 1f + 0.05f * sin(finalPulse * 1.5f)
        fun bp(x: Float, y: Float) = pt(50f + (x - 50f) * s, 39f + (y - 39f) * s)

        // Внутреннее свечение мозга
        glow(bp(50f, 38f), 12f * u, Color(0xFFE36F8C).copy(alpha = if (finalPulse != 0f) 0.40f else 0.1f))
        
        // Извилины мозга (добавляем градиенты для полушарий)
        listOf(
            Triple(43f, 39f, 4.5f), Triple(57f, 39f, 4.5f),
            Triple(50f, 33f, 4.2f), Triple(50f, 42f, 4.2f),
            Triple(37f, 42f, 3f), Triple(63f, 42f, 3f),
            Triple(46f, 35f, 2.5f), Triple(54f, 35f, 2.5f),
            Triple(40f, 37f, 2.5f), Triple(60f, 37f, 2.5f)
        ).forEach { (x, y, r) -> 
            drawCircle(
                Brush.radialGradient(listOf(brainPink.copy(alpha = 0.9f), brainDark), center = bp(x, y), radius = r * s * u),
                r * s * u, bp(x, y)
            ) 
        }

        // Тени между извилинами (борозды)
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
                style = Stroke(1.4f * u, cap = StrokeCap.Round)
            )
        }

        // ================= Орбиты и искры =================
        // Делаем орбиты более плавными и светящимися
        rotate(-16f, pivot = pt(50f, 38f)) {
            drawOval(
                cyan.copy(alpha = 0.25f),
                topLeft = pt(28f, 30.5f),
                size = Size(44f * u, 15f * u),
                style = Stroke(1.6f * u)
            )
            drawOval(
                cyanBright.copy(alpha = 0.7f),
                topLeft = pt(28f, 30.5f),
                size = Size(44f * u, 15f * u),
                style = Stroke(0.7f * u)
            )
        }
        rotate(12f, pivot = pt(50f, 38f)) {
            drawOval(
                cyan.copy(alpha = 0.25f),
                topLeft = pt(29.5f, 31.5f),
                size = Size(41f * u, 13f * u),
                style = Stroke(1.6f * u)
            )
            drawOval(
                cyanBright.copy(alpha = 0.65f),
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
            for (i in 0..7) { // Увеличили количество искр для красоты
                val a = finalPhase * (1.2f + 0.15f * i) + i * 1.2f
                val p = if (i % 2 == 0) orbitPos(22f, 7.5f, -16f, a)
                else orbitPos(20.5f, 6.5f, 12f, a)
                val tw = 0.5f + 0.5f * sin(finalPulse * 2f + i * 1.3f)
                
                // Мощное гало
                glow(p, (3f + 2.5f * tw) * u, cyan.copy(alpha = 0.4f + 0.4f * tw))
                // Ядро искры (белое + цветное)
                drawCircle(cyanBright, 1.2f * u, p)
                drawCircle(Color.White.copy(alpha = 0.8f), 0.5f * u, p)
            }
        }

        // ================= Купол: ободок и блик =================
        // Двойная обводка для объема
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
            metalDeep,
            180f,
            180f,
            false,
            topLeft = Offset(domeTL.x - 0.4f * u, domeTL.y + 0.4f * u),
            size = domeSize,
            style = Stroke(1.1f * u)
        )
        
        // Блик на стекле
        drawArc(
            Color.White.copy(alpha = 0.9f),
            195f,
            45f,
            false,
            topLeft = Offset(domeC.x - domeR + 2.5f * u, domeC.y - domeR + 2.5f * u),
            size = Size(domeR * 2f - 5f * u, domeR * 2f - 5f * u),
            style = Stroke(1.6f * u, cap = StrokeCap.Round)
        )
        // Добавляем второй, маленький блик снизу
        drawArc(
            Color.White.copy(alpha = 0.4f),
            15f,
            30f,
            false,
            topLeft = Offset(domeC.x - domeR + 3.5f * u, domeC.y - domeR - 1f * u),
            size = Size(domeR * 2f - 7f * u, domeR * 2f - 7f * u),
            style = Stroke(1f * u)
        )

        // Ободок купола (стекло->металл)
        drawOval(metalDark, topLeft = pt(27.5f, 46.2f), size = Size(45f * u, 3.4f * u))
        drawOval(Color.White.copy(alpha = 0.3f), topLeft = pt(28f, 46.3f), size = Size(44f * u, 1.2f * u))

        // ================= Голова =================
        // Объемный металлический корпус с бликом и тенью
        drawOval(
            Brush.verticalGradient(listOf(metalLight, metalMid, metalDark, metalDeep)),
            topLeft = pt(26f, 47f),
            size = Size(48f * u, 32f * u)
        )
        // Большой глянцевый блик слева-сверху
        rotate(-16f, pivot = pt(35f, 53f)) {
            drawOval(
                Brush.radialGradient(
                    listOf(Color.White.copy(alpha = 0.45f), Color.Transparent),
                    center = pt(35f, 52f),
                    radius = 10f * u
                ),
                topLeft = pt(29f, 51f),
                size = Size(13f * u, 3.6f * u)
            )
        }
        
        // Цветные рефлексы на металле
        glow(pt(37f, 67f), 5f * u, Color(0xFFE36F8C).copy(alpha = 0.25f))
        glow(pt(63f, 67f), 5f * u, Color(0xFFE36F8C).copy(alpha = 0.25f))
        glow(pt(50f, 76f), 6f * u, cyan.copy(alpha = 0.15f)) // Отражение от света рта/экрана

        // ================= Глаза + зрачки =================
        for (sx in listOf(-1f, 1f)) {
            val ec = pt(50f + sx * 9.5f, 61f)
            val ry = 7f * u * finalBlink
            
            // Глубокое свечение вокруг глаз
            glow(ec, 9f * u, cyan.copy(alpha = 0.45f))
            
            // Темная впадина глаза (объем)
            drawOval(
                Brush.radialGradient(listOf(Color(0xFF000000).copy(alpha = 0.8f), Color.Transparent), center = ec, radius = 7f * u),
                topLeft = Offset(ec.x - 5f * u, ec.y - ry),
                size = Size(10f * u, ry * 2f)
            )
            
            // Само светящееся веко
            drawOval(
                eyeDark,
                topLeft = Offset(ec.x - 5f * u, ec.y - ry),
                size = Size(10f * u, ry * 2f)
            )
            
            // Светящаяся радужка
            val iry = 5.8f * u * finalBlink
            drawOval(
                Brush.verticalGradient(listOf(cyanBright, cyan, cyanDeep)),
                topLeft = Offset(ec.x - 3.8f * u, ec.y - iry),
                size = Size(7.6f * u, iry * 2f)
            )
            
            // Внутренний блик (светящийся эффект лампы)
            drawOval(
                Color.White.copy(alpha = 0.25f),
                topLeft = Offset(ec.x - 3.5f * u, ec.y - iry + 0.8f * u * finalBlink),
                size = Size(7f * u, 2f * u * finalBlink)
            )

            if (finalBlink > 0.25f) {
                val pr = 2.3f * u
                val pc = Offset(
                    ec.x + finalLookX * 1.6f * u,
                    ec.y + finalLookY * 1.4f * u * finalBlink
                )
                
                // Зрачок с объемом
                drawOval(
                    Brush.radialGradient(listOf(Color(0xFF000000), Color(0xFF081C26)), center = pc, radius = pr),
                    topLeft = Offset(pc.x - pr, pc.y - pr * finalBlink),
                    size = Size(pr * 2f, pr * 2f * finalBlink)
                )
                
                // Блик на зрачке
                drawCircle(
                    Color.White.copy(alpha = 0.95f),
                    0.7f * u,
                    Offset(pc.x - 0.6f * u, pc.y - 0.8f * u * finalBlink)
                )
                // Отражающий свет (снизу)
                drawCircle(
                    Color(0xFF00FFFF).copy(alpha = 0.4f),
                    0.3f * u,
                    Offset(pc.x + 0.5f * u, pc.y + 0.6f * u * finalBlink)
                )
            }
            
            // Большой глянцевый блик на стекле глаза
            if (finalBlink > 0.3f) {
                drawOval(
                    Brush.radialGradient(listOf(Color.White.copy(alpha = 0.9f * finalBlink), Color.Transparent), center = Offset(ec.x - 1.6f * u, ec.y - 2.5f * u), radius = 3f * u),
                    topLeft = Offset(ec.x - 2.5f * u, ec.y - 3.5f * u * finalBlink),
                    size = Size(5f * u, 4f * u * finalBlink)
                )
            }
        }

        // ================= Нос =================
        // Создаем 3D-объем носа с тенью снизу
        drawRoundRect(
            Brush.verticalGradient(listOf(metalLight, metalDark)),
            topLeft = pt(48.4f, 64.2f),
            size = Size(3.2f * u, 2.2f * u),
            cornerRadius = CornerRadius(1.1f * u)
        )
        drawRoundRect(
            metalDeep,
            topLeft = pt(48.9f, 65.5f),
            size = Size(2.2f * u, 0.5f * u),
            cornerRadius = CornerRadius(0.2f * u)
        )
        drawCircle(Color.White.copy(alpha = 0.4f), 0.6f * u, pt(49.3f, 64.9f))

        // ================= Рот (Улучшенная детализация) =================
        // Тень от рта (подсветка челюсти)
        drawOval(
            Brush.radialGradient(listOf(Color(0xFF000000).copy(alpha = 0.3f), Color.Transparent), center = pt(50f, 73f), radius = 5f * u),
            topLeft = pt(45f, 71f),
            size = Size(10f * u, 4f * u)
        )

        if (isSpeaking) {
            for (i in 0..4) {
                val h = (3f + 4f * (0.5f + 0.5f * sin(finalPhase * 3f + i * 1.1f))) * u
                val x = 50f + (i - 2) * 2.6f
                
                // Подсветка рта
                glow(Offset(offX + x * u, offY + 73f * u - h / 2), h * 0.7f, cyan.copy(alpha = 0.4f))
                
                // Столбики с градиентом (3D)
                drawRoundRect(
                    Brush.verticalGradient(listOf(cyanBright, cyan, cyanDeep)),
                    topLeft = Offset(offX + (x - 0.7f) * u, offY + 73f * u - h),
                    size = Size(1.4f * u, h),
                    cornerRadius = CornerRadius(0.7f * u)
                )
            }
        } else if (isThinking) {
            for (i in 0..4) {
                val h = (2.5f + 2f * (0.5f + 0.5f * sin(finalPhase * 2f + i * 1.1f))) * u
                val x = 50f + (i - 2) * 2.6f
                
                glow(Offset(offX + x * u, offY + 73f * u - h / 2), h * 0.6f, cyan.copy(alpha = 0.3f))
                
                drawRoundRect(
                    Brush.verticalGradient(listOf(cyanBright, cyan, cyanDeep)),
                    topLeft = Offset(offX + (x - 0.7f) * u, offY + 73f * u - h),
                    size = Size(1.4f * u, h),
                    cornerRadius = CornerRadius(0.7f * u)
                )
            }
        } else {
            // Улыбка с многослойным свечением
            val smileTL = pt(44.5f, 62.5f)
            val smileSize = Size(13f * u, 11f * u)
            
            // Тень улыбки
            drawArc(
                Color(0xFF000000).copy(alpha = 0.4f),
                20f, 140f, false,
                topLeft = Offset(smileTL.x, smileTL.y + 0.3f * u),
                size = smileSize,
                style = Stroke(3f * u, cap = StrokeCap.Round)
            )
            
            // Внешнее свечение
            drawArc(
                cyan.copy(alpha = 0.4f),
                20f, 140f, false,
                topLeft = smileTL,
                size = smileSize,
                style = Stroke(3.5f * u, cap = StrokeCap.Round)
            )
            
            // Основная яркая линия
            drawArc(
                cyan,
                20f, 140f, false,
                topLeft = smileTL,
                size = smileSize,
                style = Stroke(1.5f * u, cap = StrokeCap.Round)
            )
            
            // Блик в самой высокой точке улыбки
            drawCircle(Color.White.copy(alpha = 0.8f), 0.3f * u, pt(50f, 60.5f))
        }
    }
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
    
    val robotOrbitAngle by transition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "robot_orbit"
    )
    
    var flightStarted by remember { mutableStateOf(false) }
    var startAngle by remember { mutableStateOf(0f) }
    
    val flightProgress by animateFloatAsState(
        targetValue = if (isTtsReady && flightStarted) 1f else 0f,
        animationSpec = tween(durationMillis = 3000, easing = FastOutSlowInEasing),
        label = "flight_progress"
    )
    
    LaunchedEffect(isTtsReady) {
        if (isTtsReady && !flightStarted) {
            startAngle = robotOrbitAngle
            flightStarted = true
        }
    }
    
    val robotOnOrbitAlpha = if (flightProgress < 0.15f) 1f - (flightProgress / 0.15f) else 0f
    
    val robotAlpha by animateFloatAsState(
        targetValue = if (flightProgress >= 0.99f) 1f else 0f,
        animationSpec = tween(durationMillis = 400),
        label = "robot_alpha"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(84.dp)
            .padding(4.dp)
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
            robotOnOrbitAlpha = robotOnOrbitAlpha
        )
        
        if (flightProgress > 0f && flightProgress < 1f) {
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val w = constraints.maxWidth.toFloat()
                val h = constraints.maxHeight.toFloat()
                val minDim = min(w, h)
                
                val orbitCenterX = w * SpaceConstants.ORBIT_CENTER_X_RATIO
                val orbitCenterY = h * SpaceConstants.ORBIT_CENTER_Y_RATIO
                val robotOrbitRx = minDim * SpaceConstants.ROBOT_ORBIT_RX
                val robotOrbitRy = minDim * SpaceConstants.ROBOT_ORBIT_RY
                
                val startX = orbitCenterX + cos(startAngle) * robotOrbitRx
                val startY = orbitCenterY + sin(startAngle) * robotOrbitRy
                
                val density = LocalDensity.current
                val logoWidth = with(density) { 56.dp.toPx() }
                val rightWidth = with(density) { 132.dp.toPx() }
                val robotCenterX = logoWidth + (w - logoWidth - rightWidth) / 2f
                val endX = robotCenterX
                val endY = h / 2f
                
                val ctrlX = (startX + endX) / 2f
                val ctrlY = startY - h * 0.4f
                
                val oneMinusT = 1f - flightProgress
                val currentX = oneMinusT * oneMinusT * startX + 
                               2f * oneMinusT * flightProgress * ctrlX + 
                               flightProgress * flightProgress * endX
                val currentY = oneMinusT * oneMinusT * startY + 
                               2f * oneMinusT * flightProgress * ctrlY + 
                               flightProgress * flightProgress * endY
                
                val startSize = h * SpaceConstants.ROBOT_SIZE_RATIO
                val density2 = LocalDensity.current
                val endSizePx = with(density2) { 70.dp.toPx() }
                val currentSize = startSize + (endSizePx - startSize) * flightProgress
                
                // Вектор движения (для ориентации в полете)
                val dx = 2f * oneMinusT * (ctrlX - startX) + 
                         2f * flightProgress * (endX - ctrlX)
                val dy = 2f * oneMinusT * (ctrlY - startY) + 
                         2f * flightProgress * (endY - ctrlY)
                val angleRad = atan2(dy, dx)
                val angleDeg = angleRad * 180f / PI.toFloat()
                
                // Плавная коррекция угла при приземлении
                val landingProgress = if (flightProgress > 0.7f) {
                    (flightProgress - 0.7f) / 0.3f
                } else {
                    0f
                }
                
                // Стартовый угол полета (в градусах), от которого мы плавно отходим
                val flightAngleDeg = angleDeg + 90f
                // Целевой угол - 0 градусов (стоит ровно)
                val targetAngle = 0f
                
                // ПЛАВНАЯ ИНТЕРПОЛЯЦИЯ УГЛА: 
                // Пока не началось приземление (landingProgress < 1), 
                // угол = текущий угол полета. 
                // Как только началось приземление, угол плавно переходит к 0.
                val finalAngle = flightAngleDeg * (1f - landingProgress) + targetAngle * landingProgress
                
                val scale = currentSize / endSizePx
                val offsetXDp = with(density2) { (currentX - endSizePx / 2f).toDp() }
                val offsetYDp = with(density2) { (currentY - endSizePx / 2f).toDp() }
                
                Box(
                    modifier = Modifier
                        .offset(x = offsetXDp, y = offsetYDp)
                        .size(70.dp)
                        .graphicsLayer(
                            scaleX = scale,
                            scaleY = scale,
                            rotationZ = finalAngle
                        )
                ) {
                    ThinkingRobotAnimation(
                        height = 70.dp,
                        isActive = false,
                        isSpeaking = false,
                        isThinking = false,
                        isIdle = true,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }

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
                if (robotAlpha > 0.01f) {
                    Box(modifier = Modifier.graphicsLayer(alpha = robotAlpha)) {
                        ThinkingRobotAnimation(
                            height = 70.dp,
                            isActive = isGenerating,
                            isSpeaking = isSpeaking,
                            isThinking = isGenerating,
                            isIdle = isLocalReady || isCloudReady,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
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

    Canvas(modifier = modifier) {
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
        // Огромный фиолетовый вихрь слева снизу
        drawCircle(
            Brush.radialGradient(
                listOf(Color(0xFF9B59B6).copy(alpha = 0.08f * vis), Color.Transparent),
                center = Offset(w * 0.18f, h * 0.75f), radius = h * 0.7f
            ),
            radius = h * 0.7f, center = Offset(w * 0.18f, h * 0.75f)
        )
        // Зеленый газовый пузырь справа сверху
        drawCircle(
            Brush.radialGradient(
                listOf(Color(0xFF2ECC71).copy(alpha = 0.05f * vis), Color.Transparent),
                center = Offset(w * 0.75f, h * 0.25f), radius = h * 0.5f
            ),
            radius = h * 0.5f, center = Offset(w * 0.75f, h * 0.25f)
        )
        // Дымка из звездной пыли, расширяющаяся от центра
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
        for (i in 0 until 70) { // Увеличил количество звезд с 46 до 70
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
        // Квазар 1 (далекий яркий объект с мощными лучами)
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

        // Квазар 2 (смещенный вправо вниз)
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

        // Сверхновая (взрывающаяся звезда в фоне)
        val snX = w * 0.92f; val snY = h * 0.55f
        val snLife = (T * 0.7f) % (2f * PI.toFloat()) // Жизненный цикл взрыва
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

        // ===== ОРБИТЫ (как в старой версии) =====
        val orbitColor = Color(0xFF7FB4FF)
        val mRx = w * 0.27f; val mRy = h * 0.22f
        val eRx = w * 0.37f; val eRy = h * 0.33f
        val sRx = w * 0.46f; val sRy = h * 0.42f
        val rRx = w * SpaceConstants.ROBOT_ORBIT_RX
        val rRy = h * SpaceConstants.ROBOT_ORBIT_RY

        val orbits = listOf(
            Triple(rRx, rRy, -10f),
            Triple(mRx, mRy, -8f),
            Triple(eRx, eRy, 6f),
            Triple(sRx, sRy, 12f)
        )
        for (orbit in orbits) {
            val rx = orbit.first
            val ry = orbit.second
            val rot = orbit.third
            rotate(rot, pivot = Offset(cx, cy)) {
                drawOval(
                    orbitColor.copy(alpha = 0.14f * vis),
                    topLeft = Offset(cx - rx, cy - ry),
                    size = Size(rx * 2f, ry * 2f),
                    style = Stroke(width = h / 70f)
                )
            }
        }

        // ===== ЭЛЕКТРИЧЕСКОЕ СОЛНЦЕ (пульсирующая сверхновая) =====
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

        // ===== РОБОТ НА ОРБИТЕ =====
        if (robotOnOrbitAlpha > 0.01f) {
            val robotX = cx + cos(robotOrbitAngle) * rRx
            val robotY = cy + sin(robotOrbitAngle) * rRy
            val robotSize = h * SpaceConstants.ROBOT_SIZE_RATIO
            rotate(
                degrees = (robotOrbitAngle * 180f / PI.toFloat()) + 90f,
                pivot = Offset(robotX, robotY)
            ) {
                drawRoundRect(
                    color = Color(0xFFCCCCCC).copy(alpha = robotOnOrbitAlpha),
                    topLeft = Offset(robotX - robotSize * 0.4f, robotY - robotSize * 0.5f),
                    size = Size(robotSize * 0.8f, robotSize),
                    cornerRadius = CornerRadius(robotSize * 0.15f)
                )
                drawRoundRect(
                    color = Color(0xFFDDDDDD).copy(alpha = robotOnOrbitAlpha),
                    topLeft = Offset(robotX - robotSize * 0.35f, robotY - robotSize * 0.95f),
                    size = Size(robotSize * 0.7f, robotSize * 0.5f),
                    cornerRadius = CornerRadius(robotSize * 0.1f)
                )
                drawLine(
                    color = Color(0xFF888888).copy(alpha = robotOnOrbitAlpha),
                    start = Offset(robotX, robotY - robotSize * 0.95f),
                    end = Offset(robotX, robotY - robotSize * 1.2f),
                    strokeWidth = 1.dp.toPx()
                )
                drawCircle(
                    color = Color(0xFFFF0000).copy(alpha = robotOnOrbitAlpha),
                    radius = robotSize * 0.08f,
                    center = Offset(robotX, robotY - robotSize * 1.2f)
                )
                drawCircle(
                    color = Color(0xFF4499FF).copy(alpha = robotOnOrbitAlpha),
                    radius = robotSize * 0.10f,
                    center = Offset(robotX - robotSize * 0.18f, robotY - robotSize * 0.70f)
                )
                drawCircle(
                    color = Color(0xFF4499FF).copy(alpha = robotOnOrbitAlpha),
                    radius = robotSize * 0.10f,
                    center = Offset(robotX + robotSize * 0.18f, robotY - robotSize * 0.70f)
                )
            }
        }

        // ===== ЛУНА (жёлтая) =====
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
        // Траектория: входит слева-сверху, выходит справа-снизу
        val comP = (T * 0.15f) % 1f
        val cometX = lerp(-w * 0.2f, w * 1.3f, comP)
        val cometY = lerp(h * 1.0f, -h * 0.2f, comP) // Идет снизу вверх
        val cometAngle = atan2(-cometY, cometX)
        val cometRadius = h * 0.015f
        val cometTailLen = h * 0.18f * (1f - comP) // Хвост укорачивается
        
        rotate(degrees = cometAngle * 180f / PI.toFloat(), pivot = Offset(cometX, cometY)) {
            // Яркое свечение ядра
            drawCircle(
                Brush.radialGradient(
                    listOf(Color.White.copy(alpha = 0.9f * vis), Color(0xFF00FFFF).copy(alpha = 0.5f * vis), Color.Transparent),
                    center = Offset(cometX, cometY),
                    radius = cometRadius * 5f
                ),
                radius = cometRadius * 5f,
                center = Offset(cometX, cometY)
            )
            
            // Хвост (состоящий из частиц, убывающих по яркости)
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
            
            // Ядро кометы
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

        // ===================== ЛИНЗА ВРЕМЕНИ (Световой эффект по краям) =====================
        drawCircle(
            Brush.radialGradient(
                listOf(Color.Transparent, Color.Black.copy(alpha = 0.35f * vis)),
                center = Offset(cx, cy),
                radius = h * 0.9f
            ),
            radius = h * 0.9f,
            center = Offset(cx, cy)
        )
    }
}

// Вспомогательная функция для интерполяции (добавляется вне Composable)
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
