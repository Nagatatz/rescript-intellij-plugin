package com.rescript.plugin.wizard

import com.rescript.plugin.wizard.templates.TemplateContext
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

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

    @Test
    fun `PackageManager toString returns the lowercase command for Swing renderers`() {
        assertEquals("npm", PackageManager.NPM.toString())
        assertEquals("pnpm", PackageManager.PNPM.toString())
        assertEquals("yarn", PackageManager.YARN.toString())
    }

    @Test
    fun `generateFiles context overload dispatches to the template`() {
        val ctx = TemplateContext("ctx-app", PackageManager.PNPM)
        val files = RescriptProjectGenerator.generateFiles(ProjectTemplate.BASIC, ctx)
        assertTrue(files.containsKey("rescript.json"))
        assertTrue(files["rescript.json"]!!.contains("\"name\": \"ctx-app\""))
    }
}
