package com.rescript.plugin.wizard

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ProjectFileBuildersTest {
    @Test
    fun `rescriptJson includes project name`() {
        val json = ProjectFileBuilders.rescriptJson("my-app")
        assertTrue(json.contains("\"name\": \"my-app\""))
    }

    @Test
    fun `rescriptJson includes default sources config`() {
        val json = ProjectFileBuilders.rescriptJson("my-app")
        assertTrue(json.contains("\"dir\": \"src\""))
        assertTrue(json.contains("\"subdirs\": true"))
    }

    @Test
    fun `rescriptJson includes package-specs and suffix`() {
        val json = ProjectFileBuilders.rescriptJson("my-app")
        assertTrue(json.contains("\"module\": \"esmodule\""))
        assertTrue(json.contains("\"suffix\": \".res.mjs\""))
    }

    @Test
    fun `rescriptJson includes default bs-dependencies`() {
        val json = ProjectFileBuilders.rescriptJson("my-app")
        assertTrue(json.contains("\"@rescript/core\""))
    }

    @Test
    fun `rescriptJson with jsx includes jsx section`() {
        val json = ProjectFileBuilders.rescriptJson("my-app", includeJsx = true)
        assertTrue(json.contains("\"jsx\""))
        assertTrue(json.contains("\"version\": 4"))
    }

    @Test
    fun `rescriptJson without jsx excludes jsx section`() {
        val json = ProjectFileBuilders.rescriptJson("my-app")
        assertFalse(json.contains("\"jsx\""))
    }

    @Test
    fun `rescriptJson with genType includes gentypeconfig`() {
        val json = ProjectFileBuilders.rescriptJson("my-app", includeGenType = true)
        assertTrue(json.contains("\"gentypeconfig\""))
    }

    @Test
    fun `rescriptJson includes bsc-flags`() {
        val json = ProjectFileBuilders.rescriptJson("my-app")
        assertTrue(json.contains("\"-open RescriptCore\""))
    }

    @Test
    fun `packageJson includes name and version`() {
        val json = ProjectFileBuilders.packageJson("my-app")
        assertTrue(json.contains("\"name\": \"my-app\""))
        assertTrue(json.contains("\"version\": \"0.1.0\""))
    }

    @Test
    fun `packageJson includes default scripts`() {
        val json = ProjectFileBuilders.packageJson("my-app")
        assertTrue(json.contains("\"res:build\": \"rescript\""))
    }

    @Test
    fun `packageJson with dependencies`() {
        val json =
            ProjectFileBuilders.packageJson(
                "my-app",
                dependencies = linkedMapOf("rescript" to "^12.0.0"),
            )
        assertTrue(json.contains("\"rescript\": \"^12.0.0\""))
    }

    @Test
    fun `packageJson with devDependencies`() {
        val json =
            ProjectFileBuilders.packageJson(
                "my-app",
                devDependencies = linkedMapOf("vite" to "^6.0.0"),
            )
        assertTrue(json.contains("\"devDependencies\""))
        assertTrue(json.contains("\"vite\": \"^6.0.0\""))
    }

    @Test
    fun `packageJson without devDependencies omits section`() {
        val json = ProjectFileBuilders.packageJson("my-app")
        assertFalse(json.contains("\"devDependencies\""))
    }

    @Test
    fun `packageJson with bin entry`() {
        val json = ProjectFileBuilders.packageJson("my-app", bin = "./src/cli.mjs")
        assertTrue(json.contains("\"bin\": \"./src/cli.mjs\""))
    }

    @Test
    fun `packageJson with workspaces`() {
        val json = ProjectFileBuilders.packageJson("my-app", workspaces = listOf("packages/*"))
        assertTrue(json.contains("\"workspaces\""))
    }

    @Test
    fun `packageJson with type module`() {
        val json = ProjectFileBuilders.packageJson("my-app", type = "module")
        assertTrue(json.contains("\"type\": \"module\""))
    }

    @Test
    fun `packageJson with private flag`() {
        val json = ProjectFileBuilders.packageJson("my-app", isPrivate = true)
        assertTrue(json.contains("\"private\": true"))
    }

    @Test
    fun `defaultScripts returns three entries`() {
        assertEquals(3, ProjectFileBuilders.defaultScripts().size)
    }

    @Test
    fun `starterModule generates console log`() {
        assertTrue(ProjectFileBuilders.starterModule().contains("Console.log"))
    }

    @Test
    fun `reactComponent generates valid component`() {
        val code = ProjectFileBuilders.reactComponent()
        assertTrue(code.contains("@react.component"))
        assertTrue(code.contains("let make"))
    }

    @Test
    fun `honoBindings generates Hono types`() {
        val code = ProjectFileBuilders.honoBindings()
        assertTrue(code.contains("type app"))
        assertTrue(code.contains("type context"))
        assertTrue(code.contains("createApp"))
    }

    @Test
    fun `honoNodeServerBindings generates serve binding`() {
        val code = ProjectFileBuilders.honoNodeServerBindings()
        assertTrue(code.contains("serve"))
        assertTrue(code.contains("port: int"))
    }
}
