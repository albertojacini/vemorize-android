package com.example.vemorize.domain.chat.modes

import com.example.vemorize.data.chat.ConversationRepository
import com.example.vemorize.data.dto.vemorize_api.ApiLLMContext
import com.example.vemorize.domain.chat.actions.Actions
import com.example.vemorize.domain.chat.actions.ToolRegistry
import com.example.vemorize.domain.chat.commands.VoiceCommand
import com.example.vemorize.domain.chat.managers.NavigationManager
import com.example.vemorize.domain.chat.modes.commands.NextContentCommand
import com.example.vemorize.domain.chat.modes.commands.ReadCurrentCommand
import com.example.vemorize.domain.chat.modes.commands.ReadingHelpCommand
import com.example.vemorize.domain.chat.modes.commands.ReadingSwitchModeCommand
import com.example.vemorize.domain.chat.modes.commands.StopReadingCommand
import com.example.vemorize.domain.chat.model.ChatMode
import com.example.vemorize.domain.chat.model.HandlerResponse
import com.example.vemorize.domain.chat.model.TtsModel

/**
 * Handler for READING mode - reading content with navigation
 * Port of TypeScript ReadingHandler from reading/handler.ts
 */
class ReadingModeHandler(
    conversationRepository: ConversationRepository,
    actions: Actions,
    navigationManager: NavigationManager,
    toolRegistry: ToolRegistry
) : BaseModeHandler(conversationRepository, actions, navigationManager, toolRegistry) {

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

    override suspend fun buildLLMContext(userInput: String): ApiLLMContext {
        val currentContent = navigationManager.getReadingText() ?: "No content available"

        return ApiLLMContext(
            userMessage = userInput,
            toolNames = getToolNames(),
            mode = "reading",
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
        return "I'm in reading mode. Say 'next' to continue or 'exit' to leave."
    }

    override fun handleConversationError(error: Exception): HandlerResponse {
        return HandlerResponse(
            generatedBy = mode,
            message = "Sorry, I had trouble processing that in reading mode. Please try again."
        )
    }

    override fun getPreferredTtsModel(): TtsModel {
        return TtsModel.OPENAI_GPT_4O_MINI
    }
}
