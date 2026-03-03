package com.rescript.plugin.lsp

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.lsp.api.LspServerSupportProvider
import com.rescript.plugin.util.RescriptFileUtil

/**
 * Triggers the ReScript LSP server when a .res or .resi file is opened.
 */
class RescriptLspServerSupportProvider : LspServerSupportProvider {
    override fun fileOpened(
        project: Project,
        file: VirtualFile,
        serverStarter: LspServerSupportProvider.LspServerStarter,
    ) {
        if (RescriptFileUtil.isRescriptFile(file)) {
            serverStarter.ensureServerStarted(RescriptLspServerDescriptor(project))
        }
    }
}
