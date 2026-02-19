package com.rescript.plugin.editor

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RescriptSmartEnterProcessorTest {
    private val processor = RescriptSmartEnterProcessor()

    @Test
    fun testProcessorCanBeInstantiated() {
        assertNotNull(processor)
    }

    @Test
    fun testAnalyzeLineDetectsUnclosedBrace() {
        val result = processor.analyzeLine("let x = {")
        assertEquals('{', result.unclosedBracket)
    }

    @Test
    fun testAnalyzeLineDetectsUnclosedParen() {
        val result = processor.analyzeLine("let x = foo(")
        assertEquals('(', result.unclosedBracket)
    }

    @Test
    fun testAnalyzeLineDetectsUnclosedBracket() {
        val result = processor.analyzeLine("let arr = [")
        assertEquals('[', result.unclosedBracket)
    }

    @Test
    fun testAnalyzeLineNoUnclosedBracketWhenBalanced() {
        val result = processor.analyzeLine("let x = {a: 1}")
        assertNull(result.unclosedBracket)
    }

    @Test
    fun testAnalyzeLineBalancedParens() {
        val result = processor.analyzeLine("let x = foo(1, 2)")
        assertNull(result.unclosedBracket)
    }

    @Test
    fun testAnalyzeLineBalancedBrackets() {
        val result = processor.analyzeLine("let arr = [1, 2, 3]")
        assertNull(result.unclosedBracket)
    }

    @Test
    fun testAnalyzeLineDetectsSwitchWithoutBrace() {
        val result = processor.analyzeLine("switch x")
        assertTrue(result.hasSwitchWithoutBrace)
    }

    @Test
    fun testAnalyzeLineSwitchWithBraceIsNotDetected() {
        val result = processor.analyzeLine("switch x {")
        assertFalse(result.hasSwitchWithoutBrace)
    }

    @Test
    fun testAnalyzeLineDetectsPipeWithoutArrow() {
        val result = processor.analyzeLine("| Some(x)")
        assertTrue(result.hasPipeWithoutArrow)
    }

    @Test
    fun testAnalyzeLinePipeWithArrowIsNotDetected() {
        val result = processor.analyzeLine("| Some(x) => x")
        assertFalse(result.hasPipeWithoutArrow)
    }

    @Test
    fun testAnalyzeLinePipeInsideSwitchNotDetected() {
        val result = processor.analyzeLine("switch x | foo")
        assertFalse(result.hasPipeWithoutArrow)
    }

    @Test
    fun testAnalyzeLinePlainText() {
        val result = processor.analyzeLine("let x = 42")
        assertNull(result.unclosedBracket)
        assertFalse(result.hasSwitchWithoutBrace)
        assertFalse(result.hasPipeWithoutArrow)
    }

    @Test
    fun testAnalyzeLineEmptyString() {
        val result = processor.analyzeLine("")
        assertNull(result.unclosedBracket)
        assertFalse(result.hasSwitchWithoutBrace)
        assertFalse(result.hasPipeWithoutArrow)
    }

    @Test
    fun testAnalyzeLineBracePrioritizedOverParen() {
        val result = processor.analyzeLine("foo({bar(")
        assertEquals('{', result.unclosedBracket)
    }

    @Test
    fun testAnalyzeLineNestedBrackets() {
        val result = processor.analyzeLine("let x = [[1, 2]")
        assertEquals('[', result.unclosedBracket)
    }

    @Test
    fun testAnalyzeLineMultipleUnclosedParens() {
        val result = processor.analyzeLine("foo(bar(")
        assertEquals('(', result.unclosedBracket)
    }

    @Test
    fun testAnalyzeLineSwitchWithCompleteExpression() {
        val result = processor.analyzeLine("switch list {")
        assertFalse(result.hasSwitchWithoutBrace)
    }

    // -- Additional edge cases --

    @Test
    fun testAnalyzeLineWhitespaceOnlyLine() {
        val result = processor.analyzeLine("   \t  ")
        assertNull(result.unclosedBracket)
        assertFalse(result.hasSwitchWithoutBrace)
        assertFalse(result.hasPipeWithoutArrow)
    }

    @Test
    fun testAnalyzeLineCommentOnlyLine() {
        val result = processor.analyzeLine("// this is a comment")
        assertNull(result.unclosedBracket)
        assertFalse(result.hasSwitchWithoutBrace)
        assertFalse(result.hasPipeWithoutArrow)
    }

    @Test
    fun testAnalyzeLineBlockCommentOnly() {
        val result = processor.analyzeLine("/* block comment */")
        assertNull(result.unclosedBracket)
        assertFalse(result.hasSwitchWithoutBrace)
        assertFalse(result.hasPipeWithoutArrow)
    }

    @Test
    fun testAnalyzeLineTemplateLiteralBacktick() {
        val result = processor.analyzeLine("let x = `hello")
        // Template literal with backtick — no unclosed bracket
        assertNull(result.unclosedBracket)
        assertFalse(result.hasSwitchWithoutBrace)
    }

    @Test
    fun testAnalyzeLineSwitchInsideComment() {
        // "switch" inside a comment should still be lexed but the brace check may still detect it;
        // this documents current behavior
        val result = processor.analyzeLine("// switch x")
        // The comment is a single token, so switch won't be detected
        assertFalse(result.hasSwitchWithoutBrace)
    }

    @Test
    fun testAnalyzeLinePipeArrowWithTrailingContent() {
        val result = processor.analyzeLine("| Some(x) => process(x)")
        assertFalse(result.hasPipeWithoutArrow)
    }

    @Test
    fun testAnalyzeLineMultiplePipesNoArrow() {
        val result = processor.analyzeLine("| Some(x) | None")
        assertTrue(result.hasPipeWithoutArrow)
    }

    @Test
    fun testAnalyzeLineOnlyBraces() {
        val result = processor.analyzeLine("{}")
        assertNull(result.unclosedBracket)
    }

    @Test
    fun testAnalyzeLineIndentedSwitch() {
        val result = processor.analyzeLine("    switch value")
        assertTrue(result.hasSwitchWithoutBrace)
    }

    @Test
    fun testAnalyzeLineIndentedPipe() {
        val result = processor.analyzeLine("    | None")
        assertTrue(result.hasPipeWithoutArrow)
    }

    @Test
    fun testLineAnalysisDefaultValues() {
        val analysis = RescriptSmartEnterProcessor.LineAnalysis()
        assertFalse(analysis.hasSwitchWithoutBrace)
        assertFalse(analysis.hasPipeWithoutArrow)
        assertNull(analysis.unclosedBracket)
    }

    @Test
    fun testAnalyzeLineExtraClosingBraceNoUnclosed() {
        // More closing than opening should not report unclosed
        val result = processor.analyzeLine("let x = }}")
        assertNull(result.unclosedBracket)
    }

    @Test
    fun testAnalyzeLineExtraClosingParenNoUnclosed() {
        val result = processor.analyzeLine("foo))")
        assertNull(result.unclosedBracket)
    }

    @Test
    fun testAnalyzeLineExtraClosingBracketNoUnclosed() {
        val result = processor.analyzeLine("arr]]")
        assertNull(result.unclosedBracket)
    }

    @Test
    fun testAnalyzeLineSwitchWithPipeAndArrow() {
        // switch keyword on same line as pipe and arrow
        val result = processor.analyzeLine("switch x { | A => 1 }")
        assertFalse(result.hasSwitchWithoutBrace)
        // pipe is present but so is arrow, and switch is present so no hasPipeWithoutArrow
        assertFalse(result.hasPipeWithoutArrow)
    }

    @Test
    fun testAnalyzeLineArrowWithoutPipe() {
        val result = processor.analyzeLine("let f = (x) =>")
        assertFalse(result.hasPipeWithoutArrow)
        assertNull(result.unclosedBracket)
    }

    @Test
    fun testAnalyzeLineOnlyClosingBrace() {
        val result = processor.analyzeLine("}")
        assertNull(result.unclosedBracket)
    }

    @Test
    fun testAnalyzeLineMixedBracketsAndParens() {
        val result = processor.analyzeLine("let x = {a: [1, 2]")
        assertEquals('{', result.unclosedBracket)
    }

    @Test
    fun testAnalyzeLineBalancedMixedBrackets() {
        val result = processor.analyzeLine("let x = {a: [1, (2)]}")
        assertNull(result.unclosedBracket)
    }

    @Test
    fun testLineAnalysisDataClassEquality() {
        val a = RescriptSmartEnterProcessor.LineAnalysis(hasSwitchWithoutBrace = true)
        val b = RescriptSmartEnterProcessor.LineAnalysis(hasSwitchWithoutBrace = true)
        assertEquals(a, b)
    }

    @Test
    fun testLineAnalysisDataClassCopy() {
        val original = RescriptSmartEnterProcessor.LineAnalysis(unclosedBracket = '{')
        val copy = original.copy(hasPipeWithoutArrow = true)
        assertEquals('{', copy.unclosedBracket)
        assertTrue(copy.hasPipeWithoutArrow)
    }
}
