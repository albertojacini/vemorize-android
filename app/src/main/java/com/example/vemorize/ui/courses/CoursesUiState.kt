package com.example.vemorize.ui.courses

import com.example.vemorize.domain.courses.Course

sealed interface CoursesUiState {
    data object Loading : CoursesUiState
    data class Success(val courses: List<Course>) : CoursesUiState
    data class Error(val message: String) : CoursesUiState
}
