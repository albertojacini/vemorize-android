package com.example.vemorize.domain.model.chat

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserPreferences(
    val id: String,
    @SerialName("user_id")
    val userId: String,
    @SerialName("default_tts_model")
    val defaultTtsModel: TtsModel = TtsModel.LOCAL,
    @SerialName("default_speech_speed")
    val defaultSpeechSpeed: Float = 1.0f,
    @SerialName("reading_speech_speed")
    val readingSpeechSpeed: Float = 1.0f,
    @SerialName("created_at")
    val createdAt: String,
    @SerialName("updated_at")
    val updatedAt: String
)

@Serializable
enum class TtsModel {
    @SerialName("local")
    LOCAL,
    @SerialName("cloud")
    CLOUD
}
