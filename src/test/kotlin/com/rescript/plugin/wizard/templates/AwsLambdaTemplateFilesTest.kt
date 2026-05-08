package com.rescript.plugin.wizard.templates

import com.rescript.plugin.wizard.PackageManager
import com.rescript.plugin.wizard.ValidationLibrary
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class AwsLambdaTemplateFilesTest {
    private val ctx = TemplateContext("fn", PackageManager.PNPM)
    private val suryCtx = TemplateContext("fn", PackageManager.PNPM, ValidationLibrary.SURY)

    @Test
    fun `package json includes esbuild from TemplateVersions`() {
        val pkg = AwsLambdaTemplateFiles.generate(ctx)["package.json"]!!
        assertTrue(pkg.contains("\"esbuild\": \"${TemplateVersions.ESBUILD}\""))
    }

    @Test
    fun `build script chains rescript and bundle`() {
        val pkg = AwsLambdaTemplateFiles.generate(ctx)["package.json"]!!
        assertTrue(pkg.contains("\"build\""))
        assertTrue(pkg.contains("rescript &&"))
    }

    @Test
    fun `README documents lambda deploy notes`() {
        val readme = AwsLambdaTemplateFiles.generate(ctx)["README.md"]!!
        assertTrue(readme.contains("index.handler"))
        assertTrue(readme.contains("Lambda") || readme.contains("lambda"))
    }

    @Test
    fun `server ships POST + path param orders endpoints`() {
        val server = AwsLambdaTemplateFiles.generate(ctx)["src/Server.res"]!!
        assertTrue(server.contains("Hono.post"))
        assertTrue(server.contains("/orders"))
        assertTrue(server.contains("paramAt"))
        assertTrue(server.contains("jsonBody"))
    }

    @Test
    fun `server validates POST body via Validation and returns 400 on Error`() {
        val server = AwsLambdaTemplateFiles.generate(ctx)["src/Server.res"]!!
        assertTrue(server.contains("Validation.parseCreateOrderPayload"))
        assertTrue(server.contains("Hono.status(400)"))
    }

    @Test
    fun `zod variant ships zod Validation module and zod dependency`() {
        val files = AwsLambdaTemplateFiles.generate(ctx)
        assertTrue(files.containsKey("src/Validation.res"))
        val validation = files["src/Validation.res"]!!
        assertTrue(validation.contains("@module(\"zod\")"))
        assertTrue(validation.contains("parseCreateOrderPayload"))
        val pkg = files["package.json"]!!
        assertTrue(pkg.contains("\"zod\": \"${TemplateVersions.ZOD}\""))
        assertFalse(pkg.contains("\"sury\""))
    }

    @Test
    fun `sury variant ships sury Validation module and sury dependency`() {
        val files = AwsLambdaTemplateFiles.generate(suryCtx)
        val validation = files["src/Validation.res"]!!
        assertTrue(validation.contains("S.object"))
        assertTrue(validation.contains("S.parseOrThrow"))
        assertFalse(validation.contains("@module(\"zod\")"))
        val pkg = files["package.json"]!!
        assertTrue(pkg.contains("\"sury\": \"${TemplateVersions.SURY}\""))
        assertFalse(pkg.contains("\"zod\":"))
    }

    @Test
    fun `package json declares test script and vitest devDep`() {
        val pkg = AwsLambdaTemplateFiles.generate(ctx)["package.json"]!!
        assertTrue(pkg.contains("\"test\": \"vitest run\""))
        assertTrue(pkg.contains("\"vitest\": \"${TemplateVersions.VITEST}\""))
    }

    @Test
    fun `ships a vitest smoke test`() {
        val files = AwsLambdaTemplateFiles.generate(ctx)
        assertTrue(files.containsKey("src/__tests__/Server.test.mjs"))
        assertTrue(files["src/__tests__/Server.test.mjs"]!!.contains("import(\"../Server.res.mjs\")"))
    }

    @Test
    fun `README includes DynamoDB recipe`() {
        val readme = AwsLambdaTemplateFiles.generate(ctx)["README.md"]!!
        assertTrue(readme.contains("DynamoDB"))
        assertTrue(readme.contains("@aws-sdk/lib-dynamodb"))
    }

    @Test
    fun `ships nvmrc, LICENSE, and dependabot config`() {
        val files = AwsLambdaTemplateFiles.generate(ctx)
        assertTrue(files.containsKey(".nvmrc"))
        assertTrue(files.containsKey("LICENSE"))
        assertTrue(files.containsKey(".github/dependabot.yml"))
        assertTrue(files[".nvmrc"]!!.contains(TemplateVersions.NODE_MAJOR))
        assertTrue(files["LICENSE"]!!.contains("MIT License"))
        assertTrue(files["LICENSE"]!!.contains("fn"))
        assertTrue(files[".github/dependabot.yml"]!!.contains("package-ecosystem: \"npm\""))
    }

    @Test
    fun `package json declares test coverage script and provider`() {
        val pkg = AwsLambdaTemplateFiles.generate(ctx)["package.json"]!!
        assertTrue(pkg.contains("\"test:coverage\""))
        assertTrue(pkg.contains("\"@vitest/coverage-v8\""))
    }

    @Test
    fun `gitignore excludes SAM CLI build artifacts`() {
        val gitignore = AwsLambdaTemplateFiles.generate(ctx)[".gitignore"]!!
        assertTrue(gitignore.contains(".aws-sam/"))
    }

    @Test
    fun `README documents bundling strategy with esbuild --external and Lambda Layers`() {
        val readme = AwsLambdaTemplateFiles.generate(ctx)["README.md"]!!
        assertTrue(readme.contains("## Bundling Strategy"))
        assertTrue(readme.contains("--external:@aws-sdk/*"))
        assertTrue(readme.contains("publish-layer-version"))
        // Layer publish command interpolates the Node major from the context, not 20
        assertTrue(readme.contains("nodejs${TemplateVersions.NODE_MAJOR}.x"))
    }

    @Test
    fun `Deploy section reports the runtime Node major from the context`() {
        val readme = AwsLambdaTemplateFiles.generate(ctx)["README.md"]!!
        assertTrue(readme.contains("Runtime: Node.js ${TemplateVersions.NODE_MAJOR}"))
        assertFalse(
            readme.contains("Runtime: Node.js 20"),
            "Deploy section must not mention the legacy Node.js 20 runtime",
        )
    }

    @Test
    fun `package json declares dev and start scripts pointing at Local res`() {
        val pkg = AwsLambdaTemplateFiles.generate(ctx)["package.json"]!!
        assertTrue(pkg.contains("\"dev\": \"node --watch src/Local.res.mjs\""))
        assertTrue(pkg.contains("\"start\": \"node src/Local.res.mjs\""))
    }

    @Test
    fun `hono node-server is a devDependency, not a runtime dependency`() {
        val pkg = AwsLambdaTemplateFiles.generate(ctx)["package.json"]!!
        // devDependencies entry must be present so `npm i` installs it for local dev
        assertTrue(pkg.contains("\"@hono/node-server\": \"${TemplateVersions.HONO_NODE_SERVER}\""))
        // Must not appear in `dependencies` block — keeps the esbuild Lambda
        // bundle from accidentally pulling in the Node-only HTTP server.
        val dependenciesBlock = pkg.substringAfter("\"dependencies\"").substringBefore("\"devDependencies\"")
        assertFalse(
            dependenciesBlock.contains("@hono/node-server"),
            "@hono/node-server must live under devDependencies only",
        )
    }

    @Test
    fun `ships Local res entry that starts the Hono app on Node`() {
        val files = AwsLambdaTemplateFiles.generate(ctx)
        assertTrue(files.containsKey("src/Local.res"))
        val local = files["src/Local.res"]!!
        assertTrue(local.contains("Server.app"))
        assertTrue(local.contains("HonoNodeServer.serve"))
    }

    @Test
    fun `ships HonoNodeServer bindings module`() {
        val files = AwsLambdaTemplateFiles.generate(ctx)
        assertTrue(files.containsKey("src/HonoNodeServer.res"))
        val bindings = files["src/HonoNodeServer.res"]!!
        assertTrue(bindings.contains("@hono/node-server"))
        assertTrue(bindings.contains("serve"))
    }

    @Test
    fun `README has a Local development section that documents the two-terminal flow`() {
        val readme = AwsLambdaTemplateFiles.generate(ctx)["README.md"]!!
        assertTrue(readme.contains("## Local development"))
        // Both watch loops must be advertised so users know to run them in parallel.
        assertTrue(readme.contains("rescript -w") || readme.contains("res:dev"))
        assertTrue(readme.contains("node --watch") || readme.contains("npm run dev") || readme.contains("pnpm dev"))
    }

    @Test
    fun `Server res still exports the Lambda handler so deploy is unaffected`() {
        val server = AwsLambdaTemplateFiles.generate(ctx)["src/Server.res"]!!
        assertTrue(server.contains("HonoLambda.handle"))
        assertTrue(server.contains("let handler"))
    }
}
