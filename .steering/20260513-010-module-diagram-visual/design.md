# Design — Module Dependency Diagram Visual モード

## 全体方針

`flow/RescriptVariantFlowGraphView` の **「pure `computeLayout` + Java2D paint + CardLayout 切替」** パターンをそのまま `diagram/` に移植する。違いは入力データが「scrutinee + arm の tree」ではなく「モジュールノード集合 + 有向エッジ集合（DAG または cyclic）」である点。

## クラス構成

| クラス | 責務 | 新規/更新 |
|--------|------|----------|
| `RescriptDependencyDiagramGraphView` | Java2D で依存グラフを描画する `JComponent`。`companion object` の `computeLayout` が純粋 layout 関数 | **新規** |
| `RescriptDependencyDiagramPanel` | ToolWindow UI。`CardLayout` で Visual / Source を切替。Visual モードトグルアクションを追加 | 更新 |
| `RescriptDependencyDiagramGraphViewTest` | `computeLayout` のユニットテスト | **新規** |
| `RescriptDependencyDiagramPanelTest` | 既存テスト。Visual トグルの状態テストを追加（あれば） | 更新（任意） |

`RescriptDependencyDiagramModel` / `RescriptDependencyDiagramProvider` / `RescriptMermaidExporter` / Export アクション群は **無改修**。

## レイアウトアルゴリズム（最小限の layered layout）

入力: `RescriptDependencyDiagramModel`（ノード集合 + edges `from → to`）

### Step 1: 層 (layer) 割当

- 各ノードに layer 番号を割り当てる。layer 0 = 「他から依存されていないモジュール（in-degree 0）」
- それ以外のノードは `layer(n) = max(layer(p) for p in predecessors(n)) + 1`
- **巡回検出:** Kahn 法ベースの BFS で layer 計算する。Kahn のキューが空になった時点で未訪問ノードが残っていれば、それらはサイクル内にある。サイクル内ノードはまとめて **`残レイヤー = (現在最大 layer) + 1`** に置く（v1 ではサイクル可視化の高度な処理はしない）

### Step 2: 層内の順序 (column order)

- 同一 layer 内ではノード名の **昇順（辞書順）** に並べる。源ファイル順や crossing minimization は v1 では行わない（決定論的かつテスト容易な単純規則を優先）

### Step 3: 座標計算

- 各ノードのボックスサイズはラベル幅から計算（`flow/` の `nodeWidth` ヘルパーを踏襲）。`fm == null` の場合は文字幅推定（テスト時のヘッドレス計算用）
- 各 layer の高さは `NODE_HEIGHT` 固定、layer 間は `V_GAP`
- 同一 layer 内では `H_GAP` を空けて横並び。layer の幅は ノード幅合計 + (n-1)*H_GAP
- canvas 幅は全 layer 幅の最大値（左右マージン込み）

### Step 4: エッジ生成

- 各エッジ `from → to` について、**from の下辺中央** と **to の上辺中央** を結ぶ **4 点 polyline**（root_bottom → midY → arm_top_x → arm_top）を生成
- レイヤーをまたぐ「逆方向」エッジ（同一 layer 内のサイクル等）はとりあえず直線で描く（v1 では特別ハンドリングしない）
- 同一 layer 内のエッジ（サイクル内など）は両ボックスの右辺 → 右側ループ → 左辺で迂回 ... は v1 ではやらず、単純な水平線で繋ぐ

## カラーリング

`flow/` と同一パレットを採用してブランドを統一する:

```kotlin
private val NODE_FILL = JBColor(Color(0xFFF3F4), Color(0x4A1518))
private val NODE_BORDER = JBColor(Color(0xCB3939), Color(0xE6484F))
private val EDGE_COLOR = JBColor(Color(0xCB3939), Color(0xE6484F))
```

レイヤー 0（依存されていないモジュール = エントリポイント候補）だけ `flow/` の `ROOT_FILL` 系を使い、軽い視覚的階調を付ける。

## Panel への組み込み

`RescriptDependencyDiagramPanel` を以下のように改修する:

