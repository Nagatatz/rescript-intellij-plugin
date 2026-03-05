package com.rescript.plugin.documentation

import com.rescript.plugin.RescriptTestUtils
import com.rescript.plugin.lang.RescriptTokenTypes
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RescriptOperatorDocumentationTest {
    @Test
    fun operatorInfoMapIsNotEmpty() {
        assertTrue(
            "OPERATOR_INFO should contain operator entries",
            RescriptOperatorDocumentation.OPERATOR_INFO.isNotEmpty(),
        )
    }

    @Test
    fun allOperatorsHaveValidPrecedence() {
        for ((tokenType, info) in RescriptOperatorDocumentation.OPERATOR_INFO) {
            assertTrue(
                "Operator $tokenType (${info.name}) should have non-negative precedence",
                info.precedence >= 0,
            )
        }
    }

    @Test
    fun allOperatorsHaveNonBlankFields() {
        for ((tokenType, info) in RescriptOperatorDocumentation.OPERATOR_INFO) {
            assertTrue("Operator $tokenType should have a non-blank name", info.name.isNotBlank())
            assertTrue("Operator $tokenType should have a non-blank associativity", info.associativity.isNotBlank())
            assertTrue("Operator $tokenType should have a non-blank description", info.description.isNotBlank())
        }
    }

    @Test
    fun pipeForwardOperatorInfo() {
        val info = RescriptOperatorDocumentation.OPERATOR_INFO[RescriptTokenTypes.PIPE_FORWARD]
        assertNotNull("PIPE_FORWARD should be in OPERATOR_INFO", info)
        assertEquals("Pipe forward", info!!.name)
        assertEquals(1, info.precedence)
        assertEquals("Left", info.associativity)
    }

    @Test
    fun arrowOperatorInfo() {
        val info = RescriptOperatorDocumentation.OPERATOR_INFO[RescriptTokenTypes.ARROW]
        assertNotNull("ARROW should be in OPERATOR_INFO", info)
        assertEquals("Pipe", info!!.name)
        assertEquals(1, info.precedence)
    }

    @Test
    fun arithmeticOperatorsPresent() {
        assertNotNull(RescriptOperatorDocumentation.OPERATOR_INFO[RescriptTokenTypes.PLUS])
        assertNotNull(RescriptOperatorDocumentation.OPERATOR_INFO[RescriptTokenTypes.MINUS])
        assertNotNull(RescriptOperatorDocumentation.OPERATOR_INFO[RescriptTokenTypes.STAR])
        assertNotNull(RescriptOperatorDocumentation.OPERATOR_INFO[RescriptTokenTypes.SLASH])
    }

    @Test
    fun comparisonOperatorsPresent() {
        assertNotNull(RescriptOperatorDocumentation.OPERATOR_INFO[RescriptTokenTypes.EQEQ])
        assertNotNull(RescriptOperatorDocumentation.OPERATOR_INFO[RescriptTokenTypes.EQEQEQ])
        assertNotNull(RescriptOperatorDocumentation.OPERATOR_INFO[RescriptTokenTypes.NOT_EQ])
        assertNotNull(RescriptOperatorDocumentation.OPERATOR_INFO[RescriptTokenTypes.NOT_EQEQ])
        assertNotNull(RescriptOperatorDocumentation.OPERATOR_INFO[RescriptTokenTypes.LT])
        assertNotNull(RescriptOperatorDocumentation.OPERATOR_INFO[RescriptTokenTypes.GT])
    }

    @Test
    fun bitwiseOperatorsPresent() {
        assertNotNull(RescriptOperatorDocumentation.OPERATOR_INFO[RescriptTokenTypes.LAND])
        assertNotNull(RescriptOperatorDocumentation.OPERATOR_INFO[RescriptTokenTypes.LOR])
        assertNotNull(RescriptOperatorDocumentation.OPERATOR_INFO[RescriptTokenTypes.LXOR])
        assertNotNull(RescriptOperatorDocumentation.OPERATOR_INFO[RescriptTokenTypes.LSL])
        assertNotNull(RescriptOperatorDocumentation.OPERATOR_INFO[RescriptTokenTypes.LSR])
        assertNotNull(RescriptOperatorDocumentation.OPERATOR_INFO[RescriptTokenTypes.ASR])
    }

    @Test
    fun generateOperatorDocForKnownOperator() {
        val element = RescriptTestUtils.stubPsiElement(RescriptTokenTypes.PLUS, text = "+")
        val doc = RescriptOperatorDocumentation.generateOperatorDoc(element)
        assertNotNull("Should generate doc for PLUS operator", doc)
        assertTrue("Doc should contain operator symbol", doc!!.contains("+"))
        assertTrue("Doc should contain operator name", doc.contains("Addition"))
        assertTrue("Doc should contain precedence", doc.contains("Precedence"))
        assertTrue("Doc should contain associativity", doc.contains("Associativity"))
    }

    @Test
    fun generateOperatorDocForPipeForward() {
        val element = RescriptTestUtils.stubPsiElement(RescriptTokenTypes.PIPE_FORWARD, text = "|>")
        val doc = RescriptOperatorDocumentation.generateOperatorDoc(element)
        assertNotNull(doc)
        assertTrue(doc!!.contains("Pipe forward"))
    }

    @Test
    fun generateOperatorDocReturnsNullForNonOperator() {
        val element = RescriptTestUtils.stubPsiElement(RescriptTokenTypes.LET, text = "let")
        val doc = RescriptOperatorDocumentation.generateOperatorDoc(element)
        assertNull("Should return null for non-operator token", doc)
    }

    @Test
    fun generateOperatorDocReturnsNullForNullNode() {
        // PsiElement with null node
        val element =
            java.lang.reflect.Proxy.newProxyInstance(
                com.intellij.psi.PsiElement::class.java.classLoader,
                arrayOf(com.intellij.psi.PsiElement::class.java),
            ) { _, method, _ ->
                when (method.name) {
                    "getNode" -> null
                    "toString" -> "NullNodeElement"
                    "hashCode" -> 0
                    "equals" -> false
                    else -> null
                }
            } as com.intellij.psi.PsiElement

        val doc = RescriptOperatorDocumentation.generateOperatorDoc(element)
        assertNull("Should return null when node is null", doc)
    }

    @Test
    fun generateOperatorDocEscapesHtml() {
        // The element text should be HTML-escaped in the output
        val element = RescriptTestUtils.stubPsiElement(RescriptTokenTypes.LT, text = "<")
        val doc = RescriptOperatorDocumentation.generateOperatorDoc(element)
        assertNotNull(doc)
        // The "<" should be escaped as "&lt;"
        assertTrue("Should HTML-escape operator symbol", doc!!.contains("&lt;"))
    }

    @Test
    fun functionArrowHasZeroPrecedence() {
        val info = RescriptOperatorDocumentation.OPERATOR_INFO[RescriptTokenTypes.RIGHT_ARROW]
        assertNotNull(info)
        assertEquals("Function arrow should have precedence 0", 0, info!!.precedence)
        assertEquals("Right", info.associativity)
    }
}
