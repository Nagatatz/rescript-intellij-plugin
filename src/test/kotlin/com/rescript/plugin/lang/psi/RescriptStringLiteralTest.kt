package com.rescript.plugin.lang.psi

import com.intellij.openapi.util.TextRange
import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import com.rescript.plugin.IntelliJPlatformExtension
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

/**
 * Tests for [RescriptStringLiteral] — the PSI element that supports language injection
 * into ReScript string literals.
 *
 * Verifies the [com.intellij.psi.PsiLanguageInjectionHost] contract: valid host status,
 * text escaper decode/offset/isOneLine behaviour.
 */
@ExtendWith(IntelliJPlatformExtension::class)
class RescriptStringLiteralTest {
    private lateinit var myFixture: CodeInsightTestFixture

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
        return found ?: throw AssertionError("No RescriptStringLiteral found in PSI tree")
    }

    @Test
    fun testIsValidHost() {
        val literal = findStringLiteral("let s = \"hello\"")
        assertTrue(literal.isValidHost, "isValidHost should always return true")
    }

    @Test
    fun testLiteralTextEscaperDecode() {
        val literal = findStringLiteral("let s = \"hello world\"")
        val escaper = literal.createLiteralTextEscaper()
        val text = literal.text
        // Decode the content portion (inside quotes)
        val rangeInsideHost = TextRange(1, text.length - 1)
        val decoded = StringBuilder()
        val result = escaper.decode(rangeInsideHost, decoded)
        assertTrue(result, "decode should return true")
        assertEquals("hello world", decoded.toString())
    }

    @Test
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

    @Test
    fun testLiteralTextEscaperIsOneLine() {
        val literal = findStringLiteral("let s = \"test\"")
        val escaper = literal.createLiteralTextEscaper()
        assertFalse(escaper.isOneLine, "isOneLine should return false")
    }
}
