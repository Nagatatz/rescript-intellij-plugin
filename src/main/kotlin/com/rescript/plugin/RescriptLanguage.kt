package com.rescript.plugin

import com.intellij.lang.Language

object RescriptLanguage : Language("ReScript") {
    override fun getDisplayName(): String = "ReScript"

    override fun isCaseSensitive(): Boolean = true
}
