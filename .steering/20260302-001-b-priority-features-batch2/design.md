# B 優先度機能一括実装 (バッチ2) — 設計

## 各機能の設計概要

### #56 Framework Detector
- `FrameworkDetector("rescript")` を継承
- `rescript.json` の存在で ReScript フレームワークを検出
- `rescript-json.xml` に登録 (JSON プラグイン依存)

### #52 Code Rearranger
- `Rearranger<ArrangementEntry>` を実装
- 固定並べ替え順序: open/include → type → exception → module → external → let
- PSI の `RescriptElementTypes` を使用して宣言を分類

### #103 変更可能性の診断
- `LocalInspectionTool` を継承
- `let name = ref(...)` パターンを検出し、再代入 `:=` がなければ警告
- Quick Fix で `ref(value)` → `value` に変換

### #102 スタイルリンティング
- `LocalInspectionTool` を継承
- 3 ルール: 冗長ブール式、Belt.* 使用、ブール値 switch
- 各ルールに Quick Fix を提供

### #97 filter+map チェーン変換
- `PsiElementBaseIntentionAction` を継承
- `->Array.filter(pred)->Array.map(f)` を `->Array.filterMap(...)` に変換

### #85 型注釈追加
- `PsiElementBaseIntentionAction` を継承
- `let name = expr` を検出し LSP hover で型を取得
- `let name: <type> = expr` に変換

### #109 PPX 可視化
- `InlayHintsProvider<NoSettings>` を実装
- 静的マッピングで PPX アノテーションの効果をインライン表示

### #99 型ミスマッチ差分表示
- `RescriptTypeDiffComputer` を新規作成
- 型文字列をトークン分割し差分セグメントを計算
- `RescriptErrorLensRenderer.paint()` を拡張して差分部分を強調色で描画
