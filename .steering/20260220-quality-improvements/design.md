# Design: Quality Improvements

## 4a. Test Quality

Extend existing unit tests with additional edge cases. No `BasePlatformTestCase` integration tests -- the existing test infrastructure is unit-test-based using stubs/proxies and does not include fixture data or platform test setup.

### Additional edge cases:
- `RescriptReanalyzeAnnotatorTest` - Test the new `parseDiagnosticEntry()` helper
- `RescriptGenerateModuleTypeActionTest` / `RescriptGenerateSwitchActionTest` - Ensure refactored code still works

## 4b. Code Quality

### RescriptReanalyzeAnnotator.kt

**Problem:** `parseJsonOutput()` and `parseAllDiagnostics()` share nearly identical JSON parsing logic.

**Solution:** Extract a private `parseDiagnosticEntry(obj: JsonObject): Pair<String, ReanalyzeDiagnostic>?` method that parses a single JSON entry into a `(file, diagnostic)` pair. Both methods call this helper:
- `parseJsonOutput()` filters by file path, maps to `ReanalyzeDiagnostic`
- `parseAllDiagnostics()` returns all entries with file info

**`apply()` method:** The `@Suppress("DuplicatedCode")` on the apply method is for duplication with `RescriptUnusedCodeInspection.runInspection()` which has similar TextRange calculation logic. Since those are in different classes with different contexts (AnnotationHolder vs ProblemDescriptionsProcessor), a shared utility would add coupling without clear benefit. Remove the suppress annotation -- the minor structural similarity is acceptable.

### RescriptGenerateModuleTypeAction.kt / RescriptGenerateSwitchAction.kt

**Problem:** Both actions have duplicated patterns for getting editor/psi context and finding enclosing declarations.

**Solution:** Create a `RescriptGenerateActionUtil` utility object:
```kotlin
object RescriptGenerateActionUtil {
    fun findEnclosingDeclaration(e: AnActionEvent, elementType: IElementType): PsiElement?
    fun isInsideDeclaration(e: AnActionEvent, elementType: IElementType): Boolean
}
```

Both actions delegate to this utility for common editor context logic.

## 4c. Documentation Fixes

1. **`docs/ideas/concept.md`** - Add a blockquote notice at the top
2. **`sphinx-docs/dev/building.md`** - Remove/fix the `pluginUntilBuild` line in the example
3. **`docs/product-requirements.md`** - Update NFR-03 and platform support table
