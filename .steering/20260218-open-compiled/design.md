# Design: Open Compiled JavaScript

## 新規ファイル

- `src/main/kotlin/com/rescript/plugin/navigation/RescriptOpenCompiledJsAction.kt`

## 変更ファイル

- `src/main/resources/META-INF/plugin.xml` — アクション登録

## テストファイル

- `src/test/kotlin/com/rescript/plugin/navigation/RescriptOpenCompiledJsActionTest.kt`

## 設計

### RescriptOpenCompiledJsAction

`AnAction` を継承。

#### 処理フロー (actionPerformed)

1. 現在のファイルが `.res` / `.resi` であることを確認
2. LSP サーバーを取得（`LspServerManager`）
3. **LSP 利用可能**: `RescriptLanguageServer.openCompiled()` で URI を取得 → ファイルを開く
4. **LSP 利用不可（フォールバック）**: `RescriptGotoRelatedProvider` と同様のロジックで `lib/js/` 配下を検索
   - プロジェクトルートからの相対パスを算出
   - `.bs.js`, `.mjs`, `.js` の順で検索
5. ファイルが見つからない場合はバルーン通知「Compile your project first」

#### update()

- `.res` / `.resi` ファイルでのみ有効
- `getActionUpdateThread() = ActionUpdateThread.BGT`

### plugin.xml 登録

```xml
<action id="ReScript.OpenCompiledJs"
        class="com.rescript.plugin.navigation.RescriptOpenCompiledJsAction"
        text="Open Compiled JavaScript"
        description="Open compiled JS for this ReScript file">
    <add-to-group group-id="GoToMenu" anchor="last"/>
    <keyboard-shortcut first-keystroke="alt shift J" keymap="$default"/>
</action>
```

### テスト

- `update()` メソッドの enabled/disabled 条件テスト
- フォールバックロジック（JS ファイル検索）のテスト
- テスト省略: LSP 依存部分（`actionPerformed` の LSP パス）は結合テスト困難
