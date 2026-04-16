package com.rescript.plugin.diagram

import com.intellij.openapi.project.Project
import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import com.rescript.plugin.IntelliJPlatformExtension
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

/**
 * Smoke tests for [RescriptDependencyDiagramProvider.buildDiagram].
 *
 * The pure dependency-extraction logic is exercised by
 * [RescriptDependencyDiagramModelTest]. Here we cover the project-wide
 * traversal entry point: it must construct a non-null model and return an
 * empty model when no ReScript files are present.
 *
 * Note: the populated case (FileTypeIndex finding files added via
 * `addFileToProject`) is intentionally not asserted because the
 * `LightProjectDescriptor.EMPTY_PROJECT_DESCRIPTOR` used by
 * [IntelliJPlatformExtension] has no content roots, so files added via the
 * temp-dir fixture are not indexed. Verifying that path requires a custom
 * project descriptor with a content root, which is out of scope for this
 * unit-test backfill.
 */
@ExtendWith(IntelliJPlatformExtension::class)
class RescriptDependencyDiagramProviderTest {
    @Suppress("unused")
    private lateinit var myFixture: CodeInsightTestFixture
    private lateinit var project: Project

    @Test
    fun testBuildDiagramReturnsNonNullModel() {
        val model = RescriptDependencyDiagramProvider.buildDiagram(project)
        assertNotNull(model)
    }

    @Test
    fun testBuildDiagramReturnsEmptyForProjectWithoutRescriptFiles() {
        val model = RescriptDependencyDiagramProvider.buildDiagram(project)
        assertEquals(0, model.moduleCount())
        assertEquals(0, model.edgeCount())
    }
}
