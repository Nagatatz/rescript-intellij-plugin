# B 優先度機能一括実装 (バッチ2) — 要件定義

## 概要

docs/product-requirements.md に残る B 優先度機能 8 件を一括実装する。

## 対象機能

| # | 機能名 | カテゴリ |
|---|--------|----------|
| 56 | Framework Detector | config/ |
| 52 | Code Rearranger | codestyle/ |
| 103 | 変更可能性の診断 | inspection/ |
| 102 | スタイルリンティング | inspection/ |
| 97 | filter+map チェーン変換 | intention/ |
| 85 | 型注釈追加 | intention/ |
| 109 | PPX 可視化 | lsp/ |
| 99 | 型ミスマッチ差分表示 | errorlens/ |

## 受け入れ条件

- [ ] 8 件すべての実装が完了している
- [ ] 各機能にユニットテストがある
- [ ] 全クラスに KDoc がある
- [ ] plugin.xml に extension point が登録されている
- [ ] `./gradlew clean buildPlugin` が成功する
- [ ] 全テストがパスする
- [ ] CLAUDE.md, README.md, product-requirements.md が更新されている
