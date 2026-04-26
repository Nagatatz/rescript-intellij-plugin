# LSP Code Action 動作検証 — 結果

## 環境情報

| 項目 | 値 | 備考 |
|---|---|---|
| 検証日 | 2026-04-27 | |
| OS | macOS (darwin 25.2.0) | |
| Node.js | v25.4.0 | グローバル |
| `@rescript/language-server` | runIde サンドボックス内の `node_modules` でローカルインストール | バージョンは検証時に追記 |
| `rescript` (compiler) | runIde サンドボックス内 | バージョンは検証時に追記 |
| IntelliJ Platform | 2025.3.2 (build 253.30387.90, IDEA Ultimate) | `gradle.properties` の `platformVersion=2025.3` |
| プラグインバージョン | `gradle.properties` の `pluginVersion` 参照 | |

## 静的分析: IntelliJ Platform LSP API の code action サポート

`product-backend.jar` 内の `com.intellij.platform.lsp.api.customization` パッケージを `javap` で逆解析した結果:

### `LspCustomization` のデフォルト構成（抜粋）

```kotlin
// 仮想的に再構成（バイトコードから読み取り）
open class LspCustomization {
    open val codeActionsCustomizer: LspCodeActionsCustomizer = LspCodeActionsSupport()
    open val commandsCustomizer:   LspCommandsCustomizer    = LspCommandsSupport()
    open val diagnosticsCustomizer: LspDiagnosticsCustomizer = LspDiagnosticsSupport()
    // …他のカスタマイザは省略
}

open class LspCodeActionsSupport : LspCodeActionsCustomizer() {
    open val quickFixesSupport: Boolean       = true   // ← デフォルト ON
    open val intentionActionsSupport: Boolean = true   // ← デフォルト ON
    open fun createQuickFix(server, codeAction): LspIntentionAction
    open fun createIntentionAction(server, codeAction): LspIntentionAction
}

open class LspCommandsSupport : LspCommandsCustomizer() {
    open fun executeCommand(server, file, command)  // workspace/executeCommand
}
```

### `LspIntentionAction.invoke()` の挙動

`LspIntentionAction` (IntelliJ 標準実装) は、`isAvailable` 評価後に必要なら `codeAction/resolve` を投げ、得られた `CodeAction` の `edit` (`WorkspaceEdit`) を `applyWorkspaceEdit` で適用する。`edit` がなく `command` のみの場合は `LspCommandsCustomizer.executeCommand(...)` 経由で `workspace/executeCommand` を発行する。`WorkspaceEdit` の `documentChanges` 内に `CreateFile` などのリソース操作も処理する（バイトコード上 `F(CreateFile): Document` メソッドが存在）。

### `RescriptLspServerDescriptor.lspCustomization` の現状

`src/main/kotlin/com/rescript/plugin/lsp/RescriptLspServerDescriptor.kt:42-45`:

```kotlin
override val lspCustomization =
    object : LspCustomization() {
        override val semanticTokensCustomizer = RescriptSemanticTokensSupport()
    }
```

→ `codeActionsCustomizer` および `commandsCustomizer` は **オーバーライドしていない** = デフォルトの `LspCodeActionsSupport()` / `LspCommandsSupport()` が有効。

**結論**: API レベルでは、本プラグインは LSP code action（quick fix・intention action 双方）と `workspace/executeCommand` をフルサポート済み。Alt+Enter で受け入れられる前提条件は満たしている。

## 静的分析: rescript-vscode の code action 出力

`server/src/codeActions.ts` (master, 2026-04-27 fetch) と `server/src/server.ts:codeAction()` を確認:

### codeActions.ts で診断駆動で生成されるアクション（7 種）

