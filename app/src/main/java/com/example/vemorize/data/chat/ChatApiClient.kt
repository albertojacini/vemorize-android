package com.example.vemorize.data.chat

import android.util.Log
import com.example.vemorize.domain.model.chat.ApiLLMContext
import com.example.vemorize.domain.model.chat.LLMApiResponse
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
 * API client for chat/LLM operations
 * Ports logic from Next.js /api/conversation route
 */
class ChatApiClient @Inject constructor(
    private val supabaseClient: SupabaseClient,
    private val json: Json
) {
    /**
     * Send LLM request to Next.js API and get tool calls response
     * Matches the contract in /app/api/conversation/route.ts
     */
    suspend fun sendLLMRequest(
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

        Log.d(TAG, "Sending LLM request to $API_BASE_URL/api/conversation")
        Log.d(TAG, "Request body: $requestBody")

        val response: HttpResponse = supabaseClient.httpClient.post("$API_BASE_URL/api/conversation") {
            header(HttpHeaders.Authorization, "Bearer ${session.accessToken}")
            contentType(ContentType.Application.Json)
            setBody(json.encodeToString(requestBody))
        }

        if (!response.status.isSuccess()) {
            val errorBody = response.bodyAsText()
            Log.e(TAG, "LLM request failed: ${response.status} - $errorBody")
            throw Exception("API request failed: ${response.status} - $errorBody")
        }

        val responseBody = response.bodyAsText()
        Log.d(TAG, "LLM response: $responseBody")

        return json.decodeFromString<LLMApiResponse>(responseBody)
    }

    companion object {
        private const val TAG = "ChatApiClient"
        private const val API_BASE_URL = "http://localhost:3000"
    }
}
