package com.example.vemorize.ui.chat

import android.app.Application
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vemorize.data.auth.AuthRepository
import com.example.vemorize.data.chat.TtsException
import com.example.vemorize.data.chat.TtsRepository
import com.example.vemorize.data.courses.CoursesRepository
import com.example.vemorize.domain.chat.ChatManager
import com.example.vemorize.domain.chat.voice.VoiceInputManager
import com.example.vemorize.domain.chat.voice.VoiceOutputManager
import com.example.vemorize.domain.chat.model.ChatMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatManager: ChatManager,
    private val coursesRepository: CoursesRepository,
    private val authRepository: AuthRepository,
    private val ttsRepository: TtsRepository,
    private val application: Application,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val courseId: String = savedStateHandle.get<String>("courseId") ?: ""

    private val _uiState = MutableStateFlow<ChatUiState>(ChatUiState.Loading)
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    // Voice managers
    private lateinit var voiceInputManager: VoiceInputManager
    private lateinit var voiceOutputManager: VoiceOutputManager

    init {
        initializeChat()
    }

    private fun initializeChat() {
        viewModelScope.launch {
            try {
                android.util.Log.d(TAG, "Initializing chat...")

                // Initialize chat manager
                android.util.Log.d(TAG, "Calling chatManager.initialize()")
                chatManager.initialize()
                android.util.Log.d(TAG, "ChatManager initialized successfully")

                // Initialize voice managers
                initializeVoice()

                // Load course
                val course = if (courseId.isNotEmpty()) {
                    android.util.Log.d(TAG, "Loading course by ID: $courseId")
                    coursesRepository.getCourseById(courseId)
                } else {
                    android.util.Log.d(TAG, "No courseId provided, loading latest course")
                    coursesRepository.getLatestCourse()
                }

                if (course != null) {
                    android.util.Log.d(TAG, "Course found: ${course.title}, loading into ChatManager")
                    chatManager.loadCourse(course)
                    _uiState.value = ChatUiState.Ready(course = course)
                    android.util.Log.d(TAG, "Chat initialized successfully with course")
                } else {
                    if (courseId.isNotEmpty()) {
                        android.util.Log.e(TAG, "Course not found: $courseId")
                        _uiState.value = ChatUiState.Error("Course not found")
                    } else {
                        android.util.Log.w(TAG, "No courses available for user")
                        _uiState.value = ChatUiState.Ready(course = null)
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e(TAG, "Failed to initialize chat", e)
                _uiState.value = ChatUiState.Error(e.message ?: "Failed to initialize chat")
            }
        }
    }

    private fun initializeVoice() {
        android.util.Log.d(TAG, "Initializing voice managers...")

        // Initialize voice input
        voiceInputManager = VoiceInputManager(application)
        voiceInputManager.initialize()

        // Initialize voice output with TTS repository for cloud TTS support
        voiceOutputManager = VoiceOutputManager(application, ttsRepository)

        // Initialize with mode-specific TTS model
        val ttsModel = chatManager.getTtsModel()
        voiceOutputManager.initialize(ttsModel)

        // Set up voice output callbacks
        voiceOutputManager.onSpeakingFinished = {
            // Resume listening after speaking (to prevent feedback loop)
            val currentState = _uiState.value as? ChatUiState.Ready
            if (currentState?.isListening == true) {
                voiceInputManager.startListening()
            }
        }

        // Observe voice input state changes
        viewModelScope.launch {
            voiceInputManager.isListening.collect { isListening ->
                val currentState = _uiState.value as? ChatUiState.Ready ?: return@collect
                _uiState.value = currentState.copy(isListening = isListening)
            }
        }

        // Observe voice output state changes
        viewModelScope.launch {
            voiceOutputManager.isSpeaking.collect { isSpeaking ->
                val currentState = _uiState.value as? ChatUiState.Ready ?: return@collect
                _uiState.value = currentState.copy(isSpeaking = isSpeaking)

                // Stop listening when speaking to prevent feedback
                if (isSpeaking && voiceInputManager.isListening.value) {
                    voiceInputManager.stopListening()
                }
            }
        }

        // Observe partial transcript
        viewModelScope.launch {
            voiceInputManager.partialText.collect { partial ->
                val currentState = _uiState.value as? ChatUiState.Ready ?: return@collect
                _uiState.value = currentState.copy(partialTranscript = partial)
            }
        }

        // Observe recognized text and process commands
        viewModelScope.launch {
            voiceInputManager.recognizedText.collect { recognizedText ->
                if (!recognizedText.isNullOrBlank()) {
                    android.util.Log.d(TAG, "Recognized text: $recognizedText")
                    processVoiceInput(recognizedText)
                    voiceInputManager.clearRecognizedText()
                }
            }
        }

        // Observe voice errors
        viewModelScope.launch {
            combine(
                voiceInputManager.error,
                voiceOutputManager.error
            ) { inputError, outputError ->
                inputError ?: outputError
            }.collect { error ->
                val currentState = _uiState.value as? ChatUiState.Ready ?: return@collect
                _uiState.value = currentState.copy(voiceError = error)
            }
        }

        android.util.Log.d(TAG, "Voice managers initialized")
    }

    private fun processVoiceInput(voiceInput: String) {
        viewModelScope.launch {
            try {
                val currentState = _uiState.value as? ChatUiState.Ready ?: return@launch

                // Add user voice input to voice exchange messages
                val userVoiceExchangeMessage = VoiceExchangeMessage(content = voiceInput, isFromUser = true)
                _uiState.value = currentState.copy(
                    isProcessing = true,
                    partialTranscript = null,
                    voiceExchangeMessages = currentState.voiceExchangeMessages + userVoiceExchangeMessage
                )

                android.util.Log.d(TAG, "Processing voice input: $voiceInput")

                // Handle input through ChatManager (includes command detection and conversational fallback)
                val chatResponse = chatManager.handleInput(voiceInput)

                android.util.Log.d(TAG, "Chat response: ${chatResponse.message}")

                // Add system response to voice exchange messages
                val systemVoiceExchangeMessage = VoiceExchangeMessage(content = chatResponse.message, isFromUser = false)
                val updatedState = _uiState.value as? ChatUiState.Ready ?: return@launch

                // Speak the response with mode-specific TTS
                if (chatResponse.message.isNotBlank()) {
                    // Get speech speed and TTS model from ChatResponse (already includes mode-specific values)
                    val speed = chatResponse.speechSpeed ?: 1.0f
                    val ttsModel = chatResponse.ttsModel
                    android.util.Log.d(TAG, "Speaking response with TTS model: $ttsModel, speed: $speed")
                    viewModelScope.launch {
                        try {
                            voiceOutputManager.speak(
                                text = chatResponse.message,
                                speed = speed,
                                language = chatResponse.voiceLang,
                                ttsModel = ttsModel
                            )
                        } catch (e: TtsException) {
                            android.util.Log.e(TAG, "TTS error: ${e.message}", e)
                            val currentState = _uiState.value as? ChatUiState.Ready ?: return@launch
                            _uiState.value = currentState.copy(
                                ttsError = e.details ?: e.message ?: "TTS failed"
                            )
                        } catch (e: Exception) {
                            android.util.Log.e(TAG, "Unexpected TTS error", e)
                            val currentState = _uiState.value as? ChatUiState.Ready ?: return@launch
                            _uiState.value = currentState.copy(
                                ttsError = "TTS failed: ${e.message}"
                            )
                        }
                    }
                }

                // Sync current mode from NavigationManager (in case voice command changed it)
                val actualMode = chatManager.getCurrentModeFromNavigation()
                android.util.Log.d(TAG, "Syncing UI mode to actual mode: $actualMode")

                // Update UI
                _uiState.value = updatedState.copy(
                    isProcessing = false,
                    voiceError = null,  // Clear any previous errors
                    currentMode = actualMode,  // Sync the mode with NavigationManager
                    voiceExchangeMessages = updatedState.voiceExchangeMessages + systemVoiceExchangeMessage
                )
            } catch (e: Exception) {
                android.util.Log.e(TAG, "Error processing voice command", e)
                val currentState = _uiState.value as? ChatUiState.Ready ?: return@launch
                _uiState.value = currentState.copy(
                    isProcessing = false,
                    voiceError = e.message ?: "Failed to process command"
                )
            }
        }
    }

    companion object {
        private const val TAG = "ChatViewModel"
    }

    fun onEvent(event: ChatUiEvent) {
        when (event) {
            is ChatUiEvent.SendMessage -> sendMessage(event.message)
            is ChatUiEvent.UpdateInput -> updateInput(event.input)
            is ChatUiEvent.SwitchMode -> switchMode(event.mode)
            ChatUiEvent.NavigateNext -> navigateNext()
            ChatUiEvent.NavigatePrevious -> navigatePrevious()
            ChatUiEvent.ToggleVoiceListening -> toggleVoiceListening()
            ChatUiEvent.StopVoiceOutput -> stopVoiceOutput()
            ChatUiEvent.ClearVoiceError -> clearVoiceError()
            ChatUiEvent.ClearTtsError -> clearTtsError()
        }
    }

    private fun toggleVoiceListening() {
        if (!::voiceInputManager.isInitialized) {
            return
        }

        val currentState = _uiState.value as? ChatUiState.Ready ?: return

        if (currentState.isListening) {
            voiceInputManager.stopListening()
        } else {
            // Stop any ongoing speech before starting to listen
            if (currentState.isSpeaking) {
                voiceOutputManager.stop()
            }
            voiceInputManager.startListening(chatManager.incomingVoiceExpectedLang)
        }
    }

    private fun stopVoiceOutput() {
        if (::voiceOutputManager.isInitialized) {
            voiceOutputManager.stop()
        }
    }

    private fun clearVoiceError() {
        val currentState = _uiState.value as? ChatUiState.Ready ?: return
        _uiState.value = currentState.copy(voiceError = null)

        if (::voiceInputManager.isInitialized) {
            voiceInputManager.clearError()
        }
        if (::voiceOutputManager.isInitialized) {
            voiceOutputManager.clearError()
        }
    }

    private fun clearTtsError() {
        val currentState = _uiState.value as? ChatUiState.Ready ?: return
        _uiState.value = currentState.copy(ttsError = null)
    }

    private fun sendMessage(message: String) {
        val currentState = _uiState.value as? ChatUiState.Ready ?: return

        viewModelScope.launch {
            try {
                // Add user text input to voice exchange messages
                val userVoiceExchangeMessage = VoiceExchangeMessage(content = message, isFromUser = true)
                _uiState.value = currentState.copy(
                    isProcessing = true,
                    voiceExchangeMessages = currentState.voiceExchangeMessages + userVoiceExchangeMessage
                )

                // Send message to chat manager
                val response = chatManager.handleInput(message)

                // Add system response to voice exchange messages
                val systemVoiceExchangeMessage = VoiceExchangeMessage(content = response.message, isFromUser = false)
                val updatedState = _uiState.value as? ChatUiState.Ready ?: return@launch

                // Update UI with response
                _uiState.value = updatedState.copy(
                    isProcessing = false,
                    userInput = "",
                    voiceExchangeMessages = updatedState.voiceExchangeMessages + systemVoiceExchangeMessage
                )
            } catch (e: Exception) {
                _uiState.value = ChatUiState.Error(e.message ?: "Failed to send message")
            }
        }
    }

    private fun updateInput(input: String) {
        val currentState = _uiState.value as? ChatUiState.Ready ?: return
        _uiState.value = currentState.copy(userInput = input)
    }

    private fun switchMode(mode: ChatMode) {
        viewModelScope.launch {
            try {
                chatManager.switchMode(mode)
                val currentState = _uiState.value as? ChatUiState.Ready ?: return@launch
                _uiState.value = currentState.copy(currentMode = mode)
            } catch (e: Exception) {
                _uiState.value = ChatUiState.Error(e.message ?: "Failed to switch mode")
            }
        }
    }

    private fun navigateNext() {
        viewModelScope.launch {
            try {
                chatManager.nextContent()
            } catch (e: Exception) {
                _uiState.value = ChatUiState.Error(e.message ?: "Failed to navigate")
            }
        }
    }

    private fun navigatePrevious() {
        viewModelScope.launch {
            try {
                chatManager.previousContent()
            } catch (e: Exception) {
                _uiState.value = ChatUiState.Error(e.message ?: "Failed to navigate")
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        android.util.Log.d(TAG, "ChatViewModel onCleared - cleaning up voice managers")

        // Clean up voice managers
        if (::voiceInputManager.isInitialized) {
            voiceInputManager.destroy()
        }
        if (::voiceOutputManager.isInitialized) {
            voiceOutputManager.destroy()
        }
    }
}
