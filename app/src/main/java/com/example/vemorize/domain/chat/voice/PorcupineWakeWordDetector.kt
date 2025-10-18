package com.example.vemorize.domain.chat.voice

import android.content.Context
import android.util.Log
import ai.picovoice.porcupine.Porcupine
import ai.picovoice.porcupine.PorcupineException
import ai.picovoice.porcupine.PorcupineManager
import ai.picovoice.porcupine.PorcupineManagerCallback

/**
 * Wrapper for Porcupine wake word detection
 * Manages lifecycle and provides callbacks when wake word is detected
 */
class PorcupineWakeWordDetector(
    private val context: Context,
    private val accessKey: String
) {
    private var porcupineManager: PorcupineManager? = null
    private var onWakeWordDetected: (() -> Unit)? = null

    /**
     * Initialize the wake word detector
     * @param wakeWord Built-in wake word to use (e.g., "porcupine", "picovoice", "bumblebee")
     * @param onDetected Callback invoked when wake word is detected
     * @throws PorcupineException if initialization fails
     */
    fun initialize(wakeWord: String = "porcupine", onDetected: () -> Unit) {
        if (porcupineManager != null) {
            Log.w(TAG, "PorcupineWakeWordDetector already initialized")
            return
        }

        onWakeWordDetected = onDetected

        try {
            Log.d(TAG, "Initializing Porcupine with wake word: $wakeWord")

            val callback = PorcupineManagerCallback { keywordIndex ->
                Log.d(TAG, "Wake word detected at index: $keywordIndex")
                onWakeWordDetected?.invoke()
            }

            porcupineManager = PorcupineManager.Builder()
                .setAccessKey(accessKey)
                .setKeyword(Porcupine.BuiltInKeyword.valueOf(wakeWord.uppercase()))
                .build(context, callback)

            Log.d(TAG, "Porcupine initialized successfully")
        } catch (e: PorcupineException) {
            Log.e(TAG, "Failed to initialize Porcupine", e)
            throw e
        } catch (e: IllegalArgumentException) {
            Log.e(TAG, "Invalid wake word: $wakeWord", e)
            throw PorcupineException("Invalid wake word: $wakeWord")
        }
    }

    /**
     * Start listening for wake word
     * @throws PorcupineException if not initialized or start fails
     */
    fun start() {
        val manager = porcupineManager
        if (manager == null) {
            throw PorcupineException("PorcupineWakeWordDetector not initialized")
        }

        try {
            Log.d(TAG, "Starting wake word detection")
            manager.start()
            Log.d(TAG, "Wake word detection started")
        } catch (e: PorcupineException) {
            Log.e(TAG, "Failed to start wake word detection", e)
            throw e
        }
    }

    /**
     * Stop listening for wake word
     */
    fun stop() {
        try {
            porcupineManager?.stop()
            Log.d(TAG, "Wake word detection stopped")
        } catch (e: PorcupineException) {
            Log.e(TAG, "Error stopping wake word detection", e)
        }
    }

    /**
     * Check if currently listening
     */
    fun isListening(): Boolean {
        // PorcupineManager doesn't expose state directly, track manually
        // This is a simplification - in real usage, the service would track state
        return porcupineManager != null
    }

    /**
     * Release resources
     */
    fun destroy() {
        try {
            Log.d(TAG, "Destroying PorcupineWakeWordDetector")
            porcupineManager?.delete()
            porcupineManager = null
            onWakeWordDetected = null
            Log.d(TAG, "PorcupineWakeWordDetector destroyed")
        } catch (e: PorcupineException) {
            Log.e(TAG, "Error destroying Porcupine", e)
        }
    }

    companion object {
        private const val TAG = "PorcupineWakeWordDetector"

        /**
         * Get list of available built-in wake words
         */
        fun getAvailableWakeWords(): List<String> {
            return Porcupine.BuiltInKeyword.values().map { it.name.lowercase() }
        }
    }
}
