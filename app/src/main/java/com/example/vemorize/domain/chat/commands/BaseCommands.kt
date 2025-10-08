package com.example.vemorize.domain.chat.commands

import com.example.vemorize.domain.chat.actions.Actions
import com.example.vemorize.domain.model.chat.ChatMode
import java.util.Date

/**
 * Base class for voice commands with common utilities
 * Port of TypeScript BaseVoiceCommand from base-commands.ts
 */
abstract class BaseVoiceCommandWithUtils : BaseVoiceCommand() {

    /**
     * Normalize input for processing
     */
    protected fun normalizeInput(input: String): String {
        return input
            .lowercase()
            .trim()
            .replace(Regex("[^\\w\\s']"), "") // Remove all except alphanumeric, spaces, apostrophes
            .replace(Regex("\\s+"), " ") // Normalize whitespace
    }

    /**
     * Create a timestamp
     */
    protected fun createTimestamp(): Date {
        return Date()
    }

    /**
     * Extract arguments from input using the command's patterns
     */
    protected fun extractArguments(input: String): Map<String, Any> {
        val matcher = VoiceCommandMatcher()
        matcher.registerCommand(this)

        val match = matcher.match(input)
        return match?.arguments ?: emptyMap()
    }
}

/**
 * Base switch mode command - can be inherited by mode-specific switch commands
 * Port of TypeScript BaseSwitchModeCommand from base-commands.ts
 */
open class BaseSwitchModeCommand : BaseVoiceCommandWithUtils() {
    override val name = "switch_mode"
    override val patterns = listOf("switch to {string:mode}")
    override val description = "Switch to a different mode"

    override suspend fun execute(commandMatch: CommandMatch, actions: Actions): CommandResult {
        val targetModeStr = commandMatch.arguments["mode"] as? String
            ?: return CommandResult(response = "No mode specified")

        // Convert string to ChatMode enum
        val targetMode = try {
            ChatMode.valueOf(targetModeStr.uppercase())
        } catch (e: IllegalArgumentException) {
            return CommandResult(response = "Unknown mode: $targetModeStr")
        }

        val result = actions.switchMode(targetMode)

        if (!result.success) {
            return CommandResult(
                response = result.error ?: "Failed to switch mode"
            )
        }

        return CommandResult(
            response = "Switched to ${targetMode.name.lowercase()} mode."
        )
    }
}
