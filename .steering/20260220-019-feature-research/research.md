# ReScript IntelliJ Plugin — Feature Research

> 他プラグイン・VSCode ReScript 拡張・IntelliJ Platform SDK から調査した新機能候補の網羅リスト。
> 既存 `plugin.xml` に登録済みの Extension Point との重複を排除済み。

## 優先度の基準

| 優先度 | 定義 | 判定条件 |
|--------|------|----------|
| **S** | 最優先 | ユーザー体験に直結 + 実装容易（1-2日） |
| **A** | 高優先 | インパクト大 or VSCode パリティに必須 |
| **B** | 中優先 | あると便利 + 中程度の労力（3-5日） |
| **C** | 低優先 | nice-to-have or 高難度（1週間超） |

## 実装難易度の定義

| 難易度 | 定義 |
|--------|------|
| **低** | 1-2 クラス追加、PSI 依存なし or 既存 PSI で十分、1日以内 |
| **中** | 3-5 クラス追加、軽量パーサーの活用が必要、2-3日 |
| **高** | 大規模な追加 or パーサー拡張が必要、5日以上 |

---

## カテゴリ 1: 編集操作 (11件)

### 1-1. Extend/Shrink Selection (構造的選択拡張)
- **Extension Point:** `com.intellij.extendWordSelectionHandler`
- **インターフェース:** `ExtendWordSelectionHandler`
- **概要:** Opt+Up/Down で構造的に選択範囲を拡張・縮小する。標準のトークンベース選択に加え、関数引数グループ、タプル要素、JSX 属性、パイプライン式、パターンマッチアームなどを中間ステップとして選択可能にする。
- **ReScript での活用:**
  - `|>` パイプラインチェーンの1段階選択
  - JSX 属性 (`className="foo"`) の値→属性→タグ全体の段階選択
  - `switch` アームの1アーム→全アーム→switch 式全体
  - レコードフィールドの1フィールド→フィールドリスト→レコード全体
- **実装難易度:** 低
- **パーサー拡張:** 不要（既存 PSI のトークン境界で動作）
- **参考プラグイン:** Rust, Go, Kotlin, TypeScript
- **優先度:** **S**

### 1-2. Unwrap/Remove (囲み構造の除去)
- **Extension Point:** `com.intellij.unwrapDescriptor`
- **インターフェース:** `UnwrapDescriptor`
- **概要:** Cmd+Shift+Delete で囲み構造を除去。`Some(expr)` → `expr`、`if cond { body }` → `body`、`{ block }` → 中身の式を展開。
- **ReScript での活用:**
  - `Some(x)` → `x` (Unwrap Some)
  - `Ok(x)` → `x` (Unwrap Ok)
  - `Error(x)` → `x` (Unwrap Error)
  - `if cond { body }` → `body` (Unwrap if)
  - `switch x { ... }` → 選択されたアームのボディ (Unwrap switch arm)
  - `try { body } catch { ... }` → `body` (Unwrap try)
  - `{ block }` → 中身の式 (Unwrap block)
- **実装難易度:** 中
- **パーサー拡張:** 不要（トークンマッチングベース or 既存 PSI で動作）
- **参考プラグイン:** Java, Kotlin, Rust
- **優先度:** **A**

### 1-3. Move Element Left/Right (要素の左右移動)
- **Extension Point:** `com.intellij.moveLeftRightHandler`
- **インターフェース:** `MoveElementLeftRightHandler`
- **概要:** Opt+Shift+Left/Right で隣接する兄弟要素の位置を交換する。
- **ReScript での活用:**
  - 関数引数の順序変更: `foo(a, b)` → `foo(b, a)`
  - タプル要素の順序変更: `(x, y, z)` → `(y, x, z)`
  - JSX 属性の順序変更: `<Comp a=1 b=2 />` → `<Comp b=2 a=1 />`
  - バリアントコンストラクタのペイロード: `Some(a, b)` → `Some(b, a)`
  - パイプライン引数: `list->map(f)->filter(g)` のステージ交換
- **実装難易度:** 中
- **パーサー拡張:** 中程度（引数リスト・タプル要素を PSI 子要素として認識する必要あり）
- **参考プラグイン:** Rust, Kotlin
- **優先度:** **B**

### 1-4. Join Lines (行結合のカスタマイズ)
- **Extension Point:** `com.intellij.joinLinesHandler`
- **インターフェース:** `JoinLinesHandlerDelegate`
- **概要:** Ctrl+Shift+J の行結合ロジックをカスタマイズ。
- **ReScript での活用:**
  - 複数行文字列リテラルの結合（`\n` を適切に処理）
  - 複数行レコードフィールドの1行化: `{ a: 1, \n b: 2 }` → `{ a: 1, b: 2 }`
  - `let` バインディング + 次の式の結合
  - `open` 宣言の結合（重複 open 検知と連動）
- **実装難易度:** 低
- **パーサー拡張:** 不要
- **参考プラグイン:** Java, Kotlin
- **優先度:** **B**

### 1-5. Typed Handler (タイプ時の自動処理)
- **Extension Point:** `com.intellij.typedHandler`
- **インターフェース:** `TypedHandlerDelegate`
- **概要:** 特定の文字入力時にカスタムロジックを実行。
- **ReScript での活用:**
  - `|` 入力時のパイプ `->` 自動補完トリガー
  - `{` 入力時の JSX 式展開 `{|}`
  - `<` 入力時の JSX タグ自動補完
  - `.` 入力時のモジュールアクセス補完トリガー
  - `"` 入力後のテンプレートリテラル `\`` → `j\`` 展開
  - `(` 入力時の `switch` パターンの自動括弧付与
- **実装難易度:** 低〜中
- **パーサー拡張:** 不要
- **参考プラグイン:** TypeScript, Rust, Kotlin
- **優先度:** **A**

### 1-6. Backspace Handler (Backspace の拡張)
- **Extension Point:** `com.intellij.backspaceHandlerDelegate`
- **インターフェース:** `BackspaceHandlerDelegate`
- **概要:** Backspace キーのカスタム動作。
- **ReScript での活用:**
  - `{}` / `()` / `[]` のペア削除（空のペアで Backspace → 両方削除）
  - テンプレートリテラル `j"..."` の開始タグ削除時のペア処理
  - JSX 自己閉じタグ `<Comp />` の `/` 削除時の動作
  - インデントのスマート削除（ブロック内で Backspace → インデントレベル単位で戻る）
- **実装難易度:** 低
- **パーサー拡張:** 不要
- **参考プラグイン:** Kotlin, TypeScript
- **優先度:** **B**

### 1-7. Enter Handler (Enter キーの拡張)
- **Extension Point:** `com.intellij.enterHandlerDelegate`
- **インターフェース:** `EnterHandlerDelegate`
- **概要:** Enter キー押下時のカスタムロジック。`lang.smartEnterProcessor`（登録済み）を補完する。
- **ReScript での活用:**
  - JSX タグ内での Enter: `<div>|</div>` → `<div>\n  |\n</div>` (自動展開)
  - ドキュメントコメント `/** */` 内での Enter: `* ` プレフィックス自動挿入
  - パイプライン式の途中での Enter: `->` の後に自動インデント
  - `switch` ケース追加: `| ` の後の改行で次のケースのテンプレート挿入
- **実装難易度:** 中
- **パーサー拡張:** 不要
- **参考プラグイン:** TypeScript, Java, Kotlin
- **優先度:** **A**

### 1-8. Code Block Provider (コードブロック境界定義)
- **Extension Point:** `com.intellij.codeBlockSupportHandler`
- **インターフェース:** `CodeBlockSupportHandler`
- **概要:** Cmd+Shift+P のコードブロック選択と、対応する括弧ジャンプのカスタマイズ。
- **ReScript での活用:**
  - `module Foo = { ... }` のブロック境界認識
  - `switch x { ... }` のケースブロック
  - `if ... { } else { }` の条件ブロック
  - JSX 要素 `<Comp>...</Comp>` の開始タグ↔終了タグ
- **実装難易度:** 中
- **パーサー拡張:** 不要（トークンベースで動作可能）
- **参考プラグイン:** Rust, Go
- **優先度:** **B**

