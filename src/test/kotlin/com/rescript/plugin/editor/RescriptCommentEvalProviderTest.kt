package com.rescript.plugin.editor

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RescriptCommentEvalProviderTest {
    @Test
    fun `findCodeExamples finds example block`() {
        val source =
            """
            /**
             * @example
             * let x = 42
             * Js.log(x)
             */
            let add = (a, b) => a + b
            """.trimIndent()
        val examples = RescriptCommentEvalProvider.findCodeExamples(source)
        assertEquals(1, examples.size)
        assertTrue(examples[0].code.contains("let x = 42"))
    }

    @Test
    fun `findCodeExamples finds code fence`() {
        val source =
            """
            /**
             * ```rescript
             * let x = 42
             * ```
             */
            let add = (a, b) => a + b
            """.trimIndent()
        val examples = RescriptCommentEvalProvider.findCodeExamples(source)
        assertEquals(1, examples.size)
    }

    @Test
    fun `findCodeExamples returns empty for no doc comments`() {
        val source = "let x = 42"
        val examples = RescriptCommentEvalProvider.findCodeExamples(source)
        assertTrue(examples.isEmpty())
    }

    @Test
    fun `findCodeExamples returns empty for doc comments without examples`() {
        val source =
            """
            /**
             * Simple function that adds two numbers.
             */
            let add = (a, b) => a + b
            """.trimIndent()
        val examples = RescriptCommentEvalProvider.findCodeExamples(source)
        assertTrue(examples.isEmpty())
    }

    @Test
    fun `validateSyntax accepts valid ReScript`() {
        assertTrue(RescriptCommentEvalProvider.validateSyntax("let x = 42"))
    }

    @Test
    fun `validateSyntax accepts pipe expression`() {
        assertTrue(RescriptCommentEvalProvider.validateSyntax("arr->Array.map(x => x + 1)"))
    }

    @Test
    fun `validateSyntax accepts switch expression`() {
        val code =
            """
            switch x {
            | Some(v) => v
            | None => 0
            }
            """.trimIndent()
        assertTrue(RescriptCommentEvalProvider.validateSyntax(code))
    }

    @Test
    fun `validateSyntax rejects JavaScript`() {
        assertFalse(RescriptCommentEvalProvider.validateSyntax("const x = 42;"))
    }

    @Test
    fun `validateSyntax rejects empty code`() {
        assertFalse(RescriptCommentEvalProvider.validateSyntax(""))
    }

    @Test
    fun `validateSyntax rejects unbalanced braces`() {
        assertFalse(RescriptCommentEvalProvider.validateSyntax("let x = {"))
    }

    @Test
    fun `cleanExampleCode removes star prefixes`() {
        val raw = " * let x = 42\n * Js.log(x)"
        val cleaned = RescriptCommentEvalProvider.cleanExampleCode(raw)
        assertTrue(cleaned.contains("let x = 42"))
        assertTrue(cleaned.contains("Js.log(x)"))
        assertFalse(cleaned.contains("*"))
    }

    @Test
    fun `cleanExampleCode trims whitespace`() {
        val raw = "   let x = 42   "
        val cleaned = RescriptCommentEvalProvider.cleanExampleCode(raw)
        assertEquals("let x = 42", cleaned)
    }

    @Test
    fun `CodeExample data class holds values`() {
        val example = RescriptCommentEvalProvider.Companion.CodeExample("let x = 42", 10, 22)
        assertEquals("let x = 42", example.code)
        assertEquals(10, example.startOffset)
        assertEquals(22, example.endOffset)
    }
}
