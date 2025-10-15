package com.example.vemorize.domain.chat.model

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
 * Tool call from LLM - matches backend format
 */
@Serializable
data class ToolCall(
    val tool: String,
    val args: kotlinx.serialization.json.JsonObject
)

/**
 * LLM API response wrapper
 */
@Serializable
data class LLMApiResponse(
    val success: Boolean,
    val data: LLMApiResponseData? = null,
    val error: String? = null
)

@Serializable
data class LLMApiResponseData(
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
