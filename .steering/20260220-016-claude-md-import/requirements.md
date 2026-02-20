# Requirements: CLAUDE.md @import 構文化

## 概要

CLAUDE.md の開発規約参照セクション（247-252行目）を Claude Code の `@` import 構文に置き換える。

## 背景

現在、`.claude/rules/` 配下のルールファイルへの参照が手動のテキストリストで記述されている。Claude Code の `@` import 構文を使うことで、Claude がこれらのルールファイルを自動的にコンテキストに読み込めるようになる。

## 要件

- CLAUDE.md の247-252行目の手動リストを `@` import 構文に置き換える
- 参照先の5ファイルすべてを `@` 構文で記述する
- パスは既存と同じ相対パスを使用する

## 対象ファイル

- `CLAUDE.md` (変更)
- `.claude/rules/testing.md` (参照のみ、変更なし)
- `.claude/rules/code-comments.md` (参照のみ、変更なし)
- `.claude/rules/git-conventions.md` (参照のみ、変更なし)
- `.claude/rules/steering-workflow.md` (参照のみ、変更なし)
- `.claude/rules/documentation.md` (参照のみ、変更なし)

## 受け入れ条件

- `@` 構文が正しいパスを指していること
- コード変更なし（テスト不要）
