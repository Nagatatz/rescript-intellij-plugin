# 設計: Hono テンプレートへの CORS プレ配線

## 変更対象

### Kotlin
- `src/main/kotlin/com/rescript/plugin/wizard/ProjectFileBuilders.kt` — `honoBindings()` に `cors` external を追加

### テンプレートリソース
- **プレ配線する** (2 テンプレート)
  - `src/main/resources/templates/full-stack/src/server/Server.res`
  - `src/main/resources/templates/monorepo/packages/server/src/Server.res`
- **コメント例のみ** (5 テンプレート)
  - `src/main/resources/templates/hono/src/Server.res`
  - `src/main/resources/templates/hono-graphql/src/Server.res`
  - `src/main/resources/templates/cloudflare-workers/src/Server.res`
  - `src/main/resources/templates/aws-lambda/src/Server.res`
  - `src/main/resources/templates/google-cloud-run/src/Server.res`

### ドキュメント
- `src/main/resources/templates/common/readme/extending-bindings.md` — middleware レシピを `hono/jwt` 例に差し替え (cors は既に `Hono.cors` として使える旨を追記)
- `src/main/resources/templates/full-stack/readme/architecture.md` — dev CORS の注記を追加 (新規 CORS セクションではなく既存セクション内に 2-3 行)
- `src/main/resources/templates/monorepo/readme/` — monorepo は専用 README セクションが少ないので、README 全体を再調査して適切な位置に注記を追加

### テスト
- `src/test/kotlin/com/rescript/plugin/wizard/ProjectTemplateTest.kt` — 新規テスト追加

## `honoBindings()` への cors external

`Hono.res` 末尾に追加:

```rescript
// Middleware factories

@module("hono/cors") external cors: 'opts => middleware = "cors"
```

型を `'opts` にすることで origin / allowMethods / credentials / etc. すべての Hono CORS オプションを利用者が自由に指定できる (他の hono middleware 追加時も同じパターン)。

## FULL_STACK / MONOREPO Server.res のプレ配線

**配置**: `Hono.createApp()` の直後、`onError` / ルート登録より前。

```rescript
let app = Hono.createApp()

// Dev CORS: allow the Vite+ client on :5173.
// Tighten or replace this origin list before deploying to production.
app->Hono.use(
  Hono.cors({
    "origin": "http://localhost:5173",
    "allowMethods": ["GET", "POST", "PUT", "DELETE"],
    "credentials": true,
  }),
)

app->Hono.onError(...)
```

## その他 5 テンプレート Server.res のコメント例

`Hono.createApp()` 直後に 2 行コメントを挿入:

```rescript
let app = Hono.createApp()

// Enable CORS if this service is called from a browser:
//   app->Hono.use(Hono.cors({"origin": "https://your-app.example"}))
```

**理由**: 詰まった時に Server.res を見ただけで解決策が見つかる。コメントは行数コストが小さく、プロダクション利用時のデフォルト暗黙設定を増やさない。

## extending-bindings.md 更新

現状 (cors を手書きで bind する例):

```rescript
@module("hono/cors") external cors: {"origin": string} => Hono.middleware = "cors"
let app = Hono.createApp()
app->Hono.use(cors({"origin": "http://localhost:5173"}))
```

更新後 (cors が共通昇格済みなので jwt 例に差し替え + 共通化の言及):

```markdown
`hono/cors` is already pre-bound as `Hono.cors` in this template. For other
middlewares (`hono/jwt`, `hono/basic-auth`, `hono/cache`, ...), bind the factory
and pass the result to `Hono.use`:

```rescript
@module("hono/jwt") external jwt: {"secret": string} => Hono.middleware = "jwt"

let app = Hono.createApp()
app->Hono.use(jwt({"secret": "change-me-in-prod"}))
```
```

## テスト追加

```kotlin
@Test
fun `honoBindings exposes cors middleware factory`() {
    val bindings = ProjectFileBuilders.honoBindings()
    assertTrue(bindings.contains("@module(\"hono/cors\")"))
    assertTrue(bindings.contains("external cors"))
}

@Test
fun `FULL_STACK server pre-wires CORS for Vite dev origin`() {
    val files = ProjectTemplate.FULL_STACK.generateFiles("demo")
    val server = files["src/server/Server.res"] ?: error("FULL_STACK Server.res missing")
    assertTrue(server.contains("Hono.cors"))
    assertTrue(server.contains("http://localhost:5173"))
}

@Test
fun `MONOREPO server pre-wires CORS for Vite dev origin`() {
    val files = ProjectTemplate.MONOREPO.generateFiles("demo")
    val server = files["packages/server/src/Server.res"] ?: error("MONOREPO Server.res missing")
    assertTrue(server.contains("Hono.cors"))
    assertTrue(server.contains("http://localhost:5173"))
}
```

## リスク

- **プロダクション漏れ**: 利用者が dev origin (`http://localhost:5173`) のままデプロイし、不要なオリジンに API が晒される。→ Server.res のコメントと README の注記の 2 段階で警告
- **Vite+ のデフォルト port 変更**: Vite+ が将来 `:5174` 等をデフォルトにしたら origin を追随する必要がある (現状は `:5173` 固定)
- **他ミドルウェア順序**: CORS は `onError` より先に置くべき (エラー応答にも CORS ヘッダが必要)。現設計は `createApp()` 直後に配置するため問題なし

## ドキュメント影響

- `CLAUDE.md` — 変更不要 (言語機能の追加ではない)
- `README.md` / `docs/` — 変更不要
- `sphinx-docs/` — 変更不要 (テンプレート出力のみ)
