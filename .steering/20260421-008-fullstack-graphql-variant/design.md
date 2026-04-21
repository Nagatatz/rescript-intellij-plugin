# 設計: FULL_STACK REST/GraphQL バリアント

## アーキテクチャ

```
ApiStrategy (REST | GRAPHQL)
    ↕ (Wizard UI)
RescriptModuleBuilder.apiStrategy
    ↓
TemplateContext.apiStrategy
    ↓
FullStackTemplateFiles.generate(ctx)
    ├── REST   → 既存 Routes.res / Server.res / ApiClient.res / App.res
    └── GRAPHQL → Yoga.res (shared) / GraphqlSchema / Resolvers / schema.graphql
                  / RelayEnvironment / UsersListQuery / relay.config.js
```

## 変更対象

### 新規 (Kotlin)
- `src/main/kotlin/com/rescript/plugin/wizard/ApiStrategy.kt`
- `src/test/kotlin/com/rescript/plugin/wizard/ApiStrategyTest.kt`

### 新規 (リソース)
- `src/main/resources/templates/common/graphql/Yoga.res` (lifted from hono-graphql)
- `src/main/resources/templates/full-stack/api/graphql/src/server/Server.res` (yoga mount 版)
- `src/main/resources/templates/full-stack/api/graphql/src/server/GraphqlSchema.res`
- `src/main/resources/templates/full-stack/api/graphql/src/server/Resolvers.res`
- `src/main/resources/templates/full-stack/api/graphql/src/server/schema.graphql`
- `src/main/resources/templates/full-stack/api/graphql/src/client/RelayEnvironment.res`
- `src/main/resources/templates/full-stack/api/graphql/src/client/ClientMain.res`
- `src/main/resources/templates/full-stack/api/graphql/src/client/App.res`
- `src/main/resources/templates/full-stack/api/graphql/src/client/UsersListQuery.res`
- `src/main/resources/templates/full-stack/api/graphql/relay.config.js`
- `src/main/resources/templates/full-stack/api/graphql/readme/architecture.md`
- `src/main/resources/templates/full-stack/api/graphql/readme/graphql.md`

### 変更 (Kotlin)
- `src/main/kotlin/com/rescript/plugin/wizard/templates/TemplateContext.kt` — `apiStrategy` フィールド追加
- `src/main/kotlin/com/rescript/plugin/wizard/RescriptModuleBuilder.kt` — `apiStrategy` var + TemplateContext に渡す
- `src/main/kotlin/com/rescript/plugin/wizard/RescriptProjectWizardStep.kt` — ApiStrategy ComboBox 追加
- `src/main/kotlin/com/rescript/plugin/wizard/templates/FullStackTemplateFiles.kt` — REST / GRAPHQL 分岐
- `src/main/kotlin/com/rescript/plugin/wizard/templates/HonoGraphqlTemplateFiles.kt` — Yoga.res を共有パスから読む
- `src/main/kotlin/com/rescript/plugin/wizard/templates/TemplateVersions.kt` — `RESCRIPT_RELAY` / `RELAY_COMPILER` 定数追加

### 変更 (リソース)
- `src/main/resources/templates/full-stack/readme/architecture.md` — GraphQL バリアント案内 1 行追加
- `src/main/resources/templates/common/readme/extending-bindings.md` — `rescript-relay` 言及追記

### 変更 (テスト)
- `src/test/kotlin/com/rescript/plugin/wizard/templates/FullStackTemplateFilesTest.kt` — 4-combo テスト追加
- `src/test/kotlin/com/rescript/plugin/wizard/ProjectTemplateTest.kt` — 共有 Yoga.res 一致性アサーション追加

### 削除
- `src/main/resources/templates/hono-graphql/src/Yoga.res`

## バージョン

`TemplateVersions.kt` に追加:
```kotlin
const val RESCRIPT_RELAY = "^4.1.0"    // ReScript 12 対応の最新安定
const val RELAY_COMPILER = "^19.0.0"   // rescript-relay 4.x と対応
```

(実際の値は新規追加時に npm 最新を確認)

## package.json の差分 (GRAPHQL バリアント)

REST (現行) に対して GraphQL バリアントで追加:
- dependencies: `graphql`, `graphql-yoga`, `rescript-relay`
- devDependencies: `relay-compiler`
- scripts:
  - `"relay"`: `"relay-compiler"`
  - `"relay:watch"`: `"relay-compiler --watch"`
  - `"dev"`: concurrently に `relay-compiler --watch` を追加

## rescript.json の差分 (GRAPHQL バリアント)

REST 側:
```json
{
  "bs-dependencies": ["@rescript/core", "@rescript/react"]
}
```

GraphQL 側:
```json
{
  "bs-dependencies": ["@rescript/core", "@rescript/react", "rescript-relay"],
  "ppx-flags": ["rescript-relay/ppx"]
}
```

これをサポートするため、`ProjectFileBuilders.rescriptJson()` を拡張するか、FullStackTemplateFiles 内で文字列合成する。シンプルに後者を選ぶ。

## 生成テンプレートの dev フロー (GRAPHQL)

```bash
pnpm install
pnpm relay         # Relay compiler で __generated__ に型生成
pnpm dev           # concurrently:
                   #   rescript -w
                   #   relay-compiler --watch
                   #   server (node --watch)
                   #   client (vp dev)
```

## リスク

- **rescript-relay 4.x と ReScript 12.2.0 の互換性** — ChangeLog 上は ReScript 12 サポート済みだが、マイナーバージョン差分で互換問題が出る可能性。README にリリースノート URL を記載
- **Relay compiler の codegen ステップ** — 利用者が `pnpm relay` を忘れると `%relay()` タグが展開できずビルド失敗。README で明示
- **Schema の同期** — `relay.config.js` が `src/server/schema.graphql` を参照。SDL 編集後に `relay-compiler` 再実行が必要。`graphql.md` に記載
- **手動動作検証責任** — 生成された GraphQL プロジェクトが実際に動作するかはプラグイン側テストでは検証不能。コミットメッセージと README で明記し、利用者に `runIde` からの生成→動作確認を委ねる

## ドキュメント影響

- `CLAUDE.md` / `README.md` / `docs/` / `sphinx-docs/` — 変更不要
- `product-requirements.md` — ロードマップ記載は任意 (ユーザーに確認)
