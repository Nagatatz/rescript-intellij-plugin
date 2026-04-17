# Design — Fix Deprecated API Usages

## A. Kotlin compiler flag: `-Xjvm-default=all`

### Change

In `build.gradle.kts`, add Kotlin compiler options:

```kotlin
kotlin {
    jvmToolchain(21)
    compilerOptions {
        freeCompilerArgs.add("-Xjvm-default=all")
    }
}
```

### Effect

Kotlin stops generating `DefaultImpls` synthetic classes for Java-interface default methods. Instead, our classes inherit the interface default method via standard JVM default method dispatch. The bytecode reference chain from `com.rescript.plugin.*` to deprecated default methods (`ToolWindowFactory.isApplicable`, `isDoNotActivateOnStart`, `ProjectViewNodeDecorator.decorate(PackageDependenciesNode,...)`, `StatusBarWidget.getPresentation(PlatformType)`) is no longer emitted by the Kotlin compiler for classes that don't override those methods.

### Risk & Mitigation

- **ABI change:** Only affects classes in this plugin. No library consumers.
- **Runtime behavior:** Identical — the default method from the interface is still invoked; only the bytecode dispatch path changes.
- **Verification:** Run full test suite, run IDE sandbox (`./gradlew runIde`), confirm tool windows open and floating toolbar appears.

### What this flag does NOT fix

- Explicit `override fun` we wrote against deprecated methods — those remain direct usages. (Those are the items in B and C.)

## B. Source-level fixes

### B1. `FloatingToolbarProvider.priority`

**File:** `src/main/kotlin/com/rescript/plugin/editor/RescriptFloatingToolbarProvider.kt`

**Before (lines 40–44):**
```kotlin
// Explicit override to avoid Kotlin bridge method invoking DefaultImpls,
// which triggers a Marketplace verification warning for scheduled-for-removal API.
// Ordering is controlled by the "order" attribute in plugin.xml instead.
@Suppress("OVERRIDE_DEPRECATION")
override val priority: Int = 0
```

**After:**
Delete the override entirely, including the preceding comment block. With `-Xjvm-default=all` active, no DefaultImpls bridge is emitted. The interface's default value (0) is used at runtime.

**plugin.xml:** Verify the `<editor.floatingToolbar>` (or `<editorFloatingToolbarProvider>`) entry is registered with an `order=` attribute if priority-relative ordering matters. If no explicit ordering is currently specified (priority was 0, i.e. default), no change is needed in plugin.xml.

**Test impact:** Feature test for floating toolbar should still pass. If a test asserts priority value, remove that assertion.

### B2/B3. `ReadAction.compute` replacement

**Files:**
- `src/main/kotlin/com/rescript/plugin/typeinfo/RescriptTypeInfoPanel.kt:110`
- `src/main/kotlin/com/rescript/plugin/lsp/RescriptLspUtils.kt:97`

**Before (B2):**
```kotlin
val offset = ReadAction.compute<Int, RuntimeException> { editor.caretModel.offset }
```

**After:**
```kotlin
val offset = ApplicationManager.getApplication().runReadAction(
    Computable { editor.caretModel.offset }
)
```

**Before (B3):**
```kotlin
val position =
    ReadAction.compute<Position?, RuntimeException> {
        val document =
            FileDocumentManager.getInstance().getDocument(file)
                ?: return@compute null
        RescriptOffsetUtils.offsetToPosition(document, offset)
    } ?: return null
```

**After:**
```kotlin
val position =
    ApplicationManager.getApplication().runReadAction(
        Computable<Position?> {
            val document =
                FileDocumentManager.getInstance().getDocument(file)
                    ?: return@Computable null
            RescriptOffsetUtils.offsetToPosition(document, offset)
        },
    ) ?: return null
```

**Imports to update:**
- Remove `import com.intellij.openapi.application.ReadAction`
- Add `import com.intellij.openapi.application.ApplicationManager`
- Add `import com.intellij.openapi.util.Computable`

### B4. `StubBasedPsiElementBase.getElementType()`

