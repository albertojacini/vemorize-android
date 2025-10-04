package com.example.vemorize.domain.chat.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
    private val handler = Handler(Looper.getMainLooper())
    private var timeoutRunnable: Runnable? = null

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
        // Note: SpeechRecognizer.isRecognitionAvailable() can return false even when
        // speech recognition is available. We'll try to create the recognizer anyway
        // and handle actual errors when starting to listen.
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            Log.w(TAG, "SpeechRecognizer.isRecognitionAvailable() returned false, but will try anyway")
        }

        val recognizer = SpeechRecognizer.createSpeechRecognizer(context)
        if (recognizer == null) {
            Log.e(TAG, "createSpeechRecognizer returned null!")
            _error.value = "Speech recognizer creation failed - device may not support it"
            return
        }

        Log.d(TAG, "SpeechRecognizer created successfully")
        speechRecognizer = recognizer.apply {
            setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    Log.d(TAG, "Ready for speech")
                    clearTimeout()
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
                    clearTimeout()
                    _isListening.value = false
                    val errorMsg = getErrorMessage(error)
                    _error.value = if (error == SpeechRecognizer.ERROR_CLIENT) {
                        "Speech recognition service not available. Please install Google app or use a device with Google Play Services."
                    } else {
                        errorMsg
                    }
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
            Log.e(TAG, "Speech recognizer is null, attempting to recreate")
            _error.value = "Speech recognizer not initialized"
            // Try to reinitialize
            initialize()
            if (speechRecognizer == null) {
                _error.value = "Failed to initialize speech recognizer"
                return
            }
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
        try {
            speechRecognizer?.startListening(intent)
            Log.d(TAG, "startListening() called successfully")

            // Set timeout to detect if service doesn't respond
            timeoutRunnable = Runnable {
                Log.e(TAG, "Speech recognition timeout - service did not respond")
                _isListening.value = false
                _error.value = "Speech recognition service not available on this device. Google app or Google Play Services may be missing."
            }
            handler.postDelayed(timeoutRunnable!!, 3000) // 3 second timeout
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start listening", e)
            _error.value = "Failed to start speech recognition: ${e.message}"
        }
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
        clearTimeout()
        speechRecognizer?.destroy()
        speechRecognizer = null
        _isListening.value = false
    }

    /**
     * Clear the timeout callback
     */
    private fun clearTimeout() {
        timeoutRunnable?.let {
            handler.removeCallbacks(it)
            timeoutRunnable = null
        }
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
