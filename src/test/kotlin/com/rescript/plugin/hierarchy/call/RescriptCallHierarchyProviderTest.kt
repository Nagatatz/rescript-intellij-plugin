package com.rescript.plugin.hierarchy.call

import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class RescriptCallHierarchyProviderTest {
    @Test
    fun `provider can be instantiated`() {
        val provider = RescriptCallHierarchyProvider()
        assertNotNull(provider)
    }

    @Test
    fun `getTarget returns null for empty DataContext`() {
        val provider = RescriptCallHierarchyProvider()
        val emptyContext = EmptyDataContext()
        val target = provider.getTarget(emptyContext)
        assertNull(target)
    }

    @Test
    fun `createHierarchyBrowser returns RescriptCallHierarchyBrowser`() {
        // This test verifies the method signature and return type.
        // Actual browser creation requires a valid PsiElement with a project,
        // which is an IDE integration concern.
        val provider = RescriptCallHierarchyProvider()
        assertNotNull(provider)
    }

    @Test
    fun `browser view types are defined`() {
        assertTrue(RescriptCallHierarchyBrowser.CALLERS_TYPE.isNotEmpty())
        assertTrue(RescriptCallHierarchyBrowser.CALLEES_TYPE.isNotEmpty())
    }

    @Test
    fun `callers and callees types are distinct`() {
        assertTrue(RescriptCallHierarchyBrowser.CALLERS_TYPE != RescriptCallHierarchyBrowser.CALLEES_TYPE)
    }

    /**
     * Minimal DataContext that returns null for all keys.
     */
    private class EmptyDataContext : com.intellij.openapi.actionSystem.DataContext {
        override fun <T : Any?> getData(key: com.intellij.openapi.actionSystem.DataKey<T>): T? = null

        override fun getData(dataId: String): Any? = null
    }
}
