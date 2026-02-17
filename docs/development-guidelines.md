# 開発ガイドライン

ReScript IntelliJ Plugin プロジェクトのコーディング規約と開発プロセス。

> **前提**: プロジェクト概要、ビルドコマンド、アーキテクチャ、Git コミット規約、ステアリングワークフローは [CLAUDE.md](../CLAUDE.md) を参照。本ドキュメントはそれらと重複しない補足規約を定める。

## 1. コーディング規約

### 1.1 Kotlin

- **フォーマッター**: ktlint (v1.6.0) に準拠する。CI で `ktlintCheck` が実行される
- **最大行長**: 120 文字（`.editorconfig` で定義）
- **命名規則**:

| 対象 | 規則 | 例 |
|------|------|-----|
| クラス / オブジェクト | PascalCase | `RescriptSyntaxHighlighter` |
| 関数 / プロパティ | camelCase | `getHighlightingLexer()` |
| 定数 (`const val`) | UPPER_SNAKE_CASE | `RESCRIPT_EXTENSIONS` |
| `IElementType` トークン | UPPER_SNAKE_CASE | `LET`, `JSX_TAG_NAME` |
| `TextAttributesKey` | UPPER_SNAKE_CASE | `KEYWORD`, `MARKUP_TAG` |

- **パッケージ構成**: `com.rescript.plugin.*`。新しい機能は既存サブパッケージ（`lang/`, `highlight/`, `lsp/` 等）に配置する
- **`@JvmField`**: `IElementType` や `TextAttributesKey` のフィールドには `@JvmField` を付与する（JFlex 生成コードからのアクセスに必要）

### 1.2 JFlex (Rescript.flex)

- **トークン追加時の同期**: `Rescript.flex` にトークンを追加したら、`RescriptTokenTypes.kt` にも対応する `IElementType` を追加すること
- **レクサー状態**: 新しい状態を追加する場合は、既存の状態遷移パターン（`yybegin(STATE)`）に従う
- **生成ファイル**: `RescriptFlexLexer.java` は自動生成。直接編集しない

### 1.3 plugin.xml

- 新しい extension point を追加する場合は、既存の登録パターンに従いコメントで用途を示す
- 登録順序: Language → FileType → Lexer/Parser → Highlighting → Folding/Commenter → LSP

## 2. テスト規約

### 2.1 テスト配置

- テストは `src/test/kotlin/com/rescript/plugin/` 以下にソースと同じパッケージ構造で配置する
- テストクラス名: `<対象クラス名>Test`（例: `RescriptLexerTest`）

### 2.2 テスト構造

- **Arrange-Act-Assert** パターンを使用する
- テストメソッド名はバッククォートで振る舞いを記述する:

```kotlin
@Test
fun `JSX opening tag - div`() {
    // Arrange
    val input = "<div>"
    // Act
    val tokens = tokenize(input)
    // Assert
    assertEquals(expected, tokens)
}
```

### 2.3 テスト観点

- 正常系（基本パス）
- エッジケース（空入力、境界値、状態遷移の境界）
- エラーケース（不正入力）
- レクサーテストでは、類似構文の区別（例: `<` が JSX タグか比較演算子か）を重点的に検証する

### 2.4 ビルド検証

```bash
# ビルド + テスト
./gradlew clean buildPlugin

# テストのみ
./gradlew test

# ktlint チェック
./gradlew ktlintCheck
```

## 3. エラーハンドリング

- IntelliJ Platform API のエラーは `com.intellij.openapi.diagnostic.Logger` でログ出力する
- LSP サーバーの起動失敗は `ExecutionException` で明確なエラーメッセージを返す
- ユーザーに表示するメッセージには解決手順を含める（例: `npm install @rescript/language-server`）

## 4. コメント規約

- **自明なコードにはコメントを付けない**
- 複雑なレクサー状態遷移や、通常と異なる実装判断をした箇所には「なぜそうしたか」をコメントで説明する
- KDoc はパブリック API や拡張ポイントの実装クラスに付与する

## 5. 品質チェック

実装完了後、以下を確認する:

- [ ] `./gradlew clean buildPlugin` がエラーなしで完了
- [ ] `./gradlew test` が全件パス
- [ ] `./gradlew ktlintCheck` が違反なし
- [ ] 新規トークン追加時: `Rescript.flex` と `RescriptTokenTypes.kt` が同期している
- [ ] 新規 extension point 追加時: `plugin.xml` に登録されている
- [ ] CLAUDE.md の Git コミット規約に従ったコミットメッセージ

### 5.1 Qodana 静的解析

GitHub Actions で [Qodana](https://www.jetbrains.com/qodana/) による静的解析が `main` ブランチへの push / PR 時に自動実行される（`.github/workflows/qodana_code_quality.yml`）。

**Qodana 指摘の確認・修正には Claude Code スキルを使用する:**

```
/fix-qodana              # 最新の Qodana 実行結果を取得して分析
/fix-qodana <run_id>     # 指定の run ID の結果を分析
```

スキルは GitHub API 経由で指摘事項を取得し、重要度別の分類・修正方針・具体的なコード修正案を含む修正計画を生成する。

**修正方針の判断基準:**

| 方針 | 適用条件 |
|------|---------|
| コード修正 | deprecated API の置換、冗長コードの簡略化、型の修正 |
| `@Suppress` | ツール名・ファイル名の capitalization、JFlex レクサーから間接参照されるシンボルの unused 警告 |
| 設定除外 | プロジェクト全体で一律に除外すべきルール（`qodana.yaml` の `exclude` に追加） |

## 6. レビュー観点

### コードレビューチェックリスト

- [ ] ktlint に準拠しているか
- [ ] 命名規則が統一されているか
- [ ] IntelliJ Platform API の使い方が適切か（非推奨 API を使っていないか）
- [ ] テストが追加されているか
- [ ] `plugin.xml` の登録が正しいか
- [ ] LSP に影響する変更の場合、LSP サーバーとの互換性を確認したか
