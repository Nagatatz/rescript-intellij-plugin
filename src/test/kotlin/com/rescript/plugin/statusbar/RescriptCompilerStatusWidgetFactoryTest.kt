package com.rescript.plugin.statusbar

import com.rescript.plugin.lsp.RescriptCompilationStatusService.CompilationStatus
import com.rescript.plugin.statusbar.RescriptCompilerStatusWidgetFactory.RescriptCompilerStatusWidget
import org.junit.Assert.assertEquals
import org.junit.Test

class RescriptCompilerStatusWidgetFactoryTest {
    @Test
    fun testFactoryId() {
        val factory = RescriptCompilerStatusWidgetFactory()
        assertEquals("RescriptCompilerStatus", factory.id)
    }

    @Test
    fun testFactoryDisplayName() {
        val factory = RescriptCompilerStatusWidgetFactory()
        assertEquals("ReScript Compiler Status", factory.displayName)
    }

    // ── formatText tests ──

    @Test
    fun testTextCompiling() {
        val status = CompilationStatus("compiling", 0, 0)
        assertEquals("ReScript: Compiling...", RescriptCompilerStatusWidget.formatText(status))
    }

    @Test
    fun testTextSuccess() {
        val status = CompilationStatus("success", 0, 0)
        assertEquals("ReScript: \u2713", RescriptCompilerStatusWidget.formatText(status))
    }

    @Test
    fun testTextSingleError() {
        val status = CompilationStatus("error", 1, 0)
        assertEquals("ReScript: 1 error", RescriptCompilerStatusWidget.formatText(status))
    }

    @Test
    fun testTextMultipleErrors() {
        val status = CompilationStatus("error", 5, 0)
        assertEquals("ReScript: 5 errors", RescriptCompilerStatusWidget.formatText(status))
    }

    @Test
    fun testTextSingleWarning() {
        val status = CompilationStatus("warning", 0, 1)
        assertEquals("ReScript: 1 warning", RescriptCompilerStatusWidget.formatText(status))
    }

    @Test
    fun testTextMultipleWarnings() {
        val status = CompilationStatus("warning", 0, 3)
        assertEquals("ReScript: 3 warnings", RescriptCompilerStatusWidget.formatText(status))
    }

    @Test
    fun testTextUnknownStatus() {
        val status = CompilationStatus("unknown", 0, 0)
        assertEquals("ReScript", RescriptCompilerStatusWidget.formatText(status))
    }

    // ── formatTooltip tests ──

    @Test
    fun testTooltipCompiling() {
        val status = CompilationStatus("compiling", 0, 0)
        assertEquals("ReScript compiler is running...", RescriptCompilerStatusWidget.formatTooltip(status))
    }

    @Test
    fun testTooltipSuccess() {
        val status = CompilationStatus("success", 0, 0)
        assertEquals("ReScript compilation succeeded", RescriptCompilerStatusWidget.formatTooltip(status))
    }

    @Test
    fun testTooltipErrorOnly() {
        val status = CompilationStatus("error", 3, 0)
        assertEquals("ReScript compilation failed: 3 errors", RescriptCompilerStatusWidget.formatTooltip(status))
    }

    @Test
    fun testTooltipErrorAndWarning() {
        val status = CompilationStatus("error", 2, 1)
        assertEquals(
            "ReScript compilation failed: 2 errors, 1 warning",
            RescriptCompilerStatusWidget.formatTooltip(status),
        )
    }

    @Test
    fun testTooltipWarning() {
        val status = CompilationStatus("warning", 0, 4)
        assertEquals(
            "ReScript compilation completed with 4 warnings",
            RescriptCompilerStatusWidget.formatTooltip(status),
        )
    }

    @Test
    fun testTooltipSingleWarning() {
        val status = CompilationStatus("warning", 0, 1)
        assertEquals(
            "ReScript compilation completed with 1 warning",
            RescriptCompilerStatusWidget.formatTooltip(status),
        )
    }

    @Test
    fun testTooltipUnknown() {
        val status = CompilationStatus("unknown", 0, 0)
        assertEquals("ReScript compiler status unknown", RescriptCompilerStatusWidget.formatTooltip(status))
    }
}
