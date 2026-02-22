# Tasklist: External Annotator (Format Check)

## 実装

- [x] RescriptProjectSettings.kt に formatCheckEnabled 設定を追加
- [x] RescriptConfigurable.kt に設定 UI チェックボックスを追加
- [x] RescriptFormatCheckAnnotator.kt を実装（3フェーズ + QuickFix）
- [x] RescriptFormatCheckAnnotatorTest.kt を作成
- [x] plugin.xml に externalAnnotator を登録

## ドキュメント更新

- [x] CLAUDE.md — レイヤー 3 にフォーマットチェック機能を追記
- [x] README.md — Features セクションに追記
- [x] sphinx-docs/user/features/code-analysis.md — 機能説明を追記
- [x] docs/product-requirements.md — #48 を実装済みセクションに移動

## コミット前検証

- [x] KDoc コメント確認
- [x] テスト存在確認
- [x] ドキュメント同期確認
- [x] plugin.xml 登録確認
- [x] tasklist.md 進捗確認

## ビルド・マージ

- [x] ./gradlew clean buildPlugin 成功確認
- [x] ./gradlew test 成功確認
- [x] コミット（機能単位）
- [x] tasklist 全タスク完了確認
- [x] main にマージ
