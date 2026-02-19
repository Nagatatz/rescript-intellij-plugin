package com.rescript.plugin.test

import com.intellij.execution.Location
import com.intellij.execution.PsiLocation
import com.intellij.execution.testframework.sm.runner.SMTestLocator
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.psi.PsiManager
import com.intellij.psi.search.GlobalSearchScope

class RescriptTestLocator : SMTestLocator {
    companion object {
        val INSTANCE = RescriptTestLocator()
        private const val PROTOCOL_ID = "file"
        private val JS_SUFFIXES = listOf(".bs.js", ".mjs", ".js")
        private val LIB_JS_PREFIX_PATTERN = Regex("^lib/(?:js|es6|bs)/")

        /**
         * Converts a compiled JS path to the corresponding .res source path.
         * e.g., "lib/js/src/MyTest.bs.js" -> "src/MyTest.res"
         *
         * Visible for testing.
         */
        internal fun compiledJsToResPath(jsPath: String): String? {
            // Must have lib/js/ (or lib/es6/, lib/bs/) prefix
            if (!LIB_JS_PREFIX_PATTERN.containsMatchIn(jsPath)) return null

            // Strip prefix
            val withoutPrefix = LIB_JS_PREFIX_PATTERN.replaceFirst(jsPath, "")

            // Strip JS suffix and add .res
            for (suffix in JS_SUFFIXES) {
                if (withoutPrefix.endsWith(suffix)) {
                    return withoutPrefix.removeSuffix(suffix) + ".res"
                }
            }
            return null
        }
    }

    override fun getLocation(
        protocol: String,
        path: String,
        project: Project,
        scope: GlobalSearchScope,
    ): List<Location<*>> {
        if (protocol != PROTOCOL_ID) return emptyList()

        val projectDir = project.guessProjectDir() ?: return emptyList()

        // Try direct path first
        val directFile = projectDir.findFileByRelativePath(path)
        if (directFile != null) {
            val psiFile = PsiManager.getInstance(project).findFile(directFile)
            if (psiFile != null) return listOf(PsiLocation(psiFile))
        }

        // Try converting compiled JS path to .res
        val resPath = compiledJsToResPath(path)
        if (resPath != null) {
            val resFile = projectDir.findFileByRelativePath(resPath)
            if (resFile != null) {
                val psiFile = PsiManager.getInstance(project).findFile(resFile)
                if (psiFile != null) return listOf(PsiLocation(psiFile))
            }
        }

        // Try finding .res file by matching the absolute path
        val absoluteFile = VfsUtil.findFileByURL(java.net.URI("file://$path").toURL())
        if (absoluteFile != null) {
            val psiFile = PsiManager.getInstance(project).findFile(absoluteFile)
            if (psiFile != null) return listOf(PsiLocation(psiFile))
        }

        return emptyList()
    }
}
