# 設計: Alarm → coroutines 移行 (Phase 4、#128)

4 セクション + docs。各セクション = 1 コミット = 独立マージ可能。

## セクション 1: coroutine 基盤 (`util/`)

```kotlin
@Service(Service.Level.PROJECT)
internal class RescriptCoroutineScopeService(
    val scope: CoroutineScope,  // platform が注入、プロジェクトクローズで cancel
)

internal class RescriptCoroutineDebouncer(
    private val scope: CoroutineScope,
    private val delayMs: Long,
    private val context: CoroutineContext,  // Dispatchers.EDT or Default
) {
    private var job: Job? = null

    @Synchronized
    fun schedule(action: suspend () -> Unit) {
        job?.cancel()
        job = scope.launch(context) {
            delay(delayMs)
            action()
        }
    }

    @Synchronized
    fun cancel() { job?.cancel() }
}
```

- coroutines は IntelliJ Platform バンドルの kotlinx-coroutines を使用 (依存追加なし)
- light service の `CoroutineScope` コンストラクタ注入は Platform 公式パターン (2024.1+)。**実装時に `Dispatchers.EDT` (`com.intellij.openapi.application.EDT`) と scope 注入が deprecated / Internal でないことをソース jar で確認し、結果を tasklist に記録する** (deprecated-api.md の手順)
- `schedule(action)` は呼び出しごとに action を受ける — typeinfo はキャプチャ済み offset/file を closure で渡し、PanelBase は `{ doRefresh() }` を渡す
- テスト: `RescriptCoroutineDebouncerTest` — 実 dispatcher + CountDownLatch 方式 (coroutines-test 依存を増やさない):
  - schedule → 余裕を持った待機で action 実行を assert
  - 連続 schedule → 先行がキャンセルされ最後の 1 件のみ実行
  - cancel 後は不発火
- `RescriptCoroutineScopeService` はテスト免除 (IDE ライフサイクル依存: platform の scope 注入が前提)

## セクション 2: `RescriptToolWindowPanelBase` の置換

```kotlin
abstract class RescriptToolWindowPanelBase(
    project: Project,                      // 追加
    private val toolbarPlace: String,
    debounceMs: Int = 0,
) : SimpleToolWindowPanel(true, true), Disposable {
    private val debouncer: RescriptCoroutineDebouncer? =
        if (debounceMs > 0) {
            RescriptCoroutineDebouncer(
                project.service<RescriptCoroutineScopeService>().scope,
                debounceMs.toLong(),
                Dispatchers.EDT,
            )
        } else null

    protected fun scheduleRefresh() {
        debouncer?.schedule { doRefresh() } ?: doRefresh()
    }

    override fun dispose() { debouncer?.cancel() }
}
```

- **挙動パリティ**: `Alarm(SWING_THREAD)` は delay 後 EDT で実行 → `launch(Dispatchers.EDT) { delay(...) }` も同じ。cancelAllRequests + addRequest = `schedule` の cancel-and-restart
- コンストラクタ変更の追従: `DualViewToolWindowPanel` (project を素通し) + 5 panel (`super(project, TOOLBAR_PLACE, ...)`)
- dispose で pending job を cancel (従来は Alarm の child disposable 解放と等価)

## セクション 3: `RescriptTypeInfoPanel` の置換 (#128 本体)

- `Alarm(POOLED_THREAD, parentDisposable)` + `@Suppress("UnstableApiUsage")` + 理由コメント (L63-72) を削除
- `RescriptCoroutineDebouncer(scope, 300, Dispatchers.Default)` に置換。`scheduleUpdate` は現行どおり EDT で offset/file をキャプチャし、`debouncer.schedule { getHoverType(...); showMessage(...) }` (BG 実行 → showMessage は従来どおり invokeLater)
- 早期 return 経路 (非 ReScript ファイル) は `debouncer.cancel()` してから showMessage (現行の cancelAllRequests 相当)
- parentDisposable に `Disposer.register(parentDisposable) { debouncer.cancel() }` で pending job の解放を接続
- KDoc の「300ms debounce」記述は維持 (挙動不変)

## セクション 4: caret listener の tracker 統一 (Phase 2 残課題)

- `RescriptEditorCaretTracker.install` の callback を `() -> Unit` → `(Editor) -> Unit` に変更
  - flow / impact: `{ _ -> scheduleRefresh() }` (引数無視、挙動不変)
  - tracker のテストも引数付き callback に追従 (assert 内容は不変 + 渡された editor の同一性 assert を追加)
- typeinfo: `eventMulticaster.addCaretListener` → `RescriptEditorCaretTracker.install(project, parentDisposable) { editor -> scheduleUpdate(editor) }`
  - FileEditorManagerListener (アクティブファイル切替) は messageBus 由来で project スコープ済みのため現状維持
- ppx: 同様に tracker へ移行 (callback 内の document / file 判定ロジックは不変)
- **挙動変更**: 両 panel が自プロジェクトのエディタのみに反応 (クロスプロジェクト取り違えの修正)。requirements の「挙動変更」節を参照

## セクション 5: docs 同期

- `docs/product-requirements.md`: #128 を削除 (リファクタリング候補テーブルが空になる場合はテーブルごと削除せず空テーブル + 注記を残すか実装時判断)
- `docs/repository-structure.md`: util/ 行に `RescriptCoroutineDebouncer` 追記、ui/ 行の説明を「Alarm」→「coroutine デバウンス」に調整
- CLAUDE.md: 変更要否確認 (Alarm 言及がなければ不要)

## 手動スモークチェックリスト (`./gradlew runIde`)

1. **Type Info**: ToolWindow を開く → `.res` でカーソル連打 → 300ms デバウンス後に型表示が追従 → 非 ReScript ファイルで "No ReScript file selected"
2. **Variant Flow / Type Impact**: カーソル連打でデバウンス → 図/一覧が追従
3. **PPX View**: `@react.component` 等のある `.res` で caret 移動 → 注釈一覧が更新
4. **プロジェクトクローズ → 再オープン**: 例外がログに出ない (scope cancel の確認)
5. 余裕があれば: 2 プロジェクト同時に開き、片方のエディタ操作がもう片方の Type Info / PPX を更新**しない**こと (挙動変更の確認)

## リスクと緩和

| リスク | 緩和策 |
|---|---|
| スレッディング退行 (EDT 違反 / leak) | dispatcher を Alarm の従来スレッドと 1:1 対応 (SWING→EDT, POOLED→Default)。スモーク 1-4 で確認 |
| `Dispatchers.EDT` が Internal だった場合 | 実装冒頭で annotation を確認し、Internal なら `withContext(ModalityState)` 系の代替を再設計してから進める (先に確認、後で直さない) |
| デバウンスのタイミング差 (Alarm vs delay) | どちらも「最後の要求から N ms」のセマンティクス。debouncer テストで cancel-and-restart を直接検証 |
| 自動テストが薄い panel への影響 | 変更面を debouncer (テスト有) に寄せ、panel 側は配線のみ。スモーク必須 |
