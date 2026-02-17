# Design: P2 rescript-vscode ギャップ 5機能バッチ実装

## 共有インフラ（バッチブランチで事前実装）

3つの機能が LSP カスタムリクエスト、1つがカスタム通知を使用するため、共有コンポーネントをバッチブランチで事前に作成する。

### 新規ファイル

#### `lsp/RescriptLanguageServer.kt`

LSP カスタムリクエスト用のサーバーインターフェース。

```kotlin
interface RescriptLanguageServer : LanguageServer {
    @JsonRequest("textDocument/createInterface")
    fun createInterface(params: TextDocumentIdentifier): CompletableFuture<TextDocumentIdentifier>

    @JsonRequest("textDocument/openCompiled")
    fun openCompiled(params: TextDocumentIdentifier): CompletableFuture<TextDocumentIdentifier>
}
```

- `lsp4j` の `@JsonRequest` アノテーションで LSP カスタムメソッドを定義
- パラメータ・レスポンスは標準の `TextDocumentIdentifier`（`{ uri: string }`）

#### `lsp/RescriptLsp4jClient.kt`

サーバーからのカスタム通知を受信するクライアント。

```kotlin
class RescriptLsp4jClient(serverDescriptor: LspServerDescriptor) : Lsp4jClient(serverDescriptor) {
    @JsonNotification("rescript/compilationStatus")
    fun compilationStatus(params: RescriptCompilationStatusParams) {
        // MessageBus 経由で通知をブロードキャスト
    }
}
```

#### `lsp/RescriptCompilationStatusService.kt`

コンパイル状態を保持・配信するプロジェクトサービス。

```kotlin
@Service(Service.Level.PROJECT)
class RescriptCompilationStatusService {
    var currentStatus: CompilationStatus = CompilationStatus.UNKNOWN

    data class CompilationStatus(
        val status: String,      // "compiling" | "success" | "error" | "warning"
        val errorCount: Int,
        val warningCount: Int,
    )

    // Topic for listeners
    companion object {
        val TOPIC = Topic.create("RescriptCompilationStatus", CompilationStatusListener::class.java)
    }

    interface CompilationStatusListener {
        fun statusChanged(status: CompilationStatus)
    }
}
```

### 変更ファイル

#### `lsp/RescriptLspServerDescriptor.kt`

```diff
- override val lsp4jServerClass: Class<out LanguageServer> = LanguageServer::class.java
+ override val lsp4jServerClass: Class<out LanguageServer> = RescriptLanguageServer::class.java

+ override fun createLsp4jClient(): Lsp4jClient = RescriptLsp4jClient(this)
```

#### `plugin.xml`

```xml
<!-- Compilation status service -->
<projectService serviceImplementation="com.rescript.plugin.lsp.RescriptCompilationStatusService"/>
```

---

## 機能別設計

### 1. Signature Help

**変更なし（ゼロコード）。**

IntelliJ 2025.3+ の LSP API が `textDocument/signatureHelp` を自動サポート。rescript-language-server はデフォルトで `signatureHelpProvider` capability を advertise する（triggerCharacters: `(`, retriggerCharacters: `=`, `,`）。

**実装内容:**
- 動作確認（`runIde` で手動テスト）
- テスト作成（テスト省略理由: LSP サーバーとの結合が必須で単体テスト困難。動作確認は手動テストで実施）
- ステアリングドキュメント作成

**新規ファイル:** なし
**変更ファイル:** なし

### 2. Code Lens

**新規ファイル:**
- `src/main/kotlin/com/rescript/plugin/codevision/RescriptCodeVisionProvider.kt`

**変更ファイル:**
- `src/main/resources/META-INF/plugin.xml`

**設計:**

IntelliJ LSP API は `textDocument/codeLens` を非サポートのため、CodeVision API を使用する。

```kotlin
class RescriptCodeVisionProvider : DaemonBoundCodeVisionProvider {
    override val id: String = "rescript.codeLens"
    override val name: String = "ReScript Type Annotations"
    override val defaultAnchor: CodeVisionAnchorKind = CodeVisionAnchorKind.Top

    override fun computeForEditor(editor: Editor, file: PsiFile): List<Pair<TextRange, CodeVisionEntry>> {
        // 1. LspServerManager から ReScript の LSP サーバーを取得
        // 2. lsp4j サーバープロキシ経由で textDocument/codeLens リクエストを送信
        // 3. CodeLens[] レスポンスを CodeVisionEntry にマッピング
        //    - range → TextRange
        //    - command.title → ClickableTextCodeVisionEntry(text)
    }
}
```

**LSP 初期化オプション:**
共有インフラの `RescriptLspServerDescriptor` で `codeLens: true` を設定する必要があるが、これはバッチブランチの共有インフラに含める。

**plugin.xml:**
```xml
<codeInsight.daemonBoundCodeVisionProvider
    implementation="com.rescript.plugin.codevision.RescriptCodeVisionProvider"/>
```

### 3. インターフェースファイル生成

**新規ファイル:**
- `src/main/kotlin/com/rescript/plugin/navigation/RescriptCreateInterfaceAction.kt`

**変更ファイル:**
- `src/main/resources/META-INF/plugin.xml`

**設計:**

```kotlin
class RescriptCreateInterfaceAction : AnAction(
    "Create Interface File",
    "Generate .resi interface file from current .res file",
    null
) {
    override fun actionPerformed(e: AnActionEvent) {
        // 1. 現在のファイルが .res であることを確認
        // 2. .resi が既に存在するか確認 → 存在する場合、上書き確認ダイアログ
        // 3. LspServerManager から LSP サーバーを取得
        // 4. サーバープロキシを RescriptLanguageServer にキャスト
        // 5. createInterface(TextDocumentIdentifier(uri)) を呼び出し
        // 6. レスポンスの uri を VirtualFile に変換
        // 7. VFS リフレッシュ後、FileEditorManager でファイルを開く
    }

    override fun update(e: AnActionEvent) {
        // .res ファイルでのみ有効
        e.presentation.isEnabledAndVisible = currentFile?.extension == "res"
    }

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT
}
```

