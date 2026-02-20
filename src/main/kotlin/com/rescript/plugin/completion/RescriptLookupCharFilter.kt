package com.rescript.plugin.completion

import com.intellij.codeInsight.lookup.CharFilter
import com.intellij.codeInsight.lookup.Lookup
import com.rescript.plugin.lang.psi.RescriptFile

/**
 * Controls character behavior during code completion popup in ReScript files.
 *
 * Defines how specific characters interact with the active completion lookup:
 * - `.` adds to prefix (supports pipe chain completion like `array->Array.`)
 * - `~` adds to prefix (supports labeled argument completion like `~label`)
 * - `(` selects the current item and finishes lookup (function call trigger)
 *
 * Returns `null` for all other characters and non-ReScript files,
 * deferring to the default platform behavior.
 *
 * @see RescriptPostfixTemplateProvider for postfix completion support
 */
class RescriptLookupCharFilter : CharFilter() {
    override fun acceptChar(
        c: Char,
        prefixLength: Int,
        lookup: Lookup,
    ): Result? {
        val file = lookup.psiFile ?: return null
        if (file !is RescriptFile) return null

        return when (c) {
            '.' -> Result.ADD_TO_PREFIX
            '~' -> Result.ADD_TO_PREFIX
            '(' -> Result.SELECT_ITEM_AND_FINISH_LOOKUP
            else -> null
        }
    }
}
