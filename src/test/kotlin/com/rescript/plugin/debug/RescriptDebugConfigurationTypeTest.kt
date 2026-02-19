package com.rescript.plugin.debug

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class RescriptDebugConfigurationTypeTest {
    @Test
    fun `ID is RescriptDebugConfiguration`() {
        assertEquals("RescriptDebugConfiguration", RescriptDebugConfigurationType.ID)
    }

    @Test
    fun `instance has correct display name`() {
        val type = RescriptDebugConfigurationType()
        assertEquals("ReScript Debug", type.displayName)
    }

    @Test
    fun `instance has one configuration factory`() {
        val type = RescriptDebugConfigurationType()
        assertEquals(1, type.configurationFactories.size)
    }

    @Test
    fun `icon is not null`() {
        val type = RescriptDebugConfigurationType()
        assertNotNull(type.icon)
    }
}
