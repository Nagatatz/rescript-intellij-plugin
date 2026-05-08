package com.rescript.plugin.interop

import com.intellij.openapi.project.Project
import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import com.rescript.plugin.IntelliJPlatformExtension
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

/**
 * Smoke tests for [RescriptInteropScanner.scan] running against a
 * real [Project].
 *
 * Note: the `IntelliJPlatformExtension` uses
 * `LightProjectDescriptor.EMPTY_PROJECT_DESCRIPTOR`, which has no
 * content roots, so files added via `addFileToProject` are not
 * indexed by `FileTypeIndex`. The classifier and the per-file
 * line walker are already exhaustively covered by
 * [RescriptInteropClassifierTest] and [RescriptInteropScannerTest];
 * here we only verify that the IDE-side entry point is wired up
 * correctly and gracefully returns an empty result.
 */
@ExtendWith(IntelliJPlatformExtension::class)
class RescriptInteropScannerIntegrationTest {
    @Suppress("unused")
    private lateinit var myFixture: CodeInsightTestFixture
    private lateinit var project: Project

    @Test
    fun `scan does not throw against a fresh project`() {
        val result = RescriptInteropScanner.scan(project)
        assertNotNull(result)
        assertNotNull(result.entries)
    }

    @Test
    fun `empty project yields no entries and no truncation`() {
        val result = RescriptInteropScanner.scan(project)
        org.junit.jupiter.api.Assertions
            .assertEquals(emptyList<InteropEntry>(), result.entries)
        org.junit.jupiter.api.Assertions
            .assertEquals(false, result.truncated)
    }
}
