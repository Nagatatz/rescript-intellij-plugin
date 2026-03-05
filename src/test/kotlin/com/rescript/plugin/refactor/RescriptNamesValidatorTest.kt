package com.rescript.plugin.refactor

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class RescriptNamesValidatorTest {
    private val validator = RescriptNamesValidator()

    // --- isIdentifier ---

    @Test
    fun `lident - simple lowercase`() {
        assertTrue(validator.isIdentifier("foo", null))
    }

    @Test
    fun `lident - underscore prefix`() {
        assertTrue(validator.isIdentifier("_bar", null))
    }

    @Test
    fun `lident - camelCase`() {
        assertTrue(validator.isIdentifier("camelCase", null))
    }

    @Test
    fun `lident - with prime`() {
        assertTrue(validator.isIdentifier("x'", null))
    }

    @Test
    fun `lident - single underscore`() {
        assertTrue(validator.isIdentifier("_", null))
    }

    @Test
    fun `lident - with digits`() {
        assertTrue(validator.isIdentifier("foo123", null))
    }

    @Test
    fun `uident - simple uppercase`() {
        assertTrue(validator.isIdentifier("Foo", null))
    }

    @Test
    fun `uident - module name`() {
        assertTrue(validator.isIdentifier("Belt", null))
    }

    @Test
    fun `uident - MyModule`() {
        assertTrue(validator.isIdentifier("MyModule", null))
    }

    @Test
    fun `invalid - empty string`() {
        assertFalse(validator.isIdentifier("", null))
    }

    @Test
    fun `invalid - starts with digit`() {
        assertFalse(validator.isIdentifier("123", null))
    }

    @Test
    fun `invalid - contains space`() {
        assertFalse(validator.isIdentifier("foo bar", null))
    }

    @Test
    fun `invalid - starts with dash`() {
        assertFalse(validator.isIdentifier("-invalid", null))
    }

    @Test
    fun `invalid - starts with at`() {
        assertFalse(validator.isIdentifier("@attr", null))
    }

    @Test
    fun `invalid - dot notation`() {
        assertFalse(validator.isIdentifier("Belt.Array", null))
    }

    // --- isKeyword ---

    @Test
    fun `keyword - let`() {
        assertTrue(validator.isKeyword("let", null))
    }

    @Test
    fun `keyword - type`() {
        assertTrue(validator.isKeyword("type", null))
    }

    @Test
    fun `keyword - module`() {
        assertTrue(validator.isKeyword("module", null))
    }

    @Test
    fun `keyword - switch`() {
        assertTrue(validator.isKeyword("switch", null))
    }

    @Test
    fun `keyword - if`() {
        assertTrue(validator.isKeyword("if", null))
    }

    @Test
    fun `keyword - open`() {
        assertTrue(validator.isKeyword("open", null))
    }

    @Test
    fun `keyword - external`() {
        assertTrue(validator.isKeyword("external", null))
    }

    @Test
    fun `keyword - rec`() {
        assertTrue(validator.isKeyword("rec", null))
    }

    @Test
    fun `keyword - async`() {
        assertTrue(validator.isKeyword("async", null))
    }

    @Test
    fun `keyword - await`() {
        assertTrue(validator.isKeyword("await", null))
    }

    @Test
    fun `not keyword - foo`() {
        assertFalse(validator.isKeyword("foo", null))
    }

    @Test
    fun `not keyword - Belt`() {
        assertFalse(validator.isKeyword("Belt", null))
    }

    @Test
    fun `not keyword - notAKeyword`() {
        assertFalse(validator.isKeyword("notAKeyword", null))
    }

    @Test
    fun `not keyword - empty string`() {
        assertFalse(validator.isKeyword("", null))
    }
}
