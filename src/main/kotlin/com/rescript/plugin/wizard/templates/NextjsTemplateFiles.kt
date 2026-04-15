package com.rescript.plugin.wizard.templates

import com.rescript.plugin.wizard.PackageManager
import com.rescript.plugin.wizard.ProjectFileBuilders

/**
 * Generates project template files for a Next.js application powered by ReScript via genType.
 *
 * The template wires `rescript -w` and `next dev` together via concurrently and emits a
 * minimal app router page, README, .gitignore, .editorconfig, and CI workflow.
 */
internal object NextjsTemplateFiles {
    /**
     * Generates Next.js template files using the supplied [TemplateContext].
     */
    fun generate(ctx: TemplateContext): Map<String, String> =
        mapOf(
            "rescript.json" to
                ProjectFileBuilders.rescriptJson(
                    name = ctx.projectName,
                    bsDependencies = listOf("@rescript/core", "@rescript/react"),
                    includeJsx = true,
                    includeGenType = true,
                ),
            "package.json" to
                ProjectFileBuilders.packageJson(
                    name = ctx.projectName,
                    isPrivate = true,
                    packageManager = ctx.packageManagerSpec(),
                    engines = mapOf("node" to TemplateVersions.NODE_ENGINE),
                    dependencies =
                        linkedMapOf(
                            "rescript" to TemplateVersions.RESCRIPT,
                            "@rescript/core" to TemplateVersions.RESCRIPT_CORE,
                            "@rescript/react" to TemplateVersions.RESCRIPT_REACT,
                            "react" to TemplateVersions.REACT,
                            "react-dom" to TemplateVersions.REACT_DOM,
                            "next" to TemplateVersions.NEXTJS,
                        ),
                    scripts =
                        linkedMapOf(
                            "dev" to "concurrently \"rescript -w\" \"next dev\"",
                            "build" to "rescript && next build",
                            "start" to "next start",
                            "test" to "vitest run",
                            "res:build" to "rescript",
                            "res:clean" to "rescript clean",
                            "res:dev" to "rescript -w",
                        ),
                    devDependencies =
                        linkedMapOf(
                            "concurrently" to TemplateVersions.CONCURRENTLY,
                            "vitest" to TemplateVersions.VITEST,
                        ),
                ),
            "next.config.mjs" to
                "/** @type {import('next').NextConfig} */\nconst nextConfig = {};\n\nexport default nextConfig;",
            "src/app/page.tsx" to
                "import App from \"../App.gen\";\n\nexport default function Page() {\n  return <App />;\n}",
            "src/App.res" to
                "@genType @react.component\nlet make = () => {\n  <div>\n" +
                "    {React.string(\"Hello, ${ctx.projectName} with Next.js!\")}\n  </div>\n}",
            "src/__tests__/App.test.mjs" to appTest(),
            "README.md" to
                CommonFiles.readme(
                    ctx = ctx,
                    description = "A Next.js app with ReScript components exposed via genType.",
                    scripts =
                        listOf(
                            "dev" to "Start Next.js dev server with ReScript watcher",
                            "build" to "Compile ReScript and build Next.js for production",
                            "start" to "Run the production Next.js server",
                            "test" to "Run Vitest",
                        ),
                ),
            ".gitignore" to CommonFiles.gitignore(extra = listOf(".next/", "out/", "coverage/")),
            ".editorconfig" to CommonFiles.editorconfig(),
            ".github/workflows/ci.yml" to CommonFiles.ciWorkflow(ctx, hasBuild = true, hasTest = true),
        )

    /**
     * Back-compatible entry point used by tests and any external callers.
     */
    fun generate(projectName: String): Map<String, String> = generate(TemplateContext(projectName, PackageManager.PNPM))

    private fun appTest(): String =
        buildString {
            appendLine("import { describe, expect, it } from \"vitest\";")
            appendLine("import { make as App } from \"../App.res.mjs\";")
            appendLine("")
            appendLine("describe(\"App\", () => {")
            appendLine("  it(\"is a function component\", () => {")
            appendLine("    expect(typeof App).toBe(\"function\");")
            appendLine("  });")
            appendLine("});")
        }
}
