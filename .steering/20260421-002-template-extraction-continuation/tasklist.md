# Tasklist — Wizard Template Resource Extraction (Continuation)

**参照:** `.claude/rules/definition-of-done.md` の 5 フェーズに沿う。

前回 steering `20260416-002-wizard-template-resource-extraction` のパターンを踏襲。

---

## Phase 1: 計画

- [x] `.steering/20260421-002-template-extraction-continuation/` 作成
- [x] `requirements.md` 作成・承認
- [x] `design.md` 作成・承認
- [ ] `tasklist.md` 作成・承認
- [ ] `EnterWorktree` で `template-extract-cont` worktree に入る

---

## Phase 2: 実装

各テンプレートで以下を満たす 1 コミットを作る（順次処理）:

- [ ] pre-snapshot 採取（NPM/PNPM/YARN）
- [ ] 対象ファイルを `src/main/resources/templates/<name>/**` に配置
- [ ] `<Name>TemplateFiles.kt` で `TemplateResourceLoader.load(...)` に差し替え
- [ ] 新規プレースホルダがあれば `TemplateResourcesSmokeTest` を更新
- [ ] post-snapshot 採取 → `diff -r` が完全一致
- [ ] 既存 `<Name>TemplateFilesTest` が無修正で pass
- [ ] ktlint pass
- [ ] tasklist.md のテンプレート項目を `[x]` に更新
- [ ] 個別 `git add` でコミット

### コミット 1: `♻️ Extract static content from BasicTemplateFiles to resources`

- [ ] basic/ 配下に抽出
- [ ] 検証 pass
- [ ] コミット

### コミット 2: `♻️ Extract static content from AwsLambdaTemplateFiles to resources`

- [ ] aws-lambda/ 配下に抽出
- [ ] 検証 pass
- [ ] コミット

### コミット 3: `♻️ Extract static content from CloudflareWorkersTemplateFiles to resources`

- [ ] cloudflare-workers/ 配下に抽出
- [ ] 検証 pass
- [ ] コミット

### コミット 4: `♻️ Extract static content from GoogleCloudRunTemplateFiles to resources`

- [ ] google-cloud-run/ 配下に抽出
- [ ] 検証 pass
- [ ] コミット

### コミット 5: `♻️ Extract static content from CliToolTemplateFiles to resources`

- [ ] cli-tool/ 配下に抽出
- [ ] 検証 pass
- [ ] コミット

### コミット 6: `♻️ Extract static content from ViteReactTemplateFiles to resources`

- [ ] vite-react/ 配下に抽出
- [ ] 検証 pass
- [ ] コミット

### コミット 7: `♻️ Extract static content from ReactNativeTemplateFiles to resources`

- [ ] react-native/ 配下に抽出
- [ ] 検証 pass
- [ ] コミット

### コミット 8: `♻️ Extract static content from ElectronTemplateFiles to resources`

- [ ] electron/ 配下に抽出
- [ ] 検証 pass
- [ ] コミット

### コミット 9: `♻️ Extract static content from NextjsTemplateFiles to resources`

- [ ] nextjs/ 配下に抽出
- [ ] 検証 pass
- [ ] コミット

### コミット 10: `♻️ Extract static content from NpmLibraryTemplateFiles to resources`

- [ ] npm-library/ 配下に抽出
- [ ] 検証 pass
- [ ] コミット

---

## Phase 3: コミット前検証

各コミットで次を満たす。

- [ ] `./gradlew ktlintCheck` pass
- [ ] `TemplateResourcesSmokeTest` / 対象の `*TemplateFilesTest` pass
- [ ] snapshot `diff -r` が完全一致
- [ ] 新規 `class` / `object` に英語 KDoc（`<Name>TemplateFiles` の既存 KDoc を維持）
- [ ] 個別 `git add`（`-A` / `.` 禁止）
- [ ] 絵文字 ♻️ プレフィックス

---

## Phase 4: マージ前

- [ ] 全 Phase 2 / Phase 3 項目が `[x]`
- [ ] `./gradlew clean buildPlugin` pass
- [ ] `./gradlew test` pass
- [ ] Kover minBound 85 を下回らない（`./gradlew koverVerify`）
- [ ] CLAUDE.md / repository-structure.md の追加更新なし（前回 steering で対応済み）
- [ ] `AskUserQuestion` でマージ可否をユーザーに確認
  - セキュリティ影響: なし（リソースのロードのみ、新規外部 I/O なし）

---

## Phase 5: マージ後

- [ ] worktree 内で `git checkout main && git merge worktree-template-extract-cont`
- [ ] ブランチ削除、worktree クリーンアップ

---

## 備考

- snapshot diff は CI で再現しないため、各コミット内でローカル検証必須。
- 差異が出たコミットは破棄して原因調査。テンプレートの挙動が変わってはならない。
- Batch として 10 コミットをまとめてマージする方針（機能単位ルールはテンプレート 1 個 = 1
  コミットで既に満たしている）。
