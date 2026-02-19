package com.rescript.plugin.wizard

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RescriptProjectGeneratorTest {
    @Test
    fun `generateRescriptJson includes project name`() {
        val json = RescriptProjectGenerator.generateRescriptJson("my-app", false)
        assertTrue(json.contains("\"name\": \"my-app\""))
    }

    @Test
    fun `generateRescriptJson includes sources config`() {
        val json = RescriptProjectGenerator.generateRescriptJson("my-app", false)
        assertTrue(json.contains("\"dir\": \"src\""))
        assertTrue(json.contains("\"subdirs\": true"))
    }

    @Test
    fun `generateRescriptJson includes package-type and suffix`() {
        val json = RescriptProjectGenerator.generateRescriptJson("my-app", false)
        assertTrue(json.contains("\"package-type\": \"module\""))
        assertTrue(json.contains("\"suffix\": \".res.mjs\""))
    }

    @Test
    fun `generateRescriptJson includes rescript-core dependency`() {
        val json = RescriptProjectGenerator.generateRescriptJson("my-app", false)
        assertTrue(json.contains("\"@rescript/core\""))
    }

    @Test
    fun `generateRescriptJson without React does not include react deps`() {
        val json = RescriptProjectGenerator.generateRescriptJson("my-app", false)
        assertFalse(json.contains("@rescript/react"))
        assertFalse(json.contains("\"jsx\""))
    }

    @Test
    fun `generateRescriptJson with React includes react deps and jsx config`() {
        val json = RescriptProjectGenerator.generateRescriptJson("my-app", true)
        assertTrue(json.contains("\"@rescript/react\""))
        assertTrue(json.contains("\"jsx\""))
        assertTrue(json.contains("\"version\": 4"))
    }

    @Test
    fun `generateRescriptJson includes bsc-flags`() {
        val json = RescriptProjectGenerator.generateRescriptJson("my-app", false)
        assertTrue(json.contains("\"-open RescriptCore\""))
    }

    @Test
    fun `generatePackageJson includes project name and version`() {
        val json = RescriptProjectGenerator.generatePackageJson("my-app", false)
        assertTrue(json.contains("\"name\": \"my-app\""))
        assertTrue(json.contains("\"version\": \"0.1.0\""))
    }

    @Test
    fun `generatePackageJson includes scripts`() {
        val json = RescriptProjectGenerator.generatePackageJson("my-app", false)
        assertTrue(json.contains("\"build\": \"rescript build\""))
        assertTrue(json.contains("\"clean\": \"rescript clean\""))
        assertTrue(json.contains("\"dev\": \"rescript build -w\""))
    }

    @Test
    fun `generatePackageJson includes rescript dependencies`() {
        val json = RescriptProjectGenerator.generatePackageJson("my-app", false)
        assertTrue(json.contains("\"rescript\""))
        assertTrue(json.contains("\"@rescript/core\""))
    }

    @Test
    fun `generatePackageJson without React does not include react deps`() {
        val json = RescriptProjectGenerator.generatePackageJson("my-app", false)
        assertFalse(json.contains("\"react\""))
        assertFalse(json.contains("\"react-dom\""))
        assertFalse(json.contains("@rescript/react"))
    }

    @Test
    fun `generatePackageJson with React includes react deps`() {
        val json = RescriptProjectGenerator.generatePackageJson("my-app", true)
        assertTrue(json.contains("\"react\""))
        assertTrue(json.contains("\"react-dom\""))
        assertTrue(json.contains("\"@rescript/react\""))
    }

    @Test
    fun `generateStarterModule produces valid ReScript`() {
        val code = RescriptProjectGenerator.generateStarterModule()
        assertTrue(code.contains("let greeting"))
        assertTrue(code.contains("Console.log"))
    }

    @Test
    fun `generateReactComponent produces valid React component`() {
        val code = RescriptProjectGenerator.generateReactComponent()
        assertTrue(code.contains("@react.component"))
        assertTrue(code.contains("let make = ()"))
        assertTrue(code.contains("React.string"))
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
