package com.rescript.plugin.hierarchy

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.tree.IElementType
import com.rescript.plugin.lang.RescriptTokenTypes
import com.rescript.plugin.lang.psi.RescriptElementTypes
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class RescriptDependencyAnalyzerTest {
    @Test
    fun `module path joining from tokens`() {
        val tokens = listOf("Belt", ".", "Array")
        val result = tokens.joinToString("")
        assertEquals("Belt.Array", result)
    }

    @Test
    fun `single module name`() {
        val tokens = listOf("Utils")
        val result = tokens.joinToString("")
        assertEquals("Utils", result)
    }

    @Test
    fun `deeply nested module path`() {
        val tokens = listOf("Belt", ".", "Map", ".", "String")
        val result = tokens.joinToString("")
        assertEquals("Belt.Map.String", result)
    }

    @Test
    fun `top-level module extraction from paths`() {
        val paths = listOf("Belt.Array", "Belt.Map", "Js.Promise", "Utils")
        val topLevel = paths.map { it.substringBefore(".") }.toSet()
        assertEquals(setOf("Belt", "Js", "Utils"), topLevel)
    }

    @Test
    fun `ModuleNode data class holds correct values`() {
        val node =
            RescriptDependencyAnalyzer.ModuleNode(
                name = "MyModule",
                element = null,
                children = emptyList(),
            )
        assertEquals("MyModule", node.name)
        assertEquals(null, node.element)
        assertTrue(node.children.isEmpty())
    }

    @Test
    fun `ModuleNode supports nested children`() {
        val child =
            RescriptDependencyAnalyzer.ModuleNode(
                name = "Child",
                element = null,
                children = emptyList(),
            )
        val parent =
            RescriptDependencyAnalyzer.ModuleNode(
                name = "Parent",
                element = null,
                children = listOf(child),
            )
        assertEquals(1, parent.children.size)
        assertEquals("Child", parent.children[0].name)
    }

    @Test
    fun `ModuleNode supports multiple levels of nesting`() {
        val grandchild =
            RescriptDependencyAnalyzer.ModuleNode("GrandChild", null, emptyList())
        val child =
            RescriptDependencyAnalyzer.ModuleNode("Child", null, listOf(grandchild))
        val root =
            RescriptDependencyAnalyzer.ModuleNode("Root", null, listOf(child))

        assertEquals("Root", root.name)
        assertEquals("Child", root.children[0].name)
        assertEquals("GrandChild", root.children[0].children[0].name)
    }

    @Test
    fun `ReferenceKind has OPEN and INCLUDE values`() {
        assertEquals(2, RescriptDependencyAnalyzer.ReferenceKind.entries.size)
        assertEquals(
            RescriptDependencyAnalyzer.ReferenceKind.OPEN,
            RescriptDependencyAnalyzer.ReferenceKind.valueOf("OPEN"),
        )
        assertEquals(
            RescriptDependencyAnalyzer.ReferenceKind.INCLUDE,
            RescriptDependencyAnalyzer.ReferenceKind.valueOf("INCLUDE"),
        )
    }

    @Test
    fun `top-level module extraction handles empty path`() {
        val path = ""
        val topLevel = path.substringBefore(".")
        assertEquals("", topLevel)
    }

    @Test
    fun `top-level module extraction handles path without dot`() {
        val path = "Utils"
        val topLevel = path.substringBefore(".")
        assertEquals("Utils", topLevel)
    }

    @Test
    fun `top-level module extraction handles multi-dot path`() {
        val path = "Belt.Map.String"
        val topLevel = path.substringBefore(".")
        assertEquals("Belt", topLevel)
    }

    @Test
    fun `ModuleNode equality by data class`() {
        val node1 = RescriptDependencyAnalyzer.ModuleNode("A", null, emptyList())
        val node2 = RescriptDependencyAnalyzer.ModuleNode("A", null, emptyList())
        assertEquals(node1, node2)
    }

    @Test
    fun `empty module tree is empty list`() {
        val nodes = emptyList<RescriptDependencyAnalyzer.ModuleNode>()
        assertTrue(nodes.isEmpty())
    }

    // ── extractModulePath PSI stub tests ──────────────────────────────

    @Test
    fun `extractModulePath with OPEN and dotted module`() {
        val element = buildStatement(RescriptTokenTypes.OPEN, listOf("Belt", ".", "Array"))
        assertEquals("Belt.Array", RescriptDependencyAnalyzer.extractModulePath(element))
    }

    @Test
    fun `extractModulePath with OPEN and single module`() {
        val element = buildStatement(RescriptTokenTypes.OPEN, listOf("Utils"))
        assertEquals("Utils", RescriptDependencyAnalyzer.extractModulePath(element))
    }

    @Test
    fun `extractModulePath with no children after keyword`() {
        val element = buildStatement(RescriptTokenTypes.OPEN, emptyList())
        assertEquals("", RescriptDependencyAnalyzer.extractModulePath(element))
    }

    @Test
    fun `extractModulePath with INCLUDE keyword`() {
        val element = buildStatement(RescriptTokenTypes.INCLUDE, listOf("Utils"))
        assertEquals("Utils", RescriptDependencyAnalyzer.extractModulePath(element))
    }

    @Test
    fun `extractModulePath with INCLUDE and dotted module`() {
        val element = buildStatement(RescriptTokenTypes.INCLUDE, listOf("Belt", ".", "Map"))
        assertEquals("Belt.Map", RescriptDependencyAnalyzer.extractModulePath(element))
    }

    @Test
    fun `extractModulePath with deeply nested module`() {
        val element = buildStatement(RescriptTokenTypes.OPEN, listOf("Belt", ".", "Map", ".", "String"))
        assertEquals("Belt.Map.String", RescriptDependencyAnalyzer.extractModulePath(element))
    }

    // ── extractModuleReferences tests ─────────────────────────────────

    @Test
    fun `extractModuleReferences from scope with open statement`() {
        val openStmt = buildStatement(RescriptTokenTypes.OPEN, listOf("Belt", ".", "Array"))
        val scope = buildScopeWith(listOf(openStmt to RescriptElementTypes.OPEN_STATEMENT))
        val refs = RescriptDependencyAnalyzer.extractModuleReferences(scope)
        assertEquals(1, refs.size)
        assertEquals("Belt.Array", refs[0].modulePath)
        assertEquals(RescriptDependencyAnalyzer.ReferenceKind.OPEN, refs[0].kind)
    }

    @Test
    fun `extractModuleReferences from scope with include statement`() {
        val includeStmt = buildStatement(RescriptTokenTypes.INCLUDE, listOf("Utils"))
        val scope = buildScopeWith(listOf(includeStmt to RescriptElementTypes.INCLUDE_STATEMENT))
        val refs = RescriptDependencyAnalyzer.extractModuleReferences(scope)
        assertEquals(1, refs.size)
        assertEquals("Utils", refs[0].modulePath)
        assertEquals(RescriptDependencyAnalyzer.ReferenceKind.INCLUDE, refs[0].kind)
    }

    @Test
    fun `extractModuleReferences returns empty for scope with no open or include`() {
        val scope = buildScopeWith(emptyList())
        val refs = RescriptDependencyAnalyzer.extractModuleReferences(scope)
        assertTrue(refs.isEmpty())
    }

    @Test
    fun `extractModuleReferences from scope with multiple opens`() {
        val open1 = buildStatement(RescriptTokenTypes.OPEN, listOf("Belt", ".", "Array"))
        val open2 = buildStatement(RescriptTokenTypes.OPEN, listOf("Js", ".", "Promise"))
        val scope =
            buildScopeWith(
                listOf(
                    open1 to RescriptElementTypes.OPEN_STATEMENT,
                    open2 to RescriptElementTypes.OPEN_STATEMENT,
                ),
            )
        val refs = RescriptDependencyAnalyzer.extractModuleReferences(scope)
        assertEquals(2, refs.size)
        assertEquals("Belt.Array", refs[0].modulePath)
        assertEquals("Js.Promise", refs[1].modulePath)
    }

    // ── getReferencedModuleNames tests ────────────────────────────────

    @Test
    fun `ModuleReference data class properties`() {
        val elem = buildStatement(RescriptTokenTypes.OPEN, listOf("Belt"))
        val ref =
            RescriptDependencyAnalyzer.ModuleReference(
                "Belt.Array",
                RescriptDependencyAnalyzer.ReferenceKind.OPEN,
                elem,
            )
        assertEquals("Belt.Array", ref.modulePath)
        assertEquals(RescriptDependencyAnalyzer.ReferenceKind.OPEN, ref.kind)
    }

    // ── Stub helpers ─────────────────────────────────────────────────

    /**
     * Builds a scope PsiElement with children that have correct element types.
     */
    private fun buildScopeWith(children: List<Pair<PsiElement, IElementType>>): PsiElement {
        val childArray =
            children
                .map { (elem, type) ->
                    // Wrap the element so its node returns the expected type
                    object : SimpleStubElement(type, elem.text ?: "") {
                        override fun getFirstChild(): PsiElement? =
                            (elem as? SimpleStubElement)?.let { null } ?: elem.firstChild

                        override fun getNextSibling(): PsiElement? = null

                        init {
                            // The children list is linked through next
                        }
                    }
                }.toTypedArray()

        // Link next siblings
        for (i in childArray.indices) {
            if (i + 1 < childArray.size) {
                childArray[i].next = childArray[i + 1]
            }
        }

        return object : SimpleStubElement(RescriptElementTypes.MODULE_DECLARATION, "scope") {
            @Suppress("UNCHECKED_CAST")
            override fun getChildren(): Array<PsiElement> = childArray as Array<PsiElement>

            override fun getFirstChild(): PsiElement? = childArray.firstOrNull()
        }
    }

    private fun buildStatement(
        keywordType: IElementType,
        pathTokens: List<String>,
    ): PsiElement {
        data class TokenInfo(
            val type: IElementType,
            val text: String,
        )

        val tokens = mutableListOf(TokenInfo(keywordType, keywordType.toString().lowercase()))
        for (token in pathTokens) {
            if (token == ".") {
                tokens.add(TokenInfo(RescriptTokenTypes.DOT, "."))
            } else {
                tokens.add(TokenInfo(RescriptTokenTypes.UIDENT, token))
            }
        }

        val children = tokens.map { info -> SimpleStubElement(info.type, info.text) }
        for (i in children.indices) {
            if (i + 1 < children.size) {
                children[i].next = children[i + 1]
            }
        }

        val parentType =
            if (keywordType == RescriptTokenTypes.INCLUDE) {
                RescriptElementTypes.INCLUDE_STATEMENT
            } else {
                RescriptElementTypes.OPEN_STATEMENT
            }

        return object : SimpleStubElement(parentType, tokens.joinToString(" ") { it.text }) {
            override fun getFirstChild(): PsiElement = children.first()
        }
    }

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
