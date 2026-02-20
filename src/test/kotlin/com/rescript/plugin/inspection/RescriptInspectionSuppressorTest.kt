package com.rescript.plugin.inspection

import org.junit.Assert.assertNotNull
import org.junit.Test

class RescriptInspectionSuppressorTest {
    @Test
    fun `suppressor can be instantiated`() {
        val suppressor = RescriptInspectionSuppressor()
        assertNotNull(suppressor)
    }

    @Test
    fun `getSuppressActions returns empty array`() {
        val suppressor = RescriptInspectionSuppressor()
        val actions = suppressor.getSuppressActions(null, "RescriptDuplicateOpen")
        assertNotNull(actions)
        assert(actions.isEmpty())
    }
}
