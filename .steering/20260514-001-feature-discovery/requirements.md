# 機能発掘調査 — 要求内容

## 背景

`docs/product-requirements.md` のロードマップは 109 件すべて実装完了で空になっている。今後の開発方針を決めるため、以下の 4 軸で調査を行い、次に取り組む候補の優先度を決める。

1. 他の ReScript 関連 IntelliJ プラグインの新機能を棚卸し、自プラグインに欠けている機能を抽出する
2. 関数型/他言語 IDE で評価されている機能を棚卸し、ReScript に転用できるものを抽出する
3. 既存機能のブラッシュアップ候補を抽出する
4. 最近実装した Visual / Panel 機能の色付けが単色なので、適切な意味別色分けの設計をする

調査は `.claude/rules/audit-tasks.md` に従い、subagent の一次調査 → 二段検証の手順で実施した。

## 調査結果サマリ

### 軸 1: 他 ReScript プラグイン

| プラグイン | 状態 | 自プラグインに欠ける機能 |
|---|---|---|
| reasonml-idea-plugin (giraud) | 低頻度メンテ。`archived: false`、351 stars、28 open issues。最新リリースは 0.131 (2025-09-01)、それ以降は README + dependabot のみ。**CLAUDE.md の「メンテ停止状態」は実態と齟齬あり、要訂正** | `.cmt`/`.cmti` バイナリ読取、Build Console ToolWindow、variant constructor / record field / object field / parameter の stub index、CMT ベースの ModuleSignature view |
| ReScript Language (Johannes Klauss) | 2026-03-20 リリースの新顔プラグイン (DL 75)。LSP 薄ラッパー | 機能は自プラグインの完全サブセット — 移植価値なし |
| OCaml 系プラグイン (3 件) | OCaml 専用 | ReScript への直接移植価値なし |

### 軸 2: 関数型/他言語 IDE 機能 (トップ 5 抜粋)

| 機能 | 由来 | ReScript 適用性 | 既存資産との接続 |
|---|---|---|---|
| Pipeline Hints (`->` 各段の中間型を inline 表示) | F# Ionide | 高 | Narrowing Visualizer の延長 |
| `open` を qualified access に展開する code action | HLS importLens / rust-analyzer | 高 | 既存の `RescriptImportOptimizer` 拡張 |
| Structural Search and Replace (AST ワイルドカード一括置換) | rust-analyzer | 高 | 既存 `RescriptVariantFlowModel` の AST 走査再利用 |
| doc コメント内評価 (`// >` で結果を inline 注釈) | HLS Eval plugin | 高 | 既存 `RescriptReplExecutor` + CodeVision |
| Test Code Lens (テスト関数上に Run/Debug レンズ) | Metals / rust-analyzer | 高 | 既存 `RescriptTestRunConfigurationType` 接続のみ |

その他の候補 (中〜高): Wingman 風 type hole 補完、`if/match` 相互変換 intention、Call Hierarchy ToolWindow、ネスト switch 平坦化 intention、record/variant placeholder 補完、inferred 型注釈の一括挿入 quick fix。

### 軸 3: 既存機能のブラッシュアップ候補

軸 4 (色付け監査) と統合した結果、計 14 箇所の表示コンポーネントを監査し、11 件の色付け候補を確定した (詳細は次節「候補機能リスト」を参照)。

### 軸 4: ドキュメント整合性

- `CLAUDE.md` および `docs/product-requirements.md` の「reasonml-idea-plugin はメンテナンス停止状態」記述は実態とずれているため、「2025-09 を最後にリリース停滞」等の正確な表現に補正する

## 14 箇所の表示コンポーネント監査結果

`src/main/kotlin/com/rescript/plugin/` 配下の全パッケージで「ReScript ソースっぽいテキスト・型シグネチャ・Mermaid・コード断片」を表示している箇所を洗い出した。

| 区分 | 件数 | 内容 |
|---|---|---|
| 既に Editor 経由でハイライト済み (N/A) | 4 件 | REPL input/output、Compiled JS Preview |
| 色付け価値「高」 | 6 件 | Hoogle 検索結果、Variant Flow Mermaid ソース、Module Dependency Mermaid ソース、Type Info、PPX View、Notebook 入力 |
| 色付け価値「中」 | 2 件 | Interop Risk previewLine、Type Impact previewLine (severity 色付けで主目的は満たされ、preview 行までハイライトする ROI は低) |
| 色付け価値「低」 | 2 件 | Type Coverage テーブル、Dependencies ツリー (メタデータ表示で syntax highlighting の意味薄い) |

加えて、Visual 機能の意味別色分け候補 (color audit の本筋):

| 機能 | 課題 |
|---|---|
| Variant Flow Visual | 全 box / edge / arm が単色赤系 |
| Module Dependency Visual | 全 box / edge が単色赤系 (in-degree 0 / 中間 / 葉 / サイクル内の区別なし) |
| Interop Risk panel | HIGH/MEDIUM/LOW がテキストのみ |
| Type Impact panel | kind がテキストのみ |
| Notebook cell | `Color(0xC0C0C0)` 等の生 `Color` ハードコード (ダーク対応バグ) |
| Coverage panel | red/yellow/green で正しく色分け (OK) |
| Error Lens | severity 色付き (OK) |