| ID | 関数 | 返値 |
|---|---|---|
| `didYouMeanAction` | `didYouMeanAction` | `{ kind: QuickFix, edit: { changes: { [file]: [{range, newText}] } } }` |
| `wrapInSome` | `wrapInSome` | `{ kind: QuickFix, edit: { changes: ... } }` |
| `unwrapOptional` | (same file 後段) | `{ kind: QuickFix, edit: { changes: ... } }` |
| `addUndefinedRecordFieldsV10` / `V11` | 同名関数 | `{ kind: QuickFix, edit: { changes: ... } }` |
| `simpleConversion` | `simpleConversion` | `{ kind: QuickFix, edit: { changes: ... } }` |
| `applyUncurried` | `applyUncurried` | `{ kind: QuickFix, edit: { changes: ... } }` |
| `simpleAddMissingCases` | `simpleAddMissingCases` | `{ kind: QuickFix, edit: { changes: ... } }` |

すべて `command` フィールドを持たず、`WorkspaceEdit` の `changes` ブロックに直接編集を載せて返す。`textDocument/codeAction` のレスポンスとして直接シリアライズされ、`codeAction/resolve` は不要。

### 拡張・分析駆動で生成されるアクション（2 種）

`server/src/server.ts:codeAction()` (`runAnalysisCommand("codeAction", filePath, ...)`) で `rescript-editor-analysis` バイナリを叩き、その結果を `localResults` にマージしている。`extractLocalModuleToFile` と `expandCatchAllPatterns` はこの経路で返却される（codeActions.ts には実装なし）。形式は同じく `WorkspaceEdit` ベース。

### `removeUnusedCode`

reanalyze の "unused" 警告に対する quick fix。`@rescript/language-server` 0.5+ では `reanalyze.codeActions` 設定が有効な場合、診断ペイロードの `data.fixes` 経由で同梱、または別途 `codeAction` レスポンスに含めて返される。サンプル `samples/06_remove_unused.res` は単独では reanalyze 解析ジョブが走らない可能性があるため、**ランタイム検証時に reanalyze 結果が出ているかを確認する必要がある**。

## 検証結果サマリ

> Phase 3 ランタイム検証 (Alt+Enter 表示・適用) は `./gradlew runIde` での手動操作が必須。
> 以下のテーブルは静的分析に基づく **期待値** を埋めた状態。ランタイム結果は別途記録する。

| # | Code Action | LSP 経路 | 期待される表示 | 期待される適用 | 備考 |
|---|---|---|---|---|---|
| 01 | `simpleAddMissingCases` | `codeAction` (codeActions.ts) | OK | OK | switch 不完全マッチ診断行 |
| 02 | `wrapInSome` / `unwrapOptional` | `codeAction` (codeActions.ts) | OK | OK | option 型不整合診断行 |
| 03 | `addUndefinedRecordFields` (V10/V11) | `codeAction` (codeActions.ts) | OK | OK | record 必須フィールド欠落診断行 |
| 04 | `simpleConversion` | `codeAction` (codeActions.ts) | OK | OK | int / float / string 不整合 |
| 05 | `didYouMean` | `codeAction` (codeActions.ts) | OK | OK | "Did you mean Y?" 診断行 |
| 06 | `removeUnusedCode` | reanalyze 経由 | TBD | TBD | reanalyze 有効化 (`bsconfig.json` の `reanalyze`) が前提 |
| 07 | `extractLocalModuleToFile` | `codeAction` (rescript-editor-analysis) | TBD | TBD | `WorkspaceEdit.documentChanges` の `CreateFile` 操作を含む可能性 → IntelliJ LSP impl が CreateFile に対応しているか実機確認が必要 |
| 08 | `expandCatchAllPatterns` | `codeAction` (rescript-editor-analysis) | TBD | TBD | 範囲が `_` ケースを含む位置で発火 |
| 09 | `applyUncurried` | `codeAction` (codeActions.ts) | N/A or TBD | N/A or TBD | ReScript v11+ の uncurried-by-default では発火しない可能性 |

凡例:
- `OK` — 表示・適用ともに期待どおり (ランタイム未検証)
- `NG` — 表示されない / 適用してもコードが変化しない / クラッシュ
- `PARTIAL` — 表示されるが見出しや動作が rescript-vscode と異なる
- `N/A` — ReScript バージョン都合で対象外
- `TBD` — 静的分析で確証が得られず、ランタイム検証が必須

