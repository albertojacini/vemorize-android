package com.example.vemorize.data.courses

import com.example.vemorize.domain.model.CourseTree

interface CourseTreeRepository {
    suspend fun getCourseTree(courseId: String): CourseTree?
}
