# Design: docs.yml Workflow Improvements

## Target File

- `.github/workflows/docs.yml`

## Job Dependency Graph

### Before
```
lint-and-test → build → deploy
```

### After
```
lint-and-test ─┐
               ├→ deploy
build ─────────┘
```

## Detailed Changes

### 1. uv cache
Add `enable-cache: true` to both `setup-uv` steps (lint-and-test and build jobs).

### 2. `make build-all`
Replace the 5 separate build steps (generate-lexer, html, build-ja, assemble, pagefind) with a single `make build-all` step. The Makefile's `build-all` target already handles all of these.

### 3. Parallel jobs
Remove `needs: lint-and-test` from `build` job. Add `needs: [lint-and-test, build]` to `deploy` job.

### 4. `workflow_dispatch`
Add `workflow_dispatch:` to the `on:` triggers.

### 5. PR-scoped concurrency
Replace fixed `"pages"` group with a conditional expression:
- Push events: `"pages"` (for deployment serialization)
- PR events: `"docs-{head_ref}"` (per-PR isolation)
- `cancel-in-progress` only for PRs

### 6. Link check Job Summary
Add a new step after link check that reads `_build/linkcheck/output.txt` and writes it to `$GITHUB_STEP_SUMMARY`.
