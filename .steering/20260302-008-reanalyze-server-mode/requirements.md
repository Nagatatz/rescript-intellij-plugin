# Requirements: #118 Reanalyze Server Mode

## 概要

ReScript >= 12.1.0 の `rescript-tools reanalyze-server` サブコマンドを活用し、reanalyze をデーモンモードで常駐させて差分分析を高速化する。

## 背景

現在の reanalyze 統合は `rescript-tools reanalyze -json` をファイル毎・検査毎に毎回起動する一発実行モデル。プロジェクト全体を毎回再解析するため大規模プロジェクトで遅い。`reanalyze-server` は Unix ソケット経由で既存の `reanalyze -json` を透過的に高速化する。

## 受け入れ条件

- [ ] ReScript >= 12.1.0 のプロジェクトで `reanalyze-server` プロセスが自動起動する
- [ ] 既存の `RescriptReanalyzeAnnotator` / `RescriptUnusedCodeInspection` の変更なしで自動高速化される
- [ ] 設定画面でサーバーモードの有効/無効を切り替えられる
- [ ] サーバークラッシュ時に自動再起動される（最大3回）
- [ ] IDE/プロジェクト終了時にプロセスがクリーンアップされる
- [ ] 外部で起動済みのサーバーがあれば検出してプロセス管理しない
- [ ] ReScript < 12.1.0 では従来の一発実行モードが維持される
- [ ] ビルドが成功し、全テストがパスする
