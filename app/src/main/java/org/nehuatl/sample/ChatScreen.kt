package org.nehuatl.sample

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Edit
import androidx.compute.material.icons.filled.Info
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compute.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compute.material3.SliderColors
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle

// Цветовая палитра "Озёрная чайка" (Larus ridibundus)
private val GullBg = Color(0xFF1C222E)       // Фон приложения (Речной сизый туман)
private val GullHead = Color(0xFF2E231D)     // Шапка и карточки настроек (Шоколадно-бурый)
private val GullBeak = Color(0xFFD63B2F)     // Акценты и кнопки отправки/очистки (Кораллово-красный клюв)
private val GullBorder = Color(0xFF434B5A)   // Контуры и рамки
private val UserBubble = Color(0xFFE6ECEF)   // Сообщение пользователя (Чистое белое крыло)
private val AIBubble = Color(0xFF2E3542)     // Ответ ИИ (Мантия крыла)
private val PureWhite = Color(0xFFFFFFFF)
private val BlackText = Color(0xFF121417)

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
    imagePath: String? = null // Passed from MainActivity
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val generatedText by viewModel.generatedText.collectAsStateWithLifecycle()
    val systemPromptText by viewModel.systemPrompt.collectAsStateWithLifecycle()
    val chatMessages by viewModel.chatHistory.collectAsStateWithLifecycle()
    val temperature by viewModel.temperature.collectAsStateWithLifecycle()

    var promptInput by remember { mutableStateOf("") }
    var showModelDialog by remember { mutableStateOf(currentModelPath == null) }
    var showSettings by remember { mutableStateOf(false) }
    var tempPromptText by remember(systemPromptText) { mutableStateOf(systemPromptText) }
    var tempTemperature by remember(temperature) { mutableStateOf(temperature) }
    var showHelpDialog by remember { mutableStateOf(false) }
    var showMemoryEditor by remember { mutableStateOf(false) }
    var memoryEditText by remember { mutableStateOf("") }

    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val listState = rememberLazyListState()

    // Автопрокрутка чата вниз при появлении новых сообщений или генерации текста
    LaunchedEffect(chatMessages.size, generatedText) {
        if (chatMessages.isNotEmpty() || generatedText.isNotEmpty()) {
            val targetIndex = if (state is GenerationState.Generating && generatedText.isNotEmpty()) {
                chatMessages.size
            } else {
                (chatMessages.size - 1).coerceAtLeast(0)
            }
            listState.animateScrollToItem(targetIndex)
        }
    }

    // Show keyboard only when model is fully loaded and ready
    LaunchedEffect(state) {
        if (state is GenerationState.ModelLoaded) {
            try {
                focusRequester.requestFocus()
                keyboardController?.show()
            } catch (e: Exception) {
                // Focus request might fail if UI isn't ready yet
            }
        }
    }

    // Синхронизируем локальную температуру с глобальной при открытии настроек
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
                    text = "🛡️ Меч Правды v2.0 — Руководство",
                    style = MaterialTheme.typography.titleLarge,
                    color = GullBeak
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = "Добро пожаловать в твой полностью локальный ИИ-ассистент! Приложение работает на 100% без интернета и защищено в песочнице устройства.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = PureWhite
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(text = "🧠 1. Долговременная память", fontWeight = FontWeight.Bold, color = GullBeak)
                    Text(text = "• Чтобы ИИ что-то зафиксировал в защищённый файл, начни фразу со слова 'запомни' (например: 'запомни, моя любимая реакция — это реакция Кучерова'). Модель мгновенно запишет это в песочницу.", color = PureWhite)
                    Text(text = "• Чтобы извлечь данные, используй в запросе слово 'вспомни' (например: 'вспомни мою любимую реакцию и распиши её'). ИИ вычитает архив и ответит на основе твоих заметок.", color = PureWhite)
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(text = "💬 2. Сплошной чат с контекстом", fontWeight = FontWeight.Bold, color = GullBeak)
                    Text(text = "• Приложение сохраняет историю текущего разговора. Ты можешь задавать уточняющие вопросы, ИИ помнит начало беседы.", color = PureWhite)
                    Text(text = "• Чтобы полностью очистить ОЗУ и начать диалог с чистого листа, нажми красную кнопку 'Очистить чат' в самом низу.", color = PureWhite)
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(text = "📷 3. Зрение и работа с камерой", fontWeight = FontWeight.Bold, color = GullBeak)
                    Text(text = "• Для анализа изображений загрузи мультимодальный файл проектора зрения (.gguf) в стартовом меню.", color = PureWhite)
                    Text(text = "• Нажми на скрепку, сделай фото задачи или формулы, напиши текстовый вопрос (например, 'Реши это уравнение') и отправь. Приложение автоматически склеит нужные теги зрения.", color = PureWhite)
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(text = "⚙️ 4. Динамическая смена роли ИИ", fontWeight = FontWeight.Bold, color = GullBeak)
                    Text(text = "• Нажми на Шестерёнку в шапке. В поле 'Системный промпт' ты можешь на ходу переписать инструкцию (например, превратить ИИ в строгого химика), нажми 'Сохранить', и модель мгновенно перестроится.", color = PureWhite)
                }
            },
            confirmButton = {
                Button(
                    onClick = { showHelpDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = GullBeak)
                ) {
                    Text("Понятно", color = BlackText)
                }
            }
        )
    }

    if (showMemoryEditor) {
        AlertDialog(
            onDismissRequest = { showMemoryEditor = false },
            title = {
                Text(
                    text = "🧠 База Знаний ИИ (Прайс-листы и заметки)",
                    style = MaterialTheme.typography.titleLarge,
                    color = GullBeak
                )
            },
            text = {
                OutlinedTextField(
                    value = memoryEditText,
                    onValueChange = { memoryEditText = it },
                    placeholder = { Text("Скопируй и вставь сюда свой прайс-лист или любые важные данные...", color = PureWhite.copy(alpha = 0.6f)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(400.dp),
                    maxLines = 100,
                    singleLine = false,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = PureWhite,
                        unfocusedTextColor = PureWhite,
                        focusedContainerColor = GullHead,
                        unfocusedContainerColor = GullHead,
                        focusedBorderColor = GullBeak,
                        unfocusedBorderColor = GullBorder,
                        cursorColor = GullBeak
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.overwriteLongTermMemory(memoryEditText)
                        showMemoryEditor = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GullBeak)
                ) {
                    Text("Сохранить", color = BlackText)
                }
            },
            dismissButton = {
                TextButton(onClick = { showMemoryEditor = false }) {
                    Text("Закрыть", color = PureWhite)
                }
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(GullBg)
            .imePadding()
    ) {
        // Верхняя брендированная панель с логотипом и кнопками управления в Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            shape = MaterialTheme.shapes.medium,
            border = BorderStroke(1.dp, GullBorder),
            colors = CardDefaults.cardColors(
                containerColor = GullHead
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Левая часть: логотип и название
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_launcher_foreground),
                        contentDescription = "Логотип приложения",
                        modifier = Modifier
                            .size(40.dp)
                            .padding(end = 4.dp)
                    )
                    Text(
                        text = "Меч Правды v2.0",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = PureWhite
                    )
                }

                // Правая часть: кнопки управления
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Кнопка редактирования базы знаний
                    IconButton(
                        onClick = {
                            memoryEditText = viewModel.readFromLongTermMemory()
                            showMemoryEditor = true
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Редактировать Базу Знаний",
                            tint = GullBeak
                        )
                    }

                    // Кнопка справки
                    IconButton(onClick = { showHelpDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Справка",
                            tint = GullBeak
                        )
                    }

                    // Кнопка «Шестеренка» — открывает/закрывает панель системного промпта
                    IconButton(onClick = { showSettings = !showSettings }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Настройки токенов",
                            tint = GullBeak
                        )
                    }
                }
            }
        }

        // Выезжающая панель настроек токенов
        if (showSettings) {
            Card(
                colors = CardDefaults.cardColors(containerColor = GullHead),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                border = BorderStroke(1.dp, GullBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "⚙️ Настройки движка ИИ",
                        color = PureWhite,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Поле системного промпта
                    OutlinedTextField(
                        value = tempPromptText,
                        onValueChange = { tempPromptText = it },
                        label = { Text("Системный промпт (Роль ИИ)", color = GullBeak) },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3,
                        singleLine = false,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = PureWhite,
                            unfocusedTextColor = PureWhite,
                            focusedContainerColor = GullHead,
                            unfocusedContainerColor = GullHead,
                            focusedBorderColor = GullBeak,
                            unfocusedBorderColor = GullBorder,
                            cursorColor = GullBeak
                        )
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Ползунок температуры (креативности)
                    Text(
                        text = "Креативность (Температура): ${String.format("%.1f", tempTemperature)}",
                        color = PureWhite
                    )
                    Slider(
                        value = tempTemperature,
                        onValueChange = { tempTemperature = it },
                        valueRange = 0.1f..1.0f,
                        steps = 9,
                        modifier = Modifier.fillMaxWidth(),
                        colors = SliderDefaults.colors(
                            thumbColor = GullBeak,
                            activeTrackColor = GullBeak,
                            inactiveTrackColor = GullBorder
                        )
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Кнопка смены/сброса модели
                    Button(
                        onClick = { showModelDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = GullBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Сменить или перезагрузить модель", color = PureWhite)
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    // Кнопка Сохранить и Закрыть
                    Button(
                        onClick = {
                            viewModel.updateSystemPrompt(tempPromptText)
                            viewModel.updateTemperature(tempTemperature)
                            showSettings = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GullBeak),
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Сохранить и Закрыть", color = BlackText)
                    }
                }
            }
        }

        // Status bar
        StatusBar(
            state = state,
            currentModel = currentModelPath,
            onChangeModel = { showModelDialog = true },
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )

        // Chat messages list with LazyColumn
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            state = listState,
            reverseLayout = false
        ) {
            items(chatMessages) { message ->
                val isUser = message.role == "user"
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isUser) UserBubble else AIBubble
                    ),
                    modifier = Modifier
                        .padding(vertical = 4.dp)
                        .fillMaxWidth()
                ) {
                    Text(
                        text = message.text,
                        color = if (isUser) BlackText else PureWhite,
                        modifier = Modifier
                            .padding(8.dp)
                            .align(if (isUser) Alignment.End else Alignment.Start)
                    )
                }
            }

            // Текущий генерируемый текст
            if (generatedText.isNotEmpty() && state is GenerationState.Generating) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = AIBubble
                        ),
                        modifier = Modifier
                            .padding(vertical = 4.dp)
                            .fillMaxWidth()
                    ) {
                        Text(
                            text = generatedText,
                            color = PureWhite,
                            modifier = Modifier
                                .padding(8.dp)
                                .align(Alignment.Start)
                        )
                    }
                }
            }
        }

        // Image indicator if selected
        imagePath?.let {
            Card(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = GullHead)
            ) {
                Row(
                    modifier = Modifier.padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("[Изображение]", style = MaterialTheme.typography.bodySmall, color = PureWhite)
                }
            }
        }

        // Prompt input
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
            colors = CardDefaults.cardColors(containerColor = GullHead)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    "Настройка ИИ",
                    style = MaterialTheme.typography.headlineSmall,
                    color = PureWhite
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Языковая модель", color = PureWhite)
                    if (currentModelPath != null) Text(
                        text = "[Файл модели]",
                        style = MaterialTheme.typography.bodySmall,
                        color = PureWhite.copy(alpha = 0.6f)
                    )

                    Button(
                        onClick = onPickModel,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = GullBorder)
                    ) {
                        Text(if (currentModelPath == null) "Выбрать модель" else "Изменить модель", color = PureWhite)
                    }
                }

                Column(
                    modifier = Modifier.padding(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        "(опционально)",
                        style = MaterialTheme.typography.bodySmall,
                        color = PureWhite.copy(alpha = 0.6f)
                    )
                    Text("Мультимодальный проектор (mmproj)", color = PureWhite)
                    if (mmprojPath != null) Text(
                        text = "[Файл проектора]",
                        style = MaterialTheme.typography.bodySmall,
                        color = PureWhite.copy(alpha = 0.6f)
                    )

                    Button(
                        onClick = onPickMmproj,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = GullBorder)
                    ) {
                        Text(if (mmprojPath == null) "Выбрать проектор" else "Изменить проектор", color = PureWhite)
                    }
                }

                Button(
                    onClick = onLoad,
                    enabled = currentModelPath != null,
                    modifier = Modifier.fillMaxWidth().padding(top=8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GullBeak
                    )
                ) {
                    Text("Запустить нейросеть", color = BlackText)
                }

                if (onDismiss != null) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("Отмена", color = PureWhite)
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
    onChangeModel: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (state) {
                is GenerationState.Error -> GullBeak.copy(alpha = 0.2f)
                is GenerationState.Generating -> GullBeak.copy(alpha = 0.2f)
                is GenerationState.LoadingModel -> GullBorder.copy(alpha = 0.5f)
                else -> GullHead
            }
        ),
        border = BorderStroke(1.dp, GullBorder)
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
                        Text(if (currentModel == null) "Выберите модель" else "ИИ Готов", color = if (currentModel == null) PureWhite.copy(alpha = 0.6f) else GullBeak)
                    }
                    is GenerationState.LoadingModel -> {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = GullBeak)
                        Text("Загрузка модели...", color = PureWhite)
                    }
                    is GenerationState.ModelLoaded -> {
                        Text("✓ ИИ Готов", color = GullBeak)
                    }
                    is GenerationState.Generating -> {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = GullBeak)
                        val label = if (state.tokensGenerated == 0) "Думаю..." else "Думаю... (${state.tokensGenerated} токенов)"
                        Text(label, color = PureWhite)
                    }
                    is GenerationState.Completed -> {
                        Text(
                            "✓ Ответ готов (${state.tokenCount} токенов, ${state.durationMs}мс)",
                            color = GullBeak
                        )
                    }
                    is GenerationState.Error -> {
                        Text("⚠ ${state.message}", color = GullBeak)
                    }
                }
            }

            if (!state.isActive()) {
                TextButton(onClick = onChangeModel) {
                    Text("Настроить", color = GullBeak)
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
                tint = if (enabled && !isGenerating) GullBeak else GullBorder
            )
        }

        TextField(
            value = prompt,
            onValueChange = onPromptChange,
            modifier = Modifier
                .weight(1f)
                .focusRequester(focusRequester),
            enabled = enabled && !isGenerating,
            placeholder = { Text("Введите запрос...", color = PureWhite.copy(alpha = 0.6f)) },
            maxLines = 3,
            singleLine = false,
            colors = TextFieldDefaults.colors(
                focusedTextColor = PureWhite,
                unfocusedTextColor = PureWhite,
                focusedContainerColor = GullHead,
                unfocusedContainerColor = GullHead,
                focusedIndicatorColor = GullBeak,
                unfocusedIndicatorColor = GullBorder,
                cursorColor = GullBeak
            )
        )

        if (isGenerating) {
            // Кнопка "Стоп" — останавливает генерацию и озвучку через viewModel.abort()
            Button(
                onClick = onAbort,
                enabled = true,
                colors = ButtonDefaults.buttonColors(containerColor = GullBeak)
            ) {
                Text("Стоп", color = PureWhite)
            }
        } else {
            Button(
                onClick = onGenerate,
                enabled = enabled && prompt.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (enabled && prompt.isNotBlank()) GullBeak else GullBorder
                )
            ) {
                Text(
                    "Отправить",
                    color = if (enabled && prompt.isNotBlank()) PureWhite else PureWhite.copy(alpha = 0.4f)
                )
            }
        }

        // Кнопка очистки чата — одного размера с кнопкой "Отправить"
        Button(
            onClick = onClearChat,
            enabled = true,
            colors = ButtonDefaults.buttonColors(
                containerColor = GullBeak
            ),
            modifier = Modifier
        ) {
            Text("Очистить", color = PureWhite)
        }
    }
}
