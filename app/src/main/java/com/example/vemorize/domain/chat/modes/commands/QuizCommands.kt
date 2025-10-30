package com.example.vemorize.domain.chat.modes.commands

import com.example.vemorize.domain.chat.actions.Actions
import com.example.vemorize.domain.chat.commands.BaseSwitchModeCommand
import com.example.vemorize.domain.chat.commands.BaseVoiceCommand
import com.example.vemorize.domain.chat.commands.CommandMatch
import com.example.vemorize.domain.chat.commands.CommandResult

/**
 * Switch mode command for Quiz mode
 * Port of TypeScript SwitchModeCommand from quiz/commands.ts
 */
class QuizSwitchModeCommand : BaseSwitchModeCommand() {
    override suspend fun execute(commandMatch: CommandMatch, actions: Actions): CommandResult {
        // Can add quiz-specific logging or behavior here if needed
        return super.execute(commandMatch, actions)
    }
}

/**
 * Help command for quiz mode
 * Port of TypeScript QuizHelpCommand from quiz/commands.ts
 */
class QuizHelpCommand : BaseVoiceCommand() {
    override val name = "quiz_help"
    override val patterns = listOf("help", "what can i do", "commands")
    override val description = "Show available quiz commands"

    override suspend fun execute(commandMatch: CommandMatch, actions: Actions): CommandResult {
        return CommandResult(
            response = "You can answer questions, ask for hints, or switch to another mode."
        )
    }
}

/**
 * Start quiz command
 * Basic implementation - can be enhanced based on quiz requirements
 */
class StartQuizCommand : BaseVoiceCommand() {
    override val name = "start_quiz"
    override val patterns = listOf("start quiz", "begin quiz", "start")
    override val description = "Start a quiz session"

    override suspend fun execute(commandMatch: CommandMatch, actions: Actions): CommandResult {
        // TODO: Implement quiz start logic when quiz manager is available
        return CommandResult(response = "Starting quiz...")
    }
}

/**
 * Stop quiz command - return to idle mode
 */
class StopQuizCommand : BaseVoiceCommand() {
    override val name = "stop_quiz"
    override val patterns = listOf("stop", "quit", "exit quiz", "back to idle")
    override val description = "Stop the quiz and return to idle mode"

    override suspend fun execute(commandMatch: CommandMatch, actions: Actions): CommandResult {
        return CommandResult(response = "Quiz stopped. Returning to idle mode.")
    }
}
