# Design: テストカバレッジの拡充

## 実装アプローチ

### 方針

- レクサー・パーサーは IntelliJ Platform のコンテナなしで直接インスタンス化できるため、JUnit 4 の単体テストで完結させる
- 既存の `RescriptLexerTest.kt` はそのまま維持し、新規テストクラスを追加する形で拡充
- `test-local` のテストは `src/test/` に移動し、`build.gradle.kts` から `test-local` の参照を削除する
- パーサーテストは `PsiBuilder` のモック不要。`PsiBuilderFactory` + `RescriptLexer` を使って直接テストする

### テスト手法

#### レクサーテスト

既存テストと同じパターンを踏襲:

```kotlin
private fun tokenize(input: String): List<Pair<IElementType, String>>
private fun tokenizeNoWs(input: String): List<Pair<IElementType, String>>
private fun tokenTypesNoWs(input: String): List<IElementType>
```

#### パーサーテスト

IntelliJ Platform の `ParsingTestCase` は IDE 環境が必要なため使用しない。代わりに `PsiBuilder` を直接利用:

```kotlin
private fun parseToTree(input: String): ASTNode {
    val lexer = RescriptLexer()
    val builder = PsiBuilderFactory.getInstance()
        .createBuilder(RescriptParserDefinition(), lexer, input)
    return RescriptParser().parse(RescriptParserDefinition.FILE, builder)
}
```

ただし `PsiBuilderFactory.getInstance()` は IntelliJ Platform ランタイムが必要。テストフレームワーク `TestFrameworkType.Platform` はすでに依存に含まれているが、実行には `BasePlatformTestCase` の継承が必要になる可能性がある。

**代替案:** パーサーテストが Platform 依存で複雑になる場合は、レクサーテストの拡充を優先し、パーサーテストは `BasePlatformTestCase` を使った統合テストとして別ファイルで実装する。

## ファイル構成

### 変更するファイル

| ファイル | 変更内容 |
|---------|---------|
| `src/test/kotlin/.../RescriptLexerTest.kt` | テストメソッド追加（既存テストは維持） |
| `build.gradle.kts` | `test-local` の `sourceSets` 参照を削除 |

### 新規作成するファイル

| ファイル | 内容 |
|---------|------|
| `src/test/kotlin/.../RescriptLexerBlogContentTest.kt` | `test-local` から移動（内容は同一） |
| `src/test/kotlin/.../RescriptParserTest.kt` | パーサーのトップレベル宣言認識テスト |

### 削除するファイル

| ファイル | 理由 |
|---------|------|
| `src/test-local/kotlin/.../RescriptLexerBlogContentTest.kt` | `src/test/` に移動のため |
| `src/test-local/` ディレクトリ | 空になるため削除 |

## テストカテゴリ詳細

### 1. レクサーテスト追加分（`RescriptLexerTest.kt` に追加）

#### 1.1 キーワード認識
- 主要キーワード: `let`, `type`, `module`, `external`, `open`, `include`, `exception`, `switch`, `if`, `else`, `for`, `while`, `try`, `catch`, `async`, `await`
- キーワード演算子: `mod`, `land`, `lor`
- ビルトイン: `unit`, `ref`, `raise`, `option`, `Some`, `None`

#### 1.2 リテラル
- 整数: `42`, `0xFF`, `0o77`, `0b1010`, `1_000_000`
- 浮動小数点: `3.14`, `1e10`, `0xAp3`
- 文字リテラル: `'a'`, `'\n'`, `'\x41'`
- 文字列: `"hello"`, エスケープ付き `"he\"llo"`, 空文字列 `""`
- ブーリアン: `true`, `false`（既存の test-local テストと重複しないもの）

#### 1.3 コメント
- 単行コメント: `// comment`
- ブロックコメント: `/* comment */`
- ネストブロックコメント: `/* outer /* inner */ still comment */`
- Doc コメント: `/** doc */`
- コメント内の文字列: `/* "not a string" */`

#### 1.4 テンプレートリテラル
- 単純: `` `hello` ``
- 補間付き: `` `hello ${name}` ``
- 複数補間: `` `${a} and ${b}` ``

#### 1.5 演算子
- 算術: `+`, `-`, `*`, `/`, `%`, `+.`, `-.`, `*.`, `/.`
- 比較: `==`, `===`, `!=`, `!==`, `<`, `>`, `<=`
- 論理: `&&`, `||`
- パイプ・アロー: `|>`, `=>`, `->`, `<-`
- その他: `++`, `::`, `:=`, `:>`

#### 1.6 アノテーション
- 単純: `@module`
- ドット付き: `@react.component`
- 引数付き: `@module("fs")`
- ダブル: `@@deriving`

#### 1.7 識別子・特殊トークン
- 小文字識別子: `myVar`, `_private`
- 大文字識別子: `MyModule`, `React`
- アンダースコア: `_`
- 型引数: `'a`, `'myType`
- ポリバリアント: `#Success`, `#error`

#### 1.8 レクサー状態遷移の境界ケース
- 識別子の後の `<` が JSX にならないこと（AFTER_IDENT 状態）
- 改行後の `<` が JSX になること（AFTER_IDENT → INITIAL リセット）
- `let`/`external` 後の宣言名認識（IN_LOWER_DECLARATION 状態）
- JSX タグ名後のスペースで通常状態に戻ること

### 2. パーサーテスト（`RescriptParserTest.kt`）

#### 2.1 基本宣言
- `let x = 1` → LET_DECLARATION
- `let rec f = ...` → LET_DECLARATION（rec 付き）
- `type t = int` → TYPE_DECLARATION
- `type rec t = ...` → TYPE_DECLARATION（rec 付き）
- `module M = { ... }` → MODULE_DECLARATION
- `module type S = { ... }` → MODULE_DECLARATION（type 付き）
- `module rec M = { ... }` → MODULE_DECLARATION（rec 付き）
- `external f: int => int = "f"` → EXTERNAL_DECLARATION
- `open Belt` → OPEN_STATEMENT
- `include Common` → INCLUDE_STATEMENT
- `exception NotFound` → EXCEPTION_DECLARATION

#### 2.2 アノテーション
- `@react.component` → ANNOTATION
- `@@deriving(show)` → ANNOTATION × 2? or ANNOTATION × 1（実装に依存）

#### 2.3 複合ケース
- 複数宣言の連続パース
- アノテーション + 宣言の組み合わせ
- ネストブレース内の宣言キーワードがトップレベルとして誤認識されないこと

## 影響範囲

- テストコードの追加のみ。プロダクションコードへの変更なし
- `build.gradle.kts` の `sourceSets` 変更は `test-local` の除去のみ（テスト実行に影響なし）
- CI の `./gradlew test` でテストが増加するが、レクサー・パーサーの単体テストは高速（ms 単位）

## リスク

| リスク | 対策 |
|-------|------|
| パーサーテストで `PsiBuilderFactory` が利用不可 | `BasePlatformTestCase` を使った統合テストに切り替える。または AST ノード検証の代わりにレクサーレベルのトークン列テストで代替 |
| `test-local` 削除後に未知の依存が発生 | `./gradlew test` で全テスト PASS を確認してから `test-local` を削除 |
