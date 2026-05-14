# Contributing to the ReScript IntelliJ Plugin

Thanks for taking the time to look at this project! This guide is the short, English-language version of the contributor workflow. The authoritative rule set lives in `.claude/rules/` (Japanese) and `CLAUDE.md`; pull from there whenever this file is silent on a topic.

## Before you start

- **Issues first.** Open a GitHub Issue describing the change you want to make before sending a large patch. For bug reports, include a minimal `.res` snippet and the IDE / plugin / ReScript versions.
- **Look for `good-first-issue` tags.** A curated list of contributor-friendly tasks lives in [`docs/good-first-issues.md`](docs/good-first-issues.md).
- **Sign-off is not required.** Commits do not need a DCO sign-off. By contributing you agree your changes are licensed under the project's MIT License.

## Development environment

```bash
# Prerequisites: JDK 21+, Node.js (for the LSP), and pnpm if you want to test templates.

git clone https://github.com/Nagatatz/rescript-intellij-plugin.git
cd rescript-intellij-plugin

./gradlew buildPlugin       # builds the plugin distribution
./gradlew runIde            # launches a sandbox IDE with the plugin loaded
./gradlew test              # runs the full unit-test suite
./gradlew ktlintCheck       # verifies Kotlin formatting
```

`docs/repository-structure.md` and `sphinx-docs/dev/setup.md` cover the layout in more detail.

## Test slicing

The default `test` task runs every unit test. For tighter feedback loops, pass `-Pscope`:

```bash
./gradlew test -Pscope=fast    # excludes perf / *IntegrationTest / cli suites
./gradlew test -Pscope=perf    # perf smoke benchmarks only
./gradlew test -Pscope=cli     # external-CLI tests (auto-skips without mmdc/dot)
```

Heavier suites:

```bash
./gradlew integrationTest      # template generation E2E (requires Node.js + pnpm)
./gradlew uiTest               # Remote-Robot UI tests
./gradlew verifyPlugin         # JetBrains plugin verifier
```

## Coding conventions

- **Language:** all production code, KDoc comments, commit messages, GitHub Release notes, and `sphinx-docs/**/*.md` are written in **English**. Internal authoring docs (`.claude/`, `.steering/`, `CLAUDE.md`) and the Japanese translation files (`sphinx-docs/locale/ja/`) are written in Japanese. See `.claude/rules/language.md`.
- **KDoc:** every `class` / `object` / `enum class` / `sealed class` / `interface` declaration needs a KDoc block. The `checkKdoc` Gradle task enforces this — run it locally before opening a PR.
- **Tests:** every new production class needs a matching `<ClassName>Test.kt` unless it falls under the exemption list (Swing UI panels, LSP-coupled wiring, `RunConfiguration`-style IDE entry points, etc.). See `.claude/rules/testing.md`.
- **Lexer:** edit `src/main/java/com/rescript/plugin/lang/Rescript.flex`. The companion `RescriptFlexLexer.java` is auto-generated and ignored from Git — do not edit it.
- **Deprecated APIs:** new code must not call APIs marked `@Deprecated` or "scheduled for removal" in the IntelliJ Platform. If unavoidable, add a `@Suppress("DEPRECATION")` with a one-line reason and an entry in `plugin-verifier-ignored-problems.txt` (`Status: KEEP`, `Reviewed: YYYY-MM-DD`, `Expires:`). See `.claude/rules/deprecated-api.md`.

## Commit conventions

Commit messages start with an emoji prefix that signals intent. Use the table below; the highest-precedence applicable emoji wins.

| Emoji | When to use |
|-------|-------------|
| ✨ | New feature |
| 🐛 | Bug fix |
| ♻️ | Refactor (no behaviour change) |
| 📝 | Docs only |
| 🎨 | UI / styling |
| ⚡ | Performance improvement |
| 🔧 | Build / config change |
| ⬆ | Version bump |
| ✅ | Tests added / adjusted |
| 🗑️ | Code removal |

Format: `<emoji> <imperative verb> <subject>` — keep it under ~70 characters.

**Commit granularity:** one logical change per commit. A feature commit may bundle implementation + tests + `plugin.xml` registration, but it should not bundle two unrelated features. See `.claude/rules/git-conventions.md`.

## Branches and pull requests

- Branch off `main`. Prefix the branch: `feature/<name>`, `fix/<name>`, `refactor/<name>`, `docs/<name>`, `test/<name>`, or `chore/<name>`.
- Push the branch and open a PR against `main`. Fill in the PR description with a short summary and the manual / automated test plan.
- CI must be green (`ci.yml`: build + tests + ktlint + coverage). Coverage is enforced via Kover's `koverVerify` ratchet; if your change drops coverage below the minimum, add tests rather than lower the ratchet.
- Plugin Verifier (`./gradlew verifyPlugin`) runs monthly and on bumps to `platformVersion`. If you introduce a deprecated API usage, the verifier will flag it — fix it or document the suppression.

## What gets reviewed

A maintainer will check:

1. **Functional correctness** — does the change do what the PR description claims, with tests?
2. **Style** — ktlint passes, KDoc present, no deprecated-API references without justification.
3. **Documentation sync** — for user-facing changes, that `README.md`, `CLAUDE.md`, `sphinx-docs/user/features/*.md`, and the matching `.po` translation are all updated in the same PR. See `.claude/rules/documentation.md`.
4. **Coverage** — new logic has tests; exemptions are spelled out in the PR description.

## Release process

Maintainers cut releases following `.claude/rules/release.md`. Contributors don't need to bump `pluginVersion`, edit `plugin.xml`'s `<change-notes>`, or tag releases — those steps are owned by the release flow.

## Getting help

- File an Issue for bugs, feature requests, and design questions.
- Tag the issue `question` if you just want clarification rather than action.
- For security-sensitive reports, see `SECURITY.md` (if present) or email the maintainer listed in `package.json` / `gradle.properties`.

Welcome aboard!
