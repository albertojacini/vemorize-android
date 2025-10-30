package com.example.vemorize.domain.chat.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Simplified cross-course user memory
 * TODO: Expand this as needed when user memory features are implemented
 */
@Serializable
data class UserMemory(
    val id: String,
    @SerialName("user_id")
    val userId: String,
    @SerialName("created_at")
    val createdAt: String,
    @SerialName("updated_at")
    val updatedAt: String
)
