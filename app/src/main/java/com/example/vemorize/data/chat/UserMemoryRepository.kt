package com.example.vemorize.data.chat

import com.example.vemorize.domain.chat.model.UserMemory

interface UserMemoryRepository {
    /**
     * Get or create user memory
     */
    suspend fun getOrCreateMemory(userId: String): UserMemory

    /**
     * Add a fact to user memory
     */
    suspend fun addFact(userId: String, fact: String): UserMemory

    /**
     * Add a preference to user memory
     */
    suspend fun addPreference(userId: String, preference: String): UserMemory

    /**
     * Add a goal to user memory
     */
    suspend fun addGoal(userId: String, goal: String): UserMemory

    /**
     * Add a course to studied courses
     */
    suspend fun addCourseStudied(userId: String, courseTitle: String): UserMemory

    /**
     * Refresh memory from backend
     */
    suspend fun refreshMemory(userId: String): UserMemory
}
