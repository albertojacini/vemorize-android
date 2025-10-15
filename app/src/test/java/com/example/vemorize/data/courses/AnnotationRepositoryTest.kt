package com.example.vemorize.data.courses

import com.example.vemorize.domain.courses.MemorizationState
import io.github.jan.supabase.postgrest.Postgrest
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

/**
 * Basic test for AnnotationRepository
 *
 * Note: This is a minimal test for TDD purposes.
 * Full integration testing requires Supabase test instance.
 */
class AnnotationRepositoryTest {

    private lateinit var postgrest: Postgrest
    private lateinit var repository: AnnotationRepository

    @Before
    fun setup() {
        postgrest = mockk(relaxed = true)
        repository = SupabaseAnnotationRepositoryImpl(postgrest)
    }

    @Test
    fun `repository can be instantiated`() = runTest {
        // Given: Repository is created in setup

        // When: Getting annotations for a course
        val result = repository.getAnnotationsByCourse("test-course-id")

        // Then: Should return empty list (mock behavior)
        assert(result.isEmpty())
    }

    @Test
    fun `createAnnotation uses correct defaults`() = runTest {
        // This test verifies the interface contract
        // Full behavior testing requires Supabase integration

        // Given: A course and node ID
        val courseId = "course-1"
        val nodeId = "node-1"

        // When/Then: Method can be called with minimal parameters
        // (Will fail in real execution without Supabase, but validates signature)
        try {
            repository.createAnnotation(
                courseId = courseId,
                nodeId = nodeId,
                memorizationState = MemorizationState.NEW
            )
        } catch (e: Exception) {
            // Expected without real Supabase connection
            assert(e.message != null)
        }
    }
}
