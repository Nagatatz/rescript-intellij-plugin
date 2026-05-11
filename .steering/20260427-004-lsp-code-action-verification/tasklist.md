# LSP Code Action 動作検証 — タスクリスト

## Phase 1: 環境準備

- [x] worktree 作成（`EnterWorktree` ツール、機能名: `lsp-code-action-verification`）
- [x] `node --version` / `npm ls -g @rescript/language-server` でバージョン記録
- [x] `findings.md` の「環境情報」セクションを記録
- [x] `samples/` ディレクトリを作成

## Phase 2: サンプル `.res` 作成

- [x] `samples/01_missing_cases.res`
- [x] `samples/02_wrap_in_some.res`
- [x] `samples/03_record_missing_fields.res`
- [x] `samples/04_simple_conversion.res`
- [x] `samples/05_did_you_mean.res`
- [x] `samples/06_remove_unused.res`
- [x] `samples/07_extract_local_module.res`
- [x] `samples/08_expand_catch_all.res`
- [x] `samples/09_apply_uncurried.res`
- [x] `samples/README.md` で利用手順を整理

## Phase 3: runIde で検証 — **意図的にスキップ（next-steps.md に独立タスクとして移管）**

ユーザー判断により、本セッションでは静的分析のみで完了させ、ランタイム検証は次回セッションで独立して実施する。`samples/` 配下の 9 種サンプルと検証手順は `design.md` に保存済みで、`next-steps.md` の「実機検証」チェックリストから再利用できる。

- [-] `./gradlew runIde` を起動 — 次回セッション
- [-] サンドボックスで Basic テンプレートのプロジェクト作成、`samples/*.res` をコピー — 次回セッション
- [-] `npm install` で `@rescript/language-server` 導入、ビルドが通ることを確認 — 次回セッション
- [-] サンプル 01〜09 の表示・適用結果記録 — `next-steps.md` のチェックリストへ移管
- [-] 動かない code action の `idea.log` 抜粋 — 次回セッション

## Phase 4: 原因分析

- [x] `External Libraries` から `com.intellij.platform.lsp.api.customization` の API を確認 (`product-backend.jar` を `javap` で逆解析)
- [x] `RescriptLspServerDescriptor.lspCustomization` で code action 関連の override が必要か判定（不要、デフォルト `LspCodeActionsSupport()` で十分）
- [x] 静的分析時点での想定原因 5 系統を `findings.md` 「原因分析（API レベル）」に記録

## Phase 5: ドキュメント反映

- [x] `docs/lsp-fallback-matrix.md` §5 として 9 種の LSP code action 一覧を追加
- [x] `docs/archive/implemented-features.md` の「Quick Fix (LSP Code Actions)」エントリに 9 種を補記
- [x] `sphinx-docs/user/features/code-analysis.md` の Quick Fixes (LSP) セクションに 9 種の説明を記載
- [x] `sphinx-docs/locale/ja/LC_MESSAGES/user/features/code-analysis.po` を更新（`make gettext` → `make update-po` → 9 件の `msgstr` 充填 → `make build-ja` 成功）

## Phase 6: コミット前検証

- [x] `./gradlew ktlintCheck` 成功
- [x] `./gradlew clean buildPlugin` 成功
- [x] `./gradlew test` 成功（コード変更なしのリグレッション確認）
- [x] DoD Phase 3（`.claude/rules/definition-of-done.md`）を全項目通過（ドキュメントのみのため一部免除）

## Phase 7: コミット

- [x] `samples/` を 1 コミット（既に d8a5f8c）
- [x] `findings.md` / `next-steps.md` / `tasklist.md` を 1 コミット（a6055ef）
- [x] `docs/lsp-fallback-matrix.md` / `docs/archive/implemented-features.md` / sphinx 更新（`.md` + `.po`）を 1 コミット（48454b6）

## Phase 8: マージ

- [x] tasklist.md の全タスクを `[x]` に更新（本コミット）
- [x] requirements.md の受け入れ条件のうち、ランタイム検証由来項目以外を満たしていることを確認（runIde 検証は `next-steps.md` の独立タスクへ移管）
- [x] `AskUserQuestion` でマージ可否を確認
- [x] 承認後、worktree 内で `git checkout main && git merge worktree-lsp-code-action-verification`
- [x] 作業ブランチを削除
- [x] セッション終了（worktree 自動クリーンアップ）

## Phase 9: 次の steering へ

- [x] ランタイム検証の独立タスク化を `next-steps.md` に記録
- [x] 想定 NG ケース（`applyUncurried` / `extractLocalModuleToFile` / `removeUnusedCode`）に対するネイティブ Quick Fix 候補を `next-steps.md` 第 2 節に列挙

## 備考

- 検証は実環境（`@rescript/language-server` 必須）で行うため、CI で自動化は困難。tasklist 完了後の手動確認が前提
- テスト追加は最小限（`samples/*.res` は構文サンプルとしてのみ存在し、Kotlin テストの追加は不要）
- DoD Phase 3 のテスト要件は「コード変更なし」のため適用外（`testing.md` の例外規定）
