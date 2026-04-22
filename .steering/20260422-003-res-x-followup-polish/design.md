# design.md — res-x テンプレート フォローアップ仕上げ

## 設計方針

- **項目ごとに 1 コミット**に分割し、可読性と bisect 容易性を最優先
- **res-x と共通ヘルパーにしか触らない**: 他 17 テンプレへの副作用を出さない
- **英語カスタムエラーメッセージを維持**: zod/sury のデフォルト英文に置き換えると UX が後退するため `~message="..."` で既存文言を渡す
- **ドキュメントのみの拡張は README の extraSections に追加**: `CommonFiles.readme(..., extraSections)` の既存口を使い、新ヘルパーは不要
- **Dockerfile と persistence は静的リソースとして配置**し、`TemplateResourceLoader.load` で取り込む
- `TemplateResourcesSmokeTest` の `{{` 残留検出は `TemplateContext` のフル組み合わせ（PNPM × ZOD / PNPM × SURY / FULL_STACK は REST/GRAPHQL）をカバー

## コンポーネント設計

### (#17) プレースホルダ残留検出

`TemplateResourcesSmokeTest.kt` に新テストを追加:

```kotlin
@Test
fun `no unresolved handlebars-style placeholders survive in generated files`() {
    val contexts = listOf(
        TemplateContext("demo-zod", PackageManager.PNPM, ValidationLibrary.ZOD),
        TemplateContext("demo-sury", PackageManager.PNPM, ValidationLibrary.SURY),
        TemplateContext("demo-graphql", PackageManager.PNPM, ValidationLibrary.ZOD, ApiStrategy.GRAPHQL),
    )
    val residuePattern = Regex("""\{\{[^}]+\}\}""")
    ProjectTemplate.entries.forEach { template ->
        contexts.forEach { ctx ->
            val files = template.generateFiles(ctx)
            files.forEach { (path, content) ->
                val matches = residuePattern.findAll(content).toList()
                assertTrue(
                    matches.isEmpty(),
                    "$template/$path contains unresolved placeholders: ${matches.joinToString { it.value }}",
                )
            }
        }
    }
}
```

Regex `\{\{[^}]+\}\}` は `{{name}}` `{{htmxVersion}}` 形式を検出。`}}` を含まない文字列をキャプチャ対象にすることで、`}}` を含む正当なコード（`Js.Obj.empty()` 等）との誤検出を避ける。

### (#12) `bun build --compile`

`ResXTemplateFiles.kt` の scripts に 1 行追加:

```kotlin
"compile" to "bun build --compile src/App.res.mjs --outfile dist/app",
```

README scripts 表にも:

```kotlin
"compile" to "Compile the Bun server into a standalone binary",
```

順序は `build` の後ろ、`test` の前に配置（プロダクション寄りのタスクは先に見せる）。

### (#10) zod schema-driven validation

```rescript
// Runtime validation for Todo form input using zod v4. The schema owns the
// length and format rules, so `parseTodoInput` only decides whether the
// description collapses to `None` when the user left it blank.

type zSchema

@module("zod")
external z: {
  "string": unit => zSchema,
  "object": Dict.t<zSchema> => zSchema,
} = "z"

@send external trim: zSchema => zSchema = "trim"
@send external min: (zSchema, int, string) => zSchema = "min"
@send external max: (zSchema, int, string) => zSchema = "max"

type zIssue = {
  path: array<string>,
  message: string,
}

type zError = {issues: array<zIssue>}

type rawInput = {
  name: string,
  description: string,
}

type safeParseResult = {
  success: bool,
  data?: rawInput,
  error?: zError,
}

@send external safeParse: (zSchema, 'input) => safeParseResult = "safeParse"

type todoInput = {
  name: string,
  description: option<string>,
}

let todoInputSchema = z["object"](
  Dict.fromArray([
    (
      "name",
      z["string"]()
      ->trim
      ->min(1, "Name must not be empty")
      ->max(80, "Name must be 80 characters or fewer"),
    ),
    (
      "description",
      z["string"]()
      ->trim
      ->max(240, "Description must be 240 characters or fewer"),
    ),
  ]),
)

let parseTodoInput = (~name: string, ~description: string): result<todoInput, string> => {
  let result = todoInputSchema->safeParse({"name": name, "description": description})
  switch (result.data, result.error) {
  | (Some(data), _) =>
    Ok({
      name: data.name,
      description: data.description === "" ? None : Some(data.description),
    })
  | (None, Some(err)) =>
    err.issues
    ->Array.get(0)
    ->Option.map(issue => issue.message)
    ->Option.getOr("Validation failed")
    ->Error
  | (None, None) => Error("Validation failed")
  }
}
```

**注**: zod v4 の `.min(n, msg)` は positional 2 引数。`~message=` は使わず位置引数で渡す。`trim` は動作するが `safeParse` の結果 `data.name` には trimmed 文字列が入る（zod が副作用的に再発行）。

### (#11) sury schema-driven validation