### 1-9. Line Wrap Strategy (折り返し戦略)
- **Extension Point:** `com.intellij.lang.lineWrapStrategy`
- **インターフェース:** `LineWrapPositionStrategy`
- **概要:** エディタのソフトラップ位置を言語構造に合わせてカスタマイズ。
- **ReScript での活用:**
  - パイプライン `->` の前で折り返し
  - 関数引数のカンマの後で折り返し
  - JSX 属性の境界で折り返し
  - パターンマッチの `|` の前で折り返し
- **実装難易度:** 低
- **パーサー拡張:** 不要
- **参考プラグイン:** Java, Kotlin
- **優先度:** **C**

### 1-10. Copy/Paste Processor (コピー＆ペーストの前後処理)
- **Extension Point:** `com.intellij.copyPastePreProcessor` / `com.intellij.copyPastePostProcessor`
- **インターフェース:** `CopyPastePreProcessor` / `CopyPastePostProcessor<ReferenceData>`
- **概要:** コピー時のデータ変換、ペースト時の自動調整。
- **ReScript での活用:**
  - ペースト時の `open` 文自動追加（他モジュールからコードをペーストした際に必要な `open` を挿入）
  - HTML/JSX → ReScript JSX 自動変換（VSCode パリティ: Paste as ReScript JSX）
  - インデントの自動正規化（異なるインデントレベルからのペースト時）
  - 文字列リテラル内へのペースト時のエスケープ処理
- **実装難易度:** 中
- **パーサー拡張:** 不要
- **参考プラグイン:** Kotlin, TypeScript
- **優先度:** **A** (JSX 変換は VSCode パリティとして重要)

### 1-11. Strip Trailing Spaces Filter
- **Extension Point:** `com.intellij.stripTrailingSpacesFilterFactory`
- **インターフェース:** `StripTrailingSpacesFilterFactory`
- **概要:** ファイル保存時の末尾スペース削除ロジックをカスタマイズ。
- **ReScript での活用:**
  - テンプレートリテラル内の意図的なスペースを保護
  - ドキュメントコメント内の Markdown フォーマット用スペースを保護
- **実装難易度:** 低
- **パーサー拡張:** 不要
- **参考プラグイン:** Markdown, Python
- **優先度:** **C**

---

## カテゴリ 2: ナビゲーション (10件)

### 2-1. Context Info (コンテキスト情報表示)
- **Extension Point:** `com.intellij.codeInsight.lineMarkerProvider`
- **インターフェース:** `RelatedItemLineMarkerProvider`
- **概要:** ガターアイコンによるナビゲーションマーカー。関連ファイル・テスト・実装へのジャンプ。
- **ReScript での活用:**
  - `.res` ファイルから対応する `.resi` インターフェースへのガターアイコン（⇄マーク）
  - `.res` ファイルからコンパイル済み `.js` へのガターアイコン
  - テストファイルからソースファイルへのガターアイコン（逆方向も）
  - `@module` バインディングから外部 npm パッケージへのリンク
- **実装難易度:** 低〜中
- **パーサー拡張:** 不要
- **参考プラグイン:** Java, Kotlin, Go
- **優先度:** **A**

### 2-2. Call Hierarchy (呼び出し階層)
- **Extension Point:** `com.intellij.callHierarchyProvider`
- **インターフェース:** `CallHierarchyProvider`
- **概要:** Ctrl+Opt+H で関数の呼び出し元/呼び出し先のツリーを表示。
- **ReScript での活用:**
  - `let` バインディング関数の呼び出し元ツリー
  - モジュール関数のプロジェクト全体での使用箇所
  - React コンポーネントの使用箇所ツリー
- **実装難易度:** 高（クロスファイル参照解決が必要、ファイルベースインデックスに依存）
- **パーサー拡張:** 高（関数呼び出しの PSI ノードが必要）
- **参考プラグイン:** Java, Kotlin, Go
- **優先度:** **C**

### 2-3. Go to Test / Create Test (テストへのジャンプ)
- **Extension Point:** `com.intellij.testFinder` / `com.intellij.testCreator`
- **インターフェース:** `TestFinder` / `TestCreator`
- **概要:** Cmd+Shift+T でソース↔テストファイルを相互ジャンプ、テストファイルの自動生成。
- **ReScript での活用:**
  - `Foo.res` → `Foo_test.res` / `FooTest.res` / `__tests__/Foo_test.res` の規約ベースマッピング
  - テストファイルが存在しない場合の自動生成（jest/vitest テンプレート付き）
  - `rescript.json` の `sources` 設定からテストディレクトリを自動検出
- **実装難易度:** 低（ファイル名規約ベース）
- **パーサー拡張:** 不要
- **参考プラグイン:** Java, Go, Rust
- **優先度:** **S**

### 2-4. Search Everywhere (検索統合の強化)
- **Extension Point:** `com.intellij.searchEverywhereContributor` / `com.intellij.gotoClassContributor`
- **インターフェース:** `SearchEverywhereContributor<T>` / `ChooseByNameContributorEx`
- **概要:** Shift×2 の Search Everywhere と Cmd+O の Go to Class にモジュール名を供給。
- **ReScript での活用:**
  - モジュール名 (`User`, `App.Router`) での検索
  - 型名 (`User.t`, `response`) での検索
  - コンポーネント名 (`@react.component` 付き関数) での検索
- **実装難易度:** 高（ファイルベースインデックスに依存）
- **パーサー拡張:** 中（トップレベル宣言の認識は既存パーサーで対応可能）
- **参考プラグイン:** Rust, Go, Kotlin
- **優先度:** **B**

### 2-5. Run Anything (なんでも実行)
- **Extension Point:** `com.intellij.runAnything.executionProvider`
- **インターフェース:** `RunAnythingProvider<V>`
- **概要:** Ctrl×2 の Run Anything ダイアログに ReScript 固有のコマンドを追加。
- **ReScript での活用:**
  - `rescript build` / `rescript build -w` の即座実行
  - `rescript clean` の即座実行
  - `rescript format <file>` の即座実行
  - `npx reanalyze` の即座実行
- **実装難易度:** 中
- **パーサー拡張:** 不要
- **参考プラグイン:** npm, Gradle
- **優先度:** **B**

### 2-6. Goto Super (親モジュール・インターフェースへのジャンプ)
- **Extension Point:** LSP `textDocument/typeDefinition` (IntelliJ LSP API が処理)
- **概要:** 変数の型定義へジャンプ。LSP の `textDocument/typeDefinition` リクエストを活用。
- **ReScript での活用:**
  - 変数のカーソル位置から型定義 (`type t = ...`) へジャンプ
  - `let x: User.t = ...` の `User.t` 型定義へ移動
- **実装難易度:** 低（IntelliJ の LSP クライアントが `typeDefinition` を自動処理する可能性あり。未対応の場合はアクション登録が必要）
- **パーサー拡張:** 不要
- **参考プラグイン:** TypeScript, Kotlin
- **優先度:** **A** (VSCode パリティ)

### 2-7. Go to Implementation (実装へのジャンプ)
- **Extension Point:** LSP `textDocument/implementation` (IntelliJ LSP API が処理)
- **概要:** インターフェース (`.resi`) から実装 (`.res`) へのジャンプ。
- **ReScript での活用:**
  - `.resi` の `let` 宣言から `.res` の実装へジャンプ
  - `module type` 宣言からその実装モジュールへジャンプ
- **実装難易度:** 低〜中
- **パーサー拡張:** 不要
- **参考プラグイン:** TypeScript, Java
- **優先度:** **A**

### 2-8. Navigation Bar (ナビゲーションバー統合)
- **Extension Point:** `com.intellij.navbar`
- **インターフェース:** `StructureAwareNavBarModelExtension`
- **概要:** エディタ上部のナビゲーションバーに現在のモジュール・関数・型のコンテキストを表示。
- **ReScript での活用:**
  - `File > Module > let binding` のパスをナビゲーションバーに表示
  - ネストしたモジュール内でのパス表示: `User > Validation > validate`
  - クリックで同一ファイル内のナビゲーション
- **実装難易度:** 中（既存の Structure View モデルを活用可能）
- **パーサー拡張:** 不要（既存の軽量パーサーの PSI で動作）
- **参考プラグイン:** Java, Kotlin, Go
- **優先度:** **B**

