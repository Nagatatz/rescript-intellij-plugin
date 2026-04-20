# 言語ルール

**以下は強制的な行動指示であり、例外なく従うこと。**

ユーザー向け成果物（公開ドキュメント・ソースコード・Git 履歴・リリース）は **英語** で記述する。一方、authoring/tooling 用の内部ドキュメント（`.claude/`、`.steering/`、`CLAUDE.md`）は、日本語のまま維持することを許可する。

## 英語で記述するもの（必須）

以下は単独ユーザー環境でも常に英語で記述すること:

- ソースコード（Kotlin, JFlex, XML）
- KDoc コメント、インラインコメント
- コミットメッセージ
- GitHub Release ノート
- `plugin.xml` の `<change-notes>`
- `README.md`
- `docs/` 配下のドキュメント
- `sphinx-docs/` 配下の `.md`（`locale/` 配下の `.po` を除く）
- テスト名、テストコメント

## 日本語可の範囲（authoring / tooling 例外）

以下は authoring/tooling コンテキストであり、ユーザーには公開されないため、日本語での記述を許可する:

- `.claude/` 配下のすべて（`rules/`, `skills/`, `commands/`, `agents/`, `hooks/`, `memories/` 等）
- `.steering/` 配下のドキュメント（requirements.md, design.md, tasklist.md）
- `CLAUDE.md`
- `sphinx-docs/locale/ja/LC_MESSAGES/**/*.po` の `msgstr` フィールド（翻訳本体）

**理由:** これらは単独コントリビュータの認知負荷軽減のために母語（日本語）で書く方が効率的であり、英語化の翻訳コストとニュアンス喪失リスクに見合わない。外部 PR を受け付ける方針に変わった際は、本ルールを再検討する。

## 禁止事項

- 「英語で記述するもの（必須）」列挙ファイルに日本語を混入させること
- GitHub Release ノート、コミットメッセージ、コードコメントに日本語を使用すること
- `sphinx-docs/**/*.md`（英語本体）に日本語を混入させること（翻訳は `.po` 側で行う）
