package com.rescript.plugin.folding

import com.intellij.lang.ASTNode
import com.intellij.openapi.util.TextRange
import com.intellij.psi.tree.IElementType
import com.rescript.plugin.lang.RescriptTokenTypes
import com.rescript.plugin.lang.psi.RescriptElementTypes
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RescriptFoldingBuilderTest {
    private val builder = RescriptFoldingBuilder()

    /** Minimal ASTNode stub that only exposes elementType and textRange. */
    private fun stubNode(type: IElementType): ASTNode =
        java.lang.reflect.Proxy.newProxyInstance(
            ASTNode::class.java.classLoader,
            arrayOf(ASTNode::class.java),
        ) { _, method, _ ->
            when (method.name) {
                "getElementType" -> type
                "getTextRange" -> TextRange(0, 10)
                "toString" -> "StubASTNode($type)"
                "hashCode" -> System.identityHashCode(type)
                "equals" -> false
                else -> null
            }
        } as ASTNode

    @Test
    fun testIsRegionCollapsedByDefaultReturnsFalse() {
        assertFalse(builder.isRegionCollapsedByDefault(stubNode(RescriptTokenTypes.MULTI_COMMENT)))
        assertFalse(builder.isRegionCollapsedByDefault(stubNode(RescriptElementTypes.MODULE_DECLARATION)))
        assertFalse(builder.isRegionCollapsedByDefault(stubNode(RescriptElementTypes.LET_DECLARATION)))
    }

    @Test
    fun testGetLanguagePlaceholderTextForMultiComment() {
        val node = stubNode(RescriptTokenTypes.MULTI_COMMENT)
        assertEquals("/* ... */", builder.getLanguagePlaceholderText(node, node.textRange))
    }

    @Test
    fun testGetLanguagePlaceholderTextForModuleDeclaration() {
        val node = stubNode(RescriptElementTypes.MODULE_DECLARATION)
        assertEquals(
            "module ... { ... }",
            builder.getLanguagePlaceholderText(node, node.textRange),
        )
    }

    @Test
    fun testGetLanguagePlaceholderTextForOtherElements() {
        val letNode = stubNode(RescriptElementTypes.LET_DECLARATION)
        assertEquals("{...}", builder.getLanguagePlaceholderText(letNode, letNode.textRange))
        val typeNode = stubNode(RescriptElementTypes.TYPE_DECLARATION)
        assertEquals("{...}", builder.getLanguagePlaceholderText(typeNode, typeNode.textRange))
    }

    @Test
    fun testGetLanguagePlaceholderTextForJsxFragment() {
        val node = stubNode(RescriptElementTypes.JSX_FRAGMENT)
        assertEquals("<>...</>", builder.getLanguagePlaceholderText(node, node.textRange))
    }

    @Test
    fun testIsCustomFoldingCandidateForSingleComment() {
        assertTrue(builder.isCustomFoldingCandidate(stubNode(RescriptTokenTypes.SINGLE_COMMENT)))
    }

    @Test
    fun testIsCustomFoldingCandidateForNonComment() {
        assertFalse(builder.isCustomFoldingCandidate(stubNode(RescriptTokenTypes.MULTI_COMMENT)))
        assertFalse(builder.isCustomFoldingCandidate(stubNode(RescriptElementTypes.LET_DECLARATION)))
    }
}
