package com.rescript.plugin.inspection

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.tree.IElementType
import com.rescript.plugin.lang.RescriptTokenTypes
import com.rescript.plugin.lang.psi.RescriptElementTypes
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class RescriptInspectionSuppressorTest {
    @Test
    fun `suppressor can be instantiated`() {
        val suppressor = RescriptInspectionSuppressor()
        assertNotNull(suppressor)
    }

    @Test
    fun `getSuppressActions returns empty array`() {
        val suppressor = RescriptInspectionSuppressor()
        val actions = suppressor.getSuppressActions(null, "RescriptDuplicateOpen")
        assertNotNull(actions)
        assert(actions.isEmpty())
    }

    // -- isInLongLineSuppressedContext tests --

    @Test
    fun `string value suppresses long line`() {
        val element = stubPsiElement(RescriptTokenTypes.STRING_VALUE)
        assertTrue(RescriptInspectionSuppressor.isInLongLineSuppressedContext(element))
    }

    @Test
    fun `annotation name suppresses long line`() {
        val element = stubPsiElement(RescriptTokenTypes.ANNOTATION_NAME)
        assertTrue(RescriptInspectionSuppressor.isInLongLineSuppressedContext(element))
    }

    @Test
    fun `js string open suppresses long line`() {
        val element = stubPsiElement(RescriptTokenTypes.JS_STRING_OPEN)
        assertTrue(RescriptInspectionSuppressor.isInLongLineSuppressedContext(element))
    }

    @Test
    fun `js string close suppresses long line`() {
        val element = stubPsiElement(RescriptTokenTypes.JS_STRING_CLOSE)
        assertTrue(RescriptInspectionSuppressor.isInLongLineSuppressedContext(element))
    }

    @Test
    fun `raw keyword suppresses long line`() {
        val element = stubPsiElement(RescriptTokenTypes.RAW)
        assertTrue(RescriptInspectionSuppressor.isInLongLineSuppressedContext(element))
    }

    @Test
    fun `ffi keyword suppresses long line`() {
        val element = stubPsiElement(RescriptTokenTypes.FFI)
        assertTrue(RescriptInspectionSuppressor.isInLongLineSuppressedContext(element))
    }

    @Test
    fun `annotation parent suppresses long line`() {
        val annotation = stubPsiElement(RescriptElementTypes.ANNOTATION)
        val child = stubPsiElement(RescriptTokenTypes.LIDENT, parent = annotation)
        assertTrue(RescriptInspectionSuppressor.isInLongLineSuppressedContext(child))
    }

    @Test
    fun `normal code does not suppress long line`() {
        val element = stubPsiElement(RescriptTokenTypes.LIDENT)
        assertFalse(RescriptInspectionSuppressor.isInLongLineSuppressedContext(element))
    }

    @Test
    fun `keyword does not suppress long line`() {
        val element = stubPsiElement(RescriptTokenTypes.LET)
        assertFalse(RescriptInspectionSuppressor.isInLongLineSuppressedContext(element))
    }

    @Test
    fun `element inside let declaration does not suppress long line`() {
        val letDecl = stubPsiElement(RescriptElementTypes.LET_DECLARATION)
        val child = stubPsiElement(RescriptTokenTypes.LIDENT, parent = letDecl)
        assertFalse(RescriptInspectionSuppressor.isInLongLineSuppressedContext(child))
    }

    // -- Stub helpers --

    private fun stubPsiElement(
        type: IElementType,
        parent: PsiElement? = null,
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
                "getText" -> ""
                "getPrevSibling" -> null
                "toString" -> "StubPsiElement($type)"
                "hashCode" -> System.identityHashCode(node)
                "equals" -> false
                else -> null
            }
        } as PsiElement
    }
}
