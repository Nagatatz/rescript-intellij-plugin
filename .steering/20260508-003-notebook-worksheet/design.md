# Notebook 風 Worksheet — Design

## 1. アーキテクチャ概要

```
┌──────────────────────────────────────────────────────┐
│ User opens *.resnb file                              │
└──────────────────┬───────────────────────────────────┘
                   │ FileType + EditorProvider
                   ▼
┌──────────────────────────────────────────────────────┐
│ RescriptNotebookFileEditorProvider                   │
│ (com.intellij.fileEditorProvider extension point)    │
└──────────────────┬───────────────────────────────────┘
                   │ creates
                   ▼
┌──────────────────────────────────────────────────────┐
│ RescriptNotebookFileEditor                           │
│  - hosts a RescriptNotebookPanel                     │
│  - handles save / load via RescriptNotebookSerializer│
└──────────────────┬───────────────────────────────────┘
                   │
                   ▼
┌──────────────────────────────────────────────────────┐
│ RescriptNotebookPanel (JPanel with VerticalLayout)   │
│  - toolbar: Add Cell / Run All / Export Markdown     │
│  - list of CellPanel components                      │
└─────────┬────────────────────┬────────────────────┬──┘
          │ each cell          │ on Run             │ on Save
          ▼                    ▼                    ▼
┌───────────────────┐ ┌────────────────────┐ ┌──────────────────────┐
│ CellPanel         │ │ RescriptReplExecutor│ │ RescriptNotebookSeri │
│  - JTextArea code │ │  (existing)         │ │ alizer               │
│  - Run button     │ │                     │ │ (Notebook ↔ JSON)    │
│  - JTextArea out  │ │                     │ │                      │
└───────────────────┘ └────────────────────┘ └──────────────────────┘
```

## 2. パッケージ構成

新規パッケージ `notebook/` を追加する。既存 `worksheet/`、`repl/` とは独立。

```
src/main/kotlin/com/rescript/plugin/notebook/
├── RescriptNotebookFileType.kt              # .resnb の FileType 定義
├── RescriptNotebookModel.kt                 # NotebookCell / NotebookDocument データクラス
├── RescriptNotebookSerializer.kt            # JSON ↔ NotebookDocument
├── RescriptNotebookMarkdownExporter.kt      # NotebookDocument → Markdown 文字列
├── RescriptNotebookFileEditorProvider.kt    # FileEditorProvider 実装
├── RescriptNotebookFileEditor.kt            # FileEditor 実装（hosts panel）
├── RescriptNotebookPanel.kt                 # メイン UI
└── RescriptNotebookCellPanel.kt             # 各セルの UI

src/test/kotlin/com/rescript/plugin/notebook/
├── RescriptNotebookSerializerTest.kt
└── RescriptNotebookMarkdownExporterTest.kt
```

## 3. 主要クラス設計

### 3.1 RescriptNotebookModel (data classes)

```kotlin
data class NotebookCell(
    val code: String,
    val lastOutput: String,        // empty if never run
)

data class NotebookDocument(
    val version: Int = 1,
    val cells: List<NotebookCell>,
)
```

### 3.2 RescriptNotebookSerializer

```kotlin
object RescriptNotebookSerializer {
    fun toJson(doc: NotebookDocument): String
    fun fromJson(text: String): NotebookDocument
    fun emptyDocument(): NotebookDocument = NotebookDocument(cells = emptyList())
}
```

実装方針:
- `kotlinx.serialization` は依存追加が必要なので避ける
- `org.json` または手書きの JSON シリアライザを使う
- 不正な JSON はパース失敗で `IllegalStateException` を投げ、呼び出し元（FileEditor）で raw text view にフォールバック
- JSON 形式は `{ "version": 1, "cells": [{ "code": "...", "lastOutput": "..." }, ...] }`

### 3.3 RescriptNotebookMarkdownExporter

