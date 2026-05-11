# Tasklist: モノレポ対応（rescript.json がサブディレクトリにある場合）

## Phase 1: 計画

- [x] requirements.md 作成
- [x] design.md 作成
- [x] tasklist.md 作成
- [x] worktree 作成 (`monorepo-rescript-json-detection`)

## Phase 2: 実装

### 検出基盤

- [x] `RescriptWorkspaceLayout` data class を `lsp/` に新規作成
- [x] `RescriptGlobExpander` を新規作成（`*`, `**`, ディレクトリ階層対応、除外ディレクトリ対応）
- [x] `RescriptWorkspaceFileParser` を新規作成（pnpm-workspace.yaml 行ベースパーサ + package.json#workspaces JSON パース）
- [x] `RescriptWorkspaceDiscovery` を新規作成（4 段アルゴリズム統合、`discover(Project)` と `discover(String?)` 両 API）

### 検出ロジック差し替え

- [x] `RescriptLspDetector` に `isRescriptProject(Project)` / `isLspAvailable(Project)` オーバーロードを追加し `RescriptWorkspaceDiscovery` 委譲
- [x] `RescriptLspServerDescriptor.findLanguageServer()` に検出済み package root 探索ステップを追加
- [x] `RescriptMissingConfigInspection` を `RescriptWorkspaceDiscovery` ベースに差し替え
- [x] `RescriptCompilerStatusWidgetFactory.isAvailable()` を `RescriptWorkspaceDiscovery` ベースに差し替え
- [x] `RescriptLspStartupActivity`, `RescriptReanalyzeServerStartupActivity`, `RescriptFileTypeRecoveryStartupActivity` を `Project` 渡し API に切り替え

### Settings 拡張

- [x] `RescriptProjectSettings.State` に `packageRoots: MutableList<String>` を追加
- [x] `RescriptSettingsSchema` に `packageRoots` の `RescriptSettingDescriptor` を追加（必要なら新しい型）
- [x] `RescriptConfigurable` に "Project package roots" のテキストエリアを追加
- [x] `RescriptSettingsValidator` に `validatePackageRoots()` を追加

### テスト

- [x] `RescriptGlobExpanderTest` を新規作成（単純パターン、`**`、除外ディレクトリ）
- [x] `RescriptWorkspaceFileParserTest` を新規作成（pnpm/npm 配列/yarn obj/不正 JSON）
- [x] `RescriptWorkspaceDiscoveryTest` を新規作成（11 ケース全網羅）
- [x] `RescriptLspDetectorTest` をモノレポレイアウトで拡張
- [x] `RescriptMissingConfigInspectionTest` をモノレポ層で拡張
- [x] `RescriptSettingsValidatorTest` を `validatePackageRoots()` 含めて拡張

### tasklist.md リアルタイム更新

- [x] 各タスク着手時に即座に `[x]` 更新する
- [x] コミットタスクは `[x]` 更新後にコミットする

## Phase 3: コミット前

### 自己検証

- [x] `./gradlew ktlintCheck` 成功
- [x] `./gradlew clean buildPlugin` 成功
- [x] `./gradlew test` 成功（3827 件 PASS）
- [x] 新たなビルド警告が増えていない（既存の `RescriptTypeTargetResolver` / `RescriptLsp4jClient` 警告のみ）
- [x] Deprecated API 利用なし

### ドキュメント同期

- [x] `CLAUDE.md` のレイヤー 2 (LSP 統合) にモノレポ検出を追記
- [x] `README.md` Features にモノレポサポートを追記
- [x] `sphinx-docs/user/features/advanced.md` に "Project package roots" 設定を記述
- [x] `sphinx-docs/user/troubleshooting.md` に旧版誤警告の説明を加筆
- [x] `make gettext` / `make update-po` で `.po` を再生成し日本語訳を埋める
- [x] `make build-ja` 成功

### コミット

- [x] 機能単位でコミット分割（実装本体 / Settings UI / ドキュメント）
- [x] 絵文字プレフィックス遵守（✨ for new, ♻ for refactor, 📝 for docs）

## Phase 4: マージ前

- [x] tasklist.md の全タスクが `[x]`
- [x] requirements.md の受け入れ条件全て満たす
- [x] AskUserQuestion でマージ可否を確認
- [x] worktree から main に merge → ブランチ削除

## Phase 5: マージ後

- [x] セッション終了で worktree 自動クリーンアップ
