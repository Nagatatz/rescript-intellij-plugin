# 設計: Build Console 専用 ToolWindow (#119)

## アーキテクチャ概要

レイヤー 3（IDE 統合機能）の ToolWindow として実装する。純ロジック（出力パーサ）と
IDE 結合（プロセス起動・UI）を分離し、テスト可能な核を最大化する。

```
RescriptBuildConsoleToolWindowFactory  (EP: toolWindow, 免除)
        └── RescriptBuildConsolePanel   (Swing UI, 免除)
                ├─ listens → RescriptBuildConsoleService  (@Service PROJECT, プロセス結合, 免除)
                │                   └─ uses → RescriptBuildOutputParser  (純ロジック, テスト必須)
                │                                   └─ produces → RescriptBuildDiagnostic  (data, テスト必須)
                └─ navigates via OpenFileDescriptor
```

## 確定した設計判断

| 論点 | 決定 | 理由 |
|------|------|------|
| プロセス起動 | `GeneralCommandLine` + `OSProcessHandler` | IDE 標準。`ProcessListener.onTextAvailable` で出力をストリーム取得でき、`Disposer` 連携でリーク防止が容易。`RescriptRunConfiguration` と同系統 |
| 構造化ビュー | `Tree`（ファイル別グルーピング） | 複数ファイルにまたがるエラーの俯瞰を優先。ルート = ファイルノード、子 = 診断ノード |
| ToolWindow id | `ReScript Build` | 既存命名（`ReScript REPL` / `ReScript Type`）に倣う |
| パーサの形態 | ステートレスな `object` の純関数 `parse(output: String)` | テスト容易。watch リセットは「最後の `>>>> Start compiling` 以降だけ解析」で表現 |

## クラス設計

### 1. `build/RescriptBuildDiagnostic.kt`（テスト必須）

```kotlin
/** Severity of a ReScript compiler diagnostic surfaced in the Build Console. */
enum class RescriptBuildSeverity { ERROR, WARNING }

/**
 * A single structured diagnostic parsed from `rescript build` output.
 *
 * Line / column values are 1-based as emitted by the ReScript compiler.
 */
data class RescriptBuildDiagnostic(
    val filePath: String,
    val line: Int,
    val colStart: Int,
    val colEnd: Int,
    val severity: RescriptBuildSeverity,
    val messageHead: String,   // ブロック先頭行（"We've found a bug for you!" 等）
)
```

### 2. `build/RescriptBuildOutputParser.kt`（テスト必須・コア）

```kotlin
/**
 * Parses raw `rescript build` stdout/stderr into structured diagnostics.
 *
 * Stateless: only the diagnostics belonging to the most recent build cycle
 * (everything after the last ">>>> Start compiling" marker) are returned, so
 * watch-mode output naturally resets between recompilations.
 */
object RescriptBuildOutputParser {
    fun parse(output: String): List<RescriptBuildDiagnostic>
}
```

パースアルゴリズム（行ストリームを順次走査する状態機械）:

1. `>>>> Start compiling` を検出したら、それまでの収集をすべて破棄（watch リセット）
2. severity ヘッダ行を検出し「保留中 severity + messageHead」として記憶:
   - `We've found a bug for you!` → ERROR
   - `Syntax error!` → ERROR
   - `Warning number <N>` → WARNING
3. 保留中 severity がある状態で**位置行**を検出したら診断を 1 件生成し、保留をクリア:
   - 位置行の正規表現（同一行/複数行両対応）:
     `^\s*(.+\.res[i]?):(\d+):(\d+)(?:-(?:(\d+):)?(\d+))?\s*$`
     - group1=path, group2=line, group3=colStart, group4=endLine(任意), group5=colEnd(任意)
     - `colEnd` 不在時は `colStart` を流用。複数行 (`line:col-line:col`) は startLine/startCol のみ採用
4. 数値変換失敗・パス未取得は **そのエントリを無視**（例外を投げない）
5. 走査終了時に収集済み診断リストを返す（出現順を維持）

### 3. `build/RescriptBuildConsoleListener.kt`（純インターフェース・KDoc のみ）

```kotlin
/** Notified when the Build Console service produces new diagnostics or changes state. */
interface RescriptBuildConsoleListener {
    fun onDiagnosticsUpdated(diagnostics: List<RescriptBuildDiagnostic>)
    fun onStateChanged(state: RescriptBuildState)
}

enum class RescriptBuildState { IDLE, RUNNING, STOPPED }
```

### 4. `build/RescriptBuildConsoleService.kt`（@Service(PROJECT)・免除）

責務:
- `startWatch()`: 既存 `RescriptCliDetector.findCli()` / `RescriptWorkspaceDiscovery.discover()` で
  CLI パスと作業ディレクトリを解決し、`GeneralCommandLine` を**明示的引数リスト**
  （`listOf(cliPath, "build", "-w")`）で構成。`OSProcessHandler` を起動
