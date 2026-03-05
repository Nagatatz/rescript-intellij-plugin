package com.rescript.plugin.lang.psi

import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiLanguageInjectionHost
import com.intellij.testFramework.fixtures.BasePlatformTestCase

/**
 * Tests for [RescriptStringLiteral] — the PSI element that supports language injection
 * into ReScript string literals.
 *
 * Verifies the [PsiLanguageInjectionHost] contract: valid host status,
 * text escaper decode/offset/isOneLine behaviour.
 */
class RescriptStringLiteralTest : BasePlatformTestCase() {
    private fun findStringLiteral(code: String): RescriptStringLiteral {
        val file = myFixture.configureByText("Test.res", code)
        var found: RescriptStringLiteral? = null

        fun walk(node: com.intellij.lang.ASTNode) {
            val psi = node.psi
            if (psi is RescriptStringLiteral) {
                found = psi
                return
            }
            var child = node.firstChildNode
            while (child != null) {
                walk(child)
                if (found != null) return
                child = child.treeNext
            }
        }
        file.node?.let { walk(it) }
        return found ?: fail("No RescriptStringLiteral found in PSI tree") as RescriptStringLiteral
    }

    fun testIsValidHost() {
        val literal = findStringLiteral("let s = \"hello\"")
        assertTrue("isValidHost should always return true", literal.isValidHost)
    }

    fun testLiteralTextEscaperDecode() {
        val literal = findStringLiteral("let s = \"hello world\"")
        val escaper = literal.createLiteralTextEscaper()
        val text = literal.text
        // Decode the content portion (inside quotes)
        val rangeInsideHost = TextRange(1, text.length - 1)
        val decoded = StringBuilder()
        val result = escaper.decode(rangeInsideHost, decoded)
        assertTrue("decode should return true", result)
        assertEquals("hello world", decoded.toString())
    }

    fun testLiteralTextEscaperOffsetInHost() {
        val literal = findStringLiteral("let s = \"abcd\"")
        val escaper = literal.createLiteralTextEscaper()
        // Range starts at 1 (after opening quote)
        val rangeInsideHost = TextRange(1, literal.text.length - 1)
        // Offset mapping should be identity + rangeStart
        assertEquals(1, escaper.getOffsetInHost(0, rangeInsideHost))
        assertEquals(2, escaper.getOffsetInHost(1, rangeInsideHost))
        assertEquals(3, escaper.getOffsetInHost(2, rangeInsideHost))
    }

    fun testLiteralTextEscaperIsOneLine() {
        val literal = findStringLiteral("let s = \"test\"")
        val escaper = literal.createLiteralTextEscaper()
        assertFalse("isOneLine should return false", escaper.isOneLine)
    }
}
