# 設計: テンプレート品質向上 (1〜7)

## CommonFiles 拡張

新規ヘルパを `CommonFiles.kt` に追加する:

```kotlin
/** MIT license text with given year and copyright holder. */
fun mitLicense(year: Int = 2026, holder: String): String

/** Node major version for .nvmrc (derived from TemplateVersions.NODE_ENGINE). */
fun nvmrc(): String  // returns "20\n"

/** Basic Dependabot config for npm, weekly. */
fun dependabotYaml(): String

/** .env.example with human-readable comments and key=value lines. */
fun envExample(entries: List<Pair<String, String>>): String
//   entries: list of (key_description_comment, key=value) pairs
```

## TemplateVersions 追加

```kotlin
const val VITEST_COVERAGE_V8 = "^2.1.0"  // aligned with VITEST
const val NODE_MAJOR = "20"              // matches NODE_ENGINE >=20
```

## 各テンプレートへの追加

### 全 14 テンプレ

```kotlin
".nvmrc" to CommonFiles.nvmrc(),
"LICENSE" to CommonFiles.mitLicense(holder = ctx.projectName),
".github/dependabot.yml" to CommonFiles.dependabotYaml(),
```

scripts:
```kotlin
"test:coverage" to "vitest run --coverage"   // (vp test --coverage for Vite+ templates)
```

devDependencies:
```kotlin
"@vitest/coverage-v8" to TemplateVersions.VITEST_COVERAGE_V8
```

### env-var を読む 6 テンプレ

```kotlin
".env.example" to CommonFiles.envExample(listOf(
    "# SQLite file or Turso libsql:// URL" to "DATABASE_URL=file:./data/app.db",
    ...
)),
```

テンプレ別:
- **Hono REST / Hono GraphQL / Full-Stack / Monorepo server**: `DATABASE_URL`
- **Google Cloud Run**: `PORT` (optional, Cloud Run sets it)
- **AWS Lambda**: 実行環境で API Gateway のイベントを受けるだけなので env は不要。ただし `AWS_REGION` 等を書いてもよい → **スコープ外に戻す** (DB 統合は README recipe 扱いで実体では使っていない)

つまり `.env.example` を作るのは **5 テンプレ**: Hono REST / Hono GraphQL / Full-Stack / Monorepo / Google Cloud Run。

### Hono 系 4 テンプレの `app.onError`

各 `serverServerRes` / `serverRes` に以下を追加:

```rescript
app->Hono.onError((err, ctx) => {
  Console.error(err)
  ctx->Hono.status(500)->Hono.json({"error": "Internal Server Error"})
})
```

`ProjectFileBuilders.honoBindings()` に `onError` バインディングを追加:

```rescript
@send external onError: ('app, ('err, 'ctx) => 'response) => 'app = "onError"
```

### Hono 系 4 テンプレのテスト

現行:
```js
await expect(import("../Server.res.mjs")).resolves.toBeDefined();
```

拡張例 (Hono REST):
```js
import { describe, expect, it } from "vitest";
import { app } from "../Server.res.mjs";

describe("Users routes", () => {
  it("GET /users returns 200 with an array", async () => {
    const res = await app.request("/users");
    expect(res.status).toBe(200);
    const body = await res.json();
    expect(Array.isArray(body)).toBe(true);
  });
});
```

**注意事項**:
- `Server.res` から `app` を export する必要がある (現在 export されていない可能性あり)
- Hono REST のデフォルト DB は `file:./data/app.db` を参照する → テスト実行時に data/ が存在しない可能性。**マイグレーション未実行の in-memory 構築 or try/catch で 500 を許容**する方針で妥協
- シンプルさのため、テストは `/api/health` 等の **DB 無関係な軽量エンドポイント** を叩く

→ 各 Hono 系テンプレで **DB 不要なルート (例: `/api/hello`, `/api/health`)** を 1 つ持たせ、そこをテストで叩く。

### Full-Stack / Monorepo の Server.res 調整

`let app = Hono.createApp()` → `let app = ...; let _ = app`  (既に `app` 名で束縛済みなので export は自動)

ReScript のトップレベル `let` は .mjs で自動 export されるため、テストから `import { app } from "../Server.res.mjs"` で取り出せる。

## コミット粒度

1. CommonFiles 拡張 + TemplateVersions 定数追加 (テスト含む) — 1 コミット
2. 全 14 テンプレに `.nvmrc` / LICENSE / dependabot / coverage 追加 + テスト更新 — 1 コミット
3. 5 テンプレに `.env.example` 追加 + テスト更新 — 1 コミット
4. Hono 4 テンプレに `app.onError` 追加 (`honoBindings` + 各 server.res) + テスト更新 — 1 コミット
5. Hono 4 テンプレのテストを `app.request()` スタイルに拡張 + テスト更新 — 1 コミット
6. ドキュメント更新 (CLAUDE / README / product-requirements / sphinx EN+JA) — 1 コミット

## 影響範囲

- 約 25〜30 ファイル変更
- 推定 +400〜500 行
- 破壊的変更なし
- `app.onError` / `app.request()` は既存ユーザーの挙動に影響しないが、Server.res の一部ルートが `/api/*` プレフィックス付きかどうか templates によって違うので、テスト側でテンプレ別にエンドポイントを指定する
