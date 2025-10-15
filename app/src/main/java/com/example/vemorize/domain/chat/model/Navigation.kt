package com.example.vemorize.domain.chat.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Current navigation position per user per course
 */
@Serializable
data class Navigation(
    val id: String,
    @SerialName("user_id")
    val userId: String,
    @SerialName("course_id")
    val courseId: String,
    @SerialName("current_leaf_id")
    val currentLeafId: String,
    @SerialName("created_at")
    val createdAt: String,
    @SerialName("updated_at")
    val updatedAt: String
)
