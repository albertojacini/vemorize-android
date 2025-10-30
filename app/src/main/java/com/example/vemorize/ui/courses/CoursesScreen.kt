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
import com.example.vemorize.domain.courses.Course
import com.example.vemorize.ui.theme.VemorizeTheme

@Composable
fun CoursesScreen(
    viewModel: CoursesViewModel = hiltViewModel(),
    onCourseClick: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    CoursesContent(
        uiState = uiState,
        onCourseClick = onCourseClick
    )
}

@Composable
fun CoursesContent(
    uiState: CoursesUiState,
    onCourseClick: (String) -> Unit = {}
) {
    when (uiState) {
        is CoursesUiState.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        is CoursesUiState.Success -> {
            if (uiState.courses.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No courses yet",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.courses) { course ->
                        CourseCard(
                            course = course,
                            onClick = { onCourseClick(course.id) }
                        )
                    }
                }
            }
        }
        is CoursesUiState.Error -> {
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
fun CourseCard(
    course: Course,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = course.title,
                style = MaterialTheme.typography.titleMedium
            )
            course.description?.let { desc ->
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = desc,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CoursesScreenPreview() {
    VemorizeTheme {
        CoursesContent(
            uiState = CoursesUiState.Success(
                courses = listOf(
                    Course(
                        id = "1",
                        userId = "user1",
                        templateId = null,
                        title = "German Verbs",
                        description = "Learn basic German verbs",
                        createdAt = "2025-01-01T00:00:00Z",
                        updatedAt = "2025-01-01T00:00:00Z"
                    ),
                    Course(
                        id = "2",
                        userId = "user1",
                        templateId = null,
                        title = "Spanish Vocabulary",
                        description = "Essential Spanish words",
                        createdAt = "2025-01-01T00:00:00Z",
                        updatedAt = "2025-01-01T00:00:00Z"
                    )
                )
            ),
            onCourseClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun CoursesScreenLoadingPreview() {
    VemorizeTheme {
        CoursesContent(
            uiState = CoursesUiState.Loading,
            onCourseClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun CoursesScreenEmptyPreview() {
    VemorizeTheme {
        CoursesContent(
            uiState = CoursesUiState.Success(courses = emptyList()),
            onCourseClick = {}
        )
    }
}
