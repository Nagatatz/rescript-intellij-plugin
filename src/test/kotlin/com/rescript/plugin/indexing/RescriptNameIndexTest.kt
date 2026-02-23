package com.rescript.plugin.indexing

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RescriptNameIndexTest {
    @Test
    fun testKeyIsNotNull() {
        assertNotNull(RescriptNameIndex.KEY)
    }

    @Test
    fun testKeyName() {
        assertEquals("rescript.name.index", RescriptNameIndex.KEY.name)
    }

    @Test
    fun testVersionIsPositive() {
        val index = RescriptNameIndex()
        assertTrue(index.version > 0)
    }

    @Test
    fun testKeyReturnsCorrectKey() {
        val index = RescriptNameIndex()
        assertEquals(RescriptNameIndex.KEY, index.key)
    }
}
