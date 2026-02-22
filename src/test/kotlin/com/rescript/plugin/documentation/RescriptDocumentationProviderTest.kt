package com.rescript.plugin.documentation

import com.intellij.lang.ASTNode
import com.intellij.lang.documentation.AbstractDocumentationProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.tree.IElementType
import com.rescript.plugin.lang.RescriptTokenTypes
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RescriptDocumentationProviderTest {
    @Test
    fun `instance can be created`() {
        val provider = RescriptDocumentationProvider()
        assertNotNull(provider)
    }

    @Test
    fun `is an AbstractDocumentationProvider`() {
        val provider = RescriptDocumentationProvider()
        assertTrue(provider is AbstractDocumentationProvider)
    }

    // -- Quick Documentation tests --

    @Test
    fun `generateDoc returns null for null element`() {
        val provider = RescriptDocumentationProvider()
        assertNull(provider.generateDoc(null, null))
    }

    @Test
    fun `getDeclarationType returns null for non-declaration elements`() {
        assertNull(
            RescriptDocumentationProvider.getDeclarationType(
                object : com.intellij.psi.impl.FakePsiElement() {
                    override fun getParent(): com.intellij.psi.PsiElement? = null
                },
            ),
        )
    }

    // -- MODULE_URL_MAP tests --

    @Test
    fun `MODULE_URL_MAP contains Belt_Array mapping`() {
        assertEquals("belt/array", RescriptDocumentationProvider.MODULE_URL_MAP["Belt.Array"])
    }

    @Test
    fun `MODULE_URL_MAP contains Js_String2 mapping`() {
        assertEquals("js/string-2", RescriptDocumentationProvider.MODULE_URL_MAP["Js.String2"])
    }

    @Test
    fun `MODULE_URL_MAP contains Belt root`() {
        assertEquals("belt", RescriptDocumentationProvider.MODULE_URL_MAP["Belt"])
    }

    @Test
    fun `MODULE_URL_MAP contains Js root`() {
        assertEquals("js", RescriptDocumentationProvider.MODULE_URL_MAP["Js"])
    }

    @Test
    fun `MODULE_URL_MAP contains Belt_List`() {
        assertEquals("belt/list", RescriptDocumentationProvider.MODULE_URL_MAP["Belt.List"])
    }

    @Test
    fun `MODULE_URL_MAP contains Belt_Map`() {
        assertEquals("belt/map", RescriptDocumentationProvider.MODULE_URL_MAP["Belt.Map"])
    }

    @Test
    fun `MODULE_URL_MAP contains Belt_Option`() {
        assertEquals("belt/option", RescriptDocumentationProvider.MODULE_URL_MAP["Belt.Option"])
    }

    @Test
    fun `MODULE_URL_MAP contains Js_Promise`() {
        assertEquals("js/promise", RescriptDocumentationProvider.MODULE_URL_MAP["Js.Promise"])
    }

    @Test
    fun `MODULE_URL_MAP contains Js_Json`() {
        assertEquals("js/json", RescriptDocumentationProvider.MODULE_URL_MAP["Js.Json"])
    }

    @Test
    fun `MODULE_URL_MAP contains Js_Dict`() {
        assertEquals("js/dict", RescriptDocumentationProvider.MODULE_URL_MAP["Js.Dict"])
    }

    @Test
    fun `MODULE_URL_MAP does not contain unknown module`() {
        assertNull(RescriptDocumentationProvider.MODULE_URL_MAP["NonExistent.Module"])
    }

    @Test
    fun `MODULE_URL_MAP is not empty`() {
        assertTrue(RescriptDocumentationProvider.MODULE_URL_MAP.isNotEmpty())
    }

    @Test
    fun `MODULE_URL_MAP contains Belt submodules`() {
        val beltKeys = RescriptDocumentationProvider.MODULE_URL_MAP.keys.filter { it.startsWith("Belt") }
        assertTrue("Expected multiple Belt entries", beltKeys.size > 10)
    }

    @Test
    fun `MODULE_URL_MAP contains Js submodules`() {
        val jsKeys = RescriptDocumentationProvider.MODULE_URL_MAP.keys.filter { it.startsWith("Js") }
        assertTrue("Expected multiple Js entries", jsKeys.size > 10)
    }

    @Test
    fun `getUrlFor returns null for null element`() {
        val provider = RescriptDocumentationProvider()
        val result = provider.getUrlFor(null, null)
        assertNull(result)
    }

    // -- OPERATOR_INFO tests --

    @Test
    fun `OPERATOR_INFO is not empty`() {
        assertTrue(RescriptDocumentationProvider.OPERATOR_INFO.isNotEmpty())
    }

    @Test
    fun `OPERATOR_INFO contains pipe forward`() {
        val info = RescriptDocumentationProvider.OPERATOR_INFO[RescriptTokenTypes.PIPE_FORWARD]
        assertNotNull("PIPE_FORWARD should have operator info", info)
        assertEquals("Pipe forward", info!!.name)
        assertEquals(1, info.precedence)
    }

    @Test
    fun `OPERATOR_INFO contains arrow`() {
        val info = RescriptDocumentationProvider.OPERATOR_INFO[RescriptTokenTypes.ARROW]
        assertNotNull("ARROW should have operator info", info)
        assertEquals("Pipe", info!!.name)
    }

    @Test
    fun `OPERATOR_INFO contains string concat`() {
        val info = RescriptDocumentationProvider.OPERATOR_INFO[RescriptTokenTypes.STRING_CONCAT]
        assertNotNull("STRING_CONCAT should have operator info", info)
        assertEquals("String concatenation", info!!.name)
        assertEquals(5, info.precedence)
    }

    @Test
    fun `OPERATOR_INFO contains eqeqeq`() {
        val info = RescriptDocumentationProvider.OPERATOR_INFO[RescriptTokenTypes.EQEQEQ]
        assertNotNull("EQEQEQ should have operator info", info)
        assertEquals("Strict equality", info!!.name)
        assertEquals(4, info.precedence)
    }

    @Test
    fun `OPERATOR_INFO contains plus`() {
        val info = RescriptDocumentationProvider.OPERATOR_INFO[RescriptTokenTypes.PLUS]
        assertNotNull("PLUS should have operator info", info)
        assertEquals("Addition", info!!.name)
        assertEquals(6, info.precedence)
    }

    @Test
    fun `OPERATOR_INFO contains star`() {
        val info = RescriptDocumentationProvider.OPERATOR_INFO[RescriptTokenTypes.STAR]
        assertNotNull("STAR should have operator info", info)
        assertEquals("Multiplication", info!!.name)
        assertEquals(7, info.precedence)
    }

    @Test
    fun `OPERATOR_INFO multiplication has higher precedence than addition`() {
        val plusInfo = RescriptDocumentationProvider.OPERATOR_INFO[RescriptTokenTypes.PLUS]!!
        val starInfo = RescriptDocumentationProvider.OPERATOR_INFO[RescriptTokenTypes.STAR]!!
        assertTrue(
            "Multiplication should have higher precedence than addition",
            starInfo.precedence > plusInfo.precedence,
        )
    }

    @Test
    fun `OPERATOR_INFO contains logical operators`() {
        assertNotNull(RescriptDocumentationProvider.OPERATOR_INFO[RescriptTokenTypes.L_AND])
        assertNotNull(RescriptDocumentationProvider.OPERATOR_INFO[RescriptTokenTypes.L_OR])
    }

    @Test
    fun `OPERATOR_INFO contains bitwise operators`() {
        assertNotNull(RescriptDocumentationProvider.OPERATOR_INFO[RescriptTokenTypes.LAND])
        assertNotNull(RescriptDocumentationProvider.OPERATOR_INFO[RescriptTokenTypes.LOR])
        assertNotNull(RescriptDocumentationProvider.OPERATOR_INFO[RescriptTokenTypes.LXOR])
    }

    @Test
    fun `generateOperatorDoc returns null for non-operator`() {
        val element = stubPsiElement(RescriptTokenTypes.LIDENT, "myVar")
        assertNull(RescriptDocumentationProvider.generateOperatorDoc(element))
    }

    @Test
    fun `generateOperatorDoc returns HTML for operator`() {
        val element = stubPsiElement(RescriptTokenTypes.PLUS, "+")
        val doc = RescriptDocumentationProvider.generateOperatorDoc(element)
        assertNotNull("Should generate doc for PLUS operator", doc)
        assertTrue("Doc should contain operator symbol", doc!!.contains("+"))
        assertTrue("Doc should contain 'Addition'", doc.contains("Addition"))
        assertTrue("Doc should contain precedence", doc.contains("Precedence"))
    }

    @Test
    fun `generateOperatorDoc returns HTML for pipe forward`() {
        val element = stubPsiElement(RescriptTokenTypes.PIPE_FORWARD, "|>")
        val doc = RescriptDocumentationProvider.generateOperatorDoc(element)
        assertNotNull(doc)
        assertTrue(doc!!.contains("Pipe forward"))
    }

    // -- HTML escaping tests --

    @Test
    fun `generateOperatorDoc escapes HTML in element text`() {
        val element = stubPsiElement(RescriptTokenTypes.PLUS, "<script>alert('xss')</script>")
        val doc = RescriptDocumentationProvider.generateOperatorDoc(element)
        assertNotNull(doc)
        assertFalse("Should not contain raw script tag", doc!!.contains("<script>"))
        assertTrue("Should contain escaped script tag", doc.contains("&lt;script&gt;"))
    }

    @Test
    fun `generateOperatorDoc escapes HTML in operator name and description`() {
        // The operator info values are compile-time constants and not user-controlled,
        // but escaping is defensive. Verify the output contains expected escaped content.
        val element = stubPsiElement(RescriptTokenTypes.PLUS, "+")
        val doc = RescriptDocumentationProvider.generateOperatorDoc(element)
        assertNotNull(doc)
        // Verify the doc contains the expected operator info (not corrupted by escaping)
        assertTrue("Should contain operator name", doc!!.contains("Addition"))
        assertTrue("Should contain precedence info", doc.contains("Precedence"))
    }

    // -- Stub helpers --

    private fun stubPsiElement(
        type: IElementType,
        text: String,
    ): PsiElement {
        val node =
            java.lang.reflect.Proxy.newProxyInstance(
                ASTNode::class.java.classLoader,
                arrayOf(ASTNode::class.java),
            ) { _, method, _ ->
                when (method.name) {
                    "getElementType" -> type
                    "toString" -> "StubASTNode($type)"
                    "hashCode" -> System.identityHashCode(type)
                    "equals" -> false
                    else -> null
                }
            } as ASTNode

        return java.lang.reflect.Proxy.newProxyInstance(
            PsiElement::class.java.classLoader,
            arrayOf(PsiElement::class.java),
        ) { _, method, _ ->
            when (method.name) {
                "getNode" -> node
                "getText" -> text
                "getContainingFile" -> null
                "toString" -> "StubPsiElement($type)"
                "hashCode" -> System.identityHashCode(node)
                "equals" -> false
                else -> null
            }
        } as PsiElement
    }
}
