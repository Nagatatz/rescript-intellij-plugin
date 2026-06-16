package com.rescript.plugin.lang.psi

import com.intellij.openapi.util.TextRange
import com.intellij.psi.util.PsiTreeUtil
import com.rescript.plugin.ParsingTestHelper
import com.rescript.plugin.RescriptParsingTestExtension
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

/**
 * Tests for [RescriptJsxTagPairUtil] tag-name extraction and side resolution.
 *
 * Uses [RescriptParsingTestExtension] to drive the real lexer+parser pipeline so
 * that `extractTagNames` and `sideAt` operate on genuine PSI children with offsets.
 *
 * @see RescriptJsxTagPairUtil
 */
@ExtendWith(RescriptParsingTestExtension::class)
class RescriptJsxTagPairUtilTest {
    private lateinit var parsingHelper: ParsingTestHelper

    private fun firstElement(
        code: String,
        type: com.intellij.psi.tree.IElementType,
    ): com.intellij.psi.PsiElement {
        val file = parsingHelper.parseCode(code)
        return parsingHelper.findElements(file, type).first().psi
    }

    @Test
    fun testExtractSimpleElement() {
        val element = firstElement("let x = <div></div>", RescriptElementTypes.JSX_ELEMENT)
        val names = RescriptJsxTagPairUtil.extractTagNames(element)
        assertEquals("div", names.openName)
        assertEquals("div", names.closeName)
        assertNotNull(names.openRange)
        assertNotNull(names.closeRange)
    }

    @Test
    fun testExtractMismatchedNames() {
        val element = firstElement("let x = <div></span>", RescriptElementTypes.JSX_ELEMENT)
        val names = RescriptJsxTagPairUtil.extractTagNames(element)
        assertEquals("div", names.openName)
        assertEquals("span", names.closeName)
    }

    @Test
    fun testExtractDottedPath() {
        val element = firstElement("let x = <Module.Comp></Module.Comp>", RescriptElementTypes.JSX_ELEMENT)
        val names = RescriptJsxTagPairUtil.extractTagNames(element)
        // Dotted segments are collected verbatim, including the separators.
        assertEquals("Module.Comp", names.openName)
        assertEquals("Module.Comp", names.closeName)
    }

    @Test
    fun testFragmentHasNoNames() {
        val element = firstElement("let x = <> </>", RescriptElementTypes.JSX_FRAGMENT)
        val names = RescriptJsxTagPairUtil.extractTagNames(element)
        assertNull(names.openName)
        assertNull(names.closeName)
    }

    @Test
    fun testSelfClosingHasNoCloseName() {
        val element = firstElement("let x = <br />", RescriptElementTypes.JSX_SELF_CLOSING_ELEMENT)
        val names = RescriptJsxTagPairUtil.extractTagNames(element)
        assertEquals("br", names.openName)
        assertNull(names.closeName)
    }

    @Test
    fun testMissingCloseName() {
        // Unterminated element: the open tag is present but no closing name follows.
        val element = firstElement("let x = <div>text", RescriptElementTypes.JSX_ELEMENT)
        val names = RescriptJsxTagPairUtil.extractTagNames(element)
        assertEquals("div", names.openName)
        assertNull(names.closeName)
    }

    @Test
    fun testSideAtResolvesOpenAndClose() {
        val element = firstElement("let x = <div></div>", RescriptElementTypes.JSX_ELEMENT)
        val names = RescriptJsxTagPairUtil.extractTagNames(element)
        val openOffset = names.openRange!!.startOffset
        val closeOffset = names.closeRange!!.startOffset
        assertEquals(TagSide.OPEN, RescriptJsxTagPairUtil.sideAt(element, openOffset))
        assertEquals(TagSide.CLOSE, RescriptJsxTagPairUtil.sideAt(element, closeOffset))
    }

