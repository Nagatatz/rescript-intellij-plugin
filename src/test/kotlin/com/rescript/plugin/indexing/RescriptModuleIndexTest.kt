package com.rescript.plugin.indexing

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RescriptModuleIndexTest {
    @Test
    fun testKeyIsNotNull() {
        assertNotNull(RescriptModuleIndex.KEY)
    }

    @Test
    fun testKeyName() {
        assertEquals("rescript.module.index", RescriptModuleIndex.KEY.name)
    }

    @Test
    fun testVersionIsPositive() {
        val index = RescriptModuleIndex()
        assertTrue(index.version > 0)
    }

    @Test
    fun testKeyReturnsCorrectKey() {
        val index = RescriptModuleIndex()
        assertEquals(RescriptModuleIndex.KEY, index.key)
    }

    @Test
    fun testKeyIsDifferentFromNameIndex() {
        assertTrue(RescriptNameIndex.KEY != RescriptModuleIndex.KEY)
    }
}
