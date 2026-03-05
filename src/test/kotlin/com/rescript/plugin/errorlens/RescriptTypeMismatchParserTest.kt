package com.rescript.plugin.errorlens

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class RescriptTypeMismatchParserTest {
    @Test
    fun testParseThisHasTypeSomewhereWanted() {
        val message = "This has type: int  Somewhere wanted: string"
        val result = RescriptTypeMismatchParser.parse(message)
        assertNotNull(result)
        assertEquals("int", result!!.actual)
        assertEquals("string", result.expected)
    }

    @Test
    fun testParseButSomewhereWanted() {
        val message = "This has type: float  But somewhere wanted: int"
        val result = RescriptTypeMismatchParser.parse(message)
        assertNotNull(result)
        assertEquals("float", result!!.actual)
        assertEquals("int", result.expected)
    }

    @Test
    fun testParseExpectedButGot() {
        val message = "expected int but got string"
        val result = RescriptTypeMismatchParser.parse(message)
        assertNotNull(result)
        assertEquals("int", result!!.expected)
        assertEquals("string", result.actual)
    }

    @Test
    fun testParseNotCompatible() {
        val message = "Type int is not compatible with type string"
        val result = RescriptTypeMismatchParser.parse(message)
        assertNotNull(result)
        assertEquals("int", result!!.actual)
        assertEquals("string", result.expected)
    }

    @Test
    fun testParseExpectedToHaveType() {
        val message = "This has type: float  It's expected to have type: int"
        val result = RescriptTypeMismatchParser.parse(message)
        assertNotNull(result)
        assertEquals("float", result!!.actual)
        assertEquals("int", result.expected)
    }

    @Test
    fun testParseWithNewlines() {
        val message = "This has type: int\n  Somewhere wanted: string"
        val result = RescriptTypeMismatchParser.parse(message)
        assertNotNull(result)
        assertEquals("int", result!!.actual)
        assertEquals("string", result.expected)
    }

    @Test
    fun testParseWithComplexTypes() {
        val message = "This has type: array<int>  Somewhere wanted: option<string>"
        val result = RescriptTypeMismatchParser.parse(message)
        assertNotNull(result)
        assertEquals("array<int>", result!!.actual)
        assertEquals("option<string>", result.expected)
    }

    @Test
    fun testParseReturnsNullForUnrelatedMessage() {
        val message = "Unbound value foo"
        val result = RescriptTypeMismatchParser.parse(message)
        assertNull(result)
    }

    @Test
    fun testParseReturnsNullForEmptyMessage() {
        val result = RescriptTypeMismatchParser.parse("")
        assertNull(result)
    }

    @Test
    fun testFormatMismatch() {
        val mismatch = RescriptTypeMismatchParser.TypeMismatch("int", "string")
        val result = RescriptTypeMismatchParser.formatMismatch(mismatch)
        assertEquals("Expected: int | Actual: string", result)
    }
}
