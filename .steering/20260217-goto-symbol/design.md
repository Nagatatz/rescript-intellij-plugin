# Design: Go to Symbol

## 実装アプローチ

`ChooseByNameContributorEx` を実装し、既存の PSI ツリーからトップレベル宣言を収集して Go to Symbol ダイアログに提供する。

## 変更するコンポーネント

### 新規ファイル

#### `src/main/kotlin/com/rescript/plugin/navigation/RescriptSymbolContributor.kt`

`ChooseByNameContributorEx` を実装。

```kotlin
class RescriptSymbolContributor : ChooseByNameContributorEx {
    override fun processNames(
        processor: Processor<in String>,
        scope: GlobalSearchScope,
        filter: IdFilter?
    ) {
        // scope 内の全 .res/.resi ファイルを走査
        // RescriptPsiUtils.extractName() で宣言名を収集
        // processor.process(name) で提供
    }

    override fun processElementsWithName(
        name: String,
        processor: Processor<in NavigationItem>,
        parameters: FindSymbolParameters
    ) {
        // name に一致する PSI 要素を検索
        // NavigatablePsiElement として processor に渡す
    }
}
```

主要ロジック:
- `FileTypeIndex.getFiles()` で `.res`/`.resi` ファイルを列挙
- `PsiManager.findFile()` で `RescriptFile` を取得
- PSI ツリーを走査して `NAVIGABLE_TYPES` に一致する要素を収集
- ネストされた `MODULE_DECLARATION` 内の子要素も再帰的に収集

### 変更ファイル

#### `src/main/resources/META-INF/plugin.xml`

```xml
<gotoSymbolContributor
    implementation="com.rescript.plugin.navigation.RescriptSymbolContributor"/>
```

## 影響範囲

- 既存コードへの変更は `plugin.xml` への登録追加のみ
- `RescriptPsiUtils` の既存メソッドを再利用
- 新規パッケージ `navigation` を追加
