package com.rescript.plugin.errorlens

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.Inlay
import com.intellij.openapi.editor.impl.DocumentMarkupModel
import com.rescript.plugin.settings.RescriptProjectSettings

/**
 * Per-editor manager for Error Lens inlays that listens to daemon code
 * analyzer events and creates/removes inline diagnostic annotations.
 *
 * Subscribes to [DaemonCodeAnalyzer.DAEMON_EVENT_TOPIC] to detect when
 * highlighting analysis completes, then scans the document markup model
 * for diagnostic highlighters and creates after-line-end inlays.
 *
 * Handles same-line consolidation: if multiple diagnostics appear on the
 * same line, only the highest severity is displayed with a "(+N more)" suffix.
 *
 * @param editor the editor this manager is attached to
 * @param parentDisposable the disposable parent for inlay lifecycle
 *
 * @see RescriptErrorLensEditorListener for editor lifecycle management
 * @see RescriptErrorLensRenderer for the visual rendering
 */
class RescriptErrorLensManager(
    private val editor: Editor,
    private val parentDisposable: Disposable,
) {
    /**
     * Maps line numbers to the currently displayed inlay on that line.
     * Only one inlay per line is shown (the highest severity diagnostic).
     */
    private val lineInlayMap = mutableMapOf<Int, Inlay<*>>()

    /**
     * Installs this manager by subscribing to daemon code analyzer events.
     *
     * Must be called on the EDT. After installation, the manager will
     * automatically rebuild inlays when diagnostic highlighting completes.
     */
    fun install() {
        val project = editor.project ?: return

        project.messageBus
            .connect(parentDisposable)
            .subscribe(
                DaemonCodeAnalyzer.DAEMON_EVENT_TOPIC,
                object : DaemonCodeAnalyzer.DaemonListener {
                    override fun daemonFinished() {
                        ApplicationManager.getApplication().invokeLater({
                            if (!editor.isDisposed) {
                                refreshAllInlays()
                            }
                        }, ModalityState.any())
                    }
                },
            )

        // Process existing highlighters on install
        refreshAllInlays()
    }

    /**
     * Rebuilds all inlays by scanning the document markup model for
     * diagnostic highlighters.
     *
     * Clears all existing inlays first, then collects diagnostics
     * grouped by line number and creates one inlay per line showing
     * the highest severity message.
     */
    internal fun refreshAllInlays() {
        // Remove all existing inlays
        lineInlayMap.values.forEach { it.dispose() }
        lineInlayMap.clear()

        val project = editor.project ?: return
        val settings = RescriptProjectSettings.getInstance(project)
        if (!settings.errorLensEnabled) return

        val markupModel =
            DocumentMarkupModel.forDocument(editor.document, project, false) ?: return

        // Group diagnostics by line
        val diagnosticsByLine = mutableMapOf<Int, MutableList<RescriptErrorLensHighlighterInfo>>()

        for (highlighter in markupModel.allHighlighters) {
            val info = RescriptErrorLensHighlighterInfo.fromHighlighter(highlighter) ?: continue
            if (!RescriptErrorLensSeverity.meetsMinimum(info.severity, settings.errorLensMinSeverity)) continue
            diagnosticsByLine.getOrPut(info.line) { mutableListOf() }.add(info)
        }

        // Create one inlay per line
        for ((line, diagnostics) in diagnosticsByLine) {
            createInlayForLine(line, diagnostics)
        }
    }

    /**
     * Creates an after-line-end inlay for a specific line showing the
     * highest severity diagnostic message.
     *
     * @param line the zero-based line number
     * @param diagnostics all diagnostics on this line
     */
    private fun createInlayForLine(
        line: Int,
        diagnostics: List<RescriptErrorLensHighlighterInfo>,
    ) {
        val (displayMessage, primarySeverity) = buildDisplayData(diagnostics)

        val renderer = RescriptErrorLensRenderer(displayMessage, primarySeverity)

        val document = editor.document
        if (line >= document.lineCount) return
        val lineEndOffset = document.getLineEndOffset(line)

        val inlay =
            editor.inlayModel.addAfterLineEndElement(
                lineEndOffset,
                false,
                renderer,
            )
        if (inlay != null) {
            lineInlayMap[line] = inlay
        }
    }

    /**
     * Disposes all managed inlays and clears internal state.
     * Called when the editor is closed or Error Lens is disabled.
     */
    fun dispose() {
        lineInlayMap.values.forEach { it.dispose() }
        lineInlayMap.clear()
    }

    companion object {
        /**
         * Builds the display data for a set of diagnostics on the same line.
         *
         * Selects the highest severity diagnostic as the primary message and
         * appends a "(+N more)" suffix when multiple diagnostics are present.
         *
         * @param diagnostics all diagnostics on a single line (must not be empty)
         * @return a pair of (displayMessage, primarySeverity)
         */
        internal fun buildDisplayData(
            diagnostics: List<RescriptErrorLensHighlighterInfo>,
        ): Pair<String, HighlightSeverity> {
            require(diagnostics.isNotEmpty()) { "diagnostics must not be empty" }

            val sorted = diagnostics.sortedByDescending { it.severity }
            val primary = sorted.first()
            val extraCount = sorted.size - 1

            val displayMessage =
                if (extraCount > 0) {
                    "${primary.message} (+$extraCount more)"
                } else {
                    primary.message
                }

            return Pair(displayMessage, primary.severity)
        }
    }
}
