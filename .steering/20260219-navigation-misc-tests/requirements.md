# Requirements: Navigation & Misc Tests

## 概要
Navigation および Misc カテゴリの既存テストのカバレッジ改善と、新規テストファイルの追加を行う。

## 対象

### 新規テスト (1 ファイル)
1. **RescriptRawJsInjectorTest.kt** — `getInjectionRange` テスト（`"content"` → TextRange(1,8), `""` → null, テンプレート文字列 → フルレンジ）、`isInsideRawBlock` PSI スタブ

### 既存テスト改善 (7 ファイル)
2. **RescriptQualifiedNameProviderTest.kt** — `findDeclarationElement`/`buildModulePath` PSI スタブ, `getQualifiedName`
3. **RescriptGotoRelatedProviderTest.kt** — `.mjs`/`.js` JS 出力バリアント, サブディレクトリ
4. **RescriptImportOptimizerTest.kt** — `extractModulePath` child.node==null, `supports()` 追加
5. **RescriptPasteAsJsonActionTest.kt** — `getActionUpdateThread()==BGT`, `escapeString` バックスラッシュ
6. **RescriptTestFrameworkDetectorTest.kt** — 未カバー分岐追加
7. **RescriptTestLocatorTest.kt** — 未カバー分岐追加
8. **RescriptCompilerStatusWidgetFactoryTest.kt** — 未カバー分岐追加

## 受け入れ条件
- 全テストが `./gradlew test` で通過すること
- 各テストファイルで未カバーだった分岐がカバーされること
- ビルド成功 (`./gradlew buildPlugin`)
