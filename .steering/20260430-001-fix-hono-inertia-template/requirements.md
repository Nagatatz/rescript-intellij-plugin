# 要求内容

## 背景

`HONO_INERTIA` プロジェクトテンプレートを実際の `@hono/inertia` v0.2.0 / `@inertiajs/react` v3 の API と照らし合わせた結果、複数の不整合とデッドコードが残っており、生成されたプロジェクトがそのままでは正しく動かない可能性が高い。

## ユーザーからの指示

「HONO_INERTIA テンプレートを修正してください」（修正範囲は (A) 全部修正 を選択）

## 不整合一覧（全部修正）

1. **`src/HonoInertia.res` の `render` バインディングの型が誤り**
   - 現在: `external render: (Hono.context, string, 'props) => promise<'response>`
   - 正しい: `c.render(component, props)` は同期で `Response` を返す（`@hono/inertia/dist/index.d.ts` の `ContextRenderer` 拡張参照）。`promise` ラップは不要。

2. **`src/Routes.res` の不必要な `await`**
   - 現在: `await ctx->HonoInertia.render(...)` を 3 箇所で実行。
   - 正しい: `await` は型を `Response` のままで素通りするが、本来不要。`return ctx->HonoInertia.render(...)` で十分。同期化に伴い handler の `async` 修飾も削除する（form post の `Hono.jsonBody` は `await` 必要なので post handler は async のまま）。

3. **`vite.config.mjs` の `inertiaPages()` プラグインが本テンプレートでは無意味**
   - `inertiaPages()` は TypeScript で `c.render` の第 1 引数を狭めるための `pages.gen.ts` を生成するプラグイン。ReScript では `HonoInertia.res` の external 宣言で型が確定しており、生成された `.gen.ts` は使われない。
   - さらに既定値が `pagesDir: 'app/pages'` / `extensions: ['tsx']` で本テンプレートのレイアウト（`src/client/Pages/*.res`）に合わず、無効な参照を生成する。
   - 修正方針: `vite.config.mjs` から `inertiaPages` の import と plugin 登録を削除する。`@hono/inertia` ランタイム本体は引き続き使うため依存自体は残す。

4. **`index.html` が `inertia()` の既定 `rootView` と競合**
   - `inertia()` ミドルウェアは未指定時に最小 HTML shell（`<div id="app" data-page="...">`）をデフォルト `rootView` として返す。Vite の `index.html` は配信されない経路なので存在自体がノイズ。
   - 修正方針: `Server.res` で `inertia({rootView})` を渡し、`<script type="module" src="/src/client/Main.res.mjs">` を含む HTML shell を返す `rootView` を ReScript 側で記述する。`index.html` ファイルは削除する。`HonoInertia.res` に `serializePage` バインディングと `rootView` 型を追加し、Vite が `Main.res.mjs` を解決できるようにする。

5. **`src/client/Pages/Home.res` のデッドコード**
   - `type props = {...}` と `let _ = ({title, message}: props)` は `@react.component` PPX が同名の `props` 型を生成するため重複・抑制コードに過ぎない。
   - 修正方針: `type props` と `let _ = ...` の 2 箇所を削除し、`@react.component let make = (~title, ~message) => ...` のみに整理する。

6. **`src/client/pages.js` の resolver フォールバックを最小化**
   - 現在: `mod.make ?? mod.default ?? mod` の三段フォールバック。
   - 修正方針: `@react.component` 経由で必ず `make` が export されるため、`mod.make` を `default` に再 export するだけにし、無いケースは明示的にエラーにする（`if (!mod.make) throw ...`）。

## 受け入れ条件

- [ ] `HonoInertiaTemplateFiles.generate()` が生成する `src/HonoInertia.res` の `render` 戻り値型に `promise<` が含まれない
- [ ] `src/Routes.res` の `await ctx->HonoInertia.render(...)` がすべて削除されている（`Hono.jsonBody` の await のみ残る）
- [ ] `vite.config.mjs` に `inertiaPages` の import / plugin 登録が含まれない
- [ ] 生成ファイル一覧から `index.html` が消えている
- [ ] `src/Server.res` が `HonoInertia.inertia({rootView: ...})` の形でミドルウェアを登録し、rootView は `<script type="module" src="/src/client/Main.res.mjs">` と Inertia の `data-page` 埋め込みを含む HTML 文字列を返す
- [ ] `src/HonoInertia.res` が `serializePage` と `rootView` 型を export している
- [ ] `src/client/Pages/Home.res` に `type props` も `let _ = ...` も含まれない
- [ ] `src/client/pages.js` が `mod.make` のみを参照し、無い場合は明示的にエラーを投げる
- [ ] 既存の `HonoInertiaTemplateFilesTest` が新仕様に合わせて更新され、すべてパスする
- [ ] `./gradlew ktlintCheck buildPlugin test` がすべて通る

## 非対象

- `@hono/inertia` のバージョンアップ（既に 0.2.0 を参照しており据え置き）
- `Hono.res` の共有バインディング（他テンプレートと byte-identical の制約あり）
- 他のテンプレート（`HONO`, `HONO_GRAPHQL` 等）の修正
