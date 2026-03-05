package com.rescript.plugin.editor

import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

class RescriptListSplitJoinContextTest {
    @Test
    fun `context can be instantiated`() {
        val context = RescriptListSplitJoinContext()
        assertNotNull(context)
    }
}
