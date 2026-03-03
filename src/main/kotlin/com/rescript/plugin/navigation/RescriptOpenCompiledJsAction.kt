package com.rescript.plugin.navigation

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import com.rescript.plugin.lsp.RescriptLanguageServer
import com.rescript.plugin.lsp.RescriptLspUtils
import com.rescript.plugin.util.RescriptFileUtil
import com.rescript.plugin.util.RescriptSecurityUtils

/**
 * Action to open the compiled JavaScript file for the current ReScript file.
 *
 * Uses the LSP custom request `textDocument/openCompiled` when available,
 * falling back to searching `lib/js/` when LSP is not connected.
 */
class RescriptOpenCompiledJsAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val file = e.getData(CommonDataKeys.VIRTUAL_FILE) ?: return
        if (!RescriptFileUtil.isRescriptFile(file)) return

        val jsFile = tryOpenViaLsp(project, file) ?: findCompiledJsFile(project, file)
        if (jsFile != null) {
            FileEditorManager.getInstance(project).openFile(jsFile, true)
        } else {
            Messages.showInfoMessage(
                project,
                "Compile your project first to generate JavaScript output.",
                "Compiled File Not Found",
            )
        }
    }

    private fun tryOpenViaLsp(
        project: Project,
        file: VirtualFile,
    ): VirtualFile? {
        val lspServer = RescriptLspUtils.getServer(project) ?: return null

        return try {
            val textDocId = lspServer.getDocumentIdentifier(file)
            val result =
                lspServer.sendRequestSync(TIMEOUT_MS) { server ->
                    (server as RescriptLanguageServer).openCompiled(textDocId)
                }
            val resultUri = result?.uri ?: return null

            val vfsUrl = RescriptLspUtils.lspUriToVfsUrl(resultUri)
            val resolved = VirtualFileManager.getInstance().refreshAndFindFileByUrl(vfsUrl)

            // Validate the resolved file is within the project directory
            if (resolved != null && !RescriptSecurityUtils.isWithinProject(project, resolved)) {
                LOG.warn("LSP openCompiled returned a file outside the project scope, ignoring")
                return null
            }
            resolved
        } catch (ex: Exception) {
            LOG.info("LSP openCompiled request failed, falling back to file search", ex)
            null
        }
    }

    override fun update(e: AnActionEvent) {
        val file = e.getData(CommonDataKeys.VIRTUAL_FILE)
        e.presentation.isEnabledAndVisible = file != null && RescriptFileUtil.isRescriptFile(file)
    }

    override fun getActionUpdateThread() = ActionUpdateThread.BGT

    companion object {
        private val LOG = logger<RescriptOpenCompiledJsAction>()
        private const val TIMEOUT_MS = 10_000
        private val JS_SUFFIXES = listOf(".bs.js", ".mjs", ".js")

        /**
         * Finds the compiled JS file for a given ReScript source file.
         * Searches `lib/js/` under the project root with `.bs.js`, `.mjs`, `.js` suffixes.
         *
         * Visible for testing.
         */
        internal fun findCompiledJsFile(
            project: Project,
            file: VirtualFile,
        ): VirtualFile? {
            val projectDir = project.guessProjectDir() ?: return null
            val relativePath = VfsUtil.getRelativePath(file, projectDir) ?: return null

            val basePath = relativePath.removeSuffix(".resi").removeSuffix(".res")

            val libJsDir = projectDir.findFileByRelativePath("lib/js") ?: return null
            for (suffix in JS_SUFFIXES) {
                libJsDir.findFileByRelativePath("$basePath$suffix")?.let { return it }
            }
            return null
        }
    }
}
