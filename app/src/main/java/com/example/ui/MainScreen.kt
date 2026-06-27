package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.CallReceived
import androidx.compose.material.icons.filled.CallMade
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.Chat
import com.example.data.StatusUpdate
import com.example.data.User
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: ChatViewModel,
    onChatClick: (Long) -> Unit,
    onAdminClick: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Chats, 1: Status, 2: Calls, 3: Settings
    var isCreatingGroup by remember { mutableStateOf(false) }
    var showSearchRow by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    
    val searchVal by viewModel.searchQuery.collectAsState()
    val themeMode by viewModel.appTheme.collectAsState()
    val isSocketConnected by viewModel.isSocketConnected.collectAsState()

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "WhatsApp",
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            // Small socket state light
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(if (isSocketConnected) WaAccentGreen else Color.Gray)
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { showSearchRow = !showSearchRow }) {
                            Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.White)
                        }
                        IconButton(onClick = { viewModel.toggleTheme() }) {
                            Icon(
                                if (themeMode == AppThemeSetting.DARK) Icons.Default.LightMode else Icons.Default.DarkMode,
                                contentDescription = "Theme Toggle",
                                tint = Color.White
                            )
                        }
                        IconButton(onClick = onAdminClick) {
                            Icon(Icons.Default.Build, contentDescription = "Admin Panel", tint = WaAccentGreen)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = WaTealGreen)
                )

                // Tab Bar
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = WaTealGreen,
                    contentColor = Color.White,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = WaAccentGreen
                        )
                    }
                ) {
                    Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }) {
                        Text("CHATS", modifier = Modifier.padding(14.dp), fontWeight = FontWeight.Bold, color = if (selectedTab == 0) Color.White else Color.White.copy(alpha = 0.7f))
                    }
                    Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }) {
                        Text("STATUS", modifier = Modifier.padding(14.dp), fontWeight = FontWeight.Bold, color = if (selectedTab == 1) Color.White else Color.White.copy(alpha = 0.7f))
                    }
                    Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }) {
                        Text("CALLS", modifier = Modifier.padding(14.dp), fontWeight = FontWeight.Bold, color = if (selectedTab == 2) Color.White else Color.White.copy(alpha = 0.7f))
                    }
                    Tab(selected = selectedTab == 3, onClick = { selectedTab = 3 }) {
                        Text("SETTINGS", modifier = Modifier.padding(14.dp), fontWeight = FontWeight.Bold, color = if (selectedTab == 3) Color.White else Color.White.copy(alpha = 0.7f))
                    }
                }

                // Dynamic Search Row
                AnimatedVisibility(
                    visible = showSearchRow,
                    enter = slideInVertically() + fadeIn(),
                    exit = slideOutVertically() + fadeOut()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = searchVal,
                            onValueChange = { viewModel.updateSearchQuery(it) },
                            placeholder = { Text("Search messages, contacts...") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(24.dp),
                            singleLine = true,
                            trailingIcon = {
                                if (searchVal.isNotEmpty()) {
                                    IconButton(onClick = { viewModel.clearSearch() }) {
                                        Icon(Icons.Default.Clear, contentDescription = "Clear Search")
                                    }
                                }
                            }
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            when (selectedTab) {
                0 -> {
                    FloatingActionButton(
                        onClick = { isCreatingGroup = true },
                        containerColor = WaAccentGreen,
                        contentColor = Color.Black
                    ) {
                        Icon(Icons.Default.GroupAdd, contentDescription = "New Group")
                    }
                }
                1 -> {
                    FloatingActionButton(
                        onClick = { 
                            // Add a random status update
                            val quotes = listOf(
                                "Coding until sunset! 🚀💻",
                                "Loving the new Jetpack Compose updates!",
                                "Peace and quiet 🏞️🌾",
                                "E2EE Chat is fully secure and operational.",
                                "A cup of hot coffee solves everything. ☕️"
                            )
                            viewModel.addStatusUpdate(quotes.random())
                        },
                        containerColor = WaAccentGreen,
                        contentColor = Color.Black
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Add Status")
                    }
                }
                2 -> {
                    FloatingActionButton(
                        onClick = {
                            // Dial back the latest caller
                            scope.launch {
                                val users = viewModel.allUsers.value.filter { it.id != 0L }
                                if (users.isNotEmpty()) {
                                    viewModel.startCall(users.random(), isVideo = false)
                                }
                            }
                        },
                        containerColor = WaAccentGreen,
                        contentColor = Color.Black
                    ) {
                        Icon(Icons.Default.Call, contentDescription = "Make Call")
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (searchVal.length >= 2) {
                SearchSection(viewModel = viewModel, onChatClick = onChatClick)
            } else {
                when (selectedTab) {
                    0 -> ChatsTab(viewModel = viewModel, onChatClick = onChatClick)
                    1 -> StatusTab(viewModel = viewModel)
                    2 -> CallsTab(viewModel = viewModel)
                    3 -> SettingsTab(viewModel = viewModel)
                }
            }

            if (isCreatingGroup) {
                CreateGroupDialog(
                    viewModel = viewModel,
                    onDismiss = { isCreatingGroup = false }
                )
            }
        }
    }
}

// --- subtabs implementation ---

@Composable
fun ChatsTab(viewModel: ChatViewModel, onChatClick: (Long) -> Unit) {
    val chatsList by viewModel.chats.collectAsState()
    val typingList by viewModel.typingIndicators.collectAsState()

    if (chatsList.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No active chats yet. Start typing!", color = Color.Gray)
        }
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(chatsList) { chat ->
                ChatItemRow(
                    chat = chat,
                    typingUser = typingList[chat.id],
                    viewModel = viewModel,
                    onClick = { onChatClick(chat.id) }
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.08f), thickness = 0.5.dp)
            }
        }
    }
}

