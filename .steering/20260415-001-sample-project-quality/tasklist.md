# タスクリスト: サンプルプロジェクト品質改善

## 進捗凡例
- [ ] 未着手
- [x] 完了

---

## Step 1: 基盤整備 (Phase 3)

- [x] `TemplateVersions.kt` を追加 (`src/main/kotlin/com/rescript/plugin/wizard/templates/`)
- [x] `TemplateContext.kt` を追加（PackageManager 依存のヘルパー）
- [x] `CommonFiles.kt` を追加（gitignore/editorconfig/readme/ci ワークフロー生成）
- [x] `ProjectTemplate.generateFiles(ctx: TemplateContext)` オーバーロードを追加、既存 API はデリゲートに
- [x] 各テンプレートに `generate(ctx)` シムを追加して段階移行を可能に
- [x] 新規 3 ファイルのユニットテスト (`TemplateVersionsTest.kt`, `TemplateContextTest.kt`, `CommonFilesTest.kt`) を追加
- [x] `./gradlew ktlintCheck compileKotlin compileTestKotlin` 成功、新規テストパス
- [x] コミット: `✨ Add template versions, context, and common files foundation`

## Step 2: PackageManager 選択値の反映

- [x] `RescriptProjectWizardStep.kt` のデフォルト選択を `PNPM` に変更
- [x] `RescriptModuleBuilder.kt` のデフォルトを `PNPM` に変更、`generateFiles(ctx)` 経路に切替
- [x] `RescriptProjectGenerator.kt` に `generateFiles(template, ctx)` 追加、旧 API はデフォルト `PNPM` にフォールバック
- [x] `RescriptModuleBuilderTest.kt` / `RescriptProjectGeneratorTest.kt` を更新
- [x] コミット: `✨ Wire PackageManager selection through to template generation`

## Step 3: Basic テンプレート改善 (Phase 2 + 4)

- [x] `BasicTemplateFiles.kt` を `TemplateContext` 対応に変更
- [x] README / .gitignore / .editorconfig / ci.yml を追加
- [x] `package.json` に `packageManager`, `engines`, `private` フィールドを追加
- [x] `ProjectFileBuilders.packageJson` に `packageManager` / `engines` パラメータを追加
- [x] `BasicTemplateFilesTest.kt` 新規追加
- [x] コミット: `✨ Enhance Basic template with docs, tooling, and packageManager metadata`

## Step 4: Vite+ 導入 — Vite + React テンプレート (Phase 4)

- [x] `ViteReactTemplateFiles.kt` を Vite+ 向けに書き換え（`vite-plus` + `@voidzero-dev/vite-plus-core` + `vitest`）
- [x] `vite.config.mjs` の import を `vite-plus` に変更
- [x] JSX v4 を `rescript.json` で設定済みのまま維持
- [x] README / .gitignore / .editorconfig / ci.yml / Vitest サンプル を追加
- [x] README に Vite+ pre-1.0 の注意書きを記載
- [x] `ViteReactTemplateFilesTest.kt` 新規追加
- [x] `TemplateDependencyVersionsTest` の vite アサーションを vite-plus 存在チェックに更新
- [x] コミット: `✨ Migrate Vite+React template to Vite+ toolchain`

## Step 5: Vite+ 導入 — Electron / Monorepo (Phase 4)

- [x] `ElectronTemplateFiles.kt` を Vite+ 向けに書き換え
- [x] Electron の README / ci.yml / .gitignore / .editorconfig を追加
- [x] `ElectronTemplateFilesTest.kt` 新規追加
- [x] `MonorepoTemplateFiles.kt` を Vite+ (client) + `pnpm-workspace.yaml` 対応に書き換え
- [x] Monorepo: PM が pnpm 以外の場合は root `package.json` に `workspaces` フィールドを出力
- [x] Monorepo dev script を PM ごとに切替 (pnpm filter / yarn workspace / npm --workspace)
- [x] Monorepo の README / ci.yml / .gitignore / .editorconfig を追加
- [x] `MonorepoTemplateFilesTest.kt` 新規追加
- [x] `ProjectTemplateTest` の workspaces アサーションを更新（PNPM/NPM 別ケース）
- [x] コミット: `✨ Migrate Electron and Monorepo templates to Vite+ and pnpm workspaces`

## Step 6: 残り React 系テンプレート (Phase 2 + 4)

- [x] `NextjsTemplateFiles.kt`: TemplateContext 対応 + Vitest + README / ci.yml / .gitignore / .editorconfig
- [x] `NextjsTemplateFilesTest.kt` 新規追加
- [x] `ReactNativeTemplateFiles.kt`: TemplateContext 対応 + README / ci.yml / .gitignore / .editorconfig
- [x] `ReactNativeTemplateFilesTest.kt` 新規追加
- [x] コミット: `✨ Enhance Next.js and React Native templates with modern config`

## Step 7: Backend 系テンプレート (Phase 2 + 4)

