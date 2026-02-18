# Design: Paste as JSON.t

## アーキテクチャ
- `RescriptPasteAsJsonAction` (`AnAction`) - クリップボードから JSON を読み取り変換
- `convertJsonToRescript()` - `JsonElement` を再帰的に `JSON.t` 構文へ変換
- `isLikelyJson()` - テキストが JSON かどうかの簡易判定

## 依存関係
- `com.google.gson.JsonParser` (IntelliJ Platform バンドル)
- `CopyPasteManager` (クリップボードアクセス)

## 登録
- `plugin.xml` の `<actions>` セクションに `<action>` を追加
- `EditorPopupMenu` グループに配置