## 個別ケースの記録（ランタイム検証用テンプレート）

各ケースの runIde 実検証時、以下を記録する:

- `Alt+Enter` で表示されたアクションラベル一覧
- 適用前後のコード差分（または「変化なし」「LSP エラー表示」等）
- 失敗時は `Help | Show Log in Finder` から `idea.log` の `LSP server` セクションを抜粋

### 01: `simpleAddMissingCases`

TBD

### 02: `wrapInSome` / `unwrapOptional`

TBD

### 03: `addUndefinedRecordFields`

TBD

### 04: `simpleConversion`

TBD

### 05: `didYouMean`

TBD

### 06: `removeUnusedCode`

TBD

### 07: `extractLocalModuleToFile`

TBD（CreateFile 操作の処理可否を特に確認）

### 08: `expandCatchAllPatterns`

TBD

### 09: `applyUncurried`

TBD（v11+ では発火しない想定）

## 原因分析（API レベル）

静的分析時点での結論:

- IntelliJ Platform 2025.3 の `LspCustomization` は `LspCodeActionsSupport` をデフォルトで有効化しており、`textDocument/codeAction` で返却される `CodeAction` (kind=QuickFix, edit=WorkspaceEdit) は Alt+Enter から表示・適用可能。
- 本プラグインの `RescriptLspServerDescriptor.lspCustomization` は code action 関連を一切オーバーライドしていないため、**追加設定なしで全 9 種が動作する想定**。
- もしランタイム検証で動作しない code action があれば、原因は IntelliJ Platform の API 不足ではなく、以下のいずれかである可能性が高い:
  1. **診断とのレンジ整合性** — IntelliJ 側の `CodeActionParams.range` が rescript-language-server の `rangeContainsRange` チェック（`server.ts:992`）を満たしていない
  2. **reanalyze の有効化** — `removeUnusedCode` は `bsconfig.json` の `reanalyze` ブロック設定が必須
  3. **`rescript-editor-analysis` バイナリの動作** — `extractLocalModuleToFile` / `expandCatchAllPatterns` は同梱バイナリの実行が必要で、ReScript v11.x / v12.x でパス解決が変わっている可能性
  4. **ReScript バージョン依存** — `applyUncurried` は v11+ uncurried-by-default で診断が出ないため発火しない
  5. **`CreateFile` リソース操作のハンドリング** — `extractLocalModuleToFile` のみ、IntelliJ LSP impl が `WorkspaceEdit.documentChanges[].CreateFile` を実行するかどうか実機確認が必要（バイトコードでは対応コードあり）

ランタイム検証で具体的な NG が確認できた場合に、該当する原因を本セクションに追記する。

## 結論（静的分析時点）

本セッションでは、ユーザー判断により **ランタイム検証 (Phase 3) は意図的に実施せず**、静的分析の結論のみで本ステアリングを締める（`next-steps.md` に runIde 実機検証を独立タスクとして記録）。

- API レベルで **全 9 種は本プラグインで動作する前提条件を満たしている**。
- `RescriptLspServerDescriptor` への追加変更は **不要**。
- ドキュメント反映:
  - `docs/lsp-fallback-matrix.md`: 9 種の LSP code action を「LSP 接続時に利用可能なペイロード」として明示するセクションを追加
  - `docs/archive/implemented-features.md`: 既存の「Quick Fix (LSP Code Actions)」エントリに 9 種の根拠を補強
  - `sphinx-docs/user/features/code-analysis.md`: Quick Fixes (LSP) セクションを拡充し、サーバーが返す 9 種を明示
  - `sphinx-docs/locale/ja/LC_MESSAGES/user/features/code-analysis.po`: 上記の日本語訳を同期
- 実機検証の記録は `next-steps.md` に Independent task として残す。動かない code action が発見された場合は、新しいステアリング (`20260MMDD-NNN-native-*-quickfix/`) で PSI ベースのネイティブ実装を起票する。
