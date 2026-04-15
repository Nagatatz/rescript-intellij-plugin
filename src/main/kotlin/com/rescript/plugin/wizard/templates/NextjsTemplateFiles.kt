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
            "src/app/page.tsx" to pageTsx(),
            "src/app/client/GreetForm.tsx" to greetFormTsx(),
            "src/app/api/greet/route.ts" to greetRouteTs(),
            "src/App.res" to appRes(ctx.projectName),
            "src/GreetForm.res" to greetFormRes(),
            "src/Fetch.res" to fetchRes(),
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

    private fun fetchRes(): String =
        buildString {
            appendLine("// Tiny fetch wrapper for JSON POSTs. Shared between Client Components.")
            appendLine("type response")
            appendLine("@send external json: response => promise<'a> = \"json\"")
            appendLine("@val external fetch: (string, 'opts) => promise<response> = \"fetch\"")
            appendLine("")
            appendLine("let post = (url: string, body: string): promise<response> =>")
            appendLine("  fetch(url, {")
            appendLine("    \"method\": \"POST\",")
            appendLine("    \"headers\": {\"Content-Type\": \"application/json\"},")
            appendLine("    \"body\": body,")
            append("  })")
        }

    private fun pageTsx(): String =
        buildString {
            appendLine("// Server Component: renders the App shell + the Client Component GreetForm.")
            appendLine("// This is the default rendering mode for Next.js app router — no \"use client\" directive.")
            appendLine("import App from \"../App.gen\";")
            appendLine("import GreetForm from \"./client/GreetForm\";")
            appendLine("")
            appendLine("export default function Page() {")
            appendLine("  return (")
            appendLine("    <main style={{ padding: \"2rem\", fontFamily: \"sans-serif\" }}>")
            appendLine("      <App />")
            appendLine("      <GreetForm />")
            appendLine("    </main>")
            appendLine("  );")
            append("}")
        }

    private fun greetFormTsx(): String =
        buildString {
            appendLine("// Client Component: \"use client\" opts into the browser bundle so we can")
            appendLine("// use React.useState and event handlers.")
            appendLine("\"use client\";")
            appendLine("")
            appendLine("import GreetFormRescript from \"../../GreetForm.gen\";")
            appendLine("")
            appendLine("export default function GreetForm() {")
            appendLine("  return <GreetFormRescript />;")
            append("}")
        }

    private fun greetRouteTs(): String =
        buildString {
            appendLine("// POST /api/greet — minimal Route Handler. Parses { name } and returns a greeting.")
            appendLine("// Extend with zod/valibot for production validation.")
            appendLine("import { NextRequest, NextResponse } from \"next/server\";")
            appendLine("")
            appendLine("export async function POST(req: NextRequest) {")
            appendLine("  const body = await req.json().catch(() => ({}));")
            appendLine("  const name = typeof body?.name === \"string\" ? body.name : \"stranger\";")
            appendLine("  return NextResponse.json({ message: `Hello, \${name}!` });")
            append("}")
        }

    private fun appRes(projectName: String): String =
        buildString {
            appendLine("// Server-rendered component (no state, no client-only APIs).")
            appendLine("@genType @react.component")
            appendLine("let make = () => {")
            appendLine("  <section>")
            appendLine("    <h1> {React.string(\"Welcome to $projectName\")} </h1>")
            appendLine("    <p>")
            appendLine(
                "      {React.string(\"This block is a Server Component. \" ++ " +
                    "\"The form below is a Client Component.\")}",
            )
            appendLine("    </p>")
            append("  </section>")
            appendLine()
            append("}")
        }

    private fun greetFormRes(): String {
        val dollar = '$'
        return buildString {
            appendLine("// Client Component (state + fetch). Consumed from app/client/GreetForm.tsx.")
            appendLine("@genType @react.component")
            appendLine("let make = () => {")
            appendLine("  let (name, setName) = React.useState(() => \"\")")
            appendLine("  let (greeting, setGreeting) = React.useState(() => None)")
            appendLine("")
            appendLine("  let handleSubmit = async event => {")
            appendLine("    ReactEvent.Form.preventDefault(event)")
            appendLine("    let response =")
            appendLine("      await Fetch.post(")
            appendLine("        \"/api/greet\",")
            appendLine("        JSON.stringifyAny({\"name\": name})->Option.getOr(\"{}\"),")
            appendLine("      )")
            appendLine("    let body = await response->Fetch.json")
            appendLine("    setGreeting(_ => Some(body[\"message\"]))")
            appendLine("  }")
            appendLine("")
            appendLine("  <form onSubmit={handleSubmit}>")
            appendLine("    <input")
            appendLine("      type_=\"text\"")
            appendLine("      placeholder=\"Your name\"")
            appendLine("      value={name}")
            appendLine("      onChange={e => setName(_ => (e->ReactEvent.Form.target)[\"value\"])}")
            appendLine("    />")
            appendLine("    <button type_=\"submit\"> {React.string(\"Greet\")} </button>")
            appendLine("    {switch greeting {")
            appendLine("    | Some(msg) => <p> {React.string(msg)} </p>")
            appendLine("    | None => React.null")
            appendLine("    }}")
            append("  </form>")
            appendLine()
            append("}")
        }
    }

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
