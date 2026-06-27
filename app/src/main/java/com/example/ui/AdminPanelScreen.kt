package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Chat
import com.example.ui.theme.WaAccentGreen
import com.example.ui.theme.WaTealGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPanelScreen(
    viewModel: ChatViewModel,
    onBackClick: () -> Unit
) {
    val chatsList by viewModel.chats.collectAsState()
    var selectedChatForMessage by remember { mutableStateOf<Chat?>(null) }
    var textMessageInput by remember { mutableStateOf("") }
    var messageTypeSelection by remember { mutableStateOf("TEXT") } // TEXT, IMAGE, DOCUMENT
    
    var expandedChatDropdown by remember { mutableStateOf(false) }
    var expandedTypeDropdown by remember { mutableStateOf(false) }

    LaunchedEffect(chatsList) {
        if (selectedChatForMessage == null && chatsList.isNotEmpty()) {
            selectedChatForMessage = chatsList.first()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("WhatsApp Admin Dashboard", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = WaTealGreen)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(WaAccentGreen.copy(alpha = 0.15f))
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Build, contentDescription = "Admin", tint = WaTealGreen, modifier = Modifier.size(36.dp))
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text("Mock Server Control Panel", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = WaTealGreen)
                        Text(
                            "Simulate multi-user triggers, dynamic inputs, WebRTC calling feeds, and database administration tools directly.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            // --- Database & Seed section ---
            item {
                Text("System Diagnostics & Seeding", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = WaTealGreen)
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Active Direct/Group Channels: ${chatsList.size}", fontSize = 14.sp)
                        Text("Encryption Protocol: ${viewModel.encryptionKeyId}", fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = { viewModel.adminResetDatabase() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Reset Logs", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // --- Trigger WebRTC Ringing section ---
            item {
                Text("Simulate WebRTC Calling Rings", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = WaTealGreen)
                Spacer(modifier = Modifier.height(8.dp))
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Send an incoming call simulation alert from a random contact in the SQLite pool to trigger incoming ring overlay.",
                            fontSize = 12.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = { viewModel.adminTriggerIncomingCall(isVideo = false) },
                                colors = ButtonDefaults.buttonColors(containerColor = WaTealGreen),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Incoming Voice Call", fontSize = 12.sp)
                            }
                            Button(
                                onClick = { viewModel.adminTriggerIncomingCall(isVideo = true) },
                                colors = ButtonDefaults.buttonColors(containerColor = WaTealGreen),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Incoming Video Call", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            // --- Trigger Incoming Messages section ---
            item {
                Text("Inject Incoming Simulated Message", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = WaTealGreen)
                Spacer(modifier = Modifier.height(8.dp))
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Dropdown selection for chat
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedButton(
                                onClick = { expandedChatDropdown = true },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Channel: ${selectedChatForMessage?.name ?: "Select Chat"}")
                            }
                            DropdownMenu(
                                expanded = expandedChatDropdown,
                                onDismissRequest = { expandedChatDropdown = false }
                            ) {
                                chatsList.forEach { chat ->
                                    DropdownMenuItem(
                                        text = { Text(chat.name) },
                                        onClick = {
                                            selectedChatForMessage = chat
                                            expandedChatDropdown = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Dropdown selection for type
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedButton(
                                onClick = { expandedTypeDropdown = true },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Type: $messageTypeSelection")
                            }
                            DropdownMenu(
                                expanded = expandedTypeDropdown,
                                onDismissRequest = { expandedTypeDropdown = false }
                            ) {
                                listOf("TEXT", "IMAGE", "DOCUMENT").forEach { type ->
                                    DropdownMenuItem(
                                        text = { Text(type) },
                                        onClick = {
                                            messageTypeSelection = type
                                            expandedTypeDropdown = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = textMessageInput,
                            onValueChange = { textMessageInput = it },
                            label = { Text("Message Body / Document Name") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                val target = selectedChatForMessage
                                if (target != null && textMessageInput.isNotBlank()) {
                                    viewModel.adminTriggerIncomingMessage(
                                        chatId = target.id,
                                        text = textMessageInput,
                                        type = messageTypeSelection
                                    )
                                    textMessageInput = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = WaAccentGreen),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("INJECT MESSAGE", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
