# design.md — res-x テンプレートの堅牢化

## 設計方針

- **機能単位で 3 コミット** に分割（(ウ) CI Bun setup / (A) コンパイルエラー修正 / (B) Bun UX 改善）。git-conventions.md に従い絵文字 + 英語メッセージ
- **`CommonFiles` は非破壊拡張**（デフォルト値付きオプショナル引数）: 他 17 テンプレートの generate 出力・既存テストを不変に保つ
- **`TodoForm.res` の循環依存は ref 経由で解消**: `onSubmit` と `renderForm` の相互参照を単一の ref セルに forward-declare することで `let rec` のラムダ RHS 制約を回避。コメントで理由を明記
- **`sury` variant の `%raw` 廃止**: ReScript の通常のオブジェクトリテラル `{"name": trimmedName, "description": trimmedDescription}` で payload を構築し、`S.parseOrThrow` に渡す。`zod` variant と同じ姿勢
- **`vite.config.js` の `clientDirs` は `[]`**: テンプレートには `client/` ディレクトリを生成しないため、empty 配列で plugin が起動時に失敗しないようにする
- **`dev` スクリプトの concurrently 化**: `concurrently "rescript -w" "bun --watch run src/App.res.mjs"` で res-x 上流のパターンに寄せる。`concurrently` は `TemplateVersions.CONCURRENTLY = "^9.2.1"` 既存定数を流用

## コンポーネント設計

### (ウ) `.github/workflows/integration-tests.yml`

既存 `actions/setup-node@v6` の直後に `oven-sh/setup-bun@v2` を追加する。Bun は `bun-version: latest` で最新版を取得する（LTS 概念が無いためデフォルト方針）。

```yaml
- uses: actions/setup-node@v6
  with:
    node-version: 22

- uses: oven-sh/setup-bun@v2
  with:
    bun-version: latest

- uses: pnpm/action-setup@v6
  with:
    version: 10
    run_install: false
```

`TemplateIntegrationTest.kt` は既存の `@EnumSource(ProjectTemplate::class)` で全テンプレートを自動巡回するため、RES_X 向けの特別扱いは不要。integrationTest 自体は `pnpm install` + `pnpm exec rescript` のみ実行するため、Bun が未導入でも ReScript コンパイル段階では進む。Bun が必要になる将来の拡張（`bun run` 実行検証など）のために CI 側のセットアップを先行して揃える。

### (A-1) `TodoForm.res` — 循環依存を ref で解消

旧: `let onSubmit = ... and renderFormWithError = ...`（構文エラー）  
新: `onSubmit` の handler と `renderForm` の相互参照を ref 経由で解消し、さらに `renderForm` / `renderFormWithError` の重複を `~error: option<string>=?` パラメータで統合する。

```rescript
// Forward-declare the hxPost handle so that renderForm (which needs
// `hx-post={onSubmit}`) and onSubmit's handler (which needs `renderForm` on
// validation error) can reference each other without a lexical cycle.
// ReScript's `let rec ... and ...` rejects non-lambda RHS such as
// `Handler.handler.hxPost(...)`, so a ref cell is the minimal workaround.
let hxPostHandle = ref(None)

let renderForm = (~error: option<string>=?, ()) =>
  switch hxPostHandle.contents {
  | None => Hjsx.null
  | Some(hxPost) =>
    <form
      id={formId}
      hxPost={hxPost}
      hxSwap={ResX.Htmx.Swap.make(OuterHTML)}
      hxTarget={ResX.Htmx.Target.make(CssSelector(`#${formId}`))}>
      ... fields ...
      {switch error {
      | Some(msg) => <p style="color:crimson"> {Hjsx.string(msg)} </p>
      | None => Hjsx.null
      }}
    </form>
  }

let onSubmit = Handler.handler.hxPost(
  "/todos",
  ~securityPolicy=ResX.SecurityPolicy.allow,
  ~handler=async ({request, requestController}) => {
    ...
    switch Validation.parseTodoInput(...) {
    | Ok(...) => ...
    | Error(msg) =>
      requestController.setStatus(400)
      renderForm(~error=msg, ())
    }
  },
)

hxPostHandle := Some(onSubmit)

@jsx.component
let make = () =>
  <section>
    <h2> {Hjsx.string("Todos")} </h2>
    {renderList()}
    {renderForm()}
  </section>
