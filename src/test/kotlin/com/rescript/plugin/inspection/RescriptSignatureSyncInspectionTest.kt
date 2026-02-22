package com.rescript.plugin.inspection

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Tests for [RescriptSignatureSyncInspection] static helper methods. */
class RescriptSignatureSyncInspectionTest {
    @Test
    fun `parseDeclarationNames extracts let bindings`() {
        val text =
            """
            let foo = 42
            let bar = "hello"
            """.trimIndent()
        val names = RescriptSignatureSyncInspection.parseDeclarationNames(text)
        assertEquals(setOf("foo", "bar"), names)
    }

    @Test
    fun `parseDeclarationNames extracts type declarations`() {
        val text =
            """
            type t = A | B
            type user = {name: string}
            """.trimIndent()
        val names = RescriptSignatureSyncInspection.parseDeclarationNames(text)
        assertEquals(setOf("t", "user"), names)
    }

    @Test
    fun `parseDeclarationNames extracts module declarations`() {
        val text =
            """
            module Foo = {
              let x = 1
            }
            """.trimIndent()
        val names = RescriptSignatureSyncInspection.parseDeclarationNames(text)
        // Only top-level declarations are matched (regex anchors to ^)
        // "let x" is indented inside the module, so not matched
        assertEquals(setOf("Foo"), names)
    }

    @Test
    fun `parseDeclarationNames extracts external declarations`() {
        val text =
            """
            external log: string => unit = "console.log"
            """.trimIndent()
        val names = RescriptSignatureSyncInspection.parseDeclarationNames(text)
        assertEquals(setOf("log"), names)
    }

    @Test
    fun `parseDeclarationNames extracts mixed declarations`() {
        val text =
            """
            type t = int
            let make = () => 42
            module Utils = {}
            external fetch: string => promise<response> = "fetch"
            """.trimIndent()
        val names = RescriptSignatureSyncInspection.parseDeclarationNames(text)
        assertEquals(setOf("t", "make", "Utils", "fetch"), names)
    }

    @Test
    fun `parseDeclarationNames returns empty set for no declarations`() {
        val text = "// just a comment"
        val names = RescriptSignatureSyncInspection.parseDeclarationNames(text)
        assertTrue(names.isEmpty())
    }

    @Test
    fun `parseDeclarationNames ignores indented declarations`() {
        val text =
            """
            module M = {
              let inner = 1
            }
            """.trimIndent()
        val names = RescriptSignatureSyncInspection.parseDeclarationNames(text)
        // Only top-level: M. "let inner" is indented so not matched by ^
        assertTrue(names.contains("M"))
    }
}
