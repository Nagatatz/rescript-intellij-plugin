package com.rescript.plugin.migration

import com.intellij.testFramework.LightVirtualFile
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * Tests the pure path-filtering helper of [RescriptMigrationFinder].
 * The full [com.intellij.psi.search.FilenameIndex] integration falls
 * under the IDE-fixture exemption documented in tasklist.md.
 */
class RescriptMigrationFinderTest {
    private fun fakeFile(path: String): LightVirtualFile {
        val name = path.substringAfterLast('/')
        return object : LightVirtualFile(name) {
            override fun getPath(): String = path
        }
    }

    @Test
    fun `relative paths are computed against project base`() {
        val files =
            sequenceOf(
                fakeFile("/work/proj/src/Main.re"),
                fakeFile("/work/proj/src/Util.rei"),
            )
        val candidates = RescriptMigrationFinder.toCandidates("/work/proj", files)
        assertEquals(listOf("src/Main.re", "src/Util.rei"), candidates.map { it.relativePath })
    }

    @Test
    fun `files outside the project base are dropped`() {
        val files =
            sequenceOf(
                fakeFile("/work/other/Main.re"),
                fakeFile("/work/proj/src/Util.re"),
            )
        val candidates = RescriptMigrationFinder.toCandidates("/work/proj", files)
        assertEquals(listOf("src/Util.re"), candidates.map { it.relativePath })
    }

    @Test
    fun `trailing slash on project base is tolerated`() {
        val files = sequenceOf(fakeFile("/work/proj/src/Main.re"))
        val candidates = RescriptMigrationFinder.toCandidates("/work/proj/", files)
        assertEquals("src/Main.re", candidates.single().relativePath)
    }

    @Test
    fun `output is sorted alphabetically by relative path`() {
        val files =
            sequenceOf(
                fakeFile("/work/proj/src/Z.re"),
                fakeFile("/work/proj/src/A.re"),
                fakeFile("/work/proj/src/M.rei"),
            )
        val candidates = RescriptMigrationFinder.toCandidates("/work/proj", files)
        assertEquals(listOf("src/A.re", "src/M.rei", "src/Z.re"), candidates.map { it.relativePath })
    }

    @Test
    fun `empty input yields empty list`() {
        assertEquals(
            emptyList<MigrationCandidate>(),
            RescriptMigrationFinder.toCandidates("/work/proj", emptySequence()),
        )
    }
}
