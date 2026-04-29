# Requirements — 4 つの React フレームワークテンプレート追加

## 1. 背景

ReScript IntelliJ Plugin の Project Wizard には現在 16 種類のテンプレートが揃っているが、フロントエンドフレームワークは Vite+React / Next.js / Electron / React Native 系のみで、ここ数年で台頭した React エコシステムの新潮流（型志向のフルスタック / RSC / Islands アーキテクチャ）が含まれていない。並行して進行中の `feature/hono-inertia-template`（17 番目のテンプレート）とは独立に、本作業ではフロントエンドフレームワーク 4 種類を追加する。

## 2. 目的

Project Wizard に以下 4 種類を追加する:

| 追加テンプレート | カテゴリ | 主要価値 |
|---|---|---|
| **TanStack Start** | Frontend | Vite ベース・エンドツーエンド型安全・ReScript 型志向と相性最良 |
| **Remix / React Router v7 (Framework Mode)** | Frontend | 成熟した loader/action パターン・SSR・Web 標準志向 |
| **Astro (React Islands)** | Frontend | コンテンツ志向 SSG/SSR・React を Islands として埋め込み |
| **Waku (RSC-first)** | Frontend | Daishi Kato 製の最小 RSC フレームワーク・新興だが教育価値高 |

加えて、新テンプレートでは **Validation library (zod / sury) 選択 UI を無効化**する仕組みを導入する（用途的に主要バリデーションが薄いか、フレームワーク内に標準があるため）。

## 3. スコープ

### IN SCOPE

- `ProjectTemplate` enum に `supportsValidationSelection: Boolean = true` フィールド追加
- `RescriptProjectWizardStep` で選択中テンプレートに応じて Validation コンボの可視性を制御
- 4 件の新規 enum 値（`TANSTACK_START`, `REMIX_RR_V7`, `ASTRO`, `WAKU`）追加（すべて `supportsValidationSelection = false`）
- 4 件の `*TemplateFiles.kt` および対応するユニットテスト
- 4 件の静的リソース `src/main/resources/templates/<key>/`（variants/ サブディレクトリは持たない）
- `TemplateVersions.kt` への 4 フレームワーク関連バージョン定数追加
- ドキュメント更新:
  - `CLAUDE.md`（テンプレート数 16 → 20、Validation 例外を明記）
  - `docs/repository-structure.md`（テンプレート列挙）
  - `docs/product-requirements.md`（US-11 の 16 → 20）
  - `docs/templates.md`（一覧表）
  - `README.md`（Features セクション）
  - `sphinx-docs/user/templates/index.md` および 4 件の detail ページ（英）+ 対応 `.po`（日本語訳完全）
  - `plugin.xml` `<change-notes>` の Unreleased セクション

### OUT OF SCOPE

- 既存 16 テンプレートの zod/sury variants 構造の変更
- TanStack Router / Astro / Waku 用の包括的 ReScript バインディング公開（テンプレート同梱の最小バインディングのみ）
- 各フレームワークの advanced 機能（Astro Content Collections / Waku custom router / RR v7 future flags / TanStack Start middleware）のサンプル化
- `feature/hono-inertia-template` の 17 番目テンプレートとのマージ調整（独立に進行）
- Plugin Marketplace のスクリーンショット差し替え

## 4. ユーザーストーリー

### US-1: 型志向フルスタックを試したい開発者

**ReScript の型を活かしたフルスタック React 開発を試したい開発者として**、IDE の New Project ダイアログから「TanStack Start」を選ぶだけで、Server Functions と File-based Routing が動く雛形を得たい。

**受け入れ条件:**
- [ ] New Project ダイアログに「TanStack Start」が表示される
- [ ] 生成直後に `<package-manager> install && <package-manager> dev` が成功する
- [ ] ブラウザで開発サーバー URL を開くと TanStack Router がレンダリングしたページが表示される
- [ ] ReScript で書かれた React コンポーネントが genType 経由で TSX 側にエクスポートされている

### US-2: Remix の loader/action モデルを試したい開発者

