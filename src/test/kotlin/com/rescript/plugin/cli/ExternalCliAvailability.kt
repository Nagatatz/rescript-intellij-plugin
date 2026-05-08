package com.rescript.plugin.cli

import java.util.concurrent.TimeUnit

/**
 * Probes whether the external CLIs that back the Phase 2 verification
 * tests (`mmdc`, `dot`, `npx rescript`) are available on the host.
 *
 * Each probe runs a short version-check command with a 5-second
 * timeout and treats any non-zero exit, missing binary, or
 * `IOException` as "unavailable". Tests use these probes through
 * `Assumptions.assumeTrue` so CI environments install the CLI and
 * developer machines that lack it simply skip.
 */
object ExternalCliAvailability {
    /** True iff `mmdc --version` runs and exits 0. */
    fun isMermaidCliAvailable(): Boolean = canRun(listOf("mmdc", "--version"))

    /** True iff `dot -V` runs and exits 0. */
    fun isDotAvailable(): Boolean = canRun(listOf("dot", "-V"))

    /**
     * True iff `npx rescript -h` runs and exits 0.
     *
     * Uses `npx` because the migration converter itself falls through
     * to `npx rescript` when no explicit binary path is configured,
     * so the probe matches the production resolution.
     */
    fun isRescriptCliAvailable(): Boolean = canRun(listOf("npx", "rescript", "-h"))

    private fun canRun(argv: List<String>): Boolean =
        try {
            val process =
                ProcessBuilder(argv)
                    .redirectErrorStream(true)
                    .start()
            val finished = process.waitFor(PROBE_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            if (!finished) {
                process.destroyForcibly()
                false
            } else {
                process.exitValue() == 0
            }
        } catch (_: Exception) {
            false
        }

    private const val PROBE_TIMEOUT_SECONDS = 5L
}
