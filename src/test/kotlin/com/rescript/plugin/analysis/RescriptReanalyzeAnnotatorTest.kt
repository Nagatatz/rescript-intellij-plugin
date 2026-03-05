package com.rescript.plugin.analysis

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

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

    @Test
    fun `parseJsonOutput handles empty array`() {
        val result = RescriptReanalyzeAnnotator.parseJsonOutput("[]", "/project/App.res")
        assertTrue(result.isEmpty())
    }

    @Test
    fun `parseJsonOutput skips entries without file field`() {
        val json =
            """
            [{"range": [1, 0, 1, 10], "message": "test"}]
            """.trimIndent()
        val result = RescriptReanalyzeAnnotator.parseJsonOutput(json, "/project/App.res")
        assertTrue(result.isEmpty())
    }

    @Test
    fun `parseJsonOutput skips entries without message field`() {
        val json =
            """
            [{"file": "App.res", "range": [1, 0, 1, 10]}]
            """.trimIndent()
        val result = RescriptReanalyzeAnnotator.parseJsonOutput(json, "/project/App.res")
        assertTrue(result.isEmpty())
    }

    // --- parseAllDiagnostics ---

    @Test
    fun `parseAllDiagnostics returns empty for blank input`() {
        assertTrue(RescriptReanalyzeAnnotator.parseAllDiagnostics("").isEmpty())
    }

    @Test
    fun `parseAllDiagnostics returns empty for whitespace`() {
        assertTrue(RescriptReanalyzeAnnotator.parseAllDiagnostics("   ").isEmpty())
    }

    @Test
    fun `parseAllDiagnostics parses all entries without file filtering`() {
        val json =
            """
            [
                {"file": "A.res", "range": [1, 0, 1, 10], "message": "msg1", "name": "n1"},
                {"file": "B.res", "range": [2, 0, 2, 20], "message": "msg2", "name": "n2"}
            ]
            """.trimIndent()
        val result = RescriptReanalyzeAnnotator.parseAllDiagnostics(json)
        assertEquals(2, result.size)
        assertEquals("A.res", result[0].first)
        assertEquals("B.res", result[1].first)
        assertEquals("n1", result[0].second.name)
        assertEquals("n2", result[1].second.name)
    }

    @Test
    fun `parseAllDiagnostics returns empty for invalid JSON`() {
        assertTrue(RescriptReanalyzeAnnotator.parseAllDiagnostics("invalid").isEmpty())
    }

    @Test
    fun `parseAllDiagnostics returns empty for empty array`() {
        assertTrue(RescriptReanalyzeAnnotator.parseAllDiagnostics("[]").isEmpty())
    }

    @Test
    fun `parseAllDiagnostics defaults name to unknown`() {
        val json = """[{"file": "A.res", "range": [1, 0, 1, 10], "message": "msg"}]"""
        val result = RescriptReanalyzeAnnotator.parseAllDiagnostics(json)
        assertEquals(1, result.size)
        assertEquals("unknown", result[0].second.name)
    }

    @Test
    fun `parseAllDiagnostics skips entries without file`() {
        val json = """[{"range": [1, 0, 1, 10], "message": "msg"}]"""
        assertTrue(RescriptReanalyzeAnnotator.parseAllDiagnostics(json).isEmpty())
    }

    @Test
    fun `parseAllDiagnostics skips entries without message`() {
        val json = """[{"file": "A.res", "range": [1, 0, 1, 10]}]"""
        assertTrue(RescriptReanalyzeAnnotator.parseAllDiagnostics(json).isEmpty())
    }

    // --- Data classes ---

    @Test
    fun `CollectedInfo holds filePath and projectBasePath`() {
        val info = RescriptReanalyzeAnnotator.CollectedInfo("/a/b.res", "/a")
        assertEquals("/a/b.res", info.filePath)
        assertEquals("/a", info.projectBasePath)
    }

    @Test
    fun `CollectedInfo equality`() {
        val a = RescriptReanalyzeAnnotator.CollectedInfo("/a.res", "/a")
        val b = RescriptReanalyzeAnnotator.CollectedInfo("/a.res", "/a")
        assertEquals(a, b)
    }

    @Test
    fun `ReanalyzeDiagnostic data class properties`() {
        val diag = RescriptReanalyzeAnnotator.ReanalyzeDiagnostic("dead", "Dead code", 1, 2, 3, 4)
        assertEquals("dead", diag.name)
        assertEquals("Dead code", diag.message)
        assertEquals(1, diag.startLine)
        assertEquals(2, diag.startChar)
        assertEquals(3, diag.endLine)
        assertEquals(4, diag.endChar)
    }

    @Test
    fun `AnnotationResult holds diagnostics list`() {
        val diag = RescriptReanalyzeAnnotator.ReanalyzeDiagnostic("x", "msg", 0, 0, 0, 5)
        val result = RescriptReanalyzeAnnotator.AnnotationResult(listOf(diag))
        assertEquals(1, result.diagnostics.size)
    }

    @Test
    fun `AnnotationResult empty diagnostics`() {
        val result = RescriptReanalyzeAnnotator.AnnotationResult(emptyList())
        assertTrue(result.diagnostics.isEmpty())
    }

    // --- parseDiagnosticEntry ---

    @Test
    fun `parseDiagnosticEntry returns pair for valid entry`() {
        val obj =
            com.google.gson.JsonParser
                .parseString("""{"file": "App.res", "range": [1, 0, 1, 10], "message": "msg", "name": "unused"}""")
                .asJsonObject
        val result = RescriptReanalyzeAnnotator.parseDiagnosticEntry(obj)
        assertNotNull(result)
        assertEquals("App.res", result!!.first)
        assertEquals("unused", result.second.name)
        assertEquals("msg", result.second.message)
        assertEquals(1, result.second.startLine)
        assertEquals(0, result.second.startChar)
        assertEquals(1, result.second.endLine)
        assertEquals(10, result.second.endChar)
    }

    @Test
    fun `parseDiagnosticEntry returns null when file is missing`() {
        val obj =
            com.google.gson.JsonParser
                .parseString("""{"range": [1, 0, 1, 10], "message": "msg"}""")
                .asJsonObject
        assertNull(RescriptReanalyzeAnnotator.parseDiagnosticEntry(obj))
    }

    @Test
    fun `parseDiagnosticEntry returns null when range is missing`() {
        val obj =
            com.google.gson.JsonParser
                .parseString("""{"file": "App.res", "message": "msg"}""")
                .asJsonObject
        assertNull(RescriptReanalyzeAnnotator.parseDiagnosticEntry(obj))
    }

    @Test
    fun `parseDiagnosticEntry returns null when range has fewer than 4 elements`() {
        val obj =
            com.google.gson.JsonParser
                .parseString("""{"file": "App.res", "range": [1, 0, 1], "message": "msg"}""")
                .asJsonObject
        assertNull(RescriptReanalyzeAnnotator.parseDiagnosticEntry(obj))
    }

    @Test
    fun `parseDiagnosticEntry returns null when message is missing`() {
        val obj =
            com.google.gson.JsonParser
                .parseString("""{"file": "App.res", "range": [1, 0, 1, 10]}""")
                .asJsonObject
        assertNull(RescriptReanalyzeAnnotator.parseDiagnosticEntry(obj))
    }

    @Test
    fun `parseDiagnosticEntry defaults name to unknown`() {
        val obj =
            com.google.gson.JsonParser
                .parseString("""{"file": "App.res", "range": [1, 0, 1, 10], "message": "msg"}""")
                .asJsonObject
        val result = RescriptReanalyzeAnnotator.parseDiagnosticEntry(obj)
        assertNotNull(result)
        assertEquals("unknown", result!!.second.name)
    }

    @Test
    fun `parseDiagnosticEntry handles range with more than 4 elements`() {
        val obj =
            com.google.gson.JsonParser
                .parseString("""{"file": "A.res", "range": [1, 2, 3, 4, 5, 6], "message": "ok", "name": "n"}""")
                .asJsonObject
        val result = RescriptReanalyzeAnnotator.parseDiagnosticEntry(obj)
        assertNotNull(result)
        assertEquals(1, result!!.second.startLine)
        assertEquals(2, result.second.startChar)
        assertEquals(3, result.second.endLine)
        assertEquals(4, result.second.endChar)
    }

    @Test
    fun `parseDiagnosticEntry handles range with exactly 4 elements`() {
        val obj =
            com.google.gson.JsonParser
                .parseString("""{"file": "B.res", "range": [0, 0, 0, 0], "message": "zero", "name": "z"}""")
                .asJsonObject
        val result = RescriptReanalyzeAnnotator.parseDiagnosticEntry(obj)
        assertNotNull(result)
        assertEquals(0, result!!.second.startLine)
        assertEquals(0, result.second.startChar)
        assertEquals(0, result.second.endLine)
        assertEquals(0, result.second.endChar)
    }

    // --- findReanalyzeTool ---

    @Test
    fun `findReanalyzeTool returns null for temp dir without node_modules`() {
        val tempDir =
            java.nio.file.Files
                .createTempDirectory("reanalyze-test")
        try {
            assertNull(RescriptReanalyzeAnnotator.findReanalyzeTool(tempDir.toString()))
        } finally {
            tempDir.toFile().deleteRecursively()
        }
    }
}
