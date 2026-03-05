package com.rescript.plugin.generate

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class RescriptGenerateModuleImplActionTest {
    @Test
    fun `parseModuleTypeBody finds let declarations`() {
        val body =
            """
            let make: string => element
            let toString: t => string
            """.trimIndent()
        val decls = RescriptGenerateModuleImplAction.parseModuleTypeBody(body)
        assertEquals(2, decls.size)
        assertEquals("let", decls[0].kind)
        assertEquals("make", decls[0].name)
    }

    @Test
    fun `parseModuleTypeBody finds type declarations`() {
        val body =
            """
            type t
            type config = {name: string}
            """.trimIndent()
        val decls = RescriptGenerateModuleImplAction.parseModuleTypeBody(body)
        assertEquals(2, decls.size)
        assertEquals("type", decls[0].kind)
        assertEquals("t", decls[0].name)
    }

    @Test
    fun `parseModuleTypeBody finds module declarations`() {
        val body = "module Internal: {}"
        val decls = RescriptGenerateModuleImplAction.parseModuleTypeBody(body)
        assertEquals(1, decls.size)
        assertEquals("module", decls[0].kind)
        assertEquals("Internal", decls[0].name)
    }

    @Test
    fun `parseModuleTypeBody handles empty body`() {
        val decls = RescriptGenerateModuleImplAction.parseModuleTypeBody("")
        assertTrue(decls.isEmpty())
    }

    @Test
    fun `generateSkeleton creates module with Type suffix removed`() {
        val decls =
            listOf(
                RescriptGenerateModuleImplAction.Companion.TypeDeclaration("type", "t", ""),
                RescriptGenerateModuleImplAction.Companion.TypeDeclaration("let", "make", "string => element"),
            )
        val result = RescriptGenerateModuleImplAction.generateSkeleton("FooType", decls)
        assertTrue(result.contains("module FooImpl: FooType = {"))
        assertTrue(result.contains("type t = unit"))
        assertTrue(result.contains("let make"))
    }

    @Test
    fun `generateSkeleton adds Impl suffix for non-Type names`() {
        val result = RescriptGenerateModuleImplAction.generateSkeleton("Printable", emptyList())
        assertTrue(result.contains("module PrintableImpl: Printable = {"))
    }

    @Test
    fun `generateLetImpl generates function impl for arrow type`() {
        val result = RescriptGenerateModuleImplAction.generateLetImpl("string => int")
        assertTrue(result.contains("=>"))
        assertTrue(result.contains("assert(false)"))
    }

    @Test
    fun `generateLetImpl generates string default`() {
        val result = RescriptGenerateModuleImplAction.generateLetImpl("string")
        assertEquals(" = \"\"", result)
    }

    @Test
    fun `generateLetImpl generates int default`() {
        val result = RescriptGenerateModuleImplAction.generateLetImpl("int")
        assertEquals(" = 0", result)
    }

    @Test
    fun `generateLetImpl generates bool default`() {
        val result = RescriptGenerateModuleImplAction.generateLetImpl("bool")
        assertEquals(" = false", result)
    }

    @Test
    fun `generateLetImpl generates option default`() {
        val result = RescriptGenerateModuleImplAction.generateLetImpl("option<string>")
        assertEquals(" = None", result)
    }

    @Test
    fun `generateParamNames handles labeled args`() {
        val result = RescriptGenerateModuleImplAction.generateParamNames("~name: string, ~age: int")
        assertEquals("~name, ~age", result)
    }

    @Test
    fun `generateParamNames generates positional names`() {
        val result = RescriptGenerateModuleImplAction.generateParamNames("string, int")
        assertEquals("a, b", result)
    }

    @Test
    fun `findModuleTypeAtCaret returns null for no module type`() {
        val source = "let x = 42"
        val result = RescriptGenerateModuleImplAction.findModuleTypeAtCaret(source, 0)
        assertNull(result)
    }

    @Test
    fun `findModuleTypeAtCaret finds module type at offset`() {
        val source =
            """
            module type Printable = {
              let toString: t => string
            }
            """.trimIndent()
        val result = RescriptGenerateModuleImplAction.findModuleTypeAtCaret(source, 10)
        assertNotNull(result)
        assertEquals("Printable", result!!.name)
    }

    @Test
    fun `TypeDeclaration data class holds values`() {
        val decl = RescriptGenerateModuleImplAction.Companion.TypeDeclaration("let", "make", "unit => element")
        assertEquals("let", decl.kind)
        assertEquals("make", decl.name)
        assertEquals("unit => element", decl.typeAnnotation)
    }
}
