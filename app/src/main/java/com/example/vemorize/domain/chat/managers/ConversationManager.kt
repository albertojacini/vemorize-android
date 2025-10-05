package com.example.vemorize.domain.chat.managers

import com.example.vemorize.data.chat.ConversationRepository
import com.example.vemorize.domain.model.chat.Conversation
import com.example.vemorize.domain.model.chat.Message
import com.example.vemorize.domain.model.chat.MessageType
import kotlinx.datetime.Clock
import javax.inject.Inject

/**
 * Manages conversation sessions per course
 */
class ConversationManager(
    private val conversationRepository: ConversationRepository,
    private val userId: String
) {
    private val sessions = mutableMapOf<String, Conversation>()
    private val loadingPromises = mutableMapOf<String, kotlinx.coroutines.Deferred<Conversation>>()

    /**
     * Get or create conversation session for a course
     */
    suspend fun getOrCreate(courseId: String): Conversation {
        // Check cache
        sessions[courseId]?.let { return it }

        // Load from repository
        val conversation = conversationRepository.getOrCreateConversation(userId, courseId)
        sessions[courseId] = conversation
        return conversation
    }

    /**
     * Get current session for a course
     */
    fun getCurrent(courseId: String): Conversation? = sessions[courseId]

    /**
     * Add message to session
     */
    suspend fun addMessage(
        courseId: String,
        role: MessageType,
        content: String,
        additionalKwargs: Map<String, kotlinx.serialization.json.JsonElement>? = null
    ) {
        val conversation = getCurrent(courseId) ?: return

        val message = Message(
            id = generateId(),
            conversationId = conversation.id,
            type = role,
            content = content,
            additionalKwargs = additionalKwargs,
            createdAt = Clock.System.now().toString()
        )

        conversationRepository.addMessage(conversation.id, message)

        // Update conversation metadata
        val updated = conversation.copy(
            messageCount = conversation.messageCount + 1,
            lastMessageAt = message.createdAt
        )
        sessions[courseId] = updated
    }

    /**
     * Save conversation to backend
     */
    suspend fun save(courseId: String) {
        val conversation = getCurrent(courseId) ?: return
        conversationRepository.saveConversation(conversation)
    }

    /**
     * Save periodically (e.g., every 5 messages)
     */
    suspend fun saveIfNeeded(courseId: String) {
        val conversation = getCurrent(courseId) ?: return
        if (conversation.messageCount % 5 == 0) {
            save(courseId)
        }
    }

    /**
     * Create new conversation session
     */
    suspend fun createNew(courseId: String): Conversation {
        save(courseId) // Save current first

        val newConversation = conversationRepository.createConversation(userId, courseId)
        sessions[courseId] = newConversation
        return newConversation
    }

    /**
     * Get conversation history
     */
    suspend fun getHistory(courseId: String, limit: Int = 10): List<Conversation> {
        return conversationRepository.getConversationHistory(userId, courseId, limit)
    }

    /**
     * Check if compression is needed
     */
    fun needsCompression(courseId: String): Boolean {
        return getCurrent(courseId)?.needsCompression() ?: false
    }

    /**
     * Get conversation context for LLM
     */
    fun getConversationContext(courseId: String): String {
        return getCurrent(courseId)?.toPromptContext() ?: ""
    }

    /**
     * Get session ID for LLM persistence
     */
    fun getSessionId(courseId: String): String? {
        return getCurrent(courseId)?.id
    }

    /**
     * Clear cache for a course
     */
    fun clearCache(courseId: String? = null) {
        if (courseId != null) {
            sessions.remove(courseId)
        } else {
            sessions.clear()
        }
    }

    private fun generateId(): String = java.util.UUID.randomUUID().toString()
}
