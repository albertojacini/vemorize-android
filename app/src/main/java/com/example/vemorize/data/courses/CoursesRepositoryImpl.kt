package com.example.vemorize.data.courses

import android.util.Log
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
            Log.d(TAG, "Fetching course by id: $courseId")

            val courses = postgrest
                .from("courses")
                .select {
                    filter {
                        eq("id", courseId)
                    }
                }
                .decodeList<Course>()

            val course = courses.firstOrNull()
            if (course != null) {
                Log.d(TAG, "Found course: ${course.title}")
            } else {
                Log.w(TAG, "No course found with id: $courseId")
            }

            course
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching course $courseId", e)
            null
        }
    }

    companion object {
        private const val TAG = "CoursesRepository"
    }
}
