package com.example.vemorize.data.chat

import android.util.Log
import com.example.vemorize.data.dto.vemorize_api.ApiLLMContext
import com.example.vemorize.data.dto.vemorize_api.LLMApiResponse
import com.example.vemorize.data.dto.vemorize_api.LLMRequest
import com.example.vemorize.data.dto.vemorize_api.RequestData
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.json.Json
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
        val startTime = System.currentTimeMillis()
        Log.d(TAG, "═══════════════════════════════════════════════════════════")
        Log.d(TAG, "▶ [START] LLM Request")
        Log.d(TAG, "  Timestamp: $startTime")
        Log.d(TAG, "  Mode: ${llmContext.mode}")
        Log.d(TAG, "  User Message: '${llmContext.userMessage}'")
        Log.d(TAG, "  Tool Names: ${llmContext.toolNames}")

        val session = supabaseClient.auth.currentSessionOrNull()
            ?: throw IllegalStateException("No active session")

        // Build request body matching ApiLLMRequestSchema
        val requestBody = LLMRequest(
            data = RequestData(
                courseId = courseId,
                userId = userId
            ),
            llmContext = llmContext
        )

        val requestBodyJson = json.encodeToString(requestBody)
        Log.d(TAG, "  URL: $API_BASE_URL/functions/v1/chat-llm")
        Log.d(TAG, "  Request Body (JSON):")
        Log.d(TAG, requestBodyJson)

        try {
            val beforeRequestTime = System.currentTimeMillis()
            Log.d(TAG, "  ⏳ Sending HTTP POST... (t+${beforeRequestTime - startTime}ms)")

            val response: HttpResponse = supabaseClient.httpClient.post("$API_BASE_URL/functions/v1/chat-llm") {
                header(HttpHeaders.Authorization, "Bearer ${session.accessToken}")
                contentType(ContentType.Application.Json)
                setBody(requestBodyJson)
            }

            val afterRequestTime = System.currentTimeMillis()
            val requestDuration = afterRequestTime - beforeRequestTime
            Log.d(TAG, "  ✓ HTTP Response received (took ${requestDuration}ms)")

            val responseBody = response.bodyAsText()
            val afterBodyReadTime = System.currentTimeMillis()
            val bodyReadDuration = afterBodyReadTime - afterRequestTime

            Log.d(TAG, "  Response Status: ${response.status}")
            Log.d(TAG, "  Body read time: ${bodyReadDuration}ms")
            Log.d(TAG, "  Response Body (${responseBody.length} chars):")
            Log.d(TAG, responseBody)

            if (!response.status.isSuccess()) {
                val totalTime = System.currentTimeMillis() - startTime
                Log.e(TAG, "✗ [FAILED] LLM request failed (${response.status}) after ${totalTime}ms")
                Log.e(TAG, "  Response: $responseBody")
                Log.e(TAG, "═══════════════════════════════════════════════════════════")
                throw Exception("API request failed: ${response.status} - $responseBody")
            }

            val apiResponse = json.decodeFromString<LLMApiResponse>(responseBody)
            val afterParseTime = System.currentTimeMillis()
            val parseDuration = afterParseTime - afterBodyReadTime

            Log.d(TAG, "  JSON parse time: ${parseDuration}ms")
            Log.d(TAG, "  API Success: ${apiResponse.success}")
            Log.d(TAG, "  Tool Calls Count: ${apiResponse.data?.toolCalls?.size ?: 0}")

            if (apiResponse.data?.toolCalls?.isNotEmpty() == true) {
                Log.d(TAG, "  Tool Calls Detail:")
                apiResponse.data.toolCalls.forEachIndexed { index, toolCall ->
                    Log.d(TAG, "    [$index] Tool: ${toolCall.tool}")
                    Log.d(TAG, "        Args: ${toolCall.args}")
                }
            }

            if (!apiResponse.success) {
                val totalTime = System.currentTimeMillis() - startTime
                Log.e(TAG, "✗ [FAILED] API returned error after ${totalTime}ms: ${apiResponse.error}")
                Log.e(TAG, "═══════════════════════════════════════════════════════════")
                throw Exception("API error: ${apiResponse.error}")
            }

            if (apiResponse.data == null) {
                val totalTime = System.currentTimeMillis() - startTime
                Log.e(TAG, "✗ [FAILED] API returned no data after ${totalTime}ms")
                Log.e(TAG, "═══════════════════════════════════════════════════════════")
                throw Exception("API returned no data")
            }

            val totalTime = System.currentTimeMillis() - startTime
            Log.d(TAG, "◀ [SUCCESS] LLM Request completed in ${totalTime}ms")
            Log.d(TAG, "  Breakdown: Request=${requestDuration}ms, BodyRead=${bodyReadDuration}ms, Parse=${parseDuration}ms")
            Log.d(TAG, "═══════════════════════════════════════════════════════════")

            return apiResponse

        } catch (e: Exception) {
            val totalTime = System.currentTimeMillis() - startTime
            Log.e(TAG, "✗ [EXCEPTION] LLM Request failed after ${totalTime}ms")
            Log.e(TAG, "  Exception: ${e.javaClass.simpleName}")
            Log.e(TAG, "  Message: ${e.message}")
            Log.e(TAG, "  Stack trace:", e)
            Log.e(TAG, "═══════════════════════════════════════════════════════════")
            throw e
        }
    }

    companion object {
        private const val TAG = "VemorizeApiConversationRepositoryImpl"
        private const val API_BASE_URL = "http://localhost:54321"
    }
}
