package com.example.vemorize.data.chat

import com.example.vemorize.domain.model.chat.Conversation
import com.example.vemorize.domain.model.chat.Message
import kotlinx.coroutines.flow.Flow

interface ConversationRepository {
    /**
     * Get or create active conversation for a course
     */
    suspend fun getOrCreateConversation(userId: String, courseId: String): Conversation

    /**
     * Create a new conversation
     */
    suspend fun createConversation(userId: String, courseId: String): Conversation

    /**
     * Get conversation by ID
     */
    suspend fun getConversationById(conversationId: String): Conversation?

    /**
     * Save/update conversation
     */
    suspend fun saveConversation(conversation: Conversation)

    /**
     * Get conversation history for a course
     */
    suspend fun getConversationHistory(userId: String, courseId: String, limit: Int = 10): List<Conversation>

    /**
     * Get messages for a conversation
     */
    suspend fun getConversationMessages(conversationId: String, limit: Int? = null): List<Message>

    /**
     * Add message to conversation
     */
    suspend fun addMessage(conversationId: String, message: Message)

    /**
     * Observe messages for a conversation
     */
    fun observeMessages(conversationId: String): Flow<List<Message>>
}
