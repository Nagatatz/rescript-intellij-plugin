package com.rescript.plugin.lang.psi

import com.intellij.icons.AllIcons
import com.intellij.psi.PsiElement
import com.intellij.psi.tree.IElementType
import com.intellij.psi.util.PsiTreeUtil
import com.rescript.plugin.RescriptIcons
import com.rescript.plugin.lang.RescriptTokenTypes
import javax.swing.Icon

/**
 * Utility functions for working with ReScript PSI elements.
 *
 * Provides name extraction, icon mapping, and description generation for
 * top-level declarations. Used by structure view, breadcrumbs, and navigation features.
 */
object RescriptPsiUtils {
    /**
     * Element types that represent navigable declarations.
     *
     * This set drives many declaration-oriented features: breadcrumbs, navbar, Find Usages,
     * Safe Delete, Search Everywhere, qualified-name copy, Goto Super and the interface
     * intentions. JSX nodes are deliberately *not* included here — a `<div>` is not a
     * declaration and must not become a Find Usages or Safe Delete target. The structure view,
     * which does want JSX, uses the wider [STRUCTURE_VIEW_TYPES] instead.
     */
    val NAVIGABLE_TYPES: Set<IElementType> =
        setOf(
            RescriptElementTypes.LET_DECLARATION,
            RescriptElementTypes.TYPE_DECLARATION,
            RescriptElementTypes.MODULE_DECLARATION,
            RescriptElementTypes.EXTERNAL_DECLARATION,
            RescriptElementTypes.EXCEPTION_DECLARATION,
        )

    /**
     * Element types shown in the structure view outline.
     *
     * Superset of [NAVIGABLE_TYPES] that additionally includes JSX elements and fragments so the
     * outline mirrors the markup tree. Self-closing elements are intentionally excluded because
     * they are leaf nodes that would only add noise. Scoped to the structure view so JSX nodes
     * do not leak into declaration-oriented features (Find Usages, Safe Delete, qualified-name
     * copy, etc.) that key off [NAVIGABLE_TYPES].
     */
    val STRUCTURE_VIEW_TYPES: Set<IElementType> =
        NAVIGABLE_TYPES +
            setOf(
                RescriptElementTypes.JSX_ELEMENT,
                RescriptElementTypes.JSX_FRAGMENT,
            )

    /** Element types for value bindings (let, external). Used by AddIgnore, AddUnderscore. */
    val BINDING_TYPES: Set<IElementType> =
        setOf(
            RescriptElementTypes.LET_DECLARATION,
            RescriptElementTypes.EXTERNAL_DECLARATION,
        )

    /** Element types eligible for annotation actions (let, type, module). Used by AddGenType. */
    val ANNOTATION_ELIGIBLE_TYPES: Set<IElementType> =
        setOf(
            RescriptElementTypes.LET_DECLARATION,
            RescriptElementTypes.TYPE_DECLARATION,
            RescriptElementTypes.MODULE_DECLARATION,
        )

    /** All top-level declaration types including open/include. Used by EmptyModule, StatementMover. */
    val ALL_DECLARATION_TYPES: Set<IElementType> =
        setOf(
            RescriptElementTypes.LET_DECLARATION,
            RescriptElementTypes.TYPE_DECLARATION,
            RescriptElementTypes.MODULE_DECLARATION,
            RescriptElementTypes.EXTERNAL_DECLARATION,
            RescriptElementTypes.OPEN_STATEMENT,
            RescriptElementTypes.INCLUDE_STATEMENT,
            RescriptElementTypes.EXCEPTION_DECLARATION,
        )

    /**
     * Finds the nearest enclosing declaration for the given element.
     *
     * @param element the PSI element to start searching from
     * @param types the set of declaration element types to match
     * @param includeElement whether to include the element itself in the search
     * @return the enclosing declaration, or null if not inside one
     */
    fun findEnclosingDeclaration(
        element: PsiElement,
        types: Set<IElementType>,
        includeElement: Boolean = true,
    ): PsiElement? =
        PsiTreeUtil.findFirstParent(element, !includeElement) { el ->
            el.node?.elementType in types
        }

