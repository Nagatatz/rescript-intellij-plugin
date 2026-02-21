package com.rescript.plugin.intention

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.tree.IElementType
import com.rescript.plugin.lang.RescriptTokenTypes
import com.rescript.plugin.lang.psi.RescriptElementTypes
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RescriptFixIdentifierCaseIntentionTest {
    @Test
    fun testFamilyName() {
        val intention = RescriptFixIdentifierCaseIntention()
        assertEquals("Fix identifier case", intention.familyName)
    }

    @Test
    fun testStartInWriteAction() {
        val intention = RescriptFixIdentifierCaseIntention()
        assertTrue(intention.startInWriteAction())
    }

    // -- toPascalCase tests --

    @Test
    fun testToPascalCaseConvertsFirstCharToUppercase() {
        assertEquals("MyModule", RescriptFixIdentifierCaseIntention.toPascalCase("myModule"))
    }

    @Test
    fun testToPascalCaseKeepsAlreadyPascalCase() {
        assertEquals("MyModule", RescriptFixIdentifierCaseIntention.toPascalCase("MyModule"))
    }

    @Test
    fun testToPascalCaseSingleChar() {
        assertEquals("A", RescriptFixIdentifierCaseIntention.toPascalCase("a"))
    }

    @Test
    fun testToPascalCaseEmpty() {
        assertEquals("", RescriptFixIdentifierCaseIntention.toPascalCase(""))
    }

    // -- toCamelCase tests --

    @Test
    fun testToCamelCaseConvertsFirstCharToLowercase() {
        assertEquals("myVar", RescriptFixIdentifierCaseIntention.toCamelCase("MyVar"))
    }

    @Test
    fun testToCamelCaseKeepsAlreadyCamelCase() {
        assertEquals("myVar", RescriptFixIdentifierCaseIntention.toCamelCase("myVar"))
    }

    @Test
    fun testToCamelCaseSingleChar() {
        assertEquals("a", RescriptFixIdentifierCaseIntention.toCamelCase("A"))
    }

    @Test
    fun testToCamelCaseEmpty() {
        assertEquals("", RescriptFixIdentifierCaseIntention.toCamelCase(""))
    }

    // -- isInsideModuleDeclaration tests --

    @Test
    fun testIsInsideModuleDeclarationReturnsTrue() {
        val moduleDecl = stubPsiElement(RescriptElementTypes.MODULE_DECLARATION)
        val child = stubPsiElement(RescriptTokenTypes.LIDENT, parent = moduleDecl)
        assertTrue(RescriptFixIdentifierCaseIntention.isInsideModuleDeclaration(child))
    }

    @Test
    fun testIsInsideModuleDeclarationReturnsFalseForLetDecl() {
        val letDecl = stubPsiElement(RescriptElementTypes.LET_DECLARATION)
        val child = stubPsiElement(RescriptTokenTypes.LIDENT, parent = letDecl)
        assertFalse(RescriptFixIdentifierCaseIntention.isInsideModuleDeclaration(child))
    }

    @Test
    fun testIsInsideModuleDeclarationReturnsFalseForNoParent() {
        val child = stubPsiElement(RescriptTokenTypes.LIDENT, parent = null)
        assertFalse(RescriptFixIdentifierCaseIntention.isInsideModuleDeclaration(child))
    }

    // -- isInsideLetDeclaration tests --

    @Test
    fun testIsInsideLetDeclarationReturnsTrue() {
        val letDecl = stubPsiElement(RescriptElementTypes.LET_DECLARATION)
        val child = stubPsiElement(RescriptTokenTypes.UIDENT, parent = letDecl)
        assertTrue(RescriptFixIdentifierCaseIntention.isInsideLetDeclaration(child))
    }

    @Test
    fun testIsInsideLetDeclarationReturnsFalseForModuleDecl() {
        val moduleDecl = stubPsiElement(RescriptElementTypes.MODULE_DECLARATION)
        val child = stubPsiElement(RescriptTokenTypes.UIDENT, parent = moduleDecl)
        assertFalse(RescriptFixIdentifierCaseIntention.isInsideLetDeclaration(child))
    }

    @Test
    fun testIsInsideLetDeclarationReturnsFalseForNoParent() {
        val child = stubPsiElement(RescriptTokenTypes.UIDENT, parent = null)
        assertFalse(RescriptFixIdentifierCaseIntention.isInsideLetDeclaration(child))
    }

    @Test
    fun testIsAvailableReturnsFalseForNullEditor() {
        val intention = RescriptFixIdentifierCaseIntention()
        val element = stubPsiElement(RescriptTokenTypes.LIDENT)
        val project = stubProject()
        assertFalse(intention.isAvailable(project, null, element))
    }

    // -- Stub helpers --

    private fun stubPsiElement(
        type: IElementType,
        parent: PsiElement? = null,
        text: String = "identifier",
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
