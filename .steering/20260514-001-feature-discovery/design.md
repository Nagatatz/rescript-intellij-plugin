# 機能発掘調査 — 設計

調査の成果物。実装は別ステアリングに分割して承継する。

## 採用判断

ユーザーレビューの結果、以下を確定:

- **バケット A (5 件、visual / panel 色付け)** を最優先で着手 → `20260514-002-visual-color-brushup` で実装
- **バケット B (4 件、ReScript syntax 色付け)** は A 完了後に再評価 → 後続ステアリング候補
- **バケット C (5 件、高 ROI 新機能) と D (6 件、重い新機能) と E (1 件、ドキュメント補正)** は計画段階のリスト保持

## バケット A の設計概要 (実装スケッチ)

詳細は `20260514-002-visual-color-brushup/design.md` を参照。本ステアリングでは方針のみ記載。

### 共通方針

- 色は全て `JBColor(Color(lightHex), Color(darkHex))` で Light/Dark 対応
- palette マップは `*Model.kt` に `internal` で抽出 (テストカバレッジ確保)
- 凡例ラベルは英語リテラル (現状の Visual ビューの他ラベルと一貫)
- TODO placeholder 検出は `bodyPreview.startsWith("todo")` リテラルのみ (偽陽性ゼロ優先)

### 機能別アプローチ

| # | 機能 | 主要変更 |
|---|---|---|
| 1 | Variant Flow Visual | `ArmKind` enum + `FlowNode.kind` + `classifyArm` + palette map + 凡例 |
| 2 | Module Dependency Visual | `NodeRole` enum + `LayoutNode.role` + `classifyNodes` (Kahn BFS) + palette map + 凡例 |
| 3 | Interop Risk panel | `JBLabel` を `JPanel(BorderLayout)` に置換、WEST に 4px 色帯、CENTER に既存ラベル |
| 4 | Type Impact panel | `ListCellRenderer` → `ColoredListCellRenderer` 変換、kind を bold + 色付き |
| 5 | Notebook cell | 3 つの `Color` を `JBColor` 化 (ERROR_FOREGROUND / BORDER_COLOR / OUTPUT_BACKGROUND) |

## バケット B 以降の設計概要 (将来実装用ヒント)

### バケット B (ReScript syntax 色付け、4 件)

共通基盤:
- `RescriptLexer` でテキストをトークン分解
- `RescriptSyntaxHighlighter.getTokenHighlights(tokenType)` で `TextAttributesKey[]` を取得
- `EditorColorsManager.getInstance().globalScheme.getAttributes(key)` で `TextAttributes` を解決
- `SimpleTextAttributes.fromTextAttributes(ta)` で `SimpleTextAttributes` 化

機能別:
- **Hoogle 検索結果**: `ColoredListCellRenderer.append()` に上記マッピング適用
- **Type Info / PPX View / Notebook 入力**: `JBLabel` / `JTextArea` を `EditorTextField` (read-only / 編集可) に置換し、ReScript file type を渡せば自動でハイライト

### バケット C (高 ROI 新機能、5 件)

- **Test Code Lens**: `RescriptTestRunConfigurationType` を `CodeVisionProvider` (既存 `RescriptCodeVisionProvider` のパターン) で呼び出す
- **doc コメント評価**: コメント正規表現で `// > expr` を抽出 → `RescriptReplExecutor.run(expr)` → CodeVision で結果を inline 表示
- **`open` 展開 code action**: `Alt+Enter` の Intention 実装、`open Module` を AST 走査でファイル内の `Module.foo` リライト後 `open` 行を削除
- **Pipeline Hints**: `InlayHintsProvider` で `->` パイプの各セグメントに LSP hover をかけて中間型を表示
- **ネスト switch 平坦化**: `RescriptVariantFlowModel.RescriptSwitchArmCollector` を再利用して構文ベースで AST 書き換え

### バケット D (重い新機能、6 件)

- **Mermaid syntax 色付け**: 新規 `MermaidLexer` (JFlex) + `MermaidSyntaxHighlighter` を作成し、Variant Flow / Module Dependency ソースモードの `JTextArea` を `EditorTextField` に置換
- **`.cmt` バイナリ読取**: OCaml の cmt フォーマット仕様調査が必要。LSP 非依存ホバー型表示を `RescriptDocumentationProvider` のフォールバックに組み込む
- **Build Console ToolWindow**: `ToolWindowFactory` + `rescript build --watch` のプロセス管理 + 出力パーサー + クリックで該当行ジャンプ
- **追加 stub index**: `IStubElementType` 5 種類追加、`getStubVersion()` を bump
- **Structural Search and Replace**: `StructuralSearchProfile` 拡張、ReScript の AST パターン構文を定義
- **Call Hierarchy**: LSP `callHierarchy/incomingCalls` / `outgoingCalls` 経由、ToolWindow 表示

## 制約と判断記録

- バケット A の panel 色付け 3 件 (Interop / Type Impact / Notebook) は元の renderer のシグネチャ互換を維持しつつ色を足す。マウスイベント・ダブルクリックナビゲーション等の既存挙動には触れない
- バケット A の Visual 2 件 (Variant Flow / Module Dependency) は `computeLayout` 純関数の geometry に触れない。凡例は `paintComponent` で `canvasSize.height` を `LEGEND_HEIGHT = 28` だけ拡張する形で追加
- バケット B 以降のスコープは A 完了後にユーザー再評価する。本ステアリングではコミット粒度・テスト戦略のみ仮置き
