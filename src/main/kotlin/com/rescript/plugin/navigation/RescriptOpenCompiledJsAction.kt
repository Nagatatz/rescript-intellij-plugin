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
import com.intellij.platform.lsp.api.LspServer
import com.intellij.platform.lsp.api.LspServerManager
import com.rescript.plugin.lsp.RescriptLanguageServer
import com.rescript.plugin.lsp.RescriptLspServerSupportProvider
import java.net.URI

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
        val ext = file.extension ?: return
        if (ext != "res" && ext != "resi") return

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
        val lspServer = findLspServer(project) ?: return null

        return try {
            val textDocId = lspServer.getDocumentIdentifier(file)
            val result =
                lspServer.sendRequestSync(TIMEOUT_MS) { server ->
                    (server as RescriptLanguageServer).openCompiled(textDocId)
                }
            val resultUri = result?.uri ?: return null

            val vfsUrl = lspUriToVfsUrl(resultUri)
            VirtualFileManager.getInstance().refreshAndFindFileByUrl(vfsUrl)
        } catch (ex: Exception) {
            LOG.info("LSP openCompiled request failed, falling back to file search", ex)
            null
        }
    }

    private fun findLspServer(project: Project): LspServer? {
        @Suppress("UnstableApiUsage")
        val servers =
            LspServerManager
                .getInstance(project)
                .getServersForProvider(RescriptLspServerSupportProvider::class.java)
        return servers.firstOrNull()
    }

    override fun update(e: AnActionEvent) {
        val file = e.getData(CommonDataKeys.VIRTUAL_FILE)
        val ext = file?.extension
        e.presentation.isEnabledAndVisible = ext == "res" || ext == "resi"
    }

    override fun getActionUpdateThread() = ActionUpdateThread.BGT

    /** Convert LSP file URI (file:///path) to IntelliJ VFS URL (file:///path). */
    private fun lspUriToVfsUrl(uri: String): String =
        try {
            val parsed = URI(uri)
            "file://${parsed.path}"
        } catch (_: Exception) {
            uri
        }

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
