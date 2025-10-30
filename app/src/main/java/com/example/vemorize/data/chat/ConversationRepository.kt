package com.example.vemorize.data.chat

import com.example.vemorize.data.dto.vemorize_api.ApiLLMContext
import com.example.vemorize.data.dto.vemorize_api.LLMApiResponse

/**
 * Repository for conversation/LLM operations
 *
 * Abstracts the backend API for chat interactions, allowing domain layer
 * to depend on an interface rather than concrete implementation.
 */
interface ConversationRepository {
    /**
     * Send LLM request and get tool calls response
     */
    suspend fun sendLLMRequest(
        llmContext: ApiLLMContext,
        courseId: String,
        userId: String
    ): LLMApiResponse
}
