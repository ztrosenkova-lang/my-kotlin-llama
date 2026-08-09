package org.nehuatl.sample

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
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
import androidx.compose.runtime.remember

private val AppBackground = Color(0xFFFFFFFF)
private val SurfaceGray = Color(0xFFF1F3F5)
private val BorderGray = Color(0xFFCED4DA)
private val AccentColor = Color(0xFF74C0FC)
private val DarkText = Color(0xFF212529)
private val ChatFontFamily = FontFamily.Monospace
private val GreenColor = Color(0xFF4CD964)
private val PaleYellowColor = Color(0xFFFFF9DB)
private val FriendlyRobotColor = Color(0xFF00B4D8)

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

    // НОВАЯ ПОДПИСКА: имя загруженной модели
    val loadedModelName by viewModel.loadedModelName.collectAsStateWithLifecycle(initialValue = "")

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
        if (hasRecordPermission) {
            viewModel.enableTts()
        } else {
            viewModel.appendSystemMessage("⚠️ Для работы распознавания речи требуется разрешение на запись аудио.")
        }
    }

    val fullWelcomeString = "Привет! Я твой персональный ИИ-Друг, это лучший хранитель паролей,переводчик со всех языков и просто умный собеседник! 🤖✨ Я создан, чтобы доказать: искусственный интеллект — это твой надежный и полностью автономный союзник, защищенный от любых внешних блокировок!\n\nВот что я умею:\n🎤 СЛЫШАТЬ И ГОВОРИТЬ: Нажми микрофон в подвале, чтобы общаться голосом.\n🧠 РАЗДЕЛЬНАЯ ПАМЯТЬ:Я обладаю тремя видами памятии об этом ты можешь узнать в справке\n А также я могу напоминать тебе о важных событиях! \n\n Давай общаться! Включи локальный движок Llama или облачный ИИ в шапке приложения, и погнали!"

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(20000)
        viewModel.speakText(fullWelcomeString)
        var runningText = ""
        for (i in fullWelcomeString.indices) {
            runningText += fullWelcomeString[i]
            viewModel.updateLastSystemMessage(runningText)
            delay(35)
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
            viewModel = viewModel
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
            }
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
            onHelpClick = { showHelpDialog = true },
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
                                "assistant" -> "ИИ: "
                                "system" -> "📢 "
                                else -> ""
                            }
                            Text(
                                text = prefix + message.text,
                                color = DarkText,
                                fontFamily = ChatFontFamily,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        }

                        if (generatedText.isNotEmpty() && state is GenerationState.Generating) {
                            Text(
                                text = "ИИ: $generatedText",
                                color = DarkText,
                                fontFamily = ChatFontFamily,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        }

                        if (cloudGeneratedText.isNotEmpty() && cloudState is CloudAIState.Generating) {
                            Text(
                                text = "☁️ ИИ: $cloudGeneratedText",
                                color = DarkText.copy(alpha = 0.8f),
                                fontFamily = ChatFontFamily,
                                style = MaterialTheme.typography.bodyLarge,
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

        // ОБНОВЛЁННЫЙ КОМПОНЕНТ: отображает имя загруженной модели
        DeviceBindingStatus(
            isBound = isDeviceBound,
            modelName = loadedModelName,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
        )

        PromptInput(
            prompt = promptInput,
            onPromptChange = { promptInput = it },
            onGenerate = {
                if (promptInput.isNotBlank()) {
                    keyboardController?.hide()
                    viewModel.sendUserMessage(promptInput)
                    promptInput = ""
                    onImageUsed()
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
            focusRequester = focusRequester,
            isTtsReady = isTtsReady,
            viewModel = viewModel,
            context = context,
            speechRecognizerLauncher = speechRecognizerLauncher,
            modifier = Modifier.padding(8.dp)
        )
    }
}

// ОБНОВЛЁННЫЙ КОМПОНЕНТ: Отображение имени загруженной модели
@Composable
private fun DeviceBindingStatus(
    isBound: Boolean,
    modelName: String,
    modifier: Modifier = Modifier
) {
    // Состояние для текста с эффектом печатной машинки
    var displayedText by remember { mutableStateOf("") }
    // Флаг, показывающий, завершена ли анимация печати
    var isTypingComplete by remember { mutableStateOf(false) }

    // Эффект для анимации печатной машинки
    LaunchedEffect(isBound, modelName) {
        if (isBound && modelName.isNotEmpty()) {
            val fullText = "Модель: $modelName"
            displayedText = ""
            isTypingComplete = false
            for (i in fullText.indices) {
                displayedText += fullText[i]
                delay(35) // Скорость печати
            }
            isTypingComplete = true
        } else if (!isBound) {
            displayedText = "🔓 Устройство не привязано"
            isTypingComplete = true
        } else if (isBound && modelName.isEmpty()) {
            displayedText = "✅ Устройство защищено"
            isTypingComplete = true
        }
    }

    val textColor = when {
        !isBound -> Color.Red
        modelName.isNotEmpty() -> GreenColor
        else -> GreenColor
    }

    val statusText = when {
        !isBound -> "🔓 Устройство не привязано"
        modelName.isNotEmpty() && displayedText.isNotEmpty() -> displayedText
        else -> "✅ Устройство защищено"
    }

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = SurfaceGray.copy(alpha = 0.7f)
        ),
        border = BorderStroke(1.dp, BorderGray),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = statusText,
            color = textColor,
            fontSize = 10.sp,
            fontFamily = ChatFontFamily,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            textAlign = TextAlign.Center
        )
    }
}

