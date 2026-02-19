# Tasklist: Sphinx Documentation Site

## Phase 1: Steering Documents

- [x] Create `.steering/20260219-sphinx-docs/` directory
- [x] Create `requirements.md` → user approval
- [x] Create `design.md` → user approval
- [x] Create `tasklist.md` → user approval

## Phase 2: Sphinx Infrastructure

- [x] Create branch `docs/sphinx-documentation` from `main`
- [x] Create `sphinx-docs/pyproject.toml` (uv + Sphinx, Furo, MyST, sphinx-intl, sphinx-copybutton, sphinx-design)
- [x] Create `sphinx-docs/conf.py` (Furo theme, MyST Parser, i18n settings, custom ReScript lexer/style)
- [x] Create `sphinx-docs/Makefile` (generate-lexer, html, gettext, update-po, build-ja, build-all, serve, install, clean, linkcheck)
- [x] Create `sphinx-docs/_ext/generate_rescript_lexer.py` (auto-extract from Rescript.flex + color scheme XMLs)
- [x] Create `sphinx-docs/_static/css/custom.css` (language switcher CSS)
- [x] Create `sphinx-docs/_templates/page.html` (language switcher UI — Furo uses page.html, not layout.html)
- [x] Create `sphinx-docs/_static/img/screenshots/.gitkeep` (placeholder)
- [x] Update `.gitignore` (sphinx-docs/_build/, .venv/, .python-version, rescript_lexer.py, *.mo, *.pot)
- [x] Verify build: `make html` succeeds with no warnings
- [x] Commit: `📝 Add Sphinx documentation infrastructure`

## Phase 3: Document Tree — Root & User Docs

- [x] Create `sphinx-docs/index.md` (root landing page)
- [x] Create `sphinx-docs/user/index.md` (user docs landing)
- [x] Create `sphinx-docs/user/installation.md` (plugin install + LSP setup)
- [x] Create `sphinx-docs/user/quickstart.md` (first-time usage flow)
- [x] Create `sphinx-docs/user/features/index.md` (feature overview)
- [x] Create `sphinx-docs/user/features/syntax-highlighting.md`
- [x] Create `sphinx-docs/user/features/code-completion.md`
- [x] Create `sphinx-docs/user/features/navigation.md`
- [x] Create `sphinx-docs/user/features/code-editing.md`
- [x] Create `sphinx-docs/user/features/run-build.md`
- [x] Create `sphinx-docs/user/features/testing.md`
- [x] Create `sphinx-docs/user/features/code-analysis.md`
- [x] Create `sphinx-docs/user/features/advanced.md`
- [x] Create `sphinx-docs/user/configuration.md`
- [x] Create `sphinx-docs/user/rescript-basics.md`
- [x] Create `sphinx-docs/user/keyboard-shortcuts.md`
- [x] Create `sphinx-docs/user/troubleshooting.md`
- [x] Create `sphinx-docs/user/changelog.md`
- [x] Commit: `📝 Add user documentation`

## Phase 4: Document Tree — Developer Docs

- [x] Create `sphinx-docs/dev/index.md` (developer docs landing)
- [x] Create `sphinx-docs/dev/architecture.md` (hybrid architecture)
- [x] Create `sphinx-docs/dev/setup.md` (JDK 21+, Gradle, IDE setup)
- [x] Create `sphinx-docs/dev/building.md` (build & run commands)
- [x] Create `sphinx-docs/dev/project-structure.md` (source code layout)
- [x] Create `sphinx-docs/dev/extending.md` (Extension Point patterns)
- [x] Create `sphinx-docs/dev/testing.md` (testing guide)
- [x] Create `sphinx-docs/dev/contributing.md` (contribution guide)
- [x] Commit: `📝 Add developer documentation`

## Phase 5: i18n Setup

- [x] Run `make gettext` to extract .pot files
- [x] Run `make update-po` to generate Japanese .po files
- [x] Translate `index.po` (root landing page)
- [x] Translate `user/installation.po`
- [x] Translate `user/quickstart.po`
- [x] Run `make build-ja` and verify Japanese build
- [x] Verify language switcher works (EN ↔ JA navigation)
- [x] Commit: `📝 Add Japanese translations for key pages`

## Phase 6: GitHub Actions Deployment

- [x] Create `.github/workflows/docs.yml` (build + deploy workflow)
- [x] Verify workflow syntax with actionlint — passed with no errors
- [x] Commit: `📝 Add GitHub Actions docs deployment workflow`

## Phase 7: Final Verification & Merge

- [x] Run `make build-all` locally — verify English build
- [x] Run `make build-all` locally — verify Japanese build
- [x] Run `make linkcheck` — no broken links
- [x] Verify language switcher EN → JA and JA → EN
- [x] Fix language switcher template (rename layout.html → page.html for Furo compatibility)
- [x] Commit: `🐛 Fix language switcher template for Furo theme`
- [x] Update tasklist.md — all tasks checked
- [x] Merge `docs/sphinx-documentation` branch to `main`
- [x] Delete branch `docs/sphinx-documentation`

