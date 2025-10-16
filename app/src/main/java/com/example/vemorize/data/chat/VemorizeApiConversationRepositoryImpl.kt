package com.example.vemorize.data.chat

import android.util.Log
import com.example.vemorize.data.dto.vemorize_api.ApiLLMContext
import com.example.vemorize.data.dto.vemorize_api.LLMApiResponse
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.*
import kotlinx.serialization.encodeToString
import javax.inject.Inject

/**
 * Implementation of ConversationRepository using Vemorize Backend API
 *
 * Handles communication with the Vemorize API layer (Supabase Edge Functions).
 */
class VemorizeApiConversationRepositoryImpl @Inject constructor(
    private val supabaseClient: SupabaseClient,
    private val json: Json
) : ConversationRepository {

    override suspend fun sendLLMRequest(
        llmContext: ApiLLMContext,
        courseId: String,
        userId: String
    ): LLMApiResponse {
        val session = supabaseClient.auth.currentSessionOrNull()
            ?: throw IllegalStateException("No active session")

        // Build request body matching ApiLLMRequestSchema
        val requestBody = buildJsonObject {
            putJsonObject("llmContext") {
                put("userMessage", llmContext.userMessage)
                putJsonArray("toolNames") {
                    llmContext.toolNames.forEach { add(it) }
                }
                put("mode", llmContext.mode)
                llmContext.userMemory?.let { put("userMemory", it) }
                llmContext.leafReprForPrompt?.let { put("leafReprForPrompt", it) }
            }
            putJsonObject("data") {
                put("courseId", courseId)
                put("userId", userId)
            }
        }

        Log.d(TAG, "Sending LLM request to $API_BASE_URL/functions/v1/chat-llm")
        Log.d(TAG, "Request body: $requestBody")

        val response: HttpResponse = supabaseClient.httpClient.post("$API_BASE_URL/functions/v1/chat-llm") {
            header(HttpHeaders.Authorization, "Bearer ${session.accessToken}")
            contentType(ContentType.Application.Json)
            setBody(json.encodeToString(requestBody))
        }

        val responseBody = response.bodyAsText()
        Log.d(TAG, "LLM response status: ${response.status}")
        Log.d(TAG, "LLM response body: $responseBody")

        if (!response.status.isSuccess()) {
            Log.e(TAG, "LLM request failed: ${response.status} - $responseBody")
            throw Exception("API request failed: ${response.status} - $responseBody")
        }

        val apiResponse = json.decodeFromString<LLMApiResponse>(responseBody)

        if (!apiResponse.success) {
            Log.e(TAG, "API returned error: ${apiResponse.error}")
            throw Exception("API error: ${apiResponse.error}")
        }

        if (apiResponse.data == null) {
            Log.e(TAG, "API returned no data")
            throw Exception("API returned no data")
        }

        return apiResponse
    }

    companion object {
        private const val TAG = "VemorizeApiConversationRepositoryImpl"
        private const val API_BASE_URL = "http://10.0.2.2:54321"
    }
}
