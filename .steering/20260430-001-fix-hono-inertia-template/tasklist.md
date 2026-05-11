# Task List

## 実装

- [x] `src/main/resources/templates/hono-inertia/src/HonoInertia.res` を更新
  - `pageObject` 型、`rootView` 型、`options` 型を追加
  - `inertia` を `options => Hono.middleware` に変更
  - `render` を `=> 'response`（promise 解除）に変更
  - `serializePage` バインディングを追加
- [x] `src/main/resources/templates/hono-inertia/src/Server.res` を更新
  - `let rootView: HonoInertia.rootView = ...` を追加（HTML shell を返す）
  - `' → &#39;` エスケープを実装
  - `app->Hono.use(HonoInertia.inertia({rootView: rootView}))` に変更
- [x] `src/main/resources/templates/hono-inertia/src/Routes.res` を更新
  - GET handler の `async` / `await` を削除し直接 return
  - POST handler の async は維持（`Hono.jsonBody` のため）
- [x] `src/main/resources/templates/hono-inertia/src/client/Pages/Home.res` を更新
  - `type props` と `let _ = ...` を削除
- [x] `src/main/resources/templates/hono-inertia/src/client/pages.js` を更新
  - `mod.make` のみ参照、欠如時は throw
- [x] `src/main/resources/templates/hono-inertia/vite.config.mjs` を更新
  - `inertiaPages` import と plugin 登録を削除
  - 注釈で `@hono/inertia/vite` の文字列を踏まないよう「Hono Inertia type-safety plugin」と表現
- [x] `src/main/resources/templates/hono-inertia/index.html` を削除
- [x] `src/main/kotlin/com/rescript/plugin/wizard/templates/HonoInertiaTemplateFiles.kt` を更新
  - files マップから `"index.html"` エントリを削除
- [x] `src/main/resources/templates/hono-inertia/readme/project-layout.md` を更新
  - ディレクトリツリーから `index.html` 行を削除し、`vite.config.mjs` の説明を `@vitejs/plugin-react` に変更
  - 末尾に「Where is the HTML host page?」セクションを追加し rootView の役割を明示
- [x] `src/main/resources/templates/hono-inertia/readme/frontend.md` を更新
  - 新規ページ作成手順のサンプルから `async ctx` / `await` を削除

## テスト

- [x] `src/test/kotlin/com/rescript/plugin/wizard/templates/HonoInertiaTemplateFilesTest.kt` を更新
  - `template ships ... scaffolding`: `index.html` 確認を削除し、`assertFalse(files.containsKey("index.html"))` を追加
  - `vite config ...`: テスト名と内容を `does not register the inertiaPages plugin` に変更
  - `server wires the Inertia middleware ...`: `HonoInertia.inertia({rootView` を確認
  - `routes call HonoInertia render ...`: `await ctx->HonoInertia.render` を含まないことを確認
  - 新規 `HonoInertia bindings declare a non-promise render and serializePage`
  - 新規 `Server.res defines a rootView that embeds the page JSON and the client entry`
  - `ships sample Home and About pages ...`: `type props` / `let _ = (` がないことを確認
  - 新規 `pages js shim resolves make and throws on missing exports`
- [x] `src/test/kotlin/com/rescript/plugin/wizard/ProjectTemplateTest.kt` を更新
  - `HonoInertia.inertia()` の引数なし期待を `HonoInertia.inertia({rootView` に変更

## 検証

- [x] `./gradlew ktlintCheck` 成功
- [x] `./gradlew test` 全件成功（3624 件、folding の 1 件 / HonoInertia / ProjectTemplate すべてパス）
- [x] `./gradlew buildPlugin` 成功

## ドキュメント

- ドキュメント（CLAUDE.md / README.md / sphinx-docs）への影響なし（API ユーザー視点で機能差分なし）

## マージ

- [x] `AskUserQuestion` でマージ可否確認
- [x] main にマージ
