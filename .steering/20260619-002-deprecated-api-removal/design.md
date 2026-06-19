# design — Deprecated API 利用の除去・抑制

## 全体方針

`.claude/rules/deprecated-api.md` の二択（代替 API があれば移行 / 無ければ `@Suppress` + ignored-problems 抑制）に従う。

- **B (MarkedString) → 移行（真の除去）**: lsp4j の `MarkedString` は同 API 内の `MarkupContent` で代替可能。legacy left 分岐の `MarkedString.value` 利用を落とす。
- **A (LSP API 31 件) → 抑制**: 代替 `LspClientDescriptor` / `ProjectWideLspClientDescriptor` は 2026.2 EAP 新規導入であり、コンパイルターゲット 2026.1.2 に存在しない。EAP に対してはコンパイルしない方針のため移行不可。`@Suppress` + ignored-problems で抑制する。

## B: MarkedString の真の除去

### 対象

`src/main/kotlin/com/rescript/plugin/lsp/RescriptExpressionTypeProvider.kt` の `getInformationHint`。
hover の `content: Either<List<Either<String, MarkedString>>, MarkupContent>` のうち、left 分岐で
`it.right.value`（`MarkedString.getValue()`）を参照している箇所がバイトコード上の deprecated 参照。

### 変更内容

left 分岐を「plain-string 部分のみ取り出す」形に変更し、`MarkedString` 部分の処理を落とす:

```kotlin
content.isLeft -> {
    // Legacy hover format List<Either<String, MarkedString>>: only the
    // plain-string variant is consumed. The MarkedString variant (deprecated
    // in lsp4j) is intentionally dropped — rescript-language-server returns
    // MarkupContent (the isRight branch below).
    content.left
        .firstOrNull()
        ?.takeIf { it.isLeft }
        ?.left
}
```

`it.left` は `String` を返すためバイトコードに `MarkedString` クラス参照が残らない
（型引数 `Either<String, MarkedString>` はジェネリック消去で参照を生まない）。
`content.isRight`（`MarkupContent.value`）の分岐は非 deprecated なので不変。

### 残存する LspServer 利用

同ファイルは `RescriptLspUtils.getServer`（report 行 29 の `LspServer` 参照）も持つため、
クラスまたは該当メソッドに `@Suppress("DEPRECATION")` を付け、ignored-problems の A 側でカバーする。

## A: LSP API 31 件の抑制

### @Suppress 付与方針

- 利用箇所（メソッド単位、または override メンバ）に `@Suppress("DEPRECATION")` を付ける。
  override メンバ（`fileOpened` / `prepareRename` 等インタフェース実装）は `"OVERRIDE_DEPRECATION"` を併記する。
- 各 `@Suppress` 直上に 1 行コメントで理由（代替 `LspClientDescriptor` が 2026.1.2 に未導入）を記す。
- 粒度は「ファイルあたり最小数の `@Suppress`」を狙い、クラス全体が LSP API に依存する
  `RescriptLspServerDescriptor` / `RescriptLspServerSupportProvider` はクラス宣言に付ける。

### 対象 13 ファイルと report 行の対応

| ファイル | report 行 | 付与位置 |
|---------|-----------|----------|
| `lsp/RescriptDumpLspStateAction.kt` | 1,2,21,33 | `collectLspState` メソッド（lambda 含む） |
| `lsp/RescriptLspUtils.kt` | 3,6,14,18,24 | `getServer` / `getHoverType` メソッド |
| `codevision/RescriptCodeVisionProvider.java` | 4,28 | `computeForEditor`（Java は `@SuppressWarnings("deprecation")`） |
| `inspection/RescriptSignatureSyncInspection.kt` | 5 | `applyFix` メソッド |
| `lsp/RescriptLspInstaller.kt` | 7,8,32 | `onInstallSuccess` メソッド |
| `lsp/RescriptLspServerDescriptor.kt` | 9,31 | クラス宣言（`ProjectWideLspServerDescriptor` 継承） |
| `navigation/RescriptOpenCompiledJsAction.kt` | 10 | `tryOpenViaLsp` メソッド |
| `lsp/RescriptLspServerSupportProvider.kt` | 11,19,20 | クラス宣言 + `fileOpened`（override） |
| `lsp/RescriptRestartLspAction.kt` | 12,13,26 | `actionPerformed` メソッド |
| `settings/RescriptConfigurable.kt` | 16,17,23 | `apply` メソッド |
| `navigation/RescriptCreateInterfaceAction.kt` | 22 | `actionPerformed` メソッド |
| `refactor/RescriptRenameHandler.kt` | 27,34 | `prepareRename` / `invoke` メソッド |
| `lsp/RescriptExpressionTypeProvider.kt` | 29 | `getInformationHint` メソッド |

