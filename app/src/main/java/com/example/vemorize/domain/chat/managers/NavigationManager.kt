package com.example.vemorize.domain.chat.managers

import android.util.Log
import com.example.vemorize.data.chat.NavigationRepository
import com.example.vemorize.data.courses.CoursesRepository
import com.example.vemorize.domain.model.chat.ChatMode
import com.example.vemorize.domain.model.chat.Navigation
import com.example.vemorize.domain.model.courses.Course
import javax.inject.Inject

/**
 * Manages course navigation and mode
 */
class NavigationManager(
    private val navigationRepository: NavigationRepository,
    private val coursesRepository: CoursesRepository,
    private val userId: String
) {
    var activeCourse: Course? = null
        private set

    var activeNavigation: Navigation? = null
        private set

    var mode: ChatMode = ChatMode.IDLE

    var readingConfig: ReadingConfig = ReadingConfig.REGULAR

    /**
     * Load a course
     */
    suspend fun loadCourse(course: Course) {
        activeCourse = course
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
     * Get reading text based on config
     * Note: This is simplified - actual implementation would fetch from course tree
     */
    fun getReadingText(): String? {
        // TODO: Implement actual reading text retrieval from course tree
        return "Reading text placeholder"
    }

    /**
     * Move navigation by steps
     */
    suspend fun moveNavigation(steps: Int) {
        val navigation = activeNavigation ?: return

        // TODO: Implement actual leaf navigation using course tree
        // For now, this is a placeholder
        val newLeafId = "next-leaf-id" // Placeholder

        activeNavigation = navigationRepository.updateCurrentLeaf(navigation.id, newLeafId)
    }

    enum class ReadingConfig {
        SHORT, REGULAR, LONG
    }
}
