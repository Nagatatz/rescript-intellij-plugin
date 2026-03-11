package com.rescript.plugin.refactor

import com.intellij.openapi.actionSystem.ActionUpdateThread
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class RescriptChangeSignatureHandlerTest {
    @Test
    fun `RescriptChangeSignatureAction getActionUpdateThread returns BGT`() {
        assertEquals(ActionUpdateThread.BGT, RescriptChangeSignatureAction().getActionUpdateThread())
    }

    @Test
    fun `parseParameters parses positional params`() {
        val params = RescriptChangeSignatureHandler.parseParameters("a, b, c")
        assertEquals(3, params.size)
        assertEquals("a", params[0].name)
        assertEquals("b", params[1].name)
        assertEquals("c", params[2].name)
        assertTrue(params.all { !it.isLabeled })
    }

    @Test
    fun `parseParameters parses labeled params`() {
        val params = RescriptChangeSignatureHandler.parseParameters("~name: string, ~age: int")
        assertEquals(2, params.size)
        assertEquals("name", params[0].name)
        assertTrue(params[0].isLabeled)
        assertEquals("string", params[0].typeAnnotation)
        assertEquals("age", params[1].name)
        assertTrue(params[1].isLabeled)
    }

    @Test
    fun `parseParameters parses labeled param with default`() {
        val params = RescriptChangeSignatureHandler.parseParameters("~count: int=0")
        assertEquals(1, params.size)
        assertEquals("count", params[0].name)
        assertTrue(params[0].isLabeled)
        assertEquals("0", params[0].defaultValue)
    }

    @Test
    fun `parseParameters handles empty string`() {
        val params = RescriptChangeSignatureHandler.parseParameters("")
        assertTrue(params.isEmpty())
    }

    @Test
    fun `parseParameters handles blank string`() {
        val params = RescriptChangeSignatureHandler.parseParameters("   ")
        assertTrue(params.isEmpty())
    }

    @Test
    fun `parseFunctionAtCaret finds function`() {
        val source = "let add = (a, b) => a + b"
        val info = RescriptChangeSignatureHandler.parseFunctionAtCaret(source, 5)
        assertNotNull(info)
        assertEquals("add", info!!.name)
        assertEquals(2, info.params.size)
    }

    @Test
    fun `parseFunctionAtCaret returns null for non-function`() {
        val source = "let x = 42"
        val info = RescriptChangeSignatureHandler.parseFunctionAtCaret(source, 5)
        assertNull(info)
    }

    @Test
    fun `applySignatureChange reorders params`() {
        val original = "let add = (a, b) => a + b"
        val originalParams =
            listOf(
                RescriptChangeSignatureHandler.Companion.FunctionParam("a", false, null, null),
                RescriptChangeSignatureHandler.Companion.FunctionParam("b", false, null, null),
            )
        val newParams =
            listOf(
                RescriptChangeSignatureHandler.Companion.FunctionParam("b", false, null, null),
                RescriptChangeSignatureHandler.Companion.FunctionParam("a", false, null, null),
            )
        val result = RescriptChangeSignatureHandler.applySignatureChange(original, originalParams, newParams)
        assertTrue(result.contains("(b, a)"))
    }

    @Test
    fun `applySignatureChange renames param`() {
        val original = "let greet = (~name: string) => name"
        val originalParams =
            listOf(
                RescriptChangeSignatureHandler.Companion.FunctionParam("name", true, "string", null),
            )
        val newParams =
            listOf(
                RescriptChangeSignatureHandler.Companion.FunctionParam("firstName", true, "string", null),
            )
        val result = RescriptChangeSignatureHandler.applySignatureChange(original, originalParams, newParams)
        assertTrue(result.contains("~firstName: string"))
    }

    @Test
    fun `splitParams handles simple list`() {
        val parts = RescriptChangeSignatureHandler.splitParams("a, b, c")
        assertEquals(3, parts.size)
    }

    @Test
    fun `splitParams respects nested types`() {
        val parts = RescriptChangeSignatureHandler.splitParams("a: option<int>, b: array<string>")
        assertEquals(2, parts.size)
    }

    @Test
    fun `splitParams handles empty string`() {
        val parts = RescriptChangeSignatureHandler.splitParams("")
        assertTrue(parts.isEmpty())
    }

    @Test
    fun `FunctionParam data class holds values`() {
        val param = RescriptChangeSignatureHandler.Companion.FunctionParam("x", true, "int", "0")
        assertEquals("x", param.name)
        assertTrue(param.isLabeled)
        assertEquals("int", param.typeAnnotation)
        assertEquals("0", param.defaultValue)
    }
}