### 2-9. File Include Provider (ファイルインクルード関係)
- **Extension Point:** `com.intellij.include.provider`
- **インターフェース:** `FileIncludeProvider`
- **概要:** `open` 文によるモジュール依存関係の認識。
- **ReScript での活用:**
  - `open User` → `User.res` / `User.resi` ファイルへの依存リンク
  - Cmd+Click での `open` 先ファイルジャンプの補強
- **実装難易度:** 中
- **パーサー拡張:** 不要（`open` 宣言は既存パーサーで認識済み）
- **参考プラグイン:** PHP, Python
- **優先度:** **C**

### 2-10. Go to Declaration Handler (宣言ジャンプのカスタマイズ)
- **Extension Point:** `com.intellij.gotoDeclarationHandler`
- **インターフェース:** `GotoDeclarationHandler`
- **概要:** Cmd+Click / Cmd+B のジャンプ動作をカスタマイズ。LSP の `textDocument/definition` を補完。
- **ReScript での活用:**
  - `@module("react")` の文字列から `node_modules/react` へジャンプ
  - `@scope("document")` の文字列から MDN ドキュメントへの外部リンク
  - `rescript.json` 内のパスから実際のファイルへのジャンプ
- **実装難易度:** 中
- **パーサー拡張:** 不要
- **参考プラグイン:** TypeScript, PHP
- **優先度:** **B**

---

## カテゴリ 3: コード補完 (5件)

### 3-1. Completion Confidence (補完の自動表示制御)
- **Extension Point:** `com.intellij.completion.confidence`
- **インターフェース:** `CompletionConfidence`
- **概要:** 補完ポップアップの自動表示/非表示をコンテキストに応じて制御。
- **ReScript での活用:**
  - コメント内での自動補完を抑制
  - 文字列リテラル内での自動補完を抑制（テンプレートリテラル `${}` 内は例外）
  - `//` 行コメント入力時の補完抑制
  - 数値リテラル入力中の補完抑制
- **実装難易度:** 低
- **パーサー拡張:** 不要
- **参考プラグイン:** Kotlin, Java
- **優先度:** **S**

### 3-2. Lookup Char Filter (補完確定文字の制御)
- **Extension Point:** `com.intellij.lookup.charFilter`
- **インターフェース:** `CharFilter`
- **概要:** 補完ポップアップ表示中に特定の文字入力で確定/キャンセル/継続を制御。
- **ReScript での活用:**
  - `->` (パイプ) 入力で補完を確定して次の補完をトリガー
  - `.` 入力でモジュールアクセス補完を確定して次の補完をトリガー
  - `(` 入力で関数名補完を確定
  - `~` 入力でラベル付き引数の補完を継続
  - `|` 入力で補完をキャンセル（パターンマッチの `|` と解釈）
- **実装難易度:** 低
- **パーサー拡張:** 不要
- **参考プラグイン:** Kotlin, Go
- **優先度:** **A**

### 3-3. Completion Weigher (補完候補の重み付け)
- **Extension Point:** `com.intellij.weigher` (key=`"completion"`)
- **インターフェース:** `CompletionWeigher`
- **概要:** 補完候補のソート順をカスタムロジックで制御。
- **ReScript での活用:**
  - 同一モジュール内の宣言を優先
  - `open` 済みモジュールの関数を優先
  - `@react.component` コンテキストでは React 関連の補完を優先
  - 最近使用したシンボルを上位に
- **実装難易度:** 中
- **パーサー拡張:** 不要
- **参考プラグイン:** Kotlin, Java
- **優先度:** **B**

### 3-4. Live Template Context (テンプレートコンテキスト)
- **Extension Point:** `com.intellij.liveTemplateContext`
- **インターフェース:** `TemplateContextType`
- **概要:** Live Template の適用範囲をファイルタイプより細かく制御。
- **ReScript での活用:**
  - 「JSX 内」コンテキスト — JSX 属性・子要素用テンプレート
  - 「モジュール本体」コンテキスト — `let`、`type`、`module` 宣言用テンプレート
  - 「パターンマッチ」コンテキスト — `switch` アーム用テンプレート
  - 「トップレベル」コンテキスト — `@react.component`、`external` 用テンプレート
  - 「式」コンテキスト — 式レベルのスニペット
- **実装難易度:** 低
- **パーサー拡張:** 不要（トークンタイプ検査で動作）
- **参考プラグイン:** Kotlin, Java, Rust
- **優先度:** **A**

### 3-5. Live Template Macros (テンプレートマクロ)
- **Extension Point:** `com.intellij.liveTemplateMacro`
- **インターフェース:** `Macro`
- **概要:** Live Template の変数に動的な値を供給するカスタムマクロ。
- **ReScript での活用:**
  - `RESCRIPT_MODULE_NAME()` — ファイル名からモジュール名を推定 (`User.res` → `User`)
  - `RESCRIPT_COMPONENT_NAME()` — React コンポーネント名推定
  - `RESCRIPT_TYPE_NAME()` — カーソル位置の型名取得
  - `RESCRIPT_QUALIFIED_NAME()` — 完全修飾モジュールパス
- **実装難易度:** 低
- **パーサー拡張:** 不要
- **参考プラグイン:** Java, Kotlin
- **優先度:** **B**

---

## カテゴリ 4: リファクタリング (8件)

### 4-1. Extract Variable (変数抽出)
- **Extension Point:** `com.intellij.lang.refactoringSupport` (`RefactoringSupportProvider`)
- **概要:** 選択した式を `let` バインディングとして抽出。
- **ReScript での活用:**
  - 式を選択 → `let <name> = <expr>` を挿入し、元の場所を `<name>` に置換
  - パイプラインの中間結果を変数に抽出
- **実装難易度:** 高（正確な式境界の検出が必要）
- **パーサー拡張:** 高（式ノードの完全な PSI が必要）
- **参考プラグイン:** Kotlin, TypeScript, Rust
- **優先度:** **C**

### 4-2. Extract Function (関数抽出)
- **Extension Point:** `com.intellij.refactoring.extractMethod` (or LSP code action)
- **概要:** 選択したコードブロックを新しい関数として抽出。
- **ReScript での活用:**
  - 選択範囲のコードを `let <name> = (<params>) => { ... }` として抽出
  - 自由変数をパラメータとして自動検出
  - 戻り値の型推論（LSP との連携が理想）
- **実装難易度:** 高
- **パーサー拡張:** 高
- **参考プラグイン:** Kotlin, Java, TypeScript
- **優先度:** **C**

### 4-3. Inline Variable/Function (インライン化)
- **Extension Point:** `com.intellij.lang.refactoringSupport` (`RefactoringSupportProvider`)
- **概要:** 変数/関数の定義を使用箇所にインライン展開。
- **ReScript での活用:**
  - `let x = expr` → 全使用箇所を `expr` に置換し、`let` を削除
  - 単一使用の `let` バインディングの自動インライン化提案
- **実装難易度:** 高（全使用箇所の正確な検出が必要）
- **パーサー拡張:** 高
- **参考プラグイン:** Kotlin, Java
- **優先度:** **C**

### 4-4. Safe Delete (安全な削除)
- **Extension Point:** `com.intellij.lang.refactoringSupport` → `isSafeDeleteAvailable()`
- **概要:** 宣言を削除する前に使用箇所がないことを確認。
- **ReScript での活用:**
  - `let` バインディングの安全な削除（他ファイルから参照されていないことを確認）
  - `type` 定義の安全な削除
  - `module` 定義の安全な削除
  - ファイル削除時の参照チェック
- **実装難易度:** 高（クロスファイル参照解決が必要）
- **パーサー拡張:** 中
- **参考プラグイン:** Java, Kotlin
- **優先度:** **C**

### 4-5. Change Signature (シグネチャ変更)
- **Extension Point:** `com.intellij.refactoring.changeSignatureHandler`
- **概要:** 関数のパラメータ名・型・順序を変更し、全呼び出し箇所を自動更新。
- **ReScript での活用:**
  - ラベル付き引数の名前変更
  - パラメータの追加・削除・順序変更
  - デフォルト値の追加・変更
