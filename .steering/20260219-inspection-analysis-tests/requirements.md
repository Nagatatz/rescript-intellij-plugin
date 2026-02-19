# Inspection & Analysis テストカバレッジ向上 — Requirements

## 目的

Inspection & Analysis 関連クラスのテストカバレッジを向上させる。新規テスト 3 ファイルの作成と、既存テスト 4 ファイルの改善を行う。

## 対象

### 新規テスト (3 ファイル)

1. **RescriptEmptyModuleInspectionTest.kt** — `inspection/RescriptEmptyModuleInspection`
   - メタ情報の検証
   - QuickFix familyName の検証
   - `hasDeclarationChildren` ロジック（DECLARATION_TYPES セット）の検証

2. **RescriptDuplicateOpenInspectionTest.kt** — `inspection/RescriptDuplicateOpenInspection`
   - メタ情報の検証
   - QuickFix familyName の検証
   - 重複検出ロジックの検証

3. **RescriptMissingConfigInspectionTest.kt** — `inspection/RescriptMissingConfigInspection`
   - メタ情報の検証
   - 検証対象ファイル名リスト（rescript.json / bsconfig.json）の確認

### 既存テスト改善 (4 ファイル)

4. **RescriptReanalyzeAnnotatorTest.kt** — 追加テスト:
   - `parseJsonOutput` 完全パスマッチ / 逆方向マッチ
   - `parseAllDiagnostics` range.size < 4 のスキップ
   - `doAnnotate(null)` が null を返す検証

5. **RescriptReanalyzeQuickFixTest.kt** — 追加テスト:
   - `isAvailable()` が常に true を返す
   - `findWordStart` 空文字列の処理
   - `findWordEnd` 末尾超過の処理
   - アポストロフィ含みの識別子

6. **RescriptUnusedCodeInspectionTest.kt** — 追加テスト:
   - `isGraphNeeded` のテスト（既存あり、追加で `getDisplayName` / `getGroupDisplayName`）

7. **RescriptDependencyAnalyzerTest.kt** — 追加テスト:
   - `extractModulePath` PSI スタブ (OPEN + UIDENT + DOT + UIDENT)
   - 単一モジュール
   - 子要素なし

## 受け入れ条件

- 全テストが `./gradlew test` で通過する
- 新規テストクラスが正しいパッケージに配置されている
- 既存テストの既存テストケースが壊れない
