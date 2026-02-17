# Import Optimizer 設計

## 新規ファイル

- `src/main/kotlin/com/rescript/plugin/imports/RescriptImportOptimizer.kt`
- `src/test/kotlin/com/rescript/plugin/imports/RescriptImportOptimizerTest.kt`

## 変更ファイル

- `src/main/resources/META-INF/plugin.xml` — `<lang.importOptimizer>` 登録

## 実装設計

### RescriptImportOptimizer

`com.intellij.lang.ImportOptimizer` を実装:

- `supports(file)`: `file is RescriptFile` で ReScript ファイルのみサポート
- `processFile(file)`: `CollectingInfoRunnable` を返す
  - **READ フェーズ**: PSI ツリーから `OPEN_STATEMENT` を収集、モジュール名を抽出、重複を検出
  - **WRITE フェーズ**: 重複要素を逆順で削除（オフセットのずれを防止）
  - `getUserNotificationInfo()`: `"Removed N duplicate open statement(s)"`

### モジュール名抽出

既存 `RescriptDuplicateOpenInspection.extractModulePath()` と同じロジック:
- `OPEN_STATEMENT` ノードの子を走査
- `OPEN` キーワード以降の `UIDENT` と `DOT` トークンを連結

### 重複判定

- 同一モジュール名の2回目以降の出現を削除対象とする
- ファイルトップレベルのスコープのみを対象（ネストモジュール内は対象外）

### plugin.xml 登録

```xml
<lang.importOptimizer language="ReScript"
                      implementationClass="com.rescript.plugin.imports.RescriptImportOptimizer"/>
```

## テスト

- `supports()` の条件テスト
- 重複 open の検出・削除ロジックのテスト
- 削除数の通知メッセージのテスト
