package com.example.vemorize.domain.chat.managers

import com.example.vemorize.data.chat.NavigationRepository
import com.example.vemorize.data.courses.CoursesRepository
import com.example.vemorize.data.courses.CourseTreeRepository
import com.example.vemorize.domain.chat.model.Navigation
import com.example.vemorize.domain.courses.Course
import com.example.vemorize.domain.courses.CourseNode
import com.example.vemorize.domain.courses.CourseTree
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class NavigationManagerTest {
    private lateinit var navigationManager: NavigationManager
    private lateinit var navigationRepository: NavigationRepository
    private lateinit var coursesRepository: CoursesRepository
    private lateinit var courseTreeRepository: CourseTreeRepository

    private val testUserId = "test-user-id"
    private val testCourseId = "test-course-id"
    private val testLeaf1Id = "leaf-1"
    private val testLeaf2Id = "leaf-2"
    private val testLeaf3Id = "leaf-3"

    @Before
    fun setup() {
        navigationRepository = mockk()
        coursesRepository = mockk()
        courseTreeRepository = mockk()

        navigationManager = NavigationManager(
            navigationRepository,
            coursesRepository,
            courseTreeRepository,
            testUserId
        )
    }

    @Test
    fun `getCurrentLeaf returns correct leaf`() = runTest {
        // Given
        val course = createTestCourse()
        val tree = createTestTree()
        val navigation = Navigation(
            id = "nav-id",
            userId = testUserId,
            courseId = testCourseId,
            currentLeafId = testLeaf1Id,
            createdAt = "2024-01-01T00:00:00Z",
            updatedAt = "2024-01-01T00:00:00Z"
        )

        coEvery { courseTreeRepository.getCourseTree(testCourseId) } returns tree
        coEvery { navigationRepository.getOrCreateNavigation(testUserId, testCourseId) } returns navigation

        // When
        navigationManager.loadCourse(course)
        val currentLeaf = navigationManager.getCurrentLeaf()

        // Then
        assertNotNull(currentLeaf)
        assertEquals(testLeaf1Id, currentLeaf?.id)
        assertEquals("leaf", currentLeaf?.nodeType)
    }

    @Test
    fun `getReadingText returns regular text by default`() = runTest {
        // Given
        val course = createTestCourse()
        val tree = createTestTree()
        val navigation = Navigation(
            id = "nav-id",
            userId = testUserId,
            courseId = testCourseId,
            currentLeafId = testLeaf1Id,
            createdAt = "2024-01-01T00:00:00Z",
            updatedAt = "2024-01-01T00:00:00Z"
        )

        coEvery { courseTreeRepository.getCourseTree(testCourseId) } returns tree
        coEvery { navigationRepository.getOrCreateNavigation(testUserId, testCourseId) } returns navigation

        // When
        navigationManager.loadCourse(course)
        val readingText = navigationManager.getReadingText()

        // Then
        assertEquals("Regular reading text for leaf 1", readingText)
    }

    @Test
    fun `getReadingText returns short text when configured`() = runTest {
        // Given
        val course = createTestCourse()
        val tree = createTestTree()
        val navigation = Navigation(
            id = "nav-id",
            userId = testUserId,
            courseId = testCourseId,
            currentLeafId = testLeaf1Id,
            createdAt = "2024-01-01T00:00:00Z",
            updatedAt = "2024-01-01T00:00:00Z"
        )

        coEvery { courseTreeRepository.getCourseTree(testCourseId) } returns tree
        coEvery { navigationRepository.getOrCreateNavigation(testUserId, testCourseId) } returns navigation

        navigationManager.loadCourse(course)
        navigationManager.readingConfig = ReadingConfig(length = ReadingLength.SHORT)

        // When
        val readingText = navigationManager.getReadingText()

        // Then
        assertEquals("Short reading text for leaf 1", readingText)
    }

    @Test
    fun `moveNavigation moves to next leaf`() = runTest {
        // Given
        val course = createTestCourse()
        val tree = createTestTree()
        val navigation = Navigation(
            id = "nav-id",
            userId = testUserId,
            courseId = testCourseId,
            currentLeafId = testLeaf1Id,
            createdAt = "2024-01-01T00:00:00Z",
            updatedAt = "2024-01-01T00:00:00Z"
        )
        val updatedNavigation = navigation.copy(currentLeafId = testLeaf2Id)

        coEvery { courseTreeRepository.getCourseTree(testCourseId) } returns tree
        coEvery { navigationRepository.getOrCreateNavigation(testUserId, testCourseId) } returns navigation
        coEvery { navigationRepository.updateCurrentLeaf("nav-id", testLeaf2Id) } returns updatedNavigation

        navigationManager.loadCourse(course)

        // When
        val nextLeaf = navigationManager.moveNavigation(1)

        // Then
        assertNotNull(nextLeaf)
        assertEquals(testLeaf2Id, nextLeaf?.id)
    }

    @Test
    fun `moveNavigation moves to previous leaf`() = runTest {
        // Given
        val course = createTestCourse()
        val tree = createTestTree()
        val navigation = Navigation(
            id = "nav-id",
            userId = testUserId,
            courseId = testCourseId,
            currentLeafId = testLeaf2Id,
            createdAt = "2024-01-01T00:00:00Z",
            updatedAt = "2024-01-01T00:00:00Z"
        )
        val updatedNavigation = navigation.copy(currentLeafId = testLeaf1Id)

        coEvery { courseTreeRepository.getCourseTree(testCourseId) } returns tree
        coEvery { navigationRepository.getOrCreateNavigation(testUserId, testCourseId) } returns navigation
        coEvery { navigationRepository.updateCurrentLeaf("nav-id", testLeaf1Id) } returns updatedNavigation

        navigationManager.loadCourse(course)

        // When
        val previousLeaf = navigationManager.moveNavigation(-1)

        // Then
        assertNotNull(previousLeaf)
        assertEquals(testLeaf1Id, previousLeaf?.id)
    }

    private fun createTestCourse(): Course {
        return Course(
            id = testCourseId,
            userId = testUserId,
            templateId = null,
            title = "Test Course",
            description = "Test Description",
            createdAt = "2024-01-01T00:00:00Z",
            updatedAt = "2024-01-01T00:00:00Z"
        )
    }

    private fun createTestTree(): CourseTree {
        val leaf1 = CourseNode(
            id = testLeaf1Id,
            courseId = testCourseId,
            parentId = "root",
            nodeType = "leaf",
            leafType = "text",
            title = "Leaf 1",
            description = "First leaf",
            orderIndex = 0,
            readingTextRegular = "Regular reading text for leaf 1",
            readingTextShort = "Short reading text for leaf 1",
            readingTextLong = "Long reading text for leaf 1",
            quizQuestions = null,
            data = null,
            createdAt = "2024-01-01T00:00:00Z"
        )

        val leaf2 = CourseNode(
            id = testLeaf2Id,
            courseId = testCourseId,
            parentId = "root",
            nodeType = "leaf",
            leafType = "text",
            title = "Leaf 2",
            description = "Second leaf",
            orderIndex = 1,
            readingTextRegular = "Regular reading text for leaf 2",
            readingTextShort = "Short reading text for leaf 2",
            readingTextLong = "Long reading text for leaf 2",
            quizQuestions = null,
            data = null,
            createdAt = "2024-01-01T00:00:00Z"
        )

        val leaf3 = CourseNode(
            id = testLeaf3Id,
            courseId = testCourseId,
            parentId = "root",
            nodeType = "leaf",
            leafType = "text",
            title = "Leaf 3",
            description = "Third leaf",
            orderIndex = 2,
            readingTextRegular = "Regular reading text for leaf 3",
            readingTextShort = "Short reading text for leaf 3",
            readingTextLong = "Long reading text for leaf 3",
            quizQuestions = null,
            data = null,
            createdAt = "2024-01-01T00:00:00Z"
        )

        return CourseTree.fromNodes(listOf(leaf1, leaf2, leaf3))
    }
}
