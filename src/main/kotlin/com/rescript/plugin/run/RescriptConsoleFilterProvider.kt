package com.rescript.plugin.run

import com.intellij.execution.filters.ConsoleFilterProvider
import com.intellij.execution.filters.Filter
import com.intellij.execution.filters.OpenFileHyperlinkInfo
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFileManager
import java.io.File

/**
 * Registers the [RescriptConsoleFilter] for all console outputs in the project.
 */
class RescriptConsoleFilterProvider : ConsoleFilterProvider {
    override fun getDefaultFilters(project: Project): Array<Filter> = arrayOf(RescriptConsoleFilter(project))
}

/**
 * Console output filter that converts ReScript file paths (e.g., `src/App.res:10:5`)
 * into clickable hyperlinks that navigate to the referenced source location.
 */
class RescriptConsoleFilter(
    private val project: Project,
) : Filter {
    companion object {
        // Matches: path/to/file.res:10:5 or path/to/file.resi:10:5-15 etc.
        val FILE_LINE_COL_PATTERN: Regex =
            Regex("""((?:[A-Za-z]:[\\/])?[^\s:]+\.resi?):(\d+):(\d+)""")
    }

    override fun applyFilter(
        line: String,
        entireLength: Int,
    ): Filter.Result? {
        val match = FILE_LINE_COL_PATTERN.find(line) ?: return null

        val filePath = match.groupValues[1]
        val lineNumber = match.groupValues[2].toIntOrNull() ?: return null
        val column = match.groupValues[3].toIntOrNull() ?: return null

        val resolvedFile = resolveFile(filePath) ?: return null
        val virtualFile =
            VirtualFileManager
                .getInstance()
                .findFileByNioPath(resolvedFile.toPath()) ?: return null

        val lineStartOffset = entireLength - line.length
        val hyperlinkStart = lineStartOffset + match.range.first
        val hyperlinkEnd = lineStartOffset + match.range.last + 1

        val hyperlinkInfo =
            OpenFileHyperlinkInfo(
                project,
                virtualFile,
                lineNumber - 1, // 0-based
                column - 1, // 0-based
            )

        return Filter.Result(hyperlinkStart, hyperlinkEnd, hyperlinkInfo)
    }

    private fun resolveFile(filePath: String): File? {
        val file = File(filePath)
        if (file.isAbsolute && file.exists()) {
            return file
        }

        val basePath = project.basePath ?: return null
        val resolved = File(basePath, filePath)
        if (resolved.exists()) {
            return resolved
        }

        return null
    }
}
