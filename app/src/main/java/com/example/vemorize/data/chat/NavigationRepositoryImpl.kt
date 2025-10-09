package com.example.vemorize.data.chat

import android.util.Log
import com.example.vemorize.data.courses.CoursesRepository
import com.example.vemorize.domain.model.chat.Navigation
import io.github.jan.supabase.postgrest.Postgrest
import kotlinx.datetime.Clock
import javax.inject.Inject

class NavigationRepositoryImpl @Inject constructor(
    private val postgrest: Postgrest,
    private val coursesRepository: CoursesRepository
) : NavigationRepository {

    override suspend fun getOrCreateNavigation(userId: String, courseId: String): Navigation {
        return try {
            // Try to get existing navigation
            val existing = postgrest
                .from("navigation")
                .select {
                    filter {
                        eq("user_id", userId)
                        eq("course_id", courseId)
                    }
                }
                .decodeSingleOrNull<Navigation>()

            existing ?: createDefaultNavigation(userId, courseId)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting or creating navigation", e)
            throw e
        }
    }

    private suspend fun createDefaultNavigation(userId: String, courseId: String): Navigation {
        // Get the course to find the initial leaf
        val course = coursesRepository.getCourseById(courseId)
            ?: throw IllegalStateException("Course not found: $courseId")

        // TODO: Get the first leaf from course tree
        // For now, using a placeholder
        val initialLeafId = "placeholder-leaf-id"

        val now = Clock.System.now().toString()

        return postgrest
            .from("navigation")
            .insert(Navigation(
                id = java.util.UUID.randomUUID().toString(),
                userId = userId,
                courseId = courseId,
                currentLeafId = initialLeafId,
                createdAt = now,
                updatedAt = now
            ))
            .decodeSingle<Navigation>()
    }

    override suspend fun updateCurrentLeaf(navigationId: String, leafId: String): Navigation {
        return try {
            // Get current navigation
            val current = getNavigationById(navigationId)
                ?: throw IllegalStateException("Navigation not found: $navigationId")

            // Update
            val updated = current.copy(
                currentLeafId = leafId,
                updatedAt = Clock.System.now().toString()
            )

            // Update the navigation
            postgrest
                .from("navigation")
                .update(updated) {
                    filter {
                        eq("id", navigationId)
                    }
                }

            // Fetch and return the updated navigation
            getNavigationById(navigationId)
                ?: throw IllegalStateException("Navigation not found after update: $navigationId")
        } catch (e: Exception) {
            Log.e(TAG, "Error updating current leaf", e)
            throw e
        }
    }

    override suspend fun getNavigationById(navigationId: String): Navigation? {
        return try {
            postgrest
                .from("navigation")
                .select {
                    filter {
                        eq("id", navigationId)
                    }
                }
                .decodeSingleOrNull<Navigation>()
        } catch (e: Exception) {
            Log.e(TAG, "Error getting navigation by id", e)
            null
        }
    }

    companion object {
        private const val TAG = "NavigationRepository"
    }
}
