package com.rescript.plugin.test

import com.intellij.execution.lineMarker.ExecutorAction
import com.intellij.execution.lineMarker.RunLineMarkerContributor
import com.intellij.icons.AllIcons
import com.intellij.psi.PsiElement
import com.rescript.plugin.RescriptFileType
import com.rescript.plugin.lang.RescriptTokenTypes

/**
 * Adds Run/Debug gutter icons to `describe` / `it` / `test` calls in ReScript test files.
 *
 * Implements the IntelliJ Platform [RunLineMarkerContributor] extension point. The gutter
 * markers carry [ExecutorAction.getActions] so the platform bridges them to a
 * [com.intellij.execution.actions.ConfigurationContext], which [RescriptTestConfigurationProducer]
 * turns into a `-t <name>`-filtered [RescriptTestRunConfiguration]. The marker is shown only on the
 * function-name identifier leaf of a call recognized by [RescriptTestCallDetector], and only in
 * files whose name matches a test naming convention; detection is pure-syntax and LSP-independent.
 *
 * @see RescriptTestCallDetector for the call recognition logic
 */
class RescriptTestRunLineMarkerContributor : RunLineMarkerContributor() {
    override fun getInfo(element: PsiElement): Info? {
        // Only the leaf LIDENT token whose text is a framework function name can anchor a marker.
        if (element.node?.elementType != RescriptTokenTypes.LIDENT) return null
        if (element.text !in TEST_FUNCTION_NAMES) return null

        val file = element.containingFile ?: return null
        if (file.fileType != RescriptFileType) return null
        if (!RescriptTestConfigurationProducer.isTestFileName(file.viewProvider.virtualFile.nameWithoutExtension)) {
            return null
        }

        // Confirm this identifier actually begins a recognized test call by matching its offset.
        val offset = element.textRange.startOffset
        val isTestCall = RescriptTestCallDetector.detect(file.text).any { it.callOffset == offset }
        if (!isTestCall) return null

        val actions = ExecutorAction.getActions(0)
        return Info(
            AllIcons.RunConfigurations.TestState.Run,
            actions,
        ) { "Run Test" }
    }

    companion object {
        private val TEST_FUNCTION_NAMES = setOf("describe", "it", "test")
    }
}
