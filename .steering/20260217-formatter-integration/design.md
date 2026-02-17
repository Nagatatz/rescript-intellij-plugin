# Design: フォーマッタ連携

## 実装アプローチ

`AsyncDocumentFormattingService` を継承し、`rescript format --stdin .<ext>` CLI をサブプロセスで呼び出す。既存の `RescriptCliDetector` を再利用して CLI パスを解決する。

### API 選定理由

- `AsyncDocumentFormattingService`（`com.intellij.formatting.service`）は外部フォーマッタ連携用の公式 API
- `FormattingTask` を返すことで、バックグラウンドスレッドでの実行とキャンセルをフレームワークが管理
- Shell Script プラグイン（`ShExternalFormatter`）と同一パターン
- Extension point: `com.intellij.formattingService`

### コマンド仕様

```
rescript format --stdin .res   # .res ファイル用
rescript format --stdin .resi  # .resi ファイル用
```

- stdin: フォーマット前のソースコード
- stdout: フォーマット済みソースコード
- 終了コード 0: 成功、非ゼロ: 失敗（構文エラー等）
- stderr: エラーメッセージ

## 変更するコンポーネント

### 新規ファイル

#### `src/main/kotlin/com/rescript/plugin/formatter/RescriptFormattingService.kt`

`AsyncDocumentFormattingService` を継承。

```kotlin
package com.rescript.plugin.formatter

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.formatting.service.AsyncDocumentFormattingService
import com.intellij.formatting.service.AsyncFormattingRequest
import com.intellij.formatting.service.FormattingService.Feature
import com.intellij.psi.PsiFile
import com.rescript.plugin.RescriptFileType
import com.rescript.plugin.RescriptInterfaceFileType
import com.rescript.plugin.run.RescriptCliDetector
import java.io.IOException

class RescriptFormattingService : AsyncDocumentFormattingService() {

    companion object {
        private const val NOTIFICATION_GROUP = "ReScript"
        private const val TIMEOUT_MS = 10_000
    }

    override fun getFeatures(): Set<Feature> = emptySet()

    override fun canFormat(file: PsiFile): Boolean =
        file.fileType is RescriptFileType || file.fileType is RescriptInterfaceFileType

    override fun createFormattingTask(request: AsyncFormattingRequest): FormattingTask? {
        val project = request.context.project
        val ioFile = request.ioFile ?: return null
        val ext = ioFile.extension ?: "res"

        val cliPath = RescriptCliDetector.findCli(
            ioFile.parent,
            project.basePath
        ) ?: run {
            request.onError("ReScript", "rescript CLI not found in node_modules")
            return null
        }

        val documentText = request.documentText

        return object : FormattingTask {
            private var process: Process? = null

            override fun run() {
                try {
                    val commandLine = GeneralCommandLine(cliPath, "format", "--stdin", ".$ext")
                        .withCharset(Charsets.UTF_8)

                    val proc = commandLine.createProcess()
                    process = proc

                    // stdin への書き込み（デッドロック防止のため別スレッド）
                    val stdinThread = Thread("rescript-format-stdin") {
                        try {
                            proc.outputStream.bufferedWriter(Charsets.UTF_8).use {
                                it.write(documentText)
                            }
                        } catch (_: IOException) {
                            // プロセスが既に終了した場合
                        }
                    }
                    stdinThread.start()

                    // stderr を別スレッドで読み取り（デッドロック防止）
                    val stderr = StringBuilder()
                    val stderrThread = Thread("rescript-format-stderr") {
                        try {
                            proc.errorStream.reader(Charsets.UTF_8).use {
                                stderr.append(it.readText())
                            }
                        } catch (_: IOException) {}
                    }
                    stderrThread.start()

                    // stdout をメインスレッドで読み取り
                    val stdout = proc.inputStream.reader(Charsets.UTF_8).use {
                        it.readText()
                    }

                    stdinThread.join(TIMEOUT_MS.toLong())
                    stderrThread.join(TIMEOUT_MS.toLong())
                    val exitCode = proc.waitFor()

                    if (exitCode == 0 && stdout.isNotEmpty()) {
                        request.onTextReady(stdout)
                    } else {
                        request.onError(
                            "ReScript",
                            stderr.toString().ifBlank { "rescript format failed (exit code $exitCode)" }
                        )
                    }
                } catch (e: Exception) {
                    request.onError("ReScript", e.message ?: "Unknown error")
                }
            }

            override fun cancel(): Boolean {
                process?.destroyForcibly()
                return true
            }
        }
    }

    override fun getNotificationGroupId(): String = NOTIFICATION_GROUP

    override fun getName(): String = "rescript format"
}
```

### 変更ファイル

#### `src/main/resources/META-INF/plugin.xml`

`com.intellij.formattingService` extension point に登録:

```xml
<!-- Code formatting (rescript format CLI) -->
<formattingService
    implementation="com.rescript.plugin.formatter.RescriptFormattingService"/>
```

## スレッディング設計

`FormattingTask.run()` はフレームワークによりバックグラウンドスレッドで呼び出される。内部でのサブプロセス I/O は以下の 3 スレッドで並行処理し、パイプバッファのデッドロックを防止する:

```
FormattingTask.run() スレッド (stdout 読み取り)
├── rescript-format-stdin  スレッド (stdin 書き込み → close)
└── rescript-format-stderr スレッド (stderr 読み取り)
```

## 影響範囲

| 対象 | 変更内容 |
|------|---------|
| `RescriptCliDetector` | 変更なし（既存 API を再利用） |
| `formatter/` パッケージ | 新規作成（1 ファイル） |
| `plugin.xml` | `formattingService` 登録追加 |
| `CLAUDE.md` | プロジェクト構成に `formatter/` 追記 |

## 既存機能との関係

- **LSP ドキュメントフォーマット**: LSP が起動している場合、LSP のフォーマットと本サービスの両方が利用可能になる。`AsyncDocumentFormattingService` は `canFormat()` で対象ファイルを判定し、IntelliJ は登録された `formattingService` を IDE 内蔵フォーマッタより優先する。LSP のフォーマットとの優先順位は IntelliJ Platform が管理する。
- **Undo 対応**: `onTextReady()` 経由のテキスト置換は IntelliJ の Undo/Redo フレームワークに自動統合される。
