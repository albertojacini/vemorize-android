package com.example.vemorize.domain.chat.commands

import com.example.vemorize.domain.chat.actions.Actions

/**
 * Voice command interface - self-contained with all logic
 * Mirrors TypeScript VoiceCommand from command-types.ts
 */
interface VoiceCommand {
    val name: String
    val patterns: List<String>
    val description: String

    /**
     * Execute the command and return structured result
     */
    suspend fun execute(commandMatch: CommandMatch, actions: Actions): CommandResult

    /**
     * Optional: validate if command can execute in current context
     */
    fun canExecute(): Boolean = true

    /**
     * Optional: get help text for this specific command
     */
    fun getHelp(): String = description
}

/**
 * Base implementation of VoiceCommand
 * Mirrors TypeScript BaseVoiceCommand from base-commands.ts
 */
abstract class BaseVoiceCommand : VoiceCommand {
    override fun canExecute(): Boolean = true
    override fun getHelp(): String = description
}