    /**
     * Checks whether the element is inside a declaration of the given types.
     *
     * @param element the PSI element to check
     * @param types the set of declaration element types to match
     * @return true if a parent declaration is found
     */
    fun isInsideDeclaration(
        element: PsiElement,
        types: Set<IElementType>,
    ): Boolean = findEnclosingDeclaration(element, types, includeElement = false) != null

    private val IDENTIFIER_TYPES: Set<IElementType> =
        setOf(
            RescriptTokenTypes.LIDENT,
            RescriptTokenTypes.UIDENT,
            RescriptTokenTypes.UNDERSCORE,
        )

    /**
     * Extracts the declared name from a ReScript PSI element.
     *
     * Walks through child AST nodes to find the first identifier token after a
     * top-level keyword (e.g., `let`, `type`, `module`), skipping `rec` keywords.
     *
     * For JSX nodes the name is the opening tag name (e.g., `div`, `MyComponent`);
     * fragments are rendered as `<>` since they have no tag name.
     *
     * @return the declared name, or "(anonymous)" if no identifier is found
     */
    fun extractName(element: PsiElement): String {
        if (element is RescriptFile) return element.name

        val elementType = element.node?.elementType
        if (elementType == RescriptElementTypes.JSX_ELEMENT) {
            return RescriptJsxTagPairUtil.extractTagNames(element).openName ?: "(jsx)"
        }
        if (elementType == RescriptElementTypes.JSX_FRAGMENT) return "<>"

        val node = element.node ?: return "(unknown)"
        var child = node.firstChildNode
        var afterKeyword = false
        while (child != null) {
            if (RescriptTokenTypes.TOP_LEVEL_KEYWORDS.contains(child.elementType)) {
                afterKeyword = true
            } else if (child.elementType == RescriptTokenTypes.REC) {
                // skip 'rec' keyword — name follows after it
            } else if (afterKeyword && child.elementType in IDENTIFIER_TYPES) {
                return child.text
            }
            child = child.treeNext
        }
        return "(anonymous)"
    }

    /** Returns the appropriate icon for a ReScript PSI element based on its declaration type. */
    fun getIcon(element: PsiElement): Icon? {
        if (element is RescriptFile) return RescriptIcons.FILE

        return when (element.node?.elementType) {
            RescriptElementTypes.LET_DECLARATION -> AllIcons.Nodes.Function
            RescriptElementTypes.TYPE_DECLARATION -> AllIcons.Nodes.Type
            RescriptElementTypes.MODULE_DECLARATION -> AllIcons.Nodes.Module
            RescriptElementTypes.EXTERNAL_DECLARATION -> AllIcons.Nodes.PluginJB
            RescriptElementTypes.EXCEPTION_DECLARATION -> AllIcons.Nodes.ExceptionClass
            RescriptElementTypes.JSX_ELEMENT -> AllIcons.Nodes.Tag
            RescriptElementTypes.JSX_FRAGMENT -> AllIcons.Nodes.Tag
            else -> null
        }
    }

    /** Returns a human-readable description of the element's declaration type (e.g., "let declaration"). */
    fun getElementDescription(element: PsiElement): String? =
        when (element.node?.elementType) {
            RescriptElementTypes.LET_DECLARATION -> "let declaration"
            RescriptElementTypes.TYPE_DECLARATION -> "type declaration"
            RescriptElementTypes.MODULE_DECLARATION -> "module declaration"
            RescriptElementTypes.EXTERNAL_DECLARATION -> "external declaration"
            RescriptElementTypes.EXCEPTION_DECLARATION -> "exception declaration"
            RescriptElementTypes.JSX_ELEMENT -> "JSX element"
            RescriptElementTypes.JSX_FRAGMENT -> "JSX fragment"
            else -> null
        }
}
