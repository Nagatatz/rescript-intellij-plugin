# 設計書: A 優先度機能 (#112, #113, #114)

## #112 ビルド自動開始プロンプト

### 方針

`RescriptLspStartupActivity` と同じパターンで `ProjectActivity` を実装する。プロジェクト起動時に条件を確認し、バルーン通知で `rescript build -w` の開始を提案する。

### 新規ファイル

| ファイル | 説明 |
|---------|------|
| `run/RescriptBuildWatchStartupActivity.kt` | ProjectActivity 実装 |
| `run/RescriptBuildWatchStartupActivityTest.kt` | テスト（免除対象: IDE ライフサイクル依存） |

### 設計詳細

```kotlin
class RescriptBuildWatchStartupActivity : ProjectActivity {
    override suspend fun execute(project: Project) {
        if (PropertiesComponent.getInstance(project).getBoolean(DISMISSED_KEY, false)) return
        if (!RescriptLspDetector.isRescriptProject(project.basePath)) return
        if (RescriptCliDetector.findCli(project.basePath, project.basePath) == null) return
        showBuildWatchNotification(project)
    }
}
```

**通知アクション:**
- **Start Build Watch** — `RescriptRunConfiguration` を `BUILD_WATCH` コマンドで作成・実行
- **Don't show again** — `PropertiesComponent` に dismiss 状態を保存

**実行方法:**
```kotlin
val factory = RescriptRunConfigurationType.getInstance().configurationFactories.first()
val settings = RunManager.getInstance(project)
    .createConfiguration("ReScript: build -w", factory)
val config = settings.configuration as RescriptRunConfiguration
config.options.command = RescriptCommand.BUILD_WATCH.id
RunManager.getInstance(project).addConfiguration(settings)
ProgramRunnerUtil.executeConfiguration(settings, DefaultRunExecutor.getRunExecutorInstance())
```

### plugin.xml 登録

```xml
<postStartupActivity implementation="com.rescript.plugin.run.RescriptBuildWatchStartupActivity"/>
```

---

## #113 Dump LSP State

### 方針

`RescriptRestartLspAction` と同じパターンで Tools メニューアクションを実装する。rescript-language-server はカスタムリクエストとして `rescript/dumpState` はサポートしていないが、LSP 標準の接続情報やサーバープロセス情報を収集して表示する。

### 新規ファイル

| ファイル | 説明 |
|---------|------|
| `lsp/RescriptDumpLspStateAction.kt` | AnAction 実装 |
| `lsp/RescriptDumpLspStateActionTest.kt` | テスト（免除対象: LSP 結合必須） |

### 設計詳細

LSP サーバーが独自の dump コマンドをサポートしていないため、プラグイン側で収集可能な情報を表示する:

1. **LSP サーバー稼働状態** — `LspServerManager` から取得
2. **サーバープロセス情報** — PID、起動時間
3. **プロジェクト設定** — LSP パス、各種設定値
4. **検出情報** — rescript.json パス、CLI パス、node パス

**表示方法:** テキストダイアログ（`Messages.showMultilineInputDialog` はない。スクロール可能なテキストエリアを持つ `DialogWrapper` を使用する。ただし、シンプルに `Notification` のログウィンドウに出力する方法もある）。

→ シンプルさを優先し、**Notification（INFORMATION）でサマリを表示 + Event Log に詳細を出力**する方式を採用する。

```kotlin
class RescriptDumpLspStateAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val state = collectLspState(project)
        NotificationGroupManager.getInstance()
            .getNotificationGroup("ReScript")
            .createNotification("ReScript LSP State", state, NotificationType.INFORMATION)
            .notify(project)
    }

    private fun collectLspState(project: Project): String {
        // LSP server status, project settings, detected paths
    }
}
```

### plugin.xml 登録

```xml
<action id="ReScript.DumpLspState"
        class="com.rescript.plugin.lsp.RescriptDumpLspStateAction"
        text="Dump ReScript LSP State"
        description="Show diagnostic information about the ReScript LSP server">
    <add-to-group group-id="ToolsMenu" anchor="last"/>
</action>
```

---

## #114 offset↔position 変換共通化

### 方針

`RescriptOffsetUtils` ユーティリティオブジェクトを作成し、散在する offset↔position 変換ロジックを集約する。調査結果では 18+ ファイルに 60+ 箇所の重複が確認された。

### 新規ファイル

| ファイル | 説明 |
|---------|------|
| `util/RescriptOffsetUtils.kt` | ユーティリティオブジェクト |
| `util/RescriptOffsetUtilsTest.kt` | テスト |

### API 設計

```kotlin
object RescriptOffsetUtils {
    /** Convert editor offset to LSP Position(line, character). */
    fun offsetToPosition(document: Document, offset: Int): Position {
        val line = document.getLineNumber(offset)
        val lineStart = document.getLineStartOffset(line)
        return Position(line, offset - lineStart)
    }

    /** Convert LSP Position to editor offset. Returns -1 if out of bounds. */
    fun positionToOffset(document: Document, position: Position): Int {
        if (position.line < 0 || position.line >= document.lineCount) return -1
        val lineStart = document.getLineStartOffset(position.line)
        return lineStart + position.character
    }
}
```

### リファクタリング対象ファイル

offset→Position 変換（`document.getLineNumber` + `offset - getLineStartOffset` パターン）を使用するファイル:

| ファイル | 置換箇所数 |
|---------|-----------|
| `lsp/RescriptLspUtils.kt` | 1 |
| `lsp/RescriptExpressionTypeProvider.kt` | 1 |
| `refactor/RescriptRenameHandler.kt` | 2（offsetToPosition + positionToOffset private methods を削除） |
| `refactor/RescriptExtractVariableHandler.kt` | 2 |
| `refactor/RescriptExtractFunctionHandler.kt` | 2 |
| `refactor/RescriptExtractComponentHandler.kt` | 2 |
| `refactor/RescriptInlineHandler.kt` | 3 |
| `intention/RescriptFilterMapChainIntention.kt` | 4 |
| `intention/RescriptExpandDestructuringIntention.kt` | 4 |
| `intention/RescriptAddTypeAnnotationIntention.kt` | 4 |
| `intention/RescriptCaseSplitIntention.kt` | 2 |
| `editor/RescriptEnterHandler.kt` | 2 |
| `editor/RescriptSmartEnterProcessor.kt` | 2 |
| `quickfix/RescriptReanalyzeQuickFix.kt` | 5 |

**注意:** `document.getLineStartOffset()` / `document.getLineEndOffset()` のみを使用するケース（折りたたみ、インデント計算等）は LSP Position 変換とは異なる用途のため、今回のスコープ外とする。

---

## 共通事項

### テスト方針

| 機能 | テスト戦略 |
|------|-----------|
| #112 | 免除（IDE ライフサイクル依存の ProjectActivity） |
| #113 | 免除（LSP 結合必須の AnAction） |
| #114 | `RescriptOffsetUtilsTest` で変換ロジックをテスト |

### 影響範囲

- #112, #113: 新規ファイル追加 + plugin.xml 登録のみ。既存コードへの影響なし
- #114: 14 ファイルのリファクタリング。動作変更なし（純粋なコード置換）
