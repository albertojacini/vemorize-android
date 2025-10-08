package com.example.vemorize.domain.chat.modes

import com.example.vemorize.data.chat.ChatApiClient
import com.example.vemorize.domain.chat.actions.Actions
import com.example.vemorize.domain.chat.actions.ToolRegistry
import com.example.vemorize.domain.chat.commands.VoiceCommand
import com.example.vemorize.domain.chat.managers.NavigationManager
import com.example.vemorize.domain.chat.modes.commands.NextContentCommand
import com.example.vemorize.domain.chat.modes.commands.ReadCurrentCommand
import com.example.vemorize.domain.chat.modes.commands.ReadingHelpCommand
import com.example.vemorize.domain.chat.modes.commands.ReadingSwitchModeCommand
import com.example.vemorize.domain.chat.modes.commands.StopReadingCommand
import com.example.vemorize.domain.model.chat.ChatMode
import com.example.vemorize.domain.model.chat.ChatResponse
import com.example.vemorize.domain.model.chat.HandlerResponse
import com.example.vemorize.domain.model.chat.LLMRequest

/**
 * Handler for READING mode - reading content with navigation
 * Port of TypeScript ReadingHandler from reading/handler.ts
 */
class ReadingModeHandler(
    chatApiClient: ChatApiClient,
    actions: Actions,
    navigationManager: NavigationManager,
    toolRegistry: ToolRegistry
) : BaseModeHandler(chatApiClient, actions, navigationManager, toolRegistry) {

    override val mode = ChatMode.READING

    /**
     * Commands available in Reading mode
     */
    override val commands: List<VoiceCommand> = listOf(
        ReadingSwitchModeCommand(),
        ReadCurrentCommand(),
        NextContentCommand(),
        StopReadingCommand(),
        ReadingHelpCommand()
    )

    init {
        // Register commands with matcher
        matcher.registerCommands(commands)
    }

    override suspend fun onEnter(): String {
        val text = navigationManager.getReadingText()
        return text ?: "No content available to read"
    }

    override suspend fun onExit() {
        // Nothing to clean up
    }

    override suspend fun buildLLMRequest(userInput: String): LLMRequest {
        val course = navigationManager.activeCourse
            ?: throw IllegalStateException("No active course")

        val currentContent = navigationManager.getReadingText() ?: "No content"

        return LLMRequest(
            userMessage = userInput,
            systemPrompt = buildReadingSystemPrompt(currentContent),
            courseId = course.id,
            userId = course.userId
        )
    }

    private fun buildReadingSystemPrompt(currentContent: String): String {
        return """
            You are a reading assistant. The current content being read is:

            $currentContent

            You can help the user with:
            - Explaining the content
            - Navigating to next/previous content
            - Exiting reading mode

            Available tools:
            - provide_chat_response: Respond to the user
            - next_content: Move to next content
            - previous_content: Move to previous content
            - exit_mode: Exit reading mode
        """.trimIndent()
    }

    override fun getDefaultResponseMessage(): String {
        return "I'm in reading mode. Say 'next' to continue or 'exit' to leave."
    }

    override fun handleConversationError(error: Exception): HandlerResponse {
        return HandlerResponse(
            generatedBy = mode,
            message = "Sorry, I had trouble processing that. Please try again."
        )
    }
}
