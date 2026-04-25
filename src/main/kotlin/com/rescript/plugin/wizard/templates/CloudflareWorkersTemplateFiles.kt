package com.rescript.plugin.wizard.templates

import com.rescript.plugin.wizard.PackageManager
import com.rescript.plugin.wizard.ProjectFileBuilders
import com.rescript.plugin.wizard.ValidationLibrary

/**
 * Generates project template files for a Cloudflare Workers Hono service.
 *
 * The generated app goes beyond "hello world": it wires POST + GET against a KV namespace so
 * users have a working storage example out of the box (add/list greetings). `wrangler.jsonc`
 * pre-declares the KV binding so `wrangler dev` boots with a real local store.
 * Runtime HTTP body validation is provided by `Validation.res` (zod or sury, selected in
 * Wizard via [TemplateContext.validationLibrary]).
 */
internal object CloudflareWorkersTemplateFiles {
    private const val RESOURCE_ROOT = "cloudflare-workers"

    /**
     * Generates Cloudflare Workers template files using the supplied [TemplateContext].
     */
    fun generate(ctx: TemplateContext): Map<String, String> {
        val projectVars = mapOf("projectName" to ctx.projectName)
        val deployVars = mapOf("cmdDeploy" to ctx.runCmd("deploy"))
        val variantKey = ctx.validationLibrary.variantKey()
        return mapOf(
            "rescript.json" to
                ProjectFileBuilders.rescriptJson(
                    name = ctx.projectName,
                    bsDependencies = listOf("@rescript/core") + ctx.validationBsDeps(),
                ),
            "package.json" to
                ProjectFileBuilders.packageJson(
                    name = ctx.projectName,
                    isPrivate = true,
                    type = "module",
                    packageManager = ctx.packageManagerSpec(),
                    engines = mapOf("node" to ctx.nodeEngine),
                    dependencies = cloudflareWorkersDependencies(ctx.validationLibrary),
                    devDependencies =
                        linkedMapOf(
                            "wrangler" to TemplateVersions.WRANGLER,
                            "vitest" to TemplateVersions.VITEST,
                            "@vitest/coverage-v8" to TemplateVersions.VITEST_COVERAGE_V8,
                        ),
                    scripts =
                        linkedMapOf(
                            "dev" to "wrangler dev",
                            "deploy" to "wrangler deploy",
                            "test" to "vitest run",
                            "test:coverage" to "vitest run --coverage",
                            "res:build" to "rescript",
                            "res:clean" to "rescript clean",
                            "res:dev" to "rescript -w",
                        ),
                ),
            "wrangler.jsonc" to TemplateResourceLoader.load("$RESOURCE_ROOT/wrangler.jsonc", projectVars),
            "src/Hono.res" to ProjectFileBuilders.honoBindings(),
            "src/Kv.res" to TemplateResourceLoader.load("$RESOURCE_ROOT/src/Kv.res"),
            "src/Validation.res" to
                TemplateResourceLoader.load("$RESOURCE_ROOT/variants/$variantKey/src/Validation.res"),
            "src/Server.res" to TemplateResourceLoader.load("$RESOURCE_ROOT/src/Server.res"),
            "src/__tests__/Server.test.mjs" to
                TemplateResourceLoader.load("$RESOURCE_ROOT/src/__tests__/Server.test.mjs"),
            "README.md" to
                CommonFiles.readme(
                    ctx = ctx,
                    description =
                        "A Cloudflare Workers service powered by Hono, demonstrating POST/GET against " +
                            "a Workers KV namespace.",
                    scripts =
                        listOf(
                            "dev" to "Run wrangler dev locally",
                            "deploy" to "Deploy with `wrangler deploy`",
                            "test" to "Run Vitest",
                            "res:dev" to "Watch ReScript sources",
                        ),
                    extraSections =
                        listOf(
                            "API" to TemplateResourceLoader.load("$RESOURCE_ROOT/readme/api.md"),
                            "KV Setup" to TemplateResourceLoader.load("$RESOURCE_ROOT/readme/kv-setup.md"),
                            "Deploy" to TemplateResourceLoader.load("$RESOURCE_ROOT/readme/deploy.md", deployVars),
                        ),
                ),
            ".nvmrc" to CommonFiles.nvmrc(ctx),
            "LICENSE" to CommonFiles.mitLicense(ctx, holder = ctx.projectName),
            ".github/dependabot.yml" to CommonFiles.dependabotYaml(),
            ".gitignore" to CommonFiles.gitignore(extra = listOf(".wrangler/", "dist/", ".dev.vars")),
            ".editorconfig" to CommonFiles.editorconfig(),
            ".github/workflows/ci.yml" to CommonFiles.ciWorkflow(ctx, hasTest = true),
        )
    }

    /**
     * Back-compatible entry point used by tests and any external callers.
     */
    fun generate(projectName: String): Map<String, String> = generate(TemplateContext(projectName, PackageManager.PNPM))

    private fun cloudflareWorkersDependencies(validationLibrary: ValidationLibrary): LinkedHashMap<String, String> {
        val deps = linkedMapOf<String, String>()
        deps["rescript"] = TemplateVersions.RESCRIPT
        deps["@rescript/core"] = TemplateVersions.RESCRIPT_CORE
        deps["@rescript/runtime"] = TemplateVersions.RESCRIPT_RUNTIME
        deps["hono"] = TemplateVersions.HONO
        when (validationLibrary) {
            ValidationLibrary.ZOD -> deps["zod"] = TemplateVersions.ZOD
            ValidationLibrary.SURY -> deps["sury"] = TemplateVersions.SURY
        }
        return deps
    }
}
