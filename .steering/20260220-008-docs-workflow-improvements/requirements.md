# Requirements: docs.yml Workflow Improvements

## Overview

Improve the `.github/workflows/docs.yml` GitHub Actions workflow for better CI speed, maintainability, reliability, and operational flexibility.

## Changes

1. **uv cache**: Enable `enable-cache: true` in `astral-sh/setup-uv@v6` to avoid full reinstall on every run
2. **`make build-all`**: Replace 5 build steps with single `make build-all` to eliminate logic duplication with Makefile
3. **Parallel jobs**: Run `lint-and-test` and `build` in parallel; `deploy` waits on both
4. **`workflow_dispatch`**: Add manual trigger for debugging and redeployment
5. **PR-scoped concurrency**: Use PR-specific concurrency groups instead of fixed `"pages"` group
6. **Link check Job Summary**: Output link check results to `$GITHUB_STEP_SUMMARY` for PR visibility

## Acceptance Criteria

- All 6 improvements applied to `docs.yml`
- `actionlint` passes on the modified workflow
- No functional regression (same build/deploy behavior)
