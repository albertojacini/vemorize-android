package com.example.vemorize.domain.model.chat

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.JsonElement

/**
 * LangChain-compatible message format with tool call support
 */
@Serializable
data class Message(
    val id: String,
    @SerialName("conversation_id")
    val conversationId: String,
    val type: MessageType,
    val content: String,
    @Transient
    val additionalKwargs: Map<String, JsonElement>? = null,
    @Transient
    val toolCalls: List<ToolCall>? = null,
    @SerialName("created_at")
    val createdAt: String
)

@Serializable
enum class MessageType {
    @SerialName("HumanMessage")
    HUMAN,
    @SerialName("AIMessage")
    AI,
    @SerialName("SystemMessage")
    SYSTEM,
    @SerialName("ToolMessage")
    TOOL
}

@Serializable
data class ToolCall(
    val tool: String,
    val args: Map<String, JsonElement>
)
