package com.rescript.plugin.lsp

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Unit tests for the pure declaration-head matcher inside
 * [RescriptVariantTypeResolver].
 *
 * The project-wide [RescriptVariantTypeResolver.resolve] entry point
 * relies on a stub index and is exercised by the diagnoser-level
 * integration tests instead; here we focus on the cheap textual filter
 * that drives the index walker.
 */
class RescriptVariantTypeResolverTest {
    @Test
    fun `matchesTypeHead accepts a plain type declaration`() {
        assertTrue(
            RescriptVariantTypeResolver.matchesTypeHead(
                "type color = | Red | Blue",
                "color",
            ),
        )
    }

    @Test
    fun `matchesTypeHead accepts a parameterised type declaration`() {
        assertTrue(
            RescriptVariantTypeResolver.matchesTypeHead(
                "type result<'a, 'b> = | Ok('a) | Err('b)",
                "result",
            ),
        )
    }

    @Test
    fun `matchesTypeHead rejects a let binding with the same name`() {
        assertFalse(
            RescriptVariantTypeResolver.matchesTypeHead(
                "let color = \"red\"",
                "color",
            ),
        )
    }

    @Test
    fun `matchesTypeHead rejects a type with a name that only shares a prefix`() {
        assertFalse(
            RescriptVariantTypeResolver.matchesTypeHead(
                "type colors = | Red | Blue",
                "color",
            ),
        )
    }

    @Test
    fun `matchesTypeHead accepts type with leading whitespace`() {
        assertTrue(
            RescriptVariantTypeResolver.matchesTypeHead(
                "   type color = | Red | Blue",
                "color",
            ),
        )
    }

    @Test
    fun `matchesTypeHead rejects empty text`() {
        assertFalse(
            RescriptVariantTypeResolver.matchesTypeHead("", "color"),
        )
    }

    @Test
    fun `matchesTypeHead rejects text that lacks type keyword`() {
        assertFalse(
            RescriptVariantTypeResolver.matchesTypeHead(
                "module Color = { type color = Red }",
                "color",
            ),
        )
    }
}
