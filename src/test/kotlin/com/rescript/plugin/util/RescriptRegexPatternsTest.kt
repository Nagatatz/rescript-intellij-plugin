package com.rescript.plugin.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RescriptRegexPatternsTest {
    // ── LIDENT ──────────────────────────────────────────────────────

    @Test
    fun `LIDENT matches simple lowercase identifier`() {
        assertTrue(RescriptRegexPatterns.LIDENT.matches("foo"))
    }

    @Test
    fun `LIDENT matches underscore-prefixed identifier`() {
        assertTrue(RescriptRegexPatterns.LIDENT.matches("_bar"))
    }

    @Test
    fun `LIDENT matches identifier with prime`() {
        assertTrue(RescriptRegexPatterns.LIDENT.matches("x'"))
    }

    @Test
    fun `LIDENT matches identifier with digits`() {
        assertTrue(RescriptRegexPatterns.LIDENT.matches("foo123"))
    }

    @Test
    fun `LIDENT rejects uppercase start`() {
        assertFalse(RescriptRegexPatterns.LIDENT.matches("Foo"))
    }

    @Test
    fun `LIDENT rejects digit start`() {
        assertFalse(RescriptRegexPatterns.LIDENT.matches("1foo"))
    }

    @Test
    fun `LIDENT rejects empty string`() {
        assertFalse(RescriptRegexPatterns.LIDENT.matches(""))
    }

    // ── UIDENT ──────────────────────────────────────────────────────

    @Test
    fun `UIDENT matches simple uppercase identifier`() {
        assertTrue(RescriptRegexPatterns.UIDENT.matches("Foo"))
    }

    @Test
    fun `UIDENT matches single uppercase letter`() {
        assertTrue(RescriptRegexPatterns.UIDENT.matches("A"))
    }

    @Test
    fun `UIDENT matches identifier with digits and prime`() {
        assertTrue(RescriptRegexPatterns.UIDENT.matches("Module123'"))
    }

    @Test
    fun `UIDENT rejects lowercase start`() {
        assertFalse(RescriptRegexPatterns.UIDENT.matches("foo"))
    }

    @Test
    fun `UIDENT rejects underscore start`() {
        assertFalse(RescriptRegexPatterns.UIDENT.matches("_Foo"))
    }

    @Test
    fun `UIDENT rejects empty string`() {
        assertFalse(RescriptRegexPatterns.UIDENT.matches(""))
    }

    // ── WHITESPACE ──────────────────────────────────────────────────

    @Test
    fun `WHITESPACE splits by spaces`() {
        val result = "a b c".split(RescriptRegexPatterns.WHITESPACE)
        assertTrue(result == listOf("a", "b", "c"))
    }

    @Test
    fun `WHITESPACE splits by tabs and newlines`() {
        val result = "a\tb\nc".split(RescriptRegexPatterns.WHITESPACE)
        assertTrue(result == listOf("a", "b", "c"))
    }

    @Test
    fun `WHITESPACE splits by multiple spaces`() {
        val result = "a   b".split(RescriptRegexPatterns.WHITESPACE)
        assertTrue(result == listOf("a", "b"))
    }

    // ── OPEN_STATEMENT ───────────────────────────────────────────

    @Test
    fun `OPEN_STATEMENT matches simple open`() {
        val match = RescriptRegexPatterns.OPEN_STATEMENT.find("open Belt")
        assertEquals("open Belt", match?.value)
    }

    @Test
    fun `OPEN_STATEMENT matches dotted module`() {
        val match = RescriptRegexPatterns.OPEN_STATEMENT.find("open Belt.Array")
        assertEquals("open Belt.Array", match?.value)
    }

    @Test
    fun `OPEN_STATEMENT matches in multiline text`() {
        val text = "let x = 1\nopen Belt\nlet y = 2"
        val match = RescriptRegexPatterns.OPEN_STATEMENT.find(text)
        assertEquals("open Belt", match?.value)
    }

    @Test
    fun `OPEN_STATEMENT does not match mid-line open`() {
        val match = RescriptRegexPatterns.OPEN_STATEMENT.find("let x = open Belt")
        assertNull(match)
    }

    // ── OPEN_MODULE_CAPTURE ──────────────────────────────────────

    @Test
    fun `OPEN_MODULE_CAPTURE captures module name`() {
        val match = RescriptRegexPatterns.OPEN_MODULE_CAPTURE.find("open Belt")
        assertEquals("Belt", match?.groupValues?.get(1))
    }

    @Test
    fun `OPEN_MODULE_CAPTURE captures dotted module name`() {
        val match = RescriptRegexPatterns.OPEN_MODULE_CAPTURE.find("open Belt.Array")
        assertEquals("Belt.Array", match?.groupValues?.get(1))
    }

    @Test
    fun `OPEN_MODULE_CAPTURE finds all modules in multiline text`() {
        val text = "open Belt\nopen Js.Array2"
        val modules =
            RescriptRegexPatterns.OPEN_MODULE_CAPTURE
                .findAll(text)
                .map { it.groupValues[1] }
                .toList()
        assertEquals(listOf("Belt", "Js.Array2"), modules)
    }

    @Test
    fun `OPEN_MODULE_CAPTURE does not match mid-line open`() {
        assertNull(RescriptRegexPatterns.OPEN_MODULE_CAPTURE.find("let x = open Belt"))
    }

    // ── OPEN_MODULE_STRICT ───────────────────────────────────────

    @Test
    fun `OPEN_MODULE_STRICT captures module with leading whitespace`() {
        val match = RescriptRegexPatterns.OPEN_MODULE_STRICT.find("  open Belt")
        assertEquals("Belt", match?.groupValues?.get(1))
    }

    @Test
    fun `OPEN_MODULE_STRICT captures dotted module`() {
        val match = RescriptRegexPatterns.OPEN_MODULE_STRICT.find("open Belt.Array")
        assertEquals("Belt.Array", match?.groupValues?.get(1))
    }

    @Test
    fun `OPEN_MODULE_STRICT rejects lowercase module`() {
        assertNull(RescriptRegexPatterns.OPEN_MODULE_STRICT.find("open belt"))
    }

    @Test
    fun `OPEN_MODULE_STRICT rejects trailing content`() {
        assertNull(RescriptRegexPatterns.OPEN_MODULE_STRICT.find("open Belt // comment"))
    }

    @Test
    fun `OPEN_MODULE_STRICT finds all in multiline text`() {
        val text = "  open Belt\nlet x = 1\nopen Js.Array2"
        val modules =
            RescriptRegexPatterns.OPEN_MODULE_STRICT
                .findAll(text)
                .map { it.groupValues[1] }
                .toList()
        assertEquals(listOf("Belt", "Js.Array2"), modules)
    }

    // ── OPEN_LINE_TEST ───────────────────────────────────────────

    @Test
    fun `OPEN_LINE_TEST matches open line`() {
        assertTrue(RescriptRegexPatterns.OPEN_LINE_TEST.containsMatchIn("open Belt"))
    }

    @Test
    fun `OPEN_LINE_TEST matches open line with leading whitespace`() {
        assertTrue(RescriptRegexPatterns.OPEN_LINE_TEST.containsMatchIn("  open Belt"))
    }

    @Test
    fun `OPEN_LINE_TEST matches in multiline text`() {
        assertTrue(RescriptRegexPatterns.OPEN_LINE_TEST.containsMatchIn("let x = 1\nopen Belt"))
    }

    @Test
    fun `OPEN_LINE_TEST does not match mid-line open`() {
        assertFalse(RescriptRegexPatterns.OPEN_LINE_TEST.containsMatchIn("let open = 1"))
    }

    @Test
    fun `OPEN_LINE_TEST does not match openBar`() {
        assertFalse(RescriptRegexPatterns.OPEN_LINE_TEST.containsMatchIn("openBar"))
    }

    // ── INCLUDE_MODULE_CAPTURE ─────────────────────────────────

    @Test
    fun `INCLUDE_MODULE_CAPTURE captures module name`() {
        val match = RescriptRegexPatterns.INCLUDE_MODULE_CAPTURE.find("include Belt")
        assertEquals("Belt", match?.groupValues?.get(1))
    }

    @Test
    fun `INCLUDE_MODULE_CAPTURE captures dotted module name`() {
        val match = RescriptRegexPatterns.INCLUDE_MODULE_CAPTURE.find("include Belt.Array")
        assertEquals("Belt.Array", match?.groupValues?.get(1))
    }

    @Test
    fun `INCLUDE_MODULE_CAPTURE rejects lowercase module`() {
        assertNull(RescriptRegexPatterns.INCLUDE_MODULE_CAPTURE.find("include belt"))
    }

    @Test
    fun `INCLUDE_MODULE_CAPTURE does not match mid-line include`() {
        assertNull(RescriptRegexPatterns.INCLUDE_MODULE_CAPTURE.find("let x = include Belt"))
    }

    // ── CONSTRUCTOR_WITH_PAYLOAD ───────────────────────────────

    @Test
    fun `CONSTRUCTOR_WITH_PAYLOAD matches simple constructor`() {
        val match = RescriptRegexPatterns.CONSTRUCTOR_WITH_PAYLOAD.find("Some")
        assertEquals("Some", match?.groupValues?.get(1))
        assertEquals("", match?.groupValues?.get(2))
    }

    @Test
    fun `CONSTRUCTOR_WITH_PAYLOAD matches constructor with payload`() {
        val match = RescriptRegexPatterns.CONSTRUCTOR_WITH_PAYLOAD.find("Ok(string)")
        assertEquals("Ok", match?.groupValues?.get(1))
        assertEquals("string", match?.groupValues?.get(2))
    }

    @Test
    fun `CONSTRUCTOR_WITH_PAYLOAD matches with trailing whitespace`() {
        val match = RescriptRegexPatterns.CONSTRUCTOR_WITH_PAYLOAD.find("None  ")
        assertEquals("None", match?.groupValues?.get(1))
    }

    @Test
    fun `CONSTRUCTOR_WITH_PAYLOAD rejects lowercase`() {
        assertNull(RescriptRegexPatterns.CONSTRUCTOR_WITH_PAYLOAD.find("some"))
    }

    // ── LABELED_PARAM_NAME ─────────────────────────────────────

    @Test
    fun `LABELED_PARAM_NAME matches simple labeled param`() {
        val match = RescriptRegexPatterns.LABELED_PARAM_NAME.find("~name")
        assertEquals("name", match?.groupValues?.get(1))
    }

    @Test
    fun `LABELED_PARAM_NAME matches labeled param with type`() {
        val match = RescriptRegexPatterns.LABELED_PARAM_NAME.find("~age: int")
        assertEquals("age", match?.groupValues?.get(1))
    }

    @Test
    fun `LABELED_PARAM_NAME finds all in signature`() {
        val params =
            RescriptRegexPatterns.LABELED_PARAM_NAME
                .findAll("(~name: string, ~age: int) => person")
                .map { it.groupValues[1] }
                .toList()
        assertEquals(listOf("name", "age"), params)
    }

    @Test
    fun `LABELED_PARAM_NAME does not match without tilde`() {
        assertNull(RescriptRegexPatterns.LABELED_PARAM_NAME.find("name: string"))
    }
}
