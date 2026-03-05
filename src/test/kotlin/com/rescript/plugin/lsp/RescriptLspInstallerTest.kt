package com.rescript.plugin.lsp

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.File

class RescriptLspInstallerTest {
    // ── Command line construction ───────────────────────────────────
    // Note: Actual process execution is not tested here because OS process
    // invocation is difficult to mock in unit tests. Only command construction
    // is verified.

    @Test
    fun `buildCommandLine creates correct NPM command`() {
        val cmd = RescriptLspInstaller.buildCommandLine(PackageManager.NPM, "/my/project")
        assertEquals("npm", cmd.exePath)
        assertEquals(
            listOf("install", "--save-dev", "@rescript/language-server"),
            cmd.parametersList.list,
        )
        assertEquals(File("/my/project"), cmd.workDirectory)
    }

    @Test
    fun `buildCommandLine creates correct YARN command`() {
        val cmd = RescriptLspInstaller.buildCommandLine(PackageManager.YARN, "/my/project")
        assertEquals("yarn", cmd.exePath)
        assertEquals(
            listOf("add", "--dev", "@rescript/language-server"),
            cmd.parametersList.list,
        )
        assertEquals(File("/my/project"), cmd.workDirectory)
    }

    @Test
    fun `buildCommandLine creates correct PNPM command`() {
        val cmd = RescriptLspInstaller.buildCommandLine(PackageManager.PNPM, "/my/project")
        assertEquals("pnpm", cmd.exePath)
        assertEquals(
            listOf("add", "--save-dev", "@rescript/language-server"),
            cmd.parametersList.list,
        )
        assertEquals(File("/my/project"), cmd.workDirectory)
    }

    @Test
    fun `buildCommandLine sets correct working directory`() {
        val workDir = "/workspace/packages/my-app"
        val cmd = RescriptLspInstaller.buildCommandLine(PackageManager.NPM, workDir)
        assertEquals(File(workDir), cmd.workDirectory)
    }

    @Test
    fun `buildCommandLine command line string contains package name`() {
        val cmd = RescriptLspInstaller.buildCommandLine(PackageManager.NPM, "/tmp")
        assertTrue(cmd.commandLineString.contains("@rescript/language-server"))
    }
}
