package com.rescript.plugin.lsp

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.lsp.api.LspServerSupportProvider
import com.rescript.plugin.util.RescriptFileUtil

// LspServerSupportProvider and its members are deprecated in 2026.2 EAP; the replacement
// LspClientSupportProvider API does not exist on the 2026.1.2 compile target.

/**
 * Triggers the ReScript LSP server when a .res or .resi file is opened.
 */
@Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")
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
