package com.example.vemorize.domain.chat.voice

import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import com.example.vemorize.data.chat.TtsException
import com.example.vemorize.data.chat.TtsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import java.io.File
import java.io.FileOutputStream

/**
 * Cloud TTS provider using OpenAI gpt-4o-mini-tts via Supabase Edge Function
 * Falls back to local TTS on error
 */
class CloudTtsProvider(
    private val context: Context,
    private val ttsRepository: TtsRepository,
    private val localFallback: LocalTtsProvider
) : TtsProvider {

    private var mediaPlayer: MediaPlayer? = null
    private var currentlySpeaking = false
    private var isInitialized = false

    override fun initialize(onInitialized: (() -> Unit)?) {
        // Cloud TTS doesn't need initialization, but we initialize the fallback
        localFallback.initialize {
            isInitialized = true
            onInitialized?.invoke()
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
        if (text.isBlank()) {
            Log.w(TAG, "Cannot speak empty text")
            onError?.invoke("Cannot speak empty text")
            return
        }

        try {
            Log.d(TAG, "Generating cloud TTS for: \"$text\" (speed: $speed)")

            // Call TTS API to get MP3 audio
            val audioBytes = withContext(Dispatchers.IO) {
                ttsRepository.generateTts(text, speed)
            }

            Log.d(TAG, "Received ${audioBytes.size} bytes of MP3 audio")

            // Write audio to temp file
            val tempFile = withContext(Dispatchers.IO) {
                File.createTempFile("tts_", ".mp3", context.cacheDir).apply {
                    FileOutputStream(this).use { it.write(audioBytes) }
                }
            }

            // Play audio with MediaPlayer (AWAIT completion)
            playAudioAndWait(tempFile, onStart, onComplete, onError)

        } catch (e: TtsException) {
            Log.e(TAG, "Cloud TTS failed: ${e.message}, falling back to local TTS", e)

            // Fallback to local TTS
            Log.d(TAG, "Using local TTS fallback")
            localFallback.speak(text, speed, language, onStart, onComplete, onError)

        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error in cloud TTS: ${e.message}, falling back to local TTS", e)

            // Fallback to local TTS
            Log.d(TAG, "Using local TTS fallback")
            localFallback.speak(text, speed, language, onStart, onComplete, onError)
        }
    }

    /**
     * Play audio file and suspend until playback completes
     */
    private suspend fun playAudioAndWait(
        audioFile: File,
        onStart: (() -> Unit)?,
        onComplete: (() -> Unit)?,
        onError: ((String) -> Unit)?
    ) = withContext(Dispatchers.Main) {
        suspendCancellableCoroutine { continuation ->
            try {
                // Clean up previous MediaPlayer
                mediaPlayer?.release()

                mediaPlayer = MediaPlayer().apply {
                    setDataSource(audioFile.absolutePath)

                    setOnPreparedListener {
                        Log.d(TAG, "MediaPlayer prepared, starting playback")
                        currentlySpeaking = true
                        onStart?.invoke()
                        start()
                    }

                    setOnCompletionListener {
                        Log.d(TAG, "MediaPlayer playback completed")
                        currentlySpeaking = false
                        onComplete?.invoke()

                        // Clean up temp file
                        audioFile.delete()
                        release()
                        mediaPlayer = null

                        // Resume coroutine when playback completes
                        continuation.resume(Unit)
                    }

                    setOnErrorListener { mp, what, extra ->
                        Log.e(TAG, "MediaPlayer error: what=$what, extra=$extra")
                        currentlySpeaking = false
                        val errorMessage = "Audio playback error"
                        onError?.invoke(errorMessage)

                        // Clean up
                        audioFile.delete()
                        mp.release()
                        mediaPlayer = null

                        // Resume with exception on error
                        continuation.resumeWithException(Exception(errorMessage))
                        true
                    }

                    prepareAsync()
                }

                // Handle cancellation
                continuation.invokeOnCancellation {
                    Log.d(TAG, "Audio playback cancelled")
                    mediaPlayer?.stop()
                    mediaPlayer?.release()
                    mediaPlayer = null
                    currentlySpeaking = false
                    audioFile.delete()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to play audio", e)
                currentlySpeaking = false
                val errorMessage = "Failed to play audio: ${e.message}"
                onError?.invoke(errorMessage)
                audioFile.delete()
                continuation.resumeWithException(Exception(errorMessage))
            }
        }
    }

    override fun stop() {
        if (currentlySpeaking) {
            Log.d(TAG, "Stopping cloud TTS playback")
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
            currentlySpeaking = false
        }

        // Also stop fallback if it's speaking
        localFallback.stop()
    }

    override fun isReady(): Boolean = isInitialized

    override fun isSpeaking(): Boolean = currentlySpeaking || localFallback.isSpeaking()

    override fun destroy() {
        Log.d(TAG, "Destroying CloudTtsProvider")
        mediaPlayer?.release()
        mediaPlayer = null
        currentlySpeaking = false
        localFallback.destroy()
    }

    companion object {
        private const val TAG = "CloudTtsProvider"
    }
}
