package com.example.vemorize.domain.chat.managers

import android.util.Log
import com.example.vemorize.data.chat.NavigationRepository
import com.example.vemorize.data.courses.CoursesRepository
import com.example.vemorize.data.courses.CourseTreeRepository
import com.example.vemorize.domain.chat.model.ChatMode
import com.example.vemorize.domain.chat.model.Navigation
import com.example.vemorize.domain.courses.Course
import com.example.vemorize.domain.courses.CourseNode
import com.example.vemorize.domain.courses.CourseTree
import javax.inject.Inject

/**
 * Manages course navigation and mode
 */
class NavigationManager(
    private val navigationRepository: NavigationRepository,
    private val coursesRepository: CoursesRepository,
    private val courseTreeRepository: CourseTreeRepository,
    private val userId: String
) {
    var activeCourse: Course? = null
        private set

    var activeCourseTree: CourseTree? = null
        private set

    var activeNavigation: Navigation? = null
        private set

    var mode: ChatMode = ChatMode.IDLE

    var readingConfig: ReadingConfig = ReadingConfig(length = ReadingLength.REGULAR)

    /**
     * Load a course
     */
    suspend fun loadCourse(course: Course) {
        activeCourse = course
        activeCourseTree = courseTreeRepository.getCourseTree(course.id)
        activeNavigation = navigationRepository.getOrCreateNavigation(userId, course.id)
    }


    /**
     * Switch to a mode
     */
    fun switchToMode(newMode: ChatMode) {
        Log.d(TAG, "Switching mode from $mode to $newMode")
        mode = newMode
        Log.d(TAG, "Mode switched successfully to $mode")
    }

    companion object {
        private const val TAG = "NavigationManager"
    }

    /**
     * Get current leaf ID
     */
    fun getCurrentLeafId(): String? = activeNavigation?.currentLeafId

    /**
     * Get current leaf node
     */
    fun getCurrentLeaf(): CourseNode? {
        val navigation = activeNavigation ?: return null
        val tree = activeCourseTree ?: return null
        return tree.getLeafById(navigation.currentLeafId)
    }

    /**
     * Get reading text based on config
     */
    fun getReadingText(): String? {
        val leaf = getCurrentLeaf() ?: return null

        return when (readingConfig.length) {
            ReadingLength.SHORT -> leaf.readingTextShort
            ReadingLength.REGULAR -> leaf.readingTextRegular
            ReadingLength.LONG -> leaf.readingTextLong
        }
    }

    /**
     * Move navigation by steps
     */
    suspend fun moveNavigation(steps: Int): CourseNode? {
        val navigation = activeNavigation ?: return null
        val tree = activeCourseTree ?: return null

        val currentLeaf = tree.getLeafById(navigation.currentLeafId) ?: return null
        val nextLeaf = tree.getLeafAtOffset(currentLeaf, steps) ?: return null

        activeNavigation = navigationRepository.updateCurrentLeaf(navigation.id, nextLeaf.id)
        return nextLeaf
    }

}

data class ReadingConfig(
    val length: ReadingLength = ReadingLength.REGULAR
)

enum class ReadingLength {
    SHORT, REGULAR, LONG
}
