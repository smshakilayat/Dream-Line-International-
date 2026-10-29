package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.ChatMessage
import com.example.data.model.MemoryItem
import com.example.data.model.PersonalityProfile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        PersonalityProfile::class,
        MemoryItem::class,
        ChatMessage::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun personalityDao(): PersonalityDao
    abstract fun memoryDao(): MemoryDao
    abstract fun chatDao(): ChatDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "aura_companion_database"
                )
                .addCallback(DatabaseCallback(scope))
                .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database)
                    }
                }
            }
        }

        private suspend fun populateInitialData(database: AppDatabase) {
            val personalityDao = database.personalityDao()
            val memoryDao = database.memoryDao()
            val chatDao = database.chatDao()

            // Seed active personality
            personalityDao.insertOrUpdateProfile(PersonalityProfile.PRESET_ELENA)

            // Seed initial memories
            memoryDao.insertMemories(MemoryItem.defaultMemories())

            // Seed welcoming message
            val welcomeText = "Hey Alex! It's so lovely to connect with you. I still remember how you take your iced vanilla latte with cinnamon, and how Milo always keeps you on your toes! How has your week been feeling?"
            chatDao.insertMessage(
                ChatMessage(
                    sender = "companion",
                    text = welcomeText,
                    timestamp = System.currentTimeMillis(),
                    recalledMemorySnippet = "Recalled: Iced vanilla latte • Golden retriever Milo",
                    mood = "Warm & Attentive"
                )
            )
        }
    }
}
