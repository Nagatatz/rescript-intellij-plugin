package com.rescript.plugin.wizard.templates

import com.rescript.plugin.wizard.PackageManager
import com.rescript.plugin.wizard.ProjectFileBuilders
import com.rescript.plugin.wizard.ValidationLibrary

/**
 * Generates project template files for a res-x application running on Bun + Vite.
 *
 * res-x (npm package `rescript-x`) is a ReScript framework for server-driven
 * web apps: JSX renders HTML on the server and HTMX drives client-side
 * interactivity. The generated starter demonstrates the two patterns developers
 * reach for most often:
 *
 * - Counter component (`src/Counter.res`) with `hx-post` endpoints for
 *   increment / decrement, swapping a `<span>` via `hx-swap="outerHTML"`.
 * - Todo form (`src/TodoForm.res`) that posts form data, validates it through
 *   [Validation] (zod or sury, selected via `TemplateContext.validationLibrary`),
 *   and either re-renders the list or sends back the form with an inline error.
 *
 * Static file content lives under `src/main/resources/templates/res-x/` and is
 * loaded via [TemplateResourceLoader]. `rescript.json` is bundled verbatim
 * (with a `{{name}}` placeholder) because res-x requires a specific
 * `jsx.module` / `compiler-flags` shape that [ProjectFileBuilders.rescriptJson]
 * does not produce.
 *
 * The template hardcodes `bun` in its npm scripts regardless of the selected
 * [PackageManager]; the PM selection only affects the install-command shown in
 * the README and the `packageManager` Corepack field. Generated projects
 * require Bun 1.1+ to be installed separately (https://bun.sh).
 */
