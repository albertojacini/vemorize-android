package com.example.vemorize.ui.courses

import com.example.vemorize.domain.courses.Annotation
import com.example.vemorize.domain.courses.Course
import com.example.vemorize.domain.courses.CourseTree

sealed class CourseDetailUiState {
    data object Loading : CourseDetailUiState()

    data class Success(
        val course: Course,
        val tree: CourseTree,
        val annotations: Map<String, Annotation> = emptyMap() // nodeId -> Annotation
    ) : CourseDetailUiState()

    data class Error(val message: String) : CourseDetailUiState()
}
