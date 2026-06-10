# 設計: 低リスク util 抽出 (Phase 1)

## セクション 1: `util/EditorTextFieldFactory`

### 新クラス

`com.rescript.plugin.util.EditorTextFieldFactory` (object) — `HtmlEditorPaneFactory` (20260519-003) と同型。

```kotlin
object EditorTextFieldFactory {
    /**
     * Applies the panel-standard editor settings (no line numbers, no folding
     * outline, no right margin) and forwards the editor to [customizer] for
     * per-panel tweaks.
     */
    fun applyPanelDefaults(
        field: EditorTextField,
        customizer: (EditorEx) -> Unit = {},
    ) {
        field.addSettingsProvider { editor ->
            editor.settings.isLineNumbersShown = false
            editor.settings.isFoldingOutlineShown = false
            editor.settings.isRightMarginShown = false
            customizer(editor)
        }
    }
}
```

- 生成自体 (コンストラクタ引数: document/fileType/project) は 3 panel で異なるため**集約しない** — 設定適用のみ集約する (シグネチャは実装時に EditorTextField 拡張関数とトップレベル object のどちらが ktlint/慣習に合うか確認して最終決定。既定は object + 関数)
- 固有設定 (focusable / softWraps / caretRow) は呼び出し側の customizer ラムダに残す。3 箇所しかないためフラグ引数化しない

### 呼び出し側変更

- `repl/RescriptReplPanel.kt:84` / `notebook/RescriptNotebookCellPanel.kt:58` / `typeinfo/RescriptTypeInfoPanel.kt:57` の `addSettingsProvider` ブロックを factory 呼び出し + customizer に置換

### テスト

- `util/EditorTextFieldFactoryTest.kt`: `EditorTextField` を生成して `applyPanelDefaults` 適用後、エディタ生成時に設定値が反映されることを assert。エディタ実体化が light fixture で困難な場合は、SettingsProvider が登録されること + customizer が呼ばれることの検証に切り替える (HtmlEditorPaneFactoryTest の手法を踏襲)
- 呼び出し側 3 panel は Swing UI 免除クラス (kover 除外済み) — 既存テストへの変更なし

## セクション 2: `util/RescriptProjectFileScanner`

### 新クラス

```kotlin
object RescriptProjectFileScanner {
    /**
     * Iterates project files of [fileTypes] inside a read action, reading each
     * file's text safely. [shouldContinue] is checked before each file; when it
     * returns false while files remain, the scan stops and `true` (truncated)
     * is returned. Unreadable files are skipped silently.
     */
    fun scanFiles(
        project: Project,
        fileTypes: List<FileType>,
        shouldContinue: () -> Boolean,
        onFile: (VirtualFile, String) -> Unit,
    ): Boolean  // truncated
}
```

- **truncated 判定の互換性**: 両 scanner とも「ループ先頭でキャップ判定 → 達していたら truncated=true で break」。`shouldContinue` を各ファイル処理前に評価する設計はこれと完全一致する
  - coverage: `shouldContinue = { perFile.size < MAX_FILES }`
  - interop: `shouldContinue = { collected.size < MAX_TOTAL }`
- interop の二段キャップ (`perFileBudget = (MAX_TOTAL - collected.size).coerceAtMost(MAX_PER_FILE)`) は `onFile` コールバック内に残す
- `FileTypeIndex.getFiles` の結果結合 (interop の `.res` + `.resi`) は `fileTypes` リストで吸収
- 読み取り失敗 (`contentsToByteArray` 例外) は現行どおり silent skip

### 呼び出し側変更

- `RescriptTypeCoverageScanner.scan` / `RescriptInteropScanner.scan` の本体ループを `scanFiles` 呼び出しに置換。**公開シグネチャ (`scan(project): ProjectCoverage` / `scan(project): Result`)、戻り値の構造、ソート処理は不変**

### テスト

- `util/RescriptProjectFileScannerTest.kt`: light fixture でファイルを用意し、(a) 全件走査、(b) shouldContinue=false で truncated=true、(c) fileTypes 複数指定、を assert
- 既存の `RescriptTypeCoverageScannerTest` / `RescriptInteropScannerTest` / interop IntegrationTest は**無変更で green** が受け入れ条件 (回帰検出器)

## セクション 3: docs 同期

- `docs/repository-structure.md`: util/ 行の代表クラス列に追記
- `docs/product-requirements.md`: #126 行を削除し実装済みへ (roadmap-format.md に従う)。scanner 共通化はロードマップ未登録のため追記不要 (steering が記録)
- CLAUDE.md: util/ の説明はかっこ書きの列挙のみなので変更最小限 (repository-structure.md 側で十分なら変更なし)

## 進め方・検証

- ブランチ: Claude Code worktree (`EnterWorktree`)。コミットは「セクション 1」「セクション 2」「セクション 3 (docs)」の 3 つ (各セクション独立ビルド可)
  - コミット 1: ✨ ではなく ♻️ (機能変更なしのリファクタリング) — `♻️ Extract EditorTextFieldFactory for shared panel editor settings`
  - コミット 2: `♻️ Extract RescriptProjectFileScanner for shared file scan loop`
  - コミット 3: `📝 Sync docs for Phase 1 util extractions` (+#126 実装済み移動)
  - 🚧 マーキング: worktree 内の最初のコミットに含める
- 検証: 各コミット前 `./gradlew ktlintCheck test`、マージ前 `./gradlew clean buildPlugin test koverVerify`

## リスク

| リスク | 緩和策 |
|---|---|
| util/ は kover 対象 + PIT (mutation test) 対象 | 新 2 クラスは小さくテスト同梱。マージ前 koverHtmlReport で 86% 維持確認 |
| scanner の truncated 挙動が微妙に変わる | shouldContinue 設計が現行のループ先頭判定と同型であることを既存テストで担保 |
| EditorTextField のエディタ実体化がテストで困難 | HtmlEditorPaneFactoryTest の検証手法に切り替え可能な設計にする |
