package com.rescript.plugin.navigation

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.fileEditor.FileEditorManager

class RescriptSwitchFileAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val file = e.getData(CommonDataKeys.VIRTUAL_FILE) ?: return

        val ext = file.extension ?: return
        val targetExt =
            when (ext) {
                "res" -> "resi"
                "resi" -> "res"
                else -> return
            }

        val targetFile = file.parent?.findChild("${file.nameWithoutExtension}.$targetExt") ?: return
        FileEditorManager.getInstance(project).openFile(targetFile, true)
    }

    override fun update(e: AnActionEvent) {
        val file = e.getData(CommonDataKeys.VIRTUAL_FILE)
        val ext = file?.extension
        e.presentation.isEnabledAndVisible = ext == "res" || ext == "resi"
    }

    override fun getActionUpdateThread() = com.intellij.openapi.actionSystem.ActionUpdateThread.BGT
}
