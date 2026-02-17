# Tasklist: JSON Schema for rescript.json

## 実装タスク

- [x] `src/main/resources/schemas/rescript.schema.json` を作成（公式スキーマをバンドル）
- [x] `src/main/kotlin/com/rescript/plugin/config/RescriptJsonSchemaProviderFactory.kt` を作成
- [x] `src/main/resources/META-INF/rescript-json.xml` を作成（jsonSchemaProviderFactory 登録）
- [x] `src/main/resources/META-INF/plugin.xml` に optional dependency を追加
- [x] `build.gradle.kts` に JSON モジュールの bundledModule 依存を追加
- [x] ビルド確認（`./gradlew buildPlugin`）
- [x] ドキュメント更新（CLAUDE.md, product-requirements.md, functional-design.md）
- [x] コミット: `✨ Add JSON Schema for rescript.json`

## テスト省略理由

JSON Schema の統合テストは IDE フレームワーク全体の起動と JSON プラグインの読み込みが必要で、単体テストが困難なため省略する。