    @Test
    fun testSideAtOutsideNamesReturnsNull() {
        val element = firstElement("let x = <div></div>", RescriptElementTypes.JSX_ELEMENT)
        // Offset 0 is well before the element's tag names.
        assertNull(RescriptJsxTagPairUtil.sideAt(element, 0))
    }

    @Test
    fun testFindEnclosingJsxElement() {
        val file = parsingHelper.parseCode("let x = <div></div>")
        val element = parsingHelper.findElements(file, RescriptElementTypes.JSX_ELEMENT).first().psi
        // A descendant leaf of the element resolves back to the enclosing JSX_ELEMENT.
        val leaf = PsiTreeUtil.getDeepestFirst(element)
        assertEquals(element, RescriptJsxTagPairUtil.findEnclosingJsxElement(leaf))
    }

    @Test
    fun testFindEnclosingJsxElementReturnsNullOutsideJsx() {
        val file = parsingHelper.parseCode("let x = 1")
        val leaf = PsiTreeUtil.getDeepestFirst(file)
        assertNull(RescriptJsxTagPairUtil.findEnclosingJsxElement(leaf))
    }

    @Test
    fun testComputeSyncEditOpenToClose() {
        // Open side was edited from "div" to "divx"; the close side still reads the pre-edit "div".
        val names =
            JsxTagNames(
                openName = "divx",
                openRange = TextRange(0, 4),
                closeName = "div",
                closeRange = TextRange(10, 13),
            )
        val edit = RescriptJsxTagPairUtil.computeSyncEdit(names, TagSide.OPEN, "div")
        assertNotNull(edit)
        assertEquals(TextRange(10, 13), edit!!.range)
        assertEquals("divx", edit.replacement)
    }

    @Test
    fun testComputeSyncEditCloseToOpen() {
        // Close side was edited from "div" to "span"; mirror back onto the open side.
        val names =
            JsxTagNames(
                openName = "div",
                openRange = TextRange(0, 3),
                closeName = "span",
                closeRange = TextRange(10, 14),
            )
        val edit = RescriptJsxTagPairUtil.computeSyncEdit(names, TagSide.CLOSE, "div")
        assertNotNull(edit)
        assertEquals(TextRange(0, 3), edit!!.range)
        assertEquals("span", edit.replacement)
    }

    @Test
    fun testComputeSyncEditPreEditMismatchReturnsNull() {
        // The two sides did not match before the edit, so a deliberate mismatch must be preserved.
        val names =
            JsxTagNames(
                openName = "divx",
                openRange = TextRange(0, 4),
                closeName = "span",
                closeRange = TextRange(10, 14),
            )
        assertNull(RescriptJsxTagPairUtil.computeSyncEdit(names, TagSide.OPEN, "div"))
    }

    @Test
    fun testComputeSyncEditAlreadyInSyncReturnsNull() {
        // Both sides already equal: nothing to mirror.
        val names =
            JsxTagNames(
                openName = "div",
                openRange = TextRange(0, 3),
                closeName = "div",
                closeRange = TextRange(8, 11),
            )
        assertNull(RescriptJsxTagPairUtil.computeSyncEdit(names, TagSide.OPEN, "div"))
    }

    @Test
    fun testComputeSyncEditSelfClosingReturnsNull() {
        // Self-closing: no close name/range to mirror onto.
        val names =
            JsxTagNames(
                openName = "br",
                openRange = TextRange(0, 2),
                closeName = null,
                closeRange = null,
            )
        assertNull(RescriptJsxTagPairUtil.computeSyncEdit(names, TagSide.OPEN, "b"))
    }

    @Test
    fun testComputeSyncEditFragmentReturnsNull() {
        // Fragment: neither side has a name, so there is nothing to mirror.
        val names =
            JsxTagNames(
                openName = null,
                openRange = null,
                closeName = null,
                closeRange = null,
            )
        assertNull(RescriptJsxTagPairUtil.computeSyncEdit(names, TagSide.OPEN, null))
    }
}
