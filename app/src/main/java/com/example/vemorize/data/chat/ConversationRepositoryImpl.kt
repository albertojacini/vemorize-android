package com.example.vemorize.data.chat

import android.util.Log
import com.example.vemorize.domain.model.chat.Conversation
import com.example.vemorize.domain.model.chat.Message
import io.github.jan.supabase.postgrest.Postgrest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.Clock
import javax.inject.Inject

class ConversationRepositoryImpl @Inject constructor(
    private val postgrest: Postgrest
) : ConversationRepository {

    override suspend fun getOrCreateConversation(userId: String, courseId: String): Conversation {
        return try {
            // Try to get active conversation
            val existing = postgrest
                .from("conversations")
                .select {
                    filter {
                        eq("user_id", userId)
                        eq("course_id", courseId)
                        eq("is_active", true)
                    }
                }
                .decodeSingleOrNull<Conversation>()

            existing ?: createConversation(userId, courseId)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting or creating conversation", e)
            throw e
        }
    }

    override suspend fun createConversation(userId: String, courseId: String): Conversation {
        return try {
            val now = Clock.System.now().toString()

            postgrest
                .from("conversations")
                .insert(Conversation(
                    id = java.util.UUID.randomUUID().toString(),
                    userId = userId,
                    courseId = courseId,
                    summary = null,
                    messageCount = 0,
                    createdAt = now,
                    updatedAt = now,
                    lastMessageAt = null,
                    isActive = true
                ))
                .decodeSingle<Conversation>()
        } catch (e: Exception) {
            Log.e(TAG, "Error creating conversation", e)
            throw e
        }
    }

    override suspend fun getConversationById(conversationId: String): Conversation? {
        return try {
            postgrest
                .from("conversations")
                .select {
                    filter {
                        eq("id", conversationId)
                    }
                }
                .decodeSingleOrNull<Conversation>()
        } catch (e: Exception) {
            Log.e(TAG, "Error getting conversation by id", e)
            null
        }
    }

    override suspend fun saveConversation(conversation: Conversation) {
        try {
            val updated = conversation.copy(
                updatedAt = Clock.System.now().toString()
            )

            postgrest
                .from("conversations")
                .update(updated) {
                    filter {
                        eq("id", conversation.id)
                    }
                }
        } catch (e: Exception) {
            Log.e(TAG, "Error saving conversation", e)
            throw e
        }
    }

    override suspend fun getConversationHistory(
        userId: String,
        courseId: String,
        limit: Int
    ): List<Conversation> {
        return try {
            postgrest
                .from("conversations")
                .select {
                    filter {
                        eq("user_id", userId)
                        eq("course_id", courseId)
                    }
                    order(column = "created_at", order = io.github.jan.supabase.postgrest.query.Order.DESCENDING)
                    limit(limit.toLong())
                }
                .decodeList<Conversation>()
        } catch (e: Exception) {
            Log.e(TAG, "Error getting conversation history", e)
            emptyList()
        }
    }

    override suspend fun getConversationMessages(
        conversationId: String,
        limit: Int?
    ): List<Message> {
        return try {
            val query = postgrest
                .from("messages")
                .select {
                    filter {
                        eq("conversation_id", conversationId)
                    }
                    order(column = "created_at", order = io.github.jan.supabase.postgrest.query.Order.ASCENDING)
                    if (limit != null) {
                        limit(limit.toLong())
                    }
                }

            query.decodeList<Message>()
        } catch (e: Exception) {
            Log.e(TAG, "Error getting conversation messages", e)
            emptyList()
        }
    }

    override suspend fun addMessage(conversationId: String, message: Message) {
        try {
            postgrest
                .from("messages")
                .insert(message)
        } catch (e: Exception) {
            Log.e(TAG, "Error adding message", e)
            throw e
        }
    }

    override fun observeMessages(conversationId: String): Flow<List<Message>> = flow {
        // Initial fetch
        val messages = getConversationMessages(conversationId)
        emit(messages)

        // TODO: Add realtime subscription when needed
    }

    companion object {
        private const val TAG = "ConversationRepository"
    }
}
