package com.rescript.plugin.projectview

import com.intellij.ide.projectView.TreeStructureProvider
import com.intellij.ide.projectView.ViewSettings
import com.intellij.ide.projectView.impl.nodes.NestingTreeNode
import com.intellij.ide.projectView.impl.nodes.PsiFileNode
import com.intellij.ide.util.treeView.AbstractTreeNode

/**
 * Nests `.resi` interface files under their corresponding `.res` implementation
 * files in the Project View tree.
 *
 * For example, if `Foo.res` and `Foo.resi` exist in the same directory,
 * `Foo.resi` will appear as a child of `Foo.res` rather than as a sibling.
 * Standalone `.resi` files (without a matching `.res`) remain at the top level.
 */
class RescriptTreeStructureProvider : TreeStructureProvider {
    override fun modify(
        parent: AbstractTreeNode<*>,
        children: MutableCollection<AbstractTreeNode<*>>,
        settings: ViewSettings?,
    ): Collection<AbstractTreeNode<*>> {
        // Collect .res and .resi PsiFileNodes
        val resNodes = mutableMapOf<String, PsiFileNode>()
        val resiNodes = mutableMapOf<String, PsiFileNode>()

        for (child in children) {
            if (child !is PsiFileNode) continue
            val file = child.virtualFile ?: continue
            val name = file.name
            when {
                name.endsWith(".res") -> {
                    val baseName = name.removeSuffix(".res")
                    resNodes[baseName] = child
                }
                name.endsWith(".resi") -> {
                    val baseName = name.removeSuffix(".resi")
                    resiNodes[baseName] = child
                }
            }
        }

        // If no .resi files or no .res files, nothing to nest
        if (resiNodes.isEmpty() || resNodes.isEmpty()) return children

        // Build the nested result
        val resiNamesToNest = mutableSetOf<String>()
        val nestedNodes = mutableListOf<AbstractTreeNode<*>>()

        for (child in children) {
            if (child !is PsiFileNode) {
                nestedNodes.add(child)
                continue
            }
            val file = child.virtualFile
            if (file == null) {
                nestedNodes.add(child)
                continue
            }
            val name = file.name

            if (name.endsWith(".resi")) {
                val baseName = name.removeSuffix(".resi")
                if (resNodes.containsKey(baseName)) {
                    // This .resi has a matching .res — skip it (will be nested)
                    resiNamesToNest.add(baseName)
                    continue
                }
            }

            if (name.endsWith(".res")) {
                val baseName = name.removeSuffix(".res")
                val matchingResi = resiNodes[baseName]
                if (matchingResi != null) {
                    // Create a nesting node: .res parent with .resi child
                    val nestingNode = NestingTreeNode(child, listOf(matchingResi))
                    nestedNodes.add(nestingNode)
                    continue
                }
            }

            nestedNodes.add(child)
        }

        return nestedNodes
    }
}
