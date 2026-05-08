# Variant Flow Diagram — Requirements

## 背景

Type Narrowing Visualizer (#2) で各 `switch` arm の絞り込み型は可視化できたが、**switch 全体の構造**——どのパターンが何を分岐し、どのケースが見落とされているか——を一目で把握する手段はまだない。複雑な variant、特に `result<option<X>, _>` のような nested variant や、20 ケースを超える sealed sum-type を扱うとき、開発者は依然として switch 本体をスクロールしながら頭の中で decision tree を組み立てる必要がある。

本機能はこのギャップを埋めるため、`switch` 式の構造を **Decision Tree（意思決定木）** として ToolWindow 内に視覚化する。Mermaid 形式でレンダリングし、エクスポート可能にする。reanalyze 連携による「実行頻度ヒートマップ」は将来拡張とする。

## ユーザーストーリー

### US-Flow-01: switch のフロー可視化

**ReScript 開発者として**、現在カーソルがある（または明示的に選択した）`switch` 式の構造を、ツールウィンドウ内のフローダイアグラムで確認したい。

**受け入れ条件:**

- [x] `Tools > Show Switch Flow Diagram` メニューから ToolWindow を開ける
- [x] ToolWindow には現在のエディタのカーソル位置を含む `switch` 式の decision tree が表示される
- [x] カーソルが switch の外にある場合は「No switch under caret」のプレースホルダーを表示する
- [x] カーソル位置が変わると ToolWindow が自動更新される（200ms debounce）
- [x] decision tree は Mermaid `flowchart TD` シンタックスで生成し、Mermaid ソースを ToolWindow 内のテキストパネルに表示する（IDE 内 SVG レンダリングは現フェーズではスコープ外）
- [x] 各ノードはパターンを表す（`Some(_)` `None` `Ok(_)` `Error(_)` 等）
- [x] 各エッジは scrutinee の評価結果を表す
- [x] 末端ノード（葉）は arm body の先頭非空行を要約として表示する（最大 40 文字、省略は `…`）

### US-Flow-02: Mermaid / DOT エクスポート

**ドキュメントを書く開発者として**、生成された decision tree を外部ツール（Mermaid Live、graphviz）に持ち出して PR / 設計書に貼り付けたい。

**受け入れ条件:**

- [x] ToolWindow ツールバーに「Copy Mermaid」ボタンがあり、現在の図の Mermaid ソースをクリップボードにコピーできる
- [x] ToolWindow ツールバーに「Copy DOT」ボタンがあり、graphviz 互換の DOT 文字列をクリップボードにコピーできる

**Phase 2 以降:**
- ファイル保存ダイアログによる DOT エクスポート + `RescriptSecurityUtils` 経由の出力先検証。クリップボードコピーで主用途を満たすため、ファイル保存は需要が出るまで保留。

### US-Flow-03: ネスト switch の対応

**ネストした switch を持つコードを読む開発者として**、内側の switch も同じ図の中で「葉ノードの代わりに sub-graph」として展開して見たい。

**受け入れ条件:**

- [x] ネストした `switch` がある arm のノードは、対応する内側の decision tree のノードを子として展開する
- [x] 3 階層を超えるネストは「(deeper switch hidden)」の単一葉に折り畳む（パフォーマンス保護、Mermaid 出力サイズの制御）

**Phase 2 以降:**
- インタラクティブな sub-graph 折りたたみ。現状の実装はプレーンテキスト Mermaid ソース表示で、折りたたみ操作は外部レンダラー（Mermaid Live 等）に任せる。

## スコープ外

- reanalyze 実行カバレッジに基づくヒートマップ（次フェーズ）
- 動的トレース（実行ログから decision tree のヒット率を出す）
- arm の自動並び替え提案
- Pattern overlap / unreachable arm の自動検出（既存の inspection に任せる）

## 受け入れ確認

- [x] 5 種類の variant（`option`, `result`, polymorphic variant, custom variant, list）が `RescriptVariantFlowModelTest` および収集元の `RescriptSwitchArmCollectorTest` でカバーされる
- [x] ネスト 2 段階の switch が sub-graph として展開される（`nested switch becomes a sub-graph` テストで検証）
- [ ] Mermaid コピー結果を Mermaid Live で開いて同等の図が生成される — Phase 2 で `mmdc` (Mermaid CLI) を CI に追加して構文検証する案あり
- [ ] DOT 出力が graphviz `dot` でレンダリングできる — Phase 2 で `dot -Tsvg` を CI に追加して構文検証する案あり
- [x] LSP の有無に関わらず動作する（型情報を使わない、構文ベース）。実装は `RescriptSwitchArmCollector`（lexer ベース）のみに依存
- [x] ユニットテストで Mermaid / DOT シリアライザの出力をスナップショット検証する（4 件 + 3 件）
- [x] 大規模ファイル（5000 行ネスト switch）のレンダリングが 1 秒以内であることを `RescriptVariantFlowModelPerfTest`（20260508-006）で自動検証

## 非機能要件

- ToolWindow 描画は WebView ではなく既存の Mermaid プレビュー（`RescriptDependencyDiagramPanel` のパターン: `SimpleToolWindowPanel` + 読み取り専用 `JTextArea` + Copy 系ツールバー）を再利用する
- ダイアグラム生成は IDE の read action（`ApplicationManager.runReadAction`）で囲み、UI スレッドをブロックしない
- 5,000 行ファイルでの描画が 1 秒以内に完了すること — マージ後に手動検証（Phase 2 以降のベンチマーク化を検討）
