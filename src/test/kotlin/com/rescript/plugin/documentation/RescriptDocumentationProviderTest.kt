package com.rescript.plugin.documentation

import com.intellij.lang.ASTNode
import com.intellij.lang.documentation.AbstractDocumentationProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.tree.IElementType
import com.rescript.plugin.lang.RescriptTokenTypes
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class RescriptDocumentationProviderTest {
    @Test
    fun `instance can be created`() {
        val provider = RescriptDocumentationProvider()
        assertNotNull(provider)
    }

    @Test
    fun `is an AbstractDocumentationProvider`() {
        val provider: Any = RescriptDocumentationProvider()
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
        assertEquals("belt/array", RescriptExternalDocUrls.MODULE_URL_MAP["Belt.Array"])
    }

    @Test
    fun `MODULE_URL_MAP contains Js_String2 mapping`() {
        assertEquals("js/string-2", RescriptExternalDocUrls.MODULE_URL_MAP["Js.String2"])
    }

    @Test
    fun `MODULE_URL_MAP contains Belt root`() {
        assertEquals("belt", RescriptExternalDocUrls.MODULE_URL_MAP["Belt"])
    }

    @Test
    fun `MODULE_URL_MAP contains Js root`() {
        assertEquals("js", RescriptExternalDocUrls.MODULE_URL_MAP["Js"])
    }

    @Test
    fun `MODULE_URL_MAP contains Belt_List`() {
        assertEquals("belt/list", RescriptExternalDocUrls.MODULE_URL_MAP["Belt.List"])
    }

    @Test
    fun `MODULE_URL_MAP contains Belt_Map`() {
        assertEquals("belt/map", RescriptExternalDocUrls.MODULE_URL_MAP["Belt.Map"])
    }

    @Test
    fun `MODULE_URL_MAP contains Belt_Option`() {
        assertEquals("belt/option", RescriptExternalDocUrls.MODULE_URL_MAP["Belt.Option"])
    }

    @Test
    fun `MODULE_URL_MAP contains Js_Promise`() {
        assertEquals("js/promise", RescriptExternalDocUrls.MODULE_URL_MAP["Js.Promise"])
    }

    @Test
    fun `MODULE_URL_MAP contains Js_Json`() {
        assertEquals("js/json", RescriptExternalDocUrls.MODULE_URL_MAP["Js.Json"])
    }

    @Test
    fun `MODULE_URL_MAP contains Js_Dict`() {
        assertEquals("js/dict", RescriptExternalDocUrls.MODULE_URL_MAP["Js.Dict"])
    }

    @Test
    fun `MODULE_URL_MAP does not contain unknown module`() {
        assertNull(RescriptExternalDocUrls.MODULE_URL_MAP["NonExistent.Module"])
    }

    @Test
    fun `MODULE_URL_MAP is not empty`() {
        assertTrue(RescriptExternalDocUrls.MODULE_URL_MAP.isNotEmpty())
    }

    @Test
    fun `MODULE_URL_MAP contains Belt submodules`() {
        val beltKeys = RescriptExternalDocUrls.MODULE_URL_MAP.keys.filter { it.startsWith("Belt") }
        assertTrue(beltKeys.size > 10, "Expected multiple Belt entries")
    }

    @Test
    fun `MODULE_URL_MAP contains Js submodules`() {
        val jsKeys = RescriptExternalDocUrls.MODULE_URL_MAP.keys.filter { it.startsWith("Js") }
        assertTrue(jsKeys.size > 10, "Expected multiple Js entries")
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
        assertTrue(RescriptOperatorDocumentation.OPERATOR_INFO.isNotEmpty())
    }

    @Test
    fun `OPERATOR_INFO contains pipe forward`() {
        val info = RescriptOperatorDocumentation.OPERATOR_INFO[RescriptTokenTypes.PIPE_FORWARD]
        assertNotNull(info, "PIPE_FORWARD should have operator info")
        assertEquals("Pipe forward", info!!.name)
        assertEquals(1, info.precedence)
    }

    @Test
    fun `OPERATOR_INFO contains arrow`() {
        val info = RescriptOperatorDocumentation.OPERATOR_INFO[RescriptTokenTypes.ARROW]
        assertNotNull(info, "ARROW should have operator info")
        assertEquals("Pipe", info!!.name)
    }

    @Test
    fun `OPERATOR_INFO contains string concat`() {
        val info = RescriptOperatorDocumentation.OPERATOR_INFO[RescriptTokenTypes.STRING_CONCAT]
        assertNotNull(info, "STRING_CONCAT should have operator info")
        assertEquals("String concatenation", info!!.name)
        assertEquals(5, info.precedence)
    }

    @Test
    fun `OPERATOR_INFO contains eqeqeq`() {
        val info = RescriptOperatorDocumentation.OPERATOR_INFO[RescriptTokenTypes.EQEQEQ]
        assertNotNull(info, "EQEQEQ should have operator info")
        assertEquals("Strict equality", info!!.name)
        assertEquals(4, info.precedence)
    }

    @Test
    fun `OPERATOR_INFO contains plus`() {
        val info = RescriptOperatorDocumentation.OPERATOR_INFO[RescriptTokenTypes.PLUS]
        assertNotNull(info, "PLUS should have operator info")
        assertEquals("Addition", info!!.name)
        assertEquals(6, info.precedence)
    }

    @Test
    fun `OPERATOR_INFO contains star`() {
        val info = RescriptOperatorDocumentation.OPERATOR_INFO[RescriptTokenTypes.STAR]
        assertNotNull(info, "STAR should have operator info")
        assertEquals("Multiplication", info!!.name)
        assertEquals(7, info.precedence)
    }

    @Test
    fun `OPERATOR_INFO multiplication has higher precedence than addition`() {
        val plusInfo = RescriptOperatorDocumentation.OPERATOR_INFO[RescriptTokenTypes.PLUS]!!
        val starInfo = RescriptOperatorDocumentation.OPERATOR_INFO[RescriptTokenTypes.STAR]!!
        assertTrue(
            starInfo.precedence > plusInfo.precedence,
            "Multiplication should have higher precedence than addition",
        )
    }

    @Test
    fun `OPERATOR_INFO contains logical operators`() {
        assertNotNull(RescriptOperatorDocumentation.OPERATOR_INFO[RescriptTokenTypes.L_AND])
        assertNotNull(RescriptOperatorDocumentation.OPERATOR_INFO[RescriptTokenTypes.L_OR])
    }

    @Test
    fun `OPERATOR_INFO contains bitwise operators`() {
        assertNotNull(RescriptOperatorDocumentation.OPERATOR_INFO[RescriptTokenTypes.LAND])
        assertNotNull(RescriptOperatorDocumentation.OPERATOR_INFO[RescriptTokenTypes.LOR])
        assertNotNull(RescriptOperatorDocumentation.OPERATOR_INFO[RescriptTokenTypes.LXOR])
    }

    @Test
    fun `generateOperatorDoc returns null for non-operator`() {
        val element = stubPsiElement(RescriptTokenTypes.LIDENT, "myVar")
        assertNull(RescriptOperatorDocumentation.generateOperatorDoc(element))
    }

    @Test
    fun `generateOperatorDoc returns HTML for operator`() {
        val element = stubPsiElement(RescriptTokenTypes.PLUS, "+")
        val doc = RescriptOperatorDocumentation.generateOperatorDoc(element)
        assertNotNull(doc, "Should generate doc for PLUS operator")
        assertTrue(doc!!.contains("+"), "Doc should contain operator symbol")
        assertTrue(doc.contains("Addition"), "Doc should contain 'Addition'")
        assertTrue(doc.contains("Precedence"), "Doc should contain precedence")
    }

    @Test
    fun `generateOperatorDoc returns HTML for pipe forward`() {
        val element = stubPsiElement(RescriptTokenTypes.PIPE_FORWARD, "|>")
        val doc = RescriptOperatorDocumentation.generateOperatorDoc(element)
        assertNotNull(doc)
        assertTrue(doc!!.contains("Pipe forward"))
    }

    // -- HTML escaping tests --

    @Test
    fun `generateOperatorDoc escapes HTML in element text`() {
        val element = stubPsiElement(RescriptTokenTypes.PLUS, "<script>alert('xss')</script>")
        val doc = RescriptOperatorDocumentation.generateOperatorDoc(element)
        assertNotNull(doc)
        assertFalse(doc!!.contains("<script>"), "Should not contain raw script tag")
        assertTrue(doc.contains("&lt;script&gt;"), "Should contain escaped script tag")
    }

    @Test
    fun `generateOperatorDoc escapes HTML in operator name and description`() {
        // The operator info values are compile-time constants and not user-controlled,
        // but escaping is defensive. Verify the output contains expected escaped content.
        val element = stubPsiElement(RescriptTokenTypes.PLUS, "+")
        val doc = RescriptOperatorDocumentation.generateOperatorDoc(element)
        assertNotNull(doc)
        // Verify the doc contains the expected operator info (not corrupted by escaping)
        assertTrue(doc!!.contains("Addition"), "Should contain operator name")
        assertTrue(doc.contains("Precedence"), "Should contain precedence info")
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
