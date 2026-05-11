# Tasklist — 4 つの React フレームワークテンプレート追加

ブランチ: `feature/add-react-templates`（worktree で作業）

## Phase 0: 事前調査

- [x] `feature/hono-inertia-template` ブランチが並行進行中だが本作業とは独立であることを確認
- [x] 既存 16 テンプレートの enum / TemplateFiles 構造を把握（ProjectTemplate.kt, NextjsTemplateFiles.kt 参照）
- [x] Validation library 選択 UI の現状実装を把握（RescriptProjectWizardStep.kt 46-52, 129-137）
- [x] 各フレームワーク (TanStack Start / Remix RR v7 / Astro / Waku) の最小起動構成を `/plan` で確定

## Phase 1: ブランチ準備

- [x] `EnterWorktree` で worktree 作成し `feature/add-react-templates` で作業開始

## Phase 2: 基盤 — Validation 選択フラグ

### 2.1 ProjectTemplate.kt

- [x] `supportsValidationSelection: Boolean = true` フィールドを enum コンストラクタに追加
- [x] 既存 16 件の宣言を一切触らない（デフォルト値で吸収）

### 2.2 RescriptProjectWizardStep.kt

- [x] `templateList.addListSelectionListener` 内で `supportsValidationSelection` に応じて `validationLibraryLabel.isVisible` / `validationLibraryCombo.isVisible` を切り替え
- [x] 既存 PackageManager UI には影響なし

### 2.3 ProjectTemplateTest.kt

- [x] 既存 16 件の `supportsValidationSelection` がデフォルト `true` であることを assert
- [x] enum エントリ数の検証（後続 Phase で 17 → 18 → 19 → 20 に更新）

### 2.4 検証 + コミット

- [x] `./gradlew ktlintCheck buildPlugin test` 成功
- [x] コミット: `✨ Add validation selection toggle to ProjectTemplate`

## Phase 3: TanStack Start テンプレート

### 3.1 TemplateVersions.kt

- [x] TanStack Start / TanStack Router / Vite 関連バージョン定数追加

### 3.2 ProjectTemplate.kt

- [x] `TANSTACK_START` enum 値を `RES_X` の後ろに追加（`supportsValidationSelection = false`, `sourceRoots = listOf("app")`）
- [x] `generateFiles()` に dispatch 追加

### 3.3 TanstackStartTemplateFiles.kt

- [x] 新規作成（KDoc 付き）
- [x] `generate(ctx)` 実装、CommonFiles + 静的リソースロード

### 3.4 リソースファイル `src/main/resources/templates/tanstack-start/`

- [x] `vite.config.ts`
- [x] `tsconfig.json`, `rescript-modules.d.ts`
- [x] `app/router.tsx`, `app/routes/__root.tsx`, `app/routes/index.tsx`
- [x] `app/components/Greeting.res`
- [x] `app/server/Greet.res`
- [x] `app/__tests__/Greeting.test.mjs`
- [x] `readme/server-functions.md`, `readme/file-routing.md`
- [x] `.gitignore` 用エントリ（`dist/`, `.tanstack/`, `.output/`）

### 3.5 TanstackStartTemplateFilesTest.kt

- [x] 生成ファイル数・必須ファイル存在の検証
- [x] package.json の必須 deps + scripts 検証
- [x] フレームワーク固有: `__root.tsx` の `createRootRoute` 確認

### 3.6 ドキュメント

- [x] CLAUDE.md, README.md, docs/repository-structure.md, docs/templates.md, docs/product-requirements.md 更新（4 件分まとめて Phase 6 で OK だが、テンプレート数の最終形 20 を意識）
- [x] sphinx-docs/user/templates/tanstack-start.md 新規作成
- [x] sphinx-docs/user/templates/index.md にカード追加
- [x] `make gettext && make update-po` 実行 + `.po` の `msgstr` 日本語充填
- [x] `make build-ja` 成功

