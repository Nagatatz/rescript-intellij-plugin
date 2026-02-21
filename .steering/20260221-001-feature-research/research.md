# 他プラグイン機能調査 — ReScript IntelliJ Plugin 機能候補一覧

## 調査概要

ReScript IntelliJ プラグインに追加可能な機能を、以下のソースから網羅的に調査した。

**調査対象:**
- JetBrains 言語プラグイン: Rust/RustRover, GoLand, Scala, Elm, Gleam, Haskell, OCaml/ReasonML, Dart/Flutter, Elixir, Zig, Crystal, Nim, Prisma
- フロントエンドプラグイン: Svelte, Vue.js, Astro, Tailwind CSS
- IntelliJ Platform Plugin SDK ドキュメント（Extension Point 一覧）
- ReScript VSCode 拡張との差分分析

**前提条件:**
- 現在のパーサーは**軽量パーサー**（トップレベル宣言 + JSX のみ。式レベル AST なし）
- LSP 統合は IntelliJ Platform の LSP API 経由で `@rescript/language-server` を使用
- 既存機能 70+ は除外済み（plugin.xml + CLAUDE.md と照合）

**凡例:**
- 難易度: `低` / `中` / `高` / `非常に高`
- パーサー依存: `★` パーサー変更不要 / `▲` トークンレベル工夫で可能 / `●` パーサー拡張 or LSP 対応必要

---

## カテゴリ 1: 編集操作

日常的なコード編集を効率化する機能群。使用頻度が高く、開発者体験への影響が大きい。

### 1-1. Extend/Shrink Word Selection

| 項目 | 内容 |
|------|------|
| Extension Point | `com.intellij.extendWordSelectionHandler` |
| 概要 | Ctrl+W / Ctrl+Shift+W で言語構造に沿った選択拡大/縮小 |
| ReScript での活用 | パイプチェーン(`->`)の引数 → 式全体 → let束縛 → ブロック → モジュール と段階的に選択範囲を拡大。パターンマッチのアーム → switch式全体、JSX属性 → JSX要素全体など |
| 難易度 | 中 `▲` |
| 参考プラグイン | Java, Kotlin, Go, Scala, Rust, Elm |
| 実装方針 | トークンのバランス解析（括弧・波括弧・パイプ）で段階的に実装。完全な式AST不要。`ExtendWordSelectionHandler` を複数登録し、文字列リテラル → 括弧内 → ブロック → 宣言の順で拡張 |

### 1-2. Unwrap/Remove

| 項目 | 内容 |
|------|------|
| Extension Point | `com.intellij.codeInsight.unwrapDescriptor` |
| 概要 | Ctrl+Shift+Delete で囲む構造を除去 |
| ReScript での活用 | `Some(expr)` → `expr`、`Ok(expr)` → `expr`、`Error(expr)` → `expr`、`if (cond) { body }` → `body`、`switch expr { \| _ => body }` → `body`、`try { body } catch { ... }` → `body`、`{ body }` → `body` |
| 難易度 | 低〜中 `★` |
| 参考プラグイン | Java, Kotlin, JavaScript |
| 実装方針 | テキストパターンマッチで実装。既存の `RescriptSurroundDescriptor` の逆操作。`UnwrapDescriptor` で各パターンを `UnwrapAction` として登録 |

### 1-3. Move Element Left/Right

| 項目 | 内容 |
|------|------|
| Extension Point | `com.intellij.moveLeftRightHandler` |
| 概要 | Alt+Shift+Left/Right でカンマ区切り要素の位置交換 |
| ReScript での活用 | 関数引数、タプル要素、配列リテラル要素、レコードフィールド、variant コンストラクタ引数の並び替え |
| 難易度 | 中〜高 `▲` |
| 参考プラグイン | Java, Kotlin, Go |
| 実装方針 | カーソル位置から最寄りの括弧/カンマをトークンスキャンで特定し、カンマ区切りの要素を交換。括弧のネストバランスを考慮 |

### 1-4. Join Lines (Smart)

| 項目 | 内容 |
|------|------|
| Extension Point | `com.intellij.joinLinesHandler` |
| 概要 | Ctrl+Shift+J で言語構造を考慮した行結合 |
| ReScript での活用 | `let x =` + 次行の値を1行に結合、パイプチェーンの結合、`if` + `else` の結合 |
| 難易度 | 低〜中 `★` |
| 参考プラグイン | Java, Kotlin |
| 実装方針 | 行末/行頭のトークンパターンで結合方法を判断。`=` で終わる行 + 値の行 → 空白で結合、`->` で終わる行 → パイプチェーン結合 |

### 1-5. Typed Handler Delegate

| 項目 | 内容 |
|------|------|
| Extension Point | `com.intellij.typedHandler` |
| 概要 | 特定文字入力時のカスタム動作 |
| ReScript での活用 | (a) `>` 入力時に JSX 閉じタグ `</tagName>` を自動挿入、(b) `\|` 入力で switch パターンアーム補助（`\| ` + カーソル + ` =>`）、(c) `>` 入力で `-` の後なら `->` と認識してパイプ補完 |
| 難易度 | 中 `★` |
| 参考プラグイン | Svelte (auto-close tags), Dart, Vue, JavaScript |
| 実装方針 | `TypedHandlerDelegate` を実装。入力文字と直前のコンテキスト（トークン）をチェックして条件分岐 |

### 1-6. Backspace Handler Delegate

| 項目 | 内容 |
|------|------|
| Extension Point | `com.intellij.backspaceHandlerDelegate` |
| 概要 | Backspace キーのカスタム動作 |
| ReScript での活用 | JSX タグのペア削除、テンプレートリテラルの `${` 削除時に `}` も削除 |
| 難易度 | 低 `★` |
| 参考プラグイン | Dart, Svelte |
| 実装方針 | `BackspaceHandlerDelegate` でカーソル前後のトークンを確認し、ペア削除を判断 |

### 1-7. Enter Handler Delegate

| 項目 | 内容 |
|------|------|
| Extension Point | `com.intellij.enterHandlerDelegate` |
| 概要 | Enter キーのカスタム動作 |
| ReScript での活用 | (a) `/** */` ドキュメントコメント内で Enter → `*` を自動挿入して継続、(b) `=>` の後で Enter → 自動インデント、(c) switch 内で Enter → `\| ` を自動挿入 |
| 難易度 | 低〜中 `★` |
| 参考プラグイン | Dart, Kotlin, Svelte |
| 実装方針 | `EnterHandlerDelegate` で行末トークンをチェック。ドキュメントコメント内なら `* ` を挿入 |

### 1-8. Code Block Support Handler

| 項目 | 内容 |
|------|------|
| Extension Point | `com.intellij.codeBlockSupportHandler` |
| 概要 | マルチブロック式（if/else, switch/case, try/catch）の選択と操作 |
| ReScript での活用 | `switch`/`\|` アーム間のナビゲーション（Ctrl+Shift+M）、`if`/`else if`/`else` ブロック間の移動、`try`/`catch` 間の移動 |
| 難易度 | 中 `▲` |
| 参考プラグイン | Dart, Kotlin |
| 実装方針 | キーワードトークンの対応関係をトークンスキャンで特定 |

### 1-9. Copy/Paste Pre-Processor

| 項目 | 内容 |
|------|------|
| Extension Point | `com.intellij.copyPastePreProcessor` |
| 概要 | コピー/ペースト時のテキスト変換 |
| ReScript での活用 | (a) 文字列リテラル内にペースト → 特殊文字を自動エスケープ、(b) JavaScript コードペースト → ReScript 構文に自動変換の候補表示 |
| 難易度 | 中 `★` |
| 参考プラグイン | Dart, Kotlin |
| 実装方針 | `CopyPastePreProcessor` でペースト先のコンテキスト（文字列内か否か）を判定 |

### 1-10. Split/Join List Constructs

| 項目 | 内容 |
|------|------|
| Extension Point | `com.intellij.listSplitJoinContext` |
| 概要 | 関数引数・配列・レコードフィールドを1行⇔複数行に変換 |
| ReScript での活用 | `let f = (a, b, c) =>` ⇔ 各引数を改行、`[1, 2, 3]` ⇔ 各要素を改行、`{name: "foo", age: 25}` ⇔ 各フィールドを改行 |
| 難易度 | 中 `▲` |
| 参考プラグイン | Java, Kotlin |
| 実装方針 | カンマ区切りトークンの検出と、括弧のバランス解析で実装 |

### 1-11. Strip Trailing Spaces Filter

| 項目 | 内容 |
|------|------|
| Extension Point | `com.intellij.stripTrailingSpacesFilterFactory` |
| 概要 | 特定領域の末尾空白保持 |
| ReScript での活用 | バッククォートテンプレートリテラル、`%raw()` JavaScript ブロック内の空白を保持 |
| 難易度 | 低 `★` |
| 参考プラグイン | 各種 |
| 実装方針 | トークンタイプを確認し、テンプレートリテラル・raw ブロック内の行は空白除去をスキップ |

---

## カテゴリ 2: ナビゲーション

コード内の移動・探索を効率化する機能群。大規模プロジェクトでの生産性に直結。

### 2-1. Context Info (Declaration Range Handler)

| 項目 | 内容 |
|------|------|
| Extension Point | `com.intellij.declarationRangeHandler` |
| 概要 | 長い関数内でスクロール時、囲む宣言のヘッダーをエディタ上部にスティッキー表示 |
| ReScript での活用 | 長い `let` 束縛や `module` ブロック内で、囲む宣言名とシグネチャが常に見える |
| 難易度 | 低 `★` |
| 参考プラグイン | Java, Kotlin, Scala |
| 実装方針 | 既存の PSI 宣言ノード（LET_DECLARATION, MODULE_DECLARATION 等）の TextRange を返す |

### 2-2. Call Hierarchy

| 項目 | 内容 |
|------|------|
| Extension Point | `com.intellij.callHierarchyProvider` |
| 概要 | 関数の呼び出し元/呼び出し先をツリー表示（Ctrl+Alt+H） |
| ReScript での活用 | 関数の影響範囲分析、依存関係の追跡 |
| 難易度 | 高 `●` |
| 参考プラグイン | Java, Go, Scala, Rust |
| 実装方針 | LSP 3.16+ の `textDocument/prepareCallHierarchy` / `callHierarchy/incomingCalls` / `callHierarchy/outgoingCalls` を活用（rescript-language-server の対応状況要確認） |

### 2-3. Go to Test / Create Test

