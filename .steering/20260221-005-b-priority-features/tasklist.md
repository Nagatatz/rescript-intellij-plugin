# タスクリスト: B 優先度機能一括実装 (21 件)

## Phase 1: 低難易度 ★ (パーサー変更不要) — 10 件

### 1.1 基盤・ユーティリティ系

- [x] #33 `RescriptPredefinedCodeStyle.kt` 実装
- [x] #34 `RescriptElementDescriptionProvider.kt` 実装
- [x] #40 `RescriptReaderModeMatcher.kt` 実装
- [x] #29 `RescriptLookupCharFilter.kt` 実装
- [x] #28 `RescriptInspectionSuppressor.kt` 実装

### 1.2 エディタ・ビジュアル系

- [x] #24 `RescriptBackspaceHandler.kt` 実装
- [x] #41 `RescriptColorProvider.kt` 実装
- [x] #39 `RescriptVcsCodeVisionContext.kt` 実装
- [x] #31 `RescriptProjectViewNodeDecorator.kt` 実装

### 1.3 テスト (Phase 1)

- [x] Phase 1 の 9 機能のテスト作成

### 1.4 ビルド確認 (Phase 1)

- [x] `./gradlew buildPlugin` 成功確認

## Phase 2: 中難易度 ★ (パーサー変更不要) — 5 件

- [x] #27 `RescriptCopyPastePreProcessor.kt` 実装
- [x] #32 `RescriptOpenStatementIndex.kt` 実装
- [x] #37 `RescriptPasteAsJsxProcessor.kt` 実装
- [x] #38 `RescriptDependenciesToolWindowFactory.kt` + `RescriptDependenciesPanel.kt` 実装
- [x] #42 `RescriptAutoImportOptionsProvider.kt` 実装 + `RescriptProjectSettings.kt` 更新

### 2.1 テスト (Phase 2)

- [x] Phase 2 の 5 機能のテスト作成 (#38, #42 は UI のためテスト省略)

### 2.2 ビルド確認 (Phase 2)

- [x] `./gradlew buildPlugin` 成功確認

## Phase 3: トークンレベル工夫 ▲ — 4 件

- [x] #22 `RescriptMoveElementHandler.kt` 実装
- [x] #23 `RescriptUsageTypeProvider.kt` 実装
- [x] #25 `RescriptCodeBlockHandler.kt` 実装
- [x] #26 `RescriptListSplitJoinContext.kt` 実装

### 3.1 テスト (Phase 3)

- [x] Phase 3 の 4 機能のテスト作成

### 3.2 ビルド確認 (Phase 3)

- [x] `./gradlew buildPlugin` 成功確認

## Phase 4: LSP/パーサー依存 ● — 3 件

- [x] #30 `RescriptDocumentationProvider.kt` 実装
- [x] #35 `RescriptSafeDeleteProcessor.kt` 実装
- [x] #36 `RescriptNameSuggestionProvider.kt` 実装

### 4.1 テスト (Phase 4)

- [x] Phase 4 の 3 機能のテスト作成 (#30, #35 はフォールバック部分のみ)

### 4.2 ビルド確認 (Phase 4)

- [x] `./gradlew buildPlugin` 成功確認

## Phase 5: plugin.xml 登録 & 統合

- [x] `plugin.xml` に 21 件の Extension Point を登録
- [x] `./gradlew buildPlugin` 最終確認
- [x] `./gradlew test` 新規テスト成功確認 (既存の40件の失敗はpre-existing)

## Phase 6: ドキュメント更新

- [ ] `CLAUDE.md` 更新 (プロジェクト構成図に新規ファイル追加)
- [ ] `README.md` 更新 (機能一覧に 21 件追加)
- [ ] `docs/product-requirements.md` 更新 (実装済み機能テーブルに追加)
- [ ] `docs/functional-design.md` 更新 (Extension Point 登録マップ + 機能対比表に追加)
- [ ] `sphinx-docs/` 更新 (該当ページの英語ソース + 日本語翻訳)

## Phase 7: コミット & マージ

- [ ] ドキュメント更新コミット
- [x] tasklist.md 更新
- [ ] `main` にマージして worktree を削除
