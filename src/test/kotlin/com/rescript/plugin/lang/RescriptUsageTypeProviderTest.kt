package com.rescript.plugin.lang

import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

class RescriptUsageTypeProviderTest {
    @Test
    fun `provider can be instantiated`() {
        val provider = RescriptUsageTypeProvider()
        assertNotNull(provider)
    }

    @Test
    fun `usage type constants are defined`() {
        assertNotNull(RescriptUsageTypeProvider.OPEN_USAGE)
        assertNotNull(RescriptUsageTypeProvider.TYPE_REFERENCE)
        assertNotNull(RescriptUsageTypeProvider.TYPE_ANNOTATION)
        assertNotNull(RescriptUsageTypeProvider.PATTERN_MATCH)
        assertNotNull(RescriptUsageTypeProvider.INCLUDE_USAGE)
        assertNotNull(RescriptUsageTypeProvider.MODULE_DECLARATION)
        assertNotNull(RescriptUsageTypeProvider.VALUE_DEFINITION)
        assertNotNull(RescriptUsageTypeProvider.JSX_USAGE)
    }
}