- **実装難易度:** 高
- **パーサー拡張:** 高
- **参考プラグイン:** Java, Kotlin
- **優先度:** **C**

### 4-6. Name Suggestion (名前候補の提案)
- **Extension Point:** `com.intellij.nameSuggestionProvider`
- **インターフェース:** `NameSuggestionProvider`
- **概要:** リネーム時に候補名を提案。
- **ReScript での活用:**
  - 型名から変数名を推定: `User.t` → `user`
  - コレクション型から複数形: `list<User.t>` → `users`
  - モジュール名から変数名: `UserService.make()` → `userService`
  - camelCase ↔ snake_case 変換候補
- **実装難易度:** 低
- **パーサー拡張:** 不要
- **参考プラグイン:** Kotlin, Java
- **優先度:** **B**

### 4-7. Suggested Refactoring (自動リファクタリング提案)
- **Extension Point:** `com.intellij.suggestedRefactoringSupport`
- **インターフェース:** `SuggestedRefactoringSupport`
- **概要:** 編集操作を監視し、リファクタリングを自動提案。
- **ReScript での活用:**
  - パラメータ名の変更を検知 → 全呼び出し箇所のリネーム提案
  - 関数シグネチャの変更を検知 → Change Signature 提案
- **実装難易度:** 高（PSI ミューテーション追跡が必要）
- **パーサー拡張:** 高
- **参考プラグイン:** Kotlin
- **優先度:** **C**

### 4-8. Refactoring Support Provider (リファクタリング機能宣言)
- **Extension Point:** `com.intellij.lang.refactoringSupport`
- **インターフェース:** `RefactoringSupportProvider`
- **概要:** 言語で利用可能なリファクタリングを宣言（インプレースリネーム、Safe Delete 等）。
- **ReScript での活用:**
  - インプレースリネームの有効化（ダイアログなしのインラインリネーム）
  - Safe Delete の有効化フラグ
  - Extract 系リファクタリングの有効化フラグ
- **実装難易度:** 低（メソッドのオーバーライドのみ）
- **パーサー拡張:** 不要（機能宣言のみ、実装は他の EP に委任）
- **参考プラグイン:** 全ての言語プラグイン
- **優先度:** **A**

---

## カテゴリ 5: コード分析・インスペクション (6件)

### 5-1. Expression Type Info (式の型情報表示)
- **Extension Point:** `com.intellij.expressionTypeProvider`
- **インターフェース:** `ExpressionTypeProvider<T extends PsiElement>`
- **概要:** Shift+Ctrl+P で式の推論型をポップアップ表示。
- **ReScript での活用:**
  - カーソル位置の式の推論型を表示（LSP hover を補完するオフライン版）
  - パイプラインの各ステップの型を表示
  - パターンマッチのバインディング変数の型表示
- **実装難易度:** 中（LSP の hover を内部で呼び出すハイブリッド実装が可能）
- **パーサー拡張:** 中
- **参考プラグイン:** Kotlin, Rust
- **優先度:** **B**

### 5-2. Custom Inspections (追加カスタムインスペクション)
- **Extension Point:** `com.intellij.localInspection` (既に3つ登録済み)
- **概要:** 追加のコードインスペクション。
- **ReScript での活用候補:**
  - **未使用 `let` バインディング検出** — `_` プレフィックスなしの未使用変数
  - **不要な型注釈検出** — 推論可能な型注釈に対する警告
  - **非推奨 API 使用検出** — `@deprecated` 属性付き関数の使用
  - **JSX key 属性欠落検出** — `list->map(...)` 内の JSX 要素に `key` がない場合
  - **冗長な `Some()` ラップ検出** — Option 型が期待される文脈での不要な `Some()`
  - **空の `switch` アーム検出** — ボディが空のパターンマッチ
- **実装難易度:** 中（各インスペクションにつき1クラス）
- **パーサー拡張:** 中（一部は PSI ツリー走査が必要）
- **参考プラグイン:** Kotlin, Rust, ESLint
- **優先度:** **B**

### 5-3. Problem Highlight Filter (ハイライト抑制フィルタ)
- **Extension Point:** `com.intellij.problemHighlightFilter`
- **インターフェース:** `ProblemHighlightFilter`
- **概要:** 特定のファイルでエラー/警告ハイライトを抑制。
- **ReScript での活用:**
  - `lib/bs/` / `lib/ocaml/` ビルド成果物のハイライト抑制
  - `node_modules/` 内の `.res` ファイルのハイライト抑制
  - 生成された `.js` / `.mjs` ファイルのハイライト抑制
- **実装難易度:** 低
- **パーサー拡張:** 不要
- **参考プラグイン:** Java, Kotlin
- **優先度:** **S**

### 5-4. Inspection Suppressor (インスペクション抑制コメント)
- **Extension Point:** `com.intellij.lang.inspectionSuppressor`
- **インターフェース:** `InspectionSuppressor`
- **概要:** コメントによるインスペクションの行単位/ブロック単位での抑制。
- **ReScript での活用:**
  - `// @rescript-ignore` で次の行のインスペクションを抑制
  - `// @rescript-ignore-all` でファイル全体の特定インスペクションを抑制
  - `// noinspection <InspectionId>` (IntelliJ 標準形式) のサポート
- **実装難易度:** 低
- **パーサー拡張:** 不要（コメントトークンの検査のみ）
- **参考プラグイン:** Kotlin, Java
- **優先度:** **A**

### 5-5. Highlight Usages (使用箇所のハイライト)
- **Extension Point:** `com.intellij.highlightUsagesHandlerFactory`
- **インターフェース:** `HighlightUsagesHandlerFactory`
- **概要:** カーソル位置のシンボルに対するセマンティックハイライト（Cmd+F7 モード）をカスタマイズ。
- **ReScript での活用:**
  - `switch` キーワードにカーソル → 全ケースアーム `|` をハイライト
  - `if` キーワードにカーソル → `else if` / `else` をハイライト
  - `try` キーワードにカーソル → `catch` をハイライト
  - 関数の戻り値位置にカーソル → 全 `return` / 最終式をハイライト
- **実装難易度:** 中
- **パーサー拡張:** 不要（トークンベースで動作可能）
- **参考プラグイン:** Java, Kotlin, Rust
- **優先度:** **B**

### 5-6. Annotator (同期アノテータ)
- **Extension Point:** `com.intellij.annotator`
- **インターフェース:** `Annotator`
- **概要:** PSI ツリー上のリアルタイム同期アノテーション。`externalAnnotator`（登録済み・非同期）を補完する軽量な同期版。
- **ReScript での活用:**
  - 未解決のモジュール参照の即時エラー表示
  - 不正な属性名 (`@genType` のタイポ) のリアルタイム警告
  - JSX 構造の不整合（開始タグ≠終了タグ）の即時検出
  - `@deprecated` 属性付きの宣言に取り消し線を表示
- **実装難易度:** 中
- **パーサー拡張:** 中
- **参考プラグイン:** Kotlin, Rust, Go
- **優先度:** **B**

---

## カテゴリ 6: Find Usages (3件)

### 6-1. FindUsagesProvider + WordsScanner
- **Extension Point:** `com.intellij.lang.findUsagesProvider`
- **インターフェース:** `FindUsagesProvider`
- **概要:** Find Usages (Cmd+F7) のための言語固有プロバイダー。識別子のスキャン、要素タイプの判定、人間可読な説明の提供。
- **ReScript での活用:**
  - `let` バインディング、`type` 定義、`module` 定義の使用箇所検索
  - `DefaultWordsScanner` を使い、識別子・コメント・文字列リテラルの区別
  - Find Usages パネルでの要素タイプ表示: "function foo", "type user", "module User"
  - JSX コンポーネント名の使用箇所検索
- **実装難易度:** 中
- **パーサー拡張:** 中（`PsiNamedElement` の実装が必要）
- **参考プラグイン:** 全ての言語プラグイン
- **優先度:** **A**

