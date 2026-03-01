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
     */
    private fun appendJsonObject(
        entries: Map<String, String>,
        builder: StringBuilder,
    ) {
        val list = entries.entries.toList()
        list.forEachIndexed { index, (key, value) ->
            val comma = if (index < list.size - 1) "," else ""
            builder.appendLine("    \"$key\": \"$value\"$comma")
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
     * Used by Hono, Cloudflare Workers, AWS Lambda, and Google Cloud Run templates.
     */
    fun honoBindings(): String =
        buildString {
            appendLine("type app")
            appendLine("type context")
            appendLine("")
            appendLine("@module(\"hono\") @new external createApp: unit => app = \"Hono\"")
            appendLine("@send external get: (app, string, context => 'a) => unit = \"get\"")
            appendLine("@send external post: (app, string, context => 'a) => unit = \"post\"")
            appendLine("@send external text: (context, string) => 'a = \"text\"")
            append("@send external json: (context, 'a) => 'b = \"json\"")
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
