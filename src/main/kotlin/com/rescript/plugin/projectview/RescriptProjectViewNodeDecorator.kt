package com.rescript.plugin.projectview

import com.intellij.ide.projectView.PresentationData
import com.intellij.ide.projectView.ProjectViewNode
import com.intellij.ide.projectView.ProjectViewNodeDecorator
import com.rescript.plugin.util.RescriptFileUtil

/**
 * Decorates ReScript file nodes in the Project View with additional information.
 *
 * Adds visual indicators to help navigate the project structure:
 * - `.res` files with a corresponding `.resi` interface show "(has interface)" suffix
 * - `rescript.json` files show the ReScript version if available
 *
 * @see RescriptTreeStructureProvider for .resi file nesting
 */
class RescriptProjectViewNodeDecorator : ProjectViewNodeDecorator {
    override fun decorate(
        node: ProjectViewNode<*>,
        data: PresentationData,
    ) {
        val file = node.virtualFile ?: return

        when {
            RescriptFileUtil.isResFile(file) -> {
                // Show "(has interface)" suffix when a .resi file exists
                val resiFile = RescriptFileUtil.findInterfaceFile(file)
                if (resiFile != null) {
                    data.locationString = "has interface"
                }
            }
            file.name == "rescript.json" -> {
                // Show ReScript version from rescript.json
                try {
                    val content = String(file.contentsToByteArray(), Charsets.UTF_8)
                    val versionMatch = VERSION_PATTERN.find(content)
                    if (versionMatch != null) {
                        data.locationString = "v${versionMatch.groupValues[1]}"
                    }
                } catch (_: Exception) {
                    // Ignore read errors
                }
            }
        }
    }

    companion object {
        // Matches "version": "x.y.z" in rescript.json
        private val VERSION_PATTERN = Regex(""""version"\s*:\s*"([^"]+)"""")
    }
}
