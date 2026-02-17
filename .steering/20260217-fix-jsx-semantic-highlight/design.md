# Design: JSX タグの LSP セマンティックトークンによるハイライト消失修正

## 修正方針

### 1. フォールバックチェーンの修正 (`RescriptSyntaxHighlighter.kt`)

セマンティックトークンキーのフォールバック先を Platform デフォルトからプラグイン独自キーに変更:

| キー | 変更前 | 変更後 |
|------|--------|--------|
| `SEMANTIC_INTERFACE` | `Defaults.MARKUP_TAG` | `MARKUP_TAG` |
| `SEMANTIC_NAMESPACE` | `Defaults.CLASS_NAME` | `MODULE_NAME` |
| `SEMANTIC_ENUM_MEMBER` | `Defaults.CONSTANT` | `POLY_VARIANT` |
| `SEMANTIC_OPERATOR` | `Defaults.OPERATION_SIGN` | `OPERATOR` |

### 2. `else` ブランチの修正 (`RescriptSemanticTokensSupport.kt`)

`super.getTextAttributesKey(tokenType, modifiers)` → `null` に変更。`null` を返すと IntelliJ Platform はそのトークン範囲のレクサーベースハイライトを保持する。

### 3. カラースキーム XML にセマンティックトークン色を追加

Darcula / Default 両テーマに `RESCRIPT_SEMANTIC_*` の色定義を追加。

## 修正対象ファイル

1. `src/main/kotlin/com/rescript/plugin/highlight/RescriptSyntaxHighlighter.kt`
2. `src/main/kotlin/com/rescript/plugin/lsp/RescriptSemanticTokensSupport.kt`
3. `src/main/resources/colorSchemes/RescriptDarcula.xml`
4. `src/main/resources/colorSchemes/RescriptDefault.xml`
