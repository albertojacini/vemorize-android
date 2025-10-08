package com.example.vemorize.domain.chat.modes

import com.example.vemorize.data.chat.ChatApiClient
import com.example.vemorize.domain.chat.actions.Actions
import com.example.vemorize.domain.chat.actions.ToolRegistry
import com.example.vemorize.domain.chat.commands.VoiceCommand
import com.example.vemorize.domain.chat.commands.VoiceCommandMatcher
import com.example.vemorize.domain.chat.managers.NavigationManager
import com.example.vemorize.domain.model.chat.*

/**
 * Base class for mode handlers
 * Port of TypeScript Handler from base.ts
 */
abstract class BaseModeHandler(
    protected val chatApiClient: ChatApiClient,
    protected val actions: Actions,
    protected val navigationManager: NavigationManager,
    protected val toolRegistry: ToolRegistry
) {
    abstract val mode: ChatMode
    protected val matcher: VoiceCommandMatcher = VoiceCommandMatcher()

    /**
     * Mode-specific commands - override in each mode handler
     */
    protected abstract val commands: List<VoiceCommand>

    /**
     * Called when entering this mode
     */
    abstract suspend fun onEnter(): String

    /**
     * Called when exiting this mode
     */
    abstract suspend fun onExit()

    /**
     * Main entry point for handling user input
     * Three-path routing: detect command → execute command OR handle conversationally
     * Port of TypeScript handleUserInput from base.ts:41-64
     */
    suspend fun handleUserInput(userInput: String): HandlerResponse {
        // Path 1: Detect command
        val commandMatch = matcher.match(userInput)

        if (commandMatch != null) {
            // Path 2: Execute command
            val commandResponse = handleCommand(commandMatch)
            return HandlerResponse(
                generatedBy = mode,
                message = commandResponse.response,
                voiceLang = commandResponse.voiceLang
            )
        }

        // Path 3: Handle as conversational input
        return handleConversationalInput(userInput)
    }

    /**
     * Handle command execution
     * Port of TypeScript handleCommand from base.ts:122-139
     */
    private suspend fun handleCommand(commandMatch: com.example.vemorize.domain.chat.commands.CommandMatch): com.example.vemorize.domain.chat.commands.CommandResult {
        // Select the correct command based on the command name
        val command = commands.find { it.name == commandMatch.command }
            ?: return com.example.vemorize.domain.chat.commands.CommandResult(
                response = "Command not found"
            )

        // Execute the command
        return command.execute(commandMatch, actions)
    }

    /**
     * Handle conversational input using LLM
     */
    protected suspend fun handleConversationalInput(userInput: String): HandlerResponse {
        return try {
            // REIMPLEMENT THIS
        } catch (e: Exception) {
            handleConversationError(e)
        }
    }

    /**
     * Build LLM request context - override for mode-specific context
     */
    protected abstract suspend fun buildLLMRequest(userInput: String): LLMRequest

    /**
     * Get default response when no chat response is found
     */
    protected abstract fun getDefaultResponseMessage(): String

    /**
     * Handle conversation errors with mode-specific fallbacks
     */
    protected abstract fun handleConversationError(error: Exception): HandlerResponse
}
