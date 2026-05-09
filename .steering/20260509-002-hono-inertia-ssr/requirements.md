# Hono + Inertia Template SSR — Requirements

## 背景

CLAUDE.md と product-requirements.md には Hono + Inertia テンプレートが「CSR のみ、SSR は将来対応」と明記されており、唯一の `CSR-only` 例外として残っている。SSR は SEO・初期描画速度・OGP 表示など実用面で必要になる場面が多く、特にバックエンドを Hono (Node) に置く時点で React のサーバ側レンダリングは無料に近いコストで提供できる。

既存資産:

- `src/Server.res` の `rootView`: 非 Inertia 訪問のときに HTML ホストページを返す関数。SSR HTML を組み込む差し込み口として最適。
- `src/InertiaBindings.res`: `@inertiajs/react` クライアント側のバインディング。サーバ側 (`@inertiajs/react/server`) のバインディングは未追加。
- `src/client/Main.res`: `createRoot` で純粋にクライアント描画。SSR 化後は `hydrateRoot` に切り替え必要。
- `vite.config.mjs` / `vp build`: Vite+ ベース。SSR 用の追加ビルドはまだ無い。
- `src/__tests__/Server.test.mjs`: 既存の vitest スモークがあるので、SSR 用テストも同フォーマットで足せる。

## ユーザーストーリー

### US-01: 初期 HTML レスポンスにレンダリング済みページが含まれる

**Hono + Inertia テンプレートを採用した開発者として**、初回 GET (`X-Inertia` ヘッダなし) のレスポンス HTML 内に、Inertia ページの React コンポーネントを `ReactDOMServer.renderToString` で事前レンダリングしたマークアップを埋め込みたい。これにより SEO クローラ・OGP・JS 無効環境でもコンテンツが見える。

**受け入れ条件:**

- [ ] `GET /` (Inertia ヘッダなし) のレスポンス HTML を取得すると `<div id="app" data-page='…'>…非空のレンダリング済み HTML…</div>` が返る
- [ ] `<head>` に Inertia の `head` スロット由来の追加要素 (タイトル、meta 等) が含まれる
- [ ] `GET /about` (Inertia ヘッダなし) でも同様に SSR されたマークアップが含まれる
- [ ] `GET /` に `X-Inertia: true` を送ると **JSON ページオブジェクト** が返る (SSR は迂回される)
- [ ] CSR 時のテストは引き続き 200 を返し、JSON のページプロップ ( `component=Home`, `props.title=Home` ) が一致する
- [ ] 新しい vitest ケース「`GET / responds with SSR-rendered HTML on a non-Inertia visit`」が pass する

### US-02: クライアントは hydration で動く

**ユーザーとして**、サーバから受け取った HTML を React がそのまま再利用 (hydrate) し、追加リクエストや再描画なしにインタラクティブになってほしい。

**受け入れ条件:**

- [ ] `src/client/Main.res` が `ReactDOM.Client.hydrateRoot` を使う
- [ ] hydration mismatch 警告が出ない (サーバとクライアントで同じ Inertia ページオブジェクトを使うため、props は完全一致)
- [ ] 既存の Inertia ナビゲーション (`<InertiaBindings.Link>`) はクライアント側ルーティングのまま動く

### US-03: SSR 用ページ登録のメンテ性

**保守者として**、SSR レンダリングで使うページコンポーネント解決ロジックは「ページを足したら 1 ファイルだけ更新すれば済む」状態にしたい。動的 glob は Node ランタイムで素直に動かないので、明示的なレジストリで簡潔にする。

**受け入れ条件:**

- [ ] 新規 `src/Ssr.res` に `pageRegistry: dict<HonoInertia.pageProps => React.element>` を持ち、`Home` / `About` を 1 か所で列挙する
- [ ] 未登録ページが要求された場合は `failwith("Inertia SSR: unknown page <name>")` で落とす (型安全側に倒す)
- [ ] README の `frontend.md` に「ページを追加するときは Pages/ と Ssr.res の両方を更新する」明文化

### US-04: 既存 CSR テストとの両立

**Hono + Inertia テンプレートの統合テスト** (`TemplateIntegrationTest.HONO_INERTIA`) は SSR 化後も pass しなければならない。SSR 用の追加 vitest ケースは新規追加のみで、既存 3 ケースの assertion は変更しない (互換性維持)。

**受け入れ条件:**

- [ ] `template-integration` ジョブが緑のまま
- [ ] `pnpm test` (Vitest) で 4 ケース全て pass する: 既存 3 + SSR 1
- [ ] `rescript build` が通る (新規 `Ssr.res` も含めて)
- [ ] CSR モード (将来 `INERTIA_SSR=0` 等で無効化) は本 PR では実装しない (`SSR by default`)

## スコープ外

- 動的 `import.meta.glob` ベースの自動ページ登録 (将来 Vite SSR 統合と一緒に検討)
- HMR + SSR の両立 (dev は CSR-only fallback でも可、prod build で SSR 化)
- ストリーミング SSR (`renderToPipeableStream` 等) — `renderToString` で十分
- 既存 `vp dev` ワークフローへの SSR 組み込み (まずは Hono 起動時の SSR を実現し、dev 時の挙動は次フェーズ)
- SEO 用追加 metadata DSL (`<Head>` コンポーネント) — Inertia 公式 API があるが本 PR では触らない

## 機能カテゴリ

- Project Wizard (テンプレート更新)
- フロントエンド統合
