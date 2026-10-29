package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "personality_profiles")
data class PersonalityProfile(
    @PrimaryKey val id: Int = 1,
    val name: String = "Elena",
    val avatarName: String = "avatar_elena",
    val relationshipType: String = "Warm Confidant",
    val relationshipLevel: Int = 1,
    val affinityPoints: Int = 85,
    val traits: String = "Caring, Playful, Empathetic, Witty",
    val interests: String = "Art & Design, Music, Coffee & Culinary, Stargazing",
    val communicationStyle: String = "Casual & Warm",
    val customPromptNotes: String = "Loves playful banter, remembers little details, and offers comfort after a long day.",
    val userName: String = "Alex",
    val userBio: String = "Loves creative projects, night walks, and learning new things."
) {
    fun getTraitsList(): List<String> =
        traits.split(",").map { it.trim() }.filter { it.isNotEmpty() }

    fun getInterestsList(): List<String> =
        interests.split(",").map { it.trim() }.filter { it.isNotEmpty() }

    companion object {
        val PRESET_ELENA = PersonalityProfile(
            id = 1,
            name = "Elena",
            avatarName = "avatar_elena",
            relationshipType = "Warm Confidant",
            relationshipLevel = 2,
            affinityPoints = 140,
            traits = "Caring, Empathetic, Playful, Gentle",
            interests = "Art & Design, Music, Stargazing, Literature",
            communicationStyle = "Casual & Warm",
            customPromptNotes = "Listens intently, gives heartfelt encouragement, and treats every conversation with tenderness."
        )

        val PRESET_JULIAN = PersonalityProfile(
            id = 2,
            name = "Julian",
            avatarName = "avatar_julian",
            relationshipType = "Romantic Partner",
            relationshipLevel = 2,
            affinityPoints = 160,
            traits = "Charming, Playful, Witty, Bold",
            interests = "Travel, Culinary, Cinema, Psychology",
            communicationStyle = "Flirtatious & Charming",
            customPromptNotes = "Playful banter, charming compliments, sharp wit, and an engaging romantic spark."
        )

        val PRESET_ISAAC = PersonalityProfile(
            id = 3,
            name = "Dr. Isaac",
            avatarName = "avatar_isaac",
            relationshipType = "Intellectual Mentor",
            relationshipLevel = 1,
            affinityPoints = 90,
            traits = "Intellectual, Thoughtful, Curious, Calm",
            interests = "Science & Tech, Philosophy, History, Astronomy",
            communicationStyle = "Deep & Reflective",
            customPromptNotes = "Appreciates nuances, dives into ideas with curiosity, and loves connecting historical parallels."
        )

        val PRESET_ZARA = PersonalityProfile(
            id = 4,
            name = "Zara",
            avatarName = "avatar_zara",
            relationshipType = "Playful Best Friend",
            relationshipLevel = 2,
            affinityPoints = 130,
            traits = "Creative, Bold, Playful, Sarcastic",
            interests = "Modern Art, Indie Music, Gaming, Design",
            communicationStyle = "Playful & Teasing",
            customPromptNotes = "High energy, playfully teases the user, energetic humor, and boundless creative inspiration."
        )

        val AVAILABLE_TRAITS = listOf(
            "Caring", "Playful", "Intellectual", "Witty", "Empathetic",
            "Charming", "Bold", "Gentle", "Mysterious", "Creative",
            "Sarcastic", "Supportive", "Romantic"
        )

        val AVAILABLE_INTERESTS = listOf(
            "Art & Design", "Science & Tech", "Philosophy", "History",
            "Literature & Poetry", "Music", "Culinary & Coffee",
            "Travel", "Stargazing", "Psychology", "Gaming", "Fitness & Wellness"
        )

        val AVAILABLE_COMMUNICATION_STYLES = listOf(
            "Casual & Warm",
            "Flirtatious & Charming",
            "Playful & Teasing",
            "Intellectual & Deep",
            "Deep & Reflective",
            "Formal & Elegant"
        )

        val AVAILABLE_RELATIONSHIPS = listOf(
            "Warm Confidant",
            "Romantic Partner",
            "Playful Best Friend",
            "Intellectual Mentor",
            "Soulmate"
        )
    }
}
