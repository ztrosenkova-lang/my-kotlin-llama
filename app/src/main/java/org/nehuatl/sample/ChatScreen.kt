package org.nehuatl.sample

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle

// Светлая воздушная палитра
private val AppBackground = Color(0xFFFFFFFF)
private val SurfaceGray = Color(0xFFF1F3F5)
private val BorderGray = Color(0xFFCED4DA)
private val AccentColor = Color(0xFF74C0FC)
private val DarkText = Color(0xFF212529)

private val ChatFontFamily = FontFamily.Monospace

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
    val systemPromptText by viewModel.systemPrompt.collectAsStateWithLifecycle()
    val chatMessages by viewModel.chatHistory.collectAsStateWithLifecycle()
    val temperature by viewModel.temperature.collectAsStateWithLifecycle()

    var promptInput by remember { mutableStateOf("") }
    var showModelDialog by remember { mutableStateOf(currentModelPath == null) }
    var showSettings by remember { mutableStateOf(false) }
    var showPromptSettings by remember { mutableStateOf(false) }
    var showCloudDialog by remember { mutableStateOf(false) }
    var tempPromptText by remember(systemPromptText) { mutableStateOf(systemPromptText) }
    var tempTemperature by remember(temperature) { mutableStateOf(temperature) }
    var showHelpDialog by remember { mutableStateOf(false) }
    var showMemoryEditor by remember { mutableStateOf(false) }
    var memoryEditText by remember { mutableStateOf("") }

    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val scrollState = rememberScrollState()

    LaunchedEffect(chatMessages.size, generatedText.length) {
        scrollState.scrollTo(scrollState.maxValue)
    }

    LaunchedEffect(state) {
        if (state is GenerationState.ModelLoaded) {
            try {
                focusRequester.requestFocus()
                keyboardController?.show()
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    LaunchedEffect(showSettings) {
        if (showSettings) {
            tempTemperature = temperature
        }
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
                }
            },
            onDismiss = if (currentModelPath != null) {
                { showModelDialog = false }
            } else null
        )
    }

    if (showHelpDialog) {
        AlertDialog(
            onDismissRequest = { showHelpDialog = false },
            title = {
                Text(
                    text = "📖 Меч Правды v2.0 — Руководство",
                    style = MaterialTheme.typography.titleLarge,
                    color = DarkText
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = "Добро пожаловать в твой полностью локальный ИИ-ассистент!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = DarkText
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(text = "🧠 1. Долговременная память", fontWeight = FontWeight.Bold, color = AccentColor)
                    Text(text = "• Чтобы ИИ что-то зафиксировал, начни фразу со слова 'запомни'.", color = DarkText)
                    Text(text = "• Чтобы извлечь данные, используй слово 'вспомни'.", color = DarkText)
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(text = "💬 2. Сплошной чат с контекстом", fontWeight = FontWeight.Bold, color = AccentColor)
                    Text(text = "• Приложение сохраняет историю текущего разговора.", color = DarkText)
                    Text(text = "• Чтобы очистить ОЗУ, нажми кнопку 'Очистить'.", color = DarkText)
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(text = "📷 3. Зрение и работа с камерой", fontWeight = FontWeight.Bold, color = AccentColor)
                    Text(text = "• Загрузи мультимодальный файл проектора зрения (.gguf).", color = DarkText)
                    Text(text = "• Нажми на скрепку, сделай фото и отправь.", color = DarkText)
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(text = "⚙️ 4. Динамическая смена роли ИИ", fontWeight = FontWeight.Bold, color = AccentColor)
                    Text(text = "• Нажми на Шестерёнку и измени системный промпт.", color = DarkText)
                }
            },
            confirmButton = {
                Button(
                    onClick = { showHelpDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentColor)
                ) {
                    Text("Понятно", color = DarkText)
                }
            }
        )
    }

    if (showMemoryEditor) {
        AlertDialog(
            onDismissRequest = { showMemoryEditor = false },
            title = {
                Text(
                    text = "🧠 База Знаний ИИ",
                    style = MaterialTheme.typography.titleLarge,
                    color = DarkText
                )
            },
            text = {
                OutlinedTextField(
                    value = memoryEditText,
                    onValueChange = { memoryEditText = it },
                    placeholder = { Text("Вставь сюда свой прайс-лист или данные...", color = DarkText.copy(alpha = 0.5f)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(400.dp),
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
                Button(
                    onClick = {
                        viewModel.overwriteLongTermMemory(memoryEditText)
                        showMemoryEditor = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentColor)
                ) {
                    Text("Сохранить", color = DarkText)
                }
            },
            dismissButton = {
                TextButton(onClick = { showMemoryEditor = false }) {
                    Text("Закрыть", color = DarkText)
                }
            }
        )
    }

    if (showCloudDialog) {
        AlertDialog(
            onDismissRequest = { showCloudDialog = false },
            title = {
                Text(
                    text = "☁️ Облачный ИИ",
                    style = MaterialTheme.typography.titleLarge,
                    color = DarkText
                )
            },
            text = {
                Text(
                    text = "Модуль подключения внешних облачных моделей находится в разработке.",
                    color = DarkText
                )
            },
            confirmButton = {
                Button(
                    onClick = { showCloudDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentColor)
                ) {
                    Text("ОК", color = DarkText)
                }
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppBackground)
            .imePadding()
    ) {
        // Верхняя панель с логотипом
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            shape = MaterialTheme.shapes.medium,
            border = BorderStroke(1.dp, BorderGray),
            colors = CardDefaults.cardColors(
                containerColor = SurfaceGray
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.mipmap.ic_launcher),
                        contentDescription = "Лого",
                        contentScale = ContentScale.FillBounds,
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(16.dp))
                    )
                    Text(
                        text = "Меч Правды v2.0",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = DarkText
                    )
                }
            }
        }

        // Панель с пятью кнопками и подписями
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, BorderGray),
            colors = CardDefaults.cardColors(
                containerColor = SurfaceGray
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Кнопка 1: Блокнот Базы Знаний
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    IconButton(
                        onClick = {
                            memoryEditText = viewModel.readFromLongTermMemory()
                            showMemoryEditor = true
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.List,
                            contentDescription = "База Знаний",
                            tint = AccentColor
                        )
                    }
                    Text(
                        text = "мозг",
                        color = DarkText,
                        fontSize = 8.sp
                    )
                }

                // Кнопка 2: Настройки движка
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    IconButton(
                        onClick = { showSettings = !showSettings }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Настройки движка",
                            tint = AccentColor
                        )
                    }
                    Text(
                        text = "движок",
                        color = DarkText,
                        fontSize = 8.sp
                    )
                }

                // Кнопка 3: Роль ИИ
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    IconButton(
                        onClick = { showPromptSettings = !showPromptSettings }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Build,
                            contentDescription = "Роль ИИ",
                            tint = AccentColor
                        )
                    }
                    Text(
                        text = "характер",
                        color = DarkText,
                        fontSize = 8.sp
                    )
                }

                // Кнопка 4: Облачный ИИ
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    IconButton(
                        onClick = { showCloudDialog = true }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Computer,
                            contentDescription = "Облачный ИИ",
                            tint = AccentColor
                        )
                    }
                    Text(
                        text = "облачный ии",
                        color = DarkText,
                        fontSize = 8.sp
                    )
                }

                // Кнопка 5: Справка
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    IconButton(
                        onClick = { showHelpDialog = true }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Справка",
                            tint = AccentColor
                        )
                    }
                    Text(
                        text = "справка",
                        color = DarkText,
                        fontSize = 8.sp
                    )
                }
            }
        }

        // Выезжающая панель настроек температуры
        if (showSettings) {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceGray),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                border = BorderStroke(1.dp, BorderGray)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "🌡️ Настройки движка ИИ",
                        color = DarkText,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Креативность (Температура): ${String.format("%.1f", tempTemperature)}",
                        color = DarkText
                    )
                    Slider(
                        value = tempTemperature,
                        onValueChange = { tempTemperature = it },
                        valueRange = 0.1f..1.0f,
                        steps = 9,
                        modifier = Modifier.fillMaxWidth(),
                        colors = SliderDefaults.colors(
                            thumbColor = AccentColor,
                            activeTrackColor = AccentColor,
                            inactiveTrackColor = BorderGray
                        )
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { showModelDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = BorderGray),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Сменить или перезагрузить модель", color = DarkText)
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    // ⭐ ИСПРАВЛЕНО: теперь сохраняет температуру
                    Button(
                        onClick = {
                            viewModel.updateTemperature(tempTemperature) // Фиксируем изменения в ОЗУ!
                            showSettings = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentColor),
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Закрыть", color = DarkText)
                    }
                }
            }
        }

        // Выезжающая панель системного промпта
        if (showPromptSettings) {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceGray),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                border = BorderStroke(1.dp, BorderGray)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "🧠 Роль ИИ (Системный промпт)",
                        color = DarkText,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = tempPromptText,
                        onValueChange = { tempPromptText = it },
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
                        onClick = {
                            viewModel.updateSystemPrompt(tempPromptText)
                            showPromptSettings = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentColor),
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Сохранить", color = DarkText)
                    }
                }
            }
        }

        // StatusBar (без кнопки "настроить")
        StatusBar(
            state = state,
            currentModel = currentModelPath,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )

        // Текстовое поле чата
        Card(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(8.dp),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, BorderGray),
            colors = CardDefaults.cardColors(containerColor = AppBackground)
        ) {
            SelectionContainer {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .verticalScroll(scrollState)
                ) {
                    chatMessages.forEach { message ->
                        val prefix = if (message.role == "user") "Вы: " else "ИИ: "
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
                }
            }
        }

        imagePath?.let {
            Card(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceGray)
            ) {
                Row(
                    modifier = Modifier.padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("[Изображение]", style = MaterialTheme.typography.bodySmall, color = DarkText)
                }
            }
        }

        PromptInput(
            prompt = promptInput,
            onPromptChange = { promptInput = it },
            onGenerate = {
                if (state.canGenerate() && promptInput.isNotBlank()) {
                    keyboardController?.hide()
                    viewModel.generate(promptInput, imagePath)
                    promptInput = ""
                    onImageUsed()
                }
            },
            onAbort = {
                keyboardController?.hide()
                viewModel.abort()
            },
            onClearChat = { viewModel.clearChat() },
            onPickImage = onPickImage,
            enabled = state.canGenerate(),
            isGenerating = state is GenerationState.Generating,
            focusRequester = focusRequester,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Composable
private fun ModelPickerDialog(
    currentModelPath: String?,
    mmprojPath: String?,
    onPickModel: () -> Unit,
    onPickMmproj: () -> Unit,
    onLoad: () -> Unit,
    onDismiss: (() -> Unit)?
) {
    Dialog(
        onDismissRequest = { onDismiss?.invoke() }
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceGray)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    "Настройка ИИ",
                    style = MaterialTheme.typography.headlineSmall,
                    color = DarkText
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Языковая модель", color = DarkText)
                    if (currentModelPath != null) Text(
                        text = "[Файл модели]",
                        style = MaterialTheme.typography.bodySmall,
                        color = DarkText.copy(alpha = 0.6f)
                    )

                    Button(
                        onClick = onPickModel,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = BorderGray)
                    ) {
                        Text(if (currentModelPath == null) "Выбрать модель" else "Изменить модель", color = DarkText)
                    }
                }

                Column(
                    modifier = Modifier.padding(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        "(опционально)",
                        style = MaterialTheme.typography.bodySmall,
                        color = DarkText.copy(alpha = 0.6f)
                    )
                    Text("Мультимодальный проектор (mmproj)", color = DarkText)
                    if (mmprojPath != null) Text(
                        text = "[Файл проектора]",
                        style = MaterialTheme.typography.bodySmall,
                        color = DarkText.copy(alpha = 0.6f)
                    )

                    Button(
                        onClick = onPickMmproj,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = BorderGray)
                    ) {
                        Text(if (mmprojPath == null) "Выбрать проектор" else "Изменить проектор", color = DarkText)
                    }
                }

                Button(
                    onClick = onLoad,
                    enabled = currentModelPath != null,
                    modifier = Modifier.fillMaxWidth().padding(top=8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentColor
                    )
                ) {
                    Text("Запустить нейросеть", color = DarkText)
                }

                if (onDismiss != null) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("Отмена", color = DarkText)
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusBar(
    state: GenerationState,
    currentModel: String?,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (state) {
                is GenerationState.Error -> AccentColor.copy(alpha = 0.15f)
                is GenerationState.Generating -> AccentColor.copy(alpha = 0.15f)
                is GenerationState.LoadingModel -> BorderGray.copy(alpha = 0.3f)
                else -> SurfaceGray
            }
        ),
        border = BorderStroke(1.dp, BorderGray)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                when (state) {
                    is GenerationState.Idle -> {
                        Text(if (currentModel == null) "Выберите модель" else "ИИ Готов", color = if (currentModel == null) DarkText.copy(alpha = 0.5f) else AccentColor)
                    }
                    is GenerationState.LoadingModel -> {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = AccentColor)
                        Text("Загрузка модели...", color = DarkText)
                    }
                    is GenerationState.ModelLoaded -> {
                        Text("✓ ИИ Готов", color = AccentColor)
                    }
                    is GenerationState.Generating -> {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = AccentColor)
                        val label = if (state.tokensGenerated == 0) "Думаю..." else "Думаю... (${state.tokensGenerated} токенов)"
                        Text(label, color = DarkText)
                    }
                    is GenerationState.Completed -> {
                        Text(
                            "✓ Ответ готов (${state.tokenCount} токенов, ${state.durationMs}мс)",
                            color = AccentColor
                        )
                    }
                    is GenerationState.Error -> {
                        Text("⚠ ${state.message}", color = AccentColor)
                    }
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
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onPickImage,
            enabled = enabled && !isGenerating
        ) {
            Icon(
                Icons.Default.Add,
                contentDescription = "Добавить изображение",
                tint = if (enabled && !isGenerating) AccentColor else BorderGray
            )
        }

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
                focusedBorderColor = AccentColor,
                unfocusedBorderColor = BorderGray,
                cursorColor = AccentColor
            )
        )

        // Вертикальный контейнер с двумя кнопками
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isGenerating) {
                // Кнопка "Стоп" - фон SurfaceGray
                IconButton(
                    onClick = onAbort,
                    modifier = Modifier.size(48.dp),
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = SurfaceGray
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Стоп",
                        tint = DarkText
                    )
                }
            } else {
                // Кнопка "Отправить" - фон SurfaceGray
                IconButton(
                    onClick = onGenerate,
                    enabled = enabled && prompt.isNotBlank(),
                    modifier = Modifier.size(48.dp),
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = if (enabled && prompt.isNotBlank()) SurfaceGray else BorderGray
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Отправить",
                        tint = if (enabled && prompt.isNotBlank()) DarkText else DarkText.copy(alpha = 0.4f)
                    )
                }
            }

            // Кнопка "Очистить чат" - фон SurfaceGray
            IconButton(
                onClick = onClearChat,
                enabled = true,
                modifier = Modifier.size(48.dp),
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = SurfaceGray
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Очистить чат",
                    tint = DarkText
                )
            }
        }
    }
}
