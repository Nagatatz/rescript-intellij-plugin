# Good-First-Issue Inventory

A curated set of contributor-friendly tasks. Each entry is sized to ~1–3 days for someone newly onboarded, with clear acceptance criteria, suggested files to read first, and the rule files that apply.

When you start one of these, please open a GitHub Issue titled "Good first issue: \<name>" so duplicate effort is avoided, then send the PR following [`CONTRIBUTING.md`](../CONTRIBUTING.md).

## 1. Add a new `.res` Live Template

**Goal:** add another snippet to `src/main/resources/liveTemplates/ReScript.xml` (currently 21 templates). Suggested additions: `match` arms scaffolding, a `Belt.Result` chain, a JSX hook skeleton.

- **Files to read:** `liveTemplates/ReScript.xml`, `sphinx-docs/user/features/code-completion.md`
- **Acceptance:** template registered, screenshot in `sphinx-docs/user/features/code-completion.md`, matching `.po` updated, manual smoke via `runIde`
- **Rules:** none beyond standard

## 2. Translate a single `sphinx-docs/user/features/*.md` page to Japanese

**Goal:** fill empty `msgstr ""` entries for one feature page in `sphinx-docs/locale/ja/LC_MESSAGES/user/features/*.po`.

- **Files to read:** `.claude/rules/documentation.md` (translation rules), `sphinx-docs/Makefile`
- **Acceptance:** chosen `.po` has no empty `msgstr`, `cd sphinx-docs && make build-ja` succeeds
- **Rules:** `documentation.md`, `language.md`

## 3. Add an inspection for a missing recursive `let rec` keyword

**Goal:** new `RescriptMissingLetRecInspection` that flags `let f = (x) => f(x)` (self-reference without `rec`) and offers a quick fix to insert `rec`.

- **Files to read:** existing inspections in `src/main/kotlin/com/rescript/plugin/inspection/`, `quickfix/`
- **Acceptance:** new inspection class + quick fix + unit tests, registered in `plugin.xml`, documented in `sphinx-docs/user/features/code-analysis.md`
- **Rules:** `plugin-xml-rules.md`, `testing.md`, `code-comments.md`, `documentation.md`

## 4. Expose Variant Flow's `MAX_NESTING_DEPTH` as a project setting

**Goal:** today `RescriptVariantFlowModel.MAX_NESTING_DEPTH` is a hard-coded `3`. Add a project setting so users can raise it for deeply nested switches.

- **Files to read:** `flow/RescriptVariantFlowModel.kt`, `settings/RescriptProjectSettings.kt`, `settings/RescriptSettingsSchema.kt`
- **Acceptance:** new setting with default 3, persisted, used by Source + Visual modes, validated for sane bounds (1–10), tests
- **Rules:** `testing.md`, `code-comments.md`

## 5. Add an `astro check`-style run configuration template

**Goal:** the Wizard generates 22 templates; add a `Type-check on save` ReScript run configuration template that calls `./node_modules/.bin/rescript build --quiet` so users get a single-button type check from the toolbar.

- **Files to read:** `run/RescriptRunConfiguration*.kt`, `template/RescriptCreateFileAction.kt`
- **Acceptance:** new run config template available from the gutter, picks up project root via `RescriptWorkspaceDiscovery`, test covers the command-line assembly
- **Rules:** `testing.md`

## 6. Tighten a perf baseline

**Goal:** the four perf smoke benchmarks under `src/test/kotlin/com/rescript/plugin/perf/` log `ratio=<actual/baseline>` on every run. Pick one whose ratio is consistently `< 0.5` on CI and ratchet its `BASELINE_MS` down so future regressions are caught earlier.

- **Files to read:** any `src/test/kotlin/com/rescript/plugin/perf/*PerfTest.kt`
- **Acceptance:** updated `BASELINE_MS`, commit message explains why the new baseline is safe, `./gradlew test -Pscope=perf` passes locally
- **Rules:** `testing.md`

## 7. Add Markdown / GraphQL injection to a `%raw()` payload variant

**Goal:** the plugin already injects JavaScript into `%raw()` and Markdown into doc-comment fences. Add GraphQL injection into `%raw.gql()` (if `JS GraphQL` plugin is present).

- **Files to read:** `injection/`, `META-INF/rescript-markdown.xml`, `META-INF/rescript-js-injection.xml`
- **Acceptance:** new optional dependency on `com.intellij.lang.jsgraphql` declared in a fresh `rescript-graphql.xml`, injection works on a small `.res` sample, screenshot in docs
- **Rules:** `plugin-xml-rules.md`, `documentation.md`

## 8. Type-coverage CSV export

**Goal:** the Type Coverage Heat Map (`coverage/`) tool window already renders a table. Add a "Copy as CSV" action so users can paste the report into a spreadsheet for tracking over time.

- **Files to read:** `coverage/RescriptTypeCoveragePanel.kt`, `coverage/RescriptTypeCoverageModel.kt`
- **Acceptance:** new toolbar action, CSV format matches `file,total,annotated,inferred,coverage_pct`, unit test on the serialiser
- **Rules:** `testing.md`, `documentation.md`

## 9. Add a `pnpm-workspace.yaml` fixture-based test for `RescriptWorkspaceDiscovery`

**Goal:** monorepo discovery has prioritised paths (Settings → workspace globs → depth scan → parent walk). Add a heavy-fixture test that builds a 2-level pnpm workspace on disk and asserts the right package roots are detected.

- **Files to read:** `lsp/RescriptWorkspaceDiscovery.kt`, `IntelliJPlatformExtensionWithContentRoot.kt`
- **Acceptance:** new test under `src/test/kotlin/com/rescript/plugin/lsp/`, passes locally, runs in CI without flake
- **Rules:** `testing.md`

## 10. Update `plugin-verifier-ignored-problems.txt` expiry sweep

**Goal:** each entry has an `Expires: YYYY-MM-DD`. Audit entries with `Expires:` older than today; either confirm with a fresh `Reviewed:` line or remove the entry if the underlying API no longer exists.

- **Files to read:** `plugin-verifier-ignored-problems.txt`, `.claude/rules/deprecated-api.md`
- **Acceptance:** all entries have `Expires:` >= today, removed entries are justified in the commit message
- **Rules:** `deprecated-api.md`

---

## Adding a new entry

When proposing a new good-first-issue, check that it:

1. Is **scoped tightly** — under 200 LOC including tests
2. Has a **clear acceptance criterion** that a reviewer can verify
3. Names **at least one file** the contributor should read first
4. References the **specific rule files** that apply (testing, docs, etc.)

Entries become stale; when an issue is implemented, remove it from this list in the same PR that lands the feature.
