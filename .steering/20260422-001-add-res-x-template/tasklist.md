# タスクリスト: res-x Project Template

## Phase 1: セットアップ

- [x] `.steering/20260422-001-add-res-x-template/` を作成し requirements.md / design.md を執筆
- [x] `feature/res-x-template` ブランチを `main` から作成

## Phase 2: 実装

### TemplateVersions

- [x] `TemplateVersions.kt` に `RESCRIPT_X`, `RESCRIPT_BUN`, `HTMX_CDN` 定数を追加

### 静的リソース（`src/main/resources/templates/res-x/`）

- [x] `rescript.json` — jsx.module=Hjsx / bs-dependencies / bsc-flags を手書き（builder 拡張を避けるため）
- [x] `src/App.res` — エントリ（Bun.serve + res-x Handler 組立）
- [x] `src/Handler.res` — res-x Handler 初期化
- [x] `src/Layout.res` — HTML シェル + HTMX script
- [x] `src/Counter.res` — Counter コンポーネント + hx-post ハンドラ
- [x] `src/TodoForm.res` — Todo フォーム + hx-post + validation 呼び出し
- [x] `src/__tests__/App.test.mjs` — Vitest スモークテスト
- [x] `vite.config.js` — res-x Vite プラグイン
- [x] `readme/app.md` — アプリ概要
- [x] `readme/htmx.md` — HTMX 利用方法
- [x] `readme/project-layout.md` — ファイルツリー
- [x] `variants/zod/src/Validation.res` — zod パーサ
- [x] `variants/sury/src/Validation.res` — sury パーサ

**設計変更メモ**: `ResX.res` / `BunServe.res` はバインディング npm パッケージ (`rescript-x`, `rescript-bun`) が提供するため生成しない。`rescript.json` の `bsc-flags` で `-open ResX.Globals` / `-open RescriptBun.Globals` して直接参照する。

### Kotlin 生成器

- [x] `wizard/templates/ResXTemplateFiles.kt` を `HonoTemplateFiles.kt` パターンで実装
- [x] `wizard/ProjectTemplate.kt` に `RES_X` enum エントリと when 分岐を追加

### テスト

- [x] `src/test/.../wizard/templates/ResXTemplateFilesTest.kt` を作成（14 件のテストケース）
- [x] `ProjectTemplateTest.kt` の enum count を 15→16 に更新
- [x] `TemplateResourcesSmokeTest.kt` の knownPlaceholders に `name`/`htmxVersion`/`validationLib` を追加

## Phase 3: ドキュメント更新

- [x] `README.md` の templates count 15→16 とテンプレート一覧に res-x 追加
- [x] `CLAUDE.md` の wizard 段落を更新（サーバー系 8→9）
- [x] `docs/templates.md` のテーブルに行 16 追加・グループ別注記を更新
- [x] `sphinx-docs/user/templates/index.md` に res-x カード追加・選択表にも追加
- [x] `sphinx-docs/locale/ja/LC_MESSAGES/user/templates/index.po` を同期し `make build-ja` が通ることを確認

## Phase 4: コミット前検証（DoD Phase 3）

- [x] `./gradlew ktlintCheck` 成功
- [x] `./gradlew test` 成功（ResXTemplateFilesTest 含め全テスト）
- [x] `./gradlew buildPlugin verifyPluginStructure` 成功
- [x] 新規 `.kt` ファイルに KDoc が付与されている
- [x] 新規 `.kt` ファイルに対応する `*Test.kt` が存在する

## Phase 5: コミット

- [x] コミット 1: `🔧 Add TemplateVersions constants for res-x`
- [x] コミット 2: `✨ Add res-x (HTMX on Bun) project template`
- [x] コミット 3: `📝 Document res-x template across README, CLAUDE, and docs`
- [x] コミット 4: `📝 Add steering docs for res-x template work`

## Phase 6: マージ

- [x] `AskUserQuestion` でマージ可否をユーザー確認
- [x] `main` に merge、`feature/res-x-template` ブランチを削除
