package com.example.vemorize.domain.chat.actions

import android.util.Log
import com.example.vemorize.domain.model.chat.ActionResult
import com.example.vemorize.domain.model.chat.ChatMode
import com.example.vemorize.domain.model.chat.ToolCall
import kotlinx.serialization.json.*
import javax.inject.Inject

/**
 * Tool registry for executing LLM tool calls
 */
class ToolRegistry(
    private val actions: Actions
) {
    private val tools = mutableMapOf<String, ToolHandler>()

    init {
        // Register default tools
        registerTool(ProvideChatResponseToolHandler())
        registerTool(ExitModeToolHandler(actions))
        registerTool(SwitchModeToolHandler(actions))
    }

    fun registerTool(handler: ToolHandler) {
        tools[handler.name] = handler
    }

    suspend fun executeAll(toolCalls: List<ToolCall>): List<ActionResult> {
        return toolCalls.mapNotNull { toolCall ->
            tools[toolCall.tool]?.execute(toolCall)
        }
    }

    suspend fun extractChatResponse(toolCalls: List<ToolCall>): String {
        val provideChatResponse = tools["provide_chat_response"] as? ProvideChatResponseToolHandler
        val foundToolCall = toolCalls.find { it.tool == "provide_chat_response" }

        if (foundToolCall != null && provideChatResponse != null) {
            val result = provideChatResponse.execute(foundToolCall)
            if (result.success && result.data is String) {
                return result.data
            }
        }

        Log.w(TAG, "No provide_chat_response tool call found")
        return ""
    }

    companion object {
        private const val TAG = "ToolRegistry"
    }
}

/**
 * Base tool handler interface
 */
interface ToolHandler {
    val name: String
    suspend fun execute(toolCall: ToolCall): ActionResult
}

/**
 * Tool handler for providing chat responses
 */
class ProvideChatResponseToolHandler : ToolHandler {
    override val name = "provide_chat_response"

    override suspend fun execute(toolCall: ToolCall): ActionResult {
        return try {
            val args = toolCall.args
            val message = args["response"]?.jsonPrimitive?.content
                ?: args["message"]?.jsonPrimitive?.content
            if (message != null) {
                ActionResult(success = true, data = message)
            } else {
                ActionResult(success = false, error = "No message/response in tool call")
            }
        } catch (e: Exception) {
            ActionResult(success = false, error = "Failed to parse arguments: ${e.message}")
        }
    }
}

/**
 * Tool handler for exiting mode
 */
class ExitModeToolHandler(
    private val actions: Actions
) : ToolHandler {
    override val name = "exit_mode"

    override suspend fun execute(toolCall: ToolCall): ActionResult {
        actions.exitCurrentMode()
        return ActionResult(success = true)
    }
}

/**
 * Tool handler for switching mode
 */
class SwitchModeToolHandler(
    private val actions: Actions
) : ToolHandler {
    override val name = "switch_mode"

    override suspend fun execute(toolCall: ToolCall): ActionResult {
        return try {
            val args = toolCall.args
            val modeStr = args["targetMode"]?.jsonPrimitive?.content
            val mode = when (modeStr) {
                "idle" -> ChatMode.IDLE
                "reading" -> ChatMode.READING
                "quiz" -> ChatMode.QUIZ
                else -> return ActionResult(success = false, error = "Invalid mode: $modeStr")
            }

            // Actually switch the mode
            actions.switchMode(mode)
        } catch (e: Exception) {
            ActionResult(success = false, error = "Failed to switch mode: ${e.message}")
        }
    }
}
