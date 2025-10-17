package com.example.vemorize.domain.chat

import android.util.Log
import com.example.vemorize.data.chat.ConversationRepository
import com.example.vemorize.domain.chat.actions.Actions
import com.example.vemorize.domain.chat.actions.ToolRegistry
import com.example.vemorize.domain.chat.managers.NavigationManager
import com.example.vemorize.domain.chat.managers.UserMemoryManager
import com.example.vemorize.domain.chat.managers.UserPreferencesManager
import com.example.vemorize.domain.chat.modes.BaseModeHandler
import com.example.vemorize.domain.chat.modes.IdleModeHandler
import com.example.vemorize.domain.chat.modes.QuizModeHandler
import com.example.vemorize.domain.chat.modes.ReadingModeHandler
import com.example.vemorize.domain.chat.model.*
import com.example.vemorize.domain.courses.Course
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

/**
 * Main chat orchestrator - coordinates all managers and mode handlers
 */
class ChatManager(
    private val navigationManager: NavigationManager,
    private val userMemoryManager: UserMemoryManager,
    private val userPreferencesManager: UserPreferencesManager,
    private val conversationRepository: ConversationRepository,
    private val actions: Actions,
    private val toolRegistry: ToolRegistry,
    private val userId: String
) {
    private val _isInitialized = MutableStateFlow(false)
    val isInitialized: StateFlow<Boolean> = _isInitialized.asStateFlow()

    private val _currentMode = MutableStateFlow(ChatMode.IDLE)
    val currentMode: StateFlow<ChatMode> = _currentMode.asStateFlow()

    private val _currentCourse = MutableStateFlow<Course?>(null)
    val currentCourse: StateFlow<Course?> = _currentCourse.asStateFlow()

    // Mode handlers
    private lateinit var handlers: Map<ChatMode, BaseModeHandler>

    var incomingVoiceExpectedLang: String = "en-US"

    /**
     * Initialize the chat manager
     */
    suspend fun initialize() {
        try {
            Log.d(TAG, "ChatManager.initialize() started")

            // Initialize managers
            Log.d(TAG, "Initializing UserPreferencesManager...")
            userPreferencesManager.initialize()
            Log.d(TAG, "UserPreferencesManager initialized")

            Log.d(TAG, "Initializing UserMemoryManager...")
            userMemoryManager.initialize()
            Log.d(TAG, "UserMemoryManager initialized")

            // Initialize mode handlers
            Log.d(TAG, "Creating mode handlers...")
            handlers = mapOf(
                ChatMode.IDLE to IdleModeHandler(conversationRepository, actions, navigationManager, toolRegistry),
                ChatMode.READING to ReadingModeHandler(conversationRepository, actions, navigationManager, toolRegistry),
                ChatMode.QUIZ to QuizModeHandler(conversationRepository, actions, navigationManager, toolRegistry)
            )
            Log.d(TAG, "Mode handlers created")

            _isInitialized.value = true
            Log.d(TAG, "ChatManager initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize ChatManager", e)
            throw IllegalStateException("Failed to initialize ChatManager", e)
        }
    }

    companion object {
        private const val TAG = "ChatManager"
    }

    /**
     * Load a course
     */
    suspend fun loadCourse(course: Course) {
        // Load course in navigation manager
        navigationManager.loadCourse(course)

        // Update user memory
        userMemoryManager.addCourseStudied(course.title)

        _currentCourse.value = course
    }

    /**
     * Get current TTS model
     */
    fun getTtsModel(): TtsModel = userPreferencesManager.getTtsModel()

    /**
     * Get current speech speed
     */
    fun getSpeechSpeed(): Float = userPreferencesManager.getSpeechSpeed()

    /**
     * Get speech speed based on mode
     */
    suspend fun getModeSpecificSpeechSpeed(): Float {
        return if (navigationManager.mode == ChatMode.READING) {
            userPreferencesManager.getReadingSpeechSpeed()
        } else {
            userPreferencesManager.getSpeechSpeed()
        }
    }

    /**
     * Get TTS model based on current mode
     * Uses mode handler's preferred TTS model
     */
    fun getModeSpecificTtsModel(): TtsModel {
        val handler = handlers[navigationManager.mode]
            ?: return TtsModel.LOCAL
        return handler.getPreferredTtsModel()
    }

    /**
     * Main entry point for handling user input
     */
    suspend fun handleInput(userInput: String): ChatResponse {
        val course = _currentCourse.value
            ?: throw IllegalStateException("No active course loaded")

        // Get current mode handler
        val handler = handlers[navigationManager.mode]
            ?: throw IllegalStateException("No handler for mode ${navigationManager.mode}")

        // Handle input
        val handlerResponse = handler.handleUserInput(userInput)

        // Build final response with speech speed and TTS model
        return ChatResponse(
            message = handlerResponse.message,
            voiceLang = handlerResponse.voiceLang,
            speechSpeed = getModeSpecificSpeechSpeed(),
            ttsModel = getModeSpecificTtsModel(),
            handlerResponse = handlerResponse as? HandlerResponse
        )
    }

    /**
     * Get current preferences
     */
    fun getPreferences(): UserPreferences? = userPreferencesManager.getCurrent()

    /**
     * Get actions instance for command parsing
     */
    fun getActions(): Actions = actions

    /**
     * Get current mode from NavigationManager
     * This is needed to sync UI state with the actual mode after voice commands
     */
    fun getCurrentModeFromNavigation(): ChatMode = navigationManager.mode

    /**
     * Update TTS model
     */
    suspend fun updateTtsModel(ttsModel: TtsModel) {
        userPreferencesManager.updateTtsModel(ttsModel)
    }

    /**
     * Update speech speed
     */
    suspend fun updateSpeechSpeed(speed: Float) {
        userPreferencesManager.updateSpeechSpeed(speed)
    }

    /**
     * Update reading speech speed
     */
    suspend fun updateReadingSpeechSpeed(speed: Float) {
        userPreferencesManager.updateReadingSpeechSpeed(speed)
    }

    /**
     * Switch mode
     */
    suspend fun switchMode(targetMode: ChatMode): ActionResult {
        val previousMode = navigationManager.mode
        val result = actions.switchMode(targetMode)

        if (result.success) {
            _currentMode.value = targetMode
        }

        return result
    }

    /**
     * Navigate to next content
     */
    suspend fun nextContent(params: Map<String, Any> = emptyMap()): ActionResult {
        return actions.nextContent(params)
    }

    /**
     * Navigate to previous content
     */
    suspend fun previousContent(params: Map<String, Any> = emptyMap()): ActionResult {
        return actions.previousContent(params)
    }
}
