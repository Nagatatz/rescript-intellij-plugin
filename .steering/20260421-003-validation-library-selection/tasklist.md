# Tasklist — Wizard Validation Library Selection (zod / sury)

**参照:** `.claude/rules/definition-of-done.md` の 5 フェーズに沿う。

---

## Phase 1: 計画

- [x] `.steering/20260421-003-validation-library-selection/` 作成
- [x] `requirements.md` 作成・承認
- [x] `design.md` 作成・承認
- [x] `tasklist.md` 作成・承認（ExitPlanMode で承認済み）
- [ ] `EnterWorktree` で `validation-library-selection` worktree に入る

---

## Phase 2: 実装

各コミットで個別 `git add`、ktlint/test pass を確認してからコミット。

### コミット 1: `✨ Add ValidationLibrary enum + Wizard ComboBox`

- [ ] `src/main/kotlin/com/rescript/plugin/wizard/ValidationLibrary.kt` 新規
- [ ] `src/main/kotlin/com/rescript/plugin/wizard/RescriptProjectWizardStep.kt` に ComboBox + updateDataModel 追加
- [ ] `src/main/kotlin/com/rescript/plugin/wizard/RescriptModuleBuilder.kt` に field + ctx 構築追加
- [ ] `src/main/kotlin/com/rescript/plugin/wizard/templates/TemplateContext.kt` に `validationLibrary` field 追加（デフォルト ZOD）
- [ ] `src/test/kotlin/com/rescript/plugin/wizard/ValidationLibraryTest.kt` 新規
- [ ] `src/test/kotlin/com/rescript/plugin/wizard/RescriptModuleBuilderTest.kt` で新フィールドテスト
- [ ] `src/test/kotlin/com/rescript/plugin/wizard/templates/TemplateContextTest.kt` で round-trip
- [ ] ktlint + build + test pass（全 `*TemplateFilesTest` は無修正で pass、デフォルト ZOD で既存挙動）

### コミット 2: `🔧 Add SURY dependency version`

- [ ] `src/main/kotlin/com/rescript/plugin/wizard/templates/TemplateVersions.kt` に `SURY` 追加
- [ ] `TemplateVersionsTest.kt` を更新

### コミット 3: `♻️ Switch Hono templates to selectable zod/sury Schema`

- [ ] 既存 `templates/hono/src/Schema.res` を `templates/hono/variants/zod/src/Schema.res` へ移動
- [ ] `templates/hono/variants/sury/src/Schema.res` を新規追加
- [ ] `HonoTemplateFiles.kt` で `variants/<key>/...` を参照
- [ ] hono-graphql に対して同様の措置
- [ ] Server.res 側で `Schema.parseXxx : JSON.t => result<_,_>` の呼び出しに統一
- [ ] `HonoTemplateFilesTest` / `HonoGraphqlTemplateFilesTest` を ZOD/SURY 両バリアントで検証

### コミット 4: `♻️ Add zod/sury validation to AwsLambda server`

### コミット 5: `♻️ Add zod/sury validation to CloudflareWorkers server`

### コミット 6: `♻️ Add zod/sury validation to GoogleCloudRun server`

### コミット 7: `♻️ Add zod/sury validation to Nextjs route handler`

### コミット 8: `♻️ Add zod/sury validation to FullStack server`

### コミット 9: `♻️ Add zod/sury validation to Monorepo server`

- [ ] 上記各コミットで `variants/{zod,sury}/src/Schema.res` 作成
- [ ] `<Name>TemplateFiles.kt` で依存と Schema 差し替え
- [ ] `<Name>TemplateFilesTest.kt` を ZOD/SURY 両バリアントで検証
- [ ] snapshot 両バリアントで diff 0 確認（意図した差分のみ）

### コミット 10: `📝 Document validation library selection`

- [ ] `CLAUDE.md` レイヤー 3 の `wizard/` 行に 1 文追記
- [ ] `docs/repository-structure.md` で wizard/ パッケージに `ValidationLibrary` を追加
- [ ] 必要に応じ `sphinx-docs/user/features/advanced.md`（Wizard 節）に 1 文追記

---

## Phase 3: コミット前検証

各コミットで以下:

- [ ] `./gradlew ktlintCheck` pass
- [ ] `./gradlew clean buildPlugin` pass
- [ ] `./gradlew test` pass
- [ ] 新規クラス/object に英語 KDoc
- [ ] deprecated API 新規利用なし
- [ ] `TemplateResourcesSmokeTest` が pass（新プレースホルダ追加時は `knownPlaceholders` を更新）
- [ ] 個別 `git add`、絵文字プレフィックス

---

## Phase 4: マージ前

- [ ] 全 Phase 2 / Phase 3 項目が `[x]`
- [ ] `./gradlew clean buildPlugin test koverVerify verifyPluginStructure` が pass
- [ ] Kover minBound 85 を下回らない
- [ ] `AskUserQuestion` でマージ可否を確認
- [ ] `./gradlew runIde` による Wizard の手動確認（ユーザー側）

---

## Phase 5: マージ後

- [ ] `git checkout main && git merge worktree-validation-library-selection`
- [ ] worktree / ブランチ片付け（セッション終了で自動クリーンアップ）
