package com.example.vemorize.data.courses

import android.util.Log
import com.example.vemorize.domain.courses.Annotation
import com.example.vemorize.domain.courses.MemorizationState
import io.github.jan.supabase.postgrest.Postgrest
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import javax.inject.Inject

/**
 * ════════════════════════════════════════════════════════════════════════════
 * ⚠️  WARNING: THIS LOGIC SHOULD BE IN THE BACKEND! ⚠️
 * ════════════════════════════════════════════════════════════════════════════
 *
 * This repository directly accesses Supabase Postgrest for CRUD operations.
 *
 * WHY THIS IS PROBLEMATIC:
 * - Business logic duplication (web client uses backend API with validation)
 * - No centralized validation (Zod schemas exist in backend but not enforced here)
 * - Future business rules (audit logs, state transitions, side effects) will diverge
 * - Multiple clients = multiple implementations = bugs & inconsistency
 *
 * RECOMMENDED APPROACH:
 * - Move annotation operations to backend service layer (like web client does)
 * - Add API endpoints: POST /api/annotations, PATCH /api/annotations/:id
 * - This client should call those endpoints instead of direct DB access
 *
 * WHY WE'RE DOING IT THIS WAY (for now):
 * - Existing Android repos use direct Postgrest (consistency with current pattern)
 * - RLS provides security (user_id filtering at DB level)
 * - Initial development phase - simplicity over architecture
 * - Annotations are simple CRUD with minimal business logic
 *
 * TODO: Refactor to use backend API when:
 * - Business logic becomes complex
 * - Web/mobile feature parity is critical
 * - We add audit logging, analytics, or complex state transitions
 * ════════════════════════════════════════════════════════════════════════════
 */
class SupabaseAnnotationRepositoryImpl @Inject constructor(
    private val postgrest: Postgrest
) : AnnotationRepository {

    override suspend fun getAnnotationsByCourse(courseId: String): List<Annotation> {
        return try {
            Log.d(TAG, "Fetching annotations for course: $courseId")

            val annotations = postgrest
                .from("annotations")
                .select {
                    filter {
                        eq("course_id", courseId)
                    }
                }
                .decodeList<Annotation>()

            Log.d(TAG, "Found ${annotations.size} annotations for course $courseId")
            annotations
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching annotations for course $courseId", e)
            emptyList()
        }
    }

    override suspend fun createAnnotation(
        courseId: String,
        nodeId: String,
        memorizationState: MemorizationState,
        personalNotes: String?,
        visitCount: Int
    ): Annotation {
        return try {
            Log.d(TAG, "Creating annotation for node $nodeId with state $memorizationState")

            // ⚠️ BACKEND MIGRATION NOTE: This should be POST /api/annotations
            // with validation via createAnnotationSchema (Zod)
            val id = java.util.UUID.randomUUID().toString()
            val now = java.time.Instant.now().toString()

            val createRequest = CreateAnnotationRequest(
                id = id,
                courseId = courseId,
                nodeId = nodeId,
                createdAt = now,
                updatedAt = now,
                memorizationState = memorizationState,
                personalNotes = personalNotes,
                visitCount = visitCount
            )

            val created = postgrest
                .from("annotations")
                .insert(createRequest) {
                    select()
                }
                .decodeSingle<Annotation>()

            Log.d(TAG, "Created annotation with id: ${created.id}")
            created
        } catch (e: Exception) {
            Log.e(TAG, "Error creating annotation for node $nodeId", e)
            throw e
        }
    }

    override suspend fun updateAnnotation(
        annotationId: String,
        memorizationState: MemorizationState?,
        personalNotes: String?,
        visitCount: Int?
    ) {
        try {
            Log.d(TAG, "Updating annotation $annotationId")

            // ⚠️ BACKEND MIGRATION NOTE: This should be PATCH /api/annotations/:id
            // with validation via updateAnnotationSchema (Zod)

            // Build update map with only non-null values
            val updates = buildMap {
                put("updated_at", java.time.Instant.now().toString())
                memorizationState?.let { put("memorization_state", it) }
                if (personalNotes != null) put("personal_notes", personalNotes)
                visitCount?.let { put("visit_count", it) }
            }

            postgrest
                .from("annotations")
                .update(updates) {
                    filter {
                        eq("id", annotationId)
                    }
                }

            Log.d(TAG, "Updated annotation $annotationId successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error updating annotation $annotationId", e)
            throw e
        }
    }

    companion object {
        private const val TAG = "AnnotationRepository"
    }
}

/**
 * Request DTOs for Supabase operations
 * ⚠️ These duplicate validation logic that exists in backend Zod schemas
 */
@Serializable
private data class CreateAnnotationRequest(
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
    val personalNotes: String? = null,

    @SerialName("visit_count")
    val visitCount: Int = 1
)

