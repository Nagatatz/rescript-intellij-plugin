# プロジェクトルール索引

このディレクトリには開発プロセス・コード品質・ドキュメント管理に関するルールをまとめている。目的別のエントリポイントを以下に示す。初見でも参照先を迷わないよう、**ルールを当てはめるタイミング**で分類している。

## 基盤 — 全作業に通底する

| ファイル | 用途 |
|---|---|
| [language.md](language.md) | 英語必須範囲と `.claude/` / `.steering/` / CLAUDE.md の exemption |

## 作業着手時 — 計画する

| ファイル | 用途 |
|---|---|
| [steering-workflow.md](steering-workflow.md) | `.steering/` ディレクトリの作成と requirements/design/tasklist の承認フロー |
| [definition-of-done.md](definition-of-done.md) | Phase 1〜5 で構成される「完了」の全チェック項目 |

## 実装中 — コードを書く

| ファイル | 用途 |
|---|---|
| [code-comments.md](code-comments.md) | Kotlin の KDoc 規約（クラス・メソッドに英語で付与） |
| [testing.md](testing.md) | テスト配置・命名・免除基準 |
| [flex-rules.md](flex-rules.md) | JFlex レクサー編集時の制約（`RescriptFlexLexer.java` は自動生成） |
| [plugin-xml-rules.md](plugin-xml-rules.md) | Extension Point 登録の配置ルール |
| [deprecated-api.md](deprecated-api.md) | Deprecated / scheduled-for-removal API の使用禁止と抑制手順 |
| [audit-tasks.md](audit-tasks.md) | リポジトリ横断 audit / カバレッジ調査の二段検証プロセス |

## コミット・リリース時 — 公開する

| ファイル | 用途 |
|---|---|
| [git-conventions.md](git-conventions.md) | コミットメッセージの絵文字プレフィックス・粒度・ブランチ運用 |
| [github-actions-pinning.md](github-actions-pinning.md) | `uses:` のバージョン固定方針（公式 vs サードパーティ） |
| [release.md](release.md) | バージョンアップとタグ作成の手順 |

## ドキュメント作成時 — 伝える

| ファイル | 用途 |
|---|---|
| [documentation.md](documentation.md) | 機能実装時に同期するドキュメント・日本語訳の運用 |
| [roadmap-format.md](roadmap-format.md) | `product-requirements.md` の将来機能テーブルのフォーマット |
| [diagram-rules.md](diagram-rules.md) | 図表作成ツールの選択基準 |

## セッション運用 — 効率よく進める

| ファイル | 用途 |
|---|---|
| [context-management.md](context-management.md) | `/compact` のタイミングやセッション分離の指針 |
| [automation-playbooks.md](automation-playbooks.md) | Type Coverage 自動上昇ループ・テンプレート並列実装スワームの仕様書（実装は要発火指示） |

## 規約間の関係

- 新しい作業を始めるときは **steering-workflow → definition-of-done** の順で確認する
- 実装中は **code-comments + testing** を満たしながら進める
- コミット前は **definition-of-done Phase 3** に列挙された検証項目を通過する
- ドキュメント更新は **documentation.md** の「同期対象表」に沿って行う

新しいルールを追加する場合は、このページの該当カテゴリに1行追加すること。
