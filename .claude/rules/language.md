# 言語ルール

**以下は強制的な行動指示であり、例外なく従うこと。**

プロジェクト内のすべての成果物は **英語** で記述すること。

## 英語で記述するもの

- ソースコード（Kotlin, JFlex, XML）
- KDoc コメント、インラインコメント
- コミットメッセージ
- GitHub Release ノート
- `plugin.xml` の `<change-notes>`
- `README.md`
- `CLAUDE.md`
- `docs/` 配下のドキュメント
- `.steering/` 配下のドキュメント（requirements.md, design.md, tasklist.md）
- `.claude/rules/` 配下のルールファイル
- テスト名、テストコメント

## 唯一の例外

`sphinx-docs/locale/ja/LC_MESSAGES/` 配下の `.po` ファイルの `msgstr` フィールドのみ日本語で記述する。これは Sphinx ドキュメントの日本語翻訳であり、翻訳が目的であるため例外とする。

## 禁止事項

- 上記例外を除き、リポジトリ内のファイルに日本語を含めてはならない
- GitHub Release ノート、コミットメッセージ、コードコメントに日本語を使用してはならない
