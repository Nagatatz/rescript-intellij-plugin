package com.rescript.plugin.documentation

import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class RescriptDocumentationProviderTest {
    @Test
    fun `provider can be instantiated`() {
        val provider = RescriptDocumentationProvider()
        assertNotNull(provider)
    }

    @Test
    fun `generateDoc returns null for null element`() {
        val provider = RescriptDocumentationProvider()
        assertNull(provider.generateDoc(null, null))
    }

    @Test
    fun `getDeclarationType returns correct types`() {
        // These require PSI elements which need the full IntelliJ platform;
        // here we test the null case for non-declaration elements
        assertNull(
            RescriptDocumentationProvider.getDeclarationType(
                object : com.intellij.psi.impl.FakePsiElement() {
                    override fun getParent(): com.intellij.psi.PsiElement? = null
                },
            ),
        )
    }
}
