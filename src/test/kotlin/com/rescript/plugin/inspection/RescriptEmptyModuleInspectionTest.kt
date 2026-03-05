package com.rescript.plugin.inspection

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class RescriptEmptyModuleInspectionTest {
    private val inspection = RescriptEmptyModuleInspection()

    @Test
    fun `inspection can be instantiated`() {
        assertNotNull(inspection)
    }

    @Test
    fun `inspection is a LocalInspectionTool`() {
        assertTrue(inspection is com.intellij.codeInspection.LocalInspectionTool)
    }

    @Test
    fun `getShortName returns class name without Inspection suffix`() {
        // IntelliJ convention: short name = class simple name
        val shortName = inspection.shortName
        assertEquals("RescriptEmptyModule", shortName)
    }

    @Test
    fun `RemoveEmptyModuleQuickFix familyName`() {
        // Access through reflection since it's private
        val quickFixClass =
            Class.forName("com.rescript.plugin.inspection.RescriptEmptyModuleInspection\$RemoveEmptyModuleQuickFix")
        val instance = quickFixClass.getDeclaredConstructor().apply { isAccessible = true }.newInstance()
        val familyName = quickFixClass.getMethod("getFamilyName").invoke(instance) as String
        assertEquals("Remove empty module", familyName)
    }

    @Test
    fun `DECLARATION_TYPES contains all expected element types`() {
        // Access DECLARATION_TYPES through reflection
        // In Kotlin, companion object private members are accessible on the enclosing class
        val field =
            RescriptEmptyModuleInspection::class.java.getDeclaredField("DECLARATION_TYPES")
        field.isAccessible = true
        @Suppress("UNCHECKED_CAST")
        val types = field.get(null) as Set<*>

        // Should contain all 7 declaration types
        assertEquals(7, types.size)
        val typeNames = types.map { it.toString() }.toSet()
        assertTrue(typeNames.contains("LET_DECLARATION"))
        assertTrue(typeNames.contains("TYPE_DECLARATION"))
        assertTrue(typeNames.contains("MODULE_DECLARATION"))
        assertTrue(typeNames.contains("EXTERNAL_DECLARATION"))
        assertTrue(typeNames.contains("OPEN_STATEMENT"))
        assertTrue(typeNames.contains("INCLUDE_STATEMENT"))
        assertTrue(typeNames.contains("EXCEPTION_DECLARATION"))
    }
}
