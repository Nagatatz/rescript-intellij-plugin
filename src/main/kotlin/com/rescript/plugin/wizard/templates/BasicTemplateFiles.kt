package com.rescript.plugin.wizard.templates

import com.rescript.plugin.wizard.ProjectFileBuilders

/**
 * Generates project template files for a minimal ReScript project.
 *
 * The Basic template is the entry point for new ReScript users, so it ships the same
 * documentation, ignore rules, editor config, and CI scaffolding as every other template
 * to encourage a complete starting point rather than an unsupported skeleton.
 */
internal object BasicTemplateFiles {
    /**
     * Generates Basic template files using the supplied [TemplateContext].
     */
    fun generate(ctx: TemplateContext): Map<String, String> =
        mapOf(
            "rescript.json" to ProjectFileBuilders.rescriptJson(name = ctx.projectName),
            "package.json" to
                ProjectFileBuilders.packageJson(
                    name = ctx.projectName,
                    isPrivate = true,
                    packageManager = ctx.packageManagerSpec(),
                    engines = mapOf("node" to TemplateVersions.NODE_ENGINE),
                    dependencies =
                        linkedMapOf(
                            "rescript" to TemplateVersions.RESCRIPT,
                            "@rescript/core" to TemplateVersions.RESCRIPT_CORE,
                        ),
                ),
            "src/App.res" to ProjectFileBuilders.starterModule(),
            "README.md" to
                CommonFiles.readme(
                    ctx = ctx,
                    description = "A minimal ReScript starter project.",
                    scripts =
                        listOf(
                            "res:dev" to "Compile ReScript sources in watch mode",
                            "res:build" to "Compile ReScript sources once",
                            "res:clean" to "Remove generated build artifacts",
                        ),
                    extraSections =
                        listOf(
                            "Run the App" to
                                "After building, run the entry module:\n\n```bash\nnode src/App.res.mjs\n```",
                        ),
                ),
            ".gitignore" to CommonFiles.gitignore(),
            ".editorconfig" to CommonFiles.editorconfig(),
            ".github/workflows/ci.yml" to CommonFiles.ciWorkflow(ctx),
        )

    /**
     * Back-compatible entry point that defaults to pnpm when only a project name is provided.
     */
    fun generate(projectName: String): Map<String, String> =
        generate(TemplateContext(projectName, com.rescript.plugin.wizard.PackageManager.PNPM))
}
