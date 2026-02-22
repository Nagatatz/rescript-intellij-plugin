# Tasklist: Utility Code Consolidation Refactoring

## Group A: PSI Utilities
- [x] Add constant sets and helper methods to RescriptPsiUtils.kt
- [x] Update RescriptPsiUtilsTest.kt for new methods
- [x] Replace duplicates in 8 source files
- [x] Commit Group A

## Group B: LSP Utilities
- [x] Add getServer(), toLspUri(), lspUriToVfsUrl() to RescriptLspUtils.kt
- [x] Add RescriptLspUtilsTest.kt for new methods
- [x] Replace duplicates in 6 source files
- [x] Commit Group B

## Group C: Test Stubs
- [x] Add stubPsiElement() and stubProject() to RescriptTestUtils.kt
- [x] Replace duplicates in 5 test files
- [x] Commit Group C

## Group D: Open Statement Regex
- [x] Add OPEN_PATTERN and helpers to RescriptImportUtil.kt
- [x] Add tests for new helpers
- [x] Replace duplicates in 3 source files
- [x] Commit Group D

## Group E: WHITESPACE_REGEX
- [x] Create RescriptRunUtils.kt with shared constant
- [x] Replace duplicates in 3 run configuration files
- [x] Commit Group E

## Finalization
- [x] Verify build: ./gradlew clean buildPlugin
- [x] Verify tests: ./gradlew test
- [x] Update documentation (CLAUDE.md, docs/product-requirements.md)
- [x] Final commit and merge
