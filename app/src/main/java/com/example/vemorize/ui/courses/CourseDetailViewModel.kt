package com.example.vemorize.ui.courses

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vemorize.data.courses.AnnotationRepository
import com.example.vemorize.data.courses.CourseTreeRepository
import com.example.vemorize.data.courses.CoursesRepository
import com.example.vemorize.domain.courses.MemorizationState
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
    private val courseTreeRepository: CourseTreeRepository,
    private val annotationRepository: AnnotationRepository
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
                val annotations = annotationRepository.getAnnotationsByCourse(courseId)

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
                        Log.d(TAG, "Successfully loaded course and tree. Tree has ${tree.allNodes.size} nodes, ${annotations.size} annotations")
                        val annotationsMap = annotations.associateBy { it.nodeId }
                        _uiState.value = CourseDetailUiState.Success(
                            course = course,
                            tree = tree,
                            annotations = annotationsMap
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading course detail", e)
                _uiState.value = CourseDetailUiState.Error("Exception: ${e.message ?: "Unknown error"}\n${e.stackTraceToString().take(200)}")
            }
        }
    }

    /**
     * Update memorization state for a node
     * Creates annotation if it doesn't exist yet (lazy creation)
     */
    fun updateMemorizationState(nodeId: String, newState: MemorizationState) {
        val currentState = _uiState.value
        if (currentState !is CourseDetailUiState.Success) return

        viewModelScope.launch {
            try {
                val existingAnnotation = currentState.annotations[nodeId]

                // Optimistic update
                val optimisticAnnotation = if (existingAnnotation != null) {
                    existingAnnotation.copy(memorizationState = newState)
                } else {
                    // Create placeholder for optimistic UI
                    null
                }

                if (optimisticAnnotation != null) {
                    val updatedAnnotations = currentState.annotations + (nodeId to optimisticAnnotation)
                    _uiState.value = currentState.copy(annotations = updatedAnnotations)
                }

                // Perform actual update/create
                if (existingAnnotation != null) {
                    Log.d(TAG, "Updating annotation ${existingAnnotation.id} state to $newState")
                    annotationRepository.updateAnnotation(
                        annotationId = existingAnnotation.id,
                        memorizationState = newState
                    )
                } else {
                    Log.d(TAG, "Creating new annotation for node $nodeId with state $newState")
                    val created = annotationRepository.createAnnotation(
                        courseId = courseId,
                        nodeId = nodeId,
                        memorizationState = newState
                    )
                    // Update with real annotation data
                    val updatedAnnotations = currentState.annotations + (nodeId to created)
                    _uiState.value = currentState.copy(annotations = updatedAnnotations)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error updating memorization state", e)
                // Revert optimistic update on error
                _uiState.value = currentState
            }
        }
    }

    /**
     * Update personal notes for a node
     * Creates annotation if it doesn't exist yet
     */
    fun updatePersonalNotes(nodeId: String, notes: String?) {
        val currentState = _uiState.value
        if (currentState !is CourseDetailUiState.Success) return

        viewModelScope.launch {
            try {
                val existingAnnotation = currentState.annotations[nodeId]

                if (existingAnnotation != null) {
                    Log.d(TAG, "Updating annotation ${existingAnnotation.id} notes")
                    annotationRepository.updateAnnotation(
                        annotationId = existingAnnotation.id,
                        personalNotes = notes
                    )
                    // Update local state
                    val updated = existingAnnotation.copy(personalNotes = notes)
                    val updatedAnnotations = currentState.annotations + (nodeId to updated)
                    _uiState.value = currentState.copy(annotations = updatedAnnotations)
                } else {
                    Log.d(TAG, "Creating new annotation for node $nodeId with notes")
                    val created = annotationRepository.createAnnotation(
                        courseId = courseId,
                        nodeId = nodeId,
                        personalNotes = notes
                    )
                    val updatedAnnotations = currentState.annotations + (nodeId to created)
                    _uiState.value = currentState.copy(annotations = updatedAnnotations)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error updating personal notes", e)
            }
        }
    }

    /**
     * Increment visit count for a node
     * Creates annotation if it doesn't exist yet
     */
    fun incrementVisitCount(nodeId: String) {
        val currentState = _uiState.value
        if (currentState !is CourseDetailUiState.Success) return

        viewModelScope.launch {
            try {
                val existingAnnotation = currentState.annotations[nodeId]

                if (existingAnnotation != null) {
                    val newCount = existingAnnotation.visitCount + 1
                    Log.d(TAG, "Incrementing visit count for annotation ${existingAnnotation.id} to $newCount")

                    // Optimistic update
                    val updated = existingAnnotation.copy(visitCount = newCount)
                    val updatedAnnotations = currentState.annotations + (nodeId to updated)
                    _uiState.value = currentState.copy(annotations = updatedAnnotations)

                    annotationRepository.updateAnnotation(
                        annotationId = existingAnnotation.id,
                        visitCount = newCount
                    )
                } else {
                    Log.d(TAG, "Creating new annotation for node $nodeId with visit count 1")
                    val created = annotationRepository.createAnnotation(
                        courseId = courseId,
                        nodeId = nodeId,
                        visitCount = 1
                    )
                    val updatedAnnotations = currentState.annotations + (nodeId to created)
                    _uiState.value = currentState.copy(annotations = updatedAnnotations)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error incrementing visit count", e)
                // Revert on error
                _uiState.value = currentState
            }
        }
    }

    companion object {
        private const val TAG = "CourseDetailViewModel"
    }
}
