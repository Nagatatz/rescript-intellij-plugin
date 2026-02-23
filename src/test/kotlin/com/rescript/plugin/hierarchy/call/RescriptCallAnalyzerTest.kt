package com.rescript.plugin.hierarchy.call

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.tree.IElementType
import com.rescript.plugin.lang.RescriptTokenTypes
import com.rescript.plugin.lang.psi.RescriptElementTypes
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RescriptCallAnalyzerTest {
    // ── CallReference data class tests ───────────────────────────────

    @Test
    fun `CallReference holds correct declaration and callSite`() {
        val decl = SimpleStubElement(RescriptElementTypes.LET_DECLARATION, "let foo = 1")
        val site = SimpleStubElement(RescriptTokenTypes.LIDENT, "foo")
        val ref = RescriptCallAnalyzer.CallReference(decl, site)
        assertEquals(decl, ref.declaration)
        assertEquals(site, ref.callSite)
    }

    @Test
    fun `CallReference equality by data class`() {
        val decl = SimpleStubElement(RescriptElementTypes.LET_DECLARATION, "let foo = 1")
        val site = SimpleStubElement(RescriptTokenTypes.LIDENT, "foo")
        val ref1 = RescriptCallAnalyzer.CallReference(decl, site)
        val ref2 = RescriptCallAnalyzer.CallReference(decl, site)
        assertEquals(ref1, ref2)
    }

    // ── extractBodyIdentifiers tests ─────────────────────────────────

    @Test
    fun `extractBodyIdentifiers finds identifiers after equals`() {
        // Simulate: let foo = bar(baz)
        val letToken = SimpleStubElement(RescriptTokenTypes.LET, "let")
        val nameToken = SimpleStubElement(RescriptTokenTypes.LIDENT, "foo")
        val eqToken = SimpleStubElement(RescriptTokenTypes.EQ, "=")
        val barToken = SimpleStubElement(RescriptTokenTypes.LIDENT, "bar")
        val bazToken = SimpleStubElement(RescriptTokenTypes.LIDENT, "baz")

        linkSiblings(letToken, nameToken, eqToken, barToken, bazToken)

        val decl =
            object : SimpleStubElement(RescriptElementTypes.LET_DECLARATION, "let foo = bar(baz)") {
                override fun getFirstChild(): PsiElement = letToken
            }

        val result = RescriptCallAnalyzer.extractBodyIdentifiers(decl)
        val names = result.map { it.first }
        assertEquals(listOf("bar", "baz"), names)
    }

    @Test
    fun `extractBodyIdentifiers returns empty for declaration with no equals`() {
        // Simulate: external foo = "foo"
        val extToken = SimpleStubElement(RescriptTokenTypes.EXTERNAL, "external")
        val nameToken = SimpleStubElement(RescriptTokenTypes.LIDENT, "foo")

        linkSiblings(extToken, nameToken)

        val decl =
            object : SimpleStubElement(RescriptElementTypes.EXTERNAL_DECLARATION, "external foo") {
                override fun getFirstChild(): PsiElement = extToken
            }

        val result = RescriptCallAnalyzer.extractBodyIdentifiers(decl)
        assertTrue(result.isEmpty())
    }

    @Test
    fun `extractBodyIdentifiers ignores identifiers before equals`() {
        // Simulate: let foo = bar
        val letToken = SimpleStubElement(RescriptTokenTypes.LET, "let")
        val nameToken = SimpleStubElement(RescriptTokenTypes.LIDENT, "foo")
        val eqToken = SimpleStubElement(RescriptTokenTypes.EQ, "=")
        val barToken = SimpleStubElement(RescriptTokenTypes.LIDENT, "bar")

        linkSiblings(letToken, nameToken, eqToken, barToken)

        val decl =
            object : SimpleStubElement(RescriptElementTypes.LET_DECLARATION, "let foo = bar") {
                override fun getFirstChild(): PsiElement = letToken
            }

        val result = RescriptCallAnalyzer.extractBodyIdentifiers(decl)
        assertEquals(1, result.size)
        assertEquals("bar", result[0].first)
    }

    @Test
    fun `extractBodyIdentifiers returns empty for empty body`() {
        // Simulate: let foo =
        val letToken = SimpleStubElement(RescriptTokenTypes.LET, "let")
        val nameToken = SimpleStubElement(RescriptTokenTypes.LIDENT, "foo")
        val eqToken = SimpleStubElement(RescriptTokenTypes.EQ, "=")

        linkSiblings(letToken, nameToken, eqToken)

        val decl =
            object : SimpleStubElement(RescriptElementTypes.LET_DECLARATION, "let foo =") {
                override fun getFirstChild(): PsiElement = letToken
            }

        val result = RescriptCallAnalyzer.extractBodyIdentifiers(decl)
        assertTrue(result.isEmpty())
    }

    @Test
    fun `extractBodyIdentifiers handles multiple identifiers in body`() {
        // Simulate: let foo = a + b + c
        val letToken = SimpleStubElement(RescriptTokenTypes.LET, "let")
        val nameToken = SimpleStubElement(RescriptTokenTypes.LIDENT, "foo")
        val eqToken = SimpleStubElement(RescriptTokenTypes.EQ, "=")
        val aToken = SimpleStubElement(RescriptTokenTypes.LIDENT, "a")
        val plusToken = SimpleStubElement(RescriptTokenTypes.PLUS, "+")
        val bToken = SimpleStubElement(RescriptTokenTypes.LIDENT, "b")
        val plus2Token = SimpleStubElement(RescriptTokenTypes.PLUS, "+")
        val cToken = SimpleStubElement(RescriptTokenTypes.LIDENT, "c")

        linkSiblings(letToken, nameToken, eqToken, aToken, plusToken, bToken, plus2Token, cToken)

        val decl =
            object : SimpleStubElement(RescriptElementTypes.LET_DECLARATION, "let foo = a + b + c") {
                override fun getFirstChild(): PsiElement = letToken
            }

        val result = RescriptCallAnalyzer.extractBodyIdentifiers(decl)
        val names = result.map { it.first }
        assertEquals(listOf("a", "b", "c"), names)
    }

    // ── collectFileDeclarations tests ────────────────────────────────

    @Test
    fun `collectFileDeclarations finds let declarations`() {
        val letDecl = SimpleStubElement(RescriptElementTypes.LET_DECLARATION, "let foo = 1")
        val typeDecl = SimpleStubElement(RescriptElementTypes.TYPE_DECLARATION, "type t = int")
        val letDecl2 = SimpleStubElement(RescriptElementTypes.LET_DECLARATION, "let bar = 2")

        val file =
            object : SimpleStubElement(null, "file") {
                override fun getChildren(): Array<PsiElement> = arrayOf(letDecl, typeDecl, letDecl2)
            }

        val result = RescriptCallAnalyzer.collectFileDeclarations(file)
        assertEquals(2, result.size)
    }

    @Test
    fun `collectFileDeclarations returns empty for file with no let or external`() {
        val typeDecl = SimpleStubElement(RescriptElementTypes.TYPE_DECLARATION, "type t = int")
        val openStmt = SimpleStubElement(RescriptElementTypes.OPEN_STATEMENT, "open Belt")

        val file =
            object : SimpleStubElement(null, "file") {
                override fun getChildren(): Array<PsiElement> = arrayOf(typeDecl, openStmt)
            }

        val result = RescriptCallAnalyzer.collectFileDeclarations(file)
        assertTrue(result.isEmpty())
    }

    @Test
    fun `collectFileDeclarations includes external declarations`() {
        val extDecl = SimpleStubElement(RescriptElementTypes.EXTERNAL_DECLARATION, "external foo: int = \"foo\"")

        val file =
            object : SimpleStubElement(null, "file") {
                override fun getChildren(): Array<PsiElement> = arrayOf(extDecl)
            }

        val result = RescriptCallAnalyzer.collectFileDeclarations(file)
        assertEquals(1, result.size)
    }

    @Test
    fun `collectFileDeclarations recurses into module declarations`() {
        val innerLet = SimpleStubElement(RescriptElementTypes.LET_DECLARATION, "let inner = 1")
        val module =
            object : SimpleStubElement(RescriptElementTypes.MODULE_DECLARATION, "module M = {}") {
                override fun getChildren(): Array<PsiElement> = arrayOf(innerLet)
            }
        val outerLet = SimpleStubElement(RescriptElementTypes.LET_DECLARATION, "let outer = 2")

        val file =
            object : SimpleStubElement(null, "file") {
                override fun getChildren(): Array<PsiElement> = arrayOf(outerLet, module)
            }

        val result = RescriptCallAnalyzer.collectFileDeclarations(file)
        assertEquals(2, result.size)
    }

    // ── Stub helpers ─────────────────────────────────────────────────

    private fun linkSiblings(vararg elements: SimpleStubElement) {
        for (i in elements.indices) {
            if (i + 1 < elements.size) {
                elements[i].next = elements[i + 1]
            }
        }
    }

    open class SimpleStubElement(
        private val elementType: IElementType?,
        private val textContent: String,
    ) : PsiElement by stubProxy() {
        var next: PsiElement? = null

        override fun getNode(): ASTNode? = if (elementType != null) stubAstNode(elementType) else null

        override fun getText(): String = textContent

        override fun getNextSibling(): PsiElement? = next

        override fun getFirstChild(): PsiElement? = null

        override fun getChildren(): Array<PsiElement> = emptyArray()
    }

    companion object {
        fun stubAstNode(type: IElementType): ASTNode =
            java.lang.reflect.Proxy.newProxyInstance(
                ASTNode::class.java.classLoader,
                arrayOf(ASTNode::class.java),
            ) { _, method, _ ->
                when (method.name) {
                    "getElementType" -> type
                    "toString" -> "StubASTNode($type)"
                    "hashCode" -> type.hashCode()
                    "equals" -> false
                    else -> null
                }
            } as ASTNode

        inline fun <reified T> stubProxy(): T =
            java.lang.reflect.Proxy.newProxyInstance(
                T::class.java.classLoader,
                arrayOf(T::class.java),
            ) { proxy, method, _ ->
                when (method.name) {
                    "toString" -> "StubProxy(${T::class.simpleName})"
                    "hashCode" -> System.identityHashCode(proxy)
                    "equals" -> false
                    else -> null
                }
            } as T
    }
}
