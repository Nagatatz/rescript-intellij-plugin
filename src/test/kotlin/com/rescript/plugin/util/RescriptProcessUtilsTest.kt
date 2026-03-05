package com.rescript.plugin.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

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
        assertTrue("Exit code should be non-zero", result.exitCode != 0)
        assertFalse(result.timedOut)
    }

    @Test
    fun testRunSimpleCommandHandlesNonExistentCommand() {
        val result = RescriptProcessUtils.runSimpleCommand("__nonexistent_command_12345__")
        // Should not throw, returns error result
        assertTrue("Exit code should be -1 for failed process", result.exitCode == -1)
        assertFalse(result.timedOut)
    }
}
