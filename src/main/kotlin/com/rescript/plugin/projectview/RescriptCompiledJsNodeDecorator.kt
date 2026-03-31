package com.rescript.plugin.projectview

import com.intellij.ide.projectView.PresentationData
import com.intellij.ide.projectView.ProjectViewNode
import com.intellij.ide.projectView.ProjectViewNodeDecorator
import com.intellij.ui.JBColor
import java.awt.Color

/**
 * Applies gray text color to compiled ReScript JS output files
 * (`.res.js`, `.res.mjs`, `.res.cjs`, `.bs.js`, etc.) in the Project tool window.
 *
 * Uses [PresentationData.forcedTextForeground] to override VCS coloring
 * (e.g., orange for untracked files), making generated files visually subdued.
 *
 * @see RescriptTreeStructureProvider for file nesting in Project View
 */
class RescriptCompiledJsNodeDecorator : ProjectViewNodeDecorator {
    companion object {
        // Gray color appropriate for both light and dark themes
        @JvmField
        val GRAY_COLOR: JBColor = JBColor(Color(153, 153, 153), Color(128, 128, 128))

        // All suffixes that indicate a compiled ReScript JS output file.
        // Covers .res.js/.mjs/.cjs (default), .bs.js/.mjs/.cjs (legacy), and .resi variants.
        @JvmField
        val COMPILED_JS_SUFFIXES =
            listOf(
                ".res.js",
                ".res.mjs",
                ".res.cjs",
                ".resi.js",
                ".resi.mjs",
                ".resi.cjs",
                ".bs.js",
                ".bs.mjs",
                ".bs.cjs",
            )

        /**
         * Returns true if the given file name represents a compiled ReScript JS output.
         */
        @JvmStatic
        fun isCompiledJsFile(fileName: String): Boolean = COMPILED_JS_SUFFIXES.any { fileName.endsWith(it) }

        /**
         * Extracts the base name (without ReScript-related suffixes) from a compiled JS file name.
         * For example, `Demo.res.js` returns `Demo`, `Demo.bs.mjs` returns `Demo`.
         *
         * @param fileName the compiled JS file name
         * @return the base name, or null if the file is not a compiled JS file
         */
        @JvmStatic
        fun extractBaseName(fileName: String): String? {
            val suffix = COMPILED_JS_SUFFIXES.firstOrNull { fileName.endsWith(it) } ?: return null
            return fileName.removeSuffix(suffix)
        }
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
