# Design: インターフェースファイル生成

## 新規ファイル

### `src/main/kotlin/com/rescript/plugin/navigation/RescriptCreateInterfaceAction.kt`
- `AnAction` を継承
- 処理フロー:
  1. 現在のファイルが `.res` であることを確認
  2. `.resi` が既に存在するか確認 → `Messages.showYesNoDialog()` で上書き確認
  3. `LspServerManager.getInstance(project).getServersForProvider(RescriptLspServerSupportProvider::class.java)` で LSP サーバー取得
  4. サーバーが持つ lsp4j プロキシを `RescriptLanguageServer` にキャストして `createInterface()` 呼び出し
  5. レスポンスの URI を `VirtualFileManager.getInstance().refreshAndFindFileByUrl()` で VirtualFile に変換
  6. `FileEditorManager.getInstance(project).openFile()` でファイルを開く
- `update()`: `.res` ファイルでのみ有効（`getActionUpdateThread() = ActionUpdateThread.BGT`）

## 変更ファイル

### `plugin.xml`
```xml
<action id="ReScript.CreateInterface"
        class="com.rescript.plugin.navigation.RescriptCreateInterfaceAction"
        text="Create Interface File"
        description="Generate .resi from current .res file">
    <add-to-group group-id="GoToMenu" anchor="last"/>
</action>
```

## テスト

### `src/test/kotlin/com/rescript/plugin/navigation/RescriptCreateInterfaceActionTest.kt`
- `update()` メソッドの enabled/disabled 条件テスト:
  - `.res` ファイルで enabled
  - `.resi` ファイルで disabled
  - 非 ReScript ファイルで disabled
  - ファイルなしで disabled
