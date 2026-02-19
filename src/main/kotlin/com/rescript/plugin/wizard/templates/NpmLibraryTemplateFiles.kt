package com.rescript.plugin.wizard.templates

import com.rescript.plugin.wizard.ProjectFileBuilders

/**
 * Generates files for the npm Library template.
 *
 * Produces a publishable npm package with ReScript and `@genType`
 * for TypeScript consumers.
 */
internal object NpmLibraryTemplateFiles {
    fun generate(projectName: String): Map<String, String> =
        mapOf(
            "rescript.json" to ProjectFileBuilders.rescriptJson(name = projectName, includeGenType = true),
            "package.json" to
                ProjectFileBuilders.packageJson(
                    name = projectName,
                    dependencies = linkedMapOf("rescript" to "^12.0.0", "@rescript/core" to "^1.0.0"),
                    scripts =
                        linkedMapOf(
                            "build" to "rescript",
                            "clean" to "rescript clean",
                            "prepare" to "rescript",
                            "res:build" to "rescript",
                            "res:clean" to "rescript clean",
                            "res:dev" to "rescript -w",
                        ),
                    type = "module",
                ),
            "src/Index.res" to indexRes(projectName),
        )

    private fun indexRes(projectName: String): String {
        val dollar = '$'
        return "@genType\n" +
            "let greet = (name: string) => {\n" +
            "  `Hello from $projectName, $dollar{name}!`\n" +
            "}"
    }
}
