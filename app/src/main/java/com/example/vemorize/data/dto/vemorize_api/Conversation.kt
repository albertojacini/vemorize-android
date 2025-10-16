package com.example.vemorize.data.dto.vemorize_api

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

/**
 * API DTOs for Vemorize Backend API (Supabase Edge Functions)
 * These are API request/response payloads - NOT domain models
 */

/**
 * API LLM Context - matches TypeScript ApiLLMContext
 */
@Serializable
data class ApiLLMContext(
    val userMessage: String,
    val toolNames: List<String>,
    val mode: String,
    val userMemory: String? = null,
    val leafReprForPrompt: String? = null
)

@Serializable
data class RequestData(
    val courseId: String,
    val userId: String,
)

@Serializable
data class LLMRequest(
    val data: RequestData,
    val llmContext: ApiLLMContext
)

/**
 * Tool call from LLM - matches backend format
 */
@Serializable
data class ToolCall(
    val tool: String,
    val args: JsonObject
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
