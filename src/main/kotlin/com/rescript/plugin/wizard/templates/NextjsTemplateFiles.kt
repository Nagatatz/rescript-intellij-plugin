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
                            "test:coverage" to "vitest run --coverage",
                            "res:build" to "rescript",
                            "res:clean" to "rescript clean",
                            "res:dev" to "rescript -w",
                        ),
                    devDependencies =
                        linkedMapOf(
                            "concurrently" to TemplateVersions.CONCURRENTLY,
                            "vitest" to TemplateVersions.VITEST,
                            "@vitest/coverage-v8" to TemplateVersions.VITEST_COVERAGE_V8,
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
                    extraSections =
                        listOf(
                            "Server vs Client Components" to serverVsClientSection(),
                            "Route Handlers" to routeHandlerSection(),
                            "Project Layout" to nextjsLayoutSection(),
                        ),
                ),
            ".nvmrc" to CommonFiles.nvmrc(),
            "LICENSE" to CommonFiles.mitLicense(holder = ctx.projectName),
            ".github/dependabot.yml" to CommonFiles.dependabotYaml(),
            ".gitignore" to CommonFiles.gitignore(extra = listOf(".next/", "out/")),
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

    private fun serverVsClientSection(): String =
        """
        `src/App.res` is annotated `@genType` and consumed from `src/app/page.tsx`, a
        **Server Component**. It has no state and no browser-only APIs. The form with
        `useState` lives in `src/GreetForm.res`, consumed from `src/app/client/GreetForm.tsx`
        which opts in with `"use client"`. Keep stateful ReScript components behind a
        `use client` boundary; keep pure rendering components on the server side.
        """.trimIndent()

    private fun routeHandlerSection(): String =
        """
        `src/app/api/greet/route.ts` is a Next.js Route Handler (edge/node runtime).
        Swap the hand-rolled parsing for `zod` / `valibot` before production:

        ```ts
        import { z } from "zod";
        const Body = z.object({ name: z.string().min(1) });
        export async function POST(req: NextRequest) {
          const parsed = Body.safeParse(await req.json());
          if (!parsed.success) return NextResponse.json({ error: parsed.error }, { status: 400 });
          return NextResponse.json({ message: `Hello, ${'$'}{parsed.data.name}!` });
        }
        ```
        """.trimIndent()

    private fun nextjsLayoutSection(): String =
        buildString {
            appendLine("| File | Purpose |")
            appendLine("| --- | --- |")
            appendLine("| `src/app/page.tsx` | Server Component — app shell |")
            appendLine("| `src/app/client/GreetForm.tsx` | Client wrapper around ReScript form |")
            appendLine("| `src/app/api/greet/route.ts` | POST /api/greet Route Handler |")
            appendLine("| `src/App.res` | ReScript server-rendered component |")
            appendLine("| `src/GreetForm.res` | ReScript client component (state + fetch) |")
            append("| `src/Fetch.res` | Fetch wrapper shared by clients |")
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
