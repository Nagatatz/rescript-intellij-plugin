package com.rescript.plugin.analysis

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.openapi.Disposable
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import com.intellij.util.concurrency.AppExecutorUtil
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

/**
 * Project-level service that manages the lifecycle of the `reanalyze-server` daemon process.
 *
 * When ReScript >= 12.1.0 is installed and the setting is enabled, this service starts
 * `rescript-tools reanalyze-server` as a background process. The server communicates via a
 * Unix socket (`.rescript-reanalyze.sock`) and transparently accelerates existing
 * `reanalyze -json` invocations from [RescriptReanalyzeAnnotator] and
 * [RescriptUnusedCodeInspection] without any protocol changes.
 *
 * Features:
 * - Detects externally started servers (socket exists but no managed process)
 * - Periodic health checks (every 5 seconds) with automatic restart (up to 3 times)
 * - Clean shutdown on project close via [Disposable]
 *
 * @see RescriptReanalyzeVersionDetector
 * @see RescriptReanalyzeServerStartupActivity
 * @see RescriptCompilationStatusService for a similar service pattern
 */
@Service(Service.Level.PROJECT)
class RescriptReanalyzeServerService(
    private val project: Project,
) : Disposable {
    @Volatile
    var serverState: ServerState = ServerState.STOPPED
        private set

    private var serverProcess: Process? = null
    private var startedByUs: Boolean = false
    private var restartCount: Int = 0
    private var healthCheckFuture: ScheduledFuture<*>? = null

    /**
     * Represents the possible states of the reanalyze server.
     */
    enum class ServerState {
        /** Server is not running. */
        STOPPED,

        /** Server is being started; waiting for socket. */
        STARTING,

        /** Server was started by this service and is running. */
        RUNNING,

        /** An external server was detected (socket exists, not managed by us). */
        EXTERNAL,
    }

    /**
     * Starts the reanalyze server if conditions are met.
     *
     * Checks for an existing socket (external server), then attempts to start
     * `rescript-tools reanalyze-server` and waits for the socket to appear.
     */
    fun startServer() {
        if (serverState == ServerState.RUNNING || serverState == ServerState.EXTERNAL) {
            LOG.debug("Server already in state $serverState, skipping start")
            return
        }

        val basePath = project.basePath ?: return

        // Check for externally started server
        if (isSocketPresent(basePath)) {
            LOG.info("External reanalyze server detected (socket exists)")
            serverState = ServerState.EXTERNAL
            startHealthCheck()
            return
        }

        val toolPath =
            RescriptReanalyzeAnnotator.findReanalyzeTool(basePath) ?: run {
                LOG.debug("rescript-tools not found, cannot start reanalyze server")
                return
            }

        serverState = ServerState.STARTING

        try {
            val commandLine =
                GeneralCommandLine(toolPath, "reanalyze-server")
                    .withWorkDirectory(basePath)
                    .withCharset(Charsets.UTF_8)

            val process = commandLine.createProcess()
            serverProcess = process
            startedByUs = true

            // Wait for socket to appear (up to 10 seconds)
            val socketAppeared = waitForSocket(basePath, SOCKET_WAIT_TIMEOUT_MS)
            if (!socketAppeared) {
                LOG.warn("Reanalyze server socket did not appear within timeout")
                stopServerProcess()
                serverState = ServerState.STOPPED
                return
            }

            serverState = ServerState.RUNNING
            restartCount = 0
            LOG.info("Reanalyze server started successfully")
            startHealthCheck()
        } catch (e: Exception) {
            LOG.warn("Failed to start reanalyze server", e)
            serverState = ServerState.STOPPED
        }
    }

    /**
     * Stops the reanalyze server if it was started by this service.
     */
    fun stopServer() {
        stopHealthCheck()

        if (startedByUs) {
            stopServerProcess()
            cleanupSocket()
        }

        serverProcess = null
        startedByUs = false
        restartCount = 0
        serverState = ServerState.STOPPED
        LOG.info("Reanalyze server stopped")
    }

    override fun dispose() {
        stopServer()
    }

    private fun stopServerProcess() {
        val process = serverProcess ?: return
        if (process.isAlive) {
            process.destroy()
            try {
                if (!process.waitFor(PROCESS_STOP_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                    process.destroyForcibly()
                }
            } catch (e: InterruptedException) {
                process.destroyForcibly()
                Thread.currentThread().interrupt()
            }
        }
    }

    private fun cleanupSocket() {
        val basePath = project.basePath ?: return
        val socketPath = getSocketPath(basePath)
        try {
            Files.deleteIfExists(socketPath)
        } catch (e: Exception) {
            LOG.debug("Failed to delete socket file: $socketPath", e)
        }
    }

    private fun startHealthCheck() {
        stopHealthCheck()
        healthCheckFuture =
            AppExecutorUtil.getAppScheduledExecutorService().scheduleWithFixedDelay(
                { performHealthCheck() },
                HEALTH_CHECK_INTERVAL_SECONDS,
                HEALTH_CHECK_INTERVAL_SECONDS,
                TimeUnit.SECONDS,
            )
    }

    private fun stopHealthCheck() {
        healthCheckFuture?.cancel(false)
        healthCheckFuture = null
    }

    private fun performHealthCheck() {
        when (serverState) {
            ServerState.RUNNING -> {
                val process = serverProcess
                if (process == null || !process.isAlive) {
                    LOG.warn("Reanalyze server process died unexpectedly")
                    cleanupSocket()
                    serverState = ServerState.STOPPED

                    if (restartCount < MAX_RESTART_COUNT) {
                        restartCount++
                        LOG.info("Attempting restart ($restartCount/$MAX_RESTART_COUNT)")
                        startServer()
                    } else {
                        LOG.warn("Max restart attempts reached, giving up")
                        stopHealthCheck()
                    }
                }
            }
            ServerState.EXTERNAL -> {
                val basePath = project.basePath
                if (basePath == null || !isSocketPresent(basePath)) {
                    LOG.info("External reanalyze server no longer available")
                    serverState = ServerState.STOPPED
                    stopHealthCheck()
                }
            }
            else -> {
                // No health check needed for STOPPED or STARTING
            }
        }
    }

    /**
     * Waits for the reanalyze socket file to appear.
     *
     * @param basePath the project base path
     * @param timeoutMs maximum time to wait in milliseconds
     * @return true if the socket appeared within the timeout
     */
    private fun waitForSocket(
        basePath: String,
        timeoutMs: Long,
    ): Boolean {
        val socketPath = getSocketPath(basePath)
        val deadline = System.currentTimeMillis() + timeoutMs
        while (System.currentTimeMillis() < deadline) {
            if (Files.exists(socketPath)) return true
            // Also check if process died early
            val process = serverProcess
            if (process != null && !process.isAlive) return false
            try {
                Thread.sleep(SOCKET_POLL_INTERVAL_MS)
            } catch (e: InterruptedException) {
                Thread.currentThread().interrupt()
                return false
            }
        }
        return false
    }

    companion object {
        private val LOG = logger<RescriptReanalyzeServerService>()

        /** Name of the Unix socket file created by `reanalyze-server`. */
        const val SOCKET_FILE_NAME = ".rescript-reanalyze.sock"

        /** Maximum number of automatic restart attempts after a crash. */
        const val MAX_RESTART_COUNT = 3

        /** Interval between health checks in seconds. */
        const val HEALTH_CHECK_INTERVAL_SECONDS = 5L

        /** Maximum time to wait for the socket to appear in milliseconds. */
        const val SOCKET_WAIT_TIMEOUT_MS = 10_000L

        /** Time to wait for the server process to stop gracefully. */
        const val PROCESS_STOP_TIMEOUT_SECONDS = 5L

        /** Interval between socket existence polls in milliseconds. */
        private const val SOCKET_POLL_INTERVAL_MS = 200L

        /**
         * Returns the path to the reanalyze socket file.
         *
         * @param basePath the project base path
         * @return the socket file path
         */
        fun getSocketPath(basePath: String): Path = Path.of(basePath).resolve(SOCKET_FILE_NAME)

        /**
         * Checks whether the reanalyze socket file exists.
         *
         * @param basePath the project base path
         * @return true if the socket file exists
         */
        fun isSocketPresent(basePath: String): Boolean = Files.exists(getSocketPath(basePath))

        fun getInstance(project: Project): RescriptReanalyzeServerService = project.service()
    }
}
