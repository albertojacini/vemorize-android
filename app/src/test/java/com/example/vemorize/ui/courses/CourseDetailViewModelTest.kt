package com.example.vemorize.ui.courses

import com.example.vemorize.domain.model.Course
import com.example.vemorize.domain.model.CourseNode
import com.example.vemorize.domain.model.CourseTree
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Simple unit tests for CourseDetailViewModel.
 * Full integration tests with mocked repositories can be added later.
 */
class CourseDetailViewModelTest {

    @Test
    fun `CourseDetailUiState has expected sealed implementations`() {
        val loading: CourseDetailUiState = CourseDetailUiState.Loading
        val course = Course(
            id = "1",
            userId = "user1",
            templateId = null,
            title = "German Verbs",
            description = "Learn basic German verbs",
            createdAt = "2025-01-01T00:00:00Z",
            updatedAt = "2025-01-01T00:00:00Z"
        )
        val tree = CourseTree(rootNode = null, allNodes = emptyList())
        val success: CourseDetailUiState = CourseDetailUiState.Success(course, tree)
        val error: CourseDetailUiState = CourseDetailUiState.Error("test error")

        assertEquals(CourseDetailUiState.Loading, loading)
        assert(success is CourseDetailUiState.Success)
        assert(error is CourseDetailUiState.Error)
    }

    @Test
    fun `CourseDetailUiState Success contains course and tree`() {
        val course = Course(
            id = "1",
            userId = "user1",
            templateId = null,
            title = "German Verbs",
            description = "Learn basic German verbs",
            createdAt = "2025-01-01T00:00:00Z",
            updatedAt = "2025-01-01T00:00:00Z"
        )

        val rootNode = CourseNode(
            id = "root-1",
            courseId = "1",
            parentId = null,
            nodeType = "container",
            leafType = null,
            title = "Root Container",
            description = "Root",
            orderIndex = 0,
            readingTextRegular = null,
            readingTextShort = null,
            readingTextLong = null,
            quizQuestions = null,
            data = null,
            createdAt = "2025-01-01T00:00:00Z"
        )

        val tree = CourseTree.fromNodes(listOf(rootNode))
        val successState = CourseDetailUiState.Success(course, tree)

        assertEquals("German Verbs", successState.course.title)
        assertEquals("root-1", successState.tree.rootNode?.id)
    }
}
