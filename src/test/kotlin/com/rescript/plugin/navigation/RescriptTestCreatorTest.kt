package com.rescript.plugin.navigation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RescriptTestCreatorTest {
    // ── testFileNames ────────────────────────────────────────────

    @Test
    fun `testFileNames generates underscore and dot variants for res`() {
        val names = RescriptTestCreator.testFileNames("Foo.res")
        assertEquals(listOf("Foo_test.res", "Foo.test.res"), names)
    }

    @Test
    fun `testFileNames generates variants for resi`() {
        val names = RescriptTestCreator.testFileNames("Foo.resi")
        assertEquals(listOf("Foo_test.resi", "Foo.test.resi"), names)
    }

    @Test
    fun `testFileNames handles multi-word module names`() {
        val names = RescriptTestCreator.testFileNames("MyComponent.res")
        assertEquals(listOf("MyComponent_test.res", "MyComponent.test.res"), names)
    }

    // ── sourceFileName ───────────────────────────────────────────

    @Test
    fun `sourceFileName extracts from underscore test`() {
        assertEquals("Foo.res", RescriptTestCreator.sourceFileName("Foo_test.res"))
    }

    @Test
    fun `sourceFileName extracts from dot test`() {
        assertEquals("Foo.res", RescriptTestCreator.sourceFileName("Foo.test.res"))
    }

    @Test
    fun `sourceFileName extracts from resi test`() {
        assertEquals("Foo.resi", RescriptTestCreator.sourceFileName("Foo_test.resi"))
    }

    @Test
    fun `sourceFileName returns null for non-test file`() {
        assertNull(RescriptTestCreator.sourceFileName("Foo.res"))
    }

    @Test
    fun `sourceFileName returns null for FooTest res`() {
        assertNull(RescriptTestCreator.sourceFileName("FooTest.res"))
    }

    // ── isTestFile ───────────────────────────────────────────────

    @Test
    fun `isTestFile recognizes underscore test`() {
        assertTrue(RescriptTestCreator.isTestFile("Foo_test.res"))
    }

    @Test
    fun `isTestFile recognizes dot test`() {
        assertTrue(RescriptTestCreator.isTestFile("Foo.test.res"))
    }

    @Test
    fun `isTestFile recognizes resi test files`() {
        assertTrue(RescriptTestCreator.isTestFile("Foo_test.resi"))
        assertTrue(RescriptTestCreator.isTestFile("Foo.test.resi"))
    }

    @Test
    fun `isTestFile rejects regular source files`() {
        assertFalse(RescriptTestCreator.isTestFile("Foo.res"))
        assertFalse(RescriptTestCreator.isTestFile("Bar.resi"))
    }

    @Test
    fun `isTestFile rejects non-rescript files`() {
        assertFalse(RescriptTestCreator.isTestFile("Foo_test.js"))
        assertFalse(RescriptTestCreator.isTestFile("Foo.test.ts"))
    }

    @Test
    fun `isTestFile rejects FooTest without separator`() {
        assertFalse(RescriptTestCreator.isTestFile("FooTest.res"))
    }

    // ── generateTestContent ──────────────────────────────────────

    @Test
    fun `generateTestContent with jest framework`() {
        val creator = RescriptTestCreator()
        val content = creator.generateTestContent("MyModule", com.rescript.plugin.test.TestFramework.JEST)
        assertTrue(content.contains("open Jest"))
        assertTrue(content.contains("describe(\"MyModule\""))
        assertTrue(content.contains("test(\"should work\""))
    }

    @Test
    fun `generateTestContent with vitest framework`() {
        val creator = RescriptTestCreator()
        val content = creator.generateTestContent("MyModule", com.rescript.plugin.test.TestFramework.VITEST)
        assertTrue(content.contains("open Vitest"))
        assertTrue(content.contains("describe(\"MyModule\""))
    }

    @Test
    fun `generateTestContent with no framework`() {
        val creator = RescriptTestCreator()
        val content = creator.generateTestContent("MyModule", null)
        assertTrue(content.contains("// Test for MyModule"))
        assertTrue(content.contains("Js.log"))
    }
}
