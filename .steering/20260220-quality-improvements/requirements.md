# Requirements: Quality Improvements

## Overview

Improve overall code quality and documentation accuracy across three sub-tracks:
1. **Test Quality** - Extend unit tests with more edge cases
2. **Code Quality** - Remove duplicated code (`@Suppress("DuplicatedCode")`) via refactoring
3. **Documentation Fixes** - Correct outdated information in docs

## Acceptance Criteria

### 4a. Test Quality
- Add edge case tests to existing test suites for better coverage
- No `BasePlatformTestCase` integration tests required (unit test scope only)

### 4b. Code Quality
- Remove all `@Suppress("DuplicatedCode")` annotations from:
  - `RescriptReanalyzeAnnotator.kt` (2 occurrences)
  - `RescriptGenerateModuleTypeAction.kt` (2 occurrences)
- Extract shared code into common helpers/utility classes
- Existing tests still pass after refactoring

### 4c. Documentation Fixes
- `docs/ideas/concept.md` - Add historical document notice
- `sphinx-docs/dev/building.md` - Fix outdated `pluginUntilBuild` reference
- `docs/product-requirements.md` - Update NFR-03 (codebase size) and platform support (CE support)
