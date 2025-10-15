package com.example.vemorize.domain.chat.modes

import com.example.vemorize.data.chat.ChatApiClient
import com.example.vemorize.domain.chat.actions.Actions
import com.example.vemorize.domain.chat.actions.ToolRegistry
import com.example.vemorize.domain.chat.commands.VoiceCommand
import com.example.vemorize.domain.chat.commands.VoiceCommandMatcher
import com.example.vemorize.domain.chat.managers.NavigationManager
import com.example.vemorize.domain.chat.model.*

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
        android.util.Log.d(TAG, "handleUserInput: '$userInput' in mode: $mode")
        android.util.Log.d(TAG, "Registered commands: ${matcher.getRegisteredCommands()}")

        // Path 1: Detect command
        val commandMatch = matcher.match(userInput)
        android.util.Log.d(TAG, "Command match result: $commandMatch")

        if (commandMatch != null) {
            android.util.Log.d(TAG, "Executing command: ${commandMatch.command}")
            // Path 2: Execute command
            val commandResponse = handleCommand(commandMatch)
            return HandlerResponse(
                generatedBy = mode,
                message = commandResponse.response,
                voiceLang = commandResponse.voiceLang
            )
        }

        android.util.Log.d(TAG, "No command matched, handling as conversational input")
        // Path 3: Handle as conversational input
        return handleConversationalInput(userInput)
    }

    companion object {
        private const val TAG = "BaseModeHandler"
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
     * Port of TypeScript handleConversationalInput from base.ts:70-117
     */
    protected suspend fun handleConversationalInput(userInput: String): HandlerResponse {
        return try {
            android.util.Log.d(TAG, "handleConversationalInput: starting for input: '$userInput'")

            // Build LLM context
            val llmContext = buildLLMContext(userInput)
            android.util.Log.d(TAG, "handleConversationalInput: built LLM context")

            val course = navigationManager.activeCourse
                ?: throw IllegalStateException("No active course")
            android.util.Log.d(TAG, "handleConversationalInput: active course: ${course.id}")

            // Call API
            android.util.Log.d(TAG, "handleConversationalInput: calling API...")
            val response = chatApiClient.sendLLMRequest(
                llmContext = llmContext,
                courseId = course.id,
                userId = course.userId
            )

            val toolCalls = response.data?.toolCalls ?: emptyList()
            android.util.Log.d(TAG, "handleConversationalInput: API response received, toolCalls: ${toolCalls.size}")

            // Extract chat response from tool calls
            val assistantMessage = toolRegistry.extractChatResponse(toolCalls)
            android.util.Log.d(TAG, "handleConversationalInput: extracted message: '$assistantMessage'")

            // Execute tool calls
            toolRegistry.executeAll(toolCalls)
            android.util.Log.d(TAG, "handleConversationalInput: tool calls executed")

            HandlerResponse(
                generatedBy = mode,
                message = assistantMessage.ifEmpty { getDefaultResponseMessage() }
            )
        } catch (e: Exception) {
            android.util.Log.e(TAG, "handleConversationalInput: ERROR", e)
            handleConversationError(e)
        }
    }

    /**
     * Build LLM context - override for mode-specific context
     */
    protected abstract suspend fun buildLLMContext(userInput: String): ApiLLMContext

    /**
     * Get tool names for this mode
     */
    protected abstract fun getToolNames(): List<String>

    /**
     * Get default response when no chat response is found
     */
    protected abstract fun getDefaultResponseMessage(): String

    /**
     * Handle conversation errors with mode-specific fallbacks
     */
    protected abstract fun handleConversationError(error: Exception): HandlerResponse
}
