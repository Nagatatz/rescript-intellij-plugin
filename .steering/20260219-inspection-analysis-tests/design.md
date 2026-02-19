# Inspection & Analysis テストカバレッジ向上 — Design

## 設計方針

- IDE 依存を避けるため、PSI スタブベースの純粋なユニットテストを作成する
- 既存テストの `RescriptImportOptimizerTest` のスタブパターン（`SimpleStubElement` + `stubProxy` + `stubAstNode`）を再利用する
- Inspection クラスはインスタンス化してメタ情報を検証する（`LocalInspectionTool` / `GlobalInspectionTool` のメソッド）
- QuickFix は `familyName` を検証する

## テスト設計

### 1. RescriptEmptyModuleInspectionTest

```
- getDisplayName / getGroupDisplayName / getShortName のデフォルト値
- RemoveEmptyModuleQuickFix.familyName == "Remove empty module"
- DECLARATION_TYPES セットが全7種を含む（private companion → Reflection で検証、または hasDeclarationChildren を間接的にテスト不要 → メタ情報のみ）
```

### 2. RescriptDuplicateOpenInspectionTest

```
- getDisplayName / getGroupDisplayName / getShortName のデフォルト値
- RemoveDuplicateOpenQuickFix.familyName == "Remove duplicate open"
```

### 3. RescriptMissingConfigInspectionTest

```
- getDisplayName / getGroupDisplayName / getShortName のデフォルト値
- クラスが LocalInspectionTool を継承していることの確認
```

### 4. RescriptReanalyzeAnnotatorTest 改善

```
- parseJsonOutput: filePath 完全一致マッチ (file == filePath)
- parseJsonOutput: 逆方向マッチ (filePath.endsWith(file))
- parseAllDiagnostics: range.size < 4 の場合スキップ
- doAnnotate(null) → null
```

### 5. RescriptReanalyzeQuickFixTest 改善

```
- isAvailable() が true を返す（PrefixWithUnderscore, RemoveUnused）
- findWordStart: 空文字列 → 0
- findWordEnd: offset が text.length を超える → text.length
- findWordEnd: アポストロフィ含みの識別子 (x')
```

### 6. RescriptUnusedCodeInspectionTest 改善

```
- getDisplayName のデフォルト値
- getGroupDisplayName のデフォルト値
```

### 7. RescriptDependencyAnalyzerTest 改善

```
- extractModulePath: PSI スタブ (OPEN + UIDENT + DOT + UIDENT) → "Belt.Array"
- extractModulePath: 単一モジュール (OPEN + UIDENT) → "Utils"
- extractModulePath: 子要素なし → ""
- extractModulePath: INCLUDE + UIDENT → "Utils"
```

## PSI スタブの再利用

`RescriptImportOptimizerTest` の `SimpleStubElement`, `stubAstNode`, `stubProxy` をパターンとして各テストで再利用する。各テストファイルに必要最小限のスタブヘルパーを定義する。
