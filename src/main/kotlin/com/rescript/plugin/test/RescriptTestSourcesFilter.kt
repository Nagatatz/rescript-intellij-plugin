package com.rescript.plugin.test

import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.TestSourcesFilter
import com.intellij.openapi.vfs.VirtualFile

/**
 * Marks ReScript test files and directories as test sources.
 *
 * Recognizes files matching `*_test.res`, `*.test.res` (and their `.resi` variants),
 * as well as any file under a `__tests__` directory. This enables the IDE to apply
 * test-specific icons, search scopes, and other behaviors.
 */
class RescriptTestSourcesFilter : TestSourcesFilter() {
    companion object {
        private val TEST_FILE_SUFFIXES = listOf("_test.res", ".test.res", "_test.resi", ".test.resi")
        private const val TEST_DIR_NAME = "__tests__"
    }

    override fun isTestSource(
        file: VirtualFile,
        project: Project,
    ): Boolean {
        val fileName = file.name

        // Check file name patterns
        if (TEST_FILE_SUFFIXES.any { fileName.endsWith(it) }) {
            return true
        }

        // Check if any ancestor directory is __tests__
        var parent = file.parent
        while (parent != null) {
            if (parent.name == TEST_DIR_NAME) {
                return true
            }
            parent = parent.parent
        }

        return false
    }
}