### 6-2. Usage Type Provider
- **Extension Point:** `com.intellij.usageTypeProvider`
- **インターフェース:** `UsageTypeProvider`
- **概要:** Find Usages 結果のカテゴリ分類。
- **ReScript での活用:**
  - 「型注釈として使用」(`let x: User.t`)
  - 「関数呼び出し」(`User.make()`)
  - 「open 文」(`open User`)
  - 「モジュールアクセス」(`User.name`)
  - 「パターンマッチ」(`| User(x) =>`)
  - 「JSX コンポーネント」(`<User name="..." />`)
- **実装難易度:** 低
- **パーサー拡張:** 不要（PSI コンテキストの検査のみ）
- **参考プラグイン:** Java, Kotlin, Go
- **優先度:** **A**

### 6-3. Element Description Provider
- **Extension Point:** `com.intellij.elementDescriptionProvider`
- **インターフェース:** `ElementDescriptionProvider`
- **概要:** リネームダイアログ、Find Usages ヘッダー等での要素名の表示カスタマイズ。
- **ReScript での活用:**
  - リネーム時: "Rename function 'validate' and its usages?"
  - Find Usages: "Usages of type 'User.t'"
  - Safe Delete: "The module 'Router' is referenced in 3 files"
- **実装難易度:** 低
- **パーサー拡張:** 不要
- **参考プラグイン:** 全ての言語プラグイン
- **優先度:** **A**

---

## カテゴリ 7: ドキュメント (3件)

### 7-1. Quick Documentation (DocumentationTarget)
- **Extension Point:** `com.intellij.platform.backend.documentation.psiTargetProvider` (推奨, 2023.1+) / `com.intellij.lang.documentationProvider` (レガシー)
- **インターフェース:** `PsiDocumentationTargetProvider` / `AbstractDocumentationProvider`
- **概要:** F1 / hover で表示される Quick Documentation ポップアップ。
- **ReScript での活用:**
  - LSP hover 情報を HTML フォーマットで整形表示
  - `/** ... */` ドキュメントコメントのレンダリング
  - キーワード (`let`, `switch`, `type`, `module` 等) のビルトインドキュメント
  - `@deprecated` 属性の情報を docs に反映
  - シンタックスハイライト付きの型シグネチャ表示
- **実装難易度:** 中
- **パーサー拡張:** 不要（LSP hover レスポンスの加工がメイン）
- **参考プラグイン:** Kotlin, Rust, Go
- **優先度:** **A**

### 7-2. External Documentation (外部ドキュメント)
- **Extension Point:** `com.intellij.documentationProvider` (同じ EP で `getUrlFor()` メソッド)
- **概要:** Shift+F1 で外部ドキュメントをブラウザで開く。
- **ReScript での活用:**
  - ReScript 標準ライブラリ関数 → `rescript-lang.org/docs/manual/api/` へのリンク
  - `Belt` モジュール → Belt API ドキュメントへのリンク
  - `Js` モジュール → JS バインディング API ドキュメントへのリンク
  - `@module("react")` 等の外部バインディング → npm パッケージドキュメントへのリンク
- **実装難易度:** 低〜中
- **パーサー拡張:** 不要
- **参考プラグイン:** Java, Kotlin, Go
- **優先度:** **B**

### 7-3. Predefined Code Style (事前定義コードスタイル)
- **Extension Point:** `com.intellij.predefinedCodeStyle`
- **インターフェース:** `PredefinedCodeStyle`
- **概要:** 事前定義のコードスタイル設定セット。
- **ReScript での活用:**
  - ReScript 公式フォーマッターに準拠したインデント・スペーシング設定
  - `rescript format` の出力と一致する IDE 内インデント設定の自動適用
- **実装難易度:** 低
- **パーサー拡張:** 不要
- **参考プラグイン:** Kotlin, Scala
- **優先度:** **C**

---

## カテゴリ 8: プロジェクトビュー (4件)

### 8-1. Tree Structure Provider (.resi ファイルネスティング)
- **Extension Point:** `com.intellij.treeStructureProvider`
- **インターフェース:** `TreeStructureProvider`
- **概要:** プロジェクトツリーのノード構造をカスタマイズ。
- **ReScript での活用:**
  - `.resi` ファイルを `.res` ファイルの子要素としてネスト表示
  - コンパイル済み `.js` / `.mjs` を `.res` ファイルの子要素としてネスト表示
  - `lib/bs/` / `lib/ocaml/` ビルドディレクトリの非表示化
  - `.bsc.lock`, `.merlin` 等のビルド中間ファイルの非表示化
- **実装難易度:** 低〜中
- **パーサー拡張:** 不要
- **参考プラグイン:** Kotlin (`.kt` + `.kts` nesting), TypeScript (`.ts` + `.d.ts`)
- **優先度:** **S**

### 8-2. Node Decorator (プロジェクトビューノード装飾)
- **Extension Point:** `com.intellij.projectViewNodeDecorator`
- **インターフェース:** `ProjectViewNodeDecorator`
- **概要:** プロジェクトツリーのノードにアイコンオーバーレイ・テキスト・色を追加。
- **ReScript での活用:**
  - コンパイルエラーのある `.res` ファイルにエラーバッジ
  - `@react.component` を含むファイルに React アイコンオーバーレイ
  - テストファイルにテストアイコンオーバーレイ
  - `.resi` ファイルにインターフェースバッジ
- **実装難易度:** 低
- **パーサー拡張:** 不要
- **参考プラグイン:** Java, Kotlin
- **優先度:** **B**

### 8-3. Test Source Filter (テストソース判定)
- **Extension Point:** テストルート判定用ユーティリティ（`TestSourcesFilter` 等）
- **概要:** プロジェクト内のテストファイル/テストディレクトリの自動判定。
- **ReScript での活用:**
  - `rescript.json` の `sources` → `type: "dev"` エントリからテストディレクトリを検出
  - `__tests__/`, `test/`, `tests/` ディレクトリのテストルートマーキング
  - テストファイルのアイコン区別（緑の盾マーク等）
- **実装難易度:** 中
- **パーサー拡張:** 不要（`rescript.json` のパースのみ）
- **参考プラグイン:** Java (Maven/Gradle), Go
- **優先度:** **B**

### 8-4. Framework Detector (フレームワーク検出)
- **Extension Point:** `com.intellij.framework.detector`
- **インターフェース:** `FrameworkDetector`
- **概要:** プロジェクトを開いた時に ReScript プロジェクトであることを自動検出し通知。
- **ReScript での活用:**
  - `rescript.json` / `bsconfig.json` の存在で ReScript プロジェクトを検出
  - 検出時に「ReScript Framework Detected」通知を表示
  - LSP サーバーのセットアップを自動提案
  - SDK（Node.js）の設定を促す
- **実装難易度:** 低
- **パーサー拡張:** 不要
- **参考プラグイン:** Spring, Django, Rails
- **優先度:** **A**

---

## カテゴリ 9: インデキシング (3件)

### 9-1. File-Based Index (ファイルベースインデックス)
- **Extension Point:** `com.intellij.fileBasedIndex`
- **インターフェース:** `FileBasedIndexExtension<K, V>`
- **概要:** プロジェクト全体の ReScript ファイルから永続的なキー/値インデックスを構築。
- **ReScript での活用:**
  - **モジュール名インデックス:** ファイルパス → モジュール名のマッピング（Go to Class、Find Usages の高速化）
  - **`open` 文インデックス:** ファイル → `open` しているモジュール名のリスト（Import Optimizer の高速化）
  - **`@react.component` インデックス:** React コンポーネントを含むファイルの列挙
  - **`external` バインディングインデックス:** 外部バインディング名 → ファイルのマッピング
- **実装難易度:** 高（インデクサー実装 + シリアライザーが必要）
- **パーサー拡張:** 低（トークンストリームベースのインデクシングが可能）
- **参考プラグイン:** Kotlin, Java, Go
- **優先度:** **B** (他の高度な機能の基盤として将来的に重要)

### 9-2. Stub Index (スタブインデックス)
- **Extension Point:** `com.intellij.stubIndex` + `com.intellij.stubElementTypeHolder`
- **インターフェース:** `StringStubIndexExtension<T>` + `IStubFileElementType`
- **概要:** PSI のスタブ（軽量バイナリ表現）を永続化。ファイルを開かずに宣言情報にアクセス可能。
- **ReScript での活用:**
  - Go to Symbol のインデックスバックエンド（現在は LSP 依存）
  - 全モジュールの `let` / `type` / `module` 宣言のスタブ化
  - プロジェクト全体での高速な宣言検索
  - Find Usages のオフラインバックエンド
