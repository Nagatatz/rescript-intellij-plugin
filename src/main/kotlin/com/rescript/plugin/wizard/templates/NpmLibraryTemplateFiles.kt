package com.rescript.plugin.wizard.templates

import com.rescript.plugin.wizard.PackageManager
import com.rescript.plugin.wizard.ProjectFileBuilders

/**
 * Generates files for the npm Library template.
 *
 * Produces a publishable npm package with ReScript and `@genType` for TypeScript consumers.
 * Ships a small API surface (sync greeting, async fetchWithTimeout, list helpers) that
 * exercises the common patterns library authors hit on day two: pure utilities, async I/O,
 * and generic helpers. Includes README, .gitignore, .editorconfig, CI workflow, and Vitest.
 */
internal object NpmLibraryTemplateFiles {
    /**
     * Generates npm Library template files using the supplied [TemplateContext].
     */
    fun generate(ctx: TemplateContext): Map<String, String> =
        mapOf(
            "rescript.json" to
                ProjectFileBuilders.rescriptJson(
                    name = ctx.projectName,
                    includeGenType = true,
                ),
            "package.json" to
                ProjectFileBuilders.packageJson(
                    name = ctx.projectName,
                    type = "module",
                    packageManager = ctx.packageManagerSpec(),
                    engines = mapOf("node" to TemplateVersions.NODE_ENGINE),
                    dependencies =
                        linkedMapOf(
                            "rescript" to TemplateVersions.RESCRIPT,
                            "@rescript/core" to TemplateVersions.RESCRIPT_CORE,
                        ),
                    devDependencies =
                        linkedMapOf(
                            "vitest" to TemplateVersions.VITEST,
                            "typescript" to TemplateVersions.TYPESCRIPT,
                        ),
                    scripts =
                        linkedMapOf(
                            "build" to "rescript",
                            "clean" to "rescript clean",
                            "test" to "vitest run",
                            "prepare" to "rescript",
                            "res:build" to "rescript",
                            "res:clean" to "rescript clean",
                            "res:dev" to "rescript -w",
                        ),
                ),
            "src/Index.res" to indexRes(ctx.projectName),
            "src/ListUtils.res" to listUtilsRes(),
            "src/Fetcher.res" to fetcherRes(),
            "src/__tests__/Index.test.mjs" to indexTest(),
            "src/__tests__/ListUtils.test.mjs" to listUtilsTest(),
            "src/__tests__/Fetcher.test.mjs" to fetcherTest(),
            "README.md" to
                CommonFiles.readme(
                    ctx = ctx,
                    description =
                        "A publishable npm package written in ReScript with TypeScript bindings via genType. " +
                            "Ships a small utility API (sync greet, async fetch-with-timeout, list helpers) " +
                            "to demonstrate common day-two patterns for library authors.",
                    scripts =
                        listOf(
                            "build" to "Compile ReScript sources",
                            "test" to "Run Vitest",
                            "res:dev" to "Watch ReScript sources",
                        ),
                    extraSections =
                        listOf(
                            "API Surface" to apiSurfaceSection(),
                            "Publish" to publishSection(ctx),
                        ),
                ),
            ".gitignore" to CommonFiles.gitignore(extra = listOf("coverage/")),
            ".editorconfig" to CommonFiles.editorconfig(),
            ".github/workflows/ci.yml" to CommonFiles.ciWorkflow(ctx, hasBuild = true, hasTest = true),
        )

    /**
     * Back-compatible entry point used by tests and any external callers.
     */
    fun generate(projectName: String): Map<String, String> = generate(TemplateContext(projectName, PackageManager.PNPM))

    private fun apiSurfaceSection(): String =
        buildString {
            appendLine("| Module | Export | Description |")
            appendLine("| --- | --- | --- |")
            appendLine("| `Index.res` | `greet(name)` | Returns a greeting string (sync, `@genType`) |")
            appendLine("| `Fetcher.res` | `fetchWithTimeout(url, ~timeoutMs)` | Fetch that rejects after a timeout |")
            appendLine("| `ListUtils.res` | `chunk(xs, ~size)` | Splits a list into fixed-size chunks |")
            append("| `ListUtils.res` | `partitionMap(xs, f)` | Partitions by Result into `(ok, err)` |")
        }

    private fun publishSection(ctx: TemplateContext): String =
        buildString {
            appendLine("1. Bump the version in `package.json`.")
            appendLine("2. Run `${ctx.runCmd("build")}` to regenerate TypeScript bindings.")
            appendLine("3. Run `${ctx.runCmd("test")}` to confirm Vitest passes.")
            append("4. Run `npm publish --access public`.")
        }

    private fun indexRes(projectName: String): String {
        val dollar = '$'
        return buildString {
            appendLine("/** Returns a greeting addressed to [name]. */")
            appendLine("@genType")
            appendLine("let greet = (name: string) => {")
            appendLine("  `Hello from $projectName, $dollar{name}!`")
            appendLine("}")
            appendLine("")
            appendLine("/** Exposed helpers for JS/TS consumers. */")
            appendLine("@genType")
            appendLine("let chunk = ListUtils.chunk")
            appendLine("")
            appendLine("@genType")
            append("let fetchWithTimeout = Fetcher.fetchWithTimeout")
        }
    }

