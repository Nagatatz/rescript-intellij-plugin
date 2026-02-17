# Design: Editor Notification Bar

## 実装方針

`EditorNotificationProvider` を実装し、LSP サーバーが見つからない場合にエディタ上部に通知バーを表示する。

## 新規ファイル

- `src/main/kotlin/com/rescript/plugin/editor/RescriptEditorNotificationProvider.kt`

## 変更ファイル

- `src/main/resources/META-INF/plugin.xml` — `editorNotificationProvider` 登録

## 設計詳細

### RescriptEditorNotificationProvider

- `EditorNotificationProvider` を実装
- `collectNotificationData()` で以下をチェック:
  1. ファイルが `.res` / `.resi` かチェック
  2. `PropertiesComponent` で dismiss フラグを確認（dismiss 済みなら null を返す）
  3. `RescriptProjectSettings` の `lspServerPath` が設定済みなら null を返す
  4. プロジェクトルートの `node_modules/@rescript/language-server/` ディレクトリの存在チェック
  5. 見つからない場合 `EditorNotificationPanel` を返す

### 通知バーの構成

- テキスト: "ReScript Language Server not found. Install @rescript/language-server for full IDE support."
- アクション "Configure...": `ShowSettingsUtil.getInstance().showSettingsDialog(project, "ReScript")`
- アクション "Dismiss": `PropertiesComponent` で dismiss フラグを保存、`EditorNotifications.getInstance(project).updateAllNotifications()` で再描画

### dismiss 管理

- `PropertiesComponent.getInstance(project)` でプロジェクト単位のキー `rescript.notification.lsp.dismissed` を管理
- dismiss したら以後表示しない

### plugin.xml 登録

```xml
<editorNotificationProvider
    implementation="com.rescript.plugin.editor.RescriptEditorNotificationProvider"/>
```
