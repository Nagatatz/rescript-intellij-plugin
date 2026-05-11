# 設計: ReScriptLexer (Pygments)

## アプローチ概要

`pygments.lexer.RegexLexer` ベースの新規 lexer を `pygments/lexers/rescript.py` として実装する。`pygments/lexers/ml.py` の `ReasonLexer` を構造の参考にするが、別ファイル・別クラスとして独立させ、コード共有はしない（Reason と ReScript のメンテナンスを互いに引きずらないため）。

本リポジトリの `src/main/java/com/rescript/plugin/lang/Rescript.flex` を権威ある token 定義として参照し、JFlex の `%state` 駆動を Pygments の `tokens` dict + `#push` / `#pop` / 名前付き state で表現する。JFlex の貪欲一致と Pygments の正規表現順序評価の挙動差に注意する。

## ファイル構成（Pygments 側）

```
pygments/
├── lexers/
│   ├── _mapping.py              # 1 行追加
│   └── rescript.py              # 新規。ReScriptLexer 本体
tests/
├── examplefiles/
│   └── rescript/
│       ├── example.res          # 網羅サンプル
│       └── example.res.output   # 期待トークン列
└── snippets/
    └── rescript/
        ├── keyword.txt
        ├── template-string.txt
        ├── jsx.txt
        ├── decorator.txt
        ├── variant-constructor.txt
        ├── nested-comment.txt
        ├── pipe-first.txt
        └── type-variable.txt
CHANGES                          # 追記
AUTHORS                          # 追記
```

## トークンマッピング

| ReScript 言語要素 | Pygments Token | 備考 |
|------------------|----------------|------|
| `let`, `module`, `type`, `external`, `open`, `include`, `exception`, `mutable`, `rec`, `and`, `lazy`, `assert` | `Keyword.Declaration` | |
| `if`, `else`, `switch`, `when`, `for`, `while`, `to`, `downto`, `try`, `catch`, `throw`, `return` | `Keyword` | |
| `true`, `false`, `unit`, `Some`, `None`, `Ok`, `Error`, `list{}` | `Keyword.Constant` | |
| 数値リテラル（int / float / hex / oct / bin / `_` separator） | `Number.Integer` / `Number.Float` / `Number.Hex` / `Number.Oct` / `Number.Bin` | |
| 通常文字列 `"..."` | `String.Double` | エスケープは `String.Escape` |
| テンプレート文字列 `` `...` `` | `String.Backtick` | `${...}` 中の式は `interpolation` state へ push |
| 文字リテラル `'a'` | `String.Char` | 型変数 `'a` と衝突するため正規表現の優先順位に注意 |
| 行コメント `//` | `Comment.Single` | |
| ブロックコメント `/* */` | `Comment.Multiline` | ネスト対応（`block-comment` state を再帰 push） |
| ドキュメントコメント `/** */` | `Comment.Special` | |
| デコレータ `@react.component`, `@bs.module`, `@deriving` | `Name.Decorator` | 旧 Reason 構文 `[@...]` も lexer が壊れない範囲で許容 |
| 大文字始まり識別子（Module / Variant Constructor） | `Name.Class` | static lexer では Module と Variant を文脈分離できないため近似 |
| 小文字始まり識別子 | `Name` | 関数名・束縛名・型名すべて含む |
| 型変数 `'a`, `'b`, `'_a` | `Name.Variable` | 文字リテラルとの優先順位を慎重に設計 |
| 演算子 `=>`, `->`, `\|>`, `<-`, `::`, `..`, `...`, `&&`, `\|\|`, `==`, `===`, `!=`, `!==` | `Operator` | `=>` / `->` / `\|>` を最長一致で先に match |
| パンクチュエーション `{ } [ ] ( ) , ; :` | `Punctuation` | |
| JSX `<Foo>`, `</Foo>`, `<Foo />` | `Name.Tag` | 属性名 = `Name.Attribute`、属性値の `{...}` は root state へ戻る |
| `%raw(...)`, `%re(...)`, `%bs.raw(...)` | `Comment.Preproc` 接頭辞 + 中身は文字列扱い | DelegatingLexer は初版では使わない |

## State 設計

