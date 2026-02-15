# Requirements: テストカバレッジ拡充（Phase 2）

## 背景

前回のテストカバレッジ拡充（20260215-enhance-test-coverage）により、レクサー（110テスト）、パーサー（38テスト）、ラインインデント（21テスト）の合計169テストが整備された。

しかし、以下のコンポーネントにはテストが存在しない：

- `RescriptSyntaxHighlighter` — トークン→属性マッピング
- `RescriptBraceMatcher` — ブレースペア定義
- `RescriptCommenter` — コメント文字列定義
- `RescriptParserDefinition` — ファクトリメソッド
- `RescriptTokenTypes` — TokenSet の内容検証
- `RescriptFoldingBuilder` — プレースホルダーテキスト
- `RescriptColorSettingsPage` — 設定ページ属性

また、既存テストでも以下のエッジケースがカバーされていない：

- レクサーの文字列エスケープ境界ケース
- テンプレートリテラルの異常系
- コメントのネスト深度の極端なケース
- `findLastSignificantToken` の追加パターン

さらに、`build.gradle.kts` に残っている `test-local` ディレクトリ参照のクリーンアップも必要。

## 目的

1. 未テストコンポーネントにユニットテストを追加し、リグレッション検出能力を向上させる
2. 既存テストのエッジケースカバレッジを拡充する
3. `build.gradle.kts` の不要な `test-local` 参照を削除する

## スコープ

### 対象

**新規テストファイル（IntelliJ Platform 不要・JUnit 4 単体テスト）：**

1. **RescriptSyntaxHighlighterTest** — トークン→TextAttributesKey マッピングの網羅検証
   - KEYWORDS TokenSet の全トークンが KEYWORD 属性を返すこと
   - OPERATORS TokenSet の全トークンが OPERATOR 属性を返すこと
   - 文字列・数値・コメント・JSX トークンの属性マッピング
   - 未知トークンに対して空配列を返すこと
   - `getHighlightingLexer()` が `RescriptLexer` を返すこと

2. **RescriptBraceMatcherTest** — ブレースペアの検証
   - 3ペア（`{}`、`[]`、`()`）が定義されていること
   - `{}` が structural、`[]` / `()` が non-structural であること
   - `isPairedBracesAllowedBeforeType` が常に `true` を返すこと
   - `getCodeConstructStart` がオフセットをそのまま返すこと

3. **RescriptCommenterTest** — コメント文字列の検証
   - `lineCommentPrefix` が `//` であること
   - `blockCommentPrefix` / `blockCommentSuffix` が `/*` / `*/` であること
   - `commentedBlockCommentPrefix` / `commentedBlockCommentSuffix` が `null` であること

4. **RescriptParserDefinitionTest** — ファクトリメソッドの検証
   - `createLexer(null)` が `RescriptLexer` を返すこと
   - `createParser(null)` が `RescriptParser` を返すこと
   - `getFileNodeType()` が正しい `IFileElementType` を返すこと
   - `getCommentTokens()` が `SINGLE_COMMENT` と `MULTI_COMMENT` を含むこと
   - `getStringLiteralElements()` が `STRING_VALUE` を含むこと

5. **RescriptTokenTypesTest** — TokenSet 内容の検証
   - `KEYWORDS` の要素数が正しいこと
   - `OPERATORS` の要素数が正しいこと
   - `TOP_LEVEL_KEYWORDS` が7宣言キーワードを含むこと
   - `COMMENTS` が2要素であること
   - `NUMBERS` が2要素であること
   - `STRINGS` が3要素であること

6. **RescriptColorSettingsPageTest** — 設定ページの検証
   - `displayName` が "ReScript" であること
   - `icon` が non-null であること
   - `attributeDescriptors` が空でないこと
   - `colorDescriptors` が空であること
   - `demoText` がサンプルコード含むこと
   - `additionalHighlightingTagToDescriptorMap` が空でないこと

7. **RescriptFoldingBuilderTest** — プレースホルダーテキストの検証
   - `isCollapsedByDefault` が常に `false` を返すこと

**既存テストファイルへのエッジケース追加：**

8. **RescriptLexerTest に追加** — レクサーエッジケース
   - 閉じられていない文字列（EOF で終了）
   - 閉じられていないテンプレートリテラル
   - テンプレート内の `$` 単体（`${` なし）
   - テンプレート内のネストされたブレース
   - 深くネストしたブロックコメント
   - 閉じられていないネストコメント
   - 数値リテラルの先頭ゼロ
   - 小数点後に数字がないフロート（`1.`）
   - レクサーの再利用（reset 後の動作）

9. **RescriptLineIndentProviderTest に追加** — 追加パターン
   - コメントのみの行で `null` を返すこと
   - ブロックコメントのみの行で `null` を返すこと
   - 文字列内にトークンが含まれる場合の挙動
   - 複雑なコード行での最終トークン検出

**ビルド設定クリーンアップ：**

10. `build.gradle.kts` から `test-local` の `sourceSets` 参照を削除

### 対象外

- LSP 統合テスト
- UI テスト（`runIde` 依存）
- `BasePlatformTestCase` を必要とするテスト（folding の `buildFoldRegions` 等）
- Structure View テスト（PSI ツリー依存）

## 受け入れ条件

1. `./gradlew test` で全テスト PASS すること
2. 新規テストクラスが7つ追加されていること
3. 既存テストにエッジケースが追加されていること
4. `build.gradle.kts` から `test-local` 参照が削除されていること
5. `./gradlew buildPlugin` でビルド成功すること

## 制約事項

- IntelliJ Platform ランタイムが不要なテストのみ追加する（`BasePlatformTestCase` 非依存）
- 既存テストの削除・変更は行わず、追加のみ
- テストクラスのパッケージは対応するプロダクションコードのパッケージに合わせる
