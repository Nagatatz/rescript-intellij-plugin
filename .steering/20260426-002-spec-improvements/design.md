# 設計: 仕様ドキュメントの改善

## 全体方針

- すべての変更は `docs/`、`README.md`、`plugin.xml` description、`sphinx-docs/` 配下のドキュメント変更のみ
- `.steering/` 例外規定により `main` 直コミット可（ただし機能単位でコミット粒度を分ける）
- `sphinx-docs/` の `.md` を更新する場合は同一コミットで `locale/ja/LC_MESSAGES/**/*.po` も更新（CLAUDE.md ルール）
- コード変更を伴わないため worktree 不要

## コミット分割

7 個の独立した改善があるため、以下のコミット粒度に分割する:

| # | コミット内容 | 絵文字 | 対象ファイル |
|---|------|--------|-------------|
| 1 | テンプレート数 12 → 16 修正 | 🐛 | `plugin.xml` (description), `functional-design.md` |
| 2 | LSP 最低バージョン要件追加 | 📝 | `architecture.md`, `plugin.xml` (description), `sphinx-docs/user/installation.md`, `sphinx-docs/user/version-matrix.md`, 対応する `.po` |
| 3 | プラットフォーム互換性戦略を PRD に追加 | 📝 | `docs/product-requirements.md` |
| 4 | EP マップ欠落補填 | 📝 | `docs/functional-design.md` |
| 5 | US-11〜US-15 追加 | 📝 | `docs/product-requirements.md` |
| 6 | LSP フォールバックマトリクス作成 | 📝 | `docs/lsp-fallback-matrix.md`（新規） |
| 7 | パフォーマンス検証ドキュメント作成 | 📝 | `docs/performance-validation.md`（新規） |

最後に tasklist 更新コミット（📝）でこのステアリングディレクトリの完了状態をコミットする。

## 各タスクの詳細設計

### Task 1: テンプレート数 12 → 16 修正

**現状**: 16 テンプレート実装済み (AwsLambda / Basic / CliTool / CloudflareWorkers / Electron / FullStack / GoogleCloudRun / Hono / HonoGraphql / Monorepo / Nextjs / NpmLibrary / ReactNative / ReactNativeCli / ResX / ViteReact)

**変更箇所**:
- `src/main/resources/META-INF/plugin.xml` line 57: `"12 templates"` → `"16 templates"`
- `docs/functional-design.md` line 473: `"12 テンプレート選択 UI"` → `"16 テンプレート選択 UI"`

**注意**: `sphinx-docs/user/version-matrix.md` line 199 の `"12 project templates"` は **0.1.2 リリース当時の事実** として保持（履歴記録）。

### Task 2: LSP 最低バージョン要件追加

**追加内容**: `@rescript/language-server` 1.0.0+ を明記。理由: 0.x は ReScript 11 未対応で、本プラグインが想定する LSP 拡張プロトコル（`rescript/compilationStatus`、`textDocument/createInterface`、`textDocument/openCompiled`）に対応しないため。

**変更箇所**:
- `docs/architecture.md` § 3.外部依存テーブル: `@rescript/language-server` 行に `1.0.0+` を追記
- `src/main/resources/META-INF/plugin.xml` description 内 Requirements セクション: バージョン明記
- `sphinx-docs/user/installation.md`: prerequisites セクション
- `sphinx-docs/user/version-matrix.md` line 13-15 の IDE Compatibility テーブル: `Language Server` 列を具体的バージョンに更新
- 対応する `.po`: `sphinx-docs/locale/ja/LC_MESSAGES/user/installation.po`, `version-matrix.po`

### Task 3: プラットフォーム互換性戦略を PRD に追加

**追加位置**: PRD § 5「ビジネス要件」と § 6「ユーザーストーリー」の間に新規 § 6「プラットフォーム互換性戦略」を挿入（既存 § 6 以降は番号繰り下げ）。

**含める内容**:
- 年次サポートポリシー: 毎年 IntelliJ Platform 新バージョンを `pluginSinceBuild` 候補として評価
- `pluginUntilBuild` 運用: 通常未設定。破壊的変更検出時のみ緊急設定
- verifier ブロッカー対応: 例として「IntelliJ Platform 2026.1 への移行は verifier-cli 1.402 が split-jar layout 非対応のため 1.403+ リリース待ち」を記載
- LTS 保証範囲: 公開済み最新マイナーバージョンの 2025.3 LTS 範囲をサポート
- 月次 verifier 検証: CI で nightly 実行（提案）

### Task 4: EP マップ欠落補填

**追加対象 EP**（plugin.xml で確認済み）:
- `scratch.rootType` → `RescriptScratchRootType` (line 1102-1104)
- `scratch.creationHelper` → `RescriptScratchCreationHelper` (line 1105-1106)
- `com.intellij.toolWindow` → `RescriptReplToolWindowFactory` (line 1108-1110, REPL)
- `com.intellij.toolWindow` → `RescriptPpxViewToolWindowFactory` (line 1129-1131, PPX)
- `com.intellij.toolWindow` → `RescriptTypeInfoToolWindowFactory` (line 745, TypeInfo)
- `com.intellij.fileType` → `RescriptWorksheetFileType` (line 1146-1151, .resw)
- `com.intellij.codeInsight.declarativeInlayProvider`（または現行 EP 名） → `RescriptPpxVisualizationProvider` (line 993-995)
- 依存ダイアグラム関連 EP（`<diagram.elementsProvider>` 等）

**配置**: 既存テーブルの末尾に追記、カテゴリ順を意識する。

### Task 5: US-11〜US-15 追加

**追加位置**: PRD § 6 ユーザーストーリーの末尾（既存 US-10 の後）。

各 US は:
- ペルソナ視点の動機文
- 受け入れ条件（実装済みは [x]、想定機能は [ ]）

新規 US:
- US-11: Project Wizard でのプロジェクト雛形生成
- US-12: Worksheet / REPL でのインタラクティブ評価
- US-13: PPX 展開ビューによる macro 効果の可視化
- US-14: Type Info ToolWindow での常時型表示
- US-15: 依存ダイアグラムでのモジュール関係把握

### Task 6: LSP フォールバックマトリクス作成

**ファイル**: `docs/lsp-fallback-matrix.md`（新規）

**構造**:
1. 目的・参照先の説明
2. テーブル形式の機能リスト（カテゴリ別）
   - 列: 機能名 / LSP 必須? / 非接続時の動作
3. LSP 起動失敗時のユーザー観点フロー（エディタ通知 → 案内バー → 手動インストール）

### Task 7: パフォーマンス検証ドキュメント作成

**ファイル**: `docs/performance-validation.md`（新規）

**構造**:
1. NFR-01 の目標値再掲
2. 計測手段（IntelliJ Platform Profiler、Gradle profiler、JFR、メモリスナップショット）
3. 計測タイミング（リリース前手動、CI 月次、ベンチマーク用ブランチ）
4. 結果の記録場所（`docs/performance-results.md` を将来的に追加するか、リリースノートに記載）
5. ラチェットポリシー（カバレッジラチェットと類似のパフォーマンス低下ガード）

## 検証方針

- 各コミット後 `./gradlew ktlintCheck verifyPluginStructure` を実行（軽量、コードに変更がないため fast-pass 想定）
- `sphinx-docs/` 変更後は `.po` 翻訳の整合性を `make build-ja` で確認

## ロールバック計画

各コミットが小さいため問題があれば該当コミットを `git revert` で取り消す。
