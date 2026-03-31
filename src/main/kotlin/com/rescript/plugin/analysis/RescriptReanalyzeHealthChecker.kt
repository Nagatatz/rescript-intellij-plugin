package com.rescript.plugin.analysis

/**
 * Encapsulates the decision logic for reanalyze server health checks.
 *
 * This class extracts the pure health-check determination from
 * [RescriptReanalyzeServerService], making it independently testable
 * without requiring process or scheduler mocks.
 *
 * @see RescriptReanalyzeServerService
 */
data object RescriptReanalyzeHealthChecker {
    /**
     * Represents the action to take after a health check evaluation.
     */
    enum class HealthCheckAction {
        /** Server is healthy, no action needed. */
        HEALTHY,

        /** Managed server process died; should attempt a restart. */
        RESTART,

        /** Maximum restart attempts reached; stop monitoring. */
        MAX_RESTARTS_REACHED,

        /** External server is no longer available; stop monitoring. */
        EXTERNAL_LOST,

        /** Server is in a state that does not require health checks. */
        NO_CHECK_NEEDED,
    }

    /**
     * Determines the appropriate health check action based on current server state.
     *
     * This is a pure function with no side effects, enabling straightforward unit testing
     * of the health check decision logic.
     *
     * @param state the current server state
     * @param isProcessAlive whether the managed server process is still alive
     * @param isSocketPresent whether the reanalyze socket file exists
     * @param restartCount the number of restarts already attempted
     * @param maxRestartCount the maximum number of allowed restart attempts
     * @return the action to take based on the evaluation
     */
    fun determineAction(
        state: RescriptReanalyzeServerService.ServerState,
        isProcessAlive: Boolean,
        isSocketPresent: Boolean,
        restartCount: Int,
        maxRestartCount: Int,
    ): HealthCheckAction =
        when (state) {
            RescriptReanalyzeServerService.ServerState.RUNNING -> {
                if (isProcessAlive) {
                    HealthCheckAction.HEALTHY
                } else if (restartCount < maxRestartCount) {
                    HealthCheckAction.RESTART
                } else {
                    HealthCheckAction.MAX_RESTARTS_REACHED
                }
            }

            RescriptReanalyzeServerService.ServerState.EXTERNAL -> {
                if (isSocketPresent) {
                    HealthCheckAction.HEALTHY
                } else {
                    HealthCheckAction.EXTERNAL_LOST
                }
            }

            else -> {
                HealthCheckAction.NO_CHECK_NEEDED
            }
        }
}
