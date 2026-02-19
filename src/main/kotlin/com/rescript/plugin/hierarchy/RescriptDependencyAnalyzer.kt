package com.rescript.plugin.hierarchy

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.rescript.plugin.lang.RescriptTokenTypes
import com.rescript.plugin.lang.psi.RescriptElementTypes
import com.rescript.plugin.lang.psi.RescriptPsiUtils

/**
 * Analyzes module dependencies from PSI elements.
 * Pure utility class for extracting module references and building module trees.
 */
object RescriptDependencyAnalyzer {
    /**
     * Represents a module node in the hierarchy tree.
     */
    data class ModuleNode(
        val name: String,
        val element: PsiElement?,
        val children: List<ModuleNode>,
    )

    /**
     * Represents a module reference (open or include statement).
     */
    data class ModuleReference(
        val modulePath: String,
        val kind: ReferenceKind,
        val element: PsiElement,
    )

    enum class ReferenceKind {
        OPEN,
        INCLUDE,
    }

    /**
     * Extracts all open/include module references from a PSI scope.
     */
    fun extractModuleReferences(scope: PsiElement): List<ModuleReference> {
        val references = mutableListOf<ModuleReference>()
        collectReferences(scope, references)
        return references
    }

    private fun collectReferences(
        scope: PsiElement,
        references: MutableList<ModuleReference>,
    ) {
        for (child in scope.children) {
            val elementType = child.node?.elementType
            when (elementType) {
                RescriptElementTypes.OPEN_STATEMENT -> {
                    val path = extractModulePath(child)
                    if (path.isNotEmpty()) {
                        references.add(ModuleReference(path, ReferenceKind.OPEN, child))
                    }
                }
                RescriptElementTypes.INCLUDE_STATEMENT -> {
                    val path = extractModulePath(child)
                    if (path.isNotEmpty()) {
                        references.add(ModuleReference(path, ReferenceKind.INCLUDE, child))
                    }
                }
                RescriptElementTypes.MODULE_DECLARATION -> {
                    // Recurse into nested modules
                    collectReferences(child, references)
                }
            }
        }
    }

    /**
     * Extracts the module path from an OPEN_STATEMENT or INCLUDE_STATEMENT element.
     * e.g., "open Belt.Array" -> "Belt.Array"
     * e.g., "include Utils" -> "Utils"
     */
    internal fun extractModulePath(element: PsiElement): String {
        val tokens =
            buildList {
                var child = element.firstChild
                var pastKeyword = false
                while (child != null) {
                    val type = child.node?.elementType
                    if (type == RescriptTokenTypes.OPEN || type == RescriptTokenTypes.INCLUDE) {
                        pastKeyword = true
                    } else if (pastKeyword && (type == RescriptTokenTypes.UIDENT || type == RescriptTokenTypes.DOT)) {
                        add(child.text)
                    }
                    child = child.nextSibling
                }
            }
        return tokens.joinToString("")
    }

    /**
     * Builds a tree of nested MODULE_DECLARATION elements from a file.
     */
    @Suppress("unused")
    fun buildModuleTree(file: PsiFile): List<ModuleNode> = collectModuleNodes(file)

    private fun collectModuleNodes(scope: PsiElement): List<ModuleNode> {
        val nodes = mutableListOf<ModuleNode>()
        for (child in scope.children) {
            if (child.node?.elementType == RescriptElementTypes.MODULE_DECLARATION) {
                val name = RescriptPsiUtils.extractName(child)
                val children = collectModuleNodes(child)
                nodes.add(ModuleNode(name, child, children))
            }
        }
        return nodes
    }

    /**
     * Gets all unique module names referenced by open/include statements in a file.
     * Returns just the top-level module name (e.g., "Belt" from "Belt.Array").
     */
    @Suppress("unused")
    fun getReferencedModuleNames(file: PsiFile): Set<String> {
        val refs = extractModuleReferences(file)
        return refs
            .map { it.modulePath.substringBefore(".") }
            .filter { it.isNotEmpty() }
            .toSet()
    }
}
