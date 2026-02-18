# Design: //#region 折りたたみ

## アーキテクチャ
- `RescriptCustomFoldingProvider` (`CustomFoldingProvider`) - `//#region` パターン認識
- `RescriptFoldingBuilder` - `FoldingBuilderEx` → `CustomFoldingBuilder` に移行
  - `buildFoldRegions` → `buildLanguageFoldRegions`
  - `getPlaceholderText` → `getLanguagePlaceholderText`
  - `isCollapsedByDefault` → `isRegionCollapsedByDefault`
  - `isCustomFoldingCandidate()` 追加

## 登録
- `plugin.xml` に `<customFoldingProvider>` を追加
