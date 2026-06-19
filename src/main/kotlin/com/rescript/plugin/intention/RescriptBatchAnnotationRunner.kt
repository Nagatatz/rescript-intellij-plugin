package com.rescript.plugin.intention

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Document
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.VirtualFile
import com.rescript.plugin.narrowing.RescriptHoverTypeResolver

/**
 * Execution glue that drives a batch inferred-type annotation pass over a
 * single ReScript file.
 *
 * The pure planning logic lives in [RescriptBatchAnnotationPlanner]; this
 * object supplies the IDE-coupled half: it resolves each binding's type via
 * the LSP `textDocument/hover` request off the EDT (inside a cancelable
 * [Task.Backgroundable]), guards against concurrent edits by comparing the
 * document's modification stamp, and applies the resulting [
 * RescriptBatchAnnotationPlanner.Plan] in a single [WriteCommandAction] so
 * the whole batch undoes as one step.
 *
 * Both entry points — the editor [RescriptBatchInsertInferredTypesIntention]
 * and the Type Coverage Heat Map row action — funnel through [run].
 *
 * User-facing messages reference only [VirtualFile.getName]; absolute paths
 * are never surfaced, per the project's security guidelines.
 *
 * @see RescriptBatchAnnotationPlanner for the LSP-independent planning logic
 */
object RescriptBatchAnnotationRunner {
    private const val TITLE = "Insert Inferred Types"
    private const val COMMAND_NAME = "Insert Inferred Type Annotations"

    /**
     * Runs the batch annotation pass for [file], whose contents are backed by
     * [document].
     *
     * Collects the un-annotated top-level `let` bindings synchronously (cheap,
     * lexer-only), then — if any exist — resolves their types in the
     * background and applies the edits on the EDT. When no inferred bindings
     * remain, an informational dialog is shown instead of starting any work.
     *
     * @param project the project the file belongs to
     * @param document the editor document to mutate
     * @param file the virtual file backing [document]; used only for its name
     *   in user-facing messages and to bind the hover resolver
     */
    fun run(
        project: Project,
        document: Document,
        file: VirtualFile,
    ) {
        val text = document.text
        val candidates = RescriptBatchAnnotationPlanner.collectInferredLets(text)
        if (candidates.isEmpty()) {
            Messages.showInfoMessage(
                project,
                "${file.name} has no inferred top-level let bindings to annotate.",
                TITLE,
            )
            return
        }

        val stampBefore = document.modificationStamp
        val resolver = RescriptHoverTypeResolver.forFile(project, file)
        val total = candidates.size

        ProgressManager.getInstance().run(
            object : Task.Backgroundable(project, "Inferring ReScript types…", true) {
                override fun run(indicator: ProgressIndicator) {
                    indicator.isIndeterminate = false
                    var processed = 0
                    // The resolver makes a synchronous LSP hover call per
                    // binding; running it here keeps it off the EDT.
                    val plan =
                        RescriptBatchAnnotationPlanner.buildPlan(text) { offset ->
                            indicator.checkCanceled()
                            indicator.fraction = processed.toDouble() / total
                            processed++
                            resolver.resolveAt(offset)
                        }
                    ApplicationManager.getApplication().invokeLater {
                        applyPlan(project, document, file, plan, stampBefore)
                    }
                }
            },
        )
    }

    /**
     * Applies a resolved [plan] to [document] on the EDT, guarding against
     * edits made while the background hover pass was running.
     *
     * Aborts with a dialog when the plan produced no edits or when the
     * document changed since [stampBefore] was captured.
     */
    private fun applyPlan(
        project: Project,
        document: Document,
        file: VirtualFile,
        plan: RescriptBatchAnnotationPlanner.Plan,
        stampBefore: Long,
    ) {
        if (plan.edits.isEmpty()) {
            Messages.showInfoMessage(
                project,
                "No inferred types could be resolved for ${file.name}.",
                TITLE,
            )
            return
        }
        if (document.modificationStamp != stampBefore) {
            Messages.showErrorDialog(
                project,
                "${file.name} changed during analysis. No annotations were inserted — please retry.",
                TITLE,
            )
            return
        }
        WriteCommandAction.runWriteCommandAction(project, COMMAND_NAME, null, {
            // Edits are sorted descending by offset, so inserting front-to-back
            // never shifts a not-yet-applied (earlier) offset.
            for (edit in plan.edits) {
                document.insertString(edit.offset, edit.text)
            }
        })
        notifyResult(project, file, plan)
    }

    /** Surfaces a balloon summarizing how many bindings were annotated. */
    private fun notifyResult(
        project: Project,
        file: VirtualFile,
        plan: RescriptBatchAnnotationPlanner.Plan,
    ) {
        val skippedSuffix =
            if (plan.skippedCount > 0) " (${plan.skippedCount} skipped — type unavailable or not insertable)" else ""
        val message = "Annotated ${plan.annotatedCount} binding(s) in ${file.name}$skippedSuffix."
        NotificationGroupManager
            .getInstance()
            .getNotificationGroup("ReScript")
            .createNotification(message, NotificationType.INFORMATION)
            .notify(project)
    }
}
