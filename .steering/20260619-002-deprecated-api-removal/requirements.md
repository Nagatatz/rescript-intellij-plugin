# requirements — Deprecated API 利用の除去・抑制 (2026.2 EAP 基準 34 件)

## 背景

`./gradlew verifyPlugin`（`recommended()` = 2025.3 / 2026.1 / 2026.2 EAP）の 2026.2 EAP レポート
（`build/reports/pluginVerifier/IU-262.8117.19/.../0.1.16.2/deprecated-usages.txt`）で
**deprecated API 利用が 34 件** 検出された。本作業はこの 34 件を `.claude/rules/deprecated-api.md`
に準拠して全件 disposition することを目的とする。

なお `build.gradle.kts` には `failureLevel` 明示設定がなく、deprecated 利用は現状ビルドを
fail させない（情報レベル警告）。したがって本作業は「ビルド破壊の修正」ではなく
「規約準拠の除去 + 抑制文書化」である。

## 34 件の分類（バイトコードレベルで 2026.2 EAP / 2026.1.2 両 jar を突合した確定結果）

| 区分 | 件数 | 内容 | コンパイルターゲット 2026.1.2 での状況 | disposition |
|------|------|------|----------------------------------------|-------------|
| A. LSP API | 31 | `com.intellij.platform.lsp.api.*`（`LspServer` / `LspServerManager` / `LspServerDescriptor` / `ProjectWideLspServerDescriptor` / `LspServerSupportProvider(.LspServerStarter)` とその method 群） | **2026.1.2 では非 deprecated**。代替 `LspClientDescriptor` / `ProjectWideLspClientDescriptor` は **2026.1.2 に存在しない**（2026.2 EAP で新規導入）。プロジェクト方針上 EAP に対してコンパイルしない | **@Suppress + ignored-problems**（代替 API が無いため抑制。`.claude/rules/deprecated-api.md`「代替 API が存在しない場合のみ抑制」に該当） |
| B. lsp4j MarkedString | 1 | `RescriptExpressionTypeProvider.getInformationHint` の `it.right.value`（`org.eclipse.lsp4j.MarkedString.value`） | 代替 `MarkupContent` は lsp4j に存在。hover の legacy left 分岐の `MarkedString` 部分のみが対象 | **真の除去**（left 分岐から `MarkedString.value` の利用を落とす。rescript-language-server は MarkupContent を返すため挙動影響なし） |
| C. 処理済み | 2 | `FloatingToolbarProvider.isApplicable`（行 25）/ `FileIncludeProvider.acceptFile`（行 30） | 既に `@Suppress` + ignored-problems で disposition 済み | **action 不要**（既存エントリの維持のみ） |

## 受け入れ条件

- [ ] B（MarkedString）1 件を真に除去し、verifyPlugin 2026.2 EAP レポートから当該行が消える
- [ ] A（LSP API）31 件すべての利用箇所に `@Suppress("DEPRECATION")`（override は `"OVERRIDE_DEPRECATION"`）を付与し、1 行の理由コメントを添える
- [ ] A 31 件すべてを `plugin-verifier-ignored-problems.txt` でカバーする regex エントリを追加する（`Status: KEEP` / `Reviewed: 2026-06-19` / `Expires: 2027-06-19` / Source ファイル名 / 代替 API 不在の理由）
- [ ] C（処理済み 2 件）は変更しない
- [ ] `./gradlew ktlintCheck` / `./gradlew clean buildPlugin` / `./gradlew test` がすべて成功する
- [ ] 既存挙動（hover 型表示・LSP 起動・補完等）が壊れていない（コンパイル + 既存テスト緑で担保）
- [ ] verifyPlugin 再実行で 2026.2 EAP の deprecated 利用が ignored-problems により抑制 / 除去され、未文書化の残件が無い

## 非対象

- Experimental API 利用（`ImmediateConfigurable` / `NoSettings` / `SettingsKey` / `VcsCodeVisionLanguageContext`）。
  これらは deprecated ではなく experimental であり本作業のスコープ外。
- platformVersion の 2026.2 への引き上げ（2026.2 が stable 化していないため）。
  2026.2 stable 化後に A 31 件を `LspClientDescriptor` へ一括移行する方針はメモとして残す。
