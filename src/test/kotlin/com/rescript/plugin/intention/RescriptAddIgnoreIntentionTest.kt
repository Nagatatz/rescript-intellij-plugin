package com.rescript.plugin.intention

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.tree.IElementType
import com.rescript.plugin.lang.psi.RescriptElementTypes
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RescriptAddIgnoreIntentionTest {
    @Test
    fun testIntentionText() {
        val intention = RescriptAddIgnoreIntention()
        assertEquals("Add ->ignore", intention.text)
    }

    @Test
    fun testIntentionFamilyName() {
        val intention = RescriptAddIgnoreIntention()
        assertEquals("Add ->ignore", intention.familyName)
    }

    @Test
    fun testStartInWriteAction() {
        val intention = RescriptAddIgnoreIntention()
        assertTrue(intention.startInWriteAction())
    }

    @Test
    fun testTextAndFamilyNameAreConsistent() {
        val intention = RescriptAddIgnoreIntention()
        assertEquals(intention.text, intention.familyName)
    }

    // -- findParentDeclaration tests --

    @Test
    fun testFindParentDeclarationForLetDeclaration() {
        val letDecl = stubPsiElement(RescriptElementTypes.LET_DECLARATION)
        val child = stubPsiElement(RescriptElementTypes.ANNOTATION, parent = letDecl)
        val result = RescriptAddIgnoreIntention.findParentDeclaration(child)
        assertNotNull(result)
        assertEquals(RescriptElementTypes.LET_DECLARATION, result!!.node?.elementType)
    }

    @Test
    fun testFindParentDeclarationForExternalDeclaration() {
        val externalDecl = stubPsiElement(RescriptElementTypes.EXTERNAL_DECLARATION)
        val child = stubPsiElement(RescriptElementTypes.ANNOTATION, parent = externalDecl)
        val result = RescriptAddIgnoreIntention.findParentDeclaration(child)
        assertNotNull(result)
        assertEquals(RescriptElementTypes.EXTERNAL_DECLARATION, result!!.node?.elementType)
    }

    @Test
    fun testFindParentDeclarationReturnsNullForNonDeclaration() {
        val openStmt = stubPsiElement(RescriptElementTypes.OPEN_STATEMENT, parent = null)
        val result = RescriptAddIgnoreIntention.findParentDeclaration(openStmt)
        assertNull(result)
    }

    @Test
    fun testFindParentDeclarationReturnsNullForModuleDeclaration() {
        val moduleDecl = stubPsiElement(RescriptElementTypes.MODULE_DECLARATION, parent = null)
        val result = RescriptAddIgnoreIntention.findParentDeclaration(moduleDecl)
        assertNull(result)
    }

    // -- alreadyHasIgnore tests --

    @Test
    fun testAlreadyHasIgnoreReturnsTrueForIgnore() {
        assertTrue(RescriptAddIgnoreIntention.alreadyHasIgnore("expr->ignore"))
    }

    @Test
    fun testAlreadyHasIgnoreReturnsTrueForIgnoreWithSpace() {
        assertTrue(RescriptAddIgnoreIntention.alreadyHasIgnore("expr-> ignore"))
    }

    @Test
    fun testAlreadyHasIgnoreReturnsTrueWithTrailingWhitespace() {
        assertTrue(RescriptAddIgnoreIntention.alreadyHasIgnore("expr->ignore  "))
    }

    @Test
    fun testAlreadyHasIgnoreReturnsFalseForOther() {
        assertFalse(RescriptAddIgnoreIntention.alreadyHasIgnore("expr->map"))
    }

    @Test
    fun testAlreadyHasIgnoreReturnsFalseForEmpty() {
        assertFalse(RescriptAddIgnoreIntention.alreadyHasIgnore(""))
    }

    @Test
    fun testAlreadyHasIgnoreReturnsFalseForSimpleExpr() {
        assertFalse(RescriptAddIgnoreIntention.alreadyHasIgnore("let x = 1"))
    }

    @Test
    fun testIsAvailableReturnsFalseForNullEditor() {
        val intention = RescriptAddIgnoreIntention()
        val element = stubPsiElement(RescriptElementTypes.LET_DECLARATION)
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
