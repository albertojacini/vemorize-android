package com.example.vemorize.ui.courses

import com.example.vemorize.domain.model.courses.Course
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Simple unit tests for CoursesViewModel.
 * Full integration tests with mocked repository can be added later.
 */
class CoursesViewModelTest {

    @Test
    fun `CoursesUiState has expected sealed implementations`() {
        val loading: CoursesUiState = CoursesUiState.Loading
        val success: CoursesUiState = CoursesUiState.Success(emptyList())
        val error: CoursesUiState = CoursesUiState.Error("test error")

        assertEquals(CoursesUiState.Loading, loading)
        assert(success is CoursesUiState.Success)
        assert(error is CoursesUiState.Error)
    }

    @Test
    fun `CoursesUiState Success contains courses list`() {
        val courses = listOf(
            Course(
                id = "1",
                userId = "user1",
                templateId = null,
                title = "German Verbs",
                description = "Learn basic German verbs",
                createdAt = "2025-01-01T00:00:00Z",
                updatedAt = "2025-01-01T00:00:00Z"
            )
        )

        val successState = CoursesUiState.Success(courses)

        assertEquals(1, successState.courses.size)
        assertEquals("German Verbs", successState.courses.first().title)
    }
}
