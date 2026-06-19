# タスクリスト: Build Console 専用 ToolWindow (#119)

各セクション = 「マージ可能な単位」= 1 コミット。純ロジック（セクション 1〜2）を先に緑で刻み、
その後 IDE 結合（サービス・UI・EP 登録）、最後にドキュメントをまとめる。

依存関係:
- セクション 2 (Parser) はセクション 1 (Diagnostic data) に依存
- セクション 4 (Service) はセクション 2 (Parser) と 3 (Listener) に依存
- セクション 5 (Factory+Panel+plugin.xml) はセクション 4 (Service) に依存
- セクション 6 (ドキュメント) は全機能完成後

## セクション 0: 準備

- [ ] MAIN repo で `git fetch origin` / `git log --oneline origin/main..HEAD` / `HEAD..origin/main` で ahead-behind 確認
- [ ] ローカル main が origin より遅れていれば `git pull --ff-only origin main`
- [ ] `.claude/worktrees/build-console-toolwindow/` を最新 main から作成し EnterWorktree
- [ ] worktree 内で `pwd` / `git rev-parse --show-toplevel` で編集パスを確認

## セクション 1: RescriptBuildDiagnostic（診断データ型）

- [ ] `build/RescriptBuildDiagnostic.kt` 作成
  - [ ] `enum class RescriptBuildSeverity { ERROR, WARNING }`（KDoc）
  - [ ] `data class RescriptBuildDiagnostic(filePath, line, colStart, colEnd, severity, messageHead)`（KDoc）
- [ ] `./gradlew test` グリーン確認（既存テストが壊れていないこと）
- [ ] コミット `✨ Add build diagnostic data type for build console`（tasklist 更新含む）

## セクション 2: RescriptBuildOutputParser（出力パーサ・コア）

- [ ] `build/RescriptBuildOutputParser.kt` 作成
  - [ ] `object` + `fun parse(output: String): List<RescriptBuildDiagnostic>`
  - [ ] watch リセット（最後の `>>>> Start compiling` 以降のみ解析）
  - [ ] severity ヘッダ検出（bug=ERROR / Syntax error=ERROR / Warning number=WARNING）
  - [ ] 位置行正規表現（同一行 + 複数行 `line:col-line:col` 両対応 / `.resi` 対応）
  - [ ] 不正な行列番号・パス未取得は無視（例外を投げない）
  - [ ] KDoc（クラス + parse）
- [ ] `test/build/RescriptBuildOutputParserTest.kt` 作成（fixture 不要）
  - [ ] 受け入れ条件 1: エラー 1 件抽出（filePath/line/colStart/colEnd/severity）
  - [ ] 受け入れ条件 2: 警告 1 件抽出（`Warning number 110`）
  - [ ] 構文エラー（`Syntax error!`）→ ERROR
  - [ ] 受け入れ条件 3: 混在複数件を出現順で全件抽出
  - [ ] 受け入れ条件 4: watch リセット（前サイクル診断を引き継がない）
  - [ ] 受け入れ条件 5: 無関係出力（`Finish compiling 0 errors`）→ 空リスト
  - [ ] 受け入れ条件 6: 不正な行列番号 → 該当無視・例外なし
  - [ ] 複数行位置 `3:9-5:2` → startLine=3, colStart=9 採用
  - [ ] `.resi` パスの位置行
- [ ] `./gradlew test` グリーン確認
- [ ] コミット `✨ Add build output parser for build console`（tasklist 更新含む）

## セクション 3: RescriptBuildConsoleListener（リスナ + 状態 enum）

- [ ] `build/RescriptBuildConsoleListener.kt` 作成
  - [ ] `interface RescriptBuildConsoleListener { onDiagnosticsUpdated, onStateChanged }`（KDoc）
  - [ ] `enum class RescriptBuildState { IDLE, RUNNING, STOPPED }`（KDoc）
- [ ] テスト免除（ロジックなし interface + enum）— tasklist に理由記載済み
- [ ] `./gradlew test` グリーン確認
- [ ] コミット `✨ Add build console listener and state model`（tasklist 更新含む）

## セクション 4: RescriptBuildConsoleService（プロセス起動・出力収集）

