package com.rescript.plugin.intention

import com.rescript.plugin.intention.RescriptExtractLocalModuleToFileIntention.Companion.extractModuleBody
import com.rescript.plugin.intention.RescriptExtractLocalModuleToFileIntention.Companion.hasBraceBody
import com.rescript.plugin.intention.RescriptExtractLocalModuleToFileIntention.Companion.hasInternalReferences
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/** Tests for [RescriptExtractLocalModuleToFileIntention] companion helpers. */
class RescriptExtractLocalModuleToFileIntentionTest {
    @Test
    fun `hasBraceBody true for simple module declaration`() {
        assertTrue(hasBraceBody("module M = { let x = 42 }"))
    }

    @Test
    fun `hasBraceBody true with signature annotation`() {
        assertTrue(hasBraceBody("module M: Sig = { let x = 42 }"))
    }

    @Test
    fun `hasBraceBody false for module alias`() {
        assertFalse(hasBraceBody("module M = OtherModule.Sub"))
    }

    @Test
    fun `hasBraceBody false for functor form`() {
        assertFalse(hasBraceBody("module Make = (X: S) => { let x = 1 }"))
    }

    @Test
    fun `hasBraceBody false when equals sign missing`() {
        assertFalse(hasBraceBody("module M"))
    }

    @Test
    fun `extractModuleBody returns body of single-line declaration`() {
        val body = extractModuleBody("module M = { let x = 42 }")
        assertEquals(" let x = 42 ", body)
    }

    @Test
    fun `extractModuleBody returns body of multi-line declaration`() {
        val decl =
            """
            module M = {
              let x = 1
              let y = 2
            }
            """.trimIndent()
        val body = extractModuleBody(decl)
        assertEquals("\n  let x = 1\n  let y = 2\n", body)
    }

    @Test
    fun `extractModuleBody handles nested braces`() {
        val decl =
            """
            module M = {
              let pair = {a: 1, b: 2}
              let nested = {let z = {3 + 4}}
            }
            """.trimIndent()
        val body = extractModuleBody(decl)
        assertTrue(body!!.contains("let pair = {a: 1, b: 2}"))
        assertTrue(body.contains("let nested = {let z = {3 + 4}}"))
    }

    @Test
    fun `extractModuleBody returns null when no opening brace`() {
        assertNull(extractModuleBody("module M = OtherMod"))
    }

    @Test
    fun `extractModuleBody returns null on unbalanced braces`() {
        assertNull(extractModuleBody("module M = { let x = 1"))
    }

    @Test
    fun `hasInternalReferences detects qualified usage`() {
        val source =
            """
            let result = Foo.bar
            """.trimIndent()
        assertTrue(hasInternalReferences(source, "Foo"))
    }

    @Test
    fun `hasInternalReferences ignores unrelated names`() {
        val source =
            """
            let result = Bar.x
            """.trimIndent()
        assertFalse(hasInternalReferences(source, "Foo"))
    }

    @Test
    fun `hasInternalReferences requires word boundary on the module name`() {
        // `FooBar.x` should not match a reference to `Foo`.
        val source = "let r = FooBar.x"
        assertFalse(hasInternalReferences(source, "Foo"))
    }

    @Test
    fun `hasInternalReferences requires alphanumeric after dot`() {
        // `Foo.` followed by punctuation (not a member access) should not match.
        val source = "module Aux = Foo.{}"
        assertFalse(hasInternalReferences(source, "Foo"))
    }

    @Test
    fun `hasInternalReferences detects multi-line file`() {
        val source =
            """
            let a = 1
            let b = Foo.helper(a)
            let c = a + b
            """.trimIndent()
        assertTrue(hasInternalReferences(source, "Foo"))
    }
}
