package com.example.vemorize.data.courses

import com.example.vemorize.domain.model.annotations.Annotation
import com.example.vemorize.domain.model.annotations.MemorizationState

/**
 * Repository for managing course node annotations (learning progress tracking)
 *
 * NOTE: This currently uses direct Postgrest access.
 * TODO: Consider migrating to backend API for centralized business logic & validation
 */
interface AnnotationRepository {
    /**
     * Get all annotations for a course
     */
    suspend fun getAnnotationsByCourse(courseId: String): List<Annotation>

    /**
     * Create a new annotation for a node
     * Used for lazy creation on first user interaction
     */
    suspend fun createAnnotation(
        courseId: String,
        nodeId: String,
        memorizationState: MemorizationState = MemorizationState.NEW,
        personalNotes: String? = null,
        visitCount: Int = 1
    ): Annotation

    /**
     * Update an existing annotation
     * Pass null for fields you don't want to update
     */
    suspend fun updateAnnotation(
        annotationId: String,
        memorizationState: MemorizationState? = null,
        personalNotes: String? = null,
        visitCount: Int? = null
    )
}
