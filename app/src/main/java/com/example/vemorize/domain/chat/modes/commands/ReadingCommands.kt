package com.example.vemorize.domain.chat.modes.commands

import com.example.vemorize.domain.chat.actions.Actions
import com.example.vemorize.domain.chat.commands.BaseSwitchModeCommand
import com.example.vemorize.domain.chat.commands.BaseVoiceCommand
import com.example.vemorize.domain.chat.commands.CommandMatch
import com.example.vemorize.domain.chat.commands.CommandResult

/**
 * Switch mode command for Reading mode
 * Port of TypeScript SwitchModeCommand from reading/commands.ts
 */
class ReadingSwitchModeCommand : BaseSwitchModeCommand() {
    override suspend fun execute(commandMatch: CommandMatch, actions: Actions): CommandResult {
        // Can add reading-specific logging or behavior here if needed
        return super.execute(commandMatch, actions)
    }
}

/**
 * Read current content command
 * Port of TypeScript ReadCurrentCommand from reading/commands.ts
 */
class ReadCurrentCommand : BaseVoiceCommand() {
    override val name = "read_current"
    override val patterns = listOf("read current", "read", "read the current content")
    override val description = "Read the current content"

    override suspend fun execute(commandMatch: CommandMatch, actions: Actions): CommandResult {
        val result = actions.readCurrentContent()
        if (!result.success) {
            return CommandResult(response = result.error ?: "Failed to read current content")
        }
        return CommandResult(response = result.data as? String ?: "No content to read")
    }
}

/**
 * Navigate to next content command
 * Port of TypeScript NextContentCommand from reading/commands.ts
 */
class NextContentCommand : BaseVoiceCommand() {
    override val name = "next_content"
    override val patterns = listOf("next", "next content", "next page")
    override val description = "Move to the next content"

    override suspend fun execute(commandMatch: CommandMatch, actions: Actions): CommandResult {
        val result = actions.nextContent()
        if (!result.success) {
            return CommandResult(response = result.error ?: "Failed to move to next content")
        }
        return CommandResult(response = result.data as? String ?: "No content to move to")
    }
}

/**
 * Stop reading command - return to idle mode
 * Port of TypeScript StopReadingCommand from reading/commands.ts
 */
class StopReadingCommand : BaseVoiceCommand() {
    override val name = "stop_reading"
    override val patterns = listOf("stop", "idle", "back to idle", "exit mode")
    override val description = "Return to idle mode"

    override suspend fun execute(commandMatch: CommandMatch, actions: Actions): CommandResult {
        return CommandResult(response = "Switched to Idle mode. What would you like to do?")
    }
}

/**
 * Help command for reading mode
 * Port of TypeScript ReadingHelpCommand from reading/commands.ts
 */
class ReadingHelpCommand : BaseVoiceCommand() {
    override val name = "reading_help"
    override val patterns = listOf("help", "what can i do", "commands")
    override val description = "Show available commands"

    override suspend fun execute(commandMatch: CommandMatch, actions: Actions): CommandResult {
        return CommandResult(
            response = "You can say: next, previous, read current, or switch to another mode."
        )
    }
}
