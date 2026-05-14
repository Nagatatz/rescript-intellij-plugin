# Tasklist: Deprecated API Migration

## Section 1: CompletionConfidence 移行

- [x] `RescriptCompletionConfidence.kt` の override を 4-arg シグネチャ (`Editor` first) に変更し、`@Suppress("OVERRIDE_DEPRECATION")` を除去
- [x] `RescriptCompletionConfidenceTest.kt` は `shouldSkipAutopopup` を直接呼ばないため変更不要
- [x] `./gradlew test` がパス

## Section 2: FloatingToolbarProvider — suppression 維持

- [x] 2026.1.1 では `isApplicableAsync` が未追加であることを javap で確認 (`intellij.platform.ide.impl.jar`)
- [x] `@Suppress("DEPRECATION")` と override は維持。コメントを「2026.1.1 にまだ存在しない」と明確化
- [x] `plugin-verifier-ignored-problems.txt` のエントリを残し、`Reviewed` 日付と説明を更新

## Section 3: FileIncludeProvider — suppression 追加

- [x] 2026.1.1 では `acceptFile(VirtualFile)` が abstract、`acceptFile(IndexedFile)` 未存在であることを javap で確認 (`intellij.platform.lang.jar`)
- [x] 本体コードは無変更
- [x] `plugin-verifier-ignored-problems.txt` に FileIncludeProvider 用エントリを新規追加

## Section 4: MarkedString 参照除去

- [x] `RescriptLspUtils.kt#getHoverType` の Either.right 経路を撤去 (`takeIf { it.isLeft }?.left` で plain-string のみハンドリング)
- [x] 既存テストが回帰なくパス
- [x] `./gradlew test` がパス

## Section 5: 抑制エントリ整理 + 全体検証

- [x] `plugin-verifier-ignored-problems.txt` から CompletionConfidence と MarkedString のエントリを削除
- [x] 見出しコメントを更新し `Reviewed: 2026-05-14` / `Expires: 2027-05-14` に
- [x] `./gradlew ktlintCheck` がパス
- [x] `./gradlew clean buildPlugin` がパス
- [x] `./gradlew test` がパス
- [ ] tasklist.md / steering の `[x]` を更新してマージ前コミット
- [ ] ユーザーに main マージ可否を確認
- [ ] main にマージ後 worktree クリーンアップ
