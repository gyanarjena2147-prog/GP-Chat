package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.SentimentSatisfied
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Headset
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Chat
import com.example.data.Message
import com.example.data.User
import com.example.ui.theme.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDetailScreen(
    viewModel: ChatViewModel,
    chatId: Long,
    onBackClick: () -> Unit
) {
    var textState by remember { mutableStateOf("") }
    var showAttachmentMenu by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val scrollState = rememberLazyListState()

    val messages by viewModel.activeChatMessages.collectAsState()
    val typingList by viewModel.typingIndicators.collectAsState()
    val usersList by viewModel.allUsers.collectAsState()
    val chatsList by viewModel.chats.collectAsState()

    // Find current active chat and user metadata
    val currentChat = chatsList.find { it.id == chatId }
    val activeContact = usersList.find { it.id == chatId } // Direct chats use userId as chatId

    val isGroup = currentChat?.isGroup == true
    val chatName = currentChat?.name ?: "Chat"
    val isOnline = activeContact?.isOnline == true
    val typingUser = typingList[chatId]

    // Scroll to bottom when new messages arrive
    LaunchedEffect(messages.size, typingUser) {
        if (messages.isNotEmpty()) {
            scrollState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable {
                            // Can click profile to view details
                        }
                    ) {
                        // Avatar
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(if (isGroup) WaTealGreenLight else WaTealGreen),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (isGroup) "👥" else chatName.take(1).uppercase(),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = chatName,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = when {
                                    typingUser != null -> "$typingUser typing..."
                                    isGroup -> "tap for group info"
                                    isOnline -> "Online"
                                    else -> activeContact?.lastSeen ?: "offline"
                                },
                                fontSize = 11.sp,
                                color = if (typingUser != null) WaAccentGreen else Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.selectChat(null)
                        onBackClick()
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    val showCiphertext by viewModel.showCiphertext.collectAsState()
                    IconButton(onClick = { viewModel.toggleCiphertext() }) {
                        Icon(
                            imageVector = if (showCiphertext) Icons.Default.LockOpen else Icons.Default.Lock,
                            contentDescription = "Toggle Ciphertext",
                            tint = if (showCiphertext) WaAccentGreen else Color.White
                        )
                    }

                    if (!isGroup && activeContact != null) {
                        IconButton(onClick = { viewModel.startCall(activeContact, isVideo = true) }) {
                            Icon(Icons.Default.Videocam, contentDescription = "Video Call", tint = Color.White)
                        }
                        IconButton(onClick = { viewModel.startCall(activeContact, isVideo = false) }) {
                            Icon(Icons.Default.Call, contentDescription = "Voice Call", tint = Color.White)
                        }
                    } else {
                        IconButton(onClick = { /* Group call simulation */ }) {
                            Icon(Icons.Default.Group, contentDescription = "Group", tint = Color.White)
                        }
                    }
                    IconButton(onClick = { /* Details */ }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More Options", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = WaTealGreen)
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(if (MaterialTheme.colorScheme.primary == WaAccentGreen) Color(0xFF0B141A) else Color(0xFFE5DDD5))
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Scrollable Chat Window
                LazyColumn(
                    state = scrollState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    item {
                        // Encryption Badge Info
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFFFE082).copy(alpha = 0.25f))
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "🔒 Messages are secured with end-to-end encryption. No one outside of this chat can read them.",
                                    fontSize = 11.sp,
                                    color = Color(0xFFE65100),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    lineHeight = 14.sp
                                )
                            }
                        }
                    }

                    items(messages) { message ->
                        val isMe = message.senderId == 0L
                        MessageBubbleRow(message = message, isMe = isMe, viewModel = viewModel)
                        Spacer(modifier = Modifier.height(6.dp))
                    }

                    if (typingUser != null) {
                        item {
                            TypingBubbleRow(typingUser = typingUser)
                            Spacer(modifier = Modifier.height(6.dp))
                        }
                    }
                }

                // Attachments choosers
                AnimatedVisibility(
                    visible = showAttachmentMenu,
                    enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                    exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
                ) {
                    AttachmentChooserMenu(
                        onSelectFile = { type, name, size, mediaUrl ->
                            viewModel.sendMessage(
                                chatId = chatId,
                                text = name,
                                type = type,
                                fileName = name,
                                fileSize = size,
                                mediaUrl = mediaUrl
                            )
                            showAttachmentMenu = false
                        }
                    )
                }

                // Input Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left Input field pill
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(24.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = {
                            // Emojis mock input
                            textState += "😃"
                        }) {
                            Icon(Icons.Default.SentimentSatisfied, contentDescription = "Emojis", tint = Color.Gray)
                        }

                        TextField(
                            value = textState,
                            onValueChange = { textState = it },
                            placeholder = { Text("Message", color = Color.Gray) },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                disabledContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            modifier = Modifier.weight(1f)
                        )

                        IconButton(onClick = { showAttachmentMenu = !showAttachmentMenu }) {
                            Icon(Icons.Default.AttachFile, contentDescription = "Attach", tint = Color.Gray)
                        }
                        IconButton(onClick = {
                            // Quick Camera send photo mock
                            viewModel.sendMessage(
                                chatId = chatId,
                                text = "📷 Snapshot.jpg",
                                type = "IMAGE",
                                mediaUrl = "https://images.unsplash.com/photo-1506744038136-46273834b3fb?w=500"
                            )
                        }) {
                            Icon(Icons.Default.PhotoCamera, contentDescription = "Camera", tint = Color.Gray)
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Right action floating button
                    FloatingActionButton(
                        onClick = {
                            if (textState.isNotBlank()) {
                                viewModel.sendMessage(chatId, textState)
                                textState = ""
                            } else {
                                // Send simulated Voice Message
                                viewModel.sendMessage(
                                    chatId = chatId,
                                    text = "🎤 Voice Note (0:12)",
                                    type = "VOICE",
                                    mediaUrl = "voice_recording_01"
                                )
                            }
                        },
                        containerColor = WaTealGreenLight,
                        contentColor = Color.White,
                        shape = CircleShape,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = if (textState.isNotBlank()) Icons.Default.Send else Icons.Default.Mic,
                            contentDescription = "Send"
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MessageBubbleRow(message: Message, isMe: Boolean, viewModel: ChatViewModel) {
    val bubbleBg = if (isMe) {
        if (MaterialTheme.colorScheme.primary == WaAccentGreen) WaDarkBubbleOutgoing else WaLightBubbleOutgoing
    } else {
        if (MaterialTheme.colorScheme.primary == WaAccentGreen) WaDarkBubbleIncoming else WaLightBubbleIncoming
    }

    val bubbleAlignment = if (isMe) Alignment.CenterEnd else Alignment.CenterStart

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = bubbleAlignment
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = bubbleBg),
            shape = RoundedCornerShape(
                topStart = 12.dp,
                topEnd = 12.dp,
                bottomStart = if (isMe) 12.dp else 0.dp,
                bottomEnd = if (isMe) 0.dp else 12.dp
            ),
            modifier = Modifier.widthIn(max = 290.dp)
        ) {
            Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                // Group sender label
                if (!isMe && message.senderId != 0L && message.senderName != "System") {
                    Text(
                        text = message.senderName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = WaTealGreenLight,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }

                // Render Content by Type
                when (message.type) {
                    "IMAGE" -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.DarkGray),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("📸 PHOTO ATTACHMENT", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = message.text, fontSize = 14.sp)
                    }
                    "DOCUMENT" -> {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.Gray.copy(alpha = 0.15f))
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Description, contentDescription = "PDF", tint = Color.Red, modifier = Modifier.size(36.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(message.fileName ?: "Document.pdf", fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 1)
                                Text(message.fileSize ?: "Unknown size", fontSize = 11.sp, color = Color.Gray)
                            }
                        }
                    }
                    "VOICE" -> {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = "Play", tint = WaTealGreenLight, modifier = Modifier.size(28.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            // Draw dynamic lines symbolizing voice waves
                            Row(
                                modifier = Modifier.weight(1f).height(16.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                repeat(15) {
                                    val height = (5..16).random().dp
                                    Box(modifier = Modifier.width(2.dp).height(height).background(Color.Gray))
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("0:12", fontSize = 11.sp, color = Color.Gray)
                        }
                    }
                    else -> {
                        // Standard TEXT Bubble
                        val isCipher = message.text.startsWith("E2EE:")
                        Text(
                            text = message.text,
                            fontSize = 14.sp,
                            fontFamily = if (isCipher) androidx.compose.ui.text.font.FontFamily.Monospace else androidx.compose.ui.text.font.FontFamily.Default,
                            color = if (isCipher) WaAccentGreen else MaterialTheme.colorScheme.onBackground
                        )
                    }
                }

                // Time and read status indicators at bottom right
                Row(
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(top = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = viewModel.formatTime(message.timestamp),
                        fontSize = 10.sp,
                        color = Color.Gray
                    )
                    if (message.isEncrypted) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Encrypted",
                            tint = WaAccentGreen.copy(alpha = 0.8f),
                            modifier = Modifier.size(10.dp)
                        )
                    }
                    if (isMe) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = when (message.status) {
                                "READ" -> Icons.Default.DoneAll
                                "DELIVERED" -> Icons.Default.DoneAll
                                else -> Icons.Default.Done
                            },
                            contentDescription = message.status,
                            tint = if (message.status == "READ") WaBlueTick else Color.Gray,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TypingBubbleRow(typingUser: String) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.CenterStart
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = if (MaterialTheme.colorScheme.primary == WaAccentGreen) WaDarkBubbleIncoming else WaLightBubbleIncoming),
            shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp, bottomStart = 0.dp, bottomEnd = 12.dp),
            modifier = Modifier.widthIn(max = 140.dp)
        ) {
            Row(
                modifier = Modifier.padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("$typingUser typing", fontSize = 12.sp, color = Color.Gray)
                Spacer(modifier = Modifier.width(6.dp))
                // Simple dots flashing animation placeholder
                Text("...", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = WaAccentGreen)
            }
        }
    }
}

