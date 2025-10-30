package com.example.vemorize.domain.chat.modes.commands

import com.example.vemorize.domain.chat.actions.Actions
import com.example.vemorize.domain.chat.commands.BaseSwitchModeCommand
import com.example.vemorize.domain.chat.commands.CommandMatch
import com.example.vemorize.domain.chat.commands.CommandResult

/**
 * Switch mode command for Idle mode
 * Port of TypeScript SwitchModeCommand from idle/commands.ts
 */
class IdleSwitchModeCommand : BaseSwitchModeCommand() {
    override suspend fun execute(commandMatch: CommandMatch, actions: Actions): CommandResult {
        // Can add idle-specific logging or behavior here if needed
        return super.execute(commandMatch, actions)
    }
}
