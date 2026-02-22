# Requirements: Utility Code Consolidation Refactoring

## Background

The codebase has accumulated significant code duplication across 5 areas:
- PSI declaration type sets (9 files)
- LSP server access patterns (7 files)
- Test stub helpers (5 test files)
- Open statement regex patterns (3 files)
- WHITESPACE_REGEX constant (3 files)

## Goals

1. Reduce code duplication by consolidating common patterns into shared utilities
2. Improve consistency of error handling and naming across the codebase
3. Make future maintenance easier by having single points of change
4. Preserve all existing behavior (pure refactoring, no functional changes)

## Non-Goals

- Changing error handling strategies (each call site keeps its own approach)
- Refactoring well-cohesive large files (PostfixTemplateProvider, UnwrapDescriptor, etc.)
- Adding new features or capabilities

## Acceptance Criteria

- All existing tests pass
- `./gradlew clean buildPlugin` succeeds
- No functional behavior changes
- ~290 lines of duplicate code eliminated
