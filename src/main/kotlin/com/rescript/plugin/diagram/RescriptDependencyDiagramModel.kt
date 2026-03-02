package com.rescript.plugin.diagram

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
                appendLine("  \"${escapeDot(edge.from)}\" -> \"${escapeDot(edge.to)}\";")
            }
            appendLine("}")
        }

    /**
     * Escapes special characters for DOT format string context.
     *
     * Prevents injection of DOT directives via module names containing
     * double quotes or backslashes.
     */
    private fun escapeDot(value: String): String = value.replace("\\", "\\\\").replace("\"", "\\\"")
}
