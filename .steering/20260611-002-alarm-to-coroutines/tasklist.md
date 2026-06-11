# タスクリスト: Alarm → coroutines 移行 (Phase 4、#128)

セクション間依存: 2・3 は 1 の後。4 は独立 (3 と同じファイルを触るため 3 の後に実施)。5 (docs) は最後。
各セクション = 1 コミット = 独立にビルド・テスト通過可能な単位。

## セクション 0: セットアップ

- [ ] `git fetch origin` + main の ahead/behind 確認
- [ ] `EnterWorktree` で worktree 作成、`pwd` / `git rev-parse --show-toplevel` で編集パス確認
- [ ] `docs/product-requirements.md` の #128 に 🚧 (最初のコミットに含める)
- [ ] **`Dispatchers.EDT` と light service の CoroutineScope 注入が deprecated / `@ApiStatus.Internal` でないことをソース jar で確認し、結果をここに記録** (NG なら設計を見直してから進む)

## セクション 1: coroutine 基盤 (util/)

- [ ] `util/RescriptCoroutineScopeService.kt` 新規 (KDoc 付き)
- [ ] `util/RescriptCoroutineDebouncer.kt` 新規 (KDoc 付き)
- [ ] `util/RescriptCoroutineDebouncerTest.kt` 新規 (発火 / cancel-and-restart / cancel 後不発火)
- [ ] テスト免除の記録: `RescriptCoroutineScopeService` は IDE ライフサイクル依存 (platform の scope 注入) のため免除
- [ ] kover: scope service のクラス除外要否を確認 (util は対象パッケージ)
- [ ] `./gradlew ktlintCheck test` green
- [ ] コミット: `✨ Add project-scoped coroutine debouncer infrastructure`

## セクション 2: PanelBase の Alarm 置換

- [ ] `RescriptToolWindowPanelBase` のコンストラクタに `project` 追加、Alarm → debouncer (Dispatchers.EDT)、dispose で cancel
- [ ] `DualViewToolWindowPanel` + 5 panel (flow / diagram / coverage / impact / interop) の super 呼び出しを追従
- [ ] `./gradlew ktlintCheck test` green
- [ ] コミット: `♻️ Replace the panel base Alarm debounce with coroutines`

## セクション 3: TypeInfoPanel の Alarm 置換 (#128 本体)

- [ ] `Alarm(POOLED_THREAD)` + `@Suppress("UnstableApiUsage")` + 理由コメントを削除し debouncer (Dispatchers.Default) に置換
- [ ] 早期 return 経路の `debouncer.cancel()`、parentDisposable への cancel 接続
- [ ] `src/main` から `com.intellij.util.Alarm` import が消えたことを grep で確認
- [ ] `./gradlew ktlintCheck test` green
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
