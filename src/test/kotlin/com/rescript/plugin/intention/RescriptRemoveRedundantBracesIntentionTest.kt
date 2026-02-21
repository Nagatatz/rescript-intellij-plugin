package com.rescript.plugin.intention

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RescriptRemoveRedundantBracesIntentionTest {
    @Test
    fun testIntentionText() {
        val intention = RescriptRemoveRedundantBracesIntention()
        assertEquals("Remove redundant braces", intention.text)
    }

    @Test
    fun testIntentionFamilyName() {
        val intention = RescriptRemoveRedundantBracesIntention()
        assertEquals("Remove redundant braces", intention.familyName)
    }

    @Test
    fun testStartInWriteAction() {
        val intention = RescriptRemoveRedundantBracesIntention()
        assertTrue(intention.startInWriteAction())
    }

    @Test
    fun testTextAndFamilyNameAreConsistent() {
        val intention = RescriptRemoveRedundantBracesIntention()
        assertEquals(intention.text, intention.familyName)
    }

    // -- isSingleExpression tests --

    @Test
    fun testIsSingleExpressionForSimpleExpr() {
        val text = "{ x + 1 }"
        assertTrue(RescriptRemoveRedundantBracesIntention.isSingleExpression(text, 1, 8))
    }

    @Test
    fun testIsSingleExpressionReturnsFalseForMultipleStatements() {
        val text = "{ x + 1; y + 2 }"
        assertFalse(RescriptRemoveRedundantBracesIntention.isSingleExpression(text, 1, 16))
    }

    @Test
    fun testIsSingleExpressionReturnsFalseForEmptyBlock() {
        val text = "{}"
        assertFalse(RescriptRemoveRedundantBracesIntention.isSingleExpression(text, 1, 1))
    }

    @Test
    fun testIsSingleExpressionReturnsFalseForWhitespaceOnly() {
        val text = "{   }"
        assertFalse(RescriptRemoveRedundantBracesIntention.isSingleExpression(text, 1, 4))
    }

    @Test
    fun testIsSingleExpressionIgnoresSemicolonInNestedBraces() {
        // Semicolons inside nested braces should not count
        val text = "{ {a; b} }"
        assertTrue(RescriptRemoveRedundantBracesIntention.isSingleExpression(text, 1, 9))
    }

    @Test
    fun testIsSingleExpressionIgnoresSemicolonInString() {
        val text = "{ \"hello; world\" }"
        assertTrue(RescriptRemoveRedundantBracesIntention.isSingleExpression(text, 1, 17))
    }

    @Test
    fun testIsSingleExpressionIgnoresSemicolonInParens() {
        val text = "{ foo(a; b) }"
        // Semicolons inside parens are at depth > 0
        assertTrue(RescriptRemoveRedundantBracesIntention.isSingleExpression(text, 1, 12))
    }

    @Test
    fun testIsSingleExpressionReturnsFalseForInvalidRange() {
        assertFalse(RescriptRemoveRedundantBracesIntention.isSingleExpression("abc", 5, 3))
    }

    // -- findBracePairRange null cases --

    @Test
    fun testFindBracePairRangeReturnsNullForNullNode() {
        val element =
            java.lang.reflect.Proxy.newProxyInstance(
                com.intellij.psi.PsiElement::class.java.classLoader,
                arrayOf(com.intellij.psi.PsiElement::class.java),
            ) { _, method, _ ->
                when (method.name) {
                    "getNode" -> null
                    "toString" -> "StubElement"
                    "hashCode" -> 0
                    "equals" -> false
                    else -> null
                }
            } as com.intellij.psi.PsiElement
        assertNull(RescriptRemoveRedundantBracesIntention.findBracePairRange(element, "{}"))
    }
}
