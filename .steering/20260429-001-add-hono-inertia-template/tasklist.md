# Tasklist — Hono + Inertia (React) テンプレート追加

ブランチ: `feature/hono-inertia-template`（worktree で作業）

## Phase 0: 事前調査（実装前の最新版確認）

- [x] `@hono/inertia` v0.2.0 確認（`inertia()` middleware、`c.render(component, props)` でレスポンス、`@hono/inertia/vite` の `inertiaPages()` プラグイン）
- [x] `@inertiajs/react` v3.0.3 確認（v2 ではない、React 19 peer、`createInertiaApp` の `resolve` / `setup`、`usePage` / `Link` あり）
- [x] Vite+ 確認（npm `vite-plus@^0.1.19`、bin は `vp`、CLI: `vp dev` / `vp build` / `vp test` / `vp check`、設定キーは未公開のためデフォルト依存）
- [x] ReScript 12+ の動的 import: `Js.import` は静的解決のみ → JS シム (`import.meta.glob`) が必要と判明
- [x] design.md 反映済み（バージョン、API 名、JS シム追加）

## Phase 1: ブランチ準備

- [x] `EnterWorktree` で `worktree-hono-inertia-template` worktree を作成
- [x] steering ドキュメントを worktree にコピー

## Phase 2: Kotlin 実装

### 2.1 TemplateVersions.kt

- [x] `HONO_INERTIA = "^0.2.0"`、`INERTIA_REACT = "^3.0.3"` 定数を追加（Vite+ / React は既存定数を再利用）
- [x] 既存の Hono 関連定数のすぐ近くに配置

### 2.2 ProjectTemplate.kt

- [x] `HONO_INERTIA` enum 値を `HONO_GRAPHQL` の直後に追加
- [x] DisplayName: `"Hono + Inertia (React)"`、説明、カテゴリ FULL_STACK、sourceRoots を設定
- [x] `generateFiles()` の `when` 分岐に `HONO_INERTIA -> HonoInertiaTemplateFiles.generate(ctx)` を追加

### 2.3 HonoInertiaTemplateFiles.kt（新規）

- [x] `src/main/kotlin/com/rescript/plugin/wizard/templates/HonoInertiaTemplateFiles.kt` を新規作成
- [x] KDoc を英語で記述（責務、variants 挙動）
- [x] `generate(ctx: TemplateContext): Map<String, String>` を実装
- [x] `vp dev` / `vp build` / `vp test` / `vp check` scripts、deps に `@hono/inertia` / `@inertiajs/react` を追加
- [x] README 各 extraSections を組み立て
- [x] `TemplateResourceLoader.load("hono-inertia/...")` で静的ファイルを取り込み
- [x] variants 適用: `hono-inertia/variants/${variantKey}/src/Validation.res` をロード

## Phase 3: リソース実装

### 3.1 共通設定ファイル

- [x] `src/main/resources/templates/hono-inertia/vite.config.mjs`（Vite+ デフォルト + plugins のみ）
- [x] `src/main/resources/templates/hono-inertia/drizzle.config.ts`
- [x] `src/main/resources/templates/hono-inertia/index.html`（Inertia ホスト HTML）
- 注: rescript.json / tsconfig.json / .gitignore は ProjectFileBuilders / CommonFiles で動的生成するため不要

### 3.2 サーバー側 ReScript ファイル

- [x] `src/main/resources/templates/hono-inertia/src/Server.res`（Hono + Inertia middleware 配線、middleware 順序のコメント明示）
- [x] `src/main/resources/templates/hono-inertia/src/ServerMain.res`
- [x] `src/main/resources/templates/hono-inertia/src/Routes.res`（/, /about, POST /greet サンプル）
- [x] `src/main/resources/templates/hono-inertia/src/Schema.res`（posts テーブル）
- [x] `src/main/resources/templates/hono-inertia/src/Logger.res`
- [x] `src/main/resources/templates/hono-inertia/src/HonoInertia.res`（@hono/inertia バインディング）
- 注: Db.res は common/db/Db.res を再利用、Hono.res / HonoNodeServer.res は ProjectFileBuilders で動的生成

### 3.3 クライアント側 ReScript ファイル

- [x] `src/main/resources/templates/hono-inertia/src/InertiaBindings.res`（@inertiajs/react バインディング）
- [x] `src/main/resources/templates/hono-inertia/src/client/Main.res`（createInertiaApp エントリ）
- [x] `src/main/resources/templates/hono-inertia/src/client/pages.js`（import.meta.glob シム）
- [x] `src/main/resources/templates/hono-inertia/src/client/MainLayout.res`
- [x] `src/main/resources/templates/hono-inertia/src/client/Pages/Home.res`
- [x] `src/main/resources/templates/hono-inertia/src/client/Pages/About.res`

### 3.4 テストとドキュメント

- [x] `src/main/resources/templates/hono-inertia/src/__tests__/Server.test.mjs`
- [x] `src/main/resources/templates/hono-inertia/readme/api.md`
- [x] `src/main/resources/templates/hono-inertia/readme/frontend.md`
- [x] `src/main/resources/templates/hono-inertia/readme/project-layout.md`
- [x] `src/main/resources/templates/hono-inertia/readme/viteplus.md`（Vite+ サブコマンド一覧と alpha 注意喚起）

