package com.example.vemorize.ui.chat

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.vemorize.domain.model.chat.Message
import com.example.vemorize.domain.model.chat.MessageType
import com.example.vemorize.ui.theme.VemorizeTheme

@Composable
fun ChatScreen(
    courseId: String? = null,
    viewModel: ChatViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ChatScreenContent(
        uiState = uiState,
        onEvent = viewModel::onEvent
    )
}

@Composable
fun ChatScreenContent(
    uiState: ChatUiState,
    onEvent: (ChatUiEvent) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        when (uiState) {
            is ChatUiState.Loading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            is ChatUiState.Error -> {
                Text(
                    text = uiState.message,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp)
                )
            }

            is ChatUiState.Ready -> {
                Box(modifier = Modifier.fillMaxSize()) {
                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Course header
                        if (uiState.course != null) {
                            Surface(
                                tonalElevation = 2.dp,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    Text(
                                        text = uiState.course.title,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Text(
                                        text = "Mode: ${uiState.currentMode.name}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        // Voice status banner
                        if (uiState.isListening || uiState.isSpeaking || uiState.partialTranscript != null) {
                            VoiceStatusBanner(
                                isListening = uiState.isListening,
                                isSpeaking = uiState.isSpeaking,
                                partialTranscript = uiState.partialTranscript
                            )
                        }

                        // Voice error banner
                        if (uiState.voiceError != null) {
                            Surface(
                                color = MaterialTheme.colorScheme.errorContainer,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = uiState.voiceError,
                                        color = MaterialTheme.colorScheme.onErrorContainer,
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.weight(1f)
                                    )
                                    IconButton(onClick = { onEvent(ChatUiEvent.ClearVoiceError) }) {
                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = "Dismiss error",
                                            tint = MaterialTheme.colorScheme.onErrorContainer
                                        )
                                    }
                                }
                            }
                        }

                        // Messages list
                        LazyColumn(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(uiState.messages) { message ->
                                MessageBubble(message)
                            }
                        }

                        // Input area
                        Surface(
                            tonalElevation = 3.dp,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = uiState.userInput,
                                    onValueChange = { onEvent(ChatUiEvent.UpdateInput(it)) },
                                    modifier = Modifier.weight(1f),
                                    placeholder = { Text("Type a message...") },
                                    enabled = !uiState.isProcessing && !uiState.isListening
                                )
                                IconButton(
                                    onClick = {
                                        if (uiState.userInput.isNotBlank()) {
                                            onEvent(ChatUiEvent.SendMessage(uiState.userInput))
                                        }
                                    },
                                    enabled = !uiState.isProcessing && uiState.userInput.isNotBlank() && !uiState.isListening
                                ) {
                                    Icon(Icons.Default.Send, contentDescription = "Send")
                                }
                            }
                        }
                    }

                    // Floating microphone button
                    VoiceFab(
                        isListening = uiState.isListening,
                        isSpeaking = uiState.isSpeaking,
                        isProcessing = uiState.isProcessing,
                        onClick = { onEvent(ChatUiEvent.ToggleVoiceListening) },
                        onLongClick = { onEvent(ChatUiEvent.StopVoiceOutput) },
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun MessageBubble(message: Message) {
    val isUser = message.type == MessageType.HUMAN

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            color = if (isUser) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            },
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isUser) 16.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 16.dp
            ),
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Text(
                text = message.content,
                modifier = Modifier.padding(12.dp),
                color = if (isUser) {
                    MaterialTheme.colorScheme.onPrimary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }
    }
}

@Composable
fun VoiceStatusBanner(
    isListening: Boolean,
    isSpeaking: Boolean,
    partialTranscript: String?
) {
    val backgroundColor = when {
        isSpeaking -> MaterialTheme.colorScheme.tertiaryContainer
        isListening -> MaterialTheme.colorScheme.primaryContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    val textColor = when {
        isSpeaking -> MaterialTheme.colorScheme.onTertiaryContainer
        isListening -> MaterialTheme.colorScheme.onPrimaryContainer
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    val statusText = when {
        isSpeaking -> "Speaking..."
        isListening && partialTranscript.isNullOrBlank() -> "Listening..."
        isListening -> partialTranscript ?: "Listening..."
        else -> ""
    }

    Surface(
        color = backgroundColor,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Pulsing indicator
            if (isListening || isSpeaking) {
                val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                val scale by infiniteTransition.animateFloat(
                    initialValue = 0.8f,
                    targetValue = 1.2f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(800, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "scale"
                )

                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .scale(scale),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        shape = CircleShape,
                        color = if (isSpeaking) {
                            MaterialTheme.colorScheme.tertiary
                        } else {
                            MaterialTheme.colorScheme.primary
                        },
                        modifier = Modifier.fillMaxSize()
                    ) {}
                }
            }

            Text(
                text = statusText,
                color = textColor,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun VoiceFab(
    isListening: Boolean,
    isSpeaking: Boolean,
    isProcessing: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = when {
        isListening -> MaterialTheme.colorScheme.primary
        isSpeaking -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.primaryContainer
    }

    val contentColor = when {
        isListening -> MaterialTheme.colorScheme.onPrimary
        isSpeaking -> MaterialTheme.colorScheme.onTertiary
        else -> MaterialTheme.colorScheme.onPrimaryContainer
    }

    val icon = when {
        isListening -> Icons.Default.Close
        isSpeaking -> Icons.Default.Settings  // Using Settings icon temporarily
        else -> Icons.Default.Info  // Using Info icon temporarily
    }

    FloatingActionButton(
        onClick = onClick,
        modifier = modifier,
        containerColor = backgroundColor,
        contentColor = contentColor
    ) {
        if (isProcessing) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = contentColor,
                strokeWidth = 2.dp
            )
        } else {
            Icon(
                imageVector = icon,
                contentDescription = when {
                    isListening -> "Stop listening"
                    isSpeaking -> "Speaking"
                    else -> "Start voice input"
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ChatScreenPreview() {
    VemorizeTheme {
        ChatScreenContent(
            uiState = ChatUiState.Ready(
                course = null,
                messages = emptyList()
            ),
            onEvent = {}
        )
    }
}
