# 要求内容: Alarm → coroutines 移行 (完全リファクタリング Phase 4、#128)

## 背景

完全リファクタリング計画の Phase 4。ロードマップ #128 の実施。Phase 2 で `Alarm(SWING_THREAD)` デバウンスは `ui/RescriptToolWindowPanelBase` の 1 箇所に集約済みのため、置換対象は「基盤 1 箇所 + typeinfo の `Alarm(POOLED_THREAD)`」に限定されている。

## 対象 (実コードで確認済み・2026-06-11)

### 1. `ui/RescriptToolWindowPanelBase` の `Alarm(SWING_THREAD)` (デバウンス 200ms、flow / impact が利用)

- `@Suppress` 不要だが、Alarm 自体を coroutines に置換して typeinfo と機構を統一する

### 2. `typeinfo/RescriptTypeInfoPanel` の `Alarm(POOLED_THREAD)` (#128 本体)

- `Alarm.ThreadToUse.POOLED_THREAD` は `@ApiStatus.Internal` で `@Suppress("UnstableApiUsage")` 付き — これの排除が #128 の受け入れ条件
- デバウンス 300ms 後に LSP hover (ブロッキング) を BG スレッドで実行し、`invokeLater` で EDT 表示

### 3. (Phase 2 残課題の再評価) typeinfo / ppx の multicaster caret listener

- 両 panel は `eventMulticaster.addCaretListener` で **全プロジェクトの全エディタ** のイベントを受けており、project フィルタがない
- 実害確認: 別プロジェクトのエディタで caret を動かすと、typeinfo は「他プロジェクトの file × 自プロジェクトの LSP」で hover を発行し、ppx は他プロジェクトの `.res` 内容で自 panel を更新する — **クロスプロジェクトの取り違え**
- Phase 2 で新設した `ui/RescriptEditorCaretTracker` (project フィルタ付き) へ統一することでこれを修正する。tracker の callback に `Editor` 引数を追加する必要がある (typeinfo / ppx は editor/document を使うため)

## 要求

1. `util/` に coroutine 基盤を新設する:
   - `RescriptCoroutineScopeService` — `@Service(Service.Level.PROJECT)` light service で platform 注入の `CoroutineScope` を保持 (プロジェクトクローズで自動 cancel)
   - `RescriptCoroutineDebouncer` — cancel-and-restart デバウンス (`Job` 管理、dispatcher 注入可能、テスト可能)
2. `RescriptToolWindowPanelBase` の Alarm を debouncer (`Dispatchers.EDT`) に置換する。基盤コンストラクタに `project` を追加 (5 panel + DualView が追従)
3. `RescriptTypeInfoPanel` の Alarm を debouncer (`Dispatchers.Default`) に置換し、`@Suppress("UnstableApiUsage")` と理由コメントを削除する
4. `RescriptEditorCaretTracker` の callback を `(Editor) -> Unit` に変更し (flow / impact は引数を無視)、typeinfo / ppx を multicaster から tracker に移行する
5. ロードマップ #128 の進捗管理 (🚧 → 完了時に削除)

## 受け入れ条件

- [ ] `src/main` から `com.intellij.util.Alarm` の import が消えている
- [ ] `@Suppress("UnstableApiUsage")` が typeinfo から消えている (プロジェクト全体で UnstableApiUsage suppress が純減)
- [ ] 新規利用する coroutine API (`Dispatchers.EDT` 等) が deprecated / `@ApiStatus.Internal` でないことを確認している
- [ ] debouncer にユニットテストがある (デバウンス動作 / cancel-and-restart / dispose 後の不発火)
- [ ] 既存テスト + `./gradlew ktlintCheck clean buildPlugin test koverVerify verifyPluginStructure` green (minBound 86 維持)
- [ ] `runIde` 手動スモーク: Type Info / Variant Flow / Type Impact / PPX でカーソル連打 → デバウンスが効く、プロジェクトクローズで例外なし
- [ ] docs 同期 (#128 削除、repository-structure.md の util/ 行更新)。sphinx 更新なし

## 挙動変更 (意図的・要承認)

- typeinfo / ppx が **自プロジェクトのエディタの caret イベントのみ** に反応するようになる (従来は全プロジェクト)。クロスプロジェクトの取り違え修正であり、単一プロジェクト利用では挙動不変

## スコープ外

- LSP hover 呼び出し自体の suspend 化 (ブロッキング呼び出しを BG dispatcher で包む現行方式を維持)
- 他パッケージへの coroutine 展開