| 項目 | 内容 |
|------|------|
| Extension Point | `com.intellij.testCreator` |
| 概要 | Ctrl+Shift+T で実装⇔テストファイル間ナビゲーション。テスト未存在なら自動生成 |
| ReScript での活用 | `Foo.res` ⇔ `Foo_test.res` / `__tests__/Foo_test.res` / `Foo.test.res` 間の移動。テストファイル自動生成時にテストフレームワーク（jest/vitest）のボイラープレートを挿入 |
| 難易度 | 低 `★` |
| 参考プラグイン | Java, Kotlin, Go, Scala |
| 実装方針 | 既存の `RescriptSwitchFileAction` と同パターン。ファイル名パターンマッチでテストファイルを探索。`RescriptTestFrameworkDetector` を活用してテストテンプレートを決定 |

### 2-4. Search Everywhere Contributor

| 項目 | 内容 |
|------|------|
| Extension Point | `com.intellij.searchEverywhereContributor` |
| 概要 | Shift+Shift の Search Everywhere にカスタムタブ/結果を追加 |
| ReScript での活用 | 「ReScript Modules」タブでモジュール名検索、型名検索、外部定義検索等 |
| 難易度 | 中 `★` |
| 参考プラグイン | Dart, Flutter, Tailwind CSS |
| 実装方針 | `SearchEverywhereContributor` を実装し、.res/.resi ファイルのモジュール名をインデックスから返す |

### 2-5. Run Anything Provider

| 項目 | 内容 |
|------|------|
| Extension Point | `com.intellij.runAnythingProvider` |
| 概要 | Ctrl+Ctrl の Run Anything にコマンドを追加 |
| ReScript での活用 | `rescript build`, `rescript clean`, `rescript format <file>` をダイアログから直接実行 |
| 難易度 | 低〜中 `★` |
| 参考プラグイン | npm, Gradle |
| 実装方針 | `RunAnythingProvider` を実装し、`rescript` プレフィックスのコマンドをハンドル |

### 2-6. Goto Super (実装 → インターフェース)

| 項目 | 内容 |
|------|------|
| Extension Point | `com.intellij.codeInsight.gotoSuper` |
| 概要 | Ctrl+U で親宣言/インターフェースにジャンプ |
| ReScript での活用 | `.res` の関数実装 → `.resi` の対応するインターフェース宣言にジャンプ。モジュール → module type にジャンプ |
| 難易度 | 中 `★` |
| 参考プラグイン | Dart, Kotlin |
| 実装方針 | 既存の `RescriptSwitchFileAction` を活用し、同名の宣言位置をテキスト検索で特定 |

### 2-7. Go to Implementation

| 項目 | 内容 |
|------|------|
| Extension Point | `com.intellij.definitionsScopedSearch` |
| 概要 | Ctrl+Alt+B でインターフェース宣言から実装にジャンプ |
| ReScript での活用 | `.resi` の宣言 → `.res` の対応する実装にジャンプ。module type → 実装モジュール一覧 |
| 難易度 | 中 `★` |
| 参考プラグイン | Dart, Kotlin |
| 実装方針 | Goto Super の逆方向。LSP の `textDocument/implementation` も活用可能 |

### 2-8. File Include Provider

| 項目 | 内容 |
|------|------|
| Extension Point | `com.intellij.include.provider` |
| 概要 | `open` 文のモジュール名を Ctrl+Click でファイルにナビゲート |
| ReScript での活用 | `open Belt.Array` → Belt.Array のソースファイルにジャンプ |
| 難易度 | 中 `●` |
| 参考プラグイン | PHP, Python |
| 実装方針 | `open` 文のモジュール名からファイルパスを解決。LSP の定義ジャンプで代替可能な部分もあり |

### 2-9. Navigation Bar Model Extension

| 項目 | 内容 |
|------|------|
| Extension Point | `com.intellij.navbar.item.provider` |
| 概要 | ナビゲーションバーに言語構造を表示 |
| ReScript での活用 | `src > MyModule > myFunction` のようなパスをナビゲーションバーに表示 |
| 難易度 | 低〜中 `★` |
| 参考プラグイン | Dart, Kotlin |
| 実装方針 | 既存の PSI 宣言ノードから構造情報を抽出して表示 |

### 2-10. Expression Type Info

| 項目 | 内容 |
|------|------|
| Extension Point | `com.intellij.codeInsight.typeInfo` / `ExpressionTypeProvider` |
| 概要 | Ctrl+Shift+P でカーソル位置の式の型をポップアップ表示 |
| ReScript での活用 | 型推論が強力な ReScript では非常に有用。任意の式にカーソルを置いて型を確認 |
| 難易度 | 中 `●` |
| 参考プラグイン | Dart, Kotlin, Rust |
| 実装方針 | LSP の `textDocument/hover` レスポンスから型情報を抽出して表示 |

---

## カテゴリ 3: コード補完

補完の質と使い勝手を向上させる機能群。

### 3-1. Completion Confidence

| 項目 | 内容 |
|------|------|
| Extension Point | `com.intellij.completion.confidence` |
| 概要 | 自動補完ポップアップの表示制御 |
| ReScript での活用 | コメント内、文字列内、`%raw()` ブロック内での不要なポップアップを抑制 |
| 難易度 | 低 `★` |
| 参考プラグイン | Dart, Kotlin, Rust |
| 実装方針 | `CompletionConfidence` でカーソル位置のトークンタイプを確認し、不適切な位置では `ThreeState.NO` を返す |

### 3-2. Lookup Char Filter

| 項目 | 内容 |
|------|------|
| Extension Point | `com.intellij.lookup.charFilter` |
| 概要 | 補完ポップアップ表示中の文字入力制御 |
| ReScript での活用 | `.` → 補完確定してチェーン（パイプ補完）、`~` → ラベル付き引数の継続、`(` → 確定して括弧挿入 |
| 難易度 | 低 `★` |
| 参考プラグイン | Dart, Kotlin |
| 実装方針 | `CharFilter` で文字ごとの accept/continue/cancel を判定 |

### 3-3. Completion Weigher

| 項目 | 内容 |
|------|------|
| Extension Point | `com.intellij.weigher` (key: `completion`) |
| 概要 | 補完候補の優先順位付け |
| ReScript での活用 | パイプ互換関数を上位に、最近使用したモジュールを優先、型が一致する候補をブースト |
| 難易度 | 中 `●` |
| 参考プラグイン | Dart, Kotlin, Rust |
| 実装方針 | `CompletionWeigher` で各候補にスコアを付与。LSP 補完結果の `sortText` も活用 |

### 3-4. Live Template Context

| 項目 | 内容 |
|------|------|
| Extension Point | `com.intellij.liveTemplateContext` |
| 概要 | Live Template が有効になるコンテキストの定義 |
| ReScript での活用 | 「コメント内では無効」「文字列内では無効」「トップレベルのみ」「式内のみ」等のコンテキスト制御 |
| 難易度 | 低 `★` |
| 参考プラグイン | Dart, Kotlin |
| 実装方針 | `TemplateContextType` のサブクラスでトークンコンテキストを判定 |

### 3-5. Live Template Macros

| 項目 | 内容 |
|------|------|
| Extension Point | `com.intellij.liveTemplateMacro` |
| 概要 | Live Template 変数で使えるカスタム関数 |
| ReScript での活用 | `rescriptModuleName()` — 現在のモジュール名（ファイル名から）、`rescriptComponentName()` — React コンポーネント名 |
| 難易度 | 低 `★` |
| 参考プラグイン | Dart, Kotlin |
| 実装方針 | `Macro` サブクラスでファイル名からモジュール名を導出 |

---

## カテゴリ 4: リファクタリング

コード構造を安全に変更するための機能群。高度な機能はパーサー拡張が前提。

### 4-1. Extract Variable

| 項目 | 内容 |
|------|------|
| Extension Point | `com.intellij.refactoring.introduceHandler` |
| 概要 | Ctrl+Alt+V で選択式を `let` 束縛に抽出 |
| ReScript での活用 | 複雑な式を名前付き変数に分離。`foo->bar->baz` の中間結果を `let intermediate = foo->bar` に抽出 |
| 難易度 | 高 `●` |
| 参考プラグイン | Java, Kotlin, Go, Rust |
| 実装方針 | ユーザー選択範囲ベースなら中程度の難易度。選択テキストを `let` 束縛に変換し、元の位置を変数名で置換。名前候補は `NameSuggestionProvider` で提供 |

### 4-2. Extract Function

| 項目 | 内容 |
|------|------|
| Extension Point | `com.intellij.lang.refactoringSupport` |
| 概要 | Ctrl+Alt+M で選択コードブロックを新関数に抽出 |
| ReScript での活用 | コードブロックを `let extractedFn = (...) => { ... }` として抽出 |
| 難易度 | 非常に高 `●` |
| 参考プラグイン | Java, Kotlin, Go, Scala |
| 実装方針 | 式解析 + 変数スコープ分析が必要。自由変数を引数として渡すロジックが複雑。LSP にこの機能があればそれを活用 |

### 4-3. Inline Variable/Function

| 項目 | 内容 |
|------|------|
| Extension Point | `com.intellij.lang.refactoringSupport` |
| 概要 | Ctrl+Alt+N で `let` 束縛を使用箇所にインライン展開 |
| ReScript での活用 | `let x = expr` を全使用箇所で `expr` に置換 |
| 難易度 | 高 `●` |
| 参考プラグイン | Java, Kotlin, Go, Scala |
| 実装方針 | LSP の Find References で使用箇所を特定し、テキスト置換。括弧の追加が必要なケースの判定が課題 |

### 4-4. Safe Delete

| 項目 | 内容 |
|------|------|
| Extension Point | `com.intellij.refactoring.safeDeleteProcessor` |
| 概要 | 削除前に使用箇所を確認し、コンフリクトをプレビュー表示 |
| ReScript での活用 | 関数・型・モジュールの安全な削除。使用箇所がある場合は警告 |
| 難易度 | 中〜高 `●` |
| 参考プラグイン | Java, Kotlin, Go |
| 実装方針 | LSP の `textDocument/references` で使用箇所をチェック。0件なら即削除、存在すれば確認ダイアログ表示 |

### 4-5. Change Signature

| 項目 | 内容 |
|------|------|
| Extension Point | `com.intellij.refactoring.changeSignatureHandler` |
| 概要 | 関数パラメータの追加/削除/並び替えを全呼び出し箇所に伝播 |
| ReScript での活用 | ラベル付き引数の追加・削除・リネームとその影響の自動適用 |
| 難易度 | 非常に高 `●` |
| 参考プラグイン | Java, Kotlin, Go, Scala |
| 実装方針 | 関数シグネチャの完全な解析が必要。パーサー大幅拡張が前提 |

