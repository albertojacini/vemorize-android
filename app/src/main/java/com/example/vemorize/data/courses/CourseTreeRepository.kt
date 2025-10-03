package com.example.vemorize.data.courses

import com.example.vemorize.domain.model.courses.CourseTree

interface CourseTreeRepository {
    suspend fun getCourseTree(courseId: String): CourseTree?
}
