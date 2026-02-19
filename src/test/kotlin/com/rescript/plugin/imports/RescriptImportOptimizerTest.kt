package com.rescript.plugin.imports

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.tree.IElementType
import com.rescript.plugin.lang.RescriptTokenTypes
import com.rescript.plugin.lang.psi.RescriptElementTypes
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class RescriptImportOptimizerTest {
    private val optimizer = RescriptImportOptimizer()

    // ── supports() tests ─────────────────────────────────────────────

    @Test
    fun testSupportsReturnsFalseForNonRescriptFile() {
        val file = stubProxy<com.intellij.psi.PsiFile>()
        assertFalse(optimizer.supports(file))
    }

    // ── extractModulePath() tests ────────────────────────────────────

    @Test
    fun testExtractSimpleModulePath() {
        val openStmt = buildOpenStatement(listOf("Belt"))
        assertEquals("Belt", RescriptImportUtil.extractModulePath(openStmt))
    }

    @Test
    fun testExtractDottedModulePath() {
        val openStmt = buildOpenStatement(listOf("Belt", ".", "Array"))
        assertEquals("Belt.Array", RescriptImportUtil.extractModulePath(openStmt))
    }

    @Test
    fun testExtractDeepModulePath() {
        val openStmt = buildOpenStatement(listOf("Js", ".", "Promise2", ".", "Result"))
        assertEquals("Js.Promise2.Result", RescriptImportUtil.extractModulePath(openStmt))
    }

    @Test
    fun testExtractEmptyModulePath() {
        val openStmt = buildOpenStatement(emptyList())
        assertEquals("", RescriptImportUtil.extractModulePath(openStmt))
    }

    @Test
    fun testExtractModulePathSkipsChildWithNullNode() {
        // Build open statement with a child whose node returns null
        val openChild = SimpleStubElement(RescriptTokenTypes.OPEN, "open")
        val nullNodeChild = NullNodeStubElement("whitespace")
        val moduleChild = SimpleStubElement(RescriptTokenTypes.UIDENT, "Belt")

        openChild.next = nullNodeChild
        nullNodeChild.next = moduleChild

        val openStmt =
            object : SimpleStubElement(RescriptElementTypes.OPEN_STATEMENT, "open Belt") {
                override fun getFirstChild(): PsiElement = openChild
            }

        assertEquals("Belt", RescriptImportUtil.extractModulePath(openStmt))
    }

    // ── Stub helpers ─────────────────────────────────────────────────

    private fun buildOpenStatement(pathTokens: List<String>): PsiElement {
        data class TokenInfo(
            val type: IElementType,
            val text: String,
        )

        val tokens = mutableListOf(TokenInfo(RescriptTokenTypes.OPEN, "open"))
        for (token in pathTokens) {
            if (token == ".") {
                tokens.add(TokenInfo(RescriptTokenTypes.DOT, "."))
            } else {
                tokens.add(TokenInfo(RescriptTokenTypes.UIDENT, token))
            }
        }

        // Create stub child elements as linked list
        val children = tokens.map { info -> SimpleStubElement(info.type, info.text) }
        for (i in children.indices) {
            if (i + 1 < children.size) {
                children[i].next = children[i + 1]
            }
        }

        return object : SimpleStubElement(RescriptElementTypes.OPEN_STATEMENT, "open ${pathTokens.joinToString("")}") {
            override fun getFirstChild(): PsiElement = children.first()
        }
    }

    /** PsiElement stub whose getNode() returns null, simulating missing AST. */
    private class NullNodeStubElement(
        private val textContent: String,
    ) : PsiElement by stubProxy() {
        var next: PsiElement? = null

        override fun getNode(): ASTNode? = null

        override fun getText(): String = textContent

        override fun getNextSibling(): PsiElement? = next
    }

    /** Minimal PsiElement stub that exposes elementType, text, and sibling chain. */
    private open class SimpleStubElement(
        private val elementType: IElementType,
        private val textContent: String,
    ) : PsiElement by stubProxy() {
        var next: PsiElement? = null

        override fun getNode(): ASTNode = stubAstNode(elementType)

        override fun getText(): String = textContent

        override fun getNextSibling(): PsiElement? = next

        override fun getFirstChild(): PsiElement? = null
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
