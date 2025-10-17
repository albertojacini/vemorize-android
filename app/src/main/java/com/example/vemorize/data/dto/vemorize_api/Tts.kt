package com.example.vemorize.data.dto.vemorize_api

import kotlinx.serialization.Serializable

/**
 * TTS API DTOs for Vemorize Backend API (Supabase Edge Functions)
 * Matches backend types in tts.ts
 */

/**
 * TTS request payload
 */
@Serializable
data class TtsRequest(
    val text: String,
    val speed: Float = 1.0f
)

/**
 * TTS error response
 */
@Serializable
data class TtsErrorResponse(
    val error: String,
    val details: String? = null
)
