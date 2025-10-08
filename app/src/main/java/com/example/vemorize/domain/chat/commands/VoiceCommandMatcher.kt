package com.example.vemorize.domain.chat.commands

/**
 * Internal pattern structure after parsing
 */
private data class ParsedPattern(
    val commandName: String,
    val originalPattern: String,
    val segments: List<Any>, // String or ArgumentDefinition
    val regex: Regex
)

/**
 * Argument definition for pattern parsing
 */
private data class ArgumentDefinition(
    val name: String,
    val type: ArgumentType
)

/**
 * Command registration interface
 */
private interface CommandDefinition {
    val name: String
    val patterns: List<String>
}

/**
 * Voice command matcher - pattern-based command detection with argument extraction
 * Port of TypeScript VoiceCommandMatcher from command-matcher.ts
 */
class VoiceCommandMatcher {
    private val parsedPatterns = mutableListOf<ParsedPattern>()

    /**
     * Register a command with its patterns
     */
    fun registerCommand(command: VoiceCommand) {
        for (pattern in command.patterns) {
            val parsedPattern = parsePattern(command.name, pattern)
            parsedPatterns.add(parsedPattern)
        }
    }

    /**
     * Register multiple commands at once
     */
    fun registerCommands(commands: List<VoiceCommand>) {
        commands.forEach { registerCommand(it) }
    }

    /**
     * Match input against registered commands
     * Returns CommandMatch if a pattern matches, null otherwise
     */
    fun match(input: String): CommandMatch? {
        // Convert word numbers first, then clean
        val convertedInput = convertWordNumbers(input)
        val cleanInput = cleanInput(convertedInput)

        android.util.Log.d(TAG, "match() - Original input: '$input'")
        android.util.Log.d(TAG, "match() - Cleaned input: '$cleanInput'")

        // Find first matching pattern (first registered wins)
        for (pattern in parsedPatterns) {
            android.util.Log.d(TAG, "match() - Testing pattern: ${pattern.originalPattern}, regex: ${pattern.regex.pattern}")
            val match = pattern.regex.find(cleanInput)
            android.util.Log.d(TAG, "match() - Regex match result: $match")
            if (match != null) {
                android.util.Log.d(TAG, "match() - Match groups: ${match.groupValues}")
                val args = extractArguments(pattern, match)
                android.util.Log.d(TAG, "match() - Extracted args: $args")
                if (args != null) { // Only return if argument validation passed
                    return CommandMatch(
                        command = pattern.commandName,
                        pattern = pattern.originalPattern,
                        arguments = args
                    )
                }
            }
        }

        android.util.Log.d(TAG, "match() - No pattern matched")
        return null
    }

    companion object {
        private const val TAG = "VoiceCommandMatcher"
    }

    /**
     * Get all registered commands and their patterns
     */
    fun getRegisteredCommands(): List<Pair<String, List<String>>> {
        val commandMap = mutableMapOf<String, MutableList<String>>()

        for (pattern in parsedPatterns) {
            if (!commandMap.containsKey(pattern.commandName)) {
                commandMap[pattern.commandName] = mutableListOf()
            }
            commandMap[pattern.commandName]!!.add(pattern.originalPattern)
        }

        return commandMap.map { (command, patterns) ->
            command to patterns
        }
    }

    /**
     * Clear all registered commands
     */
    fun clear() {
        parsedPatterns.clear()
    }

