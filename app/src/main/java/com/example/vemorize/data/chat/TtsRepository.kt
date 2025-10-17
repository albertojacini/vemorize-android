package com.example.vemorize.data.chat

/**
 * Repository for TTS audio generation via Supabase Edge Function
 */
interface TtsRepository {
    /**
     * Generate TTS audio from text using OpenAI gpt-4o-mini-tts
     *
     * @param text The text to synthesize (max 4096 characters)
     * @param speed Speech speed (0.25 to 4.0, default 1.0)
     * @return ByteArray of MP3 audio data
     * @throws TtsException on errors
     */
    suspend fun generateTts(text: String, speed: Float = 1.0f): ByteArray
}

/**
 * Exception thrown when TTS generation fails
 */
class TtsException(
    message: String,
    val details: String? = null,
    cause: Throwable? = null
) : Exception(message, cause)
