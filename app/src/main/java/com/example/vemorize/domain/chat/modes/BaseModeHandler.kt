package com.example.vemorize.domain.chat.modes

import com.example.vemorize.data.chat.ConversationRepository
import com.example.vemorize.data.dto.vemorize_api.ApiLLMContext
import com.example.vemorize.domain.chat.actions.Actions
import com.example.vemorize.domain.chat.actions.ToolRegistry
import com.example.vemorize.domain.chat.commands.VoiceCommand
import com.example.vemorize.domain.chat.commands.VoiceCommandMatcher
import com.example.vemorize.domain.chat.managers.NavigationManager
import com.example.vemorize.domain.chat.model.*

/**
 * Base class for mode handlers
 * Port of TypeScript Handler from base.ts
 */
abstract class BaseModeHandler(
    protected val conversationRepository: ConversationRepository,
    protected val actions: Actions,
    protected val navigationManager: NavigationManager,
    protected val toolRegistry: ToolRegistry
) {
    abstract val mode: ChatMode
    protected val matcher: VoiceCommandMatcher = VoiceCommandMatcher()

    /**
     * Mode-specific commands - override in each mode handler
     */
    protected abstract val commands: List<VoiceCommand>

    /**
     * Called when entering this mode
     */
    abstract suspend fun onEnter(): String

    /**
     * Called when exiting this mode
     */
    abstract suspend fun onExit()

    /**
     * Main entry point for handling user input
     * Three-path routing: detect command → execute command OR handle conversationally
     * Port of TypeScript handleUserInput from base.ts:41-64
     */
    suspend fun handleUserInput(userInput: String): HandlerResponse {
        android.util.Log.d(TAG, "handleUserInput: '$userInput' in mode: $mode")
        android.util.Log.d(TAG, "Registered commands: ${matcher.getRegisteredCommands()}")

        // Path 1: Detect command
        val commandMatch = matcher.match(userInput)
        android.util.Log.d(TAG, "Command match result: $commandMatch")

        if (commandMatch != null) {
            android.util.Log.d(TAG, "Executing command: ${commandMatch.command}")
            // Path 2: Execute command
            val commandResponse = handleCommand(commandMatch)
            return HandlerResponse(
                generatedBy = mode,
                message = commandResponse.response,
                voiceLang = commandResponse.voiceLang
            )
        }

        android.util.Log.d(TAG, "No command matched, handling as conversational input")
        // Path 3: Handle as conversational input
        return handleConversationalInput(userInput)
    }

    companion object {
        private const val TAG = "BaseModeHandler"
    }

    /**
     * Handle command execution
     * Port of TypeScript handleCommand from base.ts:122-139
     */
    private suspend fun handleCommand(commandMatch: com.example.vemorize.domain.chat.commands.CommandMatch): com.example.vemorize.domain.chat.commands.CommandResult {
        // Select the correct command based on the command name
        val command = commands.find { it.name == commandMatch.command }
            ?: return com.example.vemorize.domain.chat.commands.CommandResult(
                response = "Command not found"
            )

        // Execute the command
        return command.execute(commandMatch, actions)
    }

    /**
     * Handle conversational input using LLM
     * Port of TypeScript handleConversationalInput from base.ts:70-117
     */
    protected suspend fun handleConversationalInput(userInput: String): HandlerResponse {
        val startTime = System.currentTimeMillis()
        android.util.Log.d(TAG, "═══════════════════════════════════════════════════════════")
        android.util.Log.d(TAG, "▶ [START] handleConversationalInput")
        android.util.Log.d(TAG, "  Mode: $mode")
        android.util.Log.d(TAG, "  User Input: '$userInput'")
        android.util.Log.d(TAG, "  Timestamp: $startTime")

        return try {
            // Build LLM context
            val beforeContextBuild = System.currentTimeMillis()
            val llmContext = buildLLMContext(userInput)
            val contextBuildTime = System.currentTimeMillis() - beforeContextBuild
            android.util.Log.d(TAG, "  ✓ LLM context built (${contextBuildTime}ms)")
            android.util.Log.d(TAG, "    Context mode: ${llmContext.mode}")
            android.util.Log.d(TAG, "    Tools: ${llmContext.toolNames}")

            val course = navigationManager.activeCourse
                ?: throw IllegalStateException("No active course")
            android.util.Log.d(TAG, "  ✓ Active course: ${course.title} (${course.id})")

            // Call API
            android.util.Log.d(TAG, "  ⏳ Calling LLM API...")
            val beforeApiCall = System.currentTimeMillis()
            val response = conversationRepository.sendLLMRequest(
                llmContext = llmContext,
                courseId = course.id,
                userId = course.userId
            )
            val apiCallTime = System.currentTimeMillis() - beforeApiCall
            android.util.Log.d(TAG, "  ✓ API call completed (${apiCallTime}ms)")

            val toolCalls = response.data?.toolCalls ?: emptyList()
            android.util.Log.d(TAG, "  Tool calls received: ${toolCalls.size}")
            if (toolCalls.isEmpty()) {
                android.util.Log.w(TAG, "  ⚠ WARNING: No tool calls in response!")
            }

            // Extract chat response from tool calls
            android.util.Log.d(TAG, "  ⏳ Extracting chat response...")
            val beforeExtract = System.currentTimeMillis()
            val assistantMessage = toolRegistry.extractChatResponse(toolCalls)
            val extractTime = System.currentTimeMillis() - beforeExtract
            android.util.Log.d(TAG, "  ✓ Message extracted (${extractTime}ms): '${assistantMessage}'")

            if (assistantMessage.isEmpty()) {
                android.util.Log.w(TAG, "  ⚠ WARNING: Empty message extracted, will use default")
            }

            // Execute tool calls
            android.util.Log.d(TAG, "  ⏳ Executing tool calls...")
            val beforeExecute = System.currentTimeMillis()
            toolRegistry.executeAll(toolCalls)
            val executeTime = System.currentTimeMillis() - beforeExecute
            android.util.Log.d(TAG, "  ✓ Tool calls executed (${executeTime}ms)")

            val totalTime = System.currentTimeMillis() - startTime
            val finalMessage = assistantMessage.ifEmpty { getDefaultResponseMessage() }

            android.util.Log.d(TAG, "◀ [SUCCESS] handleConversationalInput completed in ${totalTime}ms")
            android.util.Log.d(TAG, "  Breakdown: Context=${contextBuildTime}ms, API=${apiCallTime}ms, Extract=${extractTime}ms, Execute=${executeTime}ms")
            android.util.Log.d(TAG, "  Final message: '$finalMessage'")
            android.util.Log.d(TAG, "═══════════════════════════════════════════════════════════")

            HandlerResponse(
                generatedBy = mode,
                message = finalMessage
            )
        } catch (e: Exception) {
            val totalTime = System.currentTimeMillis() - startTime
            android.util.Log.e(TAG, "✗ [EXCEPTION] handleConversationalInput failed after ${totalTime}ms")
            android.util.Log.e(TAG, "  Exception: ${e.javaClass.simpleName}")
            android.util.Log.e(TAG, "  Message: ${e.message}")
            android.util.Log.e(TAG, "  Stack trace:", e)
            android.util.Log.e(TAG, "═══════════════════════════════════════════════════════════")
            handleConversationError(e)
        }
    }

    /**
     * Build LLM context - override for mode-specific context
     */
    protected abstract suspend fun buildLLMContext(userInput: String): ApiLLMContext

    /**
     * Get tool names for this mode
     */
    protected abstract fun getToolNames(): List<String>

    /**
     * Get default response when no chat response is found
     */
    protected abstract fun getDefaultResponseMessage(): String

    /**
     * Handle conversation errors with mode-specific fallbacks
     */
    protected abstract fun handleConversationError(error: Exception): HandlerResponse

    /**
     * Get preferred TTS model for this mode
     * Override to specify mode-specific TTS preferences
     */
    open fun getPreferredTtsModel(): TtsModel = TtsModel.LOCAL
}
