package com.rescript.plugin.generate

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.tree.IElementType
import com.rescript.plugin.lang.psi.RescriptElementTypes
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RescriptGenerateModuleTypeActionTest {
    @Test
    fun `generates module type with let declarations`() {
        val declarations =
            listOf(
                RescriptGenerateModuleTypeAction.Companion.Declaration("let", "x"),
                RescriptGenerateModuleTypeAction.Companion.Declaration("let", "y"),
            )
        val result = RescriptGenerateModuleTypeAction.generateModuleTypeText("Foo", declarations)
        assertTrue(result.contains("module type FooType = {"))
        assertTrue(result.contains("let x: 'a"))
        assertTrue(result.contains("let y: 'a"))
        assertTrue(result.endsWith("}"))
    }

    @Test
    fun `generates module type with type declarations`() {
        val declarations =
            listOf(
                RescriptGenerateModuleTypeAction.Companion.Declaration("type", "t"),
                RescriptGenerateModuleTypeAction.Companion.Declaration("type", "config"),
            )
        val result = RescriptGenerateModuleTypeAction.generateModuleTypeText("Foo", declarations)
        assertTrue(result.contains("type t"))
        assertTrue(result.contains("type config"))
    }

    @Test
    fun `generates module type with module declarations`() {
        val declarations =
            listOf(
                RescriptGenerateModuleTypeAction.Companion.Declaration("module", "Sub"),
            )
        val result = RescriptGenerateModuleTypeAction.generateModuleTypeText("Foo", declarations)
        assertTrue(result.contains("module Sub: {}"))
    }

    @Test
    fun `generates module type with mixed declarations`() {
        val declarations =
            listOf(
                RescriptGenerateModuleTypeAction.Companion.Declaration("type", "t"),
                RescriptGenerateModuleTypeAction.Companion.Declaration("let", "make"),
                RescriptGenerateModuleTypeAction.Companion.Declaration("let", "toString"),
                RescriptGenerateModuleTypeAction.Companion.Declaration("module", "Internal"),
            )
        val result = RescriptGenerateModuleTypeAction.generateModuleTypeText("MyModule", declarations)
        assertTrue(result.contains("module type MyModuleType = {"))
        assertTrue(result.contains("type t"))
        assertTrue(result.contains("let make: 'a"))
        assertTrue(result.contains("let toString: 'a"))
        assertTrue(result.contains("module Internal: {}"))
    }

    @Test
    fun `generates empty module type for no declarations`() {
        val declarations = emptyList<RescriptGenerateModuleTypeAction.Companion.Declaration>()
        val result = RescriptGenerateModuleTypeAction.generateModuleTypeText("Empty", declarations)
        val expected =
            buildString {
                appendLine("module type EmptyType = {")
                append("}")
            }
        assertEquals(expected, result)
    }

    @Test
    fun `module type name appends Type suffix`() {
        val result =
            RescriptGenerateModuleTypeAction.generateModuleTypeText(
                "Parser",
                emptyList(),
            )
        assertTrue(result.contains("module type ParserType = {"))
    }

    @Test
    fun `Declaration data class holds kind and name`() {
        val decl = RescriptGenerateModuleTypeAction.Companion.Declaration("let", "x")
        assertEquals("let", decl.kind)
        assertEquals("x", decl.name)
    }

    @Test
    fun `Declaration data class equality`() {
        val a = RescriptGenerateModuleTypeAction.Companion.Declaration("let", "x")
        val b = RescriptGenerateModuleTypeAction.Companion.Declaration("let", "x")
        assertEquals(a, b)
    }

    @Test
    fun `generates module type skips unknown declaration kind`() {
        val declarations =
            listOf(
                RescriptGenerateModuleTypeAction.Companion.Declaration("let", "x"),
                RescriptGenerateModuleTypeAction.Companion.Declaration("open", "Belt"),
                RescriptGenerateModuleTypeAction.Companion.Declaration("type", "t"),
            )
        val result = RescriptGenerateModuleTypeAction.generateModuleTypeText("Foo", declarations)
        assertTrue(result.contains("let x: 'a"))
        assertTrue(result.contains("type t"))
        // "open" is not a recognized kind, so it should be silently skipped
        assertFalse(result.contains("open"))
        assertFalse(result.contains("Belt"))
    }

    // -- collectDeclarations tests --

    @Test
    fun `collectDeclarations finds let declarations`() {
        val moduleDecl =
            stubModuleWithChildren(
                listOf(RescriptElementTypes.LET_DECLARATION to "myFunc"),
            )
        val result = RescriptGenerateModuleTypeAction.collectDeclarations(moduleDecl)
        assertEquals(1, result.size)
        assertEquals("let", result[0].kind)
    }

    @Test
    fun `collectDeclarations finds type declarations`() {
        val moduleDecl =
            stubModuleWithChildren(
                listOf(RescriptElementTypes.TYPE_DECLARATION to "t"),
            )
        val result = RescriptGenerateModuleTypeAction.collectDeclarations(moduleDecl)
        assertEquals(1, result.size)
        assertEquals("type", result[0].kind)
    }

    @Test
    fun `collectDeclarations finds external declarations as let`() {
        val moduleDecl =
            stubModuleWithChildren(
                listOf(RescriptElementTypes.EXTERNAL_DECLARATION to "log"),
            )
        val result = RescriptGenerateModuleTypeAction.collectDeclarations(moduleDecl)
        assertEquals(1, result.size)
        assertEquals("let", result[0].kind)
    }

    @Test
    fun `collectDeclarations finds nested module declarations`() {
        val moduleDecl =
            stubModuleWithChildren(
                listOf(RescriptElementTypes.MODULE_DECLARATION to "Sub"),
            )
        val result = RescriptGenerateModuleTypeAction.collectDeclarations(moduleDecl)
        assertEquals(1, result.size)
        assertEquals("module", result[0].kind)
    }

    @Test
    fun `collectDeclarations skips non-declaration children`() {
        val moduleDecl =
            stubModuleWithChildren(
                listOf(
                    RescriptElementTypes.OPEN_STATEMENT to "Belt",
                    RescriptElementTypes.LET_DECLARATION to "x",
                    RescriptElementTypes.INCLUDE_STATEMENT to "Utils",
                ),
            )
        val result = RescriptGenerateModuleTypeAction.collectDeclarations(moduleDecl)
        assertEquals(1, result.size)
        assertEquals("let", result[0].kind)
    }

    @Test
    fun `collectDeclarations with empty children`() {
        val moduleDecl = stubModuleWithChildren(emptyList())
        val result = RescriptGenerateModuleTypeAction.collectDeclarations(moduleDecl)
        assertTrue(result.isEmpty())
    }

    @Test
    fun `collectDeclarations finds mixed declarations`() {
        val moduleDecl =
            stubModuleWithChildren(
                listOf(
                    RescriptElementTypes.TYPE_DECLARATION to "t",
                    RescriptElementTypes.LET_DECLARATION to "make",
                    RescriptElementTypes.EXTERNAL_DECLARATION to "log",
                    RescriptElementTypes.MODULE_DECLARATION to "Internal",
                ),
            )
        val result = RescriptGenerateModuleTypeAction.collectDeclarations(moduleDecl)
        assertEquals(4, result.size)
        assertEquals("type", result[0].kind)
        assertEquals("let", result[1].kind)
        assertEquals("let", result[2].kind) // external becomes let
        assertEquals("module", result[3].kind)
    }

    // -- stub helpers --

    private fun assertFalse(condition: Boolean) {
        org.junit.Assert.assertFalse(condition)
    }

    private fun stubModuleWithChildren(children: List<Pair<IElementType, String>>): PsiElement {
        val childElements =
            children
                .map { (type, _) ->
                    stubPsiElement(type)
                }.toTypedArray()

        return java.lang.reflect.Proxy.newProxyInstance(
            PsiElement::class.java.classLoader,
            arrayOf(PsiElement::class.java),
        ) { _, method, _ ->
            when (method.name) {
                "getChildren" -> childElements
                "getNode" -> stubAstNode(RescriptElementTypes.MODULE_DECLARATION)
                "toString" -> "StubModuleDecl"
                "hashCode" -> 42
                "equals" -> false
                else -> null
            }
        } as PsiElement
    }

    private fun stubPsiElement(type: IElementType): PsiElement {
        // The extractName function looks at firstChildNode chain for LIDENT/UIDENT,
        // so we return a simple node that has no children (extractName returns "(unknown)")
        val node = stubAstNode(type)

        return java.lang.reflect.Proxy.newProxyInstance(
            PsiElement::class.java.classLoader,
            arrayOf(PsiElement::class.java),
        ) { _, method, _ ->
            when (method.name) {
                "getNode" -> node
                "toString" -> "StubPsiElement($type)"
                "hashCode" -> System.identityHashCode(node)
                "equals" -> false
                else -> null
            }
        } as PsiElement
    }

    private fun stubAstNode(type: IElementType): ASTNode =
        java.lang.reflect.Proxy.newProxyInstance(
            ASTNode::class.java.classLoader,
            arrayOf(ASTNode::class.java),
        ) { _, method, _ ->
            when (method.name) {
                "getElementType" -> type
                "getFirstChildNode" -> null
                "toString" -> "StubASTNode($type)"
                "hashCode" -> type.hashCode()
                "equals" -> false
                else -> null
            }
        } as ASTNode
}
