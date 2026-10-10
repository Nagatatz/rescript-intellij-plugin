package com.rescript.plugin.inspection

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class RescriptMutabilityInspectionTest {
    @Test
    fun `inspection remains registered under the existing short name`() {
        val inspection = RescriptMutabilityInspection()
        assertEquals("RescriptMutability", inspection.shortName)
        val subject: Any = inspection
        assertTrue(subject is com.intellij.codeInspection.LocalInspectionTool)
    }
}
