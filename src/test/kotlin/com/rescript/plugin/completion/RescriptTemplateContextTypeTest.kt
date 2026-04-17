package com.rescript.plugin.completion

import com.intellij.codeInsight.template.TemplateContextType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class RescriptTemplateContextTypeTest {
    @Test
    fun `instance can be created`() {
        val contextType = RescriptTemplateContextType()
        assertNotNull(contextType)
    }

    @Test
    fun `is a TemplateContextType`() {
        val contextType: Any = RescriptTemplateContextType()
        assertTrue(contextType is TemplateContextType)
    }

    @Test
    fun `presentable name is ReScript`() {
        val contextType = RescriptTemplateContextType()
        assertEquals("ReScript", contextType.presentableName)
    }
}
