# Design: Editor & Intention Tests

## 方針

- IntelliJ Platform テストフレームワーク不要の純粋ユニットテスト
- PSI 要素は `java.lang.reflect.Proxy` でスタブ化 (既存テストパターンに準拠)
- `NavigatablePsiElement` スタブは `node.elementType`, `text`, `children`, `prevSibling`, `nextSibling`, `parent` をサポート

## テスト設計

### 1. RescriptBreadcrumbsProviderTest
- `getLanguages()` → `RescriptLanguage` を含む配列
- `acceptElement()` → NAVIGABLE_TYPES に含まれる要素 → true、含まれない要素 → false
- `getElementInfo()` → `RescriptPsiUtils.extractName` 相当の結果

### 2. RescriptStructureViewElementTest
- `getAlphaSortKey()` → extractName の結果
- `getPresentation()` → PresentationData の presentableText 検証
- `getChildren()` → NAVIGABLE_TYPES の子のみフィルタ

### 3. RescriptPostfixTemplateProviderTest
- テンプレート数 = 7
- 各テンプレートの key 検証 (switch, pipe, log, some, ok, error, ignore)
- `isTerminalSymbol('.')` → true, 他文字 → false

### 4-11. 既存テスト改善
- 各ファイルの requirements に記載のエッジケース・メソッドを追加
