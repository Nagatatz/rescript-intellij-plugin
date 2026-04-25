package com.rescript.plugin.wizard.templates

import com.rescript.plugin.wizard.ApiStrategy
import com.rescript.plugin.wizard.PackageManager
import com.rescript.plugin.wizard.ProjectTemplate
import com.rescript.plugin.wizard.ValidationLibrary
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.File

class TemplateResourcesSmokeTest {
    private val templatesDir = File("src/main/resources/templates")

    // Every `{{key}}` appearing in `src/main/resources/templates/` must be listed here.
    // Add new keys as template extraction introduces them — this guards against typos
    // in resource files (e.g. `{{projecName}}` instead of `{{projectName}}`).
    private val knownPlaceholders: Set<String> =
        setOf(
            // hono/readme/database.md, hono-graphql/readme/database.md
            "cmdDbGenerate",
            "cmdDbMigrate",
            // hono-graphql/readme/schema.md uses {{cmdDocsGraphql}} when documenting
            // how to regenerate the human-readable schema.md after editing SDL.
            "cmdDocsGraphql",
            // react-native-cli: app.json, src/App.res
            "projectName",
            // react-native-cli/readme/community-cli.md
            "installCmd",
            "execReactNative",
            // react-native-cli/readme/running-on-android.md, troubleshooting.md
            "cmdResDev",
            "cmdStart",
            "cmdAndroid",
            // basic/readme/run-the-app.md already uses cmdStart above
            // aws-lambda/readme/deploy.md
            "cmdBuild",
            // cloudflare-workers/readme/deploy.md
            "cmdDeploy",
            // npm-library/readme/publish.md
            "cmdTest",
            // cli-tool/readme/usage.md + project-layout.md + basic/readme + all
            // new template READMEs that embed the selected validation library name
            "validationLibrary",
            // res-x: rescript.json uses {{name}} for the project name
            "name",
            // res-x: Layout.res and readme/htmx.md embed the pinned HTMX CDN version
            "htmxVersion",
            // res-x: readme/app.md, readme/project-layout.md, TodoForm.res describe
            // the selected validation library (zod or sury) in prose
            "validationLib",
            // aws-lambda/readme/deploy.md + bundling.md embed the Node major used by
            // the Lambda runtime (e.g. nodejs22.x), which now follows ctx.nodeMajor
            "nodeMajor",
        )

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
    fun `no unresolved handlebars placeholders survive in any generated file`() {
        // Match only identifier-shaped placeholders so JSX style records like
        // `{{padding: "2rem"}}` are not flagged.
        val residuePattern = Regex("""\{\{[a-zA-Z][a-zA-Z0-9_]*}}""")
        val contexts =
            listOf(
                TemplateContext("demo-zod", PackageManager.PNPM, ValidationLibrary.ZOD),
                TemplateContext("demo-sury", PackageManager.PNPM, ValidationLibrary.SURY),
                TemplateContext(
                    "demo-graphql",
                    PackageManager.PNPM,
                    ValidationLibrary.ZOD,
                    ApiStrategy.GRAPHQL,
                ),
                TemplateContext("demo-bun", PackageManager.BUN, ValidationLibrary.ZOD),
            )
        val violations = mutableListOf<String>()
        ProjectTemplate.entries.forEach { template ->
            contexts.forEach { ctx ->
                val files = template.generateFiles(ctx)
                files.forEach { (path, content) ->
                    residuePattern.findAll(content).forEach { match ->
                        violations.add(
                            "$template (${ctx.validationLibrary}/${ctx.apiStrategy}) @ $path: ${match.value}",
                        )
                    }
                }
            }
        }
        assertTrue(
            violations.isEmpty(),
            "Unresolved {{placeholder}} strings found in generated files:\n" +
                violations.joinToString("\n") { "  $it" },
        )
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
