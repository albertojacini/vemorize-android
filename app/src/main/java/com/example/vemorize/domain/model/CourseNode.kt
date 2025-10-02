package com.example.vemorize.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class CourseNode(
    val id: String,
    @SerialName("course_id")
    val courseId: String,
    @SerialName("parent_id")
    val parentId: String? = null,
    @SerialName("node_type")
    val nodeType: String, // "container" or "leaf"
    @SerialName("leaf_type")
    val leafType: String? = null, // "language_vocabulary", "code", "text"
    val title: String,
    val description: String? = null,
    @SerialName("order_index")
    val orderIndex: Int,
    @SerialName("reading_text_regular")
    val readingTextRegular: String? = null,
    @SerialName("reading_text_short")
    val readingTextShort: String? = null,
    @SerialName("reading_text_long")
    val readingTextLong: String? = null,
    @SerialName("quiz_questions")
    val quizQuestions: List<String>? = null,
    val data: JsonElement? = null, // JSONB - can be any JSON structure
    @SerialName("created_at")
    val createdAt: String
)
