package org.nehuatl.sample

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch

private sealed class MessengerTab {
    object Chats : MessengerTab()
    object Users : MessengerTab()
}

@Composable
fun MessengerScreen(
    viewModel: MainViewModel,
    colors: AppColors,
    isDarkTheme: Boolean,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Сначала init(), потом ВСЁ остальное. Без этого loadMyName
    // вызывает auth.currentUser на lateinit auth, который ещё не создан.
    var firebaseReady by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        FirebaseChatManager.init()
        FirebaseChatManager.setOnline(true)
        firebaseReady = true
    }
    DisposableEffect(Unit) {
        onDispose {
            FirebaseChatManager.setOnline(false)
            FirebaseChatManager.removeAllListeners()
        }
    }

    var myName by remember { mutableStateOf<String?>(null) }
    var showNameDialog by remember { mutableStateOf(false) }
    var nameInput by remember { mutableStateOf("") }

    LaunchedEffect(firebaseReady) {
        if (!firebaseReady) return@LaunchedEffect
        FirebaseChatManager.loadMyName { name ->
            if (name.isNullOrBlank()) {
                showNameDialog = true
            } else {
                myName = name
                FirebaseChatManager.cacheMyName(name)
            }
        }
    }

    if (showNameDialog) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text("Ваше имя", color = colors.text) },
            text = {
                OutlinedTextField(
                    value = nameInput,
                    onValueChange = { nameInput = it.take(30) },
                    placeholder = { Text("Как вас зовут?", color = colors.text.copy(alpha = 0.5f)) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = colors.text,
                        unfocusedTextColor = colors.text,
                        focusedBorderColor = colors.accent,
                        unfocusedBorderColor = colors.borderGray,
                        cursorColor = colors.accent
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val name = nameInput.trim()
                        if (name.isNotEmpty()) {
                            FirebaseChatManager.saveMyName(name) {
                                myName = name
                                FirebaseChatManager.cacheMyName(name)
                                showNameDialog = false
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.accent)
                ) { Text("Сохранить", color = colors.background) }
            },
            containerColor = colors.surfaceGray
        )
    }

    var currentTab by remember { mutableStateOf<MessengerTab>(MessengerTab.Chats) }
    var openedChatUid by remember { mutableStateOf<String?>(null) }
    var openedChatName by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Шапка
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.surfaceGray)
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (openedChatUid != null) {
                    IconButton(onClick = {
                        openedChatUid = null
                        openedChatName = null
                    }) {
                        Icon(Icons.Default.ArrowBack, "Назад", tint = colors.accent)
                    }
                    Text(
                        text = openedChatName ?: "Чат",
                        color = colors.text,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    Text(
                        text = "💬 Чат",
                        color = colors.accent,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.weight(1f)
                    )
                }
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, "Закрыть", tint = colors.accent)
                }
            }

            if (openedChatUid != null) {
                ChatConversation(
                    myUid = FirebaseChatManager.getMyUid(),
                    otherUid = openedChatUid!!,
                    otherName = openedChatName ?: "",
                    colors = colors,
                    modifier = Modifier.weight(1f)
                )
            } else {
                // Вкладки
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colors.surfaceGray),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    TabButton("Чаты", currentTab is MessengerTab.Chats, colors) {
                        currentTab = MessengerTab.Chats
                    }
                    TabButton("Пользователи", currentTab is MessengerTab.Users, colors) {
                        currentTab = MessengerTab.Users
                    }
                }

                when (currentTab) {
                    is MessengerTab.Chats -> ChatsList(
                        colors = colors,
                        onOpenPublic = {
                            openedChatUid = "PUBLIC"
                            openedChatName = "Общий чат"
                        },
                        onOpenPrivate = { uid, name ->
                            openedChatUid = uid
                            openedChatName = name
                        },
                        modifier = Modifier.weight(1f)
                    )
                    is MessengerTab.Users -> UsersList(
                        colors = colors,
                        onOpenPrivate = { uid, name ->
                            openedChatUid = uid
                            openedChatName = name
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun TabButton(text: String, selected: Boolean, colors: AppColors, onClick: () -> Unit) {
    Text(
        text = text,
        color = if (selected) colors.accent else colors.text.copy(alpha = 0.6f),
        fontSize = 12.sp,
        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
        modifier = Modifier
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 8.dp)
    )
}

@Composable
private fun ChatsList(
    colors: AppColors,
    onOpenPublic: () -> Unit,
    onOpenPrivate: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var chats by remember { mutableStateOf<List<FirebaseChatManager.ChatPreview>>(emptyList()) }

    LaunchedEffect(Unit) {
        FirebaseChatManager.listenMyChats { list -> chats = list }
    }

    LazyColumn(modifier = modifier.fillMaxWidth()) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenPublic() }
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(colors.accent, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🌐", fontSize = 20.sp)
                }
                Spacer(Modifier.width(12.dp))
                Text("Общий чат", color = colors.text, fontSize = 14.sp)
            }
        }
        items(chats) { chat ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenPrivate(chat.otherUid, chat.otherName) }
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(colors.borderGray, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(chat.otherName.take(1).uppercase(), color = colors.background, fontSize = 16.sp)
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(chat.otherName, color = colors.text, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text(
                        chat.lastMessage,
                        color = colors.text.copy(alpha = 0.6f),
                        fontSize = 11.sp,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
private fun UsersList(
    colors: AppColors,
    onOpenPrivate: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var users by remember { mutableStateOf<List<ChatUser>>(emptyList()) }
    val myUid = FirebaseChatManager.getMyUid()

    LaunchedEffect(Unit) {
        FirebaseChatManager.listenUsers { list ->
            users = list.filter { it.uid != myUid }
        }
    }

    LazyColumn(modifier = modifier.fillMaxWidth()) {
        items(users) { user ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenPrivate(user.uid, user.name) }
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(if (user.online) colors.green else colors.borderGray, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(user.name.take(1).uppercase(), color = colors.background, fontSize = 16.sp)
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(user.name, color = colors.text, fontSize = 14.sp)
                    Text(
                        if (user.online) "онлайн" else "офлайн",
                        color = if (user.online) colors.green else colors.text.copy(alpha = 0.5f),
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun ChatConversation(
    myUid: String?,
    otherUid: String,
    otherName: String,
    colors: AppColors,
    modifier: Modifier = Modifier
) {
    var messages by remember { mutableStateOf<List<ChatMessageItem>>(emptyList()) }
    var input by remember { mutableStateOf("") }
    var pendingImageUri by remember { mutableStateOf<Uri?>(null) }
    var otherTyping by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val context = LocalContext.current
    val typingJob = remember { mutableStateOf<kotlinx.coroutines.Job?>(null) }
    val scope = rememberCoroutineScope()

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) pendingImageUri = uri
    }

    LaunchedEffect(otherUid) {
        messages = emptyList()
        if (otherUid == "PUBLIC") {
            FirebaseChatManager.listenPublicMessages { item ->
                messages = messages + item
            }
        } else {
            FirebaseChatManager.listenPrivateMessages(otherUid) { item ->
                messages = messages + item
            }
            val listener = FirebaseChatManager.listenTyping(otherUid) { typing ->
                otherTyping = typing
            }
        }
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            itemsIndexed(messages) { index, msg ->
                val currentDay = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(msg.timestamp))
                val prevDay = if (index > 0) {
                    SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(messages[index - 1].timestamp))
                } else null
                if (currentDay != prevDay) {
                    val dayLabel = when (currentDay) {
                        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) -> "Сегодня"
                        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(System.currentTimeMillis() - 86400000L)) -> "Вчера"
                        else -> SimpleDateFormat("d MMMM", Locale("ru")).format(Date(msg.timestamp))
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            dayLabel,
                            color = colors.text.copy(alpha = 0.5f),
                            fontSize = 10.sp,
                            modifier = Modifier
                                .background(colors.surfaceGray, RoundedCornerShape(10.dp))
                                .padding(horizontal = 10.dp, vertical = 3.dp)
                        )
                    }
                }
                val isMine = msg.senderId == myUid
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start
                ) {
                    Column(
                        modifier = Modifier
                            .widthIn(max = 260.dp)
                            .background(
                                if (isMine) colors.accent.copy(alpha = 0.25f) else colors.surfaceGray,
                                RoundedCornerShape(12.dp)
                            )
                            .padding(8.dp)
                    ) {
                        if (!isMine) {
                            Text(msg.senderName, color = colors.accent, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                        if (msg.imageUrl != null) {
                            AsyncImage(
                                model = msg.imageUrl,
                                contentDescription = "Картинка",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 200.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Fit
                            )
                            Spacer(Modifier.height(4.dp))
                        }
                        if (msg.text.isNotBlank()) {
                            Text(msg.text, color = colors.text, fontSize = 12.sp)
                        }
                        val time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(msg.timestamp))
                        Text(time, color = colors.text.copy(alpha = 0.4f), fontSize = 8.sp, modifier = Modifier.align(Alignment.End))
                    }
                }
            }
        }

        if (otherTyping && otherUid != "PUBLIC") {
            Text(
                "$otherName печатает...",
                color = colors.text.copy(alpha = 0.6f),
                fontSize = 10.sp,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
            )
        }

        if (pendingImageUri != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.surfaceGray)
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("📷 Картинка прикреплена", color = colors.text, fontSize = 11.sp, modifier = Modifier.weight(1f))
                IconButton(onClick = { pendingImageUri = null }) {
                    Icon(Icons.Default.Close, "Убрать", tint = colors.accent)
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.surfaceGray)
                .padding(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { imagePicker.launch("image/*") }) {
                Icon(Icons.Default.Add, "Картинка", tint = colors.accent)
            }
            OutlinedTextField(
                value = input,
                onValueChange = { newValue ->
                    input = newValue
                    if (otherUid != "PUBLIC") {
                        // Печатает — ставим true, через 2 секунды без ввода — false
                        typingJob.value?.cancel()
                        FirebaseChatManager.setTyping(otherUid, true)
                        typingJob.value = scope.launch {
                            kotlinx.coroutines.delay(2000)
                            FirebaseChatManager.setTyping(otherUid, false)
                        }
                    }
                },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Сообщение...", color = colors.text.copy(alpha = 0.5f), fontSize = 12.sp) },
                maxLines = 3,
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
            IconButton(onClick = {
                val text = input.trim()
                val uri = pendingImageUri
                if (text.isEmpty() && uri == null) return@IconButton
                input = ""
                pendingImageUri = null
                if (otherUid != "PUBLIC") FirebaseChatManager.setTyping(otherUid, false)
                if (uri != null) {
                    FirebaseChatManager.uploadImage(uri) { url ->
                        if (url != null) {
                            if (otherUid == "PUBLIC") FirebaseChatManager.sendPublicMessage(text, url)
                            else FirebaseChatManager.sendPrivateMessage(otherUid, text, url)
                        }
                    }
                } else {
                    if (otherUid == "PUBLIC") FirebaseChatManager.sendPublicMessage(text)
                    else FirebaseChatManager.sendPrivateMessage(otherUid, text)
                }
            }) {
                Icon(Icons.Default.ArrowUpward, "Отправить", tint = colors.accent)
            }
        }
    }
}
