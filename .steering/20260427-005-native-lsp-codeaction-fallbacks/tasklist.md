# Native PSI Fallbacks — タスクリスト

## Phase 1: 環境準備

- [x] worktree 作成（`worktree-native-lsp-codeaction-fallbacks`）
- [x] `.steering/20260427-005-native-lsp-codeaction-fallbacks/` 配下に requirements / design / tasklist 起票
- [x] 既存 `RescriptNameIndex` の API 確認（`StubIndex.processElements` 経由で利用）
- [x] 既存 Intention の plugin.xml 登録形式を確認（`<intentionAction>` + `<skipBeforeAfter>true</skipBeforeAfter>`）

## Phase 2: A1 実装 — `RescriptApplyUncurriedIntention`

- [x] `src/main/kotlin/com/rescript/plugin/intention/RescriptApplyUncurriedIntention.kt` 作成
- [x] `isAvailableInRescript`: call site 検出 + 定義テキストパターン検証
- [x] `invoke`: `(` の直後に `. ` 挿入（0 引数は `(.)` に変換）
- [x] KDoc を英語で記述
- [x] `src/test/kotlin/com/rescript/plugin/intention/RescriptApplyUncurriedIntentionTest.kt` 作成（13 テスト、すべてグリーン）
- [x] `plugin.xml` に `<intentionAction>` 登録
- [x] コミット: `✨ Add applyUncurried PSI fallback intention` (3e9137a)

備考: テストは companion object のヘルパー関数を直接呼ぶ JUnit5 ユニットテストで実装。既存パターン（`RescriptAddTypeAnnotationIntentionTest` 等）と整合。

## Phase 3: A2 実装 — `RescriptExtractLocalModuleToFileIntention`

- [x] `src/main/kotlin/com/rescript/plugin/intention/RescriptExtractLocalModuleToFileIntention.kt` 作成
- [x] `isAvailableInRescript`: トップレベル `module M = { ... }` 検出 + `M.res` 既存チェック
- [x] `invoke`: 本体抽出 + 新規ファイル作成 + 元宣言削除 + 参照警告通知
- [x] KDoc を英語で記述
- [x] `src/test/kotlin/com/rescript/plugin/intention/RescriptExtractLocalModuleToFileIntentionTest.kt` 作成（15 テスト、すべてグリーン）
- [x] `plugin.xml` に `<intentionAction>` 登録
- [x] コミット: `✨ Add extractLocalModuleToFile PSI fallback intention` (289ae02)

## Phase 4: ドキュメント反映

- [x] `README.md` Intention Actions 行に 2 つを追記
- [x] `sphinx-docs/user/features/code-editing.md` のテーブル + 詳細セクション 2 つ（before/after コードサンプル付き）
- [x] `sphinx-docs/locale/ja/LC_MESSAGES/user/features/code-editing.po` 同期（`make gettext` → `make update-po` → 9 件の `msgstr` 充填 → `make build-ja` 成功）
- [x] `docs/functional-design.md`: 既存テーブルが代表例のみ列挙する方針のため、本ステアリングでは追加しない（既存パターン尊重）
- [x] コミット: `📝 Document native LSP code action fallback intentions` (4a913fc)

## Phase 5: コミット前検証

- [x] `./gradlew ktlintCheck` 成功
- [x] `./gradlew clean buildPlugin` 成功
- [x] `./gradlew test` 成功
- [x] DoD Phase 3（`.claude/rules/definition-of-done.md`）を全項目通過

## Phase 6: マージ

- [x] tasklist.md の全タスクを `[x]` に更新（本コミットに含める）
- [x] requirements.md の受け入れ条件をすべて満たしていることを確認
- [ ] `AskUserQuestion` でマージ可否を確認
- [ ] 承認後、main へマージ
- [ ] worktree クリーンアップ
- [ ] 完了後、Task D（ドキュメント整理）に着手