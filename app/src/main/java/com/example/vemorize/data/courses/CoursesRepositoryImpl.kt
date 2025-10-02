package com.example.vemorize.data.courses

import com.example.vemorize.domain.model.Course
import io.github.jan.supabase.postgrest.Postgrest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class CoursesRepositoryImpl @Inject constructor(
    private val postgrest: Postgrest
) : CoursesRepository {

    override fun getUserCourses(): Flow<List<Course>> = flow {
        val courses = postgrest
            .from("courses")
            .select()
            .decodeList<Course>()
        emit(courses)
    }

    override suspend fun getCourseById(courseId: String): Course? {
        return try {
            val courses = postgrest
                .from("courses")
                .select {
                    filter {
                        eq("id", courseId)
                    }
                }
                .decodeList<Course>()
            courses.firstOrNull()
        } catch (e: Exception) {
            null
        }
    }
}
