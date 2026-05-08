package com.rescript.plugin.interop

import com.intellij.testFramework.LightVirtualFile
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Tests for the pure `collectEntriesFromText` helper inside
 * [RescriptInteropScanner]. The full FileTypeIndex-backed scan is
 * exercised at runtime and falls under the IDE-fixture exemption
 * documented in tasklist.md.
 */
class RescriptInteropScannerTest {
    private fun fakeFile(name: String = "Sample.res"): LightVirtualFile = LightVirtualFile(name)

    @Test
    fun `collects matching lines and ignores unrelated ones`() {
        val text =
            """
            let x = 1
            let s = %raw("'hi'")
            external alert: string => unit = "alert"
            let y = Obj.magic(payload)
            let z = 2
            """.trimIndent()
        val entries = RescriptInteropScanner.collectEntriesFromText(fakeFile(), text, maxEntries = 100)
        assertEquals(3, entries.size)
        assertEquals(
            setOf(InteropKind.RAW, InteropKind.EXTERNAL, InteropKind.OBJ_MAGIC),
            entries.map { it.kind }.toSet(),
        )
    }

    @Test
    fun `respects maxEntries`() {
        val text =
            """
            let a = Obj.magic(1)
            let b = Obj.magic(2)
            let c = Obj.magic(3)
            """.trimIndent()
        val entries = RescriptInteropScanner.collectEntriesFromText(fakeFile(), text, maxEntries = 2)
        assertEquals(2, entries.size)
    }

    @Test
    fun `line numbers are 1-based`() {
        val text =
            """
            let a = 1
            let b = Obj.magic(payload)
            let c = 3
            """.trimIndent()
        val entries = RescriptInteropScanner.collectEntriesFromText(fakeFile(), text, maxEntries = 10)
        assertEquals(1, entries.size)
        assertEquals(2, entries.first().lineNumber)
    }

    @Test
    fun `previewLine is trimmed`() {
        val text = "    let n = Obj.magic(x)\n"
        val entries = RescriptInteropScanner.collectEntriesFromText(fakeFile(), text, maxEntries = 10)
        assertEquals("let n = Obj.magic(x)", entries.first().previewLine)
    }

    @Test
    fun `empty text yields empty result`() {
        val entries = RescriptInteropScanner.collectEntriesFromText(fakeFile(), "", maxEntries = 10)
        assertTrue(entries.isEmpty())
    }
}
