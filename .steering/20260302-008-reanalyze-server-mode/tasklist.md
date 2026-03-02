# Tasklist: #118 Reanalyze Server Mode

## 実装タスク

- [x] 1. `RescriptReanalyzeVersionDetector` 実装 + テスト
- [x] 2. `RescriptProjectSettings` に `reanalyzeServerEnabled` 設定追加
- [x] 3. `RescriptReanalyzeServerService` 実装 + テスト
- [x] 4. `RescriptReanalyzeServerStartupActivity` 実装（テスト免除: IDE ライフサイクル依存）
- [x] 5. `RescriptConfigurable` に UI チェックボックス追加
- [x] 6. `plugin.xml` に projectService + postStartupActivity 登録

## ドキュメント更新

- [x] 7. CLAUDE.md アーキテクチャセクション更新
- [x] 8. README.md Features セクション更新
- [x] 9. sphinx-docs/user/features/code-analysis.md 更新（advanced.md ではなく code-analysis.md に reanalyze 関連記述があるため）
- [x] 10. docs/product-requirements.md 更新（#118 を実装済みに移動）

## コミット前検証

- [x] 11. `./gradlew clean buildPlugin` 成功（`--no-daemon` 使用: worktree の macOS extended attributes 制約回避）
- [x] 12. `./gradlew test` 全テストパス
- [x] 13. KDoc コメント確認
- [x] 14. セキュリティ確認（外部プロセス実行の安全性: GeneralCommandLine + 明示的引数リスト使用）

## マージ

- [x] 15. ビルド・テスト最終確認
- [x] 16. ユーザーにマージ可否確認
- [x] 17. main にマージ + ブランチ削除
