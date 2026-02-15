package com.rescript.plugin.lang.psi

import com.intellij.icons.AllIcons
import com.intellij.psi.PsiElement
import com.intellij.psi.tree.IElementType
import com.rescript.plugin.RescriptIcons
import com.rescript.plugin.lang.RescriptTokenTypes
import javax.swing.Icon

object RescriptPsiUtils {
    val NAVIGABLE_TYPES: Set<IElementType> =
        setOf(
            RescriptElementTypes.LET_DECLARATION,
            RescriptElementTypes.TYPE_DECLARATION,
            RescriptElementTypes.MODULE_DECLARATION,
            RescriptElementTypes.EXTERNAL_DECLARATION,
            RescriptElementTypes.EXCEPTION_DECLARATION,
        )

    private val IDENTIFIER_TYPES: Set<IElementType> =
        setOf(
            RescriptTokenTypes.LIDENT,
            RescriptTokenTypes.UIDENT,
            RescriptTokenTypes.UNDERSCORE,
        )

    fun extractName(element: PsiElement): String {
        if (element is RescriptFile) return element.name

        val node = element.node ?: return "(unknown)"
        var child = node.firstChildNode
        var afterKeyword = false
        while (child != null) {
            if (RescriptTokenTypes.TOP_LEVEL_KEYWORDS.contains(child.elementType)) {
                afterKeyword = true
            } else if (child.elementType == RescriptTokenTypes.REC) {
                // skip 'rec' keyword
            } else if (afterKeyword && child.elementType in IDENTIFIER_TYPES) {
                return child.text
            }
            child = child.treeNext
        }
        return "(anonymous)"
    }

    fun getIcon(element: PsiElement): Icon? {
        if (element is RescriptFile) return RescriptIcons.FILE

        return when (element.node?.elementType) {
            RescriptElementTypes.LET_DECLARATION -> AllIcons.Nodes.Function
            RescriptElementTypes.TYPE_DECLARATION -> AllIcons.Nodes.Type
            RescriptElementTypes.MODULE_DECLARATION -> AllIcons.Nodes.Module
            RescriptElementTypes.EXTERNAL_DECLARATION -> AllIcons.Nodes.PluginJB
            RescriptElementTypes.EXCEPTION_DECLARATION -> AllIcons.Nodes.ExceptionClass
            else -> null
        }
    }

    fun getElementDescription(element: PsiElement): String? =
        when (element.node?.elementType) {
            RescriptElementTypes.LET_DECLARATION -> "let declaration"
            RescriptElementTypes.TYPE_DECLARATION -> "type declaration"
            RescriptElementTypes.MODULE_DECLARATION -> "module declaration"
            RescriptElementTypes.EXTERNAL_DECLARATION -> "external declaration"
            RescriptElementTypes.EXCEPTION_DECLARATION -> "exception declaration"
            else -> null
        }
}
