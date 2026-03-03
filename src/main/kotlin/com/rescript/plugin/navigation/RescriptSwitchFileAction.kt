package com.rescript.plugin.navigation

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.fileEditor.FileEditorManager
import com.rescript.plugin.util.RescriptFileUtil

/**
 * Action to switch between a ReScript source file (.res) and its interface (.resi).
 *
 * Bound to Alt+O. Looks for the counterpart file in the same directory.
 */
class RescriptSwitchFileAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val file = e.getData(CommonDataKeys.VIRTUAL_FILE) ?: return

        val targetFile = RescriptFileUtil.findCounterpartFile(file) ?: return
        FileEditorManager.getInstance(project).openFile(targetFile, true)
    }

    override fun update(e: AnActionEvent) {
        val file = e.getData(CommonDataKeys.VIRTUAL_FILE)
        e.presentation.isEnabledAndVisible = file != null && RescriptFileUtil.isRescriptFile(file)
    }

    override fun getActionUpdateThread() = com.intellij.openapi.actionSystem.ActionUpdateThread.BGT
}
