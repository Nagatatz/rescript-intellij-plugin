package com.rescript.plugin.lsp

import com.rescript.plugin.highlight.RescriptSyntaxHighlighter
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class RescriptSemanticTokensSupportTest {
    private val support = RescriptSemanticTokensSupport()

    @Test
    fun `variable token maps to SEMANTIC_VARIABLE`() {
        assertEquals(
            RescriptSyntaxHighlighter.SEMANTIC_VARIABLE,
            support.getTextAttributesKey("variable", emptyList()),
        )
    }

    @Test
    fun `type token maps to SEMANTIC_TYPE`() {
        assertEquals(
            RescriptSyntaxHighlighter.SEMANTIC_TYPE,
            support.getTextAttributesKey("type", emptyList()),
        )
    }

    @Test
    fun `namespace token maps to SEMANTIC_NAMESPACE`() {
        assertEquals(
            RescriptSyntaxHighlighter.SEMANTIC_NAMESPACE,
            support.getTextAttributesKey("namespace", emptyList()),
        )
    }

    @Test
    fun `enumMember token maps to SEMANTIC_ENUM_MEMBER`() {
        assertEquals(
            RescriptSyntaxHighlighter.SEMANTIC_ENUM_MEMBER,
            support.getTextAttributesKey("enumMember", emptyList()),
        )
    }

    @Test
    fun `property token maps to SEMANTIC_PROPERTY`() {
        assertEquals(
            RescriptSyntaxHighlighter.SEMANTIC_PROPERTY,
            support.getTextAttributesKey("property", emptyList()),
        )
    }

    @Test
    fun `interface token maps to SEMANTIC_INTERFACE`() {
        assertEquals(
            RescriptSyntaxHighlighter.SEMANTIC_INTERFACE,
            support.getTextAttributesKey("interface", emptyList()),
        )
    }

    @Test
    fun `operator token maps to SEMANTIC_OPERATOR`() {
        assertEquals(
            RescriptSyntaxHighlighter.SEMANTIC_OPERATOR,
            support.getTextAttributesKey("operator", emptyList()),
        )
    }

    @Test
    fun `modifier token maps to SEMANTIC_MODIFIER`() {
        assertEquals(
            RescriptSyntaxHighlighter.SEMANTIC_MODIFIER,
            support.getTextAttributesKey("modifier", emptyList()),
        )
    }

    @Test
    fun `unknown token returns null`() {
        assertNull(support.getTextAttributesKey("unknown", emptyList()))
    }

    @Test
    fun `empty token returns null`() {
        assertNull(support.getTextAttributesKey("", emptyList()))
    }

    @Test
    fun `modifiers parameter does not affect result`() {
        assertEquals(
            RescriptSyntaxHighlighter.SEMANTIC_VARIABLE,
            support.getTextAttributesKey("variable", listOf("declaration", "readonly")),
        )
    }
}