### 3.7 検証 + コミット

- [x] `./gradlew ktlintCheck buildPlugin test` 成功
- [x] コミット: `✨ Add TanStack Start project template`

## Phase 4: Remix / React Router v7 テンプレート

### 4.1 TemplateVersions.kt

- [x] React Router v7 関連バージョン定数追加

### 4.2 ProjectTemplate.kt

- [x] `REMIX_RR_V7` enum 値追加（`sourceRoots = listOf("app")`, `supportsValidationSelection = false`）
- [x] `generateFiles()` dispatch 追加

### 4.3 RemixV7TemplateFiles.kt + リソース + テスト

- [x] Kotlin ファイル新規（KDoc 付き）
- [x] `src/main/resources/templates/remix-v7/` 配下に必要ファイル群
  - `vite.config.ts`, `react-router.config.ts`, `tsconfig.json`, `rescript-modules.d.ts`
  - `app/root.tsx`, `app/routes.ts`, `app/routes/home.tsx`
  - `app/components/Greet.res`, `app/loaders/HomeLoader.res`
  - `app/__tests__/Greet.test.mjs`
  - `readme/loaders-actions.md`, `readme/file-routing.md`
- [x] テスト: 生成ファイル数 + 必須 deps + `routes.ts` の `index` route 検証

### 4.4 ドキュメント

- [x] sphinx-docs/user/templates/remix-v7.md 新規 + index.md カード追加
- [x] `.po` 同期と日本語充填

### 4.5 検証 + コミット

- [x] `./gradlew ktlintCheck buildPlugin test` 成功
- [x] コミット: `✨ Add Remix / React Router v7 project template`

## Phase 5: Astro テンプレート

### 5.1 TemplateVersions.kt

- [x] Astro / @astrojs/react / @astrojs/node バージョン定数追加

### 5.2 ProjectTemplate.kt

- [x] `ASTRO` enum 値追加
- [x] `generateFiles()` dispatch 追加

### 5.3 AstroTemplateFiles.kt + リソース + テスト

- [x] Kotlin ファイル新規（KDoc 付き）
- [x] `src/main/resources/templates/astro/` 配下に必要ファイル群
  - `astro.config.mjs`, `tsconfig.json`, `rescript-modules.d.ts`
  - `src/pages/index.astro`
  - `src/components/Counter.res`, `src/components/StaticGreeting.res`
  - `src/__tests__/Counter.test.mjs`
  - `readme/islands.md`, `readme/static-vs-ssr.md`
- [x] テスト: 生成ファイル数 + 必須 deps + `index.astro` の `client:load` 検証

### 5.4 ドキュメント

- [x] sphinx-docs/user/templates/astro.md 新規 + index.md カード追加
- [x] `.po` 同期と日本語充填

### 5.5 検証 + コミット

- [x] `./gradlew ktlintCheck buildPlugin test` 成功
- [x] コミット: `✨ Add Astro project template`

## Phase 6: Waku テンプレート

### 6.1 TemplateVersions.kt

- [x] Waku バージョン定数追加

### 6.2 ProjectTemplate.kt

- [x] `WAKU` enum 値追加
- [x] `generateFiles()` dispatch 追加

### 6.3 WakuTemplateFiles.kt + リソース + テスト

- [x] Kotlin ファイル新規（KDoc 付き）
- [x] `src/main/resources/templates/waku/` 配下に必要ファイル群
  - `tsconfig.json`, `rescript-modules.d.ts`
  - `src/pages/index.tsx`
  - `src/components/Counter.res`, `src/components/CounterClient.tsx` (`"use client"` ラッパー)
  - `src/components/Greet.res`
  - `src/__tests__/Greet.test.mjs`
  - `readme/server-vs-client.md`, `readme/rsc-basics.md`
- [x] テスト: 生成ファイル数 + 必須 deps + `CounterClient.tsx` の `"use client"` 検証