@Composable
fun ChatItemRow(
    chat: Chat,
    typingUser: String?,
    viewModel: ChatViewModel,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar circle
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(if (chat.isGroup) WaTealGreenLight else WaTealGreen),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (chat.isGroup) "👥" else chat.name.take(1).uppercase(),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Name and Message details
        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = chat.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = if (chat.lastMessageTime > 0) viewModel.formatTime(chat.lastMessageTime) else "",
                    fontSize = 11.sp,
                    color = if (chat.unreadCount > 0) WaAccentGreen else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (typingUser != null) {
                    Text(
                        text = "$typingUser is typing...",
                        color = WaAccentGreen,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1
                    )
                } else {
                    Text(
                        text = chat.lastMessageText,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                }

                if (chat.unreadCount > 0) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(WaAccentGreen),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = chat.unreadCount.toString(),
                            color = Color.Black,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatusTab(viewModel: ChatViewModel) {
    val statuses by viewModel.statusUpdates.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            // My Status item
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(WaTealGreenLight),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🧑‍💻", fontSize = 24.sp)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("My Status", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("Tap to add status updates", fontSize = 13.sp, color = Color.Gray)
                }
                IconButton(onClick = { viewModel.addStatusUpdate("On WhatsApp!") }) {
                    Icon(Icons.Default.AddCircle, contentDescription = "Add Status", tint = WaAccentGreen)
                }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.08f))
            Text(
                text = "Recent updates",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = WaTealGreen,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )
        }

        if (statuses.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text("No status updates from friends", color = Color.Gray, fontSize = 14.sp)
                }
            }
        } else {
            // Group status updates by user
            val groupedStatus = statuses.groupBy { it.userId }
            items(groupedStatus.values.toList()) { userUpdates ->
                val latest = userUpdates.first()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.viewStatusGroup(userUpdates) }
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Circle around avatar if unviewed
                    val hasUnviewed = userUpdates.any { !it.isViewed }
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(if (hasUnviewed) WaAccentGreen else Color.LightGray)
                            .padding(2.5.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surface),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (latest.userName.contains("Sarah")) "👩" else if (latest.userName.contains("John")) "👨" else "👤",
                            fontSize = 24.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(latest.userName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(
                            text = latest.statusText,
                            fontSize = 13.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                        )
                    }
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f))
            }
        }
    }
}

