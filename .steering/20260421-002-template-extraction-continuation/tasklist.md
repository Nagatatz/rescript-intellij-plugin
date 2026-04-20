# Tasklist — Wizard Template Resource Extraction (Continuation)

**参照:** `.claude/rules/definition-of-done.md` の 5 フェーズに沿う。

前回 steering `20260416-002-wizard-template-resource-extraction` のパターンを踏襲。

---

## Phase 1: 計画

- [x] `.steering/20260421-002-template-extraction-continuation/` 作成
- [x] `requirements.md` 作成・承認
- [x] `design.md` 作成・承認
- [x] `tasklist.md` 作成・承認
- [x] `EnterWorktree` で `template-extract-cont` worktree に入る

---

## Phase 2: 実装

各テンプレートで pre-snapshot → 抽出 → post-snapshot の byte-identical を確認して
1 コミット。合計 10 コミット。

- [x] **コミット 1** `BasicTemplateFiles` (186→91 行)
- [x] **コミット 2** `AwsLambdaTemplateFiles` (179→96 行)
- [x] **コミット 3** `CloudflareWorkersTemplateFiles` (181→93 行)
- [x] **コミット 4** `GoogleCloudRunTemplateFiles` (204→121 行)
- [x] **コミット 5** `CliToolTemplateFiles` (233→97 行)
- [x] **コミット 6** `ViteReactTemplateFiles` (240→103 行)
- [x] **コミット 7** `ReactNativeTemplateFiles` (245→100 行)
- [x] **コミット 8** `ElectronTemplateFiles` (248→106 行)
- [x] **コミット 9** `NextjsTemplateFiles` (260→106 行)
- [x] **コミット 10** `NpmLibraryTemplateFiles` (260→105 行)

合計: 2,236 → 1,018 行（-1,218 行）。

---

## Phase 3: コミット前検証

各コミットで次を満たしたことを確認:

- [x] `./gradlew ktlintCheck` pass（各コミットごと pre-commit hook で実行）
- [x] 対応する `*TemplateFilesTest` が**無修正で** pass
- [x] `TemplateResourcesSmokeTest` が pass（新規プレースホルダは都度追加）
- [x] snapshot `diff -r /tmp/tpl-pre/<name> /tmp/tpl-post/<name>` が完全一致
- [x] 新規クラス・object の既存 KDoc を維持（内容変更なし）
- [x] 個別 `git add`（`-A` / `.` 未使用）
- [x] 絵文字 ♻️ プレフィックス

---

## Phase 4: マージ前

- [x] 全 Phase 2 / Phase 3 項目が `[x]`
- [x] `./gradlew clean buildPlugin` pass
- [x] `./gradlew test` pass (3304 tests, 0 failures, 11 skipped)
- [x] Kover minBound 85 を下回らない（`koverVerify` pass）
- [x] `verifyPluginStructure` pass
- [x] CLAUDE.md / repository-structure.md の追加更新なし（前回 steering で対応済み）
- [ ] `AskUserQuestion` でマージ可否をユーザーに確認
  - セキュリティ影響: なし（リソースのロード追加のみ、新規外部 I/O なし）

---

## Phase 5: マージ後

- [ ] `git checkout main && git merge worktree-template-extract-cont`
- [ ] ブランチ削除、worktree 自動クリーンアップ

---

## 備考

- `TemplateSnapshotDumper.kt`（検証用ツール）は worktree 内のみに残置し、コミットしない
  (git status に untracked のまま残る)。次セッション終了で worktree と共に消える。
- 各コミットで snapshot diff が 0 件一致を確認済み。テンプレートの挙動は完全保存。
