package org.nehuatl.sample

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.Build
import android.os.ParcelFileDescriptor
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.speech.RecognizerIntent
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColor
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.rememberTextMeasurer
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
import java.io.File
import java.util.Random
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

// Длительность плавной смены темы. 600–900 мс — комфортно.
// Хочешь затянуть — ставь 1200–1500. Больше 1500 — уже раздражает.
private const val THEME_ANIMATION_MS = 4500

private data class AppColors(
    val background: Color,
    val surfaceGray: Color,
    val borderGray: Color,
    val accent: Color,
    val text: Color,
    val chatFont: FontFamily,
    val green: Color,
    val paleYellow: Color,
    val gradTop: Color,
    val gradMid: Color,
    val gradBottom: Color
)

private val LightColors = AppColors(
    background = Color(0xFFFFFFFF),
    surfaceGray = Color(0xFFF1F3F5),
    borderGray = Color(0xFFCED4DA),
    accent = Color(0xFF74C0FC),
    text = Color(0xFF212529),
    chatFont = FontFamily.Monospace,
    green = Color(0xFF2E7D32),
    paleYellow = Color(0xFFFFF9DB),
    gradTop = Color(0xFFFFFDF5),
    gradMid = Color(0xFFFFF8DC),
    gradBottom = Color(0xFFF0E0B8)
)

private val DarkColors = AppColors(
    background = Color(0xFF121212),
    surfaceGray = Color(0xFF1E1E1E),
    borderGray = Color(0xFF3A3A3A),
    accent = Color(0xFF90CAF9),
    text = Color(0xFFE0E0E0),
    chatFont = FontFamily.Monospace,
    green = Color(0xFF81C784),
    paleYellow = Color(0xFF2A2A1E),
    gradTop = Color(0xFF3A3A2E),
    gradMid = Color(0xFF2A2A1E),
    gradBottom = Color(0xFF1A1A10)
)

