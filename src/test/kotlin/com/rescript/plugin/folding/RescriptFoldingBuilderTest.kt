package com.rescript.plugin.folding

import com.intellij.lang.ASTNode
import com.intellij.openapi.util.TextRange
import com.intellij.psi.tree.IElementType
import com.rescript.plugin.lang.RescriptTokenTypes
import com.rescript.plugin.lang.psi.RescriptElementTypes
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Subclass that exposes protected methods for unit testing.
 */
private class TestableFoldingBuilder : RescriptFoldingBuilder() {
    public override fun isRegionCollapsedByDefault(node: ASTNode): Boolean = super.isRegionCollapsedByDefault(node)

    public override fun getLanguagePlaceholderText(
        node: ASTNode,
        range: TextRange,
    ): String? = super.getLanguagePlaceholderText(node, range)

    public override fun isCustomFoldingCandidate(node: ASTNode): Boolean = super.isCustomFoldingCandidate(node)
}

class RescriptFoldingBuilderTest {
    private val builder = TestableFoldingBuilder()

    /** Minimal ASTNode stub that only exposes elementType and textRange. */
    private fun stubNode(type: IElementType): ASTNode =
        java.lang.reflect.Proxy.newProxyInstance(
            ASTNode::class.java.classLoader,
            arrayOf(ASTNode::class.java),
        ) { _, method, _ ->
            when (method.name) {
                "getElementType" -> type
                "getTextRange" -> TextRange(0, 10)
                "toString" -> "StubASTNode($type)"
                "hashCode" -> System.identityHashCode(type)
                "equals" -> false
                "getFirstChildNode" -> null
                else -> null
            }
        } as ASTNode

    /**
     * Creates a JSX_ELEMENT ASTNode with child nodes simulating `<TagName>`.
     * The child chain: TAG_LT -> JSX_TAG_NAME(name) -> DOT -> ... -> TAG_GT
     * Built in reverse to ensure treeNext links are correct.
     */
    private fun stubJsxElementNode(vararg tagParts: String): ASTNode {
        // Collect (type, text) pairs in order
        val specs = mutableListOf<Pair<IElementType, String>>()
        specs.add(RescriptTokenTypes.TAG_LT to "<")
        for ((i, part) in tagParts.withIndex()) {
            if (i > 0) {
                specs.add(RescriptTokenTypes.DOT to ".")
            }
            val nameType =
                if (part[0].isUpperCase()) RescriptTokenTypes.JSX_COMPONENT_NAME else RescriptTokenTypes.JSX_TAG_NAME
            specs.add(nameType to part)
        }
        specs.add(RescriptTokenTypes.TAG_GT to ">")

        // Build chain in reverse so each node's treeNext is already finalized
        var next: ASTNode? = null
        for (i in specs.indices.reversed()) {
            val (type, text) = specs[i]
            val currentNext = next
            next =
                java.lang.reflect.Proxy.newProxyInstance(
                    ASTNode::class.java.classLoader,
                    arrayOf(ASTNode::class.java),
                ) { _, method, _ ->
                    when (method.name) {
                        "getElementType" -> type
                        "getText" -> text
                        "getTreeNext" -> currentNext
                        "toString" -> "StubChildASTNode($type, $text)"
                        "hashCode" -> System.identityHashCode(type)
                        "equals" -> false
                        else -> null
                    }
                } as ASTNode
        }

        val firstChild = next

        return java.lang.reflect.Proxy.newProxyInstance(
            ASTNode::class.java.classLoader,
            arrayOf(ASTNode::class.java),
        ) { _, method, _ ->
            when (method.name) {
                "getElementType" -> RescriptElementTypes.JSX_ELEMENT
                "getTextRange" -> TextRange(0, 10)
                "getFirstChildNode" -> firstChild
                "toString" -> "StubASTNode(JSX_ELEMENT)"
                "hashCode" -> System.identityHashCode(RescriptElementTypes.JSX_ELEMENT)
                "equals" -> false
                else -> null
            }
        } as ASTNode
    }

    @Test
    fun testIsRegionCollapsedByDefaultReturnsFalse() {
        assertFalse(builder.isRegionCollapsedByDefault(stubNode(RescriptTokenTypes.MULTI_COMMENT)))
        assertFalse(builder.isRegionCollapsedByDefault(stubNode(RescriptElementTypes.MODULE_DECLARATION)))
        assertFalse(builder.isRegionCollapsedByDefault(stubNode(RescriptElementTypes.LET_DECLARATION)))
    }

