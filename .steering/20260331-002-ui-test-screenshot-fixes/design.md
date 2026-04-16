# Design: UI テストスクリーンショット品質修正

## 変更概要

5つの問題を3ファイルの修正で解決する。

## 変更 1: IDE ウィンドウのみキャプチャ

**ファイル**: `UiTestBase.kt` + `MarketplaceScreenshotTest.kt`

- `UiTestBase` に `protected var ideFrame: IdeFrameFixture? = null` を追加
- `takeScreenshot()` を `ideFrame?.getScreenshot() ?: remoteRobot.getScreenshot()` に変更
- `MarketplaceScreenshotTest` の `private lateinit var ideFrame` を削除し、基底クラスのフィールドを使用

## 変更 2: ファイルオープンの信頼性改善

**ファイル**: `IdeFixtures.kt`

キーボードショートカット（Cmd+Shift+O）ベースの `openFileByName` を `runJs` + `FileEditorManager.openFile()` に置換:

```kotlin
fun openFileByName(remoteRobot: RemoteRobot, fileName: String) {
    remoteRobot.runJs("""
        importClass(com.intellij.openapi.application.ApplicationManager)
        importClass(com.intellij.openapi.project.ProjectManager)
        importClass(com.intellij.openapi.fileEditor.FileEditorManager)
        const project = ProjectManager.getInstance().getOpenProjects()[0]
        const baseDir = project.getBaseDir()
        const file = baseDir.findFileByRelativePath("src/" + "$fileName")
        if (file == null) { throw new Error("File not found: src/$fileName") }
        ApplicationManager.getApplication().invokeAndWait(new Runnable({
            run: function() {
                FileEditorManager.getInstance(project).openFile(file, true)
            }
        }))
    """)
    Thread.sleep(500)
}
```

- `invokeAndWait` でブロッキング — ファイルが確実に開いてから次へ進む
- `Thread.sleep(500)` はエディタ描画の安定化のみ

## 変更 3: LSP 非依存化

**ファイル**: `MarketplaceScreenshotTest.kt`

| テスト | 変更内容 |
|--------|---------|
| 02 (補完) | テキスト入力・補完トリガー・Undo を削除。ファイル表示のみ |
| 03 (Error Lens) | wait 5000→2000ms に短縮 |
| 04 (インレイヒント) | wait 3000→2000ms に短縮 |
| 06 (Code Vision) | wait 5000→2000ms に短縮 |
| 09 (Intention) | Alt+Enter を try-catch で囲む |
| 10 (ホバー) | F1 を try-catch で囲む |

## 変更 4: REPL ツールウィンドウ修正

**ファイル**: `MarketplaceScreenshotTest.kt`

テスト 11 の `invokeLater` → `invokeAndWait` に変更。`runJs` がブロッキングになるため、ツールウィンドウの `show()` が確実に完了してからスクリーンショットを撮る。

## 変更 5: IDE 内部エラー通知・LSP 未検出バーの対処

**ファイル**: `MarketplaceScreenshotTest.kt`

### 問題
- テスト 02 で LSP なし環境での補完トリガーが IDE 内部エラーを発生させ、右下に赤い「IDE error occurred」通知が表示される
- この通知は消えず、後続の全スクリーンショット（05, 08 等）にも映り込む
- 「ReScript Language Server not found」の黄色い通知バーも全スクリーンショットに映り込む

### 対策

1. **テスト 02 のエラー原因除去**: 補完トリガーを削除（変更 3 で対応済み）
2. **IDE エラー通知の消去**: `@BeforeAll` で IDE の通知を消去する `runJs` を追加
3. **LSP 未検出バーの非表示**: エディタ通知バーを `runJs` で閉じる

```kotlin
// @BeforeAll の waitForIde() 内で実行
private fun dismissNotifications() {
    // IDE エラー通知バルーンを消去
    remoteRobot.runJs("""
        importClass(com.intellij.openapi.application.ApplicationManager)
        importClass(com.intellij.notification.NotificationsManager)
        importClass(com.intellij.openapi.project.ProjectManager)
        ApplicationManager.getApplication().invokeAndWait(new Runnable({
            run: function() {
                var notifications = NotificationsManager.getNotificationsManager().getNotificationsOfType(
                    com.intellij.notification.Notification.class, null
                )
                for (var i = 0; i < notifications.length; i++) {
                    notifications[i].expire()
                }
            }
        }))
    """)
}
```

LSP 未検出の通知バーについては、ファイルを開いた後に Escape キーで閉じるか、`EditorNotificationPanel` を `runJs` で閉じる。最もシンプルなのは各 `openFile` 後に通知バーを閉じるキーストロークを送ること。

## 変更 6: LSP が利用可能な前提への変更

ユーザーがサンプルプロジェクトに LSP をインストール済みのため、LSP 依存テストは簡略化せず維持する。ただし、LSP がない環境でもテストが致命的にならないよう try-catch は残す。

- テスト 02 (補完): LSP 補完が動作するため、補完トリガーを維持。ただしテキスト入力後のUndoは簡略化
- テスト 03 (Error Lens): LSP 診断を期待。wait は十分な時間を確保
- テスト 09 (Intention): Alt+Enter メニューが表示されることを期待
- テスト 10 (ホバー): F1 でドキュメント表示を期待

## スコープ外（別タスクで対応）

### Stub Builder 互換性バグ

IDE ログに 15 個の SEVERE エラーが記録されている:
```
Non-StubBasedPsiElement requests stub creation.
Stub type: TYPE_DECLARATION / LET_DECLARATION / MODULE_DECLARATION / ...
PSI: RescriptDeclarationPsiElement(...)
```

`RescriptDeclarationPsiElement` は `StubBasedPsiElementBase` を正しく継承しているが、`DefaultStubBuilder` の `instanceof StubBasedPsiElement` チェックに失敗する。IntelliJ 2025.3 との互換性問題の可能性がある。5 宣言型 × 3 ファイル = 15 エラー。

**本タスクでは**: エラー通知をスクリーンショット前に消去して対処する。
**別タスクで**: 根本原因を調査・修正する。

## リスク

- `project.getBaseDir()` は deprecated だが動作する。将来的には `ProjectRootManager.getContentRoots()[0]` に移行可能
- `invokeAndWait` は Remote-Robot の HTTP ハンドラスレッドから呼ぶためデッドロックリスクなし
- IDE ウィンドウサイズは環境依存。一貫したサイズが必要なら将来 JVM 引数で制御可能
- Stub Builder エラーが発生すると IDE 内部エラー通知が表示されるため、スクリーンショット前の通知消去が必要
