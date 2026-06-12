package com.rescript.plugin.wizard.templates

import com.rescript.plugin.wizard.PackageManager
import com.rescript.plugin.wizard.ProjectFileBuilders

/**
 * Generates project template files for a Google Cloud Run Hono service.
 *
 * Container-based deployment with a multi-stage Dockerfile, README, .gitignore, .editorconfig,
 * and CI workflow. The Dockerfile honors the selected package manager so the production image
 * uses the same toolchain as local development. Runtime HTTP body validation is provided by
 * `Validation.res` (zod or sury, selected in Wizard via [TemplateContext.validationLibrary]).
 */
internal object GoogleCloudRunTemplateFiles {
    private const val RESOURCE_ROOT = "google-cloud-run"

    /**
     * Generates Google Cloud Run template files using the supplied [TemplateContext].
     */
    fun generate(ctx: TemplateContext): Map<String, String> {
        val projectVars = mapOf("projectName" to ctx.projectName)
        val deps = linkedMapOf<String, String>()
        deps["rescript"] = TemplateVersions.RESCRIPT
        deps["@rescript/core"] = TemplateVersions.RESCRIPT_CORE
        deps["@rescript/runtime"] = TemplateVersions.RESCRIPT_RUNTIME
        deps["hono"] = TemplateVersions.HONO
        deps["@hono/node-server"] = TemplateVersions.HONO_NODE_SERVER
        val (validationName, validationVersion) = TemplateScaffold.validationDependency(ctx)
        deps[validationName] = validationVersion
        val readme =
            CommonFiles.readme(
                ctx = ctx,
                description =
                    "A Hono service deployable to Google Cloud Run. Ships POST/GET endpoints, " +
                        "environment-variable reading, and a Cloud SQL integration recipe.",
                scripts =
                    listOf(
                        "dev" to "Run locally with file watching",
                        "start" to "Run the server once",
                        "test" to "Run Vitest",
                        "res:dev" to "Watch ReScript sources",
                    ),
                extraSections =
                    listOf(
                        "API" to TemplateResourceLoader.load("$RESOURCE_ROOT/readme/api.md"),
                        "Environment" to TemplateResourceLoader.load("$RESOURCE_ROOT/readme/environment.md"),
                        "Deploy" to TemplateResourceLoader.load("$RESOURCE_ROOT/readme/deploy.md", projectVars),
                        "Cloud SQL Recipe" to
                            TemplateResourceLoader.load("$RESOURCE_ROOT/readme/cloud-sql.md"),
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
                        "vitest" to TemplateVersions.VITEST,
                        "@vitest/coverage-v8" to TemplateVersions.VITEST_COVERAGE_V8,
                    ),
                scripts =
                    linkedMapOf(
                        // ServerMain.res calls `Server.start()`; importing
                        // Server.res alone has no side effects so vitest stays happy.
                        "start" to "node src/ServerMain.res.mjs",
                        "dev" to "node --watch src/ServerMain.res.mjs",
                        "test" to "vitest run",
                        "test:coverage" to "vitest run --coverage",
                        "res:build" to "rescript",
                        "res:clean" to "rescript clean",
                        "res:dev" to "rescript -w",
                    ),
            )
        files["Dockerfile"] = CommonFiles.serverDockerfile(ctx, port = 8080)
        files[".dockerignore"] = CommonFiles.dockerignore()
        files["src/Hono.res"] = ProjectFileBuilders.honoBindings()
        files["src/HonoNodeServer.res"] = ProjectFileBuilders.honoNodeServerBindings()
        files += TemplateScaffold.validationVariant(ctx, RESOURCE_ROOT)
        files.putAll(
            TemplateScaffold.resourceFiles(
                RESOURCE_ROOT,
                listOf("src/Server.res", "src/ServerMain.res", "src/__tests__/Server.test.mjs"),
            ),
        )
        files[".env.example"] =
            CommonFiles.envExample(
                listOf(
                    "Cloud Run sets PORT in production; override locally if needed" to
                        "PORT=8080",
                ),
            )
        files.putAll(
            TemplateScaffold.commonTail(
                ctx,
                readme = readme,
                gitignoreExtra = listOf("dist/", ".env"),
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