```kotlin
object RescriptNotebookMarkdownExporter {
    fun toMarkdown(doc: NotebookDocument): String
}
```

出力例:
```
## Cell 1

```rescript
let x = 42
Js.log(x)
```

```
42
```

## Cell 2
...
```

### 3.4 RescriptNotebookFileType

`com.intellij.openapi.fileTypes.FileType` を実装:
- 名前: `ReScript Notebook`
- 拡張子: `resnb`
- アイコン: 既存の汎用 ReScript アイコンを流用
- `isBinary = false`

### 3.5 RescriptNotebookFileEditorProvider / FileEditor

`com.intellij.openapi.fileEditor.FileEditorProvider` と `com.intellij.openapi.fileEditor.FileEditor` を実装:
- Provider は `accept(project, file)` で `.resnb` のみ受け入れる
- Provider の `getEditorTypeId()` は `"rescript-notebook"`
- Editor は `JComponent` を返す（`RescriptNotebookPanel` のラッパー）
- Editor は `isModified()`, `setState()`, `selectNotify()`, `deselectNotify()` を最低限実装

### 3.6 RescriptNotebookPanel

`JBPanel`（VerticalLayout）。
- 上部: Toolbar（Add Cell, Run All, Export Markdown ボタン）
- 中央: `JBScrollPane` 内に CellPanel を縦に並べる `JPanel`
- 各 CellPanel への参照を `MutableList<CellPanel>` で保持
- Save 時に各 CellPanel から code/output を集めて `NotebookDocument` を構築

### 3.7 RescriptNotebookCellPanel

`JBPanel`（BorderLayout）。
- 上: 自分の行番号 + Delete / Move Up / Move Down ボタン
- 中央: コード `JTextArea`（multi-line, syntax highlighting なし、placeholder で十分）
- 下: 出力 `JTextArea`（read-only、monospace）+ Run ボタン

評価ロジック:
1. Run ボタン押下で UI thread から `executeOnPooledThread` に発射
2. `RescriptReplExecutor.execute(code, projectPath)` を呼ぶ
3. 結果を出力エリアに表示し、`cell.lastOutput` を更新
4. 失敗時は赤テキストで表示
5. 評価中はボタンを「Running…」に変える

## 4. 既存資産の再利用

- `RescriptReplExecutor.execute(code, projectPath)` — セル評価
- 既存 worksheet パッケージは触らない（`.resw` は別フォーマットとして残す）
- ApplicationManager / IntelliJ Platform の `executeOnPooledThread`

## 5. テスト戦略

| テスト種別 | 対象 | 手法 |
|-----------|------|------|
| Unit | `RescriptNotebookSerializer.toJson` | スナップショット |
| Unit | `RescriptNotebookSerializer.fromJson` | 入力 JSON のラウンドトリップと不正入力のエラーケース |
| Unit | `RescriptNotebookMarkdownExporter.toMarkdown` | スナップショット |
| 免除 | `RescriptNotebookPanel` / `RescriptNotebookCellPanel` | Swing UI |
| 免除 | `RescriptNotebookFileEditor` / `RescriptNotebookFileEditorProvider` | IDE ライフサイクル依存 |
| 免除 | `RescriptNotebookFileType` | FileType 定義のみ |

## 6. プラグイン互換性

- IntelliJ Platform 2025.3+ の `FileEditorProvider` API
- Deprecated API なし
- LSP 不要

## 7. ドキュメント更新

- `CLAUDE.md` レイヤー 3 に `notebook/` パッケージを追記
- `docs/repository-structure.md` パッケージ表に `notebook/` を追加
- `docs/functional-design.md` に `fileType` + `fileEditorProvider` 拡張ポイントを追加
- `README.md` Features セクションに「Notebook-style worksheet」追加
- `sphinx-docs/user/features/advanced.md` に Notebook セクション
- 日本語訳同時更新
- `docs/lsp-fallback-matrix.md` に「LSP 不要」行を追加