**Web 標準志向の SSR フレームワークを試したい開発者として**、React Router v7 の Framework モードで loaders / actions を ReScript で書ける雛形を得たい。

**受け入れ条件:**
- [ ] New Project ダイアログに「Remix / React Router v7」が表示される
- [ ] 生成直後に `<package-manager> install && <package-manager> dev` が成功する
- [ ] `app/routes/home.tsx` から ReScript 製の loader と React コンポーネントが呼び出されている
- [ ] sourceRoots は `app/` を指す

### US-3: Astro Islands を ReScript で書きたい開発者

**コンテンツ中心サイトに React Islands を使いたい開発者として**、Astro の React integration を `client:load` で利用する雛形を得たい。

**受け入れ条件:**
- [ ] New Project ダイアログに「Astro」が表示される
- [ ] 生成直後に `<package-manager> install && <package-manager> dev` が成功する
- [ ] `src/pages/index.astro` から ReScript 製の React Islands コンポーネントが呼び出されている
- [ ] ブラウザで `/` を開くとボタンクリックで Counter が動く（Islands の hydration 確認）

### US-4: RSC を学びたい開発者

**React Server Components を素直に学びたい開発者として**、Waku の最小構成で Server Component と Client Component の境界を明確に学べる雛形を得たい。

**受け入れ条件:**
- [ ] New Project ダイアログに「Waku」が表示される
- [ ] 生成直後に `<package-manager> install && <package-manager> dev` が成功する
- [ ] Server Component が SSR レンダリングされ、Client Component が hydrate される
- [ ] `"use client"` 境界の意味を README で説明している

### US-5: Validation 不要なテンプレートで UI を出さない

**Validation を使わないフレームワーク用テンプレートを選んだ開発者として**、Wizard で Validation library 選択 UI が表示されないことで、不要な選択肢に惑わされたくない。

**受け入れ条件:**
- [ ] 既存 16 テンプレート（Basic / Vite+React 等）選択時は Validation コンボが表示される（既存挙動維持）
- [ ] 新 4 テンプレート選択時は Validation コンボが非表示（または disable）になる

## 5. 受け入れ条件（プロダクト全体）

- [ ] `./gradlew clean buildPlugin test` が成功する
- [ ] `./gradlew ktlintCheck` が成功する
- [ ] `./gradlew verifyPluginStructure` が成功する
- [ ] 生成された 4 テンプレートそれぞれが `pnpm install && pnpm dev` で起動する（手動検証）
- [ ] sphinx-docs の英 `.md` と日本語 `.po` が同期し、`make build-ja` が成功する
- [ ] plugin.xml `<change-notes>` の Unreleased セクションに新規エントリが追加されている
- [ ] テンプレート数を表示する全ドキュメントで「20」へ更新済み

## 6. 非機能要件

- 既存 16 テンプレートの動作・出力を変更しない（回帰テストで検証）
- Validation 選択 UI のフラグはデフォルト `true` で、既存 enum 値は宣言を一切触らない
- `TemplateVersions.kt` 経由で全バージョンを集中管理する
- セキュリティ要件は CLAUDE.md セキュリティセクションに従う

## 7. 想定リスクと緩和策

| リスク | 緩和策 |
|-------|-------|
| TanStack Start / Astro / Waku の ReScript バインディングが薄い | フレームワーク固有 API は `.tsx` 側で書き、ReScript 側は `@react.component` でエクスポートしたコンポーネントを呼ぶ構成に統一 |
| Astro `.astro` ファイルはプラグインの言語サポート対象外 | README で「`.astro` は VSCode/Astro 拡張で書き、`.res` はプラグインで書く」と明記 |
| Waku の `"use client"` 境界の手書き `.tsx` ラッパーが煩雑 | `readme/server-vs-client.md` で境界の書き方を例示 |
| 4 テンプレートの作業ボリュームが大きく、検証が後ろ倒しになる | テンプレート単位で 1 コミットに分け、各コミットで `./gradlew test` を必ず通す |
| Validation コンボ hide/disable のレイアウトジャンプ | 実装時に `isVisible` と `isEnabled` 両案を試し、IDE 表示で判断 |
