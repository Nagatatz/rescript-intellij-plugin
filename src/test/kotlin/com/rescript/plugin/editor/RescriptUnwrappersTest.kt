package com.rescript.plugin.editor

import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`

class RescriptUnwrappersTest {
    // ── RescriptBaseUnwrapper ──────────────────────────────────────

    @Test
    fun `base unwrapper isApplicableTo returns true for any element`() {
        val unwrapper = RescriptBraceUnwrapper(0, 5)
        val element = mock(PsiElement::class.java)
        assertTrue(unwrapper.isApplicableTo(element))
    }

    @Test
    fun `base unwrapper getDescription returns configured description`() {
        val unwrapper = RescriptBraceUnwrapper(0, 5)
        val element = mock(PsiElement::class.java)
        assertEquals("Remove { }", unwrapper.getDescription(element))
    }

    @Test
    fun `base unwrapper collectAffectedElements adds element to list`() {
        val unwrapper = RescriptBraceUnwrapper(0, 5)
        val element = mock(PsiElement::class.java)
        val list = mutableListOf<PsiElement>()
        val result = unwrapper.collectAffectedElements(element, list)
        assertEquals(1, list.size)
        assertEquals(element, list[0])
        assertEquals(element, result)
    }

    // ── RescriptFunctionUnwrapper ──────────────────────────────────

    @Test
    fun `function unwrapper removes Some wrapper`() {
        val text = "let x = Some(value)"
        val editor = mockEditorWithText(text)
        val element = mock(PsiElement::class.java)

        // Some( starts at 8, ) ends at 19
        val unwrapper = RescriptFunctionUnwrapper("Some", 8, 19)
        val result = unwrapper.unwrap(editor, element)

        // innerStart = 8 + 4 + 1 = 13, innerEnd = 18
        verify(editor.document).replaceString(8, 19, "value")
        assertTrue(result.isEmpty())
    }

    @Test
    fun `function unwrapper removes Ok wrapper`() {
        val text = "let x = Ok(value)"
        val editor = mockEditorWithText(text)
        val element = mock(PsiElement::class.java)

        val unwrapper = RescriptFunctionUnwrapper("Ok", 8, 17)
        unwrapper.unwrap(editor, element)

        verify(editor.document).replaceString(8, 17, "value")
    }

    @Test
    fun `function unwrapper does nothing when inner range is empty`() {
        val text = "Some()"
        val editor = mockEditorWithText(text)
        val element = mock(PsiElement::class.java)

        // innerStart = 0 + 4 + 1 = 5, innerEnd = 6 - 1 = 5 => not less, skip
        val unwrapper = RescriptFunctionUnwrapper("Some", 0, 6)
        val result = unwrapper.unwrap(editor, element)
        assertTrue(result.isEmpty())
    }

    @Test
    fun `function unwrapper getDescription includes wrapper name`() {
        val unwrapper = RescriptFunctionUnwrapper("Error", 0, 10)
        val element = mock(PsiElement::class.java)
        assertEquals("Remove Error(...)", unwrapper.getDescription(element))
    }

    // ── RescriptBlockUnwrapper ─────────────────────────────────────

    @Test
    fun `block unwrapper extracts body from if block`() {
        val text = "if (cond) { body }"
        val editor = mockEditorWithText(text)
        val element = mock(PsiElement::class.java)

        val unwrapper = RescriptBlockUnwrapper("Unwrap if", 0, 12, 16, 18)
        unwrapper.unwrap(editor, element)

        verify(editor.document).replaceString(0, 18, "body")
    }

    @Test
    fun `block unwrapper trims whitespace from body`() {
        val text = "try {   expr   }"
        val editor = mockEditorWithText(text)
        val element = mock(PsiElement::class.java)

        val unwrapper = RescriptBlockUnwrapper("Unwrap try", 0, 5, 15, 16)
        unwrapper.unwrap(editor, element)

        verify(editor.document).replaceString(0, 16, "expr")
    }

    // ── RescriptBraceUnwrapper ─────────────────────────────────────

    @Test
    fun `brace unwrapper removes surrounding braces`() {
        val text = "{ body }"
        val editor = mockEditorWithText(text)
        val element = mock(PsiElement::class.java)

        val unwrapper = RescriptBraceUnwrapper(0, 8)
        unwrapper.unwrap(editor, element)

        verify(editor.document).replaceString(0, 8, "body")
    }

    @Test
    fun `brace unwrapper trims inner content`() {
        val text = "{  spaced  }"
        val editor = mockEditorWithText(text)
        val element = mock(PsiElement::class.java)

        val unwrapper = RescriptBraceUnwrapper(0, 12)
        unwrapper.unwrap(editor, element)

        verify(editor.document).replaceString(0, 12, "spaced")
    }

    private fun mockEditorWithText(text: String): Editor {
        val document = mock(Document::class.java)
        `when`(document.text).thenReturn(text)
        val editor = mock(Editor::class.java)
        `when`(editor.document).thenReturn(document)
        return editor
    }
}
