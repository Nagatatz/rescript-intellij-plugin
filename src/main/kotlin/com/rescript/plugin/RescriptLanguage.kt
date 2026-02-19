package com.rescript.plugin

import com.intellij.lang.Language

/**
 * Singleton language definition for ReScript.
 *
 * Registered as the [Language] instance for all ReScript-related file types (.res, .resi).
 * Case-sensitive to match ReScript's identifier rules.
 */
object RescriptLanguage : Language("ReScript") {
    override fun isCaseSensitive(): Boolean = true

    private fun readResolve(): Any = RescriptLanguage
}
