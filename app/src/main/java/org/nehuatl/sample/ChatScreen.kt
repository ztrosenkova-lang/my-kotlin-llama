package org.nehuatl.sample

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Psychology
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
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle

private val AppBackground = Color(0xFFFFFFFF)
private val SurfaceGray = Color(0xFFF1F3F5)
private val BorderGray = Color(0xFFCED4DA)
private val AccentColor = Color(0xFF74C0FC)
private val DarkText = Color(0xFF212529)
private val ChatFontFamily = FontFamily.Monospace

// Состояния переключателя на английском
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
    val systemPromptText by viewModel.systemPrompt.collectAsStateWithLifecycle()
    val chatMessages by viewModel.chatHistory.collectAsStateWithLifecycle()
    val temperature by viewModel.temperature.collectAsStateWithLifecycle()
    val maxTokens by viewModel.maxTokens.collectAsStateWithLifecycle()
    
    val cloudState by viewModel.cloudState.collectAsStateWithLifecycle()
    val cloudGeneratedText by viewModel.cloudGeneratedText.collectAsStateWithLifecycle()

    // Флаг загрузки модели из ViewModel
    val isModelLoaded by viewModel.isModelLoaded.collectAsStateWithLifecycle()

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

    var currentMode by remember { mutableStateOf(AIMode.NEUTRAL) }

    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val scrollState = rememberScrollState()

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

    LaunchedEffect(chatMessages.size, generatedText.length) {
        scrollState.scrollTo(scrollState.maxValue)
    }

    LaunchedEffect(state) {
        if (state is GenerationState.ModelLoaded) {
            try {
                focusRequester.requestFocus()
                keyboardController?.show()
            } catch (_: Exception) {}
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
                    currentMode = AIMode.LOCAL
                }
            },
            onDismiss = { showModelDialog = false }
        )
    }

    if (showCloudDialog) {
        CloudAIDialog(
            apiUrl = cloudApiUrl,
            authKey = cloudAuthKey,
            isGigaChat = cloudIsGigaChat,
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
                showCloudDialog = false
                currentMode = AIMode.CLOUD
            },
            onClear = {
                viewModel.clearCloudConfig()
                cloudApiUrl = "https://gigachat.devices.sberbank.ru/api/v1/chat/completions"
                cloudAuthKey = ""
                cloudIsGigaChat = true
                showCloudDialog = false
                if (currentMode == AIMode.CLOUD) currentMode = AIMode.NEUTRAL
            },
            onDismiss = { showCloudDialog = false },
            onGenerateToken = {
                isGeneratingToken = true
                viewModel.generateCloudToken { success ->
                    isGeneratingToken = false
                }
            },
            isGeneratingToken = isGeneratingToken
        )
    }

    if (showHelpDialog) {
        HelpDialog(onDismiss = { showHelpDialog = false })
    }

    if (showMemoryEditor) {
        val currentPassword = remember { mutableStateOf("") }
        val newPassword = remember { mutableStateOf("") }
        val isPasswordCorrect = remember { mutableStateOf(false) }
        val showChangePassword = remember { mutableStateOf(false) }
        val passwordError = remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { showMemoryEditor = false },
            title = {
                Text(
                    text = "🧠 Доступ к мозгу",
                    style = MaterialTheme.typography.titleLarge,
                    color = DarkText
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(500.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (!isPasswordCorrect.value) {
                        OutlinedTextField(
                            value = currentPassword.value,
                            onValueChange = { currentPassword.value = it },
                            label = { Text("Введите пароль", color = DarkText) },
                            // ПОДСКАЗКА УБРАНА
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
                        if (passwordError.value != null) {
                            Text(
                                text = passwordError.value!!,
                                color = Color.Red,
                                fontSize = 12.sp
                            )
                        }
                        Button(
                            onClick = {
                                if (viewModel.checkPassword(currentPassword.value)) {
                                    isPasswordCorrect.value = true
                                    passwordError.value = null
                                } else {
                                    passwordError.value = "Неверный пароль"
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AccentColor),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Войти", color = DarkText)
                        }
                    } else {
                        Text(
                            text = "Доступ разрешён. Вы можете редактировать базу знаний или изменить пароль.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = DarkText
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        OutlinedTextField(
                            value = memoryEditText,
                            onValueChange = { memoryEditText = it },
                            placeholder = { Text("Вставьте сюда данные для мозга...", color = DarkText.copy(alpha = 0.5f)) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(400.dp), // УВЕЛИЧЕНО С 200 ДО 400
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
                        
                        TextButton(
                            onClick = { showChangePassword.value = !showChangePassword.value },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("Изменить пароль", color = AccentColor)
                        }
                        
                        if (showChangePassword.value) {
                            OutlinedTextField(
                                value = newPassword.value,
                                onValueChange = { newPassword.value = it },
                                label = { Text("Новый пароль", color = DarkText) },
                                placeholder = { Text("Введите новый пароль", color = DarkText.copy(alpha = 0.5f)) },
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
                                onClick = {
                                    if (newPassword.value.isNotBlank()) {
                                        viewModel.savePassword(newPassword.value)
                                        showChangePassword.value = false
                                        passwordError.value = "Пароль изменён"
                                    } else {
                                        passwordError.value = "Пароль не может быть пустым"
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = AccentColor),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Сохранить новый пароль", color = DarkText)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                if (isPasswordCorrect.value) {
                    Button(
                        onClick = {
                            viewModel.overwriteLongTermMemory(memoryEditText)
                            showMemoryEditor = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentColor)
                    ) {
                        Text("Сохранить мозг", color = DarkText)
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        if (isPasswordCorrect.value) {
                            showMemoryEditor = false
                        } else {
                            showMemoryEditor = false
                        }
                    }
                ) {
                    Text(if (isPasswordCorrect.value) "Закрыть" else "Отмена", color = DarkText)
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
        // Верхняя панель с переключателем
        TopBarWithSwitch(
            currentMode = currentMode,
            onModeChange = { newMode ->
                currentMode = newMode
                when (newMode) {
                    AIMode.LOCAL -> {
                        // Используем флаг isModelLoaded вместо проверки state
                        if (!isModelLoaded) {
                            showModelDialog = true
                        }
                    }
                    AIMode.CLOUD -> {
                        if (!viewModel.isCloudConfigured()) {
                            showCloudDialog = true
                        }
                    }
                    AIMode.NEUTRAL -> {
                        // Выгружаем модель при переходе в нейтральный режим
                        viewModel.releaseModel()
                    }
                }
            }
        )

        ControlPanel(
            onMemoryClick = {
                memoryEditText = viewModel.readFromLongTermMemory()
                showMemoryEditor = true
            },
            onSettingsClick = { showSettings = !showSettings },
            onPromptSettingsClick = { showPromptSettings = !showPromptSettings },
            onCloudClick = { showCloudDialog = true },
            onHelpClick = { showHelpDialog = true }
        )

        if (showSettings) {
            SettingsPanel(
                temperature = tempTemperature,
                onTemperatureChange = { tempTemperature = it },
                maxTokens = maxTokens,
                onMaxTokensChange = { viewModel.updateMaxTokens(it) },
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
            currentModel = currentModelPath,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            // Нижний слой — матрица
            AndroidView(
                factory = { context ->
                    MatrixChatBackground(context)
                },
                modifier = Modifier.matchParentSize()
            )

            // Верхний слой — чат (прозрачный)
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

        CloudStatusBar(
            state = cloudState,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )

        if (imagePath != null) {
            ImagePreview(imagePath = imagePath)
        }

        PromptInput(
            prompt = promptInput,
            onPromptChange = { promptInput = it },
            onGenerate = {
                if (promptInput.isNotBlank()) {
                    keyboardController?.hide()
                    when (currentMode) {
                        AIMode.LOCAL -> {
                            if (isModelLoaded) {
                                viewModel.generateLocal(promptInput, imagePath)
                            } else {
                                showModelDialog = true
                            }
                        }
                        AIMode.CLOUD -> {
                            if (viewModel.isCloudConfigured()) {
                                viewModel.generateCloud(promptInput)
                            } else {
                                showCloudDialog = true
                            }
                        }
                        AIMode.NEUTRAL -> {
                            viewModel.appendSystemMessage("Выберите режим работы: локальный или облачный ИИ")
                        }
                    }
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
            modifier = Modifier.padding(16.dp)
        )
    }
}

// === Обновленная верхняя панель с переключателем ===
@Composable
private fun TopBarWithSwitch(
    currentMode: AIMode,
    onModeChange: (AIMode) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        shape = MaterialTheme.shapes.medium,
        border = BorderStroke(1.dp, BorderGray),
        colors = CardDefaults.cardColors(containerColor = SurfaceGray)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Логотип слева
            Image(
                painter = painterResource(id = R.mipmap.ic_launcher),
                contentDescription = "Лого",
                contentScale = ContentScale.FillBounds,
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(16.dp))
            )

            // Центральная колонка с текстом и переключателем
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Меч Правды v2.0",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = DarkText,
                    fontSize = 16.sp,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Переключатель режимов — по центру
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ModeButton(
                        label = "Local",
                        isSelected = currentMode == AIMode.LOCAL,
                        onClick = { onModeChange(AIMode.LOCAL) }
                    )
                    ModeButton(
                        label = "Neutral",
                        isSelected = currentMode == AIMode.NEUTRAL,
                        onClick = { onModeChange(AIMode.NEUTRAL) }
                    )
                    ModeButton(
                        label = "Cloud",
                        isSelected = currentMode == AIMode.CLOUD,
                        onClick = { onModeChange(AIMode.CLOUD) }
                    )
                }
            }
        }
    }
}

// Уменьшенная кнопка переключателя (увеличена на 30%)
@Composable
private fun ModeButton(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(42.dp, 26.dp) // Увеличено на ~30% (было 32x20)
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
            fontSize = 10.sp, // Увеличен до 10.sp
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

// === Остальные компоненты без изменений ===

@Composable
private fun ControlPanel(
    onMemoryClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onPromptSettingsClick: () -> Unit,
    onCloudClick: () -> Unit,
    onHelpClick: () -> Unit
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
                icon = Icons.Default.Cloud,
                label = "облачный ии",
                onClick = onCloudClick
            )
            IconButtonWithLabel(
                icon = Icons.Default.Info,
                label = "справка",
                onClick = onHelpClick
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
                    onClick = onSave,
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
    onApiUrlChange: (String) -> Unit,
    onAuthKeyChange: (String) -> Unit,
    onIsGigaChatChange: (Boolean) -> Unit,
    onSave: () -> Unit,
    onClear: () -> Unit,
    onDismiss: () -> Unit,
    onGenerateToken: () -> Unit,
    isGeneratingToken: Boolean
) {
    var connectionStatus by remember { mutableStateOf<ConnectionStatus?>(null) }
    var isCheckingConnection by remember { mutableStateOf(false) }

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
                
                // НОВАЯ КОМПОНОВКА: кнопки "Получить" и "Сохранить" на одном уровне
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = {
                            isCheckingConnection = true
                            onGenerateToken()
                        },
                        enabled = !isGeneratingToken && authKey.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (authKey.isNotBlank()) AccentColor else BorderGray
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        if (isGeneratingToken || isCheckingConnection) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = DarkText,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Проверка...", color = DarkText)
                        } else {
                            Text("Получить", color = DarkText) // БЕЗ ЗНАЧКА КЛЮЧА
                        }
                    }

                    Button(
                        onClick = {
                            connectionStatus = ConnectionStatus.Success
                            onSave()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentColor),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Сохранить", color = DarkText)
                    }
                }

                // СТАТУС-БАР ПОД КНОПКАМИ (по центру)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .background(
                            color = when (connectionStatus) {
                                ConnectionStatus.Success -> Color(0xFF4CAF50).copy(alpha = 0.2f)
                                ConnectionStatus.Error -> Color(0xFFF44336).copy(alpha = 0.2f)
                                else -> SurfaceGray
                            },
                            shape = RoundedCornerShape(8.dp)
                        )
                        .border(
                            width = 1.dp,
                            color = when (connectionStatus) {
                                ConnectionStatus.Success -> Color(0xFF4CAF50)
                                ConnectionStatus.Error -> Color(0xFFF44336)
                                else -> BorderGray
                            },
                            shape = RoundedCornerShape(8.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        when (connectionStatus) {
                            ConnectionStatus.Success -> {
                                Icon(
                                    imageVector = Icons.Default.Cloud,
                                    contentDescription = null,
                                    tint = Color(0xFF4CAF50)
                                )
                                Text(
                                    text = "Подключено",
                                    color = Color(0xFF4CAF50),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            ConnectionStatus.Error -> {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = null,
                                    tint = Color(0xFFF44336)
                                )
                                Text(
                                    text = "Ошибка",
                                    color = Color(0xFFF44336),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            else -> {
                                Text(
                                    text = "Ожидание",
                                    color = DarkText.copy(alpha = 0.5f),
                                    fontSize = 12.sp
                                )
                            }
                        }
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
            // Кнопка "Сохранить" теперь внутри Row, поэтому здесь она не нужна
        },
        dismissButton = {
            Row {
                TextButton(onClick = onClear) {
                    Text("Очистить", color = DarkText.copy(alpha = 0.6f))
                }
                TextButton(onClick = onDismiss) {
                    Text("Закрыть", color = DarkText)
                }
            }
        }
    )
}

// Статус подключения
enum class ConnectionStatus {
    Success,
    Error
}

@Composable
private fun HelpDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
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
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = AccentColor)
            ) {
                Text("Понятно", color = DarkText)
            }
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
private fun StatusBar(state: GenerationState, currentModel: String?, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (state) {
                is GenerationState.Error -> AccentColor.copy(alpha = 0.15f)
                is GenerationState.Generating -> AccentColor.copy(alpha = 0.15f)
                is GenerationState.AnalyzingImage -> AccentColor.copy(alpha = 0.15f)
                is GenerationState.LoadingModel -> BorderGray.copy(alpha = 0.3f)
                else -> SurfaceGray
            }
        ),
        border = BorderStroke(1.dp, BorderGray)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                when (state) {
                    is GenerationState.Idle -> {
                        Text(if (currentModel == null) "Выберите модель" else "Готов", color = if (currentModel == null) DarkText.copy(alpha = 0.5f) else AccentColor, fontSize = 8.sp)
                    }
                    is GenerationState.LoadingModel -> {
                        CircularProgressIndicator(modifier = Modifier.size(12.dp), color = AccentColor, strokeWidth = 2.dp)
                        Text("Загрузка...", color = DarkText, fontSize = 8.sp)
                    }
                    is GenerationState.ModelLoaded -> {
                        val modelName = state.path.substringAfterLast("/")
                            .replace(Regex("^primary%3AModels%"), "")
                            .replace(Regex("^primary:Models:"), "")
                        val displayName = modelName.substringBeforeLast(".")
                        Text("✓ $displayName", color = AccentColor, fontSize = 8.sp)
                    }
                    is GenerationState.AnalyzingImage -> {
                        CircularProgressIndicator(modifier = Modifier.size(12.dp), color = AccentColor, strokeWidth = 2.dp)
                        Text("🧐 Анализ...", color = DarkText, fontSize = 8.sp)
                    }
                    is GenerationState.Generating -> {
                        CircularProgressIndicator(modifier = Modifier.size(12.dp), color = AccentColor, strokeWidth = 2.dp)
                        val label = if (state.tokensGenerated == 0) "Думаю..." else "${state.tokensGenerated} т."
                        Text(label, color = DarkText, fontSize = 8.sp)
                    }
                    is GenerationState.Completed -> {
                        Text("✓ ${state.tokenCount} т. ${state.durationMs}мс", color = AccentColor, fontSize = 8.sp)
                    }
                    is GenerationState.Error -> {
                        Text("⚠ ${state.message}", color = AccentColor, fontSize = 8.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun CloudStatusBar(state: CloudAIState, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (state) {
                is CloudAIState.Error -> AccentColor.copy(alpha = 0.15f)
                is CloudAIState.Generating -> AccentColor.copy(alpha = 0.15f)
                is CloudAIState.Ready -> SurfaceGray
                else -> SurfaceGray
            }
        ),
        border = BorderStroke(1.dp, BorderGray)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                when (state) {
                    is CloudAIState.Idle -> Text("☁️ не настроен", color = DarkText.copy(alpha = 0.5f), fontSize = 8.sp)
                    is CloudAIState.Ready -> Text("☁️ готов (${state.modelId})", color = AccentColor, fontSize = 8.sp)
                    is CloudAIState.Generating -> {
                        CircularProgressIndicator(modifier = Modifier.size(12.dp), color = AccentColor, strokeWidth = 2.dp)
                        val label = if (state.tokensGenerated == 0) "☁️ Думаю..." else "${state.tokensGenerated} т."
                        Text(label, color = DarkText, fontSize = 8.sp)
                    }
                    is CloudAIState.Completed -> Text("☁️ ${state.tokenCount} т. ${state.durationMs}мс", color = AccentColor, fontSize = 8.sp)
                    is CloudAIState.Error -> Text("⚠️ ${state.message}", color = AccentColor, fontSize = 8.sp)
                }
            }
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
                    if (currentModelPath != null) Text(text = "[Файл модели]", style = MaterialTheme.typography.bodySmall, color = DarkText.copy(alpha = 0.6f))
                    Button(onClick = onPickModel, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = BorderGray)) {
                        Text(if (currentModelPath == null) "Выбрать модель" else "Изменить модель", color = DarkText)
                    }
                }
                Column(modifier = Modifier.padding(vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("(опционально)", style = MaterialTheme.typography.bodySmall, color = DarkText.copy(alpha = 0.6f))
                    Text("Мультимодальный проектор (mmproj)", color = DarkText)
                    if (mmprojPath != null) Text(text = "[Файл проектора]", style = MaterialTheme.typography.bodySmall, color = DarkText.copy(alpha = 0.6f))
                    Button(onClick = onPickMmproj, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = BorderGray)) {
                        Text(if (mmprojPath == null) "Выбрать проектор" else "Изменить проектор", color = DarkText)
                    }
                }
                Button(onClick = onLoad, enabled = currentModelPath != null, modifier = Modifier.fillMaxWidth().padding(top = 8.dp), colors = ButtonDefaults.buttonColors(containerColor = AccentColor)) {
                    Text("Запустить нейросеть", color = DarkText)
                }
                TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) { Text("Отмена", color = DarkText) }
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
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onPickImage, enabled = enabled && !isGenerating) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Добавить изображение", tint = if (enabled && !isGenerating) AccentColor else BorderGray)
        }

        OutlinedTextField(
            value = prompt,
            onValueChange = onPromptChange,
            modifier = Modifier.weight(1f).focusRequester(focusRequester),
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

        Column(verticalArrangement = Arrangement.spacedBy(4.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            if (isGenerating) {
                IconButton(onClick = onAbort, modifier = Modifier.size(48.dp), colors = IconButtonDefaults.iconButtonColors(containerColor = SurfaceGray)) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Стоп", tint = DarkText)
                }
            } else {
                IconButton(onClick = onGenerate, enabled = enabled && prompt.isNotBlank(), modifier = Modifier.size(48.dp), colors = IconButtonDefaults.iconButtonColors(containerColor = if (enabled && prompt.isNotBlank()) SurfaceGray else BorderGray)) {
                    Icon(imageVector = Icons.Default.ArrowUpward, contentDescription = "Отправить", tint = if (enabled && prompt.isNotBlank()) DarkText else DarkText.copy(alpha = 0.4f))
                }
            }

            IconButton(onClick = onClearChat, enabled = true, modifier = Modifier.size(48.dp), colors = IconButtonDefaults.iconButtonColors(containerColor = SurfaceGray)) {
                Icon(imageVector = Icons.Default.Delete, contentDescription = "Очистить чат", tint = DarkText)
            }
        }
    }
}
