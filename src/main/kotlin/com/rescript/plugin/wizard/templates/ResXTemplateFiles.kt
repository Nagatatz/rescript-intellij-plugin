package com.rescript.plugin.wizard.templates

import com.rescript.plugin.wizard.PackageManager
import com.rescript.plugin.wizard.ProjectFileBuilders

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
        val readme =
            CommonFiles.readme(
                ctx = ctx,
                description = resXDescription(variantKey),
                scripts =
                    listOf(
                        "dev" to "Run ReScript and the Bun server together in watch mode",
                        "start" to "Run the compiled Bun server once",
                        "build" to "Build client assets with Vite",
                        "compile" to "Compile the Bun server into a standalone binary at dist/app",
                        "test" to "Run bun test",
                        "res:dev" to "Watch ReScript sources",
                    ),
                extraSections =
                    listOf(
                        "Application" to
                            TemplateResourceLoader.load(
                                "res-x/readme/app.md",
                                mapOf("validationLib" to variantKey),
                            ),
                        "HTMX" to
                            TemplateResourceLoader.load(
                                "res-x/readme/htmx.md",
                                mapOf("htmxVersion" to TemplateVersions.HTMX_CDN),
                            ),
                        "Project Layout" to
                            TemplateResourceLoader.load(
                                "res-x/readme/project-layout.md",
                                mapOf("validationLib" to variantKey),
                            ),
                        "Deploy" to TemplateResourceLoader.load("res-x/readme/deploy.md"),
                        "Persistence" to
                            TemplateResourceLoader.load("res-x/readme/persistence.md"),
                    ),
                extraPrerequisites = listOf("Bun 1.3 or later (install from https://bun.sh)"),
            )
        val files =
            linkedMapOf(
                "rescript.json" to
                    TemplateResourceLoader.load(
                        "res-x/rescript.json",
                        mapOf(
                            "name" to ctx.projectName,
                            // Append `, "sury"` only when the SURY variant is selected so
                            // the dependencies array stays valid JSON for both variants.
                            "maybeSuryDep" to
                                ctx.validationBsDeps().joinToString("") { ", \"$it\"" },
                        ),
                    ),
                "package.json" to
                    ProjectFileBuilders.packageJson(
                        name = ctx.projectName,
                        isPrivate = true,
                        type = "module",
                        packageManager = ctx.packageManagerSpec(),
                        engines = mapOf("node" to ctx.nodeEngine),
                        dependencies = resXDependencies(ctx),
                        // res-x uses Bun's built-in test runner instead of vitest because
                        // the compiled output dereferences the global `Bun` object via
                        // rescript-bun — vitest under Node would crash with `Bun is not
                        // defined`. `bun test` ships its own coverage reporter so no
                        // separate vitest devDeps are needed.
                        devDependencies =
                            linkedMapOf(
                                "concurrently" to TemplateVersions.CONCURRENTLY,
                                "vite" to TemplateVersions.VITE,
                            ),
                        scripts =
                            linkedMapOf(
                                "start" to "bun run src/App.res.mjs",
                                "dev" to "concurrently \"rescript -w\" \"bun --watch run src/App.res.mjs\"",
                                "build" to "vite build",
                                "compile" to "bun build --compile src/App.res.mjs --outfile dist/app",
                                "test" to "bun test",
                                "test:coverage" to "bun test --coverage",
                                "res:build" to "rescript",
                                "res:clean" to "rescript clean",
                                "res:dev" to "rescript -w",
                            ),
                    ),
            )
        files.putAll(TemplateScaffold.resourceFiles("res-x", listOf("src/App.res", "src/Handler.res")))
        files["src/Layout.res"] =
            TemplateResourceLoader.load(
                "res-x/src/Layout.res",
                mapOf("htmxVersion" to TemplateVersions.HTMX_CDN),
            )
        files["src/Counter.res"] = TemplateResourceLoader.load("res-x/src/Counter.res")
        files["src/TodoForm.res"] =
            TemplateResourceLoader.load(
                "res-x/src/TodoForm.res",
                mapOf("validationLib" to variantKey),
            )
        val (validationTarget, validationContent) = TemplateScaffold.validationVariant(ctx, "res-x")
        files[validationTarget] = validationContent
        files.putAll(
            TemplateScaffold.resourceFiles(
                "res-x",
                listOf(
                    "src/__tests__/App.test.mjs",
                    "vite.config.js",
                    "Dockerfile",
                ),
            ),
        )
        files.putAll(
            TemplateScaffold.commonTail(
                ctx,
                readme = readme,
                gitignoreExtra = listOf("dist/", "build/", ".env", ".res-x-cache/"),
                ciHasTest = true,
                ciSetupBun = true,
            ),
        )
        return files
    }

    /**
     * Back-compatible entry point used by tests and any external callers.
     */
    fun generate(projectName: String): Map<String, String> = generate(TemplateContext(projectName, PackageManager.PNPM))

    private fun resXDependencies(ctx: TemplateContext): LinkedHashMap<String, String> {
        val deps = linkedMapOf<String, String>()
        deps["rescript"] = TemplateVersions.RESCRIPT
        deps["@rescript/core"] = TemplateVersions.RESCRIPT_CORE
        deps["@rescript/runtime"] = TemplateVersions.RESCRIPT_RUNTIME
        deps["rescript-x"] = TemplateVersions.RESCRIPT_X
        deps["rescript-bun"] = TemplateVersions.RESCRIPT_BUN
        val (name, version) = TemplateScaffold.validationDependency(ctx)
        deps[name] = version
        return deps
    }

    private fun resXDescription(validationBlurb: String): String =
        "A server-driven web application on Bun + Vite using res-x. " +
            "Ships a counter and a todo form that demonstrate HTMX partials, " +
            "JSX on the server, and $validationBlurb-validated form input."
}
