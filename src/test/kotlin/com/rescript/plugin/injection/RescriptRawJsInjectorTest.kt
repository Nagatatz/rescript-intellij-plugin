package com.rescript.plugin.injection

import com.intellij.lang.ASTNode
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.TokenType
import com.intellij.psi.tree.IElementType
import com.rescript.plugin.lang.RescriptTokenTypes
import com.rescript.plugin.lang.psi.RescriptStringLiteral
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RescriptRawJsInjectorTest {
    private val injector = RescriptRawJsInjector()

    // ── detectDirective tests ───────────────────────────────────────

    @Test
    fun `detectDirective returns raw for percent-raw-lparen pattern`() {
        val percent = stubElement(RescriptTokenTypes.PERCENT)
        val raw = stubElement(RescriptTokenTypes.RAW, prevSibling = percent)
        val lparen = stubElement(RescriptTokenTypes.LPAREN, prevSibling = raw)
        val stringEl = stubElement(RescriptTokenTypes.STRING_VALUE, prevSibling = lparen)

        assertEquals("raw", injector.detectDirective(stringEl))
    }

    @Test
    fun `detectDirective returns null when no prevSibling`() {
        val stringEl = stubElement(RescriptTokenTypes.STRING_VALUE)
        assertNull(injector.detectDirective(stringEl))
    }

    @Test
    fun `detectDirective returns null when pattern is incomplete`() {
        // Only LPAREN (missing RAW and PERCENT)
        val lparen = stubElement(RescriptTokenTypes.LPAREN)
        val stringEl = stubElement(RescriptTokenTypes.STRING_VALUE, prevSibling = lparen)

        assertNull(injector.detectDirective(stringEl))
    }

    @Test
    fun `detectDirective skips whitespace`() {
        val percent = stubElement(RescriptTokenTypes.PERCENT)
        val raw = stubElement(RescriptTokenTypes.RAW, prevSibling = percent)
        val ws = stubElement(TokenType.WHITE_SPACE, prevSibling = raw)
        val lparen = stubElement(RescriptTokenTypes.LPAREN, prevSibling = ws)
        val stringEl = stubElement(RescriptTokenTypes.STRING_VALUE, prevSibling = lparen)

        assertEquals("raw", injector.detectDirective(stringEl))
    }

    @Test
    fun `detectDirective skips JS_STRING_OPEN for template strings`() {
        val percent = stubElement(RescriptTokenTypes.PERCENT)
        val percent2 = stubElement(RescriptTokenTypes.PERCENT, prevSibling = percent)
        val raw = stubElement(RescriptTokenTypes.RAW, prevSibling = percent2)
        val lparen = stubElement(RescriptTokenTypes.LPAREN, prevSibling = raw)
        val jsOpen = stubElement(RescriptTokenTypes.JS_STRING_OPEN, prevSibling = lparen)
        val stringEl = stubElement(RescriptTokenTypes.STRING_VALUE, prevSibling = jsOpen)

        assertEquals("raw", injector.detectDirective(stringEl))
    }

    @Test
    fun `detectDirective returns null when missing PERCENT`() {
        val raw = stubElement(RescriptTokenTypes.RAW)
        val lparen = stubElement(RescriptTokenTypes.LPAREN, prevSibling = raw)
        val stringEl = stubElement(RescriptTokenTypes.STRING_VALUE, prevSibling = lparen)

        assertNull(injector.detectDirective(stringEl))
    }

    @Test
    fun `detectDirective returns null when LPAREN is missing`() {
        val raw = stubElement(RescriptTokenTypes.RAW)
        val stringEl = stubElement(RescriptTokenTypes.STRING_VALUE, prevSibling = raw)

        assertNull(injector.detectDirective(stringEl))
    }

    // ── FFI pattern tests ─────────────────────────────────────────────

    @Test
    fun `detectDirective returns ffi for percent-ffi-lparen pattern`() {
        val percent = stubElement(RescriptTokenTypes.PERCENT)
        val ffi = stubElement(RescriptTokenTypes.FFI, prevSibling = percent)
        val lparen = stubElement(RescriptTokenTypes.LPAREN, prevSibling = ffi)
        val stringEl = stubElement(RescriptTokenTypes.STRING_VALUE, prevSibling = lparen)

        assertEquals("ffi", injector.detectDirective(stringEl))
    }

    @Test
    fun `detectDirective returns ffi for double-percent-ffi pattern`() {
        val percent = stubElement(RescriptTokenTypes.PERCENT)
        val percent2 = stubElement(RescriptTokenTypes.PERCENT, prevSibling = percent)
        val ffi = stubElement(RescriptTokenTypes.FFI, prevSibling = percent2)
        val lparen = stubElement(RescriptTokenTypes.LPAREN, prevSibling = ffi)
        val stringEl = stubElement(RescriptTokenTypes.STRING_VALUE, prevSibling = lparen)

        assertEquals("ffi", injector.detectDirective(stringEl))
    }

    @Test
    fun `detectDirective returns ffi for ffi with JS_STRING_OPEN`() {
        val percent = stubElement(RescriptTokenTypes.PERCENT)
        val percent2 = stubElement(RescriptTokenTypes.PERCENT, prevSibling = percent)
        val ffi = stubElement(RescriptTokenTypes.FFI, prevSibling = percent2)
        val lparen = stubElement(RescriptTokenTypes.LPAREN, prevSibling = ffi)
        val jsOpen = stubElement(RescriptTokenTypes.JS_STRING_OPEN, prevSibling = lparen)
        val stringEl = stubElement(RescriptTokenTypes.STRING_VALUE, prevSibling = jsOpen)

        assertEquals("ffi", injector.detectDirective(stringEl))
    }

    @Test
    fun `detectDirective returns null for ffi without PERCENT`() {
        val ffi = stubElement(RescriptTokenTypes.FFI)
        val lparen = stubElement(RescriptTokenTypes.LPAREN, prevSibling = ffi)
        val stringEl = stubElement(RescriptTokenTypes.STRING_VALUE, prevSibling = lparen)

        assertNull(injector.detectDirective(stringEl))
    }

    // ── %re pattern tests ─────────────────────────────────────────────

    @Test
    fun `detectDirective returns re for percent-re-lparen pattern`() {
        val percent = stubElement(RescriptTokenTypes.PERCENT)
        val re = stubElement(RescriptTokenTypes.LIDENT, prevSibling = percent, text = "re")
        val lparen = stubElement(RescriptTokenTypes.LPAREN, prevSibling = re)
        val stringEl = stubElement(RescriptTokenTypes.STRING_VALUE, prevSibling = lparen)

        assertEquals("re", injector.detectDirective(stringEl))
    }

    @Test
    fun `detectDirective returns null for percent-lident-not-re pattern`() {
        val percent = stubElement(RescriptTokenTypes.PERCENT)
        val other = stubElement(RescriptTokenTypes.LIDENT, prevSibling = percent, text = "something")
        val lparen = stubElement(RescriptTokenTypes.LPAREN, prevSibling = other)
        val stringEl = stubElement(RescriptTokenTypes.STRING_VALUE, prevSibling = lparen)

        assertNull(injector.detectDirective(stringEl))
    }

    @Test
    fun `detectDirective returns null for re without PERCENT`() {
        val re = stubElement(RescriptTokenTypes.LIDENT, text = "re")
        val lparen = stubElement(RescriptTokenTypes.LPAREN, prevSibling = re)
        val stringEl = stubElement(RescriptTokenTypes.STRING_VALUE, prevSibling = lparen)

        assertNull(injector.detectDirective(stringEl))
    }

    @Test
    fun `detectDirective returns re for re with JS_STRING_OPEN`() {
        val percent = stubElement(RescriptTokenTypes.PERCENT)
        val re = stubElement(RescriptTokenTypes.LIDENT, prevSibling = percent, text = "re")
        val lparen = stubElement(RescriptTokenTypes.LPAREN, prevSibling = re)
        val jsOpen = stubElement(RescriptTokenTypes.JS_STRING_OPEN, prevSibling = lparen)
        val stringEl = stubElement(RescriptTokenTypes.STRING_VALUE, prevSibling = jsOpen)

        assertEquals("re", injector.detectDirective(stringEl))
    }

    // ── getRegexPatternRange tests ──────────────────────────────────

    @Test
    fun `getRegexPatternRange extracts pattern from slash-delimited regex`() {
        // /abc/g -> pattern range covers "abc"
        val range = injector.getRegexPatternRange("/abc/g", 0)
        assertEquals(TextRange(1, 4), range)
    }

    @Test
    fun `getRegexPatternRange with base offset`() {
        // Inside a quoted string: "/abc/g" -> offset 1 for the opening quote
        val range = injector.getRegexPatternRange("/abc/g", 1)
        assertEquals(TextRange(2, 5), range)
    }

    @Test
    fun `getRegexPatternRange returns null for non-slash string`() {
        assertNull(injector.getRegexPatternRange("abc", 0))
    }

    @Test
    fun `getRegexPatternRange returns null for single slash`() {
        assertNull(injector.getRegexPatternRange("/", 0))
    }

    @Test
    fun `getRegexPatternRange returns null for empty pattern`() {
        // //g -> lastSlash=1, patternStart=1, patternEnd=1, start >= end
        assertNull(injector.getRegexPatternRange("//g", 0))
    }

    @Test
    fun `getRegexPatternRange handles complex pattern`() {
        // /[a-z]+\d{2,}/gi
        val content = "/[a-z]+\\d{2,}/gi"
        val range = injector.getRegexPatternRange(content, 0)
        // Pattern is between first / (index 0) and last / (index 13)
        val lastSlash = content.lastIndexOf('/')
        assertEquals(TextRange(1, lastSlash), range)
    }

    @Test
    fun `getRegexPatternRange with no flags`() {
        // /pattern/ -> pattern range covers "pattern"
        val range = injector.getRegexPatternRange("/pattern/", 0)
        assertEquals(TextRange(1, 8), range)
    }

    // ── elementsToInjectIn test ───────────────────────────────────────

    @Test
    fun `elementsToInjectIn contains RescriptStringLiteral`() {
        val elements = injector.elementsToInjectIn()
        assertTrue(elements.contains(RescriptStringLiteral::class.java))
        assertEquals(1, elements.size)
    }

    // ── Helper methods ────────────────────────────────────────────────

    private fun stubElement(
        type: IElementType,
        prevSibling: PsiElement? = null,
        text: String? = null,
    ): PsiElement {
        val astNode = stubAstNode(type)
        return java.lang.reflect.Proxy.newProxyInstance(
            PsiElement::class.java.classLoader,
            arrayOf(PsiElement::class.java),
        ) { _, method, _ ->
            when (method.name) {
                "getNode" -> astNode
                "getPrevSibling" -> prevSibling
                "getText" -> text
                "toString" -> "StubElement($type)"
                "hashCode" -> type.hashCode()
                "equals" -> false
                else -> null
            }
        } as PsiElement
    }

    private fun stubAstNode(type: IElementType): ASTNode =
        java.lang.reflect.Proxy.newProxyInstance(
            ASTNode::class.java.classLoader,
            arrayOf(ASTNode::class.java),
        ) { _, method, _ ->
            when (method.name) {
                "getElementType" -> type
                "toString" -> "StubASTNode($type)"
                "hashCode" -> type.hashCode()
                "equals" -> false
                else -> null
            }
        } as ASTNode
}