    @Test
    fun testGetLanguagePlaceholderTextForMultiComment() {
        val node = stubNode(RescriptTokenTypes.MULTI_COMMENT)
        assertEquals("/* ... */", builder.getLanguagePlaceholderText(node, node.textRange))
    }

    @Test
    fun testGetLanguagePlaceholderTextForModuleDeclaration() {
        val node = stubNode(RescriptElementTypes.MODULE_DECLARATION)
        assertEquals(
            "module ... { ... }",
            builder.getLanguagePlaceholderText(node, node.textRange),
        )
    }

    @Test
    fun testGetLanguagePlaceholderTextForOtherElements() {
        val letNode = stubNode(RescriptElementTypes.LET_DECLARATION)
        assertEquals("{...}", builder.getLanguagePlaceholderText(letNode, letNode.textRange))
        val typeNode = stubNode(RescriptElementTypes.TYPE_DECLARATION)
        assertEquals("{...}", builder.getLanguagePlaceholderText(typeNode, typeNode.textRange))
    }

    @Test
    fun testGetLanguagePlaceholderTextForJsxFragment() {
        val node = stubNode(RescriptElementTypes.JSX_FRAGMENT)
        assertEquals("<>...</>", builder.getLanguagePlaceholderText(node, node.textRange))
    }

    // -- JSX_ELEMENT placeholder tests --

    @Test
    fun testGetLanguagePlaceholderTextForJsxElementSimpleTag() {
        val node = stubJsxElementNode("div")
        val result = builder.getLanguagePlaceholderText(node, node.textRange)
        assertEquals("<div>...</div>", result)
    }

    @Test
    fun testGetLanguagePlaceholderTextForJsxElementComponentTag() {
        val node = stubJsxElementNode("MyComponent")
        val result = builder.getLanguagePlaceholderText(node, node.textRange)
        assertEquals("<MyComponent>...</MyComponent>", result)
    }

    @Test
    fun testGetLanguagePlaceholderTextForJsxElementDottedTag() {
        val node = stubJsxElementNode("Ui", "Button")
        val result = builder.getLanguagePlaceholderText(node, node.textRange)
        assertEquals("<Ui.Button>...</Ui.Button>", result)
    }

    @Test
    fun testGetLanguagePlaceholderTextForJsxElementTripleDottedTag() {
        val node = stubJsxElementNode("Ns", "Ui", "Card")
        val result = builder.getLanguagePlaceholderText(node, node.textRange)
        assertEquals("<Ns.Ui.Card>...</Ns.Ui.Card>", result)
    }

    @Test
    fun testGetLanguagePlaceholderTextForJsxElementNoChildren() {
        // JSX_ELEMENT with no child nodes at all
        val node = stubNode(RescriptElementTypes.JSX_ELEMENT)
        val result = builder.getLanguagePlaceholderText(node, node.textRange)
        // extractJsxTagName returns "..." when no tag name parts found
        assertEquals("<...>...</...>", result)
    }

    @Test
    fun testIsCustomFoldingCandidateForSingleComment() {
        assertTrue(builder.isCustomFoldingCandidate(stubNode(RescriptTokenTypes.SINGLE_COMMENT)))
    }

    @Test
    fun testIsCustomFoldingCandidateForNonComment() {
        assertFalse(builder.isCustomFoldingCandidate(stubNode(RescriptTokenTypes.MULTI_COMMENT)))
        assertFalse(builder.isCustomFoldingCandidate(stubNode(RescriptElementTypes.LET_DECLARATION)))
    }

    @Test
    fun testIsCustomFoldingCandidateForJsxElement() {
        assertFalse(builder.isCustomFoldingCandidate(stubNode(RescriptElementTypes.JSX_ELEMENT)))
    }

    @Test
    fun testIsRegionCollapsedByDefaultForJsxElements() {
        assertFalse(builder.isRegionCollapsedByDefault(stubNode(RescriptElementTypes.JSX_ELEMENT)))
        assertFalse(builder.isRegionCollapsedByDefault(stubNode(RescriptElementTypes.JSX_FRAGMENT)))
    }
}
