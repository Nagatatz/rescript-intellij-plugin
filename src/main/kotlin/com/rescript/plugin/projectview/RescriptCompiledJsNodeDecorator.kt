package com.rescript.plugin.projectview

import com.intellij.ide.projectView.PresentationData
import com.intellij.ide.projectView.ProjectViewNode
import com.intellij.ide.projectView.ProjectViewNodeDecorator
import com.intellij.ui.JBColor
import java.awt.Color

/**
 * Applies gray text color to compiled `.res.js` and `.resi.js` files
 * in the Project tool window.
 *
 * Uses [PresentationData.forcedTextForeground] to override VCS coloring
 * (e.g., orange for untracked files), making generated files visually subdued.
 *
 * @see RescriptFileNestingProvider for file nesting rules
 */
class RescriptCompiledJsNodeDecorator : ProjectViewNodeDecorator {
    companion object {
        // Gray color appropriate for both light and dark themes
        @JvmField
        val GRAY_COLOR: JBColor = JBColor(Color(153, 153, 153), Color(128, 128, 128))

        /**
         * Returns true if the given file name represents a compiled ReScript JS output.
         */
        @JvmStatic
        fun isCompiledJsFile(fileName: String): Boolean = fileName.endsWith(".res.js") || fileName.endsWith(".resi.js")
    }

    override fun decorate(
        node: ProjectViewNode<*>,
        data: PresentationData,
    ) {
        val virtualFile = node.virtualFile ?: return
        if (isCompiledJsFile(virtualFile.name)) {
            data.forcedTextForeground = GRAY_COLOR
        }
    }
}
