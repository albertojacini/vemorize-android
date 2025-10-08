package com.example.vemorize.domain.chat.commands

/**
 * Result of executing a voice command
 * Mirrors TypeScript CommandResult from command-types.ts
 */
data class CommandResult(
    // Required: immediate response to user
    val response: String,

    // Optional: voice language for speech synthesis
    val voiceLang: String? = null
)

/**
 * Result of pattern matching
 * Mirrors TypeScript CommandMatch from command-matcher.ts
 */
data class CommandMatch(
    val command: String,
    val pattern: String,
    val arguments: Map<String, Any>
)

/**
 * Argument types for command patterns
 */
enum class ArgumentType {
    STRING,
    NUMBER
}

/**
 * Parsed argument from command input
 */
data class ParsedArgument(
    val name: String,
    val value: Any,
    val type: ArgumentType
)
