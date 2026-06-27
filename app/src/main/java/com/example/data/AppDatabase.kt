package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        User::class,
        Chat::class,
        Message::class,
        StatusUpdate::class,
        CallLog::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "whatsapp_database"
                )
                .addCallback(DatabaseCallback(scope))
                .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateDatabase(database.appDao())
                    }
                }
            }

            private suspend fun populateDatabase(dao: AppDao) {
                // --- Seed Users ---
                val users = listOf(
                    User(1, "+15550199", "Sarah Jenkins", "At the gym, text later.", "", true),
                    User(2, "+15550244", "John Doe", "Busy coding 💻🚀", "", false, "last seen 5m ago"),
                    User(3, "+15550388", "Family Group", "Family is everything!", "", false),
                    User(4, "+15550411", "Alex Rivera", "Urgent calls only.", "", true),
                    User(5, "+15550522", "Elon Mask", "Occupy Mars!", "", false, "last seen 2h ago"),
                    User(6, "+15550677", "Work Colleagues", "Sync up here.", "", false)
                )

                dao.insertUsers(users)

                // --- Seed Chats ---
                val now = System.currentTimeMillis()
                val chats = listOf(
                    Chat(1, "Sarah Jenkins", false, "Hey! Are we still meeting up today?", now - 2 * 60 * 1000, 2, false, 1),
                    Chat(2, "John Doe", false, "Code is working perfectly! Double check the main.cpp", now - 30 * 60 * 1000, 0, false, 2),
                    Chat(3, "Family Group 🏠", true, "Mom: Don't forget dinner at 7!", now - 45 * 60 * 1000, 0, false, 3),
                    Chat(4, "Alex Rivera", false, "📄 ProjectBrief_v2.pdf", now - 2 * 60 * 60 * 1000, 0, true, 4),
                    Chat(5, "Elon Mask", false, "We are launching the starship next week.", now - 24 * 60 * 60 * 1000, 0, false, 5),
                    Chat(6, "Work Group 🏢", true, "Dev Team: Sprint review started", now - 3 * 24 * 60 * 60 * 1000, 0, false, 6)
                )
                dao.insertChats(chats)

                // --- Seed Messages ---
                val messages = listOf(
                    // Sarah
                    Message(chatId = 1, senderId = 1, senderName = "Sarah Jenkins", text = EncryptionHelper.encrypt("Hey there!"), timestamp = now - 15 * 60 * 1000, status = "READ", type = "TEXT"),
                    Message(chatId = 1, senderId = 0, senderName = "Me", text = EncryptionHelper.encrypt("Hi Sarah! Yes, what's up?"), timestamp = now - 10 * 60 * 1000, status = "READ", type = "TEXT"),
                    Message(chatId = 1, senderId = 1, senderName = "Sarah Jenkins", text = EncryptionHelper.encrypt("Hey! Are we still meeting up today?"), timestamp = now - 2 * 60 * 1000, status = "DELIVERED", type = "TEXT"),
                    
                    // John
                    Message(chatId = 2, senderId = 2, senderName = "John Doe", text = EncryptionHelper.encrypt("I'm pushing the updates now."), timestamp = now - 40 * 60 * 1000, status = "READ", type = "TEXT"),
                    Message(chatId = 2, senderId = 0, senderName = "Me", text = EncryptionHelper.encrypt("Perfect, I'll review it."), timestamp = now - 35 * 60 * 1000, status = "READ", type = "TEXT"),
                    Message(chatId = 2, senderId = 2, senderName = "John Doe", text = EncryptionHelper.encrypt("Code is working perfectly! Double check the main.cpp"), timestamp = now - 30 * 60 * 1000, status = "READ", type = "TEXT"),

                    // Family
                    Message(chatId = 3, senderId = 2, senderName = "John", text = EncryptionHelper.encrypt("Is anyone home?"), timestamp = now - 2 * 60 * 60 * 1000, status = "READ", type = "TEXT"),
                    Message(chatId = 3, senderId = 1, senderName = "Sarah", text = EncryptionHelper.encrypt("I am on my way!"), timestamp = now - 1 * 60 * 60 * 1000, status = "READ", type = "TEXT"),
                    Message(chatId = 3, senderId = 3, senderName = "Mom", text = EncryptionHelper.encrypt("Mom: Don't forget dinner at 7!"), timestamp = now - 45 * 60 * 1000, status = "READ", type = "TEXT"),

                    // Alex (Document and image)
                    Message(chatId = 4, senderId = 4, senderName = "Alex Rivera", text = EncryptionHelper.encrypt("Shared the updated brief"), timestamp = now - 3 * 60 * 60 * 1000, status = "READ", type = "TEXT"),
                    Message(chatId = 4, senderId = 4, senderName = "Alex Rivera", text = EncryptionHelper.encrypt("📄 ProjectBrief_v2.pdf"), timestamp = now - 2 * 60 * 60 * 1000, status = "READ", type = "DOCUMENT", fileName = "ProjectBrief_v2.pdf", fileSize = "2.4 MB"),

                    // Elon (Image message)
                    Message(chatId = 5, senderId = 5, senderName = "Elon Mask", text = EncryptionHelper.encrypt("Check this rocket shot!"), timestamp = now - 25 * 60 * 60 * 1000, status = "READ", type = "TEXT"),
                    Message(chatId = 5, senderId = 5, senderName = "Elon Mask", text = EncryptionHelper.encrypt("🚀 Starship Liftoff"), timestamp = now - 24 * 60 * 60 * 1000, status = "READ", type = "IMAGE", mediaUrl = "https://images.unsplash.com/photo-1541185933-ef5d8ed016c2?w=500")
                )
                for (msg in messages) {
                    dao.insertMessage(msg)
                }

                // --- Seed Status Updates ---
                val statuses = listOf(
                    StatusUpdate(userId = 1, userName = "Sarah Jenkins", statusText = "Beautiful sunrise today! 🌅", timestamp = now - 1 * 60 * 60 * 1000, statusColorHex = "#2E7D32"),
                    StatusUpdate(userId = 2, userName = "John Doe", statusText = "Working on the next big feature 🚀☕", timestamp = now - 4 * 60 * 60 * 1000, statusColorHex = "#1565C0"),
                    StatusUpdate(userId = 4, userName = "Alex Rivera", statusText = "Weekend hiking vibes 🥾🌲", timestamp = now - 8 * 60 * 60 * 1000, statusColorHex = "#AD1457")
                )
                for (status in statuses) {
                    dao.insertStatusUpdate(status)
                }

                // --- Seed Call Logs ---
                val calls = listOf(
                    CallLog(userId = 1, userName = "Sarah Jenkins", isVideo = false, isIncoming = true, isMissed = false, timestamp = now - 3 * 60 * 60 * 1000, durationSeconds = 142),
                    CallLog(userId = 2, userName = "John Doe", isVideo = true, isIncoming = false, isMissed = false, timestamp = now - 23 * 60 * 60 * 1000, durationSeconds = 450),
                    CallLog(userId = 4, userName = "Alex Rivera", isVideo = false, isIncoming = true, isMissed = true, timestamp = now - 2 * 24 * 60 * 60 * 1000)
                )
                for (call in calls) {
                    dao.insertCallLog(call)
                }
            }
        }
    }
}
