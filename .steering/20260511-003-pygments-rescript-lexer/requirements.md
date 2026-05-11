# 要求: ReScript Lexer の Pygments への upstream 貢献

## 背景

Sphinx のシンタックスハイライトは Pygments に委譲されている。現状の Pygments には ReScript 専用 lexer が存在せず、Pygments を経由してコードフェンスをハイライトする全ドキュメントツール（Sphinx・MkDocs・Hugo の旧 highlight・docutils など）で ` ```rescript ` フェンスがプレーンテキスト描画になっている。本リポジトリの `sphinx-docs/` でも同じ問題があり、ユーザー向けドキュメントのコードサンプルが読みにくい。

Pygments 本家に ReScript lexer を upstream すれば、Sphinx 側で個別設定なしに ` ```rescript ` / ` ```res ` フェンスが自動カラーリングされ、ReScript エコシステム全体のドキュメント体験が底上げされる。

参考: Pygments には既に `ReasonLexer` (`pygments/lexers/ml.py`) が存在するが、ReScript は中括弧スタイル・JSX・テンプレートリテラル・`->` パイプファースト・`@react.component` 形式のデコレータなど、Reason と異なる構文要素を多数持つため、別 lexer として実装するのが妥当。

## 目的

1. Pygments 本家リポジトリ（https://github.com/pygments/pygments）に `ReScriptLexer` を追加する PR を提出し、マージしてもらう
2. PR の提案 → 議論 → 実装 → マージ → 本リポジトリ側 sphinx-docs の Pygments 公式版切り替え、までの長期作業を本ステアリングで一貫管理する
3. Pygments リリースを待つ間、本リポジトリ `sphinx-docs/` にローカル lexer 拡張を一時的に登録し、実運用での検証データを蓄積する

## 受け入れ条件

- [ ] pygments/pygments に「ReScript lexer 追加提案」の Issue を起票し、URL を `.steering/20260511-003-pygments-rescript-lexer/upstream-status.md` に記録する
- [ ] Issue 上でメンテナの welcome 返答を得る、または 7 営業日応答なしの場合は PR 先行送付に切り替える判断を記録する
- [ ] `pygments/lexers/rescript.py`（新規）に `ReScriptLexer` を実装し、`pygments/lexers/_mapping.py` に登録する
- [ ] `aliases = ['rescript', 'res']`、`filenames = ['*.res', '*.resi']`、`mimetypes = ['text/x-rescript']` を定義する
- [ ] `tests/examplefiles/rescript/` に網羅的な代表 `.res` ファイルを 1 件以上追加する
- [ ] `tests/snippets/rescript/` に 6〜10 件の lexer 出力検証スニペットを追加する（keyword / decorator / template literal + 補間 / JSX / variant / ネストコメント / `->` パイプ / type 変数 を含む）
- [ ] `CHANGES` および `AUTHORS` を更新する
- [ ] pygments/pygments に PR を提出し、レビューコメントに対応してマージされる
- [ ] Pygments の新リリースバージョンを記録し、本リポジトリ `sphinx-docs/` の Pygments 最低バージョンをそのバージョンに引き上げる
- [ ] 本リポジトリ `sphinx-docs/_ext/rescript_pygments.py`（ローカル lexer 拡張）を Pygments 公式版リリース後に削除する

## スコープ外

- `ReasonLexer` の修正・更新（既存 `pygments/lexers/ml.py` の `ReasonLexer` は別物として touch しない）
- ReScript v12 以降の未確定構文への対応（v11 安定版を対象とする）
- JSX 式の完全パース（Pygments は lexer であり parser ではないため、トークンレベルの近似に留める）
- MkDocs / Hugo / GitHub Linguist など他ツールへの直接的なロビー活動（Pygments がマージされれば自動的に波及する範囲のみ恩恵を受ける）
- `%raw(...)` / `%re(...)` 内部の JavaScript / 正規表現の再帰ハイライト（Pygments の `DelegatingLexer` を使えば可能だが、初版ではスコープ外）
- 本リポジトリ側プラグインのコード変更（`Rescript.flex` 等は参照ソースとしてのみ使う）

## 関連 Issue / PR / 過去事例

- 既存 `ReasonLexer` 追加 PR の経緯を `.steering/20260511-003-pygments-rescript-lexer/reason-lexer-notes.md` に調査結果として残す
- Pygments の lexer 追加ガイドライン: `doc/docs/lexerdevelopment.rst`
