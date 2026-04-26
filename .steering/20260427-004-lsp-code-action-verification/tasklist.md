# LSP Code Action 動作検証 — タスクリスト

## Phase 1: 環境準備

- [ ] worktree 作成（`EnterWorktree` ツール、機能名: `lsp-code-action-verification`）
- [ ] `node --version` / `npm ls -g @rescript/language-server` でバージョン記録
- [ ] `findings.md` の「環境情報」セクションを記録
- [ ] `samples/` ディレクトリを作成

## Phase 2: サンプル `.res` 作成

- [ ] `samples/01_missing_cases.res`
- [ ] `samples/02_wrap_in_some.res`
- [ ] `samples/03_record_missing_fields.res`
- [ ] `samples/04_simple_conversion.res`
- [ ] `samples/05_did_you_mean.res`
- [ ] `samples/06_remove_unused.res`
- [ ] `samples/07_extract_local_module.res`
- [ ] `samples/08_expand_catch_all.res`
- [ ] `samples/09_apply_uncurried.res`

## Phase 3: runIde で検証

- [ ] `./gradlew runIde` を起動
- [ ] サンドボックスで Basic テンプレートのプロジェクト作成、`samples/*.res` をコピー
- [ ] `npm install` で `@rescript/language-server` 導入、ビルドが通ることを確認
- [ ] サンプル 01: `simpleAddMissingCases` の表示・適用結果を記録
- [ ] サンプル 02: `wrapInSome` の表示・適用結果を記録
- [ ] サンプル 03: `addUndefinedRecordFields` の表示・適用結果を記録
- [ ] サンプル 04: `simpleConversion` の表示・適用結果を記録
- [ ] サンプル 05: `didYouMean` の表示・適用結果を記録
- [ ] サンプル 06: `removeUnusedCode` の表示・適用結果を記録（reanalyze の有効化が必要）
- [ ] サンプル 07: `extractLocalModuleToFile` の表示・適用結果を記録
- [ ] サンプル 08: `expandCatchAllPatterns` の表示・適用結果を記録
- [ ] サンプル 09: `applyUncurried` の表示・適用結果を記録（v10/11 互換のサンプルが必要かを判断）
- [ ] 動かない code action は `idea.log` の LSP セクションを抜粋して記録

## Phase 4: 原因分析

- [ ] `External Libraries` から `com.intellij.platform.lsp.api.customization` の API を確認
- [ ] `RescriptLspServerDescriptor.lspCustomization` で code action 関連の override が必要か判定
- [ ] 動かない code action ごとに「原因 / 対処方針」を `findings.md` に記録

## Phase 5: ドキュメント反映

- [ ] `docs/lsp-fallback-matrix.md` の "LSP Code Actions" セクションに 9 種の動作可否表を追記
- [ ] `docs/archive/implemented-features.md` に「Quick Fix (LSP Code Actions)」エントリを補強し、確認済みの 9 種をリストに追加
- [ ] `sphinx-docs/user/features/code-analysis.md` に利用可能な Quick Fix 一覧を記載
- [ ] `sphinx-docs/locale/ja/LC_MESSAGES/user/features/code-analysis.po` を更新（`make gettext` → `make update-po` → 翻訳 → `make build-ja`）
- [ ] `sphinx-po-ja-sync` skill で sphinx 同期を最終確認

## Phase 6: コミット前検証

- [ ] `./gradlew ktlintCheck` 成功
- [ ] `./gradlew clean buildPlugin` 成功
- [ ] `./gradlew test` 成功（テストコードを変更していなくても regression 確認）
- [ ] サンプル `.res` ファイルがコンパイル可能（`samples/` 単独で `rescript build` が通るか）
- [ ] DoD Phase 3（`.claude/rules/definition-of-done.md`）を全項目通過

## Phase 7: コミット

- [ ] `samples/` を 1 コミット（`📝 Add LSP code action verification samples`）
- [ ] `findings.md` / `next-steps.md` を 1 コミット（`📝 Record LSP code action verification results`）
- [ ] `docs/lsp-fallback-matrix.md` / `docs/archive/implemented-features.md` / sphinx 更新を 1 コミット（`📝 Document LSP code action availability`）

## Phase 8: マージ

- [ ] tasklist.md の全タスクを `[x]` に更新
- [ ] requirements.md の受け入れ条件を全て満たしていることを確認
- [ ] `AskUserQuestion` でマージ可否を確認
- [ ] 承認後、worktree 内で `git checkout main && git merge worktree-lsp-code-action-verification`
- [ ] 作業ブランチを削除
- [ ] セッション終了（worktree 自動クリーンアップ）

## Phase 9: 次の steering へ

- [ ] 動作しない code action のうち、PSI ベースのネイティブ Quick Fix を実装すべきものをリストアップ
- [ ] 次の steering（例: `20260427-005-native-missing-cases-quickfix/`）の起票判断材料として `next-steps.md` を残す

## 備考

- 検証は実環境（`@rescript/language-server` 必須）で行うため、CI で自動化は困難。tasklist 完了後の手動確認が前提
- テスト追加は最小限（`samples/*.res` は構文サンプルとしてのみ存在し、Kotlin テストの追加は不要）
- DoD Phase 3 のテスト要件は「コード変更なし」のため適用外（`testing.md` の例外規定）
