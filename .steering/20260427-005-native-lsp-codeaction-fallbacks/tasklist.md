# Native PSI Fallbacks — タスクリスト

## Phase 1: 環境準備

- [x] worktree 作成（`worktree-native-lsp-codeaction-fallbacks`）
- [x] `.steering/20260427-005-native-lsp-codeaction-fallbacks/` 配下に requirements / design / tasklist 起票
- [ ] 既存 `RescriptStubIndexKey` `let` インデックスの公開 API を確認
- [ ] 既存 Intention の plugin.xml 登録形式を確認

## Phase 2: A1 実装 — `RescriptApplyUncurriedIntention`

- [ ] `src/main/kotlin/com/rescript/plugin/intention/RescriptApplyUncurriedIntention.kt` 作成
- [ ] `isAvailableInRescript`: call site 検出 + 定義テキストパターン検証
- [ ] `invoke`: `(` の直後に `. ` 挿入（0 引数は `(.)` に変換）
- [ ] KDoc を英語で記述
- [ ] `src/test/kotlin/com/rescript/plugin/intention/RescriptApplyUncurriedIntentionTest.kt` 作成
  - [ ] `testCurriedCallToUncurriedDef_isAvailable`
  - [ ] `testNoCurriedDef_notAvailable`
  - [ ] `testAlreadyUncurried_notAvailable`
  - [ ] `testApply_insertsDotBeforeFirstArg`
  - [ ] `testApply_zeroArgs`
- [ ] `plugin.xml` に `<intentionAction>` 登録
- [ ] `./gradlew test --tests RescriptApplyUncurriedIntentionTest` 成功
- [ ] コミット: `✨ Add applyUncurried PSI fallback intention`

## Phase 3: A2 実装 — `RescriptExtractLocalModuleToFileIntention`

- [ ] `src/main/kotlin/com/rescript/plugin/intention/RescriptExtractLocalModuleToFileIntention.kt` 作成
- [ ] `isAvailableInRescript`: トップレベル `module M = { ... }` 検出 + `M.res` 既存チェック
- [ ] `invoke`: 本体抽出 + 新規ファイル作成 + 元宣言削除 + 参照警告通知
- [ ] KDoc を英語で記述
- [ ] `src/test/kotlin/com/rescript/plugin/intention/RescriptExtractLocalModuleToFileIntentionTest.kt` 作成
  - [ ] `testTopLevelModule_isAvailable`
  - [ ] `testNestedModule_notAvailable`
  - [ ] `testAliasModule_notAvailable`
  - [ ] `testExistingFile_notAvailable`
  - [ ] `testApply_createsFileAndRemovesDeclaration`
- [ ] `plugin.xml` に `<intentionAction>` 登録
- [ ] `./gradlew test --tests RescriptExtractLocalModuleToFileIntentionTest` 成功
- [ ] コミット: `✨ Add extractLocalModuleToFile PSI fallback intention`

## Phase 4: ドキュメント反映

- [ ] `README.md` Features「Code Editing > Intention Actions」に 2 つ追加
- [ ] `sphinx-docs/user/features/code-editing.md` に before/after サンプル追加
- [ ] `sphinx-docs/locale/ja/LC_MESSAGES/user/features/code-editing.po` 同期（`make gettext` → `make update-po` → 翻訳 → `make build-ja`）
- [ ] `docs/functional-design.md` の Extension Point マップ更新（intention カテゴリに該当行があれば）
- [ ] コミット: `📝 Document native LSP code action fallback intentions`

## Phase 5: コミット前検証

- [ ] `./gradlew ktlintCheck` 成功
- [ ] `./gradlew clean buildPlugin` 成功
- [ ] `./gradlew test` 成功
- [ ] DoD Phase 3（`.claude/rules/definition-of-done.md`）を全項目通過

## Phase 6: マージ

- [ ] tasklist.md の全タスクを `[x]` に更新（最終コミットに含める）
- [ ] requirements.md の受け入れ条件をすべて満たしていることを確認
- [ ] `AskUserQuestion` でマージ可否を確認
- [ ] 承認後、main へマージ
- [ ] worktree クリーンアップ
- [ ] 完了後、Task D（ドキュメント整理）に着手