```python
tokens = {
    'root': [
        # 1. 空白
        (r'\s+', Text),
        # 2. コメント (block より line を先に判定すると // と /* で誤認しない)
        (r'/\*\*(?!/)', Comment.Special, 'doc-comment'),
        (r'/\*', Comment.Multiline, 'block-comment'),
        (r'//.*?$', Comment.Single),
        # 3. テンプレート文字列
        (r'`', String.Backtick, 'template-string'),
        # 4. 通常文字列
        (r'"', String.Double, 'string'),
        # 5. 文字リテラル (型変数より厳密なパターンで先取り)
        (r"'(?:\\.|[^\\'])'", String.Char),
        # 6. 型変数
        (r"'[a-z_][a-zA-Z0-9_]*", Name.Variable),
        # 7. 数値
        (r'0[xX][0-9a-fA-F_]+', Number.Hex),
        (r'0[oO][0-7_]+', Number.Oct),
        (r'0[bB][01_]+', Number.Bin),
        (r'\d[\d_]*\.[\d_]*([eE][+-]?\d+)?', Number.Float),
        (r'\d[\d_]*', Number.Integer),
        # 8. デコレータ
        (r'@[a-zA-Z_][a-zA-Z0-9_.]*', Name.Decorator),
        # 9. キーワード
        (r'\b(let|module|type|...)\b', Keyword.Declaration),
        (r'\b(if|else|switch|...)\b', Keyword),
        (r'\b(true|false|Some|None|Ok|Error|unit)\b', Keyword.Constant),
        # 10. JSX タグ開始（識別子の前に `<` + 大文字 or 小文字タグ）
        (r'</?[a-zA-Z][a-zA-Z0-9_.]*', Name.Tag, 'jsx-tag'),
        # 11. 識別子
        (r'[A-Z][a-zA-Z0-9_]*', Name.Class),
        (r'[a-z_][a-zA-Z0-9_]*', Name),
        # 12. 演算子（長いものから順に）
        (r'(=>|->|\|>|<-|::|\.\.\.|\.\.|&&|\|\||===|!==|==|!=|<=|>=)', Operator),
        (r'[+\-*/=<>!&|^~?]', Operator),
        # 13. パンクチュエーション
        (r'[{}()\[\],;:]', Punctuation),
    ],
    'block-comment': [
        (r'[^/*]+', Comment.Multiline),
        (r'/\*', Comment.Multiline, '#push'),  # ネスト
        (r'\*/', Comment.Multiline, '#pop'),
        (r'[/*]', Comment.Multiline),
    ],
    'doc-comment': [
        (r'[^/*]+', Comment.Special),
        (r'\*/', Comment.Special, '#pop'),
        (r'[/*]', Comment.Special),
    ],
    'string': [
        (r'[^\\"]+', String.Double),
        (r'\\.', String.Escape),
        (r'"', String.Double, '#pop'),
    ],
    'template-string': [
        (r'[^`\\$]+', String.Backtick),
        (r'\\.', String.Escape),
        (r'\$\{', String.Interpol, 'interpolation'),
        (r'\$', String.Backtick),
        (r'`', String.Backtick, '#pop'),
    ],
    'interpolation': [
        (r'\}', String.Interpol, '#pop'),
        include('root'),  # root のルールを再帰利用
    ],
    'jsx-tag': [
        (r'\s+', Text),
        (r'/?>', Name.Tag, '#pop'),
        (r'[a-zA-Z_][a-zA-Z0-9_-]*', Name.Attribute),
        (r'=', Operator),
        (r'"[^"]*"', String.Double),
        (r'\{', Punctuation, 'interpolation'),
    ],
}
```

ネストブロックコメントは Pygments の state stack を `#push` で再帰させるだけで自然に表現できる。Reason の `[@...]` 形式は decorator 正規表現を `@?\[?@...` と緩めるのではなく、別ルールとして root に追加する。

## 既存 ReasonLexer との差異

| 項目 | Reason | ReScript | 対応方針 |
|------|--------|----------|---------|
| デコレータ構文 | `[@bs.module]` 形式が主 | `@bs.module` / `@react.component` 形式が主 | ReScript モダン構文を主軸にしつつ、旧形式も受理 |
| パイプ | `\|>` のみ | `->`（パイプファースト）と `\|>`（パイプラスト）両方 | `->` を Operator として明示的にハイライト |
| switch / match | `switch x { ... }` | `switch x { ... }` | 同じ扱い |
| JSX | 大文字始まりタグ | 大文字 / 小文字どちらも | 小文字タグも `Name.Tag` として受理 |
| `list{...}` | なし（リストは `[...]`） | `list{...}` 専用構文 | `list` を `Keyword.Constant` として処理 |
| テンプレートリテラル | 限定的 | 一般的（`${...}` 補間あり） | template-string state を独立に持つ |

