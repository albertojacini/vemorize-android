package com.example.vemorize.data.chat

import android.util.Log
import com.example.vemorize.data.dto.vemorize_api.TtsErrorResponse
import com.example.vemorize.data.dto.vemorize_api.TtsRequest
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import javax.inject.Inject

/**
 * Implementation of TtsRepository using Vemorize Backend API (Supabase Edge Function)
 * Calls the /tts endpoint which proxies to OpenAI gpt-4o-mini-tts
 */
class VemorizeApiTtsRepositoryImpl @Inject constructor(
    private val supabaseClient: SupabaseClient,
    private val json: Json
) : TtsRepository {

    override suspend fun generateTts(text: String, speed: Float): ByteArray {
        val session = supabaseClient.auth.currentSessionOrNull()
            ?: throw TtsException("No active session")

        // Validate input
        if (text.isEmpty()) {
            throw TtsException("Text is required")
        }
        if (text.length > 4096) {
            throw TtsException("Text too long (max 4096 characters)")
        }
        if (speed < 0.25f || speed > 4.0f) {
            throw TtsException("Speed must be between 0.25 and 4.0")
        }

        val requestBody = TtsRequest(
            text = text,
            speed = speed
        )

        Log.d(TAG, "Generating TTS for ${text.length} characters at speed $speed")

        try {
            val response: HttpResponse = supabaseClient.httpClient.post("$API_BASE_URL/functions/v1/tts") {
                header(HttpHeaders.Authorization, "Bearer ${session.accessToken}")
                contentType(ContentType.Application.Json)
                setBody(json.encodeToString(requestBody))
            }

            Log.d(TAG, "TTS response status: ${response.status}")

            if (!response.status.isSuccess()) {
                // Try to parse error response
                val errorBody = response.bodyAsText()
                Log.e(TAG, "TTS request failed: ${response.status} - $errorBody")

                try {
                    val errorResponse = json.decodeFromString<TtsErrorResponse>(errorBody)
                    throw TtsException(
                        errorResponse.error,
                        errorResponse.details
                    )
                } catch (e: Exception) {
                    // Fallback if can't parse error
                    throw TtsException(
                        "TTS request failed: ${response.status}",
                        errorBody,
                        e
                    )
                }
            }

            // Response should be audio/mpeg
            val audioBytes = response.readBytes()
            Log.d(TAG, "Received ${audioBytes.size} bytes of MP3 audio")

            if (audioBytes.isEmpty()) {
                throw TtsException("Received empty audio response")
            }

            return audioBytes
        } catch (e: TtsException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "TTS generation failed", e)
            throw TtsException(
                "TTS generation failed",
                e.message,
                e
            )
        }
    }

    companion object {
        private const val TAG = "VemorizeApiTtsRepositoryImpl"
        private const val API_BASE_URL = "http://10.0.2.2:54321"
    }
}
