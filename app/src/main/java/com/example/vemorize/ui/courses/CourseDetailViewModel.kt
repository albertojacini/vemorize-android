package com.example.vemorize.ui.courses

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
        loadCourseDetail()
    }

    private fun loadCourseDetail() {
        viewModelScope.launch {
            try {
                val course = coursesRepository.getCourseById(courseId)
                val tree = courseTreeRepository.getCourseTree(courseId)

                if (course != null && tree != null) {
                    _uiState.value = CourseDetailUiState.Success(course = course, tree = tree)
                } else {
                    _uiState.value = CourseDetailUiState.Error("Course or tree not found")
                }
            } catch (e: Exception) {
                _uiState.value = CourseDetailUiState.Error(e.message ?: "Unknown error")
            }
        }
    }
}
