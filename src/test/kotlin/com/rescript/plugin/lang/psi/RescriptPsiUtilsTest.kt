package com.rescript.plugin.lang.psi

import com.intellij.icons.AllIcons
import com.intellij.psi.PsiElement
import com.rescript.plugin.RescriptTestUtils
import com.rescript.plugin.lang.RescriptTokenTypes
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RescriptPsiUtilsTest {
    // ── extractName ──

    @Test
    fun `extractName returns name from let declaration`() {
        val node =
            RescriptTestUtils.stubAstNodeWithChildren(
                RescriptElementTypes.LET_DECLARATION,
                listOf(
                    RescriptTokenTypes.LET to "let",
                    RescriptTokenTypes.LIDENT to "myFunc",
                ),
            )
        val element = stubPsiElement(node)
        assertEquals("myFunc", RescriptPsiUtils.extractName(element))
    }

    @Test
    fun `extractName returns name from type declaration`() {
        val node =
            RescriptTestUtils.stubAstNodeWithChildren(
                RescriptElementTypes.TYPE_DECLARATION,
                listOf(
                    RescriptTokenTypes.TYPE to "type",
                    RescriptTokenTypes.LIDENT to "color",
                ),
            )
        val element = stubPsiElement(node)
        assertEquals("color", RescriptPsiUtils.extractName(element))
    }

    @Test
    fun `extractName returns name from module declaration`() {
        val node =
            RescriptTestUtils.stubAstNodeWithChildren(
                RescriptElementTypes.MODULE_DECLARATION,
                listOf(
                    RescriptTokenTypes.MODULE to "module",
                    RescriptTokenTypes.UIDENT to "MyModule",
                ),
            )
        val element = stubPsiElement(node)
        assertEquals("MyModule", RescriptPsiUtils.extractName(element))
    }

    @Test
    fun `extractName skips rec keyword`() {
        val node =
            RescriptTestUtils.stubAstNodeWithChildren(
                RescriptElementTypes.LET_DECLARATION,
                listOf(
                    RescriptTokenTypes.LET to "let",
                    RescriptTokenTypes.REC to "rec",
                    RescriptTokenTypes.LIDENT to "loop",
                ),
            )
        val element = stubPsiElement(node)
        assertEquals("loop", RescriptPsiUtils.extractName(element))
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
        val element = stubPsiElement(node)
        assertEquals("(anonymous)", RescriptPsiUtils.extractName(element))
    }

    @Test
    fun `extractName returns unknown when node is null`() {
        val element = RescriptTestUtils.stubProxy<PsiElement>()
        assertEquals("(unknown)", RescriptPsiUtils.extractName(element))
    }

    // ── getIcon ──

    @Test
    fun `getIcon returns Function for let declaration`() {
        val element = stubPsiElementWithType(RescriptElementTypes.LET_DECLARATION)
        assertEquals(AllIcons.Nodes.Function, RescriptPsiUtils.getIcon(element))
    }

    @Test
    fun `getIcon returns Type for type declaration`() {
        val element = stubPsiElementWithType(RescriptElementTypes.TYPE_DECLARATION)
        assertEquals(AllIcons.Nodes.Type, RescriptPsiUtils.getIcon(element))
    }

    @Test
    fun `getIcon returns Module for module declaration`() {
        val element = stubPsiElementWithType(RescriptElementTypes.MODULE_DECLARATION)
        assertEquals(AllIcons.Nodes.Module, RescriptPsiUtils.getIcon(element))
    }

    @Test
    fun `getIcon returns PluginJB for external declaration`() {
        val element = stubPsiElementWithType(RescriptElementTypes.EXTERNAL_DECLARATION)
        assertEquals(AllIcons.Nodes.PluginJB, RescriptPsiUtils.getIcon(element))
    }

    @Test
    fun `getIcon returns ExceptionClass for exception declaration`() {
        val element = stubPsiElementWithType(RescriptElementTypes.EXCEPTION_DECLARATION)
        assertEquals(AllIcons.Nodes.ExceptionClass, RescriptPsiUtils.getIcon(element))
    }

    @Test
    fun `getIcon returns null for unknown element type`() {
        val element = stubPsiElementWithType(RescriptElementTypes.OPEN_STATEMENT)
        assertNull(RescriptPsiUtils.getIcon(element))
    }

    // ── getElementDescription ──

    @Test
    fun `getElementDescription returns let declaration`() {
        val element = stubPsiElementWithType(RescriptElementTypes.LET_DECLARATION)
        assertEquals("let declaration", RescriptPsiUtils.getElementDescription(element))
    }

    @Test
    fun `getElementDescription returns type declaration`() {
        val element = stubPsiElementWithType(RescriptElementTypes.TYPE_DECLARATION)
        assertEquals("type declaration", RescriptPsiUtils.getElementDescription(element))
    }

    @Test
    fun `getElementDescription returns module declaration`() {
        val element = stubPsiElementWithType(RescriptElementTypes.MODULE_DECLARATION)
        assertEquals("module declaration", RescriptPsiUtils.getElementDescription(element))
    }

    @Test
    fun `getElementDescription returns external declaration`() {
        val element = stubPsiElementWithType(RescriptElementTypes.EXTERNAL_DECLARATION)
        assertEquals("external declaration", RescriptPsiUtils.getElementDescription(element))
    }

    @Test
    fun `getElementDescription returns exception declaration`() {
        val element = stubPsiElementWithType(RescriptElementTypes.EXCEPTION_DECLARATION)
        assertEquals("exception declaration", RescriptPsiUtils.getElementDescription(element))
    }

    @Test
    fun `getElementDescription returns null for unknown type`() {
        val element = stubPsiElementWithType(RescriptElementTypes.OPEN_STATEMENT)
        assertNull(RescriptPsiUtils.getElementDescription(element))
    }

    // ── NAVIGABLE_TYPES ──

    @Test
    fun `NAVIGABLE_TYPES contains expected types`() {
        val types = RescriptPsiUtils.NAVIGABLE_TYPES
        assertEquals(5, types.size)
        assert(RescriptElementTypes.LET_DECLARATION in types)
        assert(RescriptElementTypes.TYPE_DECLARATION in types)
        assert(RescriptElementTypes.MODULE_DECLARATION in types)
        assert(RescriptElementTypes.EXTERNAL_DECLARATION in types)
        assert(RescriptElementTypes.EXCEPTION_DECLARATION in types)
    }

    // ── helpers ──

    private fun stubPsiElement(node: com.intellij.lang.ASTNode): PsiElement =
        object : PsiElement by RescriptTestUtils.stubProxy() {
            override fun getNode() = node
        }

    private fun stubPsiElementWithType(type: com.intellij.psi.tree.IElementType): PsiElement =
        object : PsiElement by RescriptTestUtils.stubProxy() {
            override fun getNode() = RescriptTestUtils.stubAstNode(type)
        }
}
