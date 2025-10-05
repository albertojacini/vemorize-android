package com.example.vemorize.domain.chat.voice

import com.example.vemorize.domain.chat.actions.Actions
import com.example.vemorize.domain.model.chat.ActionResult
import com.example.vemorize.domain.model.chat.ChatMode

/**
 * Result of command parsing and execution
 */
data class CommandResult(
    val success: Boolean,
    val message: String,
    val wasCommand: Boolean = true
)

/**
 * Parses voice commands and executes corresponding actions
 * This is a simple pattern-matching approach (no LLM required)
 */
class CommandParser(private val actions: Actions) {

    /**
     * Parse and execute a voice command
     * Returns a CommandResult with the response message
     */
    suspend fun parseAndExecute(input: String): CommandResult {
        val normalizedInput = input.trim().lowercase()

        // === Mode Switching Commands ===
        when {
            normalizedInput.contains("switch") && normalizedInput.contains("idle") -> {
                return executeSwitchMode(ChatMode.IDLE, "Switching to idle mode")
            }

            normalizedInput.contains("switch") && normalizedInput.contains("reading") -> {
                return executeSwitchMode(ChatMode.READING, "Switching to reading mode")
            }

            normalizedInput.contains("switch") && normalizedInput.contains("quiz") -> {
                return executeSwitchMode(ChatMode.QUIZ, "Switching to quiz mode")
            }

            // Shorthand mode commands
            normalizedInput == "idle" || normalizedInput == "idle mode" -> {
                return executeSwitchMode(ChatMode.IDLE, "Switching to idle mode")
            }

            normalizedInput == "reading" || normalizedInput == "reading mode" || normalizedInput == "read" -> {
                return executeSwitchMode(ChatMode.READING, "Switching to reading mode")
            }

            normalizedInput == "quiz" || normalizedInput == "quiz mode" -> {
                return executeSwitchMode(ChatMode.QUIZ, "Switching to quiz mode")
            }

            // === Navigation Commands ===
            normalizedInput.contains("next") || normalizedInput.contains("forward") -> {
                return executeNavigation(forward = true)
            }

            normalizedInput.contains("previous") || normalizedInput.contains("back") || normalizedInput.contains("prev") -> {
                return executeNavigation(forward = false)
            }

            // === Reading Commands ===
            normalizedInput.contains("read current") || normalizedInput.contains("read this") || normalizedInput == "read" -> {
                return executeReadCurrent()
            }

            // === Exit Commands ===
            normalizedInput.contains("exit") || normalizedInput.contains("stop mode") -> {
                actions.exitCurrentMode()
                return CommandResult(success = true, message = "Exiting to idle mode")
            }

            // === Help Commands ===
            normalizedInput.contains("help") || normalizedInput.contains("what can you do") -> {
                return CommandResult(
                    success = true,
                    message = "You can say: switch to reading mode, switch to quiz mode, next, previous, read current, or exit"
                )
            }

            // No command matched
            else -> {
                return CommandResult(
                    success = false,
                    message = "I didn't understand that command. Try saying: switch to reading mode, next, previous, or help",
                    wasCommand = false
                )
            }
        }
    }

    private suspend fun executeSwitchMode(mode: ChatMode, successMessage: String): CommandResult {
        val result = actions.switchMode(mode)
        return if (result.success) {
            CommandResult(success = true, message = successMessage)
        } else {
            CommandResult(success = false, message = result.error ?: "Failed to switch mode")
        }
    }

    private suspend fun executeNavigation(forward: Boolean): CommandResult {
        val result = if (forward) {
            actions.nextContent()
        } else {
            actions.previousContent()
        }

        return if (result.success) {
            // Extract content text if available
            val contentText = result.data as? String
            val message = if (forward) {
                "Moving to next content"
            } else {
                "Moving to previous content"
            }

            // If there's content to read, include it in the response
            if (!contentText.isNullOrBlank()) {
                CommandResult(success = true, message = "$message. $contentText")
            } else {
                CommandResult(success = true, message = message)
            }
        } else {
            CommandResult(success = false, message = result.error ?: "Failed to navigate")
        }
    }

    private fun executeReadCurrent(): CommandResult {
        val result = actions.readCurrentContent()
        return if (result.success) {
            val text = result.data as? String
            if (!text.isNullOrBlank()) {
                CommandResult(success = true, message = text)
            } else {
                CommandResult(success = true, message = "No content available to read")
            }
        } else {
            CommandResult(success = false, message = result.error ?: "Failed to read content")
        }
    }
}
