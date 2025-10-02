package com.example.vemorize.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Course(
    val id: String,
    @SerialName("user_id")
    val userId: String,
    @SerialName("template_id")
    val templateId: String? = null,
    val title: String,
    val description: String?,
    @SerialName("created_at")
    val createdAt: String,
    @SerialName("updated_at")
    val updatedAt: String
)
