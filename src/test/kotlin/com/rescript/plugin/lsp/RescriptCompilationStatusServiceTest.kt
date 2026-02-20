package com.rescript.plugin.lsp

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class RescriptCompilationStatusServiceTest {
    @Test
    fun `CompilationStatus UNKNOWN has status unknown`() {
        val unknown = RescriptCompilationStatusService.CompilationStatus.UNKNOWN
        assertEquals("unknown", unknown.status)
        assertEquals(0, unknown.errorCount)
        assertEquals(0, unknown.warningCount)
    }

    @Test
    fun `CompilationStatus data class properties`() {
        val status = RescriptCompilationStatusService.CompilationStatus("success", 2, 3)
        assertEquals("success", status.status)
        assertEquals(2, status.errorCount)
        assertEquals(3, status.warningCount)
    }

    @Test
    fun `CompilationStatus equality`() {
        val a = RescriptCompilationStatusService.CompilationStatus("success", 0, 0)
        val b = RescriptCompilationStatusService.CompilationStatus("success", 0, 0)
        assertEquals(a, b)
    }

    @Test
    fun `CompilationStatus inequality`() {
        val a = RescriptCompilationStatusService.CompilationStatus("success", 0, 0)
        val b = RescriptCompilationStatusService.CompilationStatus("error", 1, 0)
        assertNotEquals(a, b)
    }

    @Test
    fun `CompilationStatus copy`() {
        val original = RescriptCompilationStatusService.CompilationStatus("success", 0, 0)
        val copy = original.copy(errorCount = 5)
        assertEquals(5, copy.errorCount)
        assertEquals("success", copy.status)
    }

    // ── CompilationFinishedParams tests ───────────────────────────────

    @Test
    fun `CompilationFinishedParams default values`() {
        val params = RescriptLsp4jClient.CompilationFinishedParams()
        assertEquals("", params.project)
        assertEquals("", params.projectRootPath)
    }

    @Test
    fun `CompilationFinishedParams with values`() {
        val params = RescriptLsp4jClient.CompilationFinishedParams("myProject", "/path/to/project")
        assertEquals("myProject", params.project)
        assertEquals("/path/to/project", params.projectRootPath)
    }

    @Test
    fun `CompilationFinishedParams equality`() {
        val a = RescriptLsp4jClient.CompilationFinishedParams("proj", "/path")
        val b = RescriptLsp4jClient.CompilationFinishedParams("proj", "/path")
        assertEquals(a, b)
    }

    @Test
    fun `CompilationFinishedParams inequality`() {
        val a = RescriptLsp4jClient.CompilationFinishedParams("proj1", "/path1")
        val b = RescriptLsp4jClient.CompilationFinishedParams("proj2", "/path2")
        assertNotEquals(a, b)
    }
}
