package com.rescript.plugin.generate

import com.intellij.openapi.actionSystem.ActionGroup
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.rescript.plugin.lang.psi.RescriptFile

/**
 * Action group for the Generate menu (Alt+Insert) in ReScript files.
 *
 * Provides Switch Arms, Module Type, Make Function, and JSON Encoder/Decoder
 * generation actions. Only visible when the active file is a [RescriptFile].
 */
class RescriptGenerateGroup : ActionGroup() {
    private val actions: Array<AnAction> =
        arrayOf(
            RescriptGenerateSwitchAction(),
            RescriptGenerateModuleTypeAction(),
            RescriptGenerateMakeAction(),
            RescriptGenerateJsonCodecAction(),
        )

    override fun getChildren(e: AnActionEvent?): Array<AnAction> = actions

    override fun update(e: AnActionEvent) {
        val psiFile = e.getData(CommonDataKeys.PSI_FILE)
        e.presentation.isEnabledAndVisible = psiFile is RescriptFile
    }
}
