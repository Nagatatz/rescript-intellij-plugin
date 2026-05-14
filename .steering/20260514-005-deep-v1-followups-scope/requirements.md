# 要求: v1 制限の解消 — フォローアップスコープ

## 背景

本ステアリングは **実装ステアリングではない**。本セッション (`worktree-v1-followups-20260514`) で steering 003 / 004 (元 002 / 003 — 並列セッションの 002-visual-color-brushup と番号衝突したため rebase 時に renumber) を実装した後の、フォローアップで実装すべき 4 項目をスコープ化するための文書。CLAUDE.md / docs/product-requirements.md / README.md の現状を読み、各機能の「v1 制限事項」として明示されている部分の解消を計画する。

各項目は独立したセッションで実装する想定で、それぞれ専用の `.steering/[新日付]-NNN-*` ディレクトリを切ること。

## 項目 1: Hoogle-style Type Signature Search v2

### 現状

`CLAUDE.md` より:

> Hoogle-style Type Signature Search (`navigation/RescriptTypeSignatureSearchContributor`) は Search Everywhere の "ReScript Types" タブで …. レコード型・ポリモーフィックバリアント・ラベル引数は v1 ではパースしない。

### ゴール

`RescriptTypeParser` / `RescriptTypeAst` / `RescriptTypeUnifier` を拡張し、次の型をサポート:

- レコード型 `{x: int, y: string}`
- ポリモーフィックバリアント `[#A | #B(int)]`
- ラベル引数 `(~x: int, ~y: string=?) => unit`

### スコープ

- 拡張対象: `src/main/kotlin/com/rescript/plugin/navigation/RescriptType*.kt`
- 新 AST ノード: `Record(fields: List<Pair<String, AstNode>>)`, `PolyVariant(constructors: List<...>)`, `Labeled(name: String, optional: Boolean, ...)`
- Unifier に新ノードのマッチング規則を追加（EXACT / TVAR_MATCH / PARTIAL の意味は維持）
- テスト: 各型ごとに 5 件以上の typed query
- ドキュメント: `sphinx-docs/user/features/navigation.md` の例示を更新

### 難易度

中〜高（パーサーの状態機械が増える）

---

## 項目 2: Type Coverage Heat Map — LSP hover precision

### 現状

`CLAUDE.md` より:

> Type Coverage Heat Map (`coverage/`) は … パラメータ単位の annotated 判定や LSP hover ベースの精度向上は将来検討。

現状の `RescriptTypeCoverageClassifier` は depth-0 `:` ヒューリスティック。返り値型注釈の有無は判定するが、パラメータごとの annotated 状態は見ない。

### ゴール

1. **パラメータ単位の annotated 判定**: `let f = (x: int, y: string, z) => ...` で「3 パラメータ中 2 個が annotated = 2/3」のような細粒度カウント
2. **LSP hover ベースの精度向上**: 推論結果が `'a`（型変数のみ）の場合を「弱型付け」と分類し、annotated でも inferred でもない第三カテゴリとして集計

### スコープ

- `coverage/RescriptTypeCoverageClassifier`: パラメータ抽出ロジック追加
- `coverage/RescriptTypeCoverageModel`: `{annotated, inferredKnown, inferredWeak}` の 3 値カウント
- `coverage/RescriptTypeCoveragePanel`: 列追加 + 色分け調整
- LSP 統合: `textDocument/hover` を非同期で発行し、`'a` を含む結果をマークする
- LSP 不在時は従来動作にフォールバック
- テスト: フィクスチャ `.res` で各パターンを検証

### 難易度

中（LSP の非同期呼び出しと UI 更新の thread 管理が要る）

---

## 項目 3: Add Missing Switch Arms — GADT / first-class module / 多タプル

### 現状

`CLAUDE.md` より:

> Add Missing Switch Arms Intention … `_` ワイルドカードや LIDENT 単独 binding を含む switch では非表示。

GADT (`type t<_>`) や first-class module (`module type S = sig ... end`) を scrutinee に持つ switch、多タプル `(a, b, c, d)` の payload を持つ variant への対応は未実装。

### ゴール

- GADT 用の型パラメータ表記をパースし、constructor 抽出に活用
- 多タプル payload `Foo(a, b, c, d)` のアーム生成で正しい個数の `_` を出す
- first-class module は対応難（hover 結果から module type を取得する必要あり）— optional

### スコープ

- 拡張対象: `intention/RescriptAddMissingSwitchArmsIntention`, `lsp/RescriptVariantTypeResolver`, `generate/RescriptTypeDeclarationParser`
- テスト: GADT / 多タプル / first-class module の小さな switch ケース 5 件以上

### 難易度

中〜高（GADT の型パラメータ伝搬は地味だが範囲が広い）

---

## 項目 4: Variant Flow Diagram — MAX_NESTING_DEPTH を Settings 化

### 現状

`flow/RescriptVariantFlowModel.kt:58`:

```kotlin
internal const val MAX_NESTING_DEPTH = 3
```

ユーザーが深いネストを見たい場合に手段がない。

### ゴール

`RescriptProjectSettings` に `variantFlowMaxDepth: Int = 3` を追加し、Settings UI に IntSpinner で公開。`RescriptVariantFlowModel.buildAtOffset` がデフォルトを取りつつ、明示パラメータでオーバーライド可能にする。

### スコープ

- `settings/RescriptProjectSettings`: `variantFlowMaxDepth` プロパティ追加
- `settings/RescriptSettingsSchema`: 新規 `IntDescriptor`（無ければ追加）で UI 行を追加
- `flow/RescriptVariantFlowModel.buildAtOffset(source, offset, maxDepth)`: 引数を追加（デフォルト維持で既存テストは影響なし）
- `flow/RescriptVariantFlowPanel`: 設定値を読み取って渡す
- テスト: model に `maxDepth = 1` と `maxDepth = 5` の 2 シナリオ
- ドキュメント: CLAUDE.md / sphinx-docs / README に depth 設定の説明追加

### 難易度

低〜中

### 注意

`good-first-issues.md` のエントリ #4 と内容が重複している。実装着手時に good-first-issues.md からそのエントリを削除すること（実装完了マーカー）。

---

## 各項目の共通要件

- 専用の `.steering/[新日付]-NNN-*` ディレクトリを切る
- ステアリング番号採番ルール (`.claude/rules/steering-workflow.md`) に従う
- `EnterWorktree` で隔離した worktree で実装する
- DoD (`.claude/rules/definition-of-done.md`) の 5 フェーズすべて通過する
- 各項目は独立 PR とする（4 項目を 1 PR にまとめない）

## 範囲外

- Mutation testing 拡張 (PIT を `indexing/` `narrowing/` `flow/` に拡大) — 別ステアリングで対応
- CI workflow 更新（PR ジョブを `test -Pscope=fast` に切り替え） — 別ステアリング
- Plugin Verifier ignored-problems entries の Expires 棚卸し — `good-first-issues.md` エントリ #10 に対応
