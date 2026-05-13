# タスクリスト: Panel navigation

## セクション A: MigrationPanel double-click

- [x] `RescriptMigrationPanel.kt` のリストマウスリスナーにダブルクリック分岐を追加
- [x] `OpenFileDescriptor(project, candidate.file).navigate(true)` を呼ぶ
- [x] `./gradlew ktlintCheck` グリーン

## セクション B: VariantFlowPanel Jump to switch

- [x] `RescriptVariantFlowPanel.kt` に `JumpTarget` を追加し、`refresh()` で代入 / `renderEmpty()` でクリア
- [x] `JumpToSwitchAction` toolbar action を追加
- [x] `./gradlew ktlintCheck` グリーン

## セクション C: DependenciesPanel double-click

- [x] `RescriptDependenciesPanel.kt` に `PackageNode` data class と `displayLabelFor` pure helper を追加
- [x] ツリーノードを `userObject = PackageNode(...)` に変更
- [x] tree に MouseListener を追加し、ダブルクリックで `package.json` を開く
- [x] `RescriptDependenciesPackageNodeTest.kt` 新規作成（pure helper のみテスト）
- [x] `./gradlew ktlintCheck` グリーン

## セクション D: 検証 + コミット

- [x] `./gradlew clean buildPlugin && ./gradlew test --rerun-tasks` グリーン
- [x] `✨ Add editor navigation to Migration / Variant Flow / Dependencies panels` でコミット

## セクション E: マージ

- [x] tasklist の全項目を `[x]` に更新（このコミットに含める）
- [x] `main` にマージ、worktree クリーンアップ
