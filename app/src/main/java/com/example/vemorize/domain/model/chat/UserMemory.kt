package com.example.vemorize.domain.model.chat

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Cross-course user knowledge (facts, preferences, goals)
 */
@Serializable
data class UserMemory(
    val id: String,
    @SerialName("user_id")
    val userId: String,
    val facts: List<String> = emptyList(),
    val preferences: List<String> = emptyList(),
    val goals: List<String> = emptyList(),
    @SerialName("courses_studied")
    val coursesStudied: List<String> = emptyList(),
    @SerialName("created_at")
    val createdAt: String,
    @SerialName("updated_at")
    val updatedAt: String
)
