package com.example.vemorize.domain.chat.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

/**
 * Manages voice input using Android SpeechRecognizer API
 */
class VoiceInputManager(private val context: Context) {

    private var speechRecognizer: SpeechRecognizer? = null

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _recognizedText = MutableStateFlow<String?>(null)
    val recognizedText: StateFlow<String?> = _recognizedText.asStateFlow()

    private val _partialText = MutableStateFlow<String?>(null)
    val partialText: StateFlow<String?> = _partialText.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    /**
     * Initialize the speech recognizer
     */
    fun initialize() {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            _error.value = "Speech recognition not available on this device"
            return
        }

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
            setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    Log.d(TAG, "Ready for speech")
                    _isListening.value = true
                    _error.value = null
                }

                override fun onBeginningOfSpeech() {
                    Log.d(TAG, "Beginning of speech")
                }

                override fun onRmsChanged(rmsdB: Float) {
                    // Audio level changed - could use for visual feedback
                }

                override fun onBufferReceived(buffer: ByteArray?) {
                    // Audio data received
                }

                override fun onEndOfSpeech() {
                    Log.d(TAG, "End of speech")
                    _isListening.value = false
                }

                override fun onError(error: Int) {
                    Log.e(TAG, "Speech recognition error: $error")
                    _isListening.value = false
                    _error.value = getErrorMessage(error)
                }

                override fun onResults(results: Bundle?) {
                    Log.d(TAG, "Speech recognition results received")
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    if (!matches.isNullOrEmpty()) {
                        val recognizedText = matches[0]
                        Log.d(TAG, "Recognized: $recognizedText")
                        _recognizedText.value = recognizedText
                        _partialText.value = null
                    }
                    _isListening.value = false
                }

                override fun onPartialResults(partialResults: Bundle?) {
                    val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    if (!matches.isNullOrEmpty()) {
                        val partial = matches[0]
                        Log.d(TAG, "Partial: $partial")
                        _partialText.value = partial
                    }
                }

                override fun onEvent(eventType: Int, params: Bundle?) {
                    // Reserved for future use
                }
            })
        }

        Log.d(TAG, "VoiceInputManager initialized")
    }

    /**
     * Start listening for voice input
     */
    fun startListening(language: String = "en-US") {
        if (speechRecognizer == null) {
            _error.value = "Speech recognizer not initialized"
            return
        }

        if (_isListening.value) {
            Log.d(TAG, "Already listening")
            return
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, language)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, language)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }

        _recognizedText.value = null
        _partialText.value = null
        _error.value = null

        Log.d(TAG, "Starting speech recognition (language: $language)")
        speechRecognizer?.startListening(intent)
    }

    /**
     * Stop listening
     */
    fun stopListening() {
        if (_isListening.value) {
            Log.d(TAG, "Stopping speech recognition")
            speechRecognizer?.stopListening()
            _isListening.value = false
        }
    }

    /**
     * Cancel listening (won't trigger onResults)
     */
    fun cancel() {
        if (_isListening.value) {
            Log.d(TAG, "Canceling speech recognition")
            speechRecognizer?.cancel()
            _isListening.value = false
            _partialText.value = null
        }
    }

    /**
     * Clear the last recognized text
     */
    fun clearRecognizedText() {
        _recognizedText.value = null
    }

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
        Log.d(TAG, "Destroying VoiceInputManager")
        speechRecognizer?.destroy()
        speechRecognizer = null
        _isListening.value = false
    }

    private fun getErrorMessage(error: Int): String {
        return when (error) {
            SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
            SpeechRecognizer.ERROR_CLIENT -> "Client side error"
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Insufficient permissions"
            SpeechRecognizer.ERROR_NETWORK -> "Network error"
            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
            SpeechRecognizer.ERROR_NO_MATCH -> "No speech match"
            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Recognition service busy"
            SpeechRecognizer.ERROR_SERVER -> "Server error"
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech input"
            else -> "Unknown error: $error"
        }
    }

    companion object {
        private const val TAG = "VoiceInputManager"
    }
}
