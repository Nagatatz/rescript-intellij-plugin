package com.rescript.plugin.wizard

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RescriptProjectGeneratorTest {
    @Test
    fun `generateFiles delegates to template`() {
        val files = RescriptProjectGenerator.generateFiles(ProjectTemplate.BASIC, "my-app")
        assertTrue(files.containsKey("rescript.json"))
        assertTrue(files.containsKey("package.json"))
        assertTrue(files.containsKey("src/App.res"))
    }

    @Test
    fun `generateFiles includes project name in rescript json`() {
        val files = RescriptProjectGenerator.generateFiles(ProjectTemplate.BASIC, "my-app")
        assertTrue(files["rescript.json"]!!.contains("\"name\": \"my-app\""))
    }

    @Test
    fun `generateFiles includes project name in package json`() {
        val files = RescriptProjectGenerator.generateFiles(ProjectTemplate.BASIC, "my-app")
        assertTrue(files["package.json"]!!.contains("\"name\": \"my-app\""))
    }

    @Test
    fun `generateFiles for VITE_REACT includes react files`() {
        val files = RescriptProjectGenerator.generateFiles(ProjectTemplate.VITE_REACT, "my-app")
        assertTrue(files.containsKey("index.html"))
        assertTrue(files.containsKey("vite.config.mjs"))
        assertTrue(files.containsKey("src/App.res"))
        assertTrue(files.containsKey("src/Main.res"))
    }

    @Test
    fun `PackageManager enum has correct commands`() {
        assertEquals("npm", PackageManager.NPM.command)
        assertEquals("pnpm", PackageManager.PNPM.command)
        assertEquals("yarn", PackageManager.YARN.command)
    }

    @Test
    fun `PackageManager enum has three values`() {
        assertEquals(3, PackageManager.entries.size)
    }
}
