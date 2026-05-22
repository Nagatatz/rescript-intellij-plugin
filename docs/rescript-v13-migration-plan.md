# ReScript v13 Migration Plan

This document records the plan for adapting this plugin to ReScript v13. **No code changes are made by this document** — it is a preparation artifact to be executed on a separate branch once ReScript v13 stable is released.

## 1. Status

| Item | Value |
|---|---|
| ReScript stable (compiler) | v12.x (as of 2026-05-22) |
| ReScript v13 status | alpha only |
| Plugin compatibility goal | **dual support v12 + v13** at the same plugin version |
| Trigger for execution | v13.0.0 stable published on `npm:rescript` |
| Sunset of v12 support (tentative) | 2 plugin releases (~6 months) after v13 stable, aligned with a plugin MAJOR bump |

When the trigger condition is met, open a new branch from `main`, copy the `Section A`…`Section F` checklist below into a fresh `.steering/[YYYYMMDD]-NNN-rescript-v13-dual-support/tasklist.md`, and execute section-by-section.

## 2. Summary of v13 changes

Source: [rescript-lang/rescript-compiler releases](https://github.com/rescript-lang/rescript-compiler/releases) plus the v12 release coverage on [ReScript blog](https://rescript-lang.org/blog/) and [InfoQ ReScript 12.0 article](https://www.infoq.com/news/2025/12/rescript-12-release/). Each row marks whether the plugin needs explicit work.

### 2.1 Removals (breaking)

| Item | Plugin impact | Notes |
|---|---|---|
| Legacy build system (`rescript-legacy` command) | no | Run config already migrated from `build -w` to `watch` in v12 cycle |
| `bsconfig.json` support | **yes** | First-class in `RescriptPaths.CONFIG_FILE_NAMES` and ~12 downstream files; keep recognizing but warn on v13 projects (see §3) |
| `bs-dependencies` / `bs-dev-dependencies` / `bsc-flags` config keys | **yes** | Two template readmes still reference them: `src/main/resources/templates/full-stack/api/graphql/readme/graphql.md`, `src/main/resources/templates/res-x/readme/project-layout.md`. JSON schema also enumerates them |
| `external-stdlib` config key | **yes** | Remove from v13 schema branch |
| `es6` / `es6-global` module names | **yes** | v13 defaults to `esmodule`; schema enum needs updating |
| CLI flags `--dev`, `--create-sourcedirs`, `build -w` | no | Not surfaced in current Run config |
| Legacy uncurried `(. args) => ...` syntax | no | No dedicated token in lexer; only ensure no test fixture relies on it |
| `%external` extension | no | Same as above |

### 2.2 Additions (new syntax / features)

| Item | Plugin impact | Notes |
|---|---|---|
| `break` / `continue` keywords (in loops) | **yes** | Add to `Rescript.flex` keyword set and `RescriptTokenTypes.kt` |
| `for...of` / `for await...of` loops | partial | `for` / `await` / `of` are already token-able; verify lexer/highlighter doesn't trip on the new combinations. No semantic change required for v12 either |
| Dict spread `dict{...foo, "k": v}` | no | Lightweight parser consumes by brace balance; `dict` and `...` already tokenized |
| Inline records in `external` definitions | no | Same — parser consumes external bodies as opaque ranges |
| Scoped `@@live` / `@@dead` annotations | maybe | Optional: surface via completion or dedicated inspection; deferred (see §5) |
| `reanalyze-server` (long-lived) | doc-only | Plugin already runs reanalyze server for ReScript ≥ 12.1.0 (`RescriptReanalyzeServerStartupActivity`, `RescriptSettingsSchema:233`). Update the `≥ 12.1.0` gate wording for v13 |
| CLI flags `--prod`, `--features`, `--clear-screen` | **yes** | Add as options in `run/RescriptRunConfiguration` UI |

### 2.3 Stdlib

| Item | Plugin impact | Notes |
|---|---|---|
| `Dict.assignMany`, `Dict.concat*`, `Array.concatAll` etc. | no | Completion is LSP-driven; auto-follows |
| `Intl.Collator.compare` returns `Ordering.t` (was `int`) | no | LSP/type info reflects automatically |
| Belt API trims (`undefined<'a>` returners removed) | **yes** | Refresh deprecation message in `RescriptStyleLintInspection` to mention v13 removal |

## 3. Dual support strategy

Goal: a single plugin binary that detects the project's ReScript version once and gates version-sensitive behavior on the result. v12 users keep working; v13 users get v13-specific schema / wizard / inspections.

### 3.1 Version detection (single source of truth)

Add `src/main/kotlin/com/rescript/plugin/util/RescriptVersionDetector.kt`:

- Read `package.json` `dependencies.rescript` / `devDependencies.rescript` semver range from the project root (and per workspace package via `RescriptWorkspaceLayout`).
- Parse to a minimal `SemVer(major, minor, patch)` data class.
- Return `null` if no `rescript` dep is found (treat as "unknown — default to latest known stable schema").
- Cache per project; invalidate on `package.json` VFS change.

All version-gated features call `RescriptVersionDetector.major(project)` and branch on `>= 13`. No ad-hoc parsing elsewhere.

### 3.2 Lexer (`Rescript.flex` + `RescriptTokenTypes.kt`)

**Superset approach.** Add `break` / `continue` to the keyword macro and a corresponding `IElementType` constant. Do **not** add per-version lexer modes — these tokens behave as keywords in v13 and as legal-but-unused identifiers in v12; the only collision risk is v12 user code that uses `break`/`continue` as identifiers, which is rare and survives as a "use of reserved word" highlight at worst.

`for await...of` does not need new tokens — `for`, `await`, `of` are all already in the lexer. Verify no state machine in `Rescript.flex` (lines 73–82) misinterprets the sequence; add a sample to `testData/v13-only/ForAwaitOf.res` to lock in behavior.

### 3.3 Parser (`RescriptParser.kt`, `RescriptJsxParser.kt`)

No changes needed. The lightweight parser consumes top-level declaration bodies by brace balance and delegates the rest to the LSP. Dict spreads, inline-record externals, and new loop forms all fit within existing brace-balanced consumption.

### 3.4 JSON schema (`schemas/rescript.schema.json`)

Split into two files:

- `schemas/rescript-v12.schema.json` — current schema, keep as-is.
- `schemas/rescript-v13.schema.json` — derived; remove `es6`, `es6-global`, `external-stdlib`, `bs-dependencies`, `bs-dev-dependencies`, `bsc-flags`; default `module-format` to `esmodule`; update `package-spec` enum.

Extract the common subset (`reanalyze`, `ppx-specs`, `pp-specs`, `suffix-spec`) into `schemas/rescript-common.schema.json` and `$ref` from both branches.

`RescriptJsonSchemaProviderFactory.getSchemaFile(file)` consults `RescriptVersionDetector` and returns the matching branch. Default to v13 schema when detector returns `null` (forward-looking default).

### 3.5 Config file recognition (`RescriptPaths`, downstream)

Keep `bsconfig.json` in `CONFIG_FILE_NAMES`. v12 projects still write it.

Extend `RescriptMissingConfigInspection` (or add a sibling `RescriptBsconfigDeprecationInspection`) so that when `RescriptVersionDetector.major(project) >= 13` and a `bsconfig.json` is present, emit a `WARNING` with a quick-fix to rename to `rescript.json` and migrate `bs-*` keys. v12 projects see no change.

### 3.6 Wizard & templates

Split `wizard/templates/TemplateVersions.kt` into two value objects:

```kotlin
object TemplateVersionsV12 { const val RESCRIPT = "^12.2.0"; ... }
object TemplateVersionsV13 { const val RESCRIPT = "^13.0.0"; ... }
```

Add a Wizard step "ReScript version" with V12 / V13 radio. Default to V13 once stable lands. Templates select the matching `TemplateVersions*` object based on the wizard choice. `RESCRIPT_RUNTIME` must always match `RESCRIPT`.

Strip `bs-dependencies` / `bsc-flags` terminology from template readmes; both are documentation-only and apply to all versions.

### 3.7 Inspections & intentions

- `RescriptStyleLintInspection` Belt warning: extend the message to mention v13 stdlib removals when the project is v13.
- `RescriptCaseSplitIntention`, pipe conversion intention: no change. Switch/pipe syntax is unchanged in v13.
- Optional: completion contributor for `@@live` / `@@dead` scoped annotations — **deferred**, see §5.

### 3.8 Reanalyze

Update the wording of `RescriptSettingsSchema.kt:234` ("requires ReScript ≥ 12.1.0") to reflect that v13 ships `reanalyze-server` as default-on. The gate logic itself stays (`>= 12.1.0` already covers v13).

### 3.9 LSP

No change. `@rescript/language-server` is project-local and abstracted via LSP4j. Verify post-release that the v13 LSP responds to all `textDocument/*` requests the plugin uses.

### 3.10 Run configuration

Add three optional checkboxes / fields to `run/RescriptRunConfiguration` for `--prod`, `--features <list>`, `--clear-screen`. Gate visibility (or just enabled state) on `RescriptVersionDetector.major(project) >= 13`. Unknown flags on v12 CLIs would fail, so the gate must be enforced before launch.

### 3.11 Tests

Mirror `src/test/testData/lexer/` etc. with a `src/test/testData/v13-only/` directory for new-syntax fixtures (`BreakContinue.res`, `ForAwaitOf.res`, `DictSpread.res`, `ExternalInlineRecord.res`, `ScopedDeadLive.res`). Existing fixtures stay valid for v12. Lexer tests parameterize over both v12 and v13 fixture roots.

## 4. Execution checklist

Each section is a single mergeable unit (1 PR, 1+ commits). Run `./gradlew ktlintCheck buildPlugin test koverHtmlReport verifyPluginStructure` before opening each PR.

### Section A — Infrastructure: version detector

| Task | Files | Verification |
|---|---|---|
| Add `util/RescriptVersionDetector.kt` + `SemVer` data class | `src/main/kotlin/com/rescript/plugin/util/RescriptVersionDetector.kt` | unit test |
| Add `RescriptVersionDetectorTest.kt` covering: caret range, tilde range, exact version, missing dep, malformed JSON, workspace packages | `src/test/kotlin/com/rescript/plugin/util/RescriptVersionDetectorTest.kt` | `./gradlew test --tests *RescriptVersionDetectorTest` |
| Wire VFS listener for `package.json` invalidation | same | manual: edit `package.json` in sandbox IDE, confirm cache flushed |

### Section B — Lexer & samples

| Task | Files | Verification |
|---|---|---|
| Add `break` / `continue` keywords | `src/main/java/com/rescript/plugin/lang/Rescript.flex`, `src/main/kotlin/com/rescript/plugin/lang/RescriptTokenTypes.kt` | `./gradlew generateRescriptLexer test` |
| Add v13-only sample fixtures | `src/test/testData/v13-only/{BreakContinue,ForAwaitOf,DictSpread,ExternalInlineRecord,ScopedDeadLive}.res` | lexer tests pass against both `testData/lexer/` and `testData/v13-only/` |
| Update `RescriptLexerTest` to parameterize over the two fixture roots | `src/test/kotlin/com/rescript/plugin/lang/RescriptLexerTest.kt` | `./gradlew test` |

### Section C — Config & schema

| Task | Files | Verification |
|---|---|---|
| Split schema into `rescript-v12.schema.json` / `rescript-v13.schema.json` / `rescript-common.schema.json` | `src/main/resources/schemas/` | JSON syntactic validity (`./gradlew test --tests *RescriptJsonSchema*`) |
| Branch `JsonSchemaProviderFactory` on `RescriptVersionDetector.major` | `src/main/kotlin/com/rescript/plugin/config/RescriptJsonSchemaProviderFactory.kt` | unit test with both versions |
| Add `RescriptBsconfigDeprecationInspection` (or extend `RescriptMissingConfigInspection`) | `src/main/kotlin/com/rescript/plugin/inspection/`, `src/main/resources/META-INF/plugin.xml` | inspection test that asserts warning only on v13 |

### Section D — Wizard & templates

| Task | Files | Verification |
|---|---|---|
| Split `TemplateVersions` into V12 / V13 objects | `src/main/kotlin/com/rescript/plugin/wizard/templates/TemplateVersions.kt` | existing wizard tests parameterized over both |
| Add "ReScript version" wizard step | `src/main/kotlin/com/rescript/plugin/wizard/RescriptModuleBuilder.kt` and step UI | `./gradlew runIde`, manual: New Project → ReScript, both branches generate |
| Strip `bs-dependencies` / `bsc-flags` mentions from template readmes | `src/main/resources/templates/full-stack/api/graphql/readme/graphql.md`, `src/main/resources/templates/res-x/readme/project-layout.md` | `grep -r "bs-dependencies\|bsc-flags" src/main/resources/templates/` returns nothing |

### Section E — Inspections & run config

| Task | Files | Verification |
|---|---|---|
| Update Belt warning copy in `RescriptStyleLintInspection` to mention v13 stdlib removals | `src/main/kotlin/com/rescript/plugin/inspection/RescriptStyleLintInspection.kt` | inspection test snapshot |
| Add `--prod` / `--features` / `--clear-screen` to Run config UI, gated by version | `src/main/kotlin/com/rescript/plugin/run/RescriptRunConfiguration.kt` and editor | run config persistence test + manual launch |
| Update reanalyze wording to drop `≥ 12.1.0` qualifier when project is v13 | `src/main/kotlin/com/rescript/plugin/settings/RescriptSettingsSchema.kt` | manual settings dialog check |

### Section F — Docs & marketplace

| Task | Files | Verification |
|---|---|---|
| Update plugin description compatibility line and add v13 change-notes entry | `src/main/resources/META-INF/plugin.xml` | `./gradlew verifyPluginStructure` |
| Update README.md Features / requirements | `README.md` | reviewer reads diff |
| Update sphinx-docs (en + ja sync) for v13 features and migration notes | `sphinx-docs/user/installation.md`, `sphinx-docs/user/changelog.md`, `sphinx-docs/locale/ja/LC_MESSAGES/**/*.po` | `cd sphinx-docs && make build-all` succeeds |
| Update `docs/product-requirements.md` compatibility matrix | `docs/product-requirements.md` (§5 Platform 対応) | reviewer reads diff |
| Bump `pluginVersion` and update `<change-notes>` per `.claude/rules/release.md` | `gradle.properties`, `src/main/resources/META-INF/plugin.xml` | release skill / manual release |

## 5. Out of scope (this migration cycle)

These ideas surfaced during v13 research but are deferred to follow-up RFCs:

- **Automated Belt → stdlib codemod**. Belt removal in v13 is partial; full automated migration is its own feature.
- **Loop-context-aware highlighting for `break` / `continue`**. Plain keyword highlight is sufficient for v1; semantic-token-driven contextual highlight requires a new pass.
- **Completion / inspection provider for `@@live` / `@@dead`**. Useful for dead code workflows but unrelated to v13 compatibility.
- **Dropping v12 support**. Scheduled for a later MAJOR bump (~6 months after v13 stable). Tracked as a separate steering item at that time.
- **Tauri / Relay / React adapter version bumps**. Verify compatibility post-stable; bump only when upstream supports v13.

## 6. References

- ReScript compiler releases: <https://github.com/rescript-lang/rescript-compiler/releases>
- ReScript blog (v12 release): <https://rescript-lang.org/blog/>
- ReScript v12 → v13 migration guide (compiler manual, when published): <https://rescript-lang.org/docs/manual/>
- InfoQ v12 release coverage: <https://www.infoq.com/news/2025/12/rescript-12-release/>
- Repository structure reference: [./repository-structure.md](./repository-structure.md)
- Product requirements (platform compatibility matrix): [./product-requirements.md](./product-requirements.md)
- LSP fallback matrix: [./lsp-fallback-matrix.md](./lsp-fallback-matrix.md)
