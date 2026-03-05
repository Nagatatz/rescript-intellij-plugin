package com.rescript.plugin.codevision

import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

class RescriptVcsCodeVisionContextTest {
    @Test
    fun `context can be instantiated`() {
        val context = RescriptVcsCodeVisionContext()
        assertNotNull(context)
    }
}
