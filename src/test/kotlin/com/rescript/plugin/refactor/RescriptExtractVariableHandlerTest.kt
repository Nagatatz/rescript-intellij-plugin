package com.rescript.plugin.refactor

import com.intellij.refactoring.RefactoringActionHandler
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RescriptExtractVariableHandlerTest {
    @Test
    fun `handler can be instantiated`() {
        val handler = RescriptExtractVariableHandler()
        assertNotNull(handler)
    }

    @Test
    fun `handler implements RefactoringActionHandler`() {
        val handler = RescriptExtractVariableHandler()
        assertTrue(handler is RefactoringActionHandler)
    }
}
