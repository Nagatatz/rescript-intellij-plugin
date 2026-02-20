package com.rescript.plugin.editor

import org.junit.Assert.assertNotNull
import org.junit.Test

class RescriptListSplitJoinContextTest {
    @Test
    fun `context can be instantiated`() {
        val context = RescriptListSplitJoinContext()
        assertNotNull(context)
    }
}
