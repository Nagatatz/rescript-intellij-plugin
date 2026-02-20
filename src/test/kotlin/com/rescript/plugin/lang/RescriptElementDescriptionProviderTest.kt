package com.rescript.plugin.lang

import org.junit.Assert.assertNotNull
import org.junit.Test

class RescriptElementDescriptionProviderTest {
    private val provider = RescriptElementDescriptionProvider()

    @Test
    fun `provider can be instantiated`() {
        assertNotNull(provider)
    }
}