```kotlin
private val graphView = RescriptDependencyDiagramGraphView()
private val viewCards = CardLayout()
private val viewSwitcher = JPanel(viewCards).apply {
    add(JBScrollPane(graphView), CARD_VISUAL)
    add(JBScrollPane(textArea), CARD_SOURCE)
}

@Volatile private var visualMode: Boolean = true
```

ツールバーには `VisualModeAction` / `SourceModeAction`（ToggleAction）を先頭に追加。デフォルトは **Visual モード**。

`refresh()` は両方のビューを同時に更新する:

```kotlin
private fun refresh() {
    val model = RescriptDependencyDiagramProvider.buildDiagram(project)
    textArea.text = RescriptMermaidExporter.toMermaid(model)
    textArea.caretPosition = 0
    graphView.setModel(model)
    statusLabel.text = " Modules: ${model.moduleCount()}   Edges: ${model.edgeCount()}"
}
```

## エッジケース

| ケース | 期待動作 |
|--------|---------|
| プロジェクトに `.res` ファイルなし | `model.moduleCount() == 0`。Visual モードは空キャンバスを表示。例外を出さない |
| 単一モジュール | layer 0 に 1 ノードのみ表示。エッジなし |
| 循環依存（A → B → A） | 両ノードを別 layer に置き（Kahn 法の残ノード処理で同一の追加 layer に積む）、エッジは引かれる。サイクルの強調表示は v1 ではしない |
| 自己ループ（A → A） | エッジ生成時にスキップする（描画したい場合は将来検討） |
| 非常に大きなグラフ（数千ノード） | canvas を素直に巨大化させ、スクロールに委ねる。パフォーマンス調整は v1 では行わない |

## テスト戦略

`RescriptDependencyDiagramGraphViewTest`（`fm = null` で純粋計算）:

- empty: `model.moduleCount() == 0` → `canvasSize` がデフォルト最小値、ノードリスト・エッジリストが空
- single: 単一ノード → layer 0 にひとつ、エッジなし、`canvasSize.width >= 単一ボックス幅`
- linear chain: A → B → C → D → 4 layer、各 layer 1 ノード、3 エッジ
- branching: A → {B, C}; B → D; C → D → A は layer 0, B/C は layer 1, D は layer 2
- cycle: A → B → A → 両ノードが描画され（追加 layer に）、2 エッジが描画される
- self-loop: A → A → ノードは描画され、自己ループエッジは生成されない（v1）
- determinism: 同じモデルから 2 回 `computeLayout` した結果は等価（layer 内辞書順ソートにより保証）

heavy fixture は不要（pure 関数のみ）。テストは light fixture or 単純 JUnit5 で書ける。

## ファイル変更一覧

| パス | 変更 |
|------|------|
| `src/main/kotlin/com/rescript/plugin/diagram/RescriptDependencyDiagramGraphView.kt` | 新規 |
| `src/main/kotlin/com/rescript/plugin/diagram/RescriptDependencyDiagramPanel.kt` | CardLayout 化と ToggleAction 追加 |
| `src/test/kotlin/com/rescript/plugin/diagram/RescriptDependencyDiagramGraphViewTest.kt` | 新規 |
| `CLAUDE.md` | レイヤー 3 解説の `diagram/` 段落に「Visual / Source トグルあり」を追記 |
| `README.md` | Features 該当行に Visual モード追記 |
| `sphinx-docs/user/features/advanced.md` | Module Dependency Diagram セクションに Visual モード説明追記 |
| `sphinx-docs/locale/ja/LC_MESSAGES/user/features/advanced.po` | `msgstr` 更新 |
| `docs/repository-structure.md` | `diagram/` 列の代表クラスに `RescriptDependencyDiagramGraphView` を追加 |

## デザイン上の注意点

- `graphView` は `JBScrollPane` でラップする。`preferredSize` は `setModel` 時に再計算（`flow/` と同じパターン）
- `paintComponent` 内で `Graphics.create()` → finally `dispose()` で `Graphics2D` を正しく解放する
- `JBColor` は Light / Dark 両方の値を取り、IDE テーマ切替に追従する
- Visual モードがデフォルトだが、Source モードでも従来動作を保つこと（コピー & 持ち出しを壊さない）
