package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "memory_items")
data class MemoryItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val category: String, // "Preference", "Key Event", "Personal Fact", "Emotional Bond", "Goal"
    val content: String,
    val importance: Int = 3, // 1 to 5
    val timestamp: Long = System.currentTimeMillis(),
    val lastRecalledAt: Long = 0L,
    val recalledCount: Int = 0,
    val isPinned: Boolean = false
) {
    companion object {
        const val CAT_PREFERENCE = "Preference"
        const val CAT_EVENT = "Key Event"
        const val CAT_FACT = "Personal Fact"
        const val CAT_BOND = "Emotional Bond"
        const val CAT_GOAL = "Goal & Dream"

        val ALL_CATEGORIES = listOf(
            CAT_PREFERENCE,
            CAT_EVENT,
            CAT_FACT,
            CAT_BOND,
            CAT_GOAL
        )

        fun defaultMemories(): List<MemoryItem> = listOf(
            MemoryItem(
                category = CAT_PREFERENCE,
                content = "Prefers iced vanilla oat latte in the morning with a touch of cinnamon",
                importance = 4,
                isPinned = true
            ),
            MemoryItem(
                category = CAT_FACT,
                content = "Has an energetic golden retriever named Milo who loves tennis balls",
                importance = 5,
                isPinned = true
            ),
            MemoryItem(
                category = CAT_EVENT,
                content = "Preparing for a big product design review next week at work",
                importance = 4,
                isPinned = false
            ),
            MemoryItem(
                category = CAT_BOND,
                content = "Finds comfort in quiet late-night stargazing talks and honest conversations",
                importance = 5,
                isPinned = true
            ),
            MemoryItem(
                category = CAT_GOAL,
                content = "Dreaming of visiting Kyoto during autumn to sketch traditional architecture",
                importance = 3,
                isPinned = false
            )
        )
    }
}
