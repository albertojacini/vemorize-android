package com.example.vemorize.ui.courses

import com.example.vemorize.domain.model.Course
import com.example.vemorize.domain.model.CourseTree

sealed class CourseDetailUiState {
    data object Loading : CourseDetailUiState()

    data class Success(
        val course: Course,
        val tree: CourseTree
    ) : CourseDetailUiState()

    data class Error(val message: String) : CourseDetailUiState()
}
