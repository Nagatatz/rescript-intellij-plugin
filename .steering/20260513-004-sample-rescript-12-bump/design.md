# 設計: サンプル package.json のバージョンバンプ

`manual-test-projects/main/package.json` の `devDependencies` を以下に書き換えるだけ。

```diff
   "devDependencies": {
-    "rescript": "^11.1.4",
-    "@rescript/language-server": "^1.62.0"
+    "rescript": "^12.2.0",
+    "@rescript/language-server": "^1.72.0"
   }
```

`rescript.json` には変更不要（フィールドはバージョン非依存）。

## なぜ 12.2.0 / 1.72.0 か

- `TemplateVersions.RESCRIPT = "^12.2.0"`（プロジェクト雛形生成器の現行値）
- UI テストフィクスチャ (`src/uiTest/testData/sample-project/package.json`) が `@rescript/language-server: ^1.72.0`

サンプル fixture もこれらに合わせることで、リポジトリ内のすべての「サンプルプロジェクト」が ReScript 12 系で一貫する。

## テスト

コード変更なし。`./gradlew clean buildPlugin test` でリグレッションがないことだけ確認。
