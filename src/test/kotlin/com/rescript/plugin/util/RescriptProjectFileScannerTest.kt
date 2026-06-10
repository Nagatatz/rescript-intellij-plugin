package com.rescript.plugin.util

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.testFramework.LightVirtualFile
import com.rescript.plugin.IntelliJPlatformExtension
import com.rescript.plugin.RescriptFileType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.io.IOException

/**
 * Tests for the shared project file scan loop.
 *
 * The visiting loop is driven with in-memory [LightVirtualFile]s because
 * the light fixture has no content roots, so `scanFiles`' FileTypeIndex
 * lookup cannot be populated; the index-backed entry point is smoke-tested
 * against an empty project, mirroring `RescriptInteropScannerIntegrationTest`.
 */
@ExtendWith(IntelliJPlatformExtension::class)
class RescriptProjectFileScannerTest {
    private lateinit var project: Project

    private fun resFile(
        name: String,
        text: String,
    ): VirtualFile = LightVirtualFile(name, RescriptFileType, text)

    @Test
    fun `visitFiles visits every readable file in order and reports no truncation`() {
        val files = listOf(resFile("A.res", "let a = 1"), resFile("B.res", "let b = 2"))
        val seen = mutableListOf<String>()
        val truncated =
            RescriptProjectFileScanner.visitFiles(files, shouldContinue = { true }) { file, text ->
                seen.add("${file.name}:$text")
            }
        assertFalse(truncated)
        assertEquals(listOf("A.res:let a = 1", "B.res:let b = 2"), seen)
    }

    @Test
    fun `visitFiles stops and reports truncation when the cap predicate trips`() {
        val files = listOf(resFile("A.res", "1"), resFile("B.res", "2"), resFile("C.res", "3"))
        val seen = mutableListOf<String>()
        val truncated =
            RescriptProjectFileScanner.visitFiles(files, shouldContinue = { seen.size < 2 }) { file, _ ->
                seen.add(file.name)
            }
        assertTrue(truncated)
        assertEquals(listOf("A.res", "B.res"), seen)
    }

    @Test
    fun `visitFiles skips files whose contents cannot be read`() {
        val broken =
            object : LightVirtualFile("Broken.res", RescriptFileType, "x") {
                override fun contentsToByteArray(): ByteArray = throw IOException("unreadable")
            }
        val files = listOf(resFile("A.res", "ok"), broken, resFile("B.res", "ok"))
        val seen = mutableListOf<String>()
        val truncated =
            RescriptProjectFileScanner.visitFiles(files, shouldContinue = { true }) { file, _ ->
                seen.add(file.name)
            }
        assertFalse(truncated)
        assertEquals(listOf("A.res", "B.res"), seen)
    }

    @Test
    fun `scanFiles returns no truncation against an empty project`() {
        var calls = 0
        val truncated =
            RescriptProjectFileScanner.scanFiles(
                project = project,
                fileTypes = listOf(RescriptFileType),
                shouldContinue = { true },
            ) { _, _ -> calls++ }
        assertFalse(truncated)
        assertEquals(0, calls)
    }
}
