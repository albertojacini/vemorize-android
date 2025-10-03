package com.example.vemorize.domain.chat

import com.example.vemorize.data.chat.ChatApiClient
import com.example.vemorize.domain.chat.actions.Actions
import com.example.vemorize.domain.chat.actions.ToolRegistry
import com.example.vemorize.domain.chat.managers.ConversationManager
import com.example.vemorize.domain.chat.managers.NavigationManager
import com.example.vemorize.domain.chat.managers.UserMemoryManager
import com.example.vemorize.domain.chat.managers.UserPreferencesManager
import com.example.vemorize.domain.chat.modes.BaseModeHandler
import com.example.vemorize.domain.chat.modes.IdleModeHandler
import com.example.vemorize.domain.chat.modes.QuizModeHandler
import com.example.vemorize.domain.chat.modes.ReadingModeHandler
import com.example.vemorize.domain.model.chat.*
import com.example.vemorize.domain.model.courses.Course
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
    private val conversationManager: ConversationManager,
    private val chatApiClient: ChatApiClient,
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
            // Initialize managers
            userPreferencesManager.initialize()
            userMemoryManager.initialize()

            // Initialize mode handlers
            handlers = mapOf(
                ChatMode.IDLE to IdleModeHandler(chatApiClient, actions, navigationManager, toolRegistry),
                ChatMode.READING to ReadingModeHandler(chatApiClient, actions, navigationManager, toolRegistry),
                ChatMode.QUIZ to QuizModeHandler(chatApiClient, actions, navigationManager, toolRegistry)
            )

            _isInitialized.value = true
        } catch (e: Exception) {
            throw IllegalStateException("Failed to initialize ChatManager", e)
        }
    }

    /**
     * Load a course
     */
    suspend fun loadCourse(course: Course) {
        // Load course in navigation manager
        navigationManager.loadCourse(course)

        // Load or create conversation for this course
        conversationManager.getOrCreate(course.id)

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
     * Get conversation context for LLM
     */
    private fun getConversationContext(): String {
        val course = _currentCourse.value ?: return ""
        return conversationManager.getConversationContext(course.id)
    }

    /**
     * Get session ID for LLM
     */
    private fun getSessionId(): String? {
        val course = _currentCourse.value ?: return null
        return conversationManager.getSessionId(course.id)
    }

    /**
     * Main entry point for handling user input
     */
    suspend fun handleInput(userInput: String): ChatResponse {
        val course = _currentCourse.value
            ?: throw IllegalStateException("No active course loaded")

        // Add user message to conversation
        conversationManager.addMessage(course.id, MessageType.HUMAN, userInput)

        // Get current mode handler
        val handler = handlers[navigationManager.mode]
            ?: throw IllegalStateException("No handler for mode ${navigationManager.mode}")

        // Handle input
        val handlerResponse = handler.handleUserInput(userInput)

        // Add assistant response to conversation
        if (handlerResponse.message.isNotEmpty()) {
            conversationManager.addMessage(course.id, MessageType.AI, handlerResponse.message)

            // Check if compression is needed
            if (conversationManager.needsCompression(course.id)) {
                // TODO: Implement message compression
            }

            // Save conversation periodically
            conversationManager.saveIfNeeded(course.id)
        }

        // Build final response with speech speed
        return ChatResponse(
            message = handlerResponse.message,
            voiceLang = handlerResponse.voiceLang,
            speechSpeed = getModeSpecificSpeechSpeed(),
            handlerResponse = handlerResponse as? HandlerResponse
        )
    }

    /**
     * Create a new conversation (clear history)
     */
    suspend fun startNewConversation() {
        val course = _currentCourse.value
            ?: throw IllegalStateException("No active course loaded")
        conversationManager.createNew(course.id)
    }

    /**
     * Get conversation history
     */
    suspend fun getConversationHistory(limit: Int = 10): List<Conversation> {
        val course = _currentCourse.value ?: return emptyList()
        return conversationManager.getHistory(course.id, limit)
    }

    /**
     * Get current preferences
     */
    fun getPreferences(): UserPreferences? = userPreferencesManager.getCurrent()

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
