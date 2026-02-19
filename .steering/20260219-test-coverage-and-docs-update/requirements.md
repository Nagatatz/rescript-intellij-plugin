# Requirements: テストカバレッジ拡充 & 公開ドキュメント更新

## 概要

テスト未対応の主要ソースファイルにユニットテストを追加し、README.md と plugin.xml の機能説明を実態に合わせて更新する。

## 背景

- ソースファイル 73 に対しテストファイル 29（約 40%）。テスト可能なロジックを含むファイルの多くが未カバー
- README.md は 14 機能しか記載がないが、実際は 40 以上の機能を提供
- plugin.xml の `<description>` は 10 機能のみ記載。Marketplace ユーザーに提供内容が正しく伝わらない

## 要求事項

### 1. テストカバレッジ拡充

以下のファイルにユニットテストを追加する:

| # | ファイル | テスト対象のロジック |
|---|---------|-------------------|
| 1 | `RescriptNamesValidator.kt` | `isIdentifier()` / `isKeyword()` — 純粋ロジック、外部依存なし |
| 2 | `RescriptCommand.kt` | `fromId()` — enum ルックアップ |
| 3 | `RescriptPsiUtils.kt` | `extractName()` / `getIcon()` / `getElementDescription()` — PSI ユーティリティ |
| 4 | `RescriptBreadcrumbsProvider.kt` | `acceptElement()` / `getElementInfo()` — PSI 委譲 |
| 5 | `RescriptStructureViewElement.kt` | `getAlphaSortKey()` / `getPresentation()` / `getChildren()` — PSI 走査 |
| 6 | `RescriptDuplicateOpenInspection.kt` | 重複 open 検出ロジック |
| 7 | `RescriptEmptyModuleInspection.kt` | 空モジュール検出ロジック |
| 8 | `RescriptPostfixTemplateProvider.kt` | テンプレート定義の検証、`isTerminalSymbol()` |

**テスト省略対象（理由付き）:**
- `RescriptMissingConfigInspection.kt` — `LocalFileSystem.getInstance()` を使用しており、IntelliJ Platform テストフレームワーク（Heavy Test）が必要。単体テストが困難
- LSP 関連クラス — LSP サーバーとの結合が必須
- UI コンポーネント（`RescriptSettingsEditor`, `RescriptConfigurable` 等）— Swing ベース
- シンプルなファクトリ/シングルトン（`RescriptLanguage`, `RescriptIcons`, `RescriptFileTypes` 等）— テストの価値が低い

### 2. 公開ドキュメント更新

#### README.md
- Features セクションを実際の機能一覧に更新（カテゴリ別に整理）
- 現在の記載（14 機能）を全機能に拡充

#### plugin.xml `<description>`
- 機能一覧を実態に合わせて更新
- Marketplace で表示される内容として適切な粒度で記載

## 受け入れ条件

- [ ] 上記 8 ファイルに対するユニットテストが追加されている
- [ ] 全テストが `./gradlew test` で通過する
- [ ] README.md の Features セクションが全機能を網羅している
- [ ] plugin.xml の `<description>` が主要機能を網羅している
- [ ] `./gradlew buildPlugin` が成功する

## 制約事項

- 既存テストのパターン（JUnit 4、スタブベースの PSI モック）に従う
- `RescriptImportOptimizerTest` の `stubProxy()` / `stubAstNode()` パターンを再利用する
- プロダクションコードへの変更は最小限にする
