# 各ウィンドウへの命令文

## 前提

各ウィンドウは `/Users/ngtz/Documents/repos/rescript-intellij-plugin` で Claude Code が起動済み。
命令文の冒頭で worktree ディレクトリへ `cd` する。

| ウィンドウ | worktree パス | ブランチ |
|-----------|-------------|---------|
| Window 1 | `../rescript-wt-signature-help` | `feature/signature-help` |
| Window 2 | `../rescript-wt-code-lens` | `feature/code-lens` |
| Window 3 | `../rescript-wt-create-interface` | `feature/create-interface` |
| Window 4 | `../rescript-wt-open-compiled` | `feature/open-compiled` |
| Window 5 | `../rescript-wt-build-status` | `feature/build-status` |

**共有インフラ:** 各ブランチにはバッチブランチで作成済みの以下が含まれている:
- `lsp/RescriptLanguageServer.kt` — カスタム LSP リクエスト用インターフェース
- `lsp/RescriptLsp4jClient.kt` — カスタム通知受信クライアント
- `lsp/RescriptCompilationStatusService.kt` — コンパイル状態保持サービス
- `RescriptLspServerDescriptor.kt` — 上記を使用するよう更新済み

---

## Window 1: Signature Help

```
cd /Users/ngtz/Documents/repos/rescript-wt-signature-help

ブランチ `feature/signature-help` で Signature Help（Parameter Info）の動作確認を実装してください。
ステアリングワークフローに従い、以下の手順で進めてください。
各ステアリングドキュメントの作成後、承認確認は不要です（親ウィンドウで承認済み）。連続して作成・実装してください。

## ステップ 1: ステアリングドキュメント作成
`.steering/20260218-signature-help/` ディレクトリを作成し、requirements.md, design.md, tasklist.md を作成。

### requirements.md の要約
- IntelliJ 2025.3+ の LSP API が `textDocument/signatureHelp` を自動サポートしている
- rescript-language-server はデフォルトで signatureHelp を有効化（triggerCharacters: `(`, retriggerCharacters: `=`, `,`）
- 追加のプラグインコードは不要（ゼロコード）
- 動作確認とドキュメント用のステアリングのみ

### design.md の要約
- 新規ファイル: なし
- 変更ファイル: なし
- IntelliJ LSP API が自動的に `signatureHelpProvider` capability を認識し、Parameter Info を提供する
- テスト省略理由: LSP サーバーとの結合が必須で単体テスト困難

## ステップ 2: 実装
コード変更なし。ステアリングドキュメントの作成のみ。

## ステップ 3: ビルド確認
`./gradlew buildPlugin` を実行し、成功を確認。

## ステップ 4: コミット
tasklist.md を更新してコミット。
※ 共有ドキュメント（CLAUDE.md, product-requirements.md, functional-design.md）はバッチブランチで一括更新するため、このウィンドウでは更新不要。
コミットメッセージ: `✨ Verify Signature Help support via LSP`

## ステップ 5: マージ確認
コミット完了後、ユーザーに「バッチブランチ `feature/p2-batch-vscode-gap` にマージして worktree を削除しますか？」と確認。
承認された場合:
  git -C /Users/ngtz/Documents/repos/rescript-intellij-plugin checkout feature/p2-batch-vscode-gap
  git -C /Users/ngtz/Documents/repos/rescript-intellij-plugin merge feature/signature-help
  git -C /Users/ngtz/Documents/repos/rescript-intellij-plugin worktree remove /Users/ngtz/Documents/repos/rescript-wt-signature-help
  git -C /Users/ngtz/Documents/repos/rescript-intellij-plugin branch -d feature/signature-help

## ステップ 6: 元のディレクトリに戻る
cd /Users/ngtz/Documents/repos/rescript-intellij-plugin
```

---

## Window 2: Code Lens

```
cd /Users/ngtz/Documents/repos/rescript-wt-code-lens

ブランチ `feature/code-lens` で Code Lens（関数の型注釈表示）を実装してください。
ステアリングワークフローに従い、以下の手順で進めてください。
各ステアリングドキュメントの作成後、承認確認は不要です（親ウィンドウで承認済み）。連続して作成・実装してください。

## ステップ 1: ステアリングドキュメント作成
`.steering/20260218-code-lens/` ディレクトリを作成し、requirements.md, design.md, tasklist.md を作成。

### requirements.md の要約
- IntelliJ LSP API は Code Lens を非サポート → CodeVision API を使用
- rescript-language-server の `textDocument/codeLens` は条件付き有効（デフォルト無効）
- 関数定義の上に推論された型シグネチャを表示（command.title に型情報、command.command は空文字列 → 情報表示のみ）
- `.resi` ファイルでは表示しない
- Settings > Inlay Hints > Code Vision で ON/OFF 切り替え可能

### design.md の要約
- 新規ファイル: `src/main/kotlin/com/rescript/plugin/codevision/RescriptCodeVisionProvider.kt`
- 変更ファイル: `plugin.xml`
- `DaemonBoundCodeVisionProvider` を実装
- LSP サーバーから `textDocument/codeLens` レスポンスを取得する方法:
  - `LspServerManager.getInstance(project).getServersForProvider(RescriptLspServerSupportProvider::class.java)` で LSP サーバーを取得
  - lsp4j サーバープロキシの `textDocumentService.codeLens()` を呼び出し
  - `CodeLens[]` の各要素を `TextCodeVisionEntry` にマッピング（range → TextRange, command.title → テキスト）
- rescript-language-server で codeLens を有効化するには初期化オプション `codeLens: true` が必要
  - `RescriptLspServerDescriptor` の `initializationOptions` をオーバーライドして `{"codeLens": true}` を返す
  - ※ バッチブランチの共有インフラには含まれていないため、このブランチで追加する
- plugin.xml 登録:
  ```xml
  <codeInsight.daemonBoundCodeVisionProvider
      implementation="com.rescript.plugin.codevision.RescriptCodeVisionProvider"/>
  ```
- テスト: CodeVisionProvider のユニットテスト（LSP 依存部分はモック）

## ステップ 2: 実装
設計に従い実装。

## ステップ 3: ビルド確認
`./gradlew buildPlugin` を実行し、成功を確認。

## ステップ 4: コミット
tasklist.md を更新してコミット。
※ 共有ドキュメント（CLAUDE.md, product-requirements.md, functional-design.md）はバッチブランチで一括更新するため、このウィンドウでは更新不要。
コミットメッセージ: `✨ Add Code Lens via CodeVision API`

## ステップ 5: マージ確認
コミット完了後、ユーザーに「バッチブランチ `feature/p2-batch-vscode-gap` にマージして worktree を削除しますか？」と確認。
承認された場合:
  git -C /Users/ngtz/Documents/repos/rescript-intellij-plugin checkout feature/p2-batch-vscode-gap
  git -C /Users/ngtz/Documents/repos/rescript-intellij-plugin merge feature/code-lens
  git -C /Users/ngtz/Documents/repos/rescript-intellij-plugin worktree remove /Users/ngtz/Documents/repos/rescript-wt-code-lens
  git -C /Users/ngtz/Documents/repos/rescript-intellij-plugin branch -d feature/code-lens

## ステップ 6: 元のディレクトリに戻る
cd /Users/ngtz/Documents/repos/rescript-intellij-plugin
```

---

## Window 3: インターフェースファイル生成

```
cd /Users/ngtz/Documents/repos/rescript-wt-create-interface

ブランチ `feature/create-interface` でインターフェースファイル生成アクションを実装してください。
ステアリングワークフローに従い、以下の手順で進めてください。
各ステアリングドキュメントの作成後、承認確認は不要です（親ウィンドウで承認済み）。連続して作成・実装してください。

## ステップ 1: ステアリングドキュメント作成
`.steering/20260218-create-interface/` ディレクトリを作成し、requirements.md, design.md, tasklist.md を作成。

### requirements.md の要約
- `.res` ファイルから `.resi` インターフェースファイルを自動生成
- LSP カスタムリクエスト `textDocument/createInterface` を使用
- リクエスト: `TextDocumentIdentifier { uri }` → レスポンス: `TextDocumentIdentifier { uri }` （生成された .resi の URI）
- サーバーが `.cmi` ファイルから .resi を生成してディスクに書き込む
- `.resi` が既存の場合は上書き確認ダイアログを表示
- `.cmi` 未生成時はサーバーが `window/showMessage` でエラー通知（IntelliJ LSP 層が自動表示）
- Go To メニューにアクション登録

### design.md の要約
- 新規ファイル: `src/main/kotlin/com/rescript/plugin/navigation/RescriptCreateInterfaceAction.kt`
- 変更ファイル: `plugin.xml`
- `AnAction` を継承
- 処理フロー:
  1. 現在のファイルが `.res` であることを確認
  2. `.resi` が既に存在するか確認 → `Messages.showYesNoDialog()` で上書き確認
  3. `LspServerManager.getInstance(project).getServersForProvider(RescriptLspServerSupportProvider::class.java)` で LSP サーバー取得
  4. サーバーが持つ lsp4j プロキシを `RescriptLanguageServer` にキャストして `createInterface()` 呼び出し
  5. レスポンスの URI を `VirtualFileManager.getInstance().refreshAndFindFileByUrl()` で VirtualFile に変換
  6. `FileEditorManager.getInstance(project).openFile()` でファイルを開く
- `update()`: `.res` ファイルでのみ有効（`getActionUpdateThread() = ActionUpdateThread.BGT`）
- plugin.xml 登録:
  ```xml
  <action id="ReScript.CreateInterface"
          class="com.rescript.plugin.navigation.RescriptCreateInterfaceAction"
          text="Create Interface File"
          description="Generate .resi from current .res file">
      <add-to-group group-id="GoToMenu" anchor="last"/>
  </action>
  ```
- テスト: アクションの `update()` メソッドのテスト（enabled/disabled 条件）

## ステップ 2: 実装
設計に従い実装。

## ステップ 3: ビルド確認
`./gradlew buildPlugin` を実行し、成功を確認。

## ステップ 4: コミット
tasklist.md を更新してコミット。
※ 共有ドキュメント（CLAUDE.md, product-requirements.md, functional-design.md）はバッチブランチで一括更新するため、このウィンドウでは更新不要。
コミットメッセージ: `✨ Add Create Interface File action`

## ステップ 5: マージ確認
コミット完了後、ユーザーに「バッチブランチ `feature/p2-batch-vscode-gap` にマージして worktree を削除しますか？」と確認。
承認された場合:
  git -C /Users/ngtz/Documents/repos/rescript-intellij-plugin checkout feature/p2-batch-vscode-gap
  git -C /Users/ngtz/Documents/repos/rescript-intellij-plugin merge feature/create-interface
  git -C /Users/ngtz/Documents/repos/rescript-intellij-plugin worktree remove /Users/ngtz/Documents/repos/rescript-wt-create-interface
  git -C /Users/ngtz/Documents/repos/rescript-intellij-plugin branch -d feature/create-interface

## ステップ 6: 元のディレクトリに戻る
cd /Users/ngtz/Documents/repos/rescript-intellij-plugin
```

---

## Window 4: コンパイル済み JS を開く

```
cd /Users/ngtz/Documents/repos/rescript-wt-open-compiled

ブランチ `feature/open-compiled` でコンパイル済み JS を開くアクションを実装してください。
ステアリングワークフローに従い、以下の手順で進めてください。
各ステアリングドキュメントの作成後、承認確認は不要です（親ウィンドウで承認済み）。連続して作成・実装してください。

## ステップ 1: ステアリングドキュメント作成
`.steering/20260218-open-compiled/` ディレクトリを作成し、requirements.md, design.md, tasklist.md を作成。

### requirements.md の要約
- `.res` / `.resi` ファイルに対応するコンパイル済み JavaScript ファイルを開く
- LSP カスタムリクエスト `textDocument/openCompiled` を使用
- リクエスト: `TextDocumentIdentifier { uri }` → レスポンス: `TextDocumentIdentifier { uri }` （コンパイル済み .js の URI）
- サーバーが `rescript.json` の `package-specs` を読んでパスを解決
- コンパイル済みファイルが存在しない場合、サーバーが `window/showMessage` でエラー通知
- LSP 未接続時のフォールバック: ファイルパス推測（`lib/js/<path>.bs.js`, `.mjs`, `.js`）
- Go To メニューにアクション登録、ショートカット `Alt+Shift+J`

### design.md の要約
- 新規ファイル: `src/main/kotlin/com/rescript/plugin/navigation/RescriptOpenCompiledJsAction.kt`
- 変更ファイル: `plugin.xml`
- `AnAction` を継承
- 処理フロー:
  1. 現在のファイルが `.res` / `.resi` であることを確認
  2. LSP サーバーを取得（`LspServerManager`）
  3. LSP 利用可能: `RescriptLanguageServer.openCompiled()` で URI を取得 → ファイルを開く
  4. LSP 利用不可（フォールバック）: `RescriptGotoRelatedProvider` と同様のロジックで `lib/js/` 配下を検索
  5. ファイルが見つからない場合はバルーン通知「Compile your project first」
- `update()`: `.res` / `.resi` ファイルでのみ有効
- 既存の `RescriptGotoRelatedProvider`（`navigation/RescriptGotoRelatedProvider.kt`）の JS ファイル検索ロジックを参考にする
- plugin.xml 登録:
  ```xml
  <action id="ReScript.OpenCompiledJs"
          class="com.rescript.plugin.navigation.RescriptOpenCompiledJsAction"
          text="Open Compiled JavaScript"
          description="Open compiled JS for this ReScript file">
      <add-to-group group-id="GoToMenu" anchor="last"/>
      <keyboard-shortcut first-keystroke="alt shift J" keymap="$default"/>
  </action>
  ```
- テスト: アクションの `update()` メソッドとフォールバックロジックのテスト

## ステップ 2: 実装
設計に従い実装。

## ステップ 3: ビルド確認
`./gradlew buildPlugin` を実行し、成功を確認。

## ステップ 4: コミット
tasklist.md を更新してコミット。
※ 共有ドキュメント（CLAUDE.md, product-requirements.md, functional-design.md）はバッチブランチで一括更新するため、このウィンドウでは更新不要。
コミットメッセージ: `✨ Add Open Compiled JavaScript action`

## ステップ 5: マージ確認
コミット完了後、ユーザーに「バッチブランチ `feature/p2-batch-vscode-gap` にマージして worktree を削除しますか？」と確認。
承認された場合:
  git -C /Users/ngtz/Documents/repos/rescript-intellij-plugin checkout feature/p2-batch-vscode-gap
  git -C /Users/ngtz/Documents/repos/rescript-intellij-plugin merge feature/open-compiled
  git -C /Users/ngtz/Documents/repos/rescript-intellij-plugin worktree remove /Users/ngtz/Documents/repos/rescript-wt-open-compiled
  git -C /Users/ngtz/Documents/repos/rescript-intellij-plugin branch -d feature/open-compiled

## ステップ 6: 元のディレクトリに戻る
cd /Users/ngtz/Documents/repos/rescript-intellij-plugin
```

---

## Window 5: ビルドステータス表示

```
cd /Users/ngtz/Documents/repos/rescript-wt-build-status

ブランチ `feature/build-status` でビルドステータスウィジェットを実装してください。
ステアリングワークフローに従い、以下の手順で進めてください。
各ステアリングドキュメントの作成後、承認確認は不要です（親ウィンドウで承認済み）。連続して作成・実装してください。

## ステップ 1: ステアリングドキュメント作成
`.steering/20260218-build-status/` ディレクトリを作成し、requirements.md, design.md, tasklist.md を作成。

### requirements.md の要約
- ステータスバーに ReScript コンパイラのビルド状態を表示
- LSP サーバーからの `rescript/compilationStatus` カスタム通知を受信（共有インフラで実装済み）
- 状態表示: compiling（スピナー）, success（チェック）, error（エラー数）, warning（警告数）
- ReScript プロジェクトでない場合はウィジェット非表示
- ウィジェットクリックでツールチップに詳細表示

### design.md の要約
- 新規ファイル: `src/main/kotlin/com/rescript/plugin/statusbar/RescriptCompilerStatusWidgetFactory.kt`
- 変更ファイル: `plugin.xml`
- `StatusBarWidgetFactory` + `StatusBarWidget` + `StatusBarWidget.TextPresentation` を実装
- データフロー:
  ```
  LSP Server → rescript/compilationStatus → RescriptLsp4jClient
    → RescriptCompilationStatusService.updateStatus()
      → listener callback
        → RescriptCompilerStatusWidget が statusBar.updateWidget() で UI 更新
  ```
- `install()` で `RescriptCompilationStatusService.addListener()` を使って状態変更を購読
- `getText()` で状態に応じたテキストを返す:
  - `"compiling"` → `"ReScript: Compiling..."`
  - `"success"` → `"ReScript: ✓"`
  - `"error"` → `"ReScript: N error(s)"`
  - `"warning"` → `"ReScript: N warning(s)"`
  - デフォルト → `"ReScript"`
- `getTooltipText()` で詳細情報（エラー数・警告数）
- `isAvailable()`: プロジェクトルートに `rescript.json` が存在するか確認
- plugin.xml 登録:
  ```xml
  <statusBarWidgetFactory id="RescriptCompilerStatus"
      implementation="com.rescript.plugin.statusbar.RescriptCompilerStatusWidgetFactory"/>
  ```
- テスト: ウィジェットの `getText()` / `getTooltipText()` の状態別テスト

## ステップ 2: 実装
設計に従い実装。

## ステップ 3: ビルド確認
`./gradlew buildPlugin` を実行し、成功を確認。

## ステップ 4: コミット
tasklist.md を更新してコミット。
※ 共有ドキュメント（CLAUDE.md, product-requirements.md, functional-design.md）はバッチブランチで一括更新するため、このウィンドウでは更新不要。
コミットメッセージ: `✨ Add compiler build status widget`

## ステップ 5: マージ確認
コミット完了後、ユーザーに「バッチブランチ `feature/p2-batch-vscode-gap` にマージして worktree を削除しますか？」と確認。
承認された場合:
  git -C /Users/ngtz/Documents/repos/rescript-intellij-plugin checkout feature/p2-batch-vscode-gap
  git -C /Users/ngtz/Documents/repos/rescript-intellij-plugin merge feature/build-status
  git -C /Users/ngtz/Documents/repos/rescript-intellij-plugin worktree remove /Users/ngtz/Documents/repos/rescript-wt-build-status
  git -C /Users/ngtz/Documents/repos/rescript-intellij-plugin branch -d feature/build-status

## ステップ 6: 元のディレクトリに戻る
cd /Users/ngtz/Documents/repos/rescript-intellij-plugin
```
