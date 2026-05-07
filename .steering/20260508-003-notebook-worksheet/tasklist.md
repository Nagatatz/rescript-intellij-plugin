# Notebook 風 Worksheet — Tasklist

## Phase 1: 計画
- [x] requirements.md 作成
- [x] design.md 作成
- [x] tasklist.md 作成
- [ ] requirements / design / tasklist のユーザー承認
- [ ] `EnterWorktree notebook-worksheet` で worktree 作成

## Phase 2: 既存資産の確認
- [ ] `RescriptReplExecutor.execute(code, projectPath)` の戻り値仕様を確認
- [ ] `org.json` などの JSON ライブラリが既に依存に入っているか確認

## Phase 3: 実装（コアロジック）
- [ ] `notebook/RescriptNotebookModel.kt` を実装（NotebookCell + NotebookDocument）
- [ ] `notebook/RescriptNotebookSerializer.kt` を実装（JSON ↔ Document）
- [ ] `notebook/RescriptNotebookSerializerTest.kt` を作成（empty / single / multi cell ラウンドトリップ + 不正 JSON）
- [ ] `notebook/RescriptNotebookMarkdownExporter.kt` を実装
- [ ] `notebook/RescriptNotebookMarkdownExporterTest.kt` を作成（スナップショット 3-4 件）

## Phase 3: 実装（IDE 統合）
- [ ] `notebook/RescriptNotebookFileType.kt` を実装
- [ ] `notebook/RescriptNotebookFileEditorProvider.kt` を実装
- [ ] `notebook/RescriptNotebookFileEditor.kt` を実装
- [ ] `notebook/RescriptNotebookPanel.kt` を実装（toolbar + cell list）
- [ ] `notebook/RescriptNotebookCellPanel.kt` を実装（code + run + output）
- [ ] `plugin.xml` に fileType と fileEditorProvider を登録

## Phase 3: コミット前検証
- [ ] `./gradlew ktlintCheck` パス
- [ ] `./gradlew clean buildPlugin` パス
- [ ] `./gradlew test` パス
- [ ] ビルド警告が増加していない
- [ ] Deprecated API なし

## Phase 3: ドキュメント更新
- [ ] `CLAUDE.md` レイヤー 3 に `notebook/` パッケージを追記
- [ ] `docs/repository-structure.md` パッケージ表に `notebook/` を追加
- [ ] `docs/functional-design.md` Extension Point マップに fileType / fileEditorProvider を追加
- [ ] `README.md` Features セクションに「Notebook-style worksheet」追加
- [ ] `sphinx-docs/user/features/advanced.md` に Notebook セクション
- [ ] 日本語 `.po` 同時更新（`make build-ja` パス確認）
- [ ] `docs/lsp-fallback-matrix.md` に「Notebook (LSP 不要)」行を追加

## Phase 3: コミット
- [ ] Model + Serializer + MarkdownExporter コミット（`✨ Add notebook document model and serializers`）
- [ ] FileType + FileEditor + Panel コミット（`✨ Add notebook-style worksheet editor`）
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
