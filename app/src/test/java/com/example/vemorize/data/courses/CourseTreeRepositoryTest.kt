package com.example.vemorize.data.courses

import com.example.vemorize.domain.courses.CourseNode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

/**
 * Simple unit tests for CourseTreeRepository.
 * Full integration tests with Supabase mocking can be added later.
 */
class CourseTreeRepositoryTest {

    @Test
    fun `CourseNode model has expected properties`() {
        val node = CourseNode(
            id = "node-1",
            courseId = "course-1",
            parentId = null,
            nodeType = "container",
            leafType = null,
            title = "Root Container",
            description = "Description",
            orderIndex = 0,
            readingTextRegular = null,
            readingTextShort = null,
            readingTextLong = null,
            quizQuestions = null,
            data = null,
            createdAt = "2025-01-01T00:00:00Z"
        )

        assertNotNull(node.id)
        assertNotNull(node.courseId)
        assertNotNull(node.title)
        assertEquals("container", node.nodeType)
    }

    @Test
    fun `CourseNode leaf has reading texts`() {
        val leafNode = CourseNode(
            id = "leaf-1",
            courseId = "course-1",
            parentId = "root-1",
            nodeType = "leaf",
            leafType = "text",
            title = "Text Leaf",
            description = "Leaf description",
            orderIndex = 1,
            readingTextRegular = "Regular reading text",
            readingTextShort = "Short text",
            readingTextLong = "Long reading text",
            quizQuestions = listOf("Question 1", "Question 2"),
            data = null,
            createdAt = "2025-01-01T00:00:00Z"
        )

        assertEquals("leaf", leafNode.nodeType)
        assertEquals("text", leafNode.leafType)
        assertNotNull(leafNode.readingTextRegular)
        assertNotNull(leafNode.readingTextShort)
        assertNotNull(leafNode.readingTextLong)
        assertEquals(2, leafNode.quizQuestions?.size)
    }
}
