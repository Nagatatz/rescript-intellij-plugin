package com.rescript.plugin.run

import java.nio.file.Files
import java.nio.file.Path

object RescriptCliDetector {
    private const val BIN_NAME = "rescript"

    fun findCli(
        workingDirectory: String?,
        projectBasePath: String?,
    ): String? =
        findFromDirectory(workingDirectory)
            ?: findFromDirectory(projectBasePath)
            ?: findInParentDirectories(projectBasePath)

    private fun findFromDirectory(directory: String?): String? {
        if (directory == null) return null
        return findInNodeModulesBin(Path.of(directory))
    }

    private fun findInParentDirectories(basePath: String?): String? {
        var dir = basePath?.let { Path.of(it).parent }
        while (dir != null) {
            findInNodeModulesBin(dir)?.let { return it }
            dir = dir.parent
        }
        return null
    }

    private fun findInNodeModulesBin(base: Path): String? {
        val bin = base.resolve("node_modules/.bin/$BIN_NAME")
        if (Files.isExecutable(bin)) return bin.toString()
        return null
    }
}
