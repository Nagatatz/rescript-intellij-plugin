# 設計: drizzle-orm ヘルパーの共通化と拡充

## 変更対象

### 新規
- `src/main/resources/templates/common/db/Db.res` — 正準 Db.res

### 変更 (Kotlin)
- `src/main/kotlin/com/rescript/plugin/wizard/templates/HonoTemplateFiles.kt` — Db.res 読込パスを `common/db/Db.res` に変更
- `src/main/kotlin/com/rescript/plugin/wizard/templates/HonoGraphqlTemplateFiles.kt` — 同上
- `src/main/kotlin/com/rescript/plugin/wizard/templates/FullStackTemplateFiles.kt` — 同上 (出力先は `src/server/Db.res` のまま)
- `src/main/kotlin/com/rescript/plugin/wizard/templates/MonorepoTemplateFiles.kt` — 同上 (出力先は `packages/server/src/Db.res` のまま)

### 変更 (リソース)
- `src/main/resources/templates/hono-graphql/src/Resolvers.res` — userById / deleteUser を新ヘルパーで実装
- `src/main/resources/templates/common/readme/extending-bindings.md` — drizzle レシピ差し替え + pgtyped/edgedb 案内

### 変更 (テスト)
- `src/test/kotlin/com/rescript/plugin/wizard/ProjectTemplateTest.kt` — 3 テスト追加

### 削除
- `src/main/resources/templates/hono/src/Db.res`
- `src/main/resources/templates/hono-graphql/src/Db.res`
- `src/main/resources/templates/full-stack/src/server/Db.res`
- `src/main/resources/templates/monorepo/packages/server/src/Db.res`

## 共通 Db.res の構造

```rescript
// libsql + Drizzle client. Uses a local SQLite file under ./data/app.db;
// swap the URL for a Turso libsql:// URL to scale to the cloud.
@module("@libsql/client") external createClient: 'opts => 'client = "createClient"
@module("drizzle-orm/libsql") external drizzle: 'client => 'db = "drizzle"

@val external processEnv: Dict.t<string> = "process.env"

let dbUrl = processEnv->Dict.get("DATABASE_URL")->Option.getOr("file:./data/app.db")
let client = createClient({"url": dbUrl})
let db: 'db = drizzle(client)

// --- Query chain constructors ---
@send external select: ('db, 'opts) => 'query = "select"
@send external from: ('q, 'table) => 'q = "from"
@send external insert: ('db, 'table) => 'builder = "insert"
@send external values: ('builder, 'row) => 'builder = "values"
@send external update: ('db, 'table) => 'builder = "update"
@send external set: ('builder, 'patch) => 'builder = "set"
@send external deleteFrom: ('db, 'table) => 'builder = "delete"

// --- Query chain refinements ---
@send external where: ('q, 'expr) => 'q = "where"
@send external orderBy: ('q, 'expr) => 'q = "orderBy"
@send external limit: ('q, int) => 'q = "limit"
@send external offset: ('q, int) => 'q = "offset"
@send external groupBy: ('q, 'expr) => 'q = "groupBy"

// --- Awaitable terminals ---
@send external allAsync: 'q => promise<array<'row>> = "all"
@send external getAsync: 'q => promise<Nullable.t<'row>> = "get"
@send external returning: 'q => promise<array<'row>> = "returning"

// --- Comparison operators (from drizzle-orm) ---
@module("drizzle-orm") external eq: ('col, 'val) => 'expr = "eq"
@module("drizzle-orm") external ne: ('col, 'val) => 'expr = "ne"
@module("drizzle-orm") external gt: ('col, 'val) => 'expr = "gt"
@module("drizzle-orm") external gte: ('col, 'val) => 'expr = "gte"
@module("drizzle-orm") external lt: ('col, 'val) => 'expr = "lt"
@module("drizzle-orm") external lte: ('col, 'val) => 'expr = "lte"
@module("drizzle-orm") external inArray: ('col, array<'val>) => 'expr = "inArray"
@module("drizzle-orm") external notInArray: ('col, array<'val>) => 'expr = "notInArray"
@module("drizzle-orm") external like: ('col, string) => 'expr = "like"
@module("drizzle-orm") external ilike: ('col, string) => 'expr = "ilike"
@module("drizzle-orm") external isNull: 'col => 'expr = "isNull"
@module("drizzle-orm") external isNotNull: 'col => 'expr = "isNotNull"

// --- Boolean combinators (variadic via @variadic) ---
@module("drizzle-orm") @variadic external and: array<'expr> => 'expr = "and"
@module("drizzle-orm") @variadic external or: array<'expr> => 'expr = "or"
@module("drizzle-orm") external not: 'expr => 'expr = "not"

// --- Ordering helpers ---
@module("drizzle-orm") external asc: 'col => 'expr = "asc"
@module("drizzle-orm") external desc: 'col => 'expr = "desc"
```

**設計メモ**:

- `and` / `or` は drizzle-orm では可変長引数で呼び出せるため `@variadic external (...) : array<'expr> => 'expr` の形にする
- Kotlin 側では `TemplateResourceLoader.load("common/db/Db.res")` 1 行で済む。既存の `TemplateResourceLoader` は `templates/$path` を付けるため、パスは `common/db/Db.res` でよい
- 出力先パスは変えない (4 テンプレートの package.json / import パスを壊さない)

## Resolvers.res の書き換え

```rescript
let userById = async (_parent, args, _ctx, _info) => {
  let rows =
    await Db.db
    ->Db.select({
      "id": Schema.users["id"],
      "name": Schema.users["name"],
      "email": Schema.users["email"],
    })
    ->Db.from(Schema.users)
    ->Db.where(Db.eq(Schema.users["id"], args["id"]))
    ->Db.allAsync
  rows->Array.get(0)
}

let deleteUser = async (_parent, args, _ctx, _info) => {
  let deleted =
    await Db.db
    ->Db.deleteFrom(Schema.users)
    ->Db.where(Db.eq(Schema.users["id"], args["id"]))
    ->Db.returning
  deleted->Array.length > 0
}
```

## Extending Bindings の更新

`Pattern: filtering with drizzle-orm` を stock helper 利用例に書き換え、`If you need fuller type safety` サブセクションで以下を列挙:

- `pgtyped-rescript` (Postgres + codegen、zth 氏維持)
- `rescript-edgedb` (EdgeDB/Gel 用、zth 氏維持)

## テスト戦略

```kotlin
@Test
fun `drizzle Db res is shared across all four drizzle-backed templates`() { ... }

@Test
fun `shared Db res exposes the new drizzle helpers`() { ... }

@Test
fun `hono-graphql resolvers use the new helpers and carry no TODO placeholders`() { ... }
```

## リスク

- `@variadic` 属性は drizzle-orm の `and` / `or` を真に可変長で呼ぶために必要。テンプレートが既存でこの属性を使っていないので、生成された `.res` でも問題なくコンパイルするか確認する
- 型パラメータは polymorphic `'expr` / `'col` / `'row` を維持。drizzle-orm 側の TypeScript 型推論は ReScript に流れないので、これ以上の型付けは labor が合わない
- `common/db/Db.res` を jar にパッケージングするのは既存の `common/readme/extending-bindings.md` と同じ仕組み。追加のビルド設定は不要

## ドキュメント影響

- `CLAUDE.md` / `README.md` / `sphinx-docs/` / `docs/` — 変更不要 (テンプレート生成の内部実装)
- `product-requirements.md` — ロードマップ影響なし
