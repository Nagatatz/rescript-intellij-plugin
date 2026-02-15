# Tasklist: テストカバレッジの拡充

## Phase 1: test-local の保護

- [x] `.gitignore` に `src/test-local/` を追加（誤コミット防止）

## Phase 2: レクサーテスト拡充

`RescriptLexerTest.kt` に以下のテストを追加:

- [x] キーワード認識テスト（`let`, `type`, `module`, `external`, `open`, `include`, `exception`, `switch`, `if`, `else`, `for`, `while`, `try`, `catch`, `async`, `await`）
- [x] キーワード演算子テスト（`mod`, `land`, `lor`）
- [x] ビルトインテスト（`unit`, `ref`, `raise`）
- [x] 整数リテラルテスト（10進、16進、8進、2進、アンダースコア区切り）
- [x] 浮動小数点リテラルテスト（通常、指数表記、16進浮動小数点）
- [x] 文字リテラルテスト（通常、エスケープシーケンス）
- [x] 文字列リテラルテスト（通常、エスケープ付き、空文字列）
- [x] コメントテスト（単行、ブロック、ネスト、doc コメント、コメント内文字列）
- [x] テンプレートリテラルテスト（単純、補間付き、複数補間）
- [x] 演算子テスト（算術、比較、論理、パイプ・アロー、その他）
- [x] アノテーションテスト（単純、ドット付き、引数付き、ダブル）
- [x] 識別子テスト（小文字、大文字、アンダースコア、型引数、ポリバリアント）
- [x] レクサー状態遷移の境界ケーステスト（AFTER_IDENT、IN_LOWER_DECLARATION、改行リセット）

## Phase 3: パーサーテスト新規作成

- [x] `RescriptParserTest.kt` を作成し、テスト基盤（ヘルパーメソッド）を実装
- [x] `let` 宣言テスト（単純、`rec` 付き）
- [x] `type` 宣言テスト（単純、`rec` 付き）
- [x] `module` 宣言テスト（通常、`type` 付き、`rec` 付き）
- [x] `external` 宣言テスト
- [x] `open` / `include` ステートメントテスト
- [x] `exception` 宣言テスト
- [x] アノテーションテスト（`@name`、`@dotted.name`、`@name(args)`）
- [x] 複合ケーステスト（複数宣言連続、アノテーション + 宣言、ネストブレース内のキーワード非誤認）

## Phase 4: 最終確認

- [x] `./gradlew test` で全テスト PASS を確認（175テスト、0 failures）
- [x] `./gradlew buildPlugin` でビルド成功を確認
- [x] Git コミット
