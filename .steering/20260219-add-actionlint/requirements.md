# Requirements: actionlint 導入

## 概要

GitHub Actions ワークフローファイル（`.github/workflows/*.yml`）の静的解析ツール actionlint を CI パイプラインに追加する。

## 背景

現在プロジェクトには 3 本のワークフローファイルがある:
- `ci.yml` — ビルド・テスト・検証
- `release.yml` — タグベースのリリース
- `qodana_code_quality.yml` — Qodana 静的解析

これらに対する構文チェックツールが未導入のため、ワークフロー定義の誤りを事前に検出できない。

## 要件

1. CI ワークフロー（`ci.yml`）に actionlint ステップを追加する
2. ktlint と同様、lint 系チェックはビルドの前段で実行する
3. actionlint のエラーがある場合は CI を失敗させる

## 受け入れ条件

- [ ] `ci.yml` に actionlint ステップが追加されている
- [ ] 既存の 3 ワークフローファイルが actionlint をパスする
- [ ] CI パイプラインが正常に動作する（ローカルで構文確認）
