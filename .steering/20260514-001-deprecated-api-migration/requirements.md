# Requirements: Deprecated API Migration (0.1.14)

## 背景

Marketplace の Plugin Verifier レポートで以下の deprecated API 利用が指摘された:

| API | 件数 | ソースファイル |
|---|---|---|
| `CompletionConfidence.shouldSkipAutopopup(PsiElement, PsiFile, int)` | 1 | `completion/RescriptCompletionConfidence.kt` |
| `FloatingToolbarProvider.isApplicable(DataContext)` | 1 | `editor/RescriptFloatingToolbarProvider.kt` |
| `FileIncludeProvider.acceptFile(VirtualFile)` | 1 | `navigation/RescriptFileIncludeProvider.kt` |
| `MarkedString` (class) | 2 | `lsp/RescriptLspUtils.kt` |

調査の結果、compile target である IntelliJ Platform 2026.1.1 で以下のとおり判明:

- `CompletionConfidence` は 4-arg overload (`Editor` first) が利用可能 → **置換できる**
- `FloatingToolbarProvider.isApplicableAsync` は 2026.1.1 にまだ追加されていない → **suppression 維持**
- `FileIncludeProvider.acceptFile(IndexedFile)` は 2026.1.1 にまだ追加されていない → **suppression 維持**
- `MarkedString` は LSP4J の Hover.contents 内で参照されており、左ブランチの右経路アクセスを撤去すれば bytecode から消える → **撤去できる**

## 受け入れ条件

- [x] `RescriptCompletionConfidence.shouldSkipAutopopup` が `Editor` を第一引数に取る非 deprecated overload に移行している
- [x] `RescriptLspUtils.getHoverType` から `MarkedString` への参照が消えている
- [x] `plugin-verifier-ignored-problems.txt` から CompletionConfidence と MarkedString のエントリを削除
- [x] `FloatingToolbarProvider` / `FileIncludeProvider` は 2026.1.1 では置換 API が存在しないため、suppression を維持
- [x] `plugin-verifier-ignored-problems.txt` のコメントと日付を更新
- [x] `./gradlew ktlintCheck clean buildPlugin test` がすべて成功する
- [x] 既存テストがすべてグリーン
- [ ] tasklist.md / steering の `[x]` を更新してコミット
- [ ] main マージ可否確認

## スコープ外

- pluginVersion の更新 / リリース手順
- LSP4J のバージョン更新
- 他の deprecated 警告（今回レポートされなかったもの）
- platformVersion を `isApplicableAsync` / `acceptFile(IndexedFile)` 同梱版にバンプする作業 (将来別途)
