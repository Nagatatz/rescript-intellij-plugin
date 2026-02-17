package com.rescript.plugin

import com.intellij.lang.Language

object RescriptLanguage : Language("ReScript") {
    override fun isCaseSensitive(): Boolean = true

    private fun readResolve(): Any = RescriptLanguage
}
