package com.example.personalhealthcareapp.ViewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.personalhealthcareapp.LLMinference.LLMInferenceManager
import com.example.personalhealthcareapp.LLMinference.ModelState
import com.example.personalhealthcareapp.chat_managment.Chat
import com.example.personalhealthcareapp.chat_managment.ChatRepository
import com.example.personalhealthcareapp.chat_managment.Conversation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ViewModel(application: Application) : AndroidViewModel(application) {

    val modelState: StateFlow<ModelState> = LLMInferenceManager.modelState

    private val chatRepo = ChatRepository(application)

    // ── Active conversation ──────────────────────────────────────────
    private val _activeConversationId = MutableStateFlow<String?>(null)
    val activeConversationId: StateFlow<String?> = _activeConversationId.asStateFlow()

    private val _chatHistory = MutableStateFlow<List<Chat>>(emptyList())
    val chathistory: StateFlow<List<Chat>> = _chatHistory.asStateFlow()

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    // ── Conversation list for drawer ─────────────────────────────────
    private val _conversations = MutableStateFlow<List<Conversation>>(emptyList())
    val conversations: StateFlow<List<Conversation>> = _conversations.asStateFlow()

    // Track whether the first message in this session has set the title
    private var titleSetForCurrentConversation = false

    init {
        // Migrate legacy single-key data if present
        chatRepo.migrateIfNeeded()

        // Load the conversation list
        _conversations.value = chatRepo.loadConversations()

        // Always start a fresh new chat on app launch
        startNewChat()

        // Load model on IO thread
        viewModelScope.launch(Dispatchers.IO) {
            LLMInferenceManager.initModel(application)
        }

        // Collect partial tokens on Default dispatcher
        viewModelScope.launch(Dispatchers.Default) {
            LLMInferenceManager.partialResult.collect { token ->
                appendToken(token)
            }
        }

        // Collect completion signals
        viewModelScope.launch(Dispatchers.Default) {
            LLMInferenceManager.responseDone.collect {
                finalizeResponse()
            }
        }
    }

    // ── Public API ───────────────────────────────────────────────────

    fun startNewChat() {
        val conv = chatRepo.createConversation()
        _activeConversationId.value = conv.id
        _chatHistory.value = emptyList()
        titleSetForCurrentConversation = false
        // Refresh the list
        _conversations.value = chatRepo.loadConversations()
    }

    fun switchConversation(conversationId: String) {
        if (conversationId == _activeConversationId.value) return

        // Clean up empty current conversation before switching
        cleanUpEmptyConversation()

        _activeConversationId.value = conversationId
        _chatHistory.value = chatRepo.loadMessages(conversationId)
        titleSetForCurrentConversation = true // Existing conversations already have titles
    }

    fun deleteConversation(conversationId: String) {
        chatRepo.deleteConversation(conversationId)
        _conversations.value = chatRepo.loadConversations()

        // If deleting the active conversation, start a new chat
        if (conversationId == _activeConversationId.value) {
            startNewChat()
        }
    }

    fun sendMessage(message: String) {
        if (_isGenerating.value) return
        val trimmed = message.trim()
        if (trimmed.isEmpty()) return

        val convId = _activeConversationId.value ?: return

        _isGenerating.value = true

        // Auto-title the conversation from the first user message
        if (!titleSetForCurrentConversation) {
            val title = trimmed.take(30)
            chatRepo.updateConversationTitle(convId, title)
            titleSetForCurrentConversation = true
            _conversations.value = chatRepo.loadConversations()
        }

        // Update timestamp so it bubbles to top of the list
        chatRepo.updateConversationTimestamp(convId)
        _conversations.value = chatRepo.loadConversations()

        // Add user message + placeholder AI loading bubble
        val updated = _chatHistory.value + listOf(
            Chat(conversationId = convId, text = trimmed, isUser = true),
            Chat(conversationId = convId, text = "", isUser = false, isLoading = true)
        )
        _chatHistory.value = updated
        chatRepo.saveMessages(convId, updated)

        // Fire async generation on IO
        viewModelScope.launch(Dispatchers.IO) {
            val success = LLMInferenceManager.generateResponse(trimmed)
            if (!success) {
                markError("Model is not ready. Please wait.")
            }
        }
    }

    // ── Internal helpers ─────────────────────────────────────────────

    /**
     * Remove a conversation if it was never used (no messages sent).
     */
    private fun cleanUpEmptyConversation() {
        val currentId = _activeConversationId.value ?: return
        if (_chatHistory.value.isEmpty()) {
            chatRepo.deleteConversation(currentId)
            _conversations.value = chatRepo.loadConversations()
        }
    }

    private fun appendToken(token: String) {
        val current = _chatHistory.value.toMutableList()
        if (current.isEmpty()) return

        val lastIndex = current.lastIndex
        val last = current[lastIndex]

        if (!last.isUser) {
            current[lastIndex] = last.copy(
                text = last.text + token,
                isLoading = false
            )
            _chatHistory.value = current
        }
    }

    private fun finalizeResponse() {
        _isGenerating.value = false
        val convId = _activeConversationId.value ?: return

        val current = _chatHistory.value.toMutableList()
        if (current.isEmpty()) return

        val lastIndex = current.lastIndex
        val last = current[lastIndex]
        if (!last.isUser && last.isLoading) {
            current[lastIndex] = last.copy(isLoading = false)
            _chatHistory.value = current
        }
        // Persist completed conversation
        chatRepo.saveMessages(convId, _chatHistory.value)
    }

    private fun markError(errorMsg: String) {
        _isGenerating.value = false
        val current = _chatHistory.value.toMutableList()
        if (current.isEmpty()) return

        val lastIndex = current.lastIndex
        val last = current[lastIndex]
        if (!last.isUser) {
            current[lastIndex] = last.copy(
                text = errorMsg,
                isLoading = false,
                isError = true
            )
            _chatHistory.value = current
        }
    }
}