---
myst:
  html_meta:
    "keywords": "ci, cd, github actions, continuous integration, deployment"
---

# CI/CD Pipeline

This page describes the CI/CD infrastructure for the ReScript IntelliJ Plugin.

## Overview

The project uses **GitHub Actions** with 3 workflows:

| Workflow | File | Trigger | Purpose |
|----------|------|---------|---------|
| CI | `ci.yml` | Push/PR to `main` | Build, test, lint, verify |
| Release | `release.yml` | Tag `v*.*.*` | Create GitHub Release |
| Docs | `docs.yml` | Push/PR to `main` (sphinx-docs changes) | Build & deploy documentation |

Additionally, **Dependabot** is configured to automatically update Gradle and GitHub Actions dependencies on a weekly basis.

## CI Workflow (`ci.yml`)

The main CI pipeline runs on every push to `main` and on pull requests targeting `main`.

### Jobs

#### 1. actionlint

Validates GitHub Actions workflow syntax using [reviewdog](https://github.com/reviewdog/reviewdog). Fails on errors.

#### 2. build

The primary build and test job:

```
Checkout → Validate Gradle Wrapper → Setup JDK 21 → Setup Gradle (with caching)
    → ktlint check → Build plugin → Run tests with coverage
    → Coverage report on PR → Verify plugin structure → Upload artifacts
```

**Steps:**

| Step | Command | Purpose |
|------|---------|---------|
| ktlint | `./gradlew ktlintCheck` | Enforce Kotlin code style |
| Build | `./gradlew buildPlugin` | Compile, generate lexer, package plugin |
| Test | `./gradlew test koverXmlReport koverHtmlReport` | Run tests and generate coverage reports |
| Verify | `./gradlew verifyPluginStructure` | Check plugin descriptor and JAR structure |
| Verify config | `./gradlew verifyPluginProjectConfiguration` | Validate project configuration |

**Artifacts uploaded:**
- `plugin-zip` — Plugin distribution ZIP
- `test-results` — JUnit test reports
- `coverage-report` — Kover HTML coverage report

**PR-specific features:**
- Code coverage is reported as a PR comment via [kover-report](https://github.com/mi-kas/kover-report)
- Gradle caching is read-only on PRs (writable only on `main` pushes)

#### 3. verify (push only)

Runs `./gradlew verifyPlugin` for binary compatibility verification using IntelliJ Plugin Verifier. This checks the plugin against recommended IDE versions to detect API incompatibilities.

## Release Workflow (`release.yml`)

Triggered when a git tag matching `v*.*.*` is pushed.

### Release flow

1. Extract version from tag (e.g., `v1.2.3` → `1.2.3`)
2. Validate Gradle wrapper
3. Run pre-release checks: `ktlintCheck test verifyPluginStructure verifyPlugin`
4. Build plugin
5. Create GitHub Release with auto-generated release notes
6. Attach plugin ZIP as release asset
7. Pre-release detection: tags containing `-` (e.g., `v1.0.0-beta.1`) are marked as pre-release

### Creating a release

```bash
# Tag a release
git tag v1.0.0
git push origin v1.0.0

# Tag a pre-release
git tag v1.0.0-beta.1
git push origin v1.0.0-beta.1
```

:::{note}
JetBrains Marketplace publishing is not yet automated. See the TODO in `release.yml`.
:::

## Documentation Workflow (`docs.yml`)

Triggered on push/PR to `main` when files in `sphinx-docs/` change. Also supports manual dispatch.

### Jobs

#### 1. lint-and-test

Runs quality checks on the Sphinx documentation source:

| Check | Command | Purpose |
|-------|---------|---------|
| Ruff lint | `uv run ruff check .` | Python linting |
| Ruff format | `uv run ruff format --check .` | Python formatting |
| Mypy | `uv run mypy _ext/generate_rescript_lexer.py` | Type checking |
| Pytest | `uv run pytest` | Test custom extensions |

#### 2. build

Builds the documentation site (English + Japanese) with Sphinx:

- Runs `make build-all` which builds both language versions and assembles the site with Pagefind search
- On PRs: runs link checking (`make linkcheck`) and reports results to the Job Summary
- On push: uploads the built site as a GitHub Pages artifact

#### 3. deploy (push only)

Deploys the built site to GitHub Pages using `actions/deploy-pages@v4`.

## Local CI Reproduction

You can run the same checks locally that CI performs:

```bash
# Full CI check (matches the build job)
./gradlew ktlintCheck buildPlugin test koverXmlReport koverHtmlReport verifyPluginStructure verifyPluginProjectConfiguration

# Quick check (build + test only)
./gradlew buildPlugin test

# Individual checks
./gradlew ktlintCheck          # Code style
./gradlew test                 # Unit & integration tests
./gradlew koverHtmlReport      # Coverage report (open build/reports/kover/html/index.html)
./gradlew verifyPlugin         # Binary compatibility (slow, runs on push only)

# Documentation checks (from sphinx-docs/ directory)
cd sphinx-docs
uv sync
make check                     # lint + typecheck + test
make html                      # Build English docs
make build-all                 # Build full site (EN + JA)
make serve                     # Serve locally at http://localhost:8000
```

## Troubleshooting

### Build failures

**"Could not resolve all files for configuration"**
: Gradle dependency resolution failure. Run `./gradlew --refresh-dependencies buildPlugin` to force re-download.

**JFlex lexer generation fails**
: The `generateRescriptLexer` task auto-runs before compilation. If it fails, check `src/main/java/com/rescript/plugin/lang/Rescript.flex` for syntax errors. Run `./gradlew generateRescriptLexer` in isolation to see detailed errors.

**ktlint check fails**
: Run `./gradlew ktlintFormat` to auto-fix most issues, then commit the changes.

### Test failures

**Tests pass locally but fail in CI**
: CI runs on Ubuntu with a headless JDK. Tests that depend on UI components (Swing) may behave differently. Use `BasePlatformTestCase` for IDE integration tests and avoid direct UI interactions.

**"No display available" errors**
: The Gradle `runIde` task requires a display. In CI, this is handled automatically, but local headless testing may need `DISPLAY` set or `Xvfb` running.

### Coverage

**Coverage report is empty**
: Ensure tests actually run by checking `./gradlew test` output. Kover reports are generated only if tests execute.

**Coverage not appearing on PR**
: The `kover-report` action requires the XML report at `build/reports/kover/report.xml`. Verify this file exists after running tests.

### Documentation

**Sphinx build fails**
: Run `uv sync` in `sphinx-docs/` to install dependencies. Ensure Python 3.12+ is available.

**Link check failures on PR**
: Link checking is non-blocking (`continue-on-error: true`). Review the Job Summary for broken links and fix them if they point to project pages.

## Secrets

The following GitHub repository secrets are used:

| Secret | Workflow | Purpose |
|--------|----------|---------|
| `JETBRAINS_MARKETPLACE_TOKEN` | Release (TODO) | Plugin publishing (not yet configured) |

## Dependabot

Dependabot is configured in `.github/dependabot.yml`:

- **Gradle dependencies**: Weekly updates on Mondays, up to 5 concurrent PRs
- **GitHub Actions**: Weekly updates on Mondays, up to 5 concurrent PRs
- All update PRs are labeled with `dependencies`
