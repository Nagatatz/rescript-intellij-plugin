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
                    dependencies = googleCloudRunDependencies(ctx.validationLibrary),
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
                ),
            "Dockerfile" to dockerfile(ctx),
            ".dockerignore" to dockerignore(),
            "src/Hono.res" to ProjectFileBuilders.honoBindings(),
            "src/HonoNodeServer.res" to ProjectFileBuilders.honoNodeServerBindings(),
            "src/Validation.res" to
                TemplateResourceLoader.load("$RESOURCE_ROOT/variants/$variantKey/src/Validation.res"),
            "src/Server.res" to TemplateResourceLoader.load("$RESOURCE_ROOT/src/Server.res"),
            "src/ServerMain.res" to TemplateResourceLoader.load("$RESOURCE_ROOT/src/ServerMain.res"),
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
            ".nvmrc" to CommonFiles.nvmrc(ctx),
            "LICENSE" to CommonFiles.mitLicense(ctx, holder = ctx.projectName),
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
        deps["@rescript/runtime"] = TemplateVersions.RESCRIPT_RUNTIME
        deps["hono"] = TemplateVersions.HONO
        deps["@hono/node-server"] = TemplateVersions.HONO_NODE_SERVER
        when (validationLibrary) {
            ValidationLibrary.ZOD -> deps["zod"] = TemplateVersions.ZOD
            ValidationLibrary.SURY -> deps["sury"] = TemplateVersions.SURY
        }
        return deps
    }

    /**
     * Returns a `.dockerignore` that prevents host-side artifacts (lockless caches, IDE state,
     * git metadata, test fixtures) from inflating the build context.
     */
    private fun dockerignore(): String =
        """
        # VCS / IDE
        .git
        .github
        .idea
        .vscode

        # Node / package manager state
        node_modules
        npm-debug.log*
        yarn-debug.log*
        yarn-error.log*
        pnpm-debug.log*
        bun-debug.log*

        # ReScript build artifacts (regenerated inside the builder stage)
        lib
        .merlin

        # Test artifacts
        coverage
        .nyc_output

        # Local env / secrets — never bake into an image
        .env
        .env.*
        !.env.example
        """.trimIndent() + "\n"

    /**
     * Builds a multi-stage Dockerfile that compiles ReScript in a `builder` stage and ships
     * only production deps + compiled `.mjs` output in the runtime stage. The runtime stage
     * runs as a non-root user (Cloud Run best practice) and pins the Node major to
     * [TemplateContext.nodeMajor] so the deployed runtime matches `.nvmrc` / `package.json`.
     */
    private fun dockerfile(ctx: TemplateContext): String {
        val isBun = ctx.packageManager == PackageManager.BUN
        val baseImage = if (isBun) "oven/bun:1-slim" else "node:${ctx.nodeMajor}-slim"
        // `--ignore-scripts` blocks transitive lifecycle scripts (postinstall etc.) from
        // running inside the Docker build, mitigating supply-chain attacks where a compromised
        // dep would otherwise execute arbitrary code with build-time access. The res-x
        // Dockerfile applies the same hardening.
        val builderInstall =
            when (ctx.packageManager) {
                PackageManager.NPM -> "npm install --ignore-scripts"
                PackageManager.PNPM -> "corepack enable && pnpm install --ignore-scripts"
                PackageManager.YARN -> "corepack enable && yarn install --ignore-scripts"
                PackageManager.BUN -> "bun install --ignore-scripts"
            }
        val runtimeInstall =
            when (ctx.packageManager) {
                PackageManager.NPM -> "npm install --omit=dev --ignore-scripts"
                PackageManager.PNPM -> "corepack enable && pnpm install --prod --ignore-scripts"
                PackageManager.YARN -> "corepack enable && yarn install --production --ignore-scripts"
                PackageManager.BUN -> "bun install --production --ignore-scripts"
            }
        // Cloud Run best practice: run as a non-root user. The official node images ship a
        // pre-created `node` user (uid 1000); oven/bun ships a `bun` user with the same uid.
        val runtimeUser = if (isBun) "bun" else "node"
        val runner = if (isBun) "bun" else "node"
        return buildString {
            appendLine("# syntax=docker/dockerfile:1.7")
            appendLine()
            appendLine("# --- Builder stage: install all deps + compile ReScript ---")
            appendLine("FROM $baseImage AS builder")
            appendLine("WORKDIR /app")
            appendLine("COPY package*.json ./")
            appendLine("RUN $builderInstall")
            appendLine("COPY . .")
            appendLine("RUN ${ctx.execCmd("rescript")}")
            appendLine()
            appendLine("# --- Runtime stage: prod deps + compiled output only ---")
            appendLine("FROM $baseImage AS runtime")
            appendLine("ENV NODE_ENV=production")
            appendLine("WORKDIR /app")
            appendLine("COPY package*.json ./")
            appendLine("RUN $runtimeInstall")
            appendLine("COPY --from=builder /app/src ./src")
            appendLine("USER $runtimeUser")
            appendLine("EXPOSE 8080")
            append("CMD [\"$runner\", \"src/ServerMain.res.mjs\"]")
        }
    }
}
