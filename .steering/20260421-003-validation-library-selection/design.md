# Design — Wizard Validation Library Selection (zod / sury)

## 全体

`PackageManager` 選択の実装経路をそのまま踏襲し、新オプション `ValidationLibrary` を並べる。

```
[Wizard Step UI]
   ├─ Package manager: [NPM | PNPM | YARN]
   └─ Validation library: [zod | sury]    ← 追加
       ↓
[RescriptModuleBuilder]
   var packageManager: PackageManager
   var validationLibrary: ValidationLibrary  ← 追加
       ↓
[TemplateContext(projectName, packageManager, validationLibrary)]
       ↓
[<Name>TemplateFiles.generate(ctx)] ← 分岐ロジック
       ↓
[生成ファイル群]
   - package.json: zod または sury 依存
   - src/Schema.res: 選択ライブラリの API
   - 呼び出し側 (Server.res 等): 無変更
```

## コンポーネント

### `ValidationLibrary` enum

```kotlin
enum class ValidationLibrary(val displayName: String, val npmPackage: String) {
    ZOD("zod", "zod"),
    SURY("sury", "sury"),
    ;

    fun variantKey(): String = name.lowercase()  // "zod" | "sury"
}
```

### Wizard UI 追加

`RescriptProjectWizardStep.kt` 既存 ComboBox 配置の直下に 1 行。`JLabel("Validation library:")` + `JComboBox(ValidationLibrary.entries.toTypedArray())`。`updateDataModel()` で `builder.validationLibrary = combo.selectedItem`。

### TemplateContext

```kotlin
data class TemplateContext(
    val projectName: String,
    val packageManager: PackageManager,
    val validationLibrary: ValidationLibrary = ValidationLibrary.ZOD,
)
```

デフォルト引数により既存コール箇所は無改修で動く。

### テンプレート分岐

**依存ライブラリ**:
```kotlin
val validationDep = when (ctx.validationLibrary) {
    ValidationLibrary.ZOD -> "zod" to TemplateVersions.ZOD
    ValidationLibrary.SURY -> "sury" to TemplateVersions.SURY
}
```

**モジュール名**: `Validation.res`（`Schema.res` は drizzle ORM 用に既存 4 テンプレートで使われているため名前衝突を避ける）。

**リソース配置**:
```
src/main/resources/templates/<name>/
├── src/                             # 共通ファイル（drizzle Schema.res, Server.res 等）
└── variants/
    ├── zod/src/Validation.res       # zod 固有
    └── sury/src/Validation.res      # sury 固有
```

`generate()` 内:
```kotlin
"src/Validation.res" to TemplateResourceLoader.load(
    "$RESOURCE_ROOT/variants/${ctx.validationLibrary.variantKey()}/src/Validation.res",
    vars
)
```

## Validation.res 公開 API 統一

どちらのバリアントも以下の形に揃える（例: greet エンドポイント）:

```rescript
// result を返す純関数。呼び出し側で一貫したエラーハンドリングが可能
let parseGreetInput: JSON.t => result<greetInput, string>
```

zod 版（`@module("zod")` バインディング）:
- `@module("zod")` 経由で JS 側の `z.object({ ... })` を ReScript から構築
- `safeParse` の結果を `result` に翻訳

sury 版（実機で v10.0.4 API 確認済み）:
- `S.object(s => { name: s.field("name", S.string) })` でスキーマ定義
- 型は `S.t<greetInput>`
- `S.parseOrThrow` を try/catch し、`S.Error(err)` で `err.message : string` を取得
- `sury` パッケージは `namespace: false` でモジュール `S` を公開するため、ReScript 側では `S.*` 直接呼び出し（`open Sury` 不要）

sury バインディングの最小サンプル:
```rescript
type greetInput = {name: string}

let greetInputSchema: S.t<greetInput> = S.object(s => {
  name: s.field("name", S.string),
})

let parseGreetInput = (json: JSON.t): result<greetInput, string> =>
  try Ok(json->S.parseOrThrow(greetInputSchema)) catch {
  | S.Error(err) => Error(err.message)
  }
```

## Server.res 側

大半のテンプレートで `Server.res` / `Routes.res` の呼び出し側を以下に統一する:

```rescript
let raw = await ctx->Hono.req->Hono.jsonBody
switch Validation.parseGreetInput(raw) {
| Ok(input) => ctx->Hono.json({"message": "Hello, " ++ input.name})
| Error(msg) => ctx->Hono.status(400)->Hono.json({"error": msg})
}
```

