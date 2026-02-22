package com.rescript.plugin.run

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RescriptRunUtilsTest {
    @Test
    fun testWhitespaceRegexSplitsOnSpaces() {
        val result = "arg1 arg2 arg3".split(RescriptRunUtils.WHITESPACE_REGEX)
        assertEquals(listOf("arg1", "arg2", "arg3"), result)
    }

    @Test
    fun testWhitespaceRegexSplitsOnTabs() {
        val result = "arg1\targ2".split(RescriptRunUtils.WHITESPACE_REGEX)
        assertEquals(listOf("arg1", "arg2"), result)
    }

    @Test
    fun testWhitespaceRegexSplitsOnMultipleSpaces() {
        val result = "arg1   arg2".split(RescriptRunUtils.WHITESPACE_REGEX)
        assertEquals(listOf("arg1", "arg2"), result)
    }

    @Test
    fun testWhitespaceRegexSplitsOnMixedWhitespace() {
        val result = "arg1 \t arg2".split(RescriptRunUtils.WHITESPACE_REGEX)
        assertEquals(listOf("arg1", "arg2"), result)
    }

    @Test
    fun testWhitespaceRegexSingleArg() {
        val result = "arg1".split(RescriptRunUtils.WHITESPACE_REGEX)
        assertEquals(listOf("arg1"), result)
    }

    @Test
    fun testWhitespaceRegexEmptyString() {
        val result = "".split(RescriptRunUtils.WHITESPACE_REGEX)
        assertTrue(result.size == 1 && result[0] == "")
    }
}
