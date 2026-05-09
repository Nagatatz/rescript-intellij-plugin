# Cross-file Variant Constructor Rename — Requirements

## 背景

`refactor/RescriptRenameHandler` は Shift+F6 → LSP `textDocument/rename` で variant constructor のリネームをカバーしている (rescript-language-server が型情報を使って正確に書き換える)。一方、LSP が起動していない環境 (Node.js が PATH に無い、`@rescript/language-server` 未インストール、ネットワーク制限下、`docs/lsp-fallback-matrix.md` 参照) ではリネームが完全に止まる。

本機能は **LSP 非依存の constructor-only rename intention** を追加する。エディタで variant constructor (UIDENT) にキャレットを置いて Alt+Enter を押すと「Rename Variant Constructor」が表示され、プロジェクト全体の出現箇所をトークンヒューリスティックで集計し、ダイアログで新名を入力するだけで一括置換できる。

既存資産:

- `intention/RescriptAddMissingSwitchArmsIntention`: switch arm に対する Alt+Enter intention の前例
- `impact/RescriptTypeImpactScanner` + `RescriptReferenceClassifier`: PsiSearchHelper word-index で UIDENT を集めてトークン分類する前例
- `narrowing/RescriptSwitchArmCollector`: switch arm の lexer ベース解析 (or-pattern を含む) の前例
- `lang/RescriptTokenTypes`: `UIDENT` / `LPAREN` / `PIPE` 等の token type が揃っている

## ユーザーストーリー

### US-01: LSP 非依存の constructor リネーム

**LSP が無効 / 未起動な ReScript 開発者として**、`switch` arm の `| Foo(_) => …` や `type t = | Foo | Bar` で `Foo` にキャレットを置いて Alt+Enter を押すと、**プロジェクト全体の `Foo` の出現箇所** (constructor として使われているもの) を一括リネームする intention を出してほしい。

**受け入れ条件:**

- [ ] caret が UIDENT (variant constructor 名) 上にあるとき、Intention "Rename variant constructor `<Name>` …" が Alt+Enter メニューに表示される
- [ ] caret が UIDENT 以外 (`let`, `module`, 数値, 文字列, etc.) のときは表示されない
- [ ] Intention 起動 → 新名入力ダイアログ → confirm で全プロジェクトを書き換え (write action 1 つにまとめる)
- [ ] 新名のバリデーション: 大文字始まり、英数字 / `_` のみ、空文字や既存名と同じなら no-op
- [ ] LSP 未起動でも動作する (本 intention は LSP 呼び出しを行わない)
- [ ] LSP rename ハンドラは既存どおり Shift+F6 で動作する (本 intention とは独立)

### US-02: トークンヒューリスティックによる constructor 識別

**保守者として**、UIDENT のうち「変数構築または pattern として使われている」もののみを書き換え対象とし、JSX 要素 (`<Foo />`)・モジュール参照のうち constructor 文脈に該当しないもの・JS interop 文字列内の同名トークンを誤って書き換えないようにしたい。

**受け入れ条件:**

- [ ] `RescriptConstructorOccurrenceClassifier` を pure object として切り出し、トークン位置から `Constructor / Pattern / Module-qualified-tail / Other` を返す
- [ ] **Constructor**: UIDENT が `(` の直前 (例: `Foo(x)`) または、行頭・カンマ・パイプ直後で `(` を伴わない (例: `let x = Foo`)
- [ ] **Pattern**: UIDENT が `|` の直後にあり `=>` までに到達する (例: `| Foo(_) => ...`, `| Foo => ...`)
- [ ] **Module-qualified-tail**: UIDENT の直前が `.` (例: `Module.Foo`) — リネーム対象は末尾の `Foo` のみ、`Module` は触らない
- [ ] **Other**: 上記いずれでもない (型注釈中の UIDENT が型名を指す場合、JS interop 文字列内など) — 書き換え対象外
- [ ] 文字列リテラル / コメント内の UIDENT は対象外 (lexer の token kind で除外)

### US-03: 上限と確認 UI

**ユーザーとして**、想定外の大量書き換えを避けるため、対象件数を事前に確認したい。

**受け入れ条件:**

- [ ] Intention は対象件数を集計した後、`<N> occurrences across <M> files. Rename all?` のダイアログで確認を求める
- [ ] `Cancel` で中止、`OK` で全置換
- [ ] 対象上限はソフトキャップ 500 件 (超過時は「対象が多すぎます。LSP rename (Shift+F6) を使うか、対象を絞ってください」のメッセージで中止)

## スコープ外

- プレビュー UI (`UsageView` ベースの個別チェックボックス) — v2 以降
- ポリモーフィックバリアント (`#Foo`) のリネーム — 別 intention で対応
- `type` 宣言と arm の不一致 (異なる型の同名 constructor) の自動分離 — v1 では混同を許容し、ユーザーがプレビュー (将来)・確認ダイアログで判断
- LSP rename と本 intention の自動切り替え — それぞれ独立、ユーザー選択
- module-qualified call site の `Module.Foo` 部分のうち、`Foo` の所属モジュールが現在の type 宣言と同じかの厳密チェック — v1 はトークン形だけで判定
- インターフェイス (`.resi`) と実装 (`.res`) の双方向同期更新 — LSP rename を使う方が安全

## 機能カテゴリ

- Intention (新規 Alt+Enter)
- リファクタリング (LSP 非依存)
