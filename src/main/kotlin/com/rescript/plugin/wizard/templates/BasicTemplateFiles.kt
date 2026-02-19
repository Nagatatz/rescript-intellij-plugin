package com.rescript.plugin.wizard.templates

import com.rescript.plugin.wizard.ProjectFileBuilders

internal object BasicTemplateFiles {
    fun generate(projectName: String): Map<String, String> =
        mapOf(
            "rescript.json" to ProjectFileBuilders.rescriptJson(name = projectName),
            "package.json" to
                ProjectFileBuilders.packageJson(
                    name = projectName,
                    dependencies = linkedMapOf("rescript" to "^12.0.0", "@rescript/core" to "^1.0.0"),
                ),
            "src/App.res" to ProjectFileBuilders.starterModule(),
        )
}
