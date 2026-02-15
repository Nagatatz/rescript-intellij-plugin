# Requirements: セマンティックハイライティング

## 概要

LSP の `textDocument/semanticTokens/full` を活用し、rescript-language-server が提供するセマンティックトークン情報をエディタのハイライティングに反映する。レクサーベースのシンタックスハイライトでは区別できない「変数」「型名」「モジュール名」「バリアント」「レコードフィールド」等を、意味解析に基づいて正確にカラーリングする。

## 背景

現在のハイライティングはレクサーベースのみであり、以下の制約がある:

- **小文字識別子の区別不可**: `LIDENT` トークンは変数名・型名・レコードフィールド・関数名を区別できない
- **大文字識別子の一律ハイライト**: `UIDENT` は全てモジュール名色で表示されるが、実際にはバリアントコンストラクタの場合もある
- **JSX 要素の文脈依存性**: `<div>` のようなHTML要素と `<Component>` の区別はレクサーで対応済みだが、LSP による正確な分類も提供可能

セマンティックハイライティングにより、VSCode の ReScript 拡張と同等の視覚的なコード理解を実現する。

## 機能要件

### FR-01: LSP セマンティックトークンの受信と適用

- IntelliJ Platform の `LspSemanticTokensSupport` API を使用して、rescript-language-server からセマンティックトークンを受信する
- `.res` / `.resi` ファイルに対してセマンティックトークンを要求する
- 受信したトークンをエディタのハイライティングに反映する

### FR-02: トークンタイプのマッピング

rescript-language-server が提供する8種類のセマンティックトークンタイプを、プラグインの `TextAttributesKey` にマッピングする:

| LSP トークンタイプ | ReScript での意味 | マッピング先 |
|---|---|---|
| `variable` | 変数・パラメータ | `RESCRIPT_SEMANTIC_VARIABLE` |
| `type` | 型名 | `RESCRIPT_SEMANTIC_TYPE` |
| `namespace` | モジュール名 | `RESCRIPT_SEMANTIC_NAMESPACE` |
| `enumMember` | バリアント・コンストラクタ | `RESCRIPT_SEMANTIC_ENUM_MEMBER` |
| `property` | レコードフィールド | `RESCRIPT_SEMANTIC_PROPERTY` |
| `interface` | JSX HTML 要素（div, span等） | `RESCRIPT_SEMANTIC_INTERFACE` |
| `operator` | 演算子 | `RESCRIPT_SEMANTIC_OPERATOR` |
| `modifier` | JSX ブラケット（<, >, />） | `RESCRIPT_SEMANTIC_MODIFIER` |

### FR-03: カラースキーム設定

- Settings > Editor > Color Scheme > ReScript > Semantic カテゴリで各トークンタイプの色をカスタマイズ可能（既存の `RescriptColorSettingsPage` で対応済み）
- Default / Darcula テーマ用のデフォルトカラーは既存の `colorSchemes/*.xml` で定義済み

## 非機能要件

### NFR-01: パフォーマンス

- セマンティックハイライティングの適用はエディタの応答性に影響を与えない（IntelliJ Platform のアノテータ機構により非同期処理される）

### NFR-02: フォールバック

- LSP サーバーが利用不可の場合、レクサーベースのハイライティングが引き続き正常動作する
- セマンティックトークンが取得できない場合でも、既存のハイライティングに影響しない

### NFR-03: 保守性

- IntelliJ Platform の公式 LSP API（`LspSemanticTokensSupport`）のみを使用し、内部 API への依存を避ける
- 実装は最小限のコード量（1クラス + 既存クラスの数行修正）に留める

## 受け入れ条件

- [ ] `.res` ファイルを開いた際に、LSP からセマンティックトークンが取得され、ハイライティングに反映される
- [ ] 変数、型名、モジュール名、バリアント、レコードフィールドがそれぞれ異なる色で表示される
- [ ] Settings > Editor > Color Scheme > ReScript で Semantic カテゴリの色をカスタマイズできる
- [ ] LSP サーバーが利用不可の場合でもレクサーベースのハイライティングが正常動作する
- [ ] `./gradlew buildPlugin` が成功する

## スコープ外

- セマンティックトークンの差分更新（`textDocument/semanticTokens/full/delta`）— rescript-language-server が未対応
- レンジベースのセマンティックトークン（`textDocument/semanticTokens/range`）— rescript-language-server が未対応
- セマンティックハイライティングの有効/無効切り替え UI — 将来対応
