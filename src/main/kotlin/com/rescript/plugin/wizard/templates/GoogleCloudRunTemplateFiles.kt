package com.rescript.plugin.wizard.templates

import com.rescript.plugin.wizard.PackageManager
import com.rescript.plugin.wizard.ProjectFileBuilders
import com.rescript.plugin.wizard.ValidationLibrary

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
        val variantKey = ctx.validationLibrary.variantKey()
        return mapOf(
            "rescript.json" to ProjectFileBuilders.rescriptJson(name = ctx.projectName),
            "package.json" to
                ProjectFileBuilders.packageJson(
                    name = ctx.projectName,
                    isPrivate = true,
                    type = "module",
                    packageManager = ctx.packageManagerSpec(),
                    engines = mapOf("node" to TemplateVersions.NODE_ENGINE),
                    dependencies = googleCloudRunDependencies(ctx.validationLibrary),
                    devDependencies =
                        linkedMapOf(
                            "vitest" to TemplateVersions.VITEST,
                            "@vitest/coverage-v8" to TemplateVersions.VITEST_COVERAGE_V8,
                        ),
                    scripts =
                        linkedMapOf(
                            "start" to "node src/Server.res.mjs",
                            "dev" to "node --watch src/Server.res.mjs",
                            "test" to "vitest run",
                            "test:coverage" to "vitest run --coverage",
                            "res:build" to "rescript",
                            "res:clean" to "rescript clean",
                            "res:dev" to "rescript -w",
                        ),
                ),
            "Dockerfile" to dockerfile(ctx),
            ".dockerignore" to "node_modules\nlib\n.git\n.github\n.idea\n.vscode\n",
            "src/Hono.res" to ProjectFileBuilders.honoBindings(),
            "src/HonoNodeServer.res" to ProjectFileBuilders.honoNodeServerBindings(),
            "src/Validation.res" to
                TemplateResourceLoader.load("$RESOURCE_ROOT/variants/$variantKey/src/Validation.res"),
            "src/Server.res" to TemplateResourceLoader.load("$RESOURCE_ROOT/src/Server.res"),
            "src/__tests__/Server.test.mjs" to
                TemplateResourceLoader.load("$RESOURCE_ROOT/src/__tests__/Server.test.mjs"),
            "README.md" to
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
                ),
            ".nvmrc" to CommonFiles.nvmrc(),
            "LICENSE" to CommonFiles.mitLicense(holder = ctx.projectName),
            ".github/dependabot.yml" to CommonFiles.dependabotYaml(),
            ".env.example" to
                CommonFiles.envExample(
                    listOf(
                        "Cloud Run sets PORT in production; override locally if needed" to
                            "PORT=8080",
                    ),
                ),
            ".gitignore" to CommonFiles.gitignore(extra = listOf("dist/", ".env")),
            ".editorconfig" to CommonFiles.editorconfig(),
            ".github/workflows/ci.yml" to CommonFiles.ciWorkflow(ctx, hasTest = true),
        )
    }

    /**
     * Back-compatible entry point used by tests and any external callers.
     */
    fun generate(projectName: String): Map<String, String> = generate(TemplateContext(projectName, PackageManager.PNPM))

    private fun googleCloudRunDependencies(validationLibrary: ValidationLibrary): LinkedHashMap<String, String> {
        val deps = linkedMapOf<String, String>()
        deps["rescript"] = TemplateVersions.RESCRIPT
        deps["@rescript/core"] = TemplateVersions.RESCRIPT_CORE
        deps["hono"] = TemplateVersions.HONO
        deps["@hono/node-server"] = TemplateVersions.HONO_NODE_SERVER
        when (validationLibrary) {
            ValidationLibrary.ZOD -> deps["zod"] = TemplateVersions.ZOD
            ValidationLibrary.SURY -> deps["sury"] = TemplateVersions.SURY
        }
        return deps
    }

    private fun dockerfile(ctx: TemplateContext): String {
        val installInPm =
            when (ctx.packageManager) {
                PackageManager.NPM -> "npm install --omit=dev"
                PackageManager.PNPM -> "corepack enable && pnpm install --prod --frozen-lockfile=false"
                PackageManager.YARN -> "corepack enable && yarn install --production"
            }
        return buildString {
            appendLine("FROM node:22-slim")
            appendLine("WORKDIR /app")
            appendLine("COPY package*.json ./")
            appendLine("RUN $installInPm")
            appendLine("COPY . .")
            appendLine("RUN ${ctx.execCmd("rescript")}")
            appendLine("EXPOSE 8080")
            append("CMD [\"node\", \"src/Server.res.mjs\"]")
        }
    }
}
