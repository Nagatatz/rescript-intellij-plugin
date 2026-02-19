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
- [x] Create `sphinx-docs/_templates/layout.html` (language switcher UI)
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

- [ ] Run `make gettext` to extract .pot files
- [ ] Run `make update-po` to generate Japanese .po files
- [ ] Translate `index.po` (root landing page)
- [ ] Translate `user/installation.po`
- [ ] Translate `user/quickstart.po`
- [ ] Run `make build-ja` and verify Japanese build
- [ ] Verify language switcher works (EN ↔ JA navigation)
- [ ] Commit: `📝 Add Japanese translations for key pages`

## Phase 6: GitHub Actions Deployment

- [ ] Create `.github/workflows/docs.yml` (build + deploy workflow)
- [ ] Verify workflow syntax with actionlint (if available)
- [ ] Commit: `📝 Add GitHub Actions docs deployment workflow`

## Phase 7: Final Verification & Merge

- [ ] Run `make build-all` locally — verify English build
- [ ] Run `make build-all` locally — verify Japanese build
- [ ] Run `make linkcheck` — no broken links
- [ ] Verify language switcher EN → JA and JA → EN
- [ ] Update tasklist.md — all tasks checked
- [ ] Merge `docs/sphinx-documentation` branch to `main`
- [ ] Delete branch `docs/sphinx-documentation`

## Notes

- **No test required**: This task creates documentation files only (Sphinx Markdown + config). No Kotlin/Java code changes. Per CLAUDE.md test convention exception, UI/documentation-only changes do not require unit tests.
- **No shared doc updates**: CLAUDE.md and docs/ files are not updated since this adds a new independent documentation system (`sphinx-docs/`) alongside existing internal docs.
- **Package manager**: Uses uv instead of pip/requirements.txt. Python version is managed by uv (3.12+).
- **Custom Pygments lexer**: Auto-generated from Rescript.flex at build time via `_ext/generate_rescript_lexer.py`. Colors match the plugin's Darcula (dark) and Default (light) themes.
