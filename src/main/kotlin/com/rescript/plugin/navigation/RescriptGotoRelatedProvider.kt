package com.rescript.plugin.navigation

import com.intellij.navigation.GotoRelatedItem
import com.intellij.navigation.GotoRelatedProvider
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.psi.PsiManager

class RescriptGotoRelatedProvider : GotoRelatedProvider() {
    override fun getItems(context: DataContext): List<GotoRelatedItem> {
        val project = CommonDataKeys.PROJECT.getData(context) ?: return emptyList()
        val file = CommonDataKeys.VIRTUAL_FILE.getData(context) ?: return emptyList()
        val ext = file.extension ?: return emptyList()
        if (ext != "res" && ext != "resi") return emptyList()

        val psiManager = PsiManager.getInstance(project)
        val items = mutableListOf<GotoRelatedItem>()
        val parent = file.parent ?: return emptyList()
        val nameWithoutExt = file.nameWithoutExtension

        when (ext) {
            "res" -> {
                // Find corresponding .resi file
                parent.findChild("$nameWithoutExt.resi")?.let { resiFile ->
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
            "resi" -> {
                // Find corresponding .res file
                parent.findChild("$nameWithoutExt.res")?.let { resFile ->
                    psiManager.findFile(resFile)?.let { psiFile ->
                        items.add(GotoRelatedItem(psiFile, "ReScript"))
                    }
                }
            }
        }

        return items
    }
}