- [ ] `build/RescriptBuildConsoleService.kt` 作成（`@Service(Service.Level.PROJECT)` + `Disposable`）
  - [ ] `startWatch()`: `RescriptCliDetector` + `RescriptWorkspaceDiscovery` で解決、
        `GeneralCommandLine(listOf(cliPath, "build", "-w"))` + `OSProcessHandler`
  - [ ] `ProcessListener.onTextAvailable` → バッファ蓄積 → デバウンス → `parse` → リスナ通知
  - [ ] `stopWatch()` / `clear()` / state 管理
  - [ ] `dispose()` でプロセス終了（リーク防止）
  - [ ] リスナ登録/解除
  - [ ] KDoc（クラス: @Service 言及 / 各 public メソッド）
  - [ ] セキュリティ: 引数固定リスト・絶対パス非露出
- [ ] テスト免除（外部プロセス結合 + Service ライフサイクル依存）— 純ロジックは Parser に分離済み
- [ ] deprecated API 不使用を確認（`OSProcessHandler` / `GeneralCommandLine` / `ProcessListener`）
- [ ] `./gradlew test` グリーン確認
- [ ] コミット `✨ Add build console service for rescript watch`（tasklist 更新含む）

## セクション 5: Factory + Panel + plugin.xml 登録（UI 統合）

- [ ] `build/RescriptBuildConsoleToolWindowFactory.kt` 作成（`ToolWindowFactory`, `DumbAware`, KDoc）
- [ ] `build/RescriptBuildConsolePanel.kt` 作成（`RescriptToolWindowPanelBase` 継承, KDoc）
  - [ ] center: `Tree`（ファイルノード → 診断子ノード, severity アイコン）
  - [ ] toolbar: Start watch / Stop / Clear
  - [ ] statusLabel: Idle / Building… / N errors, M warnings
  - [ ] ダブルクリック + Enter → `OpenFileDescriptor(project, vFile, line-1, colStart-1).navigate(true)`
  - [ ] `RescriptBuildConsoleListener` 実装 / UI 更新は EDT / dispose 時にリスナ解除
- [ ] `plugin.xml` に `<toolWindow id="ReScript Build" anchor="bottom" .../>` 登録（既存並び順に従う）
- [ ] テスト免除（Swing UI + ToolWindowFactory）— tasklist に理由記載済み
- [ ] deprecated API 不使用を確認
- [ ] `./gradlew ktlintCheck` / `clean buildPlugin` グリーン確認
- [ ] コミット `✨ Add build console tool window and panel`（tasklist 更新含む）

## セクション 6: ドキュメント

- [ ] `docs/repository-structure.md` — `build/` パッケージ行を追加
- [ ] `docs/functional-design.md` — ToolWindow カテゴリに Build Console 解説 + EP マップ
- [ ] `README.md` — Features に 1 項目追加
- [ ] CLAUDE.md — レイヤー 3 の ToolWindow 列挙に「Build Console」を追記
- [ ] `sphinx-docs/` — 該当 ToolWindow ページ（EN）作成/追記 + `make gettext`/`update-po`/msgstr 日本語/`make build-ja`（`sphinx-po-ja-sync` スキル）
- [ ] `docs/product-requirements.md` — ロードマップ #119 行を削除
- [ ] コミット `📝 Document build console tool window`（tasklist 更新含む）

## セクション 7: DoD・マージ

- [ ] `./gradlew ktlintCheck` グリーン
- [ ] `./gradlew clean buildPlugin` グリーン
- [ ] `./gradlew test` グリーン
- [ ] 新規 `.kt` すべてに対応テスト存在 or 免除理由を確認（Parser/Diagnostic はテスト有、その他は免除）
- [ ] requirements.md の受け入れ条件 1〜6（自動）/ 7〜8（手動 or UI smoke）を確認
- [ ] ビルド警告が新たに増えていないことを確認
- [ ] tasklist 全項目 `[x]` 化（このセクション含む）をマージ前最終コミットに含める
- [ ] `AskUserQuestion` でマージ可否確認（セキュリティ: 外部プロセス起動を明示）
- [ ] 承認後 main にマージ → 作業ブランチ削除 → セッション終了（worktree 自動クリーンアップ）

## テスト免除サマリ（testing.md 準拠）

| クラス | 免除理由 |
|--------|---------|
| `RescriptBuildConsoleListener` / `RescriptBuildState` | ロジックなし interface + enum |
| `RescriptBuildConsoleService` | 外部プロセス結合 + IDE Service ライフサイクル依存（純ロジックは Parser に分離） |
| `RescriptBuildConsoleToolWindowFactory` | ToolWindowFactory（登録のみ） |
| `RescriptBuildConsolePanel` | Swing UI コンポーネント |
