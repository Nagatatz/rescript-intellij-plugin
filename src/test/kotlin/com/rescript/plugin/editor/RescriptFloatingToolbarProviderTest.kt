package com.rescript.plugin.editor

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class RescriptFloatingToolbarProviderTest {
    private val provider = RescriptFloatingToolbarProvider()

    @Test
    fun testProviderCanBeInstantiated() {
        assertNotNull(provider)
    }

    @Test
    fun testActionGroupIsNotNull() {
        assertNotNull(provider.actionGroup)
    }

    @Test
    fun testBuildActionGroupReturnsDefaultGroup() {
        val group = RescriptFloatingToolbarProvider.buildActionGroup()
        assertNotNull(group)
    }

    @Suppress("DEPRECATION")
    @Test
    fun testPriorityIsZero() {
        assertEquals(0, provider.priority)
    }
}
