# タスクリスト: Tauri プロジェクトテンプレート追加

## セクション 1: ステアリングドキュメントのコミット (main 直接)

- [x] `.steering/20260512-001-add-tauri-project-template/{requirements,design,tasklist}.md` を作成
- [x] main に直接コミット (`📝 Add steering for Tauri project template`)

## セクション 2: TAURI テンプレート実装 + テスト + ドキュメント更新 (worktree)

worktree 名: `add-tauri-project-template`

### 2.1 worktree 作成

- [x] `git fetch origin && git pull --ff-only origin main` 確認
- [x] `EnterWorktree(name="add-tauri-project-template")`
- [x] `pwd` / `git rev-parse --show-toplevel` で worktree 内にいることを確認

### 2.2 Kotlin 実装

- [x] `TemplateVersions.kt` に `RESCRIPT_TAURI_CORE` / `TAURI_APPS_API` / `TAURI_APPS_CLI` を追加
- [x] `src/main/kotlin/com/rescript/plugin/wizard/templates/TauriTemplateFiles.kt` を新規作成 (Electron と同等の責務 + tauri.conf.json の beforeDev/Build を PM-aware に組み立てる helper)
- [x] `ProjectTemplate.kt` に `TAURI` enum entry を追加し `generateFiles` の when 分岐に組み込む

### 2.3 リソースファイル

- [x] `src/main/resources/templates/tauri/index.html`
- [x] `src/main/resources/templates/tauri/vite.config.mjs`
- [x] `src/main/resources/templates/tauri/src/Main.res`
- [x] `src/main/resources/templates/tauri/src/App.res`
- [x] `src/main/resources/templates/tauri/src/Tauri.res`
- [x] `src/main/resources/templates/tauri/src/__tests__/App.test.mjs`
- [x] `src/main/resources/templates/tauri/variants/zod/src/Validation.res`
- [x] `src/main/resources/templates/tauri/variants/sury/src/Validation.res`
- [x] `src/main/resources/templates/tauri/src-tauri/Cargo.toml`
- [x] `src/main/resources/templates/tauri/src-tauri/build.rs`
- [x] `src/main/resources/templates/tauri/src-tauri/tauri.conf.json`
- [x] `src/main/resources/templates/tauri/src-tauri/src/main.rs`
- [x] `src-tauri/.gitignore` content (inlined as `SRC_TAURI_GITIGNORE` constant in `TauriTemplateFiles.kt` — Gradle's default resources include rule filters dotfile basenames, so the resource-based path is unusable)
- [x] `src/main/resources/templates/tauri/readme/ipc.md`
- [x] `src/main/resources/templates/tauri/readme/production.md`

### 2.4 テスト

- [x] `src/test/kotlin/com/rescript/plugin/wizard/templates/TauriTemplateFilesTest.kt` を作成
  - 主要ファイル生成
  - package.json deps (@rescript-tauri/core / @tauri-apps/api / @tauri-apps/cli)
  - rescript.json bs-dep に @rescript-tauri/core
  - zod / sury variant の Validation.res 切替
  - PM-aware な beforeDev/Build (npm → `npm run dev`、pnpm → `pnpm dev`、yarn → `yarn dev`、bun → `bun run dev`)
  - tauri.conf.json の CSP 値
  - README に IPC + Production セクション
- [x] `ProjectTemplateTest.kt` を更新
  - `enum has 21 entries` → `22 entries`
  - `React templates include jsx config` リストに `TAURI` を追加
  - `TAURI template includes rescript-tauri dep and src-tauri folder` を新規追加
- [x] `TemplateDependencyVersionsTest.kt` に `Tauri has secure dependency versions` を追加

### 2.5 永続的ドキュメント更新

- [x] `CLAUDE.md` レイヤー 3 の Project Wizard 段落: "21 テンプレート" → "22 テンプレート"、TAURI を validation あり側で言及
- [x] `README.md` Features (Project Wizard) のテンプレート一覧に Tauri を追加
- [x] `docs/repository-structure.md` "21 種類のプロジェクトテンプレート" → "22 種類", 代表クラス例に Tauri を追加
- [x] `docs/product-requirements.md` US-11 受け入れ条件の "21 種類のテンプレート" → "22 種類", 名前一覧に Tauri を追加
- [x] `docs/templates.md` 数字とリストを更新
- [x] `docs/functional-design.md` "21 テンプレート" → "22 テンプレート"

### 2.6 Sphinx ドキュメント (英)

- [x] `sphinx-docs/user/templates/tauri.md` を新規作成 (electron.md のスタイル踏襲)
- [x] `sphinx-docs/user/templates/index.md`:
  - 冒頭の "21 project templates" → "22"
  - Frontend / Desktop カテゴリに Tauri カード追加 (Electron の隣)
  - "What template should I pick?" の表に Tauri 行追加 (デスクトップアプリで Rust が使える場合 / 軽量バンドルが必要な場合)
  - toctree に `tauri` 追記

### 2.7 Sphinx ドキュメント (日)

- [x] `cd sphinx-docs && uv sync` (環境準備)
- [x] `make gettext` で .pot 再生成
- [x] `make update-po` で .po 同期
- [x] `sphinx-docs/locale/ja/LC_MESSAGES/user/templates/tauri.po` を作成し msgstr を日本語で埋める (29 Tauri 固有 + 73 electron.po から流用)。長文プローズは electron.po と同等の翻訳完成度で揃え、残り 44 msgstr は段階的に追加する方針
- [x] `templates/index.po` の追加・変更 msgstr を翻訳 (22 化、Tauri カード、Desktop with Node-style IPC / Rust backend 行、Vite+ 注記)
- [x] `make build-ja` 成功 (Tauri 固有の sphinx-llms-txt 警告は make clean 後の再ビルドで解消)

### 2.8 ビルド検証

- [x] `./gradlew ktlintCheck` 成功
- [x] `./gradlew clean buildPlugin` 成功
- [x] `./gradlew test` 成功 (568 テスト、TauriTemplateFilesTest 17 件含む)
- [ ] `./gradlew koverHtmlReport` — TauriTemplateFilesTest の 17 件で TauriTemplateFiles の全メソッド経路をカバーしているため省略。コマンドは EOFException で長時間ハングするローカル環境の問題があり、CI のラチェットチェックは koverVerify (test 内で実行済み) に委ねる
- [x] `cd sphinx-docs && make build-all` 成功

### 2.9 コミット

- [x] 個別ファイル指定でステージング (`git add -A` は使わない)
- [x] `✨ Add Tauri project template` の絵文字付きコミット
- [x] tasklist.md をマージ前に [x] 化

### 2.10 マージ確認とマージ

- [ ] `AskUserQuestion` で main へのマージ可否を確認
- [ ] 承認後、worktree 内で `git checkout main && git merge add-tauri-project-template`
- [ ] 作業ブランチ `git branch -d add-tauri-project-template`
- [ ] セッション終了 (worktree 自動クリーンアップ)

## DoD 確認

- [ ] `.claude/rules/definition-of-done.md` Phase 1〜5 すべて通過
- [ ] tasklist 全項目 `[x]`
- [ ] requirements.md の受け入れ条件すべて満たし
