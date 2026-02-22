package com.rescript.plugin.lang.psi

import com.intellij.icons.AllIcons
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.tree.IElementType
import com.rescript.plugin.RescriptTestUtils
import com.rescript.plugin.RescriptTestUtils.SimpleStubElement
import com.rescript.plugin.lang.RescriptTokenTypes
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RescriptPsiUtilsTest {
    @Test
    fun `extractName for LET_DECLARATION returns identifier`() {
        val node =
            RescriptTestUtils.stubAstNodeWithChildren(
                RescriptElementTypes.LET_DECLARATION,
                listOf(
                    RescriptTokenTypes.LET to "let",
                    RescriptTokenTypes.LIDENT to "foo",
                ),
            )
        val element = SimpleStubElement(RescriptElementTypes.LET_DECLARATION, "let foo = 1")
        // Override getNode to return our custom node
        val psiElement =
            object : SimpleStubElement(RescriptElementTypes.LET_DECLARATION, "let foo = 1") {
                override fun getNode() = node
            }
        assertEquals("foo", RescriptPsiUtils.extractName(psiElement))
    }

    @Test
    fun `extractName for MODULE_DECLARATION returns uident`() {
        val node =
            RescriptTestUtils.stubAstNodeWithChildren(
                RescriptElementTypes.MODULE_DECLARATION,
                listOf(
                    RescriptTokenTypes.MODULE to "module",
                    RescriptTokenTypes.UIDENT to "MyModule",
                ),
            )
        val psiElement =
            object : SimpleStubElement(RescriptElementTypes.MODULE_DECLARATION, "module MyModule = {}") {
                override fun getNode() = node
            }
        assertEquals("MyModule", RescriptPsiUtils.extractName(psiElement))
    }

    @Test
    fun `extractName for TYPE_DECLARATION returns identifier`() {
        val node =
            RescriptTestUtils.stubAstNodeWithChildren(
                RescriptElementTypes.TYPE_DECLARATION,
                listOf(
                    RescriptTokenTypes.TYPE to "type",
                    RescriptTokenTypes.LIDENT to "person",
                ),
            )
        val psiElement =
            object : SimpleStubElement(RescriptElementTypes.TYPE_DECLARATION, "type person = {}") {
                override fun getNode() = node
            }
        assertEquals("person", RescriptPsiUtils.extractName(psiElement))
    }

    @Test
    fun `extractName skips rec keyword`() {
        val node =
            RescriptTestUtils.stubAstNodeWithChildren(
                RescriptElementTypes.LET_DECLARATION,
                listOf(
                    RescriptTokenTypes.LET to "let",
                    RescriptTokenTypes.REC to "rec",
                    RescriptTokenTypes.LIDENT to "factorial",
                ),
            )
        val psiElement =
            object : SimpleStubElement(RescriptElementTypes.LET_DECLARATION, "let rec factorial = ...") {
                override fun getNode() = node
            }
        assertEquals("factorial", RescriptPsiUtils.extractName(psiElement))
    }

    @Test
    fun `extractName returns anonymous when no identifier found`() {
        val node =
            RescriptTestUtils.stubAstNodeWithChildren(
                RescriptElementTypes.LET_DECLARATION,
                listOf(
                    RescriptTokenTypes.LET to "let",
                ),
            )
        val psiElement =
            object : SimpleStubElement(RescriptElementTypes.LET_DECLARATION, "let") {
                override fun getNode() = node
            }
        assertEquals("(anonymous)", RescriptPsiUtils.extractName(psiElement))
    }

    @Test
    fun `extractName returns unknown when node is null`() {
        // Use a proxy-based PsiElement that returns null for getNode()
        val psiElement = RescriptTestUtils.stubProxy<com.intellij.psi.PsiElement>()
        assertEquals("(unknown)", RescriptPsiUtils.extractName(psiElement))
    }

    // --- getIcon ---

    @Test
    fun `getIcon for LET_DECLARATION returns Function icon`() {
        val element = SimpleStubElement(RescriptElementTypes.LET_DECLARATION, "let x = 1")
        assertEquals(AllIcons.Nodes.Function, RescriptPsiUtils.getIcon(element))
    }

    @Test
    fun `getIcon for TYPE_DECLARATION returns Type icon`() {
        val element = SimpleStubElement(RescriptElementTypes.TYPE_DECLARATION, "type t = int")
        assertEquals(AllIcons.Nodes.Type, RescriptPsiUtils.getIcon(element))
    }

    @Test
    fun `getIcon for MODULE_DECLARATION returns Module icon`() {
        val element = SimpleStubElement(RescriptElementTypes.MODULE_DECLARATION, "module M = {}")
        assertEquals(AllIcons.Nodes.Module, RescriptPsiUtils.getIcon(element))
    }

    @Test
    fun `getIcon for EXTERNAL_DECLARATION returns PluginJB icon`() {
        val element = SimpleStubElement(RescriptElementTypes.EXTERNAL_DECLARATION, "external f: int => int")
        assertEquals(AllIcons.Nodes.PluginJB, RescriptPsiUtils.getIcon(element))
    }

    @Test
    fun `getIcon for EXCEPTION_DECLARATION returns ExceptionClass icon`() {
        val element = SimpleStubElement(RescriptElementTypes.EXCEPTION_DECLARATION, "exception MyError")
        assertEquals(AllIcons.Nodes.ExceptionClass, RescriptPsiUtils.getIcon(element))
    }

    @Test
    fun `getIcon for unknown type returns null`() {
        val element = SimpleStubElement(RescriptTokenTypes.LIDENT, "foo")
        assertNull(RescriptPsiUtils.getIcon(element))
    }

    // --- getElementDescription ---

    @Test
    fun `getElementDescription for LET_DECLARATION`() {
        val element = SimpleStubElement(RescriptElementTypes.LET_DECLARATION, "let x = 1")
        assertEquals("let declaration", RescriptPsiUtils.getElementDescription(element))
    }

    @Test
    fun `getElementDescription for TYPE_DECLARATION`() {
        val element = SimpleStubElement(RescriptElementTypes.TYPE_DECLARATION, "type t = int")
        assertEquals("type declaration", RescriptPsiUtils.getElementDescription(element))
    }

    @Test
    fun `getElementDescription for MODULE_DECLARATION`() {
        val element = SimpleStubElement(RescriptElementTypes.MODULE_DECLARATION, "module M = {}")
        assertEquals("module declaration", RescriptPsiUtils.getElementDescription(element))
    }

    @Test
    fun `getElementDescription for EXTERNAL_DECLARATION`() {
        val element = SimpleStubElement(RescriptElementTypes.EXTERNAL_DECLARATION, "external f: int")
        assertEquals("external declaration", RescriptPsiUtils.getElementDescription(element))
    }

    @Test
    fun `getElementDescription for EXCEPTION_DECLARATION`() {
        val element = SimpleStubElement(RescriptElementTypes.EXCEPTION_DECLARATION, "exception E")
        assertEquals("exception declaration", RescriptPsiUtils.getElementDescription(element))
    }

    @Test
    fun `getElementDescription for unknown type returns null`() {
        val element = SimpleStubElement(RescriptTokenTypes.LIDENT, "foo")
        assertNull(RescriptPsiUtils.getElementDescription(element))
    }

    // --- NAVIGABLE_TYPES ---

    @Test
    fun `NAVIGABLE_TYPES contains all 5 declaration types`() {
        assertEquals(5, RescriptPsiUtils.NAVIGABLE_TYPES.size)
        assertTrue(RescriptPsiUtils.NAVIGABLE_TYPES.contains(RescriptElementTypes.LET_DECLARATION))
        assertTrue(RescriptPsiUtils.NAVIGABLE_TYPES.contains(RescriptElementTypes.TYPE_DECLARATION))
        assertTrue(RescriptPsiUtils.NAVIGABLE_TYPES.contains(RescriptElementTypes.MODULE_DECLARATION))
        assertTrue(RescriptPsiUtils.NAVIGABLE_TYPES.contains(RescriptElementTypes.EXTERNAL_DECLARATION))
        assertTrue(RescriptPsiUtils.NAVIGABLE_TYPES.contains(RescriptElementTypes.EXCEPTION_DECLARATION))
    }

    // --- BINDING_TYPES ---

    @Test
    fun `BINDING_TYPES contains 2 types`() {
        assertEquals(2, RescriptPsiUtils.BINDING_TYPES.size)
        assertTrue(RescriptPsiUtils.BINDING_TYPES.contains(RescriptElementTypes.LET_DECLARATION))
        assertTrue(RescriptPsiUtils.BINDING_TYPES.contains(RescriptElementTypes.EXTERNAL_DECLARATION))
    }

    @Test
    fun `BINDING_TYPES does not contain MODULE_DECLARATION`() {
        assertFalse(RescriptPsiUtils.BINDING_TYPES.contains(RescriptElementTypes.MODULE_DECLARATION))
    }

    // --- ANNOTATION_ELIGIBLE_TYPES ---

    @Test
    fun `ANNOTATION_ELIGIBLE_TYPES contains 3 types`() {
        assertEquals(3, RescriptPsiUtils.ANNOTATION_ELIGIBLE_TYPES.size)
        assertTrue(RescriptPsiUtils.ANNOTATION_ELIGIBLE_TYPES.contains(RescriptElementTypes.LET_DECLARATION))
        assertTrue(RescriptPsiUtils.ANNOTATION_ELIGIBLE_TYPES.contains(RescriptElementTypes.TYPE_DECLARATION))
        assertTrue(RescriptPsiUtils.ANNOTATION_ELIGIBLE_TYPES.contains(RescriptElementTypes.MODULE_DECLARATION))
    }

    @Test
    fun `ANNOTATION_ELIGIBLE_TYPES does not contain EXTERNAL_DECLARATION`() {
        assertFalse(RescriptPsiUtils.ANNOTATION_ELIGIBLE_TYPES.contains(RescriptElementTypes.EXTERNAL_DECLARATION))
    }

    // --- ALL_DECLARATION_TYPES ---

    @Test
    fun `ALL_DECLARATION_TYPES contains 7 types`() {
        assertEquals(7, RescriptPsiUtils.ALL_DECLARATION_TYPES.size)
        assertTrue(RescriptPsiUtils.ALL_DECLARATION_TYPES.contains(RescriptElementTypes.OPEN_STATEMENT))
        assertTrue(RescriptPsiUtils.ALL_DECLARATION_TYPES.contains(RescriptElementTypes.INCLUDE_STATEMENT))
    }

    @Test
    fun `ALL_DECLARATION_TYPES is superset of NAVIGABLE_TYPES`() {
        assertTrue(RescriptPsiUtils.ALL_DECLARATION_TYPES.containsAll(RescriptPsiUtils.NAVIGABLE_TYPES))
    }

    @Test
    fun `ALL_DECLARATION_TYPES is superset of BINDING_TYPES`() {
        assertTrue(RescriptPsiUtils.ALL_DECLARATION_TYPES.containsAll(RescriptPsiUtils.BINDING_TYPES))
    }

    @Test
    fun `ALL_DECLARATION_TYPES is superset of ANNOTATION_ELIGIBLE_TYPES`() {
        assertTrue(RescriptPsiUtils.ALL_DECLARATION_TYPES.containsAll(RescriptPsiUtils.ANNOTATION_ELIGIBLE_TYPES))
    }

    // --- findEnclosingDeclaration ---

    @Test
    fun `findEnclosingDeclaration finds parent of matching type`() {
        val parent = stubPsiElement(RescriptElementTypes.LET_DECLARATION)
        val child = stubPsiElement(RescriptElementTypes.ANNOTATION, parent = parent)
        val result = RescriptPsiUtils.findEnclosingDeclaration(child, RescriptPsiUtils.NAVIGABLE_TYPES)
        assertNotNull(result)
        assertEquals(RescriptElementTypes.LET_DECLARATION, result!!.node?.elementType)
    }

    @Test
    fun `findEnclosingDeclaration includes element itself by default`() {
        val element = stubPsiElement(RescriptElementTypes.MODULE_DECLARATION)
        val result = RescriptPsiUtils.findEnclosingDeclaration(element, RescriptPsiUtils.NAVIGABLE_TYPES)
        assertNotNull(result)
        assertEquals(RescriptElementTypes.MODULE_DECLARATION, result!!.node?.elementType)
    }

    @Test
    fun `findEnclosingDeclaration excludes element when includeElement is false`() {
        val element = stubPsiElement(RescriptElementTypes.MODULE_DECLARATION, parent = null)
        val result =
            RescriptPsiUtils.findEnclosingDeclaration(
                element,
                RescriptPsiUtils.NAVIGABLE_TYPES,
                includeElement = false,
            )
        assertNull(result)
    }

    @Test
    fun `findEnclosingDeclaration returns null when no match`() {
        val element = stubPsiElement(RescriptElementTypes.ANNOTATION, parent = null)
        val result = RescriptPsiUtils.findEnclosingDeclaration(element, RescriptPsiUtils.NAVIGABLE_TYPES)
        assertNull(result)
    }

    @Test
    fun `findEnclosingDeclaration respects type filter`() {
        val parent = stubPsiElement(RescriptElementTypes.MODULE_DECLARATION)
        val child = stubPsiElement(RescriptElementTypes.ANNOTATION, parent = parent)
        // BINDING_TYPES doesn't contain MODULE_DECLARATION
        val result = RescriptPsiUtils.findEnclosingDeclaration(child, RescriptPsiUtils.BINDING_TYPES)
        assertNull(result)
    }

    // --- isInsideDeclaration ---

    @Test
    fun `isInsideDeclaration returns true when inside matching parent`() {
        val parent = stubPsiElement(RescriptElementTypes.LET_DECLARATION)
        val child = stubPsiElement(RescriptElementTypes.ANNOTATION, parent = parent)
        assertTrue(RescriptPsiUtils.isInsideDeclaration(child, RescriptPsiUtils.BINDING_TYPES))
    }

    @Test
    fun `isInsideDeclaration returns false when not inside matching parent`() {
        val parent = stubPsiElement(RescriptElementTypes.MODULE_DECLARATION)
        val child = stubPsiElement(RescriptElementTypes.ANNOTATION, parent = parent)
        assertFalse(RescriptPsiUtils.isInsideDeclaration(child, RescriptPsiUtils.BINDING_TYPES))
    }

    @Test
    fun `isInsideDeclaration returns false for top-level element`() {
        val element = stubPsiElement(RescriptElementTypes.ANNOTATION, parent = null)
        assertFalse(RescriptPsiUtils.isInsideDeclaration(element, RescriptPsiUtils.NAVIGABLE_TYPES))
    }

    @Test
    fun `isInsideDeclaration excludes element itself`() {
        val element = stubPsiElement(RescriptElementTypes.LET_DECLARATION, parent = null)
        assertFalse(RescriptPsiUtils.isInsideDeclaration(element, RescriptPsiUtils.BINDING_TYPES))
    }

    // -- Helper to create PSI element stubs for findEnclosingDeclaration/isInsideDeclaration --

    private fun stubPsiElement(
        type: IElementType,
        parent: PsiElement? = null,
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
                "toString" -> "StubPsiElement($type)"
                "hashCode" -> System.identityHashCode(node)
                "equals" -> false
                else -> null
            }
        } as PsiElement
    }
}
