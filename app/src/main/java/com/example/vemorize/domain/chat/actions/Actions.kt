package com.example.vemorize.domain.chat.actions

import com.example.vemorize.domain.chat.managers.NavigationManager
import com.example.vemorize.domain.model.chat.ActionResult
import com.example.vemorize.domain.model.chat.ChatMode
import javax.inject.Inject

/**
 * Action system for executing commands (mode switching, navigation, etc.)
 */
class Actions(
    private val navigationManager: NavigationManager
) {
    /**
     * Exit current mode (return to IDLE)
     */
    fun exitCurrentMode() {
        navigationManager.switchToMode(ChatMode.IDLE)
    }

    /**
     * Read current content
     */
    fun readCurrentContent(): ActionResult {
        val text = navigationManager.getReadingText()
        return if (text != null) {
            ActionResult(success = true, data = text)
        } else {
            ActionResult(success = false, error = "No content available to read")
        }
    }

    /**
     * Switch to a mode
     */
    suspend fun switchMode(targetMode: ChatMode): ActionResult {
        return try {
            navigationManager.switchToMode(targetMode)
            ActionResult(success = true)
        } catch (e: Exception) {
            ActionResult(success = false, error = e.message ?: "Failed to switch mode")
        }
    }

    /**
     * Navigate to next content
     */
    suspend fun nextContent(params: Map<String, Any> = emptyMap()): ActionResult {
        return try {
            navigationManager.moveNavigation(1)
            val text = navigationManager.getReadingText()
            if (text != null) {
                ActionResult(success = true, data = text)
            } else {
                ActionResult(success = false, error = "No content available to read")
            }
        } catch (e: Exception) {
            ActionResult(success = false, error = e.message ?: "Failed to navigate to next content")
        }
    }

    /**
     * Navigate to previous content
     */
    suspend fun previousContent(params: Map<String, Any> = emptyMap()): ActionResult {
        return try {
            navigationManager.moveNavigation(-1)
            val text = navigationManager.getReadingText()
            if (text != null) {
                ActionResult(success = true, data = text)
            } else {
                ActionResult(success = false, error = "No content available to read")
            }
        } catch (e: Exception) {
            ActionResult(success = false, error = e.message ?: "Failed to navigate to previous content")
        }
    }
}
