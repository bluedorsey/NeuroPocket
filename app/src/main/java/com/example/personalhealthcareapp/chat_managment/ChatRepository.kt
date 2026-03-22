package com.example.personalhealthcareapp.chat_managment

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class ChatRepository(context: Context) {

    private val prefs = context.getSharedPreferences("chat_history", Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        private const val MAX_MESSAGES = 50
        private const val MAX_CONVERSATIONS = 50
        private const val CONVERSATIONS_KEY = "conversations_list"
        private fun messagesKey(conversationId: String) = "chat_$conversationId"
    }

    // ── Conversation CRUD ────────────────────────────────────────────

    fun createConversation(): Conversation {
        val conversation = Conversation()
        val conversations = loadConversations().toMutableList()

        // Enforce max conversations – remove oldest
        while (conversations.size >= MAX_CONVERSATIONS) {
            val oldest = conversations.removeLastOrNull() ?: break
            deleteMessages(oldest.id)
        }

        conversations.add(0, conversation)
        saveConversationList(conversations)
        return conversation
    }

    fun loadConversations(): List<Conversation> {
        val json = prefs.getString(CONVERSATIONS_KEY, null) ?: return emptyList()
        return try {
            val type = object : TypeToken<List<Conversation>>() {}.type
            gson.fromJson(json, type)
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun updateConversationTitle(conversationId: String, title: String) {
        val conversations = loadConversations().toMutableList()
        val index = conversations.indexOfFirst { it.id == conversationId }
        if (index != -1) {
            conversations[index] = conversations[index].copy(
                title = title,
                updatedAt = System.currentTimeMillis()
            )
            saveConversationList(conversations)
        }
    }

    fun updateConversationTimestamp(conversationId: String) {
        val conversations = loadConversations().toMutableList()
        val index = conversations.indexOfFirst { it.id == conversationId }
        if (index != -1) {
            val updated = conversations.removeAt(index).copy(
                updatedAt = System.currentTimeMillis()
            )
            conversations.add(0, updated) // Move to top
            saveConversationList(conversations)
        }
    }

    fun deleteConversation(conversationId: String) {
        val conversations = loadConversations().toMutableList()
        conversations.removeAll { it.id == conversationId }
        saveConversationList(conversations)
        deleteMessages(conversationId)
    }

    private fun saveConversationList(conversations: List<Conversation>) {
        prefs.edit().putString(CONVERSATIONS_KEY, gson.toJson(conversations)).apply()
    }

    // ── Messages CRUD ────────────────────────────────────────────────

    fun saveMessages(conversationId: String, chats: List<Chat>) {
        val toSave = chats
            .filter { !it.isLoading && !it.isError }
            .takeLast(MAX_MESSAGES)

        val json = gson.toJson(toSave)
        prefs.edit().putString(messagesKey(conversationId), json).apply()
    }

    fun loadMessages(conversationId: String): List<Chat> {
        val json = prefs.getString(messagesKey(conversationId), null) ?: return emptyList()
        return try {
            val type = object : TypeToken<List<Chat>>() {}.type
            gson.fromJson(json, type)
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun deleteMessages(conversationId: String) {
        prefs.edit().remove(messagesKey(conversationId)).apply()
    }

    // ── Legacy migration ─────────────────────────────────────────────

    /**
     * One-time migration from the old single-key "messages" format.
     * Moves existing messages into a new conversation, then removes the old key.
     */
    fun migrateIfNeeded(): String? {
        val oldJson = prefs.getString("messages", null) ?: return null
        return try {
            val type = object : TypeToken<List<Chat>>() {}.type
            val oldMessages: List<Chat> = gson.fromJson(oldJson, type)
            if (oldMessages.isEmpty()) {
                prefs.edit().remove("messages").apply()
                return null
            }
            val conversation = createConversation()
            val titled = if (oldMessages.isNotEmpty()) {
                val firstUserMsg = oldMessages.firstOrNull { it.isUser }?.text
                if (firstUserMsg != null) {
                    conversation.copy(title = firstUserMsg.take(30))
                } else conversation
            } else conversation

            // Update the conversation title
            updateConversationTitle(titled.id, titled.title)

            // Tag messages with conversation ID and save
            val taggedMessages = oldMessages.map { it.copy(conversationId = titled.id) }
            saveMessages(titled.id, taggedMessages)

            // Remove old key
            prefs.edit().remove("messages").apply()
            titled.id
        } catch (e: Exception) {
            prefs.edit().remove("messages").apply()
            null
        }
    }
}
