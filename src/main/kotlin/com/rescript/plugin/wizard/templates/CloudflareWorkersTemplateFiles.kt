package com.rescript.plugin.wizard.templates

import com.rescript.plugin.wizard.PackageManager
import com.rescript.plugin.wizard.ProjectFileBuilders

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
        val deps = linkedMapOf<String, String>()
        deps["rescript"] = TemplateVersions.RESCRIPT
        deps["@rescript/core"] = TemplateVersions.RESCRIPT_CORE
        deps["@rescript/runtime"] = TemplateVersions.RESCRIPT_RUNTIME
        deps["hono"] = TemplateVersions.HONO
        val (validationName, validationVersion) = TemplateScaffold.validationDependency(ctx)
        deps[validationName] = validationVersion
        val readme =
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
            )
        val files = linkedMapOf<String, String>()
        files["rescript.json"] =
            ProjectFileBuilders.rescriptJson(
                name = ctx.projectName,
                bsDependencies = listOf("@rescript/core") + ctx.validationBsDeps(),
            )
        files["package.json"] =
            ProjectFileBuilders.packageJson(
                name = ctx.projectName,
                isPrivate = true,
                type = "module",
                packageManager = ctx.packageManagerSpec(),
                engines = mapOf("node" to ctx.nodeEngine),
                dependencies = deps,
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
            )
        files["wrangler.jsonc"] = TemplateResourceLoader.load("$RESOURCE_ROOT/wrangler.jsonc", projectVars)
        files["src/Hono.res"] = ProjectFileBuilders.honoBindings()
        files["src/Kv.res"] = TemplateResourceLoader.load("$RESOURCE_ROOT/src/Kv.res")
        files += TemplateScaffold.validationVariant(ctx, RESOURCE_ROOT)
        files.putAll(
            TemplateScaffold.resourceFiles(
                RESOURCE_ROOT,
                listOf("src/Server.res", "src/__tests__/Server.test.mjs"),
            ),
        )
        files.putAll(
            TemplateScaffold.commonTail(
                ctx,
                readme = readme,
                gitignoreExtra = listOf(".wrangler/", "dist/", ".dev.vars"),
                ciHasTest = true,
            ),
        )
        return files
    }

    /**
     * Back-compatible entry point used by tests and any external callers.
     */
    fun generate(projectName: String): Map<String, String> = generate(TemplateContext(projectName, PackageManager.PNPM))
}
