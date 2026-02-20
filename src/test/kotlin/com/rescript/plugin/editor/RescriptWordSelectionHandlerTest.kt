package com.rescript.plugin.editor

import com.intellij.lang.ASTNode
import com.intellij.lang.Language
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.tree.IElementType
import com.rescript.plugin.RescriptLanguage
import com.rescript.plugin.lang.RescriptTokenTypes
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RescriptWordSelectionHandlerTest {
    // -- RescriptStringSelectionHandler --

    @Test
    fun `StringSelectionHandler can be instantiated`() {
        val handler = RescriptStringSelectionHandler()
        assertNotNull(handler)
    }

    @Test
    fun `StringSelectionHandler canSelect returns true for STRING_VALUE in ReScript`() {
        val element = stubPsiElement(RescriptLanguage, RescriptTokenTypes.STRING_VALUE)
        val handler = RescriptStringSelectionHandler()
        assertTrue(handler.canSelect(element))
    }

    @Test
    fun `StringSelectionHandler canSelect returns false for LIDENT in ReScript`() {
        val element = stubPsiElement(RescriptLanguage, RescriptTokenTypes.LIDENT)
        val handler = RescriptStringSelectionHandler()
        assertFalse(handler.canSelect(element))
    }

    @Test
    fun `StringSelectionHandler canSelect returns false for non-ReScript language`() {
        val element = stubPsiElement(null, RescriptTokenTypes.STRING_VALUE)
        val handler = RescriptStringSelectionHandler()
        assertFalse(handler.canSelect(element))
    }

    @Test
    fun `StringSelectionHandler canSelect returns false for SINGLE_COMMENT`() {
        val element = stubPsiElement(RescriptLanguage, RescriptTokenTypes.SINGLE_COMMENT)
        val handler = RescriptStringSelectionHandler()
        assertFalse(handler.canSelect(element))
    }

    // -- RescriptBracketSelectionHandler --

    @Test
    fun `BracketSelectionHandler can be instantiated`() {
        val handler = RescriptBracketSelectionHandler()
        assertNotNull(handler)
    }

    @Test
    fun `BracketSelectionHandler canSelect returns true for LPAREN`() {
        val element = stubPsiElement(RescriptLanguage, RescriptTokenTypes.LPAREN)
        val handler = RescriptBracketSelectionHandler()
        assertTrue(handler.canSelect(element))
    }

    @Test
    fun `BracketSelectionHandler canSelect returns true for RPAREN`() {
        val element = stubPsiElement(RescriptLanguage, RescriptTokenTypes.RPAREN)
        val handler = RescriptBracketSelectionHandler()
        assertTrue(handler.canSelect(element))
    }

    @Test
    fun `BracketSelectionHandler canSelect returns true for LBRACE`() {
        val element = stubPsiElement(RescriptLanguage, RescriptTokenTypes.LBRACE)
        val handler = RescriptBracketSelectionHandler()
        assertTrue(handler.canSelect(element))
    }

    @Test
    fun `BracketSelectionHandler canSelect returns true for RBRACE`() {
        val element = stubPsiElement(RescriptLanguage, RescriptTokenTypes.RBRACE)
        val handler = RescriptBracketSelectionHandler()
        assertTrue(handler.canSelect(element))
    }

    @Test
    fun `BracketSelectionHandler canSelect returns true for LBRACKET`() {
        val element = stubPsiElement(RescriptLanguage, RescriptTokenTypes.LBRACKET)
        val handler = RescriptBracketSelectionHandler()
        assertTrue(handler.canSelect(element))
    }

    @Test
    fun `BracketSelectionHandler canSelect returns true for RBRACKET`() {
        val element = stubPsiElement(RescriptLanguage, RescriptTokenTypes.RBRACKET)
        val handler = RescriptBracketSelectionHandler()
        assertTrue(handler.canSelect(element))
    }

    @Test
    fun `BracketSelectionHandler canSelect returns false for LIDENT`() {
        val element = stubPsiElement(RescriptLanguage, RescriptTokenTypes.LIDENT)
        val handler = RescriptBracketSelectionHandler()
        assertFalse(handler.canSelect(element))
    }

    @Test
    fun `BracketSelectionHandler canSelect returns false for non-ReScript language`() {
        val element = stubPsiElement(null, RescriptTokenTypes.LPAREN)
        val handler = RescriptBracketSelectionHandler()
        assertFalse(handler.canSelect(element))
    }

    // -- RescriptCommentSelectionHandler --

    @Test
    fun `CommentSelectionHandler can be instantiated`() {
        val handler = RescriptCommentSelectionHandler()
        assertNotNull(handler)
    }

    @Test
    fun `CommentSelectionHandler canSelect returns true for SINGLE_COMMENT`() {
        val element = stubPsiElement(RescriptLanguage, RescriptTokenTypes.SINGLE_COMMENT)
        val handler = RescriptCommentSelectionHandler()
        assertTrue(handler.canSelect(element))
    }

    @Test
    fun `CommentSelectionHandler canSelect returns true for MULTI_COMMENT`() {
        val element = stubPsiElement(RescriptLanguage, RescriptTokenTypes.MULTI_COMMENT)
        val handler = RescriptCommentSelectionHandler()
        assertTrue(handler.canSelect(element))
    }

    @Test
    fun `CommentSelectionHandler canSelect returns false for STRING_VALUE`() {
        val element = stubPsiElement(RescriptLanguage, RescriptTokenTypes.STRING_VALUE)
        val handler = RescriptCommentSelectionHandler()
        assertFalse(handler.canSelect(element))
    }

    @Test
    fun `CommentSelectionHandler canSelect returns false for non-ReScript language`() {
        val element = stubPsiElement(null, RescriptTokenTypes.SINGLE_COMMENT)
        val handler = RescriptCommentSelectionHandler()
        assertFalse(handler.canSelect(element))
    }

    // -- Stub helpers --

    private fun stubPsiElement(
        lang: Language?,
        tokenType: IElementType,
    ): PsiElement {
        val file =
            java.lang.reflect.Proxy.newProxyInstance(
                PsiFile::class.java.classLoader,
                arrayOf(PsiFile::class.java),
            ) { _, method, _ ->
                when (method.name) {
                    "getLanguage" -> lang
                    "toString" -> "StubPsiFile($lang)"
                    "hashCode" -> 0
                    "equals" -> false
                    else -> null
                }
            } as PsiFile

        val node =
            java.lang.reflect.Proxy.newProxyInstance(
                ASTNode::class.java.classLoader,
                arrayOf(ASTNode::class.java),
            ) { _, method, _ ->
                when (method.name) {
                    "getElementType" -> tokenType
                    "toString" -> "StubASTNode($tokenType)"
                    "hashCode" -> tokenType.hashCode()
                    "equals" -> false
                    else -> null
                }
            } as ASTNode

        return java.lang.reflect.Proxy.newProxyInstance(
            PsiElement::class.java.classLoader,
            arrayOf(PsiElement::class.java),
        ) { _, method, _ ->
            when (method.name) {
                "getLanguage" -> lang ?: Language.ANY
                "getContainingFile" -> file
                "getNode" -> node
                "toString" -> "StubPsiElement($tokenType)"
                "hashCode" -> tokenType.hashCode()
                "equals" -> false
                else -> null
            }
        } as PsiElement
    }
}
