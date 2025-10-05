package com.example.vemorize.domain.chat.modes

import com.example.vemorize.data.chat.ChatApiClient
import com.example.vemorize.domain.chat.actions.Actions
import com.example.vemorize.domain.chat.actions.ToolRegistry
import com.example.vemorize.domain.chat.managers.NavigationManager
import com.example.vemorize.domain.model.chat.ChatMode
import com.example.vemorize.domain.model.chat.ChatResponse
import com.example.vemorize.domain.model.chat.HandlerResponse
import com.example.vemorize.domain.model.chat.LLMRequest

/**
 * Handler for QUIZ mode - testing knowledge
 */
class QuizModeHandler(
    chatApiClient: ChatApiClient,
    actions: Actions,
    navigationManager: NavigationManager,
    toolRegistry: ToolRegistry
) : BaseModeHandler(chatApiClient, actions, navigationManager, toolRegistry) {

    override val mode = ChatMode.QUIZ

    override suspend fun onEnter(): String {
        return "Quiz mode activated. I'll ask you questions about the content."
    }

    override suspend fun onExit() {
        // Save quiz results if needed
    }

    override suspend fun buildLLMRequest(userInput: String): LLMRequest {
        val course = navigationManager.activeCourse
            ?: throw IllegalStateException("No active course")

        return LLMRequest(
            userMessage = userInput,
            systemPrompt = buildQuizSystemPrompt(),
            courseId = course.id,
            userId = course.userId
        )
    }

    private fun buildQuizSystemPrompt(): String {
        return """
            You are a quiz assistant. Your role is to:
            - Ask questions about the course content
            - Evaluate user answers
            - Provide feedback
            - Track quiz progress

            Available tools:
            - provide_chat_response: Respond to the user
            - exit_mode: Exit quiz mode
        """.trimIndent()
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