// Глобальный доступ к анимированным цветам темы из любого экрана.
// Позволяет вложенным диалогам получать те же плавно меняющиеся цвета,
// что и основной экран.
private val LocalAppColors = staticCompositionLocalOf<AppColors> {
    error("LocalAppColors not provided")
}
enum class AIMode {
    LOCAL,
    NEUTRAL,
    CLOUD
}
// Очищает URI от мусорных символов и возвращает чистое имя файла
private fun cleanModelName(path: String?): String {
    if (path.isNullOrEmpty()) return ""
    // Декодируем URL-encoded символы (%2F → /, %3A → : и т.д.)
    val decoded = try {
        java.net.URLDecoder.decode(path, "UTF-8")
    } catch (e: Exception) {
        path
    }
    // Берём имя файла после последнего слэша
    val fileName = decoded.substringAfterLast("/")
    // Убираем префиксы типа "primary:Models:"
    return fileName
        .replace(Regex("^primary:Models:"), "")
        .replace(Regex("^primary:Models%3A"), "")
        .trim()
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
    modelDisplayName: String? = null,
    mmprojDisplayName: String? = null,
    onPickModel: () -> Unit,
    onPickMmproj: () -> Unit,
    onPickImage: () -> Unit,
    onTakePhoto: () -> Unit = {},
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
    val isPreparingSpeech by viewModel.isPreparingSpeech.collectAsStateWithLifecycle(initialValue = false)
    val organizerEntries by viewModel.organizerEntries.collectAsStateWithLifecycle(initialValue = emptyList())
    val hasActiveAlarm = organizerEntries.isNotEmpty()
    val speakStartTrigger by viewModel.speakStartTrigger.collectAsStateWithLifecycle(initialValue = false)
    val robotGreetingSignal by viewModel.robotGreetingSignal.collectAsStateWithLifecycle(initialValue = false)
    val robotAutoAnimation by viewModel.robotAutoAnimation.collectAsStateWithLifecycle(initialValue = 0)
    val robotJumpSignal by viewModel.robotJumpSignal.collectAsStateWithLifecycle(initialValue = false)
    val pendingTextToPrint by viewModel.pendingTextToPrint.collectAsStateWithLifecycle(initialValue = "")
    val isDarkTheme by viewModel.isDarkTheme.collectAsStateWithLifecycle(initialValue = false)
    val showBrainEditorState by viewModel.showBrainEditor.collectAsStateWithLifecycle(initialValue = false)
    val showChatEditorState by viewModel.showChatEditor.collectAsStateWithLifecycle(initialValue = false)
    val isFloatingRobotRunning by viewModel.floatingRobotRunning.collectAsStateWithLifecycle(initialValue = false)
    val isCompressing by viewModel.isCompressing.collectAsStateWithLifecycle(initialValue = false)
    val visionPhase by viewModel.visionPhase.collectAsStateWithLifecycle(initialValue = VisionPhase.IDLE)
    val isSmartMode by viewModel.isSmartMode.collectAsStateWithLifecycle(initialValue = false)
    val activeTranslationPrompt by viewModel.activeTranslationPrompt.collectAsStateWithLifecycle(initialValue = null)

       // Единый Transition для всех цветов темы — плавная одновременная смена
    val themeTransition = updateTransition(
        targetState = isDarkTheme,
        label = "theme_transition"
    )

    val themeSpec = tween<Color>(
        durationMillis = THEME_ANIMATION_MS,
        easing = FastOutSlowInEasing
    )

    val background by themeTransition.animateColor(transitionSpec = { themeSpec }, label = "bg_color") {
        if (it) DarkColors.background else LightColors.background
    }
    val surfaceGray by themeTransition.animateColor(transitionSpec = { themeSpec }, label = "surface_color") {
        if (it) DarkColors.surfaceGray else LightColors.surfaceGray
    }
    val borderGray by themeTransition.animateColor(transitionSpec = { themeSpec }, label = "border_color") {
        if (it) DarkColors.borderGray else LightColors.borderGray
    }
    val accent by themeTransition.animateColor(transitionSpec = { themeSpec }, label = "accent_color") {
        if (it) DarkColors.accent else LightColors.accent
    }
    val text by themeTransition.animateColor(transitionSpec = { themeSpec }, label = "text_color") {
        if (it) DarkColors.text else LightColors.text
    }
    val green by themeTransition.animateColor(transitionSpec = { themeSpec }, label = "green_color") {
        if (it) DarkColors.green else LightColors.green
    }
       val paleYellow by themeTransition.animateColor(transitionSpec = { themeSpec }, label = "pale_yellow_color") {
        if (it) DarkColors.paleYellow else LightColors.paleYellow
    }

    val gradTop by themeTransition.animateColor(transitionSpec = { themeSpec }, label = "grad_top_color") {
        if (it) DarkColors.gradTop else LightColors.gradTop
    }
    val gradMid by themeTransition.animateColor(transitionSpec = { themeSpec }, label = "grad_mid_color") {
        if (it) DarkColors.gradMid else LightColors.gradMid
    }
    val gradBottom by themeTransition.animateColor(transitionSpec = { themeSpec }, label = "grad_bottom_color") {
        if (it) DarkColors.gradBottom else LightColors.gradBottom
    }

    val colors = AppColors(
        background = background,
        surfaceGray = surfaceGray,
        borderGray = borderGray,
        accent = accent,
        text = text,
        chatFont = if (isDarkTheme) DarkColors.chatFont else LightColors.chatFont,
        green = green,
        paleYellow = paleYellow,
        gradTop = gradTop,
        gradMid = gradMid,
        gradBottom = gradBottom
    )
    CompositionLocalProvider(LocalAppColors provides colors) {
    var promptInput by remember { mutableStateOf("") }
    var showModelDialog by remember { mutableStateOf(false) }
    var showDownloadDialog by remember { mutableStateOf(false) }
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
    // Диалог разблокировки по команде «режим админа» (только в NEUTRAL)
    var showAdminUnlockDialog by remember { mutableStateOf(false) }
    var adminPhraseInput by remember { mutableStateOf("") }
    var showMediaPicker by remember { mutableStateOf(false) }
    var showBiblioteka by remember { mutableStateOf(false) }
    var showOrganizerEditor by remember { mutableStateOf(false) }

    // Состояние диалога выбора страниц PDF
    var pendingPdfUri by remember { mutableStateOf<Uri?>(null) }
    var showPdfPagePicker by remember { mutableStateOf(false) }
    var pdfPageInput by remember { mutableStateOf("") }

    // Очередь страниц PDF для последовательной отправки в vision.
    // Каждый элемент — file:// URI готового JPEG.
    var pdfPageQueue by remember { mutableStateOf<List<String>>(emptyList()) }
    var pdfQueueActive by remember { mutableStateOf(false) }
    var pdfQueueCurrentIndex by remember { mutableStateOf(0) }
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
    // Флаг «палец сейчас двигает робота» — защита от откатов при перетаскивании
    var isRobotDragging by remember { mutableStateOf(false) }
    // Реакции робота на пользователя
    var headTiltTarget by remember { mutableStateOf(0f) }
    var headNodTarget by remember { mutableStateOf(0f) }
    // Направление махания рукой при тапе: -1f влево, +1f вправо, 0f нет
    var tapWaveDirection by remember { mutableStateOf(0f) }

    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()

    // ========== СООБЩАЕМ MAINVIEWMODEL О ВИДИМОСТИ CHATSCREEN ==========
    // Нужно, чтобы MainViewModel знал: писать ответ через печатную машинку
    // (ChatScreen активен) или сразу в _chatHistory (ChatScreen в фоне).
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> viewModel.setChatScreenVisible(true)
                Lifecycle.Event.ON_PAUSE -> viewModel.setChatScreenVisible(false)
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            viewModel.setChatScreenVisible(false)
        }
    }
    val pdfPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            if (currentMode != AIMode.LOCAL && currentMode != AIMode.CLOUD) {
                viewModel.appendSystemMessage("⚠️ Анализ PDF доступен только в локальном или облачном режиме. Переключитесь на Local или Cloud.")
                return@rememberLauncherForActivityResult
            }
            pendingPdfUri = uri
            pdfPageInput = ""
            showPdfPagePicker = true
        }
    }

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
                        command == "махни рукой" -> {waveSignal = true
                   }
                        command == "расти" || command == "рости" -> {
                            growBigSignal = true
                        }
                        command == "сдуйся" || command == "сдуйсь" -> {
                            shrinkSmallSignal = true
                        }
                        command == "выйди из матрицы" -> {
                            if (!FloatingRobotService.isRunning) {
                                (context as? MainActivity)?.startFloatingWithPermissionCheck()
                            }
                        }
                        command == "уйди" -> {
                            if (FloatingRobotService.isRunning) {
                                val stopIntent = Intent(context, FloatingRobotService::class.java).apply {
                                    action = FloatingRobotService.ACTION_STOP
                                }
                                context.startService(stopIntent)
                            }
                        }
                       command == "библиотека" -> {
                            viewModel.sendUserMessage(recognizedText)
                        }
                        command == "умный режим" -> {
                            viewModel.enableSmartMode()
                        }
                        command == "режим калькулятора" -> {
                            viewModel.disableSmartMode()
                        }
                       command == "режим админа" -> {
                            if (currentMode == AIMode.NEUTRAL) {
                                adminPhraseInput = ""
                                showAdminUnlockDialog = true
                            } else {
                                viewModel.appendSystemMessage("⚠️ Команда «режим админа» работает только в нейтральном режиме")
                            }
                        }
                        command == "редактировать чат" -> {
                            viewModel.showChatEditor()
                        }
                        command == "редактировать органайзер" -> {
                            showOrganizerEditor = true
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

        LaunchedEffect(isTtsReady, isAppLocked) {
        if (isTtsReady && !welcomeStarted && !isAppLocked) {
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
            // Печать финального текста началась — «Готовлю озвучку» больше не актуально.
            // Сбрасываем сразу, не дожидаясь SPEAK_START/5-секундной страховки.
            viewModel.setPreparingSpeech(false)
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

    // Отслеживаем переход isThinking: true → false.
    // Когда модель закончила думать, крышка на голове робота ещё
    // закрывается (2000 мс, см. headPanelOpenAmount). «Готовлю озвучку»
    // показываем только после того, как крышка закрылась.
    var prevIsThinking by remember { mutableStateOf(false) }
    val currentThinking = state is GenerationState.Generating || cloudState is CloudAIState.Generating

    LaunchedEffect(currentThinking) {
        if (prevIsThinking && !currentThinking) {
            // Модель только что закончила. Ждём, пока крышка закроется.
            delay(2000)
            // Показываем «Готовлю озвучку» только если ответ ещё не начал
            // звучать и TTS включён.
            if (isTtsReady && !isSpeaking) {
                viewModel.setPreparingSpeech(true)
            }
        }
        prevIsThinking = currentThinking
    }
        LaunchedEffect(isPreparingSpeech) {
        if (isPreparingSpeech) {
            delay(5000)
            if (isPreparingSpeech && !isSpeaking) {
                viewModel.setPreparingSpeech(false)
            }
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

    // ========== ОЧЕРЕДЬ СТРАНИЦ PDF → VISION ==========
    // Отправляет страницы по одной. Следующая уходит только когда предыдущая
    // полностью обработана: vision не занята, модель не думает, TTS не говорит.
    // Защита от «⏳ Зрение загружается» на 2-й и 3-й страницах.
    LaunchedEffect(pdfQueueActive, pdfQueueCurrentIndex, visionPhase, state::class, state, cloudState, isSpeaking, isPreparingSpeech) {
        if (!pdfQueueActive) return@LaunchedEffect
        if (pdfQueueCurrentIndex >= pdfPageQueue.size) {
            pdfQueueActive = false
            pdfQueueCurrentIndex = 0
            pdfPageQueue = emptyList()
            return@LaunchedEffect
        }

        val visionBusy = visionPhase == VisionPhase.RELEASING_TEXT ||
                         visionPhase == VisionPhase.LOADING_VISION ||
                         visionPhase == VisionPhase.RESTORING_TEXT
        if (visionBusy) return@LaunchedEffect

        val modelBusy = state is GenerationState.Generating ||
                        state is GenerationState.LoadingModel ||
                        state is GenerationState.AnalyzingImage ||
                        cloudState is CloudAIState.Generating
        if (modelBusy) return@LaunchedEffect

        if (isSpeaking || isPreparingSpeech) return@LaunchedEffect

        if (visionPhase == VisionPhase.VISION_READY) return@LaunchedEffect

        val path = pdfPageQueue[pdfQueueCurrentIndex]
        pdfQueueCurrentIndex = pdfQueueCurrentIndex + 1
        viewModel.sendUserMessage("", path)
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
       } else {

        if (showModelDialog) {
                ModelPickerDialog(
            currentModelPath = currentModelPath,
            mmprojPath = mmprojPath,
            modelDisplayName = modelDisplayName,
            mmprojDisplayName = mmprojDisplayName,
            onPickModel = onPickModel,
            onPickMmproj = onPickMmproj,
            onLoad = {
                showModelDialog = false
                if (currentModelPath != null) {
                    viewModel.loadModel(currentModelPath, mmprojPath)
                    viewModel.setCurrentMode(AIMode.LOCAL)
                }
            },
            onOpenDownloadDialog = {
                showModelDialog = false
                showDownloadDialog = true
            },
            onDismiss = { showModelDialog = false },
            colors = colors,
            isDarkTheme = isDarkTheme
        )
    }

    if (showDownloadDialog) {
        ModelDownloadDialog(
            viewModel = viewModel,
            onDismiss = { showDownloadDialog = false },
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
                onRestoreBase = { viewModel.restoreBaseMemory() },
                onDismiss = { showMemoryEditor = false },
                colors = colors
            )
        }
    }

                if (showBrainEditorState) {
        BrainEditorDialog(
            initialText = viewModel.readBrain(),
            onSave = { viewModel.overwriteBrain(it) },
            onRestoreBase = { viewModel.restoreBaseBrain() },
            onDismiss = { viewModel.hideBrainEditor() },
            colors = colors,
            isDarkTheme = isDarkTheme
        )
    }

    if (showChatEditorState) {
        ChatEditorDialog(
            initialText = viewModel.readChatAsText(),
            onSave = { viewModel.overwriteChatFromText(it) },
            onDismiss = { viewModel.hideChatEditor() },
            colors = colors,
            isDarkTheme = isDarkTheme
        )
    }

        if (showOrganizerEditor) {
        OrganizerEditorDialog(
            initialText = viewModel.readOrganizerAsText(),
            onSave = { viewModel.overwriteOrganizerFromText(it) },
            onDismiss = { showOrganizerEditor = false },
            colors = colors,
            isDarkTheme = isDarkTheme
        )
    }

    if (showBiblioteka) {
        BibliotekaDialog(
            viewModel = viewModel,
            onDismiss = { showBiblioteka = false },
            colors = colors,
            isDarkTheme = isDarkTheme
        )
    }

                if (showPdfPagePicker && pendingPdfUri != null) {
        AlertDialog(
            onDismissRequest = { showPdfPagePicker = false; pendingPdfUri = null },
            title = { Text("📄 Отправить страницы в модель", color = colors.text, fontSize = 16.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "SmolVLM2 обрабатывает одну страницу за раз. Выберите, что отправить.",
                        color = colors.text.copy(alpha = 0.7f),
                        fontSize = 11.sp
                    )
                    Button(
                        onClick = {
                            showPdfPagePicker = false
                            val uri = pendingPdfUri
                            pendingPdfUri = null
                            if (uri != null) {
                                viewModel.renderPdfPagesToCache(uri, listOf(0)) { paths ->
                                    if (paths.isNotEmpty()) {
                                        pdfPageQueue = paths
                                        pdfQueueCurrentIndex = 0
                                        pdfQueueActive = true
                                    } else {
                                        viewModel.appendSystemMessage("❌ Не удалось прочитать PDF.")
                                    }
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = colors.accent, contentColor = colors.background)
                    ) { Text("Первую страницу", color = colors.background) }

                    Button(
                        onClick = {
                            showPdfPagePicker = false
                            val uri = pendingPdfUri
                            pendingPdfUri = null
                            if (uri != null) {
                                viewModel.renderPdfPagesToCache(uri, listOf(0, 1, 2)) { paths ->
                                    if (paths.isNotEmpty()) {
                                        pdfPageQueue = paths
                                        pdfQueueCurrentIndex = 0
                                        pdfQueueActive = true
                                    } else {
                                        viewModel.appendSystemMessage("❌ Не удалось прочитать PDF.")
                                    }
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = colors.accent, contentColor = colors.background)
                    ) { Text("Первые 3 страницы (по очереди)", color = colors.background) }

                    OutlinedTextField(
                        value = pdfPageInput,
                        onValueChange = { pdfPageInput = it.filter { c -> c.isDigit() } },
                        placeholder = { Text("Номер страницы (с 1)", color = colors.text.copy(alpha = 0.5f), fontSize = 11.sp) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
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
                    Button(
                        onClick = {
                            val n = pdfPageInput.toIntOrNull()
                            if (n == null || n < 1) {
                                viewModel.appendSystemMessage("⚠️ Введите номер страницы (начиная с 1)")
                                return@Button
                            }
                            showPdfPagePicker = false
                            val uri = pendingPdfUri
                            pendingPdfUri = null
                            if (uri != null) {
                                viewModel.renderPdfPagesToCache(uri, listOf(n - 1)) { paths ->
                                    if (paths.isNotEmpty()) {
                                        pdfPageQueue = paths
                                        pdfQueueCurrentIndex = 0
                                        pdfQueueActive = true
                                    } else {
                                        viewModel.appendSystemMessage("❌ Не удалось прочитать страницу $n. Возможно, в PDF меньше страниц.")
                                    }
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = colors.borderGray, contentColor = colors.text)
                    ) { Text("Отправить выбранную страницу", color = colors.text) }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showPdfPagePicker = false; pendingPdfUri = null }) {
                    Text("Отмена", color = colors.accent)
                }
            },
            containerColor = colors.surfaceGray
        )
    }

                if (showMediaPicker) {
        MediaPickerDialog(
            onPickGallery = {
                showMediaPicker = false
                onPickImage()
            },
            onTakePhoto = {
                showMediaPicker = false
                onTakePhoto()
            },
            onPickPdf = {
                showMediaPicker = false
                pdfPickerLauncher.launch(arrayOf("application/pdf"))
            },
            onDismiss = { showMediaPicker = false },
            colors = colors,
            isDarkTheme = isDarkTheme
        )
    }
        
        val density = LocalDensity.current      
   
           // Автосброс waveSignal на случай застревания (страховка)
    LaunchedEffect(waveSignal) {
        if (waveSignal) {
            delay(2500)
            waveSignal = false
        }
    }

        // Флаг «палец двигает робота»: включается на любое изменение позиции/масштаба,
    // гаснет через 300 мс тишины. Пока флаг true — синхронизация не работает,
    // и робот следует за пальцем без сопротивления.
    LaunchedEffect(robotOffsetX, robotOffsetY, robotScale) {
        isRobotDragging = true
        delay(300)
        isRobotDragging = false
    }

       // Автосброс реакций головы через 0.8 сек после срабатывания
    LaunchedEffect(headTiltTarget) {
        if (headTiltTarget != 0f) {
            delay(800)
            headTiltTarget = 0f
            tapWaveDirection = 0f
        }
    }

    // Автосброс кивка через 0.8 сек
    LaunchedEffect(headNodTarget) {
        if (headNodTarget != 0f) {
            delay(800)
            headNodTarget = 0f
        }
    }
            // Команда "стань большим" — переводит робота на экран, если он не там
    LaunchedEffect(growBigSignal) {
        if (!growBigSignal) return@LaunchedEffect
        if (robotIsLanded && robotScale >= 3f) {
            growBigSignal = false
            return@LaunchedEffect
        }
        if (!robotIsLanded) {
            robotIsFlyingHere = true
            robotOnOrbit = false
            robotIsFlyingHome = false
            delay(3200)
            robotIsLanded = true
            robotOnOrbit = false
            robotIsFlyingHere = false
            robotIsFlyingHome = false
        }
        // growBigSignal НЕ сбрасываем здесь — сброс произойдёт
        // в блоке "Робот на экране" после завершения анимации
    }

    // Команда "стань маленьким" — переводит робота на экран, если он не там
    LaunchedEffect(shrinkSmallSignal) {
        if (!shrinkSmallSignal) return@LaunchedEffect
        if (robotIsLanded && robotScale <= 0.5f && robotOffsetX == 0f && robotOffsetY == 0f) {
            shrinkSmallSignal = false
            return@LaunchedEffect
        }
        if (!robotIsLanded) {
            robotIsFlyingHere = true
            robotOnOrbit = false
            robotIsFlyingHome = false
            delay(3200)
            robotIsLanded = true
            robotOnOrbit = false
            robotIsFlyingHere = false
            robotIsFlyingHome = false
        }
        // shrinkSmallSignal НЕ сбрасываем здесь — сброс произойдёт
        // в блоке "Робот на экране" после завершения анимации
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
                        // Сбрасываем выбранные модель и проектор,
                        // чтобы в диалоге выбора модели поля были пустыми
                        (context as? MainActivity)?.clearSelectedModelAndMmproj()
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
                    viewModel.speakUiPhrase("Редактор базы знаний")
                },
                onSettingsClick = {
                    showSettings = !showSettings
                    viewModel.speakUiPhrase("Настройки движка ИИ")
                },
                onPromptSettingsClick = {
                    showPromptSettings = !showPromptSettings
                    viewModel.speakUiPhrase("Настройка роли ИИ")
                },
                onHelpClick = {
                    viewModel.speakUiPhrase("Открываю руководство пользователя.")
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
        onDismiss = {
            showPromptSettings = false
        },
        colors = colors,
        context = context
    )
}

                          StatusBar(
                state = state,
                cloudState = cloudState,
                currentMode = currentMode,
                currentModel = if (isModelLoaded) currentModelPath else null,
                modelDisplayName = loadedModelName,
                isSmartMode = isSmartMode,
                visionPhase = visionPhase,
                isPreparingSpeech = isPreparingSpeech,
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
                                                                Column(modifier = Modifier.padding(vertical = 2.dp)) {
                                    // Если у сообщения есть картинка — показываем иконку 📷 перед текстом
                                    val imagePrefix = if (message.hasImage) "📷 " else ""
                                    Text(
                                        text = prefix + imagePrefix + message.text,
                                        color = textColor,
                                        fontFamily = colors.chatFont,
                                        fontSize = 10.sp
                                    )

                                                                        if (!message.promptButtons.isNullOrEmpty()) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(top = 6.dp, start = 4.dp),
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            message.promptButtons.forEach { button ->
                                                val isActive = activeTranslationPrompt == button.promptText
                                                Button(
                                                    onClick = {
                                                        viewModel.applyTranslationPrompt(button.promptText, button.label)
                                                    },
                                                    modifier = Modifier
                                                        .fillMaxWidth(0.6f)
                                                        .height(30.dp),
                                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                                                    colors = ButtonDefaults.buttonColors(
                                                        containerColor = if (isActive) Color(0xFF4CAF50) else colors.accent,
                                                        contentColor = colors.background
                                                    ),
                                                    shape = RoundedCornerShape(8.dp),
                                                    border = BorderStroke(
                                                        width = if (isActive) 2.dp else 1.dp,
                                                        color = if (isActive) Color(0xFF2E7D32) else colors.borderGray
                                                    )
                                                ) {
                                                    Text(
                                                        text = if (isActive) "${button.label} ✅" else button.label,
                                                        color = colors.background,
                                                        fontSize = 10.sp
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
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
                                onPromptChange = {
                    promptInput = it
                    // Пока пользователь печатает — голова опускается вниз.
                    // 1f — заметный наклон, хватает для визуального отклика.
                    if (it.isNotEmpty()) {
                        headNodTarget = 1f
                    }
                },
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
                                       command == "махни рукой" -> { waveSignal = true
                   promptInput = ""
}
                        command == "расти" || command == "рости" -> {
                            growBigSignal = true
                            promptInput = ""
                        }
                        command == "сдуйся" || command == "сдуйсь" -> {
                            shrinkSmallSignal = true
                            promptInput = ""
                        }
                                               command == "выйди из матрицы" -> {
                            if (!FloatingRobotService.isRunning) {
                                (context as? MainActivity)?.startFloatingWithPermissionCheck()
                            }
                            promptInput = ""
                        }
                                               command == "уйди" -> {
                            if (FloatingRobotService.isRunning) {
                                val stopIntent = Intent(context, FloatingRobotService::class.java).apply {
                                    action = FloatingRobotService.ACTION_STOP
                                }
                                context.startService(stopIntent)
                            }
                            promptInput = ""
                        }
                        command == "умный режим" -> {
                            viewModel.enableSmartMode()
                            promptInput = ""
                        }
                                    command == "режим калькулятора" -> {
                            viewModel.disableSmartMode()
                            promptInput = ""
                        }
                                promptInput.trim() == "--пароль Связность--" -> {
                            showBiblioteka = true
                            promptInput = ""
                        }
                        command == "редактировать органайзер" -> {
                            showOrganizerEditor = true
                            promptInput = ""
                        }
                        command == "режим админа" -> {
                            if (currentMode == AIMode.NEUTRAL) {
                                adminPhraseInput = ""
                                showAdminUnlockDialog = true
                            } else {
                                viewModel.appendSystemMessage("⚠️ Команда «режим админа» работает только в нейтральном режиме")
                            }
                            promptInput = ""
                        }
                                               else -> {
                            viewModel.sendUserMessage(promptInput, imagePath)
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
                onPickImage = { showMediaPicker = true },
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
                isCompressing = isCompressing,
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
                        isAiReady = isModelLoaded || (cloudState is CloudAIState.Ready),
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
                        isAiReady = isModelLoaded || (cloudState is CloudAIState.Ready),
                        autoAnimation = robotAutoAnimation,
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
                // Центр кнопки-логотипа «ИИ-Друг» в шапке.
                // Позиция подобрана по скриншоту: ~5.5% ширины и ~6% высоты экрана.
                val logoCenterXPx = screenWidthPx * 0.055f
                val logoCenterYPx = screenHeightPx * 0.060f

                val targetOffsetX = when {
                    growBigSignal -> (screenWidthPx - robotSizePx * 3f) / 2f
                    shrinkSmallSignal -> logoCenterXPx - robotSizePx * 0.5f * 0.5f
                    else -> robotOffsetX
                }
                 val targetOffsetY = when {
                    growBigSignal -> (screenHeightPx - robotSizePx * 3f) / 2f
                    shrinkSmallSignal -> logoCenterYPx - robotSizePx * 0.5f * 0.5f
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
                        .size(70.dp)
                        .graphicsLayer(
                            translationX = animatedOffsetX,
                            translationY = animatedOffsetY,
                            scaleX = animatedScale,
                            scaleY = animatedScale,
                            transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0f, 0f)
                        )
                                                // ПЕРВЫЙ pointerInput — обработка ТАПОВ (одиночный и двойной).
                        // Идёт ПЕРВЫМ, чтобы тап не конфликтовал с перетаскиванием.
                        .pointerInput(Unit) {
                            detectTapGestures(
                                    onTap = { offset ->
                                    // Одиночный тап — «повернулся к тебе».
                                    // offset — координаты тапа ВНУТРИ Box робота (0 .. size.width).
                                    // Центр робота — это size.width / 2.
                                    val robotCenterX = size.width / 2f
                                    // Если тап слева от центра → -1f (наклон влево),
                                    // если справа → +1f (наклон вправо).
                                    val tiltDirection = if (offset.x < robotCenterX) -1f else 1f
                                    // Наклон головы на 15 градусов в сторону тапа.
                                    headTiltTarget = tiltDirection * 15f
                                    // Лёгкий кивок вниз (0.5 — заметная величина).
                                    headNodTarget = 0.5f
                                    // Махание рукой в сторону наклона
                                    tapWaveDirection = tiltDirection
                                },
                                onDoubleTap = {
                                    // Двойной тап на робота 1 — как команда «выйди из матрицы»,
                                    // но всегда сворачивает приложение и перезапускает робота 2.
                                    (context as? MainActivity)?.forceStartFloatingAndMinimize()
                                }
                            )
                        }
                                               // ВТОРОЙ pointerInput — обработка ПЕРЕТАСКИВАНИЯ и ЗУМА.
                        // Идёт ВТОРЫМ, чтобы тап имел приоритет.
                            .pointerInput(Unit) {
                            detectTransformGestures { _, pan, zoom, _ ->
                                // Сначала применяем масштаб, потом считаем границы по НОВОМУ размеру.
                                val newScale = (robotScale * zoom).coerceIn(0.5f, 3f)
                                robotScale = newScale

                                // Визуальный размер робота с учётом масштаба — по нему и ограничиваем.
                                val visualSize = robotSizePx * newScale
                                val maxX = (screenWidthPx - visualSize).coerceAtLeast(0f)
                                val maxY = (screenHeightPx - visualSize).coerceAtLeast(0f)

                                // Позиция по X и Y — мгновенно при перетаскивании.
                                robotOffsetX = (robotOffsetX + pan.x).coerceIn(0f, maxX)
                                robotOffsetY = (robotOffsetY + pan.y).coerceIn(0f, maxY)
                            }
                        }
                        // ТРЕТИЙ pointerInput — обработка СВАЙПА ВВЕРХ.
                        // Быстрое вертикальное движение вверх без бокового сноса.
                        // Триггерит прыжок робота.
                        .pointerInput(Unit) {
                            awaitEachGesture {
                                val down = awaitFirstDown(requireUnconsumed = false)
                                val startX = down.position.x
                                val startY = down.position.y
                                val downTime = System.currentTimeMillis()

                                var lastX = startX
                                var lastY = startY

                                while (true) {
                                    val event = awaitPointerEvent()
                                    val change = event.changes.firstOrNull { it.id == down.id } ?: break
                                    lastX = change.position.x
                                    lastY = change.position.y
                                    if (!change.pressed) break
                                }

                                val dx = abs(lastX - startX)
                                val dy = lastY - startY  // отрицательное — движение вверх
                                val dt = System.currentTimeMillis() - downTime

                                // Свайп вверх: быстро (<300 мс), вверх (dy < -50),
                                // без большого бокового сноса (dx < dy_abs * 0.6).
                                val dyAbs = abs(dy)
                                if (dt < 300 && dy < -50f && dx < dyAbs * 0.6f) {
                                    viewModel.triggerRobotJump()
                                }
                            }
                        }
                ) {
        ThinkingRobotAnimation(
                        height = 70.dp,
                        isActive = true,
                        isSpeaking = isSpeaking,
                        isThinking = state is GenerationState.Generating || cloudState is CloudAIState.Generating,
                        isIdle = !isSpeaking && state !is GenerationState.Generating && cloudState !is CloudAIState.Generating,
                        shouldWave = waveSignal || robotGreetingSignal,
                        isAiReady = isModelLoaded || (cloudState is CloudAIState.Ready),
                        isSmartMode = isSmartMode,
                        headTilt = headTiltTarget,
                        headNod = headNodTarget,
                        tapWaveDirection = tapWaveDirection,
                        autoAnimation = robotAutoAnimation,
                                               isCompressing = isCompressing,
                        isLoadingModel = state is GenerationState.LoadingModel,
                        visionPhase = visionPhase,
                        isPreparingSpeech = isPreparingSpeech,
                        hasActiveAlarm = hasActiveAlarm,
                        isJumping = robotJumpSignal,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                                // Синхронизация robotOffsetX/Y и robotScale с анимированными значениями.
                // Не работает, пока палец двигает робота (isRobotDragging == true),
                // чтобы не откатывать позицию назад во время перетаскивания.
                LaunchedEffect(animatedOffsetX, animatedOffsetY, animatedScale, isCommandActive, isRobotDragging) {
                    if (!isCommandActive && !isRobotDragging) {
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
                        val logoCenterXPx = screenWidthPx * 0.055f
                        val logoCenterYPx = screenHeightPx * 0.060f
                        robotOffsetX = logoCenterXPx - robotSizePx * 0.5f * 0.5f
                        robotOffsetY = logoCenterYPx - robotSizePx * 0.5f * 0.5f
                        robotScale = 0.5f
                        shrinkSmallSignal = false
                    }
                }
            }
           }// закрытие if (robotIsLanded ...)
    }// закрытие главного Box
    
    // Диалог разблокировки по команде «режим админа» — рисуется поверх чата
    if (showAdminUnlockDialog) {
        LockScreen(
            secretPhrase = adminPhraseInput,
            onSecretPhraseChange = { adminPhraseInput = it },
            onVerify = {
                val success = viewModel.verifySecretPhrase(adminPhraseInput)
                adminPhraseInput = ""
                if (success) {
                    showAdminUnlockDialog = false
                    viewModel.appendSystemMessage("✅ Режим админа: приложение разблокировано навсегда")
                }
            },
            viewModel = viewModel,
            isPermanentlyBlocked = isPermanentlyBlocked,
            colors = colors
        )
    }
    } // Закрытие else
    } // Закрытие CompositionLocalProvider
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
    commandScale: Float? = null,
    isAiReady: Boolean = false,
    isSmartMode: Boolean = false,
    isListening: Boolean = false,
    isCompressing: Boolean = false,
    isLoadingModel: Boolean = false,
    visionPhase: VisionPhase = VisionPhase.IDLE,
    isPreparingSpeech: Boolean = false,
    hasActiveAlarm: Boolean = false,
    headTilt: Float = 0f,
    headBob: Float = 0f,
    headNod: Float = 0f,
    uDivisor: Float = 200f,
    yOffsetUnits: Float = 0f,
    // Направление махания от тапа: -1f — влево, +1f — вправо, 0f — нет.
    // Когда != 0f, соответствующая рука машет приветственно.
    tapWaveDirection: Float = 0f,
    // Авто-анимация по циклу (из MainViewModel):
    // 0 — нет, 1 — махание правой, 2 — наклон головы влево,
    // 3 — махание левой, 4 — наклон головы вправо, 5 — приветственное махание правой,
    // 6 — покачивание корпуса влево, 7 — покачивание корпуса вправо,
    // 8 — покачивание корпуса влево-вправо, 9 — осмотр вверх-вниз,
    // 10 — смещение головы вправо + наклон вправо,
    // 11 — смещение головы влево + наклон влево.
       autoAnimation: Int = 0,
    // Прыжок: пламя усиливается, появляется пыль и искры, робот подпрыгивает.
    isJumping: Boolean = false,
    // Амплитуда внутреннего покачивания (bob) в единицах u.
    // В overlay передаётся 0f, чтобы покачивание делало окно, а не канвас.
    bobAmplitude: Float = 10f
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

    val indicatorPulse by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "indicator_pulse"
    )

    val headPanelOpenAmount by animateFloatAsState(
        targetValue = if (isThinking) 1f else 0f,
        animationSpec = tween(durationMillis = 2000, easing = FastOutSlowInEasing),
        label = "head_panel_open"
    )

    val leftBarsPhase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "left_bars_phase"
    )

    val smartPulse by transition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "smart_pulse"
    )

        var externalWave by remember { mutableStateOf(false) }
    LaunchedEffect(shouldWave) {
        if (shouldWave) {
            externalWave = true
        } else {
            externalWave = false
        }
    }

    // randomWave удалён — заменён на цикл авто-анимаций через MainViewModel

    val isWaving = externalWave

    val waveAmount by animateFloatAsState(
        targetValue = if (isWaving) 1f else 0f,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "wave_amount"
    )

        val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current

        val animatedScale by animateFloatAsState(
        targetValue = commandScale ?: 1f,
        animationSpec = tween(durationMillis = 3000, easing = FastOutSlowInEasing),
        label = "command_scale"
    )

        // ========== АНИМАЦИЯ ПРЫЖКА ==========
    // Управляется вручную через Animatable и LaunchedEffect.
    // Когда isJumping становится true — проигрываем всю анимацию целиком
    // (0→1→0 за 1200 мс), независимо от того, что isJumping уже сбросился.
    // Иначе keyframes прерывался бы на середине и робот дёргался.

        val jumpProgressAnim = remember { Animatable(0f) }
    val flameIntensityAnim = remember { Animatable(0f) }

    LaunchedEffect(isJumping) {
        if (isJumping) {
            // Плавный связный прыжок:
            // 0–250    — пламя разгорается (0 → 1), робот ещё на месте.
            // 250–450  — робот поднимается (0 → 1), пламя на пике.
            // 450–650  — робот висит в верхней точке, пламя на пике.
            // 650–900  — робот опускается (1 → 0), пламя начинает гаснуть.
            // 900–1100 — пламя гаснет (1 → 0), робот уже на месте.
            launch {
                jumpProgressAnim.animateTo(
                    targetValue = 1f,
                    animationSpec = keyframes {
                        durationMillis = 1100
                        0f at 0 using FastOutSlowInEasing
                        0f at 250 using LinearEasing
                        1f at 450 using FastOutSlowInEasing
                        1f at 650 using LinearEasing
                        0f at 900 using FastOutSlowInEasing
                        0f at 1100
                    }
                )
            }
            launch {
                flameIntensityAnim.animateTo(
                    targetValue = 0f,
                    animationSpec = keyframes {
                        durationMillis = 1100
                        0f at 0
                        1f at 250
                        1f at 650
                        0f at 900
                        0f at 1100
                    }
                )
            }
        }
    }

    val jumpProgress = jumpProgressAnim.value
    val flameIntensity = flameIntensityAnim.value

    // Плавный наклон головы — вместо мгновенного rotate(headTilt, ...)
    val animatedHeadTilt by animateFloatAsState(
        targetValue = headTilt,
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
        label = "head_tilt_animated"
    )
    val animatedHeadNod by animateFloatAsState(
        targetValue = headNod,
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
        label = "head_nod_animated"
    )

        // Плавная величина махания от тапа (0..1)
    val tapWaveAmount by animateFloatAsState(
        targetValue = if (tapWaveDirection != 0f) 1f else 0f,
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
        label = "tap_wave_amount"
    )

       // ========== АВТО-АНИМАЦИИ ==========
    // Из autoAnimation вычисляем:
    //  - autoTilt: наклон головы (для 2 = влево, 4 = вправо)
    //  - autoWaveDir: направление махания (для 1,5 = +1 правая; 3 = -1 левая)
    val autoTilt: Float = when (autoAnimation) {
        2 -> -15f   // наклон головы влево
        4 -> 15f    // наклон головы вправо
        10 -> 15f   // наклон головы вправо (вместе со смещением)
        11 -> -15f  // наклон головы влево (вместе со смещением)
        else -> 0f
    }
    val autoWaveDir: Float = when (autoAnimation) {
        1 -> 1f     // махание правой
        3 -> -1f    // махание левой
        5 -> 1f     // приветственное махание правой
        else -> 0f
    }

    // ========== АНИМАЦИИ 10 И 11: ТРЁХФАЗНЫЕ ==========
    // Отличие от 2/4: голова не просто наклоняется, а поворачивается,
    // сдвигается, кивает и возвращается — с разными задержками фаз.
    // Взгляд (autoLookXForAnim) синхронизирован с наклоном и «догоняет» его.
    //
    // Фазы (мс от начала):
    //   0–700     — наклон головы (взгляд ещё вперёд)
    //   700–1400  — сдвиг головы + взгляд в сторону наклона
    //   1400–2100 — лёгкий кивок (голова уже наклонена и сдвинута)
    //   2100–2800 — возврат: сначала голова, потом взгляд догоняет
    val isAnim10or11 = autoAnimation == 10 || autoAnimation == 11

    // Фазовые прогрессы (0..1) — каждый со своей задержкой.
    // Фаза 1 (наклон): старт 0, длительность 700.
    // Фаза 2 (сдвиг): старт 700, длительность 700.
    // Фаза 3 (кивок): старт 1400, длительность 700.
    // Фаза 4 (возврат): старт 2100, длительность 700.
    val phase1 by animateFloatAsState(
        targetValue = if (isAnim10or11) 1f else 0f,
        animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing),
        label = "anim10_phase1"
    )
    val phase2 by animateFloatAsState(
        targetValue = if (isAnim10or11) 1f else 0f,
        animationSpec = tween(durationMillis = 700, delayMillis = 700, easing = FastOutSlowInEasing),
        label = "anim10_phase2"
    )
    val phase3 by animateFloatAsState(
        targetValue = if (isAnim10or11) 1f else 0f,
        animationSpec = tween(durationMillis = 700, delayMillis = 1400, easing = FastOutSlowInEasing),
        label = "anim10_phase3"
    )

    // Итоговый наклон головы для 10/11 — плавный по фазам.
    // Фаза 1 даёт наклон, фаза 3 добавляет лёгкий кивок.
    val anim10HeadTilt: Float = when (autoAnimation) {
        10 -> 15f
        11 -> -15f
        else -> 0f
    }
    // Кивок вниз на 3 градуса во время фазы 3.
    val anim10HeadNod: Float = if (isAnim10or11) phase3 * 3f else 0f

    // Смещение головы по X — включается на фазе 2 и не пропадает до конца.
    // 8 u — заметное смещение, но не «уезд».
    val anim10HeadOffsetXTarget: Float = when (autoAnimation) {
        10 -> 8f
        11 -> -8f
        else -> 0f
    }
    val anim10HeadOffsetXAnimated: Float = if (isAnim10or11) {
        anim10HeadOffsetXTarget * phase2
    } else {
        0f
    }

    // Взгляд для 10/11 — уходит в сторону наклона на фазе 2,
    // возвращается на фазе 4 (phase3 = 1 → 0 через autoAnimation обратно в 0).
    // Амплитуда 1.2 — чуть больше, чем естественный lookX (1.0),
    // чтобы взгляд «догонял» голову выразительно.
    val anim10LookXTarget: Float = when (autoAnimation) {
        10 -> 1.2f
        11 -> -1.2f
        else -> 0f
    }
    val anim10LookXAnimated: Float = if (isAnim10or11) {
        anim10LookXTarget * phase2
    } else {
        0f
    }

    // ========== СТАРЫЕ АНИМАЦИИ 2/4 (наклон без сдвига) ==========
    // Работают только для 2 и 4 — 10 и 11 идут через фазовые переменные выше.
    val autoTiltOnly: Float = when (autoAnimation) {
        2 -> -15f
        4 -> 15f
        else -> 0f
    }

    val autoTiltAnimated by animateFloatAsState(
        targetValue = autoTiltOnly,
        animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing),
        label = "auto_tilt"
    )

    // Плавная величина авто-махания (0..1)
    val autoWaveAmount by animateFloatAsState(
        targetValue = if (autoWaveDir != 0f) 1f else 0f,
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
        label = "auto_wave_amount"
    )

       // ========== АВТО-ПОКАЧИВАНИЕ КОРПУСА (6, 7, 8) ==========
    // 6 — наклон корпуса влево, 7 — наклон корпуса вправо,
    // 8 — двойное покачивание: влево, потом вправо.
    var autoBodySwayFlip by remember { mutableStateOf(false) }
    LaunchedEffect(autoAnimation) {
        if (autoAnimation == 8) {
            autoBodySwayFlip = false
            delay(1200)
            autoBodySwayFlip = true
            delay(1200)
            autoBodySwayFlip = false
        } else {
            autoBodySwayFlip = false
        }
    }

    val autoBodySwayTarget: Float = when (autoAnimation) {
        6 -> -1f
        7 -> 1f
        8 -> if (autoBodySwayFlip) 1f else -1f
        else -> 0f
    }

    val autoBodySwayAnimated by animateFloatAsState(
        targetValue = autoBodySwayTarget,
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "auto_body_sway"
    )
    // ========== АВТО-ОСМОТР ВВЕРХ-ВНИЗ (9) ==========
    // 9 — голова вверх, потом вниз.
    var autoNodUpDownPhase by remember { mutableStateOf(0) }
    LaunchedEffect(autoAnimation) {
        if (autoAnimation == 9) {
            autoNodUpDownPhase = 1  // вверх
            delay(1000)
            autoNodUpDownPhase = 2  // обратно в 0
            delay(500)
            autoNodUpDownPhase = 3  // вниз
            delay(1000)
            autoNodUpDownPhase = 0  // обратно в 0
        } else {
            autoNodUpDownPhase = 0
        }
    }

    val autoNodUpDownTarget: Float = when (autoNodUpDownPhase) {
        1 -> -1f
        3 -> 1f
        else -> 0f
    }

    val autoNodUpDownAnimated by animateFloatAsState(
        targetValue = autoNodUpDownTarget,
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
        label = "auto_nod_up_down"
    )

                // Итоговые наклон и махание: тап и авто складываются.
    // Для 10/11 берём фазовый наклон (anim10HeadTilt * phase1) и
    // фазовый кивок (anim10HeadNod). Для 2/4 — старый autoTiltAnimated.
    val autoTiltCombined: Float = if (isAnim10or11) {
        anim10HeadTilt * phase1
    } else {
        autoTiltAnimated
    }
    val autoNodCombined: Float = if (isAnim10or11) {
        anim10HeadNod
    } else {
        autoNodUpDownAnimated
    }

    val effectiveHeadTilt = headTilt + autoTiltCombined
    val effectiveHeadNod = headNod + autoNodCombined
    val effectiveTapWaveDirection = if (tapWaveDirection != 0f) tapWaveDirection else autoWaveDir
    val effectiveTapWaveAmount = if (tapWaveDirection != 0f) tapWaveAmount else autoWaveAmount
    Canvas(
        modifier = modifier
            .height(height)
            .width(height * 0.85f)
            .graphicsLayer(
                scaleX = if (commandScale != null) animatedScale else 1f,
                scaleY = if (commandScale != null) animatedScale else 1f
            )
    ) {
        val u = size.height / uDivisor
        val cx = size.width / 2f

        android.util.Log.d(
            "RobotCanvasSize",
            "canvas=${size.width}x${size.height}, uDivisor=$uDivisor, u=$u, ratio=${size.width / size.height}"
        )
        val bobOffset = if (isActive) sin(bob) * bobAmplitude * u else 0f
        // Смещение корпуса от прыжка: 20 единиц u на пике.
               val jumpOffset = 0f
                // Взгляд: естественное блуждание (lookX) + фазовый взгляд для 10/11.
        // Во время 10/11 фазовый взгляд перебивает естественный — глаза
        // синхронно смотрят в сторону наклона головы.
        val lookOffsetX = if (isAnim10or11) {
            anim10LookXAnimated * u
        } else {
            lookX * 1f * u
        }
        val lookOffsetY = lookY * 0.8f * u
        val currentBlink = if (isActive) blink else 1f
        val staticYOffset = yOffsetUnits * u
        val panelOpen = headPanelOpenAmount
        val panelLift = panelOpen * 20f * u

                      // Смещение головы — независимо от тела.
        // 5f для headOffsetY — чтобы наклон вниз был заметен.
        // Смещение головы по X — от наклона корпуса (headBob) плюс
        // фазовое смещение для 10/11 (anim10HeadOffsetXAnimated).
        val headOffsetX = (headBob * 1.5f + anim10HeadOffsetXAnimated) * u
        val headOffsetY = effectiveHeadNod * 5f * u
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

        fun pt(x: Float, y: Float) = Offset(cx + x * u, y * u + bobOffset + staticYOffset - jumpOffset)

        // Функция для элементов головы — добавляет смещение головы
        fun ptHead(x: Float, y: Float): Offset {
            val base = pt(x, y)
            return Offset(base.x + headOffsetX, base.y + headOffsetY)
        }

        // Функция для элементов шеи — БЕЗ смещения головы.
        // Шея не должна ехать вслед за кивком/сдвигом головы,
        // иначе она наползает на грудной экран.
        fun ptNeck(x: Float, y: Float): Offset {
            return pt(x, y)
        }

                // Функция для элементов лица и ушей.
        // Раньше смещала лицо при повороте головы — теперь поворота нет,
        // поэтому лицо и уши привязаны к ptHead (не смещаются).
        fun ptFace(x: Float, y: Float): Offset {
            return ptHead(x, y)
        }

        // ================= ДЫМКА ПРОСЛУШИВАНИЯ =================
        // Когда робот слушает (isListening), вокруг него появляется пульсирующая
        // сине-неоновая электрическая дымка. Она рисуется ЗА роботом,
        // поэтому вызывается до отрисовки рук и тела.
        if (isListening) {
            val auraPulse = 0.5f + 0.5f * sin(pulse * 3f)
            val auraFast = 0.5f + 0.5f * sin(pulse * 7f)
            val auraPhase = pulse * 2f

            // Центр дымки — центр туловища робота
            val auraCenter = pt(0f, 115f)
            // Базовый радиус — чуть больше робота
            val auraRadius = 95f * u

            // 1. Очень мягкий внешний ореол — большой, едва заметный
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF00D9FF).copy(alpha = 0.10f + 0.06f * auraPulse),
                        Color(0xFF0088FF).copy(alpha = 0.06f + 0.04f * auraPulse),
                        Color(0xFF0044AA).copy(alpha = 0.02f),
                        Color.Transparent
                    ),
                    center = auraCenter,
                    radius = auraRadius * 1.6f
                ),
                radius = auraRadius * 1.6f,
                center = auraCenter
            )

            // 2. Средний слой дымки — сине-голубой, пульсирует
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF00E5FF).copy(alpha = 0.18f + 0.10f * auraPulse),
                        Color(0xFF00BFFF).copy(alpha = 0.12f + 0.06f * auraPulse),
                        Color(0xFF0044FF).copy(alpha = 0.05f),
                        Color.Transparent
                    ),
                    center = auraCenter,
                    radius = auraRadius * 1.2f
                ),
                radius = auraRadius * 1.2f,
                center = auraCenter
            )

            // 3. Внутренний яркий слой — прямо возле робота
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF80EFFF).copy(alpha = 0.22f + 0.12f * auraFast),
                        Color(0xFF00D9FF).copy(alpha = 0.14f + 0.08f * auraPulse),
                        Color.Transparent
                    ),
                    center = auraCenter,
                    radius = auraRadius * 0.85f
                ),
                radius = auraRadius * 0.85f,
                center = auraCenter
            )

            // 4. Электрические дуги — тонкие светящиеся кольца, вращаются
            val arcCount = 5
            for (i in 0 until arcCount) {
                val angle = (i.toFloat() / arcCount) * 2f * PI.toFloat() + auraPhase
                val arcRadius = auraRadius * (0.9f + 0.15f * sin(auraPhase * 1.3f + i))
                val arcAlpha = 0.35f + 0.35f * sin(auraPhase * 2f + i * 0.7f)

                // Точка на орбите
                val px = auraCenter.x + cos(angle) * arcRadius
                val py = auraCenter.y + sin(angle) * arcRadius * 0.9f

                // Свечение вокруг точки
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF80EFFF).copy(alpha = arcAlpha * 0.5f),
                            Color(0xFF00D9FF).copy(alpha = arcAlpha * 0.25f),
                            Color.Transparent
                        ),
                        center = Offset(px, py),
                        radius = 12f * u
                    ),
                    radius = 12f * u,
                    center = Offset(px, py)
                )

                // Ядро дуги
                drawCircle(
                    color = Color.White.copy(alpha = arcAlpha * 0.9f),
                    radius = 1.2f * u,
                    center = Offset(px, py)
                )
            }

            // 5. Второй слой дуг — медленнее, в другую сторону
            val arcCount2 = 7
            for (i in 0 until arcCount2) {
                val angle = (i.toFloat() / arcCount2) * 2f * PI.toFloat() - auraPhase * 0.6f
                val arcRadius = auraRadius * (0.6f + 0.2f * sin(auraPhase + i * 0.9f))
                val arcAlpha = 0.25f + 0.3f * sin(auraPhase * 1.7f + i * 0.5f)

                val px = auraCenter.x + cos(angle) * arcRadius
                val py = auraCenter.y + sin(angle) * arcRadius * 0.8f

                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF00FFFF).copy(alpha = arcAlpha * 0.4f),
                            Color.Transparent
                        ),
                        center = Offset(px, py),
                        radius = 8f * u
                    ),
                    radius = 8f * u,
                    center = Offset(px, py)
                )
            }

            // 6. Разряды-молнии — короткие линии от центра к краям
            val boltCount = 6
            for (i in 0 until boltCount) {
                val baseAngle = (i.toFloat() / boltCount) * 2f * PI.toFloat() + auraPhase * 1.5f
                val boltLength = auraRadius * (0.7f + 0.3f * auraFast)
                val boltAlpha = (0.4f + 0.6f * auraFast) * (0.5f + 0.5f * sin(auraPhase * 3f + i))

                if (boltAlpha > 0.3f) {
                    val segments = 4
                    var prevX = auraCenter.x
                    var prevY = auraCenter.y

                    for (s in 1..segments) {
                        val t = s.toFloat() / segments
                        val jitter = sin(auraPhase * 5f + s * 2.3f + i) * 6f * u * t
                        val angle = baseAngle + jitter / (boltLength * t + 1f)
                        val r = boltLength * t
                        val nx = auraCenter.x + cos(angle) * r
                        val ny = auraCenter.y + sin(angle) * r * 0.9f

                        drawLine(
                            color = Color(0xFF80EFFF).copy(alpha = boltAlpha * (1f - t * 0.5f) * 0.7f),
                            start = Offset(prevX, prevY),
                            end = Offset(nx, ny),
                            strokeWidth = (1.5f - t * 0.8f) * u
                        )
                        prevX = nx
                        prevY = ny
                    }
                }
            }

            // 7. Мелкие искры — много мелких точек, быстро мигают
            val sparkCount = 14
            for (i in 0 until sparkCount) {
                val sparkAngle = (i * 2.399f) + auraPhase * 2f
                val sparkRadius = auraRadius * (0.5f + 0.5f * (0.5f + 0.5f * sin(auraPhase * 2f + i * 1.7f)))
                val sparkAlpha = 0.4f + 0.6f * (0.5f + 0.5f * sin(auraPhase * 4f + i * 2.1f))

                               val px = auraCenter.x + cos(sparkAngle) * sparkRadius
                val py = auraCenter.y + sin(sparkAngle) * sparkRadius * 0.9f
                val sparkSize = (0.8f + 0.8f * sin(auraPhase * 5f + i)) * u

                if (sparkSize > 0f) {
                    drawCircle(
                        color = Color.White.copy(alpha = sparkAlpha),
                        radius = sparkSize,
                        center = Offset(px, py)
                    )
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF00FFFF).copy(alpha = sparkAlpha * 0.6f),
                                Color.Transparent
                            ),
                            center = Offset(px, py),
                            radius = sparkSize * 5f
                        ),
                        radius = sparkSize * 5f,
                        center = Offset(px, py)
                    )
                }
            }
        }

                // ================= ПОКАЧИВАНИЕ КОРПУСА =================
        // При авто-анимациях 6, 7, 8 всё тело робота наклоняется
        // влево/вправо вокруг нижней точки туловища (уровень сопла).
        // Угол небольшой (8°), чтобы робот не «падал».
        val bodySwayAngle = autoBodySwayAnimated * 8f

        rotate(bodySwayAngle, pivot = pt(0f, 160f)) {

        // ================= АНИМАЦИЯ РУК =================
        val armSway = sin(armPhase)
        val leftArmOffsetY = when {
            isSpeaking -> armSway * 0.8f
            isThinking -> armSway * 0.8f
            isIdle -> armSway * 0.8f
            else -> 0f
        }
        val rightArmOffsetY = when {
            isSpeaking -> sin(armPhase + PI.toFloat()) * 0.8f
            isThinking -> sin(armPhase + PI.toFloat()) * 0.8f
            isIdle -> sin(armPhase + PI.toFloat()) * 0.8f
            else -> 0f
        }
        val shoulderWaveAngle = -60f * waveAmount
        val forearmWaveAngle = -50f * waveAmount
        val handWiggle = sin(armPhase * 2f) * 18f * waveAmount

                // Махание от тапа или авто-анимации: рука поднимается и качается.
        // effectiveTapWaveDirection > 0f — правая рука, < 0f — левая.
        val tapWaveRight = if (effectiveTapWaveDirection > 0f) effectiveTapWaveAmount else 0f
        val tapWaveLeft = if (effectiveTapWaveDirection < 0f) effectiveTapWaveAmount else 0f

        // Углы подъёма руки при махании от тапа.
        val tapShoulderWaveRight = -70f * tapWaveRight
        val tapForearmWaveRight = -45f * tapWaveRight
        val tapHandWiggleRight = sin(armPhase * 3f) * 20f * tapWaveRight

        val tapShoulderWaveLeft = -70f * tapWaveLeft
        val tapForearmWaveLeft = -45f * tapWaveLeft
        val tapHandWiggleLeft = sin(armPhase * 3f) * 20f * tapWaveLeft

                               // ================= ЛЕВАЯ РУКА =================
        // Левая рука теперь машет ТОЧНО ТАК ЖЕ, как правая, но зеркально:
        // учитывает и общее махание (shoulderWaveAngle), и от тапа (tapShoulderWaveLeft).
        // Знак минус — потому что левая рука зеркальная.
        rotate(-(shoulderWaveAngle + tapShoulderWaveLeft), pivot = pt(-46f, 96f)) {
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
        drawRoundRect(
            color = whiteHighlight.copy(alpha = 0.8f),
            topLeft = pt(-52f, 108f + leftArmOffsetY),
            size = Size(3f * u, 14f * u),
            cornerRadius = CornerRadius(1.5f * u)
        )

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

                // ========== ПРЕДПЛЕЧЬЕ ЛЕВОЙ РУКИ — СГИБАНИЕ В ЛОКТЕ ==========
        // Как правая рука, но зеркально: учитываем и общее махание (forearmWaveAngle + handWiggle),
        // и махание от тапа (tapForearmWaveLeft).
        // Поворот вокруг локтя (pt(-46f, 126f + leftArmOffsetY)).
        // Знак минус — потому что левая рука зеркальная.
        rotate(-(forearmWaveAngle + handWiggle + tapForearmWaveLeft + tapHandWiggleLeft), pivot = pt(-46f, 126f + leftArmOffsetY)) {

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
        drawRoundRect(
            color = whiteHighlight.copy(alpha = 0.8f),
            topLeft = pt(-56f, 132f + leftArmOffsetY),
            size = Size(3f * u, 15f * u),
            cornerRadius = CornerRadius(1.5f * u)
        )

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

               val barsCenterX = -46f
        val barsCenterY = 140f + leftArmOffsetY
        // Все геометрические размеры — в u-единицах.
        // Пиксели получаются умножением на u только в Size(...).
        val barWidthU = 6f
        val barHeightU = 1.6f
        val barGapU = 1.4f
        val barsTotalHeightU = barHeightU * 3 + barGapU * 2

        for (i in 0..2) {
            val barYU = barsCenterY - barsTotalHeightU / 2f + barHeightU / 2f + i * (barHeightU + barGapU)
            val barTopLeft = pt(barsCenterX - barWidthU / 2f, barYU - barHeightU / 2f)
            val barSize = Size(barWidthU * u, barHeightU * u)

        val isBarActive = isThinking && (leftBarsPhase.toInt() == i)

            // Средняя полоска (i == 1): пульсирует зелёным, когда модель загружена
            // и находится в покое (не думает, не говорит, не сжимает, не грузит).
            val isMiddlePulse = i == 1 && isAiReady &&
                    !isThinking && !isSpeaking && !isCompressing && !isLoadingModel

            val middlePulseAlpha = if (isMiddlePulse) {
                0.5f + 0.5f * ((kotlin.math.sin(pulse * 2f) + 1f) / 2f)
            } else 1f

            val barColor = when {
                isMiddlePulse -> Color(0xFF00E676).copy(alpha = middlePulseAlpha)
                isBarActive -> Color(0xFF00E676)
                isThinking -> Color(0xFF1E88E5)
                isAiReady -> Color(0xFF1E88E5)
                else -> Color(0xFF1565C0).copy(alpha = 0.5f)
            }

                       if (isBarActive) {
                val barCenter = pt(barsCenterX, barYU)
                val glowRadiusPx = barHeightU * u * 5f
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF69F0AE).copy(alpha = 0.7f),
                            Color(0xFF00E676).copy(alpha = 0.3f),
                            Color.Transparent
                        ),
                        center = barCenter,
                        radius = glowRadiusPx
                    ),
                    radius = glowRadiusPx,
                    center = barCenter
                )
            }

            drawRoundRect(
                color = Color(0xFF1A1A20).copy(alpha = 0.85f),
                topLeft = Offset(barTopLeft.x - 0.3f * u, barTopLeft.y - 0.3f * u),
                size = Size(barSize.width + 0.6f * u, barSize.height + 0.6f * u),
                cornerRadius = CornerRadius(0.9f * u)
            )

            drawRoundRect(
                color = barColor,
                topLeft = barTopLeft,
                size = barSize,
                cornerRadius = CornerRadius(0.8f * u)
            )

            drawRoundRect(
                color = if (isBarActive) Color(0xFFB9F6CA) else darkGray,
                topLeft = barTopLeft,
                size = barSize,
                cornerRadius = CornerRadius(0.8f * u),
                style = Stroke(width = if (isBarActive) 0.6f * u else 0.4f * u)
            )

            drawRoundRect(
                color = Color.White.copy(alpha = if (isBarActive) 0.5f else 0.2f),
                topLeft = Offset(barTopLeft.x + 0.3f * u, barTopLeft.y + 0.2f * u),
                size = Size(barSize.width - 0.6f * u, barSize.height * 0.35f),
                cornerRadius = CornerRadius(0.5f * u)
            )
        }

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
        } // конец rotate предплечья левой руки (локоть)
        } // конец rotate для левой руки (плечо)

        // ================= ПРАВАЯ РУКА =================
        rotate(shoulderWaveAngle + tapShoulderWaveRight, pivot = pt(46f, 96f)) {
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

        rotate(forearmWaveAngle + handWiggle + tapForearmWaveRight + tapHandWiggleRight, pivot = pt(46f, 126f + rightArmOffsetY)) {
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

                val indicatorCenterX = 46f
                val indicatorCenterY = 140f
                val indicatorRadius = 4f * u

                // Часы показывают напоминания:
                // - есть напоминание → медленно пульсируют зелёным;
                // - нет напоминаний → синие как сейчас.
                val alarmPulseAlpha = if (hasActiveAlarm) {
                    0.5f + 0.5f * ((kotlin.math.sin(pulse * 1.2f) + 1f) / 2f)
                } else 1f

                val indicatorColor = if (hasActiveAlarm) {
                    Color(0xFF4CAF50).copy(alpha = alarmPulseAlpha)
                } else {
                    Color(0xFF2196F3)
                }

                val indicatorAlpha = if (hasActiveAlarm) alarmPulseAlpha else 1f

                val clockW = indicatorRadius * 1.9f
                val clockH = indicatorRadius * 1.5f
                val clockCorner = 1.2f * u
                val clockCenter = pt(indicatorCenterX, indicatorCenterY + rightArmOffsetY)
                val clockTopLeft = Offset(
                    clockCenter.x - clockW / 2f,
                    clockCenter.y - clockH / 2f
                )
                val clockSize = Size(clockW, clockH)

                if (isAiReady) {
                    val glowPulse = 0.5f + 0.5f * indicatorPulse

                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF4CAF50).copy(alpha = 0.55f * glowPulse),
                                Color(0xFF4CAF50).copy(alpha = 0.25f * glowPulse),
                                Color(0xFF4CAF50).copy(alpha = 0.0f)
                            ),
                            center = clockCenter,
                            radius = indicatorRadius * 2.8f
                        ),
                        radius = indicatorRadius * 2.8f,
                        center = clockCenter
                    )

                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF81F78B).copy(alpha = 0.7f * glowPulse),
                                Color(0xFF4CAF50).copy(alpha = 0.0f)
                            ),
                            center = clockCenter,
                            radius = indicatorRadius * 1.8f
                        ),
                        radius = indicatorRadius * 1.8f,
                        center = clockCenter
                    )
                }

                drawRoundRect(
                    color = darkerGray,
                    topLeft = Offset(
                        clockTopLeft.x - 0.6f * u,
                        clockTopLeft.y - 0.6f * u
                    ),
                    size = Size(clockW + 1.2f * u, clockH + 1.2f * u),
                    cornerRadius = CornerRadius(clockCorner * 1.3f)
                )

                drawRoundRect(
                    color = indicatorColor,
                    topLeft = clockTopLeft,
                    size = clockSize,
                    cornerRadius = CornerRadius(clockCorner)
                )

                drawRoundRect(
                    color = when {
                        hasActiveAlarm -> Color(0xFFB9F6CA).copy(alpha = alarmPulseAlpha)
                        isAiReady -> Color(0xFFB9F6CA).copy(alpha = 0.9f)
                        else -> darkGray
                    },
                    topLeft = clockTopLeft,
                    size = clockSize,
                    cornerRadius = CornerRadius(clockCorner),
                    style = Stroke(width = 0.7f * u)
                )

                drawRoundRect(
                    color = Color.White.copy(alpha = 0.25f),
                    topLeft = Offset(clockTopLeft.x + 0.5f * u, clockTopLeft.y + 0.4f * u),
                    size = Size(clockW - 1f * u, clockH * 0.35f),
                    cornerRadius = CornerRadius(clockCorner * 0.6f)
                )

                drawLine(
                    color = Color.White.copy(alpha = 0.95f),
                    start = clockCenter,
                    end = Offset(
                        clockCenter.x + cos(-PI.toFloat() / 3f) * indicatorRadius * 0.55f,
                        clockCenter.y + sin(-PI.toFloat() / 3f) * indicatorRadius * 0.55f
                    ),
                    strokeWidth = 0.65f * u,
                    cap = StrokeCap.Round
                )

                drawLine(
                    color = Color.White.copy(alpha = 0.95f),
                    start = clockCenter,
                    end = Offset(
                        clockCenter.x + cos(-PI.toFloat() / 2f) * indicatorRadius * 0.75f,
                        clockCenter.y + sin(-PI.toFloat() / 2f) * indicatorRadius * 0.75f
                    ),
                    strokeWidth = 0.55f * u,
                    cap = StrokeCap.Round
                )

                drawCircle(
                    color = Color.White.copy(alpha = 0.95f),
                    radius = 0.65f * u,
                    center = clockCenter
                )
            }
        }

        // ================= ТУЛОВИЩЕ =================
        val domeTopY = 70f
        val domeBottomY = 87f
        val domeWidth = 37f

        val domePath = Path().apply {
            moveTo(pt(-domeWidth, domeBottomY).x, pt(-domeWidth, domeBottomY).y)
            quadraticBezierTo(
                pt(-domeWidth, domeTopY + 5f).x, pt(-domeWidth, domeTopY + 5f).y,
                pt(0f, domeTopY).x, pt(0f, domeTopY).y
            )
            quadraticBezierTo(
                pt(domeWidth, domeTopY + 5f).x, pt(domeWidth, domeTopY + 5f).y,
                pt(domeWidth, domeBottomY).x, pt(domeWidth, domeBottomY).y
            )
            quadraticBezierTo(
                pt(0f, domeBottomY + 7f).x, pt(0f, domeBottomY + 7f).y,
                pt(-domeWidth, domeBottomY).x, pt(-domeWidth, domeBottomY).y
            )
            close()
        }

        drawPath(
            domePath,
            brush = Brush.radialGradient(
                colors = listOf(
                    whiteHighlight,
                    whiteBody,
                    lightGray,
                    mediumGray
                ),
                center = pt(0f, domeTopY + 5f),
                radius = 40f * u
            )
        )
        drawPath(domePath, color = darkGray, style = Stroke(width = 1.3f * u))

        val segment1Top = 87f
        val segment1Bottom = 107f
        val segment1WidthTop = 38f
        val segment1WidthBottom = 34f

        val segment1Path = Path().apply {
            moveTo(pt(-segment1WidthTop, segment1Top).x, pt(-segment1WidthTop, segment1Top).y)
            lineTo(pt(segment1WidthTop, segment1Top).x, pt(segment1WidthTop, segment1Top).y)
            quadraticBezierTo(
                pt(segment1WidthBottom, segment1Bottom).x, pt(segment1WidthBottom, segment1Bottom).y,
                pt(segment1WidthBottom, segment1Bottom).x, pt(segment1WidthBottom, segment1Bottom).y
            )
            lineTo(pt(-segment1WidthBottom, segment1Bottom).x, pt(-segment1WidthBottom, segment1Bottom).y)
            quadraticBezierTo(
                pt(-segment1WidthTop, segment1Top).x, pt(-segment1WidthTop, segment1Top).y,
                pt(-segment1WidthTop, segment1Top).x, pt(-segment1WidthTop, segment1Top).y
            )
            close()
        }

        drawPath(
            segment1Path,
            brush = Brush.verticalGradient(
                colors = listOf(whiteBody, lightGray),
                startY = pt(0f, segment1Top).y,
                endY = pt(0f, segment1Bottom).y
            )
        )
        drawPath(segment1Path, color = darkGray, style = Stroke(width = 1.2f * u))

        drawLine(
            color = mediumGray,
            start = pt(-segment1WidthBottom + 2f, segment1Bottom - 2f),
            end = pt(segment1WidthBottom - 2f, segment1Bottom - 2f),
            strokeWidth = 0.8f * u
        )

        for (i in -1..1 step 2) {
            drawCircle(
                color = mediumGray,
                radius = 1f * u,
                center = pt(i * 30f, segment1Top + 5f)
            )
            drawCircle(
                color = mediumGray,
                radius = 1f * u,
                center = pt(i * 27f, segment1Bottom - 5f)
            )
        }

        val screenCenterY = 97f

        drawRoundRect(
            color = darkerGray,
            topLeft = pt(-22f, screenCenterY - 12f),
            size = Size(44f * u, 24f * u),
            cornerRadius = CornerRadius(6f * u)
        )

        drawRoundRect(
            color = Color(0xFF0F1216),
            topLeft = pt(-20f, screenCenterY - 10f),
            size = Size(40f * u, 20f * u),
            cornerRadius = CornerRadius(5f * u)
        )

        drawRoundRect(
            color = Color.White.copy(alpha = 0.1f),
            topLeft = pt(-18f, screenCenterY - 9f),
            size = Size(36f * u, 4f * u),
            cornerRadius = CornerRadius(2f * u)
        )

        val heartCenter = pt(0f, screenCenterY)
        val corePulse = 0.5f + 0.5f * sin(pulse * 1.5f)
        val sunRadius = 5f * u * (1f + 0.15f * corePulse)

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

        drawCircle(
            color = Color.White.copy(alpha = 0.9f),
            radius = sunRadius * 0.35f,
            center = Offset(heartCenter.x - sunRadius * 0.2f, heartCenter.y - sunRadius * 0.2f)
        )

        drawCircle(
            color = Color(0xFF00FFFF).copy(alpha = 0.3f + 0.2f * corePulse),
            radius = sunRadius * 1.6f,
            center = heartCenter,
            style = Stroke(width = 0.6f * u)
        )

        val segment2Top = segment1Bottom
        val segment2Bottom = 127f
        val segment2WidthTop = segment1WidthBottom
        val segment2WidthBottom = 28f

        val segment2Path = Path().apply {
            moveTo(pt(-segment2WidthTop, segment2Top).x, pt(-segment2WidthTop, segment2Top).y)
            lineTo(pt(segment2WidthTop, segment2Top).x, pt(segment2WidthTop, segment2Top).y)
            quadraticBezierTo(
                pt(segment2WidthBottom, segment2Bottom).x, pt(segment2WidthBottom, segment2Bottom).y,
                pt(segment2WidthBottom, segment2Bottom).x, pt(segment2WidthBottom, segment2Bottom).y
            )
            lineTo(pt(-segment2WidthBottom, segment2Bottom).x, pt(-segment2WidthBottom, segment2Bottom).y)
            quadraticBezierTo(
                pt(-segment2WidthTop, segment2Top).x, pt(-segment2WidthTop, segment2Top).y,
                pt(-segment2WidthTop, segment2Top).x, pt(-segment2WidthTop, segment2Top).y
            )
            close()
        }

        drawPath(
            segment2Path,
            brush = Brush.verticalGradient(
                colors = listOf(lightGray, mediumGray),
                startY = pt(0f, segment2Top).y,
                endY = pt(0f, segment2Bottom).y
            )
        )
        drawPath(segment2Path, color = darkGray, style = Stroke(width = 1.2f * u))

        drawLine(
            color = mediumGray,
            start = pt(-segment2WidthBottom + 2f, segment2Bottom - 2f),
            end = pt(segment2WidthBottom - 2f, segment2Bottom - 2f),
            strokeWidth = 0.8f * u
        )

        for (i in -1..1 step 2) {
            drawCircle(
                color = mediumGray,
                radius = 1f * u,
                center = pt(i * 24f, segment2Top + 5f)
            )
            drawCircle(
                color = mediumGray,
                radius = 1f * u,
                center = pt(i * 20f, segment2Bottom - 5f)
            )
        }
        val beltHeight = 5f * u
        val beltCenterY = 139.25f

        val segment2_5Top = segment2Bottom
        val segment2_5Bottom = beltCenterY - beltHeight / (2f * u)
        val segment2_5WidthTop = segment2WidthBottom
        val segment2_5WidthBottom = 30f

        val segment2_5Path = Path().apply {
            moveTo(pt(-segment2_5WidthTop, segment2_5Top).x, pt(-segment2_5WidthTop, segment2_5Top).y)
            lineTo(pt(segment2_5WidthTop, segment2_5Top).x, pt(segment2_5WidthTop, segment2_5Top).y)
            quadraticBezierTo(
                pt(segment2_5WidthBottom, segment2_5Bottom).x, pt(segment2_5WidthBottom, segment2_5Bottom).y,
                pt(segment2_5WidthBottom, segment2_5Bottom).x, pt(segment2_5WidthBottom, segment2_5Bottom).y
            )
            lineTo(pt(-segment2_5WidthBottom, segment2_5Bottom).x, pt(-segment2_5WidthBottom, segment2_5Bottom).y)
            quadraticBezierTo(
                pt(-segment2_5WidthTop, segment2_5Top).x, pt(-segment2_5WidthTop, segment2_5Top).y,
                pt(-segment2_5WidthTop, segment2_5Top).x, pt(-segment2_5WidthTop, segment2_5Top).y
            )
            close()
        }

        drawPath(
            segment2_5Path,
            brush = Brush.verticalGradient(
                colors = listOf(mediumGray, mediumGray, darkGray),
                startY = pt(0f, segment2_5Top).y,
                endY = pt(0f, segment2_5Bottom).y
            )
        )
        drawPath(segment2_5Path, color = darkGray, style = Stroke(width = 1.2f * u))

        for (i in -1..1 step 2) {
            drawCircle(
                color = mediumGray,
                radius = 1f * u,
                center = pt(i * 22f, segment2_5Top + 3f)
            )
            drawCircle(
                color = mediumGray,
                radius = 1f * u,
                center = pt(i * 24f, segment2_5Bottom - 3f)
            )
        }

              val beltTop = beltCenterY - beltHeight / (2f * u)
        val beltWidth = 60f * u
        val beltLeft = -30f

        drawRoundRect(
            color = darkerGray,
            topLeft = pt(beltLeft, beltTop),
            size = Size(beltWidth, beltHeight),
            cornerRadius = CornerRadius(2f * u)
        )
                drawRoundRect(
            color = darkGray,
            topLeft = pt(beltLeft, beltTop),
            size = Size(beltWidth, beltHeight),
            cornerRadius = CornerRadius(2f * u),
            style = Stroke(width = 1f * u)
        )

        // Прогресс бегущего индикатора (0..1).
        // Объявлен здесь, потому что используется и индикатором, и текстом ниже.
        val chargeProgress = ((pulse % (2f * PI.toFloat())) / (2f * PI.toFloat())).toFloat()

        // ===== БЕГУЩИЙ ТЕКСТ СОСТОЯНИЯ НА ПОЯСЕ =====
        // Индикатор — «голова», текст — «хвост» справа от него.
        // Едут вместе с одинаковой скоростью (chargeProgress).
        // Лента бесконечная: ушла за правый край — снова выезжает слева.
        // Когда робот не занят — едет только индикатор.
                       // ===== РАСЧЁТ ГЕОМЕТРИИ ИНДИКАТОРА (chargeBar) =====
        // Объявлено ЗДЕСЬ, до if (statusText != null), чтобы было видно
        // и в ленте, и в самом индикаторе ниже.
        val chargeBarLeft = beltLeft + 2f
        val chargeBarRight = beltLeft + 30f + 28f - 2f
        val chargeBarWidth = chargeBarRight - chargeBarLeft
        val chargeBarHeight = 1.5f
        val chargeBarY = beltTop + (beltHeight / (2f * u)) - chargeBarHeight / 2f

        // Позиция индикатора (0..1) — той же скорости, что и лента.
        val headProgress = chargeProgress

        // Текст состояния на поясе. Показывается ВМЕСТО индикатора,
        // мигает по центру пояса. При isSpeaking — не показывается
        // (звук идёт через TTS, отдельная индикация не нужна).
                       val statusText: String? = when {
            visionPhase == VisionPhase.RELEASING_TEXT -> "Выгружаю текст..."
            visionPhase == VisionPhase.LOADING_VISION -> "Загружаю зрение..."
            visionPhase == VisionPhase.VISION_READY -> "Зрение готово"
            visionPhase == VisionPhase.RESTORING_TEXT -> "Возвращаю текст..."
            visionPhase == VisionPhase.TEXT_RESTORED -> "Текст готов"
            isPreparingSpeech -> "Готовлю озвучку..."
            isCompressing -> "Сжимаю беседу..."
            isThinking -> "ИИ думает..."
            isLoadingModel -> "Загружаю модель..."
            isListening -> "Слушаю..."
            else -> null
        }

                      if (statusText != null) {
            // Мигающий текст по центру пояса.
            // Мигание — быстрое (0.6 сек цикл), через sin(indicatorPulse).
            // Прозрачность: 0.3 → 1.0 → 0.3.
            val blinkAlpha = 0.3f + 0.7f * ((kotlin.math.sin(pulse * 5f) + 1f) / 2f)

            // Целевая высота текста — 80% от высоты пояса.
            // Пояс: beltHeight = 5f * u.
            val beltHeightPx = beltHeight  // = 5f * u
            val maxTextHeightPx = beltHeightPx * 0.8f

            // Стартовый размер шрифта = максимально возможная высота.
            val startFontSizePx = maxTextHeightPx

            // Пробный замер, чтобы вычислить реальную высоту глифа.
            val probeStyle = TextStyle(
                color = Color(0xFF80EFFF),
                fontSize = with(density) { startFontSizePx.toSp() },
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
            val probeLayout = textMeasurer.measure(
                text = "Mg",
                style = probeStyle
            )
            val probeHeightPx = probeLayout.size.height.toFloat()

            // Подгонка по высоте.
            val heightScale = if (probeHeightPx > 0f) {
                maxTextHeightPx / probeHeightPx
            } else {
                1f
            }
            val finalFontSizePx = startFontSizePx * heightScale

            val statusStyle = TextStyle(
                color = Color(0xFF80EFFF).copy(alpha = blinkAlpha),
                fontSize = with(density) { finalFontSizePx.toSp() },
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )

            // Измеряем текст ОДИН РАЗ.
            val textLayout = textMeasurer.measure(
                text = statusText,
                style = statusStyle
            )

            // Центр пояса по X и Y в экранных пикселях.
            val beltCenterXpx = pt(beltLeft + 30f, beltCenterY).x
            val beltCenterYpx = pt(0f, beltCenterY).y

            // Левый верхний угол текста так, чтобы он был отцентрирован.
            val textTopLeft = Offset(
                x = beltCenterXpx - textLayout.size.width / 2f,
                y = beltCenterYpx - textLayout.size.height / 2f
            )

            // Свечение вокруг текста — тоже мигает, чтобы текст «горел».
            val glowAlpha = blinkAlpha * 0.4f
            val textWidthPx = textLayout.size.width.toFloat()
            val textHeightPx = textLayout.size.height.toFloat()

            drawRoundRect(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF80EFFF).copy(alpha = glowAlpha),
                        Color(0xFF00BFFF).copy(alpha = glowAlpha * 0.5f),
                        Color.Transparent
                    ),
                    center = Offset(beltCenterXpx, beltCenterYpx),
                    radius = kotlin.math.max(textWidthPx, textHeightPx) * 1.2f
                ),
                topLeft = Offset(
                    beltCenterXpx - textWidthPx * 0.7f,
                    beltCenterYpx - textHeightPx * 1.2f
                ),
                size = Size(textWidthPx * 1.4f, textHeightPx * 2.4f),
                cornerRadius = CornerRadius(textHeightPx)
            )

            drawText(
                textLayoutResult = textLayout,
                topLeft = textTopLeft
            )
        }

        
               // Индикатор на поясе показывается ТОЛЬКО когда нет статусного текста.
        // Когда текста нет — бегущая полоска работает как обычно.
        if (statusText == null) {
            drawRoundRect(
                Color(0xFF0A1520),
                topLeft = pt(chargeBarLeft, chargeBarY),
                size = Size(chargeBarWidth * u, chargeBarHeight * u),
                cornerRadius = CornerRadius(0.7f * u)
            )

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

                drawCircle(
                    color = neonBlueGlow.copy(alpha = 0.9f),
                    radius = 0.5f * u,
                    center = pt(chargeBarStart + 6f, chargeBarY + chargeBarHeight / 2f)
                )
            }
        }
        val segment3Top = beltCenterY + beltHeight / (2f * u)
        val segment3Bottom = 155f
        val segment3WidthTop = 30f
        val segment3WidthBottom = 30f
        val segment3CornerRadius = 4f

        val segment3Path = Path().apply {
            moveTo(pt(-segment3WidthTop + segment3CornerRadius, segment3Top).x, pt(-segment3WidthTop + segment3CornerRadius, segment3Top).y)
            lineTo(pt(segment3WidthTop - segment3CornerRadius, segment3Top).x, pt(segment3WidthTop - segment3CornerRadius, segment3Top).y)
            quadraticBezierTo(
                pt(segment3WidthTop, segment3Top).x, pt(segment3WidthTop, segment3Top).y,
                pt(segment3WidthTop, segment3Top + segment3CornerRadius).x, pt(segment3WidthTop, segment3Top + segment3CornerRadius).y
            )
            lineTo(pt(segment3WidthBottom, segment3Bottom - segment3CornerRadius).x, pt(segment3WidthBottom, segment3Bottom - segment3CornerRadius).y)
            quadraticBezierTo(
                pt(segment3WidthBottom, segment3Bottom).x, pt(segment3WidthBottom, segment3Bottom).y,
                pt(segment3WidthBottom - segment3CornerRadius, segment3Bottom).x, pt(segment3WidthBottom - segment3CornerRadius, segment3Bottom).y
            )
            lineTo(pt(-segment3WidthBottom + segment3CornerRadius, segment3Bottom).x, pt(-segment3WidthBottom + segment3CornerRadius, segment3Bottom).y)
            quadraticBezierTo(
                pt(-segment3WidthBottom, segment3Bottom).x, pt(-segment3WidthBottom, segment3Bottom).y,
                pt(-segment3WidthBottom, segment3Bottom - segment3CornerRadius).x, pt(-segment3WidthBottom, segment3Bottom - segment3CornerRadius).y
            )
            lineTo(pt(-segment3WidthTop, segment3Top + segment3CornerRadius).x, pt(-segment3WidthTop, segment3Top + segment3CornerRadius).y)
            quadraticBezierTo(
                pt(-segment3WidthTop, segment3Top).x, pt(-segment3WidthTop, segment3Top).y,
                pt(-segment3WidthTop + segment3CornerRadius, segment3Top).x, pt(-segment3WidthTop + segment3CornerRadius, segment3Top).y
            )
            close()
        }

        drawPath(
            segment3Path,
            brush = Brush.verticalGradient(
                colors = listOf(mediumGray, darkGray),
                startY = pt(0f, segment3Top).y,
                endY = pt(0f, segment3Bottom).y
            )
        )
        drawPath(segment3Path, color = darkGray, style = Stroke(width = 1.2f * u))

        for (i in -1..1 step 2) {
            drawCircle(
                color = mediumGray,
                radius = 1f * u,
                center = pt(i * 20f, segment3Top + 3f)
            )
        }

        val nozzleTop = 155f
        val nozzleThroat = 165f
        val nozzleBottom = 180f
        val nozzleTopWidth = 31.2f
        val nozzleThroatWidth = 19.2f
        val nozzleBottomWidth = 26.4f

        val nozzlePath = Path().apply {
            moveTo(pt(-nozzleTopWidth / 2f, nozzleTop).x, pt(-nozzleTopWidth / 2f, nozzleTop).y)
            quadraticBezierTo(
                pt(-nozzleThroatWidth / 2f, nozzleThroat).x, pt(-nozzleThroatWidth / 2f, nozzleThroat).y,
                pt(-nozzleThroatWidth / 2f, nozzleThroat).x, pt(-nozzleThroatWidth / 2f, nozzleThroat).y
            )
            quadraticBezierTo(
                pt(-nozzleBottomWidth / 2f, nozzleBottom).x, pt(-nozzleBottomWidth / 2f, nozzleBottom).y,
                pt(-nozzleBottomWidth / 2f, nozzleBottom).x, pt(-nozzleBottomWidth / 2f, nozzleBottom).y
            )
            lineTo(pt(nozzleBottomWidth / 2f, nozzleBottom).x, pt(nozzleBottomWidth / 2f, nozzleBottom).y)
            quadraticBezierTo(
                pt(nozzleThroatWidth / 2f, nozzleThroat).x, pt(nozzleThroatWidth / 2f, nozzleThroat).y,
                pt(nozzleThroatWidth / 2f, nozzleThroat).x, pt(nozzleThroatWidth / 2f, nozzleThroat).y
            )
            quadraticBezierTo(
                pt(nozzleTopWidth / 2f, nozzleTop).x, pt(nozzleTopWidth / 2f, nozzleTop).y,
                pt(nozzleTopWidth / 2f, nozzleTop).x, pt(nozzleTopWidth / 2f, nozzleTop).y
            )
            close()
        }

        drawPath(
            nozzlePath,
            brush = Brush.verticalGradient(
                colors = listOf(darkGray, darkerGray, Color(0xFF1A1A20)),
                startY = pt(0f, nozzleTop).y,
                endY = pt(0f, nozzleBottom).y
            )
        )
        drawPath(nozzlePath, color = darkerGray, style = Stroke(width = 1.3f * u))

        for (i in 1..5) {
            val y = nozzleTop + i * 5f
            val width = when {
                y < nozzleThroat -> nozzleTopWidth - (nozzleTopWidth - nozzleThroatWidth) * (y - nozzleTop) / (nozzleThroat - nozzleTop)
                else -> nozzleThroatWidth + (nozzleBottomWidth - nozzleThroatWidth) * (y - nozzleThroat) / (nozzleBottom - nozzleThroat)
            }

            drawLine(
                color = Color(0xFF0A0A14).copy(alpha = 0.5f),
                start = pt(-width / 2f, y),
                end = pt(width / 2f, y),
                strokeWidth = 0.7f * u
            )
        }

        drawLine(
            color = whiteHighlight.copy(alpha = 0.3f),
            start = pt(-nozzleTopWidth / 2f + 3f, nozzleTop + 2f),
            end = pt(-nozzleBottomWidth / 2f + 2f, nozzleBottom - 2f),
            strokeWidth = 1.5f * u,
            cap = StrokeCap.Round
        )

        drawRoundRect(
            color = mediumGray,
            topLeft = pt(-nozzleTopWidth / 2f - 1f, nozzleTop - 1f),
            size = Size((nozzleTopWidth + 2f) * u, 3f * u),
            cornerRadius = CornerRadius(1.5f * u)
        )

        drawRoundRect(
            color = darkerGray,
            topLeft = pt(-nozzleBottomWidth / 2f - 2f, nozzleBottom),
            size = Size((nozzleBottomWidth + 4f) * u, 3f * u),
            cornerRadius = CornerRadius(1.5f * u)
        )

        val flameFlicker = sin(flamePhase * 2.5f) * 2f
        val flameFlickerX = sin(flamePhase * 3.7f) * 1.5f
        val flamePulse = 0.7f + 0.3f * sin(flamePhase * 6f)

        // Множитель пламени от прыжка: 1 → 2.8 на пике.
        val jumpFlameMult = 1f + flameIntensity * 1.8f
                drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFF00AAFF).copy(alpha = 0.4f * flamePulse),
                    Color(0xFF0066FF).copy(alpha = 0.2f * flamePulse),
                    Color.Transparent
                ),
                center = pt(0f, 195f).copy(x = pt(0f, 195f).x + flameFlickerX * u),
                radius = 30f * u * jumpFlameMult
            ),
            radius = 30f * u * jumpFlameMult,
            center = pt(0f, 195f).copy(x = pt(0f, 195f).x + flameFlickerX * u)
        )

                val outerFlamePath = Path().apply {
            moveTo(pt(-14.4f, nozzleBottom + 2f).x, pt(-14.4f, nozzleBottom + 2f).y)
            quadraticBezierTo(
                pt(-12f + flameFlickerX * 0.5f, nozzleBottom + 15f * jumpFlameMult).x,
                pt(-12f + flameFlickerX * 0.5f, nozzleBottom + 15f * jumpFlameMult).y,
                pt(-9.6f + flameFlickerX * 0.3f, nozzleBottom + 25f * jumpFlameMult).x,
                pt(-9.6f + flameFlickerX * 0.3f, nozzleBottom + 25f * jumpFlameMult).y
            )
            quadraticBezierTo(
                pt(0f, nozzleBottom + (30f + flameFlicker) * jumpFlameMult).x,
                pt(0f, nozzleBottom + (30f + flameFlicker) * jumpFlameMult).y,
                pt(9.6f + flameFlickerX * 0.3f, nozzleBottom + 25f * jumpFlameMult).x,
                pt(9.6f + flameFlickerX * 0.3f, nozzleBottom + 25f * jumpFlameMult).y
            )
            quadraticBezierTo(
                pt(12f + flameFlickerX * 0.5f, nozzleBottom + 15f * jumpFlameMult).x,
                pt(12f + flameFlickerX * 0.5f, nozzleBottom + 15f * jumpFlameMult).y,
                pt(14.4f, nozzleBottom + 2f).x, pt(14.4f, nozzleBottom + 2f).y
            )
            close()
        }

                drawPath(
            outerFlamePath,
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFFFF3300),
                    Color(0xFFFF4500),
                    Color(0xFFFF6600),
                    Color(0xFFFF8800).copy(alpha = 0.7f)
                ),
                startY = pt(0f, nozzleBottom).y,
                endY = pt(0f, nozzleBottom + 30f * jumpFlameMult).y
            )
        )

                val middleFlamePath = Path().apply {
            moveTo(pt(-8.4f, nozzleBottom + 3f).x, pt(-8.4f, nozzleBottom + 3f).y)
            quadraticBezierTo(
                pt(-6f, nozzleBottom + 12f * jumpFlameMult).x, pt(-6f, nozzleBottom + 12f * jumpFlameMult).y,
                pt(-3.6f, nozzleBottom + 20f * jumpFlameMult).x, pt(-3.6f, nozzleBottom + 20f * jumpFlameMult).y
            )
            quadraticBezierTo(
                pt(0f, nozzleBottom + 23f * jumpFlameMult).x, pt(0f, nozzleBottom + 23f * jumpFlameMult).y,
                pt(3.6f, nozzleBottom + 20f * jumpFlameMult).x, pt(3.6f, nozzleBottom + 20f * jumpFlameMult).y
            )
            quadraticBezierTo(
                pt(6f, nozzleBottom + 12f * jumpFlameMult).x, pt(6f, nozzleBottom + 12f * jumpFlameMult).y,
                pt(8.4f, nozzleBottom + 3f).x, pt(8.4f, nozzleBottom + 3f).y
            )
            close()
        }
                drawPath(
            middleFlamePath,
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFFFF8800),
                    Color(0xFFFFAA00),
                    Color(0xFFFFCC00),
                    Color(0xFFFFE066).copy(alpha = 0.8f)
                ),
                startY = pt(0f, nozzleBottom).y,
                endY = pt(0f, nozzleBottom + 25f * jumpFlameMult).y
            )
        )

        for (i in 0..2) {
            val diamondY = nozzleBottom + 8f + i * 5f
            val diamondSize = (3f - i * 0.72f) * u
            val diamondAlpha = 0.7f - i * 0.2f

            drawCircle(
                color = Color(0xFFFFEE88).copy(alpha = diamondAlpha * flamePulse),
                radius = diamondSize,
                center = pt(0f, diamondY),
                style = Stroke(width = 0.5f * u)
            )

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = diamondAlpha * flamePulse),
                        Color(0xFFFFF5B0).copy(alpha = diamondAlpha * 0.5f),
                        Color.Transparent
                    ),
                    center = pt(0f, diamondY),
                    radius = diamondSize * 0.6f
                ),
                radius = diamondSize * 0.6f,
                center = pt(0f, diamondY)
            )
        }

               val innerFlamePath = Path().apply {
            moveTo(pt(-4.8f, nozzleBottom + 4f).x, pt(-4.8f, nozzleBottom + 4f).y)
            quadraticBezierTo(
                pt(-2.4f, nozzleBottom + 10f * jumpFlameMult).x, pt(-2.4f, nozzleBottom + 10f * jumpFlameMult).y,
                pt(0f, nozzleBottom + 15f * jumpFlameMult).x, pt(0f, nozzleBottom + 15f * jumpFlameMult).y
            )
            quadraticBezierTo(
                pt(2.4f, nozzleBottom + 10f * jumpFlameMult).x, pt(2.4f, nozzleBottom + 10f * jumpFlameMult).y,
                pt(4.8f, nozzleBottom + 4f).x, pt(4.8f, nozzleBottom + 4f).y
            )
            close()
        }

                drawPath(
            innerFlamePath,
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color.White,
                    Color(0xFFFFF5B0),
                    Color(0xFFFFE066).copy(alpha = 0.7f)
                ),
                startY = pt(0f, nozzleBottom).y,
                endY = pt(0f, nozzleBottom + 18f * jumpFlameMult).y
            )
        )

                val sparkCount = 10
        for (i in 0 until sparkCount) {
            val sparkPhase = (flamePhase * 3f + i * 0.6f) % (2f * PI.toFloat())
            val sparkProgress = sparkPhase / (2f * PI.toFloat())

            val sparkY = nozzleBottom + 5f + sparkProgress * 35f * jumpFlameMult
            val sparkX = -9.6f + i * 2.16f + sin(sparkPhase * 4f) * 3f
            val sparkAlpha = (1f - sparkProgress) * 0.9f
            val sparkR = (1f - sparkProgress * 0.7f) * u

            if (sparkAlpha > 0.05f && sparkR > 0f) {
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

        // ================= ПЫЛЬ И ИСКРЫ ПРИ ПРЫЖКЕ =================
        // Появляются только когда идёт прыжок (flameIntensity > 0).
        // Пыль — сине-голубая, расходится вниз и в стороны от сопла.
        // Искры — 15 штук, летят вниз. Защита от нулевого радиуса обязательна.
        if (flameIntensity > 0.01f) {
            val dustCenter = pt(0f, nozzleBottom + 5f)
            val dustAlphaBase = flameIntensity

            // 1. Общее облако пыли под соплом.
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF80EFFF).copy(alpha = 0.25f * dustAlphaBase),
                        Color(0xFF00BFFF).copy(alpha = 0.15f * dustAlphaBase),
                        Color(0xFF0044AA).copy(alpha = 0.05f * dustAlphaBase),
                        Color.Transparent
                    ),
                    center = dustCenter,
                    radius = 40f * u * dustAlphaBase
                ),
                radius = 40f * u * dustAlphaBase,
                center = dustCenter
            )

            // 2. Расходящиеся клубы пыли — 6 штук, летят вниз-в стороны.
            val dustPuffCount = 6
            for (i in 0 until dustPuffCount) {
                val puffAngle = (i.toFloat() / dustPuffCount) * PI.toFloat() + PI.toFloat()
                val puffDist = (10f + i * 3f) * u * dustAlphaBase
                val puffRadius = (6f + i * 0.8f) * u * dustAlphaBase

                if (puffRadius > 0.01f) {
                    val puffX = dustCenter.x + cos(puffAngle) * puffDist
                    val puffY = dustCenter.y + abs(sin(puffAngle)) * puffDist * 0.8f
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF80EFFF).copy(alpha = 0.35f * dustAlphaBase),
                                Color(0xFF00BFFF).copy(alpha = 0.15f * dustAlphaBase),
                                Color.Transparent
                            ),
                            center = Offset(puffX, puffY),
                            radius = puffRadius
                        ),
                        radius = puffRadius,
                        center = Offset(puffX, puffY)
                    )
                }
            }

            // 3. Искры — 15 штук, летят вниз.
            val jumpSparkCount = 15
            for (i in 0 until jumpSparkCount) {
                val jumpSparkPhase = (flamePhase * 4f + i * 0.42f) % (2f * PI.toFloat())
                val jumpSparkProgress = jumpSparkPhase / (2f * PI.toFloat())

                // Искра летит от сопла вниз.
                val sparkDist = jumpSparkProgress * 45f * u * dustAlphaBase
                val sparkAngleSide = sin(flamePhase * 2f + i * 1.3f) * 0.6f
                val sparkX = sparkAngleSide * 10f * u + (i - jumpSparkCount / 2f) * 0.8f * u
                val sparkY = nozzleBottom + 5f + sparkDist / u

                val jumpSparkAlpha = (1f - jumpSparkProgress) * dustAlphaBase
                val jumpSparkR = (1f - jumpSparkProgress * 0.6f) * 1.2f * u

                // Обязательная защита: радиус должен быть > 0.
                if (jumpSparkAlpha > 0.05f && jumpSparkR > 0.01f) {
                    // Ядро искры — белое.
                    drawCircle(
                        color = Color.White.copy(alpha = jumpSparkAlpha),
                        radius = jumpSparkR,
                        center = pt(sparkX, sparkY)
                    )
                    // Свечение вокруг — сине-голубое.
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF80EFFF).copy(alpha = jumpSparkAlpha * 0.6f),
                                Color(0xFF00BFFF).copy(alpha = jumpSparkAlpha * 0.3f),
                                Color.Transparent
                            ),
                            center = pt(sparkX, sparkY),
                            radius = jumpSparkR * 4f
                        ),
                        radius = jumpSparkR * 4f,
                        center = pt(sparkX, sparkY)
                    )
                }
            }
        }

        // ================= НАДПИСЬ "ИИ-Друг" =================
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
        val labelCenterY = 117f
        val labelCenterPos = pt(0f, labelCenterY)
        drawText(
            textLayoutResult = labelLayout,
            topLeft = Offset(
                x = size.width / 2f - labelLayout.size.width / 2f,
                y = labelCenterPos.y - labelLayout.size.height / 2f
            )
        )

    // ================= ГОЛОВА И ВСЁ, ЧТО С НЕЙ СВЯЗАНО =================
        // Оборачиваем в rotate(headTilt) — голова наклоняется вокруг основания шеи.
        // Поворот головы (headTurn) — это НЕ сжатие и НЕ сдвиг всей головы.
        // Шлем и шея остаются на месте. Поворачиваются элементы «лица»
        // (панель мозга, визор, глаза, брови, рот) и уши — через ptFace.
        // headTurnShiftX объявлен выше, до ptFace.

        rotate(effectiveHeadTilt, pivot = pt(0f, 62f)) {

                       // ================= ШЕЯ =================
            // Все элементы шеи рисуются через ptNeck — БЕЗ смещения головы.
            // Голова кивает и наклоняется, шея остаётся на месте.
            drawRoundRect(
                color = lightGray,
                topLeft = ptNeck(-14f, 62f),
                size = Size(28f * u, 4f * u),
                cornerRadius = CornerRadius(2f * u)
            )
            drawRoundRect(
                color = darkGray,
                topLeft = ptNeck(-14f, 62f),
                size = Size(28f * u, 4f * u),
                cornerRadius = CornerRadius(2f * u),
                style = Stroke(width = 1f * u)
            )

            drawRoundRect(
                color = mediumGray,
                topLeft = ptNeck(-13f, 66f),
                size = Size(26f * u, 3.5f * u),
                cornerRadius = CornerRadius(1.8f * u)
            )
            drawRoundRect(
                color = darkGray,
                topLeft = ptNeck(-13f, 66f),
                size = Size(26f * u, 3.5f * u),
                cornerRadius = CornerRadius(1.8f * u),
                style = Stroke(width = 1f * u)
            )

            drawRoundRect(
                color = lightGray,
                topLeft = ptNeck(-12f, 69.5f),
                size = Size(24f * u, 3.5f * u),
                cornerRadius = CornerRadius(1.8f * u)
            )
            drawRoundRect(
                color = darkGray,
                topLeft = ptNeck(-12f, 69.5f),
                size = Size(24f * u, 3.5f * u),
                cornerRadius = CornerRadius(1.8f * u),
                style = Stroke(width = 1f * u)
            )

            drawRoundRect(
                color = mediumGray,
                topLeft = ptNeck(-11f, 73f),
                size = Size(22f * u, 3f * u),
                cornerRadius = CornerRadius(1.5f * u)
            )
            drawRoundRect(
                color = darkGray,
                topLeft = ptNeck(-11f, 73f),
                size = Size(22f * u, 3f * u),
                cornerRadius = CornerRadius(1.5f * u),
                style = Stroke(width = 1f * u)
            )

            drawLine(
                darkerGray,
                ptNeck(-12f, 65.5f),
                ptNeck(12f, 65.5f),
                strokeWidth = 0.5f * u
            )
            drawLine(
                darkerGray,
                ptNeck(-12f, 69f),
                ptNeck(12f, 69f),
                strokeWidth = 0.5f * u
            )
            drawLine(
                darkerGray,
                ptNeck(-11f, 72.5f),
                ptNeck(11f, 72.5f),
                strokeWidth = 0.5f * u
            )

            drawRoundRect(
                color = whiteHighlight.copy(alpha = 0.6f),
                topLeft = ptNeck(-13f, 62.5f),
                size = Size(2f * u, 3f * u),
                cornerRadius = CornerRadius(1f * u)
            )
            drawRoundRect(
                color = whiteHighlight.copy(alpha = 0.6f),
                topLeft = ptNeck(-12f, 66.5f),
                size = Size(2f * u, 2.5f * u),
                cornerRadius = CornerRadius(1f * u)
            )
            drawRoundRect(
                color = whiteHighlight.copy(alpha = 0.6f),
                topLeft = ptNeck(-11f, 70f),
                size = Size(2f * u, 2.5f * u),
                cornerRadius = CornerRadius(1f * u)
            )
            drawRoundRect(
                color = whiteHighlight.copy(alpha = 0.6f),
                topLeft = ptNeck(-10f, 73.5f),
                size = Size(2f * u, 2f * u),
                cornerRadius = CornerRadius(1f * u)
            )

     // ================= ЛЕВЫЙ НАУШНИК =================
     rotate(12f, pivot = ptFace(-42f, 45f)) {
                val leftEarOuterPath = Path().apply {
                    moveTo(ptFace(-42f, 18f).x, ptFace(-42f, 18f).y)
                    cubicTo(
                        ptFace(-56.4f, 20f).x, ptFace(-56.4f, 20f).y,
                        ptFace(-56.4f, 43f).x, ptFace(-56.4f, 43f).y,
                        ptFace(-42f, 45f).x, ptFace(-42f, 45f).y
                    )
                    lineTo(ptFace(-42f, 18f).x, ptFace(-42f, 18f).y)
                    close()
                }
                drawPath(
                    leftEarOuterPath,
                    brush = Brush.horizontalGradient(
                        colors = listOf(lightGray, whiteBody),
                        startX = ptFace(-56.4f, 32f).x,
                        endX = ptFace(-42f, 32f).x
                    )
                )
                drawPath(leftEarOuterPath, color = darkGray, style = Stroke(width = 1.3f * u))

                val leftEarInnerPath = Path().apply {
                    moveTo(ptFace(-47f, 23f).x, ptFace(-47f, 23f).y)
                    cubicTo(
                        ptFace(-55f, 25f).x, ptFace(-55f, 25f).y,
                        ptFace(-55f, 38f).x, ptFace(-55f, 38f).y,
                        ptFace(-47f, 40f).x, ptFace(-47f, 40f).y
                    )
                    lineTo(ptFace(-47f, 23f).x, ptFace(-47f, 23f).y)
                    close()
                }
                drawPath(leftEarInnerPath, color = mediumGray)
                drawPath(leftEarInnerPath, color = darkGray, style = Stroke(width = 1.1f * u))

                drawCircle(
                    color = darkerGray,
                    radius = 2.8f * u,
                    center = ptFace(-50f, 31f)
                )

                drawLine(
                    color = darkGray,
                    start = ptFace(-46f, 18f),
                    end = ptFace(-46f, -4f),
                    strokeWidth = 1.8f * u,
                    cap = StrokeCap.Round
                )
                drawRoundRect(
                    color = darkerGray,
                    topLeft = ptFace(-48f, 17f),
                    size = Size(4f * u, 3f * u),
                    cornerRadius = CornerRadius(1.5f * u)
                )
                if (isSmartMode) {
                    val pulseAmount = (sin(smartPulse) + 1f) / 2f
                    val currentRadius = 1.8f * u * (1f + 0.35f * pulseAmount)

                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                neonBlue.copy(alpha = 0.55f * pulseAmount),
                                neonBlue.copy(alpha = 0.25f * pulseAmount),
                                Color.Transparent
                            ),
                            center = ptFace(-46f, -4f),
                            radius = currentRadius * 4f
                        ),
                        radius = currentRadius * 4f,
                        center = ptFace(-46f, -4f)
                    )

                    drawCircle(
                        color = neonBlue.copy(alpha = 0.85f),
                        radius = currentRadius,
                        center = ptFace(-46f, -4f)
                    )
                    drawCircle(
                        color = Color.White.copy(alpha = 0.9f),
                        radius = currentRadius,
                        center = ptFace(-46f, -4f),
                        style = Stroke(width = 0.8f * u)
                    )
                } else {
                    drawCircle(
                        color = mediumGray,
                        radius = 1.8f * u,
                        center = ptFace(-46f, -4f)
                    )
                    drawCircle(
                        color = darkGray,
                        radius = 1.8f * u,
                        center = ptFace(-46f, -4f),
                        style = Stroke(width = 0.8f * u)
                    )
                }
            }

             // ================= ПРАВЫЙ НАУШНИК =================
               rotate(-12f, pivot = ptFace(42f, 45f)) {
                val rightEarOuterPath = Path().apply {
                    moveTo(ptFace(42f, 18f).x, ptFace(42f, 18f).y)
                    cubicTo(
                        ptFace(56.4f, 20f).x, ptFace(56.4f, 20f).y,
                        ptFace(56.4f, 43f).x, ptFace(56.4f, 43f).y,
                        ptFace(42f, 45f).x, ptFace(42f, 45f).y
                    )
                    lineTo(ptFace(42f, 18f).x, ptFace(42f, 18f).y)
                    close()
                }
                drawPath(
                    rightEarOuterPath,
                    brush = Brush.horizontalGradient(
                        colors = listOf(whiteBody, lightGray),
                        startX = ptFace(42f, 32f).x,
                        endX = ptFace(56.4f, 32f).x
                    )
                )
                drawPath(rightEarOuterPath, color = darkGray, style = Stroke(width = 1.3f * u))

                val rightEarInnerPath = Path().apply {
                    moveTo(ptFace(47f, 23f).x, ptFace(47f, 23f).y)
                    cubicTo(
                        ptFace(55f, 25f).x, ptFace(55f, 25f).y,
                        ptFace(55f, 38f).x, ptFace(55f, 38f).y,
                        ptFace(47f, 40f).x, ptFace(47f, 40f).y
                    )
                    lineTo(ptFace(47f, 23f).x, ptFace(47f, 23f).y)
                    close()
                }
                drawPath(rightEarInnerPath, color = mediumGray)
                drawPath(rightEarInnerPath, color = darkGray, style = Stroke(width = 1.1f * u))

                drawCircle(
                    color = darkerGray,
                    radius = 2.8f * u,
                    center = ptFace(50f, 31f)
                )

                drawLine(
                    color = darkGray,
                    start = ptFace(46f, 18f),
                    end = ptFace(46f, -4f),
                    strokeWidth = 1.8f * u,
                    cap = StrokeCap.Round
                )
                drawRoundRect(
                    color = darkerGray,
                    topLeft = ptFace(44f, 17f),
                    size = Size(4f * u, 3f * u),
                    cornerRadius = CornerRadius(1.5f * u)
                )
                if (isSmartMode) {
                    val pulseAmount = (sin(smartPulse) + 1f) / 2f
                    val currentRadius = 1.8f * u * (1f + 0.35f * pulseAmount)

                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                neonBlue.copy(alpha = 0.55f * pulseAmount),
                                neonBlue.copy(alpha = 0.25f * pulseAmount),
                                Color.Transparent
                            ),
                            center = ptFace(46f, -4f),
                            radius = currentRadius * 4f
                        ),
                        radius = currentRadius * 4f,
                        center = ptFace(46f, -4f)
                    )

                    drawCircle(
                        color = neonBlue.copy(alpha = 0.85f),
                        radius = currentRadius,
                        center = ptFace(46f, -4f)
                    )
                    drawCircle(
                        color = Color.White.copy(alpha = 0.9f),
                        radius = currentRadius,
                        center = ptFace(46f, -4f),
                        style = Stroke(width = 0.8f * u)
                    )
                } else {
                    drawCircle(
                        color = mediumGray,
                        radius = 1.8f * u,
                        center = ptFace(46f, -4f)
                    )
                    drawCircle(
                        color = darkGray,
                        radius = 1.8f * u,
                        center = ptFace(46f, -4f),
                        style = Stroke(width = 0.8f * u)
                   )
                }
            }

            // ================= ГОЛОВА =================
            val headPath = Path().apply {
                moveTo(ptHead(-42f, 48f).x, ptHead(-42f, 48f).y)
                cubicTo(
                    ptHead(-42f, 10f).x, ptHead(-42f, 10f).y,
                    ptHead(-25f, -6f).x, ptHead(-25f, -6f).y,
                    ptHead(0f, -6f).x, ptHead(0f, -6f).y
                )
                cubicTo(
                    ptHead(25f, -6f).x, ptHead(25f, -6f).y,
                    ptHead(42f, 10f).x, ptHead(42f, 10f).y,
                    ptHead(42f, 48f).x, ptHead(42f, 48f).y
                )
                cubicTo(
                    ptHead(42f, 58f).x, ptHead(42f, 58f).y,
                    ptHead(20f, 62f).x, ptHead(20f, 62f).y,
                    ptHead(0f, 62f).x, ptHead(0f, 62f).y
                )
                cubicTo(
                    ptHead(-20f, 62f).x, ptHead(-20f, 62f).y,
                    ptHead(-42f, 58f).x, ptHead(-42f, 58f).y,
                    ptHead(-42f, 48f).x, ptHead(-42f, 48f).y
                )
                close()
            }

            drawPath(
                headPath,
                brush = Brush.verticalGradient(
                    colors = listOf(whiteHighlight, whiteBody, lightGray),
                    startY = ptHead(0f, -6f).y,
                    endY = ptHead(0f, 62f).y
                )
            )
            drawPath(headPath, color = darkGray, style = Stroke(width = 1.5f * u))

            val upperBandLinePath = Path().apply {
                moveTo(ptHead(-42f, 44f).x, ptHead(-42f, 44f).y)
                cubicTo(
                    ptHead(-42f, 54f).x, ptHead(-42f, 54f).y,
                    ptHead(-20f, 58f).x, ptHead(-20f, 58f).y,
                    ptHead(0f, 58f).x, ptHead(0f, 58f).y
                )
                cubicTo(
                    ptHead(20f, 58f).x, ptHead(20f, 58f).y,
                    ptHead(42f, 54f).x, ptHead(42f, 54f).y,
                    ptHead(42f, 44f).x, ptHead(42f, 44f).y
                )
            }

            drawPath(upperBandLinePath, color = darkGray, style = Stroke(width = 1.2f * u))

            val rivetColor = mediumGray
            val rivetRadius = 1f * u

            drawCircle(
                color = rivetColor,
                radius = rivetRadius,
                center = ptHead(-38.25f, 52.42f)
            )
            drawCircle(
                color = rivetColor,
                radius = rivetRadius,
                center = ptHead(-28.5f, 56.75f)
            )
            drawCircle(
                color = rivetColor,
                radius = rivetRadius,
                center = ptHead(-15.01f, 59.22f)
            )

            drawCircle(
                color = rivetColor,
                radius = rivetRadius,
                center = ptHead(0f, 60f)
            )

            drawCircle(
                color = rivetColor,
                radius = rivetRadius,
                center = ptHead(15.01f, 59.22f)
            )
            drawCircle(
                color = rivetColor,
                radius = rivetRadius,
                center = ptHead(28.5f, 56.75f)
            )
            drawCircle(
                color = rivetColor,
                radius = rivetRadius,
                center = ptHead(38.25f, 52.42f)
            )

                        // ================= МОЗГ =================
            // Обрезаем всё «лицо» по контуру шлема.
            // Без этого при повороте головы визор/глаза/рот вылезали бы за шлем.
            // Шлем рисуется ДО этого clipPath — он не обрезается.
            clipPath(headPath) {

                       if (panelOpen > 0.05f) {
                val brainClipPath = Path().apply {
                    moveTo(ptFace(-16f, -4f).x, ptFace(-16f, -4f).y)
                    cubicTo(
                        ptFace(-8f, -6f).x, ptFace(-8f, -6f).y,
                        ptFace(8f, -6f).x, ptFace(8f, -6f).y,
                        ptFace(16f, -4f).x, ptFace(16f, -4f).y
                    )
                    cubicTo(
                        ptFace(16f, 2f).x, ptFace(16f, 2f).y,
                        ptFace(15.5f, 8f).x, ptFace(15.5f, 8f).y,
                        ptFace(15f, 14f).x, ptFace(15f, 14f).y
                    )
                    lineTo(ptFace(-15f, 14f).x, ptFace(-15f, 14f).y)
                    cubicTo(
                        ptFace(-15.5f, 8f).x, ptFace(-15.5f, 8f).y,
                        ptFace(-16f, 2f).x, ptFace(-16f, 2f).y,
                        ptFace(-16f, -4f).x, ptFace(-16f, -4f).y
                    )
                    close()
                }

                drawPath(
                    brainClipPath,
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF1A0A2E),
                            Color(0xFF0A0518),
                            Color(0xFF000000)
                        ),
                        center = ptFace(0f, 5f),
                        radius = 20f * u
                    )
                )

                val brainPulse = 0.7f + 0.3f * sin(pulse * 2f)
                val brainColor1 = Color(0xFFFF4FC3).copy(alpha = 0.8f * brainPulse)
                val brainColor2 = Color(0xFF9C27B0).copy(alpha = 0.7f * brainPulse)
                val brainColor3 = Color(0xFF00D9FF).copy(alpha = 0.5f * brainPulse)

                drawOval(
                    brush = Brush.radialGradient(
                        colors = listOf(brainColor1, brainColor2, Color.Transparent),
                        center = ptFace(-6f, 4f),
                        radius = 8f * u
                    ),
                    topLeft = ptFace(-12f, -1f),
                    size = Size(12f * u, 12f * u)
                )

                drawOval(
                    brush = Brush.radialGradient(
                        colors = listOf(brainColor1, brainColor2, Color.Transparent),
                        center = ptFace(6f, 4f),
                        radius = 8f * u
                    ),
                    topLeft = ptFace(0f, -1f),
                    size = Size(12f * u, 12f * u)
                )

                drawLine(
                    color = Color(0xFF3A1A5C),
                    start = ptFace(0f, -2f),
                    end = ptFace(0f, 12f),
                    strokeWidth = 0.8f * u,
                    cap = StrokeCap.Round
                )

                for (i in 0..4) {
                    val phase = i * 0.7f + pulse * 1.5f
                    val amplitude = 2f + sin(phase) * 0.5f
                    val yStart = -1f + i * 3f

                    val wrinklePath = Path().apply {
                        moveTo(ptFace(-11f, yStart).x, ptFace(-11f, yStart).y)
                        cubicTo(
                            ptFace(-7f, yStart - amplitude).x, ptFace(-7f, yStart - amplitude).y,
                            ptFace(-3f, yStart + amplitude).x, ptFace(-3f, yStart + amplitude).y,
                            ptFace(0f, yStart).x, ptFace(0f, yStart).y
                        )
                        cubicTo(
                            ptFace(3f, yStart - amplitude).x, ptFace(3f, yStart - amplitude).y,
                            ptFace(7f, yStart + amplitude).x, ptFace(7f, yStart + amplitude).y,
                            ptFace(11f, yStart).x, ptFace(11f, yStart).y
                        )
                    }
                    drawPath(
                        wrinklePath,
                        color = brainColor3.copy(alpha = 0.4f + 0.3f * sin(phase)),
                        style = Stroke(width = 0.6f * u, cap = StrokeCap.Round)
                    )
                }

                val orbitCount = 3
                for (orbit in 0 until orbitCount) {
                    val orbitPhase = orbit * 2.1f
                    val orbitTilt = orbit * 30f

                    rotate(orbitTilt, pivot = ptFace(0f, 5f)) {
                        drawOval(
                            color = brainColor3.copy(alpha = 0.2f),
                            topLeft = ptFace(-(6f + orbit * 2f), 5f - (4f + orbit * 1.5f)),
                            size = Size(
                                (12f + orbit * 4f) * u,
                                (8f + orbit * 3f) * u
                            ),
                            style = Stroke(width = 0.3f * u)
                        )

                        val ballCount = 2 + orbit
                        for (ball in 0 until ballCount) {
                            val angle = pulse * (1.5f + orbit * 0.5f) + ball * (2f * PI.toFloat() / ballCount) + orbitPhase
                            val bx = cos(angle) * (6f + orbit * 2f)
                            val by = 5f + sin(angle) * (4f + orbit * 1.5f)

                            val ballAlpha = 0.6f + 0.4f * sin(pulse * 3f + ball)

                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        Color.White.copy(alpha = ballAlpha * 0.8f),
                                        Color(0xFF00D9FF).copy(alpha = ballAlpha * 0.6f),
                                        Color.Transparent
                                    ),
                                    center = ptFace(bx, by),
                                    radius = 2.5f * u
                                ),
                                radius = 2.5f * u,
                                center = ptFace(bx, by)
                            )

                            drawCircle(
                                color = Color.White.copy(alpha = ballAlpha),
                                radius = 0.8f * u,
                                center = ptFace(bx, by)
                            )
                        }
                    }
                }
            }
                        val headPanelPath = Path().apply {
                moveTo(ptFace(-16f, -4f).x, ptFace(-16f, -4f).y + panelLift)
                cubicTo(
                    ptFace(-8f, -6f).x, ptFace(-8f, -6f).y + panelLift,
                    ptFace(8f, -6f).x, ptFace(8f, -6f).y + panelLift,
                    ptFace(16f, -4f).x, ptFace(16f, -4f).y + panelLift
                )
                cubicTo(
                    ptFace(16f, 2f).x, ptFace(16f, 2f).y + panelLift,
                    ptFace(15.5f, 8f).x, ptFace(15.5f, 8f).y + panelLift,
                    ptFace(15f, 14f).x, ptFace(15f, 14f).y + panelLift
                )
                lineTo(ptFace(-15f, 14f).x, ptFace(-15f, 14f).y + panelLift)
                cubicTo(
                    ptFace(-15.5f, 8f).x, ptFace(-15.5f, 8f).y + panelLift,
                    ptFace(-16f, 2f).x, ptFace(-16f, 2f).y + panelLift,
                    ptFace(-16f, -4f).x, ptFace(-16f, -4f).y + panelLift
                )
                close()
            }
            drawPath(
                headPanelPath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFEDEFF2),
                        Color(0xFFD9DEE3),
                        Color(0xFFC4CAD1)
                    ),
                    startY = ptFace(0f, -4f).y,
                    endY = ptFace(0f, 14f).y
                )
            )

            val headPanelHighlight = Path().apply {
                moveTo(ptFace(-9f, -2f).x, ptFace(-9f, -2f).y + panelLift)
                lineTo(ptFace(-4f, -2f).x, ptFace(-4f, -2f).y + panelLift)
                lineTo(ptFace(-4f, 12f).x, ptFace(-4f, 12f).y + panelLift)
                lineTo(ptFace(-9f, 12f).x, ptFace(-9f, 12f).y + panelLift)
                close()
            }
            drawPath(
                headPanelHighlight,
                color = Color.White.copy(alpha = 0.5f)
            )

            val leftHeadLinePath = Path().apply {
                moveTo(ptFace(-16f, -4f).x, ptFace(-16f, -4f).y)
                cubicTo(
                    ptFace(-16f, 2f).x, ptFace(-16f, 2f).y,
                    ptFace(-15.5f, 8f).x, ptFace(-15.5f, 8f).y,
                    ptFace(-15f, 14f).x, ptFace(-15f, 14f).y
                )
            }
            drawPath(
                leftHeadLinePath,
                color = darkGray,
                style = Stroke(width = 1f * u, cap = StrokeCap.Round)
            )

            val rightHeadLinePath = Path().apply {
                moveTo(ptFace(16f, -4f).x, ptFace(16f, -4f).y)
                cubicTo(
                    ptFace(16f, 2f).x, ptFace(16f, 2f).y,
                    ptFace(15.5f, 8f).x, ptFace(15.5f, 8f).y,
                    ptFace(15f, 14f).x, ptFace(15f, 14f).y
                )
            }
            drawPath(
                rightHeadLinePath,
                color = darkGray,
                style = Stroke(width = 1f * u, cap = StrokeCap.Round)
            )

            // ================= ВИЗОР =================
                       // Радиусы в u-единицах, без предварительного умножения на u.
            // Иначе при передаче в ptFace(...) происходит двойное умножение
            // на u, и при большом масштабе дуги arcTo строятся криво.
            val topRadius = 4f
            val bottomRadius = 13f

                       val visorPath = Path().apply {
                moveTo(ptFace(-34f + topRadius, 14f).x, ptFace(-34f + topRadius, 14f).y)
                lineTo(ptFace(34f - topRadius, 14f).x, ptFace(34f - topRadius, 14f).y)
                arcTo(
                    rect = Rect(
                        left = ptFace(34f - 2f * topRadius, 14f).x,
                        top = ptFace(34f - 2f * topRadius, 14f).y,
                        right = ptFace(34f, 14f).x,
                        bottom = ptFace(34f, 14f + 2f * topRadius).y
                    ),
                    startAngleDegrees = 270f,
                    sweepAngleDegrees = 90f,
                    forceMoveTo = false
                )
                lineTo(ptFace(34f, 42f - bottomRadius).x, ptFace(34f, 42f - bottomRadius).y)
                arcTo(
                    rect = Rect(
                        left = ptFace(34f - 2f * bottomRadius, 42f - 2f * bottomRadius).x,
                        top = ptFace(34f - 2f * bottomRadius, 42f - 2f * bottomRadius).y,
                        right = ptFace(34f, 42f).x,
                        bottom = ptFace(34f, 42f).y
                    ),
                    startAngleDegrees = 0f,
                    sweepAngleDegrees = 90f,
                    forceMoveTo = false
                )
                lineTo(ptFace(10f, 42f).x, ptFace(10f, 42f).y)
                cubicTo(
                    ptFace(6f, 40f).x, ptFace(6f, 40f).y,
                    ptFace(3f, 37f).x, ptFace(3f, 37f).y,
                    ptFace(0f, 37f).x, ptFace(0f, 37f).y
                )
                cubicTo(
                    ptFace(-3f, 37f).x, ptFace(-3f, 37f).y,
                    ptFace(-6f, 40f).x, ptFace(-6f, 40f).y,
                    ptFace(-10f, 42f).x, ptFace(-10f, 42f).y
                )
                lineTo(ptFace(-34f + bottomRadius, 42f).x, ptFace(-34f + bottomRadius, 42f).y)
                arcTo(
                    rect = Rect(
                        left = ptFace(-34f, 42f - 2f * bottomRadius).x,
                        top = ptFace(-34f, 42f - 2f * bottomRadius).y,
                        right = ptFace(-34f + 2f * bottomRadius, 42f).x,
                        bottom = ptFace(-34f + 2f * bottomRadius, 42f).y
                    ),
                    startAngleDegrees = 90f,
                    sweepAngleDegrees = 90f,
                    forceMoveTo = false
                )
                lineTo(ptFace(-34f, 14f + topRadius).x, ptFace(-34f, 14f + topRadius).y)
                arcTo(
                    rect = Rect(
                        left = ptFace(-34f, 14f).x,
                        top = ptFace(-34f, 14f).y,
                        right = ptFace(-34f + 2f * topRadius, 14f).x,
                        bottom = ptFace(-34f + 2f * topRadius, 14f + 2f * topRadius).y
                    ),
                    startAngleDegrees = 180f,
                    sweepAngleDegrees = 90f,
                    forceMoveTo = false
                )
                close()
            }

            drawPath(
                visorPath,
                brush = Brush.verticalGradient(
                    colors = listOf(visorGlass, visorDark),
                    startY = ptFace(0f, 14f).y,
                    endY = ptFace(0f, 42f).y
                )
            )

            drawPath(visorPath, color = darkerGray, style = Stroke(width = 1.5f * u))

            // ================= ГЛАЗА И БРОВИ =================
            val isBlinking = currentBlink < 0.5f
            val baseEyeW = 14f * u
            val baseEyeH = 7f * u

            val predatorPulse = if (isThinking) {
                1f + sin(pulse) * 0.2f
            } else {
                1f
            }

            val speakingExpand = if (isSpeaking) {
                1.15f + sin(mouthPhase * 2f) * 0.08f
            } else {
                1f
            }

                       val (eyeW, eyeH) = when {
                isBlinking -> baseEyeW to 1.5f * u
                isListening -> (baseEyeW * 1.25f) to (baseEyeH * 1.35f)
                isThinking -> (baseEyeW * 0.95f) to (baseEyeH * 0.6f)
                isSpeaking -> (baseEyeW * speakingExpand) to (baseEyeH * 1.1f)
                isIdle -> {
                    val p = idleEyePhase
                    when {
                        p < 0.4f -> baseEyeW to baseEyeH
                        p < 0.6f -> {
                            val t = (p - 0.4f) / 0.2f
                            (baseEyeW + 1.5f * u * t) to (baseEyeH + 1.5f * u * t)
                        }
                        p < 0.8f -> {
                            val t = (p - 0.6f) / 0.2f
                            (baseEyeW + 2f * u * t) to (baseEyeH - 3f * u * t)
                        }
                        else -> {
                            val t = (p - 0.8f) / 0.2f
                            ((baseEyeW + 2f * u) - 2f * u * t) to ((baseEyeH - 3f * u) + 3f * u * t)
                        }
                    }
                }
                else -> baseEyeW to baseEyeH
            }

                val irisRadius = when {
                isBlinking -> 1.5f * u
                isListening -> 5.2f * u
                isThinking -> 3f * u * predatorPulse
                isSpeaking -> 4.5f * u * speakingExpand
                isIdle -> {
                    val p = idleEyePhase
                    when {
                        p < 0.4f -> 4f * u
                        p < 0.6f -> 4f * u + 1.2f * u * ((p - 0.4f) / 0.2f)
                        p < 0.8f -> 5.2f * u - 2.5f * u * ((p - 0.6f) / 0.2f)
                        else -> 2.7f * u + 1.3f * u * ((p - 0.8f) / 0.2f)
                    }
                }
                else -> 4f * u
            }

            fun createPredatorEyePath(centerX: Float, centerY: Float, width: Float, height: Float, isLeft: Boolean): Path {
                val halfW = width / 2f
                val halfH = height / 2f
                val outerRatio = 0.3f

                val innerTopY = centerY - halfH
                val innerBottomY = centerY + halfH
                val outerTopY = centerY - halfH * outerRatio
                val outerBottomY = centerY + halfH * outerRatio

                val innerX = if (isLeft) centerX + halfW * 0.9f else centerX - halfW * 0.9f
                val outerX = if (isLeft) centerX - halfW else centerX + halfW
                val cornerRadius = 1f * u

                return Path().apply {
                    moveTo(innerX - cornerRadius, innerTopY)
                    lineTo(outerX + cornerRadius, outerTopY)
                    quadraticBezierTo(outerX, outerTopY, outerX, outerTopY + cornerRadius)
                    lineTo(outerX, outerBottomY - cornerRadius)
                    quadraticBezierTo(outerX, outerBottomY, outerX - cornerRadius, outerBottomY)
                    quadraticBezierTo(centerX, centerY + halfH * 1.2f, innerX + cornerRadius, innerBottomY)
                    quadraticBezierTo(innerX, innerBottomY, innerX, innerBottomY - cornerRadius)
                    lineTo(innerX, innerTopY + cornerRadius)
                    quadraticBezierTo(innerX, innerTopY, innerX - cornerRadius, innerTopY)
                    close()
                }
            }

            fun createLivingBrowPath(
                centerX: Float,
                centerY: Float,
                width: Float,
                height: Float,
                isLeft: Boolean,
                lift: Float,
                tilt: Float,
                stretch: Float
            ): Path {
                val halfW = width * stretch / 2f
                val browOffsetY = height * 0.9f

                val innerX = if (isLeft) centerX + halfW * 0.9f else centerX - halfW * 0.9f
                val outerX = if (isLeft) centerX - halfW * 1.1f else centerX + halfW * 1.1f
                val innerY = centerY - browOffsetY + lift
                val outerY = centerY - browOffsetY - height * 0.3f + lift

                val pivotX = innerX
                val pivotY = innerY
                val rad = tilt * PI.toFloat() / 180f
                val cosA = cos(rad)
                val sinA = sin(rad)

                fun rotatePoint(x: Float, y: Float): Offset {
                    val dx = x - pivotX
                    val dy = y - pivotY
                    return Offset(
                        pivotX + dx * cosA - dy * sinA,
                        pivotY + dx * sinA + dy * cosA
                    )
                }

                val innerRot = rotatePoint(innerX, innerY)
                val outerRot = rotatePoint(outerX, outerY)
                val ctrlRot = rotatePoint(
                    (innerX + outerX) / 2f,
                    innerY - height * 0.15f
                )

                return Path().apply {
                    moveTo(innerRot.x, innerRot.y)
                    quadraticBezierTo(ctrlRot.x, ctrlRot.y, outerRot.x, outerRot.y)
                }
            }

            fun createGearIrisPath(centerX: Float, centerY: Float, radius: Float, teethCount: Int = 8): Path {
                val path = Path()
                val outerRadius = radius
                val innerRadius = radius * 0.75f
                val angleStep = (2f * PI.toFloat()) / teethCount
                var firstPoint = true

                for (i in 0 until teethCount) {
                    val angle = i * angleStep - PI.toFloat() / 2f
                    val outerX = centerX + cos(angle) * outerRadius
                    val outerY = centerY + sin(angle) * outerRadius
                    val nextAngle = angle + angleStep / 2f
                    val innerX = centerX + cos(nextAngle) * innerRadius
                    val innerY = centerY + sin(nextAngle) * innerRadius

                    if (firstPoint) {
                        path.moveTo(outerX, outerY)
                        firstPoint = false
                    } else {
                        path.lineTo(outerX, outerY)
                    }
                    path.lineTo(innerX, innerY)
                }
                path.close()
                return path
            }

            val browPhase = if (isThinking) {
                -0.15f + sin(pulse * 2f) * 0.05f
            } else if (isSpeaking) {
                0.25f + sin(mouthPhase * 2f) * 0.08f
            } else if (isIdle) {
                val p = idleEyePhase
                when {
                    p < 0.4f -> 0f
                    p < 0.6f -> ((p - 0.4f) / 0.2f) * 0.15f
                    p < 0.8f -> 0.15f - ((p - 0.6f) / 0.2f) * 0.3f
                    else -> -0.15f + ((p - 0.8f) / 0.2f) * 0.15f
                }
            } else {
                0f
            }

            val browLift = when {
                isBlinking -> -0.5f * u
                isThinking -> -1.2f * u
                isSpeaking -> 1.5f * u
                isIdle -> browPhase * 3f * u
                else -> 0f
            }

            val browTilt = when {
                isThinking -> 12f
                isSpeaking -> -6f
                isIdle -> browPhase * 10f
                else -> 0f
            }

            val browStretch = when {
                isThinking -> 0.9f
                isSpeaking -> 1.1f
                isIdle -> 1f + browPhase * 0.15f
                else -> 1f
            }

            val leftEyeCenterX = -12f + lookOffsetX / u
            val leftEyeCenterY = 26f + lookOffsetY / u
            val leftCenter = ptFace(leftEyeCenterX, leftEyeCenterY)

            val leftEyePath = createPredatorEyePath(leftCenter.x, leftCenter.y, eyeW, eyeH, isLeft = true)

            drawPath(
                path = leftEyePath,
                brush = Brush.radialGradient(
                    colors = listOf(
                        neonBluePulse.copy(alpha = 0.5f),
                        neonBluePulse.copy(alpha = 0.15f),
                        Color.Transparent
                    ),
                    center = leftCenter,
                    radius = eyeW * 0.7f
                )
            )

            drawPath(leftEyePath, color = Color(0xFF0A0A14))

            drawPath(
                path = leftEyePath,
                brush = Brush.radialGradient(
                    colors = listOf(neonBluePulse, neonBluePulse.copy(alpha = 0.6f)),
                    center = Offset(leftCenter.x + eyeW * 0.1f, leftCenter.y + eyeH * 0.2f),
                    radius = eyeW * 0.5f
                )
            )

            val pupilOffsetX = (lookOffsetX / u) * 0.25f * u
            val pupilOffsetY = (lookOffsetY / u) * 0.25f * u
            val leftIrisCenter = Offset(leftCenter.x + pupilOffsetX, leftCenter.y + pupilOffsetY + eyeH * 0.15f)

            drawCircle(
                color = Color(0xFF050510),
                radius = irisRadius * 1.1f,
                center = leftIrisCenter
            )

            val leftGearPath = createGearIrisPath(leftIrisCenter.x, leftIrisCenter.y, irisRadius, teethCount = 8)
            drawPath(path = leftGearPath, color = Color(0xFF0A0A14))
            drawPath(
                path = leftGearPath,
                color = neonBluePulse.copy(alpha = 0.9f),
                style = Stroke(width = 0.7f * u)
            )

            drawCircle(
                color = Color(0xFF050510),
                radius = irisRadius * 0.35f,
                center = leftIrisCenter
            )

            drawOval(
                color = Color.White.copy(alpha = 0.95f),
                topLeft = Offset(leftIrisCenter.x - 0.4f * u, leftIrisCenter.y - irisRadius * 0.4f),
                size = Size(0.8f * u, irisRadius * 0.8f)
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.8f),
                radius = 0.6f * u,
                center = Offset(leftIrisCenter.x + irisRadius * 0.3f, leftIrisCenter.y - irisRadius * 0.3f)
            )

            val leftBrowPath = createLivingBrowPath(
                centerX = leftCenter.x,
                centerY = leftCenter.y,
                width = eyeW,
                height = eyeH,
                isLeft = true,
                lift = browLift,
                tilt = browTilt,
                stretch = browStretch
            )

            drawPath(
                path = leftBrowPath,
                color = neonBluePulse.copy(alpha = 0.35f),
                style = Stroke(width = 3.5f * u, cap = StrokeCap.Round)
            )

            drawPath(
                path = leftBrowPath,
                color = neonBluePulse.copy(alpha = 0.95f),
                style = Stroke(width = 2f * u, cap = StrokeCap.Round)
            )

            drawPath(
                path = leftBrowPath,
                color = Color.White.copy(alpha = 0.7f),
                style = Stroke(width = 0.7f * u, cap = StrokeCap.Round)
            )

            val rightEyeCenterX = 12f + lookOffsetX / u
            val rightEyeCenterY = 26f + lookOffsetY / u
            val rightCenter = ptFace(rightEyeCenterX, rightEyeCenterY)

            val rightEyePath = createPredatorEyePath(rightCenter.x, rightCenter.y, eyeW, eyeH, isLeft = false)

            drawPath(
                path = rightEyePath,
                brush = Brush.radialGradient(
                    colors = listOf(
                        neonBluePulse.copy(alpha = 0.5f),
                        neonBluePulse.copy(alpha = 0.15f),
                        Color.Transparent
                    ),
                    center = rightCenter,
                    radius = eyeW * 0.7f
                )
            )

            drawPath(rightEyePath, color = Color(0xFF0A0A14))

            drawPath(
                path = rightEyePath,
                brush = Brush.radialGradient(
                    colors = listOf(neonBluePulse, neonBluePulse.copy(alpha = 0.6f)),
                    center = Offset(rightCenter.x - eyeW * 0.1f, rightCenter.y + eyeH * 0.2f),
                    radius = eyeW * 0.5f
                )
            )

            val rightIrisCenter = Offset(rightCenter.x + pupilOffsetX, rightCenter.y + pupilOffsetY + eyeH * 0.15f)

            drawCircle(
                color = Color(0xFF050510),
                radius = irisRadius * 1.1f,
                center = rightIrisCenter
            )

            val rightGearPath = createGearIrisPath(rightIrisCenter.x, rightIrisCenter.y, irisRadius, teethCount = 8)
            drawPath(path = rightGearPath, color = Color(0xFF0A0A14))
            drawPath(
                path = rightGearPath,
                color = neonBluePulse.copy(alpha = 0.9f),
                style = Stroke(width = 0.7f * u)
            )

            drawCircle(
                color = Color(0xFF050510),
                radius = irisRadius * 0.35f,
                center = rightIrisCenter
            )

            drawOval(
                color = Color.White.copy(alpha = 0.95f),
                topLeft = Offset(rightIrisCenter.x - 0.4f * u, rightIrisCenter.y - irisRadius * 0.4f),
                size = Size(0.8f * u, irisRadius * 0.8f)
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.8f),
                radius = 0.6f * u,
                center = Offset(rightIrisCenter.x + irisRadius * 0.3f, rightIrisCenter.y - irisRadius * 0.3f)
            )

            val rightBrowPath = createLivingBrowPath(
                centerX = rightCenter.x,
                centerY = rightCenter.y,
                width = eyeW,
                height = eyeH,
                isLeft = false,
                lift = browLift,
                tilt = browTilt,
                stretch = browStretch
            )

            drawPath(
                path = rightBrowPath,
                color = neonBluePulse.copy(alpha = 0.35f),
                style = Stroke(width = 3.5f * u, cap = StrokeCap.Round)
            )

            drawPath(
                path = rightBrowPath,
                color = neonBluePulse.copy(alpha = 0.95f),
                style = Stroke(width = 2f * u, cap = StrokeCap.Round)
            )

            drawPath(
                path = rightBrowPath,
                color = Color.White.copy(alpha = 0.7f),
                style = Stroke(width = 0.7f * u, cap = StrokeCap.Round)
            )

            // ================= РОТ =================
            val mouthY = 52f
            val mouthBaseWidth = 24f * u
            val mouthBaseHeight = 10f * u

            val mouthOpenHeight = if (isSpeaking) {
                (5f + 3.5f * sin(mouthPhase * 2f)) * u
            } else {
                0f
            }

            val thinkingTwitch = if (isThinking) {
                sin(pulse * 3f) * 0.5f
            } else {
                0f
            }

                       if (isSpeaking) {
                val widthOscillate = 1f + 0.25f * sin(mouthPhase * 1.7f)
                val heightOscillate = 1f + 0.35f * sin(mouthPhase * 2.3f)

                val currentWidth = mouthBaseWidth * widthOscillate
                val currentHeight = mouthOpenHeight * heightOscillate
                val halfW = currentWidth / 2f
                val halfH = currentHeight / 2f

                val outerMouthPath = Path().apply {
                    val cx = ptFace(0f, mouthY).x
                    val cy = ptFace(0f, mouthY).y
                    moveTo(cx - halfW, cy)
                    cubicTo(
                        cx - halfW * 0.6f, cy - halfH,
                        cx + halfW * 0.6f, cy - halfH,
                        cx + halfW, cy
                    )
                    cubicTo(
                        cx + halfW * 0.6f, cy + halfH,
                        cx - halfW * 0.6f, cy + halfH,
                        cx - halfW, cy
                    )
                    close()
                }

                drawPath(outerMouthPath, color = darkerGray)

                val innerMouthPath = Path().apply {
                    val cx = ptFace(0f, mouthY).x
                    val cy = ptFace(0f, mouthY).y
                    val iw = halfW * 0.78f
                    val ih = halfH * 0.78f
                    moveTo(cx - iw, cy)
                    cubicTo(
                        cx - iw * 0.6f, cy - ih,
                        cx + iw * 0.6f, cy - ih,
                        cx + iw, cy
                    )
                    cubicTo(
                        cx + iw * 0.6f, cy + ih,
                        cx - iw * 0.6f, cy + ih,
                        cx - iw, cy
                    )
                    close()
                }
                drawPath(innerMouthPath, color = Color(0xFF050510))

                val glowPulse = 0.6f + 0.4f * sin(mouthPhase * 3f)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            neonBlue.copy(alpha = 0.55f * glowPulse),
                            neonBlue.copy(alpha = 0.25f * glowPulse),
                            Color.Transparent
                        ),
                        center = ptFace(0f, mouthY),
                        radius = currentWidth * 1.1f
                    ),
                    radius = currentWidth * 1.1f,
                    center = ptFace(0f, mouthY)
                )

                if (currentHeight > 3f * u) {
                    drawPath(
                        path = innerMouthPath,
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.7f * glowPulse),
                                neonBlue.copy(alpha = 0.5f * glowPulse),
                                Color.Transparent
                            ),
                            center = ptFace(0f, mouthY),
                            radius = currentWidth * 0.6f
                        )
                    )
                }

                val barCount = 5
                val barSpacing = currentWidth / (barCount + 1)
                for (i in 0 until barCount) {
                    val barX = -currentWidth / 2f + barSpacing * (i + 1)
                    val barPhase = mouthPhase * 2f + i * 1.1f
                    val barHeightFactor = 0.3f + 0.7f * abs(sin(barPhase))
                    val barH = currentHeight * 0.7f * barHeightFactor
                    val barW = 1.2f * u

                    val barColor = if (barHeightFactor > 0.7f) {
                        Color.White.copy(alpha = 0.95f)
                    } else {
                        neonBlue.copy(alpha = 0.85f)
                    }

                    drawRoundRect(
                        color = barColor,
                        topLeft = ptFace(barX - barW / 2f, mouthY - barH / 2f / u),
                        size = Size(barW, barH),
                        cornerRadius = CornerRadius(barW / 2f)
                    )

                    if (barHeightFactor > 0.8f) {
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    neonBlue.copy(alpha = 0.5f),
                                    Color.Transparent
                                ),
                                center = ptFace(barX, mouthY),
                                radius = barW * 3f
                            ),
                            radius = barW * 3f,
                            center = ptFace(barX, mouthY)
                        )
                    }
                }

                       } else if (isThinking) {
                val twitchPath = Path().apply {
                    moveTo(ptFace(-10f, mouthY + thinkingTwitch).x, ptFace(-10f, mouthY + thinkingTwitch).y)
                    lineTo(ptFace(10f, mouthY + thinkingTwitch).x, ptFace(10f, mouthY + thinkingTwitch).y)
                }

                drawPath(
                    twitchPath,
                    color = mediumGray,
                    style = Stroke(width = 1.5f * u, cap = StrokeCap.Round)
                )

                drawPath(
                    twitchPath,
                    color = neonBluePulse.copy(alpha = 0.4f),
                    style = Stroke(width = 0.8f * u, cap = StrokeCap.Round)
                )

            } else {
                val smilePath = Path().apply {
                    moveTo(ptFace(-10f, mouthY - 2f).x, ptFace(-10f, mouthY - 2f).y)
                    cubicTo(
                        ptFace(-5f, mouthY + 3f).x, ptFace(-5f, mouthY + 3f).y,
                        ptFace(5f, mouthY + 3f).x, ptFace(5f, mouthY + 3f).y,
                        ptFace(10f, mouthY - 2f).x, ptFace(10f, mouthY - 2f).y
                    )
                }

                drawPath(
                    smilePath,
                    color = darkerGray,
                    style = Stroke(width = 2f * u, cap = StrokeCap.Round)
                )

                if (isActive) {
                    drawPath(
                        smilePath,
                        color = neonBluePulse.copy(alpha = 0.2f),
                        style = Stroke(width = 0.8f * u, cap = StrokeCap.Round)
                    )
                }
            }

     if (!isSpeaking) {
                drawLine(
                    color = mediumGray.copy(alpha = 0.4f),
                    start = ptFace(-15f, mouthY + 8f),
                    end = ptFace(15f, mouthY + 8f),
                    strokeWidth = 0.5f * u
                )
            }
                       } // закрытие clipPath(headPath)
             } // закрытие rotate(effectiveHeadTilt)
        } // закрытие rotate(bodySwayAngle) — покачивание корпуса
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
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            colors.gradTop,
                            colors.gradMid,
                            colors.gradBottom
                        )
                    ),
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
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                colors.gradTop,
                                colors.gradMid,
                                colors.gradBottom
                            )
                        ),
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
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            colors.gradTop,
                            colors.gradMid,
                            colors.gradBottom
                        )
                    ),
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
                        val maxTokensSteps = listOf(
                256, 512, 1024, 2048, 4096, 8192, 16384, 32768
            )
            val maxTokensIndex = maxTokensSteps.indexOfFirst { it >= maxTokens }
                .let { if (it == -1) maxTokensSteps.lastIndex else it }

            Text(
                text = "Максимум токенов ответа: $maxTokens" +
                        if (maxTokens >= 8192) " (много — для думающих моделей)" else "",
                color = colors.text
            )
            Slider(
                value = maxTokensIndex.toFloat(),
                onValueChange = { idx ->
                    val i = idx.toInt().coerceIn(0, maxTokensSteps.lastIndex)
                    onMaxTokensChange(maxTokensSteps[i])
                },
                valueRange = 0f..(maxTokensSteps.size - 1).toFloat(),
                steps = maxTokensSteps.size - 2,
                modifier = Modifier.fillMaxWidth(),
                colors = SliderDefaults.colors(
                    thumbColor = colors.accent,
                    activeTrackColor = colors.accent,
                    inactiveTrackColor = colors.borderGray
                )
            )
            Text(
                text = "Для ответов с размышлениями (reasoning) ставьте 8192+. " +
                        "Большие значения замедляют генерацию.",
                color = colors.text.copy(alpha = 0.6f),
                fontSize = 10.sp,
                modifier = Modifier.padding(top = 2.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
                        // Цвет зависит от размера контекста:
            // зелёный — безопасно (≤ 8192), оранжевый — на грани (≤ 16384), красный — рискованно (> 16384)
                       val contextSteps = listOf(
                512, 1024, 2048, 4096, 8192, 16384, 32768, 65536, 131072, 262144
            )
            val contextIndex = contextSteps.indexOfFirst { it >= contextSize }
                .let { if (it == -1) contextSteps.lastIndex else it }

            val contextColor = when {
                contextSize <= 8192 -> colors.green
                contextSize <= 32768 -> Color(0xFFFFA500)
                else -> Color.Red
            }

            Text(
                text = "Размер контекстного окна: $contextSize" +
                        when {
                            contextSize <= 8192 -> " (безопасно)"
                            contextSize <= 32768 -> " (на грани)"
                            else -> " (высокий риск OOM)"
                        },
                color = contextColor
            )
            Slider(
                value = contextIndex.toFloat(),
                onValueChange = { idx ->
                    val i = idx.toInt().coerceIn(0, contextSteps.lastIndex)
                    onContextSizeChange(contextSteps[i])
                },
                valueRange = 0f..(contextSteps.size - 1).toFloat(),
                steps = contextSteps.size - 2,
                modifier = Modifier.fillMaxWidth(),
                colors = SliderDefaults.colors(
                    thumbColor = contextColor,
                    activeTrackColor = contextColor,
                    inactiveTrackColor = colors.borderGray
                )
            )
                       Text(
                text = "Реальный контекст ограничен моделью: если у модели 8192, " +
                        "ставить 65536 бесполезно — лишнее просто не загрузится. " +
                        "Для 6 ГБ RAM безопасно до 8192. Больше — только на 8+ ГБ.",
                color = colors.text.copy(alpha = 0.6f),
                fontSize = 10.sp,
                modifier = Modifier.padding(top = 2.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Button(
                onClick = { onContextSizeChange(8192) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.borderGray,
                    contentColor = colors.text
                ),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, colors.borderGray),
                modifier = Modifier.fillMaxWidth(0.7f).height(34.dp),
                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
            ) {
                Text("🧮 Сбросить на безопасные 8192", color = colors.text, fontSize = 11.sp)
            }
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
    onDismiss: () -> Unit,
    colors: AppColors,
    context: android.content.Context
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

Column(
    modifier = Modifier.fillMaxWidth(),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(6.dp)
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(
            onClick = onSave,
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.accent,
                contentColor = colors.background
            ),
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.dp, colors.borderGray),
            modifier = Modifier.weight(1f).height(38.dp),
            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
        ) {
            Text("Сохранить", color = colors.background, fontSize = 12.sp)
        }

        Button(
            onClick = onDismiss,
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.accent,
                contentColor = colors.background
            ),
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.dp, colors.borderGray),
            modifier = Modifier.weight(1f).height(38.dp),
            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
        ) {
            Text("Закрыть", color = colors.background, fontSize = 12.sp)
        }
    }

    Button(
        onClick = {
            try {
                val intent = Intent("com.android.settings.TTS_SETTINGS")
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            } catch (e: Exception) {
                android.util.Log.e("PromptSettingsPanel", "Не удалось открыть настройки TTS: ${e.message}")
                try {
                    val fallbackIntent = Intent(android.speech.tts.TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA)
                    fallbackIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(fallbackIntent)
                } catch (e2: Exception) {
                    android.util.Log.e("PromptSettingsPanel", "Fallback тоже не сработал: ${e2.message}")
                }
            }
        },
        colors = ButtonDefaults.buttonColors(
            containerColor = colors.borderGray,
            contentColor = colors.text
        ),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, colors.borderGray),
        modifier = Modifier.fillMaxWidth(0.7f).height(36.dp),
        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
    ) {
        Text("🔊 Настроить TTS", color = colors.text, fontSize = 11.sp)
    }
}
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
    onRestoreBase: () -> Unit,
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
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = { onSave(text); onDismiss() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.accent,
                            contentColor = colors.background
                        ),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, colors.borderGray),
                        modifier = Modifier.weight(1f).height(38.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                    ) {
                        Text("Сохранить", color = colors.background, fontSize = 12.sp)
                    }

                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.accent,
                            contentColor = colors.background
                        ),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, colors.borderGray),
                        modifier = Modifier.weight(1f).height(38.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                    ) {
                        Text("Закрыть", color = colors.background, fontSize = 12.sp)
                    }
                }

                Button(
                    onClick = { onRestoreBase(); onDismiss() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.borderGray,
                        contentColor = colors.text
                    ),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, colors.borderGray),
                    modifier = Modifier.fillMaxWidth(0.6f).height(36.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                ) {
                    Text("📚 Восстановить базовые", color = colors.text, fontSize = 11.sp)
                }
            }
        },
        dismissButton = null
    )
}

