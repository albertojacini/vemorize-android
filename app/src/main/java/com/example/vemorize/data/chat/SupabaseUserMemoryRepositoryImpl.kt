package com.example.vemorize.data.chat

import android.util.Log
import com.example.vemorize.domain.chat.model.UserMemory
import io.github.jan.supabase.postgrest.Postgrest
import kotlinx.datetime.Clock
import javax.inject.Inject

class SupabaseUserMemoryRepositoryImpl @Inject constructor(
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
                createdAt = now,
                updatedAt = now
            ))
            .decodeSingle<UserMemory>()
    }

    override suspend fun addFact(userId: String, fact: String): UserMemory {
        // TODO: Implement when user memory features are needed
        return getOrCreateMemory(userId)
    }

    override suspend fun addPreference(userId: String, preference: String): UserMemory {
        // TODO: Implement when user memory features are needed
        return getOrCreateMemory(userId)
    }

    override suspend fun addGoal(userId: String, goal: String): UserMemory {
        // TODO: Implement when user memory features are needed
        return getOrCreateMemory(userId)
    }

    override suspend fun addCourseStudied(userId: String, courseTitle: String): UserMemory {
        // TODO: Implement when user memory features are needed
        return getOrCreateMemory(userId)
    }

    override suspend fun refreshMemory(userId: String): UserMemory {
        return getOrCreateMemory(userId)
    }

    companion object {
        private const val TAG = "UserMemoryRepository"
    }
}
