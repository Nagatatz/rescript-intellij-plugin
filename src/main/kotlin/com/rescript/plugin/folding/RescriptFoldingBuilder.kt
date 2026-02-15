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
            if (node.elementType in
                setOf(
                    RescriptElementTypes.MODULE_DECLARATION,
                    RescriptElementTypes.LET_DECLARATION,
                    RescriptElementTypes.TYPE_DECLARATION,
                )
            ) {
                val text = node.text
                val startLine = document.getLineNumber(node.startOffset)
                val endLine = document.getLineNumber(node.startOffset + node.textLength)
                if (endLine > startLine && text.contains('{')) {
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
            else -> "{...}"
        }

    override fun isCollapsedByDefault(node: ASTNode): Boolean = false
}
