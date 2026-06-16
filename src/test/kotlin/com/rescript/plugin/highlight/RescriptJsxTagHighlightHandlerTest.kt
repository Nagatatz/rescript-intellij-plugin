package com.rescript.plugin.highlight

import com.intellij.psi.PsiElement
import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import com.rescript.plugin.IntelliJPlatformExtension
import com.rescript.plugin.ParsingTestHelper
import com.rescript.plugin.RescriptParsingTestExtension
import com.rescript.plugin.lang.psi.RescriptElementTypes
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

/**
 * Tests for [RescriptJsxTagHighlightHandler] range computation.
 *
 * The pure range logic is exercised through the real lexer+parser via
 * [RescriptParsingTestExtension]; the caret-driven factory path (which side the
 * caret is on, and nested same-name resolution) is verified end-to-end with a
 * heavy [CodeInsightTestFixture].
 *
 * @see RescriptJsxTagHighlightHandler
 * @see RescriptHighlightUsagesHandlerFactory
 */
@ExtendWith(RescriptParsingTestExtension::class)
class RescriptJsxTagHighlightHandlerTest {
    private lateinit var parsingHelper: ParsingTestHelper

    private fun firstJsxElement(
        code: String,
        type: com.intellij.psi.tree.IElementType = RescriptElementTypes.JSX_ELEMENT,
    ): PsiElement {
        val file = parsingHelper.parseCode(code)
        return parsingHelper.findElements(file, type).first().psi
    }

    @Test
    fun testPairedElementYieldsTwoRanges() {
        val element = firstJsxElement("let x = <div></div>")
        val ranges = RescriptJsxTagHighlightHandler.computeHighlightRanges(element)
        assertEquals(2, ranges.size, "Expected the open and close tag-name ranges")
        assertEquals("div", element.containingFile.text.substring(ranges[0].startOffset, ranges[0].endOffset))
        assertEquals("div", element.containingFile.text.substring(ranges[1].startOffset, ranges[1].endOffset))
    }

    @Test
    fun testUnterminatedElementYieldsSingleRange() {
        val element = firstJsxElement("let x = <div>text")
        val ranges = RescriptJsxTagHighlightHandler.computeHighlightRanges(element)
        assertEquals(1, ranges.size, "Expected only the open tag-name range when the close tag is absent")
    }

    @Test
    fun testFragmentYieldsNoRanges() {
        val element = firstJsxElement("let x = <> </>", RescriptElementTypes.JSX_FRAGMENT)
        val ranges = RescriptJsxTagHighlightHandler.computeHighlightRanges(element)
        assertEquals(0, ranges.size, "A fragment has no tag names to highlight")
    }

    @Test
    fun testNestedSameNameUsesOuterPair() {
        // The outer element must pair its own open/close names, ignoring the inner element.
        val file = parsingHelper.parseCode("let x = <div><div></div></div>")
        val outer = parsingHelper.findElements(file, RescriptElementTypes.JSX_ELEMENT).first().psi
        val ranges = RescriptJsxTagHighlightHandler.computeHighlightRanges(outer)
        assertEquals(2, ranges.size)
        val text = file.text
        assertEquals(text.indexOf("div"), ranges[0].startOffset, "Open range must be the first (outer) name")
        assertEquals(text.lastIndexOf("div"), ranges[1].startOffset, "Close range must be the last (outer) name")
    }

    /**
     * End-to-end caret tests through the full IDE platform: the factory must return
     * the JSX handler regardless of which side of the pair the caret sits on, and
     * resolve the correct enclosing element for nested same-name tags.
     */
    @ExtendWith(IntelliJPlatformExtension::class)
    class FactoryCaretTest {
        private lateinit var myFixture: CodeInsightTestFixture

        private fun rangesAtCaret(source: String): List<com.intellij.openapi.util.TextRange> {
            myFixture.configureByText("Jsx.res", source)
            val handler =
                RescriptHighlightUsagesHandlerFactory()
                    .createHighlightUsagesHandler(myFixture.editor, myFixture.file)
            assertNotNull(handler, "Expected a highlight handler for a caret on a JSX tag name")
            assertInstanceOf(RescriptJsxTagHighlightHandler::class.java, handler)
            val target = handler!!.targets.first()
            return RescriptJsxTagHighlightHandler.computeHighlightRanges(target)
        }

        @Test
        fun testCaretOnOpenTagHighlightsBoth() {
            assertEquals(2, rangesAtCaret("let x = <di<caret>v></div>").size)
        }

        @Test
        fun testCaretOnCloseTagHighlightsBoth() {
            assertEquals(2, rangesAtCaret("let x = <div></di<caret>v>").size)
        }

        @Test
        fun testCaretOnOuterNestedResolvesOuterPair() {
            val ranges = rangesAtCaret("let x = <di<caret>v><div></div></div>")
            val text = myFixture.file.text
            assertEquals(2, ranges.size)
            assertEquals(text.indexOf("div"), ranges[0].startOffset)
            assertEquals(text.lastIndexOf("div"), ranges[1].startOffset)
        }
    }
}
