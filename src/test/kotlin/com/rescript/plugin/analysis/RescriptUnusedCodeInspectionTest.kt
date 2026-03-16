package com.rescript.plugin.analysis

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RescriptUnusedCodeInspectionTest {
    @Test
    fun `parseAllDiagnostics returns all diagnostics with file paths`() {
        val json =
            """
            [
                {
                    "name": "unusedVariable",
                    "kind": "warning",
                    "file": "src/App.res",
                    "range": [10, 4, 10, 20],
                    "message": "unused variable x"
                },
                {
                    "name": "deadCode",
                    "kind": "warning",
                    "file": "src/Other.res",
                    "range": [5, 0, 5, 10],
                    "message": "dead code"
                }
            ]
            """.trimIndent()

        val result = RescriptReanalyzeAnnotator.parseAllDiagnostics(json)
        assertEquals(2, result.size)
        assertEquals("src/App.res", result[0].first)
        assertEquals("unused variable x", result[0].second.message)
        assertEquals("unusedVariable", result[0].second.name)
        assertEquals("src/Other.res", result[1].first)
        assertEquals("dead code", result[1].second.message)
        assertEquals("deadCode", result[1].second.name)
    }

    @Test
    fun `parseAllDiagnostics handles empty JSON`() {
        val result = RescriptReanalyzeAnnotator.parseAllDiagnostics("")
        assertTrue(result.isEmpty())
    }

    @Test
    fun `parseAllDiagnostics handles malformed JSON`() {
        val result = RescriptReanalyzeAnnotator.parseAllDiagnostics("not json")
        assertTrue(result.isEmpty())
    }

    @Test
    fun `parseAllDiagnostics skips entries with missing fields`() {
        val json =
            """
            [
                {
                    "name": "test",
                    "kind": "warning",
                    "file": "App.res",
                    "message": "no range"
                },
                {
                    "name": "test",
                    "kind": "warning",
                    "range": [1, 0, 1, 5],
                    "message": "no file"
                },
                {
                    "name": "test",
                    "kind": "warning",
                    "file": "App.res",
                    "range": [1, 0, 1, 5]
                }
            ]
            """.trimIndent()

        val result = RescriptReanalyzeAnnotator.parseAllDiagnostics(json)
        assertTrue(result.isEmpty())
    }

    @Test
    fun `parseAllDiagnostics includes name field`() {
        val json =
            """
            [
                {
                    "name": "unusedModule",
                    "kind": "warning",
                    "file": "src/Utils.res",
                    "range": [0, 0, 0, 15],
                    "message": "unused module Utils"
                }
            ]
            """.trimIndent()

        val result = RescriptReanalyzeAnnotator.parseAllDiagnostics(json)
        assertEquals(1, result.size)
        assertEquals("unusedModule", result[0].second.name)
    }

    @Test
    fun `parseAllDiagnostics defaults name to unknown when missing`() {
        val json =
            """
            [
                {
                    "kind": "warning",
                    "file": "src/App.res",
                    "range": [1, 0, 1, 5],
                    "message": "some warning"
                }
            ]
            """.trimIndent()

        val result = RescriptReanalyzeAnnotator.parseAllDiagnostics(json)
        assertEquals(1, result.size)
        assertEquals("unknown", result[0].second.name)
    }

    @Test
    fun `isGraphNeeded returns false`() {
        val inspection = RescriptUnusedCodeInspection()
        assertEquals(false, inspection.isGraphNeeded)
    }

    @Test
    fun `inspection is a GlobalInspectionTool`() {
        val inspection = RescriptUnusedCodeInspection()
        assertTrue(inspection is com.intellij.codeInspection.GlobalInspectionTool)
    }

    @Test
    fun `inspection can be instantiated`() {
        val inspection = RescriptUnusedCodeInspection()
        assertNotNull(inspection)
    }
}
