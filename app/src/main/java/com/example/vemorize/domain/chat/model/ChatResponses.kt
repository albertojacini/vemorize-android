package com.example.vemorize.domain.chat.model

/**
 * Domain response types for chat interactions
 * These represent business logic concepts, NOT API payloads
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
 * Action result
 */
data class ActionResult(
    val success: Boolean,
    val data: Any? = null,
    val error: String? = null
)
