# Requirements — Hono + Inertia (React) テンプレート追加

## 1. 背景

ReScript IntelliJ Plugin の Project Wizard には現在 16 種類のテンプレートがあるが、サーバーが直接 React コンポーネントへ props を流し込む「Inertia.js モデル」のフルスタックテンプレートが存在しない。Inertia は「API を書かずに SPA UX を得る」第三の選択肢として 2026 年に注目を集めており、Hono + Inertia + React 構成は Next.js / Remix とは異なる軽量な選択肢として有用である。

加えて、VoidZero が公開した **Vite+ (Vite Plus)** が 2026 年 alpha 版で出ており、`vite test` / `vite lint` / `vite format` / `vite run` を単一 CLI に統合する「次世代 Vite」となる。新規テンプレートはこの Vite+ を前提とすることで、既存テンプレートが個別に持つ vitest.config / eslintrc / prettierrc を一本化できる。

## 2. 目的

Project Wizard に **「Hono + Inertia (React)」** テンプレートを追加し、以下を満たす雛形を生成する:

- Hono バックエンド + Inertia.js + React (CSR) の最小構成
- ReScript で書かれた Server.res / Routes.res / Schema.res / Validation.res
- ReScript で書かれた React ページコンポーネント（@rescript/react 経由）
- Vite+ 統合ツールチェーン（test / lint / format / build を `vite` コマンドに統一）
- 既存パターンに揃えた Drizzle ORM + Zod/Sury Validation 層

## 3. スコープ

### IN SCOPE

- `ProjectTemplate.HONO_INERTIA` 列挙値の追加（17 番目のテンプレート）
- `HonoInertiaTemplateFiles.kt` の新規作成
- 静的リソース `src/main/resources/templates/hono-inertia/` の整備
- `variants/zod/` および `variants/sury/` 双方の Validation.res の用意
- パッケージマネージャ選択（npm / yarn / pnpm / bun）への完全追従。Vite+ は Node / Bun のいずれでも動作する前提
- TemplateVersions.kt への Inertia / Vite+ 関連バージョン定数追加
- `RescriptProjectWizardStep` 系 UI の選択肢追加（既存の dropdown に追記するだけ。新フィールドは追加しない）
- ユニットテスト（HonoInertiaTemplateFilesTest.kt）と既存 ProjectTemplateTest.kt の更新（16 → 17）
- ドキュメント更新（CLAUDE.md, README.md, sphinx-docs (英) + ja .po, plugin.xml change-notes）

### OUT OF SCOPE

- **SSR (Server-Side Rendering)** — 初版は CSR のみ。SSR は将来別 PR で対応
- **Vue / Svelte 版** — フロントは React 固定
- **Better Auth / Lucia 等の認証統合** — 認証はテンプレートに含めない（README で参照のみ）
- **Cloudflare Workers / Vercel Edge デプロイ設定** — Node / Bun のローカル開発環境のみ。エッジは将来検討
- **Vite+ の Oxlint カスタムルール拡張** — デフォルト設定で開始

## 4. ユーザーストーリー

### US-1: Inertia 採用検討者

**ReScript で「API を書かない SPA」を試したい開発者として**、IDE の New Project ダイアログから「Hono + Inertia」を選ぶだけで、Hono ルーティング → Inertia レスポンス → React ページコンポーネントまで一通り動く雛形を得たい。

**受け入れ条件:**
- [ ] New Project ダイアログに「Hono + Inertia」が表示される
- [ ] 生成直後に `<package-manager> install && <package-manager> dev` が成功する
- [ ] ブラウザで `http://localhost:5173` を開くと React ページが Inertia 経由で表示される
- [ ] サーバーから渡された props が React コンポーネントに正しく流れている

### US-2: Vite+ 統合の恩恵

**ビルドツールを最小化したい開発者として**、テンプレート同梱の package.json 1 つで test / lint / format / build / dev を完結させたい（vitest / eslint / prettier の個別設定が不要）。

**受け入れ条件:**
- [ ] `package.json` の `scripts` に `vite test` / `vite lint` / `vite format` / `vite build` / `vite dev` が含まれる
- [ ] 個別の `vitest.config.*` / `.eslintrc.*` / `.prettierrc.*` は同梱しない
- [ ] `vite.config.mjs` が Vite+ の単一設定として機能する

### US-3: Validation 層の選択

**Zod / Sury のどちらを使うか選べる開発者として**、生成されるテンプレートが選択した Validation ライブラリの実装を `Validation.res` に書き出してほしい。

**受け入れ条件:**
- [ ] Wizard で Zod を選択 → `variants/zod/src/Validation.res` の内容が書き出される
- [ ] Wizard で Sury を選択 → `variants/sury/src/Validation.res` の内容が書き出される

## 5. 受け入れ条件（プロダクト全体）

- [ ] `./gradlew clean buildPlugin test` が成功する
- [ ] `./gradlew ktlintCheck` が成功する
- [ ] 生成されたプロジェクトが選択された全 4 パッケージマネージャ（npm / yarn / pnpm / bun）で `install` → `dev` 成功する
- [ ] 生成されたプロジェクトが Zod / Sury 両 variant で `vite build` 成功する
- [ ] sphinx-docs/user/features/advanced.md（または該当ページ）に新テンプレートが記載され、`.po` も更新されている
- [ ] plugin.xml `<change-notes>` に該当エントリが追加されている

## 6. 非機能要件

- 既存の 16 テンプレートと同じ「TemplateFiles オブジェクト + variants/<key>/」構造に従い、アーキテクチャを変更しない
- `TemplateVersions.kt` 経由で全バージョンを集中管理する（Inertia / Vite+ 関連も同様）
- セキュリティ要件は CLAUDE.md セキュリティセクションに従う（特に `Schema.res` の prop 検証と Hono の入力バリデーション）

## 7. 想定リスクと緩和策

| リスク | 緩和策 |
|-------|-------|
| Vite+ alpha が破壊的変更を入れる可能性 | バージョンを `^0.x` で柔軟にし、CHANGELOG 監視を README に明記 |
| `@inertiajs/react` の ReScript バインディングが薄い | `Inertia` モジュールを `@module` external として手書きで提供（薄いラッパーのみ） |
| Bun と Node で `@inertiajs/hono` の挙動差 | スタートアップ ReadMe で動作確認済みの Node / Bun 版を明記 |
| Inertia の middleware 順序ミスで真っ白画面 | サンプル Server.res のコメントで middleware 順序を明示 |
