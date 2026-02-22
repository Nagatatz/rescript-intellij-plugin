package com.rescript.plugin

import com.intellij.lang.ASTNode
import com.intellij.openapi.project.Project
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
     * Creates a PsiElement proxy stub with an associated ASTNode.
     *
     * Supports parent, prevSibling, nextSibling, and text for testing
     * code that traverses PSI trees without the full IntelliJ Platform.
     *
     * @param type the element type for the backing ASTNode
     * @param parent the parent element returned by [PsiElement.getParent]
     * @param prevSibling the previous sibling returned by [PsiElement.getPrevSibling]
     * @param nextSibling the next sibling returned by [PsiElement.getNextSibling]
     * @param text the text content returned by [PsiElement.getText]
     * @return a proxy-based PsiElement stub
     */
    fun stubPsiElement(
        type: IElementType,
        parent: PsiElement? = null,
        prevSibling: PsiElement? = null,
        nextSibling: PsiElement? = null,
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
                "getNextSibling" -> nextSibling
                "getText" -> text
                "getContainingFile" -> null
                "toString" -> "StubPsiElement($type)"
                "hashCode" -> System.identityHashCode(node)
                "equals" -> false
                else -> null
            }
        } as PsiElement
    }

    /**
     * Creates a minimal Project proxy stub.
     *
     * All methods return null except toString, hashCode, and equals.
     *
     * @return a proxy-based Project stub
     */
    fun stubProject(): Project =
        java.lang.reflect.Proxy.newProxyInstance(
            Project::class.java.classLoader,
            arrayOf(Project::class.java),
        ) { _, method, _ ->
            when (method.name) {
                "toString" -> "StubProject"
                "hashCode" -> 0
                "equals" -> false
                else -> null
            }
        } as Project

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
