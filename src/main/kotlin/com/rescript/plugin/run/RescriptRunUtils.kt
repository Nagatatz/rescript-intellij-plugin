package com.rescript.plugin.run

/**
 * Shared utilities for ReScript run configurations (build, test, debug).
 *
 * @see RescriptRunConfiguration
 * @see com.rescript.plugin.test.RescriptTestRunConfiguration
 * @see com.rescript.plugin.debug.RescriptDebugRunConfiguration
 */
object RescriptRunUtils {
    /** Regex for splitting whitespace-separated arguments. */
    val WHITESPACE_REGEX = "\\s+".toRegex()
}
