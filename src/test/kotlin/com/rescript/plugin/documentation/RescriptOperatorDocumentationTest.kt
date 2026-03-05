package com.rescript.plugin.documentation

import com.rescript.plugin.RescriptTestUtils
import com.rescript.plugin.lang.RescriptTokenTypes
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class RescriptOperatorDocumentationTest {
    @Test
    fun operatorInfoMapIsNotEmpty() {
        assertTrue(
            RescriptOperatorDocumentation.OPERATOR_INFO.isNotEmpty(),
            "OPERATOR_INFO should contain operator entries",
        )
    }

    @Test
    fun allOperatorsHaveValidPrecedence() {
        for ((tokenType, info) in RescriptOperatorDocumentation.OPERATOR_INFO) {
            assertTrue(
                info.precedence >= 0,
                "Operator $tokenType (${info.name}) should have non-negative precedence",
            )
        }
    }

    @Test
    fun allOperatorsHaveNonBlankFields() {
        for ((tokenType, info) in RescriptOperatorDocumentation.OPERATOR_INFO) {
            assertTrue(info.name.isNotBlank(), "Operator $tokenType should have a non-blank name")
            assertTrue(info.associativity.isNotBlank(), "Operator $tokenType should have a non-blank associativity")
            assertTrue(info.description.isNotBlank(), "Operator $tokenType should have a non-blank description")
        }
    }

    @Test
    fun pipeForwardOperatorInfo() {
        val info = RescriptOperatorDocumentation.OPERATOR_INFO[RescriptTokenTypes.PIPE_FORWARD]
        assertNotNull(info, "PIPE_FORWARD should be in OPERATOR_INFO")
        assertEquals("Pipe forward", info!!.name)
        assertEquals(1, info.precedence)
        assertEquals("Left", info.associativity)
    }

    @Test
    fun arrowOperatorInfo() {
        val info = RescriptOperatorDocumentation.OPERATOR_INFO[RescriptTokenTypes.ARROW]
        assertNotNull(info, "ARROW should be in OPERATOR_INFO")
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
        assertNotNull(doc, "Should generate doc for PLUS operator")
        assertTrue(doc!!.contains("+"), "Doc should contain operator symbol")
        assertTrue(doc.contains("Addition"), "Doc should contain operator name")
        assertTrue(doc.contains("Precedence"), "Doc should contain precedence")
        assertTrue(doc.contains("Associativity"), "Doc should contain associativity")
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
        assertNull(doc, "Should return null for non-operator token")
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
        assertNull(doc, "Should return null when node is null")
    }

    @Test
    fun generateOperatorDocEscapesHtml() {
        // The element text should be HTML-escaped in the output
        val element = RescriptTestUtils.stubPsiElement(RescriptTokenTypes.LT, text = "<")
        val doc = RescriptOperatorDocumentation.generateOperatorDoc(element)
        assertNotNull(doc)
        // The "<" should be escaped as "&lt;"
        assertTrue(doc!!.contains("&lt;"), "Should HTML-escape operator symbol")
    }

    @Test
    fun functionArrowHasZeroPrecedence() {
        val info = RescriptOperatorDocumentation.OPERATOR_INFO[RescriptTokenTypes.RIGHT_ARROW]
        assertNotNull(info)
        assertEquals(0, info!!.precedence, "Function arrow should have precedence 0")
        assertEquals("Right", info.associativity)
    }
}