- **実装難易度:** 高（パーサーをスタブ対応に拡張する必要あり）
- **パーサー拡張:** 高
- **参考プラグイン:** Kotlin, Rust, Go
- **優先度:** **C** (パーサー拡張のコストが非常に高い)

### 9-3. Index Pattern Builder (インデックスパターンビルダー)
- **Extension Point:** `com.intellij.indexPatternBuilder`
- **インターフェース:** `IndexPatternBuilder`
- **概要:** TODO インデクサーのパターン認識を拡張。
- **ReScript での活用:**
  - `// FIXME(rescript):` パターンの認識
  - `// HACK:` パターンの認識
  - `// NOTE:` パターンの認識
  - ドキュメントコメント `/** @todo ... */` 内の TODO 認識
- **実装難易度:** 低
- **パーサー拡張:** 不要
- **参考プラグイン:** Java, Kotlin
- **優先度:** **C**

---

## カテゴリ 10: テスト・実行 (3件)

### 10-1. REPL / Scratch File (スクラッチファイル)
- **Extension Point:** `com.intellij.scratch.rootType` / `com.intellij.scratch.creationHelper`
- **インターフェース:** `ScratchRootType` / `ScratchFileCreationHelper`
- **概要:** Cmd+Shift+N でスクラッチ ReScript ファイルを作成し、その場でコンパイル＆実行。
- **ReScript での活用:**
  - スクラッチ `.res` ファイルの作成
  - 実行ボタンで `rescript build` → `node output.js` のワンクリック実行
  - 結果のインライン表示（Kotlin Scratch と同様のワークフロー）
  - スクラッチファイルでの LSP 補完・診断サポート
- **実装難易度:** 高（ビルドパイプラインの統合が必要）
- **パーサー拡張:** 不要
- **参考プラグイン:** Kotlin (Scratch), Scala (Worksheet)
- **優先度:** **B**

### 10-2. Package Dependencies View (パッケージ依存ビュー)
- **Extension Point:** カスタムツールウィンドウ (`com.intellij.toolWindow`)
- **概要:** `rescript.json` の `bs-dependencies` / `bs-dev-dependencies` をツリー表示。
- **ReScript での活用:**
  - `rescript.json` の依存パッケージ一覧表示
  - 各パッケージの `rescript.json` メタ情報（バージョン、ソースパス）表示
  - パッケージ名クリックで `node_modules/<pkg>/` へのナビゲーション
  - npm / yarn / pnpm 経由でのパッケージ追加アクション
- **実装難易度:** 中
- **パーサー拡張:** 不要
- **参考プラグイン:** npm (IntelliJ built-in), Cargo (Rust)
- **優先度:** **B**

### 10-3. Run Anything Provider
- **Extension Point:** `com.intellij.runAnything.executionProvider`
- **インターフェース:** `RunAnythingProvider<V>`
- **概要:** Ctrl×2 で ReScript CLI コマンドを実行。
- (2-5. Run Anything と同一。詳細は 2-5 を参照)
- **優先度:** **B**

---

## カテゴリ 11: 言語インジェクション (2件)

### 11-1. IntelliLang Support (Language Injection 拡張)
- **Extension Point:** 既存の `multiHostInjector`（登録済み: `%raw()` 用）の拡張
- **概要:** 追加の言語インジェクションパターン。
- **ReScript での活用:**
  - `%raw("...")` → JavaScript (登録済み)
  - `%re("...")` → Regular Expression (正規表現のシンタックスハイライト・バリデーション)
  - `%external("...")` → JSON / CSS (コンテキストに応じて)
  - `@module("...")` の文字列 → ファイルパスリファレンス
  - テンプレートリテラル `j"Hello ${name}"` → テンプレートエンジン風ハイライト
- **実装難易度:** 低〜中
- **パーサー拡張:** 不要（文字列リテラルの識別は既存 PSI で対応可能）
- **参考プラグイン:** Kotlin (SQL injection), Java (Regex injection)
- **優先度:** **A**

### 11-2. Formatting for Injected Options
- **Extension Point:** `com.intellij.formatting.injectedOptions`
- **概要:** インジェクトされた言語のフォーマットオプション。
- **ReScript での活用:**
  - `%raw("...")` 内の JavaScript のインデントを ReScript のインデントレベルに合わせる
  - インジェクトされた正規表現のフォーマット保護
- **実装難易度:** 低
- **パーサー拡張:** 不要
- **参考プラグイン:** Kotlin
- **優先度:** **C**

---

## カテゴリ 12: VSCode パリティ (7件)

### 12-1. Paste as ReScript JSX
- **概要:** クリップボードの HTML/JSX を ReScript JSX に変換してペースト。
- **VSCode 側の実装:** `rescript-vscode.paste_as_rescript_jsx` コマンド
- **ReScript での活用:**
  - HTML (`<div class="foo">`) → ReScript JSX (`<div className="foo">`) の自動変換
  - イベントハンドラ (`onclick`) → ReScript 形式 (`onClick`) への変換
  - `class` → `className` の自動置換
  - セルフクロージングタグの正規化
  - 実装方法: `copyPastePostProcessor` EP + エディタアクション
- **実装難易度:** 中
- **パーサー拡張:** 不要
- **優先度:** **A**

### 12-2. Restart LSP Command (LSP サーバー再起動)
- **概要:** LSP サーバーをIDEを再起動せずにリスタート。
- **VSCode 側の実装:** `rescript-vscode.restart_language_server` コマンド
- **ReScript での活用:**
  - LSP サーバーがフリーズした際のリカバリー
  - `rescript.json` 変更後のサーバー再起動
  - ツールバー/メニューからのワンクリック再起動
  - 実装方法: IntelliJ LSP API の `LspServerManager` を使用
- **実装難易度:** 低
- **パーサー拡張:** 不要
- **優先度:** **S**

### 12-3. Start Build Prompt (ビルド開始プロンプト)
- **概要:** プロジェクト起動時に ReScript ビルドの開始を提案。
- **VSCode 側の実装:** `rescript.settings.askToStartBuild` 設定 + 自動プロンプト
- **ReScript での活用:**
  - プロジェクト起動時に「ReScript ビルドを開始しますか？」通知
  - バックグラウンドでの `rescript build -w` (ウォッチモード) 起動
  - ビルド状態のリアルタイム表示（既存のステータスバーウィジェットと連携）
  - 実装方法: `postStartupActivity` (既存) の拡張 + `notificationGroup`
- **実装難易度:** 中
- **パーサー拡張:** 不要
- **優先度:** **A**

### 12-4. LSP Code Action 検証 (不足アクションの確認)
- **概要:** VSCode 拡張で利用可能な LSP Code Action が IntelliJ でも動作するか検証。
- **VSCode で確認済みの Code Action:**
  - Add missing switch cases (exhaustive pattern completion)
  - Extract module to file
  - Expand catch-all pattern (`_` → 個別ケース展開)
  - Wrap with Some (型エラーからの自動提案)
- **検証内容:**
  - IntelliJ LSP API が `textDocument/codeAction` レスポンスを正しく処理するか
  - Quick Fix として UI に表示されるか
  - `workspace/applyEdit` が正しく適用されるか
- **実装難易度:** 低（検証のみ。未対応の場合はカスタムアクションハンドラが必要）
- **パーサー拡張:** 不要
- **優先度:** **S** (既に LSP サーバーが提供しているため、プラグイン側の対応は最小限)

### 12-5. 追加 LSP 設定 (Initialization Options)
- **概要:** VSCode 拡張が LSP サーバーに渡しているが IntelliJ プラグインでは未送信の設定。
- **未送信の設定:**
  - `signatureHelp.forConstructorPayloads` — バリアントコンストラクタのペイロードでの Signature Help
  - `cache.projectConfig.enable` — プロジェクト設定キャッシュ
  - `inlayHints.maxLength` — インレイヒントの最大文字数
