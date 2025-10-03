package com.example.vemorize.ui.chat

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
                                enabled = !uiState.isProcessing
                            )
                            IconButton(
                                onClick = {
                                    if (uiState.userInput.isNotBlank()) {
                                        onEvent(ChatUiEvent.SendMessage(uiState.userInput))
                                    }
                                },
                                enabled = !uiState.isProcessing && uiState.userInput.isNotBlank()
                            ) {
                                Icon(Icons.Default.Send, contentDescription = "Send")
                            }
                        }
                    }
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
