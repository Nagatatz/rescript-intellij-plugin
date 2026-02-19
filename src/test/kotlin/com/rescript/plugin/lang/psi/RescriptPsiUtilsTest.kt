package com.rescript.plugin.lang.psi

import com.intellij.icons.AllIcons
import com.rescript.plugin.RescriptTestUtils
import com.rescript.plugin.RescriptTestUtils.SimpleStubElement
import com.rescript.plugin.lang.RescriptTokenTypes
import org.junit.Assert.assertEquals
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
}
