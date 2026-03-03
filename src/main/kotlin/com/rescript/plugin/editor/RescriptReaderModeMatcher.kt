package com.rescript.plugin.editor

import com.intellij.codeInsight.actions.ReaderModeMatcher
import com.intellij.codeInsight.actions.ReaderModeProvider
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.rescript.plugin.util.RescriptFileUtil

/**
 * Matches ReScript files inside `node_modules/` for reader mode display.
 *
 * When a `.res` or `.resi` file is opened from a `node_modules/` directory,
 * this matcher signals the IDE to display it in reader mode with enhanced
 * readability (larger font, adjusted line spacing, breadcrumbs hidden).
 * This helps distinguish library source from project source at a glance.
 */
class RescriptReaderModeMatcher : ReaderModeMatcher {
    override fun matches(
        project: Project,
        file: VirtualFile,
        editor: Editor?,
        mode: ReaderModeProvider.ReaderMode,
    ): Boolean {
        if (!RescriptFileUtil.isRescriptFile(file)) return false
        // Detect files located inside any node_modules directory
        return file.path.contains("/node_modules/")
    }
}
