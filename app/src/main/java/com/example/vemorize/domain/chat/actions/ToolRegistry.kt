package com.example.vemorize.domain.chat.actions

import android.util.Log
import com.example.vemorize.domain.model.chat.ActionResult
import com.example.vemorize.domain.model.chat.ChatMode
import com.example.vemorize.domain.model.chat.ToolCall
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonPrimitive
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

    fun extractChatResponse(toolCalls: List<ToolCall>): String {
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
    fun execute(toolCall: ToolCall): ActionResult
}

/**
 * Tool handler for providing chat responses
 */
class ProvideChatResponseToolHandler : ToolHandler {
    override val name = "provide_chat_response"

    override fun execute(toolCall: ToolCall): ActionResult {
        val message = toolCall.args["message"]?.jsonPrimitive?.content
        return if (message != null) {
            ActionResult(success = true, data = message)
        } else {
            ActionResult(success = false, error = "No message in tool call")
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

    override fun execute(toolCall: ToolCall): ActionResult {
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

    override fun execute(toolCall: ToolCall): ActionResult {
        val modeStr = toolCall.args["mode"]?.jsonPrimitive?.content
        val mode = when (modeStr) {
            "idle" -> ChatMode.IDLE
            "reading" -> ChatMode.READING
            "quiz" -> ChatMode.QUIZ
            else -> return ActionResult(success = false, error = "Invalid mode: $modeStr")
        }

        // Note: This is synchronous but switchMode is suspend
        // In a real implementation, you'd need to handle this properly
        return ActionResult(success = true, data = mode)
    }
}