@Composable
fun CallsTab(viewModel: ChatViewModel) {
    val logs by viewModel.callLogs.collectAsState()
    val scope = rememberCoroutineScope()

    if (logs.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No call history yet.", color = Color.Gray)
        }
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(logs) { log ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            scope.launch {
                                val user = viewModel.allUsers.value.find { it.id == log.userId }
                                if (user != null) viewModel.startCall(user, log.isVideo)
                            }
                        }
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(WaTealGreen),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(if (log.userName.contains("Sarah")) "👩" else "👨", fontSize = 20.sp)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(log.userName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (log.isIncoming) Icons.Default.CallReceived else Icons.Default.CallMade,
                                contentDescription = "Call Direction",
                                tint = if (log.isMissed) WaCallMissed else WaCallIncoming,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (log.isMissed) "Missed" else if (log.durationSeconds > 0) "Talked for ${viewModel.formatDuration(log.durationSeconds)}" else "Outgoing",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    }
                    IconButton(onClick = {
                        scope.launch {
                            val user = viewModel.allUsers.value.find { it.id == log.userId }
                            if (user != null) viewModel.startCall(user, log.isVideo)
                        }
                    }) {
                        Icon(
                            imageVector = if (log.isVideo) Icons.Default.Videocam else Icons.Default.Call,
                            contentDescription = "Call Back",
                            tint = WaTealGreen
                        )
                    }
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsTab(viewModel: ChatViewModel) {
    val currentUser by viewModel.currentUser.collectAsState()
    var nameInput by remember { mutableStateOf("") }
    var statusInput by remember { mutableStateOf("") }
    var isEditing by remember { mutableStateOf(false) }

    LaunchedEffect(currentUser) {
        currentUser?.let {
            nameInput = it.name
            statusInput = it.statusText
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(WaTealGreenLight),
                contentAlignment = Alignment.Center
            ) {
                Text("🧑‍💻", fontSize = 48.sp)
            }
            Spacer(modifier = Modifier.height(16.dp))

            if (isEditing) {
                OutlinedTextField(
                    value = nameInput,
                    onValueChange = { nameInput = it },
                    label = { Text("Display Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                )
                OutlinedTextField(
                    value = statusInput,
                    onValueChange = { statusInput = it },
                    label = { Text("About status") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                )
                Button(
                    onClick = {
                        viewModel.updateProfile(nameInput, statusInput)
                        isEditing = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = WaAccentGreen),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                ) {
                    Text("Save Changes", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            } else {
                Text(currentUser?.name ?: "User", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Text(currentUser?.statusText ?: "Hey there!", fontSize = 14.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = { isEditing = true },
                    colors = ButtonDefaults.buttonColors(containerColor = WaTealGreen),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Edit Profile", color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.08f))
            Spacer(modifier = Modifier.height(16.dp))

            // Encryption Indicator Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Lock, contentDescription = "Lock", tint = WaTealGreen)
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text("End-to-End Encryption", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = WaTealGreen)
                        Text(
                            text = "All messages are encrypted locally prior to transmission using ${viewModel.encryptionKeyId}. Tap to view public keys.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Extra app settings
            ListItem(
                headlineContent = { Text("Chat Backup") },
                supportingContent = { Text("Auto-backup size: 45.4 MB") },
                leadingContent = { Icon(Icons.Default.CloudUpload, contentDescription = "Backup") },
                trailingContent = { Button(onClick = {}, colors = ButtonDefaults.buttonColors(containerColor = WaTealGreenLight)) { Text("BACKUP", fontSize = 11.sp) } }
            )
            HorizontalDivider(thickness = 0.5.dp)
            ListItem(
                headlineContent = { Text("App Theme") },
                supportingContent = { Text("Tap header toggle to flip light/dark") },
                leadingContent = { Icon(Icons.Default.ColorLens, contentDescription = "Theme") }
            )
        }
    }
}

@Composable
fun SearchSection(viewModel: ChatViewModel, onChatClick: (Long) -> Unit) {
    val results by viewModel.searchResults.collectAsState()
    val query by viewModel.searchQuery.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Search results for \"$query\"",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(16.dp)
        )
        if (results.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                Text("No messages match search query.", color = Color.Gray)
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(results) { msg ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { 
                                viewModel.selectChat(msg.chatId)
                                onChatClick(msg.chatId)
                            }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(WaTealGreen),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(msg.senderName.take(1), color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                Text(msg.senderName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text(viewModel.formatTime(msg.timestamp), fontSize = 11.sp, color = Color.Gray)
                            }
                            Text(msg.text, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
                        }
                    }
                    HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray.copy(alpha = 0.3f))
                }
            }
        }
    }
}

// --- Dialog to create a Group Chat ---
@Composable
fun CreateGroupDialog(
    viewModel: ChatViewModel,
    onDismiss: () -> Unit
) {
    var groupName by remember { mutableStateOf("") }
    val users by viewModel.allUsers.collectAsState()
    val selectedUsers = remember { mutableStateListOf<User>() }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = "New Group Chat",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = WaTealGreen,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                OutlinedTextField(
                    value = groupName,
                    onValueChange = { groupName = it },
                    label = { Text("Group Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text("Add Participants", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)

                LazyColumn(
                    modifier = Modifier
                        .height(180.dp)
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    items(users.filter { it.id != 0L }) { user ->
                        val isSelected = selectedUsers.contains(user)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (isSelected) selectedUsers.remove(user) else selectedUsers.add(user)
                                }
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isSelected,
                                onCheckedChange = {
                                    if (isSelected) selectedUsers.remove(user) else selectedUsers.add(user)
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(user.name)
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("CANCEL", color = Color.Gray)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (groupName.isNotBlank() && selectedUsers.isNotEmpty()) {
                                viewModel.createNewGroupChat(groupName, selectedUsers)
                                onDismiss()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = WaAccentGreen)
                    ) {
                        Text("CREATE", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