### 3.5 variants

- [x] `src/main/resources/templates/hono-inertia/variants/zod/src/Validation.res`
- [x] `src/main/resources/templates/hono-inertia/variants/sury/src/Validation.res`

## Phase 4: ユニットテスト

### 4.1 新規テスト

- [x] `src/test/kotlin/com/rescript/plugin/wizard/templates/HonoInertiaTemplateFilesTest.kt` を作成（17 ケース）
- [x] 必須ファイル生成検証
- [x] `vitest` / `eslint` / `prettier` が package.json に **含まれない** ことを検証
- [x] Zod / Sury の variants 切り替えを検証
- [x] `vp dev` / `vp build` / `vp test` / `vp check` scripts を検証
- [x] `@hono/inertia` / `@inertiajs/react` / Vite+ trio (vite/vite-plus/@voidzero-dev/vite-plus-core) を検証
- [x] middleware 順序検証（inertia() が Routes 登録より前）

### 4.2 既存テスト更新

- [x] `ProjectTemplateTest.kt` のテンプレート数 `16` → `17` を更新
- [x] React templates jsx 検証リストに HONO_INERTIA を追加
- [x] HONO_INERTIA template surfaces the Inertia middleware and Vite+ scripts テストを追加

## Phase 5: ドキュメント更新

- [x] `CLAUDE.md` レイヤー 3 の Project Wizard セクションを「全 17 テンプレート」に更新、Hono + Inertia エントリを追加
- [x] `README.md` Features セクションの Project Wizard 一覧に追加
- [x] `docs/repository-structure.md` の wizard/templates 説明でテンプレート数 16 → 17
- [x] `docs/templates.md` のテーブル・分類・Vite+ 注記を更新
- [x] `sphinx-docs/user/templates/hono-inertia.md` を新規作成（テンプレート専用ページ）
- [x] `sphinx-docs/user/templates/index.md` のグリッドカード・FAQ 表・toctree・Vite+ 注記に Hono + Inertia を追加
- [x] `sphinx-docs/locale/ja/LC_MESSAGES/**/*.po` を `make gettext && make update-po` で再生成
- [x] `hono-inertia.po`（139 msgid）と `index.po` の差分を日本語で埋める
- [x] `make build-ja` が成功することを確認
- [-] `src/main/resources/META-INF/plugin.xml` の `<change-notes>` 更新は **次回リリースコミット時に実施**（`.claude/rules/release.md` の方針: change-notes は version bump コミットに同梱）

## Phase 6: コミット前検証（DoD Phase 3）

- [x] `./gradlew ktlintCheck` 成功
- [x] `./gradlew clean buildPlugin` 成功
- [x] `./gradlew test` 成功（ProjectTemplateTest + HonoInertiaTemplateFilesTest 含む全テスト）
- [x] ビルド警告が新たに増加していないことを確認
- [x] Deprecated API 利用なし（新規実装は `@Suppress` 不要）
- [x] 新規 `.kt`（`HonoInertiaTemplateFiles.kt`）に KDoc 付与
- [x] 新規 `.kt` に対応するテスト（`HonoInertiaTemplateFilesTest.kt`）あり
- [x] テンプレートに LSP / 外部プロセス起動なし、`ProcessBuilder` 不使用、絶対パス露出なし

## Phase 7: コミット

最終的なコミット分割（コンパイル単位の整合性を優先）:

- [x] 🔧 Pin hook commands to `$CLAUDE_PROJECT_DIR`（独立した settings 修正、CWD ドリフトでフックが失敗していた件の根治）
- [x] ✨ Add Hono + Inertia (React) project wizard template（Kotlin enum/object/versions + 全リソース + 新規/既存テスト = 27 ファイル）
- [x] 📝 Document Hono + Inertia template across docs（CLAUDE/README/repo-structure/templates + sphinx 英 .md + ja .po 同期）
- [x] 📝 Add Hono + Inertia template steering documents（最終コミット、tasklist 完了状態を含む）

各コミット時:
- [x] 個別ファイル名で `git add`（`-A` / `.` を使わない）
- [x] ktlint hook 通過、JSON 妥当性確認

## Phase 8: 手動検証（任意だが推奨）

- [ ] `./gradlew runIde` で IDE 起動
- [ ] New Project → ReScript → Hono + Inertia を選択して生成
- [ ] 生成プロジェクトで `<pm> install && <pm> dev` が成功
- [ ] ブラウザで `http://localhost:5173` を開いて Inertia 経由で React ページが表示される
- [ ] Zod variant / Sury variant 双方を生成して `vite build` を確認

> 手動検証はオプション。実装時間/環境の都合でスキップする場合は本セクションを `[~] skipped: 理由` に書き換える。

## Phase 9: マージ準備

- [ ] requirements.md の全受け入れ条件を満たしていることを確認
- [ ] tasklist.md の全タスクが `[x]` であることを確認（マージタスク自体を含む）
- [x] `AskUserQuestion` でユーザーにマージ可否を確認
- [ ] セキュリティに影響する変更がある場合、その旨を明示

## Phase 10: マージ実行

- [x] worktree 内で `git checkout main && git merge feature/hono-inertia-template`
- [x] `git branch -d feature/hono-inertia-template`
- [x] セッション終了（worktree クリーンアップは自動）
