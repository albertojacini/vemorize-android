package com.example.vemorize.data.chat

import android.util.Log
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
        val memory = mapOf(
            "user_id" to userId,
            "facts" to emptyList<String>(),
            "preferences" to emptyList<String>(),
            "goals" to emptyList<String>(),
            "courses_studied" to emptyList<String>(),
            "created_at" to now,
            "updated_at" to now
        )

        return postgrest
            .from("chat_user_memory")
            .insert(memory)
            .decodeSingle<UserMemory>()
    }

    override suspend fun addFact(userId: String, fact: String): UserMemory {
        val current = getOrCreateMemory(userId)
        val updatedFacts = current.facts + fact

        return updateMemory(userId, "facts", updatedFacts)
    }

    override suspend fun addPreference(userId: String, preference: String): UserMemory {
        val current = getOrCreateMemory(userId)
        val updatedPreferences = current.preferences + preference

        return updateMemory(userId, "preferences", updatedPreferences)
    }

    override suspend fun addGoal(userId: String, goal: String): UserMemory {
        val current = getOrCreateMemory(userId)
        val updatedGoals = current.goals + goal

        return updateMemory(userId, "goals", updatedGoals)
    }

    override suspend fun addCourseStudied(userId: String, courseTitle: String): UserMemory {
        val current = getOrCreateMemory(userId)
        if (courseTitle in current.coursesStudied) {
            return current
        }
        val updatedCourses = current.coursesStudied + courseTitle

        return updateMemory(userId, "courses_studied", updatedCourses)
    }

    private suspend fun updateMemory(
        userId: String,
        field: String,
        value: Any
    ): UserMemory {
        return try {
            postgrest
                .from("chat_user_memory")
                .update(
                    mapOf(
                        field to value,
                        "updated_at" to Clock.System.now().toString()
                    )
                ) {
                    filter {
                        eq("user_id", userId)
                    }
                }
                .decodeSingle<UserMemory>()
        } catch (e: Exception) {
            Log.e(TAG, "Error updating memory $field", e)
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