### 4-6. Name Suggestion Provider

| 項目 | 内容 |
|------|------|
| Extension Point | `com.intellij.nameSuggestionProvider` |
| 概要 | リネーム/抽出時の名前候補生成 |
| ReScript での活用 | 型ベースの名前候補（`User.t` → `user`、`array<Item.t>` → `items`）、式ベースの候補（`getUser()` → `user`） |
| 難易度 | 中 `●` |
| 参考プラグイン | Dart, Kotlin |
| 実装方針 | LSP のホバー情報から型を取得し、型名からcamelCase名を導出 |

### 4-7. Suggested Refactoring Support

| 項目 | 内容 |
|------|------|
| Extension Point | `com.intellij.suggestedRefactoringSupport` |
| 概要 | 手動編集がリファクタリングパターンに見える場合に自動提案 |
| ReScript での活用 | 関数名を手動変更 → 「全箇所をリネームしますか？」と提案 |
| 難易度 | 中〜高 `●` |
| 参考プラグイン | Kotlin |
| 実装方針 | PSI 変更をモニタリングし、識別子の変更パターンを検出 |

### 4-8. Introduce Constant

| 項目 | 内容 |
|------|------|
| Extension Point | `com.intellij.refactoring.introduceHandler` |
| 概要 | マジックナンバーや文字列リテラルを定数に抽出 |
| ReScript での活用 | `"https://api.example.com"` → `let apiUrl = "https://api.example.com"` としてモジュールトップレベルに抽出 |
| 難易度 | 高 `●` |
| 参考プラグイン | Java, Kotlin |
| 実装方針 | Extract Variable の特殊ケース。リテラルをモジュールスコープに移動 |

---

## カテゴリ 5: コード分析・インスペクション

コード品質の向上と問題の早期検出を支援する機能群。

### 5-1. Highlight Usages (Semantic)

| 項目 | 内容 |
|------|------|
| Extension Point | `com.intellij.highlightUsagesHandlerFactory` |
| 概要 | カーソル位置に応じた関連箇所のハイライト |
| ReScript での活用 | `switch` → 全パターンブランチをハイライト、`try` → 全 catch ブランチ、関数内 → 全 return ポイント（最後の式 + raise） |
| 難易度 | 中 `▲` |
| 参考プラグイン | Java, Kotlin, Go |
| 実装方針 | キーワードトークンから対応するブロック構造をトークンスキャンで特定 |

### 5-2. Usage Type Provider

| 項目 | 内容 |
|------|------|
| Extension Point | `com.intellij.usageTypeProvider` |
| 概要 | Find Usages 結果を用途別にグループ化 |
| ReScript での活用 | 「open 文での使用」「型参照」「値参照」「パターンマッチ」「JSX プロパティ」等にグループ化 |
| 難易度 | 中 `▲` |
| 参考プラグイン | Java, Kotlin, Go |
| 実装方針 | 使用箇所の前後トークンから使用コンテキストを推測 |

### 5-3. Problem Highlight Filter

| 項目 | 内容 |
|------|------|
| Extension Point | `com.intellij.problemHighlightFilter` |
| 概要 | 特定ファイルのエラーハイライトを抑制 |
| ReScript での活用 | `lib/` (コンパイル出力)、`node_modules/` 内の `.res` ファイルのハイライトを抑制 |
| 難易度 | 低 `★` |
| 参考プラグイン | 各種 |
| 実装方針 | `ProblemHighlightFilter` でファイルパスをチェック |

### 5-4. Inspection Suppression Provider

| 項目 | 内容 |
|------|------|
| Extension Point | `com.intellij.lang.inspectionSuppressor` |
| 概要 | コメントによるインスペクション抑制 |
| ReScript での活用 | `// @suppress` や `// noinspection RescriptDuplicateOpen` で特定の警告を抑制 |
| 難易度 | 低〜中 `★` |
| 参考プラグイン | Dart, Kotlin |
| 実装方針 | コメントトークンをスキャンして抑制アノテーションを検出 |

### 5-5. Expression Type Info

| 項目 | 内容 |
|------|------|
| Extension Point | `com.intellij.codeInsight.typeInfo` |
| 概要 | Ctrl+Shift+P でカーソル位置の式の型を表示 |
| ReScript での活用 | 型推論結果の確認。ホバーとは異なり、選択範囲の式の型を明示的に表示 |
| 難易度 | 中 `●` |
| 参考プラグイン | Dart, Kotlin, Rust |
| 実装方針 | LSP の hover レスポンスから型情報を抽出 |

### 5-6. External Annotator (Format Check)

| 項目 | 内容 |
|------|------|
| Extension Point | `com.intellij.externalAnnotator` |
| 概要 | 外部ツールの結果をエディタにアノテーション表示 |
| ReScript での活用 | `rescript format --check` を実行し、フォーマットされていない箇所をインライン表示。既存の reanalyze annotator に加えて |
| 難易度 | 中 `★` |
| 参考プラグイン | Go (go vet), Rust (clippy), Haskell (HLint) |
| 実装方針 | 既存の `RescriptReanalyzeAnnotator` と同パターン。`rescript format --check` の diff 出力をパース |

### 5-7. Unresolved Reference Quick Fix

| 項目 | 内容 |
|------|------|
| Extension Point | `com.intellij.codeInsight.unresolvedReferenceQuickFixProvider` |
| 概要 | 未解決参照に対する Quick Fix（open 追加、インポート追加） |
| ReScript での活用 | 未解決のモジュール名 → `open ModuleName` を提案、未インストールパッケージ → npm install を提案 |
| 難易度 | 中〜高 `●` |
| 参考プラグイン | Dart, Kotlin |
| 実装方針 | LSP の code action で部分的に対応済みだが、ネイティブの Quick Fix で補強 |

---

## カテゴリ 6: Find Usages 拡張

シンボル検索の品質向上。

### 6-1. FindUsagesProvider + WordsScanner

| 項目 | 内容 |
|------|------|
| Extension Point | `com.intellij.lang.findUsagesProvider` |
| 概要 | Find Usages のカスタマイズ（シンボルの種類表示、ワードスキャン） |
| ReScript での活用 | 検索結果に「module」「function」「type」等のシンボル種類を表示。ワードスキャナーで識別子/コメント/文字列を正しく分類 |
| 難易度 | 低〜中 `★` |
| 参考プラグイン | Elixir, Dart |
| 実装方針 | `FindUsagesProvider` を実装し、PSI 要素の種類に応じたテキストを返す。`WordsScanner` でレクサーベースのトークン分類 |

### 6-2. Element Description Provider

| 項目 | 内容 |
|------|------|
| Extension Point | `com.intellij.elementDescriptionProvider` |
| 概要 | PSI 要素のユーザー向け説明テキスト（リファクタリングダイアログ等で使用） |
| ReScript での活用 | 「function 'myFunc'」「module 'MyModule'」「type 'user'」等のわかりやすい説明 |
| 難易度 | 低 `★` |
| 参考プラグイン | 各種 |
| 実装方針 | `ElementDescriptionProvider` で PSI 要素タイプに応じた文字列を返す |

---

## カテゴリ 7: ドキュメント

コードのドキュメント閲覧・生成を支援する機能群。

### 7-1. Quick Documentation Provider

| 項目 | 内容 |
|------|------|
| Extension Point | `com.intellij.platform.backend.documentation.psiTargetProvider` (新API) / `com.intellij.lang.documentationProvider` (レガシー) |
| 概要 | Ctrl+Q / F1 でフォーマットされたドキュメントをポップアップ表示 |
| ReScript での活用 | `/** ... */` ドキュメントコメントをリッチ HTML でレンダリング。LSP ホバーの生テキストをフォーマット改善 |
| 難易度 | 中 `●` |
| 参考プラグイン | Dart, Elixir, Zig, Prisma |
| 実装方針 | LSP hover レスポンスの Markdown をパースし、シンタックスハイライト付き HTML に変換 |

### 7-2. External Documentation Provider

| 項目 | 内容 |
|------|------|
| Extension Point | `DocumentationProvider.getUrlFor()` |
| 概要 | Shift+F1 でブラウザの外部ドキュメントを開く |
| ReScript での活用 | 標準ライブラリ関数 → rescript-lang.org/docs/manual、Belt/Js モジュール → 対応するドキュメントページ |
| 難易度 | 中 `★` |
| 参考プラグイン | Dart (dart.dev), Elixir (hexdocs.pm) |
| 実装方針 | モジュール名から URL を構築。`Belt.Array` → `https://rescript-lang.org/docs/manual/api/belt/array` 等 |

### 7-3. Predefined Code Style

| 項目 | 内容 |
|------|------|
| Extension Point | `com.intellij.predefinedCodeStyle` |
| 概要 | Settings > Code Style の「Set from Predefined Style」に選択肢を追加 |
| ReScript での活用 | 「ReScript Standard」スタイルプリセット（`rescript format` と一致するインデント設定） |
| 難易度 | 低 `★` |
| 参考プラグイン | Dart, Kotlin |
| 実装方針 | `PredefinedCodeStyle` でインデント幅2、スペース使用等のデフォルト値を提供 |

---

## カテゴリ 8: プロジェクトビュー

IDE のプロジェクトツリー表示をカスタマイズする機能群。

### 8-1. Tree Structure Provider (.resi ファイルのネスト)

| 項目 | 内容 |
|------|------|
| Extension Point | `com.intellij.treeStructureProvider` |
| 概要 | プロジェクトビューのツリー構造をカスタマイズ |
| ReScript での活用 | `.resi` を `.res` の下にネスト表示（Dart の `.g.dart` ネストと同様）。`lib/` コンパイル出力を非表示。関連ファイル (.res + .resi + .js) のグルーピング |
| 難易度 | 中 `★` |
| 参考プラグイン | Dart (.g.dart nesting), Vue (.vue nesting) |
| 実装方針 | `TreeStructureProvider` で `.resi` ノードを対応する `.res` ノードの子として再配置 |

### 8-2. Project View Node Decorator

| 項目 | 内容 |
|------|------|
| Extension Point | `com.intellij.projectViewNodeDecorator` |
| 概要 | プロジェクトビューのノードに追加情報を装飾 |
| ReScript での活用 | コンパイルエラーのあるファイルにエラーアイコン、`.resi` の有無の表示、`@genType` 付きファイルのマーク |
| 難易度 | 低〜中 `★` |
| 参考プラグイン | Dart, Flutter |
| 実装方針 | `ProjectViewNodeDecorator` でファイル状態に応じたアイコン/テキストを追加 |

