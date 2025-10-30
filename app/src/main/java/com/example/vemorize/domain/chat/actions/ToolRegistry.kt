package com.example.vemorize.domain.chat.actions

import android.util.Log
import com.example.vemorize.data.dto.vemorize_api.ToolCall
import com.example.vemorize.domain.chat.model.ActionResult
import com.example.vemorize.domain.chat.model.ChatMode
import kotlinx.serialization.json.*

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
        Log.d(TAG, "═══════════════════════════════════════════════════════════")
        Log.d(TAG, "▶ [START] Executing ${toolCalls.size} tool call(s)")

        val results = toolCalls.mapIndexedNotNull { index, toolCall ->
            Log.d(TAG, "  [$index] Tool: ${toolCall.tool}")
            Log.d(TAG, "      Args: ${toolCall.args}")

            val handler = tools[toolCall.tool]
            if (handler == null) {
                Log.e(TAG, "      ✗ No handler registered for tool: ${toolCall.tool}")
                null
            } else {
                try {
                    val result = handler.execute(toolCall)
                    if (result.success) {
                        Log.d(TAG, "      ✓ Success: ${result.data}")
                    } else {
                        Log.e(TAG, "      ✗ Failed: ${result.error}")
                    }
                    result
                } catch (e: Exception) {
                    Log.e(TAG, "      ✗ Exception executing tool: ${e.message}", e)
                    null
                }
            }
        }

        Log.d(TAG, "◀ [COMPLETE] Executed ${results.size}/${toolCalls.size} tool call(s) successfully")
        Log.d(TAG, "═══════════════════════════════════════════════════════════")
        return results
    }

    suspend fun extractChatResponse(toolCalls: List<ToolCall>): String {
        Log.d(TAG, "═══════════════════════════════════════════════════════════")
        Log.d(TAG, "▶ [START] Extracting chat response from ${toolCalls.size} tool call(s)")

        toolCalls.forEachIndexed { index, toolCall ->
            Log.d(TAG, "  [$index] ${toolCall.tool}")
        }

        val provideChatResponse = tools["provide_chat_response"] as? ProvideChatResponseToolHandler
        val foundToolCall = toolCalls.find { it.tool == "provide_chat_response" }

        if (foundToolCall != null) {
            Log.d(TAG, "  ✓ Found provide_chat_response tool call")
            Log.d(TAG, "    Args: ${foundToolCall.args}")

            if (provideChatResponse != null) {
                val result = provideChatResponse.execute(foundToolCall)
                if (result.success && result.data is String) {
                    Log.d(TAG, "  ✓ Successfully extracted message: '${result.data}'")
                    Log.d(TAG, "◀ [SUCCESS] Chat response extracted")
                    Log.d(TAG, "═══════════════════════════════════════════════════════════")
                    return result.data
                } else {
                    Log.e(TAG, "  ✗ Tool execution failed or returned non-string data")
                    Log.e(TAG, "    Success: ${result.success}, Data type: ${result.data?.javaClass?.simpleName}")
                }
            } else {
                Log.e(TAG, "  ✗ Handler is not ProvideChatResponseToolHandler")
            }
        } else {
            Log.w(TAG, "  ⚠ No provide_chat_response tool call found in list")
        }

        Log.w(TAG, "◀ [WARNING] No chat response extracted, returning empty string")
        Log.d(TAG, "═══════════════════════════════════════════════════════════")
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
