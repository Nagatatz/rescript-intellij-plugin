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
}
