package com.example.vemorize.ui.courses

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vemorize.data.courses.CoursesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class CoursesViewModel @Inject constructor(
    coursesRepository: CoursesRepository
) : ViewModel() {

    val uiState: StateFlow<CoursesUiState> = coursesRepository
        .getUserCourses()
        .map<List<com.example.vemorize.domain.model.Course>, CoursesUiState> { courses ->
            CoursesUiState.Success(courses)
        }
        .catch { error ->
            emit(CoursesUiState.Error(error.message ?: "Unknown error"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = CoursesUiState.Loading
        )
}
