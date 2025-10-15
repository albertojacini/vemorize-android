package com.example.vemorize.data.chat

import com.example.vemorize.domain.chat.model.TtsModel
import com.example.vemorize.domain.chat.model.UserPreferences

interface UserPreferencesRepository {
    /**
     * Get or create user preferences
     */
    suspend fun getOrCreatePreferences(userId: String): UserPreferences

    /**
     * Update TTS model
     */
    suspend fun updateTtsModel(userId: String, ttsModel: TtsModel): UserPreferences

    /**
     * Update speech speed
     */
    suspend fun updateSpeechSpeed(userId: String, speed: Float): UserPreferences

    /**
     * Update reading speech speed
     */
    suspend fun updateReadingSpeechSpeed(userId: String, speed: Float): UserPreferences

    /**
     * Refresh preferences from backend
     */
    suspend fun refreshPreferences(userId: String): UserPreferences
}
