package com.rescript.plugin.diagram

/**
 * Structural role of a module node in the dependency graph, used by
 * the visual view to colour-code node boxes. Computed by
 * [RescriptDependencyDiagramModel.classifyNodes] from the
 * `(nodes, edges)` pair so it stays a pure function of the model state.
 */
enum class NodeRole {
    /** in-degree 0 and not part of a cycle: nothing depends on this module. */
    ENTRY_POINT,

    /** Both in-degree > 0 and out-degree > 0: a transit module. */
    INTERMEDIATE,

    /** out-degree 0 and not part of a cycle: a sink. */
    LEAF,

    /** Belongs to a strongly connected component (Kahn's BFS cannot drain it). */
    CYCLE_MEMBER,
}

/**
 * Data model representing a directed graph of ReScript module dependencies.
 *
 * Each node is a module name, and edges represent `open`/`include` relationships
 * (source module depends on target module). Used by the dependency diagram
 * visualization to render the module relationship graph.
 *
 * @see RescriptDependencyDiagramProvider which builds this model from PSI data
 */
class RescriptDependencyDiagramModel {
    /** Represents a node in the dependency graph. */
    data class ModuleNode(
        val name: String,
        val dependencies: List<String>,
    )

    /** Represents a directed edge from one module to another. */
    data class ModuleEdge(
        val from: String,
        val to: String,
    )

    private val modules = mutableMapOf<String, MutableList<String>>()

    /**
     * Adds a module and its dependencies to the graph.
     *
     * @param moduleName the name of the module
     * @param dependencies the modules this module depends on (via open/include)
     */
    fun addModule(
        moduleName: String,
        dependencies: List<String>,
    ) {
        modules.getOrPut(moduleName) { mutableListOf() }.addAll(dependencies)
        // Ensure dependency nodes exist
        for (dep in dependencies) {
            modules.getOrPut(dep) { mutableListOf() }
        }
    }

    /**
     * Returns all module nodes in the graph.
     *
     * @return list of module nodes with their dependencies
     */
    fun getNodes(): List<ModuleNode> = modules.map { (name, deps) -> ModuleNode(name, deps.distinct()) }

    /**
     * Returns all directed edges in the graph.
     *
     * @return list of edges (from → to)
     */
    fun getEdges(): List<ModuleEdge> =
        modules.flatMap { (name, deps) ->
            deps.distinct().map { dep -> ModuleEdge(name, dep) }
        }

    /**
     * Returns the number of modules in the graph.
     *
     * @return module count
     */
    fun moduleCount(): Int = modules.size

    /**
     * Returns the number of edges (dependencies) in the graph.
     *
     * @return edge count
     */
    fun edgeCount(): Int = getEdges().size

    /**
     * Generates a DOT format representation of the dependency graph.
     *
     * @return the DOT format string, suitable for Graphviz rendering
     */
    fun toDot(): String =
        buildString {
            appendLine("digraph ReScriptDependencies {")
            appendLine("  rankdir=TB;")
            appendLine("  node [shape=box];")
            for (edge in getEdges()) {
                val from = DotLabelEscaping.escapeDotLabel(edge.from)
                val to = DotLabelEscaping.escapeDotLabel(edge.to)
                appendLine("  \"$from\" -> \"$to\";")
            }
            appendLine("}")
        }

    companion object {
        /**
         * Classifies each module node into a [NodeRole] based on its
         * in-degree, out-degree, and cycle membership. Pure function of
         * `(nodes, edges)`; used by the visual view to pick palette
         * entries for box colouring.
         *
         * Algorithm:
         * 1. Compute in-degree and out-degree for every node.
         * 2. Run Kahn's BFS topological drain. Nodes that cannot be
         *    drained belong to one or more strongly connected components
         *    and are tagged [NodeRole.CYCLE_MEMBER] regardless of degree.
         * 3. For drained nodes, in-degree 0 → [NodeRole.ENTRY_POINT],
         *    out-degree 0 → [NodeRole.LEAF], otherwise
         *    [NodeRole.INTERMEDIATE].
         *
         * @param nodes module nodes in any order
         * @param edges directed dependency edges (from → to)
         * @return map from module name to its classified role
         */
        fun classifyNodes(
            nodes: List<ModuleNode>,
            edges: List<ModuleEdge>,
        ): Map<String, NodeRole> {
            if (nodes.isEmpty()) return emptyMap()

            val inDegree = nodes.associate { it.name to 0 }.toMutableMap()
            val outDegree = nodes.associate { it.name to 0 }.toMutableMap()
            val outgoing = nodes.associate { it.name to mutableListOf<String>() }
            for (edge in edges) {
                if (edge.to in inDegree) inDegree[edge.to] = inDegree.getValue(edge.to) + 1
                if (edge.from in outDegree) outDegree[edge.from] = outDegree.getValue(edge.from) + 1
                outgoing[edge.from]?.add(edge.to)
            }

            // Kahn's BFS: start from nodes with in-degree 0 and drain.
            val workingIn = inDegree.toMutableMap()
            val drained = mutableSetOf<String>()
            val queue: ArrayDeque<String> = ArrayDeque()
            for (n in nodes) if (workingIn[n.name] == 0) queue.add(n.name)
            while (queue.isNotEmpty()) {
                val cur = queue.removeFirst()
                drained.add(cur)
                for (target in outgoing[cur].orEmpty()) {
                    val remaining = (workingIn[target] ?: 0) - 1
                    workingIn[target] = remaining
                    if (remaining == 0) queue.add(target)
                }
            }

            return nodes.associate { node ->
                val name = node.name
                val role =
                    when {
                        name !in drained -> NodeRole.CYCLE_MEMBER
                        inDegree.getValue(name) == 0 -> NodeRole.ENTRY_POINT
                        outDegree.getValue(name) == 0 -> NodeRole.LEAF
                        else -> NodeRole.INTERMEDIATE
                    }
                name to role
            }
        }
    }
}
