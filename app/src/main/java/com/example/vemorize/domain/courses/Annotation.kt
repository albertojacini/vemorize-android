package com.example.vemorize.domain.courses

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Memorization state for tracking learning progress
 * Matches the DB enum: 'new' | 'learning' | 'review' | 'mastered'
 */
@Serializable
enum class MemorizationState {
    @SerialName("new")
    NEW,

    @SerialName("learning")
    LEARNING,

    @SerialName("review")
    REVIEW,

    @SerialName("mastered")
    MASTERED
}

/**
 * Annotation domain model - tracks mutable learning progress for course nodes
 *
 * Database table: annotations
 * - Separate from immutable course structure
 * - One annotation per user per node (lazy creation on first interaction)
 * - RLS: Users only access their own annotations
 */
@Serializable
data class Annotation(
    val id: String,

    @SerialName("course_id")
    val courseId: String,

    @SerialName("node_id")
    val nodeId: String,

    @SerialName("created_at")
    val createdAt: String,

    @SerialName("updated_at")
    val updatedAt: String,

    @SerialName("memorization_state")
    val memorizationState: MemorizationState,

    @SerialName("personal_notes")
    val personalNotes: String?,

    @SerialName("visit_count")
    val visitCount: Int
)
