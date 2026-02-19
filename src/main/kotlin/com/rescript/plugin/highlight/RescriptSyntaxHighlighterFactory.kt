package com.rescript.plugin.highlight

import com.intellij.openapi.fileTypes.SyntaxHighlighter
import com.intellij.openapi.fileTypes.SyntaxHighlighterFactory
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile

/**
 * Factory that creates [RescriptSyntaxHighlighter] instances for ReScript files.
 *
 * Registered as an extension point to provide syntax highlighting for `.res` and `.resi` files.
 */
class RescriptSyntaxHighlighterFactory : SyntaxHighlighterFactory() {
    override fun getSyntaxHighlighter(
        project: Project?,
        virtualFile: VirtualFile?,
    ): SyntaxHighlighter = RescriptSyntaxHighlighter()
}
