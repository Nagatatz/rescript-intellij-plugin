package com.rescript.plugin.intention

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.rescript.plugin.lsp.RescriptLspUtils
import com.rescript.plugin.util.RescriptEditorUtils.insertInWriteAction

/**
 * Outcome of diagnosing whether `Add missing switch arms` can apply at the
 * current caret. Each non-[Ready] variant carries enough information for
 * [RescriptAddMissingArmsDiagnoser.messageFor] to produce a user-facing
 * explanation, so the intention never silently no-ops.
 */
internal sealed class ArmsOutcome {
    /** Caret is not inside a `switch` expression. Should be filtered out by `isAvailable`. */
    object NotInSwitch : ArmsOutcome()

    /** The scrutinee text could not be located, so no hover query is possible. */
    object NoScrutinee : ArmsOutcome()

    /** The ReScript LSP server is not currently running for this project. */
    object LspUnavailable : ArmsOutcome()

    /** LSP hover returned no type string for the scrutinee offset. */
    object HoverEmpty : ArmsOutcome()

    /** Hover succeeded but the type was not a variant we can enumerate. */
    data class NotVariant(
        val typeText: String,
    ) : ArmsOutcome()

    /** Every constructor already has an arm (or the switch is malformed). */
    object NoMissingArms : ArmsOutcome()

    /** The intention can be applied — carries the computed edit. */
    data class Ready(
        val result: MissingArmsResult,
    ) : ArmsOutcome()
}

/**
 * Pure helper that decides whether `Add missing switch arms` should produce
 * an edit, fall through quietly, or surface a diagnostic to the user.
 *
 * Split out of [RescriptAddMissingSwitchArmsIntention] so the branching
 * logic — including the LSP hover and constructor parsing wiring — can be
 * exercised in unit tests without an IDE fixture or a running LSP server.
 *
 * @see RescriptMissingArmsBuilder for the underlying switch-arm walker
 */
internal object RescriptAddMissingArmsDiagnoser {
    /**
     * Decide what should happen for a caret at [offset] in [source].
     *
     * @param lspServerAvailable whether a ReScript LSP server is up; the
     *   intention requires hover, so we surface a dedicated message if not
     * @param hoverProbe pulls the hover type string for an offset; this is
     *   the same contract as `RescriptLspUtils.getHoverType`, but injected
     *   so tests can substitute a constant lambda
     */
    fun diagnose(
        source: String,
        offset: Int,
        lspServerAvailable: Boolean,
        hoverProbe: (Int) -> String?,
    ): ArmsOutcome {
        if (!RescriptMissingArmsBuilder.isInsideSwitch(source, offset)) return ArmsOutcome.NotInSwitch
        if (!lspServerAvailable) return ArmsOutcome.LspUnavailable
        val scrutineeOffset =
            RescriptMissingArmsBuilder.scrutineeOffset(source, offset)
                ?: return ArmsOutcome.NoScrutinee
        val typeText = hoverProbe(scrutineeOffset) ?: return ArmsOutcome.HoverEmpty
        val constructors = RescriptLspUtils.parseVariantConstructors(typeText)
        if (constructors.isEmpty()) return ArmsOutcome.NotVariant(typeText.trim())
        val result =
            RescriptMissingArmsBuilder.computeMissing(source, offset, constructors)
                ?: return ArmsOutcome.NoMissingArms
        return ArmsOutcome.Ready(result)
    }

    /**
     * Returns the user-visible message for non-success outcomes, or `null`
     * for [ArmsOutcome.NotInSwitch] (which should already have been filtered
     * by `isAvailable`) and [ArmsOutcome.Ready] (where the edit succeeded).
     */
    fun messageFor(outcome: ArmsOutcome): String? =
        when (outcome) {
            ArmsOutcome.NotInSwitch -> null
            is ArmsOutcome.Ready -> null
            ArmsOutcome.NoScrutinee -> "Add missing switch arms: could not locate the switch scrutinee."
            ArmsOutcome.LspUnavailable -> LSP_UNAVAILABLE_MESSAGE
            ArmsOutcome.HoverEmpty -> "Add missing switch arms: could not infer the scrutinee type from LSP hover."
            is ArmsOutcome.NotVariant -> notVariantMessage(outcome.typeText)
            ArmsOutcome.NoMissingArms -> NO_MISSING_ARMS_MESSAGE
        }

    private const val LSP_UNAVAILABLE_MESSAGE =
        "Add missing switch arms: ReScript LSP server is not running. Start the language server and retry."

    private const val NO_MISSING_ARMS_MESSAGE =
        "Add missing switch arms: no missing constructors — the switch is already exhaustive."

    private fun notVariantMessage(typeText: String): String =
        "Add missing switch arms: scrutinee type is not a variant (got `$typeText`)."
}

/**
 * Intention action that fills the arms missing from a partial `switch`
 * expression. Triggered with Alt+Enter while the caret is on the `switch`
 * keyword or somewhere inside the scrutinee.
 *
 * The intention queries LSP hover for the scrutinee's type, parses the
 * variant constructors, computes the set difference against the constructors
 * already covered by existing arms, and inserts a skeleton arm for each
 * missing constructor (`| Name(_) => todo` or `| Name => todo`) just before
 * the closing `}` of the switch.
 *
 * Logic-free wrapper around [RescriptAddMissingArmsDiagnoser] and
 * [RescriptMissingArmsBuilder] — everything that does not require an
 * editor/document round-trip lives in those helpers so the branching is
 * testable without an IDE fixture. When the diagnosis is anything other
 * than [ArmsOutcome.Ready], a `ReScript` notification explains why no edit
 * was applied, so the action never silently no-ops.
 *
 * @see RescriptMissingArmsBuilder
 * @see RescriptAddMissingArmsDiagnoser
 * @see RescriptLspUtils.parseVariantConstructors
 */
class RescriptAddMissingSwitchArmsIntention : RescriptBaseIntention() {
    override fun getText(): String = "Add missing switch arms"

    override fun isAvailableInRescript(
        project: Project,
        editor: Editor?,
        element: PsiElement,
    ): Boolean {
        val doc = editor?.document ?: return false
        val source = doc.text
        val offset = element.textRange.startOffset
        if (!RescriptMissingArmsBuilder.isInsideSwitch(source, offset)) return false
        if (RescriptMissingArmsBuilder.hasWildcardArm(source, offset)) return false
        return true
    }

    override fun invoke(
        project: Project,
        editor: Editor?,
        element: PsiElement,
    ) {
        editor ?: return
        val doc = editor.document
        val source = doc.text
        val offset = element.textRange.startOffset
        val virtualFile = element.containingFile?.virtualFile ?: return

        val outcome =
            RescriptAddMissingArmsDiagnoser.diagnose(
                source = source,
                offset = offset,
                lspServerAvailable = RescriptLspUtils.getServer(project) != null,
                hoverProbe = { ofs -> RescriptLspUtils.getHoverType(project, virtualFile, ofs) },
            )

        if (outcome is ArmsOutcome.Ready) {
            doc.insertInWriteAction(project, outcome.result.insertOffset, outcome.result.insertText)
        } else {
            RescriptAddMissingArmsDiagnoser.messageFor(outcome)?.let { notify(project, it) }
        }
    }

    private fun notify(
        project: Project,
        message: String,
    ) {
        NotificationGroupManager
            .getInstance()
            .getNotificationGroup("ReScript")
            .createNotification(message, NotificationType.WARNING)
            .notify(project)
    }
}
