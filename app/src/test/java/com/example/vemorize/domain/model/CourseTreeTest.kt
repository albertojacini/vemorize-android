package com.example.vemorize.domain.model

import com.example.vemorize.domain.courses.CourseNode
import com.example.vemorize.domain.courses.CourseTree
import org.junit.Assert.*
import org.junit.Test

class CourseTreeTest {

    @Test
    fun `fromNodes returns empty tree when nodes list is empty`() {
        val tree = CourseTree.fromNodes(emptyList())

        assertNull(tree.rootNode)
        assertTrue(tree.allNodes.isEmpty())
    }

    @Test
    fun `fromNodes finds root node with null parent_id`() {
        val rootNode = CourseNode(
            id = "root-1",
            courseId = "course-1",
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

        val childNode = CourseNode(
            id = "child-1",
            courseId = "course-1",
            parentId = "root-1",
            nodeType = "leaf",
            leafType = "text",
            title = "Child Leaf",
            description = "Leaf",
            orderIndex = 1,
            readingTextRegular = "Regular text",
            readingTextShort = "Short text",
            readingTextLong = "Long text",
            quizQuestions = null,
            data = null,
            createdAt = "2025-01-01T00:00:00Z"
        )

        val tree = CourseTree.fromNodes(listOf(rootNode, childNode))

        assertNotNull(tree.rootNode)
        assertEquals("root-1", tree.rootNode?.id)
        assertEquals(2, tree.allNodes.size)
    }

    @Test
    fun `fromNodes sorts nodes by order_index`() {
        val node1 = CourseNode(
            id = "node-1",
            courseId = "course-1",
            parentId = null,
            nodeType = "container",
            leafType = null,
            title = "Node 1",
            description = null,
            orderIndex = 2,
            readingTextRegular = null,
            readingTextShort = null,
            readingTextLong = null,
            quizQuestions = null,
            data = null,
            createdAt = "2025-01-01T00:00:00Z"
        )

        val node2 = CourseNode(
            id = "node-2",
            courseId = "course-1",
            parentId = "node-1",
            nodeType = "leaf",
            leafType = "text",
            title = "Node 2",
            description = null,
            orderIndex = 0,
            readingTextRegular = "Text",
            readingTextShort = "Text",
            readingTextLong = "Text",
            quizQuestions = null,
            data = null,
            createdAt = "2025-01-01T00:00:00Z"
        )

        val node3 = CourseNode(
            id = "node-3",
            courseId = "course-1",
            parentId = "node-1",
            nodeType = "leaf",
            leafType = "text",
            title = "Node 3",
            description = null,
            orderIndex = 1,
            readingTextRegular = "Text",
            readingTextShort = "Text",
            readingTextLong = "Text",
            quizQuestions = null,
            data = null,
            createdAt = "2025-01-01T00:00:00Z"
        )

        val tree = CourseTree.fromNodes(listOf(node1, node3, node2))

        assertEquals("node-2", tree.allNodes[0].id)
        assertEquals("node-3", tree.allNodes[1].id)
        assertEquals("node-1", tree.allNodes[2].id)
    }

    @Test
    fun `getChildren returns nodes with matching parent_id`() {
        val rootNode = CourseNode(
            id = "root-1",
            courseId = "course-1",
            parentId = null,
            nodeType = "container",
            leafType = null,
            title = "Root",
            description = null,
            orderIndex = 0,
            readingTextRegular = null,
            readingTextShort = null,
            readingTextLong = null,
            quizQuestions = null,
            data = null,
            createdAt = "2025-01-01T00:00:00Z"
        )

        val child1 = CourseNode(
            id = "child-1",
            courseId = "course-1",
            parentId = "root-1",
            nodeType = "leaf",
            leafType = "text",
            title = "Child 1",
            description = null,
            orderIndex = 1,
            readingTextRegular = "Text",
            readingTextShort = "Text",
            readingTextLong = "Text",
            quizQuestions = null,
            data = null,
            createdAt = "2025-01-01T00:00:00Z"
        )

        val child2 = CourseNode(
            id = "child-2",
            courseId = "course-1",
            parentId = "root-1",
            nodeType = "leaf",
            leafType = "text",
            title = "Child 2",
            description = null,
            orderIndex = 2,
            readingTextRegular = "Text",
            readingTextShort = "Text",
            readingTextLong = "Text",
            quizQuestions = null,
            data = null,
            createdAt = "2025-01-01T00:00:00Z"
        )

        val tree = CourseTree.fromNodes(listOf(rootNode, child1, child2))
        val children = tree.getChildren("root-1")

        assertEquals(2, children.size)
        assertTrue(children.all { it.parentId == "root-1" })
    }

    @Test
    fun `getAllLeaves returns only leaf nodes`() {
        val container = CourseNode(
            id = "container-1",
            courseId = "course-1",
            parentId = null,
            nodeType = "container",
            leafType = null,
            title = "Container",
            description = null,
            orderIndex = 0,
            readingTextRegular = null,
            readingTextShort = null,
            readingTextLong = null,
            quizQuestions = null,
            data = null,
            createdAt = "2025-01-01T00:00:00Z"
        )

        val leaf1 = CourseNode(
            id = "leaf-1",
            courseId = "course-1",
            parentId = "container-1",
            nodeType = "leaf",
            leafType = "text",
            title = "Leaf 1",
            description = null,
            orderIndex = 1,
            readingTextRegular = "Text",
            readingTextShort = "Text",
            readingTextLong = "Text",
            quizQuestions = null,
            data = null,
            createdAt = "2025-01-01T00:00:00Z"
        )

        val leaf2 = CourseNode(
            id = "leaf-2",
            courseId = "course-1",
            parentId = "container-1",
            nodeType = "leaf",
            leafType = "code",
            title = "Leaf 2",
            description = null,
            orderIndex = 2,
            readingTextRegular = "Code",
            readingTextShort = "Code",
            readingTextLong = "Code",
            quizQuestions = null,
            data = null,
            createdAt = "2025-01-01T00:00:00Z"
        )

        val tree = CourseTree.fromNodes(listOf(container, leaf1, leaf2))
        val leaves = tree.getAllLeaves()

        assertEquals(2, leaves.size)
        assertTrue(leaves.all { it.nodeType == "leaf" })
    }
}
