package com.rescript.plugin.indexing

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

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
