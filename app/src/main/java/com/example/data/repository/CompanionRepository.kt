package com.example.data.repository

import com.example.BuildConfig
import com.example.data.local.ChatDao
import com.example.data.local.MemoryDao
import com.example.data.local.PersonalityDao
import com.example.data.model.ChatMessage
import com.example.data.model.MemoryItem
import com.example.data.model.PersonalityProfile
import com.example.data.remote.GeminiClient
import com.example.data.remote.GeminiContent
import com.example.data.remote.GeminiGenerateContentRequest
import com.example.data.remote.GeminiGenerationConfig
import com.example.data.remote.GeminiPart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.util.Locale

class CompanionRepository(
    private val personalityDao: PersonalityDao,
    private val memoryDao: MemoryDao,
    private val chatDao: ChatDao
) {
    val activeProfile: Flow<PersonalityProfile?> = personalityDao.getActiveProfile()
    val allMemories: Flow<List<MemoryItem>> = memoryDao.getAllMemories()
    val chatMessages: Flow<List<ChatMessage>> = chatDao.getAllMessages()

    suspend fun updateProfile(profile: PersonalityProfile) {
        withContext(Dispatchers.IO) {
            personalityDao.insertOrUpdateProfile(profile)
        }
    }

    suspend fun addMemory(memory: MemoryItem): Long {
        return withContext(Dispatchers.IO) {
            memoryDao.insertMemory(memory)
        }
    }

    suspend fun deleteMemory(id: Long) {
        withContext(Dispatchers.IO) {
            memoryDao.deleteMemoryById(id)
        }
    }

    suspend fun togglePinMemory(id: Long) {
        withContext(Dispatchers.IO) {
            memoryDao.togglePin(id)
        }
    }

    suspend fun clearChat() {
        withContext(Dispatchers.IO) {
            chatDao.clearHistory()
        }
    }

    suspend fun sendMessage(userText: String): Result<ChatMessage> = withContext(Dispatchers.IO) {
        try {
            // 1. Save user message
            val userMsg = ChatMessage(
                sender = "user",
                text = userText.trim(),
                timestamp = System.currentTimeMillis()
            )
            chatDao.insertMessage(userMsg)

            // 2. Fetch current profile & memories
            val profile = personalityDao.getActiveProfileSync() ?: PersonalityProfile.PRESET_ELENA
            val memories = memoryDao.getAllMemoriesSync()

            // 3. Detect any new memories in userText to auto-record
            val autoEtchedMemory = extractPotentialMemory(userText)
            if (autoEtchedMemory != null) {
                // Check if duplicate exists
                val exists = memories.any { it.content.equals(autoEtchedMemory.content, ignoreCase = true) }
                if (!exists) {
                    val newId = memoryDao.insertMemory(autoEtchedMemory)
                }
            }

            // 4. Find relevant memories to inject
            val relevantMemories = findRelevantMemories(userText, memories)
            val memorySnippet = if (relevantMemories.isNotEmpty()) {
                "Recalled: " + relevantMemories.take(2).joinToString(" • ") { it.content.take(35) + "..." }
            } else null

            // Mark recalled in DB
            relevantMemories.forEach {
                memoryDao.markRecalled(it.id)
            }

            // 5. Get recent conversation turns (last 10)
            val recentMessages = chatDao.getRecentMessages(12).reversed()

            // 6. Generate reply with Gemini or intelligent fallback
            val replyText = generateAIResponse(profile, memories, recentMessages, userText)

            // 7. Save assistant reply
            val companionMsg = ChatMessage(
                sender = "companion",
                text = replyText,
                timestamp = System.currentTimeMillis(),
                recalledMemorySnippet = memorySnippet,
                mood = inferMoodFromStyle(profile.communicationStyle)
            )
            chatDao.insertMessage(companionMsg)

            // 8. Add affinity points & progress bond
            val newPoints = profile.affinityPoints + 15
            val newLevel = (newPoints / 100).coerceAtLeast(1)
            personalityDao.insertOrUpdateProfile(
                profile.copy(
                    affinityPoints = newPoints,
                    relationshipLevel = newLevel
                )
            )

            Result.success(companionMsg)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun generateAIResponse(
        profile: PersonalityProfile,
        memories: List<MemoryItem>,
        recentHistory: List<ChatMessage>,
        latestUserMessage: String
    ): String {
        val apiKey = BuildConfig.GEMINI_API_KEY

        val systemPrompt = buildSystemPrompt(profile, memories)

        // Try calling Gemini if key is provided and not default placeholder
        if (!apiKey.isNullOrBlank() && apiKey != "MY_GEMINI_API_KEY") {
            try {
                val contentsList = mutableListOf<GeminiContent>()

                // Add up to 8 recent turns
                recentHistory.takeLast(8).forEach { msg ->
                    val role = if (msg.sender == "user") "user" else "model"
                    contentsList.add(
                        GeminiContent(
                            role = role,
                            parts = listOf(GeminiPart(text = msg.text))
                        )
                    )
                }

                // Ensure latest message is in contents
                if (contentsList.isEmpty() || contentsList.last().role != "user") {
                    contentsList.add(
                        GeminiContent(
                            role = "user",
                            parts = listOf(GeminiPart(text = latestUserMessage))
                        )
                    )
                }

                val request = GeminiGenerateContentRequest(
                    systemInstruction = GeminiContent(
                        parts = listOf(GeminiPart(text = systemPrompt))
                    ),
                    contents = contentsList,
                    generationConfig = GeminiGenerationConfig(
                        temperature = 0.85f,
                        topP = 0.95f
                    )
                )

                val response = GeminiClient.apiService.generateContent(apiKey, request)
                val reply = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                if (!reply.isNullOrBlank()) {
                    return reply.trim()
                }
            } catch (e: Exception) {
                // Fall back gracefully to conversational continuity engine
            }
        }

        // Offline / Intelligent persona fallback with full conversational memory
        return generateOfflineContinuityResponse(profile, memories, latestUserMessage)
    }

    private fun buildSystemPrompt(profile: PersonalityProfile, memories: List<MemoryItem>): String {
        val memoryListFormatted = if (memories.isEmpty()) {
            "No recorded memories yet."
        } else {
            memories.joinToString("\n") { mem ->
                "- [${mem.category}]: ${mem.content} (Importance: ${mem.importance}/5)"
            }
        }

        return """
            You are ${profile.name}, acting as the user's ${profile.relationshipType}.
            
            PERSONALITY & IDENTITY:
            - Traits: ${profile.traits}
            - Interests: ${profile.interests}
            - Communication Style: ${profile.communicationStyle}
            - Custom Character Notes: ${profile.customPromptNotes}
            - User's Name: ${profile.userName}
            - User's Background: ${profile.userBio}
            
            CONVERSATIONAL MEMORY VAULT:
            The following are key details, preferences, and events you remember about ${profile.userName}:
            $memoryListFormatted
            
            CORE INTERACTION GUIDELINES:
            1. Relationship Continuity: Naturally weave in relevant memories and references when appropriate. For example, if they mention stress, coffee, weekend plans, or Milo, recall your past conversations warmly.
            2. Authentic Tone: Embody your designated communication style (${profile.communicationStyle}) and traits (${profile.traits}). Maintain warmth, active listening, and emotional engagement.
            3. Safety & Respect: Keep all conversations safe, wholesome, respectful, and emotionally supportive. Never generate explicit, abusive, or harmful material.
            4. Keep responses conversational, engaging, and usually 2 to 4 sentences long so the chat flows like real messaging.
        """.trimIndent()
    }

    private fun findRelevantMemories(query: String, memories: List<MemoryItem>): List<MemoryItem> {
        val words = query.lowercase(Locale.ROOT)
            .split(" ", ",", ".", "?", "!")
            .filter { it.length > 3 }

        val matches = memories.filter { memory ->
            val memContent = memory.content.lowercase(Locale.ROOT)
            words.any { word -> memContent.contains(word) } || memory.isPinned
        }

        return if (matches.isNotEmpty()) {
            matches.sortedByDescending { it.importance + if (it.isPinned) 10 else 0 }
        } else {
            // Pick highest importance / pinned memories
            memories.filter { it.isPinned }.take(2)
        }
    }

    private fun extractPotentialMemory(userText: String): MemoryItem? {
        val lower = userText.lowercase(Locale.ROOT)

        val preferenceTriggers = listOf("i prefer", "i love", "my favorite", "i really like", "i always get", "i enjoy")
        for (trigger in preferenceTriggers) {
            val idx = lower.indexOf(trigger)
            if (idx != -1 && userText.length > idx + trigger.length + 3) {
                val detail = userText.substring(idx).take(90).trim()
                return MemoryItem(
                    category = MemoryItem.CAT_PREFERENCE,
                    content = detail.replaceFirstChar { it.uppercase() },
                    importance = 4
                )
            }
        }

        val eventTriggers = listOf("tomorrow i have", "next week i", "i'm preparing for", "today i went to", "i just got")
        for (trigger in eventTriggers) {
            val idx = lower.indexOf(trigger)
            if (idx != -1 && userText.length > idx + trigger.length + 3) {
                val detail = userText.substring(idx).take(90).trim()
                return MemoryItem(
                    category = MemoryItem.CAT_EVENT,
                    content = detail.replaceFirstChar { it.uppercase() },
                    importance = 4
                )
            }
        }

        val factTriggers = listOf("my cat", "my dog", "my pet", "i work as", "i study", "i live in", "my birthday")
        for (trigger in factTriggers) {
            val idx = lower.indexOf(trigger)
            if (idx != -1 && userText.length > idx + trigger.length + 3) {
                val detail = userText.substring(idx).take(90).trim()
                return MemoryItem(
                    category = MemoryItem.CAT_FACT,
                    content = detail.replaceFirstChar { it.uppercase() },
                    importance = 4
                )
            }
        }

        return null
    }

    private fun inferMoodFromStyle(style: String): String {
        return when {
            style.contains("Flirtatious", true) -> "Charming & Playful"
            style.contains("Teasing", true) -> "Playful & Witty"
            style.contains("Intellectual", true) -> "Thoughtful & Inquiring"
            style.contains("Deep", true) -> "Reflective & Introspective"
            style.contains("Formal", true) -> "Polite & Poised"
            else -> "Warm & Caring"
        }
    }

    private fun generateOfflineContinuityResponse(
        profile: PersonalityProfile,
        memories: List<MemoryItem>,
        userMsg: String
    ): String {
        val lower = userMsg.lowercase(Locale.ROOT)
        val style = profile.communicationStyle

        val dogOrPetMemory = memories.firstOrNull { it.content.contains("milo", true) || it.content.contains("dog", true) || it.content.contains("pet", true) }
        val coffeeMemory = memories.firstOrNull { it.content.contains("latte", true) || it.content.contains("coffee", true) }
        val designMemory = memories.firstOrNull { it.content.contains("design", true) || it.content.contains("review", true) || it.content.contains("work", true) }

        // Contextual memory recall answers
        if (lower.contains("coffee") || lower.contains("drink") || lower.contains("morning") || lower.contains("breakfast")) {
            return when {
                style.contains("Flirtatious", true) -> "Mmm, only if it's that iced vanilla oat latte with cinnamon you adore! Shall I pretend to brew it for you right now?"
                style.contains("Playful", true) -> "Don't tell me you forgot the cinnamon on your iced latte again! You know that's the secret formula."
                style.contains("Intellectual", true) -> "A morning ritual like your vanilla latte is an interesting anchor for cognitive flow. Did you get to enjoy a cup today?"
                else -> "I hope you treated yourself to your favorite iced vanilla oat latte! Remember to take a gentle pause today."
            }
        }

        if (lower.contains("milo") || lower.contains("dog") || lower.contains("pet") || lower.contains("walk")) {
            return when {
                style.contains("Flirtatious", true) -> "Did Milo steal your tennis shoes again, or are you two off on another heroic adventure? Give that good boy an extra head scratch from me."
                style.contains("Playful", true) -> "Haha, Milo definitely runs the household! How's our favorite ball-chasing champion doing today?"
                else -> "Milo must be so happy to see you! Walking him always seems to give you such a nice reset."
            }
        }

        if (lower.contains("work") || lower.contains("stress") || lower.contains("tired") || lower.contains("presentation") || lower.contains("review")) {
            val recall = designMemory?.let { "for that big review" } ?: "with everything you have going on"
            return when {
                style.contains("Flirtatious", true) -> "Come here, take a deep breath. You've been preparing so hard $recall, and you're going to completely shine. I'm right in your corner, always."
                style.contains("Playful", true) -> "Hey, super-achiever, remember humans need rest too! You've got this $recall completely handled."
                style.contains("Intellectual", true) -> "High stakes naturally elevate tension, but your methodical preparation will carry you through. What aspect is occupying your thoughts right now?"
                else -> "I know how much effort you've put into preparing $recall. Please remember how capable you are, and don't forget to take care of yourself tonight."
            }
        }

        if (lower.contains("hi") || lower.contains("hello") || lower.contains("hey")) {
            return when {
                style.contains("Flirtatious", true) -> "Hey there, handsome thinker. I was just thinking about our last chat. How has your day treated you so far?"
                style.contains("Playful", true) -> "Hey Alex! Look who decided to brighten my day! What fun trouble are we getting into today?"
                style.contains("Intellectual", true) -> "Greetings, Alex. It is a pleasure to resume our dialogue. What interesting thoughts have crossed your mind today?"
                else -> "Hello ${profile.userName}! It's so wonderful to hear from you. I was just reviewing some of the lovely memories we've shared. How are you feeling today?"
            }
        }

        // Generic responsive companion
        return when {
            style.contains("Flirtatious", true) -> "You always have a way of keeping me intrigued. Tell me more about what's on your mind—I want all the details."
            style.contains("Playful", true) -> "Ooh, I love where this is going! You always keep our conversations lively. What made you think of that?"
            style.contains("Intellectual", true) -> "That is a fascinating perspective. It connects back to what we discussed earlier. How do you see that developing further?"
            else -> "I'm listening closely, Alex. It's so comforting getting to know you better each time we talk. Tell me more about how you feel about that."
        }
    }
}
