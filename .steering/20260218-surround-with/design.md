# Design: Surround With

## アーキテクチャ

### 新規ファイル

- `src/main/kotlin/com/rescript/plugin/surround/RescriptSurroundDescriptor.kt`
  - `SurroundDescriptor` の実装
  - 4 つの `Surrounder` クラスをトップレベルで定義

### 変更ファイル

- `src/main/resources/META-INF/plugin.xml`
  - `<lang.surroundDescriptor>` を追加

### テストファイル

- `src/test/kotlin/com/rescript/plugin/surround/RescriptSurroundDescriptorTest.kt`

## 設計詳細

### RescriptSurroundDescriptor

`SurroundDescriptor` を実装し、以下のメソッドを提供:

- `getElementsToSurround(file, startOffset, endOffset)`: 選択範囲内の PSI 要素を返す（ReScript ファイルのみ）
- `getSurrounders()`: 4 つの Surrounder インスタンスの配列を返す
- `isExclusive()`: `false`（他の SurroundDescriptor と共存）

### Surrounder 実装

各 Surrounder は `com.intellij.lang.surroundWith.Surrounder` を実装:

1. **RescriptIfSurrounder**
   - テンプレート: `if (condition) {\n  <selection>\n}`
   - カーソル: `condition` を選択状態に

2. **RescriptSwitchSurrounder**
   - テンプレート: `switch expr {\n| _ => <selection>\n}`
   - カーソル: `expr` を選択状態に

3. **RescriptTrySurrounder**
   - テンプレート: `try {\n  <selection>\n} catch {\n| exn => ()\n}`
   - カーソル: `()` を選択状態に

4. **RescriptBlockSurrounder**
   - テンプレート: `{\n  <selection>\n}`
   - カーソル: ブロック末尾（閉じ波括弧の前）

### plugin.xml 登録

```xml
<lang.surroundDescriptor language="ReScript"
                         implementationClass="com.rescript.plugin.surround.RescriptSurroundDescriptor"/>
```

## テスト方針

- `RescriptSurroundDescriptor` の `getSurrounders()` が 4 つの Surrounder を返すことを確認
- `isExclusive()` が `false` を返すことを確認
- 各 Surrounder の `getTemplateDescription()` が正しい名前を返すことを確認
- 各 Surrounder の `isApplicable()` が基本的に `true` を返すことを確認
