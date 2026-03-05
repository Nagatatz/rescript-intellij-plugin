package com.rescript.plugin.quickfix

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/** Tests for [RescriptGenerateFunctionQuickFix] static helper methods. */
class RescriptGenerateFunctionQuickFixTest {
    @Test
    fun `countArguments returns correct count for simple args`() {
        val text = "foo(1, 2, 3)"
        assertEquals(3, RescriptGenerateFunctionQuickFix.countArguments(text, 3))
    }

    @Test
    fun `countArguments returns 0 for empty parens`() {
        val text = "foo()"
        assertEquals(0, RescriptGenerateFunctionQuickFix.countArguments(text, 3))
    }

    @Test
    fun `countArguments returns 1 for single arg`() {
        val text = "foo(x)"
        assertEquals(1, RescriptGenerateFunctionQuickFix.countArguments(text, 3))
    }

    @Test
    fun `countArguments handles nested parens`() {
        val text = "foo(bar(1, 2), 3)"
        assertEquals(2, RescriptGenerateFunctionQuickFix.countArguments(text, 3))
    }

    @Test
    fun `countArguments returns 0 when no paren found`() {
        val text = "foo"
        assertEquals(0, RescriptGenerateFunctionQuickFix.countArguments(text, 3))
    }

    @Test
    fun `generateStubFunction with no args`() {
        assertEquals("let foo = () => todo", RescriptGenerateFunctionQuickFix.generateStubFunction("foo", 0))
    }

    @Test
    fun `generateStubFunction with one arg`() {
        assertEquals("let bar = (arg1) => todo", RescriptGenerateFunctionQuickFix.generateStubFunction("bar", 1))
    }

    @Test
    fun `generateStubFunction with multiple args`() {
        assertEquals(
            "let baz = (arg1, arg2, arg3) => todo",
            RescriptGenerateFunctionQuickFix.generateStubFunction("baz", 3),
        )
    }

    @Test
    fun `findTopLevelDeclarationStart finds let declaration`() {
        val text =
            """
            let x = 42
            let y = foo(bar)
            """.trimIndent()
        val offset = text.indexOf("foo")
        val result = RescriptGenerateFunctionQuickFix.findTopLevelDeclarationStart(text, offset)
        assertEquals(text.indexOf("let y"), result)
    }

    @Test
    fun `findTopLevelDeclarationStart returns 0 at file start`() {
        val text = "let x = foo()"
        val result = RescriptGenerateFunctionQuickFix.findTopLevelDeclarationStart(text, 8)
        assertEquals(0, result)
    }

    @Test
    fun `findTopLevelDeclarationStart finds type declaration`() {
        val text =
            """
            type t = A | B
            let x = foo(42)
            """.trimIndent()
        val offset = text.indexOf("foo")
        val result = RescriptGenerateFunctionQuickFix.findTopLevelDeclarationStart(text, offset)
        assertEquals(text.indexOf("let x"), result)
    }
}
