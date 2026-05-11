# タスクリスト: ReScript Lexer の Pygments への upstream 貢献

各セクションは独立した進捗マイルストーンとして設計している。セクション 1〜2 と 6〜7 は本リポジトリへのコミットを伴う。セクション 3〜5 は Pygments fork での作業であり、本リポジトリへの commit は発生しない（進捗メモのみ `.steering/` 内に残す）。

## 依存関係

- セクション 1（事前調査）→ セクション 2（Issue 起票）→ セクション 3（実装）→ セクション 4（テスト）→ セクション 5（PR 提出・マージ）
- セクション 6（sphinx-docs ローカル統合）はセクション 3 完了時点で先行着手可能
- セクション 7（クローズアウト）は他全セクション完了後

## セクション 1: 事前調査と Pygments ガイドライン把握

- [ ] Pygments の `CONTRIBUTING.md` および `doc/docs/lexerdevelopment.rst` を読む
- [ ] 既存 `ReasonLexer` (`pygments/lexers/ml.py`) の実装と過去 PR の議論を読み、要点を `.steering/20260511-003-pygments-rescript-lexer/reason-lexer-notes.md` に残す
- [ ] Pygments の `_mapping.py` 自動生成スクリプト（`make mapfiles` など）の有無を確認する
- [ ] Pygments のテストフレームワーク（`tests/test_basic_api.py` / `tests/test_snippets.py` / `tests/test_examplefiles.py`）の動かし方を確認する
- [ ] 本リポジトリの `Rescript.flex` から ReScript の token 表を抽出し、`.steering/20260511-003-pygments-rescript-lexer/token-table.md` に整理する
- [ ] 上記調査メモを `📝 Add ReScript-Pygments contribution research notes` でコミット

## セクション 2: Pygments 本家への Issue 起票

- [ ] pygments/pygments で「Add ReScript lexer」Issue を作成する
- [ ] Issue 本文に: 動機 / ReasonML との差異 / 想定 token 表概要 / PR 提出の意向 を記載
- [ ] Issue URL を `.steering/20260511-003-pygments-rescript-lexer/upstream-status.md` に記録
- [ ] 7 営業日返信を待ち、応答内容（または無応答）に基づいて方針を記録
- [ ] `📝 Track Pygments Issue for ReScript lexer` でコミット

## セクション 3: Pygments fork での lexer 実装

本セクションは Pygments fork 上での作業であり、本リポジトリには commit を作らない。

- [ ] pygments/pygments を GitHub fork し、`pygments-rescript-lexer` ブランチをローカルクローン上に作成
- [ ] `pygments/lexers/rescript.py` を新規作成し `ReScriptLexer` を実装（design.md の State 設計に従う）
- [ ] `pygments/lexers/_mapping.py` に登録（手動編集が必要な場合）または `make mapfiles` で再生成
- [ ] `aliases = ['rescript', 'res']` / `filenames = ['*.res', '*.resi']` / `mimetypes = ['text/x-rescript']` を定義
- [ ] `pytest tests/test_basic_api.py` で lexer が登録され、空入力でクラッシュしないことを確認
- [ ] 進捗を `.steering/20260511-003-pygments-rescript-lexer/upstream-status.md` に追記

## セクション 4: Pygments 側テスト追加

本セクションも Pygments fork 上での作業。

- [ ] `tests/examplefiles/rescript/example.res` を追加（本リポジトリのテストフィクスチャから網羅的サンプルを移植）
- [ ] `tests/examplefiles/rescript/example.res.output` を Pygments のテストヘルパで生成
- [ ] `tests/snippets/rescript/` に以下のスニペットを追加:
  - [ ] `keyword.txt` — `let` / `module` / `type` / `switch` / `if`
  - [ ] `template-string.txt` — `` `hello ${name}` `` の interpolation
  - [ ] `jsx.txt` — `<Foo bar={x} />` / `<Foo>...</Foo>`
  - [ ] `decorator.txt` — `@react.component` / `@bs.module("...")`
  - [ ] `variant-constructor.txt` — `Some(x)` / `Ok(value)` / `MyVariant(int, string)`
  - [ ] `nested-comment.txt` — `/* a /* b */ c */`
  - [ ] `pipe-first.txt` — `arr->Array.map(f)`
  - [ ] `type-variable.txt` — `let id: 'a => 'a = x => x`
- [ ] `pytest tests/ -k rescript` で全件パスを確認
- [ ] HTML 出力を `python -m pygments -l rescript -f html sample.res` で生成し目視確認

