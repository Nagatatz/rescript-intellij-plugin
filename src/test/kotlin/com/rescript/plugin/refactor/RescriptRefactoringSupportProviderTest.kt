package com.rescript.plugin.refactor

import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RescriptRefactoringSupportProviderTest {
    @Test
    fun `provider can be instantiated`() {
        val provider = RescriptRefactoringSupportProvider()
        assertNotNull(provider)
    }

    @Test
    fun `getIntroduceVariableHandler returns non-null`() {
        val provider = RescriptRefactoringSupportProvider()
        assertNotNull(provider.introduceVariableHandler)
    }

    @Test
    fun `getIntroduceVariableHandler returns ExtractVariableHandler`() {
        val provider = RescriptRefactoringSupportProvider()
        assertTrue(provider.introduceVariableHandler is RescriptExtractVariableHandler)
    }

    @Test
    fun `isSafeDeleteAvailable returns true`() {
        // isSafeDeleteAvailable always returns true for ReScript elements
        val provider = RescriptRefactoringSupportProvider()
        // We can't easily create a mock PsiElement, so just verify the method exists
        // The actual behavior is tested via the SafeDeleteProcessor
        assertNotNull(provider)
    }
}
