package com.rescript.plugin.hierarchy.call

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.PsiSearchHelper
import com.intellij.psi.search.UsageSearchContext
import com.rescript.plugin.lang.RescriptTokenTypes
import com.rescript.plugin.lang.psi.RescriptElementTypes
import com.rescript.plugin.lang.psi.RescriptPsiUtils

/**
 * Analyzes function call relationships from PSI elements.
 *
 * Provides caller and callee discovery using text-based PSI search.
 * Since the ReScript LSP does not support `textDocument/prepareCallHierarchy`,
 * this analyzer uses [PsiSearchHelper] for callers and local token scanning for callees.
 *
 * @see RescriptCallerTreeStructure for caller tree construction
 * @see RescriptCalleeTreeStructure for callee tree construction
 */
object RescriptCallAnalyzer {
    /**
     * Represents a call relationship between two declarations.
     *
     * @param declaration the enclosing declaration (caller or callee)
     * @param callSite the specific token where the call occurs
     */
    data class CallReference(
        val declaration: PsiElement,
        val callSite: PsiElement,
    )

    /**
     * Finds all declarations that call the given target function.
     *
     * Searches the entire project for occurrences of the target's name within
     * ReScript files, then resolves each occurrence to its enclosing declaration.
     *
     * @param target the LET_DECLARATION or EXTERNAL_DECLARATION to find callers for
     * @param project the current project
     * @return list of call references from other declarations to the target
     */
    fun findCallers(
        target: PsiElement,
        project: Project,
    ): List<CallReference> {
        val name = RescriptPsiUtils.extractName(target)
        if (name == "(anonymous)" || name == "(unknown)") return emptyList()

        val results = mutableListOf<CallReference>()
        val scope = GlobalSearchScope.projectScope(project)
        val searchHelper = PsiSearchHelper.getInstance(project)

        searchHelper.processElementsWithWord(
            { element, _ ->
                val enclosing =
                    RescriptPsiUtils.findEnclosingDeclaration(
                        element,
                        RescriptPsiUtils.BINDING_TYPES,
                    )
                if (enclosing != null && enclosing != target && !isSameDeclaration(enclosing, target)) {
                    results.add(CallReference(enclosing, element))
                }
                true // continue processing
            },
            scope,
            name,
            UsageSearchContext.IN_CODE,
            true, // case sensitive
        )

        return results.distinctBy { it.declaration }
    }

    /**
     * Finds all functions called within the body of the given target declaration.
     *
     * Scans the target's body (everything after the `=` token) for LIDENT tokens,
     * then resolves each to a same-file declaration with a matching name.
     *
     * @param target the LET_DECLARATION to find callees for
     * @param project the current project (unused but kept for API consistency)
     * @return list of call references from the target to other declarations
     */
    fun findCallees(
        target: PsiElement,
        @Suppress("UNUSED_PARAMETER") project: Project,
    ): List<CallReference> {
        val bodyTokens = extractBodyIdentifiers(target)
        if (bodyTokens.isEmpty()) return emptyList()

        val file = target.containingFile ?: return emptyList()
        val declarations = collectFileDeclarations(file)

        val results = mutableListOf<CallReference>()
        for ((tokenName, tokenElement) in bodyTokens) {
            for (decl in declarations) {
                if (decl == target) continue
                if (isSameDeclaration(decl, target)) continue
                val declName = RescriptPsiUtils.extractName(decl)
                if (declName == tokenName) {
                    results.add(CallReference(decl, tokenElement))
                }
            }
        }

        return results.distinctBy { it.declaration }
    }

    /**
     * Extracts identifier tokens from the body of a declaration (after the `=` sign).
     *
     * @param declaration the declaration element to scan
     * @return list of pairs (identifier name, token element)
     */
    internal fun extractBodyIdentifiers(declaration: PsiElement): List<Pair<String, PsiElement>> {
        val identifiers = mutableListOf<Pair<String, PsiElement>>()
        var afterEquals = false
        var child = declaration.firstChild

        while (child != null) {
            val type = child.node?.elementType
            if (type == RescriptTokenTypes.EQ) {
                afterEquals = true
            } else if (afterEquals && type == RescriptTokenTypes.LIDENT) {
                identifiers.add(child.text to child)
            }
            // Recurse into composite children
            if (afterEquals && child.firstChild != null) {
                identifiers.addAll(extractBodyIdentifiersRecursive(child))
            }
            child = child.nextSibling
        }

        return identifiers
    }

    private fun extractBodyIdentifiersRecursive(element: PsiElement): List<Pair<String, PsiElement>> {
        val identifiers = mutableListOf<Pair<String, PsiElement>>()
        var child = element.firstChild

        while (child != null) {
            val type = child.node?.elementType
            if (type == RescriptTokenTypes.LIDENT) {
                identifiers.add(child.text to child)
            }
            if (child.firstChild != null) {
                identifiers.addAll(extractBodyIdentifiersRecursive(child))
            }
            child = child.nextSibling
        }

        return identifiers
    }

    /**
     * Collects all LET_DECLARATION and EXTERNAL_DECLARATION elements from a file.
     */
    internal fun collectFileDeclarations(file: PsiElement): List<PsiElement> {
        val declarations = mutableListOf<PsiElement>()
        collectDeclarationsRecursive(file, declarations)
        return declarations
    }

    private fun collectDeclarationsRecursive(
        element: PsiElement,
        declarations: MutableList<PsiElement>,
    ) {
        for (child in element.children) {
            val type = child.node?.elementType
            if (type == RescriptElementTypes.LET_DECLARATION ||
                type == RescriptElementTypes.EXTERNAL_DECLARATION
            ) {
                declarations.add(child)
            }
            if (type == RescriptElementTypes.MODULE_DECLARATION) {
                collectDeclarationsRecursive(child, declarations)
            }
        }
    }

    private fun isSameDeclaration(
        a: PsiElement,
        b: PsiElement,
    ): Boolean = a.textRange == b.textRange && a.containingFile == b.containingFile
}
