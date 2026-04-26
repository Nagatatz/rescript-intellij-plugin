# LSP Code Action 動作検証 — 結果

## 環境情報

| 項目 | 値 | 備考 |
|---|---|---|
| 検証日 | 2026-04-27 | |
| OS | macOS (darwin 25.2.0) | |
| Node.js | v25.4.0 | グローバル |
| `@rescript/language-server` | runIde サンドボックス内の `node_modules` でローカルインストール | バージョンは検証時に追記 |
| `rescript` (compiler) | runIde サンドボックス内 | バージョンは検証時に追記 |
| IntelliJ Platform | `gradle.properties` の `platformVersion` 参照 | |
| プラグインバージョン | `gradle.properties` の `pluginVersion` 参照 | |

## 検証結果サマリ

検証実施後に以下のテーブルを埋める。

| # | Code Action | 表示 | 適用 | 備考 |
|---|---|---|---|---|
| 01 | `simpleAddMissingCases` | TBD | TBD | |
| 02 | `wrapInSome` / `unwrapOptional` | TBD | TBD | |
| 03 | `addUndefinedRecordFields` (V10/V11) | TBD | TBD | |
| 04 | `simpleConversion` | TBD | TBD | |
| 05 | `didYouMean` | TBD | TBD | |
| 06 | `removeUnusedCode` | TBD | TBD | reanalyze 有効化が必要 |
| 07 | `extractLocalModuleToFile` | TBD | TBD | |
| 08 | `expandCatchAllPatterns` | TBD | TBD | |
| 09 | `applyUncurried` | TBD | TBD | ReScript v11+ では非該当の可能性 |

凡例:
- `OK` — 表示・適用ともに期待どおり
- `NG` — 表示されない / 適用してもコードが変化しない / クラッシュ
- `PARTIAL` — 表示されるが見出しや動作が rescript-vscode と異なる
- `N/A` — ReScript バージョン都合で対象外

## 個別ケースの記録

検証実施後、各ケースで以下を記録する:

- 表示されたアクションラベル一覧
- 適用前後のコード差分
- 失敗時は `idea.log` の LSP セクション抜粋

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

TBD

### 08: `expandCatchAllPatterns`

TBD

### 09: `applyUncurried`

TBD

## 原因分析（動作しなかった code action）

検証実施後に追記。`com.intellij.platform.lsp.api.customization.LspCustomization` の API 一覧と `RescriptLspServerDescriptor.kt` の現状設定を比較し、不足設定を特定する。

## 結論

検証実施後に追記:

- 動作する code action 数: TBD / 9
- 設定追加で動かせる可能性のある code action: TBD
- ネイティブ実装が必要な code action: TBD
- ロードマップへの反映: TBD（`docs/product-requirements.md` への追記項目）
