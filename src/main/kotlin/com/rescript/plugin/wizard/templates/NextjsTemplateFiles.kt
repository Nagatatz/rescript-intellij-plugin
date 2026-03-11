package com.rescript.plugin.wizard.templates

import com.rescript.plugin.wizard.ProjectFileBuilders

/** Generates project template files for a Next.js ReScript project. */
internal object NextjsTemplateFiles {
    fun generate(projectName: String): Map<String, String> =
        mapOf(
            "rescript.json" to
                ProjectFileBuilders.rescriptJson(
                    name = projectName,
                    bsDependencies = listOf("@rescript/core", "@rescript/react"),
                    includeJsx = true,
                    includeGenType = true,
                ),
            "package.json" to
                ProjectFileBuilders.packageJson(
                    name = projectName,
                    dependencies =
                        linkedMapOf(
                            "rescript" to "^12.0.0",
                            "@rescript/core" to "^1.0.0",
                            "@rescript/react" to "^0.14.0",
                            "react" to "^19.0.4",
                            "react-dom" to "^19.0.4",
                            "next" to "^15.0.7",
                        ),
                    scripts =
                        linkedMapOf(
                            "dev" to "concurrently \"rescript -w\" \"next dev\"",
                            "build" to "rescript && next build",
                            "start" to "next start",
                            "res:build" to "rescript",
                            "res:clean" to "rescript clean",
                            "res:dev" to "rescript -w",
                        ),
                    devDependencies = linkedMapOf("concurrently" to "^9.0.0"),
                ),
            "next.config.mjs" to
                "/** @type {import('next').NextConfig} */\nconst nextConfig = {};\n\nexport default nextConfig;",
            "src/app/page.tsx" to
                "import App from \"../App.gen\";\n\nexport default function Page() {\n  return <App />;\n}",
            "src/App.res" to
                "@genType @react.component\nlet make = () => {\n  <div>\n    {React.string(\"Hello, $projectName with Next.js!\")}\n  </div>\n}",
        )
}
