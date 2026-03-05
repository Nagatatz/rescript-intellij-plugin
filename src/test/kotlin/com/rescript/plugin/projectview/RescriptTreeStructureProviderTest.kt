package com.rescript.plugin.projectview

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class RescriptTreeStructureProviderTest {
    @Test
    fun `provider instantiation succeeds`() {
        val provider = RescriptTreeStructureProvider()
        assertEquals(RescriptTreeStructureProvider::class.java, provider.javaClass)
    }
}