### 6.4 ドキュメント

- [x] sphinx-docs/user/templates/waku.md 新規 + index.md カード追加
- [x] `.po` 同期と日本語充填

### 6.5 検証 + コミット

- [x] `./gradlew ktlintCheck buildPlugin test` 成功
- [x] コミット: `✨ Add Waku project template`

## Phase 7: 仕上げ

### 7.1 取りこぼしドキュメント確認

- [x] CLAUDE.md, README.md, docs/repository-structure.md, docs/templates.md, docs/product-requirements.md でテンプレート数が 21 になっているか (main マージ後に 17 + 4 = 21 へ調整)
- [x] plugin.xml `<change-notes>` Unreleased に新規 4 テンプレートが記載されているか
- [x] sphinx-docs index.md / 各 detail ページが揃っているか
- [x] `.po` で `msgstr ""` が残っていないこと（`grep -r 'msgstr ""' sphinx-docs/locale/ja/`）

### 7.2 最終検証

- [x] `./gradlew ktlintCheck` 成功
- [x] `./gradlew clean buildPlugin` 成功
- [x] `./gradlew test` 成功
- [x] `./gradlew koverHtmlReport` 成功（カバレッジ確認）
- [x] `./gradlew verifyPluginStructure` 成功
- [x] `./gradlew verifyPlugin` 成功（新規 deprecated 利用なし）

### 7.3 手動検証 (`./gradlew runIde`)

- [ ] New Project ダイアログに 21 テンプレートが表示される（手動 `runIde` 確認・未実施）
- [ ] 既存テンプレート選択で Validation コンボが表示される（手動確認・未実施）
- [ ] 新 4 テンプレート選択で Validation コンボが非表示になる（手動確認・未実施）
- [x] TanStack Start 生成 → `pnpm install && rescript build && pnpm test` 成功（NewReactTemplatesIntegrationTest 経由で検証）
- [x] Remix v7 生成 → `pnpm install && rescript build && pnpm test` 成功（NewReactTemplatesIntegrationTest 経由で検証）
- [x] Astro 生成 → `pnpm install && rescript build && pnpm test` 成功（NewReactTemplatesIntegrationTest 経由で検証）
- [x] Waku 生成 → `pnpm install && rescript build && pnpm test` 成功（NewReactTemplatesIntegrationTest 経由で検証）

## Phase 8: main マージ調整

実装中に main 上で `feature/hono-inertia-template` (17 番目テンプレート) と本作業のステアリング docs がマージされたため、worktree branch を main にマージする際にコンフリクト解決が必要となった。テンプレート総数 16→20 を前提としていた箇所を 16→21 (=既存 17 + 新 4) に調整した。

- [x] worktree 内で `git merge main` 実行、コンフリクト解決 (CLAUDE.md / README.md / docs/repository-structure.md / docs/templates.md / docs/product-requirements.md / docs/functional-design.md / sphinx-docs/user/templates/index.md / sphinx-docs/locale/ja/LC_MESSAGES/user/templates/index.po / src/test/.../ProjectTemplateTest.kt / src/main/resources/META-INF/plugin.xml)
- [x] sphinx-docs `make gettext && make update-po && make build-ja` 通過、未翻訳・fuzzy 0 件
- [x] `./gradlew ktlintCheck clean buildPlugin test` 通過

### 7.4 マージ

- [x] tasklist.md の全タスク `[x]` 化（このタスク自体を含む）してコミット
- [x] `AskUserQuestion` で main マージ可否確認
- [x] 承認後、worktree 内で main にマージ実行 (worktree が main を保持できないため `git -C <main-repo>` で main へ直接マージ)
- [x] セッション終了（worktree 自動クリーンアップ）

## 補足: テスト免除

- `RescriptProjectWizardStep` の UI ロジック変更 — Swing UI コンポーネントのため `.claude/rules/testing.md` の免除対象。手動検証で確認する
