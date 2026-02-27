# B 優先度機能 — 要件定義

## 概要

ロードマップ B 優先度から、ユーザー価値が高く実装可能な 4 機能を選定して実装する。

## 対象機能

| # | 機能 | カテゴリ | 難易度 |
|---|------|---------|--------|
| 93 | 常時型表示パネル | ToolWindow | 低 |
| 96 | レコードスタブ生成 | Generate | 低 |
| 54 | IntelliLang 連携強化 | インジェクション | 低〜中 |
| 82 | 分割代入の導入/解除 | Intention | 中 |

### 除外: #109 PPX 可視化

rescript-language-server が PPX 展開結果を提供する API を持たないため、プラグイン側だけでは実装不可。LSP サーバーへの upstream issue として別途対応。

---

## #93 常時型表示パネル (Type Info ToolWindow)

### 概要

カーソル位置の式の型を常時表示するツールウィンドウ。Ctrl+Shift+P（Expression Type）の常時表示版。

### 受け入れ条件

- [ ] ToolWindow「ReScript Type」が IDE 右側/下部に表示される
- [ ] エディタのカーソル移動に追従して型情報が自動更新される
- [ ] ReScript ファイル以外ではパネルが空になる（エラーにならない）
- [ ] LSP が未起動の場合は「LSP not available」等のメッセージを表示する
- [ ] 更新は debounce する（300ms 程度）— キャレット移動のたびに LSP リクエストを発火しない

### 実装方針

- `RescriptLspUtils.getHoverType()` を利用して型情報を取得
- 既存の `RescriptCompiledJsPreviewToolWindowFactory` をパターンとして参考にする
- `CaretListener` でカーソル位置変更を検知し、debounce 後に型情報を更新

---

## #96 レコードスタブ生成 (Record Stub Generation)

### 概要

レコード型の全フィールドにデフォルト値を付けたスタブコードを生成する Generate アクション (Cmd+N)。

### 受け入れ条件

- [ ] Cmd+N メニューに「Record Value」が表示される
- [ ] 選択したレコード型の全フィールドにデフォルト値が挿入される
- [ ] デフォルト値の割り当て規則:
  - `string` → `""`
  - `int` → `0`
  - `float` → `0.0`
  - `bool` → `false`
  - `option<_>` → `None`
  - `array<_>` → `[]`
  - `unit` → `()`
  - その他 → `todo` (プレースホルダー)
- [ ] 生成されたコードは ReScript の構文として有効である

### 実装方針

- 既存の `RescriptGenerateMakeAction` のパターンを踏襲
- `RescriptTypeDeclarationParser` で型情報を取得（`RecordField.typeAnnotation` を利用）

---

## #54 IntelliLang 連携強化

### 概要

`%raw()` 以外の拡張構文でも言語インジェクションを提供する。

### 受け入れ条件

- [ ] `%re("...")` / `%%re(\`...\`)` 内で正規表現のハイライト・バリデーションが有効になる
- [ ] 設定画面等で有効/無効を切り替えられる（既存の IntelliLang 設定を活用）

### 実装方針

- 既存の `RescriptRawJsInjector` を拡張し、`%re` パターンを追加
- IntelliJ の `RegExp` 言語 ID を使用

### スコープ限定

`%sql`, `%graphql` 等の非標準拡張は対象外（ReScript 標準ライブラリの `%re` のみ）。

---

## #82 分割代入の導入/解除 (Destructuring Intention)

### 概要

let バインディングのパターンを分割代入に変換、または分割代入を展開する Intention Action。

### 受け入れ条件

- [ ] `let {x, y} = record` → 個別の let バインディングに展開できる（解除）
- [ ] 展開結果が構文として正しい
- [ ] Alt+Enter メニューに「Expand destructuring」が表示される

### スコープ限定

**解除（展開）のみ** を実装する。導入（複数 let を分割代入に統合）はパーサーの制約上、安定した実装が困難なため除外。

### 実装方針

- テキストベースのパターンマッチで `let {field1, field2, ...} = expr` を検出
- 各フィールドを `let field1 = expr.field1` に展開
- 既存の Intention パターン（`RescriptCaseSplitIntention` 等）を参考にする
