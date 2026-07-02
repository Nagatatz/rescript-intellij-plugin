package com.rescript.plugin.narrowing

import com.intellij.codeInsight.hints.ChangeListener
import com.intellij.codeInsight.hints.ImmediateConfigurable
import com.intellij.codeInsight.hints.InlayHintsCollector
import com.intellij.codeInsight.hints.InlayHintsProvider
import com.intellij.codeInsight.hints.InlayHintsSink
import com.intellij.codeInsight.hints.NoSettings
import com.intellij.codeInsight.hints.SettingsKey
import com.intellij.codeInsight.hints.presentation.PresentationFactory
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.rescript.plugin.RescriptLanguage
import com.rescript.plugin.settings.RescriptProjectSettings

/**
 * Inlay-hint provider that displays the narrowed type at each `switch`
 * arm by calling the LSP `textDocument/hover` request at the scrutinee
 * offset and rendering the result inline next to the `=>` token.
 *
 * Implements IntelliJ Platform's [InlayHintsProvider]; can be toggled
 * from Settings > Editor > Inlay Hints > ReScript > Type narrowing or
 * via the project-level setting [RescriptProjectSettings.narrowingHintsEnabled].
 *
 * Switch-arm boundaries are produced by [RescriptSwitchArmCollector],
 * type strings by a [RescriptHoverTypeResolver], and final formatting
 * by [RescriptNarrowingPresenter]. Splitting the work across these
 * collaborators keeps each piece independently testable.
 *
 * Skips work entirely when the file is not ReScript, when the project
 * setting is off, or when more than [MAX_ARMS_PER_FILE] arms appear in
 * a single file (a soft cap to keep editor scrolling responsive).
 */
@Suppress("UnstableApiUsage")
class RescriptNarrowingHintProvider : InlayHintsProvider<NoSettings> {
    /**
     * One inlay produced for a single arm: where to anchor it and what
     * text to display. Returned by [buildHints] so that the rendering
     * code (which depends on [PresentationFactory] and [InlayHintsSink])
     * stays separate from the testable pure logic.
     *
     * @property offset offset to anchor the inline element at
     * @property displayText short type label rendered inside the badge
     */
    internal data class HintEntry(
        val offset: Int,
        val displayText: String,
    )

    override val key: SettingsKey<NoSettings> = SettingsKey("rescript.narrowing.hints")

    override val name: String = "Type narrowing in switch arms"

    override val previewText: String =
        """
        let describe = result =>
          switch result {
          | Ok(value) => value
          | Error(_) => "fallback"
          }
        """.trimIndent()

    override fun createSettings(): NoSettings = NoSettings()

    override fun createConfigurable(settings: NoSettings): ImmediateConfigurable =
        object : ImmediateConfigurable {
            override fun createComponent(listener: ChangeListener): javax.swing.JComponent = javax.swing.JPanel()
        }

    override fun getCollectorFor(
        file: PsiFile,
        editor: Editor,
        settings: NoSettings,
        sink: InlayHintsSink,
    ): InlayHintsCollector? {
        if (file.language != RescriptLanguage) return null
        val project = file.project
        if (!RescriptProjectSettings.getInstance(project).narrowingHintsEnabled) return null
        val virtualFile = file.virtualFile ?: return null

        val resolver = RescriptHoverTypeResolver.forFile(project, virtualFile)
        return object : InlayHintsCollector {
            override fun collect(
                element: PsiElement,
                editor: Editor,
                sink: InlayHintsSink,
            ): Boolean {
                if (element != file) return true
                val factory = PresentationFactory(editor)
                for (entry in buildHints(editor.document.text, resolver)) {
                    val presentation =
                        factory.roundWithBackground(
                            factory.smallText(": ${entry.displayText}"),
                        )
                    sink.addInlineElement(entry.offset, false, presentation, false)
                }
                return false
            }
        }
    }

    companion object {
        /**
         * Soft cap on the number of arms processed per file. Beyond
         * this point the cost of repeated LSP hover round-trips is no
         * longer offset by the readability gain, so we skip the file
         * rather than slow the editor.
         */
        internal const val MAX_ARMS_PER_FILE = 50

        /**
         * Hard budget on the total number of LSP hover requests issued per
         * inlay pass. The arm cap alone still allows 50 × (1 + N bindings)
         * synchronous round-trips; this ceiling bounds the worst case
         * regardless of how many bindings each arm introduces.
         */
        internal const val MAX_HOVER_REQUESTS_PER_FILE = 80

        /**
         * Pure logic that drives a single inlay pass: collect switch
         * arms in [text], resolve their narrowed types via [resolver],
         * format each result, and return the inlay anchor points to
         * paint.
         *
         * Exposed `internal` so unit tests can drive the entire
         * pipeline with an in-memory resolver, without a live LSP or
         * IntelliJ Platform fixture.
         *
         * @param text full text of the editor's document
         * @param resolver hover type resolver bound to the file
         * @return one [HintEntry] per arm that has a usable type
         */
        internal fun buildHints(
            text: String,
            resolver: RescriptHoverTypeResolver,
        ): List<HintEntry> {
            val arms = RescriptSwitchArmCollector.collect(text)
            if (arms.size > MAX_ARMS_PER_FILE) return emptyList()
            val out = mutableListOf<HintEntry>()
            // Bound the total hover round-trips per pass (see MAX_HOVER_REQUESTS_PER_FILE).
            var hoverBudget = MAX_HOVER_REQUESTS_PER_FILE
            for (arm in arms) {
                if (hoverBudget <= 0) break
                val typeText = resolver.resolveAt(arm.scrutineeRange.startOffset)
                hoverBudget--
                val display = RescriptNarrowingPresenter.format(typeText, arm.patternSummary)
                if (display != null) {
                    out.add(HintEntry(arm.arrowOffset, display))
                }
                // Phase 2: emit a separate hint for each pattern binding
                // (`Some(x)` → after `x`) so the narrowed payload type
                // is visible right next to the name the user just
                // introduced. Resolved independently per binding so
                // nested types (`Loaded(payload, error)`) get accurate
                // hints for each binding.
                for (bindingOffset in arm.bindingOffsets) {
                    if (hoverBudget <= 0) break
                    val bindingType = resolver.resolveAt(bindingOffset)
                    hoverBudget--
                    val bindingDisplay = RescriptNarrowingPresenter.format(bindingType, arm.patternSummary) ?: continue
                    out.add(HintEntry(bindingOffset, bindingDisplay))
                }
            }
            return out
        }
    }
}
