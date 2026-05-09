# Hono + Inertia SSR — Tasklist

## 実装

- [x] T1: `src/main/resources/templates/hono-inertia/src/Ssr.res` を新規作成
  - `resolveComponent` (`Home` / `About`) を明示 switch 列挙
  - `renderInertia(pageObject) => {head: string array, body: string}` を提供
  - `react-dom/server` の `renderToString` バインディング + `<InertiaApp>` で context を提供
- [x] T2: `src/main/resources/templates/hono-inertia/src/Server.res` の `rootView` を更新し SSR HTML を埋め込む
- [x] T3: `src/main/resources/templates/hono-inertia/src/client/Main.res` を `hydrateRoot` に切り替え
- [x] T4: `HonoInertiaTemplateFiles.kt` の generate map に `src/Ssr.res` 行を追加
- [x] T5: `src/main/resources/templates/hono-inertia/src/__tests__/Server.test.mjs` に SSR ケース追加 (既存 3 ケース維持)
- [x] T6: `src/main/resources/templates/hono-inertia/readme/frontend.md` に SSR セクション追加 (ページ追加時の Ssr.res 更新ルールを明文化)

## 検証

- [x] `./gradlew ktlintCheck` が成功する
- [x] `./gradlew checkKdoc` が成功する
- [x] `./gradlew test` が成功する
- [x] `./gradlew integrationTest` (HONO_INERTIA zod / sury / bun zod / bun sury) がローカルで緑
- [x] `./gradlew koverVerify` が minBound=86 を維持する

## ドキュメント更新

- [x] CLAUDE.md レイヤー 3: Project Wizard 段落の Hono+Inertia 記述から「CSR のみ、SSR は将来対応」を外し、SSR 対応済みの説明に更新
- [x] sphinx-docs/user/templates/hono-inertia.md の SSR 記述
- [x] `make gettext && make update-po` で .po を同期し、`make build-ja` が通ることを確認

## マージ

- [ ] 機能単位コミット粒度: (a) ReScript テンプレート変更 (Ssr.res 新規 + Server.res / Main.res / pageRegistry 経由) / (b) Kotlin generator + テスト + readme / (c) docs 更新
- [ ] AskUserQuestion でマージ可否確認
- [ ] main へ fast-forward マージ & origin に push
- [ ] CI 緑を確認
