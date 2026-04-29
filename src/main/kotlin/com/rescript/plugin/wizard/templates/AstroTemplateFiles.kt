package com.rescript.plugin.wizard.templates

import com.rescript.plugin.wizard.PackageManager
import com.rescript.plugin.wizard.ProjectFileBuilders

/**
 * Generates project template files for an Astro content site with React Islands.
 *
 * The template wires `@astrojs/react` so that ReScript components compiled to
 * `.res.mjs` can be embedded in `.astro` pages with `client:load` / `client:visible`
 * directives. Sources stay under `src/` to follow Astro's conventions; the validation
 * library combo is hidden because Astro endpoints / Actions usually adopt validation
 * libraries on a per-route basis.
 */
internal object AstroTemplateFiles {
    private const val RESOURCE_ROOT = "astro"

    /**
     * Generates Astro template files using the supplied [TemplateContext].
     */
    fun generate(ctx: TemplateContext): Map<String, String> {
        val projectVars = mapOf("projectName" to ctx.projectName)
        return mapOf(
            "rescript.json" to
                ProjectFileBuilders.rescriptJson(
                    name = ctx.projectName,
                    bsDependencies = listOf("@rescript/core", "@rescript/react"),
                    includeJsx = true,
                ),
            "package.json" to
                ProjectFileBuilders.packageJson(
                    name = ctx.projectName,
                    isPrivate = true,
                    type = "module",
                    packageManager = ctx.packageManagerSpec(),
                    engines = mapOf("node" to ctx.nodeEngine),
                    dependencies = astroDependencies(),
                    scripts =
                        linkedMapOf(
                            "dev" to "concurrently \"rescript -w\" \"astro dev\"",
                            "build" to "rescript && astro build",
                            "preview" to "astro preview",
                            "test" to "vitest run",
                            "test:coverage" to "vitest run --coverage",
                            "res:build" to "rescript",
                            "res:clean" to "rescript clean",
                            "res:dev" to "rescript -w",
                        ),
                    devDependencies =
                        linkedMapOf(
                            "concurrently" to TemplateVersions.CONCURRENTLY,
                            "vitest" to TemplateVersions.VITEST,
                            "@vitest/coverage-v8" to TemplateVersions.VITEST_COVERAGE_V8,
                            "typescript" to TemplateVersions.TYPESCRIPT,
                            "@types/react" to TemplateVersions.REACT_TYPES,
                            "@types/react-dom" to TemplateVersions.REACT_DOM_TYPES,
                            "@types/node" to TemplateVersions.NODE_TYPES,
                        ),
                ),
            "astro.config.mjs" to TemplateResourceLoader.load("$RESOURCE_ROOT/astro.config.mjs"),
            "tsconfig.json" to TemplateResourceLoader.load("$RESOURCE_ROOT/tsconfig.json"),
            "rescript-modules.d.ts" to TemplateResourceLoader.load("$RESOURCE_ROOT/rescript-modules.d.ts"),
            "src/pages/index.astro" to
                TemplateResourceLoader.load("$RESOURCE_ROOT/src/pages/index.astro", projectVars),
            "src/components/Counter.res" to
                TemplateResourceLoader.load("$RESOURCE_ROOT/src/components/Counter.res"),
            "src/components/StaticGreeting.res" to
                TemplateResourceLoader.load("$RESOURCE_ROOT/src/components/StaticGreeting.res"),
            "src/__tests__/Counter.test.mjs" to
                TemplateResourceLoader.load("$RESOURCE_ROOT/src/__tests__/Counter.test.mjs"),
            "README.md" to
                CommonFiles.readme(
                    ctx = ctx,
                    description =
                        "An Astro content site that hydrates ReScript React Islands on demand. " +
                            "Static markup ships zero JS; interactive components opt in via client:* directives.",
                    scripts =
                        listOf(
                            "dev" to "Start Astro dev server with the ReScript watcher",
                            "build" to "Compile ReScript and run astro build",
                            "preview" to "Serve the built site locally",
                            "test" to "Run Vitest",
                        ),
                    extraSections =
                        listOf(
                            "React Islands" to
                                TemplateResourceLoader.load("$RESOURCE_ROOT/readme/islands.md"),
                            "Static vs SSR" to
                                TemplateResourceLoader.load("$RESOURCE_ROOT/readme/static-vs-ssr.md"),
                        ),
                ),
            ".nvmrc" to CommonFiles.nvmrc(ctx),
            "LICENSE" to CommonFiles.mitLicense(ctx, holder = ctx.projectName),
            ".github/dependabot.yml" to CommonFiles.dependabotYaml(),
            ".gitignore" to
                CommonFiles.gitignore(
                    extra =
                        listOf(
                            ".astro/",
                            "dist/",
                            ".env*.local",
                        ),
                ),
            ".editorconfig" to CommonFiles.editorconfig(),
            ".github/workflows/ci.yml" to CommonFiles.ciWorkflow(ctx, hasBuild = true, hasTest = true),
        )
    }

    /**
     * Back-compatible entry point used by tests and any external callers.
     */
    fun generate(projectName: String): Map<String, String> = generate(TemplateContext(projectName, PackageManager.PNPM))

    private fun astroDependencies(): LinkedHashMap<String, String> {
        val deps = linkedMapOf<String, String>()
        deps["rescript"] = TemplateVersions.RESCRIPT
        deps["@rescript/core"] = TemplateVersions.RESCRIPT_CORE
        deps["@rescript/runtime"] = TemplateVersions.RESCRIPT_RUNTIME
        deps["@rescript/react"] = TemplateVersions.RESCRIPT_REACT
        deps["react"] = TemplateVersions.REACT
        deps["react-dom"] = TemplateVersions.REACT_DOM
        deps["astro"] = TemplateVersions.ASTRO
        deps["@astrojs/react"] = TemplateVersions.ASTROJS_REACT
        deps["@astrojs/node"] = TemplateVersions.ASTROJS_NODE
        return deps
    }
}
