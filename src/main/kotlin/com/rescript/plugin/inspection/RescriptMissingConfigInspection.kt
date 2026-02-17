package com.rescript.plugin.inspection

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import com.rescript.plugin.lang.psi.RescriptFile

class RescriptMissingConfigInspection : LocalInspectionTool() {
    override fun buildVisitor(
        holder: ProblemsHolder,
        isOnTheFly: Boolean,
    ): PsiElementVisitor =
        object : PsiElementVisitor() {
            override fun visitFile(file: PsiFile) {
                if (file !is RescriptFile) return

                val project = file.project
                val basePath = project.basePath ?: return
                val baseDir = LocalFileSystem.getInstance().findFileByPath(basePath) ?: return

                val hasRescriptJson = baseDir.findChild("rescript.json") != null
                val hasBsConfig = baseDir.findChild("bsconfig.json") != null

                if (!hasRescriptJson && !hasBsConfig) {
                    holder.registerProblem(
                        file,
                        "rescript.json not found in project root. LSP features may not work correctly.",
                    )
                }
            }
        }
}