### 8-3. Test Source Filter

| 項目 | 内容 |
|------|------|
| Extension Point | `com.intellij.testSourcesFilter` |
| 概要 | テストファイル/ディレクトリの自動認識 |
| ReScript での活用 | `__tests__/`, `*_test.res`, `*.test.res` をテストソースとしてマーク。アイコン差別化、検索スコープの分離 |
| 難易度 | 低 `★` |
| 参考プラグイン | Dart, Elixir |
| 実装方針 | `TestSourcesFilter` でファイルパスパターンマッチ |

### 8-4. Framework Detector

| 項目 | 内容 |
|------|------|
| Extension Point | `com.intellij.framework.detection.facetBasedFrameworkDetector` / `com.intellij.frameworkDetector` |
| 概要 | プロジェクト内のフレームワーク自動検出 |
| ReScript での活用 | `rescript.json` の存在で ReScript プロジェクトを自動検出し、ファセットを設定 |
| 難易度 | 中 `★` |
| 参考プラグイン | Dart (pubspec.yaml), Svelte |
| 実装方針 | 既存の `RescriptLspDetector` を活用 |

---

## カテゴリ 9: インデキシング・パフォーマンス

大規模プロジェクトでのパフォーマンスを向上させる機能群。

### 9-1. Stub Index

| 項目 | 内容 |
|------|------|
| Extension Point | `com.intellij.stubIndex` / `com.intellij.stubElementTypeHolder` |
| 概要 | ファイル全体をパースせずにシンボルを高速検索するインデックス |
| ReScript での活用 | モジュール名、型宣言、関数名のインデックス。大規模モノレポでの Go to Symbol 高速化 |
| 難易度 | 高 `●` |
| 参考プラグイン | Elixir, Dart, Prisma |
| 実装方針 | PSI の Stub 版を定義し、`StubIndex` でキー検索を実装。パーサーの Stub 対応が必要 |

### 9-2. File-Based Index

| 項目 | 内容 |
|------|------|
| Extension Point | `com.intellij.fileBasedIndex` |
| 概要 | ファイルベースのカスタムインデックス |
| ReScript での活用 | `open` 文のインデックス（「このモジュールを open しているファイル一覧」）、`@genType` アノテーションのインデックス |
| 難易度 | 中 `★` |
| 参考プラグイン | Dart, Elixir, Prisma |
| 実装方針 | `FileBasedIndexExtension` で `.res` ファイルをスキャンし、特定パターンをインデックス化 |

### 9-3. Index Pattern Builder

| 項目 | 内容 |
|------|------|
| Extension Point | `com.intellij.indexPatternBuilder` |
| 概要 | TODO/FIXME パターン検索のカスタマイズ |
| ReScript での活用 | 既存の `todoIndexer` を補強。コメント内だけでなく、ドキュメントコメント内のパターンも対応 |
| 難易度 | 低 `★` |
| 参考プラグイン | 各種 |
| 実装方針 | `IndexPatternBuilder` でレクサーを使用してコメントトークンを識別 |

---

## カテゴリ 10: テスト・実行

テスト駆動開発と実行環境を支援する機能群。

### 10-1. REPL / Scratch File

| 項目 | 内容 |
|------|------|
| Extension Point | `com.intellij.scratch.creationHelper` |
| 概要 | ReScript スクラッチファイルをコンパイル・実行 |
| ReScript での活用 | その場で ReScript コードを書いて実行結果を確認。学習・実験・プロトタイピングに有用 |
| 難易度 | 中〜高 `●` |
| 参考プラグイン | Scala (worksheet), Kotlin (scratch), Haskell (GHCi) |
| 実装方針 | スクラッチ `.res` ファイルを一時ディレクトリでコンパイル（`rescript build`）→ Node.js で実行 → 結果をエディタに表示 |

### 10-2. Package Dependencies View

| 項目 | 内容 |
|------|------|
| Extension Point | `com.intellij.toolWindow` |
| 概要 | `rescript.json` の依存関係をツリー表示 |
| ReScript での活用 | `bs-dependencies`, `bs-dev-dependencies`, `pinned-dependencies` のツリー表示。依存パッケージのバージョン確認、ソースファイルへのナビゲーション |
| 難易度 | 中 `★` |
| 参考プラグイン | Go (go.mod), Rust (Cargo), Node.js (package.json) |
| 実装方針 | `rescript.json` を JSON パースし、依存関係ツリーを `ToolWindow` に表示 |

### 10-3. Run Anything Provider

| 項目 | 内容 |
|------|------|
| Extension Point | `com.intellij.runAnythingProvider` |
| 概要 | Ctrl+Ctrl で ReScript CLI コマンドを実行 |
| ReScript での活用 | `rescript build`, `rescript clean`, `rescript format <file>` をダイアログから直接実行 |
| 難易度 | 低〜中 `★` |
| 参考プラグイン | npm scripts, Gradle tasks |
| 実装方針 | 既存の `RescriptCliDetector` を活用 |

---

## カテゴリ 11: 言語インジェクション拡張

埋め込み言語のサポートを強化する機能群。

### 11-1. IntelliLang Language Support

| 項目 | 内容 |
|------|------|
| Extension Point | `org.intellij.intelliLang.languageSupport` |
| 概要 | IntelliLang による自動インジェクション設定 |
| ReScript での活用 | ユーザーが Settings でカスタムインジェクションを設定可能に（`%sql()` → SQL、`%css()` → CSS 等） |
| 難易度 | 低〜中 `★` |
| 参考プラグイン | Kotlin, Dart |
| 実装方針 | IntelliLang のパターンマッチングに ReScript の PSI パターンを登録 |

### 11-2. Formatting for Injected Options

| 項目 | 内容 |
|------|------|
| Extension Point | `com.intellij.formatting.injectedOptions` |
| 概要 | インジェクトされた言語のフォーマット制御 |
| ReScript での活用 | `%raw()` 内の JavaScript を JS フォーマッタでフォーマット。周囲の ReScript は `rescript format` を使用 |
| 難易度 | 低 `★` |
| 参考プラグイン | 各種 |
| 実装方針 | `InjectedFormattingOptionsProvider` でインジェクト先の設定を返す |

---

## カテゴリ 12: VSCode ReScript 拡張との差分

VSCode の ReScript 拡張にあってこのプラグインにない機能。

### 12-1. Paste as JSX

| 項目 | 内容 |
|------|------|
| Extension Point | `com.intellij.copyPastePostProcessor` |
| 概要 | HTML/JSX をクリップボードから ReScript JSX 形式に変換ペースト |
| ReScript での活用 | `<div class="foo" onclick="handler">` → `<div className="foo" onClick={handler}>` |
| 難易度 | 中 `★` |
| 参考プラグイン | ReScript VSCode 拡張 |
| 実装方針 | 既存の `RescriptPasteAsJsonAction` と同パターン。HTML パースして ReScript JSX に変換 |

### 12-2. Additional Snippets / Completions

| 項目 | 内容 |
|------|------|
| Extension Point | Live Templates / Postfix Completion |
| 概要 | VSCode 拡張にあるスニペットの追加 |
| ReScript での活用 | `.promise` (Postfix)、`@module` (Live Template)、`@val` (Live Template) 等の追加 |
| 難易度 | 低 `★` |
| 参考プラグイン | ReScript VSCode 拡張 |
| 実装方針 | 既存の Live Templates XML / PostfixTemplateProvider に追加 |

---

## カテゴリ 13: その他の IDE 統合

その他の便利機能。

### 13-1. Bundled Dictionary Provider

| 項目 | 内容 |
|------|------|
| Extension Point | `com.intellij.spellchecker.bundledDictionaryProvider` |
| 概要 | ReScript 固有用語の辞書をバンドル |
| ReScript での活用 | `genType`, `uncurried`, `polyvariant`, `functor`, `Belt`, `Js`, `rescript` 等のスペルチェック誤検出を防止 |
| 難易度 | 低 `★` |
| 参考プラグイン | Dart, Kotlin, Rust |
| 実装方針 | 辞書テキストファイルを `resources/` に配置し、`BundledDictionaryProvider` で登録 |

### 13-2. Color Preview in Gutter

| 項目 | 内容 |
|------|------|
| Extension Point | `com.intellij.colorProvider` |
| 概要 | 色リテラルのガタースウォッチ表示 |
| ReScript での活用 | `"#ff0000"`, `"rgb(255,0,0)"`, `"hsl(120,100%,50%)"` 等の色プレビュー |
| 難易度 | 低 `★` |
| 参考プラグイン | CSS, JavaScript, Kotlin Compose |
| 実装方針 | 文字列リテラルトークンを正規表現でスキャンし、色コードをパース |

### 13-3. Reader Mode Matcher

| 項目 | 内容 |
|------|------|
| Extension Point | `com.intellij.readerModeMatcher` |
| 概要 | ライブラリソースを読み取り専用の読みやすい表示に |
| ReScript での活用 | `node_modules/` 内の `.resi` ファイルを Reader Mode で表示 |
| 難易度 | 低 `★` |
| 参考プラグイン | Dart, Kotlin |
| 実装方針 | `ReaderModeMatcher` でファイルパスが `node_modules` 内かを判定 |

### 13-4. VCS Code Vision Language Context

| 項目 | 内容 |
|------|------|
| Extension Point | `com.intellij.vcs.codeVisionLanguageContext` |
| 概要 | 宣言上部に VCS 情報（作者、最終変更日）を Code Vision で表示 |
| ReScript での活用 | `let` 宣言や `module` 定義の上に「Last changed by X, 3 days ago」を表示 |
| 難易度 | 低〜中 `★` |
| 参考プラグイン | Dart, Kotlin |
| 実装方針 | 既存の `RescriptCodeVisionProvider` と同様に Code Vision API を使用。PSI 宣言ノードの位置を VCS に渡す |

### 13-5. Code Rearranger

| 項目 | 内容 |
|------|------|
| Extension Point | `com.intellij.lang.rearranger` |
| 概要 | 宣言の自動並び替え（Code > Rearrange Code） |
| ReScript での活用 | `type` → `module` → `let` → `external` の順序で自動整列、`open` 文をファイル先頭にまとめる |
| 難易度 | 中 `★` |
| 参考プラグイン | Dart, Kotlin |
| 実装方針 | `Rearranger` で PSI 宣言タイプに応じたソート規則を定義 |

