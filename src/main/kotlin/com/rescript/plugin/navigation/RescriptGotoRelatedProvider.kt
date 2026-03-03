package com.rescript.plugin.navigation

import com.intellij.navigation.GotoRelatedItem
import com.intellij.navigation.GotoRelatedProvider
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.psi.PsiManager
import com.rescript.plugin.util.RescriptFileUtil

/**
 * Provides "Go to Related" items for ReScript files.
 *
 * From a `.res` file, offers navigation to:
 * - The corresponding `.resi` interface file
 * - The compiled JavaScript file under `lib/js/`
 *
 * From a `.resi` file, offers navigation to the corresponding `.res` source file.
 */
class RescriptGotoRelatedProvider : GotoRelatedProvider() {
    override fun getItems(context: DataContext): List<GotoRelatedItem> {
        val project = CommonDataKeys.PROJECT.getData(context) ?: return emptyList()
        val file = CommonDataKeys.VIRTUAL_FILE.getData(context) ?: return emptyList()
        if (!RescriptFileUtil.isRescriptFile(file)) return emptyList()

        val psiManager = PsiManager.getInstance(project)
        val items = mutableListOf<GotoRelatedItem>()

        when {
            RescriptFileUtil.isResFile(file) -> {
                // Find corresponding .resi file
                RescriptFileUtil.findInterfaceFile(file)?.let { resiFile ->
                    psiManager.findFile(resiFile)?.let { psiFile ->
                        items.add(GotoRelatedItem(psiFile, "ReScript"))
                    }
                }

                // Find generated JS files under lib/js/
                val projectDir = project.guessProjectDir() ?: return items
                val srcRelativePath =
                    VfsUtil.getRelativePath(file, projectDir)
                        ?: return items
                val jsRelativePath = srcRelativePath.removeSuffix(".res")

                val libJsDir = projectDir.findFileByRelativePath("lib/js")
                if (libJsDir != null) {
                    for (jsSuffix in listOf(".bs.js", ".mjs", ".js")) {
                        libJsDir.findFileByRelativePath("$jsRelativePath$jsSuffix")?.let { jsFile ->
                            psiManager.findFile(jsFile)?.let { psiFile ->
                                items.add(GotoRelatedItem(psiFile, "ReScript"))
                            }
                        }
                    }
                }
            }
            RescriptFileUtil.isResiFile(file) -> {
                // Find corresponding .res file
                RescriptFileUtil.findCounterpartFile(file)?.let { resFile ->
                    psiManager.findFile(resFile)?.let { psiFile ->
                        items.add(GotoRelatedItem(psiFile, "ReScript"))
                    }
                }
            }
        }

        return items
    }
}
