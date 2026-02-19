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
    fun `generateRescriptJson includes sources config as object`() {
        val json = RescriptProjectGenerator.generateRescriptJson("my-app", false)
        assertTrue(json.contains("\"sources\""))
        assertTrue(json.contains("\"dir\": \"src\""))
        assertTrue(json.contains("\"subdirs\": true"))
    }

    @Test
    fun `generateRescriptJson includes package-specs and suffix`() {
        val json = RescriptProjectGenerator.generateRescriptJson("my-app", false)
        assertTrue(json.contains("\"package-specs\""))
        assertTrue(json.contains("\"module\": \"esmodule\""))
        assertTrue(json.contains("\"in-source\": true"))
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
        assertTrue(json.contains("\"res:build\": \"rescript\""))
        assertTrue(json.contains("\"res:clean\": \"rescript clean\""))
        assertTrue(json.contains("\"res:dev\": \"rescript -w\""))
    }

    @Test
    fun `generatePackageJson includes rescript dependency without core`() {
        val json = RescriptProjectGenerator.generatePackageJson("my-app", false)
        assertTrue(json.contains("\"rescript\": \"^12.0.0\""))
        assertFalse(json.contains("\"@rescript/core\""))
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
        assertTrue(json.contains("\"react\": \"^19.0.0\""))
        assertTrue(json.contains("\"react-dom\": \"^19.0.0\""))
        assertTrue(json.contains("\"@rescript/react\": \"^0.14.0\""))
    }

    @Test
    fun `generateStarterModule produces valid ReScript`() {
        val code = RescriptProjectGenerator.generateStarterModule()
        assertTrue(code.contains("Console.log"))
        assertTrue(code.contains("Hello, ReScript!"))
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
