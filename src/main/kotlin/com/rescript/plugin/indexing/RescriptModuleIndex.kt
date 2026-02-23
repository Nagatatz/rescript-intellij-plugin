package com.rescript.plugin.indexing

import com.intellij.psi.stubs.StringStubIndexExtension
import com.intellij.psi.stubs.StubIndexKey
import com.rescript.plugin.lang.psi.RescriptDeclarationPsiElement

/**
 * Stub index for ReScript module declarations only.
 *
 * Provides fast lookup of module names, useful for module-specific queries
 * such as module hierarchy navigation and unresolved reference quick fixes.
 */
class RescriptModuleIndex : StringStubIndexExtension<RescriptDeclarationPsiElement>() {
    companion object {
        @JvmField
        val KEY: StubIndexKey<String, RescriptDeclarationPsiElement> =
            StubIndexKey.createIndexKey("rescript.module.index")
    }

    override fun getKey(): StubIndexKey<String, RescriptDeclarationPsiElement> = KEY

    override fun getVersion(): Int = 1
}