    /**
     * Parse a pattern string into segments and create regex
     */
    private fun parsePattern(commandName: String, pattern: String): ParsedPattern {
        val segments = mutableListOf<Any>()
        val regexParts = mutableListOf<String>()

        // Find all argument placeholders
        val argRegex = Regex("\\{[^}]+\\}")
        val argMatches = argRegex.findAll(pattern).map { it.value to it.range }.toList()

        // Split pattern by extracting text between arguments
        var lastIndex = 0
        val parts = mutableListOf<String>()

        for ((argText, range) in argMatches) {
            // Add text before this argument
            if (range.first > lastIndex) {
                parts.add(pattern.substring(lastIndex, range.first))
            }
            // Add the argument itself
            parts.add(argText)
            lastIndex = range.last + 1
        }
        // Add remaining text after last argument
        if (lastIndex < pattern.length) {
            parts.add(pattern.substring(lastIndex))
        }

        android.util.Log.d(TAG, "parsePattern: pattern='$pattern', parts=$parts")

        for (part in parts) {
            android.util.Log.d(TAG, "parsePattern: processing part='$part'")
            when {
                part.startsWith("{") && part.endsWith("}") -> {
                    // Parse argument: {type:name}
                    val argContent = part.substring(1, part.length - 1)
                    val (typeStr, name) = argContent.split(":")

                    if (name.isBlank() || typeStr.isBlank()) {
                        throw IllegalArgumentException("Invalid argument syntax in pattern \"$pattern\": $part")
                    }

                    val type = parseArgumentType(typeStr)
                    val argDef = ArgumentDefinition(name = name.trim(), type = type)
                    segments.add(argDef)

                    // Create regex capture group based on type
                    when (type) {
                        ArgumentType.NUMBER -> regexParts.add("(-?\\d+(?:\\.\\d+)?)") // Matches integers and decimals
                        ArgumentType.STRING -> regexParts.add("(\\S+)") // Matches non-whitespace strings
                    }
                }
                part.trim().isNotEmpty() -> {
                    // Regular text segment
                    segments.add(part.trim())
                    regexParts.add(escapeRegex(part.trim()))
                }
            }
        }

        // Create final regex (case insensitive, exact match)
        val regexPattern = "^" + regexParts.joinToString("\\s+") + "$"
        val regex = Regex(regexPattern, RegexOption.IGNORE_CASE)

        return ParsedPattern(
            commandName = commandName,
            originalPattern = pattern,
            segments = segments,
            regex = regex
        )
    }

    /**
     * Extract and validate arguments from regex match
     */
    private fun extractArguments(pattern: ParsedPattern, match: MatchResult): Map<String, Any>? {
        val args = mutableMapOf<String, Any>()
        var captureIndex = 1 // Start from 1 (0 is full match)

        for (segment in pattern.segments) {
            if (segment is ArgumentDefinition) {
                val rawValue = match.groupValues.getOrNull(captureIndex) ?: return null
                val validatedValue = validateAndConvert(rawValue, segment.type) ?: return null

                args[segment.name] = validatedValue
                captureIndex++
            }
        }

        return args
    }

    /**
     * Validate and convert argument value based on type
     */
    private fun validateAndConvert(value: String, type: ArgumentType): Any? {
        return when (type) {
            ArgumentType.STRING -> value // Strings are always valid
            ArgumentType.NUMBER -> {
                value.toDoubleOrNull() ?: value.toIntOrNull() ?: return null
            }
        }
    }

    /**
     * Parse argument type string
     */
    private fun parseArgumentType(typeStr: String): ArgumentType {
        return when (typeStr.lowercase().trim()) {
            "string", "str" -> ArgumentType.STRING
            "number", "num" -> ArgumentType.NUMBER
            else -> throw IllegalArgumentException("Unsupported argument type: $typeStr")
        }
    }

    /**
     * Clean input string for matching
     */
    private fun cleanInput(input: String): String {
        return input
            .lowercase()
            .replace(Regex("[^\\w\\s.-]"), "") // Keep only alphanumeric, spaces, dots, and hyphens
            .replace(Regex("\\s+"), " ") // Normalize whitespace
            .trim()
    }

    /**
     * Escape special regex characters
     */
    private fun escapeRegex(str: String): String {
        val specialChars = setOf('.', '*', '+', '?', '^', '$', '{', '}', '(', ')', '|', '[', ']', '\\')
        return str.map { char ->
            if (char in specialChars) "\\$char" else char.toString()
        }.joinToString("")
    }

    /**
     * Convert word numbers to digits for better matching
     */
    private fun convertWordNumbers(text: String): String {
        val wordNumbers = mapOf(
            "zero" to "0", "one" to "1", "two" to "2", "three" to "3", "four" to "4", "five" to "5",
            "six" to "6", "seven" to "7", "eight" to "8", "nine" to "9", "ten" to "10",
            "eleven" to "11", "twelve" to "12", "thirteen" to "13", "fourteen" to "14", "fifteen" to "15",
            "sixteen" to "16", "seventeen" to "17", "eighteen" to "18", "nineteen" to "19", "twenty" to "20"
        )

        var result = text.lowercase()
        for ((word, digit) in wordNumbers) {
            result = result.replace(Regex("\\b$word\\b"), digit)
        }
        return result
    }
}
