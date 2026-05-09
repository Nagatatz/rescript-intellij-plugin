# Type Coverage Heat Map — Requirements

## 背景

ReScript は型推論に強く依存しており、明示的な型注釈を書かずとも動作する。一方、ライブラリ境界・公開関数・複雑なパイプラインでは、型注釈があると保守性とドキュメンテーション性が大きく向上する。本機能は **どのファイルが型注釈を欠いているか** をプロジェクト全体で可視化し、ユーザーが型を補強する箇所を発見できるようにする。

既存資産:

- `RescriptParser` / 軽量パーサー: トップレベル `let` 宣言を PSI で認識済み
- `RescriptTypeImpactScanner` / `RescriptInteropScanner`: `FileTypeIndex` ベースのプロジェクト全体走査の前例
- `RescriptVariantFlowToolWindowFactory` 等: ToolWindow + Refresh ボタンの構造の前例

## ユーザーストーリー

### US-01: プロジェクト全体の型カバレッジ可視化

**ReScript 開発者として**、`.res` ファイルごとの **型注釈率** をツールウィンドウで一覧したい。型注釈の薄いファイルを特定して、レビューや補強の優先順位付けに使う。

**受け入れ条件:**

- [ ] `View > Tool Windows > ReScript Type Coverage` を開くとプロジェクト内の `.res` ファイル一覧が表示される
- [ ] 各行: ファイルパス / 総 let 数 / 注釈付き数 / 推論依存数 / カバレッジ%
- [ ] カバレッジ% に応じた色分け: ≥ 70% 緑 / 30〜69% 黄 / < 30% 赤
- [ ] デフォルト並び順は **カバレッジ% 昇順** (改善の余地が大きいファイルが上に来る)
- [ ] 列ヘッダクリックで他キーでもソートできる
- [ ] ファイル行ダブルクリックでエディタにジャンプ
- [ ] 上部ツールバーに Refresh アクションがあり、再走査できる
- [ ] 200 ファイル規模で初回走査が < 2 秒、refresh が < 500 ms

### US-02: 純粋関数による型注釈判定

**保守者として**、型注釈判定ロジックが IDE fixture なしで単体テスト可能であることを保証したい。

**受け入れ条件:**

- [ ] `RescriptTypeCoverageClassifier.classifyLet(letSource)` を pure object に切り出し、トップレベル `let` 宣言ソース文字列を受け取り `LetCoverage` (ANNOTATED / INFERRED) を返す
- [ ] 判定基準: `let` キーワード直後の binding name から `=` までの **depth-0** に `:` トークンが現れたら ANNOTATED
- [ ] パラメータリスト `(...)` や record literal `{...}` 内部はスキップ (depth > 0)
- [ ] 既存 `RescriptParser` で抽出された宣言レンジを scanner が利用する

## スコープ外

- パラメータ単位の annotated/inferred 分解 (param-only annotated は v1 では INFERRED 扱い)
- LSP hover ベースの精度向上 (将来検討)
- Project View ガターやエディタ内ヒートマップ表示 (v2 以降)
- `.resi` ファイルへのカバレッジ計算 (interface ファイルは元から annotated 前提のため対象外)

## 機能カテゴリ

- ToolWindow (新規)
- 静的解析・分析
