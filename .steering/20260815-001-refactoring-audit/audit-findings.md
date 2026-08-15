# リファクタリング候補監査（2026-08-15）

3 つの独立 Explore エージェント（①重複コード ②サイズ・複雑度 ③API 衛生）による一次調査と、6 件の無作為スポットチェック（全件一致）による二次検証の結果。`.claude/rules/audit-tasks.md` の二段検証プロセスに準拠。

## 二次検証の記録

| # | 検証項目 | 方法 | 結果 |
|---|---------|------|------|
| 1 | ツールウィンドウ起動アクション 4 クラスの重複 | diff（文字列正規化後） | 一致（本体は同一、差分は表示文字列とアイコンのみ） |
| 2 | DOT ID サニタイザの重複 | grep 原典比較 | 一致（`RescriptVariantFlowDotExporter.kt:52` が `MermaidLabelEscaping.sanitize` を inline 再実装） |
| 3 | 未使用 import 2 件 | grep トークン全数 | 一致（`RescriptProcessUtils.kt:5`、`RescriptNotebookPanel.kt:13`） |
| 4 | REPL のパイプデッドロック | Read（164-187 行） | 一致（`waitFor` が stream 読み取りより先、`redirectErrorStream` なし） |
| 5 | 設定 apply() の EDT ブロック | grep 呼び出し連鎖 | 一致（`RescriptConfigurable.kt:89` → `startServer` → `waitForSocket` 10 秒ポーリング） |
| 6 | FormattingService の非デーモンスレッド | grep | 一致（`Thread(` ×2、`isDaemon` 設定なし、`executeWithStdin` 未使用） |

## Track A: 実質バグに近い修正（最優先）

| # | 内容 | 場所 | 根拠 |
|---|------|------|------|
| A1 | REPL 実行のパイプデッドロック。`waitFor` を stream drain より先に呼ぶため、子プロセス出力が OS パイプバッファ（~64KB）を超えると恒久タイムアウト | `repl/RescriptReplExecutor.kt:164-187` | `RescriptReanalyzeAnnotator.kt:79-80` 等は正しい順序で実装済み |
| A2 | 設定画面 `apply()`（EDT）から `startServer()`/`stopServer()` を直接呼び、有効化で最大 10 秒・無効化で最大 5 秒 IDE がフリーズ | `settings/RescriptConfigurable.kt:89-91` → `analysis/RescriptReanalyzeServerService.kt:108,158` | `SOCKET_WAIT_TIMEOUT_MS = 10_000`、`Thread.sleep(200)` ポーリング |
| A3 | intention `invoke()`（EDT）と QuickFix `applyFix`（write action 中）での同期 LSP 10 秒リクエスト | `RescriptAddTypeAnnotationIntention.kt:49` / `RescriptCaseSplitIntention.kt:57` / `RescriptInsertLabeledArgsIntention.kt:45` / `RescriptConvertToLabeledArgsIntention.kt:65` / `RescriptAddMissingSwitchArmsIntention.kt:182` / `RescriptSignatureSyncInspection.kt:92` | `RescriptHoverTypeResolver.kt:34` が「off-EDT で呼べ」と明文化済み。`RescriptTypeInfoPanel.kt:126-146` が正しい実装例 |
| A4 | FormattingService が `RescriptProcessUtils.executeWithStdin` を inline 再実装し、非デーモンスレッドを timeout 後に interrupt しないためリークし得る | `formatter/RescriptFormattingService.kt:33-128` | ユーティリティ側はデーモン化・interrupt 処理済み |

補足（A 関連・低優先）: `RescriptCodeVisionProvider.java:112` の `sendRequestSync(0, ...)`（無限タイムアウト）にも有限値を与える。

## Track B: 重複排除

