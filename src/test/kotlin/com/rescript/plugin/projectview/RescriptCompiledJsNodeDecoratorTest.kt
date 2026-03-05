package com.rescript.plugin.projectview

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
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
    fun `matches resi js file names`() {
        assertTrue(RescriptCompiledJsNodeDecorator.isCompiledJsFile("Example.resi.js"))
        assertTrue(RescriptCompiledJsNodeDecorator.isCompiledJsFile("MyModule.resi.js"))
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
    fun `gray color is not null`() {
        assertNotNull(RescriptCompiledJsNodeDecorator.GRAY_COLOR)
    }

    @Test
    fun `gray color has correct light theme value`() {
        val expectedLight = Color(153, 153, 153)
        assertEquals(expectedLight.rgb, RescriptCompiledJsNodeDecorator.GRAY_COLOR.rgb)
    }
}
