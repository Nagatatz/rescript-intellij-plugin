package com.rescript.plugin.completion

import com.rescript.plugin.lang.TypeShape
import com.rescript.plugin.lang.VariantConstructor
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Unit tests for the project-independent parts of the placeholder type
 * resolver: built-in shape resolution and the `type` head matcher. The
 * stub-index path requires a project fixture and is covered indirectly
 * by the pure logic it delegates to.
 */
class RescriptPlaceholderTypeResolverTest {
    @Test
    fun `resolves option to Some and None`() {
        val shape = RescriptPlaceholderTypeResolver.resolveBuiltIn("option")
        assertEquals(
            TypeShape.Variant(listOf(VariantConstructor("Some", "'a"), VariantConstructor("None", null))),
            shape,
        )
    }

    @Test
    fun `resolves result to Ok and Error`() {
        val shape = RescriptPlaceholderTypeResolver.resolveBuiltIn("result")
        assertEquals(
            TypeShape.Variant(listOf(VariantConstructor("Ok", "'a"), VariantConstructor("Error", "'b"))),
            shape,
        )
    }

    @Test
    fun `returns null for a non-built-in type name`() {
        assertNull(RescriptPlaceholderTypeResolver.resolveBuiltIn("person"))
    }

    @Test
    fun `matchesTypeHead accepts exact type declaration`() {
        assertTrue(RescriptPlaceholderTypeResolver.matchesTypeHead("type person = {name: string}", "person"))
        assertTrue(RescriptPlaceholderTypeResolver.matchesTypeHead("type color = Red | Blue", "color"))
    }

    @Test
    fun `matchesTypeHead accepts type with parameters`() {
        assertTrue(RescriptPlaceholderTypeResolver.matchesTypeHead("type tree<'a> = Leaf | Node", "tree"))
    }

    @Test
    fun `matchesTypeHead rejects prefix collision`() {
        assertFalse(RescriptPlaceholderTypeResolver.matchesTypeHead("type colors = Red | Blue", "color"))
    }

    @Test
    fun `matchesTypeHead rejects non-type declarations`() {
        assertFalse(RescriptPlaceholderTypeResolver.matchesTypeHead("let person = 1", "person"))
        assertFalse(RescriptPlaceholderTypeResolver.matchesTypeHead("module Person = {}", "Person"))
    }
}
