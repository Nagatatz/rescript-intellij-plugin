# Navigation Bar Model (#47) — 要件定義

## 概要

エディタ上部のナビゲーションバーに ReScript ファイル構造（let, type, module 等のトップレベル宣言）を表示する機能を実装する。

## 受け入れ条件

1. `.res` / `.resi` ファイルを開いた際、ナビゲーションバーにカーソル位置のトップレベル宣言階層が表示される
2. 表示される宣言タイプ: `let`, `type`, `module`, `external`, `exception`（`NAVIGABLE_TYPES` と一致）
3. 各宣言に適切なアイコンが表示される
4. 各宣言に名前がテキストとして表示される
5. ネストされたモジュール内の宣言で、モジュール階層が正しく表示される

## 技術的制約

- `StructureAwareNavBarModelExtension` を継承して実装する
- 既存の `RescriptPsiUtils` ユーティリティを再利用する
- Structure View モデルを自動的に利用する（`getLeafElement`, `processChildren`, `getParent` は親クラスが提供）

## スコープ外

- ナビゲーションバーからのクリックによるファイル間ジャンプ（LSP 側の機能）
- カスタムナビゲーションモデル（Structure View ベースで十分）
