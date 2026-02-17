package com.rescript.plugin.folding

import com.intellij.lang.ASTNode
import com.intellij.lang.folding.FoldingBuilderEx
import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.openapi.editor.Document
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.rescript.plugin.lang.RescriptTokenTypes
import com.rescript.plugin.lang.psi.RescriptElementTypes

class RescriptFoldingBuilder : FoldingBuilderEx() {
    companion object {
        private val FOLDABLE_DECLARATION_TYPES =
            setOf(
                RescriptElementTypes.MODULE_DECLARATION,
                RescriptElementTypes.LET_DECLARATION,
                RescriptElementTypes.TYPE_DECLARATION,
            )
        private val FOLDABLE_JSX_TYPES =
            setOf(
                RescriptElementTypes.JSX_ELEMENT,
                RescriptElementTypes.JSX_FRAGMENT,
            )
    }

    override fun buildFoldRegions(
        root: PsiElement,
        document: Document,
        quick: Boolean,
    ): Array<FoldingDescriptor> {
        val descriptors = mutableListOf<FoldingDescriptor>()

        PsiTreeUtil.findChildrenOfAnyType(root, PsiElement::class.java).forEach { element ->
            val node = element.node ?: return@forEach

            // Fold block comments
            if (node.elementType == RescriptTokenTypes.MULTI_COMMENT && node.textLength > 4) {
                descriptors += FoldingDescriptor(node, node.textRange)
            }

            // Fold module / let / type declarations that contain braces
            if (node.elementType in FOLDABLE_DECLARATION_TYPES) {
                val text = node.text
                val startLine = document.getLineNumber(node.startOffset)
                val endLine = document.getLineNumber(node.startOffset + node.textLength)
                if (endLine > startLine && text.contains('{')) {
                    descriptors += FoldingDescriptor(node, node.textRange)
                }
            }

            // Fold multi-line JSX elements and fragments
            if (node.elementType in FOLDABLE_JSX_TYPES) {
                val startLine = document.getLineNumber(node.startOffset)
                val endLine = document.getLineNumber(node.startOffset + node.textLength)
                if (endLine > startLine) {
                    descriptors += FoldingDescriptor(node, node.textRange)
                }
            }
        }

        return descriptors.toTypedArray()
    }

    override fun getPlaceholderText(node: ASTNode): String =
        when (node.elementType) {
            RescriptTokenTypes.MULTI_COMMENT -> "/* ... */"
            RescriptElementTypes.MODULE_DECLARATION -> "module ... { ... }"
            RescriptElementTypes.JSX_ELEMENT -> {
                val tagName = extractJsxTagName(node)
                "<$tagName>...</$tagName>"
            }
            RescriptElementTypes.JSX_FRAGMENT -> "<>...</>"
            else -> "{...}"
        }

    private fun extractJsxTagName(node: ASTNode): String {
        val parts = mutableListOf<String>()
        var child = node.firstChildNode
        // Skip TAG_LT, then collect tag name parts (name.name.name)
        while (child != null) {
            when (child.elementType) {
                RescriptTokenTypes.TAG_LT -> { /* skip */ }
                RescriptTokenTypes.JSX_TAG_NAME, RescriptTokenTypes.JSX_COMPONENT_NAME -> {
                    parts.add(child.text)
                }
                RescriptTokenTypes.DOT -> { /* skip, will be added as separator */ }
                else -> break
            }
            child = child.treeNext
        }
        return if (parts.isNotEmpty()) parts.joinToString(".") else "..."
    }

    override fun isCollapsedByDefault(node: ASTNode): Boolean = false
}
