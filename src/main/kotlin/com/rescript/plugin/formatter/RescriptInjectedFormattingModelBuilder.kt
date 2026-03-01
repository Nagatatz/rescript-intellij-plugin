package com.rescript.plugin.formatter

import com.intellij.formatting.Alignment
import com.intellij.formatting.Block
import com.intellij.formatting.ChildAttributes
import com.intellij.formatting.FormattingContext
import com.intellij.formatting.FormattingModel
import com.intellij.formatting.FormattingModelBuilder
import com.intellij.formatting.FormattingModelProvider
import com.intellij.formatting.Indent
import com.intellij.formatting.Spacing
import com.intellij.formatting.Wrap
import com.intellij.lang.ASTNode
import com.intellij.openapi.util.TextRange

/**
 * Provides a formatting model that enables injected language formatting in ReScript files.
 *
 * Creates a passthrough block structure mirroring the AST so that the IntelliJ Platform
 * can locate injection hosts (e.g., JavaScript inside `%raw()`) and delegate their
 * formatting to the respective language's formatter. This works independently of
 * [RescriptFormattingService], which handles the external `rescript format` CLI.
 * Implements [FormattingModelBuilder].
 */
class RescriptInjectedFormattingModelBuilder : FormattingModelBuilder {
    override fun createModel(formattingContext: FormattingContext): FormattingModel {
        val element = formattingContext.psiElement
        val block = RescriptPassthroughBlock(element.node)
        return FormattingModelProvider.createFormattingModelForPsiFile(
            element.containingFile,
            block,
            formattingContext.codeStyleSettings,
        )
    }
}

/**
 * A minimal formatting block that mirrors the AST structure without imposing
 * any formatting rules, allowing injected language formatters to operate.
 */
private class RescriptPassthroughBlock(
    private val node: ASTNode,
) : Block {
    private val children: List<Block> by lazy {
        val result = mutableListOf<Block>()
        var child = node.firstChildNode
        while (child != null) {
            result.add(RescriptPassthroughBlock(child))
            child = child.treeNext
        }
        result
    }

    override fun getTextRange(): TextRange = node.textRange

    override fun getSubBlocks(): List<Block> = children

    override fun getWrap(): Wrap? = null

    override fun getIndent(): Indent? = Indent.getNoneIndent()

    override fun getAlignment(): Alignment? = null

    override fun getSpacing(
        child1: Block?,
        child2: Block,
    ): Spacing? = null

    override fun getChildAttributes(newChildIndex: Int): ChildAttributes = ChildAttributes(Indent.getNoneIndent(), null)

    override fun isIncomplete(): Boolean = false

    override fun isLeaf(): Boolean = node.firstChildNode == null
}
