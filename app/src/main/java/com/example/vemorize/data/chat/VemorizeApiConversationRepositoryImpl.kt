package com.example.vemorize.data.chat

import com.example.vemorize.data.clients.vemorizeApi.VemorizeApiClient
import com.example.vemorize.data.clients.vemorizeApi.dto.ApiLLMContext
import com.example.vemorize.data.clients.vemorizeApi.dto.LLMApiResponse
import javax.inject.Inject

/**
 * Implementation of ConversationRepository using Vemorize Backend API
 *
 * Delegates to VemorizeApiClient for actual HTTP communication.
 */
class VemorizeApiConversationRepositoryImpl @Inject constructor(
    private val vemorizeApiClient: VemorizeApiClient
) : ConversationRepository {

    override suspend fun sendLLMRequest(
        llmContext: ApiLLMContext,
        courseId: String,
        userId: String
    ): LLMApiResponse {
        return vemorizeApiClient.sendLLMRequest(llmContext, courseId, userId)
    }
}
