package com.rescript.plugin.debug

import org.junit.Assert.assertEquals
import org.junit.Test

class RescriptDebugConfigurationFactoryTest {
    private val type = RescriptDebugConfigurationType()
    private val factory = RescriptDebugConfigurationFactory(type)

    @Test
    fun `getId returns RescriptDebugConfiguration ID`() {
        assertEquals(RescriptDebugConfigurationType.ID, factory.id)
    }

    @Test
    fun `getOptionsClass returns RescriptDebugRunConfigurationOptions`() {
        assertEquals(RescriptDebugRunConfigurationOptions::class.java, factory.optionsClass)
    }
}
