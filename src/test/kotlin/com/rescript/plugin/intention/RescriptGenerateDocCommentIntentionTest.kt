package com.rescript.plugin.intention

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class RescriptGenerateDocCommentIntentionTest {
    // ── extractParams tests ───────────────────────────────────────────

    @Test
    fun `extractParams returns empty for non-function declaration`() {
        val params = RescriptGenerateDocCommentIntention.extractParams("let x = 42")
        assertTrue(params.isEmpty())
    }

    @Test
    fun `extractParams returns empty for no-param function`() {
        val params = RescriptGenerateDocCommentIntention.extractParams("let f = () => 1")
        assertTrue(params.isEmpty())
    }

    @Test
    fun `extractParams extracts single unlabeled param`() {
        val params = RescriptGenerateDocCommentIntention.extractParams("let f = (x) => x + 1")
        assertEquals(listOf("x"), params)
    }

    @Test
    fun `extractParams extracts multiple unlabeled params`() {
        val params = RescriptGenerateDocCommentIntention.extractParams("let add = (a, b) => a + b")
        assertEquals(listOf("a", "b"), params)
    }

    @Test
    fun `extractParams extracts labeled params`() {
        val params = RescriptGenerateDocCommentIntention.extractParams("let f = (~name, ~age) => name")
        assertEquals(listOf("name", "age"), params)
    }

    @Test
    fun `extractParams extracts labeled params with types`() {
        val params =
            RescriptGenerateDocCommentIntention.extractParams("let f = (~name: string, ~age: int) => name")
        assertEquals(listOf("name", "age"), params)
    }

    @Test
    fun `extractParams excludes wildcard`() {
        val params = RescriptGenerateDocCommentIntention.extractParams("let f = (_, x) => x")
        assertEquals(listOf("x"), params)
    }

    @Test
    fun `extractParams handles mixed labeled and unlabeled`() {
        val params =
            RescriptGenerateDocCommentIntention.extractParams("let f = (~label: string, x) => x")
        assertEquals(listOf("label", "x"), params)
    }

    @Test
    fun `extractParams handles param with default value syntax`() {
        val params =
            RescriptGenerateDocCommentIntention.extractParams("let f = (~name=?, ~age) => name")
        assertEquals(listOf("name", "age"), params)
    }

    // ── buildDocComment tests ─────────────────────────────────────────

    @Test
    fun `buildDocComment without params`() {
        val result = RescriptGenerateDocCommentIntention.buildDocComment(emptyList(), "")
        assertEquals(
            "/**\n" +
                " * \n" +
                " */\n",
            result,
        )
    }

    @Test
    fun `buildDocComment with params`() {
        val result = RescriptGenerateDocCommentIntention.buildDocComment(listOf("name", "age"), "")
        assertEquals(
            "/**\n" +
                " * \n" +
                " *\n" +
                " * @param name \n" +
                " * @param age \n" +
                " */\n",
            result,
        )
    }

    @Test
    fun `buildDocComment preserves indentation`() {
        val result = RescriptGenerateDocCommentIntention.buildDocComment(listOf("x"), "  ")
        assertEquals(
            "/**\n" +
                "   * \n" +
                "   *\n" +
                "   * @param x \n" +
                "   */\n" +
                "  ",
            result,
        )
    }

    @Test
    fun `buildDocComment with single param`() {
        val result = RescriptGenerateDocCommentIntention.buildDocComment(listOf("input"), "    ")
        assertEquals(
            "/**\n" +
                "     * \n" +
                "     *\n" +
                "     * @param input \n" +
                "     */\n" +
                "    ",
            result,
        )
    }

    // ── familyName / text tests ───────────────────────────────────────

    @Test
    fun `getText returns expected string`() {
        val intention = RescriptGenerateDocCommentIntention()
        assertEquals("Generate doc comment", intention.text)
    }

    @Test
    fun `getFamilyName returns expected string`() {
        val intention = RescriptGenerateDocCommentIntention()
        assertEquals("Generate doc comment", intention.familyName)
    }
}
