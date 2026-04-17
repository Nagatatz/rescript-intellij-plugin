# Requirements — Fix Deprecated API Usages

## Background

The JetBrains Marketplace plugin verifier report for ReScript plugin 0.1.12 lists:

- **Scheduled for removal (1):** `FloatingToolbarProvider.getPriority()`
- **Deprecated methods (30):**
  - `ToolWindowFactory.isApplicable(Project)` (10)
  - `ToolWindowFactory.isDoNotActivateOnStart()` (10)
  - `ProjectViewNodeDecorator.decorate(...)` (4)
  - `ReadAction.compute(ThrowableComputable)` (2)
  - `StatusBarWidget.getPresentation(...)` (2)
  - `CompletionConfidence.shouldSkipAutopopup(...)` (1)
  - `StubBasedPsiElementBase.getElementType()` (1)
- **Deprecated classes (2):** `MarkedString` (LSP4J)
- **Deprecated enum (1):** `StatusBarWidget.PlatformType`

The local Marketplace verifier suppresses these via `plugin-verifier-ignored-problems.txt`. The public Marketplace report does not honor that file, so the warnings remain visible on the plugin page.

## Goal

Reduce the Marketplace warning count by **resolving every usage that has a source-level fix** and keeping honest suppressions only for cases where no replacement exists.

## Scope — Items to fix

### A. Kotlin bytecode-bridge warnings (eliminate via compiler flag)

Enable `-Xjvm-default=all` in the Kotlin compiler options. This stops Kotlin from generating synthetic `DefaultImpls` bridge methods for Java interface default methods, which eliminates:

- `ToolWindowFactory.isApplicable(Project)` — 10 bridges (5 factories × 2 inherited methods… actually 10 × 1)
- `ToolWindowFactory.isDoNotActivateOnStart()` — 10 bridges
- `ProjectViewNodeDecorator.decorate(PackageDependenciesNode, ColoredTreeCellRenderer)` — bridge usages
- `StatusBarWidget.getPresentation(PlatformType)` — bridge usage
- `StatusBarWidget.PlatformType` enum — bridge usage

**Risk:** `-Xjvm-default=all` changes the ABI of any library classes we expose. This plugin does not export any library types for external consumers, so the change is safe.

### B. Direct source-level fixes

| # | API | File | Fix |
|---|-----|------|-----|
| B1 | `FloatingToolbarProvider.getPriority()` | `RescriptFloatingToolbarProvider.kt:44` | Remove the `priority` override. Ordering controlled via `plugin.xml` `order=` attribute. |
| B2 | `ReadAction.compute(ThrowableComputable)` | `RescriptTypeInfoPanel.kt:110` | Replace with `ApplicationManager.getApplication().runReadAction(Computable { ... })`. |
| B3 | `ReadAction.compute(ThrowableComputable)` | `RescriptLspUtils.kt:97` | Same replacement as B2. |
| B4 | `StubBasedPsiElementBase.getElementType()` | `RescriptDeclarationPsiElement.kt:32` | Use `node.elementType` (non-deprecated ASTNode property) in `toString()`. |

### C. Irreducible — keep suppressed

Cannot be eliminated at source level. Keep `@Suppress` on source and entries in `plugin-verifier-ignored-problems.txt`:

| API | Reason |
|-----|--------|
| `FloatingToolbarProvider.isApplicable(DataContext)` | Deprecated with no replacement API in IntelliJ 2025.3. Required for file-type filtering. |
| `CompletionConfidence.shouldSkipAutopopup(...)` | Deprecated with no replacement API. Required to suppress popups inside comments/strings. |
| `ProjectViewNodeDecorator.decorate(ProjectViewNode, PresentationData)` | If Platform reports this overload itself as deprecated (not just the bridge), no replacement exists. Keep suppressed. |
| `MarkedString` (LSP4J) | Transitive type in `Hover.getContents()` return signature. Not fixable until LSP4J releases a version without it. |

## Non-Goals

- Rewriting features that depend on deprecated APIs.
- Migrating to Java source to avoid Kotlin bridges (drastic, not justified).
- Updating LSP4J (bundled with IntelliJ Platform; out of our control).

## Acceptance Criteria

1. `./gradlew clean buildPlugin` succeeds.
2. `./gradlew test` passes.
3. `./gradlew ktlintCheck` passes.
4. `./gradlew verifyPlugin` passes (respecting the updated `plugin-verifier-ignored-problems.txt`).
5. All ToolWindowFactory / StatusBarWidget / ProjectViewNodeDecorator bridge entries are **removed** from `plugin-verifier-ignored-problems.txt` if the `-Xjvm-default=all` change eliminates them. Remaining entries have updated `Reviewed:` dates and clear rationale.
6. Fix B1 removes `FloatingToolbarProvider.getPriority()` from both source and the ignored-problems list.
7. Fixes B2–B4 change source such that the deprecation report for those lines disappears.
8. Features remain functional: floating toolbar appears on ReScript files with the same actions, type-info tool window shows types, LSP hover queries succeed, declaration `toString()` still produces a meaningful debug string.

## Out of Scope Cases (Document Why)

After implementation, update `plugin-verifier-ignored-problems.txt` with a new `Reviewed: 2026-04-17` section explaining, for each remaining entry, exactly why it cannot be fixed and what would make the entry removable (e.g., "LSP4J 0.24+ removes `MarkedString`").
