package com.rescript.plugin.projectview

import com.intellij.ide.projectView.TreeStructureProvider
import com.intellij.ide.projectView.ViewSettings
import com.intellij.ide.projectView.impl.nodes.NestingTreeNode
import com.intellij.ide.projectView.impl.nodes.PsiFileNode
import com.intellij.ide.util.treeView.AbstractTreeNode
import com.rescript.plugin.util.RescriptFileUtil

/**
 * Nests `.resi` interface files and compiled JS output files (`.res.js`, `.res.mjs`,
 * `.res.cjs`, `.bs.js`, `.bs.mjs`, `.bs.cjs`) under their corresponding `.res`
 * implementation files in the Project View tree.
 *
 * For example, if `Foo.res`, `Foo.resi`, and `Foo.res.js` exist in the same directory,
 * both `Foo.resi` and `Foo.res.js` will appear as children of `Foo.res`.
 * Standalone `.resi` files (without a matching `.res`) remain at the top level.
 *
 * @see RescriptCompiledJsNodeDecorator for gray styling of compiled JS files
 */
class RescriptTreeStructureProvider : TreeStructureProvider {
    override fun modify(
        parent: AbstractTreeNode<*>,
        children: MutableCollection<AbstractTreeNode<*>>,
        settings: ViewSettings?,
    ): Collection<AbstractTreeNode<*>> {
        // Collect .res, .resi, and compiled JS PsiFileNodes by base name
        val resNodes = mutableMapOf<String, PsiFileNode>()
        val resiNodes = mutableMapOf<String, PsiFileNode>()
        // baseName -> list of compiled JS nodes (e.g., Demo.res.js, Demo.bs.mjs)
        val compiledJsNodes = mutableMapOf<String, MutableList<PsiFileNode>>()

        for (child in children) {
            if (child !is PsiFileNode) continue
            val file = child.virtualFile ?: continue
            val name = file.name
            when {
                RescriptFileUtil.isResFileName(name) -> {
                    val baseName = name.removeSuffix(".res")
                    resNodes[baseName] = child
                }

                RescriptFileUtil.isResiFileName(name) -> {
                    val baseName = name.removeSuffix(".resi")
                    resiNodes[baseName] = child
                }

                RescriptCompiledJsNodeDecorator.isCompiledJsFile(name) -> {
                    val baseName = RescriptCompiledJsNodeDecorator.extractBaseName(name)
                    if (baseName != null) {
                        compiledJsNodes.getOrPut(baseName) { mutableListOf() }.add(child)
                    }
                }
            }
        }

        // If nothing to nest, return as-is
        if (resNodes.isEmpty() || (resiNodes.isEmpty() && compiledJsNodes.isEmpty())) {
            return children
        }

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

            // Skip .resi that will be nested under matching .res
            if (RescriptFileUtil.isResiFileName(name)) {
                val baseName = name.removeSuffix(".resi")
                if (resNodes.containsKey(baseName)) continue
            }

            // Skip compiled JS that will be nested under matching .res
            if (RescriptCompiledJsNodeDecorator.isCompiledJsFile(name)) {
                val baseName = RescriptCompiledJsNodeDecorator.extractBaseName(name)
                if (baseName != null && resNodes.containsKey(baseName)) continue
            }

            // For .res files, create a nesting node with all matching children
            if (RescriptFileUtil.isResFileName(name)) {
                val baseName = name.removeSuffix(".res")
                val nestChildren = mutableListOf<PsiFileNode>()
                resiNodes[baseName]?.let { nestChildren.add(it) }
                compiledJsNodes[baseName]?.let { nestChildren.addAll(it) }
                if (nestChildren.isNotEmpty()) {
                    nestedNodes.add(NestingTreeNode(child, nestChildren))
                    continue
                }
            }

            nestedNodes.add(child)
        }

        return nestedNodes
    }
}
