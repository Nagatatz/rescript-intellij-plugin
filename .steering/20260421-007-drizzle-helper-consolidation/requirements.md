# 要求内容: drizzle-orm ヘルパーの共通化と拡充

## 背景

DB を利用する 4 テンプレート (HONO / HONO_GRAPHQL / FULL_STACK / MONOREPO) はそれぞれ独自の `Db.res` を持ち、以下の問題がある:

- 4 ファイルが ~90% 同一だが静かにドリフトしている (hono-graphql のみ `deleteFrom`/`where` を持ち、宣言順も異なる)
- 頻出する CRUD 操作 (`WHERE` / `ORDER BY` / `LIMIT` / `UPDATE` / `DELETE` の多くの演算子) がバインドされていない
- HonoGraphql `Resolvers.res` の `userById` / `deleteUser` は `eq` / `where` / `deleteFrom` が無いため TODO/Placeholder のまま

また、ReScript の型安全 ORM 調査で `pgtyped-rescript` (Postgres + codegen) / `rescript-edgedb` (EdgeDB) が利用者の次の選択肢として浮上したため、Extending Bindings ガイドでも案内する。

## 目的

1. 4 つの `Db.res` を `src/main/resources/templates/common/db/Db.res` 1 本に統合する
2. 共通 `Db.res` に CRUD で頻出する drizzle-orm ヘルパー (eq/ne/gt/... / and/or/not / where/orderBy/limit/offset/groupBy / asc/desc / update/set/deleteFrom / getAsync / inArray) を追加する
3. HonoGraphql `Resolvers.res` の `userById` / `deleteUser` を新ヘルパーを使って実装し TODO/Placeholder を除去する
4. Extending Bindings の drizzle レシピを新ヘルパー前提に書き換え、pgtyped-rescript / rescript-edgedb の案内を追記する

## 受け入れ条件

- [ ] `src/main/resources/templates/common/db/Db.res` が存在し、eq/and/or/inArray/where/orderBy/limit/update/set/deleteFrom/asc/desc を少なくとも含む
- [ ] HONO / HONO_GRAPHQL / FULL_STACK / MONOREPO の 4 テンプレートの生成物 `Db.res` (FULL_STACK は `src/server/Db.res`、MONOREPO は `packages/server/src/Db.res`) は完全一致する
- [ ] `hono-graphql/src/Db.res` 等の 4 ファイルが削除されている
- [ ] HonoGraphql `Resolvers.res` の `userById` / `deleteUser` が `Db.eq` / `Db.where` / `Db.deleteFrom` を使う実装に置き換わっている (TODO/Placeholder コメント消滅)
- [ ] `extending-bindings.md` の drizzle レシピが stock 新ヘルパー利用に書き換わり、pgtyped-rescript / rescript-edgedb の "fuller type safety" サブセクションが追加されている
- [ ] 新規 3 テスト (一致性、ヘルパー存在、resolvers TODO 無) が通る
- [ ] `./gradlew ktlintCheck clean buildPlugin test` 成功

## スコープ外

- drizzle+SQLite からの完全移行 (pgtyped-rescript / rescript-edgedb 採用)
- Wizard への DB strategy 選択肢追加
- `Db.res` の Kotlin ジェネレータ化
