# Tauri プロジェクトテンプレート追加

## 背景

ユーザー（プロジェクト作者）が npm に公開した `@rescript-tauri/*` パッケージ群 (`@rescript-tauri/core` v0.1.0 ほか 9 プラグイン) を使って、Project Wizard から Tauri 2.x デスクトップアプリの雛形を生成できるようにする。

参考:
- npm 一覧: https://www.npmjs.com/~nagatatz?activeTab=packages
- リポジトリ: https://github.com/Nagatatz/rescript-tauri
- 既存例: `examples/hello-world` (vanilla DOM + `Core.Raw.invoke`)

## ユースケース

ReScript で Tauri デスクトップアプリを始めたい開発者が、IDE の New Project ダイアログから Tauri テンプレートを選び、

- `pnpm install && pnpm tauri dev` でいきなり起動できる
- `@rescript-tauri/core` 経由で型付き IPC が書けるベース実装が入っている
- Electron テンプレートと同等の品質 (README、CI、Validation、LICENSE 等が揃う)

を満たす雛形を 1 クリックで取得できる。

## スコープに含めるもの

1. `ProjectTemplate.TAURI` enum エントリ (DESKTOP カテゴリ、validation 選択あり)
2. `TauriTemplateFiles` ジェネレーターと対応する resource ファイル群
   - `src/main/resources/templates/tauri/` 配下
   - フロントエンド: ReScript + React + Vite+
   - Rust バックエンド: `tauri = "2"` の最小構成と `greet` / `get_info` コマンド
   - `@rescript-tauri/core` 経由で IPC を呼び出すサンプル
   - zod / sury variant の `Validation.res` で IPC レスポンスを検証
3. CLAUDE.md / README.md / docs/repository-structure.md / docs/product-requirements.md / docs/templates.md / docs/functional-design.md の更新 (21 → 22)
4. sphinx-docs/user/templates/tauri.md と templates/index.md のカード追加
5. JA `.po` 翻訳の同時更新 (`make gettext` → `make update-po` → 手動翻訳 → `make build-ja`)
6. テスト
   - `TauriTemplateFilesTest`
   - `ProjectTemplateTest` の 21→22 検証
   - `TemplateDependencyVersionsTest` に Tauri セクション
   - `TemplateResourcesSmokeTest` の placeholder 集合更新が不要な命名にする (既存 `projectName` のみ使う)

## スコープ外

- Tauri プラグイン (`@rescript-tauri/plugin-*`) の同梱 → README で言及するに留める
- macOS 署名・公証・auto-updater 等のリリース配信機構 → README の Production Bundling セクションで概略のみ
- 実際のアイコン PNG/ICO/ICNS の同梱 → `tauri icon` コマンドで生成する手順を README に書く
- React 以外のフレームワーク (Solid、Svelte 等) 対応 → 将来検討

## 制約

- 既存テンプレートの命名規則・ファイル構成・CommonFiles ユーティリティを最大限再利用する
- Tauri 2.x のみサポート (Tauri 1.x は対象外)
- Rust toolchain は CI ではインストールしない (renderer の `vp build` と `vitest` のみ CI で実行)
- Tauri Rust 側 `src-tauri/` は構造のみ生成 (CI でビルドしない)

## 受け入れ条件

- [ ] Wizard で Tauri を選び pnpm/npm/yarn/bun のいずれかで `<pm> install` した後、`<pm> tauri dev` で起動できる雛形が生成される (手動検証は worktree 上で 1 度だけ)
- [ ] `./gradlew ktlintCheck clean buildPlugin test koverHtmlReport verifyPluginStructure` がすべて成功
- [ ] `cd sphinx-docs && make build-all` が成功 (英 + 日 両方)
- [ ] `ProjectTemplate.entries.size == 22`
- [ ] CLAUDE.md / README.md / docs 系の "21" 記述が "22" に更新されている
- [ ] tasklist.md の全項目が `[x]` になっている

## 関連ファイル

- `src/main/kotlin/com/rescript/plugin/wizard/ProjectTemplate.kt`
- `src/main/kotlin/com/rescript/plugin/wizard/templates/ElectronTemplateFiles.kt` (参考実装)
- `src/main/kotlin/com/rescript/plugin/wizard/templates/TemplateVersions.kt`
- `src/main/resources/templates/electron/` (参考リソース配置)
- `src/test/kotlin/com/rescript/plugin/wizard/ProjectTemplateTest.kt`
- `src/test/kotlin/com/rescript/plugin/wizard/templates/ElectronTemplateFilesTest.kt`
