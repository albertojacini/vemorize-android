package com.example.vemorize.data.chat

import android.util.Log
import com.example.vemorize.domain.model.chat.UserAchievements
import com.example.vemorize.domain.model.chat.UserFacts
import com.example.vemorize.domain.model.chat.UserMemory
import io.github.jan.supabase.postgrest.Postgrest
import kotlinx.datetime.Clock
import javax.inject.Inject

class UserMemoryRepositoryImpl @Inject constructor(
    private val postgrest: Postgrest
) : UserMemoryRepository {

    override suspend fun getOrCreateMemory(userId: String): UserMemory {
        return try {
            // Try to get existing memory
            val existing = postgrest
                .from("chat_user_memory")
                .select {
                    filter {
                        eq("user_id", userId)
                    }
                }
                .decodeSingleOrNull<UserMemory>()

            existing ?: createDefaultMemory(userId)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting or creating memory", e)
            throw e
        }
    }

    private suspend fun createDefaultMemory(userId: String): UserMemory {
        val now = Clock.System.now().toString()

        return postgrest
            .from("chat_user_memory")
            .insert(UserMemory(
                id = java.util.UUID.randomUUID().toString(),
                userId = userId,
                facts = UserFacts(),
                preferences = emptyMap(),
                goals = emptyList(),
                achievements = UserAchievements(),
                createdAt = now,
                updatedAt = now
            ))
            .decodeSingle<UserMemory>()
    }

    override suspend fun addFact(userId: String, fact: String): UserMemory {
        val current = getOrCreateMemory(userId)
        val updatedKnowledgeAreas = current.facts.knowledgeAreas + fact
        val updatedFacts = current.facts.copy(knowledgeAreas = updatedKnowledgeAreas)

        return updateMemory(userId, current.copy(facts = updatedFacts))
    }

    override suspend fun addPreference(userId: String, preference: String): UserMemory {
        // Preferences are stored as a map, so this doesn't make sense with the new schema
        // For now, just return current memory
        return getOrCreateMemory(userId)
    }

    override suspend fun addGoal(userId: String, goal: String): UserMemory {
        val current = getOrCreateMemory(userId)
        if (goal in current.goals) {
            return current
        }
        val updatedGoals = current.goals + goal

        return updateMemory(userId, current.copy(goals = updatedGoals))
    }

    override suspend fun addCourseStudied(userId: String, courseTitle: String): UserMemory {
        val current = getOrCreateMemory(userId)
        if (courseTitle in current.facts.coursesStudied) {
            return current
        }
        val updatedCoursesStudied = current.facts.coursesStudied + courseTitle
        val updatedFacts = current.facts.copy(coursesStudied = updatedCoursesStudied)

        return updateMemory(userId, current.copy(facts = updatedFacts))
    }

    private suspend fun updateMemory(
        userId: String,
        memory: UserMemory
    ): UserMemory {
        return try {
            // Update the updatedAt timestamp
            val updated = memory.copy(
                updatedAt = Clock.System.now().toString()
            )

            // Update in database
            postgrest
                .from("chat_user_memory")
                .update(updated) {
                    filter {
                        eq("user_id", userId)
                    }
                }
                .decodeSingle<UserMemory>()
        } catch (e: Exception) {
            Log.e(TAG, "Error updating memory", e)
            throw e
        }
    }

    override suspend fun refreshMemory(userId: String): UserMemory {
        return getOrCreateMemory(userId)
    }

    companion object {
        private const val TAG = "UserMemoryRepository"
    }
}
