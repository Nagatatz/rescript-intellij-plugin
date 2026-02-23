package com.rescript.plugin.indexing

import com.intellij.psi.stubs.StringStubIndexExtension
import com.intellij.psi.stubs.StubIndexKey
import com.rescript.plugin.lang.psi.RescriptDeclarationPsiElement

/**
 * Stub index mapping declaration names to their PSI elements across all ReScript files.
 *
 * Provides fast O(log n) symbol lookup by name, replacing the previous approach
 * of scanning all files and walking PSI trees. Used by [com.rescript.plugin.navigation.RescriptSymbolContributor]
 * for Go to Symbol and Search Everywhere.
 */
class RescriptNameIndex : StringStubIndexExtension<RescriptDeclarationPsiElement>() {
    companion object {
        @JvmField
        val KEY: StubIndexKey<String, RescriptDeclarationPsiElement> =
            StubIndexKey.createIndexKey("rescript.name.index")
    }

    override fun getKey(): StubIndexKey<String, RescriptDeclarationPsiElement> = KEY

    override fun getVersion(): Int = 1
}
