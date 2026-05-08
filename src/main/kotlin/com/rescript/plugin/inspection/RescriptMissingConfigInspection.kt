package com.rescript.plugin.inspection

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import com.rescript.plugin.lang.psi.RescriptFile
import com.rescript.plugin.lsp.RescriptWorkspaceDiscovery

/**
 * Local inspection that warns when neither `rescript.json` nor `bsconfig.json`
 * is found anywhere in the project — checked via [RescriptWorkspaceDiscovery]
 * so monorepo layouts (pnpm/npm/yarn workspaces), depth-limited subdirectory
 * scans, and the manual `packageRoots` override are all honoured.
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
                val layout = RescriptWorkspaceDiscovery.discover(project)
                if (layout.isRescriptProject()) return

                @Suppress("DialogTitleCapitalization")
                holder.registerProblem(
                    file,
                    "rescript.json not found in this project. LSP features may not work correctly.",
                )
            }
        }
}
