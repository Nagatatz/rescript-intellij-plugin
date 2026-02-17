package com.rescript.plugin.lang

import com.intellij.lang.ASTFactory
import com.intellij.psi.impl.source.tree.LeafElement
import com.intellij.psi.tree.IElementType
import com.rescript.plugin.lang.psi.RescriptStringLiteral

/**
 * Custom AST factory that creates [RescriptStringLiteral] for STRING_VALUE tokens,
 * enabling language injection support (e.g., JavaScript in `%raw()` blocks).
 */
class RescriptAstFactory : ASTFactory() {
    override fun createLeaf(
        type: IElementType,
        text: CharSequence,
    ): LeafElement? {
        if (type == RescriptTokenTypes.STRING_VALUE) {
            return RescriptStringLiteral(type, text)
        }
        return null
    }
}
