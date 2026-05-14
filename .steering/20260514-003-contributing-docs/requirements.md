# 要求: コントリビュータ向けドキュメント整備

## 背景

リポジトリは英語の `README.md` を持つが、`CONTRIBUTING.md`（GitHub が標準で認識するコントリビュータガイドファイル）は存在しない。`.claude/rules/` 配下のルールセットは日本語かつ単独メンテナ前提で書かれており、外部の英語話者が PR を出すための手順が明確ではない。

`docs/good-first-issues.md` のような「初参加者向けタスク台帳」も無く、新規コントリビュータが取り組みやすいタスクを発見する経路がない。

## 目的

1. **英語の CONTRIBUTING.md を新設**: `.claude/rules/` の要点（ビルドコマンド、コーディング規約、KDoc、テスト、commit 規約、ブランチ運用、deprecated API ポリシー）を 1 ページに圧縮する
2. **good-first-issue 一覧を docs/good-first-issues.md に整備**: スコープが明確で 1〜3 日で完結するタスクを 10 件提示する。各エントリは目的・読むべきファイル・受け入れ条件・関連ルールを含む
3. **README.md から両ファイルへ導線**: 既存の "Contributing" セクションを書き換え、`CONTRIBUTING.md` と `good-first-issues.md` を案内する

## 受け入れ条件

- [ ] ルート直下に `CONTRIBUTING.md` が存在し、英語で記述されている
- [ ] `docs/good-first-issues.md` に最低 10 エントリ
- [ ] 各 good-first-issue は「読むべきファイル」「受け入れ条件」「関連ルール」を含む
- [ ] `README.md` の Contributing セクションが両ファイルへリンクする
- [ ] CLAUDE.md の更新は不要（authoring/tooling docs としては言及済み）

## 範囲外

- `.claude/rules/` 本体の英訳（language.md の grandfather clause により当面は日本語維持）
- `sphinx-docs/dev/contributing.md` の刷新 — 既存内容のままで OK
- `SECURITY.md` の新設 — follow-up

## 参考

- `.claude/rules/language.md`: 英語必須範囲と日本語必須範囲のマトリクス
- `.claude/rules/git-conventions.md`: コミットメッセージ規約
- `.claude/rules/testing.md`: テスト規約と免除基準
