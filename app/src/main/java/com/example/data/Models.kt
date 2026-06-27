package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey val id: Long,
    val phoneNumber: String,
    val name: String,
    val statusText: String = "Hey there! I am using WhatsApp.",
    val profilePicUrl: String = "",
    val isOnline: Boolean = false,
    val lastSeen: String = "last seen recently"
)

@Entity(tableName = "chats")
data class Chat(
    @PrimaryKey val id: Long,
    val name: String,
    val isGroup: Boolean = false,
    val lastMessageText: String = "",
    val lastMessageTime: Long = 0,
    val unreadCount: Int = 0,
    val isMuted: Boolean = false,
    val groupAvatarSeed: Int = 0
)

@Entity(tableName = "messages")
data class Message(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val chatId: Long,
    val senderId: Long,
    val senderName: String,
    val text: String,
    val timestamp: Long,
    val status: String, // "SENT", "DELIVERED", "READ"
    val type: String,   // "TEXT", "IMAGE", "VIDEO", "DOCUMENT", "VOICE"
    val mediaUrl: String? = null,
    val fileName: String? = null,
    val fileSize: String? = null,
    val isEncrypted: Boolean = true
)

@Entity(tableName = "status_updates")
data class StatusUpdate(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val userName: String,
    val userProfilePicUrl: String = "",
    val statusText: String = "",
    val statusMediaUrl: String? = null,
    val timestamp: Long,
    val isViewed: Boolean = false,
    val statusColorHex: String = "#075E54"
)

@Entity(tableName = "call_logs")
data class CallLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val userName: String,
    val userProfilePicUrl: String = "",
    val isVideo: Boolean = false,
    val isIncoming: Boolean = true,
    val isMissed: Boolean = false,
    val timestamp: Long,
    val durationSeconds: Int = 0
)