- [ ] `HonoTemplateFiles.kt`: `@rescript/core` + README / ci.yml / .gitignore / .editorconfig / Vitest
- [ ] `HonoTemplateFilesTest.kt` を更新
- [ ] `CloudflareWorkersTemplateFiles.kt`: `@rescript/core` + README / ci.yml / .gitignore
- [ ] `CloudflareWorkersTemplateFilesTest.kt` を更新
- [ ] `AwsLambdaTemplateFiles.kt`: `@rescript/core` + README / ci.yml / .gitignore
- [ ] `AwsLambdaTemplateFilesTest.kt` を更新
- [ ] `GoogleCloudRunTemplateFiles.kt`: `@rescript/core` + Dockerfile + README / ci.yml / .gitignore
- [ ] `GoogleCloudRunTemplateFilesTest.kt` を更新
- [ ] コミット: `✨ Enhance backend templates (Hono, CF Workers, AWS Lambda, Cloud Run)`

## Step 8: Library / CLI テンプレート (Phase 2 + 4)

- [ ] `NpmLibraryTemplateFiles.kt`: genType 設定 + `@rescript/core` + README / ci.yml / .gitignore / Vitest
- [ ] `NpmLibraryTemplateFilesTest.kt` を更新
- [ ] `CliToolTemplateFiles.kt`: `@rescript/core` + README / ci.yml / .gitignore / Vitest
- [ ] `CliToolTemplateFilesTest.kt` を更新
- [ ] コミット: `✨ Enhance Library and CLI templates with genType and Vitest`

## Step 9: 統合テスト基盤 (Phase 1)

- [ ] `build.gradle.kts` に `integrationTest` ソースセットと `integrationTest` タスクを追加
- [ ] `src/integrationTest/kotlin/com/rescript/plugin/wizard/TemplateIntegrationTest.kt` を追加
- [ ] `src/integrationTest/kotlin/com/rescript/plugin/wizard/IntegrationTestSupport.kt` を追加（exec ヘルパー）
- [ ] `.gitignore` に `src/integrationTest/workdir/` を追加（必要に応じて）
- [ ] ローカルで `./gradlew integrationTest` を手動実行し、全 12 テンプレがパスすることを確認
- [ ] コミット: `✅ Add integration test harness for template generation`

## Step 10: Integration Tests ワークフロー (Phase 1)

- [ ] `.github/workflows/integration-tests.yml` を追加（`workflow_dispatch` + nightly cron）
- [ ] コミット: `🔧 Add integration tests workflow (manual + nightly)`

## Step 11: ドキュメント更新

- [ ] `CLAUDE.md` のレイヤー 3「プロジェクトウィザード」の記述を Vite+ / PM 反映 / テンプレ充実を反映
- [ ] `README.md` の Features セクションを更新
- [ ] `sphinx-docs/user/features/advanced.md` の Project Wizard セクションを更新
- [ ] `docs/product-requirements.md` の実装済み機能セクションを更新
- [ ] コミット: `📝 Update docs for sample project quality improvements`

## Step 12: コミット前検証 (Definition of Done Phase 3)

- [ ] `./gradlew ktlintCheck` 成功
- [ ] `./gradlew clean buildPlugin` 成功
- [ ] `./gradlew test` 成功
- [ ] 新規 `.kt` すべてに KDoc (`/** ... */`) 付与済み確認
- [ ] 新規クラスすべてに対応する `<ClassName>Test.kt` 存在確認
- [ ] CLAUDE.md / README.md / sphinx-docs / product-requirements.md の同期確認
- [ ] セキュリティチェック: GitHub Actions workflow が信頼済み action のみ使用しているか

## Step 13: マージ

- [ ] tasklist.md のすべてのタスクが `[x]` であること確認
- [ ] `AskUserQuestion` でユーザーにマージ可否確認
- [ ] 承認後、worktree 内で `main` にマージ
- [ ] 作業ブランチ削除
- [ ] セッション終了（worktree 自動クリーンアップ発動）

---

## テスト免除対象

以下は Swing UI / LSP / IDE ライフサイクル依存のためユニットテスト免除:
- `RescriptProjectWizardStep.kt` — Swing UI (一部ロジックは分離してテスト)
- `RescriptModuleBuilder.kt` — IDE ライフサイクル依存だが、可能な範囲でテスト

その他のクラス（`TemplateVersions`, `TemplateContext`, `CommonFiles`, 全 `*TemplateFiles`）はテスト必須。

---

## コミット粒度まとめ

計 11 コミット予定:
1. 基盤整備
2. PackageManager 反映
3. Basic
4. Vite+React → Vite+
5. Electron + Monorepo → Vite+
6. Next.js + React Native
7. Backend 4 種
8. Library + CLI
9. 統合テストハーネス
10. 統合テストワークフロー
11. ドキュメント

Step 12 (検証) と Step 13 (マージ) はコミットなし。
