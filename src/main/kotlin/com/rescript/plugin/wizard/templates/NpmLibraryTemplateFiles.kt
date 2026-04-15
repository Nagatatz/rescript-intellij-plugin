package com.rescript.plugin.wizard.templates

import com.rescript.plugin.wizard.PackageManager
import com.rescript.plugin.wizard.ProjectFileBuilders

/**
 * Generates files for the npm Library template.
 *
 * Produces a publishable npm package with ReScript and `@genType` for TypeScript consumers.
 * Ships README, .gitignore (excluding `lib/` so the npm-published artifact has clean sources),
 * .editorconfig, CI workflow, and a Vitest sample.
 */
internal object NpmLibraryTemplateFiles {
    /**
     * Generates npm Library template files using the supplied [TemplateContext].
     */
    fun generate(ctx: TemplateContext): Map<String, String> =
        mapOf(
            "rescript.json" to
                ProjectFileBuilders.rescriptJson(
                    name = ctx.projectName,
                    includeGenType = true,
                ),
            "package.json" to
                ProjectFileBuilders.packageJson(
                    name = ctx.projectName,
                    type = "module",
                    packageManager = ctx.packageManagerSpec(),
                    engines = mapOf("node" to TemplateVersions.NODE_ENGINE),
                    dependencies =
                        linkedMapOf(
                            "rescript" to TemplateVersions.RESCRIPT,
                            "@rescript/core" to TemplateVersions.RESCRIPT_CORE,
                        ),
                    devDependencies =
                        linkedMapOf(
                            "vitest" to TemplateVersions.VITEST,
                            "typescript" to TemplateVersions.TYPESCRIPT,
                        ),
                    scripts =
                        linkedMapOf(
                            "build" to "rescript",
                            "clean" to "rescript clean",
                            "test" to "vitest run",
                            "prepare" to "rescript",
                            "res:build" to "rescript",
                            "res:clean" to "rescript clean",
                            "res:dev" to "rescript -w",
                        ),
                ),
            "src/Index.res" to indexRes(ctx.projectName),
            "src/__tests__/Index.test.mjs" to indexTest(),
            "README.md" to
                CommonFiles.readme(
                    ctx = ctx,
                    description = "A publishable npm package written in ReScript with TypeScript bindings via genType.",
                    scripts =
                        listOf(
                            "build" to "Compile ReScript sources",
                            "test" to "Run Vitest",
                            "res:dev" to "Watch ReScript sources",
                        ),
                    extraSections =
                        listOf(
                            "Publish" to
                                "1. Bump the version in `package.json`.\n" +
                                "2. Run `${ctx.runCmd("build")}` to ensure types are regenerated.\n" +
                                "3. Run `npm publish --access public`.",
                        ),
                ),
            ".gitignore" to CommonFiles.gitignore(extra = listOf("coverage/")),
            ".editorconfig" to CommonFiles.editorconfig(),
            ".github/workflows/ci.yml" to CommonFiles.ciWorkflow(ctx, hasBuild = true, hasTest = true),
        )

    /**
     * Back-compatible entry point used by tests and any external callers.
     */
    fun generate(projectName: String): Map<String, String> = generate(TemplateContext(projectName, PackageManager.PNPM))

    private fun indexRes(projectName: String): String {
        val dollar = '$'
        return "@genType\n" +
            "let greet = (name: string) => {\n" +
            "  `Hello from $projectName, $dollar{name}!`\n" +
            "}"
    }

    private fun indexTest(): String =
        buildString {
            appendLine("import { describe, expect, it } from \"vitest\";")
            appendLine("import { greet } from \"../Index.res.mjs\";")
            appendLine("")
            appendLine("describe(\"greet\", () => {")
            appendLine("  it(\"returns a greeting containing the supplied name\", () => {")
            appendLine("    expect(greet(\"world\")).toContain(\"world\");")
            appendLine("  });")
            appendLine("});")
        }
}
