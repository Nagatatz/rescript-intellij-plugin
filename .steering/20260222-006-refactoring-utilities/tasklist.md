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
- [ ] Add OPEN_PATTERN and helpers to RescriptImportUtil.kt
- [ ] Add tests for new helpers
- [ ] Replace duplicates in 3 source files
- [ ] Commit Group D

## Group E: WHITESPACE_REGEX
- [ ] Create RescriptRunUtils.kt with shared constant
- [ ] Replace duplicates in 3 run configuration files
- [ ] Commit Group E

## Finalization
- [ ] Verify build: ./gradlew clean buildPlugin
- [ ] Verify tests: ./gradlew test
- [ ] Update documentation (CLAUDE.md, docs/product-requirements.md)
- [ ] Final commit and merge
