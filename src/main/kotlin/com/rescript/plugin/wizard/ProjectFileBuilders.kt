package com.rescript.plugin.wizard

/**
 * Shared utility methods for generating common project files across templates.
 *
 * Provides builders for `rescript.json`, `package.json`, ReScript starter modules,
 * and shared Hono framework bindings. Each method returns the file content as a string.
 *
 * @see ProjectTemplate for the template enum that uses these builders
 */
object ProjectFileBuilders {
    /**
     * Generates a `rescript.json` configuration file.
     *
     * @param name project name
     * @param bsDependencies list of bs-dependencies (e.g., "@rescript/core", "@rescript/react")
     * @param includeJsx whether to include JSX v4 configuration
     * @param includeGenType whether to include gentype configuration
     * @param sources custom source directory configuration string; defaults to standard src/ with subdirs
     * @return the JSON content as a string
     */
    fun rescriptJson(
        name: String,
        bsDependencies: List<String> = listOf("@rescript/core"),
        includeJsx: Boolean = false,
        includeGenType: Boolean = false,
        sources: String? = null,
        ppxFlags: List<String> = emptyList(),
    ): String {
        val bsDeps = bsDependencies.joinToString(", ") { "\"$it\"" }

        return buildString {
            appendLine("{")
            appendLine("  \"name\": \"$name\",")
            if (sources != null) {
                appendLine(sources)
            } else {
                appendLine("  \"sources\": {")
                appendLine("    \"dir\": \"src\",")
                appendLine("    \"subdirs\": true")
                appendLine("  },")
            }
            appendLine("  \"package-specs\": {")
            appendLine("    \"module\": \"esmodule\",")
            appendLine("    \"in-source\": true")
            appendLine("  },")
            appendLine("  \"suffix\": \".res.mjs\",")
            appendLine("  \"bs-dependencies\": [$bsDeps],")
            if (ppxFlags.isNotEmpty()) {
                val flags = ppxFlags.joinToString(", ") { "\"$it\"" }
                appendLine("  \"ppx-flags\": [$flags],")
            }
            if (includeJsx) {
                appendLine("  \"jsx\": {")
                appendLine("    \"version\": 4")
                appendLine("  },")
            }
            if (includeGenType) {
                appendLine("  \"gentypeconfig\": {")
                appendLine("    \"language\": \"typescript\"")
                appendLine("  },")
            }
            appendLine("  \"bsc-flags\": [\"-open RescriptCore\"]")
            append("}")
        }
    }