## Phase A: Documentation Content Expansion (English Source)

- [x] A1: Expand `syntax-highlighting.md` — Layer 1 element details, JSX/React section, Layer 2 semantic token details
- [x] A2: Expand `code-completion.md` — Postfix completion use cases, Live Templates expansion details
- [x] A3: Expand `code-editing.md` — Intention Actions Before/After, Surround With Before/After
- [x] A4: Expand `run-build.md` — Command details, Watch mode behavior
- [x] A5: Expand `testing.md` — Jest/Vitest configuration details
- [x] A6: Expand `code-analysis.md` — Inspection code examples
- [x] A7: Expand `advanced.md` — Code Lens, JS Preview, Module Hierarchy details
- [x] A8: Expand `features/index.md` — Native vs. LSP table explanations
- [x] A9: Expand `configuration.md` — Color key details, code style explanations
- [x] A10: Expand `keyboard-shortcuts.md` — Usage scenario descriptions
- [x] A11: Expand `dev/architecture.md` — Key Classes table details
- [x] Commit: `📝 Expand feature documentation with detailed explanations`

## Phase B: Pagefind Full-Text Search

- [x] B1: Create `sphinx-docs/_templates/search.html` (Pagefind UI)
- [x] B2: Update `sphinx-docs/conf.py` — `html_additional_pages` for search
- [x] B3: Update `sphinx-docs/Makefile` — `pagefind` target, integrate into `build-all`
- [x] B4: Update `sphinx-docs/pyproject.toml` — add `pagefind` dev dependency (note: Pagefind uses npx, not pip)
- [x] B5: Update `.github/workflows/docs.yml` — Pagefind build step
- [x] Commit: `✨ Add Pagefind search and Python quality tools` (combined with Phase C)

## Phase C: Python Quality Tools

- [x] C1: Update `sphinx-docs/pyproject.toml` — dev deps (ruff, mypy, pytest, pytest-cov) + tool config
- [x] C2: Create `sphinx-docs/tests/test_generate_rescript_lexer.py`
- [x] C3: Fix existing code with ruff (conf.py, generate_rescript_lexer.py)
- [x] C4: Update `sphinx-docs/Makefile` — lint, typecheck, test, check targets
- [x] C5: Update `.github/workflows/docs.yml` — lint-and-test job
- [x] C6: Run `make check` to verify all tools pass (21 tests passed, ruff clean, mypy clean)
- [x] Commit: combined with Phase B commit above

## Phase D: Full Japanese Translation

- [x] D1: Regenerate .po files (`make gettext && make update-po`)
- [x] D2: Translate Group 1 — User docs (index, installation, quickstart, user/index, configuration, rescript-basics, keyboard-shortcuts, troubleshooting, changelog)
- [x] D3: Translate Group 2 — Feature docs (features/index, syntax-highlighting, code-completion, navigation, code-editing, run-build, testing, code-analysis, advanced)
- [x] D4: Translate Group 3 — Dev docs (dev/index, architecture, setup, building, project-structure, extending, testing, contributing)
- [x] D5: Verify Japanese build (`make build-ja`)
- [x] Commit: `📝 Add Japanese translations for all pages`

## Phase E: Final Verification & Merge

- [x] E1: Run `make clean && make build-all` — EN/JA build success
- [x] E2: Run `make linkcheck` — no broken links
- [x] E3: Run `make check` — ruff, mypy, pytest all pass
- [x] E4: Verify language switcher (EN ↔ JA) — templates and HTML output verified in build
- [x] E5: Verify Pagefind search (EN / JA) — Pagefind indexed 59 pages, 5163 words across 2 languages
- [x] E6: Update tasklist.md — all tasks checked
- [x] E7: Commit final tasklist update
- [x] E8: Merge to `main`, delete branch

## Notes

- **No test required for Kotlin**: This task creates documentation files only (Sphinx Markdown + config). No Kotlin/Java code changes. Per CLAUDE.md test convention exception, UI/documentation-only changes do not require unit tests.
- **Python tests**: Phase C adds pytest tests for the `generate_rescript_lexer.py` script.
- **No shared doc updates**: CLAUDE.md and docs/ files are not updated since this adds a new independent documentation system (`sphinx-docs/`) alongside existing internal docs.
- **Package manager**: Uses uv instead of pip/requirements.txt. Python version is managed by uv (3.12+).
- **Custom Pygments lexer**: Auto-generated from Rescript.flex at build time via `_ext/generate_rescript_lexer.py`. Colors match the plugin's Darcula (dark) and Default (light) themes.