```rescript
// Runtime validation for Todo form input using sury (rescript-struct). The
// schema owns the length and format rules; `parseTodoInput` only decides
// whether the description collapses to `None` when the user left it blank.

type rawInput = {
  name: string,
  description: string,
}

let rawInputSchema: S.t<rawInput> = S.object(s => {
  name: s.field(
    "name",
    S.string
    ->S.trim
    ->S.min(1, ~message="Name must not be empty")
    ->S.max(80, ~message="Name must be 80 characters or fewer"),
  ),
  description: s.field(
    "description",
    S.string
    ->S.trim
    ->S.max(240, ~message="Description must be 240 characters or fewer"),
  ),
})

type todoInput = {
  name: string,
  description: option<string>,
}

let parseTodoInput = (~name: string, ~description: string): result<todoInput, string> => {
  let payload = {"name": name, "description": description}
  try {
    let data: rawInput = payload->Obj.magic->S.parseOrThrow(rawInputSchema)
    Ok({
      name: data.name,
      description: data.description === "" ? None : Some(data.description),
    })
  } catch {
  | S.Error(err) => Error(err.message)
  }
}
```

手書き if/else が消えて、schema が主力になる。`Obj.magic` は `{"name":_, "description":_}` を `JSON.t` へ流し込むための最小 coercion として残す。

### (#13) Dockerfile + Deploy README

`templates/res-x/Dockerfile` を新規作成:

```dockerfile
# syntax=docker/dockerfile:1

FROM oven/bun:1 AS deps
WORKDIR /app
COPY package.json pnpm-lock.yaml* bun.lockb* ./
RUN bun install --frozen-lockfile --production || bun install

FROM oven/bun:1 AS builder
WORKDIR /app
COPY --from=deps /app/node_modules ./node_modules
COPY . .
RUN bunx rescript && bun run build

FROM oven/bun:1-slim AS runtime
WORKDIR /app
ENV NODE_ENV=production
COPY --from=builder /app/node_modules ./node_modules
COPY --from=builder /app/src ./src
COPY --from=builder /app/dist ./dist
COPY package.json rescript.json ./
EXPOSE 4444
CMD ["bun", "run", "src/App.res.mjs"]
```

`templates/res-x/readme/deploy.md` を新規作成（Docker Hub / Fly.io / Render 3 行のショートガイド）。

`ResXTemplateFiles.kt` で `Dockerfile` を `linkedMapOf` に追加し、`extraSections` に `"Deploy"` を挟む。

### (#14) SQLite persistence doc

`templates/res-x/readme/persistence.md` を新規作成（Bun.SQLite + マイグレーションの Quick Sketch）。

`ResXTemplateFiles.kt` の `extraSections` に `"Persistence"` を挟む。**コード変更なし** — あくまで day-two ガイド。

## 既存 API 再利用

| 機能 | 参照先 |
|---|---|
| README extra sections | `CommonFiles.readme(..., extraSections)` (CommonFiles.kt:80) |
| 静的リソースロード | `TemplateResourceLoader.load` |
| Template スモークテスト | `TemplateResourcesSmokeTest.kt` |
| sury 型チェーン | sury v10 API: `S.string`, `S.trim`, `S.min`, `S.max` |
| zod v4 | `z.string().trim().min().max()` + `safeParse` |

## テスト設計

- **ResXTemplateFilesTest.kt** の更新点:
  - `package json declares compile script for standalone binary` (item 12)
  - `README documents the compile, deploy, and persistence sections` (items 12/13/14)
  - `Dockerfile uses oven-sh bun base image` (item 13)
  - `zod Validation moves length rules into the schema chain` (item 10): `z[\"string\"]()` の後に `->trim`, `->min(`, `->max(` が続くことを確認、`if trimmedName` 等の手書きチェックが残っていないことを assertFalse
  - `sury Validation moves length rules into the schema chain` (item 11): 同様に `S.trim`, `S.min`, `S.max` の存在と if/else 消滅を確認
  - `sury variant constructs payload via object literal` (既存テストは維持)
- **TemplateResourcesSmokeTest.kt** (item 17): 新テスト 1 件を上記設計のとおり追加

## 既知の制約とリスク

- **zod v4 binding の記述量**: 既存の単純な external を超えて、型引数なしの `zSchema` + `safeParseResult` 型を定義する必要がある。バインディング追加は 20 行程度
- **Dockerfile の `dist/` 依存**: `build` タスクが `vite build` を前提とし、`dist/` に client assets を出力する想定。res-x は現状 `clientDirs: []` なので `vite build` が空ビルドになるが、将来 client assets を置いても壊れないレイアウトを維持する
- **persistence.md はガイドのみ**: 実装は行わない。ユーザーが自分で `Bun.SQLite` を import するサンプルコードを README に掲載するに留める
- **zod の trim 挙動**: zod v4 の `.trim()` は `safeParse` 時に入力を mutate するため、返される `data` は trim 後の値。テストでは trim 前後の同値性を仮定しない

## 実装順序

1. ステアリング書類作成（本書）
2. `feature/res-x-followup-polish` ブランチ作成
3. #17 → commit
4. #12 → commit
5. #10 → commit
6. #11 → commit
7. #13 → commit
8. #14 → commit
9. DoD Phase 3 検証
10. ステアリング tasklist 完了更新 + ステアリング commit
11. ユーザー承認 → main merge → ブランチ削除
