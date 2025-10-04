package com.example.vemorize.domain.chat.voice

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

/**
 * Manages voice output using Android TextToSpeech API
 */
class VoiceOutputManager(private val context: Context) {

    private var tts: TextToSpeech? = null
    private var isInitialized = false

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    /**
     * Callback invoked when TTS initialization completes
     */
    var onInitialized: (() -> Unit)? = null

    /**
     * Callback invoked when speaking finishes
     */
    var onSpeakingFinished: (() -> Unit)? = null

    /**
     * Initialize TextToSpeech
     */
    fun initialize() {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                Log.d(TAG, "TextToSpeech initialized successfully")

                // Set default language to US English
                val result = tts?.setLanguage(Locale.US)

                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e(TAG, "Language not supported")
                    _error.value = "Language not supported"
                    isInitialized = false
                } else {
                    isInitialized = true
                    onInitialized?.invoke()
                }

                // Set up utterance progress listener
                tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        Log.d(TAG, "Started speaking: $utteranceId")
                        _isSpeaking.value = true
                    }

                    override fun onDone(utteranceId: String?) {
                        Log.d(TAG, "Finished speaking: $utteranceId")
                        _isSpeaking.value = false
                        onSpeakingFinished?.invoke()
                    }

                    override fun onError(utteranceId: String?) {
                        Log.e(TAG, "Error speaking: $utteranceId")
                        _isSpeaking.value = false
                        _error.value = "Speech synthesis error"
                    }
                })
            } else {
                Log.e(TAG, "TextToSpeech initialization failed")
                _error.value = "Failed to initialize text-to-speech"
                isInitialized = false
            }
        }
    }

    /**
     * Speak the given text
     *
     * @param text Text to speak
     * @param speed Speech rate (0.5 - 2.0, default 1.0)
     * @param language Language code (e.g., "en-US", "de-DE")
     */
    fun speak(text: String, speed: Float = 1.0f, language: String? = null) {
        if (!isInitialized) {
            Log.e(TAG, "TextToSpeech not initialized")
            _error.value = "Text-to-speech not initialized"
            return
        }

        if (text.isBlank()) {
            Log.w(TAG, "Cannot speak empty text")
            return
        }

        // Set language if provided
        if (language != null) {
            val locale = Locale.forLanguageTag(language)
            val result = tts?.setLanguage(locale)

            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.w(TAG, "Language $language not supported, using default")
            }
        }

        // Set speech rate (clamp between 0.5 and 2.0)
        val clampedSpeed = speed.coerceIn(0.5f, 2.0f)
        tts?.setSpeechRate(clampedSpeed)

        // Speak
        Log.d(TAG, "Speaking: \"$text\" (speed: $clampedSpeed, language: $language)")
        _error.value = null

        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, text.hashCode().toString())
    }

    /**
     * Stop speaking immediately
     */
    fun stop() {
        if (isInitialized && _isSpeaking.value) {
            Log.d(TAG, "Stopping speech")
            tts?.stop()
            _isSpeaking.value = false
        }
    }

    /**
     * Check if TTS is initialized and ready
     */
    fun isReady(): Boolean = isInitialized

    /**
     * Clear error
     */
    fun clearError() {
        _error.value = null
    }

    /**
     * Clean up resources
     */
    fun destroy() {
        Log.d(TAG, "Destroying VoiceOutputManager")
        tts?.stop()
        tts?.shutdown()
        tts = null
        isInitialized = false
        _isSpeaking.value = false
    }

    companion object {
        private const val TAG = "VoiceOutputManager"
    }
}
