package com.example.vemorize.ui.chat

/**
 * UI-only message for displaying voice/text exchanges in the chat screen.
 * These are ephemeral and NOT persisted to the database.
 *
 * This is separate from domain.model.chat.Message which is used for LLM conversation history.
 */
data class VoiceExchangeMessage(
    val content: String,
    val isFromUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)
