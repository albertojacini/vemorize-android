package com.example.vemorize.ui.courses

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vemorize.data.courses.CourseTreeRepository
import com.example.vemorize.data.courses.CoursesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CourseDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val coursesRepository: CoursesRepository,
    private val courseTreeRepository: CourseTreeRepository
) : ViewModel() {

    private val courseId: String = checkNotNull(savedStateHandle["courseId"])

    private val _uiState = MutableStateFlow<CourseDetailUiState>(CourseDetailUiState.Loading)
    val uiState: StateFlow<CourseDetailUiState> = _uiState.asStateFlow()

    init {
        Log.d(TAG, "CourseDetailViewModel initialized for courseId: $courseId")
        loadCourseDetail()
    }

    private fun loadCourseDetail() {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Loading course detail for: $courseId")

                val course = coursesRepository.getCourseById(courseId)
                val tree = courseTreeRepository.getCourseTree(courseId)

                when {
                    course == null -> {
                        Log.e(TAG, "Course not found for id: $courseId")
                        _uiState.value = CourseDetailUiState.Error("Course not found for id: $courseId")
                    }
                    tree == null -> {
                        Log.w(TAG, "Tree not found for course: ${course.title}")
                        _uiState.value = CourseDetailUiState.Error("This course doesn't have any content yet.\n\nAdd some course nodes in Supabase to see them here.")
                    }
                    else -> {
                        Log.d(TAG, "Successfully loaded course and tree. Tree has ${tree.allNodes.size} nodes")
                        _uiState.value = CourseDetailUiState.Success(course = course, tree = tree)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading course detail", e)
                _uiState.value = CourseDetailUiState.Error("Exception: ${e.message ?: "Unknown error"}\n${e.stackTraceToString().take(200)}")
            }
        }
    }

    companion object {
        private const val TAG = "CourseDetailViewModel"
    }
}
