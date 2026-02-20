package com.rescript.plugin.lsp

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RescriptExpressionTypeProviderTest {
    @Test
    fun `instance can be created`() {
        val provider = RescriptExpressionTypeProvider()
        assertNotNull(provider)
    }

    @Test
    fun `is an ExpressionTypeProvider`() {
        val provider = RescriptExpressionTypeProvider()
        assertTrue(provider is com.intellij.lang.ExpressionTypeProvider<*>)
    }

    @Test
    fun `getErrorHint returns expected message`() {
        val provider = RescriptExpressionTypeProvider()
        assertEquals("No type information available", provider.errorHint)
    }

    // -- extractTypeFromMarkdown tests --

    @Test
    fun `extractTypeFromMarkdown extracts type from code fence`() {
        val markdown = "```rescript\nint\n```"
        val result = RescriptExpressionTypeProvider.extractTypeFromMarkdown(markdown)
        assertEquals("int", result)
    }

    @Test
    fun `extractTypeFromMarkdown extracts type from code fence without language`() {
        val markdown = "```\nstring\n```"
        val result = RescriptExpressionTypeProvider.extractTypeFromMarkdown(markdown)
        assertEquals("string", result)
    }

    @Test
    fun `extractTypeFromMarkdown extracts multiline type from code fence`() {
        val markdown = "```rescript\narray<int> => option<string>\n```"
        val result = RescriptExpressionTypeProvider.extractTypeFromMarkdown(markdown)
        assertEquals("array<int> => option<string>", result)
    }

    @Test
    fun `extractTypeFromMarkdown extracts type from inline code`() {
        val markdown = "Type: `float`"
        val result = RescriptExpressionTypeProvider.extractTypeFromMarkdown(markdown)
        assertEquals("float", result)
    }

    @Test
    fun `extractTypeFromMarkdown returns trimmed plain text when no code markers`() {
        val markdown = "  bool  "
        val result = RescriptExpressionTypeProvider.extractTypeFromMarkdown(markdown)
        assertEquals("bool", result)
    }

    @Test
    fun `extractTypeFromMarkdown returns plain text for empty string`() {
        val result = RescriptExpressionTypeProvider.extractTypeFromMarkdown("")
        assertEquals("", result)
    }

    @Test
    fun `extractTypeFromMarkdown handles code fence with spaces after backticks`() {
        val markdown = "```rescript\n  int  \n```"
        val result = RescriptExpressionTypeProvider.extractTypeFromMarkdown(markdown)
        assertEquals("int", result)
    }

    @Test
    fun `extractTypeFromMarkdown prefers code fence over inline code`() {
        val markdown = "`inline` text\n```rescript\nfence_type\n```"
        val result = RescriptExpressionTypeProvider.extractTypeFromMarkdown(markdown)
        assertEquals("fence_type", result)
    }

    @Test
    fun `extractTypeFromMarkdown extracts first inline code occurrence`() {
        val markdown = "See `option<int>` and `string`"
        val result = RescriptExpressionTypeProvider.extractTypeFromMarkdown(markdown)
        assertEquals("option<int>", result)
    }

    @Test
    fun `extractTypeFromMarkdown handles complex function type in code fence`() {
        val markdown = "```rescript\n(~name: string, ~age: int) => person\n```"
        val result = RescriptExpressionTypeProvider.extractTypeFromMarkdown(markdown)
        assertEquals("(~name: string, ~age: int) => person", result)
    }
}
