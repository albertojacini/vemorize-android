package com.example.vemorize.ui.courses

import com.example.vemorize.domain.model.courses.Course
import com.example.vemorize.domain.model.courses.CourseTree

sealed class CourseDetailUiState {
    data object Loading : CourseDetailUiState()

    data class Success(
        val course: Course,
        val tree: CourseTree
    ) : CourseDetailUiState()

    data class Error(val message: String) : CourseDetailUiState()
}
