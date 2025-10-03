package com.example.vemorize.domain.model.chat

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

/**
 * User facts stored in memory
 */
@Serializable
data class UserFacts(
    @SerialName("courses_studied")
    val coursesStudied: List<String> = emptyList(),
    @SerialName("current_goals")
    val currentGoals: List<String> = emptyList(),
    @SerialName("learning_preferences")
    val learningPreferences: Map<String, JsonElement> = emptyMap(),
    @SerialName("knowledge_areas")
    val knowledgeAreas: List<String> = emptyList()
)

/**
 * User achievements
 */
@Serializable
data class UserAchievements(
    @SerialName("total_quizzes_completed")
    val totalQuizzesCompleted: Int = 0,
    @SerialName("total_courses_completed")
    val totalCoursesCompleted: Int = 0,
    @SerialName("learning_streak_days")
    val learningStreakDays: Int = 0
)

/**
 * Cross-course user knowledge (facts, preferences, goals)
 */
@Serializable
data class UserMemory(
    val id: String,
    @SerialName("user_id")
    val userId: String,
    val facts: UserFacts = UserFacts(),
    val preferences: Map<String, JsonElement> = emptyMap(),
    val goals: List<String> = emptyList(),
    val achievements: UserAchievements = UserAchievements(),
    @SerialName("created_at")
    val createdAt: String,
    @SerialName("updated_at")
    val updatedAt: String
)
