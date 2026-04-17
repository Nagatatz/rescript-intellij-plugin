package com.rescript.plugin.lang

import com.intellij.psi.PsiElement
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

class RescriptFindUsagesProviderTest {
    private val provider = RescriptFindUsagesProvider()

    @Test
    fun `words scanner is not null`() {
        assertNotNull(provider.wordsScanner)
    }

    @Test
    fun `help id is null for any element`() {
        val stub =
            java.lang.reflect.Proxy.newProxyInstance(
                PsiElement::class.java.classLoader,
                arrayOf(PsiElement::class.java),
            ) { _, _, _ -> null } as PsiElement
        assertEquals(null, provider.getHelpId(stub))
    }
}