@Composable
private fun BrainEditorDialog(
    initialText: String,
    onSave: (String) -> Unit,
    onRestoreBase: () -> Unit,
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
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = { onSave(text); onDismiss() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colors.accent,
                                contentColor = colors.background
                            ),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, colors.borderGray),
                            modifier = Modifier.weight(1f).height(38.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                        ) {
                            Text("Сохранить", color = colors.background, fontSize = 12.sp)
                        }

                        Button(
                            onClick = onDismiss,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colors.accent,
                                contentColor = colors.background
                            ),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, colors.borderGray),
                            modifier = Modifier.weight(1f).height(38.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                        ) {
                            Text("Закрыть", color = colors.background, fontSize = 12.sp)
                        }
                    }

                    Button(
                        onClick = { onRestoreBase(); onDismiss() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.borderGray,
                            contentColor = colors.text
                        ),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, colors.borderGray),
                        modifier = Modifier.fillMaxWidth(0.6f).height(36.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                    ) {
                        Text("📚 Восстановить базовые", color = colors.text, fontSize = 11.sp)
                    }
                }
            },
            dismissButton = null
        )
    }
}
@Composable
private fun ChatEditorDialog(
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
            title = { Text("💬 Редактор чата", style = MaterialTheme.typography.titleLarge, color = colors.text) },
            text = {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    placeholder = { Text("История чата появится здесь...", color = colors.text.copy(alpha = 0.5f), fontSize = 10.sp) },
                    modifier = Modifier.fillMaxWidth().height(400.dp),
                    maxLines = 1000,
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
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = { onSave(text); onDismiss() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colors.accent,
                                contentColor = colors.background
                            ),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, colors.borderGray),
                            modifier = Modifier.weight(1f).height(38.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                        ) {
                            Text("Сохранить", color = colors.background, fontSize = 12.sp)
                        }

                        Button(
                            onClick = onDismiss,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colors.accent,
                                contentColor = colors.background
                            ),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, colors.borderGray),
                            modifier = Modifier.weight(1f).height(38.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                        ) {
                            Text("Закрыть", color = colors.background, fontSize = 12.sp)
                        }
                    }
                }
            },
            dismissButton = null
        )
    }
}
@Composable
private fun OrganizerEditorDialog(
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
            title = { Text("📅 Органайзер", style = MaterialTheme.typography.titleLarge, color = colors.text) },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Формат: ГГГГ-ММ-ДД ЧЧ:ММ | текст",
                        color = colors.text.copy(alpha = 0.6f),
                        fontSize = 10.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = text,
                        onValueChange = { text = it },
                        placeholder = { Text("2026-10-22 18:30 | Позвонить в гараж", color = colors.text.copy(alpha = 0.5f), fontSize = 10.sp) },
                        modifier = Modifier.fillMaxWidth().height(400.dp),
                        maxLines = 1000,
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
                }
            },
            confirmButton = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { onSave(text); onDismiss() },
                        colors = ButtonDefaults.buttonColors(containerColor = colors.accent, contentColor = colors.background),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f).height(38.dp)
                    ) { Text("Сохранить", color = colors.background, fontSize = 12.sp) }
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = colors.accent, contentColor = colors.background),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f).height(38.dp)
                    ) { Text("Закрыть", color = colors.background, fontSize = 12.sp) }
                }
            },
            dismissButton = null
        )
    }
}

