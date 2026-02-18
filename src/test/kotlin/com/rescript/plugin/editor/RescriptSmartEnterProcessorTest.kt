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
}
