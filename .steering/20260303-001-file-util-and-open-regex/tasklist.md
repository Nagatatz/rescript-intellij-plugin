# タスクリスト: ファイルユーティリティ統一 + open Regex 統一リファクタリング

## Phase 1: 準備

- [x] `feature/file-util-and-open-regex` ブランチを作成

## Phase 2: RescriptFileUtil の新設と適用

- [x] `RescriptFileUtil.kt` を `util/` に新設
- [x] `RescriptFileUtilTest.kt` を作成
- [x] 23 ファイルの重複を `RescriptFileUtil` に置換（計画の20 + 追加発見4ファイル）
- [x] ビルド確認 + テスト実行
- [x] コミット: `♻️ Extract file extension checks and counterpart lookup into RescriptFileUtil`

## Phase 3: open Regex パターン統一

- [ ] `RescriptRegexPatterns.kt` に 4 パターン追加
- [ ] `RescriptRegexPatternsTest.kt` に 4 パターン分のテスト追加
- [ ] 5 ファイルの重複パターンを置換
- [ ] ビルド確認 + テスト実行
- [ ] コミット: `♻️ Extract open statement regex patterns to RescriptRegexPatterns`

## Phase 4: ドキュメント更新

- [ ] CLAUDE.md 更新
- [ ] コミット: `📝 Update docs for file util and regex pattern refactoring`

## Phase 5: 完了

- [ ] `./gradlew clean buildPlugin` 成功確認
- [ ] 全テストパス確認
- [ ] tasklist.md 全タスク `[x]` 確認
- [ ] ユーザーにマージ確認
- [ ] main にマージ + ブランチ削除
