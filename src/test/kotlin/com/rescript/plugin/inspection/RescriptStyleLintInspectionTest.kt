package com.rescript.plugin.inspection

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RescriptStyleLintInspectionTest {
    @Test
    fun `simplifyRedundantBoolean simplifies if true else false`() {
        val result =
            RescriptStyleLintInspection.simplifyRedundantBoolean(
                "if isReady { true } else { false }",
            )
        assertEquals("isReady", result)
    }

    @Test
    fun `simplifyRedundantBoolean returns null for non-matching`() {
        val result =
            RescriptStyleLintInspection.simplifyRedundantBoolean(
                "if isReady { doSomething() } else { doOther() }",
            )
        assertNull(result)
    }

    @Test
    fun `replaceBelt converts Belt_Array to Array`() {
        val result = RescriptStyleLintInspection.replaceBelt("Belt.Array.map")
        assertEquals("Array.map", result)
    }

    @Test
    fun `replaceBelt converts Belt_List to List`() {
        val result = RescriptStyleLintInspection.replaceBelt("Belt.List.filter")
        assertEquals("List.filter", result)
    }

    @Test
    fun `replaceBelt converts Belt_Option`() {
        val result = RescriptStyleLintInspection.replaceBelt("Belt.Option.getExn")
        assertEquals("Option.getExn", result)
    }

    @Test
    fun `replaceBelt returns null for non-Belt`() {
        val result = RescriptStyleLintInspection.replaceBelt("Array.map")
        assertNull(result)
    }

    @Test
    fun `convertBooleanSwitch converts to if expression`() {
        val result =
            RescriptStyleLintInspection.convertBooleanSwitch(
                "switch isActive { | true => show() | false => hide() }",
            )
        assertEquals("if isActive { show() } else { hide() }", result)
    }

    @Test
    fun `convertBooleanSwitch returns null for non-boolean switch`() {
        val result =
            RescriptStyleLintInspection.convertBooleanSwitch(
                "switch color { | Red => 1 | Blue => 2 }",
            )
        assertNull(result)
    }

    @Test
    fun `inspection can be instantiated`() {
        assertNotNull(RescriptStyleLintInspection())
    }

    @Test
    fun `inspection is a LocalInspectionTool`() {
        assertTrue(RescriptStyleLintInspection() is com.intellij.codeInspection.LocalInspectionTool)
    }

    @Test
    fun `REDUNDANT_BOOL_PATTERN matches correctly`() {
        assertTrue(
            RescriptStyleLintInspection.REDUNDANT_BOOL_PATTERN.containsMatchIn(
                "if x { true } else { false }",
            ),
        )
    }

    @Test
    fun `BELT_USAGE_PATTERN matches Belt modules`() {
        assertTrue(RescriptStyleLintInspection.BELT_USAGE_PATTERN.containsMatchIn("Belt.Array.map"))
        assertTrue(RescriptStyleLintInspection.BELT_USAGE_PATTERN.containsMatchIn("Belt.Result.getExn"))
        assertTrue(RescriptStyleLintInspection.BELT_USAGE_PATTERN.containsMatchIn("Belt.Map.set"))
    }

    @Test
    fun `BOOLEAN_SWITCH_PATTERN matches correctly`() {
        assertTrue(
            RescriptStyleLintInspection.BOOLEAN_SWITCH_PATTERN.containsMatchIn(
                "switch x { | true => a | false => b }",
            ),
        )
    }
}
