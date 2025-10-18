package com.example.vemorize.domain.chat.voice

/**
 * Represents the state of the voice control service
 */
sealed class VoiceControlState {
    /**
     * Service is stopped - no voice recognition active
     */
    data object Stopped : VoiceControlState()

    /**
     * Active listening mode - SpeechRecognizer continuously listening for commands
     * Automatically restarts after each recognition result
     * Transitions to WakeWordMode after 60 seconds of inactivity
     */
    data object ActiveListening : VoiceControlState()

    /**
     * Wake word mode - Porcupine listening for wake word only
     * Low power consumption, transitions to ActiveListening when wake word detected
     */
    data object WakeWordMode : VoiceControlState()

    /**
     * Get display name for UI
     */
    fun displayName(): String = when (this) {
        is Stopped -> "Stopped"
        is ActiveListening -> "Listening for commands"
        is WakeWordMode -> "Say wake word to activate"
    }

    /**
     * Validate state transition
     * Returns true if transition is allowed, false otherwise
     */
    fun canTransitionTo(newState: VoiceControlState): Boolean {
        return when (this) {
            is Stopped -> newState is ActiveListening
            is ActiveListening -> newState is WakeWordMode || newState is Stopped
            is WakeWordMode -> newState is ActiveListening || newState is Stopped
        }
    }

    /**
     * Perform state transition with validation
     * Throws IllegalStateException if transition is not allowed
     */
    fun transitionTo(newState: VoiceControlState): VoiceControlState {
        if (!canTransitionTo(newState)) {
            throw IllegalStateException("Cannot transition from ${this::class.simpleName} to ${newState::class.simpleName}")
        }
        return newState
    }
}
