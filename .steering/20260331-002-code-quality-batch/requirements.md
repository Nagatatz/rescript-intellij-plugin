# 要求: コード品質改善バッチ

## 目的

6項目の品質改善を実施し、コードのモダン化・テスト品質・ビルド設定・ドキュメントを向上させる。

## 対象

1. Kotlin モダナイゼーション — `data object` 変換 + `service<T>()` 移行
2. バージョンカタログ導入 — `libs.versions.toml` による依存関係一元管理
3. @Suppress DEPRECATION 見直し — 不要な抑制の削除/理由コメント追加
4. パラメータ化テスト導入 — `RescriptLexerTest` の反復テスト統合
5. Kover 除外リスト精緻化 — テスト済みパッケージの除外粒度変更
6. CONTRIBUTING.md 拡充 — 前提条件・CI コマンド・品質チェック情報追加

## 受け入れ条件

- [ ] 全6項目が実装されている
- [ ] `./gradlew ktlintCheck clean buildPlugin test koverHtmlReport` が成功する
- [ ] 既存テストに影響がない
