# Notebook 風 Worksheet — Tasklist

## Phase 1: 計画
- [x] requirements.md 作成
- [x] design.md 作成
- [x] tasklist.md 作成
- [x] requirements / design / tasklist のユーザー承認
- [x] `EnterWorktree notebook-worksheet` で worktree 作成

## Phase 2: 既存資産の確認
- [x] `RescriptReplExecutor.execute(code, projectPath)` の戻り値仕様を確認（成功時 stdout、失敗時 "Error: ..."）
- [x] Gson が既に依存に入っていることを確認（`com.google.gson` を `RescriptReanalyzeAnnotator` で利用中）

## Phase 3: 実装（コアロジック）
- [x] `notebook/RescriptNotebookModel.kt` を実装（NotebookCell + NotebookDocument）
- [x] `notebook/RescriptNotebookSerializer.kt` を実装（Gson ベース、permissive parsing）
- [x] `notebook/RescriptNotebookSerializerTest.kt` を作成（empty / single / multi cell ラウンドトリップ + 不正 JSON + unknown fields の 8 ケース）
- [x] `notebook/RescriptNotebookMarkdownExporter.kt` を実装
- [x] `notebook/RescriptNotebookMarkdownExporterTest.kt` を作成（4 ケース）

## Phase 3: 実装（IDE 統合）
- [x] `notebook/RescriptNotebookFileType.kt` を実装
- [x] `notebook/RescriptNotebookFileEditorProvider.kt` を実装（HIDE_DEFAULT_EDITOR）
- [x] `notebook/RescriptNotebookFileEditor.kt` を実装（パース失敗時のフォールバック表示付き）
- [x] `notebook/RescriptNotebookPanel.kt` を実装（toolbar + cell list）
- [x] `notebook/RescriptNotebookCellPanel.kt` を実装（code + run + output、background 評価）
- [x] `plugin.xml` に fileType と fileEditorProvider を登録

## Phase 3: コミット前検証
- [x] `./gradlew ktlintCheck` パス
- [x] `./gradlew clean buildPlugin` パス
- [x] `./gradlew test` パス
- [x] ビルド警告が増加していない（既存の RescriptLsp4jClient 警告のみ）
- [x] Deprecated API なし

## Phase 3: ドキュメント更新
- [x] `CLAUDE.md` レイヤー 3 に `notebook/` パッケージを追記
- [x] `docs/repository-structure.md` パッケージ表に `notebook/` を追加
- [x] `docs/functional-design.md` Extension Point マップに fileType / fileEditorProvider を追加
- [x] `README.md` Features セクションに「Notebook-style worksheet」追加
- [x] `sphinx-docs/user/features/advanced.md` に Notebook セクション
- [x] 日本語 `.po` 同時更新（`make build-ja` 成功）
- [x] `docs/lsp-fallback-matrix.md` に「Notebook 風 Worksheet」行を追加

## Phase 3: コミット
- [x] Model + Serializer + MarkdownExporter コミット（`✨ Add notebook document model and serializers`）
- [x] FileType + FileEditor + Panel コミット（`✨ Add notebook-style worksheet editor`）
- [ ] ドキュメント更新コミット（`📝 Document notebook-style worksheet`）
- [ ] tasklist 完了化コミット

## Phase 4: マージ前
- [ ] 全タスク `[x]` 確認
- [ ] requirements 受け入れ条件確認
- [ ] `AskUserQuestion` でマージ可否確認

## Phase 5: マージ後
- [ ] main へマージ + ブランチ削除 + worktree クリーンアップ

## テスト免除
- `RescriptNotebookPanel`, `RescriptNotebookCellPanel`: Swing UI のためテスト免除
- `RescriptNotebookFileEditor`, `RescriptNotebookFileEditorProvider`: IDE ライフサイクル依存のためテスト免除
- `RescriptNotebookFileType`: FileType 定義のみ（インターフェース実装）でテスト免除
