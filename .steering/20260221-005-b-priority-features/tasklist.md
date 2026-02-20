# タスクリスト: B 優先度機能一括実装 (21 件)

## Phase 1: 低難易度 ★ (パーサー変更不要) — 10 件

### 1.1 基盤・ユーティリティ系

- [ ] #33 `RescriptPredefinedCodeStyle.kt` 実装
- [ ] #34 `RescriptElementDescriptionProvider.kt` 実装
- [ ] #40 `RescriptReaderModeMatcher.kt` 実装
- [ ] #29 `RescriptLookupCharFilter.kt` 実装
- [ ] #28 `RescriptInspectionSuppressor.kt` 実装

### 1.2 エディタ・ビジュアル系

- [ ] #24 `RescriptBackspaceHandler.kt` 実装
- [ ] #41 `RescriptColorProvider.kt` 実装
- [ ] #39 `RescriptVcsCodeVisionContext.kt` 実装
- [ ] #31 `RescriptProjectViewNodeDecorator.kt` 実装

### 1.3 テスト (Phase 1)

- [ ] Phase 1 の 9 機能のテスト作成

### 1.4 ビルド確認 (Phase 1)

- [ ] `./gradlew buildPlugin` 成功確認

## Phase 2: 中難易度 ★ (パーサー変更不要) — 5 件

- [ ] #27 `RescriptCopyPastePreProcessor.kt` 実装
- [ ] #32 `RescriptOpenStatementIndex.kt` 実装
- [ ] #37 `RescriptPasteAsJsxProcessor.kt` 実装
- [ ] #38 `RescriptDependenciesToolWindowFactory.kt` + `RescriptDependenciesPanel.kt` 実装
- [ ] #42 `RescriptAutoImportOptionsProvider.kt` 実装 + `RescriptProjectSettings.kt` 更新

### 2.1 テスト (Phase 2)

- [ ] Phase 2 の 5 機能のテスト作成 (#38, #42 は UI のためテスト省略)

### 2.2 ビルド確認 (Phase 2)

- [ ] `./gradlew buildPlugin` 成功確認

## Phase 3: トークンレベル工夫 ▲ — 4 件

- [ ] #22 `RescriptMoveElementHandler.kt` 実装
- [ ] #23 `RescriptUsageTypeProvider.kt` 実装
- [ ] #25 `RescriptCodeBlockHandler.kt` 実装
- [ ] #26 `RescriptListSplitJoinContext.kt` 実装

### 3.1 テスト (Phase 3)

- [ ] Phase 3 の 4 機能のテスト作成

### 3.2 ビルド確認 (Phase 3)

- [ ] `./gradlew buildPlugin` 成功確認

## Phase 4: LSP/パーサー依存 ● — 3 件

- [ ] #30 `RescriptDocumentationProvider.kt` 実装
- [ ] #35 `RescriptSafeDeleteProcessor.kt` 実装
- [ ] #36 `RescriptNameSuggestionProvider.kt` 実装

### 4.1 テスト (Phase 4)

- [ ] Phase 4 の 3 機能のテスト作成 (#30, #35 はフォールバック部分のみ)

### 4.2 ビルド確認 (Phase 4)

- [ ] `./gradlew buildPlugin` 成功確認

## Phase 5: plugin.xml 登録 & 統合

- [ ] `plugin.xml` に 21 件の Extension Point を登録
- [ ] `./gradlew buildPlugin` 最終確認
- [ ] `./gradlew test` 全テスト成功確認

## Phase 6: ドキュメント更新

- [ ] `CLAUDE.md` 更新 (プロジェクト構成図に新規ファイル追加)
- [ ] `README.md` 更新 (機能一覧に 21 件追加)
- [ ] `docs/product-requirements.md` 更新 (実装済み機能テーブルに追加)
- [ ] `docs/functional-design.md` 更新 (Extension Point 登録マップ + 機能対比表に追加)
- [ ] `sphinx-docs/` 更新 (該当ページの英語ソース + 日本語翻訳)

## Phase 7: コミット & マージ

- [ ] コミット (`✨ Add 21 B-priority features`)
- [ ] `main` にマージして worktree を削除