```

### (A-2) `Layout.res`

```rescript
<main>{children}</main>  // ← JSX 式補間に波括弧
```

### (A-3) `variants/sury/src/Validation.res`

```rescript
// ReScript オブジェクトリテラルで payload を構築（`%raw` テンプレート文字列は
// ReScript 変数を補間しないため、従来の実装は trimmedName/trimmedDescription
// が JS 側で ReferenceError になっていた）。
let payload = {
  "name": trimmedName,
  "description": trimmedDescription,
}
try {
  let _: rawInput = payload->Obj.magic->S.parseOrThrow(rawInputSchema)
  Ok({...})
} catch {
| S.Error(err) => Error(err.message)
}
```

`payload->Obj.magic` で `S.parseOrThrow` の期待する型に合わせて coerce する。`zod` variant と同じ "JS オブジェクトを直接渡す" 姿勢に揃える。

### (A-4) `vite.config.js`

```javascript
export default defineConfig({
  plugins: [
    resXVitePlugin({
      clientDirs: [],  // `client/` ディレクトリはテンプレートに含めない
    }),
  ],
  server: {
    port: 9000,
  },
})
```

### (B-1) `CommonFiles.readme`

オプショナル引数 `extraPrerequisites: List<String> = emptyList()` を追加。

```kotlin
fun readme(
    ctx: TemplateContext,
    description: String,
    scripts: List<Pair<String, String>>,
    extraSections: List<Pair<String, String>> = emptyList(),
    extraPrerequisites: List<String> = emptyList(),
): String = buildString {
    // ... 既存 Prerequisites セクション後 ...
    appendLine("- Node.js ${TemplateVersions.NODE_ENGINE.removePrefix(">=")}+")
    appendLine("- ${packageManagerName(ctx.packageManager)} (managed via Corepack)")
    extraPrerequisites.forEach { appendLine("- $it") }
    // ... 既存の空行・Getting Started セクションへ ...
}
```

既存 17 呼び出しはデフォルト空配列で変化なし。

### (B-2) `CommonFiles.ciWorkflow`

オプショナル引数 `setupBun: Boolean = false` を追加。`actions/setup-node` の直後に条件付きで `oven-sh/setup-bun@v1` を挿入する。

```kotlin
fun ciWorkflow(
    ctx: TemplateContext,
    hasBuild: Boolean = false,
    hasTest: Boolean = false,
    setupBun: Boolean = false,
): String = buildString {
    // ... actions/setup-node@v4 のあと ...
    if (setupBun) {
        appendLine("      - uses: oven-sh/setup-bun@v1")
        appendLine("        with:")
        appendLine("          bun-version: latest")
    }
    // ... pnpm/action-setup の条件分岐に接続 ...
}
```

### (B-3) `ResXTemplateFiles` の呼び出し更新

```kotlin
// package.json scripts: dev を concurrently 起動に変更
scripts = linkedMapOf(
    "start" to "bun run src/App.res.mjs",
    "dev" to "concurrently \"rescript -w\" \"bun --watch run src/App.res.mjs\"",
    ...
)

// devDependencies に concurrently を追加
devDependencies = linkedMapOf(
    "concurrently" to TemplateVersions.CONCURRENTLY,
    "vite" to TemplateVersions.VITE,
    "vitest" to TemplateVersions.VITEST,
    "@vitest/coverage-v8" to TemplateVersions.VITEST_COVERAGE_V8,
),

// README に Bun prerequisite
CommonFiles.readme(
    ctx = ctx,
    ...,
    extraPrerequisites = listOf("Bun 1.3 or later (install from https://bun.sh)"),
)

// CI に Bun setup
CommonFiles.ciWorkflow(ctx, hasTest = true, setupBun = true)
```

## 既存 API 再利用

| 機能 | 参照先 |
|---|---|
| README ビルダー | `CommonFiles.readme` (CommonFiles.kt:80) |
| CI ワークフロービルダー | `CommonFiles.ciWorkflow` (CommonFiles.kt:139) |
| concurrently 定数 | `TemplateVersions.CONCURRENTLY` (既存) |
| Hjsx 空要素 | `Hjsx.null`（既存） |
| res-x 型 | `ResX.Htmx.Swap` / `ResX.Htmx.Target`（既存インポート） |

## テスト設計

### `CommonFilesTest.kt` に追加

- `readme appends extra prerequisites when provided` — Bun 前提行が出ることを確認
- `readme omits extra prerequisites section when list is empty` — 既存挙動のリグレッション
- `ci workflow includes oven-sh setup-bun when setupBun is true` — Bun セットアップ YAML 出力
- `ci workflow omits setup-bun by default` — 既存挙動のリグレッション

### `ResXTemplateFilesTest.kt` に追加・修正

- `README mentions Bun prerequisite` — `"Bun 1.3"` を含む
- `CI workflow includes setup-bun step` — `"oven-sh/setup-bun"` を含む
- `dev script launches rescript watcher and bun watcher via concurrently` — `dev` script に `concurrently` と `bun --watch` が共存
- `package json declares concurrently devDependency` — `TemplateVersions.CONCURRENTLY` を参照
- `Layout embeds children via JSX interpolation` — `<main>{children}</main>`（文字列リテラル `children` ではない）
- `TodoForm renders unified form with optional error parameter` — `renderFormWithError` の文字列が消え、`~error=` の形で統合
- `sury variant builds payload without %raw template` — `%raw(` が出現せず、ReScript オブジェクトリテラルで `S.parseOrThrow` に渡っている
- `vite config uses empty clientDirs` — `clientDirs: []`

## 前提条件・制約

- Bun はローカル開発者 / CI runner に別途インストールが必要。README で明示
- ReScript の `let rec` 制約により、`TodoForm.res` の相互参照は ref セル経由で解消する（これが最小かつ安全な方法）
- 既存 17 テンプレートの `generate()` 結果に変更を与えてはならない（スナップショット等価性）