- `ProcessListener.onTextAvailable` で出力を `StringBuilder` に蓄積。デバウンス（`Alarm` /
  coroutine）後に `RescriptBuildOutputParser.parse(buffer)` を呼び、リスナへ通知
- `stopWatch()`: `processHandler.destroyProcess()`。state を STOPPED に
- `clear()`: バッファと診断をリセット
- `Disposable` 実装。dispose 時にプロセス終了（リーク防止）
- リスナ登録/解除（パネルが購読）

セキュリティ: 引数は固定リスト。ユーザー入力を文字列連結しない。解決したパスはログ/UI に絶対表示しない。
テスト免除理由（tasklist に明記）: 外部プロセス結合 + IDE Service ライフサイクル依存。
純ロジックは `RescriptBuildOutputParser` に切り出し済み。

### 5. `build/RescriptBuildConsoleToolWindowFactory.kt`（ToolWindowFactory・免除）

`RescriptReplToolWindowFactory` と同型。`DumbAware` 実装、`createToolWindowContent` で
`RescriptBuildConsolePanel` をマウント。`shouldBeAvailable` は常に true。

### 6. `build/RescriptBuildConsolePanel.kt`（Swing UI・免除）

- `RescriptToolWindowPanelBase` を継承（toolbar + center + statusLabel の共有レイアウト）
- center: `Tree`。モデルはファイルノード → 診断子ノード。severity アイコン（error/warning）
- toolbar アクション:
  - Start watch（`AllIcons.Actions.Execute`）→ `service.startWatch()`
  - Stop（`AllIcons.Actions.Suspend`）→ `service.stopWatch()`
  - Clear（`AllIcons.Actions.GC` 等）→ `service.clear()`
- statusLabel: `Idle` / `Building…` / `N errors, M warnings`
- ツリーノードのダブルクリック + Enter キー → `OpenFileDescriptor(project, vFile, line-1, colStart-1).navigate(true)`
  - パス→VirtualFile 解決は `VirtualFileManager` / `LocalFileSystem`。相対パスは作業ディレクトリ基準
- `RescriptBuildConsoleListener` を実装し、UI 更新は `invokeLater` / EDT で実施
- パネル dispose 時にリスナ解除

### 7. plugin.xml 登録

`<extensions defaultExtensionNs="com.intellij">` 内、既存 toolWindow 群の並びに従って追加:

```xml
<toolWindow id="ReScript Build" anchor="bottom" icon="/icons/rescript-file.svg"
            factoryClass="com.rescript.plugin.build.RescriptBuildConsoleToolWindowFactory"/>
```

（専用アイコンは初版では既存 `/icons/rescript-file.svg` を流用。専用アイコン追加は将来拡張）

## テスト戦略

| クラス | テスト | 種別 |
|--------|--------|------|
| `RescriptBuildOutputParser` | `RescriptBuildOutputParserTest`（fixture 不要・純ロジック） | 必須 |
| `RescriptBuildDiagnostic` | パーサテスト内で間接検証（data class、自明） | data class 免除 |
| `RescriptBuildConsoleListener` | ロジックなし interface | 免除 |
| `RescriptBuildConsoleService` | 外部プロセス + Service ライフサイクル | 免除（理由を tasklist へ） |
| `RescriptBuildConsoleToolWindowFactory` | ToolWindowFactory | 免除 |
| `RescriptBuildConsolePanel` | Swing UI | 免除 |

パーサテストケース（受け入れ条件 1〜6 を網羅）:
- エラー 1 件 / 警告 1 件 / 構文エラー / 混在複数件 / watch リセット / 無関係出力で空 /
  不正な行列番号で無視 / 複数行位置 (`3:9-5:2`) で startLine/startCol 採用 / `.resi` パス

## 影響範囲・既存コードへの変更

- 新規パッケージ `com.rescript.plugin.build`（新規ディレクトリ）
- `plugin.xml` に toolWindow 1 行追加（共有ファイル）
- 既存クラスの変更なし（`RescriptCliDetector` / `RescriptWorkspaceDiscovery` は read-only 利用）

## ドキュメント更新対象

- `docs/repository-structure.md` — `build/` パッケージ行を追加
- `docs/functional-design.md` — ToolWindow カテゴリに Build Console を追記
- `README.md` — Features に 1 項目
- `sphinx-docs/user/features/` — 該当 ToolWindow ページ（EN）+ `.po` 日本語訳
- `docs/product-requirements.md` — ロードマップ #119 行を削除
- CLAUDE.md — レイヤー 3 の ToolWindow 列挙に「Build Console」を追記（既存列挙があるため）
