package com.rescript.plugin.worksheet

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class RescriptWorksheetRunnerTest {
    @Test
    fun `extractExpressions finds let bindings`() {
        val source =
            """
            let x = 42
            let y = x + 1
            """.trimIndent()
        val exprs = RescriptWorksheetRunner.extractExpressions(source)
        assertTrue(exprs.isNotEmpty())
    }

    @Test
    fun `extractExpressions skips empty lines`() {
        val source =
            """
            let x = 42

            let y = 1
            """.trimIndent()
        val exprs = RescriptWorksheetRunner.extractExpressions(source)
        assertEquals(2, exprs.size)
    }

    @Test
    fun `extractExpressions skips comments`() {
        val source =
            """
            // this is a comment
            let x = 42
            """.trimIndent()
        val exprs = RescriptWorksheetRunner.extractExpressions(source)
        assertEquals(1, exprs.size)
    }

    @Test
    fun `extractExpressions skips type declarations`() {
        val source =
            """
            type t = int
            let x = 42
            """.trimIndent()
        val exprs = RescriptWorksheetRunner.extractExpressions(source)
        assertEquals(1, exprs.size)
        assertTrue(exprs[0].code.contains("let x"))
    }

    @Test
    fun `extractExpressions skips open statements`() {
        val source =
            """
            open Belt
            let x = 42
            """.trimIndent()
        val exprs = RescriptWorksheetRunner.extractExpressions(source)
        assertEquals(1, exprs.size)
    }

    @Test
    fun `buildEvalScript wraps code`() {
        val result = RescriptWorksheetRunner.buildEvalScript("let x = 42")
        assertTrue(result.contains("let x = 42"))
        assertTrue(result.contains("Js.log"))
    }

    @Test
    fun `buildEvalScript does not double log`() {
        val result = RescriptWorksheetRunner.buildEvalScript("Js.log(42)")
        val logCount = Regex("Js\\.log").findAll(result).count()
        assertEquals(1, logCount)
    }

    @Test
    fun `formatResult shows empty unit result`() {
        val result = RescriptWorksheetRunner.formatResult("let x = 42", "")
        assertEquals("// => ()", result)
    }

    @Test
    fun `formatResult shows output value`() {
        val result = RescriptWorksheetRunner.formatResult("let x = 42", "42")
        assertEquals("// => 42", result)
    }

    @Test
    fun `formatResult trims whitespace`() {
        val result = RescriptWorksheetRunner.formatResult("let x = 42", "  42  \n")
        assertEquals("// => 42", result)
    }

    @Test
    fun `WorksheetExpression data class holds values`() {
        val expr = RescriptWorksheetRunner.Companion.WorksheetExpression("let x = 42", 1, 1)
        assertEquals("let x = 42", expr.code)
        assertEquals(1, expr.lineNumber)
        assertEquals(1, expr.endLineNumber)
    }

    @Test
    fun `extractExpressions returns empty for empty file`() {
        val exprs = RescriptWorksheetRunner.extractExpressions("")
        assertTrue(exprs.isEmpty())
    }
}
