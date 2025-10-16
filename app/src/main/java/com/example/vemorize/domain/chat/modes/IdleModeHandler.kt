package com.example.vemorize.domain.chat.modes

import com.example.vemorize.data.chat.ConversationRepository
import com.example.vemorize.data.dto.vemorize_api.ApiLLMContext
import com.example.vemorize.domain.chat.actions.Actions
import com.example.vemorize.domain.chat.actions.ToolRegistry
import com.example.vemorize.domain.chat.commands.VoiceCommand
import com.example.vemorize.domain.chat.managers.NavigationManager
import com.example.vemorize.domain.chat.modes.commands.IdleSwitchModeCommand
import com.example.vemorize.domain.chat.model.ChatMode
import com.example.vemorize.domain.chat.model.HandlerResponse

/**
 * Handler for IDLE mode - general conversation and mode switching
 * Port of TypeScript IdleHandler from idle/handler.ts
 */
class IdleModeHandler(
    conversationRepository: ConversationRepository,
    actions: Actions,
    navigationManager: NavigationManager,
    toolRegistry: ToolRegistry
) : BaseModeHandler(conversationRepository, actions, navigationManager, toolRegistry) {

    override val mode = ChatMode.IDLE

    /**
     * Commands available in Idle mode
     */
    override val commands: List<VoiceCommand> = listOf(
        IdleSwitchModeCommand()
    )

    init {
        // Register commands with matcher
        matcher.registerCommands(commands)
    }

    override suspend fun onEnter(): String {
        return "Idle mode activated. How can I help you?"
    }

    override suspend fun onExit() {
        // Nothing to clean up
    }

    override suspend fun buildLLMContext(userInput: String): ApiLLMContext {
        return ApiLLMContext(
            userMessage = userInput,
            toolNames = getToolNames(),
            mode = "idle",
            userMemory = null, // TODO: Integrate user memory if needed
            leafReprForPrompt = null
        )
    }

    override fun getToolNames(): List<String> {
        return listOf(
            "provide_chat_response",
            "switch_mode"
        )
    }

    override fun getDefaultResponseMessage(): String {
        return "I'm here to help. What would you like to do?"
    }

    override fun handleConversationError(error: Exception): HandlerResponse {
        return HandlerResponse(
            generatedBy = mode,
            message = "Sorry, I encountered an error. Please try again."
        )
    }
}
