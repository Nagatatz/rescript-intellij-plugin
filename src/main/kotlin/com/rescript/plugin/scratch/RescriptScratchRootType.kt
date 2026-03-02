package com.rescript.plugin.scratch

import com.intellij.ide.scratch.RootType
import com.intellij.ide.scratch.ScratchFileCreationHelper
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.project.Project
import com.rescript.plugin.RescriptLanguage

/**
 * Root type for ReScript scratch files in the IDE's Scratches and Consoles tree.
 *
 * Registers "ReScript" as a category under the Scratches panel so users can
 * create ad-hoc `.res` scratch files for quick experiments.
 *
 * @see RescriptScratchCreationHelper for the template text injected into new files
 */
class RescriptScratchRootType : RootType("rescript", "ReScript") {
    override fun substituteLanguage(
        project: Project,
        file: com.intellij.openapi.vfs.VirtualFile,
    ): com.intellij.lang.Language = RescriptLanguage
}

/**
 * Creation helper that provides the initial template for new ReScript scratch files.
 *
 * Injects a minimal boilerplate so the scratch file is immediately runnable
 * with the ReScript compiler.
 *
 * @see RescriptScratchRootType which registers this helper
 */
class RescriptScratchCreationHelper : ScratchFileCreationHelper() {
    override fun prepareText(
        project: Project,
        context: Context,
        dataContext: DataContext,
    ): Boolean {
        context.text = generateTemplate()
        context.language = RescriptLanguage
        return true
    }

    companion object {
        /** Default template for ReScript scratch files. */
        internal const val TEMPLATE_HEADER = "// ReScript Scratch File"
        internal const val TEMPLATE_BODY = "let result = \"Hello\"\nJs.log(result)"

        /**
         * Generates the initial template text for a ReScript scratch file.
         *
         * @return the template string
         */
        internal fun generateTemplate(): String = "$TEMPLATE_HEADER\n$TEMPLATE_BODY\n"
    }
}
