package com.example.vemorize.data.chat

import com.example.vemorize.domain.model.chat.Navigation

interface NavigationRepository {
    /**
     * Get or create navigation for a course
     */
    suspend fun getOrCreateNavigation(userId: String, courseId: String): Navigation

    /**
     * Update current leaf
     */
    suspend fun updateCurrentLeaf(navigationId: String, leafId: String): Navigation

    /**
     * Get navigation by ID
     */
    suspend fun getNavigationById(navigationId: String): Navigation?
}
