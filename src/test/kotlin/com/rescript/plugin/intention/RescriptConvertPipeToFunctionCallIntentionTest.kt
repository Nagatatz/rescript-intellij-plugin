package com.rescript.plugin.intention

import com.rescript.plugin.intention.RescriptConvertPipeToFunctionCallIntention.Companion
import com.rescript.plugin.intention.RescriptConvertPipeToFunctionCallIntention.PipeExpression
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class RescriptConvertPipeToFunctionCallIntentionTest {
    @Test
    fun testText() {
        val intention = RescriptConvertPipeToFunctionCallIntention()
        assertEquals("Convert pipe to function call", intention.text)
    }

    @Test
    fun testFamilyName() {
        val intention = RescriptConvertPipeToFunctionCallIntention()
        assertEquals("Convert pipe to function call", intention.familyName)
    }

    @Test
    fun testStartInWriteAction() {
        val intention = RescriptConvertPipeToFunctionCallIntention()
        assertEquals(true, intention.startInWriteAction())
    }

    // -- findPipeExpression tests --

    @Test
    fun testFindPipeExpressionSimple() {
        val text = "arr->Array.map(f)"
        val result = Companion.findPipeExpression(text, 5)
        assertNotNull(result)
        assertEquals("arr", result!!.lhs)
        assertEquals("Array.map", result.funcName)
        assertEquals("f", result.args)
    }

    @Test
    fun testFindPipeExpressionNoArgs() {
        val text = "arr->Array.length"
        val result = Companion.findPipeExpression(text, 5)
        assertNotNull(result)
        assertEquals("arr", result!!.lhs)
        assertEquals("Array.length", result.funcName)
        assertNull(result.args)
    }

    @Test
    fun testFindPipeExpressionWithMultipleArgs() {
        val text = "arr->Array.reduce(acc, f)"
        val result = Companion.findPipeExpression(text, 5)
        assertNotNull(result)
        assertEquals("arr", result!!.lhs)
        assertEquals("Array.reduce", result.funcName)
        assertEquals("acc, f", result.args)
    }

    @Test
    fun testFindPipeExpressionWithSpaces() {
        val text = "arr -> Array.map(f)"
        val result = Companion.findPipeExpression(text, 5)
        assertNotNull(result)
        assertEquals("arr", result!!.lhs)
        assertEquals("Array.map", result.funcName)
    }

    @Test
    fun testFindPipeExpressionReturnsNullForNoPipe() {
        val text = "Array.map(arr, f)"
        val result = Companion.findPipeExpression(text, 5)
        assertNull(result)
    }

    // -- convertPipeToFunctionCall tests --

    @Test
    fun testConvertWithArgs() {
        val expr = PipeExpression("arr", "Array.map", "f", 0, 17)
        val result = Companion.convertPipeToFunctionCall(expr)
        assertEquals("Array.map(arr, f)", result)
    }

    @Test
    fun testConvertWithoutArgs() {
        val expr = PipeExpression("arr", "Array.length", null, 0, 16)
        val result = Companion.convertPipeToFunctionCall(expr)
        assertEquals("Array.length(arr)", result)
    }

    @Test
    fun testConvertWithMultipleArgs() {
        val expr = PipeExpression("arr", "Array.reduce", "acc, f", 0, 25)
        val result = Companion.convertPipeToFunctionCall(expr)
        assertEquals("Array.reduce(arr, acc, f)", result)
    }

    @Test
    fun testConvertWithEmptyArgs() {
        val expr = PipeExpression("arr", "Array.map", "", 0, 15)
        val result = Companion.convertPipeToFunctionCall(expr)
        assertEquals("Array.map(arr)", result)
    }
}
