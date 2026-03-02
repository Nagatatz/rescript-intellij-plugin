# Security Fixes — Tasklist

## 実装タスク

- [x] 1. `chain.crt` / `private.pem` 削除 + `.gitignore` に `*.crt` 追加
- [x] 2. `RescriptReplExecutor` — JS アーティファクト削除 + プロセスリソースリーク修正 + パスバリデーション + テスト更新
- [x] 3. `RescriptReplPanel` — バックグラウンドスレッド実行（テスト省略: Swing UI コンポーネント）
- [x] 4. `RescriptDependencyDiagramModel` — DOT インジェクション防止 + テスト追加

## ドキュメント更新

- [x] 5. CLAUDE.md / README.md / sphinx-docs — 反映不要（セキュリティ修正のみ、機能一覧に変更なし）

## コミット前検証

- [x] 6. `./gradlew clean buildPlugin` 成功確認
- [x] 7. 全テストパス確認

## マージ

- [x] 8. main にマージ + ブランチ削除
