# Hono + Inertia SSR — Tasklist

## 実装

- [ ] T1: `src/main/resources/templates/hono-inertia/src/Ssr.res` を新規作成
  - `pageRegistry` (`Home` / `About`) を明示列挙
  - `renderInertia(pageObject) => {head: string array, body: string}` を提供
  - `react-dom/server` の `renderToString` バインディング
- [ ] T2: `src/main/resources/templates/hono-inertia/src/Server.res` の `rootView` を更新し SSR HTML を埋め込む
- [ ] T3: `src/main/resources/templates/hono-inertia/src/client/Main.res` を `hydrateRoot` に切り替え
- [ ] T4: `HonoInertiaTemplateFiles.kt` の generate map に `src/Ssr.res` 行を追加
- [ ] T5: `src/main/resources/templates/hono-inertia/src/__tests__/Server.test.mjs` に SSR ケース追加 (既存 3 ケース維持)
- [ ] T6: `src/main/resources/templates/hono-inertia/readme/frontend.md` に SSR セクション追加 (ページ追加時の Ssr.res 更新ルールを明文化)

## 検証

- [ ] `./gradlew ktlintCheck` が成功する
- [ ] `./gradlew checkKdoc` が成功する
- [ ] `./gradlew test` が成功する
- [ ] `./gradlew koverVerify` が minBound=86 を維持する
- [ ] CI の `template-integration` ジョブで HONO_INERTIA (zod / sury / bun zod / bun sury) が緑

## ドキュメント更新

- [ ] CLAUDE.md レイヤー 3: Project Wizard 段落の Hono+Inertia 記述から「CSR のみ、SSR は将来対応」を外し、SSR 対応済みの説明に更新
- [ ] docs/product-requirements.md US-11 関連の記述を更新 (該当があれば)
- [ ] sphinx-docs/user/templates/hono-inertia.md (存在すれば) の SSR 記述
- [ ] `make gettext && make update-po` で .po を同期し、`make build-ja` が通ることを確認

## マージ

- [ ] 機能単位コミット粒度: (a) ReScript テンプレート変更 (Ssr.res 新規 + Server.res / Main.res / pageRegistry 経由) / (b) Kotlin generator + テスト + readme / (c) docs 更新
- [ ] AskUserQuestion でマージ可否確認
- [ ] main へ fast-forward マージ & origin に push
- [ ] CI 緑を確認
