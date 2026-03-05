package com.rescript.plugin.config

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.rescript.plugin.RescriptIcons
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class RescriptJsonIconProviderTest {
    private val provider = RescriptJsonIconProvider()

    private fun stubPsiFile(fileName: String): PsiFile =
        java.lang.reflect.Proxy.newProxyInstance(
            PsiFile::class.java.classLoader,
            arrayOf(PsiFile::class.java),
        ) { proxy, method, _ ->
            when (method.name) {
                "getName" -> fileName
                "toString" -> "StubPsiFile($fileName)"
                "hashCode" -> System.identityHashCode(proxy)
                "equals" -> false
                else -> null
            }
        } as PsiFile

    private fun stubPsiElement(): PsiElement =
        java.lang.reflect.Proxy.newProxyInstance(
            PsiElement::class.java.classLoader,
            arrayOf(PsiElement::class.java),
        ) { proxy, method, _ ->
            when (method.name) {
                "toString" -> "StubPsiElement"
                "hashCode" -> System.identityHashCode(proxy)
                "equals" -> false
                else -> null
            }
        } as PsiElement

    @Test
    fun `getIcon returns CONFIG_FILE icon for rescript json`() {
        val file = stubPsiFile("rescript.json")
        val icon = provider.getIcon(file, 0)
        assertNotNull(icon)
        assertEquals(RescriptIcons.CONFIG_FILE, icon)
    }

    @Test
    fun `getIcon returns CONFIG_FILE icon for bsconfig json`() {
        val file = stubPsiFile("bsconfig.json")
        val icon = provider.getIcon(file, 0)
        assertNotNull(icon)
        assertEquals(RescriptIcons.CONFIG_FILE, icon)
    }

    @Test
    fun `getIcon returns null for other json files`() {
        val file = stubPsiFile("package.json")
        assertNull(provider.getIcon(file, 0))
    }

    @Test
    fun `getIcon returns null for non-PsiFile elements`() {
        val element = stubPsiElement()
        assertNull(provider.getIcon(element, 0))
    }

    @Test
    fun `getIcon returns null for random file names`() {
        val file = stubPsiFile("tsconfig.json")
        assertNull(provider.getIcon(file, 0))
    }

    @Test
    fun `getIcon returns null for similar but wrong names`() {
        val file = stubPsiFile("rescript.json.bak")
        assertNull(provider.getIcon(file, 0))
    }

    @Test
    fun `provider can be instantiated`() {
        assertNotNull(provider)
    }
}
