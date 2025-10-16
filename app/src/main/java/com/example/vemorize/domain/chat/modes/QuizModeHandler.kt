package com.example.vemorize.domain.chat.modes

import com.example.vemorize.data.chat.ConversationRepository
import com.example.vemorize.data.dto.vemorize_api.ApiLLMContext
import com.example.vemorize.domain.chat.actions.Actions
import com.example.vemorize.domain.chat.actions.ToolRegistry
import com.example.vemorize.domain.chat.commands.VoiceCommand
import com.example.vemorize.domain.chat.managers.NavigationManager
import com.example.vemorize.domain.chat.modes.commands.QuizHelpCommand
import com.example.vemorize.domain.chat.modes.commands.QuizSwitchModeCommand
import com.example.vemorize.domain.chat.modes.commands.StartQuizCommand
import com.example.vemorize.domain.chat.modes.commands.StopQuizCommand
import com.example.vemorize.domain.chat.model.ChatMode
import com.example.vemorize.domain.chat.model.HandlerResponse

/**
 * Handler for QUIZ mode - testing knowledge
 * Port of TypeScript QuizHandler from quiz/handler.ts
 */
class QuizModeHandler(
    conversationRepository: ConversationRepository,
    actions: Actions,
    navigationManager: NavigationManager,
    toolRegistry: ToolRegistry
) : BaseModeHandler(conversationRepository, actions, navigationManager, toolRegistry) {

    override val mode = ChatMode.QUIZ

    /**
     * Commands available in Quiz mode
     */
    override val commands: List<VoiceCommand> = listOf(
        QuizSwitchModeCommand(),
        QuizHelpCommand(),
        StartQuizCommand(),
        StopQuizCommand()
    )

    init {
        // Register commands with matcher
        matcher.registerCommands(commands)
    }

    override suspend fun onEnter(): String {
        return "Quiz mode activated. I'll ask you questions about the content."
    }

    override suspend fun onExit() {
        // Save quiz results if needed
    }

    override suspend fun buildLLMContext(userInput: String): ApiLLMContext {
        val currentContent = navigationManager.getReadingText() ?: "No content available"

        return ApiLLMContext(
            userMessage = userInput,
            toolNames = getToolNames(),
            mode = "quiz",
            userMemory = null, // TODO: Integrate user memory if needed
            leafReprForPrompt = currentContent
        )
    }

    override fun getToolNames(): List<String> {
        return listOf(
            "provide_chat_response",
            "exit_mode",
            "switch_mode"
        )
    }

    override fun getDefaultResponseMessage(): String {
        return "Let's test your knowledge. Are you ready?"
    }

    override fun handleConversationError(error: Exception): HandlerResponse {
        return HandlerResponse(
            generatedBy = mode,
            message = "Sorry, I had trouble with that question. Let's try another."
        )
    }
}
