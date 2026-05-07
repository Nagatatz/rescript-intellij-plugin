# Variant Flow Diagram — Requirements

## 背景

Type Narrowing Visualizer (#2) で各 `switch` arm の絞り込み型は可視化できたが、**switch 全体の構造**——どのパターンが何を分岐し、どのケースが見落とされているか——を一目で把握する手段はまだない。複雑な variant、特に `result<option<X>, _>` のような nested variant や、20 ケースを超える sealed sum-type を扱うとき、開発者は依然として switch 本体をスクロールしながら頭の中で decision tree を組み立てる必要がある。

本機能はこのギャップを埋めるため、`switch` 式の構造を **Decision Tree（意思決定木）** として ToolWindow 内に視覚化する。Mermaid 形式でレンダリングし、エクスポート可能にする。reanalyze 連携による「実行頻度ヒートマップ」は将来拡張とする。

## ユーザーストーリー

### US-Flow-01: switch のフロー可視化

**ReScript 開発者として**、現在カーソルがある（または明示的に選択した）`switch` 式の構造を、ツールウィンドウ内のフローダイアグラムで確認したい。

**受け入れ条件:**

- [ ] `Tools > Show Switch Flow Diagram` メニューから ToolWindow を開ける
- [ ] ToolWindow には現在のエディタのカーソル位置を含む `switch` 式の decision tree が表示される
- [ ] カーソルが switch の外にある場合は「No switch under caret」のプレースホルダーを表示する
- [ ] カーソル位置が変わると ToolWindow が自動更新される（編集中は debounce でスロットル）
- [ ] decision tree は Mermaid `flowchart` シンタックスで生成し、Mermaid プレビューを ToolWindow 内に埋め込む
- [ ] 各ノードはパターンを表す（`Some(_)` `None` `Ok(x)` `Error(_)` 等）
- [ ] 各エッジは scrutinee の評価結果を表す
- [ ] 末端ノード（葉）は arm body の先頭行を要約として表示する（最大 40 文字、省略は `…`）

### US-Flow-02: Mermaid / DOT エクスポート

**ドキュメントを書く開発者として**、生成された decision tree を外部ツール（Mermaid Live、graphviz）に持ち出して PR / 設計書に貼り付けたい。

**受け入れ条件:**

- [ ] ToolWindow ツールバーに「Copy Mermaid」ボタンがあり、現在の図の Mermaid ソースをクリップボードにコピーできる
- [ ] ToolWindow ツールバーに「Export DOT」ボタンがあり、graphviz 互換の DOT ファイルを保存できる
- [ ] エクスポート前に `RescriptSecurityUtils` で出力先を検証する（プロジェクト外への保存は警告する）

### US-Flow-03: ネスト switch の対応

**ネストした switch を持つコードを読む開発者として**、内側の switch も同じ図の中で「葉ノードの代わりに sub-graph」として展開して見たい。

**受け入れ条件:**

- [ ] ネストした `switch` がある arm の葉は、対応する内側の decision tree（sub-graph）に置き換えられる
- [ ] sub-graph はクリックで折りたたみ可能（Mermaid のネイティブサポート範囲内）
- [ ] 4 階層を超えるネストは「（深さ超過）」と省略し、深掘りしない（パフォーマンス保護）

## スコープ外

- reanalyze 実行カバレッジに基づくヒートマップ（次フェーズ）
- 動的トレース（実行ログから decision tree のヒット率を出す）
- arm の自動並び替え提案
- Pattern overlap / unreachable arm の自動検出（既存の inspection に任せる）

## 受け入れ確認

- [ ] 5 種類の variant（`option`, `result`, polymorphic variant, custom variant, list）でツリーが描画される
- [ ] ネスト 2 段階の switch が sub-graph として展開される
- [ ] Mermaid コピー結果を Mermaid Live で開いて同等の図が生成される
- [ ] DOT エクスポートが graphviz `dot` でレンダリングできる
- [ ] LSP の有無に関わらず動作する（型情報を使わない、構文ベース）
- [ ] ユニットテストで Mermaid シリアライザの出力をスナップショット検証する

## 非機能要件

- ToolWindow 描画は WebView ではなく既存の Mermaid プレビュー (`RescriptDependencyDiagramPanel` のパターン) を再利用する
- ダイアグラム生成はバックグラウンドスレッドで行う（編集応答性を維持）
- 5,000 行ファイルでの描画が 1 秒以内に完了すること