Reason と ReScript で `aliases` が衝突しないよう、ReScript は `['rescript', 'res']` のみ。`reason` / `re` は Reason に予約。

## テスト戦略

### Pygments 標準テスト

1. **`tests/test_basic_api.py`**: 自動収集される。lexer が `LEXERS` に登録されていれば「インスタンス化できる」「`get_tokens('')` がクラッシュしない」「`example.res` が tokenize できる」が自動的に走る
2. **`tests/snippets/rescript/*.txt`**: Pygments の snippet test format に従い、入力と期待出力を 1 ファイルに記述。`pytest tests/test_snippets.py -k rescript` で実行
3. **`tests/examplefiles/rescript/example.res`**: 網羅的サンプル。本リポジトリの `src/test/resources/` 配下の `.res` テストフィクスチャから flake が少ないものを抽出して流用する
4. **目視 HTML 確認**: `python -m pygments -l rescript -f html -O full sample.res > out.html` で生成し、ブラウザで確認

### ローカル統合検証（本リポジトリ側）

- `sphinx-docs/_ext/rescript_pygments.py` で開発中の lexer を一時登録
- `sphinx-docs/user/features/*.md` に既存の ` ```rescript ` コードフェンスがあるはずなので、`make build-en` でビルドして HTML を確認
- 日本語版も `make build-ja` で同様に確認

## ローカル Sphinx 統合の段階

Pygments の PR がマージ・リリースされる前に、本リポジトリ側で先行検証する仕組みを以下のとおり用意する:

1. **開発期** — fork した Pygments を `pip install -e <fork-path>` でインストールし、`sphinx-docs/conf.py` の `pygments_style` で動作確認
2. **PR 提出後** — `sphinx-docs/_ext/rescript_pygments.py` を新規追加。`setup(app)` で `app.add_lexer('rescript', ReScriptLexer)` を呼ぶ。これにより fork なしで本リポジトリのみで lexer を有効化できる
3. **Pygments 公式リリース後** — `_ext/rescript_pygments.py` を削除し、`sphinx-docs/pyproject.toml` の `pygments` 依存を「ReScript lexer が含まれる最低バージョン」に引き上げ

`_ext/rescript_pygments.py` は「Pygments PR と同期する mirror」とコメント冒頭で明記し、参照元コミット SHA を残す。

## リスク

| リスク | 影響 | 対応 |
|-------|------|------|
| Pygments メンテナのレスポンス遅延 | スケジュール遅延 | Issue 7 営業日応答なしで PR 先行送付 |
| Reason との混同レビュー指摘 | レビュー往復増 | PR 本文で `ReasonLexer` と独立した理由を明記（パイプファースト・`list{}` 構文等） |
| JSX ネスト時の state pop ミス | ハイライト崩れ | snippets で「JSX 内 switch」「JSX 内テンプレートリテラル」を意図的に検証 |
| 文字リテラル `'a'` と型変数 `'a` の優先順位ミス | ハイライト崩れ | 文字リテラルを `'(?:\\.|[^\\'])'` の厳密パターンで先に match、型変数は `'[a-z_]` で続ける |
| ReScript v12 構文変更 | 将来の再対応 | PR 本文でスコープを v11 と明記 |
| ローカル lexer 拡張が PR マージ前に陳腐化 | sphinx-docs が壊れる | `_ext/` のコメントに参照 commit を記載、PR 更新と同期で都度 mirror |
| `_mapping.py` の自動再生成スクリプト存在の見落とし | PR で手動編集を指摘される | CONTRIBUTING を初手で確認、`make mapfiles` 等のコマンドを使う |

## 完成イメージ

PR マージ後の最終状態:

- Pygments 公式: `from pygments.lexers.rescript import ReScriptLexer` で import 可能
- Sphinx: ` ```rescript ` / ` ```res ` フェンスが何の設定もなく自動カラーリング
- 本リポジトリ `sphinx-docs/`: Pygments 依存を最低バージョン指定するのみ。カスタム拡張不要
- README / CLAUDE.md: 「Pygments に upstream 済み」とアナウンス
