# Tasklist — Fix Deprecated API Usages

## Phase 1: Setup

- [ ] Create worktree `fix-deprecated-apis` via `EnterWorktree` (branch `worktree-fix-deprecated-apis` from `main`).

## Phase 2: Implementation

### Task A — Enable `-Xjvm-default=all`

- [ ] Edit `build.gradle.kts`: add `compilerOptions { freeCompilerArgs.add("-Xjvm-default=all") }` inside the `kotlin { ... }` block.
- [ ] Run `./gradlew clean buildPlugin` — confirm build succeeds with no new errors.
- [ ] Run `./gradlew test` — confirm all tests pass.
- [ ] Commit: `🔧 Enable -Xjvm-default=all to eliminate DefaultImpls bridge warnings`.

### Task B1 — Remove `FloatingToolbarProvider.priority` override

- [ ] Edit `src/main/kotlin/com/rescript/plugin/editor/RescriptFloatingToolbarProvider.kt`: remove the `@Suppress("OVERRIDE_DEPRECATION") override val priority: Int = 0` block and the 3-line explanatory comment preceding it (lines 40–44).
- [ ] Verify no `plugin.xml` changes are needed (priority=0 was default).
- [ ] Run `./gradlew test` — confirm `RescriptFloatingToolbarProviderTest` still passes.
- [ ] Commit: `♻️ Remove deprecated FloatingToolbarProvider.priority override`.

### Task B2/B3 — Replace `ReadAction.compute` with `runReadAction(Computable)`

- [ ] Edit `src/main/kotlin/com/rescript/plugin/typeinfo/RescriptTypeInfoPanel.kt`:
  - Replace `ReadAction.compute<Int, RuntimeException> { ... }` with `ApplicationManager.getApplication().runReadAction(Computable { ... })` at line 110.
  - Update imports: remove `ReadAction`, add `ApplicationManager` and `Computable`.
- [ ] Edit `src/main/kotlin/com/rescript/plugin/lsp/RescriptLspUtils.kt`:
  - Replace `ReadAction.compute<Position?, RuntimeException> { ... }` with `ApplicationManager.getApplication().runReadAction(Computable<Position?> { ... })` at line 97.
  - Update imports accordingly.
- [ ] Test note: `typeinfo` and `lsp` packages are test-exempt per CLAUDE.md (IDE/LSP coupled). Verify via existing build + runIde sandbox.
- [ ] Run `./gradlew ktlintCheck test` — confirm style and tests pass.
- [ ] Commit: `♻️ Replace deprecated ReadAction.compute with runReadAction(Computable)`.

### Task B4 — Use `node.elementType` in `RescriptDeclarationPsiElement.toString()`

- [ ] Edit `src/main/kotlin/com/rescript/plugin/lang/psi/RescriptDeclarationPsiElement.kt`:
  - Remove the `@Suppress("DEPRECATION")` annotation on `toString()`.
  - Remove the preceding `// StubBasedPsiElementBase.elementType is deprecated...` comment.
  - Change `val type = elementType` to `val type = node.elementType`.
- [ ] If a test asserts `toString()` format, confirm it still passes. Check `src/test/kotlin/com/rescript/plugin/lang/psi/RescriptDeclarationPsiElementTest.kt` if present.
- [ ] Run `./gradlew test` — confirm all tests pass.
- [ ] Commit: `♻️ Use ASTNode.elementType in RescriptDeclarationPsiElement.toString`.

### Task C — Update `plugin-verifier-ignored-problems.txt`

- [ ] Edit `plugin-verifier-ignored-problems.txt`:
  - Update `Reviewed:` date to `2026-04-17` for each section touched.
  - Add top-level note explaining `-Xjvm-default=all` prevents bridge regeneration (so these entries must not be re-added).
  - **Remove** the following entries (now resolved):
    - `FloatingToolbarProvider.*getPriority.*`
    - `ToolWindowFactory.*isDoNotActivateOnStart.*`
    - `ToolWindowFactory.*isApplicable.*`
    - `ProjectViewNodeDecorator.*decorate.*PackageDependenciesNode.*`
    - `StatusBarWidget.*getPresentation.*PlatformType.*`
    - `StatusBarWidget\$PlatformType.*`
    - `StubBasedPsiElementBase.*getElementType.*`
  - **Keep** these entries with refreshed rationale:
    - `CodeVisionPlaceholderCollector` (Internal API, sanctioned).
    - `FloatingToolbarProvider.*isApplicable.*` (explicit override, no replacement).
    - `CompletionConfidence.*shouldSkipAutopopup.*` (no replacement).
    - `MarkedString` (LSP4J transitive).
- [ ] Run `./gradlew verifyPlugin` — confirm verifier passes with the trimmed ignore list.
- [ ] Commit: `🔧 Trim ignored-problems list after resolving deprecated API usages`.

## Phase 3: Commit-Time Verification

- [ ] `./gradlew ktlintCheck` — passes.
- [ ] `./gradlew clean buildPlugin` — passes.
- [ ] `./gradlew test` — passes.
- [ ] `./gradlew verifyPlugin` — passes with trimmed ignore list.
- [ ] No new compiler warnings introduced.
- [ ] All commits use emoji prefix + English message per `git-conventions.md`.
- [ ] Each commit is scoped to one logical change (A / B1 / B2-B3 / B4 / C).

### Documentation sync check

This work is internal refactoring with no user-visible feature change. No documentation updates are required:

- `CLAUDE.md` — no architecture change.
- `README.md` — no feature added/removed.
- `sphinx-docs/user/features/` — no feature behavior change.
- `docs/product-requirements.md` — no roadmap item affected.

Confirm by reviewing the diff: no behavioral change should be visible to users.

### Manual sandbox check (post-Phase-3)

- [ ] `./gradlew runIde` — IDE starts, open a `.res` file:
  - [ ] Floating toolbar appears with Format / Open Compiled JS / Create Interface actions.
  - [ ] Type Info tool window shows types on caret movement.
  - [ ] Symbol hover shows type info.
  - [ ] Tool windows (REPL, Dependencies, Compiled JS Preview, PPX View, Type Info) open without errors.
  - [ ] Project view shows `.res` files decorated correctly (interface suffix, compiled JS gray).
  - [ ] Status bar shows ReScript compiler status.

## Phase 4: Merge

- [ ] Confirm all Phase 2 tasks are `[x]`.
- [ ] Confirm Phase 3 verifications are `[x]`.
- [ ] Update this tasklist with final `[x]` state including this merge task.
- [ ] Commit the tasklist update: `📝 Mark fix-deprecated-apis tasks complete`.
- [ ] Run `AskUserQuestion` to confirm merge into `main`.
- [ ] After approval: in the worktree, `git checkout main && git merge worktree-fix-deprecated-apis && git branch -d worktree-fix-deprecated-apis`.

## Phase 5: Cleanup

- [ ] End the session so the worktree is auto-cleaned.
