package com.rescript.plugin.wizard.templates

import com.rescript.plugin.wizard.PackageManager
import com.rescript.plugin.wizard.ValidationLibrary
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class GoogleCloudRunTemplateFilesTest {
    private val suryCtx = TemplateContext("svc", PackageManager.PNPM, ValidationLibrary.SURY)

    @Test
    fun `Dockerfile uses pnpm when pnpm is selected`() {
        val ctx = TemplateContext("svc", PackageManager.PNPM)
        val dockerfile =
            GoogleCloudRunTemplateFiles.generate(
                TemplateContext("svc", PackageManager.PNPM),
            )["Dockerfile"]!!
        assertTrue(dockerfile.contains("corepack enable && pnpm install"))
        assertTrue(dockerfile.contains("pnpm exec rescript"))
    }

    @Test
    fun `Dockerfile uses npm when npm is selected`() {
        val ctx = TemplateContext("svc", PackageManager.NPM)
        val dockerfile = GoogleCloudRunTemplateFiles.generate(ctx)["Dockerfile"]!!
        assertTrue(dockerfile.contains("npm install --omit=dev"))
        assertTrue(dockerfile.contains("npx rescript"))
    }

    @Test
    fun `Dockerfile switches to oven-sh bun base and bun install for BUN`() {
        val ctx = TemplateContext("svc", PackageManager.BUN)
        val dockerfile = GoogleCloudRunTemplateFiles.generate(ctx)["Dockerfile"]!!
        assertTrue(dockerfile.contains("FROM oven/bun:1-slim"))
        assertTrue(dockerfile.contains("bun install --production"))
        assertTrue(dockerfile.contains("bunx rescript"))
        assertTrue(dockerfile.contains("CMD [\"bun\", \"src/ServerMain.res.mjs\"]"))
        assertTrue(dockerfile.contains("USER bun"))
        assertFalse(dockerfile.contains("CMD [\"node\""))
    }

    @Test
    fun `Dockerfile is multi-stage with builder and runtime stages`() {
        val files = GoogleCloudRunTemplateFiles.generate(TemplateContext("svc", PackageManager.PNPM))
        val dockerfile = files["Dockerfile"]!!
        assertTrue(dockerfile.contains("AS builder"))
        assertTrue(dockerfile.contains("AS runtime"))
        assertTrue(dockerfile.contains("COPY --from=builder"))
        // Runtime stage must reinstall production-only deps, not copy node_modules from builder.
        assertFalse(dockerfile.contains("COPY --from=builder /app/node_modules"))
    }

    @Test
    fun `Dockerfile runtime stage runs as a non-root user and sets NODE_ENV`() {
        val files = GoogleCloudRunTemplateFiles.generate(TemplateContext("svc", PackageManager.PNPM))
        val dockerfile = files["Dockerfile"]!!
        assertTrue(dockerfile.contains("USER node"))
        assertTrue(dockerfile.contains("ENV NODE_ENV=production"))
    }

    @Test
    fun `Dockerfile pins base image to ctx nodeMajor instead of hardcoded 22`() {
        // ctx.nodeMajor defaults to TemplateVersions.NODE_MAJOR; verify the Dockerfile follows it.
        val files = GoogleCloudRunTemplateFiles.generate(TemplateContext("svc", PackageManager.PNPM))
        val dockerfile = files["Dockerfile"]!!
        assertTrue(dockerfile.contains("FROM node:${TemplateVersions.NODE_MAJOR}-slim AS builder"))
        assertTrue(dockerfile.contains("FROM node:${TemplateVersions.NODE_MAJOR}-slim AS runtime"))
    }

    @Test
    fun `dockerignore excludes ReScript build output and env files but keeps env example`() {
        val files = GoogleCloudRunTemplateFiles.generate(TemplateContext("svc", PackageManager.PNPM))
        val dockerignore = files[".dockerignore"]!!
        assertTrue(dockerignore.contains("lib"))
        assertTrue(dockerignore.contains("node_modules"))
        assertTrue(dockerignore.contains(".env"))
        assertTrue(dockerignore.contains("!.env.example"))
        assertTrue(dockerignore.contains("coverage"))
    }

    @Test
    fun `README deploy section documents multi-stage layout and non-root runtime`() {
        val readme = GoogleCloudRunTemplateFiles.generate(TemplateContext("svc", PackageManager.PNPM))["README.md"]!!
        assertTrue(readme.contains("multi-stage"))
        assertTrue(readme.contains("non-root"))
        assertTrue(readme.contains("NODE_ENV=production"))
    }

    @Test
    fun `template ships dockerignore, README, and CI workflow`() {
        val files = GoogleCloudRunTemplateFiles.generate(TemplateContext("svc", PackageManager.PNPM))
        assertTrue(files.containsKey(".dockerignore"))
        assertTrue(files.containsKey("README.md"))
        assertTrue(files.containsKey(".github/workflows/ci.yml"))
        val readme = files["README.md"]!!
        assertTrue(readme.contains("gcloud run deploy"))
    }

    @Test
    fun `server reads PORT env var and exposes POST echo endpoint`() {
        val ctx = TemplateContext("svc", PackageManager.PNPM)
        val server =
            GoogleCloudRunTemplateFiles.generate(
                TemplateContext("svc", PackageManager.PNPM),
            )["src/Server.res"]!!
        assertTrue(server.contains("process.env"))
        assertTrue(server.contains("PORT"))
        assertTrue(server.contains("Hono.post"))
        assertTrue(server.contains("/echo"))
    }

    @Test
    fun `server validates POST body via Validation and returns 400 on Error`() {
        val files = GoogleCloudRunTemplateFiles.generate(TemplateContext("svc", PackageManager.PNPM))
        val server = files["src/Server.res"]!!
        assertTrue(server.contains("Validation.parseEchoPayload"))
        assertTrue(server.contains("Hono.status(400)"))
    }

    @Test
    fun `zod variant ships zod Validation module and zod dependency`() {
        val files = GoogleCloudRunTemplateFiles.generate(TemplateContext("svc", PackageManager.PNPM))
        val validation = files["src/Validation.res"]!!
        assertTrue(validation.contains("@module(\"zod\")"))
        assertTrue(validation.contains("parseEchoPayload"))
        val pkg = files["package.json"]!!
        assertTrue(pkg.contains("\"zod\": \"${TemplateVersions.ZOD}\""))
        assertFalse(pkg.contains("\"sury\""))
    }

    @Test
    fun `sury variant ships sury Validation module and sury dependency`() {
        val files = GoogleCloudRunTemplateFiles.generate(suryCtx)
        val validation = files["src/Validation.res"]!!
        assertTrue(validation.contains("S.object"))
        assertTrue(validation.contains("S.parseOrThrow"))
        val pkg = files["package.json"]!!
        assertTrue(pkg.contains("\"sury\": \"${TemplateVersions.SURY}\""))
        assertFalse(pkg.contains("\"zod\":"))
    }

    @Test
    fun `package json declares test script and vitest devDep`() {
        val pkg = GoogleCloudRunTemplateFiles.generate(TemplateContext("svc", PackageManager.PNPM))["package.json"]!!
        assertTrue(pkg.contains("\"test\": \"vitest run\""))
        assertTrue(pkg.contains("\"vitest\": \"${TemplateVersions.VITEST}\""))
    }

    @Test
    fun `ships a vitest smoke test`() {
        val files = GoogleCloudRunTemplateFiles.generate(TemplateContext("svc", PackageManager.PNPM))
        assertTrue(files.containsKey("src/__tests__/Server.test.mjs"))
        assertTrue(files["src/__tests__/Server.test.mjs"]!!.contains("import(\"../Server.res.mjs\")"))
    }

    @Test
    fun `README documents Cloud SQL recipe and environment section`() {
        val readme = GoogleCloudRunTemplateFiles.generate(TemplateContext("svc", PackageManager.PNPM))["README.md"]!!
        assertTrue(readme.contains("## Environment"))
        assertTrue(readme.contains("## Cloud SQL Recipe"))
        assertTrue(readme.contains("@google-cloud/cloud-sql-connector"))
    }

    @Test
    fun `ships nvmrc, LICENSE, and dependabot config`() {
        val files = GoogleCloudRunTemplateFiles.generate(TemplateContext("svc", PackageManager.PNPM))
        assertTrue(files.containsKey(".nvmrc"))
        assertTrue(files.containsKey("LICENSE"))
        assertTrue(files.containsKey(".github/dependabot.yml"))
        assertTrue(files[".nvmrc"]!!.contains(TemplateVersions.NODE_MAJOR))
        assertTrue(files["LICENSE"]!!.contains("MIT License"))
        assertTrue(files["LICENSE"]!!.contains("svc"))
        assertTrue(files[".github/dependabot.yml"]!!.contains("package-ecosystem: \"npm\""))
    }

    @Test
    fun `package json declares test coverage script and provider`() {
        val pkg = GoogleCloudRunTemplateFiles.generate(TemplateContext("svc", PackageManager.PNPM))["package.json"]!!
        assertTrue(pkg.contains("\"test:coverage\""))
        assertTrue(pkg.contains("\"@vitest/coverage-v8\""))
    }

    @Test
    fun `ships env example documenting PORT`() {
        val files = GoogleCloudRunTemplateFiles.generate(TemplateContext("svc", PackageManager.PNPM))
        assertTrue(files.containsKey(".env.example"))
        assertTrue(files[".env.example"]!!.contains("PORT"))
    }

    @Test
    fun `Server res defines a start function and never calls serve at top level`() {
        val files = GoogleCloudRunTemplateFiles.generate(TemplateContext("svc", PackageManager.PNPM))
        val server = files["src/Server.res"]!!
        assertTrue(server.contains("let start = () => {"))
        val topLevelLines = server.lines().filter { !it.startsWith("  ") && !it.startsWith("//") }
        assertFalse(
            topLevelLines.any { it.trimStart().startsWith("HonoNodeServer.serve") },
            "Server.res must wrap serve() so vitest can import it without binding a port",
        )
    }

    @Test
    fun `ServerMain res ships and is the dev start entry point`() {
        val files = GoogleCloudRunTemplateFiles.generate(TemplateContext("svc", PackageManager.PNPM))
        assertTrue(files.containsKey("src/ServerMain.res"))
        assertTrue(files["src/ServerMain.res"]!!.contains("Server.start()"))
        val pkg = files["package.json"]!!
        assertTrue(pkg.contains("\"start\": \"node src/ServerMain.res.mjs\""))
        assertTrue(pkg.contains("\"dev\": \"node --watch src/ServerMain.res.mjs\""))
    }
}
