# Design: Go to Related

## 新規ファイル

- `src/main/kotlin/com/rescript/plugin/navigation/RescriptGotoRelatedProvider.kt`

## 変更ファイル

- `src/main/resources/META-INF/plugin.xml` — `<gotoRelatedProvider>` 登録

## 実装方針

### RescriptGotoRelatedProvider

- `GotoRelatedProvider` を実装
- `getItems(context: DataContext)` で現在のファイルを取得
- `.res` ファイルの場合:
  - 同ディレクトリの `.resi` ファイルを検索 (`parent.findChild`)
  - 生成 JS: プロジェクトルートの `lib/js/` 配下で同じ相対パス + `.bs.js` / `.mjs` を検索
- `.resi` ファイルの場合:
  - 同ディレクトリの `.res` ファイルを検索
- 各ファイルを `GotoRelatedItem(psiFile, "ReScript")` として返す

### plugin.xml

```xml
<gotoRelatedProvider
    implementation="com.rescript.plugin.navigation.RescriptGotoRelatedProvider"/>
```
