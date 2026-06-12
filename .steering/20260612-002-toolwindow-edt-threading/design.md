# 設計: ツールウィンドウの EDT スレッディング修正

## 方針

両 defect とも `coverage/RescriptTypeCoveragePanel` /
`interop/RescriptInteropRiskPanel` が確立済みの安全パターンに揃える:

```
doRefresh() (EDT) {
    statusLabel.text = " …中"            // 即時フィードバック
    executeOnPooledThread {
        val model = <重い処理>            // read-action 内 / off-EDT
        invokeLater { <UI 更新> }          // EDT に戻して反映
    }
}
```

## DEFECT 1: Module Dependency Diagram

### 変更 1-A: `RescriptDependencyDiagramProvider.buildDiagram`

provider を「自己完結で read-action を取得する」契約に変更し、共有
scanner ユーティリティと同じ安全性を持たせる。

- `buildDiagram` の index/PSI 走査本体を
  `ApplicationManager.getApplication().runReadAction { ... }` でラップする。
- KDoc に「Must be invoked off the EDT; performs a read action
  internally.」を追記（`RescriptTypeReferenceFinder` の文言に倣う）。
- `@Suppress("unused")` は実際にはパネルから使われているため削除し、
  正確な注記にする（任意・付随的整理）。

### 変更 1-B: `RescriptDependencyDiagramPanel.doRefresh`

```kotlin
override fun doRefresh() {
    statusLabel.text = " Building…"
    ApplicationManager.getApplication().executeOnPooledThread {
        val model = RescriptDependencyDiagramProvider.buildDiagram(project)
        ApplicationManager.getApplication().invokeLater {
            textArea.text = MermaidSourceColorizer.render(
                RescriptMermaidExporter.toMermaid(model))
            textArea.caretPosition = 0
            graphView.setModel(model)
            statusLabel.text =
                " Modules: ${model.moduleCount()}   Edges: ${model.edgeCount()}"
        }
    }
}
```

## DEFECT 2: Type Impact

### 変更 2: `RescriptTypeImpactPanel.doRefresh`

- caret/file は **EDT で捕捉**（`caretModel.offset` は EDT 必須）。
- target 解決 (`runReadAction`) と `findReferences` を pooled thread に
  まとめて逃がす。
- caret 連打で複数タスクが並走しうるため、`generation` カウンタで
  古い結果を破棄する（debounce 200ms で大半は単発だが、念のため）。

```kotlin
@Volatile private var refreshGeneration = 0

override fun doRefresh() {
    // --- EDT: 軽量な前提チェックと caret 捕捉 ---
    val fileEditor = FileEditorManager.getInstance(project).selectedEditor as? TextEditor
        ?: return renderEmpty("Open a ReScript file to see type impact.")
    val virtualFile = fileEditor.file
    if (虚fileType が .res/.resi でない) return renderEmpty(...)
    val offset = fileEditor.editor.caretModel.offset
    val generation = ++refreshGeneration

    // --- pooled: target 解決 + 参照検索 ---
    executeOnPooledThread {
        val target = runReadAction<TypeTarget?> { resolveAt(psiFile, offset) }
        val result = target?.let { RescriptTypeReferenceFinder.findReferences(project, it) }
        invokeLater {
            if (generation != refreshGeneration) return@invokeLater  // stale 破棄
            if (target == null) return@invokeLater renderEmpty("No type declaration under caret.")
            updateList(target, result)
        }
    }
}
```

`renderEmpty` は EDT から呼ばれる前提を維持（前提チェックは EDT のまま）。

## テスト方針

- パネル 2 件 (`RescriptDependencyDiagramPanel`,
  `RescriptTypeImpactPanel`) は **Swing UI ToolWindowPanel** であり
  `testing.md` の免除対象。スレッディング表明 (ThreadingAssertions /
  SlowOperations) はプラットフォーム層の挙動で light fixture では
  再現不能なため、リグレッション検証は **スモークテスト再実行**
  （`runIde` + ツールウィンドウ起動 + idea.log 確認）で担保する。
- `RescriptDependencyDiagramProvider` は既存テストあり。read-action
  ラップ追加後も挙動不変であることを既存テストで確認する。
- `RescriptTypeReferenceFinder` は無変更。

→ パネルのテスト省略理由は tasklist.md に明記する。

## コミット分割

- 🐛 DEFECT 1 (provider + panel) = 1 コミット
- 🐛 DEFECT 2 (impact panel) = 1 コミット
- ステアリング更新は各コミットに同梱

## ドキュメント影響

機能仕様・UI は不変のためユーザー向けドキュメント
(CLAUDE.md / README.md / sphinx-docs) の更新は不要。内部スレッディング
修正のみ。