@Composable
private fun BibliotekaDialog(
    viewModel: MainViewModel,
    onDismiss: () -> Unit,
    colors: AppColors,
    isDarkTheme: Boolean
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var books by remember { mutableStateOf<List<File>>(emptyList()) }
    var searchQuery by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }
    var selectedBook by remember { mutableStateOf<File?>(null) }
    var selectedPdfFile by remember { mutableStateOf<File?>(null) }
    var bookSentences by remember { mutableStateOf<List<String>>(emptyList()) }
    var currentSentenceIndex by remember { mutableStateOf(-1) }
    var bookChunks by remember { mutableStateOf<List<String>>(emptyList()) }
    var currentChunkIndex by remember { mutableStateOf(-1) }
    var prefetchedFiles by remember { mutableStateOf<Map<Int, File>>(emptyMap()) }
    var prefetchingIndices by remember { mutableStateOf<Set<Int>>(emptySet()) }
    var mediaPlayer by remember { mutableStateOf<android.media.MediaPlayer?>(null) }
    var isReading by remember { mutableStateOf(false) }
    var isPaused by remember { mutableStateOf(false) }
    var fontSizeSp by remember { mutableStateOf(12) }
    var inBookQuery by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val isSpeaking by viewModel.isSpeaking.collectAsStateWithLifecycle(initialValue = false)

    LaunchedEffect(Unit) {
        books = viewModel.listBibliotekaBooks()
    }
        LaunchedEffect(Unit) {
        viewModel.indexBooksIfNeeded()
    }

    LaunchedEffect(searchQuery) {
        if (searchQuery.isBlank()) {
            searchResults = emptyList()
            isSearching = false
        } else {
            isSearching = true
            searchResults = viewModel.searchBooks(searchQuery)
            isSearching = false
        }
    }

    LaunchedEffect(selectedBook) {
        selectedBook?.let { file ->
            val text = viewModel.readBookText(file)
            bookSentences = text.split(Regex("(?<=[.!?])\\s+|\\n{2,}"))
                .map { it.trim() }
                .filter { it.isNotEmpty() }
            bookChunks = buildBookChunks(bookSentences, BOOK_CHUNK_TARGET_CHARS)
            currentChunkIndex = -1
        }
    }

    LaunchedEffect(currentChunkIndex, isReading) {
        if (isReading && currentChunkIndex >= 0) {
            val firstSentence = firstSentenceOfChunk(currentChunkIndex, bookChunks, bookSentences)
            if (firstSentence in bookSentences.indices) {
                listState.animateScrollToItem(firstSentence)
            }
        }
    }

    LaunchedEffect(isReading, isPaused, currentChunkIndex) {
        if (!isReading || isPaused) return@LaunchedEffect
        if (currentChunkIndex < 0 || currentChunkIndex >= bookChunks.size) return@LaunchedEffect

        // Синтезировать текущий + 2 следующих, если ещё нет в буфере
        val toSynthesize = listOf(currentChunkIndex, currentChunkIndex + 1, currentChunkIndex + 2)
            .filter { it in bookChunks.indices }
            .filter { prefetchedFiles[it] == null && it !in prefetchingIndices }

        for (idx in toSynthesize) {
            prefetchingIndices = prefetchingIndices + idx
            val dir = File(context.cacheDir, "book_tts").apply { mkdirs() }
            val file = File(dir, "book_chunk_${idx}_${System.currentTimeMillis()}.wav")
            val intent = Intent(context, TtsService::class.java).apply {
                action = TtsService.ACTION_SYNTHESIZE_TO_FILE
                putExtra(TtsService.EXTRA_TEXT, bookChunks[idx])
                putExtra(TtsService.EXTRA_FILE_PATH, file.absolutePath)
            }
            context.startService(intent)
        }

        // Играть текущий, если готов и плеер свободен
        val currentFile = prefetchedFiles[currentChunkIndex]
        if (currentFile != null && mediaPlayer == null) {
            mediaPlayer = android.media.MediaPlayer().apply {
                setDataSource(currentFile.absolutePath)
                setOnCompletionListener {
                    mediaPlayer?.release()
                    mediaPlayer = null
                    prefetchedFiles = prefetchedFiles - currentChunkIndex
                    currentFile.delete()
                    if (currentChunkIndex + 1 < bookChunks.size) {
                        currentChunkIndex = currentChunkIndex + 1
                        currentSentenceIndex = firstSentenceOfChunk(currentChunkIndex, bookChunks, bookSentences)
                    } else {
                        isReading = false
                        isPaused = false
                        currentChunkIndex = -1
                        currentSentenceIndex = -1
                    }
                }
                prepare()
                start()
            }
        }
    }

    DisposableEffect(Unit) {
        val receiver = object : android.content.BroadcastReceiver() {
            override fun onReceive(ctx: android.content.Context?, intent: android.content.Intent?) {
                if (intent?.action == TtsService.ACTION_SYNTHESIZE_DONE) {
                    val path = intent.getStringExtra(TtsService.EXTRA_FILE_PATH) ?: ""
                    if (path.isNotEmpty()) {
                        val idx = chunkIndexForFile(path)
                        if (idx >= 0) {
                            prefetchedFiles = prefetchedFiles + (idx to File(path))
                            prefetchingIndices = prefetchingIndices - idx
                        }
                    }
                }
            }
        }
        androidx.core.content.ContextCompat.registerReceiver(
            context, receiver,
            android.content.IntentFilter(TtsService.ACTION_SYNTHESIZE_DONE),
            androidx.core.content.ContextCompat.RECEIVER_NOT_EXPORTED
        )
        onDispose {
            try { context.unregisterReceiver(receiver) } catch (_: Exception) {}
            mediaPlayer?.release()
            mediaPlayer = null
            prefetchedFiles.values.forEach { it.delete() }
            prefetchedFiles = emptyMap()
            prefetchingIndices = emptySet()
            File(context.cacheDir, "book_tts").deleteRecursively()
            if (isReading) viewModel.abortLocal()
        }
    }

    MaterialTheme(
        colorScheme = if (isDarkTheme) darkColorScheme() else lightColorScheme()
    ) {
        AlertDialog(
            onDismissRequest = {
                isReading = false
                onDismiss()
            },
                        title = {
                Text(
                    if (selectedBook == null) "📖 Библиотека" else "📖 ${selectedBook!!.nameWithoutExtension}",
                    style = MaterialTheme.typography.titleSmall,
                    color = colors.text,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
            },
                        text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                        if (selectedBook == null) {
                        if (searchQuery.isNotBlank()) {
                            if (isSearching) {
                                CircularProgressIndicator(color = colors.accent, modifier = Modifier.size(24.dp))
                            } else if (searchResults.isEmpty()) {
                                Text("Ничего не найдено", color = colors.text.copy(alpha = 0.6f), fontSize = 12.sp)
                            } else {
                LazyColumn(modifier = Modifier.fillMaxWidth().height(400.dp)) {
                                    items(searchResults) { (title, snippet) ->
                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp)
                                                .clickable {
                                                    val file = books.find { it.nameWithoutExtension == title }
                                                    if (file != null) {
                                                        selectedBook = file
                                                        selectedPdfFile = if (file.name.endsWith(".pdf", true)) file else null
                                                    }
                                                },
                                            colors = CardDefaults.cardColors(containerColor = colors.surfaceGray),
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Column(modifier = Modifier.padding(10.dp)) {
                                                Text(title, color = colors.accent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                Text(snippet, color = colors.text, fontSize = 10.sp, maxLines = 3)
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            LazyColumn(modifier = Modifier.fillMaxWidth().height(400.dp)) {
                                items(books) { file ->
                                        Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp)
                                            .clickable { selectedBook = file; selectedPdfFile = if (file.name.endsWith(".pdf", true)) file else null },
                                        colors = CardDefaults.cardColors(containerColor = colors.surfaceGray),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text(
                                            text = file.nameWithoutExtension,
                                            color = colors.text,
                                            fontSize = 13.sp,
                                            modifier = Modifier.padding(12.dp)
                                        )
                                    }
                                }
                            }
                        }
                    } else if (selectedBook?.name?.endsWith(".pdf", ignoreCase = true) == true) {
                        // PDF-просмотр через собственный рендер (PdfRenderer + LazyColumn).
                        // Кнопки A−/A+ — сверху, вместо поля поиска (поиск по PDF не работает).
                        // Ручной вертикальный скролл — как в обычной читалке.
                        val density = LocalDensity.current
                        var pdfScale by remember(selectedBook) { mutableStateOf(1.0f) }
                        val pdfScaleRange = 0.6f..2.0f

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = {
                                    pdfScale = (pdfScale - 0.1f)
                                        .coerceIn(pdfScaleRange.start, pdfScaleRange.endInclusive)
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = colors.accent,
                                    contentColor = colors.background
                                ),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, colors.borderGray),
                                modifier = Modifier.weight(1f).height(38.dp),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                            ) {
                                Text("A−", color = colors.background, fontSize = 14.sp)
                            }
                            Text(
                                text = "${(pdfScale * 100).toInt()}%",
                                color = colors.text,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.width(56.dp)
                            )
                            Button(
                                onClick = {
                                    pdfScale = (pdfScale + 0.1f)
                                        .coerceIn(pdfScaleRange.start, pdfScaleRange.endInclusive)
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = colors.accent,
                                    contentColor = colors.background
                                ),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, colors.borderGray),
                                modifier = Modifier.weight(1f).height(38.dp),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                            ) {
                                Text("A+", color = colors.background, fontSize = 14.sp)
                            }
                        }

                        PdfReaderView(
                            file = selectedBook!!,
                            pageWidthFraction = pdfScale,
                            colors = colors,
                            modifier = Modifier.fillMaxWidth().height(500.dp)
                        )
                    } else {
                                                                      Row(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Button(
                                onClick = {
                                    if (isReading && !isPaused) {
                                        isPaused = true
                                        mediaPlayer?.pause()
                                    } else if (isReading && isPaused) {
                                        isPaused = false
                                        mediaPlayer?.start()
                                    } else {
                                        isReading = true
                                        isPaused = false
                                        val startChunk = findChunkForSentence(
                                            currentSentenceIndex, bookChunks, bookSentences
                                        ).takeIf { it >= 0 } ?: 0
                                        currentChunkIndex = startChunk
                                        currentSentenceIndex = firstSentenceOfChunk(
                                            startChunk, bookChunks, bookSentences
                                        )
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = colors.accent,
                                    contentColor = colors.background
                                ),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, colors.borderGray),
                                modifier = Modifier.weight(2f).height(38.dp),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                            ) {
                                Text(
                                    when {
                                        isReading && !isPaused -> "⏸ Пауза"
                                        isReading && isPaused -> "▶ Продолжить"
                                        else -> "▶ Слушать"
                                    },
                                    color = colors.background,
                                    fontSize = 11.sp
                                )
                            }
                            Button(
                                onClick = { if (fontSizeSp > 10) fontSizeSp -= 1 },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = colors.accent,
                                    contentColor = colors.background
                                ),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, colors.borderGray),
                                modifier = Modifier.weight(1f).height(38.dp),
                                contentPadding = PaddingValues(horizontal = 2.dp, vertical = 0.dp)
                            ) {
                                Text("A−", color = colors.background, fontSize = 14.sp)
                            }
                            Button(
                                onClick = { if (fontSizeSp < 20) fontSizeSp += 1 },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = colors.accent,
                                    contentColor = colors.background
                                ),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, colors.borderGray),
                                modifier = Modifier.weight(1f).height(38.dp),
                                contentPadding = PaddingValues(horizontal = 2.dp, vertical = 0.dp)
                            ) {
                                Text("A+", color = colors.background, fontSize = 14.sp)
                            }
                        }

                                                OutlinedTextField(
                            value = inBookQuery,
                            onValueChange = { query ->
                                inBookQuery = query
                                if (query.isNotBlank()) {
                                    val idx = bookSentences.indexOfFirst { it.contains(query, ignoreCase = true) }
                                    if (idx >= 0) {
                                        currentSentenceIndex = idx
                                        coroutineScope.launch { listState.animateScrollToItem(idx) }
                                    }
                                }
                            },
                            placeholder = { Text("Найти в книге...", color = colors.text.copy(alpha = 0.5f), fontSize = 11.sp) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
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
                         LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxWidth().height(400.dp)
                        ) {
                itemsIndexed(bookSentences) { index, sentence ->
                                val isCurrent = index == currentSentenceIndex ||
                                    (currentChunkIndex >= 0 &&
                                     currentChunkIndex < bookChunks.size &&
                                     isSentenceInChunk(index, currentChunkIndex, bookChunks, bookSentences))
                                Text(
                                    text = sentence,
                                    color = if (isCurrent) colors.background else colors.text,
                                    fontSize = fontSizeSp.sp,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(
                                            if (isCurrent) colors.accent.copy(alpha = 0.6f) else Color.Transparent,
                                            RoundedCornerShape(4.dp)
                                        )
                                        .clickable {
                                            currentSentenceIndex = index
                                            val chunkIdx = findChunkForSentence(index, bookChunks, bookSentences)
                                            currentChunkIndex = chunkIdx
                                            if (isReading) {
                                                isPaused = false
                                                bookChunks.getOrNull(chunkIdx)?.let { chunk ->
                                                    viewModel.speakText(chunk)
                                                }
                                            }
                                        }
                                        .padding(6.dp)
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Button(
                                onClick = {
                                    val activity = context.findActivity()
                                    activity?.forceStartFloatingAndMinimize()
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = colors.accent,
                                    contentColor = colors.background
                                ),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, colors.borderGray),
                                modifier = Modifier.weight(1f).height(38.dp),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                            ) {
                                Text("⬇ Свернуть", color = colors.background, fontSize = 11.sp)
                            }
                            Button(
                                 onClick = {
                                    isReading = false
                                    isPaused = false
                                    currentSentenceIndex = -1
                                    currentChunkIndex = -1
                                    mediaPlayer?.release()
                                    mediaPlayer = null
                                    prefetchedFiles.values.forEach { it.delete() }
                                    prefetchedFiles = emptyMap()
                                    prefetchingIndices = emptySet()
                                    File(context.cacheDir, "book_tts").deleteRecursively()
                                    selectedBook = null
                                    bookSentences = emptyList()
                                    bookChunks = emptyList()
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = colors.accent,
                                    contentColor = colors.background
                                ),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, colors.borderGray),
                                modifier = Modifier.weight(1f).height(38.dp),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                            ) {
                                Text("← Назад", color = colors.background, fontSize = 11.sp)
                            }
                            Button(
                                onClick = {
                                    isReading = false
                                    currentChunkIndex = -1
                                    mediaPlayer?.release()
                                    mediaPlayer = null
                                    prefetchedFiles.values.forEach { it.delete() }
                                    prefetchedFiles = emptyMap()
                                    prefetchingIndices = emptySet()
                                    File(context.cacheDir, "book_tts").deleteRecursively()
                                    onDismiss()
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = colors.accent,
                                    contentColor = colors.background
                                ),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, colors.borderGray),
                                modifier = Modifier.weight(1f).height(38.dp),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                            ) {
                                Text("✕ Закрыть", color = colors.background, fontSize = 11.sp)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                if (selectedBook != null && selectedBook?.name?.endsWith(".txt", ignoreCase = true) == true) {
                    // Для txt-книги кнопки уже внутри Row — нижнюю не показываем
                } else Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (selectedBook != null) {
                        Button(
                            onClick = {
                                isReading = false
                                currentSentenceIndex = -1
                                selectedBook = null
                                selectedPdfFile = null
                                bookSentences = emptyList()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colors.accent,
                                contentColor = colors.background
                            ),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, colors.borderGray),
                            modifier = Modifier.weight(1f).height(38.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                        ) {
                            Text("← Назад", color = colors.background, fontSize = 12.sp)
                        }
                    }
                    Button(
                        onClick = {
                            isReading = false
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.accent,
                            contentColor = colors.background
                        ),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, colors.borderGray),
                        modifier = Modifier.weight(1f).height(38.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                    ) {
                        Text("Закрыть", color = colors.background, fontSize = 12.sp)
                    }
                }
            },
                       dismissButton = null
        )
    }
}

