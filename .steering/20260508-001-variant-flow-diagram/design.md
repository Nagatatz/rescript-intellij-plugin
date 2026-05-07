# Variant Flow Diagram — Design

## 1. アーキテクチャ概要

```
┌───────────────────────────────────────────────────────┐
│ Editor (.res / .resi)                                 │
└───────────────────────┬───────────────────────────────┘
                        │ Caret position
                        ▼
┌───────────────────────────────────────────────────────┐
│ RescriptVariantFlowToolWindowFactory                  │
│ (com.intellij.toolWindow extension point)             │
└───────────────────────┬───────────────────────────────┘
                        │ creates
                        ▼
┌───────────────────────────────────────────────────────┐
│ RescriptVariantFlowPanel (JBPanel)                    │
│  - listens to FileEditorManager / Caret events        │
│  - debounces 200ms before recalculation               │
└───────────┬───────────────────────────┬───────────────┘
            │ build model               │ render
            ▼                           ▼
┌─────────────────────────┐ ┌───────────────────────────┐
│ RescriptVariantFlowModel │ │ RescriptVariantFlowMermaid│
│ (decision tree builder)  │ │ Exporter (model → Mermaid)│
└─────────────────────────┘ └───────────────────────────┘
            ▲                           ▲
            │ reuses                    │ reuses Mermaid
            │ RescriptSwitchArm         │ rendering helpers
            │ Collector                 │ from existing
            │                           │ dependency-diagram
            │                           │ infrastructure
```

## 2. パッケージ構成

新規パッケージ `flow/` を追加する。既存 `narrowing/` の `RescriptSwitchArmCollector` と `diagram/` の Mermaid 描画資産を再利用する。

```
src/main/kotlin/com/rescript/plugin/flow/
├── RescriptVariantFlowToolWindowFactory.kt   # ToolWindow 登録
├── RescriptVariantFlowPanel.kt                # 描画 + イベント購読
├── RescriptVariantFlowModel.kt                # decision tree 構築 (pure)
├── RescriptVariantFlowMermaidExporter.kt      # model → Mermaid string
├── RescriptVariantFlowDotExporter.kt          # model → DOT string
└── RescriptVariantFlowAction.kt               # Tools メニュー action

src/test/kotlin/com/rescript/plugin/flow/
├── RescriptVariantFlowModelTest.kt
├── RescriptVariantFlowMermaidExporterTest.kt
└── RescriptVariantFlowDotExporterTest.kt
```

## 3. 主要クラス設計

### 3.1 RescriptVariantFlowModel

純粋関数として実装する。入力: switch を含むソースのテキスト + カーソル offset。出力: decision tree。

```kotlin
data class FlowNode(
    val id: String,                       // safe id for Mermaid/DOT
    val patternSummary: String,           // "Some(_)", "None", ...
    val bodyPreview: String,              // "x + 1", "fallback", ...
    val children: List<FlowNode>,         // nested switches
)

data class FlowDiagram(
    val scrutineeText: String,            // root label
    val arms: List<FlowNode>,             // top-level arms
)

object RescriptVariantFlowModel {
    fun buildAtOffset(source: String, offset: Int): FlowDiagram?
}
```

`RescriptSwitchArmCollector.collect(source)` を呼び出して全 switch を取得し、offset を含む最も内側の switch を選び、その arms をモデル化する。ネスト switch は arm body を再帰的に走査して `children` に追加。

**ネスト深度制限:** 4 階層を超えるネストは `FlowNode(children=emptyList(), bodyPreview="(deeper switch hidden)")` で打ち止め。

**body preview:** arm の `arrowOffset` 直後から次の `|` または `}` までのテキストを取り、最初の改行までを 40 文字まで切り出す。

### 3.2 RescriptVariantFlowMermaidExporter

```kotlin
object RescriptVariantFlowMermaidExporter {
    fun toMermaid(diagram: FlowDiagram): String
}
```

出力例:
```
flowchart TD
  root["switch x"]
  root -->|"Some(_)"| n0["x + 1"]
  root -->|"None"| n1["0"]
```

ノード ID とラベルのエスケープは既存 `RescriptMermaidExporter` のヘルパーをコピーまたは extract する（後者推奨。`MermaidLabelEscaping.escapeLabel` を `diagram/` から抽出）。

### 3.3 RescriptVariantFlowDotExporter

graphviz の `dot` で描画可能な形式を出力。

```kotlin
object RescriptVariantFlowDotExporter {
    fun toDot(diagram: FlowDiagram): String
}
```

出力例:
```
digraph SwitchFlow {
  rankdir=TB;
  root [label="switch x"];
  root -> n0 [label="Some(_)"];
  n0 [label="x + 1"];
  ...
}
```

### 3.4 RescriptVariantFlowPanel

`RescriptDependencyDiagramPanel` と同じパターンで JBPanel を継承する。差分:

- データソースが `RescriptDependencyDiagramModel` ではなく `FlowDiagram`
- カーソル位置に依存するため、`CaretListener` を購読
- 200ms debounce（既存の `Alarm` パターンを踏襲）

### 3.5 RescriptVariantFlowToolWindowFactory / Action

- ToolWindow ID: `ReScript Switch Flow`
- アイコン: 既存の汎用ダイアグラムアイコンを流用
- `Tools > Show Switch Flow Diagram` action から ToolWindow を activate

## 4. 既存資産の再利用とリファクタリング

`RescriptMermaidExporter` 内のラベル/ID エスケープは `MermaidLabelEscaping` として `diagram/` 配下に抽出し、両 exporter が共有する。これは小さなリファクタで、既存テストへの影響が無いことをユニットテストで担保する。

`RescriptSwitchArmCollector` の戻り値は本機能でも利用するが、現状は `scrutineeRange / patternOffset / arrowOffset / patternSummary` を持つ。`arm body` の終端 offset が必要なため、`SwitchArm` に `bodyEndOffset` を追加することを検討する。これは既存利用箇所には影響しない（フィールド追加のみ）。

## 5. テスト戦略

| テスト種別 | 対象 | 手法 |
|-----------|------|------|
| Unit | `RescriptVariantFlowModel.buildAtOffset` | 5 種類のパターン + ネスト + 不完全 switch のテーブル駆動 |
| Unit | `RescriptVariantFlowMermaidExporter.toMermaid` | スナップショット |
| Unit | `RescriptVariantFlowDotExporter.toDot` | スナップショット |
| Unit | `MermaidLabelEscaping` | 既存 `RescriptMermaidExporterTest` をリファクタリング |
| 免除 | `RescriptVariantFlowPanel` | Swing UI のためテスト免除 |
| 免除 | `RescriptVariantFlowToolWindowFactory` | IDE ライフサイクル依存のためテスト免除 |

## 6. プラグイン互換性

- IntelliJ Platform 2025.3+ の `com.intellij.toolWindow` extension point を使用
- Deprecated API なし（既存の `RescriptDependencyDiagramToolWindowFactory` と同等）
- LSP 不要（構文ベースの解析）

## 7. ドキュメント更新

- `CLAUDE.md` レイヤー 3 に `flow/` を追加
- `docs/repository-structure.md` パッケージ表に `flow/` を追加
- `docs/functional-design.md` に ToolWindow extension point + Action を追加
- `README.md` Features セクションに「Variant flow diagram」追加
- `sphinx-docs/user/features/advanced.md` に新セクション追加（日本語訳同時）
- `docs/lsp-fallback-matrix.md` に「LSP 不要」行を追加
