package com.rescript.plugin.statusbar

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.openapi.wm.StatusBar
import com.intellij.openapi.wm.StatusBarWidget
import com.intellij.openapi.wm.StatusBarWidgetFactory
import com.rescript.plugin.lsp.RescriptCompilationStatusService
import com.rescript.plugin.lsp.RescriptCompilationStatusService.CompilationStatus
import com.rescript.plugin.util.RescriptPaths

/**
 * Factory for the status bar widget that displays the ReScript compiler status.
 *
 * Shows compilation state (compiling, success, error count, warning count) in the
 * IDE's status bar. Only available in projects that contain a `rescript.json` file.
 * Updates automatically via [RescriptCompilationStatusService] listeners.
 */
class RescriptCompilerStatusWidgetFactory : StatusBarWidgetFactory {
    companion object {
        const val WIDGET_ID = "RescriptCompilerStatus"
    }

    override fun getId(): String = WIDGET_ID

    override fun getDisplayName(): String = "ReScript Compiler Status"

    override fun isAvailable(project: Project): Boolean {
        val basePath = project.basePath ?: return false
        val vfm = VirtualFileManager.getInstance()
        return vfm.findFileByUrl("file://$basePath/${RescriptPaths.RESCRIPT_JSON}") != null
    }

    override fun createWidget(project: Project): StatusBarWidget = RescriptCompilerStatusWidget(project)

    /**
     * Status bar widget that displays the current ReScript compilation state.
     * Listens to [RescriptCompilationStatusService] for status updates.
     */
    class RescriptCompilerStatusWidget(
        private val project: Project,
    ) : StatusBarWidget,
        StatusBarWidget.TextPresentation {
        private var statusBar: StatusBar? = null

        override fun ID(): String = WIDGET_ID

        override fun install(statusBar: StatusBar) {
            this.statusBar = statusBar
            val service = RescriptCompilationStatusService.getInstance(project)
            service.addListener(this) {
                statusBar.updateWidget(WIDGET_ID)
            }
        }

        override fun dispose() {
            statusBar = null
        }

        override fun getPresentation(): StatusBarWidget.WidgetPresentation = this

        override fun getText(): String {
            val status = RescriptCompilationStatusService.getInstance(project).currentStatus
            return formatText(status)
        }

        override fun getTooltipText(): String {
            val status = RescriptCompilationStatusService.getInstance(project).currentStatus
            return formatTooltip(status)
        }

        override fun getAlignment(): Float = 0f

        companion object {
            fun formatText(status: CompilationStatus): String =
                when (status.status) {
                    "compiling" -> {
                        "ReScript: Compiling..."
                    }

                    "success" -> {
                        "ReScript: \u2713"
                    }

                    "error" -> {
                        val n = status.errorCount
                        "ReScript: $n error${if (n != 1) "s" else ""}"
                    }

                    "warning" -> {
                        val n = status.warningCount
                        "ReScript: $n warning${if (n != 1) "s" else ""}"
                    }

                    else -> {
                        "ReScript"
                    }
                }

            fun formatTooltip(status: CompilationStatus): String =
                when (status.status) {
                    "compiling" -> {
                        "ReScript compiler is running..."
                    }

                    "success" -> {
                        "ReScript compilation succeeded"
                    }

                    "error" -> {
                        val parts = mutableListOf<String>()
                        if (status.errorCount >
                            0
                        ) {
                            parts.add("${status.errorCount} error${if (status.errorCount != 1) "s" else ""}")
                        }
                        if (status.warningCount >
                            0
                        ) {
                            parts.add("${status.warningCount} warning${if (status.warningCount != 1) "s" else ""}")
                        }
                        "ReScript compilation failed: ${parts.joinToString(", ")}"
                    }

                    "warning" -> {
                        val n = status.warningCount
                        "ReScript compilation completed with $n warning${if (n != 1) "s" else ""}"
                    }

                    else -> {
                        "ReScript compiler status unknown"
                    }
                }
        }
    }
}
