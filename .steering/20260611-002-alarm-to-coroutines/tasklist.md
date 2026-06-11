# タスクリスト: Alarm → coroutines 移行 (Phase 4、#128)

セクション間依存: 2・3 は 1 の後。4 は独立 (3 と同じファイルを触るため 3 の後に実施)。5 (docs) は最後。
各セクション = 1 コミット = 独立にビルド・テスト通過可能な単位。

## セクション 0: セットアップ

- [x] `git fetch origin` + main の ahead/behind 確認 (0/0)
- [x] `EnterWorktree` で worktree 作成、`pwd` / `git rev-parse --show-toplevel` で編集パス確認
- [x] `docs/product-requirements.md` の #128 に 🚧 (最初のコミットに含める)
- [x] **API 安定性確認の記録** (2026.1.2 の `intellij.platform.core.jar` を javap で検査 + intellij-community 261 ブランチのソース照合):
  - `Dispatchers.EDT` の getter (`CoroutinesKt.getEDT`) に JVM Deprecated 属性・`@ApiStatus.Internal`/`Experimental` **なし**。261 ブランチの `coroutines.kt` でも EDT は無 annotation (KDoc は「Platform model に触る EDT 作業はこれを使う」)。property-annotation holder (`getEDT$annotations`) にのみ Deprecated 属性が残るが (2025.2 で deprecate → 撤回された経緯の残骸)、Plugin Verifier が見る getter 呼び出しは無印。**コンパイル時の deprecation 警告ゼロを各セクションの確認項目に追加**して EDT を採用
  - `Dispatchers.UiWithModelAccess` は `@get:Internal`、`EdtImmediate` 等は `@get:Experimental` のため**不採用**
  - light service の `CoroutineScope` コンストラクタ注入は Platform 公式パターン (annotation なし) — 採用

## セクション 1: coroutine 基盤 (util/)

- [x] `util/RescriptCoroutineScopeService.kt` 新規 (KDoc 付き)
- [x] `util/RescriptCoroutineDebouncer.kt` 新規 (KDoc 付き)
- [x] `util/RescriptCoroutineDebouncerTest.kt` 新規 (発火 / cancel-and-restart で最後の 1 件のみ / cancel 後不発火 / scope cancel で不発火 — coroutines-test 依存を増やさず実 dispatcher + CountDownLatch 方式)
- [x] テスト免除の記録: `RescriptCoroutineScopeService` は IDE ライフサイクル依存 (platform の scope 注入) のため免除
- [x] kover: scope service を理由コメント付きでクラス除外に追加 (ロジックなしのホルダー)
- [x] `./gradlew ktlintCheck test` green (新規 deprecation 警告なし)
- [ ] コミット: `✨ Add project-scoped coroutine debouncer infrastructure`

## セクション 2: PanelBase の Alarm 置換

- [x] `RescriptToolWindowPanelBase` のコンストラクタに `project` 追加、Alarm → debouncer (Dispatchers.EDT)、dispose で cancel
- [x] `DualViewToolWindowPanel` + 5 panel (flow / diagram / coverage / impact / interop) の super 呼び出しを追従
- [x] `./gradlew ktlintCheck test` green (新規 deprecation 警告なし — `Dispatchers.EDT` 採用の安全性を裏付け)
- [x] コミット: `♻️ Replace the panel base Alarm debounce with coroutines` (a433bd8)

## セクション 3: TypeInfoPanel の Alarm 置換 (#128 本体)

- [x] `Alarm(POOLED_THREAD)` + `@Suppress("UnstableApiUsage")` + 理由コメントを削除し debouncer (Dispatchers.Default) に置換
- [x] 早期 return 経路の `debouncer.cancel()`、parentDisposable への cancel 接続 (`Disposer.register`)
- [x] `src/main` から `com.intellij.util.Alarm` import が消えたことを grep で確認 (0 件)
- [x] `./gradlew ktlintCheck test` green (新規 deprecation 警告なし)
- [ ] コミット: `♻️ Replace the Type Info POOLED_THREAD Alarm with a coroutine debouncer`

## セクション 4: caret listener の tracker 統一

- [ ] `RescriptEditorCaretTracker` の callback を `(Editor) -> Unit` に変更、flow / impact / tracker テストを追従
- [ ] typeinfo を multicaster → tracker に移行 (FileEditorManagerListener は現状維持)
- [ ] ppx を multicaster → tracker に移行
- [ ] `./gradlew ktlintCheck test` green
- [ ] コミット: `♻️ Route Type Info and PPX caret tracking through RescriptEditorCaretTracker`

## セクション 5: ドキュメント同期

- [ ] `docs/product-requirements.md`: #128 を削除
- [ ] `docs/repository-structure.md`: util/ 行に debouncer 追記、ui/ 行の Alarm 言及を調整
- [ ] CLAUDE.md: 変更要否確認
- [ ] sphinx-docs: 更新なしの確認のみ
- [ ] コミット: `📝 Sync docs for Phase 4 coroutine migration`

## マージ前検証 (DoD Phase 3〜4)

- [ ] `./gradlew ktlintCheck clean buildPlugin test koverVerify verifyPluginStructure` green (test は実実行を確認)
- [ ] `koverHtmlReport` で debouncer のカバレッジ確認
- [ ] `./gradlew runIde` で design.md のスモークチェックリスト実施・結果記録
- [ ] tasklist 全項目 `[x]` 更新をマージ前最終コミットに含める
- [ ] `AskUserQuestion` でマージ可否確認
- [ ] main へマージ → ブランチ削除 → push

## テスト免除の記載

- `RescriptCoroutineScopeService`: IDE ライフサイクル依存 (platform の CoroutineScope 注入が前提) のため免除
- panel 群: 既存免除クラス。変更はデバウンス機構の差し替え配線のみ (機構自体は debouncer テストで担保)
