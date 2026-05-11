# タスクリスト: Tauri プロジェクトテンプレート追加

## セクション 1: ステアリングドキュメントのコミット (main 直接)

- [x] `.steering/20260512-001-add-tauri-project-template/{requirements,design,tasklist}.md` を作成
- [ ] main に直接コミット (`📝 Add steering for Tauri project template`)

## セクション 2: TAURI テンプレート実装 + テスト + ドキュメント更新 (worktree)

worktree 名: `add-tauri-project-template`

### 2.1 worktree 作成

- [ ] `git fetch origin && git pull --ff-only origin main` 確認
- [ ] `EnterWorktree(name="add-tauri-project-template")`
- [ ] `pwd` / `git rev-parse --show-toplevel` で worktree 内にいることを確認

### 2.2 Kotlin 実装

- [ ] `TemplateVersions.kt` に `RESCRIPT_TAURI_CORE` / `TAURI_APPS_API` / `TAURI_APPS_CLI` を追加
- [ ] `src/main/kotlin/com/rescript/plugin/wizard/templates/TauriTemplateFiles.kt` を新規作成 (Electron と同等の責務 + tauri.conf.json の beforeDev/Build を PM-aware に組み立てる helper)
- [ ] `ProjectTemplate.kt` に `TAURI` enum entry を追加し `generateFiles` の when 分岐に組み込む

### 2.3 リソースファイル

- [ ] `src/main/resources/templates/tauri/index.html`
- [ ] `src/main/resources/templates/tauri/vite.config.mjs`
- [ ] `src/main/resources/templates/tauri/src/Main.res`
- [ ] `src/main/resources/templates/tauri/src/App.res`
- [ ] `src/main/resources/templates/tauri/src/Tauri.res`
- [ ] `src/main/resources/templates/tauri/src/__tests__/App.test.mjs`
- [ ] `src/main/resources/templates/tauri/variants/zod/src/Validation.res`
- [ ] `src/main/resources/templates/tauri/variants/sury/src/Validation.res`
- [ ] `src/main/resources/templates/tauri/src-tauri/Cargo.toml`
- [ ] `src/main/resources/templates/tauri/src-tauri/build.rs`
- [ ] `src/main/resources/templates/tauri/src-tauri/tauri.conf.json`
- [ ] `src/main/resources/templates/tauri/src-tauri/src/main.rs`
- [ ] `src/main/resources/templates/tauri/src-tauri/.gitignore`
- [ ] `src/main/resources/templates/tauri/readme/ipc.md`
- [ ] `src/main/resources/templates/tauri/readme/production.md`

### 2.4 テスト

- [ ] `src/test/kotlin/com/rescript/plugin/wizard/templates/TauriTemplateFilesTest.kt` を作成
  - 主要ファイル生成
  - package.json deps (@rescript-tauri/core / @tauri-apps/api / @tauri-apps/cli)
  - rescript.json bs-dep に @rescript-tauri/core
  - zod / sury variant の Validation.res 切替
  - PM-aware な beforeDev/Build (npm → `npm run dev`、pnpm → `pnpm dev`、yarn → `yarn dev`、bun → `bun run dev`)
  - tauri.conf.json の CSP 値
  - README に IPC + Production セクション
- [ ] `ProjectTemplateTest.kt` を更新
  - `enum has 21 entries` → `22 entries`
  - `React templates include jsx config` リストに `TAURI` を追加
  - `TAURI template includes rescript-tauri dep and src-tauri folder` を新規追加
- [ ] `TemplateDependencyVersionsTest.kt` に `Tauri has secure dependency versions` を追加

### 2.5 永続的ドキュメント更新

- [ ] `CLAUDE.md` レイヤー 3 の Project Wizard 段落: "21 テンプレート" → "22 テンプレート"、TAURI を validation あり側で言及
- [ ] `README.md` Features (Project Wizard) のテンプレート一覧に Tauri を追加
- [ ] `docs/repository-structure.md` "21 種類のプロジェクトテンプレート" → "22 種類", 代表クラス例に Tauri を追加
- [ ] `docs/product-requirements.md` US-11 受け入れ条件の "21 種類のテンプレート" → "22 種類", 名前一覧に Tauri を追加
- [ ] `docs/templates.md` 数字とリストを更新
- [ ] `docs/functional-design.md` "21 テンプレート" → "22 テンプレート"

### 2.6 Sphinx ドキュメント (英)

- [ ] `sphinx-docs/user/templates/tauri.md` を新規作成 (electron.md のスタイル踏襲)
- [ ] `sphinx-docs/user/templates/index.md`:
  - 冒頭の "21 project templates" → "22"
  - Frontend / Desktop カテゴリに Tauri カード追加 (Electron の隣)
  - "What template should I pick?" の表に Tauri 行追加 (デスクトップアプリで Rust が使える場合 / 軽量バンドルが必要な場合)
  - toctree に `tauri` 追記

### 2.7 Sphinx ドキュメント (日)

- [ ] `cd sphinx-docs && uv sync` (環境準備)
- [ ] `make gettext` で .pot 再生成
- [ ] `make update-po` で .po 同期
- [ ] `sphinx-docs/locale/ja/LC_MESSAGES/user/templates/tauri.po` を作成し msgstr を日本語で埋める
- [ ] `templates/index.po` の追加・変更 msgstr を翻訳
- [ ] その他差分が出た .po (`product-requirements.md` 等は対象外。templates 配下を中心に) の msgstr を翻訳
- [ ] `make build-ja` で警告ゼロを確認

### 2.8 ビルド検証

- [ ] `./gradlew ktlintCheck` 成功
- [ ] `./gradlew clean buildPlugin` 成功
- [ ] `./gradlew test` 成功 (TauriTemplateFilesTest 含む全テスト)
- [ ] `./gradlew koverHtmlReport` でカバレッジを確認 (TauriTemplateFiles が十分にカバーされている)
- [ ] `cd sphinx-docs && make build-all` 成功

### 2.9 コミット

- [ ] 個別ファイル指定でステージング (`git add -A` は使わない)
- [ ] `✨ Add Tauri project template` の絵文字付きコミット
- [ ] tasklist.md をマージ前に [x] 化

### 2.10 マージ確認とマージ

- [ ] `AskUserQuestion` で main へのマージ可否を確認
- [ ] 承認後、worktree 内で `git checkout main && git merge add-tauri-project-template`
- [ ] 作業ブランチ `git branch -d add-tauri-project-template`
- [ ] セッション終了 (worktree 自動クリーンアップ)

## DoD 確認

- [ ] `.claude/rules/definition-of-done.md` Phase 1〜5 すべて通過
- [ ] tasklist 全項目 `[x]`
- [ ] requirements.md の受け入れ条件すべて満たし
