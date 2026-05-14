package com.rescript.plugin.interop

import com.intellij.testFramework.LightVirtualFile
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test

/**
 * Shape tests for the Risk Map's domain model: [InteropKind], [RiskLevel]
 * and [InteropEntry]. These guard against accidental enum reordering /
 * constant renames and the data class plumbing that the panel relies on.
 */
class RescriptInteropModelTest {
    @Test
    fun `InteropKind has the expected five constants in declared order`() {
        assertEquals(
            listOf("RAW", "EXTERNAL", "OBJ_MAGIC", "BS_ATTR", "UNKNOWN"),
            InteropKind.entries.map { it.name },
        )
    }

    @Test
    fun `RiskLevel is ordered HIGH MEDIUM LOW`() {
        assertEquals(
            listOf("HIGH", "MEDIUM", "LOW"),
            RiskLevel.entries.map { it.name },
        )
    }

    @Test
    fun `InteropEntry retains every property verbatim`() {
        val file = LightVirtualFile("Foo.res")
        val entry =
            InteropEntry(
                file = file,
                offset = 42,
                lineNumber = 7,
                previewLine = "let x = %raw(\"1\")",
                kind = InteropKind.RAW,
                risk = RiskLevel.HIGH,
            )
        assertEquals(file, entry.file)
        assertEquals(42, entry.offset)
        assertEquals(7, entry.lineNumber)
        assertEquals("let x = %raw(\"1\")", entry.previewLine)
        assertEquals(InteropKind.RAW, entry.kind)
        assertEquals(RiskLevel.HIGH, entry.risk)
    }

    @Test
    fun `InteropEntry equality is by all fields`() {
        val file = LightVirtualFile("Foo.res")
        val a = InteropEntry(file, 0, 1, "x", InteropKind.RAW, RiskLevel.HIGH)
        val b = InteropEntry(file, 0, 1, "x", InteropKind.RAW, RiskLevel.HIGH)
        val differentRisk = a.copy(risk = RiskLevel.LOW)
        assertEquals(a, b)
        assertNotEquals(a, differentRisk)
    }

    @Test
    fun `COLOR_BY_RISK has an entry for every RiskLevel`() {
        for (level in RiskLevel.entries) {
            assertEquals(true, COLOR_BY_RISK.containsKey(level), "missing colour for $level")
        }
    }

    @Test
    fun `COLOR_BY_RISK assigns distinct light-theme colours per level`() {
        // Compare by RGB of the active-theme delegate; JBColor lacks
        // its own equals override so plain `!=` checks are unreliable.
        val rgbs = RiskLevel.entries.map { COLOR_BY_RISK.getValue(it).rgb }
        assertEquals(RiskLevel.entries.size, rgbs.distinct().size)
    }
}
