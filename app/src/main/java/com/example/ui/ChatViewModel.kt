package com.example.ui

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.CallLog
import com.example.data.Chat
import com.example.data.Message
import com.example.data.StatusUpdate
import com.example.data.User
import com.example.data.EncryptionHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

enum class AppThemeSetting {
    LIGHT, DARK, SYSTEM
}

sealed class CallState {
    object Idle : CallState()
    data class Ringing(val user: User, val isVideo: Boolean, val isIncoming: Boolean) : CallState()
    data class Active(val user: User, val isVideo: Boolean, val durationSeconds: Int = 0) : CallState()
}

class ChatViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application, viewModelScope)
    private val dao = database.appDao()

    // --- Authentication State ---
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _isRegistered = MutableStateFlow(false)
    val isRegistered: StateFlow<Boolean> = _isRegistered.asStateFlow()

    // --- Active UI Selections ---
    private val _selectedChatId = MutableStateFlow<Long?>(null)
    val selectedChatId: StateFlow<Long?> = _selectedChatId.asStateFlow()

    private val _activeStatusViewer = MutableStateFlow<List<StatusUpdate>?>(null)
    val activeStatusViewer: StateFlow<List<StatusUpdate>?> = _activeStatusViewer.asStateFlow()

    // --- Theme & Search & Indicators ---
    private val _appTheme = MutableStateFlow(AppThemeSetting.LIGHT)
    val appTheme: StateFlow<AppThemeSetting> = _appTheme.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _showCiphertext = MutableStateFlow(false)
    val showCiphertext: StateFlow<Boolean> = _showCiphertext.asStateFlow()

    fun toggleCiphertext() {
        _showCiphertext.value = !_showCiphertext.value
    }

    // Map of chatId to typing user name ("Sarah Jenkins" etc)
    private val _typingIndicators = MutableStateFlow<Map<Long, String>>(emptyMap())
    val typingIndicators: StateFlow<Map<Long, String>> = _typingIndicators.asStateFlow()

    // --- Call State ---
    private val _callState = MutableStateFlow<CallState>(CallState.Idle)
    val callState: StateFlow<CallState> = _callState.asStateFlow()

    // --- Reactive Data Flows ---
    val allUsers: StateFlow<List<User>> = dao.getAllUsers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val chats: StateFlow<List<Chat>> = combine(dao.getAllChats(), _showCiphertext) { chatsList, showCipher ->
        chatsList.map { chat ->
            if (showCipher) {
                chat
            } else {
                chat.copy(lastMessageText = EncryptionHelper.decrypt(chat.lastMessageText))
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Message list of the currently selected chat, or empty list
    val activeChatMessages: StateFlow<List<Message>> = combine(_selectedChatId, _showCiphertext) { chatId, showCipher ->
        chatId to showCipher
    }.flatMapLatest { (chatId, showCipher) ->
        if (chatId != null) {
            dao.getMessagesForChat(chatId).map { messages ->
                messages.map { msg ->
                    if (showCipher) {
                        msg
                    } else {
                        msg.copy(text = EncryptionHelper.decrypt(msg.text))
                    }
                }
            }
        } else {
            flowOf(emptyList())
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val statusUpdates: StateFlow<List<StatusUpdate>> = dao.getAllStatusUpdates()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val callLogs: StateFlow<List<CallLog>> = dao.getAllCallLogs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Global Search Flow
    val searchResults: StateFlow<List<Message>> = _searchQuery
        .flatMapLatest { query ->
            if (query.length >= 2) {
                dao.getAllMessages().map { messages ->
                    messages.map { msg ->
                        msg.copy(text = EncryptionHelper.decrypt(msg.text))
                    }.filter { msg ->
                        msg.text.contains(query, ignoreCase = true)
                    }
                }
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Simulated WebSocket Connection Status
    private val _isSocketConnected = MutableStateFlow(true)
    val isSocketConnected: StateFlow<Boolean> = _isSocketConnected.asStateFlow()

    // Encryption public keys visual indicator
    val encryptionKeyId = "E2EE-RSA-4096-AES-GCM-SHA256"

    init {
        // Load or auto-register a default profile if needed
        viewModelScope.launch(Dispatchers.IO) {
            val defaultMe = User(0, "+15550100", "You (WhatsApp User)", "Urgent calls only 📲", "", true)
            dao.insertUser(defaultMe)
            _currentUser.value = defaultMe
            _isRegistered.value = true
        }

        // Run a simulated background task for dynamic call timer and chat triggers
        startCallTimerJob()
        startSimulatedTypingJob()
    }

    // --- User Profile Functions ---
    fun registerUser(phoneNumber: String, name: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val newUser = User(0, phoneNumber, name, "Using WhatsApp!", "", true)
            dao.insertUser(newUser)
            _currentUser.value = newUser
            _isRegistered.value = true
        }
    }

    fun updateProfile(name: String, statusText: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _currentUser.value?.let { current ->
                val updated = current.copy(name = name, statusText = statusText)
                dao.insertUser(updated)
                _currentUser.value = updated
            }
        }
    }

    // --- Chat & Messages Functions ---
    fun selectChat(chatId: Long?) {
        _selectedChatId.value = chatId
        if (chatId != null) {
            viewModelScope.launch(Dispatchers.IO) {
                dao.markChatAsRead(chatId)
            }
        }
    }

    fun sendMessage(chatId: Long, text: String, type: String = "TEXT", fileName: String? = null, fileSize: String? = null, mediaUrl: String? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            val timestamp = System.currentTimeMillis()
            val senderName = _currentUser.value?.name ?: "Me"
            val securedText = EncryptionHelper.encrypt(text)
            val message = Message(
                chatId = chatId,
                senderId = 0, // 0 is Me
                senderName = senderName,
                text = securedText,
                timestamp = timestamp,
                status = "SENT",
                type = type,
                mediaUrl = mediaUrl,
                fileName = fileName,
                fileSize = fileSize,
                isEncrypted = true
            )
            
            // Insert message
            val insertedId = dao.insertMessage(message)

            // Update parent chat
            val chatText = when (type) {
                "IMAGE" -> "📷 Photo"
                "VIDEO" -> "🎥 Video"
                "DOCUMENT" -> "📄 $text"
                "VOICE" -> "🎵 Voice Message"
                else -> text
            }
            val chatTextForDb = if (type == "TEXT") EncryptionHelper.encrypt(chatText) else chatText
            dao.updateChatLastMessage(chatId, chatTextForDb, timestamp, 0)

            // Simulation: WebSocket triggers Delivered -> Read
            launch {
                delay(1000)
                dao.updateMessageStatus(insertedId, "DELIVERED")
                delay(1500)
                dao.updateMessageStatus(insertedId, "READ")

                // Contact auto-replies to make application highly interactive!
                triggerAutoReply(chatId, text)
            }
        }
    }

    // --- Interactive Automated Responses (Simulating Chat Recipients) ---
    private fun triggerAutoReply(chatId: Long, userText: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val sender = dao.getUserById(chatId).first() ?: return@launch
            if (sender.id == 0L || chatId == 3L || chatId == 6L) {
                // Group chats have multiple members, let's reply from a group member
                delay(2000)
                val groupSender = if (chatId == 3L) "Mom" else "Dev Manager"
                _typingIndicators.value = _typingIndicators.value + (chatId to groupSender)
                delay(2500)
                _typingIndicators.value = _typingIndicators.value - chatId

                val groupReply = when {
                    userText.contains("hello", true) || userText.contains("hi", true) -> "Welcome to the group conversation!"
                    userText.contains("dinner", true) -> "Yes, don't be late! I'm preparing lasagna."
                    userText.contains("status", true) -> "Let's review tasks during our standup."
                    else -> "Got your update, let's discuss details shortly!"
                }
                val timestamp = System.currentTimeMillis()
                dao.insertMessage(Message(
                    chatId = chatId,
                    senderId = 100 + chatId,
                    senderName = groupSender,
                    text = EncryptionHelper.encrypt(groupReply),
                    timestamp = timestamp,
                    status = "READ",
                    type = "TEXT"
                ))
                dao.updateChatLastMessage(chatId, EncryptionHelper.encrypt("$groupSender: $groupReply"), timestamp, 1)
                return@launch
            }

            // Standard Direct Message Auto-Reply
            delay(1500)
            _typingIndicators.value = _typingIndicators.value + (chatId to sender.name)
            delay(2000)
            _typingIndicators.value = _typingIndicators.value - chatId

            val replyText = when {
                userText.contains("hello", true) || userText.contains("hi", true) -> {
                    "Hello! Hope you're having an amazing day! What can I help you with?"
                }
                userText.contains("meeting", true) || userText.contains("meet", true) -> {
                    "Yes, absolutely! I'm free at 3 PM. Let me send a calendar invite."
                }
                userText.contains("call", true) -> {
                    "Sure! I can hop on a voice or video call right now."
                }
                userText.contains("project", true) || userText.contains("code", true) -> {
                    "The code is fully compiled and linted. Everything is production-ready! Let me know if you want a walkthrough."
                }
                userText.contains("image", true) || userText.contains("photo", true) -> {
                    "Wow, looks awesome! Thanks for sharing."
                }
                else -> {
                    "Interesting! Let me look into that and get back to you shortly."
                }
            }

            val timestamp = System.currentTimeMillis()
            dao.insertMessage(Message(
                chatId = chatId,
                senderId = chatId,
                senderName = sender.name,
                text = EncryptionHelper.encrypt(replyText),
                timestamp = timestamp,
                status = "READ",
                type = "TEXT"
            ))
            dao.updateChatLastMessage(chatId, EncryptionHelper.encrypt(replyText), timestamp, 1)
        }
    }

    // --- Status Updates / Stories Functions ---
    fun addStatusUpdate(text: String, colorHex: String = "#075E54", mediaUrl: String? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            val me = _currentUser.value ?: return@launch
            val now = System.currentTimeMillis()
            dao.insertStatusUpdate(StatusUpdate(
                userId = 0,
                userName = me.name,
                userProfilePicUrl = "",
                statusText = text,
                statusMediaUrl = mediaUrl,
                timestamp = now,
                statusColorHex = colorHex
            ))
        }
    }

    fun viewStatusGroup(updates: List<StatusUpdate>) {
        _activeStatusViewer.value = updates
        // Mark as viewed
        viewModelScope.launch(Dispatchers.IO) {
            updates.forEach { update ->
                if (update.id != 0L) {
                    dao.markStatusAsViewed(update.id)
                }
            }
        }
    }

    fun closeStatusViewer() {
        _activeStatusViewer.value = null
    }

    // --- Call Simulators (WebRTC mock layer) ---
    fun startCall(user: User, isVideo: Boolean) {
        _callState.value = CallState.Ringing(user, isVideo, isIncoming = false)
        viewModelScope.launch {
            // Ring for 3 seconds, then accept automatically (simulation)
            delay(3000)
            if (_callState.value is CallState.Ringing) {
                _callState.value = CallState.Active(user, isVideo, 0)
                // Add to Call Logs
                val log = CallLog(
                    userId = user.id,
                    userName = user.name,
                    userProfilePicUrl = user.profilePicUrl,
                    isVideo = isVideo,
                    isIncoming = false,
                    isMissed = false,
                    timestamp = System.currentTimeMillis()
                )
                dao.insertCallLog(log)
            }
        }
    }

    fun endCall() {
        val currentState = _callState.value
        if (currentState is CallState.Ringing && currentState.isIncoming) {
            // Log as missed
            viewModelScope.launch(Dispatchers.IO) {
                dao.insertCallLog(CallLog(
                    userId = currentState.user.id,
                    userName = currentState.user.name,
                    userProfilePicUrl = currentState.user.profilePicUrl,
                    isVideo = currentState.isVideo,
                    isIncoming = true,
                    isMissed = true,
                    timestamp = System.currentTimeMillis()
                ))
            }
        } else if (currentState is CallState.Active) {
            // Log with actual duration
            viewModelScope.launch(Dispatchers.IO) {
                dao.insertCallLog(CallLog(
                    userId = currentState.user.id,
                    userName = currentState.user.name,
                    userProfilePicUrl = currentState.user.profilePicUrl,
                    isVideo = currentState.isVideo,
                    isIncoming = currentState.isVideo, // incoming/outgoing based on initial log
                    isMissed = false,
                    timestamp = System.currentTimeMillis(),
                    durationSeconds = currentState.durationSeconds
                ))
            }
        }
        _callState.value = CallState.Idle
    }

    fun acceptIncomingCall() {
        val currentState = _callState.value
        if (currentState is CallState.Ringing && currentState.isIncoming) {
            _callState.value = CallState.Active(currentState.user, currentState.isVideo, 0)
        }
    }

    private fun startCallTimerJob() {
        viewModelScope.launch {
            while (true) {
                delay(1000)
                val currentState = _callState.value
                if (currentState is CallState.Active) {
                    _callState.value = currentState.copy(durationSeconds = currentState.durationSeconds + 1)
                }
            }
        }
    }

    // --- Background typing simulator for users in inactive chats ---
    private fun startSimulatedTypingJob() {
        viewModelScope.launch(Dispatchers.IO) {
            while (true) {
                delay(15000 + Random.nextLong(15000))
                val activeChats = dao.getAllChats().first()
                if (activeChats.isNotEmpty()) {
                    val randomChat = activeChats.filter { !it.isGroup && it.id != 0L }.randomOrNull()
                    if (randomChat != null && _selectedChatId.value != randomChat.id) {
                        // Simulate some background typing and message
                        val user = dao.getUserById(randomChat.id).first()
                        if (user != null) {
                            _typingIndicators.value = _typingIndicators.value + (randomChat.id to user.name)
                            delay(3000)
                            _typingIndicators.value = _typingIndicators.value - randomChat.id

                            val texts = listOf(
                                "Hey! Did you check out my latest status updates? 🌄",
                                "Let me know when you're online, I have some updates on our codebase.",
                                "Have you seen the new material components on Android?",
                                "Check out this awesome project architecture!",
                                "How's your development workspace doing? 🚀"
                            )
                            val timestamp = System.currentTimeMillis()
                            dao.insertMessage(Message(
                                chatId = randomChat.id,
                                senderId = randomChat.id,
                                senderName = user.name,
                                text = texts.random(),
                                timestamp = timestamp,
                                status = "READ",
                                type = "TEXT"
                            ))
                            dao.updateChatLastMessage(randomChat.id, "1 unread message", timestamp, 1)
                        }
                    }
                }
            }
        }
    }

    // --- Search & Settings ---
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleTheme() {
        _appTheme.value = when (_appTheme.value) {
            AppThemeSetting.LIGHT -> AppThemeSetting.DARK
            AppThemeSetting.DARK -> AppThemeSetting.LIGHT
            AppThemeSetting.SYSTEM -> AppThemeSetting.LIGHT
        }
    }

    fun clearSearch() {
        _searchQuery.value = ""
    }

    // --- Admin Operations ---
    fun adminResetDatabase() {
        viewModelScope.launch(Dispatchers.IO) {
            dao.clearCallLogs()
            // Reset chats read/unread statuses
            val currentChats = dao.getAllChats().first()
            currentChats.forEach {
                dao.updateChat(it.copy(unreadCount = 0, lastMessageText = "Chat reset by admin"))
            }
        }
    }

    fun adminTriggerIncomingCall(isVideo: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            val randomUser = dao.getAllUsers().first().filter { it.id != 0L }.randomOrNull() ?: return@launch
            _callState.value = CallState.Ringing(randomUser, isVideo, isIncoming = true)
        }
    }

    fun adminTriggerIncomingMessage(chatId: Long, text: String, type: String = "TEXT") {
        viewModelScope.launch(Dispatchers.IO) {
            val sender = dao.getUserById(chatId).first() ?: return@launch
            val timestamp = System.currentTimeMillis()
            val message = Message(
                chatId = chatId,
                senderId = chatId,
                senderName = sender.name,
                text = text,
                timestamp = timestamp,
                status = "READ",
                type = type,
                mediaUrl = if (type == "IMAGE") "https://images.unsplash.com/photo-1512917774080-9991f1c4c750?w=500" else null,
                fileName = if (type == "DOCUMENT") "AdminDocument.pdf" else null,
                fileSize = if (type == "DOCUMENT") "4.8 MB" else null
            )
            dao.insertMessage(message)
            dao.updateChatLastMessage(chatId, text, timestamp, 1)
        }
    }

    fun formatDuration(seconds: Int): String {
        val m = seconds / 60
        val s = seconds % 60
        return String.format(Locale.getDefault(), "%02d:%02d", m, s)
    }

    fun formatTime(timestamp: Long): String {
        return try {
            val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
            sdf.format(Date(timestamp))
        } catch (e: Exception) {
            ""
        }
    }

    fun createNewGroupChat(groupName: String, participants: List<User>) {
        viewModelScope.launch(Dispatchers.IO) {
            val newChatId = Random.nextLong(100, 1000000)
            val now = System.currentTimeMillis()
            val chat = Chat(
                id = newChatId,
                name = groupName,
                isGroup = true,
                lastMessageText = "You created the group \"$groupName\"",
                lastMessageTime = now,
                unreadCount = 0,
                groupAvatarSeed = Random.nextInt(1, 100)
            )
            dao.insertChat(chat)
            
            // Send initial system message
            dao.insertMessage(Message(
                chatId = newChatId,
                senderId = 0,
                senderName = "System",
                text = "✨ You created the group \"$groupName\" with ${participants.size} contacts",
                timestamp = now,
                status = "READ",
                type = "TEXT"
            ))
        }
    }
}
