package com.rescript.plugin.lang

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class RescriptFindUsagesProviderTest {
    private val provider = RescriptFindUsagesProvider()

    @Test
    fun `words scanner is not null`() {
        assertNotNull(provider.wordsScanner)
    }

    @Test
    fun `help id is null`() {
        assertEquals(null, provider.getHelpId(null as? com.intellij.psi.PsiElement ?: return))
    }
}
