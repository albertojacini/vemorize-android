package com.example.vemorize.domain.model.chat

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

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
 * API LLM Context - matches TypeScript ApiLLMContext
 */
data class ApiLLMContext(
    val userMessage: String,
    val toolNames: List<String>,
    val mode: String,
    val userMemory: String? = null,
    val leafReprForPrompt: String? = null
)

/**
 * Tool call from LLM - matches OpenAI format
 */
@Serializable
data class ToolCall(
    val id: String,
    val type: String,
    val function: ToolFunction
)

@Serializable
data class ToolFunction(
    val name: String,
    val arguments: String
)

/**
 * LLM API response - matches TypeScript ApiLLMResponse
 */
@Serializable
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
