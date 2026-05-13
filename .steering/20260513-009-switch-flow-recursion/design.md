# 20260513-009 設計: Visual GraphView の再帰ツリー化

## 影響範囲

| ファイル | 変更内容 |
|---------|---------|
| `src/main/kotlin/com/rescript/plugin/flow/RescriptVariantFlowGraphView.kt` | `computeLayout` を再帰ツリーレイアウトに拡張。`armLabel` から子持ちノードの body preview を除去 |
| `src/test/kotlin/com/rescript/plugin/flow/RescriptVariantFlowGraphViewTest.kt` | ネストアームの描画を保護するテスト追加 |
| `CLAUDE.md` | `flow/` の Variant Flow Diagram 説明文を「Visual モードでネストしたアームも描く」に更新 |
| `sphinx-docs/user/features/advanced.md` (+ `locale/ja/.po`) | Visual モード説明を更新 |

## レイアウトアルゴリズム

### 分岐戦略 — 2 つのレイアウトモードを保持する

- `flat layout` (既存): 全ての top-level アームが `children.isEmpty()` のときに使う。狭い viewport では複数行に折り返す既存実装をそのまま維持。
- `tree layout` (新規): いずれかの top-level アームが `children.isNotEmpty()` のときに使う。折り返しは行わず、横スクロールに任せる。

ディスクリミネーターは `diagram.arms.any { it.children.isNotEmpty() }`。これにより既存の wrap テストは壊れない (テスト fixture は leaf-only 構成)。

### Tree layout 詳細

各アームを **サブツリー** として捉え、サブツリーの幅・高さを再帰的に算出する:

```
ArmSubtree {
  rootBox: Rectangle        // このアーム自体の box (サブツリー内座標)
  armBoxes: List<(Rect, label)>  // 自分 + 子孫の全 box
  edges: List<List<Point>>  // 自分→直接子 + 子孫の全エッジ
  width: Int                // サブツリーの専有幅
  height: Int               // サブツリーの専有高さ
}
```

- リーフ (`children.isEmpty()`): `width = nodeWidth(label, …)`, `height = ARM_HEIGHT`
- 中間ノード: 子サブツリーを並べて `childrenWidth = Σ child.width + (n-1)*H_GAP`; `width = max(ownWidth, childrenWidth)`, `height = ARM_HEIGHT + V_GAP + max(child.height)`

座標は **サブツリーローカル** で持ち、トップレベルで合算するときに `dx, dy` で平行移動する。

### 親アームの label

```kotlin
private fun armLabel(node: FlowNode): String {
    val pattern = node.patternSummary.ifBlank { "(arm)" }
    if (node.children.isNotEmpty()) return pattern  // 子 box が body を表現するため
    val body = node.bodyPreview
    return if (body.isBlank()) pattern else "$pattern\n$body"
}
```

### ルート box

scrutinee root (`switch X` ラベル) は従来通り **1 個だけ** 描く。インナー scrutinee は (Mermaid exporter と同じく) 独立ノードに昇格しない。すべてのネスト箱は `armBoxes` 経由で「アーム色」(ARM_FILL) で描かれる。

### Canvas サイズ

- `canvasWidth = max(viewportWidth, contentWidth + 2*MARGIN)` で viewport を最小として尊重しつつ、コンテンツがはみ出る場合は拡張 (scroll pane が横スクロールを担当)。
- `canvasHeight = armsY + max(subtree.height) + MARGIN`。深いツリーは自然に縦長になる。

### エッジ

各ペアレント→チャイルド間は引き続き 4 頂点の直交ポリライン (root-bottom → midY → child-top.x → child-top)。コーディング上は再帰的なサブツリー構築の中で、親 box の `bottom` から各子サブツリーの `rootBox.top` へエッジを追加する。

## テスト計画

`RescriptVariantFlowGraphViewTest` に以下を追加:

1. **`nested arm renders children as separate boxes below parent`**
   - 入力: `Yellow` アームに `true` / `false` の 2 子ノード
   - 期待: `armBoxes.size == 3` (`Yellow`, `true`, `false`)、子 box.y > 親 box.y、エッジ数 = top-level 1 (root→Yellow) + nested 2 (Yellow→true, Yellow→false) = 3
2. **`parent arm with children drops body preview from label`**
   - 入力: 親アーム `bodyPreview = "switch blink {"`、children を持つ
   - 期待: 親アームの label が `bodyPreview` を含まない (= 親アームラベルは `patternSummary` のみ)
3. **`flat diagrams still wrap when viewport is narrow`** (回帰防止 — 既存テストの強化)
   - 既存の wrap テストが leaf-only 構成で引き続き wrap することを再確認 (新規ケース不要、既存テストで足りる)
4. **`canvas height grows to accommodate nested subtree`**
   - 入力: ネストアームあり
   - 期待: canvasHeight ≥ ARM_HEIGHT + V_GAP + ARM_HEIGHT + MARGIN

## ドキュメント変更要点

- **CLAUDE.md** の `flow/` 説明: 「Visual モードは…赤系の角丸ボックスとオーソゴナル矢印で描く」の後に「ネストした switch のアームは親アーム box の下にサブツリーとして展開され、`RescriptVariantFlowModel.MAX_NESTING_DEPTH` までを忠実に描く」を追記。
- **sphinx-docs/user/features/advanced.md**: Visual / Source 切替の説明に「Nested switches expand inline as sub-trees」を追加。
- **JA `.po`**: 対応する `msgstr` を「ネストされた switch も Visual モードのサブツリーとして展開される」相当に更新。

## リスクと緩和策

| リスク | 緩和策 |
|-------|--------|
| 既存 wrap 動作が破壊される | discriminator (`any { it.children.isNotEmpty() }`) で leaf-only 図は既存パスを通る。既存テストで保護 |
| 深いネストで canvas が爆発する | モデル側で `MAX_NESTING_DEPTH = 3` を強制している。深さ 4 以上は `(deeper switch hidden)` プレースホルダーが既に来る |
| 横スクロール幅が肥大化する | `JBScrollPane` が処理。`canvasWidth` の最小値だけ viewport で保証 |
