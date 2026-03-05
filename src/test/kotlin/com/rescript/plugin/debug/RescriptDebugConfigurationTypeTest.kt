package com.rescript.plugin.debug

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

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
