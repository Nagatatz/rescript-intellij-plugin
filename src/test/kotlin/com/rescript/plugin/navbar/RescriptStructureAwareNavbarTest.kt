package com.rescript.plugin.navbar

import com.intellij.icons.AllIcons
import com.intellij.lang.ASTNode
import com.intellij.lang.Language
import com.intellij.psi.PsiElement
import com.intellij.psi.tree.IElementType
import com.rescript.plugin.RescriptLanguage
import com.rescript.plugin.lang.RescriptTokenTypes
import com.rescript.plugin.lang.psi.RescriptElementTypes
import com.rescript.plugin.lang.psi.RescriptPsiUtils
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test

class RescriptStructureAwareNavbarTest {
    private val navbar = RescriptStructureAwareNavbar()

    /** A non-ReScript language used to exercise the language guard. */
    private object OtherLanguage : Language("NavbarTestOther")

    private fun stubElement(
        type: IElementType,
        parent: PsiElement? = null,
        language: Language? = RescriptLanguage,
    ): PsiElement {
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
                "getParent" -> parent
                "getLanguage" -> language
                "getChildren" -> emptyArray<PsiElement>()
                "toString" -> "StubPsiElement($type)"
                "hashCode" -> System.identityHashCode(type)
                "equals" -> false
                else -> null
            }
        } as PsiElement
    }

    @Test
    fun testInstantiation() {
        // Verify the navbar extension can be instantiated without errors
        assertNotNull(navbar)
    }

    @Test
    fun testGetPresentableTextReturnsNameForLetDeclaration() {
        val element = stubElement(RescriptElementTypes.LET_DECLARATION)
        // No identifier children in stub, so extractName returns "(anonymous)"
        assertEquals("(anonymous)", navbar.getPresentableText(element))
    }

    @Test
    fun testGetPresentableTextReturnsNameForModuleDeclaration() {
        val element = stubElement(RescriptElementTypes.MODULE_DECLARATION)
        assertEquals("(anonymous)", navbar.getPresentableText(element))
    }

    @Test
    fun testGetPresentableTextReturnsNullForNullInput() {
        assertNull(navbar.getPresentableText(null))
    }

    @Test
    fun testGetPresentableTextReturnsNullForNonNavigableType() {
        val element = stubElement(RescriptElementTypes.OPEN_STATEMENT)
        assertNull(navbar.getPresentableText(element))
    }

    @Test
    fun testGetPresentableTextReturnsNullForAnnotation() {
        val element = stubElement(RescriptElementTypes.ANNOTATION)
        assertNull(navbar.getPresentableText(element))
    }

    @Test
    fun testGetPresentableTextReturnsNullForNonRescriptLanguage() {
        // Language guard: a navigable element type from another language is ignored.
        val element = stubElement(RescriptElementTypes.LET_DECLARATION, language = OtherLanguage)
        assertNull(navbar.getPresentableText(element))
    }

    @Test
    fun testGetPresentableTextAcceptsAllNavigableTypes() {
        for (type in RescriptPsiUtils.NAVIGABLE_TYPES) {
            assertNotNull(navbar.getPresentableText(stubElement(type)), "$type should return presentable text")
        }
    }

    @Test
    fun testGetIconReturnsIconForLetDeclaration() {
        val element = stubElement(RescriptElementTypes.LET_DECLARATION)
        assertEquals(AllIcons.Nodes.Function, navbar.getIcon(element))
    }

    @Test
    fun testGetIconReturnsIconForTypeDeclaration() {
        val element = stubElement(RescriptElementTypes.TYPE_DECLARATION)
        assertEquals(AllIcons.Nodes.Type, navbar.getIcon(element))
    }

    @Test
    fun testGetIconReturnsIconForModuleDeclaration() {
        val element = stubElement(RescriptElementTypes.MODULE_DECLARATION)
        assertEquals(AllIcons.Nodes.Module, navbar.getIcon(element))
    }

    @Test
    fun testGetIconReturnsNullForNullInput() {
        assertNull(navbar.getIcon(null))
    }

    @Test
    fun testGetIconReturnsNullForNonNavigableType() {
        val element = stubElement(RescriptElementTypes.OPEN_STATEMENT)
        assertNull(navbar.getIcon(element))
    }

    @Test
    fun testGetIconReturnsNullForNonRescriptLanguage() {
        val element = stubElement(RescriptElementTypes.MODULE_DECLARATION, language = OtherLanguage)
        assertNull(navbar.getIcon(element))
    }

    @Test
    fun testGetParentReturnsEnclosingModuleDeclaration() {
        // module M { let inner = ... } — getParent(inner) climbs to the module.
        val module = stubElement(RescriptElementTypes.MODULE_DECLARATION)
        val inner = stubElement(RescriptElementTypes.LET_DECLARATION, parent = module)
        assertSame(module, navbar.getParent(inner))
    }

    @Test
    fun testGetParentClimbsPastNonDeclarationLeaf() {
        // A leaf token inside a let resolves to the enclosing let declaration.
        val letDecl = stubElement(RescriptElementTypes.LET_DECLARATION)
        val leaf = stubElement(RescriptTokenTypes.LIDENT, parent = letDecl)
        assertSame(letDecl, navbar.getParent(leaf))
    }
}
