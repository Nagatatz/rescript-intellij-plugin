package com.rescript.plugin.lsp

import com.intellij.openapi.project.Project
import com.rescript.plugin.RescriptTestUtils
import org.junit.Assert.assertEquals
import org.junit.Test

class RescriptCompilationStatusServiceTest {
    private val project = RescriptTestUtils.stubProxy<Project>()
    private val service = RescriptCompilationStatusService(project)

    @Test
    fun `initial status is UNKNOWN`() {
        assertEquals(RescriptCompilationStatusService.CompilationStatus.UNKNOWN, service.currentStatus)
    }

    @Test
    fun `UNKNOWN has status string unknown`() {
        val unknown = RescriptCompilationStatusService.CompilationStatus.UNKNOWN
        assertEquals("unknown", unknown.status)
        assertEquals(0, unknown.errorCount)
        assertEquals(0, unknown.warningCount)
    }

    @Test
    fun `updateStatus changes currentStatus`() {
        val newStatus = RescriptCompilationStatusService.CompilationStatus("success", 0, 0)
        service.updateStatus(newStatus)
        assertEquals(newStatus, service.currentStatus)
    }

    @Test
    fun `updateStatus notifies listeners`() {
        var notifiedStatus: RescriptCompilationStatusService.CompilationStatus? = null
        val disposable = RescriptTestUtils.stubProxy<com.intellij.openapi.Disposable>()

        service.addListener(disposable) { status ->
            notifiedStatus = status
        }

        val newStatus = RescriptCompilationStatusService.CompilationStatus("error", 2, 1)
        service.updateStatus(newStatus)

        assertEquals(newStatus, notifiedStatus)
    }

    @Test
    fun `multiple updateStatus calls update correctly`() {
        val status1 = RescriptCompilationStatusService.CompilationStatus("building", 0, 0)
        val status2 = RescriptCompilationStatusService.CompilationStatus("success", 0, 3)

        service.updateStatus(status1)
        assertEquals(status1, service.currentStatus)

        service.updateStatus(status2)
        assertEquals(status2, service.currentStatus)
    }

    @Test
    fun `CompilationStatus data class equality`() {
        val s1 = RescriptCompilationStatusService.CompilationStatus("success", 0, 0)
        val s2 = RescriptCompilationStatusService.CompilationStatus("success", 0, 0)
        assertEquals(s1, s2)
    }

    @Test
    fun `CompilationStatus preserves error and warning counts`() {
        val status = RescriptCompilationStatusService.CompilationStatus("error", 5, 10)
        assertEquals("error", status.status)
        assertEquals(5, status.errorCount)
        assertEquals(10, status.warningCount)
    }
}
