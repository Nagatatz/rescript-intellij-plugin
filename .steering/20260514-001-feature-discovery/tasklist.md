# 機能発掘調査 — タスクリスト

調査専用ステアリング。実装は別ステアリングに承継するため、本タスクリストには「調査ドキュメント作成 + コミット」のみを含める。

## セクション A: 調査と二段検証

- [x] 内部 audit (`grep` で色付け使用箇所を確認、最近の Visual 機能 5 件を直接確認)
- [x] 外部リサーチ (subagent #1): ReScript 関連 IntelliJ プラグイン landscape 調査
- [x] 外部リサーチ (subagent #2): 関数型/他言語 IDE 機能調査
- [x] 二段検証: `gh api repos/giraud/reasonml-idea-plugin` で reasonml-idea-plugin のメンテ状態を実証
- [x] 追加 audit (Explore agent): 14 箇所の表示コンポーネント網羅監査
- [x] 候補 21 件を 5 バケット (A〜E) に整理

## セクション B: ステアリングドキュメント整備

- [x] `requirements.md` 作成 (調査背景・結果サマリ・候補リスト・受け入れ条件)
- [x] `design.md` 作成 (バケット別設計概要)
- [x] `tasklist.md` 作成 (本ファイル)

## セクション C: コミットとハンドオフ

- [x] 全タスクを `[x]` に更新してコミット (調査タスクは main に直接コミット可、`.claude/rules/steering-workflow.md` の例外規定に該当)
- [x] 承認候補 (バケット A) の実装ステアリング `20260514-002-visual-color-brushup` を作成し、本ステアリングのスコープを承継
