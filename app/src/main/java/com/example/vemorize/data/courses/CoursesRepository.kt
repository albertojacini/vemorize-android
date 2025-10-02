package com.example.vemorize.data.courses

import com.example.vemorize.domain.model.Course
import kotlinx.coroutines.flow.Flow

interface CoursesRepository {
    fun getUserCourses(): Flow<List<Course>>
}
