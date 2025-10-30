package com.example.vemorize.domain.chat.voice

/**
 * Interface for TTS providers
 * Abstracts TTS implementation (local Android TTS or cloud-based)
 */
interface TtsProvider {
    /**
     * Initialize the TTS provider
     * @param onInitialized Callback invoked when initialization completes
     */
    fun initialize(onInitialized: (() -> Unit)? = null)

    /**
     * Speak the given text
     * @param text Text to synthesize
     * @param speed Speech rate (0.25 to 4.0)
     * @param language Language code (e.g., "en-US", "de-DE")
     * @param onStart Callback when speaking starts
     * @param onComplete Callback when speaking completes
     * @param onError Callback when error occurs
     */
    suspend fun speak(
        text: String,
        speed: Float = 1.0f,
        language: String? = null,
        onStart: (() -> Unit)? = null,
        onComplete: (() -> Unit)? = null,
        onError: ((String) -> Unit)? = null
    )

    /**
     * Stop speaking immediately
     */
    fun stop()

    /**
     * Check if provider is ready
     */
    fun isReady(): Boolean

    /**
     * Check if currently speaking
     */
    fun isSpeaking(): Boolean

    /**
     * Clean up resources
     */
    fun destroy()
}