// === ЭКРАН БЛОКИРОВКИ ===
@Composable
private fun LockScreen(
    secretPhrase: String,
    onSecretPhraseChange: (String) -> Unit,
    onVerify: () -> Unit,
    viewModel: MainViewModel
) {
    val context = LocalContext.current

    BackHandler(enabled = true) {
        // Ничего не делаем — блокируем выход
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceGray),
        contentAlignment = Alignment.Center
    ) {
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
                visualTransformation = PasswordVisualTransformation(),
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
                        // Игнорируем ошибки вибрации
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

@Composable
private fun TopBarWithSwitch(
    currentMode: AIMode,
    onModeChange: (AIMode) -> Unit,
    isModelLoaded: Boolean,
    cloudConfig: CloudAIConfig?,
    onCloudForceDialog: () -> Unit,
    onLocalForceDialog: () -> Unit,
    statusText: String = ""
) {
    val isLocalReady = isModelLoaded
    val isCloudReady = cloudConfig?.authKey?.isNotEmpty() == true
    val localIndicatorColor = if (isLocalReady) GreenColor else PaleYellowColor
    val cloudIndicatorColor = if (isCloudReady) GreenColor else PaleYellowColor

    val infiniteTransition = rememberInfiniteTransition()
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1200,
                easing = LinearEasing
            )
        )
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .padding(4.dp)
            .border(1.dp, BorderGray, RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = R.mipmap.ic_launcher),
            contentDescription = "Лого",
            modifier = Modifier
                .size(56.dp)
                .padding(end = 8.dp)
                .clip(RoundedCornerShape(12.dp)),
            contentScale = ContentScale.Crop
        )

        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "+",
                fontSize = 20.sp,
                color = AccentColor,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.rotate(
                    if (statusText.contains("думает")) rotationAngle else 0f
                )
            )

            Text(
                text = "ИИ-Друг",
                style = MaterialTheme.typography.titleMedium,
                color = AccentColor,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Text(
                text = "+",
                fontSize = 20.sp,
                color = AccentColor,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.rotate(
                    if (statusText.contains("думает")) rotationAngle else 0f
                )
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
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "☁️ Настройки облачного ИИ",
                style = MaterialTheme.typography.titleLarge,
                color = DarkText
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Введите данные для подключения к облачному ИИ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = DarkText
                )

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("🔵 GigaChat", color = DarkText)
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
                    Text("🌐 Другой провайдер", color = DarkText)
                }

                OutlinedTextField(
                    value = apiUrl,
                    onValueChange = onApiUrlChange,
                    label = { Text("API URL", color = DarkText) },
                    placeholder = {
                        Text(
                            if (isGigaChat) "https://gigachat.devices.sberbank.ru/api/v1/chat/completions"
                            else "https://openrouter.ai/api/v1/chat/completions",
                            color = DarkText.copy(alpha = 0.5f)
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
                            color = DarkText
                        )
                    },
                    placeholder = {
                        Text(
                            if (isGigaChat) "Введите ключ из Сбер Студии"
                            else "Введите ваш API ключ",
                            color = DarkText.copy(alpha = 0.5f)
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
                        containerColor = if (authKey.isNotBlank()) AccentColor else BorderGray
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isGeneratingToken) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = DarkText, strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Получение токена...", color = DarkText)
                    } else {
                        Text(
                            text = if (isCloudReady) "✅ Токен подключен" else if (isGigaChat) "🔑 Получить токен" else "🔑 Установить ключ",
                            color = if (isCloudReady) GreenColor else DarkText,
                            fontWeight = if (isCloudReady) FontWeight.Bold else FontWeight.Normal
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
            }
        },
        confirmButton = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
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
                        containerColor = AccentColor
                    ),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier.height(28.dp)
                ) {
                    Text(
                        text = "Сохранить",
                        color = DarkText,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Button(
                    onClick = onClear,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentColor
                    ),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier.height(28.dp)
                ) {
                    Text(
                        text = "Очистить",
                        color = DarkText,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentColor
                    ),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier.height(28.dp)
                ) {
                    Text(
                        text = "Закрыть",
                        color = DarkText,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    )
}

@Composable
private fun HelpDialog(
    onDismiss: () -> Unit,
    viewModel: MainViewModel
) {
    LaunchedEffect(Unit) {
        viewModel.speakText("Открываю руководство пользователя. Здесь вы можете прочитать инструкции по управлению моими локальными движками, настройке раздельной памяти и командам умного поиска по корням слов.")
    }

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
                placeholder = { Text("Вставь сюда свой прайс-лист или данные...", color = DarkText.copy(alpha = 0.5f)) },
                modifier = Modifier.fillMaxWidth().height(400.dp),
                maxLines = 100,
                singleLine = false,
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
    Dialog(onDismissRequest = onDismiss) {
        Card(colors = CardDefaults.cardColors(containerColor = SurfaceGray)) {
            Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Настройка ИИ", style = MaterialTheme.typography.headlineSmall, color = DarkText)

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Языковая модель", color = DarkText)
                    val displayModelPath = currentModelPath?.substringAfterLast("/")?.replace("primary%3AModels%", "") ?: "Не выбрана"
                    Text(text = "Текущая модель: $displayModelPath", style = MaterialTheme.typography.bodySmall, color = DarkText.copy(alpha = 0.7f))
                    Button(
                        onClick = onPickModel,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = BorderGray)
                    ) {
                        Text(
                            text = if (currentModelPath != null) "Изменить модель" else "Выбрать модель",
                            color = if (currentModelPath != null) GreenColor else DarkText
                        )
                    }
                }

                Column(modifier = Modifier.padding(vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("(опционально)", style = MaterialTheme.typography.bodySmall, color = DarkText.copy(alpha = 0.6f))
                    Text("Мультимодальный проектор (mmproj)", color = DarkText)
                    val displayMmprojPath = mmprojPath?.substringAfterLast("/")?.replace("primary%3AModels%", "") ?: "Не выбран"
                    Text(text = "Текущий проектор: $displayMmprojPath", style = MaterialTheme.typography.bodySmall, color = DarkText.copy(alpha = 0.7f))
                    Button(
                        onClick = onPickMmproj,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = BorderGray)
                    ) {
                        Text(
                            text = if (mmprojPath != null) "Изменить проектор" else "Выбрать проектор",
                            color = if (mmprojPath != null) GreenColor else DarkText
                        )
                    }
                }

                Button(
                    onClick = onLoad,
                    enabled = currentModelPath != null,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentColor)
                ) {
                    Text("Запустить нейросеть", color = DarkText)
                }

                TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                    Text("Отмена", color = DarkText)
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
    focusRequester: FocusRequester,
    isTtsReady: Boolean,
    viewModel: MainViewModel,
    context: android.content.Context,
    speechRecognizerLauncher: androidx.activity.result.ActivityResultLauncher<Intent>,
    modifier: Modifier = Modifier
) {
    val memoryInfoText by viewModel.memoryInfoText.collectAsStateWithLifecycle(initialValue = "Загрузка памяти...")

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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Левая группа: Plus и Delete
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
                            enabled = enabled && !isGenerating,
                            modifier = Modifier.size(41.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Добавить изображение",
                                tint = if (enabled && !isGenerating) AccentColor else DarkText.copy(alpha = 0.4f)
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

                // Центр: Поле ввода
                OutlinedTextField(
                    value = prompt,
                    onValueChange = onPromptChange,
                    modifier = Modifier
                        .weight(1f)
                        .focusRequester(focusRequester),
                    enabled = enabled && !isGenerating,
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

                // Правая группа: Микрофон и Отправить/Стоп
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

                    // Send/Stop Button
                    Box(
                        modifier = Modifier
                            .size(41.dp)
                            .background(Color(0xFFF1F3F5), shape = CircleShape)
                            .border(1.dp, BorderGray, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isGenerating) {
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
                                enabled = enabled && prompt.isNotBlank(),
                                modifier = Modifier.size(41.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ArrowUpward,
                                    contentDescription = "Отправить",
                                    tint = if (enabled && prompt.isNotBlank()) AccentColor else DarkText.copy(alpha = 0.4f)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = memoryInfoText,
                color = DarkText.copy(alpha = 0.6f),
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
