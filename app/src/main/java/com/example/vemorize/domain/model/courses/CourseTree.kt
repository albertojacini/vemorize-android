package com.example.vemorize.domain.model.courses

data class CourseTree(
    val rootNode: CourseNode?,
    val allNodes: List<CourseNode>
) {
    companion object {
        /**
         * Build a tree from a flat list of nodes.
         * Finds the root node (parent_id is null) and organizes all nodes.
         */
        fun fromNodes(nodes: List<CourseNode>): CourseTree {
            if (nodes.isEmpty()) {
                return CourseTree(rootNode = null, allNodes = emptyList())
            }

            // Find root node (parent_id is null)
            val rootNode = nodes.firstOrNull { it.parentId == null }

            // Sort by order_index
            val sortedNodes = nodes.sortedBy { it.orderIndex }

            return CourseTree(
                rootNode = rootNode,
                allNodes = sortedNodes
            )
        }
    }

    /**
     * Get all nodes that are children of the specified parent.
     */
    fun getChildren(parentId: String): List<CourseNode> {
        return allNodes.filter { it.parentId == parentId }
    }

    /**
     * Get all leaf nodes (nodes with nodeType = "leaf").
     */
    fun getAllLeaves(): List<CourseNode> {
        return allNodes.filter { it.nodeType == "leaf" }
    }
}
