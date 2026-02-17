# Design: Gutter Run Icons

## アーキテクチャ

### 新規ファイル

- `src/main/kotlin/com/rescript/plugin/run/RescriptRunLineMarkerContributor.kt`
  - `RunLineMarkerContributor` を継承
  - `getInfo(element)` でガターアイコンの表示条件を判定

### 変更ファイル

- `src/main/resources/META-INF/plugin.xml`
  - `<runLineMarkerContributor>` を追加

### テストファイル

- `src/test/kotlin/com/rescript/plugin/run/RescriptRunLineMarkerContributorTest.kt`

## 実装詳細

### RescriptRunLineMarkerContributor

`RunLineMarkerContributor` を継承し、`getInfo(element)` を実装:

1. **リーフ要素チェック**: `element.node?.elementType` が `LET`, `TYPE`, `MODULE` のいずれかであることを確認
2. **ReScript ファイルチェック**: `element.containingFile` が `RescriptFile` であることを確認
3. **rescript.json 存在チェック**: プロジェクトのベースディレクトリに `rescript.json` が存在するか確認
4. **最初のトップレベル宣言チェック**: 要素の親が `LET_DECLARATION`, `TYPE_DECLARATION`, `MODULE_DECLARATION` のいずれかで、かつファイル内で最初のトップレベル宣言であることを確認
5. 条件を満たせば `withExecutorActions(AllIcons.RunConfigurations.TestState.Run)` を返す

### plugin.xml 登録

```xml
<runLineMarkerContributor language="ReScript"
                           implementationClass="com.rescript.plugin.run.RescriptRunLineMarkerContributor"/>
```

### テスト省略理由

`RunLineMarkerContributor` は PSI 要素に基づく条件判定のためユニットテスト可能。
ただし `getInfo()` の返り値は `RunLineMarkerContributor.Info` であり、実行環境依存の `ExecutorAction.getActions()` を呼ぶため、条件判定ロジックを分離してテストする。
