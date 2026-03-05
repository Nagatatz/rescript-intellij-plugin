package com.rescript.plugin.intention

import com.rescript.plugin.intention.RescriptConvertFunctionCallToPipeIntention.Companion
import com.rescript.plugin.intention.RescriptConvertFunctionCallToPipeIntention.FunctionCall
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class RescriptConvertFunctionCallToPipeIntentionTest {
    @Test
    fun testText() {
        val intention = RescriptConvertFunctionCallToPipeIntention()
        assertEquals("Convert function call to pipe", intention.text)
    }

    @Test
    fun testFamilyName() {
        val intention = RescriptConvertFunctionCallToPipeIntention()
        assertEquals("Convert function call to pipe", intention.familyName)
    }

    @Test
    fun testStartInWriteAction() {
        val intention = RescriptConvertFunctionCallToPipeIntention()
        assertEquals(true, intention.startInWriteAction())
    }

    // -- findFunctionCall tests --

    @Test
    fun testFindFunctionCallSimple() {
        val text = "Array.map(arr, f)"
        val result = Companion.findFunctionCall(text, 5)
        assertNotNull(result)
        assertEquals("Array", result!!.modulePath)
        assertEquals("map", result.funcName)
        assertEquals("arr", result.firstArg)
        assertEquals(listOf("f"), result.remainingArgs)
    }

    @Test
    fun testFindFunctionCallSingleArg() {
        val text = "Array.length(arr)"
        val result = Companion.findFunctionCall(text, 5)
        assertNotNull(result)
        assertEquals("Array", result!!.modulePath)
        assertEquals("length", result.funcName)
        assertEquals("arr", result.firstArg)
        assertEquals(emptyList<String>(), result.remainingArgs)
    }

    @Test
    fun testFindFunctionCallMultipleArgs() {
        val text = "Array.reduce(arr, acc, f)"
        val result = Companion.findFunctionCall(text, 5)
        assertNotNull(result)
        assertEquals("Array", result!!.modulePath)
        assertEquals("reduce", result.funcName)
        assertEquals("arr", result.firstArg)
        assertEquals(listOf("acc", "f"), result.remainingArgs)
    }

    @Test
    fun testFindFunctionCallDeepModule() {
        val text = "Belt.Array.map(arr, f)"
        val result = Companion.findFunctionCall(text, 5)
        assertNotNull(result)
        assertEquals("Belt.Array", result!!.modulePath)
        assertEquals("map", result.funcName)
        assertEquals("arr", result.firstArg)
        assertEquals(listOf("f"), result.remainingArgs)
    }

    @Test
    fun testFindFunctionCallReturnsNullForNonQualified() {
        val text = "map(arr, f)"
        val result = Companion.findFunctionCall(text, 2)
        assertNull(result)
    }

    @Test
    fun testFindFunctionCallReturnsNullForNoArgs() {
        val text = "Array.length"
        val result = Companion.findFunctionCall(text, 5)
        assertNull(result)
    }

    // -- splitArgs tests --

    @Test
    fun testSplitArgsSimple() {
        val result = Companion.splitArgs("a, b, c")
        assertEquals(listOf("a", " b", " c"), result)
    }

    @Test
    fun testSplitArgsNestedParens() {
        val result = Companion.splitArgs("foo(a, b), c")
        assertEquals(listOf("foo(a, b)", " c"), result)
    }

    @Test
    fun testSplitArgsSingle() {
        val result = Companion.splitArgs("arr")
        assertEquals(listOf("arr"), result)
    }

    // -- convertFunctionCallToPipe tests --

    @Test
    fun testConvertWithRemainingArgs() {
        val call = FunctionCall("Array", "map", "arr", listOf("f"), 0, 17)
        val result = Companion.convertFunctionCallToPipe(call)
        assertEquals("arr->Array.map(f)", result)
    }

    @Test
    fun testConvertSingleArg() {
        val call = FunctionCall("Array", "length", "arr", emptyList(), 0, 17)
        val result = Companion.convertFunctionCallToPipe(call)
        assertEquals("arr->Array.length", result)
    }

    @Test
    fun testConvertWithMultipleRemainingArgs() {
        val call = FunctionCall("Array", "reduce", "arr", listOf("acc", "f"), 0, 25)
        val result = Companion.convertFunctionCallToPipe(call)
        assertEquals("arr->Array.reduce(acc, f)", result)
    }

    @Test
    fun testConvertDeepModule() {
        val call = FunctionCall("Belt.Array", "map", "arr", listOf("f"), 0, 22)
        val result = Companion.convertFunctionCallToPipe(call)
        assertEquals("arr->Belt.Array.map(f)", result)
    }
}
