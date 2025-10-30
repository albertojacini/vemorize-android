package com.example.vemorize.domain.chat.voice

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import java.util.Locale
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Local TTS provider using Android TextToSpeech API
 */
class LocalTtsProvider(private val context: Context) : TtsProvider {

    private var tts: TextToSpeech? = null
    private var isInitialized = false
    private var currentlySpeaking = false

    private var onInitializedCallback: (() -> Unit)? = null

    override fun initialize(onInitialized: (() -> Unit)?) {
        this.onInitializedCallback = onInitialized

        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                Log.d(TAG, "TextToSpeech initialized successfully")

                // Set default language to US English
                val result = tts?.setLanguage(Locale.US)

                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e(TAG, "Language not supported")
                    isInitialized = false
                } else {
                    isInitialized = true
                    onInitializedCallback?.invoke()
                }
            } else {
                Log.e(TAG, "TextToSpeech initialization failed")
                isInitialized = false
            }
        }
    }

    override suspend fun speak(
        text: String,
        speed: Float,
        language: String?,
        onStart: (() -> Unit)?,
        onComplete: (() -> Unit)?,
        onError: ((String) -> Unit)?
    ) {
        if (!isInitialized) {
            Log.e(TAG, "TextToSpeech not initialized")
            onError?.invoke("Text-to-speech not initialized")
            return
        }

        if (text.isBlank()) {
            Log.w(TAG, "Cannot speak empty text")
            onError?.invoke("Cannot speak empty text")
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

        // Set speech rate (clamp between 0.5 and 2.0 for Android TTS)
        val clampedSpeed = speed.coerceIn(0.5f, 2.0f)
        tts?.setSpeechRate(clampedSpeed)

        // AWAIT TTS completion using suspendCancellableCoroutine
        val utteranceId = text.hashCode().toString()
        Log.d(TAG, "Speaking: \"$text\" (speed: $clampedSpeed, language: $language)")

        suspendCancellableCoroutine { continuation ->
            // Set up utterance progress listener for this specific utterance
            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    Log.d(TAG, "Started speaking: $utteranceId")
                    currentlySpeaking = true
                    onStart?.invoke()
                }

                override fun onDone(utteranceId: String?) {
                    Log.d(TAG, "Finished speaking: $utteranceId")
                    currentlySpeaking = false
                    onComplete?.invoke()
                    // Resume coroutine ONLY when TTS finishes
                    continuation.resume(Unit)
                }

                override fun onError(utteranceId: String?) {
                    Log.e(TAG, "Error speaking: $utteranceId")
                    currentlySpeaking = false
                    val errorMessage = "Speech synthesis error"
                    onError?.invoke(errorMessage)
                    // Resume with exception on error
                    continuation.resumeWithException(Exception(errorMessage))
                }
            })

            // Start speaking
            val result = tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)

            if (result != TextToSpeech.SUCCESS) {
                Log.e(TAG, "Failed to queue speech")
                currentlySpeaking = false
                val errorMessage = "Failed to start speech synthesis"
                onError?.invoke(errorMessage)
                continuation.resumeWithException(Exception(errorMessage))
            }

            // Handle cancellation
            continuation.invokeOnCancellation {
                Log.d(TAG, "Speech cancelled")
                tts?.stop()
                currentlySpeaking = false
            }
        }
    }

    override fun stop() {
        if (isInitialized && currentlySpeaking) {
            Log.d(TAG, "Stopping speech")
            tts?.stop()
            currentlySpeaking = false
        }
    }

    override fun isReady(): Boolean = isInitialized

    override fun isSpeaking(): Boolean = currentlySpeaking

    override fun destroy() {
        Log.d(TAG, "Destroying LocalTtsProvider")
        tts?.stop()
        tts?.shutdown()
        tts = null
        isInitialized = false
        currentlySpeaking = false
    }

    companion object {
        private const val TAG = "LocalTtsProvider"
    }
}