internal object ResXTemplateFiles {
    /**
     * Generates res-x template files using the supplied [TemplateContext].
     */
    fun generate(ctx: TemplateContext): Map<String, String> {
        val variantKey = ctx.validationLibrary.variantKey()
        val validationLabel =
            when (ctx.validationLibrary) {
                ValidationLibrary.ZOD -> "zod"
                ValidationLibrary.SURY -> "sury"
            }
        val files =
            linkedMapOf(
                "rescript.json" to
                    TemplateResourceLoader.load(
                        "res-x/rescript.json",
                        mapOf("name" to ctx.projectName),
                    ),
                "package.json" to
                    ProjectFileBuilders.packageJson(
                        name = ctx.projectName,
                        isPrivate = true,
                        type = "module",
                        packageManager = ctx.packageManagerSpec(),
                        engines = mapOf("node" to ctx.nodeEngine),
                        dependencies = resXDependencies(ctx.validationLibrary),
                        devDependencies =
                            linkedMapOf(
                                "concurrently" to TemplateVersions.CONCURRENTLY,
                                "vite" to TemplateVersions.VITE,
                                "vitest" to TemplateVersions.VITEST,
                                "@vitest/coverage-v8" to TemplateVersions.VITEST_COVERAGE_V8,
                            ),
                        scripts =
                            linkedMapOf(
                                "start" to "bun run src/App.res.mjs",
                                "dev" to "concurrently \"rescript -w\" \"bun --watch run src/App.res.mjs\"",
                                "build" to "vite build",
                                "compile" to "bun build --compile src/App.res.mjs --outfile dist/app",
                                "test" to "vitest run",
                                "test:coverage" to "vitest run --coverage",
                                "res:build" to "rescript",
                                "res:clean" to "rescript clean",
                                "res:dev" to "rescript -w",
                            ),
                    ),
                "src/App.res" to TemplateResourceLoader.load("res-x/src/App.res"),
                "src/Handler.res" to TemplateResourceLoader.load("res-x/src/Handler.res"),
                "src/Layout.res" to
                    TemplateResourceLoader.load(
                        "res-x/src/Layout.res",
                        mapOf("htmxVersion" to TemplateVersions.HTMX_CDN),
                    ),
                "src/Counter.res" to TemplateResourceLoader.load("res-x/src/Counter.res"),
                "src/TodoForm.res" to
                    TemplateResourceLoader.load(
                        "res-x/src/TodoForm.res",
                        mapOf("validationLib" to validationLabel),
                    ),
                "src/Validation.res" to
                    TemplateResourceLoader.load("res-x/variants/$variantKey/src/Validation.res"),
                "src/__tests__/App.test.mjs" to
                    TemplateResourceLoader.load("res-x/src/__tests__/App.test.mjs"),
                "vite.config.js" to TemplateResourceLoader.load("res-x/vite.config.js"),
                "Dockerfile" to TemplateResourceLoader.load("res-x/Dockerfile"),
            )
        files["README.md"] =
            CommonFiles.readme(
                ctx = ctx,
                description = resXDescription(ctx.validationLibrary),
                scripts =
                    listOf(
                        "dev" to "Run ReScript and the Bun server together in watch mode",
                        "start" to "Run the compiled Bun server once",
                        "build" to "Build client assets with Vite",
                        "compile" to "Compile the Bun server into a standalone binary at dist/app",
                        "test" to "Run Vitest",
                        "res:dev" to "Watch ReScript sources",
                    ),
                extraSections =
                    listOf(
                        "Application" to
                            TemplateResourceLoader.load(
                                "res-x/readme/app.md",
                                mapOf("validationLib" to validationLabel),
                            ),
                        "HTMX" to
                            TemplateResourceLoader.load(
                                "res-x/readme/htmx.md",
                                mapOf("htmxVersion" to TemplateVersions.HTMX_CDN),
                            ),
                        "Project Layout" to
                            TemplateResourceLoader.load(
                                "res-x/readme/project-layout.md",
                                mapOf("validationLib" to validationLabel),
                            ),
                        "Deploy" to TemplateResourceLoader.load("res-x/readme/deploy.md"),
                        "Persistence" to
                            TemplateResourceLoader.load("res-x/readme/persistence.md"),
                    ),
                extraPrerequisites = listOf("Bun 1.3 or later (install from https://bun.sh)"),
            )
        files[".nvmrc"] = CommonFiles.nvmrc(ctx)
        files["LICENSE"] = CommonFiles.mitLicense(ctx, holder = ctx.projectName)
        files[".github/dependabot.yml"] = CommonFiles.dependabotYaml()
        files[".gitignore"] =
            CommonFiles.gitignore(
                extra = listOf("dist/", "build/", ".env", ".res-x-cache/"),
            )
        files[".editorconfig"] = CommonFiles.editorconfig()
        files[".github/workflows/ci.yml"] =
            CommonFiles.ciWorkflow(ctx, hasTest = true, setupBun = true)
        return files
    }

    /**
     * Back-compatible entry point used by tests and any external callers.
     */
    fun generate(projectName: String): Map<String, String> = generate(TemplateContext(projectName, PackageManager.PNPM))

    private fun resXDependencies(validationLibrary: ValidationLibrary): LinkedHashMap<String, String> {
        val deps = linkedMapOf<String, String>()
        deps["rescript"] = TemplateVersions.RESCRIPT
        deps["@rescript/core"] = TemplateVersions.RESCRIPT_CORE
        deps["rescript-x"] = TemplateVersions.RESCRIPT_X
        deps["rescript-bun"] = TemplateVersions.RESCRIPT_BUN
        when (validationLibrary) {
            ValidationLibrary.ZOD -> deps["zod"] = TemplateVersions.ZOD
            ValidationLibrary.SURY -> deps["sury"] = TemplateVersions.SURY
        }
        return deps
    }

    private fun resXDescription(validationLibrary: ValidationLibrary): String {
        val validationBlurb =
            when (validationLibrary) {
                ValidationLibrary.ZOD -> "zod"
                ValidationLibrary.SURY -> "sury"
            }
        return "A server-driven web application on Bun + Vite using res-x. " +
            "Ships a counter and a todo form that demonstrate HTMX partials, " +
            "JSX on the server, and $validationBlurb-validated form input."
    }
}