### 13-6. Dependency Diagram

| 項目 | 内容 |
|------|------|
| Extension Point | `com.intellij.diagram.DiagramProvider` |
| 概要 | モジュール依存関係のビジュアルグラフ表示 |
| ReScript での活用 | 既存の Module Hierarchy をグラフ形式で可視化。循環依存の検出 |
| 難易度 | 高 `★` |
| 参考プラグイン | Java, Kotlin |
| 実装方針 | 既存の `RescriptDependencyAnalyzer` を活用し、`DiagramProvider` で可視化 |

### 13-7. Auto Import Options Provider

| 項目 | 内容 |
|------|------|
| Extension Point | `com.intellij.autoImportOptionsProvider` |
| 概要 | 自動 import/open の設定 UI |
| ReScript での活用 | 自動 `open` の有効/無効、除外モジュールの設定、import スタイル（修飾名 vs open）の選択 |
| 難易度 | 低〜中 `★` |
| 参考プラグイン | Dart, Kotlin |
| 実装方針 | Settings UI でオプションを提供し、`RescriptImportOptimizer` に反映 |

### 13-8. Element Signature Provider

| 項目 | 内容 |
|------|------|
| Extension Point | `com.intellij.elementSignatureProvider` |
| 概要 | PSI 要素の安定シグネチャ生成（折りたたみ状態の永続化等） |
| ReScript での活用 | コード折りたたみの状態が IDE 再起動後も維持される |
| 難易度 | 低 `★` |
| 参考プラグイン | 各種 |
| 実装方針 | 宣言の種類 + 名前 + 位置からユニークなシグネチャ文字列を生成 |

### 13-9. Editor Floating Toolbar Provider

| 項目 | 内容 |
|------|------|
| Extension Point | `com.intellij.editorFloatingToolbarProvider` |
| 概要 | 選択コード付近にフローティングツールバーを表示 |
| ReScript での活用 | 「Wrap with pipe」「Extract to let」「Run this test」等のクイックアクション |
| 難易度 | 中 `★` |
| 参考プラグイン | 各種 |
| 実装方針 | 選択コンテキストに応じたアクションをツールバーに表示 |

### 13-10. Grazie Text Extractor

| 項目 | 内容 |
|------|------|
| Extension Point | `com.intellij.grazie.textExtractor` |
| 概要 | Grazie 文法チェッカーとの統合 |
| ReScript での活用 | ドキュメントコメントと文字列リテラルの英文法チェック |
| 難易度 | 低 `★` |
| 参考プラグイン | Dart, Kotlin |
| 実装方針 | コメント/文字列トークンからテキストを抽出する `TextExtractor` を実装 |

---

## 優先度別サマリー

### S (最優先) — 高インパクト + 実装容易

| # | 機能 | カテゴリ | 難易度 |
|---|------|---------|--------|
| 1 | Unwrap/Remove | 編集 | 低〜中 `★` |
| 2 | Go to Test / Create Test | ナビゲーション | 低 `★` |
| 3 | Tree Structure Provider (.resi nesting) | プロジェクトビュー | 中 `★` |
| 4 | Typed Handler (JSX 閉じタグ / パイプ) | 編集 | 中 `★` |
| 5 | Bundled Dictionary | その他 | 低 `★` |
| 6 | Context Info | ナビゲーション | 低 `★` |
| 7 | Test Source Filter | プロジェクトビュー | 低 `★` |
| 8 | FindUsagesProvider + WordsScanner | Find Usages | 低〜中 `★` |

### A (高優先) — 高インパクト or 中程度の労力

| # | 機能 | カテゴリ | 難易度 |
|---|------|---------|--------|
| 9 | Extend/Shrink Word Selection | 編集 | 中 `▲` |
| 10 | Enter Handler (doc comment 継続) | 編集 | 低〜中 `★` |
| 11 | Expression Type Info | 分析 | 中 `●` |
| 12 | Highlight Usages (Semantic) | 分析 | 中 `▲` |
| 13 | Join Lines (Smart) | 編集 | 低〜中 `★` |
| 14 | Completion Confidence | 補完 | 低 `★` |
| 15 | Live Template Context | 補完 | 低 `★` |
| 16 | Live Template Macros | 補完 | 低 `★` |
| 17 | Problem Highlight Filter | 分析 | 低 `★` |
| 18 | External Documentation | ドキュメント | 中 `★` |
| 19 | Run Anything Provider | 実行 | 低〜中 `★` |
| 20 | Goto Super (.res → .resi) | ナビゲーション | 中 `★` |
| 21 | Additional Snippets | VSCode パリティ | 低 `★` |

### B (中優先) — あると便利

| # | 機能 | カテゴリ | 難易度 |
|---|------|---------|--------|
| 22 | Move Element Left/Right | 編集 | 中〜高 `▲` |
| 23 | Usage Type Provider | 分析 | 中 `▲` |
| 24 | Backspace Handler | 編集 | 低 `★` |
| 25 | Code Block Support Handler | 編集 | 中 `▲` |
| 26 | Split/Join List | 編集 | 中 `▲` |
| 27 | Copy/Paste Pre-Processor | 編集 | 中 `★` |
| 28 | Inspection Suppression | 分析 | 低〜中 `★` |
| 29 | Lookup Char Filter | 補完 | 低 `★` |
| 30 | Quick Documentation Provider | ドキュメント | 中 `●` |
| 31 | Project View Node Decorator | プロジェクトビュー | 低〜中 `★` |
| 32 | File-Based Index (open 文) | インデキシング | 中 `★` |
| 33 | Predefined Code Style | ドキュメント | 低 `★` |
| 34 | Element Description Provider | Find Usages | 低 `★` |
| 35 | Safe Delete | リファクタリング | 中〜高 `●` |
| 36 | Name Suggestion Provider | リファクタリング | 中 `●` |
| 37 | Paste as JSX | VSCode パリティ | 中 `★` |
| 38 | Package Dependencies View | 実行 | 中 `★` |
| 39 | VCS Code Vision | その他 | 低〜中 `★` |
| 40 | Reader Mode | その他 | 低 `★` |
| 41 | Color Preview in Gutter | その他 | 低 `★` |
| 42 | Auto Import Options | その他 | 低〜中 `★` |

### C (低優先) — nice-to-have or 高難度

| # | 機能 | カテゴリ | 難易度 |
|---|------|---------|--------|
| 43 | Extract Variable | リファクタリング | 高 `●` |
| 44 | Call Hierarchy | ナビゲーション | 高 `●` |
| 45 | Go to Implementation | ナビゲーション | 中 `★` |
| 46 | Search Everywhere Contributor | ナビゲーション | 中 `★` |
| 47 | Navigation Bar Model | ナビゲーション | 低〜中 `★` |
| 48 | External Annotator (Format Check) | 分析 | 中 `★` |
| 49 | Unresolved Reference Quick Fix | 分析 | 中〜高 `●` |
| 50 | Completion Weigher | 補完 | 中 `●` |
| 51 | Stub Index | インデキシング | 高 `●` |
| 52 | Code Rearranger | その他 | 中 `★` |
| 53 | Strip Trailing Spaces Filter | 編集 | 低 `★` |
| 54 | IntelliLang Support | インジェクション | 低〜中 `★` |
| 55 | Formatting for Injected | インジェクション | 低 `★` |
| 56 | Framework Detector | プロジェクトビュー | 中 `★` |
| 57 | Scratch File | 実行 | 中〜高 `●` |
| 58 | REPL | 実行 | 中〜高 `●` |
| 59 | Grazie Text Extractor | その他 | 低 `★` |
| 60 | Element Signature Provider | その他 | 低 `★` |
| 61 | Index Pattern Builder | インデキシング | 低 `★` |

### D (将来検討) — パーサー大幅拡張が前提

| # | 機能 | カテゴリ | 難易度 |
|---|------|---------|--------|
| 62 | Extract Function | リファクタリング | 非常に高 `●` |
| 63 | Inline Variable/Function | リファクタリング | 高 `●` |
| 64 | Change Signature | リファクタリング | 非常に高 `●` |
| 65 | Introduce Constant | リファクタリング | 高 `●` |
| 66 | Suggested Refactoring | リファクタリング | 中〜高 `●` |
| 67 | Dependency Diagram | その他 | 高 `★` |
| 68 | File Include Provider | ナビゲーション | 中 `●` |
| 69 | Editor Floating Toolbar | その他 | 中 `★` |

---

## カテゴリ 14: 追加調査で発見された新機能

他エディタ・プラグイン（Gleam Language Server, Scala IntelliJ Plugin, GoLand, RustRover, WebStorm, Elm Plugin）の追加調査で発見された、上記リストに含まれていない機能群。

### 14-1. パイプチェーン中間型ヒント

| 項目 | 内容 |
|------|------|
| Extension Point | `com.intellij.codeInsight.inlayProvider` (InlayHintsProvider) |
| 概要 | パイプチェーン(`->`)の各ステップで中間の戻り値型をインライン表示 |
| ReScript での活用 | `data->Array.map(f)->Array.filter(g)->Array.reduce(...)` の各ステップの型を右側に表示。型推論の流れを可視化 |
| 難易度 | 中 `●` |
| 参考プラグイン | Scala IntelliJ Plugin (method chain hints), RustRover |
| 実装方針 | InlayHintsProvider で各 `->` トークン位置の LSP hover を取得し、型を表示 |

### 14-2. Pipe ⇔ 関数呼び出し変換

| 項目 | 内容 |
|------|------|
| Extension Point | `com.intellij.intentionAction` |
| 概要 | パイプ構文と通常の関数呼び出し構文を相互変換 |
| ReScript での活用 | `arr->Array.map(f)` ⇔ `Array.map(arr, f)` の変換。コーディングスタイルの統一やリファクタリングに有用 |
| 難易度 | 中 `▲` |
| 参考プラグイン | Gleam Language Server, Elm IntelliJ Plugin |
| 実装方針 | カーソル位置の `->` トークンを検出し、前後のテキストを入れ替え |

### 14-3. ラベル付き引数の一括挿入

| 項目 | 内容 |
|------|------|
| Extension Point | `com.intellij.intentionAction` |
| 概要 | 関数呼び出しに未指定のラベル付き引数をすべて挿入 |
| ReScript での活用 | `<MyComponent />` → `<MyComponent name="" age=0 onClick={_ => ()} />` のように、React コンポーネントの全 props を一括挿入 |
| 難易度 | 中 `●` |
| 参考プラグイン | Gleam Language Server |
| 実装方針 | LSP の signature help / completion から未指定ラベルを取得して挿入 |