@Composable
fun AttachmentChooserMenu(
    onSelectFile: (String, String, String, String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            AttachmentMenuItem(
                icon = Icons.Default.Description,
                color = Color(0xFF5E35B1),
                label = "Document",
                onClick = { onSelectFile("DOCUMENT", "DesignSpecs_v4.pdf", "4.2 MB", "") }
            )
            AttachmentMenuItem(
                icon = Icons.Default.PhotoLibrary,
                color = Color(0xFF00ACC1),
                label = "Gallery",
                onClick = { onSelectFile("IMAGE", "VacationPhoto.png", "", "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=500") }
            )
            AttachmentMenuItem(
                icon = Icons.Default.Headset,
                color = Color(0xFFFB8C00),
                label = "Audio",
                onClick = { onSelectFile("VOICE", "VoiceRecording.mp3", "1.5 MB", "") }
            )
            AttachmentMenuItem(
                icon = Icons.Default.Place,
                color = Color(0xFF43A047),
                label = "Location",
                onClick = { onSelectFile("TEXT", "📍 Shared Location: San Francisco, CA (37.7749, -122.4194)", "TEXT", "") }
            )
        }
    }
}

@Composable
fun AttachmentMenuItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)
                .background(color),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = label, tint = Color.White, modifier = Modifier.size(24.dp))
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}
