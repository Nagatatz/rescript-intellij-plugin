package com.rescript.plugin.refactor

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class RescriptExtractVariableUtilTest {
    // -- suggestVariableName tests --

    @Test
    fun `suggestVariableName - simple function call`() {
        assertEquals("funcResult", RescriptExtractVariableUtil.suggestVariableName("func(a)"))
    }

    @Test
    fun `suggestVariableName - qualified function call`() {
        assertEquals("mapResult", RescriptExtractVariableUtil.suggestVariableName("Array.map(f)"))
    }

    @Test
    fun `suggestVariableName - deeply qualified function call`() {
        assertEquals("mapResult", RescriptExtractVariableUtil.suggestVariableName("Belt.Array.map(f)"))
    }

    @Test
    fun `suggestVariableName - pipe chain with args`() {
        assertEquals("mapResult", RescriptExtractVariableUtil.suggestVariableName("data->Array.map(f)"))
    }

    @Test
    fun `suggestVariableName - pipe chain without args`() {
        assertEquals("lengthResult", RescriptExtractVariableUtil.suggestVariableName("data->Array.length"))
    }

    @Test
    fun `suggestVariableName - long pipe chain`() {
        assertEquals(
            "filterResult",
            RescriptExtractVariableUtil.suggestVariableName("data->Array.map(x => x + 1)->Array.filter(x => x > 0)"),
        )
    }

    @Test
    fun `suggestVariableName - simple identifier`() {
        assertEquals("foo", RescriptExtractVariableUtil.suggestVariableName("foo"))
    }

    @Test
    fun `suggestVariableName - identifier with underscore`() {
        assertEquals("my_var", RescriptExtractVariableUtil.suggestVariableName("my_var"))
    }

    @Test
    fun `suggestVariableName - numeric literal`() {
        assertEquals("extracted", RescriptExtractVariableUtil.suggestVariableName("42"))
    }

    @Test
    fun `suggestVariableName - binary expression`() {
        assertEquals("extracted", RescriptExtractVariableUtil.suggestVariableName("a + b"))
    }

    @Test
    fun `suggestVariableName - string literal`() {
        assertEquals("extracted", RescriptExtractVariableUtil.suggestVariableName("\"hello\""))
    }

    @Test
    fun `suggestVariableName - keyword returns extracted`() {
        assertEquals("extracted", RescriptExtractVariableUtil.suggestVariableName("let"))
    }

    @Test
    fun `suggestVariableName - trims whitespace`() {
        assertEquals("funcResult", RescriptExtractVariableUtil.suggestVariableName("  func(a)  "))
    }

    @Test
    fun `suggestVariableName - function call no args`() {
        assertEquals("funcResult", RescriptExtractVariableUtil.suggestVariableName("func()"))
    }

    // -- getIndentation tests --

    @Test
    fun `getIndentation - no indent`() {
        assertEquals("", RescriptExtractVariableUtil.getIndentation("let x = 1"))
    }

    @Test
    fun `getIndentation - two spaces`() {
        assertEquals("  ", RescriptExtractVariableUtil.getIndentation("  let x = 1"))
    }

    @Test
    fun `getIndentation - four spaces`() {
        assertEquals("    ", RescriptExtractVariableUtil.getIndentation("    let x = 1"))
    }

    @Test
    fun `getIndentation - tab`() {
        assertEquals("\t", RescriptExtractVariableUtil.getIndentation("\tlet x = 1"))
    }

    @Test
    fun `getIndentation - empty line`() {
        assertEquals("", RescriptExtractVariableUtil.getIndentation(""))
    }

    @Test
    fun `getIndentation - only spaces`() {
        assertEquals("   ", RescriptExtractVariableUtil.getIndentation("   "))
    }

    // -- buildLetBinding tests --

    @Test
    fun `buildLetBinding - no indent`() {
        assertEquals("let name = expr\n", RescriptExtractVariableUtil.buildLetBinding("name", "expr", ""))
    }

    @Test
    fun `buildLetBinding - with indent`() {
        assertEquals("  let name = expr\n", RescriptExtractVariableUtil.buildLetBinding("name", "expr", "  "))
    }

    @Test
    fun `buildLetBinding - complex expression`() {
        assertEquals(
            "let result = Array.map(x => x + 1)\n",
            RescriptExtractVariableUtil.buildLetBinding("result", "Array.map(x => x + 1)", ""),
        )
    }

    // -- findStatementStartLine tests --

    @Test
    fun `findStatementStartLine - top-level let`() {
        val text = "let result = someFunc(complexExpr(a, b) + other)"
        // Selection at offset 22 (inside "complexExpr")
        assertEquals(0, RescriptExtractVariableUtil.findStatementStartLine(text, 22))
    }

    @Test
    fun `findStatementStartLine - let in block`() {
        val text = "let doSomething = () => {\n  let x = foo(bar(1, 2))\n}"
        // Selection at offset 40 (inside "bar(1, 2)")
        assertEquals(1, RescriptExtractVariableUtil.findStatementStartLine(text, 40))
    }

    @Test
    fun `findStatementStartLine - nested block with arrow`() {
        val text = "let f = () => {\n  let a = 1\n  let b = a + 2\n}"
        // Selection at "a + 2" on line 2 (offset ~37)
        assertEquals(2, RescriptExtractVariableUtil.findStatementStartLine(text, 37))
    }

    @Test
    fun `findStatementStartLine - empty document`() {
        assertEquals(0, RescriptExtractVariableUtil.findStatementStartLine("", 0))
    }

    @Test
    fun `findStatementStartLine - single line`() {
        val text = "let x = 42"
        assertEquals(0, RescriptExtractVariableUtil.findStatementStartLine(text, 8))
    }

    @Test
    fun `findStatementStartLine - multiple top-level lets`() {
        val text = "let a = 1\nlet b = 2\nlet c = 3"
        // Selection at "2" on line 1
        assertEquals(1, RescriptExtractVariableUtil.findStatementStartLine(text, 15))
    }

    @Test
    fun `findStatementStartLine - type declaration`() {
        val text = "type t = {\n  name: string,\n  age: int,\n}"
        // Selection on "string" (line 1)
        assertEquals(0, RescriptExtractVariableUtil.findStatementStartLine(text, 20))
    }

    @Test
    fun `findStatementStartLine - module declaration`() {
        val text = "module M = {\n  let x = 1\n}"
        // Selection on "1" inside module (line 1)
        assertEquals(1, RescriptExtractVariableUtil.findStatementStartLine(text, 22))
    }

    // -- validateName tests --

    @Test
    fun `validateName - valid name`() {
        assertEquals("fooResult", RescriptExtractVariableUtil.validateName("fooResult"))
    }

    @Test
    fun `validateName - keyword`() {
        assertEquals("extracted", RescriptExtractVariableUtil.validateName("let"))
    }

    @Test
    fun `validateName - empty`() {
        assertEquals("extracted", RescriptExtractVariableUtil.validateName(""))
    }

    @Test
    fun `validateName - starts with digit`() {
        assertEquals("extracted", RescriptExtractVariableUtil.validateName("1abc"))
    }

    @Test
    fun `validateName - uppercase start`() {
        assertEquals("extracted", RescriptExtractVariableUtil.validateName("Foo"))
    }
}
