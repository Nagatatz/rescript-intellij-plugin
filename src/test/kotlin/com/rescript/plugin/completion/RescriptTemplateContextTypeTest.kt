package com.rescript.plugin.completion

import com.intellij.codeInsight.template.TemplateContextType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RescriptTemplateContextTypeTest {
    @Test
    fun `instance can be created`() {
        val contextType = RescriptTemplateContextType()
        assertNotNull(contextType)
    }

    @Test
    fun `is a TemplateContextType`() {
        val contextType = RescriptTemplateContextType()
        assertTrue(contextType is TemplateContextType)
    }

    @Test
    fun `presentable name is ReScript`() {
        val contextType = RescriptTemplateContextType()
        assertEquals("ReScript", contextType.presentableName)
    }
}
