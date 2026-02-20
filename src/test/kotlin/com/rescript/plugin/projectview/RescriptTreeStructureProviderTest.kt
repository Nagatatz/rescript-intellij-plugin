package com.rescript.plugin.projectview

import org.junit.Assert.assertEquals
import org.junit.Test

class RescriptTreeStructureProviderTest {
    @Test
    fun `provider instantiation succeeds`() {
        val provider = RescriptTreeStructureProvider()
        assertEquals(RescriptTreeStructureProvider::class.java, provider.javaClass)
    }
}
