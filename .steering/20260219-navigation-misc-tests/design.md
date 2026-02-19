# Design: Navigation & Misc Tests

## 方針
- PSI が必要なテストは JDK Proxy による stub か、`BasePlatformTestCase` の fixture を使用
- 純粋なユーティリティ関数のテストは JUnit 単体テストで直接テスト
- 既存テストのスタイル（stub helpers パターン）を踏襲

## 1. RescriptRawJsInjectorTest.kt (新規)
- `getInjectionRange` は private だが、リフレクションまたは同等ロジックのテストで検証
  - `"content"` → TextRange(1, 8)
  - `""` (空文字列) → null
  - テンプレート文字列 (引用符なし) → TextRange(0, length)
- `isInsideRawBlock` は private だが PSI ツリー走査のため、リフレクション経由で検証
  - PERCENT → RAW → LPAREN → STRING パターンで true
  - パターン不一致で false

## 2. RescriptQualifiedNameProviderTest.kt (改善)
- `findDeclarationElement`: PSI stub で LET_DECLARATION 要素を作り、直接要素を返す
- `buildModulePath`: MODULE_DECLARATION 内にネストされた要素で検証
- `getQualifiedName`: RescriptFile stub + 宣言要素で完全修飾名を検証

## 3. RescriptGotoRelatedProviderTest.kt (改善)
- `.mjs` JS 出力バリアント
- `.js` JS 出力バリアント
- サブディレクトリ (`src/components/Foo.res` → `lib/js/src/components/Foo.bs.js`)

## 4. RescriptImportOptimizerTest.kt (改善)
- `extractModulePath` で `child.node == null` ケース (null を返す node の子要素)
- `supports()` が RescriptFile で true を返すことの検証

## 5. RescriptPasteAsJsonActionTest.kt (改善)
- `getActionUpdateThread() == ActionUpdateThread.BGT`
- `escapeString` でバックスラッシュ (`\`) を含む文字列の検証

## 6. RescriptTestFrameworkDetectorTest.kt (改善)
- `detectFromConfigFiles` のテスト追加（vitest.config.ts, jest.config.js 等）
- config ファイルが見つからない場合 null を返す

## 7. RescriptTestLocatorTest.kt (改善)
- `compiledJsToResPath` で lib prefix ありだが JS suffix なしのケース → null
- PROTOCOL_ID 定数の検証
- INSTANCE シングルトンの検証

## 8. RescriptCompilerStatusWidgetFactoryTest.kt (改善)
- `formatTooltip` で error status + errorCount=0, warningCount>0 のケース
- `formatText` で error status + errorCount=0 のケース
- WIDGET_ID 定数の検証