// ========== PDF-ЧИТАЛКА (свой рендер через PdfRenderer + LazyColumn) ==========
// Заменяет VerticalPDFReader из bouquet в PDF-ветке библиотеки.
// Даёт ручной вертикальный скролл как в обычной читалке и кнопки A−/A+.
// Страницы рендерятся ЛЕНИВО — только видимые в LazyColumn.
// Кэш — LRU на PDF_CACHE_SIZE битмапов, чтобы листание туда-обратно
// не рендерило заново, но и не держало всю книгу в памяти.
// PdfRenderer не потокобезопасен — рендер строго под Mutex в Dispatchers.IO.

private const val PDF_CACHE_SIZE = 5
private const val PDF_MAX_RENDER_WIDTH_PX = 2048

@Composable
private fun PdfReaderView(
    file: File,
    pageWidthFraction: Float,
    colors: AppColors,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val context = LocalContext.current

    var openError by remember(file) { mutableStateOf(false) }
    val pdfiumCore = remember(file) { com.shockwave.pdfium.PdfiumCore(context) }
    val document by produceState<com.shockwave.pdfium.PdfDocument?>(initialValue = null, file) {
        value = withContext(Dispatchers.IO) {
            try {
                val pfd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
                pdfiumCore.newDocument(pfd)
            } catch (e: Exception) {
                android.util.Log.e("PdfReaderView", "open failed: ${e.message}", e)
                openError = true
                null
            }
        }
    }

        val cache = remember(file) {
        android.util.LruCache<Int, Bitmap>(PDF_CACHE_SIZE)
    }

    val renderMutex = remember(file) { Mutex() }

    LaunchedEffect(pageWidthFraction) {
        cache.evictAll()
    }

    DisposableEffect(document) {
        onDispose {
            cache.evictAll()
            try {
                document?.let { pdfiumCore.closeDocument(it) }
            } catch (_: Exception) {}
        }
    }

    val currentDocument = document
    if (currentDocument == null) {
        Box(
            modifier = modifier
                .background(colors.surfaceGray, RoundedCornerShape(12.dp))
                .border(1.dp, colors.borderGray, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (openError) {
                Text(
                    text = "❌ Не удалось открыть PDF.\nВозможно, он защищён паролем или повреждён.",
                    color = colors.text,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(16.dp)
                )
            } else {
                CircularProgressIndicator(color = colors.accent)
            }
        }
        return
    }

    val pageCount = pdfiumCore.getPageCount(currentDocument)

    BoxWithConstraints(modifier = modifier) {
        // Реальная ширина колонки (без полей AlertDialog), а не ширина экрана.
        // Именно её умножаем на pageWidthFraction — тогда 100% = точная
        // ширина колонки, без лишнего рендера и мыла при сжатии.
        val columnWidthPx = constraints.maxWidth.toFloat()

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.background.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                .border(1.dp, colors.borderGray, RoundedCornerShape(12.dp))
                .padding(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(pageCount) { index ->
                               PdfPageItem(
                    pdfiumCore = pdfiumCore,
                    document = currentDocument,
                    pageIndex = index,
                    pageWidthFraction = pageWidthFraction,
                    columnWidthPx = columnWidthPx,
                    cache = cache,
                    renderMutex = renderMutex,
                    colors = colors
                )
            }
        }
    }
}

@Composable
private fun PdfPageItem(
    pdfiumCore: com.shockwave.pdfium.PdfiumCore,
    document: com.shockwave.pdfium.PdfDocument,
    pageIndex: Int,
    pageWidthFraction: Float,
    columnWidthPx: Float,
    cache: android.util.LruCache<Int, Bitmap>,
    renderMutex: Mutex,
    colors: AppColors
) {
    var bitmap by remember(pageIndex, pageWidthFraction) { mutableStateOf(cache.get(pageIndex)) }

    LaunchedEffect(pageIndex, pageWidthFraction) {
        if (bitmap != null) return@LaunchedEffect

        val rendered = withContext(Dispatchers.IO) {
            renderMutex.withLock {
                val cached = cache.get(pageIndex)
                if (cached != null) return@withLock cached

                try {
                    pdfiumCore.openPage(document, pageIndex)

                    val pageWidthPt = pdfiumCore.getPageWidthPoint(document, pageIndex)
                    val pageHeightPt = pdfiumCore.getPageHeightPoint(document, pageIndex)

                    val renderWidth = (columnWidthPx * pageWidthFraction)
                        .toInt()
                        .coerceIn(1, PDF_MAX_RENDER_WIDTH_PX)
                    val renderHeight = (pageHeightPt.toFloat() * renderWidth / pageWidthPt.toFloat())
                        .toInt()
                        .coerceAtLeast(1)

                    val bmp = Bitmap.createBitmap(renderWidth, renderHeight, Bitmap.Config.ARGB_8888)
                    bmp.eraseColor(android.graphics.Color.WHITE)
                    pdfiumCore.renderPageBitmap(
                        document, bmp, pageIndex, 0, 0, renderWidth, renderHeight
                    )

                    pdfiumCore.closePage(document, pageIndex)

                    cache.put(pageIndex, bmp)
                    bmp
                } catch (e: Exception) {
                    android.util.Log.e("PdfReaderView", "render page $pageIndex failed: ${e.message}", e)
                    try { pdfiumCore.closePage(document, pageIndex) } catch (_: Exception) {}
                    null
                }
            }
        }
        bitmap = rendered
    }

    val bmp = bitmap
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.surfaceGray, RoundedCornerShape(8.dp))
            .border(1.dp, colors.borderGray, RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
    ) {
        if (bmp != null) {
            Image(
                bitmap = bmp.asImageBitmap(),
                contentDescription = "Страница ${pageIndex + 1}",
                modifier = Modifier.fillMaxWidth(),
                contentScale = ContentScale.FillWidth
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f / 1.414f),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = colors.accent)
            }
        }
    }
}