### 14-4. 未使用結果の明示的無視

| 項目 | 内容 |
|------|------|
| Extension Point | `com.intellij.intentionAction` / Quick Fix |
| 概要 | 未使用の式結果に `->ignore` または `let _ = ...` を追加 |
| ReScript での活用 | 未使用式結果の警告に対する Quick Fix。`Js.log("debug")` → `Js.log("debug")->ignore` |
| 難易度 | 低 `★` |
| 参考プラグイン | Gleam Language Server |
| 実装方針 | LSP 診断メッセージの未使用警告をトリガーに、`->ignore` を末尾に挿入 |

### 14-5. インターフェース公開/非公開の切り替え

| 項目 | 内容 |
|------|------|
| Extension Point | `com.intellij.intentionAction` |
| 概要 | 関数/型を `.resi` インターフェースに追加または削除 |
| ReScript での活用 | `.res` の関数にカーソルを置いて「Add to interface」で `.resi` に宣言を追加。逆に「Remove from interface」で公開を取り消し |
| 難易度 | 中 `★` |
| 参考プラグイン | Elm IntelliJ Plugin (expose/unexpose + gutter icon) |
| 実装方針 | LSP hover で型シグネチャを取得し、対応する `.resi` ファイルを検索・編集 |

### 14-6. JSON エンコーダ/デコーダ生成

| 項目 | 内容 |
|------|------|
| Extension Point | `com.intellij.codeInsight.generation` / Generate アクション |
| 概要 | 型定義から JSON のエンコード/デコード関数を自動生成 |
| ReScript での活用 | `type user = {name: string, age: int}` → `let encodeUser` / `let decodeUser` を自動生成 |
| 難易度 | 中〜高 `▲` |
| 参考プラグイン | Elm IntelliJ Plugin, Gleam Language Server |
| 実装方針 | 既存の `RescriptTypeDeclarationParser` を活用。型フィールドから JSON.Decode / JSON.Encode パターンを生成 |

### 14-7. Switch ケース統合

| 項目 | 内容 |
|------|------|
| Extension Point | `com.intellij.intentionAction` |
| 概要 | 同じボディを持つ switch ケースを統合 |
| ReScript での活用 | `\| A => body \| B => body` → `\| A \| B => body` に統合してコード重複を削減 |
| 難易度 | 中 `▲` |
| 参考プラグイン | Gleam Language Server |
| 実装方針 | switch ブロック内の連続するケースのボディテキストを比較して統合を提案 |

### 14-8. React コンポーネント抽出

| 項目 | 内容 |
|------|------|
| Extension Point | `com.intellij.lang.refactoringSupport` |
| 概要 | 選択した JSX ブロックを新しい React コンポーネントとして抽出 |
| ReScript での活用 | JSX の一部を選択して `@react.component let make = (~props) => <...>` として新コンポーネントに分離 |
| 難易度 | 高 `●` |
| 参考プラグイン | WebStorm (Extract React Component) |
| 実装方針 | JSX 範囲の特定 + 参照変数の props 化 + コンポーネントスケルトン生成 |

### 14-9. PPX/マクロ展開ビュー

| 項目 | 内容 |
|------|------|
| Extension Point | `com.intellij.toolWindow` |
| 概要 | PPX 変換の展開結果を表示するツールウィンドウ |
| ReScript での活用 | `@react.component`, `@deriving(json)`, `@genType` 等の PPX がどのようなコードに変換されるかを確認 |
| 難易度 | 高 `●` |
| 参考プラグイン | RustRover (macro expansion view) |
| 実装方針 | ReScript コンパイラの PPX パイプラインを呼び出して展開結果を表示。カスタム CLI コマンド or LSP リクエストが必要 |

### 14-10. Make 関数生成

| 項目 | 内容 |
|------|------|
| Extension Point | `com.intellij.codeInsight.generation` / Generate アクション |
| 概要 | レコード型定義からコンストラクタ（make 関数）を自動生成 |
| ReScript での活用 | `type user = {name: string, age: int}` → `let make = (~name, ~age) => {name, age}` を自動生成 |
| 難易度 | 中 `▲` |
| 参考プラグイン | GoLand (Generate Constructor), RustRover |
| 実装方針 | 既存の `RescriptTypeDeclarationParser` でフィールドを取得し、ラベル付き引数の make 関数を生成 |

### 14-11. 分割代入の導入/解除

| 項目 | 内容 |
|------|------|
| Extension Point | `com.intellij.intentionAction` |
| 概要 | パターンマッチによる分割代入の導入/解除を支援 |
| ReScript での活用 | `let result = getData(); let name = result.name` → `let {name} = getData()` に変換 |
| 難易度 | 中 `▲` |
| 参考プラグイン | WebStorm (Destructuring intentions) |
| 実装方針 | 変数の使用パターンを検出して分割代入への変換を提案 |

### 14-12. 識別子ケース修正

| 項目 | 内容 |
|------|------|
| Extension Point | `com.intellij.intentionAction` / Quick Fix |
| 概要 | 命名規則に反する識別子のケースを自動修正 |
| ReScript での活用 | `myModule` → `MyModule`（モジュール名）、`MyFunction` → `myFunction`（関数名）の修正を提案 |
| 難易度 | 低〜中 `★` |
| 参考プラグイン | Gleam Language Server |
| 実装方針 | PSI 要素の種類に応じてケース規則をチェックし、LSP リネームで修正 |

### 14-13. 型注釈一括追加

| 項目 | 内容 |
|------|------|
| Extension Point | `com.intellij.intentionAction` |
| 概要 | モジュール内の全トップレベル定義に型注釈を一括追加 |
| ReScript での活用 | `.resi` 生成前の準備として、すべての `let` バインディングに型注釈を付与 |
| 難易度 | 中 `●` |
| 参考プラグイン | Gleam Language Server |
| 実装方針 | LSP hover で各定義の推論型を取得し、型注釈として挿入 |

### 14-14. 冗長ブロック削除

| 項目 | 内容 |
|------|------|
| Extension Point | `com.intellij.intentionAction` / Inspection |
| 概要 | 単一式のみを含む不要なブロック `{ expr }` を `expr` に簡略化 |
| ReScript での活用 | `let x = { 42 }` → `let x = 42`。コードの簡潔化 |
| 難易度 | 低 `★` |
| 参考プラグイン | Gleam Language Server |
| 実装方針 | ブレースペアを検出し、内部が単一式かを判定して除去を提案 |

### 14-15. 型ミスマッチのインラインヒント

| 項目 | 内容 |
|------|------|
| Extension Point | `com.intellij.codeInsight.inlayProvider` |
| 概要 | 型エラー箇所に期待型と実際型をインラインで表示 |
| ReScript での活用 | 型エラー発生時に `expected: string, got: int` をエラー箇所の横にインライン表示。LSP 診断メッセージの解析結果をリッチに表示 |
| 難易度 | 中 `●` |
| 参考プラグイン | Scala IntelliJ Plugin |
| 実装方針 | LSP 診断メッセージを正規表現でパースし、expected/actual 型を抽出してインレイヒントとして表示 |

### 14-16. モジュールタイプ実装の自動生成

| 項目 | 内容 |
|------|------|
| Extension Point | `com.intellij.codeInsight.implementMethod` |
| 概要 | module type の全メンバーに対する実装スタブを自動生成 |
| ReScript での活用 | `module type S = { let foo: int; type t }` に対して `module M: S = { let foo = 0; type t = unit }` のスタブを生成 |
| 難易度 | 高 `●` |
| 参考プラグイン | GoLand (Implement Interface), RustRover (Implement Trait) |
| 実装方針 | module type シグネチャのパースと型ごとのデフォルト値生成が必要 |

### 14-17. MultiLang Commenter

| 項目 | 内容 |
|------|------|
| Extension Point | `com.intellij.multiLangCommenter` |
| 概要 | `%raw()` 内で JavaScript コメント構文を使用 |
| ReScript での活用 | `%raw("...")` ブロック内で Ctrl+/ すると `//`（JS）でコメント化し、ReScript 部分では `//`（ReScript）を使用。言語境界での正しいコメント切り替え |
| 難易度 | 低〜中 `★` |
| 参考プラグイン | 各種マルチ言語プラグイン |
| 実装方針 | `MultipleLangCommentProvider` でインジェクション境界を検出し、適切なコメントスタイルを返す |

### 14-18. Parameter Info Handler

| 項目 | 内容 |
|------|------|
| Extension Point | `com.intellij.codeInsight.parameterInfo` |
| 概要 | Ctrl+P で関数パラメータのネイティブポップアップ表示 |
| ReScript での活用 | ラベル付き引数（`~name`, `~age`）を構造化して表示。現在入力中のパラメータをハイライト。LSP signatureHelp より高品質な表示 |
| 難易度 | 中 `●` |
| 参考プラグイン | Java, Kotlin, Dart |
| 実装方針 | `ParameterInfoHandler` で LSP signatureHelp をラップし、ネイティブ UI で表示 |

### 14-19. 使用箇所からの関数生成

| 項目 | 内容 |
|------|------|
| Extension Point | `com.intellij.intentionAction` / Quick Fix |
| 概要 | 未定義の関数呼び出しから関数スケルトンを自動生成 |
| ReScript での活用 | `validateUser(user)` と書いてから「Create function」で `let validateUser = (user) => { ... }` を生成 |
| 難易度 | 中〜高 `●` |
| 参考プラグイン | Gleam Language Server, IntelliJ (Create from usage) |
| 実装方針 | LSP 診断の「unbound value」エラーをトリガーに、呼び出しコンテキストからパラメータを推論 |

### 14-20. Long Line Inspection Policy

| 項目 | 内容 |
|------|------|
| Extension Point | `com.intellij.longLineInspectionPolicy` |
| 概要 | 特定構造での長い行の警告を抑制 |
| ReScript での活用 | `@module("long/path/to/module")` 属性、`%raw("...")` ブロック、ドキュメントコメント内の URL で長行警告を抑制 |
| 難易度 | 低 `★` |
| 参考プラグイン | 各種 |
| 実装方針 | `LongLineInspectionPolicy` でトークンタイプに基づいて抑制判定 |

---

## 追加機能の優先度サマリー

### S+ (最優先追加) — ReScript 固有の高インパクト

