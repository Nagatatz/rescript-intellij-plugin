# Tasklist: ボイラープレート リファクタリング

## 1. Intention Action 基底クラス

- [x] `RescriptBaseIntention.kt` を `intention/` に作成
- [x] 20 intention ファイルを `RescriptBaseIntention` に移行
- [x] テスト作成: `RescriptBaseIntentionTest.kt`
- [x] 既存テストがパスすることを確認
- [x] コミット: `♻️ Extract RescriptBaseIntention base class`

## 2. Generate Action 基底クラス

- [x] `RescriptBaseGenerateAction.kt` を `generate/` に作成
- [x] 6 generate action ファイルを `RescriptBaseGenerateAction` に移行
- [x] テスト作成: `RescriptBaseGenerateActionTest.kt`
- [x] 既存テストがパスすることを確認
- [x] コミット: `♻️ Extract RescriptBaseGenerateAction base class`

## 3. WriteCommandAction 拡張関数

- [x] `RescriptEditorUtils.kt` を `util/` に作成
- [x] 単純パターンの呼び出し元を拡張関数に置換（10ファイル）
- [x] テスト作成: `RescriptEditorUtilsTest.kt`
- [x] 既存テストがパスすることを確認
- [x] コミット: `♻️ Extract WriteCommandAction utility functions`

## 4. Document 行取得ユーティリティ

- [x] `RescriptEditorUtils.kt` に行取得拡張関数を追加 (`getLineTextAt`, `getLineRangeAt`)
- [x] 呼び出し元を拡張関数に置換（6ファイル、10箇所）
- [x] テスト更新
- [x] コミット: Task 3 にまとめてコミット済み

## 5. 検証・マージ

- [x] `./gradlew ktlintCheck` パス
- [x] `./gradlew clean buildPlugin` 成功
- [x] `./gradlew test` パス
- [x] ドキュメント更新（CLAUDE.md, docs/repository-structure.md, docs/product-requirements.md）
  - README.md / sphinx-docs: 内部リファクタリングのためユーザー向け変更なし
- [x] コミット: `📝 Update docs for boilerplate refactoring`
- [x] main にマージ
