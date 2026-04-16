package com.rescript.plugin.wizard.templates

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.File

class TemplateResourcesSmokeTest {
    private val templatesDir = File("src/main/resources/templates")

    // Every `{{key}}` appearing in `src/main/resources/templates/` must be listed here.
    // Add new keys as template extraction introduces them — this guards against typos
    // in resource files (e.g. `{{projecName}}` instead of `{{projectName}}`).
    private val knownPlaceholders: Set<String> = emptySet()

    @Test
    fun `every template resource is readable as UTF-8`() {
        if (!templatesDir.isDirectory) return
        val unreadable = mutableListOf<String>()
        templatesDir
            .walkTopDown()
            .filter { it.isFile }
            .forEach { file ->
                try {
                    file.readText(Charsets.UTF_8)
                } catch (t: Throwable) {
                    unreadable.add("${file.relativeTo(templatesDir)}: ${t.message}")
                }
            }
        assertEquals(emptyList<String>(), unreadable, "unreadable resources")
    }

    @Test
    fun `every placeholder in template resources is known`() {
        if (!templatesDir.isDirectory) return
        val placeholderPattern = Regex("""\{\{([a-zA-Z][a-zA-Z0-9_]*)}}""")
        val unknown = mutableMapOf<String, MutableList<String>>()
        templatesDir
            .walkTopDown()
            .filter { it.isFile }
            .forEach { file ->
                val content = file.readText(Charsets.UTF_8)
                placeholderPattern.findAll(content).forEach { match ->
                    val key = match.groupValues[1]
                    if (key !in knownPlaceholders) {
                        unknown
                            .getOrPut(key) { mutableListOf() }
                            .add(file.relativeTo(templatesDir).path)
                    }
                }
            }
        assertTrue(
            unknown.isEmpty(),
            "Unknown placeholders found (add to knownPlaceholders if intentional):\n" +
                unknown.entries.joinToString("\n") { (k, files) -> "  {{$k}} in ${files.distinct()}" },
        )
    }
}
