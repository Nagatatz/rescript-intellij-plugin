package com.rescript.plugin.refactor

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RescriptIntroduceConstantHandlerTest {
    @Test
    fun `isLiteral detects string literals`() {
        assertTrue(RescriptIntroduceConstantHandler.isLiteral("\"hello\""))
    }

    @Test
    fun `isLiteral detects integer literals`() {
        assertTrue(RescriptIntroduceConstantHandler.isLiteral("42"))
    }

    @Test
    fun `isLiteral detects float literals`() {
        assertTrue(RescriptIntroduceConstantHandler.isLiteral("3.14"))
    }

    @Test
    fun `isLiteral detects boolean true`() {
        assertTrue(RescriptIntroduceConstantHandler.isLiteral("true"))
    }

    @Test
    fun `isLiteral detects boolean false`() {
        assertTrue(RescriptIntroduceConstantHandler.isLiteral("false"))
    }

    @Test
    fun `isLiteral detects template string`() {
        assertTrue(RescriptIntroduceConstantHandler.isLiteral("`template`"))
    }

    @Test
    fun `isLiteral rejects variable names`() {
        assertFalse(RescriptIntroduceConstantHandler.isLiteral("myVar"))
    }

    @Test
    fun `isLiteral rejects expressions`() {
        assertFalse(RescriptIntroduceConstantHandler.isLiteral("1 + 2"))
    }

    @Test
    fun `suggestName generates name from string literal`() {
        val name = RescriptIntroduceConstantHandler.suggestName("\"hello world\"")
        assertNotNull(name)
        assertEquals("message", name)
    }

    @Test
    fun `suggestName generates name from number`() {
        val name = RescriptIntroduceConstantHandler.suggestName("42")
        assertEquals("value", name)
    }

    @Test
    fun `suggestName generates name from boolean`() {
        val name = RescriptIntroduceConstantHandler.suggestName("true")
        assertEquals("flag", name)
    }

    @Test
    fun `generateBinding creates let binding`() {
        val binding = RescriptIntroduceConstantHandler.generateBinding("myConst", "42")
        assertEquals("let myConst = 42", binding)
    }

    @Test
    fun `generateBinding creates let binding with string`() {
        val binding = RescriptIntroduceConstantHandler.generateBinding("greeting", "\"hello\"")
        assertEquals("let greeting = \"hello\"", binding)
    }

    @Test
    fun `findInsertOffset finds top of file`() {
        val source = "let x = 42\nlet y = x + 1"
        val offset = RescriptIntroduceConstantHandler.findInsertOffset(source)
        assertEquals(0, offset)
    }

    @Test
    fun `findInsertOffset skips open statements`() {
        val source = "open Belt\n\nlet x = 42"
        val offset = RescriptIntroduceConstantHandler.findInsertOffset(source)
        assertTrue(offset > 0)
    }
}
