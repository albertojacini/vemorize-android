package com.example.vemorize.domain.chat.modes

import com.example.vemorize.data.chat.ChatApiClient
import com.example.vemorize.domain.chat.actions.Actions
import com.example.vemorize.domain.chat.actions.ToolRegistry
import com.example.vemorize.domain.chat.commands.VoiceCommand
import com.example.vemorize.domain.chat.managers.NavigationManager
import com.example.vemorize.domain.chat.modes.commands.IdleSwitchModeCommand
import com.example.vemorize.domain.model.chat.ChatMode
import com.example.vemorize.domain.model.chat.ChatResponse
import com.example.vemorize.domain.model.chat.HandlerResponse
import com.example.vemorize.domain.model.chat.LLMRequest

/**
 * Handler for IDLE mode - general conversation and mode switching
 * Port of TypeScript IdleHandler from idle/handler.ts
 */
class IdleModeHandler(
    chatApiClient: ChatApiClient,
    actions: Actions,
    navigationManager: NavigationManager,
    toolRegistry: ToolRegistry
) : BaseModeHandler(chatApiClient, actions, navigationManager, toolRegistry) {

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

    override suspend fun buildLLMRequest(userInput: String): LLMRequest {
        val course = navigationManager.activeCourse
            ?: throw IllegalStateException("No active course")

        return LLMRequest(
            userMessage = userInput,
            systemPrompt = buildIdleSystemPrompt(),
            courseId = course.id,
            userId = course.userId
        )
    }

    private fun buildIdleSystemPrompt(): String {
        return """
            You are a helpful learning assistant in IDLE mode.
            You can help the user with:
            - Switching to READING mode to read content
            - Switching to QUIZ mode to test knowledge
            - General questions about the course

            Available tools:
            - provide_chat_response: Respond to the user
            - switch_mode: Switch to another mode (reading, quiz)
        """.trimIndent()
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
