# 要求定義: KDoc コメント欠落補完 & ドキュメント品質改善

## 背景

コードコメント規約 (`.claude/rules/code-comments.md`) に基づく監査で、内部データクラス・sealed class サブクラスを中心に 43 箇所の KDoc 欠落が検出された。また、ドキュメント監査で sphinx-docs の軽微な記載不足と product-requirements.md のステータス不整合が発見された。

## 要求

### 1. KDoc コメント欠落の補完

以下の 43 箇所に英語 KDoc コメントを付与する:

| カテゴリ | 件数 | 対象ファイル |
|---------|------|-------------|
| Wizard テンプレート `internal object` | 10 | `wizard/templates/*.kt` (10ファイル) |
| `DtsJsonModel` 内部データクラス | 20 | `binding/DtsJsonModel.kt` |
| `TypeShape` sealed サブクラス | 3 | `generate/RescriptTypeDeclarationParser.kt` |
| `RescriptReanalyzeAnnotator` 内部データクラス | 3 | `analysis/RescriptReanalyzeAnnotator.kt` |
| LSP パラメータデータクラス | 3 | `lsp/RescriptLsp4jClient.kt`, `lsp/RescriptCompilationStatusService.kt` |
| Private inner `AnAction` | 2 | `preview/RescriptCompiledJsPreviewPanel.kt` |
| `RescriptProjectSettings.State` | 1 | `settings/RescriptProjectSettings.kt` |
| `SmartEnterProcessor.LineAnalysis` | 1 | `editor/RescriptSmartEnterProcessor.kt` |

### 2. ドキュメント品質改善

| 対象 | 改善内容 |
|------|---------|
| `sphinx-docs/user/features/code-editing.md` | "Paste as ReScript" のアクセス方法を追記 |
| `sphinx-docs/user/features/advanced.md` | "Predefined Code Style" に Settings パスを追記、"Color Preview" にコード例を追記 |
| `sphinx-docs/user/features/index.md` | Run/Build, Testing, Advanced ページへの言及を追加 |
| `docs/product-requirements.md` Section 6 | ユーザーストーリーの受け入れ条件チェックボックスを `[x]` に更新 |
| `docs/product-requirements.md` Section 4 | Marketplace ステータスを現状に更新 |

## 受け入れ条件

- すべての `class` / `object` / `enum class` / `sealed class` / `interface` に KDoc が付与されている
- sphinx-docs の指摘箇所がすべて改善されている
- product-requirements.md のステータスが最新に更新されている
- `./gradlew clean buildPlugin` が成功する