### ignored-problems エントリ

Plugin Verifier はバイトコードで検出するため、`@Suppress` だけでは消えない。
`plugin-verifier-ignored-problems.txt` に正規表現エントリを追加する。
既存の FloatingToolbarProvider / FileIncludeProvider エントリと同じ書式:

```
# LSP API (com.intellij.platform.lsp.api.*) — deprecated in 2026.2 EAP.
# Replacement LspClientDescriptor / ProjectWideLspClientDescriptor was introduced
# in 2026.2 EAP and does not exist on the 2026.1.2 compile target, so migration is
# impossible without compiling against EAP (against project policy).
# Source: lsp/RescriptLspServerSupportProvider.kt, lsp/RescriptLspServerDescriptor.kt,
#   lsp/RescriptLspUtils.kt, lsp/RescriptDumpLspStateAction.kt, lsp/RescriptLspInstaller.kt,
#   lsp/RescriptRestartLspAction.kt, lsp/RescriptExpressionTypeProvider.kt,
#   codevision/RescriptCodeVisionProvider.java, inspection/RescriptSignatureSyncInspection.kt,
#   navigation/RescriptOpenCompiledJsAction.kt, navigation/RescriptCreateInterfaceAction.kt,
#   refactor/RescriptRenameHandler.kt, settings/RescriptConfigurable.kt
# Status: KEEP  Reviewed: 2026-06-19  Expires: 2027-06-19
com\.rescript\.plugin:.*Deprecated.*com\.intellij\.platform\.lsp\.api\..*
```

正規表現の正確な書式は既存エントリ（行頭 `com\.rescript\.plugin:` 形式）に合わせて
ファイル冒頭のヘッダ説明と突合してから確定する。1 本で 31 件をカバーできない場合は
API クラスごと（LspServer / LspServerManager / LspServerDescriptor /
ProjectWideLspServerDescriptor / LspServerSupportProvider）に分割する。

## テスト方針

- **B (MarkedString 除去)**: 対象 `RescriptExpressionTypeProvider` は `ExpressionTypeProvider` 実装かつ
  `RescriptLspUtils.getServer`（LSP サーバー結合必須）に依存するため `.claude/rules/testing.md` の
  **LSP サーバー結合必須** 免除に該当。`getInformationHint` 自体は LSP server 無しで駆動できない。
  pure 関数 `extractTypeFromMarkdown` の既存テストがあれば緑を確認、無ければ免除（tasklist に明記）。
- **A (抑制)**: コード挙動を変えない `@Suppress` アノテーション追加のみ。新規ロジック無し → 新規テスト不要。
  既存テストが緑であることで回帰なしを担保。

## ドキュメント同期

ユーザー向け機能の追加・変更が無い（deprecated 抑制 + 内部 hover 分岐の縮小のみ）ため、
`.claude/rules/documentation.md` の機能ドキュメント同期（CLAUDE.md レイヤー3 / README / sphinx）は対象外。
`docs/product-requirements.md` の「既知ブロッカー」記述は LSP API 抑制の経緯を 1 行追記する余地があるが、
本タスクでは ignored-problems のヘッダコメントに集約し PRD は変更しない（過剰更新回避）。

## コミット分割

1. `🐛 Drop deprecated lsp4j MarkedString handling from hover type provider`（B: コード除去）
2. `🔧 Suppress irremovable LSP-API deprecations flagged by 2026.2 EAP verifier`（A: @Suppress + ignored-problems）
3. `📝 Add steering docs for deprecated-API removal/suppression`（steering 一式、最初か最後にまとめる）

A の @Suppress（src 変更）と ignored-problems（設定）は同一 disposition なので 1 コミットに含める。
