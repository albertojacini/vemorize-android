package com.example.vemorize.domain.chat.modes

import com.example.vemorize.data.chat.ChatApiClient
import com.example.vemorize.domain.chat.actions.Actions
import com.example.vemorize.domain.chat.actions.ToolRegistry
import com.example.vemorize.domain.chat.managers.NavigationManager
import com.example.vemorize.domain.model.chat.*

/**
 * Base class for mode handlers
 */
abstract class BaseModeHandler(
    protected val chatApiClient: ChatApiClient,
    protected val actions: Actions,
    protected val navigationManager: NavigationManager,
    protected val toolRegistry: ToolRegistry
) {
    abstract val mode: ChatMode
    protected val messages = mutableListOf<Message>()

    /**
     * Called when entering this mode
     */
    abstract suspend fun onEnter(): String

    /**
     * Called when exiting this mode
     */
    abstract suspend fun onExit()

    /**
     * Handle user input
     */
    suspend fun handleUserInput(userInput: String): HandlerResponse {
        // TODO: Check for voice commands first
        // For now, handle as conversational input
        return handleConversationalInput(userInput)
    }

    /**
     * Handle conversational input using LLM
     */
    protected suspend fun handleConversationalInput(userInput: String): HandlerResponse {
        return try {
            // Build LLM request
            val llmRequest = buildLLMRequest(userInput)

            // Call LLM
            val response = chatApiClient.sendLLMRequest(llmRequest)

            // Execute tool calls
            val toolResults = toolRegistry.executeAll(response.toolCalls)

            // Extract chat response
            val assistantMessage = toolRegistry.extractChatResponse(response.toolCalls)

            HandlerResponse(
                generatedBy = mode,
                message = assistantMessage.ifEmpty { getDefaultResponseMessage() }
            )
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
