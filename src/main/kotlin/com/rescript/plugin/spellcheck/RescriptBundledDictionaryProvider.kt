package com.rescript.plugin.spellcheck

import com.intellij.spellchecker.BundledDictionaryProvider

/**
 * Provides a bundled dictionary of ReScript-specific terms to prevent
 * false-positive spelling warnings on domain keywords, module names,
 * and common identifiers.
 *
 * @see RescriptSpellcheckingStrategy
 */
class RescriptBundledDictionaryProvider : BundledDictionaryProvider {
    override fun getBundledDictionaries(): Array<String> = arrayOf("/dictionaries/rescript.dic")
}
