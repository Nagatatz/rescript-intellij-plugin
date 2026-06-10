# 設計: コントリビュータ向けドキュメント整備

## ドキュメント配置

| ファイル | 役割 | 言語 |
|---------|------|------|
| `CONTRIBUTING.md` (新規) | コントリビュータ向け English フロー (build / test / KDoc / commit / branch / DoD 抜粋) | 英語 |
| `docs/good-first-issues.md` (新規) | 初参加者向け 10 エントリ | 英語 |
| `README.md` (更新) | Contributing セクションを CONTRIBUTING.md / good-first-issues.md へ誘導 | 英語 |

## CONTRIBUTING.md の構成

1. **Before you start** — Issue 起票推奨、good-first-issue 案内、ライセンス事項
2. **Development environment** — clone / buildPlugin / runIde / test / ktlintCheck
3. **Test slicing** — `-Pscope=fast|perf|cli` の使い方
4. **Coding conventions** — language.md / code-comments.md / testing.md / flex / deprecated-api の要点
5. **Commit conventions** — 絵文字テーブル + 粒度
6. **Branches and pull requests** — main からの分岐、CI 緑必須、Verifier
7. **What gets reviewed** — レビュー観点
8. **Release process** — リリース手順は maintainer が回す旨を明記
9. **Getting help** — 質問 Issue ラベル / セキュリティ報告

`.claude/rules/` の rule 群を「英語で要約 + 元ファイルへの参照」で繋ぐ。**rule 本文をコピーしない**（drift 防止）。

## good-first-issues.md のエントリ

すべてのエントリで次のフィールドを揃える:

- **Goal**: 1 文での課題説明
- **Files to read**: 関連実装の入口
- **Acceptance**: 受け入れ条件
- **Rules**: 適用される .claude/rules/ ファイル

初期 10 エントリ案 (難易度低 → 中):

1. Live Template 追加
2. sphinx-docs の `.po` 1 ページ翻訳
3. `let rec` 欠落 Inspection 追加
4. Variant Flow の `MAX_NESTING_DEPTH` を Settings 化
5. ReScript 型チェック run config テンプレート
6. perf baseline 1 件のラチェット下げ
7. `%raw.gql()` への GraphQL 言語注入
8. Type Coverage の CSV エクスポート
9. `RescriptWorkspaceDiscovery` の pnpm-workspace fixture テスト
10. `plugin-verifier-ignored-problems.txt` の Expires 棚卸し

## README.md の更新

既存「Contributing」セクションを書き換え:

- Before: "Developer-facing documentation lives in:" のリスト
- After: 冒頭で CONTRIBUTING.md と good-first-issues.md へ誘導、参考リンクは下に残す

`test -Pscope=fast` を Quick reference に追加。

## 影響範囲

新規 2 ファイル + README 編集。コード変更なし。`plugin.xml` 変更なし。テスト変更なし。

`.claude/rules/documentation.md` の「機能実装時のドキュメント更新」マトリクスはコード変更なしのため適用外。
