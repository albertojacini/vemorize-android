package com.example.vemorize.domain.chat.voice

import android.content.Context
import android.util.Log
import com.example.vemorize.data.chat.TtsRepository
import com.example.vemorize.domain.chat.model.TtsModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Manages voice output using pluggable TTS providers
 * Delegates to LocalTtsProvider or CloudTtsProvider based on TtsModel
 */
class VoiceOutputManager(
    private val context: Context,
    private val ttsRepository: TtsRepository? = null
) {

    // Current active provider
    private var currentProvider: TtsProvider? = null

    // Lazy-initialized providers
    private val localProvider by lazy { LocalTtsProvider(context) }
    private val cloudProvider by lazy {
        if (ttsRepository == null) {
            throw IllegalStateException("TtsRepository required for cloud TTS")
        }
        CloudTtsProvider(context, ttsRepository, localProvider)
    }

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
     * Initialize TTS with the specified model
     * @param ttsModel The TTS model to use (LOCAL or OPENAI_GPT_4O_MINI)
     */
    fun initialize(ttsModel: TtsModel = TtsModel.LOCAL) {
        Log.d(TAG, "Initializing VoiceOutputManager with model: $ttsModel")

        currentProvider = when (ttsModel) {
            TtsModel.LOCAL -> localProvider
            TtsModel.OPENAI_GPT_4O_MINI -> cloudProvider
        }

        currentProvider?.initialize {
            Log.d(TAG, "TTS provider initialized: $ttsModel")
            onInitialized?.invoke()
        }
    }

    /**
     * Speak the given text using the configured provider
     *
     * @param text Text to speak
     * @param speed Speech rate (0.25 to 4.0)
     * @param language Language code (e.g., "en-US", "de-DE")
     * @param ttsModel TTS model to use (if different from initialized)
     */
    suspend fun speak(
        text: String,
        speed: Float = 1.0f,
        language: String? = null,
        ttsModel: TtsModel? = null
    ) {
        // Switch provider if different model requested
        val targetProvider = if (ttsModel != null && getProviderForModel(ttsModel) != currentProvider) {
            Log.d(TAG, "Switching to provider for model: $ttsModel")
            val newProvider = getProviderForModel(ttsModel)

            // Initialize if not ready
            if (!newProvider.isReady()) {
                newProvider.initialize()
            }

            currentProvider = newProvider
            newProvider
        } else {
            currentProvider
        }

        if (targetProvider == null) {
            Log.e(TAG, "No TTS provider initialized")
            _error.value = "TTS not initialized"
            return
        }

        if (!targetProvider.isReady()) {
            Log.e(TAG, "TTS provider not ready")
            _error.value = "TTS not ready"
            return
        }

        _error.value = null

        targetProvider.speak(
            text = text,
            speed = speed,
            language = language,
            onStart = {
                _isSpeaking.value = true
            },
            onComplete = {
                _isSpeaking.value = false
                onSpeakingFinished?.invoke()
            },
            onError = { error ->
                _isSpeaking.value = false
                _error.value = error
            }
        )
    }

    /**
     * Convenience method for synchronous-style API (backwards compatibility)
     */
    fun speak(text: String, speed: Float = 1.0f, language: String? = null) {
        if (currentProvider == null) {
            Log.e(TAG, "No TTS provider initialized")
            _error.value = "TTS not initialized"
            return
        }

        if (!currentProvider!!.isReady()) {
            Log.e(TAG, "TTS provider not ready")
            _error.value = "TTS not ready"
            return
        }

        // Use local provider for synchronous API (to maintain backwards compatibility)
        localProvider.initialize()

        CoroutineScope(Dispatchers.Main).launch {
            localProvider.speak(
                text = text,
                speed = speed,
                language = language,
                onStart = { _isSpeaking.value = true },
                onComplete = {
                    _isSpeaking.value = false
                    onSpeakingFinished?.invoke()
                },
                onError = { error ->
                    _isSpeaking.value = false
                    _error.value = error
                }
            )
        }
    }

    private fun getProviderForModel(ttsModel: TtsModel): TtsProvider {
        return when (ttsModel) {
            TtsModel.LOCAL -> localProvider
            TtsModel.OPENAI_GPT_4O_MINI -> cloudProvider
        }
    }

    /**
     * Stop speaking immediately
     */
    fun stop() {
        currentProvider?.stop()
        _isSpeaking.value = false
    }

    /**
     * Check if TTS is initialized and ready
     */
    fun isReady(): Boolean = currentProvider?.isReady() ?: false

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
        localProvider.destroy()
        if (ttsRepository != null) {
            cloudProvider.destroy()
        }
        currentProvider = null
        _isSpeaking.value = false
    }

    companion object {
        private const val TAG = "VoiceOutputManager"
    }
}
