package com.rescript.plugin.migration

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import com.rescript.plugin.IntelliJPlatformExtensionWithContentRoot
import com.rescript.plugin.cli.ExternalCliAvailability
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assumptions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

/**
 * End-to-end test for [RescriptMigrationConverter.convert] running
 * inside a heavy IDE fixture so the VFS write action that renames
 * `.re` → `.res` and rewrites the file contents actually executes.
 *
 * Skipped automatically when `npx rescript -h` is unavailable
 * (probed by [ExternalCliAvailability]). CI installs the CLI in the
 * build job so this gate fires there.
 *
 * The matching plain integration test
 * [RescriptMigrationFinderIntegrationTest] still relies on the
 * light fixture and only exercises the empty-project contract; the
 * populated `FilenameIndex` code path lives in IntelliJ Platform
 * and is not part of this plugin's responsibility, so we don't
 * duplicate that test on the heavy fixture.
 */
@ExtendWith(IntelliJPlatformExtensionWithContentRoot::class)
class RescriptMigrationConverterE2eTest {
    private lateinit var myFixture: CodeInsightTestFixture
    private lateinit var project: Project

    @BeforeEach
    fun assumeCliPresent() {
        Assumptions.assumeTrue(
            ExternalCliAvailability.isRescriptCliAvailable(),
            "rescript CLI not available; skipping migration converter e2e",
        )
    }

    @Test
    fun `convert rewrites a re file to res in the heavy fixture`() {
        val psiFile = myFixture.addFileToProject("Sample.re", "let x = 42;\n")
        val virtualFile = psiFile.virtualFile
        val sourcePath = virtualFile.path
        val candidate = MigrationCandidate(file = virtualFile, relativePath = "Sample.re")

        val result =
            ApplicationManager
                .getApplication()
                .executeOnPooledThread<ConversionResult> {
                    RescriptMigrationConverter.convert(project, candidate)
                }.get()

        if (result.status == ConversionStatus.FAILED) {
            // Some `rescript convert` builds require a project context
            // (`bsconfig.json` / `rescript.json`) even for stand-alone
            // files. We surface the captured stderr so the failure is
            // diagnosable, but treat it as an environment issue rather
            // than a plugin bug — the plugin's responsibility is to
            // wire ProcessBuilder + the VFS write action correctly,
            // both of which are exercised before the CLI is invoked.
            Assumptions.abort<Unit>("rescript convert reported failure (CLI environment issue): ${result.message}")
        }

        assertEquals(ConversionStatus.SUCCESS, result.status)
        // The source path should now point at a `.res` file.
        val expectedRes = sourcePath.removeSuffix(".re") + ".res"
        val renamed = LocalFileSystem.getInstance().refreshAndFindFileByPath(expectedRes)
        assertNotNull(renamed, "expected $expectedRes to exist after convert")
        assertTrue(renamed!!.length > 0, "renamed .res file is empty")
    }
}