    /**
     * Generates a `package.json` file.
     *
     * @param name project name
     * @param dependencies map of dependency name to version
     * @param devDependencies map of dev dependency name to version
     * @param scripts map of script name to command
     * @param bin optional binary entry point for CLI tools
     * @param workspaces optional list of workspace glob patterns
     * @param type optional module type ("module" for ESM)
     * @param isPrivate whether to set "private": true
     * @param packageManager optional Corepack-style spec (e.g. `pnpm@9.12.0`)
     * @param engines optional map of engine constraints (e.g. `node` → `>=20`)
     * @param main optional package entry point (e.g. `./src/Index.res.mjs`)
     * @param types optional `.d.ts` entry point (e.g. `./src/Index.gen.d.ts`)
     * @param exports optional pre-rendered `exports` field body (JSON object; must not
     *                 include the surrounding `{}` braces so that the caller can embed
     *                 a subpath map such as `".": {"types": "...", "import": "..."}`)
     * @param files optional `files` allowlist controlling what ships in the npm tarball
     * @return the JSON content as a string
     */
    fun packageJson(
        name: String,
        dependencies: Map<String, String> = emptyMap(),
        devDependencies: Map<String, String> = emptyMap(),
        scripts: Map<String, String> = defaultScripts(),
        bin: String? = null,
        workspaces: List<String>? = null,
        type: String? = null,
        isPrivate: Boolean = false,
        packageManager: String? = null,
        engines: Map<String, String> = emptyMap(),
        main: String? = null,
        types: String? = null,
        exports: Map<String, Map<String, String>>? = null,
        files: List<String>? = null,
    ): String =
        buildString {
            appendLine("{")
            appendLine("  \"name\": \"$name\",")
            appendLine("  \"version\": \"0.1.0\",")
            if (isPrivate) {
                appendLine("  \"private\": true,")
            }
            if (type != null) {
                appendLine("  \"type\": \"$type\",")
            }
            if (packageManager != null) {
                appendLine("  \"packageManager\": \"$packageManager\",")
            }
            if (engines.isNotEmpty()) {
                appendLine("  \"engines\": {")
                appendJsonObject(engines, this)
                appendLine("  },")
            }
            if (main != null) {
                appendLine("  \"main\": \"$main\",")
            }
            if (types != null) {
                appendLine("  \"types\": \"$types\",")
            }
            if (exports != null) {
                appendLine("  \"exports\": {")
                val subpaths = exports.entries.toList()
                subpaths.forEachIndexed { sIdx, (subpath, conditions) ->
                    val sComma = if (sIdx < subpaths.size - 1) "," else ""
                    appendLine("    \"$subpath\": {")
                    val condList = conditions.entries.toList()
                    condList.forEachIndexed { cIdx, (cond, target) ->
                        val cComma = if (cIdx < condList.size - 1) "," else ""
                        appendLine("      \"$cond\": \"$target\"$cComma")
                    }
                    appendLine("    }$sComma")
                }
                appendLine("  },")
            }
            if (files != null) {
                val fileList = files.joinToString(", ") { "\"$it\"" }
                appendLine("  \"files\": [$fileList],")
            }
            if (bin != null) {
                appendLine("  \"bin\": \"$bin\",")
            }
            if (workspaces != null) {
                val ws = workspaces.joinToString(", ") { "\"$it\"" }
                appendLine("  \"workspaces\": [$ws],")
            }
            // Scripts
            appendLine("  \"scripts\": {")
            appendJsonObject(scripts, this)
            appendLine("  },")
            // Dependencies
            val hasDevDeps = devDependencies.isNotEmpty()
            appendLine("  \"dependencies\": {")
            appendJsonObject(dependencies, this)
            if (hasDevDeps) {
                appendLine("  },")
                appendLine("  \"devDependencies\": {")
                appendJsonObject(devDependencies, this)
            }
            appendLine("  }")
            append("}")
        }

    /**
     * Appends map entries as indented JSON key-value pairs.
     *
     * Escapes embedded backslashes and double quotes in [entries] values so that scripts
     * containing nested quotes (e.g. `concurrently "rescript -w" "next dev"`) produce valid
     * JSON that pnpm/npm can parse.
     */
    private fun appendJsonObject(
        entries: Map<String, String>,
        builder: StringBuilder,
    ) {
        val list = entries.entries.toList()
        list.forEachIndexed { index, (key, value) ->
            val comma = if (index < list.size - 1) "," else ""
            builder.appendLine("    \"$key\": \"${escapeJsonString(value)}\"$comma")
        }
    }

    /**
     * Returns a JSON-safe encoding of [value] with backslashes, double quotes, and common
     * control characters escaped per RFC 8259.
     */
    private fun escapeJsonString(value: String): String =
        buildString(value.length) {
            for (ch in value) {
                when (ch) {
                    '\\' -> append("\\\\")
                    '"' -> append("\\\"")
                    '\n' -> append("\\n")
                    '\r' -> append("\\r")
                    '\t' -> append("\\t")
                    else -> append(ch)
                }
            }
        }

    /**
     * Default ReScript build scripts for `package.json`.
     */
    fun defaultScripts(): Map<String, String> =
        linkedMapOf(
            "res:build" to "rescript",
            "res:clean" to "rescript clean",
            "res:dev" to "rescript -w",
        )

    /**
     * Generates a starter `Console.log` module.
     */
    fun starterModule(): String = "Console.log(\"Hello, ReScript!\")\n"

    /**
     * Generates a basic React component.
     */
    fun reactComponent(): String =
        buildString {
            appendLine("@react.component")
            appendLine("let make = () => {")
            appendLine("  <div>")
            appendLine("    {React.string(\"Hello, ReScript + React!\")}")
            appendLine("  </div>")
            append("}")
        }

