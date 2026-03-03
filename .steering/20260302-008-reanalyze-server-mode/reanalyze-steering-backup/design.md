# Design: #118 Reanalyze Server Mode

## アーキテクチャ

```
Project Open → StartupActivity
  → settings.reanalyzeServerEnabled? → isRescriptProject? → version >= 12.1.0?
  → ServerService.startServer()
    → 外部サーバー検出（ソケット存在チェック）→ EXTERNAL
    → rescript-tools reanalyze-server 起動 → ソケット出現待ち → RUNNING
  → 既存 Annotator/Inspection は変更なしで自動高速化

Health Check (5秒間隔)
  → プロセス死亡検出 → ソケット削除 → 自動再起動（最大3回）

Project Close → dispose()
  → startedByUs ならプロセス終了 + ソケット削除
```

## 新規ファイル

| ファイル | 責務 |
|---------|------|
| `RescriptReanalyzeVersionDetector.kt` | package.json バージョン読み取り、semver パース、サーバーモードサポート判定 |
| `RescriptReanalyzeServerService.kt` | サーバーライフサイクル管理（起動/停止/ヘルスチェック/再起動） |
| `RescriptReanalyzeServerStartupActivity.kt` | プロジェクト起動時の自動サーバー起動トリガー |

## 既存ファイル変更

| ファイル | 変更内容 |
|---------|---------|
| `RescriptProjectSettings.kt` | `reanalyzeServerEnabled: Boolean = true` 追加 |
| `RescriptConfigurable.kt` | チェックボックス UI 追加 |
| `plugin.xml` | projectService + postStartupActivity 登録 |

## エラーハンドリング

| シナリオ | 対応 |
|----------|------|
| ReScript < 12.1.0 | サーバー起動しない。既存モード継続 |
| rescript-tools 未検出 | no-op、DEBUG ログ |
| 起動タイムアウト | プロセス終了、STOPPED、WARN ログ |
| サーバークラッシュ | 最大3回自動再起動 |
| 外部サーバー検出 | EXTERNAL 状態、プロセス管理しない |
