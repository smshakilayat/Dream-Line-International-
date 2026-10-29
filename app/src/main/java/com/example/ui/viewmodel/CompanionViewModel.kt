package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.ChatMessage
import com.example.data.model.MemoryItem
import com.example.data.model.PersonalityProfile
import com.example.data.repository.CompanionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CompanionViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application, viewModelScope)
    private val repository = CompanionRepository(
        database.personalityDao(),
        database.memoryDao(),
        database.chatDao()
    )

    val activeProfile: StateFlow<PersonalityProfile> = repository.activeProfile
        .map { it ?: PersonalityProfile.PRESET_ELENA }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = PersonalityProfile.PRESET_ELENA
        )

    val allMemories: StateFlow<List<MemoryItem>> = repository.allMemories
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val chatMessages: StateFlow<List<ChatMessage>> = repository.chatMessages
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText.asStateFlow()

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _selectedTab = MutableStateFlow(0) // 0: Chat, 1: Memory Vault, 2: Persona Customization
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    private val _memorySearchQuery = MutableStateFlow("")
    val memorySearchQuery: StateFlow<String> = _memorySearchQuery.asStateFlow()

    private val _selectedMemoryCategory = MutableStateFlow<String?>(null)
    val selectedMemoryCategory: StateFlow<String?> = _selectedMemoryCategory.asStateFlow()

    val filteredMemories: StateFlow<List<MemoryItem>> = combine(
        allMemories,
        _memorySearchQuery,
        _selectedMemoryCategory
    ) { memories, query, category ->
        memories.filter { item ->
            val matchesCategory = category == null || item.category == category
            val matchesQuery = query.isBlank() ||
                    item.content.contains(query, ignoreCase = true) ||
                    item.category.contains(query, ignoreCase = true)
            matchesCategory && matchesQuery
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun onInputTextChanged(text: String) {
        _inputText.value = text
    }

    fun selectTab(index: Int) {
        _selectedTab.value = index
    }

    fun setMemorySearchQuery(query: String) {
        _memorySearchQuery.value = query
    }

    fun setMemoryCategoryFilter(category: String?) {
        _selectedMemoryCategory.value = category
    }

    fun sendMessage() {
        val text = _inputText.value.trim()
        if (text.isEmpty() || _isGenerating.value) return

        _inputText.value = ""
        _isGenerating.value = true

        viewModelScope.launch {
            repository.sendMessage(text)
            _isGenerating.value = false
        }
    }

    fun selectPreset(preset: PersonalityProfile) {
        viewModelScope.launch {
            repository.updateProfile(preset)
        }
    }

    fun updateProfile(profile: PersonalityProfile) {
        viewModelScope.launch {
            repository.updateProfile(profile)
        }
    }

    fun toggleTrait(trait: String) {
        val current = activeProfile.value
        val currentTraits = current.getTraitsList().toMutableList()
        if (currentTraits.contains(trait)) {
            if (currentTraits.size > 1) {
                currentTraits.remove(trait)
            }
        } else {
            currentTraits.add(trait)
        }
        val updated = current.copy(traits = currentTraits.joinToString(", "))
        updateProfile(updated)
    }

    fun toggleInterest(interest: String) {
        val current = activeProfile.value
        val currentInterests = current.getInterestsList().toMutableList()
        if (currentInterests.contains(interest)) {
            if (currentInterests.size > 1) {
                currentInterests.remove(interest)
            }
        } else {
            currentInterests.add(interest)
        }
        val updated = current.copy(interests = currentInterests.joinToString(", "))
        updateProfile(updated)
    }

    fun setCommunicationStyle(style: String) {
        val current = activeProfile.value
        updateProfile(current.copy(communicationStyle = style))
    }

    fun setRelationshipType(type: String) {
        val current = activeProfile.value
        updateProfile(current.copy(relationshipType = type))
    }

    fun addCustomMemory(category: String, content: String, importance: Int, isPinned: Boolean) {
        if (content.isBlank()) return
        viewModelScope.launch {
            repository.addMemory(
                MemoryItem(
                    category = category,
                    content = content.trim(),
                    importance = importance,
                    isPinned = isPinned
                )
            )
        }
    }

    fun deleteMemory(id: Long) {
        viewModelScope.launch {
            repository.deleteMemory(id)
        }
    }

    fun togglePinMemory(id: Long) {
        viewModelScope.launch {
            repository.togglePinMemory(id)
        }
    }

    fun clearChat() {
        viewModelScope.launch {
            repository.clearChat()
        }
    }
}
