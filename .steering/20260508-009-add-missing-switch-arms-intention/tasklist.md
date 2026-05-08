# Add Missing Switch Arms Intention — Tasklist

## Phase 1: 計画
- [x] requirements.md 作成
- [x] design.md 作成
- [x] tasklist.md 作成
- [x] requirements / design / tasklist のユーザー承認（auto mode）
- [x] `EnterWorktree add-missing-switch-arms-intention` で worktree 作成

## Phase 2: 実装

### 2.1 Pure helper
- [x] `intention/RescriptMissingArmsBuilder.kt` を実装
  - `MissingArmsResult` data class
  - `CoveredSet` sealed class（internal）
  - `findEnclosingSwitchArms(source, offset)` + `findSwitchKeywordStart` 補助
  - `extractCoveredNames(source, arms)` + `classifyPattern`
  - `computeMissing(source, offset, constructors)`
  - `buildInsertion(...)` 内部 helper
  - `isInsideSwitch` / `hasWildcardArm` / `scrutineeOffset` 公開 helper
- [x] `intention/RescriptMissingArmsBuilderTest.kt` 10 ケース実装
  - option / Some のみ → None 提案
  - Result 完全 → null
  - 自前 variant 一部 → Blue 提案
  - or-pattern → Blue のみ提案
  - wildcard `_` → null
  - LIDENT bind → null
  - nested switch → 最内が対象
  - 不完全 switch → null
  - isInsideSwitch / hasWildcardArm helpers
  - scrutineeOffset

### 2.2 Intention class
- [x] `intention/RescriptAddMissingSwitchArmsIntention.kt` を実装
  - `RescriptBaseIntention` 継承
  - `isAvailableInRescript`: switch 内 + WildcardSeen でない判定（LSP 不要）
  - `invoke`: LSP hover → parseVariantConstructors → computeMissing → insertInWriteAction
- [x] `intention/RescriptAddMissingSwitchArmsIntentionTest.kt` 2 ケースのスモークテスト（getText / familyName）

### 2.3 Plugin.xml
- [x] `<intentionAction>` を 1 件追加（Case Split の隣）

## Phase 3: コミット前検証
- [x] `./gradlew ktlintCheck` パス
- [x] `./gradlew clean buildPlugin` パス（disk full で 1 度失敗 → 空きを確保して `./gradlew buildPlugin test` で再検証成功）
- [x] `./gradlew test` パス（builder 10 ケース + intention 2 ケース、フルスイートも green）
- [x] 新規 `.kt` 全てに KDoc 付与確認
- [x] Deprecated API 使用なし確認
- [~] `runIde` で 5 シナリオ実機検証 — auto mode のセッション内では skip（builder 単体テストで全分岐をカバー、intention は LSP/Editor 結合のみ）。次回 hand-on 時に併せて検証する

## Phase 3: ドキュメント更新
- [x] `CLAUDE.md` レイヤー 3 に新 intention の段落追記
- [x] `README.md` の Intention 機能箇条書きに 1 行追加
- [x] `sphinx-docs/user/features/code-editing.md` に変換例追加
- [x] `sphinx-docs/locale/ja/LC_MESSAGES/user/features/code-editing.po` 同期 + ja 訳付与 + `make build-ja` 成功確認
- [~] `docs/product-requirements.md`: ロードマップ表に #10 のエントリは存在せず、変更不要

## Phase 3: コミット
- [x] Builder + テストコミット (`d629962 ✨ Add RescriptMissingArmsBuilder for missing switch arms detection`)
- [x] Intention + テスト + plugin.xml コミット (`6977511 ✨ Add intention to fill missing switch arms`)
- [x] ドキュメント更新コミット (`ebc13bd 📝 Document add-missing-switch-arms intention`)
- [x] tasklist 完了化コミット（最終）

## Phase 4: マージ前
- [x] 全タスク `[x]` または `[~]`（理由明記）確認
- [x] requirements 受け入れ条件確認
- [x] AskUserQuestion でマージ可否確認

## Phase 5: マージ後
- [x] main へマージ + ブランチ削除
- [x] worktree クリーンアップ（セッション終了時自動）

## テスト免除
- なし — Builder は pure 関数で完全カバー、Intention 本体は LSP / Editor 結合が必須なため smoke 確認のみ
