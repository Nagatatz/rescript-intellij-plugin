package com.rescript.plugin.folding

import com.intellij.lang.ASTNode
import com.intellij.psi.tree.IElementType
import com.rescript.plugin.lang.RescriptTokenTypes
import com.rescript.plugin.lang.psi.RescriptElementTypes
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class RescriptFoldingBuilderTest {
    private val builder = RescriptFoldingBuilder()

    /** Minimal ASTNode stub that only exposes elementType. */
    private fun stubNode(type: IElementType): ASTNode =
        java.lang.reflect.Proxy.newProxyInstance(
            ASTNode::class.java.classLoader,
            arrayOf(ASTNode::class.java),
        ) { _, method, _ ->
            when (method.name) {
                "getElementType" -> type
                "toString" -> "StubASTNode($type)"
                "hashCode" -> System.identityHashCode(type)
                "equals" -> false
                else -> null
            }
        } as ASTNode

    @Test
    fun testIsCollapsedByDefaultReturnsFalse() {
        assertFalse(builder.isCollapsedByDefault(stubNode(RescriptTokenTypes.MULTI_COMMENT)))
        assertFalse(builder.isCollapsedByDefault(stubNode(RescriptElementTypes.MODULE_DECLARATION)))
        assertFalse(builder.isCollapsedByDefault(stubNode(RescriptElementTypes.LET_DECLARATION)))
    }

    @Test
    fun testGetPlaceholderTextForMultiComment() {
        assertEquals("/* ... */", builder.getPlaceholderText(stubNode(RescriptTokenTypes.MULTI_COMMENT)))
    }

    @Test
    fun testGetPlaceholderTextForModuleDeclaration() {
        assertEquals(
            "module ... { ... }",
            builder.getPlaceholderText(stubNode(RescriptElementTypes.MODULE_DECLARATION)),
        )
    }

    @Test
    fun testGetPlaceholderTextForOtherElements() {
        assertEquals("{...}", builder.getPlaceholderText(stubNode(RescriptElementTypes.LET_DECLARATION)))
        assertEquals("{...}", builder.getPlaceholderText(stubNode(RescriptElementTypes.TYPE_DECLARATION)))
    }
}
