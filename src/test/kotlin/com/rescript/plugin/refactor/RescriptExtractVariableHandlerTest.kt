package com.rescript.plugin.refactor

import com.intellij.refactoring.RefactoringActionHandler
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

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
