package com.rescript.plugin.debug

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RescriptDebugRunConfigurationOptionsTest {
    @Test
    fun `default nodeExecutable is node`() {
        val options = RescriptDebugRunConfigurationOptions()
        assertEquals("node", options.nodeExecutable)
    }

    @Test
    fun `default sourceFilePath is null`() {
        val options = RescriptDebugRunConfigurationOptions()
        assertNull(options.sourceFilePath)
    }

    @Test
    fun `default nodeArgs is null`() {
        val options = RescriptDebugRunConfigurationOptions()
        assertNull(options.nodeArgs)
    }

    @Test
    fun `default workingDirectory is null`() {
        val options = RescriptDebugRunConfigurationOptions()
        assertNull(options.workingDirectory)
    }

    @Test
    fun `sourceFilePath can be set and retrieved`() {
        val options = RescriptDebugRunConfigurationOptions()
        options.sourceFilePath = "/path/to/src/Main.res"
        assertEquals("/path/to/src/Main.res", options.sourceFilePath)
    }

    @Test
    fun `nodeExecutable can be set and retrieved`() {
        val options = RescriptDebugRunConfigurationOptions()
        options.nodeExecutable = "/usr/local/bin/node"
        assertEquals("/usr/local/bin/node", options.nodeExecutable)
    }

    @Test
    fun `nodeArgs can be set and retrieved`() {
        val options = RescriptDebugRunConfigurationOptions()
        options.nodeArgs = "--max-old-space-size=4096"
        assertEquals("--max-old-space-size=4096", options.nodeArgs)
    }

    @Test
    fun `workingDirectory can be set and retrieved`() {
        val options = RescriptDebugRunConfigurationOptions()
        options.workingDirectory = "/path/to/project"
        assertEquals("/path/to/project", options.workingDirectory)
    }
}