// ========== БЛОКИ ДЛЯ ОЗВУЧКИ КНИГИ ==========
// Один блок = несколько предложений, склеенных по ~targetChars символов.
// Синтез идёт на блок, а не на каждое предложение — паузы между
// предложениями внутри блока исчезают. Паузы остаются только на границах
// блоков (раз в ~450 символов). Склейка — по границам предложений.

private const val BOOK_CHUNK_TARGET_CHARS = 450

private fun chunkIndexForFile(path: String): Int {
    val name = path.substringAfterLast("/")
    val match = Regex("book_chunk_(\\d+)_").find(name) ?: return -1
    return match.groupValues[1].toIntOrNull() ?: -1
}

private fun buildBookChunks(
    sentences: List<String>,
    targetChars: Int = BOOK_CHUNK_TARGET_CHARS
): List<String> {
    if (sentences.isEmpty()) return emptyList()
    val chunks = mutableListOf<String>()
    val sb = StringBuilder()
    for (s in sentences) {
        if (sb.isNotEmpty() && sb.length + s.length + 1 > targetChars) {
            chunks.add(sb.toString().trim())
            sb.setLength(0)
        }
        if (sb.isNotEmpty()) sb.append(' ')
        sb.append(s)
    }
    if (sb.isNotEmpty()) chunks.add(sb.toString().trim())
    return chunks
}

