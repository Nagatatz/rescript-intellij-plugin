# Requirements — Module Dependency Diagram の Visual モード追加

## 背景

`diagram/` パッケージの ReScript Module Diagram ToolWindow は現状 **Mermaid テキスト表示のみ** で、ユーザーは Copy Mermaid / Copy DOT で外部ツール（Mermaid Live / graphviz）に持ち出さないとグラフィカルに見られない。

一方、姉妹機能である Variant Flow Diagram (`flow/`) は既に **Visual / Source トグル** を備え、`RescriptVariantFlowGraphView` が純粋な `computeLayout` + Java2D 描画でルート/アームを赤系の角丸ボックス + オーソゴナル矢印で描いている（CLAUDE.md / Layer 3 参照）。

本作業は `flow/` で確立した「純粋 layout 関数 + Java2D 描画 + Visual/Source CardLayout 切替」のパターンを `diagram/` に移植し、Module Dependency Diagram を IDE 内でグラフィカルに閲覧できるようにする。

## ユーザーストーリー

**プロジェクトのモジュール依存を把握したい ReScript 開発者として**、`ReScript Dependencies` ツールウィンドウのツールバーで Visual モードに切り替えるだけで、外部ツールを起動することなく依存関係グラフを目視確認したい。

## 受け入れ条件

- [ ] `ReScript Module Diagram` ツールウィンドウのツールバーに **Visual / Source トグル** が追加されている
- [ ] Visual モードでは Java2D 描画によるモジュール依存グラフが表示される
  - [ ] ノードは赤系の角丸ボックス（`flow/` と同じカラーパレットで一貫性確保）
  - [ ] エッジは矢印付きのオーソゴナル直線（依存元 → 依存先）
  - [ ] レイヤーは **上から下** (依存元が上、依存先が下) で配置される
- [ ] Source モードでは従来通り Mermaid `flowchart TD` テキストが表示される（既存機能の後退なし）
- [ ] Visual モードと Source モードは **CardLayout で同一ウィンドウ内をスワップ** する（別タブではない）
- [ ] 既存の Refresh / Copy DOT / Copy Mermaid アクションは両モードで利用可能
- [ ] 0 モジュール時・1 モジュール時・循環依存時のいずれでも例外なく表示できる
- [ ] レイアウト計算は **純粋関数** として実装され、ヘッドレスでユニットテスト可能
- [ ] レイアウトのユニットテストでノード数 / エッジ数 / canvas サイズの不変条件が検証されている
- [ ] CLAUDE.md / README.md / sphinx-docs (EN + JA) が更新されている

## 範囲外（v1 では実装しない）

- ノードドラッグによる手動再配置
- ズーム / パン操作（IntelliJ の標準スクロールで代用）
- エッジ交差最小化 (Sugiyama の crossing reduction 相当)
- インクリメンタル再レイアウト
- ノードクリックでのソースジャンプ
- 大規模プロジェクトでの描画パフォーマンスチューニング（ノード数のソフトキャップは現状のままで、必要に応じて将来検討）
