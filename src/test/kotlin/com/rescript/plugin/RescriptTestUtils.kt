package com.rescript.plugin

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.tree.IElementType

/**
 * Shared test utilities for creating lightweight PSI stubs
 * without requiring the full IntelliJ Platform test framework.
 */
object RescriptTestUtils {
    /**
     * Creates a dynamic proxy stub for any interface [T].
     * All methods return null (or appropriate defaults for toString/hashCode/equals).
     */
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

    /**
     * Creates a minimal ASTNode stub that only provides [getElementType].
     */
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

    /**
     * Creates an ASTNode stub with child node chain support.
     * Used for testing code that traverses [firstChildNode] / [treeNext].
     */
    fun stubAstNodeWithChildren(
        type: IElementType,
        children: List<Pair<IElementType, String>>,
    ): ASTNode {
        val childNodes =
            children.map { (childType, text) ->
                stubAstNodeLeaf(childType, text)
            }
        // Link treeNext chain
        for (i in childNodes.indices) {
            if (i + 1 < childNodes.size) {
                (childNodes[i] as MutableAstNodeStub).nextSibling = childNodes[i + 1]
            }
        }
        val firstChild = childNodes.firstOrNull()
        return java.lang.reflect.Proxy.newProxyInstance(
            ASTNode::class.java.classLoader,
            arrayOf(ASTNode::class.java),
        ) { _, method, _ ->
            when (method.name) {
                "getElementType" -> type
                "getFirstChildNode" -> firstChild
                "toString" -> "StubASTNode($type, children=${children.size})"
                "hashCode" -> type.hashCode()
                "equals" -> false
                else -> null
            }
        } as ASTNode
    }

    private fun stubAstNodeLeaf(
        type: IElementType,
        text: String,
    ): ASTNode {
        val stub = MutableAstNodeStub(type, text)
        return stub
    }

    private class MutableAstNodeStub(
        private val type: IElementType,
        private val text: String,
    ) : ASTNode by stubProxy() {
        var nextSibling: ASTNode? = null

        override fun getElementType(): IElementType = type

        override fun getText(): String = text

        override fun getTreeNext(): ASTNode? = nextSibling

        override fun toString(): String = "StubASTNodeLeaf($type, '$text')"
    }

    /**
     * Minimal PsiElement stub that exposes elementType, text, and sibling chain.
     */
    open class SimpleStubElement(
        private val elementType: IElementType,
        private val textContent: String,
    ) : PsiElement by stubProxy() {
        var next: PsiElement? = null

        override fun getNode(): ASTNode = stubAstNode(elementType)

        override fun getText(): String = textContent

        override fun getNextSibling(): PsiElement? = next

        override fun getFirstChild(): PsiElement? = null
    }
}
