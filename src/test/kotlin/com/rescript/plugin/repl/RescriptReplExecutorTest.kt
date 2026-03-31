package com.rescript.plugin.repl

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class RescriptReplExecutorTest {
    @Test
    fun `wrapCode returns let binding as-is`() {
        val result = RescriptReplExecutor.wrapCode("let x = 42")
        assertEquals("let x = 42", result)
    }

    @Test
    fun `wrapCode wraps simple expression in Js log`() {
        val result = RescriptReplExecutor.wrapCode("1 + 2")
        assertEquals("Js.log(1 + 2)", result)
    }

    @Test
    fun `wrapCode does not double wrap Js log`() {
        val result = RescriptReplExecutor.wrapCode("Js.log(\"hello\")")
        assertEquals("Js.log(\"hello\")", result)
    }

    @Test
    fun `wrapCode returns module declaration as-is`() {
        val result = RescriptReplExecutor.wrapCode("module M = { let x = 1 }")
        assertEquals("module M = { let x = 1 }", result)
    }

    @Test
    fun `parseOutput extracts stdout`() {
        val result = RescriptReplExecutor.parseOutput("42\n", "")
        assertEquals("42", result)
    }

    @Test
    fun `parseOutput returns stderr on error`() {
        val result = RescriptReplExecutor.parseOutput("", "Error: something failed\n")
        assertTrue(result.contains("Error"))
    }

    @Test
    fun `parseOutput trims whitespace`() {
        val result = RescriptReplExecutor.parseOutput("  hello  \n", "")
        assertEquals("hello", result)
    }

    @Test
    fun `parseOutput includes both stdout and stderr`() {
        val result = RescriptReplExecutor.parseOutput("42\n", "warning: something\n")
        assertTrue(result.contains("42"))
        assertTrue(result.contains("warning: something"))
    }

    @Test
    fun `parseOutput returns no output for empty streams`() {
        val result = RescriptReplExecutor.parseOutput("", "")
        assertEquals("(no output)", result)
    }

    @Test
    fun `execute rejects invalid project path`() {
        val result = RescriptReplExecutor.execute("1 + 2", "/nonexistent/path")
        assertEquals("Error: invalid project path", result)
    }

    @Test
    fun `execute rejects file path instead of directory`() {
        val tmpFile = java.io.File.createTempFile("test", ".txt")
        try {
            val result = RescriptReplExecutor.execute("1 + 2", tmpFile.absolutePath)
            assertEquals("Error: invalid project path", result)
        } finally {
            tmpFile.delete()
        }
    }
}
