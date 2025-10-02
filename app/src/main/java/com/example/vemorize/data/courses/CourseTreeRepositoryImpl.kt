package com.example.vemorize.data.courses

import com.example.vemorize.domain.model.CourseNode
import com.example.vemorize.domain.model.CourseTree
import io.github.jan.supabase.postgrest.Postgrest
import javax.inject.Inject

class CourseTreeRepositoryImpl @Inject constructor(
    private val postgrest: Postgrest
) : CourseTreeRepository {

    override suspend fun getCourseTree(courseId: String): CourseTree? {
        return try {
            // Fetch all nodes for this course in one query
            val nodes = postgrest
                .from("course_nodes")
                .select {
                    filter {
                        eq("course_id", courseId)
                    }
                }
                .decodeList<CourseNode>()

            if (nodes.isEmpty()) {
                null
            } else {
                CourseTree.fromNodes(nodes)
            }
        } catch (e: Exception) {
            // Log error in production
            null
        }
    }
}
