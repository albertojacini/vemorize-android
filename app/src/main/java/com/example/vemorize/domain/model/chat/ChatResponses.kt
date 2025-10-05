package com.example.vemorize.domain.model.chat

/**
 * Response types for chat interactions
 */

data class ChatResponse(
    val message: String,
    val voiceLang: String? = null,
    val speechSpeed: Float? = null,
    val handlerResponse: HandlerResponse? = null
)

data class HandlerResponse(
    val generatedBy: ChatMode,
    val message: String,
    val voiceLang: String? = null,
    val llmResponse: LLMResponse? = null,
    val commandResponse: CommandResponse? = null
)

data class LLMResponse(
    val message: String,
    val voiceLang: String? = null
)

data class CommandResponse(
    val message: String,
    val voiceLang: String? = null,
    val executedCommand: String? = null
)

/**
 * LLM request context
 */
data class LLMRequest(
    val userMessage: String,
    val systemPrompt: String? = null,
    val conversationHistory: List<Message> = emptyList(),
    val courseId: String,
    val userId: String
)

/**
 * LLM API response
 */
data class LLMApiResponse(
    val toolCalls: List<ToolCall> = emptyList()
)

/**
 * Action result
 */
data class ActionResult(
    val success: Boolean,
    val data: Any? = null,
    val error: String? = null
)
