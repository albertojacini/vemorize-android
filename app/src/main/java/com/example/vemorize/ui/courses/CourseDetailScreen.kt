package com.example.vemorize.ui.courses

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.vemorize.domain.courses.Annotation
import com.example.vemorize.domain.courses.MemorizationState
import com.example.vemorize.domain.courses.Course
import com.example.vemorize.domain.courses.CourseNode
import com.example.vemorize.domain.courses.CourseTree
import com.example.vemorize.ui.theme.VemorizeTheme

@Composable
fun CourseDetailScreen(
    viewModel: CourseDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    CourseDetailContent(
        uiState = uiState,
        onMemorizationStateChange = viewModel::updateMemorizationState,
        onPersonalNotesChange = viewModel::updatePersonalNotes,
        onVisitCountIncrement = viewModel::incrementVisitCount
    )
}

@Composable
fun CourseDetailContent(
    uiState: CourseDetailUiState,
    onMemorizationStateChange: (String, MemorizationState) -> Unit = { _, _ -> },
    onPersonalNotesChange: (String, String?) -> Unit = { _, _ -> },
    onVisitCountIncrement: (String) -> Unit = {}
) {
    when (uiState) {
        is CourseDetailUiState.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        is CourseDetailUiState.Success -> {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Course header
                item {
                    CourseHeader(course = uiState.course)
                }

                // Tree nodes list
                items(uiState.tree.allNodes) { node ->
                    NodeCard(
                        node = node,
                        annotation = uiState.annotations[node.id],
                        onMemorizationStateChange = { newState ->
                            onMemorizationStateChange(node.id, newState)
                        },
                        onPersonalNotesChange = { notes ->
                            onPersonalNotesChange(node.id, notes)
                        },
                        onVisitCountIncrement = {
                            onVisitCountIncrement(node.id)
                        }
                    )
                }
            }
        }
        is CourseDetailUiState.Error -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Error: ${uiState.message}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun CourseHeader(course: Course) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = course.title,
                style = MaterialTheme.typography.headlineMedium
            )
            course.description?.let { desc ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = desc,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun NodeCard(
    node: CourseNode,
    annotation: Annotation? = null,
    onMemorizationStateChange: (MemorizationState) -> Unit = {},
    onPersonalNotesChange: (String?) -> Unit = {},
    onVisitCountIncrement: () -> Unit = {}
) {
    var showNotesPreview by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        border = if (annotation == null) {
            BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
        } else null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    // Node type badge
                    Text(
                        text = if (node.nodeType == "container") "Container" else "Leaf: ${node.leafType ?: ""}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Title
                    Text(
                        text = node.title,
                        style = MaterialTheme.typography.titleMedium
                    )

                    // Description
                    node.description?.let { desc ->
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = desc,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Annotation controls
                AnnotationControls(
                    annotation = annotation,
                    onMemorizationStateChange = onMemorizationStateChange,
                    onPersonalNotesChange = onPersonalNotesChange,
                    onVisitCountIncrement = onVisitCountIncrement
                )
            }

            // Personal notes preview (if exists)
            annotation?.personalNotes?.let { notes ->
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    ),
                    onClick = { showNotesPreview = true }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Personal notes",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = notes.take(60) + if (notes.length > 60) "..." else "",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 2
                        )
                    }
                }
            }

            // Reading text for leaf nodes
            if (node.nodeType == "leaf") {
                node.readingTextRegular?.let { text ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = text,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }

    // Notes preview dialog
    if (showNotesPreview && annotation?.personalNotes != null) {
        AlertDialog(
            onDismissRequest = { showNotesPreview = false },
            title = { Text("Personal Notes") },
            text = {
                Text(
                    text = annotation.personalNotes,
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(onClick = { showNotesPreview = false }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
fun AnnotationControls(
    annotation: Annotation?,
    onMemorizationStateChange: (MemorizationState) -> Unit,
    onPersonalNotesChange: (String?) -> Unit,
    onVisitCountIncrement: () -> Unit
) {
    var showNotesDialog by remember { mutableStateOf(false) }
    var stateMenuExpanded by remember { mutableStateOf(false) }

    val state = annotation?.memorizationState ?: MemorizationState.NEW
    val stateLabel = when (state) {
        MemorizationState.NEW -> "New"
        MemorizationState.LEARNING -> "Learning"
        MemorizationState.REVIEW -> "Review"
        MemorizationState.MASTERED -> "Mastered"
    }
    val stateColor = when (state) {
        MemorizationState.NEW -> MaterialTheme.colorScheme.error
        MemorizationState.LEARNING -> MaterialTheme.colorScheme.tertiary
        MemorizationState.REVIEW -> MaterialTheme.colorScheme.secondary
        MemorizationState.MASTERED -> MaterialTheme.colorScheme.primary
    }

    Column(horizontalAlignment = Alignment.End) {
        // Memorization state badge
        Box {
            AssistChip(
                onClick = { stateMenuExpanded = true },
                label = { Text(stateLabel, style = MaterialTheme.typography.labelSmall) },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = stateColor.copy(alpha = 0.2f),
                    labelColor = stateColor
                ),
                border = if (annotation == null) {
                    BorderStroke(1.dp, stateColor.copy(alpha = 0.5f))
                } else null,
                trailingIcon = {
                    Icon(
                        Icons.Default.ArrowDropDown,
                        contentDescription = "Change state",
                        modifier = Modifier.size(18.dp)
                    )
                }
            )

            DropdownMenu(
                expanded = stateMenuExpanded,
                onDismissRequest = { stateMenuExpanded = false }
            ) {
                MemorizationState.entries.forEach { state ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                when (state) {
                                    MemorizationState.NEW -> "New"
                                    MemorizationState.LEARNING -> "Learning"
                                    MemorizationState.REVIEW -> "Review"
                                    MemorizationState.MASTERED -> "Mastered"
                                }
                            )
                        },
                        onClick = {
                            onMemorizationStateChange(state)
                            stateMenuExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            // Visit count button
            FilledTonalIconButton(
                onClick = onVisitCountIncrement,
                modifier = Modifier.size(32.dp)
            ) {
                Text(
                    text = "${annotation?.visitCount ?: 0}",
                    style = MaterialTheme.typography.labelSmall
                )
            }

            // Notes button
            FilledTonalIconButton(
                onClick = { showNotesDialog = true },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = "Edit notes",
                    modifier = Modifier.size(14.dp),
                    tint = if (annotation?.personalNotes != null) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        }
    }

    // Notes dialog
    if (showNotesDialog) {
        var notesText by remember { mutableStateOf(annotation?.personalNotes ?: "") }

        AlertDialog(
            onDismissRequest = { showNotesDialog = false },
            title = { Text("Personal Notes") },
            text = {
                OutlinedTextField(
                    value = notesText,
                    onValueChange = { notesText = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Add your notes...") },
                    minLines = 4
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    onPersonalNotesChange(notesText.ifBlank { null })
                    showNotesDialog = false
                }) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNotesDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun CourseDetailScreenPreview() {
    VemorizeTheme {
        val course = Course(
            id = "1",
            userId = "user1",
            templateId = null,
            title = "German Verbs",
            description = "Learn basic German verbs",
            createdAt = "2025-01-01T00:00:00Z",
            updatedAt = "2025-01-01T00:00:00Z"
        )

        val rootNode = CourseNode(
            id = "root-1",
            courseId = "1",
            parentId = null,
            nodeType = "container",
            leafType = null,
            title = "Root Container",
            description = "Main container for verbs",
            orderIndex = 0,
            readingTextRegular = null,
            readingTextShort = null,
            readingTextLong = null,
            quizQuestions = null,
            data = null,
            createdAt = "2025-01-01T00:00:00Z"
        )

        val leafNode = CourseNode(
            id = "leaf-1",
            courseId = "1",
            parentId = "root-1",
            nodeType = "leaf",
            leafType = "language_vocabulary",
            title = "Verb: Sein",
            description = "To be",
            orderIndex = 1,
            readingTextRegular = "The verb 'sein' means 'to be' in German. It is irregular.",
            readingTextShort = "sein = to be",
            readingTextLong = "The verb 'sein' is one of the most important verbs in German...",
            quizQuestions = listOf("What does 'sein' mean?"),
            data = null,
            createdAt = "2025-01-01T00:00:00Z"
        )

        val tree = CourseTree.fromNodes(listOf(rootNode, leafNode))

        CourseDetailContent(
            uiState = CourseDetailUiState.Success(
                course = course,
                tree = tree,
                annotations = emptyMap()
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
fun CourseDetailScreenLoadingPreview() {
    VemorizeTheme {
        CourseDetailContent(uiState = CourseDetailUiState.Loading)
    }
}
