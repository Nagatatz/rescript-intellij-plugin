package com.rescript.plugin.analysis

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RescriptReanalyzeAnnotatorTest {
    @Test
    fun `parseJsonOutput returns diagnostics for matching file`() {
        val json =
            """
            [
                {
                    "name": "unusedVariable",
                    "kind": "warning",
                    "file": "src/App.res",
                    "range": [10, 4, 10, 20],
                    "message": "unused variable x"
                }
            ]
            """.trimIndent()

        val result = RescriptReanalyzeAnnotator.parseJsonOutput(json, "/project/src/App.res")
        assertEquals(1, result.size)
        assertEquals("unused variable x", result[0].message)
        assertEquals(10, result[0].startLine)
        assertEquals(4, result[0].startChar)
        assertEquals(10, result[0].endLine)
        assertEquals(20, result[0].endChar)
    }

    @Test
    fun `parseJsonOutput filters out diagnostics for other files`() {
        val json =
            """
            [
                {
                    "name": "unusedVariable",
                    "kind": "warning",
                    "file": "src/Other.res",
                    "range": [5, 0, 5, 10],
                    "message": "unused variable y"
                }
            ]
            """.trimIndent()

        val result = RescriptReanalyzeAnnotator.parseJsonOutput(json, "/project/src/App.res")
        assertTrue(result.isEmpty())
    }

    @Test
    fun `parseJsonOutput handles empty JSON`() {
        val result = RescriptReanalyzeAnnotator.parseJsonOutput("", "/project/src/App.res")
        assertTrue(result.isEmpty())
    }

    @Test
    fun `parseJsonOutput handles malformed JSON gracefully`() {
        val result = RescriptReanalyzeAnnotator.parseJsonOutput("not json", "/project/src/App.res")
        assertTrue(result.isEmpty())
    }

    @Test
    fun `parseJsonOutput handles multiple diagnostics`() {
        val json =
            """
            [
                {
                    "name": "unusedVariable",
                    "kind": "warning",
                    "file": "App.res",
                    "range": [1, 0, 1, 5],
                    "message": "msg1"
                },
                {
                    "name": "deadCode",
                    "kind": "warning",
                    "file": "App.res",
                    "range": [3, 0, 3, 10],
                    "message": "msg2"
                }
            ]
            """.trimIndent()

        val result = RescriptReanalyzeAnnotator.parseJsonOutput(json, "/project/App.res")
        assertEquals(2, result.size)
    }

    @Test
    fun `parseJsonOutput skips entries with missing range`() {
        val json =
            """
            [
                {
                    "name": "test",
                    "kind": "warning",
                    "file": "App.res",
                    "message": "no range"
                }
            ]
            """.trimIndent()

        val result = RescriptReanalyzeAnnotator.parseJsonOutput(json, "/project/App.res")
        assertTrue(result.isEmpty())
    }

    @Test
    fun `findReanalyzeTool returns null for non-existent path`() {
        val result = RescriptReanalyzeAnnotator.findReanalyzeTool("/nonexistent/path")
        assertNull(result)
    }

    @Test
    fun `parseJsonOutput matches by exact filePath`() {
        val json =
            """
            [
                {
                    "name": "unusedVariable",
                    "kind": "warning",
                    "file": "/project/src/App.res",
                    "range": [1, 0, 1, 5],
                    "message": "exact match"
                }
            ]
            """.trimIndent()

        val result = RescriptReanalyzeAnnotator.parseJsonOutput(json, "/project/src/App.res")
        assertEquals(1, result.size)
        assertEquals("exact match", result[0].message)
    }

    @Test
    fun `parseJsonOutput matches when filePath ends with file`() {
        val json =
            """
            [
                {
                    "name": "deadCode",
                    "kind": "warning",
                    "file": "src/App.res",
                    "range": [2, 0, 2, 10],
                    "message": "reverse match"
                }
            ]
            """.trimIndent()

        val result = RescriptReanalyzeAnnotator.parseJsonOutput(json, "/project/src/App.res")
        assertEquals(1, result.size)
        assertEquals("reverse match", result[0].message)
    }

    @Test
    fun `parseAllDiagnostics skips entries with range size less than 4`() {
        val json =
            """
            [
                {
                    "name": "test",
                    "kind": "warning",
                    "file": "App.res",
                    "range": [1, 0, 1],
                    "message": "short range"
                }
            ]
            """.trimIndent()

        val result = RescriptReanalyzeAnnotator.parseAllDiagnostics(json)
        assertTrue(result.isEmpty())
    }

    @Test
    fun `doAnnotate returns null when info is null`() {
        val annotator = RescriptReanalyzeAnnotator()
        val result = annotator.doAnnotate(null)
        assertNull(result)
    }

    @Test
    fun `parseJsonOutput defaults name to unknown when missing`() {
        val json =
            """
            [
                {
                    "kind": "warning",
                    "file": "App.res",
                    "range": [1, 0, 1, 5],
                    "message": "no name field"
                }
            ]
            """.trimIndent()

        val result = RescriptReanalyzeAnnotator.parseJsonOutput(json, "/project/App.res")
        assertEquals(1, result.size)
        assertEquals("unknown", result[0].name)
    }
}