## 候補機能リスト (実装単位での分類)

優先度は ROI (既存資産の再利用度) と「単独セッションで完結可能か」で評価。各候補は `[YYYYMMDD]-[NNN]-*` 単位で別ステアリングを切る想定。

### バケット A: 既存 Visual / Panel の意味別色付け (5 件、即着手可)

1. **Variant Flow Visual の意味別色分け** — arm 種別 (constructor / wildcard / pattern binding / todo placeholder / nested switch) で 5 色 + 凡例
2. **Module Dependency Visual の Kahn 分類色分け** — entry-point / 中間 / 葉 / サイクル内を 4 色 + 凡例
3. **Interop Risk panel の severity 色帯** — HIGH 赤 / MEDIUM 黄 / LOW グレーの左マージン色帯
4. **Type Impact panel の kind 別表示** — type-ref / constructor / pattern / field-access に色付きラベル
5. **Notebook cell の `JBColor` 化バグ修正** — `Color(0xCC0000)` 等の生 `Color` を `JBColor` 系へ

→ ステアリング `20260514-002-visual-color-brushup` で実装 (承認済み)

### バケット B: ReScript syntax-based 色付け (4 件、`RescriptLexer` + `RescriptSyntaxHighlighter` 共通基盤)

6. **Hoogle 検索結果のシグネチャ** — `RescriptTypeSignatureCellRenderer` でレクサーによるトークン分解 + `getTokenHighlights` で TextAttributesKey 引き
7. **Type Info panel** — `JBLabel` → `EditorTextField` (read-only) + ReScript file type
8. **PPX View panel** — `JTextArea` → `EditorTextField` + ReScript file type
9. **Notebook cell 入力** — `JTextArea` → `EditorTextField` + ReScript file type (REPL 入力と一貫)

→ 後続ステアリング (バケット A 完了後)

### バケット C: 高 ROI な新機能 (既存資産で安価に作れる、後回し可)

10. **Test Code Lens** — `RescriptTestRunConfigurationType` を既存テスト関数 (`describe` / `it` / `test`) の行に CodeVision として表示
11. **doc コメント内評価 (CodeVision)** — `// > expr` 形式のコメントを `RescriptReplExecutor` で評価し結果を inline 注釈表示
12. **`open` を qualified access に展開する code action** — `open Belt` を `Belt.Array.map` 形に展開する Alt+Enter intention
13. **Pipeline Hints** — `->` パイプ各段の中間型を InlayHint で表示
14. **ネスト switch 平坦化 intention** — `switch x { | Some(y) => switch y ...}` を 1 階層に統合

### バケット D: より重い新機能 (ステアリング複数回相当)

15. **Mermaid syntax 色付け** (Variant Flow ソースモード / Module Dependency ソースモード) — 新規 Mermaid 用ミニ lexer
16. **`.cmt`/`.cmti` バイナリ読取で LSP 非依存のホバー型表示**
17. **Build Console 専用 ToolWindow + watch ストリーム**
18. **追加 stub index (variant constructor / record field / object field / parameter)**
19. **Structural Search and Replace**
20. **Call Hierarchy ToolWindow**

### バケット E: ドキュメント整合性 (軽微)

21. **CLAUDE.md / docs/product-requirements.md の reasonml-idea-plugin 記述補正**

## 受け入れ条件

- バケット A〜E の候補について、ユーザーが次のステアリングで取り組む対象を選べる状態にする (このステアリングは調査のみで実装はしない)
- 採用候補が決まったら、それぞれを新規ステアリング `[YYYYMMDD]-[NNN]-*` として起こす
- 本ステアリングの成果物は `requirements.md` + `design.md` + `tasklist.md` の 3 点
- バケット A の実装は別ステアリング `20260514-002-visual-color-brushup` に承継済み

## 二段検証の記録

- 軸 1: subagent 報告の核心主張「reasonml-idea-plugin がメンテ継続中」を `gh api repos/giraud/reasonml-idea-plugin` で実証 → `archived: false`、ただし最終コードリリースは 2025-09-01 でその後は README + dependabot のみ。subagent 報告は楽観寄り、PRD 記述は悲観寄り、両方に補正が要る
- 軸 2: subagent が一次情報 (公式 docs / GitHub README) のみを引用、ブログ推測は除外済み。スポットチェック対象は実装段階で各候補のステアリング単位で実施
- 軸 3 & 4: 内部 audit は `grep` で本人が直接確認、追加で Explore agent に 14 箇所監査を依頼 (file:line で実証)

## 残存する不確実性

- `OREditorLinePainter` (reasonml-idea-plugin) の正確な挙動 — plugin.xml 登録だけでは LSP 不要のエラー描画か、コンパイラ出力 inline かを確定できなかった
- ReScript LSP の `callHierarchy` サポート可否 — バケット C の前提条件、別途確認が必要
