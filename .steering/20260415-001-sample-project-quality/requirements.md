# 要求内容: サンプルプロジェクト品質改善

## 背景

ReScript IntelliJ Plugin の Project Wizard は 12 種類のテンプレート (`Basic` / `Vite + React` / `Next.js` / `Electron` / `Hono` / `Cloudflare Workers` / `AWS Lambda` / `Google Cloud Run` / `React Native` / `npm Library` / `CLI Tool` / `Monorepo`) を提供している。しかし各テンプレートの実装 (17〜123 行) は最小雛形の域を出ず、以下の課題がある:

- **動作保証の欠如**: 生成物が実際に `rescript build` や JS ビルドを通るかが検証されていない。依存バージョンの陳腐化や設定不整合が発覚しない。
- **コンテンツの薄さ**: README・`.gitignore`・`.editorconfig`・テスト雛形など「そのまま開発に入れる」ためのファイルが欠けている。
- **依存バージョンの散在**: `rescript` / `@rescript/core` / `react` / `hono` 等のバージョンが各テンプレートにハードコードされ、一括更新が困難。
- **ベストプラクティスの未反映**: `@rescript/core` 導入・JSX v4 automatic runtime・genType 設定・pnpm workspaces など現代的な構成が取り込まれていない。
- **パッケージマネージャ選択の未反映**: ウィザードに `PackageManager` 選択 UI は存在するが、生成テンプレートが `npm` 前提で書かれており選択値が反映されない。

## 目的

ウィザードから作成できるサンプルプロジェクトを「そのままクローンして開発を始められる」品質に引き上げる。

## スコープ

### Phase 1: 動作検証の自動化

- 各テンプレートを一時ディレクトリに生成し、`pnpm install` と `rescript build` (必要に応じて対応ツールの `build`) が通ることを自動検証する統合テストを追加する。
- 統合テストは **CI メインワークフローには含めず**、別の `integration-tests.yml` で手動トリガー（`workflow_dispatch`）と nightly スケジュールで実行する。
- 統合テストは `@Tag("integration")` で分類し、通常の `./gradlew test` では除外される。

### Phase 2: コンテンツ充実

全 12 テンプレートに以下を追加・強化する:

- `README.md` — プロジェクト概要、セットアップ手順、使用コマンド、推奨エディタ設定
- `.gitignore` — `node_modules/`, `lib/`, `.DS_Store`, コンパイル済み JS 等の除外
- `.editorconfig` — インデント・改行コードの統一
- テスト雛形 — Vitest を前提としたサンプルテストファイル (CLI/Library/Backend 系)
- `rescript.json` の現代化 — `suffix: ".res.js"`, JSX 設定は React 系のみ
- GitHub Actions ワークフロー例 — ビルド & テストの最小構成 (`.github/workflows/ci.yml`)

### Phase 3: 依存バージョン集約

- `TemplateVersions.kt` を新設し、`rescript`, `@rescript/core`, `react`, `react-dom`, `hono`, `vite`, `vite-plus`, `vitest`, `@types/*` 等のバージョンを定数として一元管理する。
- 全テンプレートから参照するように変更する。

### Phase 4: ベストプラクティス反映 + ビルドツール刷新

- **Vite → Vite+ 切り替え**: `Vite + React`, `Electron`, `Monorepo` (client 部分) のビルドツールを Vite+ (`vite-plus`) に切り替える。`vp dev` / `vp build` / `vp test` を標準コマンドとして採用する。ただし、Vite+ は 1.0 未リリースのため、README に注意書きを添える。
- **`@rescript/core` を全テンプレで標準化** (`rescript.json` の `bs-dependencies` に追加、`open RescriptCore` を設定)。
- **JSX v4 automatic runtime** を React 系で採用 (`rescript.json` の `jsx: { version: 4, mode: "automatic" }`)。
- **pnpm workspaces** を Monorepo で採用 (`pnpm-workspace.yaml`)。
- **genType 設定** を Library テンプレで標準提供。

### 追加: PackageManager 選択値の反映

- ウィザードのデフォルト選択を `NPM` → `PNPM` に変更する。
- `PackageManager` 選択値を各テンプレートの生成内容に反映する:
  - `package.json` の `packageManager` フィールドに `pnpm@<バージョン>` 等を出力
  - README のコマンド表記 (`npm install` / `pnpm install` / `yarn`) を選択値に応じて切り替え
  - Monorepo テンプレは pnpm 選択時のみ `pnpm-workspace.yaml` を出力、npm/yarn 選択時は `workspaces` フィールドを `package.json` に出力

## 非スコープ

- プロジェクトテンプレートの新規追加（Vite+ を独立テンプレとして追加するのではなく、既存 Vite テンプレを Vite+ に置換する方針）
- ウィザード UI の大幅なレイアウト変更
- Vite+ 以外のツールチェーン変更 (Rspack, Rolldown 単独利用等)

## 受け入れ条件

### Phase 1
- [ ] `.github/workflows/integration-tests.yml` が作成され、`workflow_dispatch` と nightly cron で起動する
- [ ] 統合テストは 12 テンプレートすべてについて「生成 → `pnpm install` → `rescript build`」を検証する
- [ ] React 系テンプレはさらに `pnpm build` (Vite+ ビルド) を検証する
- [ ] 統合テストは `./gradlew integrationTest` で手動起動可能
- [ ] 通常の `./gradlew test` では統合テストは実行されない

### Phase 2
- [ ] 全テンプレートに `README.md`, `.gitignore`, `.editorconfig` が含まれる
- [ ] CLI/Library/Backend テンプレは Vitest のサンプルテストを含む
- [ ] React 系テンプレはコンポーネントの Vitest サンプルを含む
- [ ] 全テンプレートに `.github/workflows/ci.yml` が含まれる

### Phase 3
- [ ] `TemplateVersions.kt` が作成され、全テンプレートから参照される
- [ ] テンプレート内でのバージョンリテラルのハードコードが消滅する

### Phase 4
- [ ] `Vite + React`, `Electron`, `Monorepo` のビルドツールが Vite+ になる
- [ ] `rescript.json` に `@rescript/core` が追加される
- [ ] React 系で JSX v4 automatic runtime が設定される
- [ ] Monorepo で `pnpm-workspace.yaml` が生成される

### 追加
- [ ] ウィザードのデフォルト選択が `PNPM` になる
- [ ] 生成される `package.json` の `packageManager` フィールドが選択値に応じて変わる
- [ ] README のコマンド表記が選択値に応じて切り替わる

### 共通
- [ ] 全機能に対応するユニットテスト (`TemplateFilesTest.kt` 系) が更新され、新しい生成内容を検証する
- [ ] `./gradlew ktlintCheck buildPlugin test` がすべて成功する
- [ ] CLAUDE.md / README.md / sphinx-docs / product-requirements.md が更新される

## セキュリティ考慮事項

- テンプレートで生成される GitHub Actions ワークフローは、信頼できる action のみを `uses` で参照する (`actions/checkout@v4`, `actions/setup-node@v4`, `pnpm/action-setup@v4`)。
- 統合テストで `pnpm install` を実行するため、CI の npm レジストリアクセスに注意する。リモートコードは信頼済みパッケージのみに限定する。
