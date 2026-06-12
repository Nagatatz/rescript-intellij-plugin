# タスクリスト: ツールウィンドウの EDT スレッディング修正

## セクション 1: DEFECT 1 — Module Dependency Diagram (🐛 1 コミット)

- [x] `RescriptDependencyDiagramProvider.buildDiagram` の index/PSI 走査を
      `runReadAction` でラップ、KDoc に off-EDT 契約を追記
- [x] `@Suppress("unused")` を削除（パネルから使用されているため）
- [x] `RescriptDependencyDiagramPanel.doRefresh` を
      `executeOnPooledThread { build } + invokeLater { UI }` に変更
- [x] 既存 `RescriptDependencyDiagramProviderTest` が緑であることを確認
- [x] `./gradlew ktlintCheck buildPlugin test` 緑
- [x] コミット 🐛

### テスト省略理由（セクション 1）

- `RescriptDependencyDiagramPanel`: Swing UI ToolWindowPanel（testing.md
  免除対象）。スレッディング修正は ThreadingAssertions のプラットフォーム
  挙動で light fixture 再現不能 → スモークテスト再実行で担保。
- `buildDiagram` のロジックは不変（read-action ラップのみ）。既存
  provider テストでカバー済み。

## セクション 2: DEFECT 2 — Type Impact (🐛 1 コミット)

- [x] `RescriptTypeImpactPanel.doRefresh` を EDT 前段（caret 捕捉）+
      pooled thread（target 解決 + findReferences）+ invokeLater（UI 更新）
      に分割
- [x] `refreshGeneration` による stale 結果破棄ガードを追加
- [x] `./gradlew ktlintCheck buildPlugin test` 緑
- [x] コミット 🐛

### テスト省略理由（セクション 2）

- `RescriptTypeImpactPanel`: Swing UI ToolWindowPanel（testing.md 免除
  対象）。SlowOperations のプラットフォーム挙動で light fixture 再現不能
  → スモークテスト再実行で担保。
- `RescriptTypeReferenceFinder` は無変更（既に off-EDT 契約 + 内部
  read-action を持つ）。

## セクション 3: 仕上げ

- [ ] スモークテスト再実行（runIde → 両ツールウィンドウ起動 →
      idea.log に ThreadingAssertions / SlowOperations SEVERE が出ない）
- [ ] tasklist 全項目 `[x]` 化（マージ前最終コミット）
- [ ] マージ可否を AskUserQuestion で確認
- [ ] main へマージ、worktree クリーンアップ

## ドキュメント

機能仕様・UI 不変のためユーザー向けドキュメント更新は不要
（design.md「ドキュメント影響」参照）。
