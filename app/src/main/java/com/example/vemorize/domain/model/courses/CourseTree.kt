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

    /**
     * Get a leaf node by ID.
     */
    fun getLeafById(leafId: String): CourseNode? {
        return allNodes.firstOrNull { it.id == leafId && it.nodeType == "leaf" }
    }

    /**
     * Get a leaf at the specified offset from the current leaf.
     * Returns null if offset is out of bounds.
     */
    fun getLeafAtOffset(currentLeaf: CourseNode, steps: Int): CourseNode? {
        val leaves = getAllLeaves()
        val currentIndex = leaves.indexOfFirst { it.id == currentLeaf.id }

        if (currentIndex == -1) {
            // Current leaf not found in this tree
            return null
        }

        val targetIndex = currentIndex + steps

        // Check bounds
        if (targetIndex < 0 || targetIndex >= leaves.size) {
            return null
        }

        return leaves[targetIndex]
    }
}
