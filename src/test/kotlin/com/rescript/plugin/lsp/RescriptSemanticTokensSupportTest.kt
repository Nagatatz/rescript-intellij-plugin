package com.rescript.plugin.lsp

import com.rescript.plugin.highlight.RescriptSyntaxHighlighter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RescriptSemanticTokensSupportTest {
    private val support = RescriptSemanticTokensSupport()

    @Test
    fun `variable maps to SEMANTIC_VARIABLE`() {
        assertEquals(
            RescriptSyntaxHighlighter.SEMANTIC_VARIABLE,
            support.getTextAttributesKey("variable", emptyList()),
        )
    }

    @Test
    fun `type maps to SEMANTIC_TYPE`() {
        assertEquals(
            RescriptSyntaxHighlighter.SEMANTIC_TYPE,
            support.getTextAttributesKey("type", emptyList()),
        )
    }

    @Test
    fun `namespace maps to SEMANTIC_NAMESPACE`() {
        assertEquals(
            RescriptSyntaxHighlighter.SEMANTIC_NAMESPACE,
            support.getTextAttributesKey("namespace", emptyList()),
        )
    }

    @Test
    fun `enumMember maps to SEMANTIC_ENUM_MEMBER`() {
        assertEquals(
            RescriptSyntaxHighlighter.SEMANTIC_ENUM_MEMBER,
            support.getTextAttributesKey("enumMember", emptyList()),
        )
    }

    @Test
    fun `property maps to SEMANTIC_PROPERTY`() {
        assertEquals(
            RescriptSyntaxHighlighter.SEMANTIC_PROPERTY,
            support.getTextAttributesKey("property", emptyList()),
        )
    }

    @Test
    fun `interface maps to SEMANTIC_INTERFACE`() {
        assertEquals(
            RescriptSyntaxHighlighter.SEMANTIC_INTERFACE,
            support.getTextAttributesKey("interface", emptyList()),
        )
    }

    @Test
    fun `operator maps to SEMANTIC_OPERATOR`() {
        assertEquals(
            RescriptSyntaxHighlighter.SEMANTIC_OPERATOR,
            support.getTextAttributesKey("operator", emptyList()),
        )
    }

    @Test
    fun `modifier maps to SEMANTIC_MODIFIER`() {
        assertEquals(
            RescriptSyntaxHighlighter.SEMANTIC_MODIFIER,
            support.getTextAttributesKey("modifier", emptyList()),
        )
    }

    @Test
    fun `unknown token type returns null`() {
        assertNull(support.getTextAttributesKey("unknownTokenType", emptyList()))
    }

    @Test
    fun `modifiers parameter does not affect mapping`() {
        assertEquals(
            RescriptSyntaxHighlighter.SEMANTIC_VARIABLE,
            support.getTextAttributesKey("variable", listOf("declaration", "readonly")),
        )
    }
}
