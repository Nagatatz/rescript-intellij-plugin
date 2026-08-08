package com.rescript.plugin.analysis

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Path

class RescriptReanalyzeServerServiceTest {
    @TempDir
    lateinit var tempDir: Path

    // --- ServerState enum tests ---

    @Test
    fun `ServerState has four values`() {
        val values = RescriptReanalyzeServerService.ServerState.entries
        assertEquals(4, values.size)
    }

    @Test
    fun `ServerState contains STOPPED`() {
        assertEquals(
            "STOPPED",
            RescriptReanalyzeServerService.ServerState.STOPPED.name,
        )
    }

    @Test
    fun `ServerState contains STARTING`() {
        assertEquals(
            "STARTING",
            RescriptReanalyzeServerService.ServerState.STARTING.name,
        )
    }

    @Test
    fun `ServerState contains RUNNING`() {
        assertEquals(
            "RUNNING",
            RescriptReanalyzeServerService.ServerState.RUNNING.name,
        )
    }

    @Test
    fun `ServerState contains EXTERNAL`() {
        assertEquals(
            "EXTERNAL",
            RescriptReanalyzeServerService.ServerState.EXTERNAL.name,
        )
    }

    // --- Constants tests ---

    @Test
    fun `SOCKET_FILE_NAME is correct`() {
        assertEquals(
            ".rescript-reanalyze.sock",
            RescriptReanalyzeServerService.SOCKET_FILE_NAME,
        )
    }

    @Test
    fun `MAX_RESTART_COUNT is 3`() {
        assertEquals(3, RescriptReanalyzeServerService.MAX_RESTART_COUNT)
    }

    @Test
    fun `HEALTH_CHECK_INTERVAL_SECONDS is 5`() {
        assertEquals(5L, RescriptReanalyzeServerService.HEALTH_CHECK_INTERVAL_SECONDS)
    }

    @Test
    fun `SOCKET_WAIT_TIMEOUT_MS is 10000`() {
        assertEquals(10_000L, RescriptReanalyzeServerService.SOCKET_WAIT_TIMEOUT_MS)
    }

    @Test
    fun `PROCESS_STOP_TIMEOUT_SECONDS is 5`() {
        assertEquals(5L, RescriptReanalyzeServerService.PROCESS_STOP_TIMEOUT_SECONDS)
    }

    // --- getSocketPath tests ---

    @Test
    fun `getSocketPath returns correct path`() {
        val basePath = "/project/root"
        val socketPath = RescriptReanalyzeServerService.getSocketPath(basePath)
        // Assert the components rather than a rendered string: Path.toString()
        // is backslash-separated on Windows. Checking the file name and the
        // parent separately also avoids restating the implementation verbatim.
        assertEquals(".rescript-reanalyze.sock", socketPath.fileName.toString())
        assertEquals(Path.of(basePath), socketPath.parent)
    }

    @Test
    fun `getSocketPath handles trailing slash`() {
        val socketPath = RescriptReanalyzeServerService.getSocketPath("/project/root")
        assertTrue(socketPath.toString().endsWith(".rescript-reanalyze.sock"))
    }

    // --- isSocketPresent tests ---

    @Test
    fun `isSocketPresent returns false when socket does not exist`() {
        val root = tempDir.resolve("project-no-socket").toFile().also { it.mkdirs() }
        assertFalse(RescriptReanalyzeServerService.isSocketPresent(root.absolutePath))
    }

    @Test
    fun `isSocketPresent returns true when socket file exists`() {
        val root = tempDir.resolve("project-with-socket").toFile().also { it.mkdirs() }
        File(root, ".rescript-reanalyze.sock").createNewFile()
        assertTrue(RescriptReanalyzeServerService.isSocketPresent(root.absolutePath))
    }
}