- **ReScript での活用:**
  - Settings UI (`RescriptConfigurable.kt`) に上記設定を追加
  - `RescriptLspServerDescriptor.kt` の `createInitializationOptions()` に設定値を追加
- **実装難易度:** 低
- **パーサー拡張:** 不要
- **優先度:** **A**

### 12-6. Reanalyze Server Mode
- **概要:** `reanalyze` をサーバーモードで起動し、ファイル変更時に差分分析を行う。
- **現状:** 現在の `RescriptReanalyzeAnnotator.kt` はファイル保存ごとに `reanalyze` プロセスを起動している。
- **ReScript での活用:**
  - `reanalyze` をデーモンプロセスとして起動し、常駐させる
  - ファイル変更の通知を受けて差分分析のみ実行
  - 分析結果のキャッシュと差分更新
  - 大規模プロジェクトでのパフォーマンス改善
- **実装難易度:** 中
- **パーサー拡張:** 不要
- **優先度:** **B**

### 12-7. 追加スニペット
- **概要:** VSCode 拡張に含まれるスニペットで IntelliJ の Live Templates に未反映のもの。
- **確認が必要なスニペット:**
  - `@react.component` フルテンプレート
  - `useEffect` / `useState` / `useReducer` ボイラープレート
  - `external` バインディングテンプレート
  - `%%raw` / `%%re` テンプレート
  - モジュールファンクターテンプレート
  - テストケーステンプレート（jest/vitest）
- **実装難易度:** 低
- **パーサー拡張:** 不要
- **優先度:** **A**

---

## カテゴリ 13: その他 (7件)

### 13-1. Bundled Dictionary (バンドル辞書)
- **Extension Point:** `com.intellij.spellchecker.bundledDictionaryProvider`
- **インターフェース:** `BundledDictionaryProvider`
- **概要:** ReScript 固有の用語をスペルチェッカーの辞書に登録。
- **ReScript での活用:**
  - ReScript キーワード・標準ライブラリ名を辞書に追加 (`rescript`, `genType`, `uncurry`, `deriving`, `unboxed` 等)
  - `Belt`, `Js`, `Promise`, `JSON`, `Dict`, `RegExp` 等の標準モジュール名
  - `bs-dependencies`, `bs-dev-dependencies` 等の設定キー
  - npm パッケージ名の一般的なパターン
- **実装難易度:** 低
- **パーサー拡張:** 不要
- **優先度:** **S**

### 13-2. Color Preview (カラープレビュー)
- **Extension Point:** `com.intellij.colorProvider`
- **インターフェース:** `ElementColorProvider`
- **概要:** 色リテラルのガター表示とインラインカラーピッカー。
- **ReScript での活用:**
  - `"#ff0000"` 等の CSS カラー文字列にガターアイコンでプレビュー
  - `rgb(255, 0, 0)` パターンの検出
  - CSS-in-ReScript ライブラリ (emotion, bs-css) 使用時のカラー表示
  - カラーピッカーによる色の編集
- **実装難易度:** 低
- **パーサー拡張:** 不要（文字列リテラル内の正規表現マッチ）
- **参考プラグイン:** CSS, Kotlin (compose), Flutter
- **優先度:** **B**

### 13-3. Reader Mode (リーダーモード)
- **Extension Point:** `com.intellij.readerModeMatcher` / `com.intellij.readerModeProvider`
- **インターフェース:** `ReaderModeMatcher` / `ReaderModeProvider`
- **概要:** 読み取り専用ファイルに最適化されたビュー（インデントガイド強調、折りたたみ自動適用等）。
- **ReScript での活用:**
  - `node_modules/` 内の `.res` / `.resi` ファイルをリーダーモードで表示
  - `.resi` インターフェースファイルの読み取り専用表示（LSP が create する場合）
  - コンパイル済み `.js` ファイルの読み取り専用表示
- **実装難易度:** 低
- **パーサー拡張:** 不要
- **参考プラグイン:** Java, Kotlin
- **優先度:** **C**

### 13-4. VCS Code Vision (バージョン管理統合)
- **Extension Point:** `com.intellij.vcs.codeVisionLanguageContext`
- **概要:** Code Vision に VCS 情報（最終変更者、変更日時）を言語構造に紐付けて表示。
- **ReScript での活用:**
  - 関数定義に「最終変更: 3日前 by @user」を表示
  - モジュール定義に変更頻度の表示
  - 既存の Code Lens (型情報) と VCS 情報の共存
- **実装難易度:** 低〜中
- **パーサー拡張:** 不要（既存の PSI 構造で VCS 情報を紐付け）
- **参考プラグイン:** Java, Kotlin
- **優先度:** **C**

### 13-5. Code Rearranger (コード並び替え)
- **Extension Point:** `com.intellij.rearranger.ui.ArrangementPanel` (Framework)
- **概要:** Cmd+Opt+L でのフォーマット時にコード要素を自動並び替え。
- **ReScript での活用:**
  - `open` 文をファイル先頭にグループ化
  - `type` 定義を `let` バインディングの前に配置
  - `@react.component` を含む `let` をファイル末尾に配置
  - アルファベット順ソートのオプション
- **実装難易度:** 高（Arrangement API の理解が必要）
- **パーサー拡張:** 中
- **参考プラグイン:** Java, Kotlin
- **優先度:** **C**

### 13-6. Postfix Templates 追加
- **Extension Point:** `com.intellij.codeInsight.template.postfixTemplateProvider` (登録済み)
- **概要:** 既存の Postfix Completion (`RescriptPostfixTemplateProvider`) へのテンプレート追加。
- **追加候補:**
  - `.some` → `Some(expr)` (Wrap with Some)
  - `.ok` → `Ok(expr)` (Wrap with Ok)
  - `.error` → `Error(expr)` (Wrap with Error)
  - `.promise` → `expr->Promise.resolve`
  - `.map` → `expr->Array.map(item => |)`
  - `.filter` → `expr->Array.filter(item => |)`
  - `.reduce` → `expr->Array.reduce((acc, item) => |, initialValue)`
  - `.await` → `await expr`
  - `.try` → `try { expr } catch { | _ => | }`
  - `.if` → `if expr { | }`
  - `.let` → `let | = expr`
  - `.assert` → `assert(expr)`
  - `.ignore` → `expr->ignore`
- **実装難易度:** 低
- **パーサー拡張:** 不要
- **参考プラグイン:** Kotlin, Java
- **優先度:** **A**

### 13-7. Dependency Diagram (依存関係図)
- **Extension Point:** カスタムツールウィンドウ + UML/グラフ描画
- **概要:** モジュール間の依存関係をグラフィカルに表示。
- **ReScript での活用:**
  - `open` 文に基づくモジュール依存グラフ
  - `rescript.json` の `bs-dependencies` に基づくパッケージ依存グラフ
  - 循環依存の視覚的検出
  - 既存の `RescriptModuleHierarchyProvider` のグラフィカル版
- **実装難易度:** 高
- **パーサー拡張:** 不要（既存の依存分析ロジックを流用可能）
- **参考プラグイン:** Java (Module Dependencies), Gradle (Dependencies diagram)
- **優先度:** **C**

---

## 優先度別サマリー

### S (最優先) — 8件

| # | 機能 | カテゴリ | 難易度 |
|---|------|---------|--------|
| 1-1 | Extend/Shrink Selection | 編集操作 | 低 |
| 2-3 | Go to Test / Create Test | ナビゲーション | 低 |
| 3-1 | Completion Confidence | コード補完 | 低 |
| 5-3 | Problem Highlight Filter | コード分析 | 低 |
| 8-1 | Tree Structure Provider (.resi nesting) | プロジェクトビュー | 低〜中 |
| 12-2 | Restart LSP Command | VSCode パリティ | 低 |
| 12-4 | LSP Code Action 検証 | VSCode パリティ | 低 |
| 13-1 | Bundled Dictionary | その他 | 低 |

### A (高優先) — 22件

