package com.example.vemorize.domain.model.chat

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Conversation(
    val id: String,
    @SerialName("user_id")
    val userId: String,
    @SerialName("course_id")
    val courseId: String,
    val summary: String? = null,
    @SerialName("message_count")
    val messageCount: Int = 0,
    @SerialName("created_at")
    val createdAt: String,
    @SerialName("updated_at")
    val updatedAt: String,
    @SerialName("last_message_at")
    val lastMessageAt: String? = null,
    @SerialName("is_active")
    val isActive: Boolean = true
) {
    fun needsCompression(): Boolean = messageCount > 30 && summary == null

    fun toPromptContext(): String {
        return if (summary != null) {
            "Previous conversation summary: $summary\n\n"
        } else {
            ""
        }
    }
}
