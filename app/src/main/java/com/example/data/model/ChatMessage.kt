package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_messages")
data class ChatMessage(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sender: String, // "user" or "companion"
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val recalledMemorySnippet: String? = null, // e.g. "Recalled: Golden retriever Milo • Iced vanilla latte"
    val mood: String? = null // e.g. "Warm", "Playful", "Touched", "Reflective"
)
