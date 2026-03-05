package com.rescript.plugin.breadcrumb

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.tree.IElementType
import com.rescript.plugin.RescriptLanguage
import com.rescript.plugin.lang.psi.RescriptElementTypes
import com.rescript.plugin.lang.psi.RescriptPsiUtils
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class RescriptBreadcrumbsProviderTest {
    private val provider = RescriptBreadcrumbsProvider()

    private fun stubElement(type: IElementType): PsiElement {
        val node =
            java.lang.reflect.Proxy.newProxyInstance(
                ASTNode::class.java.classLoader,
                arrayOf(ASTNode::class.java),
            ) { _, method, _ ->
                when (method.name) {
                    "getElementType" -> type
                    "getFirstChildNode" -> null
                    "getTreeNext" -> null
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
                "getChildren" -> emptyArray<PsiElement>()
                "toString" -> "StubPsiElement($type)"
                "hashCode" -> System.identityHashCode(type)
                "equals" -> false
                else -> null
            }
        } as PsiElement
    }

    @Test
    fun testGetLanguagesReturnsRescriptLanguage() {
        val languages = provider.languages
        assertEquals(1, languages.size)
        assertEquals(RescriptLanguage, languages[0])
    }

    @Test
    fun testAcceptElementReturnsTrueForLetDeclaration() {
        assertTrue(provider.acceptElement(stubElement(RescriptElementTypes.LET_DECLARATION)))
    }

    @Test
    fun testAcceptElementReturnsTrueForTypeDeclaration() {
        assertTrue(provider.acceptElement(stubElement(RescriptElementTypes.TYPE_DECLARATION)))
    }

    @Test
    fun testAcceptElementReturnsTrueForModuleDeclaration() {
        assertTrue(provider.acceptElement(stubElement(RescriptElementTypes.MODULE_DECLARATION)))
    }

    @Test
    fun testAcceptElementReturnsTrueForExternalDeclaration() {
        assertTrue(provider.acceptElement(stubElement(RescriptElementTypes.EXTERNAL_DECLARATION)))
    }

    @Test
    fun testAcceptElementReturnsTrueForExceptionDeclaration() {
        assertTrue(provider.acceptElement(stubElement(RescriptElementTypes.EXCEPTION_DECLARATION)))
    }

    @Test
    fun testAcceptElementReturnsFalseForAnnotation() {
        assertFalse(provider.acceptElement(stubElement(RescriptElementTypes.ANNOTATION)))
    }

    @Test
    fun testAcceptElementReturnsFalseForOpenStatement() {
        assertFalse(provider.acceptElement(stubElement(RescriptElementTypes.OPEN_STATEMENT)))
    }

    @Test
    fun testAcceptElementReturnsFalseForJsxElement() {
        assertFalse(provider.acceptElement(stubElement(RescriptElementTypes.JSX_ELEMENT)))
    }

    @Test
    fun testAcceptElementCoversAllNavigableTypes() {
        for (type in RescriptPsiUtils.NAVIGABLE_TYPES) {
            assertTrue(provider.acceptElement(stubElement(type)), "$type should be accepted")
        }
    }

    @Test
    fun testGetElementInfoReturnsAnonymousForNoChildren() {
        // Element with no identifier children returns "(anonymous)"
        val element = stubElement(RescriptElementTypes.LET_DECLARATION)
        assertEquals("(anonymous)", provider.getElementInfo(element))
    }
}