## セクション 5: PR 提出と追跡

本セクションも Pygments fork 上での作業（PR マージ管理）。

- [ ] `CHANGES` ファイルに `Added a lexer for ReScript (PR #NNNN)` を追記
- [ ] `AUTHORS` に貢献者として追記
- [ ] pygments/pygments に PR 提出（Issue を `Closes #NNN` でリンク、本文に動機・スコープ・スコープ外を明記）
- [ ] PR URL を `.steering/20260511-003-pygments-rescript-lexer/upstream-status.md` に追記
- [ ] レビューコメント対応 → 再 push のサイクルを記録
- [ ] PR マージ後、Pygments の次回リリースバージョンと日付を `upstream-status.md` に記録
- [ ] 本リポジトリで `📝 Record Pygments ReScript lexer merge` をコミット（upstream-status.md 更新分）

## セクション 6: 本リポジトリ sphinx-docs への先行統合

セクション 3 完了時点で着手可。Pygments PR マージを待たずに進められる。

- [ ] `sphinx-docs/_ext/__init__.py` を作成（既存なら不要）
- [ ] `sphinx-docs/_ext/rescript_pygments.py` を新規追加（ローカル lexer 拡張、Pygments PR と同期する mirror。冒頭コメントに参照 commit SHA を記載）
- [ ] `sphinx-docs/_ext/rescript_pygments.py` のテストを `src/test/` ではなく `sphinx-docs/tests/` 配下（または `_ext/test_rescript_pygments.py`）に追加
- [ ] `sphinx-docs/conf.py` の `extensions` に `_ext.rescript_pygments` を追加（パス調整含む）
- [ ] `sphinx-docs/user/features/*.md` の既存 ` ```rescript ` フェンスがハイライトされることを `make build-en` で確認
- [ ] 日本語版も `make build-ja` で同様に確認
- [ ] CLAUDE.md / README.md / `sphinx-docs/dev/setup.md` のいずれか適切な箇所に「Pygments upstream ステータス」を 1 セクション追加し、PR URL を記載
- [ ] `sphinx-docs/locale/ja/LC_MESSAGES/*.po` の対応する `msgstr` を日本語化（`documentation.md` の日本語訳同時更新ルールに従う）
- [ ] `✨ Add local Pygments lexer extension for ReScript` でコミット

## セクション 7: Pygments 公式版リリース後の切り替え

セクション 5 マージ + 後続 Pygments リリース後に着手。

- [ ] Pygments の新リリースバージョンを確認
- [ ] `sphinx-docs/pyproject.toml`（または `uv.lock` 管理側）の `pygments` 最低バージョンを引き上げ
- [ ] `sphinx-docs/_ext/rescript_pygments.py` を削除
- [ ] `sphinx-docs/conf.py` の `extensions` から該当エントリを削除
- [ ] `make build-en` / `make build-ja` で公式 lexer によるハイライトを確認
- [ ] CLAUDE.md / README.md / `sphinx-docs/` 内のステータス記載を「マージ済み・公式 Pygments を利用」に更新
- [ ] `🗑️ Drop local Pygments shim, use upstream ReScript lexer` でコミット

## セクション 8: クローズアウト

- [ ] このタスクリストの全タスクを `[x]` に更新
- [ ] requirements.md の受け入れ条件を全件チェック
- [ ] `definition-of-done.md` の Phase 4（マージ確認）・Phase 5（worktree クリーンアップ）を実行
- [ ] `📝 Close out Pygments ReScript lexer steering` でコミット

## 注意事項

- 本ステアリングは外部リポジトリ（pygments/pygments）への貢献を含むため、Pygments 側のマージ判断が完了するまでクローズできない（長期 open のまま）。usage limit や PC クラッシュで中断しても、`upstream-status.md` に常に最新状態を残しておけば再開可能
- Pygments fork での作業は本リポジトリの `git worktree` 機能とは独立。fork クローンのローカルパスを `upstream-status.md` に記録しておく
- 本リポジトリへの commit は「セクション 1（調査メモ）」「セクション 2（Issue URL）」「セクション 5（マージ記録）」「セクション 6（sphinx-docs 統合）」「セクション 7（公式版切り替え）」「セクション 8（クローズ）」で発生する。Pygments 側のコミットメッセージは Pygments 慣例（絵文字なし・英語）に従う
- このタスクは「現時点で着手しない、将来の作業として queue」としてステアリングが作成されている。実際の着手はユーザーが明示的に kick off を指示したタイミングとする
