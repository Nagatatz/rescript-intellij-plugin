package com.rescript.plugin.navigation

import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.impl.include.FileIncludeInfo
import com.intellij.psi.impl.include.FileIncludeProvider
import com.intellij.util.Consumer
import com.intellij.util.indexing.FileContent
import com.rescript.plugin.RescriptFileType
import com.rescript.plugin.RescriptInterfaceFileType
import com.rescript.plugin.util.RescriptFileUtil
import com.rescript.plugin.util.RescriptRegexPatterns

/**
 * Recognizes file include relationships from `open` statements in ReScript files.
 *
 * Scans file text for `open ModuleName` patterns and converts module names to
 * file paths (e.g., `Belt.Array` -> `Array.res`), enabling the File Include Manager
 * to provide cross-file navigation. Implements [FileIncludeProvider].
 */
class RescriptFileIncludeProvider : FileIncludeProvider() {
    companion object {
        /**
         * Extracts module names from `open` statements in the given text.
         *
         * @param text the file content to scan
         * @return list of module names found in `open` statements
         */
        internal fun extractModuleNames(text: String): List<String> =
            RescriptRegexPatterns.OPEN_MODULE_STRICT
                .findAll(text)
                .map { it.groupValues[1] }
                .toList()

        /**
         * Converts a module name to the corresponding file name.
         *
         * Takes the last segment of a dotted module path (e.g., `Belt.Array` -> `Array.res`).
         *
         * @param moduleName the full module name
         * @return the corresponding file name with `.res` extension
         */
        internal fun moduleNameToFileName(moduleName: String): String {
            val lastSegment = moduleName.substringAfterLast(".")
            return "$lastSegment.res"
        }
    }

    override fun getId(): String = "rescript"

    // Deprecated on 2026.2 in favour of acceptFile(IndexedFile), but this overload is
    // still abstract on 2026.1.x, which pluginSinceBuild admits — dropping it there
    // raises AbstractMethodError. Overriding it is also sufficient on 2026.2, where
    // acceptFile(IndexedFile) delegates through acceptFile(VirtualFile, Project) to here.
    @Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")
    override fun acceptFile(file: VirtualFile): Boolean = RescriptFileUtil.isRescriptFile(file)

    override fun registerFileTypesUsedForIndexing(fileTypeSink: Consumer<in FileType>) {
        fileTypeSink.consume(RescriptFileType)
        fileTypeSink.consume(RescriptInterfaceFileType)
    }

    override fun getIncludeInfos(content: FileContent): Array<FileIncludeInfo> {
        val text = content.contentAsText.toString()
        return extractModuleNames(text)
            .map { moduleName ->
                FileIncludeInfo(moduleNameToFileName(moduleName))
            }.toTypedArray()
    }
}
