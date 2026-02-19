# Tasklist: docs.yml Workflow Improvements

- [x] Create feature branch from main
- [x] Apply all 6 improvements to `.github/workflows/docs.yml`
  - [x] Enable uv cache (`enable-cache: true`) in both jobs
  - [x] Replace 5 build steps with `make build-all`
  - [x] Parallelize lint-and-test and build jobs
  - [x] Add `workflow_dispatch` trigger
  - [x] Implement PR-scoped concurrency groups
  - [x] Add link check Job Summary output
- [x] Validate with actionlint (if available)
- [x] Commit changes
- [x] Confirm merge to main
