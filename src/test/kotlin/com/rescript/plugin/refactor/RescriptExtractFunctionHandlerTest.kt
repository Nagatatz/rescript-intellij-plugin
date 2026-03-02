package com.rescript.plugin.refactor

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RescriptExtractFunctionHandlerTest {
    @Test
    fun `findFreeVariables finds used identifiers`() {
        val selected = "x + y"
        val full = "let x = 1\nlet y = 2\nlet z = x + y"
        val freeVars = RescriptExtractFunctionHandler.findFreeVariables(selected, full, 26)
        assertTrue(freeVars.contains("x"))
        assertTrue(freeVars.contains("y"))
    }

    @Test
    fun `findFreeVariables excludes locally defined variables`() {
        val selected = "let temp = x + 1\ntemp * 2"
        val full = "let x = 42\n$selected"
        val freeVars = RescriptExtractFunctionHandler.findFreeVariables(selected, full, 11)
        assertTrue(freeVars.contains("x"))
        assertTrue(!freeVars.contains("temp"))
    }

    @Test
    fun `findFreeVariables excludes keywords`() {
        val selected = "if x { true } else { false }"
        val full = "let x = true\n$selected"
        val freeVars = RescriptExtractFunctionHandler.findFreeVariables(selected, full, 14)
        assertTrue(!freeVars.contains("if"))
        assertTrue(!freeVars.contains("else"))
        assertTrue(!freeVars.contains("true"))
        assertTrue(!freeVars.contains("false"))
    }

    @Test
    fun `findFreeVariables excludes globals`() {
        val selected = "Js.log(x)"
        val full = "let x = 42\n$selected"
        val freeVars = RescriptExtractFunctionHandler.findFreeVariables(selected, full, 11)
        assertTrue(!freeVars.contains("Js"))
    }

    @Test
    fun `findFreeVariables returns sorted list`() {
        val selected = "z + a + m"
        val full = "let z = 1\nlet a = 2\nlet m = 3\n$selected"
        val freeVars = RescriptExtractFunctionHandler.findFreeVariables(selected, full, 30)
        assertEquals(listOf("a", "m", "z"), freeVars)
    }

    @Test
    fun `generateFunction creates single-line function`() {
        val result = RescriptExtractFunctionHandler.generateFunction("add", "a + b", listOf("a", "b"))
        assertEquals("let add = (a, b) => a + b", result)
    }

    @Test
    fun `generateFunction creates multi-line function`() {
        val body = "let temp = a + 1\ntemp * b"
        val result = RescriptExtractFunctionHandler.generateFunction("compute", body, listOf("a", "b"))
        assertTrue(result.contains("let compute = (a, b) => {"))
        assertTrue(result.contains("  let temp = a + 1"))
        assertTrue(result.contains("}"))
    }

    @Test
    fun `generateFunction handles no params`() {
        val result = RescriptExtractFunctionHandler.generateFunction("doStuff", "42", emptyList())
        assertEquals("let doStuff = () => 42", result)
    }

    @Test
    fun `generateCallSite creates call with no args`() {
        val result = RescriptExtractFunctionHandler.generateCallSite("doStuff", emptyList())
        assertEquals("doStuff()", result)
    }

    @Test
    fun `generateCallSite creates call with args`() {
        val result = RescriptExtractFunctionHandler.generateCallSite("add", listOf("x", "y"))
        assertEquals("add(x, y)", result)
    }

    @Test
    fun `generateCallSite creates call with single arg`() {
        val result = RescriptExtractFunctionHandler.generateCallSite("inc", listOf("x"))
        assertEquals("inc(x)", result)
    }
}
