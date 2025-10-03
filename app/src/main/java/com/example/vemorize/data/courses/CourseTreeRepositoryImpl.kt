package com.example.vemorize.data.courses

import android.util.Log
import com.example.vemorize.domain.model.courses.CourseNode
import com.example.vemorize.domain.model.courses.CourseTree
import io.github.jan.supabase.postgrest.Postgrest
import javax.inject.Inject

class CourseTreeRepositoryImpl @Inject constructor(
    private val postgrest: Postgrest
) : CourseTreeRepository {

    override suspend fun getCourseTree(courseId: String): CourseTree? {
        return try {
            Log.d(TAG, "Fetching tree for course: $courseId")

            // Fetch all nodes for this course in one query
            val nodes = postgrest
                .from("course_nodes")
                .select {
                    filter {
                        eq("course_id", courseId)
                    }
                }
                .decodeList<CourseNode>()

            Log.d(TAG, "Found ${nodes.size} nodes for course $courseId")

            if (nodes.isEmpty()) {
                Log.w(TAG, "No nodes found for course $courseId")
                null
            } else {
                val tree = CourseTree.fromNodes(nodes)
                Log.d(TAG, "Tree built successfully with root: ${tree.rootNode?.id}")
                tree
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching tree for course $courseId", e)
            null
        }
    }

    companion object {
        private const val TAG = "CourseTreeRepository"
    }
}
