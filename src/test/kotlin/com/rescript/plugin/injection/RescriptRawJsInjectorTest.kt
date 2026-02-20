package com.rescript.plugin.injection

import com.intellij.lang.ASTNode
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.TokenType
import com.intellij.psi.tree.IElementType
import com.rescript.plugin.lang.RescriptTokenTypes
import com.rescript.plugin.lang.psi.RescriptStringLiteral
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RescriptRawJsInjectorTest {
    private val injector = RescriptRawJsInjector()

    // ── getInjectionRange tests (via reflection) ──────────────────────

    @Test
    fun `getInjectionRange returns trimmed range for regular string`() {
        // "content" -> TextRange(1, 8)
        val range = callGetInjectionRange("\"content\"")
        assertEquals(TextRange(1, 8), range)
    }

    @Test
    fun `getInjectionRange returns null for empty quoted string`() {
        // "" -> null (length == 2, empty content)
        assertNull(callGetInjectionRange("\"\""))
    }

    @Test
    fun `getInjectionRange returns null for empty text`() {
        assertNull(callGetInjectionRange(""))
    }

    @Test
    fun `getInjectionRange returns full range for template string content`() {
        // Template string content (no quotes, backtick is separate token)
        val range = callGetInjectionRange("console.log(42)")
        assertEquals(TextRange(0, 15), range)
    }

    @Test
    fun `getInjectionRange returns range for single-char string`() {
        // "x" -> TextRange(1, 2)
        val range = callGetInjectionRange("\"x\"")
        assertEquals(TextRange(1, 2), range)
    }

    // ── isInsideRawBlock tests (via reflection) ───────────────────────

    @Test
    fun `isInsideRawBlock returns true for percent-raw-lparen pattern`() {
        val percent = stubElement(RescriptTokenTypes.PERCENT)
        val raw = stubElement(RescriptTokenTypes.RAW, prevSibling = percent)
        val lparen = stubElement(RescriptTokenTypes.LPAREN, prevSibling = raw)
        val stringEl = stubElement(RescriptTokenTypes.STRING_VALUE, prevSibling = lparen)

        assertTrue(callIsInsideRawBlock(stringEl))
    }

    @Test
    fun `isInsideRawBlock returns false when no prevSibling`() {
        val stringEl = stubElement(RescriptTokenTypes.STRING_VALUE)
        assertFalse(callIsInsideRawBlock(stringEl))
    }

    @Test
    fun `isInsideRawBlock returns false when pattern is incomplete`() {
        // Only LPAREN (missing RAW and PERCENT)
        val lparen = stubElement(RescriptTokenTypes.LPAREN)
        val stringEl = stubElement(RescriptTokenTypes.STRING_VALUE, prevSibling = lparen)

        assertFalse(callIsInsideRawBlock(stringEl))
    }

    @Test
    fun `isInsideRawBlock skips whitespace`() {
        val percent = stubElement(RescriptTokenTypes.PERCENT)
        val raw = stubElement(RescriptTokenTypes.RAW, prevSibling = percent)
        val ws = stubElement(TokenType.WHITE_SPACE, prevSibling = raw)
        val lparen = stubElement(RescriptTokenTypes.LPAREN, prevSibling = ws)
        val stringEl = stubElement(RescriptTokenTypes.STRING_VALUE, prevSibling = lparen)

        // skipWhitespace should skip the WS between LPAREN and RAW
        // But wait — the chain is: stringEl -> lparen -> ws -> raw -> percent
        // skipWhitespace(lparen) returns lparen (LPAREN is not whitespace)
        // Then checks lparen == LPAREN: yes
        // skipWhitespace(ws) -> skips ws, returns raw
        // Then checks raw == RAW: yes
        // skipWhitespace(percent) returns percent
        // Then checks percent == PERCENT: yes => true
        assertTrue(callIsInsideRawBlock(stringEl))
    }

    @Test
    fun `isInsideRawBlock skips JS_STRING_OPEN for template strings`() {
        val percent = stubElement(RescriptTokenTypes.PERCENT)
        val percent2 = stubElement(RescriptTokenTypes.PERCENT, prevSibling = percent)
        val raw = stubElement(RescriptTokenTypes.RAW, prevSibling = percent2)
        val lparen = stubElement(RescriptTokenTypes.LPAREN, prevSibling = raw)
        val jsOpen = stubElement(RescriptTokenTypes.JS_STRING_OPEN, prevSibling = lparen)
        val stringEl = stubElement(RescriptTokenTypes.STRING_VALUE, prevSibling = jsOpen)

        assertTrue(callIsInsideRawBlock(stringEl))
    }

    @Test
    fun `isInsideRawBlock returns false when missing PERCENT`() {
        val raw = stubElement(RescriptTokenTypes.RAW)
        val lparen = stubElement(RescriptTokenTypes.LPAREN, prevSibling = raw)
        val stringEl = stubElement(RescriptTokenTypes.STRING_VALUE, prevSibling = lparen)

        assertFalse(callIsInsideRawBlock(stringEl))
    }

    @Test
    fun `isInsideRawBlock returns false when LPAREN is missing`() {
        // RAW directly before string (no LPAREN)
        val raw = stubElement(RescriptTokenTypes.RAW)
        val stringEl = stubElement(RescriptTokenTypes.STRING_VALUE, prevSibling = raw)

        assertFalse(callIsInsideRawBlock(stringEl))
    }

    // ── FFI pattern tests ─────────────────────────────────────────────

    @Test
    fun `isInsideRawBlock returns true for percent-ffi-lparen pattern`() {
        val percent = stubElement(RescriptTokenTypes.PERCENT)
        val ffi = stubElement(RescriptTokenTypes.FFI, prevSibling = percent)
        val lparen = stubElement(RescriptTokenTypes.LPAREN, prevSibling = ffi)
        val stringEl = stubElement(RescriptTokenTypes.STRING_VALUE, prevSibling = lparen)

        assertTrue(callIsInsideRawBlock(stringEl))
    }

    @Test
    fun `isInsideRawBlock returns true for double-percent-ffi pattern`() {
        val percent = stubElement(RescriptTokenTypes.PERCENT)
        val percent2 = stubElement(RescriptTokenTypes.PERCENT, prevSibling = percent)
        val ffi = stubElement(RescriptTokenTypes.FFI, prevSibling = percent2)
        val lparen = stubElement(RescriptTokenTypes.LPAREN, prevSibling = ffi)
        val stringEl = stubElement(RescriptTokenTypes.STRING_VALUE, prevSibling = lparen)

        assertTrue(callIsInsideRawBlock(stringEl))
    }

    @Test
    fun `isInsideRawBlock returns true for ffi with JS_STRING_OPEN`() {
        val percent = stubElement(RescriptTokenTypes.PERCENT)
        val percent2 = stubElement(RescriptTokenTypes.PERCENT, prevSibling = percent)
        val ffi = stubElement(RescriptTokenTypes.FFI, prevSibling = percent2)
        val lparen = stubElement(RescriptTokenTypes.LPAREN, prevSibling = ffi)
        val jsOpen = stubElement(RescriptTokenTypes.JS_STRING_OPEN, prevSibling = lparen)
        val stringEl = stubElement(RescriptTokenTypes.STRING_VALUE, prevSibling = jsOpen)

        assertTrue(callIsInsideRawBlock(stringEl))
    }

    @Test
    fun `isInsideRawBlock returns false for ffi without PERCENT`() {
        val ffi = stubElement(RescriptTokenTypes.FFI)
        val lparen = stubElement(RescriptTokenTypes.LPAREN, prevSibling = ffi)
        val stringEl = stubElement(RescriptTokenTypes.STRING_VALUE, prevSibling = lparen)

        assertFalse(callIsInsideRawBlock(stringEl))
    }

    // ── elementsToInjectIn test ───────────────────────────────────────

    @Test
    fun `elementsToInjectIn contains RescriptStringLiteral`() {
        val elements = injector.elementsToInjectIn()
        assertTrue(elements.contains(RescriptStringLiteral::class.java))
        assertEquals(1, elements.size)
    }

    // ── Helper methods ────────────────────────────────────────────────

    private fun callGetInjectionRange(text: String): TextRange? {
        val element = RescriptStringLiteral(RescriptTokenTypes.STRING_VALUE, text)
        val method =
            RescriptRawJsInjector::class.java.getDeclaredMethod(
                "getInjectionRange",
                RescriptStringLiteral::class.java,
            )
        method.isAccessible = true
        return method.invoke(injector, element) as? TextRange
    }

    private fun callIsInsideRawBlock(element: PsiElement): Boolean {
        val method =
            RescriptRawJsInjector::class.java.getDeclaredMethod(
                "isInsideRawBlock",
                PsiElement::class.java,
            )
        method.isAccessible = true
        return method.invoke(injector, element) as Boolean
    }

    private fun stubElement(
        type: IElementType,
        prevSibling: PsiElement? = null,
    ): PsiElement {
        val astNode = stubAstNode(type)
        return java.lang.reflect.Proxy.newProxyInstance(
            PsiElement::class.java.classLoader,
            arrayOf(PsiElement::class.java),
        ) { _, method, _ ->
            when (method.name) {
                "getNode" -> astNode
                "getPrevSibling" -> prevSibling
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
