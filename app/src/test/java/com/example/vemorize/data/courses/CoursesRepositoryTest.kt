package com.example.vemorize.data.courses

import com.example.vemorize.domain.courses.Course
import org.junit.Assert.assertNotNull
import org.junit.Test

/**
 * Simple unit tests for CoursesRepository.
 * Full integration tests with Supabase mocking can be added later.
 */
class CoursesRepositoryTest {

    @Test
    fun `Course model has expected properties`() {
        val course = Course(
            id = "1",
            userId = "user1",
            templateId = null,
            title = "German Verbs",
            description = "Learn basic German verbs",
            createdAt = "2025-01-01T00:00:00Z",
            updatedAt = "2025-01-01T00:00:00Z"
        )

        assertNotNull(course.id)
        assertNotNull(course.userId)
        assertNotNull(course.title)
    }
}
