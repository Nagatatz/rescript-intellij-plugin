package com.rescript.plugin.test

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Unit tests for [RescriptTestCallDetector.detect].
 *
 * Covers the three recognized framework functions, nested calls, the skip of
 * interpolated/backtick template names, rejection of non-call identifier usages,
 * and that the reported offsets and ranges line up with the source.
 */
class RescriptTestCallDetectorTest {
    @Test
    fun `detects describe with string name`() {
        val src = """describe("my suite", () => ())"""
        val calls = RescriptTestCallDetector.detect(src)
        assertEquals(1, calls.size)
        assertEquals("describe", calls[0].functionName)
        assertEquals("my suite", calls[0].testName)
    }

    @Test
    fun `detects it and test`() {
        val src =
            """
            it("does a thing", () => ())
            test("another thing", () => ())
            """.trimIndent()
        val calls = RescriptTestCallDetector.detect(src)
        assertEquals(listOf("it", "test"), calls.map { it.functionName })
        assertEquals(listOf("does a thing", "another thing"), calls.map { it.testName })
    }

    @Test
    fun `detects nested it inside describe independently`() {
        val src =
            """
            describe("outer", () => {
              it("inner", () => ())
            })
            """.trimIndent()
        val calls = RescriptTestCallDetector.detect(src)
        assertEquals(2, calls.size)
        assertEquals("describe", calls[0].functionName)
        assertEquals("outer", calls[0].testName)
        assertEquals("it", calls[1].functionName)
        assertEquals("inner", calls[1].testName)
    }

    @Test
    fun `skips interpolated template literal name`() {
        val src = """it(`value is ${'$'}{x}`, () => ())"""
        val calls = RescriptTestCallDetector.detect(src)
        assertTrue(calls.isEmpty(), "template-literal names should be skipped")
    }

    @Test
    fun `ignores identifier not used as a call`() {
        val src = "let it = 5"
        val calls = RescriptTestCallDetector.detect(src)
        assertTrue(calls.isEmpty())
    }

    @Test
    fun `ignores call without string first argument`() {
        val src = "describe(suiteName, () => ())"
        val calls = RescriptTestCallDetector.detect(src)
        assertTrue(calls.isEmpty())
    }

    @Test
    fun `callOffset points at the function name`() {
        val src = """  it("x", () => ())"""
        val calls = RescriptTestCallDetector.detect(src)
        assertEquals(1, calls.size)
        assertEquals(src.indexOf("it"), calls[0].callOffset)
    }

    @Test
    fun `nameArgRange covers the quoted string`() {
        val src = """it("abc", () => ())"""
        val calls = RescriptTestCallDetector.detect(src)
        val range = calls[0].nameArgRange
        assertEquals("\"abc\"", src.substring(range.startOffset, range.endOffset))
    }

    @Test
    fun `handles empty string name`() {
        val src = """it("", () => ())"""
        val calls = RescriptTestCallDetector.detect(src)
        assertEquals(1, calls.size)
        assertEquals("", calls[0].testName)
    }

    @Test
    fun `detects multiple top-level calls`() {
        val src =
            """
            describe("a", () => ())
            describe("b", () => ())
            """.trimIndent()
        val calls = RescriptTestCallDetector.detect(src)
        assertEquals(listOf("a", "b"), calls.map { it.testName })
    }
}