private fun findChunkForSentence(
    sentenceIndex: Int,
    chunks: List<String>,
    sentences: List<String>
): Int {
    if (sentenceIndex < 0 || sentences.isEmpty() || chunks.isEmpty()) return -1
    var cursor = 0
    for ((chunkIdx, chunk) in chunks.withIndex()) {
        var remaining = chunk.length
        while (cursor < sentences.size && remaining > 0) {
            val s = sentences[cursor]
            if (cursor == sentenceIndex) return chunkIdx
            remaining -= s.length
            if (remaining > 0) remaining -= 1
            cursor++
        }
        if (cursor > sentenceIndex) return chunkIdx
    }
    return chunks.lastIndex.coerceAtLeast(0)
}

private fun firstSentenceOfChunk(
    chunkIndex: Int,
    chunks: List<String>,
    sentences: List<String>
): Int {
    if (chunkIndex < 0 || chunks.isEmpty() || sentences.isEmpty()) return -1
    var cursor = 0
    for (ci in 0 until chunkIndex) {
        var remaining = chunks[ci].length
        while (cursor < sentences.size && remaining > 0) {
            remaining -= sentences[cursor].length
            if (remaining > 0) remaining -= 1
            cursor++
        }
    }
    return if (cursor in sentences.indices) cursor else -1
}