既存 hono テンプレートでは `c.req.valid("json")` 相当の zod/hono アダプタを使っているが、本プランでは「Validation モジュール経由で parse → 手動で 400 応答」に統一する（両ライブラリで同じフローにするため）。この統一に伴い、既存 hono でも Server.res の一部が変化するが、機能等価。デフォルト ZOD 時の byte-identical は hono は崩れる可能性があり、**AC-04 は hono / hono-graphql では現実的ではないため、AC-04 を「hono 系以外の既存 zod 部分は byte-identical、hono 系は『既存 zod API の範囲内で意図的に統一化』を許容」と読み替える**（steering レビュー時に明記）。

> 代替案: hono 系だけは既存の `c.req.valid` アダプタを残す。この場合、Server.res の分岐が複雑になる。統一 API（`parseXxx: JSON.t => result<_, _>`）を優先する方を採用。

## Next.js Route Handler — 完全 ReScript 化

既存 `src/app/api/greet/route.ts` は TypeScript だが、Next.js の Route Handler は `route.(ts|js|mjs)` ファイル名規約がある。一方 ReScript はモジュール名に大文字始まりを強制するため、`Route.res` → `route.mjs` には直接変換できない。

**方針**: `next/server` の `NextRequest` / `NextResponse.json` を最小バインディングし、ハンドラ本体を `src/app/api/greet/GreetRoute.res` に置く。Next.js からは `src/app/api/greet/route.ts` を **1 行の re-export shim** として残す。

```ts
// src/app/api/greet/route.ts — thin shim (variant 非依存)
export { post as POST } from "./GreetRoute.res.mjs";
```

```rescript
// src/app/api/greet/GreetRoute.res — variant 非依存
let post = async (req: NextServer.nextRequest): NextServer.nextResponse => {
  let raw = try await NextServer.reqJson(req) catch {
  | _ => JSON.Object(Dict.make())
  }
  switch Validation.parseGreetInput(raw) {
  | Ok(input) =>
    NextServer.jsonResponse(
      JSON.Object(Dict.fromArray([("message", JSON.String("Hello, " ++ input.name ++ "!"))])),
    )
  | Error(msg) =>
    NextServer.jsonResponseWithInit(
      JSON.Object(Dict.fromArray([("error", JSON.String(msg))])),
      {"status": 400},
    )
  }
}
```

```rescript
// src/NextServer.res — 共通 (variant 非依存)
type nextRequest
type nextResponse

@send external reqJson: nextRequest => promise<JSON.t> = "json"

@module("next/server") @scope("NextResponse")
external jsonResponse: JSON.t => nextResponse = "json"

@module("next/server") @scope("NextResponse")
external jsonResponseWithInit: (JSON.t, {..}) => nextResponse = "json"
```

`Validation.res` のみ variants/{zod,sury}/ に置き分け、`GreetRoute.res` / `NextServer.res` / `route.ts` は共通。上記 sury 版は実機コンパイル検証済み。

## 実装順序

1. ✅ **Foundation**: enum + Wizard UI + Builder + Context（main にマージ済み）
2. ✅ **TemplateVersions**: `SURY = "^10.0.0"`（main にマージ済み）
3. **Hono 系 2 テンプレ**: 既存 Schema.res の zod 依存部分を `variants/zod/src/Validation.res` に抽出し、drizzle 部分を `Schema.res` に残す。`variants/sury/src/Validation.res` を新規追加。Routes.res / Server.res を統一 API に寄せる。
4. **残り 6 テンプレート** (aws-lambda / cloudflare-workers / google-cloud-run / nextjs / full-stack / monorepo): 1 コミット/テンプレートで `variants/{zod,sury}/src/Validation.res` を新規追加し、Server.res / Routes.res を統一 API 呼び出しに書き換え。nextjs のみ `NextServer.res` + `GreetRoute.res` + `route.ts` shim 追加を含む。
5. **Docs**: CLAUDE.md / repository-structure.md 更新 (`wizard/` パッケージに `ValidationLibrary` への 1 行言及)。

## snapshot 検証

`TemplateSnapshotDumper` ベースの仕組みを再利用（既に worktree に 1 本存在）。各コミットで:

- ZOD snapshot（= 既存の生成物の延長）
- SURY snapshot（新規ビルド）

2 種類を diff して、ZOD 側は「既存との差分が該当テンプレートの意図変更分のみ」であること、SURY 側は「zod が sury に置換されている以外の差分がないこと」を確認。

## 非互換リスクチェック

- `TemplateContext` の data class に field を増やすが、デフォルト引数で既存呼び出しは互換。
- `RescriptModuleBuilder.validationLibrary` の永続化は Wizard セッション内のみ。`.idea/*.xml` の破壊なし。
- Plugin Verifier に deprecated API 追加なし。
