# Reason → ReScript Migration Pilot — Design

## 1. アーキテクチャ概要

```
┌──────────────────────────────────────────────────────┐
│ Tools > Show Reason Migration Pilot                  │
└─────────────────────┬────────────────────────────────┘
                      │ activate
                      ▼
┌──────────────────────────────────────────────────────┐
│ RescriptMigrationToolWindowFactory                   │
│ (com.intellij.toolWindow extension point)            │
└─────────────────────┬────────────────────────────────┘
                      │ creates
                      ▼
┌──────────────────────────────────────────────────────┐
│ RescriptMigrationPanel                               │
│  - JBList<MigrationCandidate> (with checkbox)        │
│  - Toolbar: Refresh / Select All / Clear / Convert   │
└──────────┬───────────────────────────┬───────────────┘
           │ enumerate                 │ convert
           ▼                           ▼
┌─────────────────────────┐ ┌─────────────────────────┐
│ RescriptMigrationFinder │ │ RescriptMigrationConvert│
│  - finds .re/.rei files │ │ er                      │
│    via FileTypeIndex    │ │  - ProcessBuilder       │
│    or VFS walk          │ │  - rescript convert CLI │
└─────────────────────────┘ └─────────────────────────┘
```

## 2. パッケージ構成

新規パッケージ `migration/` を追加する。

```
src/main/kotlin/com/rescript/plugin/migration/
├── RescriptMigrationToolWindowFactory.kt   # ToolWindow 登録
├── RescriptMigrationPanel.kt                # UI
├── RescriptMigrationAction.kt               # Tools メニュー
├── RescriptMigrationModel.kt                # MigrationCandidate / ConversionResult
├── RescriptMigrationFinder.kt               # `.re`/`.rei` 列挙
└── RescriptMigrationConverter.kt            # rescript convert ラッパー

src/test/kotlin/com/rescript/plugin/migration/
├── RescriptMigrationFinderTest.kt           # pure helper
└── RescriptMigrationConverterTest.kt        # parser + ProcessBuilder argv 構築
```

## 3. 主要クラス設計

### 3.1 RescriptMigrationModel

```kotlin
data class MigrationCandidate(
    val file: VirtualFile,
    val relativePath: String,
)

enum class ConversionStatus { SUCCESS, FAILED }

data class ConversionResult(
    val candidate: MigrationCandidate,
    val status: ConversionStatus,
    val message: String,            // stdout / stderr summary
)
```

### 3.2 RescriptMigrationFinder

```kotlin
object RescriptMigrationFinder {
    fun findCandidates(project: Project): List<MigrationCandidate>
    
    /** Pure helper: filters paths under project base, computes relative paths. */
    internal fun toCandidates(projectBasePath: String, files: Sequence<VirtualFile>): List<MigrationCandidate>
}
```

実装方針:
- IntelliJ Platform に `.re` 用の FileType を登録する必要はない（既存の Plain Text として扱われる）
- VFS から拡張子 `.re` `.rei` のファイルを列挙する: `FilenameIndex.getAllFilesByExt(project, "re", scope)` + 同 `"rei"`
- プロジェクトベースパス外のファイルは除外
- `node_modules/` は `projectScope` で自動除外

### 3.3 RescriptMigrationConverter

```kotlin
object RescriptMigrationConverter {
    fun convert(project: Project, candidate: MigrationCandidate): ConversionResult
    
    /** Pure helper: builds the argv for ProcessBuilder. */
    internal fun buildCommand(rescriptBinary: String, sourcePath: String): List<String>
}
```

実装方針:
- `RescriptProjectSettings.rescriptBinaryPath` を優先、なければ `npx rescript` を使う
- `ProcessBuilder(buildCommand(...))` で実行（タイムアウト 30 秒）
- 標準出力を変換結果として収集
- exit code が 0 → SUCCESS / 出力を `.res` として保存し、元ファイルを削除
- exit code が 0 以外 → FAILED / stderr をメッセージに

セキュリティ: `RescriptSecurityUtils` のパス検証ヘルパーがあれば使う（プロジェクト外のファイルを変換しない）。

### 3.4 RescriptMigrationPanel

`SimpleToolWindowPanel` + `JBList<MigrationCandidate>` with checkbox renderer:
- Checkbox 列で選択状態を管理（`MutableSet<VirtualFile>` で保持）
- Toolbar: Refresh, Select All, Clear, Convert Selected, Cancel
- 結果領域: `JBList<ConversionResult>`（成功/失敗を色分け）または status label
- Convert ボタンは pooled thread で順次変換

### 3.5 ToolWindow / Action

既存パターンに従う。
- ToolWindow ID: `ReScript Migration Pilot`
- Tools メニュー: `Show Reason Migration Pilot`

## 4. テスト戦略

| テスト種別 | 対象 | 手法 |
|-----------|------|------|
| Unit | `RescriptMigrationFinder.toCandidates` | プロジェクトベースパス + ファイルパス入力でテスト |
| Unit | `RescriptMigrationConverter.buildCommand` | rescriptBinary が "" / specified の両方をテスト |
| 免除 | `RescriptMigrationConverter.convert` | ProcessBuilder 実行のため fixture 要 |
| 免除 | `RescriptMigrationPanel` | Swing UI |
| 免除 | `RescriptMigrationToolWindowFactory` / `Action` | IDE ライフサイクル |
| 免除 | `RescriptMigrationFinder.findCandidates` | FilenameIndex のため fixture 要 |

## 5. プラグイン互換性

- IntelliJ Platform 2025.3+ の `FilenameIndex.getAllFilesByExt`
- LSP 不要
- Deprecated API なし

## 6. ドキュメント更新

- `CLAUDE.md` レイヤー 3 に `migration/` パッケージを追記
- `docs/repository-structure.md` パッケージ表に `migration/` を追加
- `docs/functional-design.md` に ToolWindow + Action を追加
- `README.md` Features セクションに「Reason → ReScript migration pilot」追加
- `sphinx-docs/user/features/advanced.md` に新セクション
- 日本語訳同時更新
- `docs/lsp-fallback-matrix.md` に「LSP 不要」行を追加