**File:** `src/main/kotlin/com/rescript/plugin/lang/psi/RescriptDeclarationPsiElement.kt:29–34`

**Before:**
```kotlin
// StubBasedPsiElementBase.elementType is deprecated but needed for debug display
@Suppress("DEPRECATION")
override fun toString(): String {
    val type = elementType
    return "RescriptDeclarationPsiElement($type)"
}
```

**After:**
```kotlin
override fun toString(): String {
    val type = node.elementType
    return "RescriptDeclarationPsiElement($type)"
}
```

`PsiElement.getNode()` returns the `ASTNode`, and `ASTNode.getElementType()` is not deprecated. For stub-backed elements, `node` may be lazily constructed, but `toString()` is a debug-only path so the cost is acceptable.

Test impact: any test asserting the exact `toString()` format must continue to show the element type — no format change.

## C. Suppression policy update

After A and B are applied, update `plugin-verifier-ignored-problems.txt`:

### Remove these entries (resolved by A or B)
- `FloatingToolbarProvider.*getPriority.*` (B1 removes the override; `-Xjvm-default=all` removes the bridge)
- `ToolWindowFactory.*isDoNotActivateOnStart.*` (A eliminates bridge)
- `ToolWindowFactory.*isApplicable.*` (A eliminates bridge; the explicit `isApplicable` on `FloatingToolbarProvider` is a different class — keep a separate entry for it)
- `ProjectViewNodeDecorator.*decorate.*PackageDependenciesNode.*` (A eliminates bridge)
- `StatusBarWidget.*getPresentation.*PlatformType.*` (A eliminates bridge)
- `StatusBarWidget\$PlatformType.*` (A eliminates)
- `StubBasedPsiElementBase.*getElementType.*` (B4)

### Keep these entries (irreducible)
- `CodeVisionPlaceholderCollector` — Internal API marker; JetBrains-sanctioned usage.
- `FloatingToolbarProvider.*isApplicable.*` — No replacement; keep `@Suppress` on source.
- `CompletionConfidence.*shouldSkipAutopopup.*` — No replacement; keep `@Suppress` on source.
- `MarkedString` — LSP4J transitive dependency.

### Refresh
- Update the file header `Reviewed: 2026-04-17 | Target: IntelliJ 2025.3+`.
- Add a note at the top describing the `-Xjvm-default=all` dependency so future reviewers know not to re-add the bridge entries.

## Test coverage

Production code changes with test impact:

| Change | Test change |
|--------|-------------|
| B1 — remove priority override | None (priority=0 was already the default; no test asserts this). Verify existing `RescriptFloatingToolbarProviderTest` still passes. |
| B2 — `RescriptTypeInfoPanel` (UI) | `typeinfo` package is test-exempt per CLAUDE.md (IDE-coupled UI). No unit test change. Validate via `runIde` sandbox. |
| B3 — `RescriptLspUtils` | `lsp` package is test-exempt (LSP server coupling). Validate via existing hover integration. |
| B4 — `RescriptDeclarationPsiElement.toString()` | Confirm `RescriptDeclarationPsiElementTest` (if exists) still passes. If none, no new test — `toString()` is debug-only per `code-comments.md` simplicity rules. |

No new test files are required. Existing build/test pipeline must pass.

## Verification plan

1. `./gradlew ktlintCheck` — style passes.
2. `./gradlew clean buildPlugin` — build passes without new warnings.
3. `./gradlew test` — unit tests pass.
4. `./gradlew verifyPlugin` — Marketplace verifier passes with updated ignored-problems file.
5. Manual sandbox (`./gradlew runIde`):
   - Open a `.res` file → floating toolbar appears.
   - Open Type Info tool window → caret-position types render.
   - Hover on a symbol → type popup shows.
   - Open the REPL / Dependencies / Compiled JS / PPX / TypeInfo tool windows → all open without errors.
6. Compare the new Marketplace report after next release — warning count should drop from 34 to ≤ 5 (keep only the irreducible C entries).

## Rollback

If `-Xjvm-default=all` causes runtime issues (e.g., `NoSuchMethodError` on an older JDK), revert the flag. All B and C changes are independent and can remain.
