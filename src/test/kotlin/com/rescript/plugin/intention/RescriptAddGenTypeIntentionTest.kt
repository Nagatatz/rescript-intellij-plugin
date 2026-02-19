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

class RescriptAddGenTypeIntentionTest {
    @Test
    fun testIntentionText() {
        val intention = RescriptAddGenTypeIntention()
        assertEquals("Add @genType annotation", intention.text)
    }

    @Test
    fun testIntentionFamilyName() {
        val intention = RescriptAddGenTypeIntention()
        assertEquals("Add @genType annotation", intention.familyName)
    }

    @Test
    fun testStartInWriteAction() {
        val intention = RescriptAddGenTypeIntention()
        assertTrue(intention.startInWriteAction())
    }

    @Test
    fun testTextAndFamilyNameAreConsistent() {
        val intention = RescriptAddGenTypeIntention()
        assertEquals(intention.text, intention.familyName)
    }

    // -- findParentDeclaration PSI stub tests --

    private fun stubPsiElement(
        type: IElementType,
        parent: PsiElement? = null,
        prevSibling: PsiElement? = null,
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
                "getPrevSibling" -> prevSibling
                "getText" -> text
                "toString" -> "StubPsiElement($type)"
                "hashCode" -> System.identityHashCode(node)
                "equals" -> false
                else -> null
            }
        } as PsiElement
    }

    @Test
    fun testFindParentDeclarationForLetDeclaration() {
        val letDecl = stubPsiElement(RescriptElementTypes.LET_DECLARATION)
        val child = stubPsiElement(RescriptElementTypes.ANNOTATION, parent = letDecl)
        val result = RescriptAddGenTypeIntention.findParentDeclaration(child)
        assertNotNull(result)
        assertEquals(RescriptElementTypes.LET_DECLARATION, result!!.node?.elementType)
    }

    @Test
    fun testFindParentDeclarationForTypeDeclaration() {
        val typeDecl = stubPsiElement(RescriptElementTypes.TYPE_DECLARATION)
        val child = stubPsiElement(RescriptElementTypes.ANNOTATION, parent = typeDecl)
        val result = RescriptAddGenTypeIntention.findParentDeclaration(child)
        assertNotNull(result)
        assertEquals(RescriptElementTypes.TYPE_DECLARATION, result!!.node?.elementType)
    }

    @Test
    fun testFindParentDeclarationForModuleDeclaration() {
        val moduleDecl = stubPsiElement(RescriptElementTypes.MODULE_DECLARATION)
        val child = stubPsiElement(RescriptElementTypes.ANNOTATION, parent = moduleDecl)
        val result = RescriptAddGenTypeIntention.findParentDeclaration(child)
        assertNotNull(result)
        assertEquals(RescriptElementTypes.MODULE_DECLARATION, result!!.node?.elementType)
    }

    @Test
    fun testFindParentDeclarationReturnsNullForNonDeclaration() {
        val annotation = stubPsiElement(RescriptElementTypes.ANNOTATION, parent = null)
        val result = RescriptAddGenTypeIntention.findParentDeclaration(annotation)
        assertNull(result)
    }

    @Test
    fun testFindParentDeclarationFindsDirectDeclaration() {
        val letDecl = stubPsiElement(RescriptElementTypes.LET_DECLARATION)
        val result = RescriptAddGenTypeIntention.findParentDeclaration(letDecl)
        assertNotNull(result)
        assertEquals(RescriptElementTypes.LET_DECLARATION, result!!.node?.elementType)
    }

    @Test
    fun testFindParentDeclarationIgnoresOpenStatement() {
        // OPEN_STATEMENT is NOT in the DECLARATION_TYPES for @genType
        val openStmt = stubPsiElement(RescriptElementTypes.OPEN_STATEMENT, parent = null)
        val result = RescriptAddGenTypeIntention.findParentDeclaration(openStmt)
        assertNull(result)
    }

    // -- hasGenTypeAnnotation PSI stub tests --

    @Test
    fun testHasGenTypeAnnotationReturnsTrueForGenType() {
        val annotation =
            stubPsiElement(
                RescriptElementTypes.ANNOTATION,
                text = "@genType",
            )
        val declaration =
            stubPsiElement(
                RescriptElementTypes.LET_DECLARATION,
                prevSibling = annotation,
            )
        assertTrue(RescriptAddGenTypeIntention.hasGenTypeAnnotation(declaration))
    }

    @Test
    fun testHasGenTypeAnnotationReturnsTrueForGenTypeWithArgs() {
        val annotation =
            stubPsiElement(
                RescriptElementTypes.ANNOTATION,
                text = "@genType(opaque)",
            )
        val declaration =
            stubPsiElement(
                RescriptElementTypes.LET_DECLARATION,
                prevSibling = annotation,
            )
        assertTrue(RescriptAddGenTypeIntention.hasGenTypeAnnotation(declaration))
    }

    @Test
    fun testHasGenTypeAnnotationReturnsFalseForOtherAnnotation() {
        val annotation =
            stubPsiElement(
                RescriptElementTypes.ANNOTATION,
                text = "@module",
            )
        val declaration =
            stubPsiElement(
                RescriptElementTypes.LET_DECLARATION,
                prevSibling = annotation,
            )
        assertFalse(RescriptAddGenTypeIntention.hasGenTypeAnnotation(declaration))
    }

    @Test
    fun testHasGenTypeAnnotationReturnsFalseForNoPrevSibling() {
        val declaration =
            stubPsiElement(
                RescriptElementTypes.LET_DECLARATION,
                prevSibling = null,
            )
        assertFalse(RescriptAddGenTypeIntention.hasGenTypeAnnotation(declaration))
    }

    @Test
    fun testHasGenTypeAnnotationSkipsWhitespaceBetween() {
        // Whitespace (blank text) between annotation and declaration is skipped
        val annotation =
            stubPsiElement(
                RescriptElementTypes.ANNOTATION,
                text = "@genType",
                prevSibling = null,
            )
        val whitespace =
            stubPsiElement(
                RescriptElementTypes.ANNOTATION, // type doesn't matter since text is blank
                text = "  ",
                prevSibling = annotation,
            )
        // Actually, the whitespace element's text is blank so it won't break the loop
        // But we need to use a non-ANNOTATION type for whitespace
        // Let's use a proper whitespace element
        val ws = stubWhitespace(annotation)
        val declaration =
            stubPsiElement(
                RescriptElementTypes.LET_DECLARATION,
                prevSibling = ws,
            )
        assertTrue(RescriptAddGenTypeIntention.hasGenTypeAnnotation(declaration))
    }

    @Test
    fun testHasGenTypeAnnotationReturnsFalseForNonAnnotationPrev() {
        // A non-blank, non-annotation prev sibling breaks the loop
        val otherDecl =
            stubPsiElement(
                RescriptElementTypes.LET_DECLARATION,
                text = "let x = 1",
                prevSibling = null,
            )
        val declaration =
            stubPsiElement(
                RescriptElementTypes.TYPE_DECLARATION,
                prevSibling = otherDecl,
            )
        assertFalse(RescriptAddGenTypeIntention.hasGenTypeAnnotation(declaration))
    }

    @Test
    fun testFindParentDeclarationWalksMultipleLevels() {
        val moduleDecl = stubPsiElement(RescriptElementTypes.MODULE_DECLARATION)
        val intermediate = stubPsiElement(RescriptElementTypes.ANNOTATION, parent = moduleDecl)
        val child = stubPsiElement(RescriptElementTypes.ANNOTATION, parent = intermediate)
        val result = RescriptAddGenTypeIntention.findParentDeclaration(child)
        assertNotNull(result)
        assertEquals(RescriptElementTypes.MODULE_DECLARATION, result!!.node?.elementType)
    }

    @Test
    fun testIsAvailableReturnsFalseForNullEditor() {
        val intention = RescriptAddGenTypeIntention()
        val element = stubPsiElement(RescriptElementTypes.LET_DECLARATION)
        val project = stubProject()
        // containingFile returns null (not RescriptFile), so returns false
        assertFalse(intention.isAvailable(project, null, element))
    }

    private fun stubWhitespace(prevSibling: PsiElement?): PsiElement =
        java.lang.reflect.Proxy.newProxyInstance(
            PsiElement::class.java.classLoader,
            arrayOf(PsiElement::class.java),
        ) { _, method, _ ->
            when (method.name) {
                "getNode" -> null
                "getParent" -> null
                "getPrevSibling" -> prevSibling
                "getText" -> "\n"
                "toString" -> "StubWhitespace"
                "hashCode" -> 0
                "equals" -> false
                else -> null
            }
        } as PsiElement

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
