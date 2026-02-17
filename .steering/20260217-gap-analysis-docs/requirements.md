# Requirements: rescript-vscode ギャップ分析の docs 追記

## 概要

rescript-vscode（公式 VS Code 拡張）と比較した結果を `docs/product-requirements.md` と `docs/functional-design.md` に反映する。

## 変更対象

### 1. `docs/product-requirements.md`

- 「将来機能（ロードマップ）」セクション（65〜73行目）を更新
- 実装済み項目を明示的にマーク
- rescript-vscode との詳細なギャップ分析に基づく P1/P2/P3 ロードマップに置き換え

### 2. `docs/functional-design.md`

- Extension Point 登録マップの更新（現在登録済みのものにチェックマーク追加）
- rescript-vscode との機能対比表を追加

## 受け入れ条件

- [ ] product-requirements.md のロードマップが P1/P2/P3 の優先度別テーブルに整理されている
- [ ] 実装済み機能が明確にマークされている
- [ ] functional-design.md に機能対比表が追加されている
- [ ] Extension Point 登録マップが最新状態に更新されている
- [ ] Markdown 記法が正しい
- [ ] 既存セクションとの整合性がある
- [ ] ビルドへの影響なし（ドキュメントのみの変更）
