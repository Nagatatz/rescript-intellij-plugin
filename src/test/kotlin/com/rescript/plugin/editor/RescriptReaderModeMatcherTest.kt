package com.rescript.plugin.editor

import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

class RescriptReaderModeMatcherTest {
    @Test
    fun `matcher can be instantiated`() {
        val matcher = RescriptReaderModeMatcher()
        assertNotNull(matcher)
    }
}
