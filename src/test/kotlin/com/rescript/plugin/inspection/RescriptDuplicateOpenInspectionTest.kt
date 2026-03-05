package com.rescript.plugin.inspection

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class RescriptDuplicateOpenInspectionTest {
    private val inspection = RescriptDuplicateOpenInspection()

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
        val shortName = inspection.shortName
        assertEquals("RescriptDuplicateOpen", shortName)
    }

    @Test
    fun `RemoveDuplicateOpenQuickFix familyName`() {
        val quickFixClass =
            Class.forName("com.rescript.plugin.inspection.RescriptDuplicateOpenInspection\$RemoveDuplicateOpenQuickFix")
        val instance = quickFixClass.getDeclaredConstructor().apply { isAccessible = true }.newInstance()
        val familyName = quickFixClass.getMethod("getFamilyName").invoke(instance) as String
        assertEquals("Remove duplicate open", familyName)
    }
}
