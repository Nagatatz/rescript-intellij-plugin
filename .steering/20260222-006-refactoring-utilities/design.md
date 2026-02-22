# Design: Utility Code Consolidation Refactoring

## Group A: PSI Utilities

Add to `RescriptPsiUtils.kt`:
- `BINDING_TYPES` (LET, EXTERNAL) for AddIgnore, AddUnderscore
- `ANNOTATION_ELIGIBLE_TYPES` (LET, TYPE, MODULE) for AddGenType
- `ALL_DECLARATION_TYPES` (7 types including OPEN, INCLUDE) for EmptyModule, StatementMover
- `findEnclosingDeclaration(element, types)` using PsiTreeUtil
- `isInsideDeclaration(element, types)` boolean helper

Replace local definitions in 8 files.

## Group B: LSP Utilities

Add to `RescriptLspUtils.kt`:
- `getServer(project): LspServer?` for server lookup
- `VirtualFile.toLspUri(): String` for VFS -> LSP URI
- `lspUriToVfsUrl(uri: String): String` for LSP URI -> VFS URL

Replace local patterns in 6 files.

## Group C: Test Stubs

Add to `RescriptTestUtils.kt`:
- `stubPsiElement(type, parent?, prevSibling?, nextSibling?, text?)`
- `stubProject()`

Replace local stubs in 5 test files.

## Group D: Open Statement Regex

Add to `RescriptImportUtil.kt`:
- `OPEN_PATTERN` shared regex
- `findOpenInsertOffset(text)`, `collectOpenModules(text)`, `isModuleOpened(text, moduleName)`

Replace local regex in 3 files.

## Group E: WHITESPACE_REGEX

Add to `run/RescriptRunUtils.kt`:
- `WHITESPACE_REGEX` shared constant

Replace in 3 run configuration files.
