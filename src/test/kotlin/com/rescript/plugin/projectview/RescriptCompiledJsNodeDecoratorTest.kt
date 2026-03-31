package com.rescript.plugin.projectview

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.awt.Color

class RescriptCompiledJsNodeDecoratorTest {
    @Test
    fun `matches res js file names`() {
        assertTrue(RescriptCompiledJsNodeDecorator.isCompiledJsFile("Example.res.js"))
        assertTrue(RescriptCompiledJsNodeDecorator.isCompiledJsFile("MyModule.res.js"))
        assertTrue(RescriptCompiledJsNodeDecorator.isCompiledJsFile("Index.res.js"))
    }

    @Test
    fun `matches res mjs file names`() {
        assertTrue(RescriptCompiledJsNodeDecorator.isCompiledJsFile("Example.res.mjs"))
        assertTrue(RescriptCompiledJsNodeDecorator.isCompiledJsFile("MyModule.res.mjs"))
    }

    @Test
    fun `matches res cjs file names`() {
        assertTrue(RescriptCompiledJsNodeDecorator.isCompiledJsFile("Example.res.cjs"))
        assertTrue(RescriptCompiledJsNodeDecorator.isCompiledJsFile("MyModule.res.cjs"))
    }

    @Test
    fun `matches resi js file names`() {
        assertTrue(RescriptCompiledJsNodeDecorator.isCompiledJsFile("Example.resi.js"))
        assertTrue(RescriptCompiledJsNodeDecorator.isCompiledJsFile("MyModule.resi.js"))
    }

    @Test
    fun `matches resi mjs and cjs file names`() {
        assertTrue(RescriptCompiledJsNodeDecorator.isCompiledJsFile("Example.resi.mjs"))
        assertTrue(RescriptCompiledJsNodeDecorator.isCompiledJsFile("Example.resi.cjs"))
    }

    @Test
    fun `matches bs js file names`() {
        assertTrue(RescriptCompiledJsNodeDecorator.isCompiledJsFile("Example.bs.js"))
        assertTrue(RescriptCompiledJsNodeDecorator.isCompiledJsFile("MyModule.bs.js"))
    }

    @Test
    fun `matches bs mjs and cjs file names`() {
        assertTrue(RescriptCompiledJsNodeDecorator.isCompiledJsFile("Example.bs.mjs"))
        assertTrue(RescriptCompiledJsNodeDecorator.isCompiledJsFile("Example.bs.cjs"))
    }

    @Test
    fun `does not match res files`() {
        assertFalse(RescriptCompiledJsNodeDecorator.isCompiledJsFile("Example.res"))
    }

    @Test
    fun `does not match resi files`() {
        assertFalse(RescriptCompiledJsNodeDecorator.isCompiledJsFile("Example.resi"))
    }

    @Test
    fun `does not match regular js files`() {
        assertFalse(RescriptCompiledJsNodeDecorator.isCompiledJsFile("index.js"))
        assertFalse(RescriptCompiledJsNodeDecorator.isCompiledJsFile("app.js"))
    }

    @Test
    fun `does not match other file types`() {
        assertFalse(RescriptCompiledJsNodeDecorator.isCompiledJsFile("README.md"))
        assertFalse(RescriptCompiledJsNodeDecorator.isCompiledJsFile("package.json"))
        assertFalse(RescriptCompiledJsNodeDecorator.isCompiledJsFile("style.css"))
    }

    @Test
    fun `extractBaseName returns base name for res js`() {
        assertEquals("Demo", RescriptCompiledJsNodeDecorator.extractBaseName("Demo.res.js"))
        assertEquals("MyModule", RescriptCompiledJsNodeDecorator.extractBaseName("MyModule.res.mjs"))
        assertEquals("Index", RescriptCompiledJsNodeDecorator.extractBaseName("Index.res.cjs"))
    }

    @Test
    fun `extractBaseName returns base name for bs js`() {
        assertEquals("Demo", RescriptCompiledJsNodeDecorator.extractBaseName("Demo.bs.js"))
        assertEquals("MyModule", RescriptCompiledJsNodeDecorator.extractBaseName("MyModule.bs.mjs"))
        assertEquals("Index", RescriptCompiledJsNodeDecorator.extractBaseName("Index.bs.cjs"))
    }

    @Test
    fun `extractBaseName returns base name for resi js`() {
        assertEquals("Demo", RescriptCompiledJsNodeDecorator.extractBaseName("Demo.resi.js"))
        assertEquals("MyModule", RescriptCompiledJsNodeDecorator.extractBaseName("MyModule.resi.mjs"))
    }

    @Test
    fun `extractBaseName returns null for non-compiled files`() {
        assertNull(RescriptCompiledJsNodeDecorator.extractBaseName("Demo.res"))
        assertNull(RescriptCompiledJsNodeDecorator.extractBaseName("index.js"))
        assertNull(RescriptCompiledJsNodeDecorator.extractBaseName("README.md"))
    }

    @Test
    fun `gray color is not null`() {
        assertNotNull(RescriptCompiledJsNodeDecorator.GRAY_COLOR)
    }

    @Test
    fun `gray color has correct light theme value`() {
        val expectedLight = Color(153, 153, 153)
        assertEquals(expectedLight.rgb, RescriptCompiledJsNodeDecorator.GRAY_COLOR.rgb)
    }
}
