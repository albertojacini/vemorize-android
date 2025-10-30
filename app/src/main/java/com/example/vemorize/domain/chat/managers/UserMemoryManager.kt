package com.example.vemorize.domain.chat.managers

import android.util.Log
import com.example.vemorize.data.chat.UserMemoryRepository
import com.example.vemorize.domain.chat.model.UserMemory
import javax.inject.Inject

/**
 * Manages user memory (cross-course knowledge)
 */
class UserMemoryManager(
    private val userMemoryRepository: UserMemoryRepository,
    private val userId: String
) {
    private var currentMemory: UserMemory? = null

    /**
     * Initialize and load memory
     */
    suspend fun initialize() {
        try {
            Log.d(TAG, "UserMemoryManager.initialize() - userId: $userId")
            currentMemory = userMemoryRepository.getOrCreateMemory(userId)
            Log.d(TAG, "UserMemoryManager initialized: $currentMemory")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize UserMemoryManager", e)
            throw e
        }
    }

    companion object {
        private const val TAG = "UserMemoryManager"
    }

    /**
     * Get current memory
     */
    suspend fun get(): UserMemory {
        if (currentMemory == null) {
            initialize()
        }
        return currentMemory ?: throw IllegalStateException("Failed to load user memory")
    }

    /**
     * Add a fact
     */
    suspend fun addFact(fact: String) {
        currentMemory = userMemoryRepository.addFact(userId, fact)
    }

    /**
     * Add a preference
     */
    suspend fun addPreference(preference: String) {
        currentMemory = userMemoryRepository.addPreference(userId, preference)
    }

    /**
     * Add a goal
     */
    suspend fun addGoal(goal: String) {
        currentMemory = userMemoryRepository.addGoal(userId, goal)
    }

    /**
     * Add a course to studied courses
     */
    suspend fun addCourseStudied(courseTitle: String) {
        currentMemory = userMemoryRepository.addCourseStudied(userId, courseTitle)
    }

    /**
     * Refresh from backend
     */
    suspend fun refresh() {
        currentMemory = userMemoryRepository.refreshMemory(userId)
    }
}
