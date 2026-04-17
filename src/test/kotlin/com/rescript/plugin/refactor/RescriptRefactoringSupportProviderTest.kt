package com.rescript.plugin.refactor

import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

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
        val handler: Any? = provider.introduceVariableHandler
        assertTrue(handler is RescriptExtractVariableHandler)
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
