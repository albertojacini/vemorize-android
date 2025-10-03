package com.example.vemorize.ui.courses

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.vemorize.domain.model.courses.Course
import com.example.vemorize.domain.model.courses.CourseNode
import com.example.vemorize.domain.model.courses.CourseTree
import com.example.vemorize.ui.theme.VemorizeTheme

@Composable
fun CourseDetailScreen(
    viewModel: CourseDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    CourseDetailContent(uiState = uiState)
}

@Composable
fun CourseDetailContent(uiState: CourseDetailUiState) {
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
                    NodeCard(node = node)
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
fun NodeCard(node: CourseNode) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
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
            uiState = CourseDetailUiState.Success(course = course, tree = tree)
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