| # | 内容 | サイト |
|---|------|--------|
| B1 | switch アーム走査の 4 重実装を `RescriptSwitchWalker`（lang/）に統一。既にドリフトあり（or-パターンの括弧処理が実装間で不一致） | `narrowing/RescriptSwitchArmCollector.kt:88` / `intention/RescriptNestedSwitchFlattener.kt:193,363` / `intention/RescriptMergeSwitchCasesIntention.kt:153` / `highlight/RescriptHighlightUsagesHandlerFactory.kt:113` |
| B2 | ツールウィンドウ起動アクション 4 クラス → 抽象基底 `RescriptShowToolWindowAction` | `diagram/RescriptDependencyDiagramAction.kt` / `impact/RescriptTypeImpactAction.kt` / `flow/RescriptVariantFlowAction.kt` / `interop/RescriptInteropRiskAction.kt` |
| B3 | sibling-walk 収集関数 6 個 → 共通ウォーカー（`util/RescriptBraceBalanceUtil.kt` に追加） | `highlight/RescriptHighlightUsagesHandlerFactory.kt:113,153,192,222,260,288` |
| B4 | LSP hover リクエスト + Either アンラップの 2 重実装を `RescriptLspUtils` に集約 | `lsp/RescriptLspUtils.kt:94-154` / `lsp/RescriptExpressionTypeProvider.kt:23-84` |
| B5 | Inspection の `buildVisitor`/`visitFile` ガード 9 箇所 → `RescriptFileInspection` 基底 | `inspection/` 8 ファイル + `quickfix/RescriptTypeHoleQuickFix.kt:22` |
| B6 | Wizard テンプレート共通化: React 依存プリアンブル ×12 → `TemplateScaffold.reactCoreDependencies()`、DB variant `when` ×5 → `TemplateScaffold`/`CommonFiles` | `wizard/templates/` 各所 |
| B7 | 2 つのグラフビューの paint 骨格・定数・`nodeWidth` → `GraphViewPaintHelpers` 拡張 or 基底クラス | `flow/RescriptVariantFlowGraphView.kt` / `diagram/RescriptDependencyDiagramGraphView.kt` |
| B8 | 行ウィンドウ計算 9 箇所（3 種の綴り）→ `util/RescriptOffsetUtils` に `lineStartAt`/`lineTextAt`。`RescriptTypeHoleQuickFix.kt:69` は検索開始 offset が他と異なるため挙動差の有無を統一時に検証 | intention/quickfix/impact 各所 |
| B9 | paste 処理の `TextBlockTransferableData` 実装 2 個 → 抽象基底 | `paste/RescriptPasteAsRescriptProcessor.kt:292` / `paste/RescriptPasteAsJsxProcessor.kt:228` |
| B10 | InlayHintsProvider 設定ボイラープレート 4 箇所 → 共通基底 | `narrowing/` `lsp/` ×2 `editor/` |

## Track C: 構造分割（今回のスコープ外・将来課題）

- `flow/RescriptVariantFlowGraphView.kt`（499 行）: レイアウトエンジン + パレット分離、レイアウト結果のキャッシュ化（現状は毎 repaint で再計算）
- `binding/DtsGenerateBindingAction.actionPerformed`（128 行・6 責務）: 環境検証と生成パイプラインの分離
- `paste/RescriptPasteAsRescriptProcessor.convertLine`（126 行）: フェーズごとの `LineRule` リスト化
- `analysis/RescriptReanalyzeServerService`（320 行・4 責務）: socket 管理とヘルスチェックの分離
- `wizard/templates/MonorepoTemplateFiles.generate`（299 行）: ワークスペース単位に分割

## Track D: 小粒の衛生

| # | 内容 | 場所 |
|---|------|------|
| D1 | 未使用 import 削除 | `util/RescriptProcessUtils.kt:5`（`java.io.File`）/ `notebook/RescriptNotebookPanel.kt:13`（`BorderLayout`） |
| D2 | デッド定数 `NODE_PADDING_Y` 削除 | `flow/RescriptVariantFlowGraphView.kt:126` |
| D3 | デッド `EmptyState` object 削除（`getState` 未 override のため未使用） | `notebook/RescriptNotebookFileEditor.kt:116` |
| D4 | DOT ID サニタイザ重複削除（`MermaidLabelEscaping.sanitize` を再利用） | `flow/RescriptVariantFlowDotExporter.kt:51-54` |
| D5 | LSP suppress 15 箇所の stale コメント修正（「2026.1.2 に代替 API が存在しない」→ 実際は 2026.2.0.1 でビルド中で API は存在。正しい理由は移行規模。`plugin-verifier-ignored-problems.txt` 側は訂正済み） | `lsp/` `navigation/` `refactor/` `inspection/` `settings/` 13 ファイル |
| D6 | 例外処理の狭窄化（代表 12 箇所を `IOException`/`JsonSyntaxException` 等へ。`try` ブロックの縮小含む） | `lsp/RescriptExpressionTypeProvider.kt:77` / `lsp/RescriptGlobExpander.kt:151` / `lsp/RescriptWorkspaceDiscovery.kt:121,172` / `util/RescriptProjectFileScanner.kt:83` / `dependencies/RescriptDependenciesPanel.kt:184` / `projectview/RescriptProjectViewNodeDecorator.kt:42` / `lsp/RescriptWorkspaceFileParser.kt:56,109,115` / `settings/RescriptSettingsValidator.kt:153` / `repl/RescriptReplExecutor.kt:39`（ログ追加） |

補足: `navigation/RescriptFileIncludeProvider.kt:55` の suppress は正当（2026.1.x で abstract のため）につき維持。TODO/FIXME 型の技術的負債コメントは src/main に存在しない（全ヒットが機能上の文字列）。

## 残存する不確実性

- Track C の `MonorepoTemplateFiles` / `RescriptReanalyzeServerService` は構造のみ確認（全行読解はしていない）
- 走査範囲は `src/main` のみ。`src/test` の重複は対象外
- LSP API 移行（suppress 自体の解消）は既存ステアリング `20260809-001-lsp-client-api-migration` の管轄であり本監査のスコープ外
