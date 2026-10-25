package com.rescript.plugin.editor

import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import java.lang.reflect.Proxy

class RescriptListSplitJoinContextTest {
    @Test
    fun `context can be instantiated`() {
        val context = RescriptListSplitJoinContext()
        assertNotNull(context)
    }

    @Test
    fun `list search stops at the containing file without listing its directory`() {
        val directory =
            proxy<PsiDirectory> { name ->
                error("PsiDirectory.$name must not be called while looking for a list")
            }
        val file =
            proxy<PsiFile> { name ->
                when (name) {
                    "getChildren" -> PsiElement.EMPTY_ARRAY
                    "getParent" -> directory
                    else -> null
                }
            }

        assertNull(RescriptListSplitJoinContext().extractData(file))
    }

    private inline fun <reified T : Any> proxy(crossinline answer: (String) -> Any?): T =
        Proxy.newProxyInstance(T::class.java.classLoader, arrayOf(T::class.java)) { _, method, _ ->
            when (method.name) {
                "hashCode" -> System.identityHashCode(this)
                "equals" -> false
                "toString" -> T::class.java.simpleName
                else -> answer(method.name)
            }
        } as T
}