    /**
     * Generates a Vite config with a reverse-proxy for API calls during development.
     *
     * Used by the Monorepo template so the Vite dev server proxies `/api` requests
     * to the Hono backend, eliminating the need for CORS configuration.
     *
     * @param plugins list of Vite plugin expressions (default: `react()`)
     * @param imports list of ES import statements for the config file
     * @param proxyTarget the backend URL to proxy to (default: `http://localhost:3000`)
     * @param proxyPath the URL path prefix to proxy (default: `/api`)
     * @return the vite.config.mjs content as a string
     */
    fun viteConfigWithProxy(
        plugins: List<String> = listOf("react()"),
        imports: List<String> =
            listOf(
                """import { defineConfig } from "vite";""",
                """import react from "@vitejs/plugin-react";""",
            ),
        proxyTarget: String = "http://localhost:3000",
        proxyPath: String = "/api",
    ): String =
        buildString {
            imports.forEach { appendLine(it) }
            appendLine("")
            appendLine("export default defineConfig({")
            appendLine("  plugins: [${plugins.joinToString(", ")}],")
            appendLine("  server: {")
            appendLine("    proxy: {")
            appendLine("      \"$proxyPath\": {")
            appendLine("        target: \"$proxyTarget\",")
            appendLine("        changeOrigin: true,")
            appendLine("      },")
            appendLine("    },")
            appendLine("  },")
            append("});")
        }

    /**
     * Generates shared Hono framework bindings for ReScript.
     *
     * Covers the request/response surface needed for typical CRUD APIs:
     * routing (GET/POST/PUT/DELETE/PATCH), middleware registration, request
     * helpers (path params / query / JSON body), and response helpers
     * (text / JSON / status code chaining).
     *
     * Used by Hono, Hono GraphQL, Cloudflare Workers, AWS Lambda, Google Cloud Run,
     * Monorepo, and Full-Stack templates.
     */
    fun honoBindings(): String =
        buildString {
            appendLine("// Hono framework bindings.")
            appendLine("// Extend as needed; this covers the common CRUD surface.")
            appendLine("")
            appendLine("type app")
            appendLine("type context")
            appendLine("type request")
            appendLine("type next = unit => promise<unit>")
            appendLine("type middleware = (context, next) => promise<unit>")
            appendLine("")
            appendLine("// Construction")
            appendLine("@module(\"hono\") @new external createApp: unit => app = \"Hono\"")
            appendLine("")
            appendLine("// Routing")
            appendLine("@send external get: (app, string, context => 'a) => unit = \"get\"")
            appendLine("@send external post: (app, string, context => 'a) => unit = \"post\"")
            appendLine("@send external put: (app, string, context => 'a) => unit = \"put\"")
            appendLine("@send external patch: (app, string, context => 'a) => unit = \"patch\"")
            appendLine("@send external deleteRoute: (app, string, context => 'a) => unit = \"delete\"")
            appendLine("")
            appendLine("// Middleware")
            appendLine("@send external use: (app, middleware) => unit = \"use\"")
            appendLine("@send external usePath: (app, string, middleware) => unit = \"use\"")
            appendLine("")
            appendLine("// Global error handler. Hono invokes the callback for any uncaught error.")
            appendLine("@send external onError: (app, ('err, context) => 'response) => unit = \"onError\"")
            appendLine("@send external notFoundHandler: (app, context => 'response) => unit = \"notFound\"")
            appendLine("")
            appendLine("// Request access")
            appendLine("@get external req: context => request = \"req\"")
            appendLine("@send external paramAt: (request, string) => string = \"param\"")
            appendLine("@send external query: (request, string) => Nullable.t<string> = \"query\"")
            appendLine("@send external jsonBody: request => promise<'a> = \"json\"")
            appendLine("@send external textBody: request => promise<string> = \"text\"")
            appendLine("@get external method: request => string = \"method\"")
            appendLine("@get external url: request => string = \"url\"")
            appendLine("")
            appendLine("// Response helpers (chainable: status returns context)")
            appendLine("@send external text: (context, string) => 'a = \"text\"")
            appendLine("@send external json: (context, 'a) => 'b = \"json\"")
            appendLine("@send external status: (context, int) => context = \"status\"")
            appendLine("@send external header: (context, string, string) => context = \"header\"")
            appendLine("@send external notFound: context => 'a = \"notFound\"")
            appendLine("")
            appendLine("// Middleware factories")
            appendLine("// CORS: accepts any hono/cors option (origin, allowMethods, credentials, ...).")
            appendLine("@module(\"hono/cors\") external cors: 'opts => middleware = \"cors\"")
        }

    /**
     * Generates Hono Node.js server bindings.
     */
    fun honoNodeServerBindings(): String =
        buildString {
            appendLine("type serveOptions = {port: int}")
            appendLine("")
            append("@module(\"@hono/node-server\") external serve: (Hono.app, serveOptions) => unit = \"serve\"")
        }
}
