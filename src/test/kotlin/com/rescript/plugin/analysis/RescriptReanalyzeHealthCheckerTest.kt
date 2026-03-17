package com.rescript.plugin.analysis

import com.rescript.plugin.analysis.RescriptReanalyzeHealthChecker.HealthCheckAction
import com.rescript.plugin.analysis.RescriptReanalyzeServerService.ServerState
import org.junit.Assert.assertEquals
import org.junit.Test

class RescriptReanalyzeHealthCheckerTest {
    // --- RUNNING state tests ---

    @Test
    fun `RUNNING with alive process returns HEALTHY`() {
        val action =
            RescriptReanalyzeHealthChecker.determineAction(
                state = ServerState.RUNNING,
                isProcessAlive = true,
                isSocketPresent = true,
                restartCount = 0,
                maxRestartCount = 3,
            )
        assertEquals(HealthCheckAction.HEALTHY, action)
    }

    @Test
    fun `RUNNING with dead process and restarts remaining returns RESTART`() {
        val action =
            RescriptReanalyzeHealthChecker.determineAction(
                state = ServerState.RUNNING,
                isProcessAlive = false,
                isSocketPresent = false,
                restartCount = 0,
                maxRestartCount = 3,
            )
        assertEquals(HealthCheckAction.RESTART, action)
    }

    @Test
    fun `RUNNING with dead process and one restart remaining returns RESTART`() {
        val action =
            RescriptReanalyzeHealthChecker.determineAction(
                state = ServerState.RUNNING,
                isProcessAlive = false,
                isSocketPresent = false,
                restartCount = 2,
                maxRestartCount = 3,
            )
        assertEquals(HealthCheckAction.RESTART, action)
    }

    @Test
    fun `RUNNING with dead process and max restarts reached returns MAX_RESTARTS_REACHED`() {
        val action =
            RescriptReanalyzeHealthChecker.determineAction(
                state = ServerState.RUNNING,
                isProcessAlive = false,
                isSocketPresent = false,
                restartCount = 3,
                maxRestartCount = 3,
            )
        assertEquals(HealthCheckAction.MAX_RESTARTS_REACHED, action)
    }

    @Test
    fun `RUNNING with dead process and restarts exceeded returns MAX_RESTARTS_REACHED`() {
        val action =
            RescriptReanalyzeHealthChecker.determineAction(
                state = ServerState.RUNNING,
                isProcessAlive = false,
                isSocketPresent = false,
                restartCount = 5,
                maxRestartCount = 3,
            )
        assertEquals(HealthCheckAction.MAX_RESTARTS_REACHED, action)
    }

    @Test
    fun `RUNNING ignores socket presence when process is alive`() {
        val action =
            RescriptReanalyzeHealthChecker.determineAction(
                state = ServerState.RUNNING,
                isProcessAlive = true,
                isSocketPresent = false,
                restartCount = 0,
                maxRestartCount = 3,
            )
        assertEquals(HealthCheckAction.HEALTHY, action)
    }

    // --- EXTERNAL state tests ---

    @Test
    fun `EXTERNAL with socket present returns HEALTHY`() {
        val action =
            RescriptReanalyzeHealthChecker.determineAction(
                state = ServerState.EXTERNAL,
                isProcessAlive = false,
                isSocketPresent = true,
                restartCount = 0,
                maxRestartCount = 3,
            )
        assertEquals(HealthCheckAction.HEALTHY, action)
    }

    @Test
    fun `EXTERNAL with socket absent returns EXTERNAL_LOST`() {
        val action =
            RescriptReanalyzeHealthChecker.determineAction(
                state = ServerState.EXTERNAL,
                isProcessAlive = false,
                isSocketPresent = false,
                restartCount = 0,
                maxRestartCount = 3,
            )
        assertEquals(HealthCheckAction.EXTERNAL_LOST, action)
    }

    @Test
    fun `EXTERNAL ignores process alive state`() {
        val action =
            RescriptReanalyzeHealthChecker.determineAction(
                state = ServerState.EXTERNAL,
                isProcessAlive = true,
                isSocketPresent = false,
                restartCount = 0,
                maxRestartCount = 3,
            )
        assertEquals(HealthCheckAction.EXTERNAL_LOST, action)
    }

    // --- STOPPED state tests ---

    @Test
    fun `STOPPED returns NO_CHECK_NEEDED`() {
        val action =
            RescriptReanalyzeHealthChecker.determineAction(
                state = ServerState.STOPPED,
                isProcessAlive = false,
                isSocketPresent = false,
                restartCount = 0,
                maxRestartCount = 3,
            )
        assertEquals(HealthCheckAction.NO_CHECK_NEEDED, action)
    }

    // --- STARTING state tests ---

    @Test
    fun `STARTING returns NO_CHECK_NEEDED`() {
        val action =
            RescriptReanalyzeHealthChecker.determineAction(
                state = ServerState.STARTING,
                isProcessAlive = false,
                isSocketPresent = false,
                restartCount = 0,
                maxRestartCount = 3,
            )
        assertEquals(HealthCheckAction.NO_CHECK_NEEDED, action)
    }

    // --- Edge cases ---

    @Test
    fun `zero maxRestartCount means no restarts allowed`() {
        val action =
            RescriptReanalyzeHealthChecker.determineAction(
                state = ServerState.RUNNING,
                isProcessAlive = false,
                isSocketPresent = false,
                restartCount = 0,
                maxRestartCount = 0,
            )
        assertEquals(HealthCheckAction.MAX_RESTARTS_REACHED, action)
    }

    @Test
    fun `HealthCheckAction has five values`() {
        assertEquals(5, HealthCheckAction.entries.size)
    }
}
