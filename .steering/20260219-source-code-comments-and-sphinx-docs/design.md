# Design: Source Code Comments

## 1. Source Code Comments

### Approach

Add English KDoc comments to all ~85 Kotlin source files. Files that already have some comments (e.g., `RescriptParser.kt`, `RescriptLspServerDescriptor.kt`) will be supplemented where missing. All additions are comment-only; no functional changes.

### Comment Levels

| Level | When Required | Style |
|-------|--------------|-------|
| File-level | Always | `/** ... */` before `class`/`object` declaration |
| Class-level | Always (merged with file-level if 1 class per file) | `/** ... */` |
| Method-level | Public/internal non-trivial methods | `/** ... */` with `@param`/`@return` if meaningful |
| Inline | Complex logic, non-obvious algorithms | `// ...` |

### Comment Guidelines

- **Language**: English only
- **Tone**: Concise and factual; describe *what* and *why*, not *how* (unless the logic is complex)
- **References**: Use `[ClassName]` KDoc links for cross-references
- **Existing comments**: Preserve and improve; do not remove existing useful comments
- **Trivial getters/setters**: No comments needed (self-explanatory)
- **Override methods**: Add comments only if the override behavior is non-obvious

### Processing Order (by package)

Files will be processed package-by-package to maintain context and consistency:

1. **Root** (`RescriptLanguage.kt`, `RescriptFileTypes.kt`, `RescriptIcons.kt`) — 3 files
2. **lang/** (`RescriptTokenTypes.kt`, `RescriptLexer.kt`, `RescriptParser.kt`, `RescriptParserDefinition.kt`, `RescriptAstFactory.kt`) — 5 files
3. **lang/psi/** (`RescriptPsi.kt`, `RescriptPsiUtils.kt`, `RescriptStringLiteral.kt`) — 3 files
4. **highlight/** — 4 files
5. **lsp/** — 6 files
6. **codestyle/** — 2 files
7. **config/** — 2 files
8. **run/** — 9 files
9. **settings/** — 2 files
10. **structure/** — 3 files
11. **indexing/** — 1 file
12. **editor/** — 4 files
13. **formatter/** — 1 file
14. **navigation/** — 6 files
15. **template/** — 1 file
16. **spellcheck/** — 1 file
17. **completion/** — 1 file
18. **analysis/** — 3 files
19. **test/** — 8 files
20. **preview/** — 2 files
21. **hierarchy/** — 5 files
22. **paste/** — 1 file
23. **injection/** — 2 files
24. **codevision/** — 1 file
25. **statusbar/** — 1 file
26. **imports/** — 2 files
27. **intention/** — 2 files
28. **surround/** — 1 file
29. **folding/** — 2 files
30. **wizard/** — 3 files
31. **generate/** — 4 files
32. **commenter/** — 1 file
33. **breadcrumb/** — 1 file
34. **refactor/** — 2 files
35. **inspection/** — 3 files

### Commit Strategy

Comments will be committed in batches (multiple packages per commit) to avoid excessive commit granularity while keeping each commit reviewable:

- Commit 1: Root + lang/ + lang/psi/ + highlight/ (15 files)
- Commit 2: lsp/ + codestyle/ + config/ + run/ + settings/ (21 files)
- Commit 3: structure/ + editor/ + navigation/ + remaining packages (49 files)
- Commit 4: CLAUDE.md update

## 2. CLAUDE.md Update

Add a new subsection under "## 開発規約":

```markdown
### コードコメント規約

- すべてのソースコードに英語の KDoc コメントを含めること
- **ファイル/クラスレベル**: 必須。クラスの目的とプラグインアーキテクチャにおける役割を記述
- **メソッドレベル**: public/internal の非自明なメソッドに必須。意味がある場合は `@param` と `@return` を含める
- **インラインコメント**: 複雑なロジック、非自明なアルゴリズム、ワークアラウンドに必須
- **コメント言語**: 英語のみ
- 他クラスへの参照には `[ClassName]` の KDoc リンクを使用
- 自明な getter/override はコメント不要（動作が非自明な場合を除く）
```

## 3. Impact Analysis

### Modified Files
- All ~85 Kotlin source files (comments only, no functional changes)
- `CLAUDE.md` — add commenting conventions section

### No Changes
- No production code logic changes
- No test changes
- No build system changes
