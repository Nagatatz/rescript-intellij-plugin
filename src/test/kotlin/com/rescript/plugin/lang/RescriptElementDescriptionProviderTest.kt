package com.rescript.plugin.lang

import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

class RescriptElementDescriptionProviderTest {
    private val provider = RescriptElementDescriptionProvider()

    @Test
    fun `provider can be instantiated`() {
        assertNotNull(provider)
    }
}