| # | 機能 | 難易度 | 理由 |
|---|------|--------|------|
| 70 | Pipe ⇔ 関数呼び出し変換 (#14-2) | 中 `▲` | ReScript の最頻出パターン |
| 71 | 未使用結果の明示的無視 (#14-4) | 低 `★` | 最も頻出する警告への Quick Fix |
| 72 | 冗長ブロック削除 (#14-14) | 低 `★` | 低コストで実装可能な品質改善 |
| 73 | 識別子ケース修正 (#14-12) | 低〜中 `★` | 命名規則違反の早期検出 |

### A+ (高優先追加) — 生産性向上

| # | 機能 | 難易度 | 理由 |
|---|------|--------|------|
| 74 | パイプチェーン中間型ヒント (#14-1) | 中 `●` | 型推論の可視化 |
| 75 | ラベル付き引数の一括挿入 (#14-3) | 中 `●` | React コンポーネント開発の効率化 |
| 76 | インターフェース公開/非公開 (#14-5) | 中 `★` | .resi 管理の効率化 |
| 77 | Make 関数生成 (#14-10) | 中 `▲` | コンストラクタ生成の自動化 |
| 78 | Switch ケース統合 (#14-7) | 中 `▲` | パターンマッチの最適化 |
| 79 | MultiLang Commenter (#14-17) | 低〜中 `★` | %raw() の UX 改善 |
| 80 | Long Line Inspection Policy (#14-20) | 低 `★` | ノイズ削減 |

### B+ (中優先追加) — あると便利

| # | 機能 | 難易度 | 理由 |
|---|------|--------|------|
| 81 | JSON エンコーダ/デコーダ生成 (#14-6) | 中〜高 `▲` | 頻出ボイラープレートの自動化 |
| 82 | 分割代入の導入/解除 (#14-11) | 中 `▲` | コードスタイル改善 |
| 83 | 型ミスマッチインラインヒント (#14-15) | 中 `●` | エラー理解の向上 |
| 84 | Parameter Info Handler (#14-18) | 中 `●` | ネイティブパラメータ表示 |
| 85 | 型注釈一括追加 (#14-13) | 中 `●` | .resi 準備の効率化 |

### C+ (低優先追加) — nice-to-have or 高難度

| # | 機能 | 難易度 | 理由 |
|---|------|--------|------|
| 86 | React コンポーネント抽出 (#14-8) | 高 `●` | JSX 解析の複雑さ |
| 87 | PPX/マクロ展開ビュー (#14-9) | 高 `●` | コンパイラ連携の複雑さ |
| 88 | モジュールタイプ実装生成 (#14-16) | 高 `●` | module type パースの複雑さ |
| 89 | 使用箇所からの関数生成 (#14-19) | 中〜高 `●` | コンテキスト推論の複雑さ |

---

## カテゴリ 15: 関数型言語プラグイン調査 (Haskell / F# / Scala)

Haskell (HLS + intellij-haskell), F# (Rider + Ionide/FsAutoComplete), Scala (IntelliJ Plugin + Metals) の関数型言語プラグインから発見された、ReScript に特に関連性の高い機能群。

### 15-1. 型ホール / 型ディレクテッド開発支援

| 項目 | 内容 |
|------|------|
| 概要 | プレースホルダ `_` の位置で期待される型を表示し、型に合致するスコープ内の値を補完候補として提示 |
| 参考 | Haskell GHC Typed Holes + HLS Wingman |
| ReScript での活用 | コンパイラの「expected type X but got Y」エラーから期待型を解析し、スコープ内で型が一致する値を Quick Fix として提案。例: `option<string>` が期待される位置で `Some(myString)` を提案 |
| 難易度 | 高 `●` |
| 実装方針 | LSP 診断の型エラーメッセージをパースし、LSP completion と組み合わせて型適合候補をフィルタリング |

### 15-2. ケースの変数分割 (Case Split)

| 項目 | 内容 |
|------|------|
| 概要 | パターンマッチの変数を、その型のすべてのコンストラクタに展開 |
| 参考 | HLS Wingman (destruct tactic), FsAutoComplete (GenerateUnionCases) |
| ReScript での活用 | switch 式内の `_` ワイルドカードや特定の変数を、variant の全コンストラクタに展開。例: `switch x { \| _ => ... }` で `x: option<'a>` なら `\| Some(v) => ... \| None => ...` に展開 |
| 難易度 | 中 `●` |
| 実装方針 | LSP code action で部分的に対応（missing cases 挿入）。より細かい「この変数を分割」は LSP hover で型を取得し、型定義からコンストラクタを列挙して展開 |

### 15-3. 常時型表示パネル (Sticky Type Info)

| 項目 | 内容 |
|------|------|
| 概要 | カーソル位置の式の型を常時表示するツールウィンドウ。カーソル移動に追従してリアルタイム更新 |
| 参考 | intellij-haskell "View sticky type info" |
| ReScript での活用 | 型推論が強力な ReScript で、Ctrl+Shift+P を毎回押さずに型を確認できる。パイプチェーンを辿りながら中間型を継続的に確認 |
| 難易度 | 中 `★` |
| 実装方針 | `ToolWindowFactory` でパネルを作成。`CaretListener` でカーソル移動を監視し、LSP hover を非同期呼び出しして型を表示。既存の `RescriptExpressionTypeProvider` ロジックを再利用 |

### 15-4. デコレータ/属性の補完 (@genType, @module 等)

| 項目 | 内容 |
|------|------|
| 概要 | `@` 入力時に有効なデコレータ名を補完候補として表示（ドキュメント付き） |
| 参考 | HLS pragmas plugin (pragma completion), F# hash directive completion |
| ReScript での活用 | `@genType`, `@module`, `@val`, `@send`, `@get`, `@set`, `@scope`, `@variadic`, `@deriving`, `@react.component`, `@unboxed`, `@tag`, `@as`, `@inline`, `@live` 等のデコレータを補完 |
| 難易度 | 低〜中 `★` |
| 実装方針 | `CompletionContributor` で `@` トークンの後に有効なデコレータ一覧を表示。各デコレータの説明をドキュメントとして添付 |

### 15-5. .resi シグネチャファイルへの宣言同期

| 項目 | 内容 |
|------|------|
| 概要 | .res の宣言変更を .resi に自動同期。宣言の追加/削除/シグネチャ変更を検出し、対応するシグネチャファイルを更新 |
| 参考 | FsAutoComplete (AddBindingToSignatureFile, UpdateValueInSignatureFile, AddTypeAliasToSignatureFile, RenameParamToMatchSignature) |
| ReScript での活用 | `.res` で関数を追加/変更したとき、対応する `.resi` の宣言を自動更新。パラメータ名変更時に `.resi` も同期 |
| 難易度 | 中 `★` |
| 実装方針 | `.res` 保存時に PSI 宣言と対応する `.resi` のテキストを比較。差分がある場合に Quick Fix / 自動修正を提案。LSP の `createInterface` を部分的に活用可能 |

### 15-6. 未使用バインディングのプレフィックス付け

| 項目 | 内容 |
|------|------|
| 概要 | 未使用の変数に `_` プレフィックスを追加して警告を抑制 |
| 参考 | FsAutoComplete (RenameUnusedValue), Haskell (prefix with underscore) |
| ReScript での活用 | `let x = ...` が未使用のとき `let _x = ...` にリネーム。既存の reanalyze Quick Fix を補強 |
| 難易度 | 低 `★` |
| 実装方針 | 未使用変数の診断に対する Quick Fix として、変数名の先頭に `_` を付加 |

### 15-7. Functional Highlighting (関数型ハイライト)

| 項目 | 内容 |
|------|------|
| 概要 | 型ミスマッチ時に、型全体ではなくミスマッチしている部分のみをハイライトし、期待型と実際型の差分を表示 |
| 参考 | Scala IntelliJ Plugin "Functional Highlighting" + Fine-grained Type Diff |
| ReScript での活用 | `expected: array<string> but got: array<int>` のような型エラーで、`string` と `int` の差分部分のみを色分け表示。複雑な型エラーの理解を大幅に改善 |
| 難易度 | 中〜高 `●` |
| 実装方針 | LSP 診断メッセージの「expected/actual」型をパースし、型構造の差分比較アルゴリズムで不一致部分を特定。Inlay Hint として表示 |

### 15-8. コメント内コード評価 (Evaluate in Comments)

| 項目 | 内容 |
|------|------|
| 概要 | コメント内の `>>>` 付きコードを実行し、結果をコメント内にインライン表示 |
| 参考 | HLS eval plugin |
| ReScript での活用 | `// >>> Array.length([1, 2, 3])` → `// 3` のようにドキュメントコメント内で式を評価。教育・プロトタイピング向け |
| 難易度 | 高 `●` |
| 実装方針 | コメント内のコードを抽出 → 一時ファイルに書き出し → `rescript build` → Node.js で実行 → 結果をコメントに挿入 |

### 15-9. map/filter チェーンの変換 (Chain Transformation)

| 項目 | 内容 |
|------|------|
| 概要 | `map` + `filter` のチェーンを `filterMap` / `flatMap` 等のより効率的な単一操作に変換 |
| 参考 | Metals (map/flatMap to for-yield, filter+map to collect) |
| ReScript での活用 | `arr->Array.filter(pred)->Array.map(f)` → `arr->Array.filterMap(x => pred(x) ? Some(f(x)) : None)` への変換。パフォーマンス最適化の提案 |
| 難易度 | 中 `▲` |
| 実装方針 | パイプチェーン内の連続する `Array.map` / `Array.filter` パターンをトークンスキャンで検出し、Intention Action で変換を提案 |

### 15-10. レコードスタブ生成

| 項目 | 内容 |
|------|------|
| 概要 | レコード型の値を作成する際、全フィールドのスタブ（デフォルト値付き）を自動生成 |
| 参考 | FsAutoComplete (GenerateRecordStub), Metals (Implement Abstract Members) |
| ReScript での活用 | `let user: user = { }` と書いた時点で全フィールド `{name: "", age: 0, email: ""}` を自動挿入 |
| 難易度 | 中 `●` |
| 実装方針 | LSP の型情報からレコードフィールド一覧を取得し、型ごとのデフォルト値（string→"", int→0, bool→false, option→None）を生成 |

### 15-11. 不要な修飾子の削除

| 項目 | 内容 |
|------|------|
| 概要 | `open Belt` されている場合に `Belt.Array.map` → `Array.map` のように冗長な修飾子を削除 |
| 参考 | FsAutoComplete (RemoveRedundantQualifier) |
| ReScript での活用 | `open` 文でインポート済みのモジュール名を冗長に書いている場合に簡略化を提案 |
| 難易度 | 中 `●` |
| 実装方針 | スコープ内の `open` 文を解析し、修飾名のプレフィックスが `open` 済みモジュールと一致する場合に削除を提案 |

### 15-12. 不要な括弧の削除

| 項目 | 内容 |
|------|------|
| 概要 | 演算子優先順位から不要と判断できる括弧を削除 |
| 参考 | FsAutoComplete (RemoveUnnecessaryParentheses) |
| ReScript での活用 | `let x = (1 + 2)` → `let x = 1 + 2`、`if (cond) { ... }` の条件部の括弧が不要な場合に削除を提案 |
| 難易度 | 中 `▲` |
| 実装方針 | 括弧で囲まれた式の前後のコンテキスト（演算子優先順位）を解析し、括弧なしでも同じ意味になる場合に削除を提案 |

### 15-13. Worksheet モード (インラインコード評価)

| 項目 | 内容 |
|------|------|
| 概要 | `.worksheet.res` ファイルで式を書くと、各式の右側に評価結果がリアルタイム表示される |
| 参考 | Scala Worksheet, F# Interactive (FSI), Haskell GHCi |
| ReScript での活用 | ReScript のコードを書きながら右側に結果を確認。学習・実験・プロトタイピングに最適 |
| 難易度 | 高 `●` |
| 実装方針 | 専用ファイルタイプ `.worksheet.res` を定義。保存時に `rescript build` + Node.js 実行し、各式の結果をインレイヒントとして右側に表示 |

### 15-14. C# → F# ラムダ変換の ReScript 版 (JS → ReScript 変換)

| 項目 | 内容 |
|------|------|
| 概要 | JavaScript/TypeScript コードをペーストした際に ReScript 構文に自動変換 |
| 参考 | FsAutoComplete (ConvertCSharpLambdaToFSharpLambda) |
| ReScript での活用 | `const foo = (x) => x + 1` → `let foo = x => x + 1`、`arr.map(x => x * 2)` → `arr->Array.map(x => x * 2)` |
| 難易度 | 中〜高 `▲` |
| 実装方針 | `CopyPastePostProcessor` でペーストされたテキストが JavaScript っぽいかを判定し、正規表現ベースで基本的な変換を適用。完全な変換は困難だが、頻出パターンのみ対応 |

### 15-15. 演算子優先順位のホバー表示

| 項目 | 内容 |
|------|------|
| 概要 | 演算子にカーソルを合わせた際に、優先順位と結合性を表示 |
| 参考 | HLS explicit-fixity-plugin |
| ReScript での活用 | `->` (パイプ), `++` (文字列連結), `+.` (浮動小数点加算), `==` vs `===` 等の優先順位表示。特に `->` と `\|>` の違いの理解に有用 |
| 難易度 | 低 `★` |
| 実装方針 | 演算子トークンの hover にハードコードされた優先順位テーブルから情報を追加 |

### 15-16. 型シグネチャで関数を検索 (Type-directed Search)

| 項目 | 内容 |
|------|------|
| 概要 | 「`string -> int` な関数はどれ？」のように型シグネチャで関数を検索 |
| 参考 | Haskell Hoogle, Scala Metals (type-directed completion) |
| ReScript での活用 | 「`array<'a> -> int` な関数」で `Array.length` を発見。標準ライブラリの API 探索に非常に有用 |
| 難易度 | 非常に高 `●` |
| 実装方針 | `.resi` ファイルの型シグネチャをインデックス化し、型パターンマッチで検索。簡易版としてテキスト検索から開始 |

### 15-17. 変更可能性の診断 (Mutability Suggestion)

| 項目 | 内容 |
|------|------|
| 概要 | 変更されない `ref` を `let` に変換する提案。逆に変更が必要な `let` を `ref` にする提案 |
| 参考 | FsAutoComplete (MakeDeclarationMutable), Scala Plugin (private var → val inspection) |
| ReScript での活用 | 変更されない `ref(0)` を通常の `let` に簡略化する提案。逆に後で変更される値に `ref` の使用を提案 |
| 難易度 | 中 `●` |
| 実装方針 | LSP 診断と使用箇所分析を組み合わせて `:=` の有無で `ref` の必要性を判定 |

### 15-18. 位置引数 → ラベル付き引数変換

| 項目 | 内容 |
|------|------|
| 概要 | 位置指定の引数をラベル付き引数（名前付き引数）形式に変換 |
| 参考 | Metals (Convert To Named Arguments), FsAutoComplete (ConvertPositionalDUToNamed) |
| ReScript での活用 | `foo(1, "hello", true)` → `foo(~id=1, ~name="hello", ~active=true)` の変換。コードの可読性向上 |
| 難易度 | 中 `●` |
| 実装方針 | LSP の signature help からパラメータ名を取得し、位置引数に名前を付加 |

### 15-19. スタイルリンティング (Idiomatic ReScript Suggestions)

| 項目 | 内容 |
|------|------|
| 概要 | ReScript のイディオムに反するコードパターンを検出し、より慣用的な書き方を提案 |
| 参考 | HLS + HLint, FsAutoComplete + FSharpLint, Scala + Scalastyle |
| ReScript での活用例 | (a) ネストした関数呼び出し → パイプ使用を提案、(b) 単一ケースの switch → if への簡略化、(c) `switch { \| Some(x) => x \| None => default }` → `Option.getOr(default)` への変換、(d) `Js.Array2` → `Array` モジュールへの移行提案、(e) レコードフィールドのショートハンド `{name: name}` → `{name}` |
| 難易度 | 中〜高 `▲` |
| 実装方針 | `LocalInspectionTool` で各パターンを個別のインスペクションとして実装。テキストパターンマッチで頻出パターンから開始 |

### 15-20. Implicit/Contextual 解決の可視化

| 項目 | 内容 |
|------|------|
| 概要 | 暗黙的に解決される引数や変換を可視化するモード |
| 参考 | Scala IntelliJ Plugin "Show implicit hints" (Ctrl+Alt+Shift+=), X-Ray Mode |
| ReScript での活用 | ReScript には Scala の implicit はないが、類似概念として: (a) JSX の props spread `...` で渡される値の展開表示、(b) `@react.component` で自動生成される `make` 関数のシグネチャ表示、(c) パイプ `->` の第1引数の挿入位置の可視化 |
| 難易度 | 中 `●` |
| 実装方針 | InlayHintsProvider で特定の構文パターン（JSX spread, pipe第1引数, PPX 変換）にヒントを表示するトグル機能 |

---

## 関数型言語調査の追加優先度サマリー

### FP-S (最優先) — 関数型言語固有の高インパクト

| # | 機能 | 難易度 | 理由 |
|---|------|--------|------|
| 90 | デコレータ補完 (#15-4) | 低〜中 `★` | 低コスト・高頻度の利用 |
| 91 | 未使用バインディングのプレフィックス (#15-6) | 低 `★` | 頻出パターンの Quick Fix |
| 92 | 演算子優先順位のホバー表示 (#15-15) | 低 `★` | ハードコードで即実装可能 |

### FP-A (高優先) — 関数型開発の生産性向上

| # | 機能 | 難易度 | 理由 |
|---|------|--------|------|
| 93 | 常時型表示パネル (#15-3) | 中 `★` | 型推論中心の開発に必須級 |
| 94 | .resi シグネチャ同期 (#15-5) | 中 `★` | .res/.resi ペア管理の効率化 |
| 95 | ケースの変数分割 (#15-2) | 中 `●` | パターンマッチ中心の開発に有用 |
| 96 | レコードスタブ生成 (#15-10) | 中 `●` | ボイラープレート削減 |
| 97 | map/filter チェーン変換 (#15-9) | 中 `▲` | パイプチェーンの最適化 |
| 98 | 位置引数 → ラベル付き引数 (#15-18) | 中 `●` | 可読性向上 |

### FP-B (中優先) — あると便利

| # | 機能 | 難易度 | 理由 |
|---|------|--------|------|
| 99 | 型ミスマッチ差分表示 (#15-7) | 中〜高 `●` | 型エラー理解の改善 |
| 100 | 不要な括弧の削除 (#15-12) | 中 `▲` | コードクリーンアップ |
| 101 | 不要な修飾子の削除 (#15-11) | 中 `●` | open 活用の改善 |
| 102 | スタイルリンティング (#15-19) | 中〜高 `▲` | イディオム教育 |
| 103 | 変更可能性の診断 (#15-17) | 中 `●` | ref 使用の最適化 |
| 104 | JS → ReScript 変換 (#15-14) | 中〜高 `▲` | 移行支援 |

### FP-C (低優先) — 高難度 or ニッチ

| # | 機能 | 難易度 | 理由 |
|---|------|--------|------|
| 105 | 型ホール支援 (#15-1) | 高 `●` | コンパイラ対応が必要 |
| 106 | コメント内コード評価 (#15-8) | 高 `●` | コンパイル+実行パイプラインが複雑 |
| 107 | Worksheet モード (#15-13) | 高 `●` | 同上 |
| 108 | 型シグネチャ検索 (#15-16) | 非常に高 `●` | 型インデックスの構築が困難 |
| 109 | Implicit 可視化 (#15-20) | 中 `●` | ReScript での適用範囲が限定的 |

---

## 推奨実装バッチ

### Batch 1: Quick Wins（2〜3日）
優先度 S の低難易度機能を一括実装:
- Bundled Dictionary (#5)
- Test Source Filter (#7)
- Context Info (#6)
- Element Description Provider (#34)
- Problem Highlight Filter (#17)

### Batch 2: 編集 UX 強化（3〜5日）
日常的な編集操作を改善:
- Unwrap/Remove (#1)
- Typed Handler (#4)
- Enter Handler (#10)
- Join Lines (#13)

### Batch 3: ナビゲーション強化（3〜5日）
コード探索を効率化:
- Go to Test / Create Test (#2)
- FindUsagesProvider + WordsScanner (#8)
- Goto Super (#20)
- Run Anything Provider (#19)

### Batch 4: プロジェクトビュー（2〜3日）
プロジェクト表示を改善:
- Tree Structure Provider (#3)
- Project View Node Decorator (#31)

### Batch 5: 補完・分析（3〜5日）
補完品質とコード分析を改善:
- Completion Confidence (#14)
- Live Template Context + Macros (#15, #16)
- Extend/Shrink Word Selection (#9)
- Expression Type Info (#11)
- Highlight Usages (#12)
