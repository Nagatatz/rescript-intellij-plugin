package com.rescript.plugin.indexing

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RescriptOpenStatementIndexTest {
    private val index = RescriptOpenStatementIndex()

    @Test
    fun `index can be instantiated`() {
        assertNotNull(index)
    }

    @Test
    fun `index name is correct`() {
        assertEquals("rescript.open.statements", index.name.name)
    }

    @Test
    fun `index version is positive`() {
        assertTrue(index.version > 0)
    }

    @Test
    fun `index depends on file content`() {
        assertTrue(index.dependsOnFileContent())
    }

    @Test
    fun `key descriptor is not null`() {
        assertNotNull(index.keyDescriptor)
    }

    @Test
    fun `value externalizer is not null`() {
        assertNotNull(index.valueExternalizer)
    }
}
