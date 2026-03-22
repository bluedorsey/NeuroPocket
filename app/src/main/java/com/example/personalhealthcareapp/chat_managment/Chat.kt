package com.example.personalhealthcareapp.chat_managment

data class Chat(
    val conversationId: String = "",
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val isLoading: Boolean = false,
    val isError: Boolean = false
)