    private fun listUtilsRes(): String =
        buildString {
            appendLine("/** Splits [xs] into arrays of [size] elements. Last chunk may be shorter. */")
            appendLine("@genType")
            appendLine("let chunk = (xs: array<'a>, ~size: int): array<array<'a>> => {")
            appendLine("  if size <= 0 {")
            appendLine("    []")
            appendLine("  } else {")
            appendLine("    let buckets = ref([])")
            appendLine("    let current = ref([])")
            appendLine("    xs->Array.forEach(x => {")
            appendLine("      current.contents->Array.push(x)")
            appendLine("      if current.contents->Array.length >= size {")
            appendLine("        buckets.contents->Array.push(current.contents)")
            appendLine("        current := []")
            appendLine("      }")
            appendLine("    })")
            appendLine("    if current.contents->Array.length > 0 {")
            appendLine("      buckets.contents->Array.push(current.contents)")
            appendLine("    }")
            appendLine("    buckets.contents")
            appendLine("  }")
            appendLine("}")
            appendLine("")
            appendLine("/**")
            appendLine(" * Partitions [xs] by running [f] on each element. Results that are `Ok`")
            appendLine(" * land in the first array; `Error` values in the second.")
            appendLine(" */")
            appendLine("@genType")
            appendLine("let partitionMap = (xs: array<'a>, f: 'a => result<'b, 'c>): (array<'b>, array<'c>) => {")
            appendLine("  let oks = []")
            appendLine("  let errs = []")
            appendLine("  xs->Array.forEach(x =>")
            appendLine("    switch f(x) {")
            appendLine("    | Ok(v) => oks->Array.push(v)")
            appendLine("    | Error(e) => errs->Array.push(e)")
            appendLine("    }")
            appendLine("  )")
            appendLine("  (oks, errs)")
            append("}")
        }

    private fun fetcherRes(): String =
        buildString {
            appendLine("// Wraps the global `fetch` with a timeout; demonstrates Promise + AbortController.")
            appendLine("@val external fetch: (string, 'opts) => promise<'response> = \"fetch\"")
            appendLine("")
            appendLine("type abortController")
            appendLine("@new external makeAbortController: unit => abortController = \"AbortController\"")
            appendLine("@get external signal: abortController => 'signal = \"signal\"")
            appendLine("@send external abort: abortController => unit = \"abort\"")
            appendLine("")
            appendLine("@val external setTimeout: (unit => unit, int) => 'timer = \"setTimeout\"")
            appendLine("@val external clearTimeout: 'timer => unit = \"clearTimeout\"")
            appendLine("")
            appendLine("/**")
            appendLine(" * Fetches [url] and rejects with a RangeError-like exception if the request")
            appendLine(" * takes longer than [timeoutMs] milliseconds.")
            appendLine(" */")
            appendLine("@genType")
            appendLine("let fetchWithTimeout = async (url: string, ~timeoutMs: int): promise<'response> => {")
            appendLine("  let controller = makeAbortController()")
            appendLine("  let timer = setTimeout(() => controller->abort, timeoutMs)")
            appendLine("  try {")
            appendLine("    let response = await fetch(url, {\"signal\": controller->signal})")
            appendLine("    clearTimeout(timer)")
            appendLine("    response")
            appendLine("  } catch {")
            appendLine("  | err =>")
            appendLine("    clearTimeout(timer)")
            appendLine("    raise(err)")
            appendLine("  }")
            append("}")
        }

    private fun indexTest(): String =
        buildString {
            appendLine("import { describe, expect, it } from \"vitest\";")
            appendLine("import { greet } from \"../Index.res.mjs\";")
            appendLine("")
            appendLine("describe(\"greet\", () => {")
            appendLine("  it(\"returns a greeting containing the supplied name\", () => {")
            appendLine("    expect(greet(\"world\")).toContain(\"world\");")
            appendLine("  });")
            appendLine("});")
        }

    private fun listUtilsTest(): String {
        val dollar = '$'
        return buildString {
            appendLine("import { describe, expect, it } from \"vitest\";")
            appendLine("import { chunk, partitionMap } from \"../ListUtils.res.mjs\";")
            appendLine("")
            appendLine("describe(\"chunk\", () => {")
            appendLine("  it(\"splits an array into fixed-size buckets\", () => {")
            appendLine("    expect(chunk([1, 2, 3, 4, 5], 2)).toEqual([[1, 2], [3, 4], [5]]);")
            appendLine("  });")
            appendLine("  it(\"returns an empty array when size is zero or negative\", () => {")
            appendLine("    expect(chunk([1, 2, 3], 0)).toEqual([]);")
            appendLine("  });")
            appendLine("});")
            appendLine("")
            appendLine("describe(\"partitionMap\", () => {")
            appendLine("  it(\"splits into oks and errs\", () => {")
            appendLine("    const [oks, errs] = partitionMap([1, 2, 3], (n) =>")
            appendLine(
                "      n % 2 === 0 ? { TAG: \"Ok\", _0: n * 10 } : " +
                    "{ TAG: \"Error\", _0: `odd: $dollar{n}` }",
            )
            appendLine("    );")
            appendLine("    expect(oks).toEqual([20]);")
            appendLine("    expect(errs).toEqual([\"odd: 1\", \"odd: 3\"]);")
            appendLine("  });")
            appendLine("});")
        }
    }

    private fun fetcherTest(): String =
        buildString {
            appendLine("import { afterEach, describe, expect, it, vi } from \"vitest\";")
            appendLine("import { fetchWithTimeout } from \"../Fetcher.res.mjs\";")
            appendLine("")
            appendLine("afterEach(() => vi.restoreAllMocks());")
            appendLine("")
            appendLine("describe(\"fetchWithTimeout\", () => {")
            appendLine("  it(\"returns the response when fetch resolves in time\", async () => {")
            appendLine("    const fake = { ok: true };")
            appendLine("    vi.stubGlobal(\"fetch\", vi.fn().mockResolvedValue(fake));")
            appendLine("    await expect(fetchWithTimeout(\"https://example.test\", 100)).resolves.toBe(fake);")
            appendLine("  });")
            appendLine("});")
        }
}
