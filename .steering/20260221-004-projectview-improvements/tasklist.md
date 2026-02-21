# Tasklist: Project View improvements for .res.js files

## Implementation Tasks

- [x] Create `RescriptFileNestingProvider.kt` — register `.res` → `.res.js` and `.resi` → `.resi.js` nesting rules
- [x] Create `RescriptCompiledJsNodeDecorator.kt` — gray out `.res.js` / `.resi.js` files in Project view
- [x] Register both extension points in `plugin.xml`
- [x] Create `RescriptFileNestingProviderTest.kt`
- [x] Create `RescriptCompiledJsNodeDecoratorTest.kt`
- [x] Update `CLAUDE.md` — add `projectview/` to project structure
- [x] Run `./gradlew buildPlugin` and verify success
- [x] Commit with appropriate emoji prefix
- [x] Merge to `main` and delete worktree
