package com.rescript.plugin.inspection

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import com.rescript.plugin.lang.psi.RescriptFile
import com.rescript.plugin.util.RescriptPaths

/**
 * Local inspection that warns when neither `rescript.json` nor `bsconfig.json`
 * is found in the project root directory.
 *
 * Without a configuration file, the LSP server cannot start and most
 * IDE features (completion, diagnostics, etc.) will not function.
 */
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

                val hasRescriptJson = baseDir.findChild(RescriptPaths.RESCRIPT_JSON) != null
                val hasBsConfig = baseDir.findChild(RescriptPaths.BSCONFIG_JSON) != null

                if (!hasRescriptJson && !hasBsConfig) {
                    @Suppress("DialogTitleCapitalization")
                    holder.registerProblem(
                        file,
                        "rescript.json not found in project root. LSP features may not work correctly.",
                    )
                }
            }
        }
}
