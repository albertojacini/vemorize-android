package com.example.vemorize.ui.chat

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vemorize.data.auth.AuthRepository
import com.example.vemorize.data.courses.CoursesRepository
import com.example.vemorize.domain.chat.ChatManager
import com.example.vemorize.domain.model.chat.ChatMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatManager: ChatManager,
    private val coursesRepository: CoursesRepository,
    private val authRepository: AuthRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val courseId: String = savedStateHandle.get<String>("courseId") ?: ""

    private val _uiState = MutableStateFlow<ChatUiState>(ChatUiState.Loading)
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

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

                // Load course if courseId is provided
                if (courseId.isNotEmpty()) {
                    android.util.Log.d(TAG, "Loading course: $courseId")
                    val course = coursesRepository.getCourseById(courseId)
                    if (course != null) {
                        android.util.Log.d(TAG, "Course found, loading into ChatManager")
                        chatManager.loadCourse(course)
                        _uiState.value = ChatUiState.Ready(course = course)
                        android.util.Log.d(TAG, "Chat initialized successfully with course")
                    } else {
                        android.util.Log.e(TAG, "Course not found: $courseId")
                        _uiState.value = ChatUiState.Error("Course not found")
                    }
                } else {
                    android.util.Log.d(TAG, "No courseId provided, ready without course")
                    _uiState.value = ChatUiState.Ready(course = null)
                }
            } catch (e: Exception) {
                android.util.Log.e(TAG, "Failed to initialize chat", e)
                _uiState.value = ChatUiState.Error(e.message ?: "Failed to initialize chat")
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
            ChatUiEvent.StartNewConversation -> startNewConversation()
        }
    }

    private fun sendMessage(message: String) {
        val currentState = _uiState.value as? ChatUiState.Ready ?: return

        viewModelScope.launch {
            try {
                // Update UI to show processing
                _uiState.value = currentState.copy(isProcessing = true)

                // Send message to chat manager
                val response = chatManager.handleInput(message)

                // Update UI with response
                _uiState.value = currentState.copy(
                    isProcessing = false,
                    userInput = ""
                )

                // TODO: Add message to UI list (need to fetch from repository)
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

    private fun startNewConversation() {
        viewModelScope.launch {
            try {
                chatManager.startNewConversation()
                val currentState = _uiState.value as? ChatUiState.Ready ?: return@launch
                _uiState.value = currentState.copy(messages = emptyList())
            } catch (e: Exception) {
                _uiState.value = ChatUiState.Error(e.message ?: "Failed to start new conversation")
            }
        }
    }
}
