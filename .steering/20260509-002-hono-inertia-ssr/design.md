# Hono + Inertia SSR — Design

## アーキテクチャ概要

Hono プロセス内で `react-dom/server` の `renderToString` を直接呼び、SSR HTML をホストページに埋め込む。`@inertiajs/react/server` のヘルパは Promise を返し `rootView` の同期シグネチャと噛み合わないので使わず、最小限の手書き SSR にする。

```
┌─────────────── Hono プロセス (Node) ───────────────┐
│                                                   │
│  GET / (no X-Inertia)                             │
│   └→ HonoInertia middleware                       │
│       └→ rootView(page, ctx)                      │
│           ├→ Ssr.renderInertia(page)              │
│           │   ├→ pageRegistry[name](page.props)   │ ← React.createElement
│           │   └→ ReactDOMServer.renderToString    │ ← body HTML
│           └→ HTML host page (head + ssrBody)      │
└───────────────────────────────────────────────────┘
                         ↓
┌────────────── Browser ─────────────────────────────┐
│  Main.res: ReactDOM.Client.hydrateRoot(...)       │
└───────────────────────────────────────────────────┘
```

非 Inertia 訪問でのみ SSR が走る。Inertia ナビゲーション (`X-Inertia: true`) は middleware が JSON で応答し SSR を迂回。

## 変更ファイル一覧

### 新規

| パス | 内容 |
|------|------|
| `src/Ssr.res` | ページレジストリ + `renderInertia(pageObject) => {head, body}` |

### 編集

| パス | 変更点 |
|------|------|
| `src/Server.res` | `rootView` から `Ssr.renderInertia(page)` を呼び、結果を `<div id="app" data-page='…'>SSR_HTML</div>` と `<head>` に展開 |
| `src/client/Main.res` | `createRoot` → `hydrateRoot` |
| `src/__tests__/Server.test.mjs` | 既存 3 ケース維持 + 新規 1 ケース「SSR 内容が含まれる」 |
| `src/main/resources/templates/hono-inertia/readme/frontend.md` | SSR セクション追加 (ページ追加時に Pages/ と Ssr.res 両方を更新する旨) |
| `CLAUDE.md` | 「CSR のみ、SSR は将来対応」→ SSR 対応済みに書き換え |
| `docs/product-requirements.md` | US-11 受け入れ条件リスト or 該当行に SSR 対応注記 |

## `Ssr.res` の中身

```res
// Pre-imported page components. Adding a new page requires touching
// both `client/Pages/` and this registry — the explicit list keeps
// SSR resolution synchronous (no dynamic import) and guards against
// silent 404s for misnamed pages.

%%raw(`import * as React from "react"`)
@module("react-dom/server")
external renderToString: React.element => string = "renderToString"

let pageRegistry: dict<HonoInertia.pageObject => React.element> = {
  let d = Dict.make()
  Dict.set(d, "Home", page => {
    let title = page.props->Js.Json.decodeObject->...->getStringOrEmpty("title")
    <Pages.Home title />
  })
  Dict.set(d, "About", _page => <Pages.About />)
  d
}

let renderInertia = (page: HonoInertia.pageObject): {"head": array<string>, "body": string} => {
  let component = Dict.getUnsafe(pageRegistry, page.component)
  let element = component(page)
  let body = renderToString(<MainLayout> {element} </MainLayout>)
  {"head": [], "body": body}
}
```

実際は `pageObject.props` が `Js.Json.t` なので、`Home` / `About` の props 抽出は最小限で済むよう shim を挟む。詳細は実装時に調整。

> 注: ReScript で React Component を `(props => element)` の dict に格納するときは型を緩めにする必要があるかもしれない。最終形は実装中に確認。

## `Server.res` の `rootView` 更新

```res
let rootView: HonoInertia.rootView = (page, _ctx) => {
  let rendered = Ssr.renderInertia(page)
  let pageJson = page->HonoInertia.serializePage->escapeApostrophes
  let head = rendered["head"]->Array.join("\n")
  let body = rendered["body"]
  `<!DOCTYPE html>
