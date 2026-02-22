# Design: External Annotator (Format Check)

## アーキテクチャ

既存の `RescriptReanalyzeAnnotator` と同じ ExternalAnnotator 3 フェーズパターンを採用する。

## コンポーネント

### 1. RescriptFormatCheckAnnotator

**パッケージ:** `com.rescript.plugin.analysis`
**親クラス:** `ExternalAnnotator<CollectedInfo, AnnotationResult>`

#### Phase 1: collectInformation (EDT)

```
入力: PsiFile
出力: CollectedInfo? (filePath, projectBasePath, documentText, extension)

1. file が RescriptFile でなければ return null
2. 設定で formatCheckEnabled が false なら return null
3. virtualFile, project.basePath を取得（null なら return null）
4. RescriptCliDetector.findCli() で CLI パスを検出（見つからなければ return null）
5. ドキュメントのテキストを取得（ReadAction 内）
6. CollectedInfo を返す
```

#### Phase 2: doAnnotate (Background Thread)

```
入力: CollectedInfo
出力: AnnotationResult?

1. RescriptCliDetector.findCli() で CLI パスを再検出
2. GeneralCommandLine(cliPath, "format", "--stdin", ".<ext>") を構築
3. プロセスを起動し、stdin にドキュメントテキストを書き込む
4. stdout から整形結果を読み取る
5. タイムアウト (PROCESS_TIMEOUT_SECONDS) を適用
6. exitCode != 0 の場合は return null（構文エラー等）
7. stdout == documentText なら return null（既にフォーマット済み）
8. 差分がある場合は AnnotationResult を返す
```

#### Phase 3: apply (EDT)

```
入力: PsiFile, AnnotationResult?, AnnotationHolder
出力: void

1. result が null なら return
2. ファイル先頭（offset 0）に INFO アノテーションを作成
3. Quick Fix (RescriptFormatQuickFix) を付与
```

### 2. RescriptFormatQuickFix

**パッケージ:** `com.rescript.plugin.analysis`

- `LocalQuickFix` を実装
- `applyFix()` で `ReformatCodeProcessor` を呼び出す（既存の `RescriptFormattingService` が連携される）

### 3. 設定拡張

**既存ファイル:** `RescriptProjectSettings.kt`, `RescriptConfigurable.kt`

- `State` に `formatCheckEnabled: Boolean = false` を追加
- 設定 UI にチェックボックスを追加

## データクラス

```kotlin
data class CollectedInfo(
    val filePath: String,
    val projectBasePath: String,
    val documentText: String,
    val extension: String  // "res" or "resi"
)

data class AnnotationResult(
    val message: String
)
```

## ファイル構成

| ファイル | 種別 |
|---------|------|
| `src/main/kotlin/com/rescript/plugin/analysis/RescriptFormatCheckAnnotator.kt` | 新規 |
| `src/test/kotlin/com/rescript/plugin/analysis/RescriptFormatCheckAnnotatorTest.kt` | 新規 |
| `src/main/kotlin/com/rescript/plugin/settings/RescriptProjectSettings.kt` | 変更 |
| `src/main/kotlin/com/rescript/plugin/settings/RescriptConfigurable.kt` | 変更 |
| `src/main/resources/META-INF/plugin.xml` | 変更 |

## plugin.xml 登録

```xml
<externalAnnotator language="ReScript"
                   implementationClass="com.rescript.plugin.analysis.RescriptFormatCheckAnnotator"/>
```

## セキュリティ考慮

- CLI パスは `RescriptCliDetector.findCli()` で安全に検出
- プロセスは `GeneralCommandLine` + 明示的引数リスト
- タイムアウトは `RescriptSecurityUtils.PROCESS_TIMEOUT_SECONDS`
- stdin/stdout/stderr は別スレッドでデッドロック防止