private fun isSentenceInChunk(
    sentenceIndex: Int,
    chunkIndex: Int,
    chunks: List<String>,
    sentences: List<String>
): Boolean {
    if (chunkIndex < 0 || chunkIndex >= chunks.size) return false
    val first = firstSentenceOfChunk(chunkIndex, chunks, sentences)
    if (first < 0) return false
    var cursor = first
    var remaining = chunks[chunkIndex].length
    while (cursor < sentences.size && remaining > 0) {
        if (cursor == sentenceIndex) return true
        remaining -= sentences[cursor].length
        if (remaining > 0) remaining -= 1
        cursor++
    }
    return false
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
    modelDisplayName: String = "",
    isSmartMode: Boolean = false,
    visionPhase: VisionPhase = VisionPhase.IDLE,
    isPreparingSpeech: Boolean = false,
    modifier: Modifier = Modifier,
    colors: AppColors
) {
          // Если TTS готовит озвучку — показываем это независимо от vision.
    // Иначе будет провал: модель закончила, а звук ещё не пошёл.
    val preparingStatus: Triple<Color, String, Boolean>? =
        if (isPreparingSpeech) {
            Triple(
                colors.accent.copy(alpha = 0.15f),
                "🔊 Готовлю озвучку...",
                true
            )
        } else null

       // Если идёт vision-фаза — показываем её независимо от режима.
    val visionStatus: Triple<Color, String, Boolean>? = when (visionPhase) {
        VisionPhase.RELEASING_TEXT -> Triple(
            colors.accent.copy(alpha = 0.15f),
            "🤖 Выгружаю текстовую модель...",
            true
        )
        VisionPhase.LOADING_VISION -> Triple(
            colors.accent.copy(alpha = 0.15f),
            "🤖 Загружаю зрение (SmolVLM2)...",
            true
        )
        VisionPhase.VISION_READY -> Triple(
            colors.accent.copy(alpha = 0.15f),
            "🤖 Зрение готово — анализирую...",
            true
        )
        VisionPhase.RESTORING_TEXT -> Triple(
            colors.accent.copy(alpha = 0.15f),
            "🤖 Возвращаю текстовую модель...",
            true
        )
        VisionPhase.TEXT_RESTORED -> Triple(
            colors.surfaceGray,
            "✅ Текстовая модель загружена обратно",
            false
        )
        VisionPhase.IDLE -> null
    }

        val (containerColor, statusText, showProgress) = preparingStatus ?: visionStatus ?: when (currentMode) {
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
                    if (modelDisplayName.isEmpty()) "🤖 Модель успешно загружена" else "🤖 Модель $modelDisplayName успешно загружена",
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
                text = if (isSmartMode) "🧠 $statusText" else statusText,
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
    modelDisplayName: String? = null,
    mmprojDisplayName: String? = null,
    onPickModel: () -> Unit,
    onPickMmproj: () -> Unit,
    onLoad: () -> Unit,
    onOpenDownloadDialog: () -> Unit,
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
                                        val displayModelPath = modelDisplayName
                        ?: cleanModelName(currentModelPath).ifEmpty { "Не выбрана" }
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
                                       val displayMmprojPath = mmprojDisplayName
                        ?: cleanModelName(mmprojPath).ifEmpty { "Не выбран" }
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
                    onClick = onOpenDownloadDialog,
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
private fun ModelDownloadDialog(
    viewModel: MainViewModel,
    onDismiss: () -> Unit,
    colors: AppColors,
    isDarkTheme: Boolean
) {
    val downloadProgress by viewModel.downloadProgress.collectAsStateWithLifecycle()
    val mmprojDownloadProgress by viewModel.mmprojDownloadProgress.collectAsStateWithLifecycle()
    var descriptionModel by remember { mutableStateOf<ModelInfo?>(null) }

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
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                    Text(text = "⬇", fontSize = 22.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Загрузка моделей",
                        style = MaterialTheme.typography.titleMedium,
                        color = colors.accent,
                        fontFamily = FontFamily.Monospace
                    )
                }

                                Text(
                    text = "Файлы сохраняются в Downloads/models/. Выберите их через «Выбрать модель».",
                    color = colors.text.copy(alpha = 0.8f),
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center,
                    fontFamily = FontFamily.Monospace
                )

                                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(420.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ModelCatalog.models.forEach { model ->
                        val progress = downloadProgress[model.id]
                        val mainFileExists = viewModel.isModelFileDownloaded(model.fileName)
                        val mmprojFileExists = model.mmprojFileName?.let {
                            viewModel.isModelFileDownloaded(it)
                        } ?: false

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, colors.borderGray),
                            colors = CardDefaults.cardColors(containerColor = colors.background.copy(alpha = 0.85f))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = model.name,
                                        color = colors.text,
                                        fontSize = 12.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.weight(1f)
                                    )
                                    if (mainFileExists && (model.mmprojFileName == null || mmprojFileExists)) {
                                        Text(
                                            text = "✅ скачано",
                                            color = Color(0xFF4CAF50),
                                            fontSize = 10.sp,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                }
                                if (mainFileExists && model.mmprojFileName != null && !mmprojFileExists) {
                                    Text(
                                        text = "⚠️ модель есть, проектор не скачан",
                                        color = Color(0xFFFFA500),
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = { descriptionModel = model },
                                        modifier = Modifier.weight(1f).height(34.dp),
                                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = colors.borderGray,
                                            contentColor = colors.text
                                        ),
                                        shape = RoundedCornerShape(8.dp),
                                        border = BorderStroke(1.dp, colors.borderGray)
                                    ) {
                                        Text("Описание", color = colors.text, fontSize = 11.sp)
                                    }

                                                                        val allDownloaded = mainFileExists &&
                                            (model.mmprojFileName == null || mmprojFileExists)

                                    Button(
                                        onClick = {
                                            viewModel.startModelDownload(model.id, model.url, model.fileName)
                                        },
                                        enabled = !allDownloaded &&
                                                (progress == null || progress.status == DownloadStatus.FAILED),
                                        modifier = Modifier.weight(1f).height(34.dp),
                                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = colors.accent,
                                            contentColor = colors.background,
                                            disabledContainerColor = colors.borderGray,
                                            disabledContentColor = colors.text.copy(alpha = 0.5f)
                                        ),
                                        shape = RoundedCornerShape(8.dp),
                                        border = BorderStroke(1.dp, colors.borderGray)
                                    ) {
                                        val label = when {
                                            allDownloaded -> "✅ Уже скачано"
                                            progress?.status == DownloadStatus.RUNNING -> "Качается..."
                                            progress?.status == DownloadStatus.SUCCESS -> "✅ Готово"
                                            progress?.status == DownloadStatus.FAILED -> "❌ Повторить"
                                            else -> "Загрузить"
                                        }
                                        Text(label, color = colors.background, fontSize = 11.sp)
                                    }
                                }

                                                                // Прогресс основной модели
                                if (progress != null && progress.status == DownloadStatus.RUNNING) {
                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalArrangement = Arrangement.spacedBy(2.dp)
                                    ) {
                                        Text(
                                            text = "📦 Модель: ${progress.percent}%",
                                            color = colors.text,
                                            fontSize = 10.sp,
                                            fontFamily = FontFamily.Monospace,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                        LinearProgressIndicator(
                                            progress = progress.percent / 100f,
                                            modifier = Modifier.fillMaxWidth().height(6.dp),
                                            color = colors.accent,
                                            trackColor = colors.borderGray
                                        )
                                    }
                                }

                                // Прогресс проектора (только если он есть у модели)
                                val mmprojProgress = mmprojDownloadProgress[model.id]
                                if (model.mmprojUrl != null && mmprojProgress != null) {
                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalArrangement = Arrangement.spacedBy(2.dp)
                                    ) {
                                        val mmprojText = when (mmprojProgress.status) {
                                            DownloadStatus.RUNNING -> "🔧 Проектор: ${mmprojProgress.percent}%"
                                            DownloadStatus.SUCCESS -> "🔧 Проектор: ✅ 100%"
                                            DownloadStatus.FAILED -> "🔧 Проектор: ❌ Ошибка"
                                            else -> "🔧 Проектор: ожидание..."
                                        }
                                        Text(
                                            text = mmprojText,
                                            color = colors.text,
                                            fontSize = 10.sp,
                                            fontFamily = FontFamily.Monospace,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                        if (mmprojProgress.status == DownloadStatus.RUNNING) {
                                            LinearProgressIndicator(
                                                progress = mmprojProgress.percent / 100f,
                                                modifier = Modifier.fillMaxWidth().height(6.dp),
                                                color = Color(0xFF4CAF50),
                                                trackColor = colors.borderGray
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(0.6f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.accent,
                        contentColor = colors.background
                    ),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, colors.borderGray)
                ) {
                    Text("Закрыть", color = colors.background, fontSize = 13.sp)
                }
            }
        }
    }

        descriptionModel?.let { model ->
        ModelDescriptionDialog(
            model = model,
            onDismiss = { descriptionModel = null },
            colors = colors,
            isDarkTheme = isDarkTheme
        )
    }
}

@Composable
private fun ModelDescriptionDialog(
    model: ModelInfo,
    onDismiss: () -> Unit,
    colors: AppColors,
    isDarkTheme: Boolean
) {
    MaterialTheme(
        colorScheme = if (isDarkTheme) darkColorScheme() else lightColorScheme()
    ) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text(
                    text = model.name,
                    color = colors.accent,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = model.description,
                        color = colors.text,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        lineHeight = 17.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.accent,
                        contentColor = colors.background
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Понятно", color = colors.background)
                }
            },
            containerColor = colors.surfaceGray
        )
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
    isCompressing: Boolean,
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
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            colors.gradTop,
                            colors.gradMid,
                            colors.gradBottom
                        )
                    ),
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
                    enabled = enabled && !isGenerating && !isSpeaking && !isCompressing,
                    placeholder = {
                        Text(
                            if (isCompressing) "⏳ Сжатие беседы..."
                            else "Введите запрос...",
                            color = colors.text.copy(alpha = 0.5f)
                        )
                    },
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
                            enabled = !isGenerating && !isSpeaking && !isCompressing,
                            modifier = Modifier.size(41.dp)
                        ) {
                                                        Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = "Распознать речь",
                                tint = if (!isGenerating && !isSpeaking && !isCompressing) {
                                    colors.accent
                                } else {
                                    colors.text.copy(alpha = 0.4f)
                                }
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
                                enabled = enabled && !isCompressing,
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

@Composable
private fun MediaPickerDialog(
    onPickGallery: () -> Unit,
    onTakePhoto: () -> Unit,
    onPickPdf: () -> Unit,
    onDismiss: () -> Unit,
    colors: AppColors,
    isDarkTheme: Boolean
) {
    MaterialTheme(
        colorScheme = if (isDarkTheme) darkColorScheme() else lightColorScheme()
    ) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text(
                    "📎 Добавить медиа",
                    style = MaterialTheme.typography.titleLarge,
                    color = colors.text
                )
            },
                        text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MediaOptionRow(
                        emoji = "🖼️",
                        label = "Галерея",
                        description = "Выбрать изображение из галереи",
                        onClick = onPickGallery,
                        colors = colors
                    )
                    MediaOptionRow(
                        emoji = "📷",
                        label = "Камера",
                        description = "Сфотографировать прямо сейчас",
                        onClick = onTakePhoto,
                        colors = colors
                    )
                    MediaOptionRow(
                        emoji = "📄",
                        label = "PDF",
                        description = "Выбрать PDF и указать страницы для отправки в модель",
                        onClick = onPickPdf,
                        colors = colors
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = colors.borderGray)
                ) {
                    Text("Отмена", color = colors.text)
                }
            },
            containerColor = colors.surfaceGray
        )
    }
}
private fun android.content.Context.findActivity(): MainActivity? {
    var ctx = this
    while (ctx is android.content.ContextWrapper) {
        if (ctx is MainActivity) return ctx
        ctx = ctx.baseContext
    }
    return null
}
@Composable
private fun MediaOptionRow(
    emoji: String,
    label: String,
    description: String,
    onClick: () -> Unit,
    colors: AppColors
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, colors.borderGray),
        colors = CardDefaults.cardColors(containerColor = colors.background)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(text = emoji, fontSize = 24.sp)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    color = colors.text,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = description,
                    color = colors.text.copy(alpha = 0.7f),
                    fontSize = 11.sp
                )
            }
        }
    }
}
