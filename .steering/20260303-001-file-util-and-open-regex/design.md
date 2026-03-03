# 設計書: ファイルユーティリティ統一 + open Regex 統一リファクタリング

## 1. RescriptFileUtil

### API 設計

```kotlin
object RescriptFileUtil {
    const val RES_EXTENSION = "res"
    const val RESI_EXTENSION = "resi"
    val RESCRIPT_EXTENSIONS: Set<String> = setOf(RES_EXTENSION, RESI_EXTENSION)

    fun isRescriptFile(file: VirtualFile): Boolean
    fun isResFile(file: VirtualFile): Boolean
    fun isResiFile(file: VirtualFile): Boolean
    fun isRescriptFileName(fileName: String): Boolean
    fun isResFileName(fileName: String): Boolean
    fun isResiFileName(fileName: String): Boolean
    fun findCounterpartFile(file: VirtualFile): VirtualFile?
    fun findInterfaceFile(resFile: VirtualFile): VirtualFile?
}
```

### 更新対象ファイル（20ファイル）

プラン本文の表を参照。

## 2. open 文 Regex パターン

### RescriptRegexPatterns に追加する 4 パターン

| 定数名 | パターン | 用途 |
|--------|---------|------|
| `OPEN_STATEMENT` | `(?m)^open\s+\S+` | open 文のマッチ（キャプチャなし） |
| `OPEN_MODULE_CAPTURE` | `(?m)^open\s+(\S+)` | モジュール名をキャプチャ |
| `OPEN_MODULE_STRICT` | `^\s*open\s+([A-Z][\w.]*)\s*$` (MULTILINE) | 厳密な行単位マッチ |
| `OPEN_LINE_TEST` | `^\s*open\s` (MULTILINE) | open 行の存在チェック |

### 更新対象ファイル（5ファイル）

| ファイル | 現在のパターン | 置換先 |
|---------|--------------|--------|
| `imports/RescriptImportUtil.kt` | `OPEN_PATTERN`, `OPEN_CAPTURE_PATTERN` | `OPEN_STATEMENT`, `OPEN_MODULE_CAPTURE` |
| `diagram/RescriptDependencyDiagramProvider.kt` | `OPEN_PATTERN` | `OPEN_MODULE_CAPTURE` |
| `navigation/RescriptFileIncludeProvider.kt` | `OPEN_PATTERN` | `OPEN_MODULE_STRICT` |
| `worksheet/RescriptWorksheetRunner.kt` | `OPEN_PATTERN` | `OPEN_LINE_TEST` |
| `editor/RescriptCommentEvalProvider.kt` | インライン `Regex(...)` | `OPEN_LINE_TEST` |

### 統一できない例外

`RescriptImportUtil.kt:100` の動的パターン `Regex("""(?m)^open\s+$moduleName\s*$""")` はランタイム変数を含むため対象外。