<html lang="en">
  <head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>Hono Inertia</title>
    ${head}
  </head>
  <body>
    <div id="app" data-page='${pageJson}'>${body}</div>
    <script type="module" src="/src/client/Main.res.mjs"></script>
  </body>
</html>`
}
```

`rootView` は引き続き `string` を返す同期関数。`Ssr.renderInertia` を sync で完結させる前提。

## `Main.res` の hydrate 化

```res
@module("./pages.js")
external resolvePage: string => promise<{..}> = "resolvePage"

InertiaBindings.createInertiaApp({
  resolve: resolvePage,
  setup: ({el, app, props}) => {
    let _root = ReactDOM.Client.hydrateRoot(el, React.createElement(app, props))
    ()
  },
})
```

`createRoot(el)->render(...)` → `hydrateRoot(el, element)`。React の戻り値 `Root` は使わない。

## React DOM Client バインディング確認

`ReactDOM.Client.hydrateRoot` のバインディングが `@rescript/react` 0.13+ にあるはず。なければ最小限の external を `Main.res` に書く。

## ページ追加ワークフロー

1. `src/client/Pages/Foo.res` を新規作成 (`@react.component let make = ...`)
2. `src/client/pages.js` の glob は自動で拾う (CSR 用)
3. `src/Ssr.res` の `pageRegistry` に `Dict.set(d, "Foo", page => <Pages.Foo ...>)` を追記
4. README にも明記

将来 Vite SSR を統合すれば Step 3 は不要になるが、本 PR ではトレードオフを取って明示レジストリにする。

## テスト方針

### 新規 Vitest ケース

```js
it("GET / responds with SSR-rendered HTML on a non-Inertia visit", async () => {
  const res = await app.request("/");
  expect(res.status).toBe(200);
  const body = await res.text();
  // SSR は <div id="app">…</div> の中に React のマークアップを書き出す
  expect(body).toMatch(/<div id="app" data-page='[^']+'>.+<\/div>/s);
  // Home コンポーネントは <h1>Home</h1> を含む (実際のテンプレート確認後に確定)
  expect(body).toContain("Home");
});
```

### 既存ケースの保護

- `GET /health returns 200 ... { status: "ok" }` — 変更なし
- `GET / responds with the Inertia HTML host page on a non-Inertia visit` — `data-page=` の存在は引き続き満たす (既存 assertion 変えない)
- `GET / with the Inertia header returns a JSON page object` — Inertia ヘッダ送信なら SSR を走らせない middleware の挙動に依存

### 統合テスト (gradle)

`TemplateIntegrationTest.HONO_INERTIA (zod/sury)` の `pnpm test` 段が緑になる必要あり。bun テストも同様。

## kover 影響

新規 `Ssr.res` は ReScript ファイル (Kotlin ではない) なので Kover には影響しない。Kotlin 側の変更は `HonoInertiaTemplateFiles.kt` の `generate` map に `src/Ssr.res` 行を 1 行追加するだけで、既存テストは Stub で通る。

## ロールアウト方針

- 既存テンプレートを SSR 化するので、ProjectTemplate の `id`/`displayName` は変更しない
- ユーザーは pull 後 `pnpm install && pnpm dev` で動く想定 (依存追加なし、`react-dom/server` は `react-dom` パッケージに同梱)
- README の SSR セクションは「Default-on」と書く

## リスクと対策

| リスク | 対策 |
|------|------|
| `renderToString` が遅い (10ms+) で初回応答が落ちる | 本 MVP では許容。将来 streaming SSR or プリレンダリング cache |
| hydration mismatch (props が一致しない) | Inertia の page object が SSR/CSR 共通なので、props は完全一致する設計 |
| ページ追加時に Ssr.res 更新を忘れる | README + コードコメントで明示 |
| Node 環境で `import.meta.glob` を使うパス (Vite 用) が問題化 | 本 PR では使わず、明示レジストリで回避 |
