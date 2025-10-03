package com.example.vemorize.domain.chat.managers

import android.util.Log
import com.example.vemorize.data.chat.UserPreferencesRepository
import com.example.vemorize.domain.model.chat.TtsModel
import com.example.vemorize.domain.model.chat.UserPreferences
import javax.inject.Inject

/**
 * Manages user preferences (TTS settings)
 */
class UserPreferencesManager(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val userId: String
) {
    private var currentPreferences: UserPreferences? = null

    /**
     * Initialize and load preferences
     */
    suspend fun initialize() {
        try {
            Log.d(TAG, "UserPreferencesManager.initialize() - userId: $userId")
            currentPreferences = userPreferencesRepository.getOrCreatePreferences(userId)
            Log.d(TAG, "UserPreferencesManager initialized: $currentPreferences")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize UserPreferencesManager", e)
            throw e
        }
    }

    companion object {
        private const val TAG = "UserPreferencesManager"
    }

    /**
     * Get current preferences
     */
    fun getCurrent(): UserPreferences? = currentPreferences

    /**
     * Get TTS model
     */
    fun getTtsModel(): TtsModel = currentPreferences?.defaultTtsModel ?: TtsModel.LOCAL

    /**
     * Get speech speed
     */
    fun getSpeechSpeed(): Float = currentPreferences?.defaultSpeechSpeed ?: 1.0f

    /**
     * Get reading speech speed
     */
    suspend fun getReadingSpeechSpeed(): Float =
        currentPreferences?.readingSpeechSpeed ?: 1.0f

    /**
     * Update TTS model
     */
    suspend fun updateTtsModel(ttsModel: TtsModel) {
        currentPreferences = userPreferencesRepository.updateTtsModel(userId, ttsModel)
    }

    /**
     * Update speech speed
     */
    suspend fun updateSpeechSpeed(speed: Float) {
        currentPreferences = userPreferencesRepository.updateSpeechSpeed(userId, speed)
    }

    /**
     * Update reading speech speed
     */
    suspend fun updateReadingSpeechSpeed(speed: Float) {
        currentPreferences = userPreferencesRepository.updateReadingSpeechSpeed(userId, speed)
    }

    /**
     * Refresh from backend
     */
    suspend fun refresh() {
        currentPreferences = userPreferencesRepository.refreshPreferences(userId)
    }
}
