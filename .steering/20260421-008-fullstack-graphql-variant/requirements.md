# 要求内容: FULL_STACK に REST/GraphQL バリアント追加 (Phase 1)

## 背景

Wizard には既に `ValidationLibrary` (zod/sury) バリアント選択があるが、REST と GraphQL を切り替える方法は無い。GraphQL フロントエンド + バックエンドを一つのテンプレートで欲しい場合、現状は HONO_GRAPHQL (バックエンドのみ) と VITE_REACT (フロントエンドのみ) を個別に作る必要がある。

また、HONO_GRAPHQL は graphql-yoga を使っているため、FULL_STACK の GraphQL バリアントが別のライブラリ (例: Postgraphile) を採用するとユーザーは 2 つの GraphQL スタックを学ぶことになる。この避けたい。

## 目的

1. `ApiStrategy` (REST / GRAPHQL) enum を追加し、Wizard UI で選択可能にする
2. FULL_STACK に GraphQL バリアントを追加 (HONO_GRAPHQL と同じ **graphql-yoga** を採用) し、クライアント側には **rescript-relay** を組み込んで end-to-end 型安全を実現する
3. `Yoga.res` バインディングを `templates/common/graphql/Yoga.res` に統合し、HONO_GRAPHQL と FULL_STACK GraphQL バリアントで共有する
4. SQLite + drizzle は両 API バリアントで共通維持 (PostgreSQL 移行はしない)
5. Validation (zod/sury) は REST / GraphQL 両方で使う (REST: HTTP body 検証、GraphQL: mutation input 検証)

## 受け入れ条件

- [ ] `ApiStrategy` enum が存在し、Wizard UI に API strategy ComboBox が表示される
- [ ] FULL_STACK で ApiStrategy=REST 選択時の出力は現行と同一
- [ ] FULL_STACK で ApiStrategy=GRAPHQL 選択時、以下が含まれる:
  - `src/server/Yoga.res` / `GraphqlSchema.res` / `Resolvers.res` / `schema.graphql` / GraphQL-flavored `Server.res`
  - `src/client/RelayEnvironment.res` / `UsersListQuery.res` / Relay-wired `App.res` / `ClientMain.res`
  - `relay.config.js`
  - `package.json` に `graphql` / `graphql-yoga` / `rescript-relay` / `relay-compiler` 依存追加
  - `rescript.json` に `rescript-relay` bs-dep + `rescript-relay/ppx` ppx-flag
  - `.gitignore` に `src/client/__generated__/` 追加
- [ ] `templates/common/graphql/Yoga.res` が新規作成され、HONO_GRAPHQL と FULL_STACK GraphQL バリアント両方が共有
- [ ] 旧 `templates/hono-graphql/src/Yoga.res` は削除
- [ ] 4 コンビネーション (REST+zod / REST+sury / GRAPHQL+zod / GRAPHQL+sury) がすべて生成可能
- [ ] `ApiStrategyTest` + `FullStackTemplateFilesTest` の新テストが通る
- [ ] `./gradlew ktlintCheck clean buildPlugin test` 全成功

## スコープ外 (Phase 2 以降)

- MONOREPO への ApiStrategy バリアント展開
- Postgraphile / PostgreSQL / docker-compose の導入
- GraphQL subscriptions / fragments の拡張例
- CLAUDE.md / README.md / sphinx-docs への影響 (テンプレート出力変更のみ)
