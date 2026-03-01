package com.rescript.plugin.config

import com.intellij.framework.FrameworkType
import com.intellij.framework.detection.DetectedFrameworkDescription
import com.intellij.framework.detection.FileContentPattern
import com.intellij.framework.detection.FrameworkDetectionContext
import com.intellij.framework.detection.FrameworkDetector
import com.intellij.json.JsonFileType
import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.patterns.ElementPattern
import com.intellij.util.indexing.FileContent

/**
 * Detects ReScript projects by looking for `rescript.json` files.
 *
 * When a `rescript.json` file is found in the project, the IDE will
 * suggest configuring the project as a ReScript framework.
 *
 * Extends [FrameworkDetector] for the IntelliJ framework detection system.
 *
 * @see RescriptFrameworkType
 */
class RescriptFrameworkDetector : FrameworkDetector("rescript") {
    override fun getFileType(): FileType = JsonFileType.INSTANCE

    override fun createSuitableFilePattern(): ElementPattern<FileContent> =
        FileContentPattern.fileContent().withName("rescript.json")

    override fun getFrameworkType(): FrameworkType = RescriptFrameworkType.INSTANCE

    override fun detect(
        newFiles: MutableCollection<out VirtualFile>,
        context: FrameworkDetectionContext,
    ): MutableList<out DetectedFrameworkDescription> = mutableListOf()
}
