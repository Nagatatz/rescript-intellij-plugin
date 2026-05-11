# 設計

## ヘルパー定義

`src/main/kotlin/com/rescript/plugin/util/RescriptToolWindowContent.kt`:

```kotlin
package com.rescript.plugin.util

import com.intellij.openapi.Disposable
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.wm.ToolWindow
import com.intellij.ui.content.ContentFactory
import javax.swing.JComponent

/**
 * Installs a single-content panel into a ReScript tool window.
 *
 * Centralises the Pattern A boilerplate ... (詳細は実装内 KDoc に同期)
 */
object RescriptToolWindowContent {
    fun install(
        toolWindow: ToolWindow,
        component: JComponent,
        disposable: Disposable,
    ) {
        val content = ContentFactory.getInstance().createContent(component, "", false)
        Disposer.register(content, disposable)
        toolWindow.contentManager.addContent(content)
    }
}
```

- パラメータ `disposable` は必須 (Pattern A は常に panel が Disposable で、Disposer 登録が前提のため)
- displayName は既存 6 ファクトリと同じ `""` を使う
- `ContentFactory.getInstance()` は既存と同じ。`toolWindow.contentManager.factory` への置き換えは検討したが、Pattern A 群は全て `ContentFactory.getInstance()` を使っているため挙動互換のため踏襲

## 各 factory の差分

Before:

```kotlin
override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
    val panel = RescriptXxxPanel(project)
    val content = ContentFactory.getInstance().createContent(panel, "", false)
    Disposer.register(content, panel)
    toolWindow.contentManager.addContent(content)
}
```

After:

```kotlin
override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
    val panel = RescriptXxxPanel(project)
    RescriptToolWindowContent.install(toolWindow, panel, panel)
}
```

不要 import の削除:
- `com.intellij.openapi.util.Disposer`
- `com.intellij.ui.content.ContentFactory`

## ガードレール

- Panel 自体は触らない (`Disposable` を実装する `SimpleToolWindowPanel` のサブクラスである前提を維持)
- factory のクラス名・package・public API シグネチャは変更しない (Extension Point 登録への影響を避ける)
- `shouldBeAvailable` を新たに override しない (既存 Pattern A は override していない)
- `companion object { TOOL_WINDOW_ID }` を新たに足さない

## テスト方針

- `RescriptToolWindowContent` は `ContentFactory.getInstance()` を呼ぶため Application が必要。これは testing.md の「IDE ライフサイクル依存」/「LSP サーバー結合必須」と同種の IDE-fixture 要求で、本来 light fixture では駆動できない。
- factory も同じ理由で既存テストがない（このリポジトリで `Rescript*ToolWindowFactoryTest.kt` は 0 件）。
- helper は単一の Disposer.register + addContent コンビネーションでしかなく、ロジック含有量はゼロ。テスト免除する旨を tasklist.md に明記する。

## 既存挙動との互換性

- Disposer.register 順序: Before は `createContent` → `Disposer.register(content, panel)` → `addContent` の順。After も helper 内で同じ順。
- `addContent` のタイミング: Before / After ともに Disposer.register の後。
- 例外パス: なし (どのステップも throw しない API)。
