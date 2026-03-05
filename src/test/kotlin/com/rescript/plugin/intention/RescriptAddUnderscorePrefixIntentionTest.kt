package com.rescript.plugin.intention

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.tree.IElementType
import com.rescript.plugin.lang.RescriptTokenTypes
import com.rescript.plugin.lang.psi.RescriptElementTypes
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class RescriptAddUnderscorePrefixIntentionTest {
    @Test
    fun testIntentionText() {
        val intention = RescriptAddUnderscorePrefixIntention()
        assertEquals("Add _ prefix to suppress unused warning", intention.text)
    }

    @Test
    fun testIntentionFamilyName() {
        val intention = RescriptAddUnderscorePrefixIntention()
        assertEquals("Add _ prefix to suppress unused warning", intention.familyName)
    }

    @Test
    fun testStartInWriteAction() {
        val intention = RescriptAddUnderscorePrefixIntention()
        assertTrue(intention.startInWriteAction())
    }

    @Test
    fun testTextAndFamilyNameAreConsistent() {
        val intention = RescriptAddUnderscorePrefixIntention()
        assertEquals(intention.text, intention.familyName)
    }

    // -- hasParentDeclaration tests --

    @Test
    fun testHasParentDeclarationForLetDeclaration() {
        val letDecl = stubPsiElement(RescriptElementTypes.LET_DECLARATION)
        val child = stubPsiElement(RescriptTokenTypes.LIDENT, parent = letDecl, text = "myVar")
        assertTrue(RescriptAddUnderscorePrefixIntention.hasParentDeclaration(child))
    }

    @Test
    fun testHasParentDeclarationForExternalDeclaration() {
        val externalDecl = stubPsiElement(RescriptElementTypes.EXTERNAL_DECLARATION)
        val child = stubPsiElement(RescriptTokenTypes.LIDENT, parent = externalDecl, text = "myExternal")
        assertTrue(RescriptAddUnderscorePrefixIntention.hasParentDeclaration(child))
    }

    @Test
    fun testHasParentDeclarationReturnsFalseForModuleDeclaration() {
        val moduleDecl = stubPsiElement(RescriptElementTypes.MODULE_DECLARATION)
        val child = stubPsiElement(RescriptTokenTypes.LIDENT, parent = moduleDecl, text = "myModule")
        assertFalse(RescriptAddUnderscorePrefixIntention.hasParentDeclaration(child))
    }

    @Test
    fun testHasParentDeclarationReturnsFalseForNoParent() {
        val child = stubPsiElement(RescriptTokenTypes.LIDENT, parent = null, text = "orphan")
        assertFalse(RescriptAddUnderscorePrefixIntention.hasParentDeclaration(child))
    }

    @Test
    fun testIsAvailableReturnsFalseForNullEditor() {
        val intention = RescriptAddUnderscorePrefixIntention()
        val element = stubPsiElement(RescriptTokenTypes.LIDENT, text = "x")
        val project = stubProject()
        assertFalse(intention.isAvailable(project, null, element))
    }

    // -- Stub helpers --

    private fun stubPsiElement(
        type: IElementType,
        parent: PsiElement? = null,
        text: String = "",
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
                "getParent" -> parent
                "getText" -> text
                "getContainingFile" -> null
                "toString" -> "StubPsiElement($type)"
                "hashCode" -> System.identityHashCode(node)
                "equals" -> false
                else -> null
            }
        } as PsiElement
    }

    private fun stubProject(): com.intellij.openapi.project.Project =
        java.lang.reflect.Proxy.newProxyInstance(
            com.intellij.openapi.project.Project::class.java.classLoader,
            arrayOf(com.intellij.openapi.project.Project::class.java),
        ) { _, method, _ ->
            when (method.name) {
                "toString" -> "StubProject"
                "hashCode" -> 0
                "equals" -> false
                else -> null
            }
        } as com.intellij.openapi.project.Project
}
