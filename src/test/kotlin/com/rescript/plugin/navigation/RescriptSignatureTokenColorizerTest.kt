package com.rescript.plugin.navigation

import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import com.rescript.plugin.IntelliJPlatformExtension
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

/**
 * Drives [RescriptSignatureTokenColorizer] through the signature shapes
 * that the Hoogle-style search produces. The tokenizer depends on
 * [com.intellij.openapi.editor.colors.EditorColorsManager] so a heavy
 * IntelliJ fixture is required, but each individual test stays small
 * because the helper itself is a pure-function wrapper over the lexer.
 */
@ExtendWith(IntelliJPlatformExtension::class)
class RescriptSignatureTokenColorizerTest {
    @Suppress("unused")
    private lateinit var myFixture: CodeInsightTestFixture

    @Test
    fun `empty signature yields no tokens`() {
        assertEquals(0, RescriptSignatureTokenColorizer.tokenize("").size)
    }

    @Test
    fun `simple arrow signature splits into more than one token`() {
        val tokens = RescriptSignatureTokenColorizer.tokenize("string => int")
        // At minimum: `string`, `=>`, `int` (whitespace tokens may also appear).
        assertTrue(tokens.size >= 3, "expected at least 3 tokens, got ${tokens.size}: ${tokens.map { it.text }}")
        val joined = tokens.joinToString("") { it.text }
        assertEquals("string => int", joined, "tokens must reconstruct the original signature verbatim")
    }

    @Test
    fun `type variable signature yields tokens that reconstruct the input`() {
        val signature = "'a => 'a"
        val tokens = RescriptSignatureTokenColorizer.tokenize(signature)
        val joined = tokens.joinToString("") { it.text }
        assertEquals(signature, joined)
    }

    @Test
    fun `every token carries a non-null SimpleTextAttributes`() {
        val signature = "(int, string) => result<int, string>"
        val tokens = RescriptSignatureTokenColorizer.tokenize(signature)
        for (token in tokens) {
            assertNotNull(token.attributes, "token '${token.text}' must have attributes")
        }
        // Sanity: the reconstructed text round-trips.
        assertEquals(signature, tokens.joinToString("") { it.text })
    }

    @Test
    fun `option type application tokenizes brackets and identifiers separately`() {
        // Structural check: the lexer must emit `<` and `>` as their own
        // tokens distinct from `option` and `int`. We don't assert colour
        // differences here — the test scheme may leave bracket attributes
        // empty, so colour-equality is environment-dependent.
        val signature = "option<int>"
        val tokens = RescriptSignatureTokenColorizer.tokenize(signature)
        val texts = tokens.map { it.text }
        assertTrue("option" in texts, "expected 'option' token, got $texts")
        assertTrue("<" in texts, "expected '<' token, got $texts")
        assertTrue("int" in texts, "expected 'int' token, got $texts")
        assertTrue(">" in texts, "expected '>' token, got $texts")
        assertEquals(signature, tokens.joinToString("") { it.text })
    }
}