| # | 機能 | カテゴリ | 難易度 |
|---|------|---------|--------|
| 1-2 | Unwrap/Remove | 編集操作 | 中 |
| 1-5 | Typed Handler | 編集操作 | 低〜中 |
| 1-7 | Enter Handler | 編集操作 | 中 |
| 1-10 | Copy/Paste Processor (JSX 変換) | 編集操作 | 中 |
| 2-1 | Context Info (Line Marker) | ナビゲーション | 低〜中 |
| 2-6 | Goto Super (Type Definition) | ナビゲーション | 低 |
| 2-7 | Go to Implementation | ナビゲーション | 低〜中 |
| 3-2 | Lookup Char Filter | コード補完 | 低 |
| 3-4 | Live Template Context | コード補完 | 低 |
| 4-8 | Refactoring Support Provider | リファクタリング | 低 |
| 5-4 | Inspection Suppressor | コード分析 | 低 |
| 6-1 | FindUsagesProvider + WordsScanner | Find Usages | 中 |
| 6-2 | Usage Type Provider | Find Usages | 低 |
| 6-3 | Element Description Provider | Find Usages | 低 |
| 7-1 | Quick Documentation | ドキュメント | 中 |
| 8-4 | Framework Detector | プロジェクトビュー | 低 |
| 11-1 | IntelliLang Support (追加インジェクション) | 言語インジェクション | 低〜中 |
| 12-1 | Paste as ReScript JSX | VSCode パリティ | 中 |
| 12-3 | Start Build Prompt | VSCode パリティ | 中 |
| 12-5 | 追加 LSP 設定 | VSCode パリティ | 低 |
| 12-7 | 追加スニペット | VSCode パリティ | 低 |
| 13-6 | Postfix Templates 追加 | その他 | 低 |

### B (中優先) — 23件

| # | 機能 | カテゴリ | 難易度 |
|---|------|---------|--------|
| 1-3 | Move Element Left/Right | 編集操作 | 中 |
| 1-4 | Join Lines | 編集操作 | 低 |
| 1-6 | Backspace Handler | 編集操作 | 低 |
| 1-8 | Code Block Provider | 編集操作 | 中 |
| 2-4 | Search Everywhere | ナビゲーション | 高 |
| 2-5 | Run Anything | ナビゲーション | 中 |
| 2-8 | Navigation Bar | ナビゲーション | 中 |
| 2-10 | Go to Declaration Handler | ナビゲーション | 中 |
| 3-3 | Completion Weigher | コード補完 | 中 |
| 3-5 | Live Template Macros | コード補完 | 低 |
| 4-6 | Name Suggestion | リファクタリング | 低 |
| 5-1 | Expression Type Info | コード分析 | 中 |
| 5-2 | Custom Inspections (追加) | コード分析 | 中 |
| 5-5 | Highlight Usages | コード分析 | 中 |
| 5-6 | Annotator | コード分析 | 中 |
| 7-2 | External Documentation | ドキュメント | 低〜中 |
| 8-2 | Node Decorator | プロジェクトビュー | 低 |
| 8-3 | Test Source Filter | プロジェクトビュー | 中 |
| 9-1 | File-Based Index | インデキシング | 高 |
| 10-1 | REPL / Scratch File | テスト・実行 | 高 |
| 10-2 | Package Dependencies View | テスト・実行 | 中 |
| 12-6 | Reanalyze Server Mode | VSCode パリティ | 中 |
| 13-2 | Color Preview | その他 | 低 |

### C (低優先) — 18件

| # | 機能 | カテゴリ | 難易度 |
|---|------|---------|--------|
| 1-9 | Line Wrap Strategy | 編集操作 | 低 |
| 1-11 | Strip Trailing Spaces Filter | 編集操作 | 低 |
| 2-2 | Call Hierarchy | ナビゲーション | 高 |
| 2-9 | File Include Provider | ナビゲーション | 中 |
| 4-1 | Extract Variable | リファクタリング | 高 |
| 4-2 | Extract Function | リファクタリング | 高 |
| 4-3 | Inline Variable/Function | リファクタリング | 高 |
| 4-4 | Safe Delete | リファクタリング | 高 |
| 4-5 | Change Signature | リファクタリング | 高 |
| 4-7 | Suggested Refactoring | リファクタリング | 高 |
| 7-3 | Predefined Code Style | ドキュメント | 低 |
| 9-2 | Stub Index | インデキシング | 高 |
| 9-3 | Index Pattern Builder | インデキシング | 低 |
| 11-2 | Formatting for Injected Options | 言語インジェクション | 低 |
| 13-3 | Reader Mode | その他 | 低 |
| 13-4 | VCS Code Vision | その他 | 低〜中 |
| 13-5 | Code Rearranger | その他 | 高 |
| 13-7 | Dependency Diagram | その他 | 高 |

---

## 実装ロードマップ（推奨）

### Phase 1: Quick Wins (S 優先度)
1. 12-2: Restart LSP Command
2. 13-1: Bundled Dictionary
3. 3-1: Completion Confidence
4. 5-3: Problem Highlight Filter
5. 1-1: Extend/Shrink Selection
6. 2-3: Go to Test / Create Test
7. 8-1: Tree Structure Provider
8. 12-4: LSP Code Action 検証

### Phase 2: High Impact (A 優先度 — 低難易度)
1. 12-5: 追加 LSP 設定
2. 12-7: 追加スニペット
3. 13-6: Postfix Templates 追加
4. 3-2: Lookup Char Filter
5. 3-4: Live Template Context
6. 4-8: Refactoring Support Provider
7. 5-4: Inspection Suppressor
8. 6-2: Usage Type Provider
9. 6-3: Element Description Provider
10. 8-4: Framework Detector

### Phase 3: Core Features (A 優先度 — 中難易度)
1. 6-1: FindUsagesProvider + WordsScanner
2. 7-1: Quick Documentation
3. 1-2: Unwrap/Remove
4. 1-5: Typed Handler
5. 1-7: Enter Handler
6. 2-1: Context Info (Line Marker)
7. 11-1: IntelliLang Support
8. 12-1: Paste as ReScript JSX
9. 12-3: Start Build Prompt

### Phase 4: Enhanced Experience (B 優先度)
- 編集操作の充実 (1-3, 1-4, 1-6, 1-8)
- ナビゲーション強化 (2-4, 2-5, 2-8, 2-10)
- コード分析の深化 (5-1, 5-2, 5-5, 5-6)
- プロジェクトビュー改善 (8-2, 8-3)

### Phase 5: Advanced Features (C 優先度)
- パーサー拡張を前提とするリファクタリング機能群
- スタブインデックスの構築
- 依存関係ビジュアライゼーション

---

## 調査メモ

### パーサー拡張の影響範囲

多くの高度な機能（Extract Variable, Stub Index, Call Hierarchy 等）は**フル PSI パーサー**を前提としている。現在の軽量パーサー (`RescriptParser.kt`) はトップレベル宣言と JSX 構造のみを認識するため、これらの機能の実装にはパーサーの大幅な拡張が必要。

**パーサー拡張が不要な機能** (全体の約 70%) は、既存のトークンベース PSI + LSP の組み合わせで実装可能。

### LSP との機能重複

IntelliJ Platform の多くの Extension Point は LSP が提供する機能と重複する:
- `lang.findUsagesProvider` ↔ LSP `textDocument/references`
- `lang.documentationProvider` ↔ LSP `textDocument/hover`
- `codeInsight.parameterInfo` ↔ LSP `textDocument/signatureHelp`
- `expressionTypeProvider` ↔ LSP hover (型情報)

**推奨戦略:** LSP をプライマリ、PSI ベースを**フォールバック/補完**として実装。LSP サーバーが利用不可能な場合のグレースフルデグラデーションを提供する。

### 参考にした他プラグイン

- **Rust (intellij-rust):** FindUsagesProvider, ExtendWordSelection, StructureView, StubIndex, FormattingModel
- **Go (GoLand built-in):** TestFinder, RunAnything, CallHierarchy, FileBasedIndex
- **Kotlin (built-in):** SuggestedRefactoring, LiveTemplateContext, CopyPastePostProcessor, JoinLinesHandler
- **TypeScript (built-in):** TypedHandler, DocumentationProvider, ColorProvider, TreeStructureProvider
- **Scala (intellij-scala):** ScratchFile, WorksheetEvaluation, ImportOptimizer
- **Elixir (intellij-elixir):** Lightweight parser + LSP hybrid approach (similar architecture)
