package com.rescript.plugin.config

import com.intellij.ide.IconProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.rescript.plugin.RescriptIcons
import javax.swing.Icon

class RescriptJsonIconProvider : IconProvider() {
    override fun getIcon(
        element: PsiElement,
        flags: Int,
    ): Icon? {
        if (element is PsiFile && element.name in CONFIG_FILE_NAMES) {
            return RescriptIcons.CONFIG_FILE
        }
        return null
    }

    companion object {
        private val CONFIG_FILE_NAMES = setOf("rescript.json", "bsconfig.json")
    }
}
