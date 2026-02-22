# Navigation Bar Model (#47) — 設計

## アプローチ

`StructureAwareNavBarModelExtension` を継承する。この抽象クラスは Structure View モデルから自動でナビゲーション階層を構築するため、最小限のオーバーライドで実装できる。

## 実装クラス

### `RescriptStructureAwareNavbar`

- `getLanguage()` — `RescriptLanguage` を返す
- `getPresentableText(Object)` — `RescriptPsiUtils.extractName()` で宣言名を返す。`NAVIGABLE_TYPES` 外の要素には `null` を返す
- `getIcon(Object)` — `RescriptPsiUtils.getIcon()` でアイコンを返す

## 再利用する既存コード

| ユーティリティ | 用途 |
|---------------|------|
| `RescriptPsiUtils.extractName()` | 宣言名の取得 |
| `RescriptPsiUtils.getIcon()` | 宣言タイプ別アイコン |
| `RescriptPsiUtils.NAVIGABLE_TYPES` | 対象要素タイプの判定 |
| `RescriptStructureViewFactory` | Structure View モデル（navbar が自動利用） |

## Extension Point 登録

`plugin.xml` の breadcrumbs 登録の直後に `<navbar>` を追加:

```xml
<navbar implementation="com.rescript.plugin.navbar.RescriptStructureAwareNavbar"/>
```
