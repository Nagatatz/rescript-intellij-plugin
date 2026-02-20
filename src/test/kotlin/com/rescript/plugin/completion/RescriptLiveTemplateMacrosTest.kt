package com.rescript.plugin.completion

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RescriptLiveTemplateMacrosTest {
    @Test
    fun `RescriptModuleNameMacro name is rescriptModuleName`() {
        val macro = RescriptModuleNameMacro()
        assertEquals("rescriptModuleName", macro.name)
    }

    @Test
    fun `RescriptModuleNameMacro presentable name is rescriptModuleName()`() {
        val macro = RescriptModuleNameMacro()
        assertEquals("rescriptModuleName()", macro.presentableName)
    }

    @Test
    fun `RescriptComponentNameMacro name is rescriptComponentName`() {
        val macro = RescriptComponentNameMacro()
        assertEquals("rescriptComponentName", macro.name)
    }

    @Test
    fun `RescriptComponentNameMacro presentable name is rescriptComponentName()`() {
        val macro = RescriptComponentNameMacro()
        assertEquals("rescriptComponentName()", macro.presentableName)
    }

    @Test
    fun `RescriptModuleNameMacro is a Macro`() {
        val macro = RescriptModuleNameMacro()
        assertTrue(macro is com.intellij.codeInsight.template.Macro)
    }

    @Test
    fun `RescriptComponentNameMacro is a Macro`() {
        val macro = RescriptComponentNameMacro()
        assertTrue(macro is com.intellij.codeInsight.template.Macro)
    }

    @Test
    fun `resolveModuleName returns null for null context`() {
        val result = RescriptModuleNameMacro.resolveModuleName(null)
        assertNull(result)
    }

    @Test
    fun `calculateResult returns null for null context`() {
        val macro = RescriptModuleNameMacro()
        val result = macro.calculateResult(emptyArray(), null)
        assertNull(result)
    }

    @Test
    fun `RescriptComponentNameMacro calculateResult returns null for null context`() {
        val macro = RescriptComponentNameMacro()
        val result = macro.calculateResult(emptyArray(), null)
        assertNull(result)
    }
}
