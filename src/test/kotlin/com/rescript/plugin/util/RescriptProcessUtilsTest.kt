package com.rescript.plugin.util

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Tests for [RescriptProcessUtils] — shared process execution utility.
 *
 * Verifies command execution, exit code capture, output reading, and timeout handling.
 */
class RescriptProcessUtilsTest {
    @Test
    fun testRunSimpleCommandReturnsOutput() {
        val result = RescriptProcessUtils.runSimpleCommand("echo", "hello")
        assertEquals(0, result.exitCode)
        assertEquals("hello", result.firstLine)
        assertFalse(result.timedOut)
    }

    @Test
    fun testRunSimpleCommandCapturesExitCode() {
        val result = RescriptProcessUtils.runSimpleCommand("false")
        assertTrue(result.exitCode != 0, "Exit code should be non-zero")
        assertFalse(result.timedOut)
    }

    @Test
    fun testRunSimpleCommandHandlesNonExistentCommand() {
        val result = RescriptProcessUtils.runSimpleCommand("__nonexistent_command_12345__")
        // Should not throw, returns error result
        assertTrue(result.exitCode == -1, "Exit code should be -1 for failed process")
        assertFalse(result.timedOut)
    }

    @Test
    fun testRunSimpleCommandTimesOut() {
        // Produce output first (so readLine() returns), then block indefinitely.
        // This exercises the timeout branch in waitFor().
        val result = RescriptProcessUtils.runSimpleCommand("bash", "-c", "echo started; sleep 60", timeoutSeconds = 1)
        assertEquals(-1, result.exitCode)
        assertTrue(result.timedOut, "Should report timed out")
        assertEquals("started", result.firstLine)
    }

    @Test
    fun testRunSimpleCommandHandlesEmptyOutput() {
        // `true` produces no output, so readLine() returns null
        val result = RescriptProcessUtils.runSimpleCommand("true")
        assertEquals(0, result.exitCode)
        assertEquals("", result.firstLine, "Empty output should return empty string")
        assertFalse(result.timedOut)
    }

    @Test
    fun testProcessResultDataClass() {
        val result = RescriptProcessUtils.ProcessResult(exitCode = 42, firstLine = "test", timedOut = true)
        assertEquals(42, result.exitCode)
        assertEquals("test", result.firstLine)
        assertTrue(result.timedOut)
    }
}
