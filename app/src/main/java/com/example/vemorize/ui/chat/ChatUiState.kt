package com.example.vemorize.ui.chat

import com.example.vemorize.domain.model.chat.ChatMode
import com.example.vemorize.domain.model.courses.Course

/**
 * UI state for chat screen
 */
sealed class ChatUiState {
    object Loading : ChatUiState()
    data class Ready(
        val course: Course?,
        val voiceExchangeMessages: List<VoiceExchangeMessage> = emptyList(),
        val currentMode: ChatMode = ChatMode.IDLE,
        val isProcessing: Boolean = false,
        val userInput: String = "",
        val isListening: Boolean = false,
        val isSpeaking: Boolean = false,
        val partialTranscript: String? = null,
        val voiceError: String? = null
    ) : ChatUiState()
    data class Error(val message: String) : ChatUiState()
}

/**
 * UI events from the screen
 */
sealed class ChatUiEvent {
    data class SendMessage(val message: String) : ChatUiEvent()
    data class UpdateInput(val input: String) : ChatUiEvent()
    data class SwitchMode(val mode: ChatMode) : ChatUiEvent()
    object NavigateNext : ChatUiEvent()
    object NavigatePrevious : ChatUiEvent()
    object StartNewConversation : ChatUiEvent()
    object ToggleVoiceListening : ChatUiEvent()
    object StopVoiceOutput : ChatUiEvent()
    object ClearVoiceError : ChatUiEvent()
}
