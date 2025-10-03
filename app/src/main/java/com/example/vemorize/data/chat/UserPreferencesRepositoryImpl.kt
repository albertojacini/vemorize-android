package com.example.vemorize.data.chat

import android.util.Log
import com.example.vemorize.domain.model.chat.TtsModel
import com.example.vemorize.domain.model.chat.UserPreferences
import io.github.jan.supabase.postgrest.Postgrest
import kotlinx.datetime.Clock
import javax.inject.Inject

class UserPreferencesRepositoryImpl @Inject constructor(
    private val postgrest: Postgrest
) : UserPreferencesRepository {

    override suspend fun getOrCreatePreferences(userId: String): UserPreferences {
        return try {
            // Try to get existing preferences
            val existing = postgrest
                .from("user_preferences")
                .select {
                    filter {
                        eq("user_id", userId)
                    }
                }
                .decodeSingleOrNull<UserPreferences>()

            existing ?: createDefaultPreferences(userId)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting or creating preferences", e)
            throw e
        }
    }

    private suspend fun createDefaultPreferences(userId: String): UserPreferences {
        val now = Clock.System.now().toString()
        val preferences = mapOf(
            "user_id" to userId,
            "default_tts_model" to "local",
            "default_speech_speed" to 1.0f,
            "reading_speech_speed" to 1.0f,
            "created_at" to now,
            "updated_at" to now
        )

        return postgrest
            .from("user_preferences")
            .insert(preferences)
            .decodeSingle<UserPreferences>()
    }

    override suspend fun updateTtsModel(userId: String, ttsModel: TtsModel): UserPreferences {
        return updatePreference(userId, "default_tts_model", ttsModel.name.lowercase())
    }

    override suspend fun updateSpeechSpeed(userId: String, speed: Float): UserPreferences {
        return updatePreference(userId, "default_speech_speed", speed)
    }

    override suspend fun updateReadingSpeechSpeed(userId: String, speed: Float): UserPreferences {
        return updatePreference(userId, "reading_speech_speed", speed)
    }

    private suspend fun updatePreference(
        userId: String,
        field: String,
        value: Any
    ): UserPreferences {
        return try {
            postgrest
                .from("user_preferences")
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
                .decodeSingle<UserPreferences>()
        } catch (e: Exception) {
            Log.e(TAG, "Error updating preference $field", e)
            throw e
        }
    }

    override suspend fun refreshPreferences(userId: String): UserPreferences {
        return getOrCreatePreferences(userId)
    }

    companion object {
        private const val TAG = "UserPreferencesRepository"
    }
}
