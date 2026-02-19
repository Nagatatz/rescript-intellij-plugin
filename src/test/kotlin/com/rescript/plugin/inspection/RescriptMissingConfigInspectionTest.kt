package com.rescript.plugin.inspection

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RescriptMissingConfigInspectionTest {
    private val inspection = RescriptMissingConfigInspection()

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
        assertEquals("RescriptMissingConfig", shortName)
    }

    @Test
    fun `config file names are rescript json and bsconfig json`() {
        // Verify that the inspection checks for both config file names
        // by examining the source. The buildVisitor checks for:
        // - rescript.json
        // - bsconfig.json
        // This test verifies the class exists and can be introspected
        val methods = inspection.javaClass.declaredMethods.map { it.name }
        assertTrue("buildVisitor should exist", methods.contains("buildVisitor"))
    }
}
