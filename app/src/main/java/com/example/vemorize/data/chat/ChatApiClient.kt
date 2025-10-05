package com.example.vemorize.data.chat

import android.util.Log
import com.example.vemorize.domain.model.chat.LLMApiResponse
import com.example.vemorize.domain.model.chat.LLMRequest
import com.example.vemorize.domain.model.chat.Message
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject
import javax.inject.Inject

/**
 * API client for chat/LLM operations
 */
class ChatApiClient(
    private val supabaseClient: SupabaseClient,
    private val json: Json
) {
    /**
     * Send LLM request and get tool calls response
     */
    suspend fun sendLLMRequest(
        request: LLMRequest,
        conversationHistory: List<Message> = emptyList()
    ): LLMApiResponse {
        return try {
            val session = supabaseClient.auth.currentSessionOrNull()
                ?: throw IllegalStateException("No active session")

            // Build request body following the Next.js API contract
            val requestBody = buildJsonObject {
                putJsonObject("llmContext") {
                    put("userMessage", request.userMessage)
                    request.systemPrompt?.let { put("systemPrompt", it) }
                }
                putJsonObject("data") {
                    put("courseId", request.courseId)
                    put("userId", request.userId)
                }
            }

            val response: HttpResponse = supabaseClient.httpClient.post("https://your-api-domain.com/api/conversation") { // TODO: Replace with actual API URL
                header(HttpHeaders.Authorization, "Bearer ${session.accessToken}")
                contentType(ContentType.Application.Json)
                setBody(requestBody.toString())
            }

            if (response.status.isSuccess()) {
                val responseBody = response.bodyAsText()
                json.decodeFromString<LLMApiResponse>(responseBody)
            } else {
                Log.e(TAG, "LLM request failed: ${response.status}")
                LLMApiResponse(emptyList())
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error sending LLM request", e)
            LLMApiResponse(emptyList())
        }
    }

    companion object {
        private const val TAG = "ChatApiClient"
    }
}
