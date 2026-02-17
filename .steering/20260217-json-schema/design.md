# Design: JSON Schema for rescript.json

## 実装アプローチ

IntelliJ Platform の `JsonSchemaProviderFactory` 拡張ポイントを使用して、`rescript.json` / `bsconfig.json` にバンドルされた JSON Schema を自動適用する。

## 新規ファイル

### 1. `src/main/kotlin/com/rescript/plugin/config/RescriptJsonSchemaProviderFactory.kt`

- `JsonSchemaProviderFactory` を実装
- `getProviders(project)` で `RescriptJsonSchemaFileProvider` のリストを返す
- `RescriptJsonSchemaFileProvider` は `JsonSchemaFileProvider` を実装:
  - `isAvailable(file)`: ファイル名が `rescript.json` または `bsconfig.json` の場合に `true`
  - `getSchemaFile()`: バンドルされた `rescript.schema.json` を返す
  - `getName()`: `"ReScript"` を返す

### 2. `src/main/resources/schemas/rescript.schema.json`

- rescript-compiler 公式リポジトリの `build-schema.json` をベースに作成
- URL: `https://raw.githubusercontent.com/rescript-lang/rescript-compiler/master/docs/docson/build-schema.json`

### 3. `src/main/resources/META-INF/rescript-json.xml`

- JSON プラグイン依存の extension point を分離登録
- `jsonSchemaProviderFactory` を登録

## 変更ファイル

### `src/main/resources/META-INF/plugin.xml`

- JSON プラグインへの optional dependency を追加:
  ```xml
  <depends optional="true" config-file="rescript-json.xml">com.intellij.modules.json</depends>
  ```

## 影響範囲

- JSON プラグインが有効な環境でのみ JSON Schema 機能が動作
- JSON プラグインが無効な環境では影響なし（optional dependency）
- 既存の `RescriptJsonIconProvider` には影響なし
