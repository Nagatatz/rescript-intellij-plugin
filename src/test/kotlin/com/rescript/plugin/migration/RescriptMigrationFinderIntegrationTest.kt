package com.rescript.plugin.migration

import com.intellij.openapi.project.Project
import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import com.rescript.plugin.IntelliJPlatformExtension
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

/**
 * Smoke tests for [RescriptMigrationFinder.findCandidates] running
 * against a real [Project].
 *
 * Note: the `IntelliJPlatformExtension` uses
 * `LightProjectDescriptor.EMPTY_PROJECT_DESCRIPTOR`, which has no
 * content roots, so files added via `addFileToProject` are not
 * indexed by `FilenameIndex`. As a result the populated case is
 * already covered by [RescriptMigrationFinderTest] (pure helper);
 * here we only verify that the IDE-side entry point is wired up
 * correctly and gracefully returns an empty list.
 */
@ExtendWith(IntelliJPlatformExtension::class)
class RescriptMigrationFinderIntegrationTest {
    @Suppress("unused")
    private lateinit var myFixture: CodeInsightTestFixture
    private lateinit var project: Project

    @Test
    fun `findCandidates does not throw against a fresh project`() {
        val candidates = RescriptMigrationFinder.findCandidates(project)
        assertNotNull(candidates)
    }

    @Test
    fun `empty project yields no Reason candidates`() {
        val candidates = RescriptMigrationFinder.findCandidates(project)
        assertTrue(
            candidates.none { it.file.name.endsWith(".re") || it.file.name.endsWith(".rei") },
            "empty fixture project should not surface any Reason files",
        )
    }
}
