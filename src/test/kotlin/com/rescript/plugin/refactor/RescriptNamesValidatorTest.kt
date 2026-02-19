package com.rescript.plugin.refactor

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RescriptNamesValidatorTest {
    private val validator = RescriptNamesValidator()

    // ── isIdentifier: lident ──

    @Test
    fun `lowercase identifier is valid`() {
        assertTrue(validator.isIdentifier("myVar", null))
    }

    @Test
    fun `underscore-prefixed identifier is valid`() {
        assertTrue(validator.isIdentifier("_unused", null))
    }

    @Test
    fun `identifier with prime is valid`() {
        assertTrue(validator.isIdentifier("x'", null))
    }

    @Test
    fun `identifier with digits is valid`() {
        assertTrue(validator.isIdentifier("item2", null))
    }

    @Test
    fun `single underscore is valid`() {
        assertTrue(validator.isIdentifier("_", null))
    }

    @Test
    fun `single lowercase letter is valid`() {
        assertTrue(validator.isIdentifier("x", null))
    }

    // ── isIdentifier: uident ──

    @Test
    fun `uppercase identifier is valid`() {
        assertTrue(validator.isIdentifier("MyModule", null))
    }

    @Test
    fun `single uppercase letter is valid`() {
        assertTrue(validator.isIdentifier("A", null))
    }

    @Test
    fun `uppercase with digits is valid`() {
        assertTrue(validator.isIdentifier("Option2", null))
    }

    // ── isIdentifier: invalid ──

    @Test
    fun `empty string is not identifier`() {
        assertFalse(validator.isIdentifier("", null))
    }

    @Test
    fun `digit-starting string is not identifier`() {
        assertFalse(validator.isIdentifier("123abc", null))
    }

    @Test
    fun `string with spaces is not identifier`() {
        assertFalse(validator.isIdentifier("my var", null))
    }

    @Test
    fun `string with hyphen is not identifier`() {
        assertFalse(validator.isIdentifier("my-var", null))
    }

    @Test
    fun `string with special chars is not identifier`() {
        assertFalse(validator.isIdentifier("foo@bar", null))
    }

    // ── isKeyword ──

    @Test
    fun `let is keyword`() {
        assertTrue(validator.isKeyword("let", null))
    }

    @Test
    fun `switch is keyword`() {
        assertTrue(validator.isKeyword("switch", null))
    }

    @Test
    fun `module is keyword`() {
        assertTrue(validator.isKeyword("module", null))
    }

    @Test
    fun `type is keyword`() {
        assertTrue(validator.isKeyword("type", null))
    }

    @Test
    fun `if is keyword`() {
        assertTrue(validator.isKeyword("if", null))
    }

    @Test
    fun `async is keyword`() {
        assertTrue(validator.isKeyword("async", null))
    }

    @Test
    fun `await is keyword`() {
        assertTrue(validator.isKeyword("await", null))
    }

    @Test
    fun `non-keyword is not keyword`() {
        assertFalse(validator.isKeyword("myFunction", null))
    }

    @Test
    fun `empty string is not keyword`() {
        assertFalse(validator.isKeyword("", null))
    }

    @Test
    fun `capitalized keyword is not keyword`() {
        assertFalse(validator.isKeyword("Let", null))
    }
}