**エラーハンドリング:**
- サーバーが `window/showMessage` で `.cmi` 未生成エラーを送信する場合がある → IntelliJ LSP 層が自動的に通知として表示
- リクエスト失敗時はバルーン通知でユーザーに報告

**plugin.xml:**
```xml
<action id="ReScript.CreateInterface"
        class="com.rescript.plugin.navigation.RescriptCreateInterfaceAction"
        text="Create Interface File"
        description="Generate .resi from current .res file">
    <add-to-group group-id="GoToMenu" anchor="last"/>
</action>
```

### 4. コンパイル済み JS を開く

**新規ファイル:**
- `src/main/kotlin/com/rescript/plugin/navigation/RescriptOpenCompiledJsAction.kt`

**変更ファイル:**
- `src/main/resources/META-INF/plugin.xml`

**設計:**

```kotlin
class RescriptOpenCompiledJsAction : AnAction(
    "Open Compiled JavaScript",
    "Open the compiled .js file for this ReScript file",
    null
) {
    override fun actionPerformed(e: AnActionEvent) {
        // 1. 現在のファイルが .res / .resi であることを確認
        // 2. LspServerManager から LSP サーバーを取得
        // 3. サーバープロキシを RescriptLanguageServer にキャスト
        // 4. openCompiled(TextDocumentIdentifier(uri)) を呼び出し
        // 5. レスポンスの uri を VirtualFile に変換
        // 6. FileEditorManager でファイルを開く
    }

    override fun update(e: AnActionEvent) {
        val file = e.getData(CommonDataKeys.VIRTUAL_FILE)
        e.presentation.isEnabledAndVisible =
            file != null && file.extension in listOf("res", "resi")
    }

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT
}
```

**フォールバック:**
LSP サーバーが利用不可の場合、ファイルパス推測によるフォールバックを実装:
- `lib/js/<relative-path>.bs.js`
- `lib/js/<relative-path>.mjs`
- `lib/js/<relative-path>.js`

（既存の `RescriptGotoRelatedProvider` と同様のロジック）

**plugin.xml:**
```xml
<action id="ReScript.OpenCompiledJs"
        class="com.rescript.plugin.navigation.RescriptOpenCompiledJsAction"
        text="Open Compiled JavaScript"
        description="Open compiled JS for this ReScript file">
    <add-to-group group-id="GoToMenu" anchor="last"/>
    <keyboard-shortcut first-keystroke="alt shift J" keymap="$default"/>
</action>
```

### 5. ビルドステータス表示

**新規ファイル:**
- `src/main/kotlin/com/rescript/plugin/statusbar/RescriptCompilerStatusWidgetFactory.kt`

**変更ファイル:**
- `src/main/resources/META-INF/plugin.xml`

**設計:**

```kotlin
class RescriptCompilerStatusWidgetFactory : StatusBarWidgetFactory {
    override fun getId(): String = "RescriptCompilerStatus"
    override fun getDisplayName(): String = "ReScript Compiler Status"

    override fun createWidget(project: Project): StatusBarWidget =
        RescriptCompilerStatusWidget(project)
}

class RescriptCompilerStatusWidget(
    private val project: Project
) : StatusBarWidget, StatusBarWidget.TextPresentation {
    private var statusBar: StatusBar? = null
    private var currentStatus = CompilationStatus.UNKNOWN

    override fun install(statusBar: StatusBar) {
        this.statusBar = statusBar
        // RescriptCompilationStatusService の TOPIC を購読
        project.messageBus.connect(this)
            .subscribe(RescriptCompilationStatusService.TOPIC, object : CompilationStatusListener {
                override fun statusChanged(status: CompilationStatus) {
                    currentStatus = status
                    statusBar.updateWidget(ID())
                }
            })
    }

    override fun getText(): String = when (currentStatus.status) {
        "compiling" -> "ReScript: Compiling..."
        "success"   -> "ReScript: ✓"
        "error"     -> "ReScript: ${currentStatus.errorCount} error(s)"
        "warning"   -> "ReScript: ${currentStatus.warningCount} warning(s)"
        else        -> "ReScript"
    }

    override fun getTooltipText(): String = // 詳細情報
}
```

**データフロー:**

```
LSP Server → rescript/compilationStatus 通知
    → RescriptLsp4jClient.compilationStatus()
        → RescriptCompilationStatusService に状態保存
            → MessageBus で TOPIC 発火
                → RescriptCompilerStatusWidget が UI 更新
```

**plugin.xml:**
```xml
<statusBarWidgetFactory id="RescriptCompilerStatus"
    implementation="com.rescript.plugin.statusbar.RescriptCompilerStatusWidgetFactory"/>
```

## ファイル競合分析

### 共有インフラ（バッチブランチ）で変更するファイル
- `lsp/RescriptLspServerDescriptor.kt` — `lsp4jServerClass` と `createLsp4jClient()` の変更
- `plugin.xml` — `RescriptCompilationStatusService` の登録

### 各 worktree で変更するファイル
- `plugin.xml` — 各機能の extension/action 登録（1行ずつ追加、マージ時手動解決）

### 競合リスク
- `plugin.xml` のみ（P1 バッチと同様、手動解決で対応可能）
- LSP 関連ファイルは共有インフラで事前変更済みのため、worktree 間の競合なし
